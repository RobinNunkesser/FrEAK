/* ************************************************************************* *
 *                                                                           *
 *        Copyright (c) 2004 Peter Cappello  <cappello@cs.ucsb.edu>          *
 *                                                                           *
 *    Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the          *
 *  "Software"), to deal in the Software without restriction, including      *
 *  without limitation the rights to use, copy, modify, merge, publish,      *
 *  distribute, sublicense, and/or sell copies of the Software, and to       *
 *  permit persons to whom the Software is furnished to do so, subject to    *
 *  the following conditions:                                                *
 *                                                                           *
 *    The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.          *
 *                                                                           *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF       *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.   *
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY     *
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,     *
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE        *
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                   *
 *                                                                           *
 * ************************************************************************* */

/**
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.admin.AdministrableHsp;
import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.StateConfig; // for serialization.
import edu.ucsb.cs.jicos.services.commands.*;
import edu.ucsb.cs.jicos.services.external.services.CollectorManager;
import edu.ucsb.cs.jicos.services.external.services.TaskExternal;
import edu.ucsb.cs.jicos.utilities.*;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.registry.Registry;
import java.sql.*; // for serialization.
import java.util.*;


/**
 * Hsp.java - Hosting Service Provider
 * @author  Peter Cappello
 * @version 1
 */
public final class Hsp extends ServiceImpl implements ClientToHsp, AdministrableHsp
{        
    // constants
    public static String SERVICE_NAME = "Jicos";
    static final int COMPUTE        = 0;
    static final int GETRESULT      = 1;
    static final int LOGIN          = 2;
    static final int LOGOUT         = 3;
    static final int SETCOMPUTATION = 4;            
    
    // set relationship between Command objects & the Departments that process them
    private static final Class[][] command2DepartmentArray =
    {
        // ASAP_DEPARTMENT - done within RMI thread.
        {
            AddServiceTaskStats.class, // by root TaskServer
            GetServiceName.class,      // by TaskServer & Host
            GetTaskServer.class,       // by Host
            PutResult.class, // by: 1. rootTaskServer when sink task completes
                                 // 2. Host when there is a ComputeException            
            Ready.class,	// by saveState
            UpdateTaskServerState.class // by TaskServer
        }
    };
    private static final CommandSynchronous SHUTDOWN_COMMAND = new Shutdown();
    private final static RemoteExceptionHandler remoteExceptionHandler = new
                                                  JicosRemoteExceptionHandler();    
    /** 
     * Declare/construct Department objects
     */
    private Department[] departments = { ServiceImpl.ASAP_DEPARTMENT };
    
    // TaskServer network management    
    private int nTaskServers;
    private int nHosts;
    private TopologyManager topologyManager = new TopologyManager( proxyManager() );
    private List taskServerTree = new ArrayList();
    private Service rootTaskServer;
    private TaskServerProxy rootTaskServerProxy;
    //private Host2TaskServer host2TaskServer = new Host2TaskServer();
    private Map service2TaskServerDataMap = new HashMap();
    private Map externalServiceProviderMap = new HashMap();
    
    // Client-oriented information
    private ClientProxy clientProxy; // used to logoff crashed client
    private Qu clientQ = new Qu(); // of ClientProfile objects (waiting clients)
    private Qu loginQ = new Qu();  // of login requests
    private ProtocolChecker protocolChecker = new ProtocolChecker();
    private SessionHSP session;
    private short computationId;
    private TaskId currentTask; // null ==> compute NOT in progress
    private Object value; // value of computation or Exception
    private ResultTable resultTable = new ResultTable();
    private ServiceTaskStats hspTaskStats = new ServiceTaskStats(serviceName());
    private Invoice invoice;
    private int pendingResults;
    private boolean isJicosException;
    
//    private ExternalRequestProcessor externalRequestProcessor;

    // For serialization
    // Count of waiting TaskServers after a load/save state command is sent.
    private int nWaitingTaskServers;
    // Save/Load State datasource object.
    private StateConfig stateConfig = null;
    // A temporary flag for indicating the state of the state load/save operation.
    private boolean loadingState=false;

    public Hsp() throws RemoteException, AlreadyBoundException
    {
        super ( command2DepartmentArray );
        super.setService( this );
        super.setDepartments( departments );
        
        CollectorManager.startCollectors( this );
        
        LogManager.getLogger( this ).log( LogManager.INFO, "Hsp has been constructed." );
        
        // construct root TaskServer
        TaskServer root = new TaskServer( this, Task.class );
        
        // bind TaskServer into RmiRegistry
        Registry registry = RegistrationManager.locateRegistry();
        registry.bind( TaskServer.SERVICE_NAME, root );
    }
    
