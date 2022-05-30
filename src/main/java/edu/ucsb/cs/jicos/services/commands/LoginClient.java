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
 * Login client command
 *
 * @version 1
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services.commands;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.foundation.*;


public final class LoginClient implements Command 
{
    private SessionInfo sessionInfo;

    public LoginClient( SessionInfo sessionInfo ) 
    {
        this.sessionInfo = sessionInfo;
    }
    
    public void execute( Proxy proxy ) 
    {         
        if ( proxy instanceof HostProxy )
        {
            ((HostProxy) proxy).loginClient( sessionInfo );
        }
        else
        {
            proxy.sendCommand( this );
        }
    }

    public void execute( ServiceImpl myService ) throws Exception
    {
        if ( myService instanceof TaskServer )
        {
            ((TaskServer) myService).loginClient( sessionInfo );
        }
        else
        {
            ((Host) myService).loginClient( sessionInfo );
        }
    }   
    
    public String toString() { return getClass() + ": " + sessionInfo.toString(); }
}
