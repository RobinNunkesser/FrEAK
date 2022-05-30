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
 *  Container class for 2 items that currently are associated with a client's
 *  login session:
 *  the input to the computation
 *  (e.g., in a Traveling Salesman problem (TSP), the input is the
 *  graph), and
 *  the <CODE>Shared</CODE> <CODE>Object</CODE>
 *  (e.g., in the TSP, the cost of the best known tour,
 *  which initially is "infinite", may be a Shared Object).
 *  Both items can be <CODE>null</CODE>.
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.commands.*;
import edu.ucsb.cs.jicos.services.external.services.*;
import edu.ucsb.cs.jicos.utilities.Qu;


public final class Environment implements java.io.Serializable
{
    private Object input;
    private Shared shared;
    private /*transient*/ Q eventQ = new Qu();
    
    // attributes that are local to containing Host
    
    private transient Object cache;
    private transient Host host; // Host that has this Environment object
    private transient Proxy taskServerProxy; // used if host is external
    private transient Service taskServer;    // used if host is internal
    private transient ProxyServiceExternal proxyServiceExternal;
    
    private TaskIdTrie killedTasks = new TaskIdTrie();
    
    /** Used to construct the client's computational environment.
     * @param input Input to the entire computation.
     * Every Task that the client sends to the Hsp has access to it
     * this input.
     * It is immutable.
     * See the TSP example given above.
     * @param shared Every Task that the client sends to the Hsp has access to
     * this object, and can change its value: It is <I>mutable</I>.
     * See the TSP example given above.
     * See <CODE>Shared</CODE>.
     */    
    public Environment( Object input, Shared shared ) 
    {
        this.input = input;
        this.shared = shared;
    }
    
    /* Broadcast event to all TaskServers and Hosts
     */
    public void addEvent( Object event )
    {
        Command command = new BroadcastEvent( event, host );
        if ( host.amInternalHost() ) 
        {
            // propagate shared to TaskServer's neighbors                        
            ((ServiceImpl) taskServer).broadcast( command, host );
        }
        else
        {
            // propagate shared to TaskServer
            taskServerProxy.execute( command );
        }
    }
    
    /* This copy is to get around the odd situation when a Host is instantiated
     * as part of a TaskServer instantiation within the same JVM, (hence sharing
     * the same heap) & this one joins later. It's environment.host field is 
     * set, when it should be null.
     */
    Environment copy() { return new Environment ( input, shared ); }
    
    /** Initiate a non-blocking Task request from the Host's TaskServer. This
     * method is used when a task my not construct any subtasks, but it is not
     * known until the execute method is invoked.
     */    
    public void fetchTask() //throws Exception
    {
        Command command = host.getRequestTask();
        taskServerProxy.execute( command );
        
        /* ?? Should I raise priority of Mailer thread over CommandProcessor 
         * thread or target it more carefully (e.g., raise myTaskServerMail's 
         * thread when it is constructed) ??
         */
        Thread.yield(); // Give the Mail thread a chance to initiate the RMI.
    }
    
    /** returns host's cache.
     * Intended to be invoked by hosted Task.
     * @return The host's cache as an <CODE>Object</CODE>:
     * It must be cast appropriately.
     */
    synchronized public Object getCache() { return cache; }
    
    /** returns input that is common to all tasks of this computation.
     * Intended to be invoked by hosted Task.
     * @return The <CODE>Object</CODE> that is common to all tasks of this computation:
     * It must be cast appropriately.
     */
    public Q getEventQ() { return eventQ; }
    
    /** returns input that is common to all tasks of this computation.
     * Intended to be invoked by hosted Task.
     * @return The <CODE>Object</CODE> that is common to all tasks of this computation:
     * It must be cast appropriately.
     */
    public Object getInput() { return input; }
    
    public ProxyServiceExternal getProxyServiceExternal()
    {
        return proxyServiceExternal;
    }
    
    public void setProxyServiceExternal( ProxyServiceExternal proxyServiceExternal)
    {
        this.proxyServiceExternal = proxyServiceExternal;
    }
    
    /** returns the <I>modifiable</I> <CODE>Object</CODE> that is common to all tasks
     * of this computation.
     *
     * Intended to be invoked by hosted Task.
     * @return The <I>modifiable</I> <CODE>Object</CODE> that is common to all tasks of this
     * computation: It must be cast appropriately.
     */    
    //CLEAN: return a reference to session.shared. 
    // Then, whenever Task references it, it gets the Host's current version.
    // Task synchronizes on it when
    // referencing it OR pass it to Task.execute(Host host, Computation computation)
    // then remove getInput, getShared, setShared from Host interface.
    public synchronized Shared getShared() { return shared; }
    
    boolean isKilledTask( Task task ) 
    { 
        return killedTasks.contains( task.getTaskId() );
    }
    
    void killTask( TaskId killedTaskId ) { killedTasks.add( killedTaskId ); }
        
    //synchronized void setHost( Host host ) { this.host = host; }
    synchronized void host( Host host ) 
    { 
        this.host = host;
        taskServer = host.getTaskServer();
        taskServerProxy = host.taskServerProxy();
    }
    
    /** Sets the value of the host's <I>mutable</I> <CODE>cache Object</CODE>.
     *
     * Intended to be invoked by a Task.
     * @param cache The new cache value.
     */    
    public synchronized void setCache ( Object cache ) { this.cache = cache; }
    
    /** Sets the value of the <I>mutable</I> <CODE>Shared Object</CODE> that
     * is common to all tasks of this computation.
     *
     * Jicos propagates this value until either all Hosts have received
     * it, or some TaskServer deems it not new. See <CODE>Shared</CODE>.
     *
     * Intended to be invoked by a Task.
     * @param shared The <CODE>Shared Object</CODE> value proposed by the
     * invoking task as being a newer value for the Shared Object.
     */    
    public synchronized void setShared ( Shared shared )
    {
        assert shared != null;
        
        if ( this.shared == null || shared.isNewerThan( this.shared ) )
        {
            this.shared = shared;
            if ( host.amInternalHost() ) 
            {
                // propagate shared to TaskServer's neighbors               
                Command command = new UpdateShared( shared, taskServer );
                ((ServiceImpl) taskServer).broadcast( command, null );
            }
            else
            {
                // propagate shared to TaskServer
                Command command = new SetShared ( shared, host );
                taskServerProxy.execute( command );
            }                
        }
    }
}
