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
 * This command is sent directly to the Hsp, to get the Hsp's ServiceName, so
 * that an HspProxy can be constructed. The Proxy version of the execute method
 * thus is never used.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services.commands;

import edu.ucsb.cs.jicos.foundation.*;


public final class GetServiceName implements CommandSynchronous 
{
    public Object execute( Proxy proxy, RemoteExceptionHandler remoteExceptionHandler ) 
    { 
        /*
        Service remoteService = proxy.remoteService();
        Service sender = proxy.serviceName().service();
        ServiceName serviceName = null;
        try
        {
            serviceName = (ServiceName) remoteService.executeCommand( sender, this );
        }
        catch ( Exception exception )
        {
            remoteExceptionHandler.handle( exception, remoteService, sender );
        }
        return serviceName;
         */
        return null; // unused
    }
    
    public Object execute( ServiceImpl myService ) 
    {
        return myService.serviceName();
    }    
    
    public String toString() { return getClass().toString(); }
}
