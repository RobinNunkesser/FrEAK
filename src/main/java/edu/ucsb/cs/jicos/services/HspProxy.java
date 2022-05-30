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
 * Is a Remote proxy for a Hsp. It thus implements the following methods:
 *    acknowledgeClientRegistration (omit?)
 *    addSessionStatistics (rename AddTaskStatistics)
 *    getTaskServer
 *    putResult
 *    registerTaskServer (CommandSynchronous)
 *    taskException
 *
 * @author  Peter Cappello
 */
final class HspProxy extends Proxy 
{
    // constants
    private static final long TERM = 1000 * 1000; // 1000 seconds
    
    HspProxy( ServiceName serviceName, ServiceImpl serviceImpl,
              RemoteExceptionHandler remoteExceptionHandler ) 
    {
        super ( serviceName, serviceImpl, remoteExceptionHandler, TERM );
    }
    
    HspProxy( Service service, ServiceImpl serviceImpl,
              RemoteExceptionHandler remoteExceptionHandler ) 
    {
        super ( service, serviceImpl, remoteExceptionHandler, TERM );
    } 
    
    public void evict() 
    { 
        System.err.println("HspProxy.evict: " + serviceName() );
        // Do something?
    }
}