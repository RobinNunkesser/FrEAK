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
 * A smart proxy for the Hsp that runs on a client within HspAgent.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import java.rmi.*;
import java.rmi.server.*;


public final class Client2Hsp 
{
    // Client ServiceImpl receives pings to verify that client is alive.
    private edu.ucsb.cs.jicos.services.Client client; 
    private ClientToHsp hsp;
    private HspAgent hspAgent;

    // state variable
    private boolean login; // true iff login is in progress
    
    /**
     * @param hsp
     * @param hspAgent
     * @throws RemoteException  */    
    Client2Hsp( ClientToHsp hsp, HspAgent hspAgent ) throws RemoteException
    {
        this.hsp = hsp;
        this.hspAgent = hspAgent;
    }    
    
    /** Application gives a task to the Hsp.
     * @param task is the root task of some Task graph.
     * @throws RemoteException This method is invoked remotely.
     * @throws ComputeException This Exception is thrown when a task's execute method
     * throws an Exception. ComputeException propagates the
     * Exception back to this original compute method.
     * @return The Object that is the returned value from this
     * computation: The returned value of the sink Task:
     * the Task with no successor.
     */
    public Object compute( Task task ) 
           throws ComputeException, JicosException, RemoteException
    {
        return hsp.compute( task );
    }
    
    /** Returns information associated with the state of the Hsp.
     * @throws RemoteException This method is invoked remotely on the Hsp.
     * @return An HspState object.
     */    
    public HspState getState() throws RemoteException
    {
        return hsp.getState();
    }
    
    /** Get the Result object for some computation that was initiated via the
     * setComputation method.
     * @return A Result object.
     * @throws ComputeException The returned Result object is associated with a computation that was initiated
     * via the setComputation method. If this computation spawned a Task object whose
     * execute method threw a ComputeException, then this invocation of getResult
     * propagates that ComputeException back to the client application.
     * @throws RemoteException This is a Remote method. */    
    public Result getResult() 
           throws ComputeException, JicosException, RemoteException
    {
        return hsp.getResult();
    }
    
    public boolean isComplete( ResultId resultId ) throws IllegalStateException, RemoteException
    {
        return hsp.isComplete( resultId );
    }
 
    /** login Client with the <CODE>Hsp</CODE>.
     * @throws RemoteException This method is invoked remotely.
     * @param environment Data container for client information associated with this
     * client during this login (session).
     */
    public void login( Environment environment ) throws RemoteException
    {
        client = new Client();
        ClientProfile clientProfile = new ClientProfile( client );
        if ( login )
        {
            // client cannot login while logged in.
            throw new IllegalStateException();
        }
        login = true;
        hsp.login( client.serviceName(), clientProfile, environment );
    }

    /** logout Client from the <CODE>Hsp</CODE>.
     * @return Invoice, an object indicating how much system resources were
     * consumed by this application.
     * @throws RemoteException This method is invoked remotely.
     * @throws InterruptedException The Hsp propagates the logout to the TaskServer
     * network. It <I>waits</I> for Session statistics
     * to be returned from the TaskServer network.
     * Hsp propagates the InterruptedException that wait
     * can throw.
     */
    public Invoice logout() throws RemoteException
    {        
        login = false; // login no longer in progress
        Invoice invoice = hsp.logout();
        UnicastRemoteObject.unexportObject( client, true );
                
        return invoice;
    }
    
    /** This method is used to initiate a computation without waiting for the
     * computation to complete. To obtain the computed result, the application must
     * invoke the getResult method.
     * @return a ResultId. This is useful, if setComputation is invoked several times before
     * getResult is invoked. Since the Results can come back in any order, ResultId
     * is used to associate the returned Result value with the particular setComputation
     * whose ResultId that matches the Result's ResultId. Please see Result for its
     * accessor methods.
     * @param task The Task object that represents the overall computation.
     * @throws RemoteException This is a Remote method.
     */    
    public ResultId setComputation( Task task ) throws RemoteException
    {
        return hsp.setComputation( task );
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
     * @param stateConfig The StateConfig object that describes the datasource.
     * @throws RemoteException  This is a Remote method.
     * @throws JicosException   
     * @throws ComputeException 
     */    
    public void loadState( StateConfig stateConfig ) 
           throws ComputeException, JicosException, RemoteException
    {
        hsp.loadState( stateConfig );
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
     * @param stateConfig The StateConfig object that describes the datasource.
     * @throws RemoteException  This is a Remote method.
     * @throws JicosException   
     * @throws ComputeException 
     */    
    public void saveState( StateConfig stateConfig ) 
           throws ComputeException, JicosException, RemoteException
    {
        hsp.saveState( stateConfig );
    }
}
