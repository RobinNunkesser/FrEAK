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

package edu.ucsb.cs.jicos.examples.external.matlab;

import edu.ucsb.cs.jicos.services.*;


final class Application
{
    public static void main ( String args[] ) throws Exception
    {
        // Command-line argument is domain name of machine running an Hsp
        if( 1 > args.length ) {
            System.err.println( "Usage <java> Application hsp" );
            System.exit( 1 );
        }

        String hspDomainName = args[0];

        // Get a remote reference to an Hsp
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();

        // Login
        Environment environment = new Environment( null, null );
        hsp.login( environment );
        
        double[][] matrix = 
        {
            { 2.0, 0.0, 0.0 },
            { 0.0, 4.0, 0.0 },
            { 0.0, 0.0, 8.0 }
        };

        // Compute
        Task task = new MatrixInverse( matrix );
        double[][] inverse = (double[][]) hsp.compute( task );
       
	    // Display result.
        for ( int r = 0; r < inverse.length; r++ )
        {
            System.out.print( "| " );
            for ( int c = 0; c < inverse[0].length; c++ )
            {
                System.out.print( inverse[r][c] + " " );
            }
            System.out.println( " |" );
        }

        // Logout
        hsp.logout();
    }
}