    public synchronized void addServiceTaskStats ( List serviceTaskStats )
    {                
        hspTaskStats.addAll ( serviceTaskStats );
        notify();
    }
    
    private void check4Exception( Object value ) 
            throws ComputeException, JicosException
    {
        String message = "";
        
        if ( value instanceof Exception // ComputeException
             || isJicosException     // Internal Exception
           )
        {
            try
            {
                logout(); // removes client's remaining tasks.
            }
            catch ( Exception ignore ) 
            {
                message += "Logout failed. \n";
            }
            Exception exception = (Exception) value;
            System.out.println("Hsp: check4Exception: Exception detected.");
            exception.printStackTrace();
            
            // set message
            message += ((Throwable) exception).toString();
            
            if ( isJicosException )
            {
                isJicosException = false;
                JicosException e = new JicosException( message );                
                e.setStackTrace( ((Throwable) exception).getStackTrace() );
                throw e;
            }
            else
            {
                ComputeException e = new ComputeException( message );
                e.setStackTrace( ((Throwable) exception).getStackTrace() );
                throw e;
            }            
        }
    }

    public synchronized Object compute( Task task ) 
           throws ComputeException, JicosException, RemoteException
    {        
        String message = protocolChecker.check( ProtocolChecker.COMPUTE );
        if ( message != null )
        {
            // violates the method protocol
            throw new IllegalStateException( message );
        }
        pendingResults++;
        spawnTask( task );
        currentTask = task.getTaskId();                                
        try
        {
            wait(); // wait for value to be reported.
        }
        catch( InterruptedException ignore ) {}
        
        check4Exception( value );
        currentTask = null;
        return value;
    }      
    
    /** An internal exception was detected. Log out client; continue.
     */
    public void exceptionHandler( Exception exception )
    {
        // log out client; propagate exception to client via putResult
        //putResult( null, exception );
        Result result = new Result( null, exception, 0 );
        putResult( null, result );
    }
    
    public int getPendingResults() { return pendingResults; }
    
    public ProtocolChecker getProtocolChecker() { return protocolChecker; }
    
    /* This method is not synchronized for 2 reasons: It does not access Hsp 
     * state, and synchronizing it locks out putResult.
     *
     * !!** Fix: if client invokes getResult > setComputation, it is undetected.
     *         Do it the old way: Store setComputations; move to Results, as
     *            they are obtained. If a getResult occurs w/ empty setComputas,
     *            throw exception.
     *
     * It may be good to have a client proxy ensure protocol of Hsp method 
     * invocations.
     */
    public Result getResult() 
           throws ComputeException, JicosException, RemoteException
    {
	String msg;
    		
	LogManager.getLogger().log( LogManager.CONFIG, "Checking protocol." );
    	    
        String message = protocolChecker.check( ProtocolChecker.GETRESULT );
        if ( message != null )
        {
	    LogManager.getLogger().log( LogManager.WARNING, message );
            // violates the method protocol
            throw new IllegalStateException( message );
        }
        
	LogManager.getLogger().log( LogManager.DEBUG, "Getting result..." );
        Result result = resultTable.remove();

	LogManager.getLogger().config( "Critical Path Time: "
			    + String.valueOf( result.getCriticalPathTime() ) );
        	
        Object value = result.getValue();
        check4Exception( value );
        return result;
    }
    
    public synchronized SessionInfo getSessionInfo()
    {
        return ( session == null ) ? null : session.getSessionInfo();
    }
    
    public HspState getState()
    {
        return new HspState( serviceName(), clientQ, nHosts, nTaskServers );
    }
     
