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
 * UpdateTaskServerState.java
 */

package edu.ucsb.cs.jicos.services.commands;

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.*;


public final class UpdateTaskServerState implements Command 
{
    /*
    private int deltaNHosts;
    
    public UpdateTaskServerState( int deltaNHosts ) 
    {
        this.deltaNHosts = deltaNHosts;
    }
    
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }
    
    public void execute( ServiceImpl myService ) throws Exception
    {       
        ((Hsp) myService).updateTaskServerState( deltaNHosts );
    }
     */
    
    private Service taskServer;
    private int nHosts;
    
    public UpdateTaskServerState( Service taskServer, int nHosts ) 
    {
        this.taskServer = taskServer;
        this.nHosts = nHosts;
    }
    
    public void execute( Proxy proxy ) { proxy.sendCommand( this ); }
    
    public void execute( ServiceImpl myService ) throws Exception
    {       
        ((Hsp) myService).updateTaskServerState( taskServer, nHosts );
    }
}
