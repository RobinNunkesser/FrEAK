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
import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.commands.*;
import edu.ucsb.cs.jicos.services.external.services.*;

import java.util.Map;

import java.util.*;
/**
 * @author  Peter Cappello
 * @version 1
 *
 * Session encapsulates all data associated with 1 session of 1 client
 */
public final class Session
{
    // constants
    private static final int TASKS_INITIAL_CAPACITY = 1024;

    // switches
    private static final boolean executeOn = true;

    // References
    private TaskServer taskserver;
    private Host internalHost;
    private Map externalServiceProviderMap;

    // Session attributes
    private SessionInfo sessionInfo;  // data pertinent to Host & application
    private Map tasks =
          Collections.synchronizedMap ( new HashMap( TASKS_INITIAL_CAPACITY ) );
    private PQTasks unassignedTasks;
    private SessionStatistics sessionStatistics = new SessionStatistics();
    private JoinCounter joinCounter = new JoinCounter();
    private TaskIdTrie killedTasks = new TaskIdTrie();

    Session( TaskServer taskserver, Host internalHost )
    {
        this.taskserver = taskserver;
        this.internalHost = internalHost;
        unassignedTasks = new PQTasks( taskserver );
        externalServiceProviderMap = taskserver.externalServiceProviderMap();
    }

    // Assign task to host.
    void assign ( Task task, Service host ) { task.setHost( host ); }

    synchronized void clear()
    {
        sessionInfo = null;
        tasks.clear();
        unassignedTasks.clear();
        sessionStatistics = new SessionStatistics();
        joinCounter.reset();
    }
    
    void dispatchTask( Task task, Environment environment, Command command )
    {
        if ( executeOn && task.executeOnServer( environment ) && (! (task instanceof TaskExternal) ))
        {
            internalHost.computeDepartment().addCommand( command );
        }
        else
        {
            unassignedTasks.put ( task );
        }
    }
    
    void dumpUnassignedTasks() { unassignedTasks.dump(); }

    JoinCounter getJoinCounter() { return joinCounter; }

    SessionInfo getSessionInfo() { return sessionInfo; }
    
    void killTask( TaskId taskId ) { System.out.println("Session.killTask: " + taskId ); killedTasks.add( taskId ); }
    
    public void setSessionInfo(SessionInfo si) { sessionInfo=si; }
    /** Should be changed so that info is piggy-backed w/ task, as needed.
     */
    SessionStatistics getSessionStatistics() { return sessionStatistics; }
    
    private TaskServer getTasksTaskServer( Task task )
    {
        if ( task instanceof TaskExternal )
        {
            // determine kind of TaskExternal
            Class serviceClass = ((TaskExternal) task).serviceClass();
            
            // determine taskserver that services this subclass of TaskExternal
            Service externalTaskServer = (Service) externalServiceProviderMap.get(serviceClass);
            
            if( null == externalTaskServer ) 
            {
                String msg = "There is no taskserver for " + serviceClass.getName() + " tasks.";
                throwComputeException( task, msg );
                return taskserver; // wrong TaskServer, but computation is terminating.
            } 
            else 
            {
                return (TaskServer) externalTaskServer;
            }               
        }
        else
        {
            if ( taskserver instanceof TaskServerExternal )
            {
                // parent TaskServer of a TaskServerExternal is root TaskServer
                return taskserver.getParentTaskServer();
            }
            else
            {
                return taskserver;
            }
        }
    }

    TaskServer taskserver() { return taskserver; }

    PQTasks unassignedTasks() { return unassignedTasks; }

    void loginClient( SessionInfo sessionInfo )
    {
        // pre-condition
        assert sessionInfo != null;

        this.sessionInfo = sessionInfo;
    }

    synchronized private void processCompletedTask( TaskInfo taskInfo )
    {
        TaskId    completedTaskId = taskInfo.taskId();
        TaskId    successorTaskId = taskInfo.getSuccessorTaskId();
        int       successorIndex  = taskInfo.getSuccessorIndex();
        Object    successorValue  = taskInfo.getValue();
        List children             = taskInfo.getChildren();
        long criticalPathTime     = taskInfo.getCriticalPathTime();

        // !! It may be that successorTaskId is never null
        if ( children == null && successorTaskId != null )
        {
            setArg( successorTaskId, successorIndex, successorValue, criticalPathTime );
        }

        if ( children != null )
        {
            Task successorTask = (Task) children.remove( children.size() - 1 );            
            spawn( successorTask );

            // spawn remaining children
            assert children.size() > 0;
            TaskServer successorTaskTaskServer = getTasksTaskServer( successorTask );
            while ( children.size() > 0 )
            {
                Task child = (Task) children.remove( 0 );
                child.setTaskServer( successorTaskTaskServer );
                child.taskServerServiceName( successorTaskTaskServer.serviceName() );
                child.setPredecessorCriticalPathTime( criticalPathTime );
                spawn( child );
            }
        }     
        tasks.remove ( completedTaskId );
    }

