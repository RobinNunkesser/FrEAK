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
 *  The object that executes on a machine that is hosting
 *  computational services.
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services; 

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.admin.AdministrableHost;
import edu.ucsb.cs.jicos.services.commands.*;
import edu.ucsb.cs.jicos.services.external.services.*;
import edu.ucsb.cs.jicos.utilities.*;

import java.net.MalformedURLException;

import java.rmi.MarshalledObject;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.NotBoundException;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Host extends ServiceImpl implements AdministrableHost
{
    // constants
    private static final int TASKSERVER_LOOKUP_LIMIT = 5;
    private static final int INTER_LOOKUP_WAITING_PERIOD = 1000;
    private final Command REQUEST_TASK = new RequestTask( this );
    protected final static CommandSynchronous GET_HSP = new GetHsp();
    protected final static RemoteExceptionHandler REMOTE_EXCEPTION_HANDLER = new JicosRemoteExceptionHandler(); 
    
    // set relation between Command objects & their Department objects.
    protected final static Class[][] command2DepartmentArray =
    {        
        {   // asapDepartment
            BroadcastEvent.class, // issued by its TaskServer
            KillTask.class,       // issued by its TaskServer
            LoginClient.class,   // issued by its TaskServer
            Shutdown.class, 
            UpdateShared.class    // issued by its TaskServer
        },        
        {   // computeDepartment
            ExecuteTask.class // issued by its TaskServer
        }
    };
           
    // switches
    private boolean prefetchOn;
    private boolean cacheOn;
    
    // declare/construct Department objects
    private Department computeDepartment = new Department( this, new Qu(), 1 );
    private Department[] departments = 
                             { ServiceImpl.ASAP_DEPARTMENT, computeDepartment };
    
    // Declare neighbor services   
    private HspProxy hspProxy;   
       
    private Service myTaskServer;
    
    private Proxy taskServerProxy;
    private boolean amInternalHost;
    private Department internalCommandDepartment;
    
    // Client-oriented state
    private SessionInfo sessionInfo;
    private long sessionId;
    private Environment environment;
    
    // To support external services
    protected ProxyServiceExternal proxyServiceExternal;
    protected Class myTaskClass = Task.class;
    
    //private TaskIdTrie killedTasks = new TaskIdTrie();
    
    // For debugging purposes.  (Added by Andy Pippin on 14 Aug 2004)
    //
    protected static boolean  displayDebug = true;
    
    /** 
     * @param taskServer TaskServer with which to associate.
     * @param amInternalHost  specify as an internal host.
     * @throws RemoteException This constructor's super invokes remote methods.
     */
    /*protected*/public Host( Service taskServer, boolean amInternalHost, int $processors ) 
              throws RemoteException
    {
        super ( command2DepartmentArray );
        super.setService( this );    
        super.setDepartments( departments ); 
        
        this.myTaskServer = taskServer;
        this.amInternalHost = amInternalHost;
        
        taskServerProxy = new TaskServerProxy( taskServer, this, REMOTE_EXCEPTION_HANDLER );
        if ( amInternalHost )                
        {            
            // make a Proxy, even though it is for my containing TaskServer            
            Service hsp = ((TaskServer) taskServer).hsp();
//            hspProxy = new HspProxy( hsp, this, REMOTE_EXCEPTION_HANDLER ); 
            internalCommandDepartment = ((TaskServer) taskServer).internalCommandDepartment();
           
            // register with TaskServer
            sessionInfo = ((TaskServer) taskServer).registerHost( serviceName(), true );
        }
        else // this is an external Host
        {                
            // set switches
            prefetchOn = true;
            cacheOn    = true;
            
            Service hsp = (Service) taskServer.executeCommand( this, GET_HSP );
            hspProxy = new HspProxy( hsp, this, REMOTE_EXCEPTION_HANDLER );
            computeDepartment.addProcessors( $processors - 1 );           
            CommandSynchronous command = new RegisterHost( serviceName() );            
            sessionInfo = (SessionInfo) taskServerProxy.execute( command, REMOTE_EXCEPTION_HANDLER );
            
            // Populate computeDepartment q with ExecuteTask Command objects
            CommandList commandList = new CommandList();
            for ( int i = 0; i < $processors; i++ )
            {                
                commandList.add( REQUEST_TASK ); // 1 Task request per processor
            }
            taskServerProxy.execute( commandList );
        }
        if ( sessionInfo != null ) 
        {
            initSession();
        }
        super.register ( taskServer );
        
        LogManager.getLogger().log( LogManager.INFO, "Constructed. Compute processors: " + $processors );
    }
    
    boolean amInternalHost() { return amInternalHost; }
    
    public void broadcastEvent( Object event, Service fromService )
    {
        Environment environment = sessionInfo.getEnvironment();
        if ( environment == null )
        {
            return; // client already logged out
        }
        Q eventQ = environment.getEventQ();
        eventQ.add( event );
    }
    
    Department computeDepartment() { return computeDepartment; }
    
    /** An internal exception was detected.
     */
    public void exceptionHandler( Exception exception )
    {
        // propagate exception to Hsp.
        Result result = new Result( null, exception, 0 );
        Command command = new PutResult( null, result );
        Proxy proxy = getHspProxy();
        proxy.execute( command );
}

    /** Not used by Jicos applications.
     * @param task Not used by Jicos applications.
     */    
    public void executeTask( Task task, int inputsIndex ) throws Exception
    //public void executeTask( Task task ) throws Exception
    {                 
        assert task != null;
        
        // DEBUG
//        if ( task.numAssignments > 1 )
//            System.out.println("Host.executeTask: " + task.getTaskId() + " numAssignments: " + task.numAssignments );
        
        if ( environment.isKilledTask( task ) )
        {
            if ( ! amInternalHost )
            {
                taskServerProxy.execute( REQUEST_TASK );
            }
            return; // ignore killed task;
        }

        /* assert: the LoginClient command is processed < any ExecuteTask 
         * command with a task from that client.
         */
        assert sessionInfo != null;
        assert task.getTaskId().getSessionId() == sessionId;
                    
        if ( task.isAtomic( environment ) && prefetchOn ) 
        {             
            taskServerProxy.execute( REQUEST_TASK );   // initiate prefetch
        }
        
        long startTime = System.currentTimeMillis();
        Object value = null;     
        try 
        {          
            value = invokeTaskExecute( task, environment, inputsIndex );
        }
        catch (Exception exception ) 
        {            
            if ( isTaskInSession( task, sessionId ) )
            {
                throwComputeException( task, exception );
            }
            else
            {
                // exception from task from previous client
                Logger log = LogManager.getLogger( Host.class );
                String message = "Ignoring: ";
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter( stringWriter );                
                exception.printStackTrace( printWriter );
                message += stringWriter.getBuffer();
                log.log( LogManager.INFO, message );
                // this is called inside throwComputeException 
                taskServerProxy.execute( REQUEST_TASK ); //prepare for next client                
            }
            return;
        }
        long stopTime = System.currentTimeMillis();   
        int executeTime = (int) (stopTime - startTime);
        TaskInfo taskInfo = new TaskInfo ( this, task, executeTime );
        Command cachedTask = null;
        Command killTaskCommand = null;
                
        if ( inputsIndex != Task.ALL_INPUTS )
        {
            // incremental execution
            if ( value == task )
            {
                if ( task.numUnsetInputs() == 0 )
                {
                    String message = "Incremental execution requested another" +
                           " input when there are no more \n     Task: " + task;
                    Exception exception = new ComputeException( message );
                    throwComputeException( task, exception );
                }
                return; // more inputs are needed.
            }
            
            // no more inputs are needed. 
            if ( task.numUnsetInputs() != 0 )
            {
                // Kill this task's siblings
                TaskId parentTaskId = task.getTaskId().getParentId();
                killTask( parentTaskId );
                killTaskCommand = new KillTask( parentTaskId, this );
            }
        }
        
        if ( value instanceof Task ) 
        {
            // value is a compose task
            task.initCompose( (Task) value );
            List children = task.getChildren();
            taskInfo.set( children );
            if ( children.size() > 0 && cacheOn ) 
            { 
                // cache a child whose type is myTaskClass, if any
                int indexCacheableChild = indexCacheableChild( children );
                if ( indexCacheableChild >= 0 )
                {                    
                    Task cacheableChild = (Task) children.get( 0 );
                    if ( indexCacheableChild > 0 )
                    {
                        // make cacheable child the 0th child 
                        cacheableChild = (Task) children.get( indexCacheableChild );
                        Task child0 = (Task) children.remove( 0 );                       
                        children.add( 0, cacheableChild );
                        children.add( child0 );
                    }                                           
                    cacheableChild.setCached( true );
                    cacheableChild.setSpawningHost( this );
                    cacheableChild.setTaskServer( myTaskServer );

                    /* The task that was just executed contains a reference to 
                     * child, the cached task, in its List of children. The
                     * ProcessResult Command object (for the task just executed) has
                     * a reference to this List of child tasks. A 
                     * Mailer, in a separate thread, marshalls this
                     * Command object just prior to sending it to a TaskServer. This
                     * thread may be interrupted during the marshalling of the 
                     * cached child task. During the interruption, the cached Task
                     * can change state (by having its execute method invoked, 
                     * creating children of its own). This causes an
                     * OptionalDataException when the MarshalledObject is 
                     * unmarshalled (the get method). 
                     *
                     * For this reason, we make a deep copy of the child to cache
                     * for subsequent execution. Currently, the best way we know of
                     * for making a deep copy of an instance of a class that extends
                     * Task is to marshal it, then unmarshal it :(
                     */
                    try
                    {
                        MarshalledObject deepCopy = new MarshalledObject( cacheableChild );
                        cachedTask = new ExecuteTask( (Task) deepCopy.get() );
                    }
                    catch ( Exception exception )
                    {
                        exception.printStackTrace();
                        String errorMessage = "Host: executeTask: [un]marshal error \n" 
                                                         + exception.getMessage();
                        LogManager.getLogger().log( LogManager.SEVERE, errorMessage, exception );
                        Result result = new Result( task.getTaskId(), exception, 0);
                        Command command = new PutResult( task.getTaskId(), result );
                        Proxy proxy = getHspProxy();
                        proxy.execute( command );         
                        taskServerProxy.execute( REQUEST_TASK ); //prepare for next client
                        return;
                    }
                }
            }
        }
        else 
        {
            // value is an output Object
            assert task.getSuccessorTaskId() != null;
            
            if ( task.getTaskServer().equals( myTaskServer ) ) 
            {
                // setArg is done on host's TaskServer
                taskInfo.set( task.getSuccessorTaskId(), 
                              task.getSuccessorIndex(), value 
                            );
            }
            else 
            { 
                // setArg is done on some other TaskServer               
                long criticalPathTime = taskInfo.getCriticalPathTime();
                Command command = new SetArg( task.getSuccessorTaskId(), 
                                              task.getSuccessorIndex(), value,
                                              criticalPathTime
                                            );
                                                           
                /* !! If Proxy.addProxy can be refactored to use Service instead 
                 * of ServiceName, is taskServerServiceName still needed by 
                 * Task? If not, omit it.
                 */
                ServiceName taskServerServiceName = task.taskServerServiceName();
                Service successorTaskServer = taskServerServiceName.service();
                Proxy proxy = getProxy( successorTaskServer );
                if ( proxy == null )
                {
                    // The 1st time I am sending a SetArg to this TaskServer
                    proxy = new TaskServerProxy( successorTaskServer, this, REMOTE_EXCEPTION_HANDLER );
                    addProxy( taskServerServiceName, proxy );
                }
                proxy.execute( command );
            }
        }
        
        Command processResult = new ProcessResult ( taskInfo );        
        if ( amInternalHost )
        {
            internalCommandDepartment.addCommand ( processResult );
            
            if ( killTaskCommand != null )
            {
                internalCommandDepartment.addCommand ( killTaskCommand );
            }
            return;
        }
        
        assert ! amInternalHost; // send task's result [& RequestTask] to myTaskServer. 
        CommandList command = new CommandList();
        command.add ( processResult );
        //if ( !( task.isAtomic( environment ) && prefetchOn ) && !cacheTask )
        if ( !( task.isAtomic( environment ) && prefetchOn ) && null == cachedTask ) 
        {
            // Neither prefetch nor cache occurred: Request another Task
            command.add ( REQUEST_TASK );
        }
        if ( killTaskCommand != null )
        {
            /* ProcessResult command is processed before KillTask command: i.e.,
             * compose task/output value is processed before siblings are killed.
             */            
            command.add( killTaskCommand );
            
        }
        taskServerProxy.execute( command );
        /* The ExecuteTask Command, cachedTask, is added to computeDepartment
         * Command queue AFTER the ProcessResult Command is sent. This
         * prevents a race condition that can occur when the 
         * computeDepartment has > 1 CommandProcessor objects: It could
         * cache the 1st child, T.1, and then be swapped out before it 
         * adds the ProcessResult Command for Task T to the TaskServer's
         * mail. Then, another CommandProcessor can get cached Task T.1,
         * execute it, and add a ProcessResult for Task T.1 before the
         * TaskServer gets informed about T.1's existence (part of 
         * result-processing of Task T).
         */
        if ( null != cachedTask )
        {             
            computeDepartment.addCommand( cachedTask ); // execute cached Task
        }
    }
    
    private Proxy getHspProxy()
    {
        return ( amInternalHost ) ? ((TaskServer) myTaskServer).hspProxy() : hspProxy;
    }
    
    // Used by Environment
     Command getRequestTask() { return REQUEST_TASK; }
     
     Service getTaskServer () { return myTaskServer; }
     
     Proxy getTaskServerProxy() { return taskServerProxy; }
     
     private int indexCacheableChild( List children )
     {
         for ( int i = 0; i < children.size(); i++ )
         {
             Task child = (Task) children.get( i );
             if ( myTaskClass.isInstance( child ) )
             {
                 return i;
             }
         }
         return -1; // no cacheable children
     }
     
     void initSession()
     {         
         sessionId   = sessionInfo.getSessionId();
         environment = sessionInfo.getEnvironment();
         environment.host( this );
         environment.setProxyServiceExternal( proxyServiceExternal );
     }
     
     Object invokeTaskExecute(Task task, Environment environment, int inputsIndex)
     {
         Object value;
         if ( inputsIndex == Task.ALL_INPUTS )
         {
             LogManager.getLogger().log( LogManager.FINEST, "task.execute()" );
             value = task.execute( environment );
         }
         else
         { 
             // invoke execute method for incremental evaluation
             value = task.execute( environment, inputsIndex );
         }
         return value;
     }
     
     private boolean isTaskInSession( Task task, long sessionId )
     {
         return ( sessionId == task.getTaskId().getSessionId() ) ? true : false;
     }
     
     public void killTask( TaskId taskId ) { environment.killTask( taskId ); }
     
     public void loginClient ( SessionInfo sessionInfo ) //throws Exception
     {
         /* explicit copy is needed for case when some hosts are in same JVM as
          * TaskServer and some are not.
          */
         this.sessionInfo = sessionInfo.copy();
         initSession();
     }
     
     RemoteExceptionHandler remoteExceptionHandler()
     {
         return REMOTE_EXCEPTION_HANDLER;
     }         
    
     /** Not used by Janet applications.
     * @param shared Not used by Janet applications.
     */    
    public void setShared( Shared shared ) //throws Exception
    {        
        assert shared != null;
        
        // Is a client logged in? Did this client's computation initiate the updateShared?
        if ( sessionInfo == null /*|| environment.getShared() == null*/ ) 
        {
            System.out.println("Host.setShared: No client. Ignoring.");
            return;
        }
        /* Still may be wrong environment. But shared will be set to correct 
         * value when current client's session is downloaded (in executeTask).
         */ 
        if ( shared.isNewerThan( environment.getShared() ) )
        {
            environment.setShared( shared );
        }       
        //LogManager.getLogger().log( LogManager.DEBUG, "Actual shared: " + shared.get() );
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
    public CurrentState getCurrentState() { return null; }
    
    /* (non-Javadoc)
     * @see edu.ucsb.cs.jicos.foundation.Administrable#startup()
     */
    public void startup() {}
    
    /* (non-Javadoc)
     * @see edu.ucsb.cs.jicos.foundation.Administrable#shutdown()
     */
    public void shutdown()
    {
        if ( ! amInternalHost )
        {
            LogManager.getLogger().log( LogManager.INFO, "Shutting down." );
            System.exit( 0 );
        }
        else
        {
            LogManager.getLogger().log( LogManager.DEBUG, "Internal Host: ignoring shut down." );
        }
    }

    /*  @see edu.ucsb.cs.jicos.admin.Administrable#echo(java.lang.String)  */
    public String echo( String request ) { return request; }
   
    synchronized Proxy taskServerProxy() { return taskServerProxy; } 
    
    protected static Service discoverTaskServer( List taskServerDomainNameList )
            throws Exception
    {
        Logger logger = LogManager.getLogger( Host.class );
    	    
        for ( int i = 0; i < TASKSERVER_LOOKUP_LIMIT; i++ )
        {
            ListIterator iterator = taskServerDomainNameList.listIterator();
            for ( ; iterator.hasNext() ; )
            {                
                String taskServerDomainName = (String) iterator.next();
                String url = "//" + taskServerDomainName + ":" + 
                             RegistrationManager.PORT + "/" + TaskServer.SERVICE_NAME;		
                try 
                {
                    Service taskServer = (Service) Naming.lookup(url);
                    logger.log(LogManager.INFO, "Url: " + url);
                    return taskServer;
                }
                catch (NotBoundException notBoundException) 
                {
                    logger.log(LogManager.SEVERE, "NotBoundException: " +
                        url + " -- " + notBoundException.getMessage() );
                } 
                catch (MalformedURLException malformedURLException) 
                {
                    logger.log(LogManager.SEVERE, "MalformedURLException: " +
                        url + " -- " + malformedURLException.getMessage() );
                } 
                catch (RemoteException remoteException) 
                {
                    logger.log(LogManager.SEVERE, "RemoteException: " +
                        url + " -- " + remoteException.getMessage() );
                }                
            }
            try
            {
                Thread.sleep( INTER_LOOKUP_WAITING_PERIOD );
            }
            catch ( InterruptedException ignore ){}
        }
        throw new Exception( "TaskServer not found.");
    }
    
    void throwComputeException( Task task, Exception exception )
    {
        exception.printStackTrace();
        Result result = new Result( task.getTaskId(), exception, 0 );
        Command command = new PutResult( task.getTaskId(), result );
        Proxy proxy = getHspProxy();
        proxy.execute( command );
        taskServerProxy.execute( REQUEST_TASK ); // prepare for next client
    }
     
     /** Not used by Jicos applications.
      * @param args Not used by Jicos applications.
      * @throws Exception Not used by Jicos applications.
      */     
     public static void main(String args[]) throws Exception
    {
         System.setSecurityManager( new RMISecurityManager() );

         // Load the host properties.
        Property.loadProperties( args );
     	Property.load( System.getProperty( "jicos.services.host.config" ) );
        
        // process command-line arguments: Put machine domain names in a List
        List taskServerDomainNameList = new LinkedList();
        for ( int i = 0; i < args.length; i++ )
        {
            taskServerDomainNameList.add ( args[i] );
        }
        
        Service taskServer = discoverTaskServer( taskServerDomainNameList );
        
        int $processors = Runtime.getRuntime().availableProcessors();
        new Host( taskServer, false, $processors ); // why is this not garbage immediately?
    }       
}
