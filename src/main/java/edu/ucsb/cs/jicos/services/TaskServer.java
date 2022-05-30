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
 *
 * @version 1.0
 * @author  Peter Cappello
 */

/*
 * TaskServer.java
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.admin.AdministrableTaskServer;
import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.commands.*;
import edu.ucsb.cs.jicos.utilities.*;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;
import java.sql.*;  //For Serialization:


public class TaskServer extends ServiceImpl implements AdministrableTaskServer
{
    // Constants
    public static final String SERVICE_NAME = "TaskServer";
    private static final Command LOGOUT_COMMAND = new LogoutClient();
    private static final CommandSynchronous SHUTDOWN_COMMAND = new Shutdown();
    private static final int REQUEST_TASKS_LEVEL = 0;    
    public static final RemoteExceptionHandler REMOTE_EXCEPTION_HANDLER = new
                                                  JicosRemoteExceptionHandler();    
    private static final int $NEIGHBORS = 4;    
    
    private final Command REQUEST_TASKS = new RequestTasks( this );
    
    // Map from Command to Department
    private final static Class[][] command2DepartmentArray =
    {
        // ASAP_DEPARTMENT
        {
            AddServiceTaskStats.class,
            BroadcastEvent.class,
            CommandList.class,
            KillTask.class, // issued by 1 of its Hosts or a neighbor TaskServer
            LoadState.class,    // This added for persistence
            LoginClient.class,
            LogoutClient.class,
            Pause.class, // This added for persistence
            ProcessResult.class,
            ReceiveTasks.class, // sent by TaskServer to TaskServer that sent RequestTasks
            RegisterHost.class,
            RegisterNeighborTaskServer.class,
            SaveState.class,    // This added for persistence
            SetArg.class,
            SetShared.class, /* send UpdateShared to my Hosts & neighbor
              TaskServers. Currently O( |S| ), where S is the set of neighbor 
              services. However, it simply adds an UpdatShared to each Service's
              Mail: no wait; no RMI. */
            Shutdown.class,
            Spawn.class,
            UpdateExternalServiceProviderMap.class,
            UpdateShared.class
        },
        // TASK_REQUESTS_DEPARTMENT
        {
            RequestTask.class, // wait until ready Task is available; send originating Host ExecuteTask.
            RequestTasks.class // wait until sufficient ready Task objects are available; send originating TaskServer ReceiveTasks.
        }
    };
    
    /** 
     * declare/construct Department objects
     */    
    // fulfill each Host/TaskServer ready Task request, 1 request at at a time!
    private final static Class[][] command2PriorityMap = 
    {
        {
            RequestTask.class,  // Host requests
            RequestTasks.class // TaskServer requests
        }
    };    
    //private Q q = new PriorityQ( 2, command2PriorityMap );
    private Q q = new PriorityQ( 1, command2PriorityMap );
    private Department taskRequests = new Department ( this, q, 1 );     
    private Department[] departments = 
    {  
        ServiceImpl.ASAP_DEPARTMENT, taskRequests
    };
    private Q taskRequestQ = new PriorityQ( 1, command2PriorityMap );
    private Department requestTaskDepartment = new Department( this, taskRequestQ, 1);
    
    /* Processes INTERNALLY GENERATED ExecuteTask & ProcessResult commands.
     * Hence, it is not included in departments array above.
     */
    private Department internalCommandDepartment = 
                                            new Department( this, new Qu(), 1 );
    private Host internalHost; // internal Host executes Compose Task objects 
    private TaskServerServiceInfo myTaskServerServiceInfo;
    
    private Collection subtree = Collections.synchronizedSet( new HashSet() );
    
    // HSP-oriented attributes
    private Service hsp;
    private HspProxy hspProxy;
    
    // TaskServer-oriented attributes    
    private TaskServer2HspReporter taskServer2HspReporter;
    private Proxy parentTaskServerProxy;
    private Set childrenTaskServerProxies;
    private NeighborManager neighborManager;
    
    private boolean amShuttingDown;
    
    // Host-oriented attributes - currently used for logout
    private HostProxyManager hostProxyManager = new HostProxyManager( this );

    // Session-oriented attributes
    private Session session;
    //  client stats associated with this TaskServer
    private ServiceTaskStats taskServerTaskStats = new
                                             ServiceTaskStats ( serviceName() );
    //  client stats associated with TaskServer tree rooted at this TaskServer
    private ServiceTaskStats hspTaskStats = new ServiceTaskStats(serviceName());
    private JoinCounter clientRegistrationCoordinator = new JoinCounter();
    
    // To support external services
    protected Class myTaskClass = Task.class;
    private Map externalServiceProviderMap = new HashMap();
    
    protected TaskServer( Service hsp, Class myTaskClass ) throws RemoteException
    {
        super ( command2DepartmentArray );
        super.setService( this );
        super.setDepartments( departments );
        
        this.hsp = hsp;
        this.myTaskClass = myTaskClass;
        
        myTaskServerServiceInfo = new TaskServerServiceInfo( serviceName(), myTaskClass );
        
        // Get the Hsp's ServiceName; construct the proxy
        ServiceName serviceName = null;
        CommandSynchronous commandSynchronous = new GetServiceName();
        serviceName = (ServiceName) hsp.executeCommand( this, commandSynchronous );      
        hspProxy = new HspProxy(serviceName, this, REMOTE_EXCEPTION_HANDLER);
        
        // Start the TaskServer2HspReporter
        taskServer2HspReporter = new TaskServer2HspReporter( this );
        
        // Get my initial TaskServer information from my Hsp
        commandSynchronous = new RegisterTaskServer( serviceName(), myTaskClass );
        TaskServerInfo taskServerInfo = (TaskServerInfo) hspProxy.execute( commandSynchronous, REMOTE_EXCEPTION_HANDLER );
        
        // Get neighbor TaskServers
        TaskServerServiceInfo[] taskServerServiceInfoArray = taskServerInfo.taskServerServiceInfoArray();               
        neighborManager = new NeighborManager( taskServerServiceInfoArray, this, REMOTE_EXCEPTION_HANDLER );
        myTaskServerServiceInfo.point( neighborManager.point() );

        parentTaskServerProxy     = neighborManager.getParent();
        childrenTaskServerProxies = neighborManager.getChildren();
        
        externalServiceProviderMap = taskServerInfo.externalServiceProviderMap();
                        
        // Get client session, if any.
        commandSynchronous = new GetSession();
        SessionInfo sessionInfo = (SessionInfo) hspProxy.execute( commandSynchronous, REMOTE_EXCEPTION_HANDLER );
        
        // construct internal Host
        internalHost = new Host ( this, true, 1 );
            
        // construct Session
        session = new Session( this, internalHost );
        if ( sessionInfo != null )
        {
            session.loginClient( sessionInfo );
        }
        
        LogManager.getLogger().log( LogManager.INFO, "TaskServer has been constructed." );
    }
    
    /** Add ServerTreeClientStats from child subtree. Report statistics to
     * parent | Hsp, as appropriate, if this is last child reporting.
     */
    public void addServiceTaskStats ( List serviceTaskStats )
    {
        hspTaskStats.addAll ( serviceTaskStats );
        JoinCounter semaphore = session.getJoinCounter();
        semaphore.decr();
        
        // is this the last child TaskServer reporting back?
        if ( semaphore.isZero() )
        {
            sendStatistics2Parent();
        }
    }
    
    /**
     * If Shared object has new value, multicast UpdateShared to all registered 
     * Host & TaskServer services.
     * Current implementation is O( |S| ), where S is the set of services in the 
     * multicast group. However, it simply adds an UpdatShared Command to each 
     * Service's Mail: no wait; no RMI.
     */
    public void broadcastEvent ( Object event, Service fromService )
    {
        // pre-conditions
        assert event != null;
        assert fromService != null;
        
        SessionInfo sessionInfo = session.getSessionInfo();
        if ( sessionInfo == null )
        {
            return; // client already logged out
        }
        
        Environment environment = sessionInfo.getEnvironment();
        Q eventQ = environment.getEventQ();
        eventQ.add( event );
        Command command = new BroadcastEvent( event, this );
        broadcast( command, fromService );
    }        
    
    /** An internal exception was detected.
     */
    public void exceptionHandler(Exception exception) //throws Exception
    {
        // propagate exception to Hsp.
        Result result = new Result( null, exception, 0 );
        Command command = new PutResult( null, result );
        hspProxy.execute( command );
    }
    
    Map externalServiceProviderMap() { return externalServiceProviderMap; }
    
    public List getSessionTasks()
    {  
        Collection tasks = session.getTasks().values();
        System.out.println( "TaskServer.getSessionTasks: # waiting threads: " + session.getWaiters() );
        return new LinkedList( tasks );
    }
    
    public SessionInfo getState() { return session.getSessionInfo(); }
    
    HostProxyManager getHostProxyManager() { return hostProxyManager; }
    
    TaskServer getParentTaskServer()
    {
        return (TaskServer) parentTaskServerProxy.getService();
    }
    
    public Service hsp() { return hsp; }
    
    HspProxy hspProxy() { return hspProxy; }
    
    Department internalCommandDepartment() { return internalCommandDepartment; }
    
    Service internalHost() { return internalHost; }
    
    /**
     * Issue RequestTasks command only to neighbor TaskServer services that
     *    exist && for which no such request is pending;
     *    Indicate that reqeust is pending for such services.
     *
     * Synchronize on parentRequestTasksPending when changing any pending flag
     */
    void issueRequestTasks()
    { 
        for ( Iterator i = neighborManager.iterator(); i.hasNext(); )
        {
            Proxy proxy = (Proxy) i.next();
            proxy.execute( REQUEST_TASKS );
        }
    }
    
    // !! associated with Chess: Deprecated.
    public void killTask( TaskId taskId, Service sourceService )
    {     
        session.killTask( taskId );
        Command command = new KillTask( taskId, this );
        broadcast( command, sourceService );
        return;
    }
   
    /**
     * LoginClient - 
     *   A new Client has the HSP. Establish its Session.
     *   wait for acknowledgement; issue AcknowledgeClientRegistration
     */
    synchronized public void loginClient ( SessionInfo sessionInfo )
    {
        assert sessionInfo != null;
        
        String debugMessage = "freeMemory: " + Runtime.getRuntime().freeMemory();
        LogManager.getLogger().config( debugMessage );
        
        // clear client data structures that must be cleared > logout completes
        taskServerTaskStats.clear();
        hspTaskStats.clear();                
        
        // set session information for this client
        session.loginClient( sessionInfo );
        
        // propagate login to my Host objects & children TaskServer objects
        LoginClient command = new LoginClient( sessionInfo );
        Service parentTaskServer = null;
        if ( parentTaskServerProxy != null )
        {
            parentTaskServer = parentTaskServerProxy.getService();
        }
        broadcast( command, parentTaskServer, subtree );
        
        // set environment's containing Host
        Environment environment = sessionInfo.getEnvironment();
        environment.host( internalHost );
    }
    
    public void logout() //throws Exception
    {        
        // add HostTaskStats to ServiceTaskStats for this TaskServer
        hostProxyManager.logout( taskServerTaskStats );
        
        // initialize Hsp stats for subtree rooted at this server
        hspTaskStats.add ( taskServerTaskStats );
        
        // request children TaskServer to report their statistics
        JoinCounter semaphore = session.getJoinCounter();
        for ( Iterator i = neighborManager.getChildren().iterator(); i.hasNext(); )
        {
            Proxy childTaskServerProxy = (Proxy) i.next();
            semaphore.incr();
            childTaskServerProxy.execute( LOGOUT_COMMAND );
        }
        
        // if there are no children, report back to parent now.
        if ( semaphore.isZero() )
        {
            sendStatistics2Parent();
        } 
    }
    
    public void processResult ( TaskInfo taskInfo )
    {           
        Service host = taskInfo.host();
        HostProxy hostProxy = (HostProxy) getProxy ( host );
        hostProxy.processResult( taskInfo );
        session.processResult( taskInfo ); // update computation data structures
    }
    
    /**
     * ReceiveTasks - issued by TaskServer in response to RequestTasks
     * Give the Tasks to Session;
     * Set pending request flag to false.
     * Synchronize on parentRequestTasksPending whenever changing any pending flag
     */
    public void receiveTasks( Task[] tasks, Service respondingTaskServer ) 
    {
        session.receiveTasks( tasks );
        TaskServerProxy respondingTaskServerProxy = (TaskServerProxy) 
                                               getProxy( respondingTaskServer );
        respondingTaskServerProxy.receiveTasks();
    }        
    
    // Host constructor invokes this method
    public SessionInfo registerHost ( ServiceName serviceName, boolean isInternalHost )
    {        
        assert serviceName != null;
        
        Service host = serviceName.service();
        // !! Make sure that no service with this ID is registered already.
        // !! If there is, unregister it.
        
        super.register ( host );
        TaskServer taskServer = this;
        if ( isInternalHost )
        {
            // registering Host is an internal Host
            taskServer = null;
        }
        HostProxy hostProxy = new HostProxy( serviceName, taskServer, 
                                             REMOTE_EXCEPTION_HANDLER );
        addProxy( serviceName, hostProxy );     
        hostProxyManager.add ( hostProxy );
        subtree.add( hostProxy );
        
        /* if Host registration is DURING a client's session: return sessionInfo
         */
        SessionInfo sessionInfo = null;
        if ( session != null )
        {
            sessionInfo = session.getSessionInfo();
        }
        
        LogManager.getLogger().log( LogManager.INFO, "Service name: " + serviceName.toStringWithSpace() );
        return sessionInfo;
    }
    
    public void registerTaskServer( TaskServerServiceInfo taskServerServiceInfo,
                                    int direction )
    {        
        TaskServerProxy taskServerProxy = 
                neighborManager.setNeighbor( taskServerServiceInfo, direction );
        addProxy( taskServerServiceInfo, taskServerProxy );
        
        if ( direction == TopologyManager.EXTERNAL )
        {
            // I must be the root TaskServer: adding a TaskServerExternal
            String logMsg = "TaskServerExternal: Task Class: " + taskServerServiceInfo.taskClass();
            LogManager.getLogger(this).log(LogManager.CONFIG, logMsg);           
            subtree.add( taskServerProxy );            
            return;
        }
        
        int myRow = neighborManager.point().row();
        
        if ( direction == TopologyManager.SOUTH ||
             myRow == 0 && direction == TopologyManager.EAST 
           )
        {
            LogManager.getLogger( this ).log( LogManager.INFO, neighborManager.point() + "adding child: " + taskServerServiceInfo.point());
            subtree.add( taskServerProxy );
        }
    }

    /**
     * Host requests a ready Task
     */
    public void requestTask( Service requestingHost )
    {              
        Task task = session.requestTask( requestingHost );
        Command command = new ExecuteTask( task ); 
        Proxy proxy = getProxy( requestingHost );
        if ( proxy == null )
        {
            // Host no longer registered
            System.out.println("TaskServer.requestTask: null HostProxy. Unassigning " + task.getTaskId() );
            session.unassign( task.getTaskId() );
            return; 
        }       
        proxy.execute( command );
        assert null != getProxy( requestingHost );
    }
    
    public Department requestTaskDepartment() { return requestTaskDepartment; }

    /**
     * TaskServer requests ready Tasks
     */
    public void requestTasks( Service requestingTaskServer ) //throws Exception
    {              
        /* factor = # of pending RequestTasks commands + 2.
         * # pending RequestTasks commands is how many other TaskServers are 
         * vying for these tasks. We add 1 for this RequestTasks and 1 more to 
         * save some for this TaskServer.
         */
        //int factor = ((PriorityQ) taskRequests.q()).size( 1 ) + 2;
        int factor = ((PriorityQ) taskRequests.q()).size( REQUEST_TASKS_LEVEL ) + 2;
        Task[] tasks = session.requestTasks( factor );
        Command command = new ReceiveTasks( tasks, this );
        sendCommand( requestingTaskServer, command );
    }
    
     private void sendStatistics2Parent()
     {
         // send statistics to parentTaskServer or Hsp, as appropriate.
         Command command = new AddServiceTaskStats( hspTaskStats );
         Proxy parentTaskServerProxy = neighborManager.getParent();
         if ( parentTaskServerProxy == null )
         {
             // taskserver is root, send to HSP            
             hspProxy.execute( command );
         }
         else
         {
             // taskserver is not the root, send to parentTaskServer
             parentTaskServerProxy.execute( command );            
         }
         session.clear(); // free session objects
         LogManager.getLogger( this ).info( "Task completed." ); // aBp.
     }
    
    /**
     * If Shared object has new value, multicast UpdateShared to all registered 
     * Host & TaskServer services.
     * Current implementation is O( |S| ), where S is the set of services in the 
     * multicast group. However, it simply adds an UpdatShared Command to each 
     * Service's Mail: no wait; no RMI.
     */
    public void setShared ( Shared proposedShared, Service fromService )
    {
        assert proposedShared != null;
        assert fromService != null;
        
        SessionInfo sessionInfo = session.getSessionInfo();
        if ( sessionInfo == null )
        {
            return; // client already logged out
        }
        
        Environment environment = sessionInfo.getEnvironment();
        if ( proposedShared.isNewerThan( environment.getShared() ) )
        {
            environment.setShared( proposedShared );
            Command command = new UpdateShared( proposedShared, this );
            broadcast( command, fromService, subtree );
            if ( fromService != parentTaskServerProxy.getService() )
            {
                parentTaskServerProxy.sendCommand( command );
            }
        }
    }            
    
    public Session session() { return session; }    
    
    RemoteExceptionHandler remoteExceptionHandler()
    {
        return REMOTE_EXCEPTION_HANDLER;
    }
    
    ServiceTaskStats taskServerTaskStats() { return taskServerTaskStats; }
    
    public synchronized void unregisterHost( Service host ) 
    { 
        System.out.println("TaskServer.unregisterHost: " + host );
        HostProxy hostProxy = (HostProxy) removeProxy( host );
        if ( ! amShuttingDown )
        {
            hostProxyManager.remove( hostProxy );
        }
        else
        {
            LogManager.getLogger( this ).log( LogManager.INFO, "Shutting down: host unregistration request ignored." );
        }
        subtree.remove( hostProxy );
        
        // add Task execution statistics of failed Host
        HostTaskStats hostTaskStats = hostProxy.hostTaskStats();
        taskServerTaskStats.add ( hostTaskStats );    
        unregister( host );
        LogManager.getLogger( this ).log( LogManager.INFO, "Completed host unregistration." );
    }

    /** This method pauses the TaskServer. After pausing, the
     * TaskServer sends a Ready command to HSP proxy.
     */
    public void pause() 
    {
        //log ("TaskServer:Pausing" );
        internalCommandDepartment.setPaused( true );
        requestTaskDepartment.setPaused( true );
        Command cmdReady = new Ready();
        hspProxy.execute( cmdReady );
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
     * A quick and dirty shutdown scheme. No statistics gathering is done.
     * The taskserver issues synchronous shutdown commands to its hosts and
     * children taskservers. RemoteExceptions are ignored - seen as equivalent
     * to a successful shutdown (dubious).
     * @see edu.ucsb.cs.jicos.foundation.Administrable#shutdown()
     */
    public void shutdown()
    {
        // put myself in the "shutting down" state so that proxy managers do not
        // remove proxies from ProxyManager objects while this method is 
        // iterating over them.
        shutdownTrue();
        
        // send shutdown command to my hosts
        LogManager.getLogger( this ).log( LogManager.INFO, "Shutting down my hosts" );
        Set hostSet = hostProxyManager.hostSet();
        for ( Iterator i = hostSet.iterator(); i.hasNext(); )
        {
            Proxy hostProxy = (Proxy) i.next();
            try
            {
                hostProxy.execute( SHUTDOWN_COMMAND, REMOTE_EXCEPTION_HANDLER );
            }
            catch ( Exception ignore ) {}
        }
        
        // request that each child TaskServer shutdown
        LogManager.getLogger( this ).log( LogManager.INFO, "Shutting down my children taskservers" );       
        for ( Iterator i = neighborManager.getChildren().iterator(); i.hasNext(); )
        {
            Proxy childTaskServerProxy = (Proxy) i.next();
            try
            {
                childTaskServerProxy.execute( SHUTDOWN_COMMAND, REMOTE_EXCEPTION_HANDLER );
            }
            catch ( Exception ignore ){}
        }
        LogManager.getLogger( this ).log( LogManager.INFO, "Shutting down."  );
        System.exit( 0 );
    }
    
    private synchronized void shutdownTrue() { amShuttingDown = true; }
    

    /*  @see edu.ucsb.cs.jicos.admin.Administrable#echo(java.lang.String)  */
    public String echo( String request ) //throws RemoteException
    {
        return( request );
    }

    
    /** This method implements the TaskServer state restoration.
     * @param stateConfig the datasource to restore from.
     */
    public void loadState( StateConfig stateConfig ) {
        //System.out.println( "TaskServer:Loading state from: " + stateConfig.dsn );        
        //System.out.println( "Loading state from database with ID: " + stateConfig.id +
            //" and server name " + serviceName().stringName() );

        try {
            Class.forName(stateConfig.driver);
            Connection con = DriverManager.getConnection(stateConfig.dsn, stateConfig.user, stateConfig.pass);            
            PreparedStatement ps = 
                con.prepareStatement("SELECT data FROM taskserver_state WHERE id=? AND servername=?");
            ps.setString(1, stateConfig.id );
            ps.setString(2, serviceName().toString() );
            ResultSet rs = ps.executeQuery();
            if( rs != null && rs.next() ) {
                byte[] bytes = rs.getBytes(1);
                Vector dataToLoad = (Vector)((java.rmi.MarshalledObject)bytesToObject( bytes )).get();
                session.setSessionInfo( (SessionInfo)dataToLoad.firstElement() );
                dataToLoad.removeElementAt( 0 );
                session.setTasks( (Map)dataToLoad.firstElement() );
                rs.close();
            } else {
                // Handle inability to load tasks here...
                System.err.println( "TaskServer: Could not load tasks from DB with ID=" +
                    stateConfig.id + " and server name " + serviceName() );
            }
            ps.close();
        }
        catch (Exception ignore) {
            ignore.printStackTrace();
        }
        // After saving state, notify Department so that
        // processor stops "waiting"
        internalCommandDepartment.setPaused( false );
        requestTaskDepartment.setPaused( false );                
    }
    
    /** This method saves the TaskServer state to the specified datasource.
     * Save unassigned tasks, assigned tasks, and completed tasks.
     * @param stateConfig the datasource
     */
    public void saveState( StateConfig stateConfig ) {
        //System.out.println( "TaskServer:Saving state to: " + stateConfig.dsn );
        try {            
            Class.forName(stateConfig.driver);
            Connection con = DriverManager.getConnection(stateConfig.dsn, 
			    stateConfig.user, stateConfig.pass);            
            PreparedStatement ps = 
                con.prepareStatement("DELETE FROM taskserver_state WHERE id=? AND servername=?");
            ps.setString(1,stateConfig.id);
            ps.setString(2,serviceName().toString());
            ps.executeUpdate();
            ps.close();
            ps =
                con.prepareStatement(
		  "INSERT INTO taskserver_state (id, servername, data) VALUES (?, ?, ?)");
            ps.setString(1, stateConfig.id );
            ps.setString(2, serviceName().toString() );
            
            //System.out.println( "Storing state to database with ID: " +
                //stateConfig.id + " and server name " + serviceName().stringName() );
            
            // Since task has a host, and Departments are not serializable,
            // remove them from stored task list.
            Vector dataToStore = new Vector();
            dataToStore.add( session.getSessionInfo() );         
            dataToStore.add( session.getTasks() );
            java.rmi.MarshalledObject storableTaskList = 
                new java.rmi.MarshalledObject(dataToStore);               
            ps.setBytes(3, objectToBytes( storableTaskList ));
            int n = ps.executeUpdate();
            ps.close();
            //System.err.println( "TaskServer: State successfully stored." );
        }
        catch (Exception ignore) {
            ignore.printStackTrace();
        }
        // After saving state, notify Department so that 
        // processor stops "waiting"
        internalCommandDepartment.setPaused( false );
        requestTaskDepartment.setPaused( false );                
    }
    
    public void updateExternalServiceProviderMap(Class taskClass, Service taskServer, Service fromService)
    {
        assert taskClass != null;
        assert taskServer != null;
        assert fromService != null;
               
        LogManager.getLogger( this ).log( LogManager.INFO, "update with Task Class: " + externalServiceProviderMap.get(taskClass) );
        externalServiceProviderMap.put( taskClass, taskServer );
        TaskServerProxy taskServerProxy = new TaskServerProxy( taskServer, this, REMOTE_EXCEPTION_HANDLER );
        
        // propagate the update to my neighbor taskservers
        Command command = new UpdateExternalServiceProviderMap(taskClass, taskServer, this);
        broadcast( command, fromService, childrenTaskServerProxies );
    }

    
    
    public static void start( String[] cmdLine ) throws RemoteException, AccessException, AlreadyBoundException
    {
        if ( null == System.getSecurityManager() )
        {
            System.setSecurityManager( new RMISecurityManager() );
        }

        // Load the taskserver properties.
        Property.loadProperties(cmdLine);
        Property.load(TaskServer.class);
     	
       
        // Get remote reference to Hsp
        String hspMachineDomainName = cmdLine[0];
        HspAgent agent = new HspAgent( hspMachineDomainName );
        Service hsp = agent.getHsp();
             
        TaskServer taskServer = new TaskServer ( hsp, Task.class );
        
        // register task server
        Registry registry = RegistrationManager.locateRegistry();
        registry.bind( SERVICE_NAME, taskServer );       
        
        int $Hosts = 0;
        if ( cmdLine.length > 1 )
        {
            // args[ 1 ] = # of Host objects to be instantiated
            $Hosts = Integer.parseInt( cmdLine[ 1 ] );
        }
        
        // construct hosts
        for ( int i = 0; i < $Hosts; i++ )
        {
            new Host ( taskServer, false, 1 );
        }
    }
    
    public static void main( String[] args ) throws Exception
    {
        if ( args.length == 0 )
        {
            System.out.println("Command line: <Hsp-domain-name> required");
            System.exit( 1 );
        }

        start( args );
    }
}