    /**
       * @param TaskInfo taskInfo: information about the task just completed.
    */
    public synchronized void processResult( TaskInfo taskInfo )
    {
        TaskId taskId = taskInfo.taskId();
        
        if ( killedTasks.contains( taskId ) )
        {
            System.out.println("Session.processResult: ignoring killed task: " + taskId );
            return; // ignore result of killed task
        }
        
        assert taskId != null;

        if ( sessionInfo == null ||
             sessionInfo.getSessionId() != taskId.getSessionId() )
        {
            /* ComputeException causes logout, which clears Session. Ignore
             * Tasks associated with old Session.
             */
            System.out.println("Session.processResult: Ignoring old TaskInfo.");
            return;
        }
        processCompletedTask( taskInfo );
        //System.out.println( "Session.processResult: unassignedTasks.size: " + unassignedTasks.size() + " tasks.size: " + tasks.size());
    }

    synchronized void receiveTasks( Task[] fetchedTasks )
    {
        for ( int i = 0; i < fetchedTasks.length; i++ )
        {
            tasks.put( fetchedTasks[i].getTaskId(), fetchedTasks[i] );
            unassignedTasks.put( fetchedTasks[i] );
        }
    }

    /** getTask cannot be synchronized: It is invoked at startup before spawn.
     * If it is synchronized, spawn could not proceed: deadlock.
     */
    Task requestTask( Service host )
    {
        Task task = unassignedTasks.remove();
        assign ( task, host );
        return task;
    }

    /**
     * return a fraction of the unassigned tasks (to be shipped to another
     * TaskServer), to effect a diffusion of unassigned tasks throughout the
     * TaskServer network. TaskServers serve not just their Hosts but their
     * subtree of TaskServers.
     */
    Task[] requestTasks( int factor ) //throws Exception
    {
        Task[] fetchedTasks = unassignedTasks.removeTasks( factor );
        for( int i = 0; i < fetchedTasks.length; i++ )
        {
            Task task = (Task) tasks.remove( fetchedTasks[i].getTaskId() );
            assert task != null; //DEBUG of OutOfMemoryException
        }
        return fetchedTasks;
    }

    /**
       Waiting successor tasks stay on their originating server (i.e. they
       do not move via diffusion). This makes it simpler for predeccessor
       tasks to know which server has the successor task.

       When caching is implemented: have producer setArg directly on
       originalServer, do not use its server as an intermediary.
       What about setting it on originalHost? At least try this?

       Devise an exception handling scheme where a Client's computation is
       aborted when the Client's Task objects interact with the TaskServer
       in an erroneous way. Give the Client ample information about what
       went wrong.
     * @param taskserver that has this session
     */
    public synchronized void setArg ( TaskId taskId, int index, Object value,
                                      long criticalPathTime )
    {
        assert taskId != null;

        if ( index == Task.RESULT )
        {
            Result result = new Result( taskId, value, criticalPathTime );
            Command command = new PutResult( taskId, result );
            Proxy hspProxy = taskserver.hspProxy();
            hspProxy.execute( command );
        }
        else
        {
            // set argument of successor task.
            Task task = (Task) tasks.get( taskId );
                      
            if ( task == null )
            {
                /* Assumption: this compose task completed before processing
                 * all of its inputs, and this input thus should be ignored.
                 */
                LogManager.getLogger().log( LogManager.DEBUG, "Task not found. Ignoring." );
                return;
            }           
            assert task.get$UnsetArgs() != 0 : "Successor: no unset arguments";

            // update critical path time
            if ( task.getPredecessorCriticalPathTime() < criticalPathTime )
            {
                task.setPredecessorCriticalPathTime( criticalPathTime );
            }

            // update total task time
            
            // set task's index'th input; decrement number of unset inputs
            task.setInput ( index, value );
            
            // dispatch task?
            Environment environment = sessionInfo.getEnvironment();
            Command command;
            if ( task.executeIncrementally( environment ) )
            {
                command = new ExecuteTask ( task, index );
                dispatchTask( task, environment, command );
                return;
            }
            if ( task.get$UnsetArgs() == 0 )
            {
                command = new ExecuteTask ( task );
                dispatchTask( task, environment, command );
            }
        }
    }

    synchronized void setInfo( SessionInfo sessionInfo )
    {
        assert sessionInfo != null;

        this.sessionInfo = sessionInfo;
    }

