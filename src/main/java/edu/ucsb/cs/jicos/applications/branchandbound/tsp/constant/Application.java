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
 * @version 1.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.constant;

import edu.ucsb.cs.jicos.applications.branchandbound.tsp.*;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.shared.*;


public class Application 
{
    public static void main (String args[]) throws Exception
    {
        if  ( args.length != 2 ) 
	    {
		System.out.println("Command line: <HSP DomainName> <number>"); 
		System.exit(1);
	    }
        // get Hsp machine's domain name from the command line.
        String hspDomainName = args[ 0 ];
        
        // construct pseudo-random TSP instance
        int  nodes = Integer.parseInt( args[1] );
        long seed = nodes;
        int  maxEdgeWeight = nodes;
        TSP tsp = new TSP( nodes, seed, maxEdgeWeight );
        
        // construct Shared upperBound
        Shared upperBound = new IntUpperBound( Integer.MAX_VALUE );
        
        // construct the computation's Environment
        Environment environment = new Environment( tsp, upperBound );
        
        // construct root task
        Solution emptySolution = new TspSolution( nodes );
        Task task = new BranchAndBound( emptySolution );
        
        // get a reference to a Host Service Provider
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();
            
        // login
        hsp.login( environment );

        // compute
        Solution solution = (Solution) hsp.compute( task );
            
        // logout
        Invoice invoice = hsp.logout();
            
        // print the solution & invoice
        System.out.println( solution );
        System.out.println( invoice );
    }
}