    private void initClient( ServiceName serviceName, 
                             ClientProfile clientProfile, 
                             Environment environment
                           )
    {
        String message = protocolChecker.check( ProtocolChecker.LOGIN );
        if ( message != null )
        {
            // violates the method protocol
            throw new IllegalStateException( message );
        }
        if ( environment == null )
        {
            message = "Login error: Environment parameter may not be null.";
            throw new IllegalArgumentException( message );
        }
        
        /* !! must modify protocolChecker to allow login by another client:
         * if not in LOGGED_OUT state, enqueue this login.
         * On logout, get next client in login queue; dequeue and process.
         */
        
        currentTask = null;
        value = null;
        resultTable.clear();
        hspTaskStats.clear();
        invoice = null;        
        session = new SessionHSP( environment );                
        clientProxy = new ClientProxy( serviceName, this, remoteExceptionHandler);
        if ( rootTaskServer != null )
        {
            //System.out.println("Hsp.initClient: sending LoginClient to rootTaskServer.");
            Command command = new LoginClient( session.getSessionInfo() );
            rootTaskServerProxy.execute( command );
        }
        else
        {
            /* !! Hsp should not advertise itself to clients until it has a 
             * TaskServer.
             *!! When done in Jini, fix this.
             * OR queue login; release when there is a TaskServer w/ >= 1 host
             */           
            System.out.println("Hsp: login: rootTaskServer: == null");
        }
    }
    
    public boolean isComplete( ResultId resultId )
    {
        String message = protocolChecker.check( ProtocolChecker.IS_COMPLETE );
        if ( message != null )
        {
            // violates the method protocol
            throw new IllegalStateException( message );
        }
        return ( resultTable.containsKey( resultId ) ) ? true : false;
    }

    /**
     * login Client.
    */
    public void login( ServiceName serviceName, 
                       ClientProfile clientProfile, 
                       Environment environment
                     )
    {
        // enqueue login request; if none are waiting proceed with this request.
        synchronized( this )
        {
            clientQ.add( clientProfile );
            loginQ.add( serviceName );
            if ( loginQ.size() <= 1 )
            {
                initClient( serviceName, clientProfile, environment );
                return;
            }
        }
        
        // wait my turn to login
        synchronized( serviceName )
        {            
            try
            {
                    serviceName.wait(); // when notifed, it is my turn
            }
            catch( InterruptedException ignore ) {}                 
        }
        
        // It now is my turn
        synchronized( this )
        {
            initClient( serviceName, clientProfile, environment );
            return;
        }        
    }
    
    // logout client      
    public synchronized Invoice logout() throws RemoteException
    {
        String message = protocolChecker.check( ProtocolChecker.LOGOUT );
        if ( message != null )
        {
            // violates the method protocol
            throw new IllegalStateException( message );
        }
                
        if ( invoice != null )
        {
            // ComputeException occurred: Invoice was generated. Use it.
            session = null;
            return invoice;
        }
        
        /* !! ?? Is this still necessary? What about when ComputeException 
         * occurs, and application catches it, and logs out? If this is the
         * only need for this, Client2HSP could handle this in its logout
         * method.
         */
        if ( session == null )
        {
            return null; // client already logged out
        } 
        
        long stopTime = System.currentTimeMillis();      
        Command command = new LogoutClient();
        rootTaskServerProxy.execute( command );
        
        // wait for Logout's callback (addServiceTaskStats) to complete & notify
        try
        {
            wait();
        }
        catch ( InterruptedException ignore ) {}
             
        long startTime = session.getStartTime();
        session = null;                        
        value = null;
        resultTable.clear();
        
        clientProxy.kill();
        clientProxy = null; // make available for gc
        clientQ.remove();
        
        loginQ.remove(); // remove my login request from loginQ
        
        // notify waiting login request, if any
        if ( loginQ.size() > 0 )
        {
            ServiceName serviceName = (ServiceName) loginQ.get();
            synchronized( serviceName )
            {
                serviceName.notify(); // notify next login request to proceed
            }
        }
        
        String debugMessage = "Logged out client.";
        LogManager.getLogger( this ).fine( debugMessage );
        return new Invoice ( startTime, stopTime, hspTaskStats );
    }        
    
    /**
     * Put a Result into resultTable to be retrieved by client via getResult.
     *
     * Regarding an Exception result: 
     * I believe that synchronizing this method prevents multiple hosts
     * from invoking it concurrently. This is the desired behavior; I 
     * want only the 1st exception to be reported & log out client.
     */
    public synchronized void putResult( TaskId taskId, Result result )
    {                
        if ( taskId == null )
        {
            isJicosException = true; // JicosException caught
        }        
        
        if ( 
             currentTask != null // client invoked compute method 
             && ( 
                  taskId == null // Internal Exception is being reported
                  || taskId.computationEquals( currentTask ) 
                )             
           ) 
        {
            // a compute method was in progress
            value = result.getValue();
            notify(); // waiting compute thread
        }
        else
        {
            ResultId key = new ResultId( taskId );
            resultTable.put( key, result );
        }
        pendingResults--;
    }
    
