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
 * Application.java
 *
 * Created on June 9, 2003, 11:15 PM
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.applications.branchandbound.tsp.*;
import edu.ucsb.cs.jicos.services.shared.*;
import edu.ucsb.cs.jicos.applications.utilities.graph.*;
import java.io.File;


final class Application 
{
    public static void main ( String args[] ) throws Exception
    {
//        if (args.length < 4) {
//            System.out.println("Usage: Application hsp num_cities seed zoom [initial_upper_bound]");
//            System.exit(1);
//        }
//        String hspname = args[0];
//        int num_cities = Integer.parseInt(args[1]);
//        int seed = Integer.parseInt(args[2]);
//        int zoom = Integer.parseInt(args[3]);
//        
//        Graph tsp = new GraphEuclidean(num_cities, seed, zoom);
//        System.out.println(tsp.toString());
        if (args.length < 3) {
            System.out.println("Usage: Application hsp tsplib_file held_karp_iterations [initial_upper_bound]");
            System.exit(1);
        }
        
        String hspname = args[0];
        String tsplib_file = args[1];
        int iters = Integer.parseInt(args[2]);
        Graph tsp = TSPLIBReader.parse(new File(tsplib_file));
        int cost = 0;
//        if (args.length > 4) {
//            cost = Integer.parseInt(args[4]);
//        }
        if (args.length > 3) {
            cost = Integer.parseInt(args[3]);
        }
        else {
            // Compute upper bound
            edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound ub 
            = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound(tsp);
            int[] tour = ub.getTour();
            int j = tour[0];
    //        System.out.print(" Tour: \n   " + j );
            for ( int i = 1; i < tour.length; i++ )
            {
    //            System.out.print(" " + tour[i] );
                cost += tsp.getCost( j , tour[i] );
                j = tour[i];
            }        
            cost += tsp.getCost( j , tour[0] );
        }
        // cost = Integer.MAX_VALUE
        System.out.println("Initial Upper Bound: " + cost);
        // construct Shared upperBound
        Shared upperBound = new IntUpperBound( cost );
//        Shared upperBound = new IntUpperBound( 7786 );

        // construct the computation's Environment
        Environment environment = new Environment( tsp, upperBound );

        // construct root task
        Solution emptySolution = new TSPMSTSolution( iters );
        Task task = new BranchAndBound( emptySolution );

        // get a reference to a Hosting Service Provider
        //HspAgent agent = new HspAgent( "test" );
        HspAgent agent = new HspAgent( hspname );
        //HspAgent agent = new HspAgent("127.0.0.1" );
        Client2Hsp hsp = agent.getClient2Hsp();

        // login
        hsp.login( environment );

        // compute
        hsp.setComputation(task);
        //Solution solution = (Solution) hsp.compute( task );

        Result result = hsp.getResult();
        
        Solution solution = (Solution) result.getValue();

        // logout
        Invoice invoice = hsp.logout();

        // print the solution & invoice
        System.out.println( solution );
        System.out.println( invoice );
        
        System.out.println("SOLUTION: " + arrayToString(((TSPMSTSolution)solution).getTour()));        
        System.exit(0);
    }
    
    public static String arrayToString(int[] array)
    {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < array.length - 1; i++) 
        {
            output.append(array[i] + ", ");
        }
        output.append(array[array.length-1]);        
        return new String( output );    
    }
}