    public synchronized void spawn ( Task task )
    {
        assert null != task;
        
        TaskServer appropriateTaskServer = getTasksTaskServer( task );
        if ( appropriateTaskServer == taskserver )
        {
            // task should be handled by this TaskServer
            tasks.put( task.getTaskId(), task ); // put task in tasks map
            if ( task.isCached() )
            {
                // assign is needed to track Task, e.g., detect when cached Task's Host fails
                assign ( task, task.getSpawningHost() );
                return;
            }

            // dispatch task?
            if ( task.get$UnsetArgs() == 0 )
            {            
                Environment environment = sessionInfo.getEnvironment();
                Command command = new ExecuteTask ( task );
                dispatchTask( task, environment, command );            
            }
        }
        else
        {
            // Send task to an appropriate TaskServer
            Task[] tasks = { task };
            Command command = new ReceiveTasks( tasks, taskserver );
            taskserver.sendCommand( appropriateTaskServer, command );
        }
       
//        // Is task a request for an external service?
//        if ( task instanceof TaskExternal && 
//             ( !(taskserver instanceof TaskServerExternal) || 
//                ( taskserver.myTaskClass.isInstance( task ) )
//             )
//           )
//        {
//            // determine kind of external task
//            Class serviceClass = ((TaskExternal) task).serviceClass();
//            
//            // determine taskserver that services that kind of external task
//            Service externalTaskServer = (Service) externalServiceProviderMap.get(serviceClass);
//  
//            // if there is no such task server
//            if( null == externalTaskServer ) 
//            {
//                String msg = "There is no taskserver for " + serviceClass.getName() + " tasks.";
//                throwComputeException( task, msg );
//            } 
//            else 
//            {
//                // send that TaskServerExternal this TastExternal object
//                Task[] tasks = new Task[1];
//                tasks[0] = task;
//                Command command = new ReceiveTasks( tasks, taskserver );
//                taskserver.sendCommand( externalTaskServer, command );
//            }
//            return;
//        }
//        
//        tasks.put( task.getTaskId(), task ); // put task in tasks map
//        if ( task.isCached() )
//        {
//            // assign is needed to track Task, e.g., detect when cached Task's Host fails
//            assign ( task, task.getSpawningHost() );
//            
//            //t.setCached( false ); // unnecessary
//            return;
//        }
//        
//        // dispatch task?
//        if ( task.get$UnsetArgs() == 0 )
//        {            
//            Environment environment = sessionInfo.getEnvironment();
//            Command command = new ExecuteTask ( task );
//            dispatchTask( task, environment, command );            
//        }
    }

    void throwComputeException( Task task, String message )
    {
        assert task != null;
        
        Exception exception = new ComputeException( message );
        Result result = new Result( task.getTaskId(), exception, 0 );
        Command command = new PutResult( task.getTaskId(), result );
        Proxy hspProxy = taskserver.hspProxy();
        hspProxy.execute( command );
        LogManager.getLogger().log( LogManager.DEBUG, "ComputeException on task:" + task + "\n message: " + message );
    }

    void unassign( TaskId taskId )
    {
        assert taskId != null;
        
        if ( killedTasks.contains( taskId ) )
        {
            System.out.println("Session.unassign: ignoring killed task: " + taskId );
            tasks.remove( taskId );
            return;
        }
        
//        System.out.println("Session.unassign: " + taskId );
        Task task = (Task) tasks.get ( taskId );
        if ( task != null /*&& ! task.isComplete()*/ )
        {
            task.setHost( null ); // For consistency: Unassigned tasks have no host            
            
//            System.out.println("Session.unassign: putting: " + taskId );
            unassignedTasks.put ( task );
        }
    }

   /** Returns the tasks for this TaskServer Session. Used for
    * serialization.
    * @return tasks The Session tasks.
    */
    public Map getTasks() { return tasks; }
    
    // DEBUG
    int getWaiters() 
    {
        return unassignedTasks.getWaiters(); 
    }

   /** Sets the tasks for this TaskServer Session. Used for
    * restoring from a previously saved state.
    * @param tasksMap The Session tasks.
    */
    public void setTasks( Map tasksMap ) 
    {
        // All loaded tasks are unassigned.
        Iterator iter = tasksMap.values().iterator();
        while( iter.hasNext() ) 
        {
            Task task = (Task)iter.next();
            task.taskServerServiceName( taskserver.serviceName() );
            task.setTaskServer( taskserver );
            tasks.put( task.getTaskId(), task );
            if ( task.get$UnsetArgs() == 0 )
            {               
                unassignedTasks.put( task ); // the task is ready for processing
            }
        }
    }
}
