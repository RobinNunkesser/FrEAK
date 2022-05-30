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

/*
 * HspState.java - immutable
 *
 * Created on August 20, 2003, 12:13 PM
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.utilities.*;

/** A snapshot of the state of an Hsp.
 * @author Peter Cappello
 */
public final class HspState implements java.io.Serializable
{
    private ServiceName hspServiceName;
    private Qu clientQ = new Qu();   
    private int nHosts; // !! currently monotically increasing
    private int nTaskServers;
    
    HspState(ServiceName hspServiceName,Qu clientQ,int nHosts, int nTaskServers) 
    {
        this.hspServiceName = hspServiceName;
        this.clientQ = clientQ;
        this.nHosts = nHosts;
        this.nTaskServers = nTaskServers;
    }  
    
    /** Get the client queue.
     * @return A Qu object that contains ClientQ objects, one for each client that is waiting
     * to use the Hsp (includes the client currently being processed by the Hsp.
     */    
    public Qu getClientQ() { return clientQ; }
    
    /** Get the HSP's ServiceName data.
     * @return the HSP's ServiceName.
     */    
    public ServiceName getHspServiceName() { return hspServiceName; }
    
    /** Get the number of hosts currently available to the Hsp. Since each task server
     * updates the Hsp with its current number of hosts only every 30 seconds, this
     * method's returned value may be 30 seconds out of date.
     * @return The number of hosts available, from the point of view of the Hsp (which is
     * a somewhat delayed version of reality).
     */    
    public int numHosts() { return nHosts; }
    
    /** Get the number of task servers currently available to the Hsp.
     * @return The number of task servers available.
     */    
    public int numTaskServers() { return nTaskServers; }
}