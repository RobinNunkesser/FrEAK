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

/**
 * @author  Peter Cappello
 */
final class JicosRemoteExceptionHandler extends RemoteExceptionHandler 
{    
    JicosRemoteExceptionHandler()
    {
        super();
//        System.out.println ( "JicosRemoteExceptionHandler.constructed." );
    }
    public void handle( Exception exception, Service fromAddress, Service toAddress )
    {
        System.out.println ( "JicosRemoteExceptionHandler.handle: entered." );
        super.handle( exception, fromAddress, toAddress );
        System.out.println("JicosRemoteExceptionHandler.handle: fromAddress: " + fromAddress);
        System.out.println("JicosRemoteExceptionHandler.handle: toAddress: " + toAddress);
        System.out.println("JicosRemoteExceptionHandler.handle: fromAddress instanceof TaskServer: " + (fromAddress instanceof TaskServer));
        System.out.println("JicosRemoteExceptionHandler.handle: toAddress instanceof Host: " + (toAddress instanceof Host));
        if ( fromAddress instanceof TaskServer && toAddress instanceof Host )
        {
            System.out.println ( "Host unavailable." );
            
            TaskServer taskServer = (TaskServer) fromAddress;
            taskServer.unregisterHost( toAddress );
        }
    }
}