    /* Hsp constructor invokes this method: 1st TaskServer to register is NOT 
     * external, and therefore is suitable to serve as root TaskServer.
     */
    public synchronized TaskServerInfo registerTaskServer( ServiceName serviceName, Class taskClass )
    {       
        // pre-conditions
        assert serviceName != null;
        assert taskClass != null;
        
        LogManager.getLogger(this).log(LogManager.INFO, "Registering: " + serviceName.toStringWithSpace() );
        
        TaskServerServiceInfo taskServerServiceInfo = new TaskServerServiceInfo( serviceName, taskClass );
        
        // add TaskServer to ToplogyManager's view of network; set its point;
        // inform existing TaskServers of their new neighbor
        TaskServerServiceInfo[] neighbors = topologyManager.add( taskServerServiceInfo );
        
        // construct TaskServerProxy                
        Service taskServer = serviceName.service();             
        TaskServerProxy taskServerProxy = new TaskServerProxy( taskServer, this, remoteExceptionHandler );
        addProxy( serviceName, taskServerProxy );
        
        // add task server data record to table
        service2TaskServerDataMap.put( taskServer, new TaskServerData( serviceName ) );
             
        if ( rootTaskServer == null ) 
        {
            // 1st TaskServer to register with Hsp. Make it the root.
            rootTaskServer = taskServer;
            
            // cache rootTaskServer's proxy
            rootTaskServerProxy = taskServerProxy;
            
            // notifyAll Hosts waiting for a TaskServer (in getTaskServer)
            // !! I think this now is unnecessary: Hosts no longer register w/ Hsp
            //notifyAll();
        }
        nTaskServers++;
        
        if ( TaskExternal.class.isAssignableFrom( taskClass ) )
        {
            // Task objects handled by this TaskServer extend TaskExternal
            // taskserver isa TaskServerExternal
            externalServiceProviderMap.put( taskClass, taskServer );
            
            // send externalServiceProviderMap update to all taskservers
            if ( nTaskServers > 1 )
            {
                Command command = new UpdateExternalServiceProviderMap( taskClass, taskServer, taskServer );
                rootTaskServerProxy.execute( command );
            }     
        }
        return new TaskServerInfo( neighbors, externalServiceProviderMap );
    }
    
    Service rootTaskServer() { return rootTaskServer; }
    
    public synchronized ResultId setComputation( Task task )
    {
        String message = protocolChecker.check( ProtocolChecker.SETCOMPUTATION );
        if ( message != null )
        {
            // violates the method protocol
            throw new IllegalStateException( message );
        }
        pendingResults++;
        spawnTask( task );
        return new ResultId( task.getTaskId() );
    }
    
    private void spawnTask( Task task ) //throws Exception
    {
        task.init( session.getStartTime(), computationId++ );
        
        // set task's successorTaskServer reference
        task.setTaskServer( rootTaskServer );
        task.taskServerServiceName( ((ServiceImpl) rootTaskServer).serviceName() );
        Command command = new Spawn( task );
        rootTaskServerProxy.execute( command );
    }
    
    public void updateTaskServerState( Service taskServer, int nHosts )
    {
        TaskServerData taskServerData = (TaskServerData) service2TaskServerDataMap.get( taskServer );
        taskServerData.setNHosts( nHosts );
    }

  
 
    /* (non-Javadoc)
     * @see edu.ucsb.cs.jicos.foundation.Administrable#getChildren()
     */
    public Administrable[] getChildren()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ucsb.cs.jicos.foundation.Administrable#getCurrentState()
     */
    public CurrentState getCurrentState()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * Since the Hsp has a TaskServer, when it shuts down, the Hsp shuts down.
     * @see edu.ucsb.cs.jicos.foundation.Administrable#shutdown()
     */
    public void shutdown()
    {
        LogManager.getLogger( this ).log( LogManager.INFO, "Shutting down root taskserver." );
        try
        {
            rootTaskServerProxy.execute( SHUTDOWN_COMMAND, remoteExceptionHandler );
        }
        catch ( Exception ignore ) {}
    }

