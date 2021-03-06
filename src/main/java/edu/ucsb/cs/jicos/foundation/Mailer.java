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
 * Encapsulates a List of Commands to be sent to a particular Service.
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.foundation;

import java.rmi.RemoteException;
import java.util.*;


public class Mailer extends Processor
{
    private Service fromAddress;
    private Service toAddress;   // the destination of this Mail
    private List commandQ = new LinkedList(); // queue of Command objects
    private Q mailQ; // queue of references to this.
    private RemoteExceptionHandler remoteExceptionHandler;
    private Proxy myProxy;
    
    public Mailer( Service fromAddress, RemoteExceptionHandler remoteExceptionHandler, 
                   Q mailQ, Proxy myProxy) 
    { 
        super( mailQ );
             
        assert fromAddress != null;
        assert remoteExceptionHandler != null;
        
        this.fromAddress = fromAddress;
        this.remoteExceptionHandler = remoteExceptionHandler;
        this.mailQ = mailQ;
        this.myProxy = myProxy;
        toAddress = myProxy.getService();
    }
    
    /** Add a Command to the list.
     * @param command The Command object to be added.
     */    
    public synchronized void add( Command command )
    { 
        assert command != null;
        
        commandQ.add( command );
        mailQ.add( this ); // notify mail processor: send commandQ
    }
    
    private synchronized final List copyCommandQ() 
    {
        List commandQCopy = commandQ;
        commandQ = new LinkedList();
        return commandQCopy;
    }
    
    /* mail command queue to distributed component for whom commands are
     */
    void process( Object object ) 
    {
        if ( commandQ.isEmpty() )
        {
            return;
        }
        List commandQCopy = copyCommandQ();
        try
        {
            toAddress.receiveCommands ( fromAddress, commandQCopy );
        }
        catch ( RemoteException exception ) 
        {
//            System.out.println("Mailer: catching exception." + this );
            myProxy.evict();
            
//            System.out.println( "Mail: toAddress: " + toAddress + "\n Command Q: \n");
//            for ( Iterator iterator = commandQCopy.iterator(); iterator.hasNext(); )
//            {
//                System.out.println( ((Command) iterator.next()) );
//            }
      
            //remoteExceptionHandler.handle ( exception, fromAddress, toAddress );         
        }
    }
    
    /** Returns a String representation of the object.
     * @return A String representation of the object.
     */    
    public String toString() 
    {
        StringBuffer stringBuffer = new StringBuffer("Mail: toAddress: " + toAddress);
        stringBuffer.append("\n Command Q: \n");
        for ( Iterator iterator = commandQ.iterator(); iterator.hasNext(); )
        {
            stringBuffer.append( ((Command) iterator.next()).toString() );
        }
        return new String( stringBuffer );
    }
}
