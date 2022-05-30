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

package edu.ucsb.cs.jicos.examples.nfibonacci;

import edu.ucsb.cs.jicos.examples.fastfibonacci.*;
import edu.ucsb.cs.jicos.services.*;


final class Application 
{
    public static void main ( String args[] ) throws Exception
    {
        // set hspDomainName to the domain name of a machine running an Hsp
        String hspDomainName = args[0];
        
        // get a reference to a Hosting Service Provider
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();
            
        // login
        Environment environment = new Environment( null, null );
        hsp.login( environment );

        // compute f( n, m ) = f(f( ... f(m)) ... ), where composition occurs n times
        int n = Integer.parseInt( args[1] );       
        int m = Integer.parseInt( args[2] );
        for ( int i = 1, k = m; i <= n; i++ )
        {
            Task task = new F( k );
            Integer fnm = (Integer) hsp.compute( task );
            k = fnm.intValue();
            System.out.println("F( " + i + ", " + m + " ) = " + k );
        }

        hsp.logout();
    }
}