    /*  @see edu.ucsb.cs.jicos.admin.Administrable#echo(java.lang.String)  */
    public String echo( String request ) throws RemoteException
    {
        return( request );
    }

    
    /** This method is used to initiate saving the state of the current
     * computation (i.e HSP and TaskServer state) to the specified datasource
     * described by StateConfig. This save operation should be used in
     * conjunction with the loadState() method and saves the HSP and TaskServer
     * state by saving all unassigned and currently in-process tasks. 
     * This method may be used by an administrator save the state of a system for
     * maintenance. 
     * This method causes all TaskServers to pause before serializing their
     * tasks. This causes some delay -- smaller delays for computations with lots
     * of quick computations, longer delays for computations with few, long 
     * computations. Eventually, this could serve as a component of a 
     * backup system by periodically saving system state.
     * @param sconf The StateConfig object that describes the datasource.
     */
    public synchronized void saveState( StateConfig sconf ) {
        stateConfig = sconf;

	/* This is a trivial implementation of a protocol
	 * for determining which state the system is in
	 * (loading or saving) after all TaskServers have
	 * sent a ReadyState(). */
        loadingState = false;
        
        // sets a variable nWaitingTaskServers= to nTaskServers
        nWaitingTaskServers = nTaskServers;
        System.out.println( "HSP:saveState:nTaskServers=" + nTaskServers );
        // sends pause to all taskservers
        Command cmdPause = new Pause();
        try {
            System.out.println( "HSP:Broadcasting pause." );
            broadcast( cmdPause, this ); //throws Exception 
        } catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }
    
    /** This method is used to initiate the restoring of state from a datasource
     * described by StateConfig. This restore operation should be used in
     * conjunction with the saveState() method and restores the HSP and TaskServer
     * state to that corresponding to the described saveState(). The restore 
     * operation clears the current set of tasks, so any running computation 
     * will in effect be aborted.
     * This method may be used by an administrator bringing a system down for
     * maintenance. In the current JICOS system, the loadState command must be
     * resubmitted by the client. This should and will likely change in the 
     * future when putResult() is moved to the client, at which point a save/load
     * operation could be completed without disruption of the client.
     * @param sconf The StateConfig object that describes the datasource.
     */    
    public synchronized void loadState( StateConfig sconf ) {
        stateConfig = sconf;
        loadingState = true;
        
        // sets a variable nWaitingTaskServers= to nTaskServers
        nWaitingTaskServers = nTaskServers;
        System.out.println( "HSP:loadState:nTaskServers=" + nTaskServers );
        // sends pause to all taskservers
        Command cmdPause = new Pause();
        try {
            System.out.println( "HSP:Broadcasting pause." );
            broadcast( cmdPause, this ); //throws Exception 
        } catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
        String message = protocolChecker.check( ProtocolChecker.SETCOMPUTATION );
        System.out.println( "HSP:Set to computation state." );
    }
    
