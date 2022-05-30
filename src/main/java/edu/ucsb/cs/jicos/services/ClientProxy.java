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
 * ClientProxy.java
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;
import java.rmi.*;


public final class ClientProxy extends Proxy 
{
    // constants
    public static final long TERM = 1000 * 3; // 3 seconds
    
    private ServiceImpl serviceImpl;
    
    public ClientProxy( ServiceName serviceName, ServiceImpl serviceImpl,
                        RemoteExceptionHandler remoteExceptionHandler
                      ) 
    {
        super ( serviceName, serviceImpl, remoteExceptionHandler, TERM );
        this.serviceImpl = serviceImpl;
    }
    
    public void evict()
    {
        // No client computation is in-progress and "ping" failed.
        try
        {
            System.out.println("ClientProxy.evict: logging out client.");
            ((Hsp) serviceImpl).logout();
        }
        catch ( RemoteException e )
        {
            // !! Do something?
        }

        System.out.println( "HspProxy.evict: logging out client." );
    }    
}
