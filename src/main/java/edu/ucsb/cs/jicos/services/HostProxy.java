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
 * Is a Remote proxy for a Host. It thus implements the following methods:
 *    ExecuteTask
 *    LoginClient
 *    LogoutClient
 *    UpdateShared
 *
 * When acting as a Host proxy for a TaskServer (its only use, currently), it
 * has information associated with a Host:
 *    - a Set of TaskId objects for the Task objects assigned to this Host
 *    - HostTaskStatistics
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.commands.*;
//
import java.util.*;


public final class HostProxy extends Proxy 
{
    // constants
    private static final long TERM = 1000 * 5; // 5 seconds
    private final static Command LOGOUT_CLIENT = new LogoutClient();                                                 
    private final static RemoteExceptionHandler REMOTE_EXCEPTION_HANDLER = new
                                                  JicosRemoteExceptionHandler();        
    // attributes
    private TaskServer taskServer;    
    private Service host;
    private Set taskIds = Collections.synchronizedSortedSet( new TreeSet() );
    private HostTaskStats hostTaskStats;
    
    HostProxy( ServiceName serviceName, TaskServer taskServer, 
               RemoteExceptionHandler remoteExceptionHandler ) 
    {
        super ( serviceName, taskServer, remoteExceptionHandler, TERM );
        host = serviceName().service();
        this.taskServer = taskServer;
                
        if ( taskServer == null )
        {
            // Proxy for TaskServer's internalHost: idle time averages excluded
            hostTaskStats = new HostTaskStats ( serviceName, false );
        }
        else
        {
            // HostProxy is for an external Host: idle time averages included
            hostTaskStats = new HostTaskStats ( serviceName, true );
        }
    }  
    
    public void evict() 
    {
        if ( !kill )
        {
            super.kill(); // kill "ping" thread
            System.out.println("HostProxy.evict: " + serviceName() );
            unregisterHost();
        }
        else
        {
            System.out.println("HostProxy.evict: Thread already evicted.");
        }
    }
    
    public synchronized void executeTask ( Task task ) 
    { 
        // record TaskId of task that is being sent to this host for execution
        taskIds.add( task.getTaskId() );
    } 
    
    Service host() { return host; }
    
    HostTaskStats hostTaskStats() { return hostTaskStats; }
                    
    public synchronized void loginClient( SessionInfo sessionInfo ) //throws Exception
    {
        taskIds.clear();
        hostTaskStats.clear();
        if ( taskServer == null )
        {                  
            // Internal Host: invoke loginClient locally
            ((Host ) host).loginClient( sessionInfo );            
        }
        else
        {
            Command command = new LoginClient ( sessionInfo );
            sendCommand( command );
        }
    }
    
    HostTaskStats logoutClient()
    {
        /* !! Put in logoutClient method on Host.
        // skip logout, if host IS actually the taskServer itself
        if ( ! host.equals ( taskServer ) )
        {      
            mail().add( LOGOUT_CLIENT );
        }
         */        
        return hostTaskStats;
    }
    
    // Update its set of incomplete Task objects
    synchronized void processResult ( TaskInfo taskInfo ) 
    { 
        renew(); 
        hostTaskStats.add ( taskInfo );
        TaskId taskId = taskInfo.taskId();
        
        // ?? I think I may not be tracking tasks that run on internalHosts 
        // !! Check later.
        //assert taskIds.contains( taskId ) : "Missing task: " + taskId;
        taskIds.remove ( taskId );
        
        // update taskIds w/ cached Task, if there is one
        if ( taskInfo.getChildren() != null )
        {
            Task task = (Task) taskInfo.getChildren().get( 0 );
            if ( task.isCached() )
            {
                taskIds.add( task.getTaskId() );
            }
        }
    }
    
    synchronized void unregisterHost()
    {
        assert taskServer != null;
        assert taskIds != null;
        
        taskServer.unregisterHost( host );
        
        // unassign tasks that are assigned to my Host
//        System.out.println("HostProxy.unregisterHost: taskIds.size: " + taskIds.size() );
        for ( Iterator i = taskIds.iterator(); i.hasNext(); )
        {
            TaskId taskId = (TaskId) i.next();
//            System.out.println("HostProxy.unregisterHost: unassigning: " + taskId);
            taskServer.session().unassign( taskId );
        }
        taskIds = null;
//        taskServer.session().dumpUnassignedTasks();
    }
}