    /** This method implements the core of the load/state commands.
     * It loads or saves dependent on the loadingState flag (which
     * needs a rewrite).
     */    
    public synchronized void readyState() {
        // decrements nWaitingTaskServers
        nWaitingTaskServers--;
        System.out.println( "HSP:readyState:nWaitingTaskServers=" +
            nWaitingTaskServers );
       
        // if nWaitingTaskServers == 0, send saveState Command to all TS's
        if( nWaitingTaskServers==0 ) {
            if( loadingState ) {                
                System.out.println( "HSP:readyState:Loading HSP state with ID:" +
                    stateConfig.id );
                try {
                    Class.forName(stateConfig.driver);
                    Connection con = DriverManager.getConnection(stateConfig.dsn, stateConfig.user, stateConfig.pass);            
                    PreparedStatement ps = 
                        con.prepareStatement("SELECT data FROM hsp_state WHERE id=?");
                    ps.setString(1, stateConfig.id );
                    ResultSet rs = ps.executeQuery();
                    if( rs != null && rs.next() ) {
                        byte[] bytes = rs.getBytes(1);
                        Vector dataToLoad = (Vector)((java.rmi.MarshalledObject)bytesToObject( bytes )).get();
                        ResultTable resTable = (ResultTable)dataToLoad.get(0);
                        /** ResultTable has a notify/wait relationship in place, so we need to
                         *handle pending waits, otherwise our notify is never sent. */
                        resultTable.clear();    // Avoid notify & ready for a new task restore
                        Iterator iter = resTable.values().iterator();
                        while( iter.hasNext() ) {
                            Result result = (Result)iter.next();
                            resultTable.put( result.getId(), result );
                        }
                        pendingResults = ((Integer)dataToLoad.get(1)).intValue();
                        rs.close();
                    } else {
                        // Handle inability to load tasks here...
                        System.err.println( "HSP: Could not load data from DB with ID="
                            + stateConfig.id );
                    }
                    ps.close();
                }
                catch (Exception ignore) {
                    ignore.printStackTrace();
                }
                Command cmdLoadState = new LoadState( stateConfig );
                try {
                    System.out.println( "HSP:readyState:Broadcasting load command." );
                    broadcast( cmdLoadState, this );
                } catch( Exception ignore )
                {
                    ignore.printStackTrace();
                }
            } else {
                // Save the state
                Command cmdSaveState = new SaveState( stateConfig );                
                // Clear stored hsp_state
                System.out.println( "TaskServer:Saving state to: " + stateConfig.dsn );
                try {
                    Class.forName(stateConfig.driver);
                } catch (ClassNotFoundException cnfe) {
                    System.err.println("Couldn't find driver class. Make sure jar is in classpath:");
                    cnfe.printStackTrace();
                    return;
                }
                try {
                    Connection con = DriverManager.getConnection(stateConfig.dsn, stateConfig.user, stateConfig.pass);            
                    PreparedStatement ps = 
                        con.prepareStatement("DELETE FROM hsp_state WHERE id=?");
                    ps.setString(1, stateConfig.id);
                    ps.executeUpdate();
                    ps.close();
                    // Clear stored app environment                    
                    ps = con.prepareStatement("DELETE FROM application_state WHERE id=?");
                    ps.setString(1, stateConfig.id);
                    ps.executeUpdate();
                    ps.close();
                    
                    // Update hsp state
                    ps =
                        con.prepareStatement("INSERT INTO hsp_state (id, data) VALUES (?, ?)");
                    ps.setString(1, stateConfig.id );
            
                    System.out.println( "Storing state to database with ID: " +
                        stateConfig.id );

                    Vector dataToStore = new Vector();
                    dataToStore.add( resultTable );
                    dataToStore.add( new Integer( pendingResults ));
                    java.rmi.MarshalledObject marshalledData = 
                        new java.rmi.MarshalledObject(dataToStore);               
                    ps.setBytes(2, objectToBytes( marshalledData ));
                    int n = ps.executeUpdate();
                    ps.close();

                    // Update application state
                    ps =
                        con.prepareStatement("INSERT INTO application_state (id, data) VALUES (?, ?)");
                    ps.setString(1, stateConfig.id );
            
                    System.out.println( "Storing state to database with ID: " +
                        stateConfig.id );
                    dataToStore = new Vector();

                    // No state to save if no session exists:
                    if( session == null ) {
                       System.err.println( 
                       "No state to save. Sending saveState command anyway to wake taskservers." );
                    } else {
                       dataToStore.add( session.getSessionInfo().getEnvironment() );
                    }
                    marshalledData = 
                        new java.rmi.MarshalledObject(dataToStore);               
                    ps.setBytes(2, objectToBytes( marshalledData ));
                    n = ps.executeUpdate();
                    ps.close();

                    System.out.println( "HSP:readyState:Broadcasting save command." );
                    broadcast( cmdSaveState, this );
                    System.out.println( "HSP:readyState:state stored." );
                } catch( Exception ignore )
                {
                    ignore.printStackTrace();
                }
            }
        }
    }
    
    private static void start( String[] args ) throws RemoteException,
			AlreadyBoundException 
    {
        // Load the hsp properties.
		Property.loadProperties(args);
		Property.load(Hsp.class);

		Service jicos = new Hsp();
		Registry registry = RegistrationManager.locateRegistry();
		registry.bind(SERVICE_NAME, jicos);       

		if (args.length == 0) {
			return;
		}
        
        // construct hosts
        Service taskServer = ((Hsp) jicos).rootTaskServer();
        int $Hosts = Integer.parseInt(args[0]);		
		for (int i = 0; i < $Hosts; i++) 
        {
			new Host( taskServer, false, 1 );
		}
    }
    
     public static void main(String[] args) throws RemoteException,
			AlreadyBoundException 
     {

		if ((1 == args.length) && ("-help".equals(args[0]))) {
			System.out.println("Usage: <java> " + Hsp.class.getName()
					                               + " [#hosts]" );
			System.exit(0);
		}

		System.setSecurityManager(new RMISecurityManager());
     	
        start( args );
	}
}
