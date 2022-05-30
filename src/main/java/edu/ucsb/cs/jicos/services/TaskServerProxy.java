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
 *  This is a Remote proxy for a TaskServer. It thus implements the
 *  following methods:
 *  <UL>
 *    <LI>acknowledgeClientRegistration (!!omit)</LI>
 *    <LI>addChildSessionStatistics (rename AddTaskStatistics?)</LI>
 *    <LI>loginClient</LI>
 *    <LI>logoutClient</LI>
 *    <LI>processResult</LI>
 *    <LI>receiveTasks</LI>
 *    <LI>registerChildTaskServer</LI>
 *    <LI>registerHost</LI>
 *    <LI>requestTasks</LI>
 *    <LI>setArg</LI>
 *    <LI>setShared</LI>
 *    <LI>spawn
 *    <LI>updateShared</LI>
 *  </UL>
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.commands.*;


public final class TaskServerProxy extends Proxy
{
    // constants
    private static final long TERM = 1000 * 10; // 10 seconds
    private final static Command LOGOUT_CLIENT = new LogoutClient();                                                 
    private final static RemoteExceptionHandler REMOTE_EXCEPTION_HANDLER = new
                                                  JicosRemoteExceptionHandler();        
    // immutable attributes   
    private Service taskServer;
    private Point point; // coordinate of TaskServer in mesh
    
    // state
    private boolean requestTasksPending = false;
    
    public TaskServerProxy( Service service, ServiceImpl serviceImpl,
                            RemoteExceptionHandler remoteExceptionHandler ) 
    {
        super ( service, serviceImpl, remoteExceptionHandler, TERM );
        taskServer = service;       
        serviceImpl.addProxy( service, this );
    }
    
    public TaskServerProxy( TaskServerServiceInfo taskServerServiceInfo, ServiceImpl serviceImpl,
                            RemoteExceptionHandler remoteExceptionHandler ) 
    {
        super ( taskServerServiceInfo, serviceImpl, remoteExceptionHandler, TERM );
        taskServer = taskServerServiceInfo.service();
        serviceImpl.addProxy( taskServerServiceInfo.service(), this );
    }
    
    public void evict() 
    {
        LogManager.getLogger(this).log(LogManager.DEBUG, "Evicting: " + serviceName());
	// Do something?
    }    
    
    synchronized void point( Point point ) { this.point = point; }
    
    synchronized Point point() { return point; }
    
    synchronized void receiveTasks() { requestTasksPending = false; }
    
    public synchronized boolean requestTasksPending()
    {
        if ( requestTasksPending )
        {
            return true;
        }
        requestTasksPending = true;
        return false;
    }
}
