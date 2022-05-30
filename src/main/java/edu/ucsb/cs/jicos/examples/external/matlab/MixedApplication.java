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
 * @author  Andy Pippin
 */
package edu.ucsb.cs.jicos.examples.external.matlab;

import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.Client2Hsp;
import edu.ucsb.cs.jicos.services.Environment;
import edu.ucsb.cs.jicos.services.HspAgent;
import edu.ucsb.cs.jicos.services.Task;

import java.rmi.UnmarshalException;
import java.util.logging.Logger;
import java.util.logging.Level;


public final class MixedApplication
{
    private static final Logger logger = LogManager.getLogger();
    private static final Level DEBUG = LogManager.DEBUG;


    public static void main ( String args[] ) throws Exception
    {
        // Command-line argument is domain name of machine running an Hsp
	if( 1 > args.length ) {
	    System.err.println( "Usage <java> MixedApplication hsp" );
	    System.exit( 1 );
	}

        String hspDomainName = args[0];

        // Get a remote reference to an Hsp
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();
	logger.log( DEBUG, "Got HSP at \"" + hspDomainName + '"' );

        // Login
        Environment environment = new Environment( null, null );
        hsp.login( environment );
	logger.log( DEBUG, "Logged in to HSP" );
        

        // Compute
	try
	{
	    Task task = new Mixed();
	    logger.log( DEBUG, "Submitting job" );
	    double[][] mixed = (double[][]) hsp.compute( task );
        
	    System.out.println("And the answer is..." );
	    System.out.println( MatrixInverse.matrixToString( mixed ) );

	    // Logout
	    hsp.logout();
	}
	catch( UnmarshalException unmarshalException )
	{
	    System.out.flush();
	    System.err.println( "java.rmi.UnmarshalException: " +
					    unmarshalException.getMessage() );
	    System.err.flush();
	}
    }

}
