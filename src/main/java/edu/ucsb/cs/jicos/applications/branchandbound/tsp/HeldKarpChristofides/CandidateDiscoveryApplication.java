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


final class CandidateDiscoveryApplication 
{
    public static void main ( String args[] ) throws Exception
    {
//        String[] filenames = {"a280.tsp", "ali535.tsp", "att532.tsp", "bier127.tsp", "brg180.tsp", "ch130.tsp", "ch150.tsp", "d198.tsp", "d493.tsp", "d657.tsp", "eil101.tsp", "fl417.tsp", "gil262.tsp", "gr120.tsp", "gr137.tsp", "gr202.tsp", "gr229.tsp", "gr431.tsp", "gr666.tsp", "kroA200.tsp", "kroB200.tsp", "lin318.tsp", "linhp318.tsp", "p654.tsp", "pa561.tsp", "pcb442.tsp", "pr107.tsp", "pr124.tsp", "pr136.tsp", "pr144.tsp", "pr152.tsp", "pr226.tsp", "pr264.tsp", "pr299.tsp", "pr439.tsp", "rat195.tsp", "rat575.tsp", "rd400.tsp", "si175.tsp", "si535.tsp", "ts225.tsp", "tsp225.tsp", "u159.tsp", "u574.tsp"};
//        int[] costs =        { 2579,      202339,       27686,        118282,        1950,         6110,        6528,        15780,      35002,      48912,      629,          11861,       2378,         6942,        69853,       40160,       134602,      171414,      294358,      29368,         29437,         42029,        41345,          34643,      2763,        50778,        44303,       59030,       96772,       58537,       73682,       80369,       49135,       48191,       107217,      2323,         6773,         15281,       21407,       48450,       126643,      3916,         42080,      36905     };
        String[] filenames = {"a280.tsp", "ali535.tsp", "att532.tsp", "bier127.tsp", "brg180.tsp", "ch130.tsp", "ch150.tsp", "d198.tsp", "d493.tsp", "d657.tsp", "eil101.tsp", "fl417.tsp", "gil262.tsp", "gr120.tsp", "gr137.tsp", "gr202.tsp", "gr229.tsp", "gr431.tsp", "gr666.tsp", "kroA200.tsp", "kroB200.tsp", "lin318.tsp", "linhp318.tsp", "p654.tsp", "pa561.tsp", "pcb442.tsp", "pr107.tsp", "pr124.tsp", "pr136.tsp", "pr144.tsp", "pr152.tsp", "pr226.tsp", "pr264.tsp", "pr299.tsp", "pr439.tsp", "rat195.tsp", "rat575.tsp", "rd400.tsp", "si175.tsp", "si535.tsp", "ts225.tsp", "tsp225.tsp", "u159.tsp", "u574.tsp"};
        int[] costs =        { 2579,      202339,       27686,        118282,        1950,         6110,        6528,        15780,      35002,      48912,      629,          11861,       2378,         6942,        69853,       40160,       134602,      171414,      294358,      29368,         29437,         42029,        41345,          34643,      2763,        50778,        44303,       59030,       96772,       58537,       73682,       80369,       49135,       48191,       107217,      2323,         6773,         15281,       21407,       48450,       126643,      3916,         42080,      36905     };

        if (args.length < 3) {
            System.out.println("Usage: CandidateDiscoveryApplication hsp tsplib_directory held_karp_iterations [total_delay] [wait_delay]");
            //System.out.println("  tsplib_directory can be set to test for 10 random 25 node instances");
            System.out.println("  total_delay defaults to 6 hours.");
            System.out.println("  wait_delay defaults to 10 minutes.");
            System.exit(1);
        }
        
        String hspname = args[0];
        String tsplib_dir = args[1];
        
        // get a reference to a Hosting Service Provider
        HspAgent agent = new HspAgent( hspname );
        Client2Hsp hsp = agent.getClient2Hsp();
        
        int iters = Integer.parseInt(args[2]);        
        long delaymillis = 6 * 60 * 60 * 1000;
        if (args.length >= 4 && args[3] != null) { delaymillis = Long.parseLong(args[3]); }
        int waitfor = 10 * 60 * 1000;        
        if (args.length >= 5 && args[4] != null) { waitfor = Integer.parseInt(args[4]); }
        
        for (int trial = 11; trial < filenames.length; ++trial) {
          try {
            Graph tsp = TSPLIBReader.parse(new File(tsplib_dir + filenames[trial]));
            if (tsp == null) {
              System.out.println("\n\n" + filenames[trial] + " was unacceptable, continuing.");
              continue;
            } else {
              System.out.println("Parsed: " + filenames[trial]);
              System.gc();
              Thread.yield();
            }
            int cost = costs[trial];
        
//        // BEGIN TEST SECTION
//        for (int trial = 0; trial < 10; ++trial) {
//          try {
//            Graph tsp = new GraphEuclidean(25, trial, 10000);
//            System.out.println("Seed: " + trial);
//            System.out.println("Ignore tsp filenames below.");
//            System.gc();
//            Thread.yield();
//
//            int cost = 0;
//            edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound ub 
//            = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound(tsp);
//            int[] tour = ub.getTour();
//            int j = tour[0];
//            for ( int i = 1; i < tour.length; i++ )
//            {
//                cost += tsp.getCost( j , tour[i] );
//                j = tour[i];
//            }        
//            cost += tsp.getCost( j , tour[0] );
//            
//            // END TEST SECTION
            
            System.out.println("Initial Upper Bound: " + cost);

            // construct Shared upperBound
            Shared upperBound = new IntUpperBound( cost );

            // construct the computation's Environment
            Environment environment = new Environment( tsp, upperBound );

            // construct root task
            Solution emptySolution = new TSPMSTSolution( iters );
            Task task = new BranchAndBound( emptySolution );


            // login
            hsp.login( environment );

            // compute
            ResultId resid = hsp.setComputation(task);
            //Solution solution = (Solution) hsp.compute( task );

            // special candidate while loop 
            while (delaymillis > 0) {
              if (hsp.isComplete(resid)) {
                break;
              } else {
                Thread.sleep(waitfor);
                delaymillis -= waitfor;
              }
            }

            if (hsp.isComplete(resid)) {
              Result result = hsp.getResult();

              Solution solution = (Solution) result.getValue();

              // logout
              Invoice invoice = hsp.logout();

              // print the solution & invoice
              System.out.println( solution );
              System.out.println( invoice );

              System.out.println("SOLUTION: " + arrayToString(((TSPMSTSolution)solution).getTour()));        
              System.out.println("\n\n" + filenames[trial] + " processing completed\n");
            } else {
              // logout
              Invoice invoice = hsp.logout();
              System.out.println( invoice );
              System.out.println("\n\n" + filenames[trial] + " is not a good candidate\n");
            }
          }
          catch (Exception e) {
            e.printStackTrace();
            System.err.println("Continuing after " + filenames[trial]);
            try {
              System.out.println("Attempting (extra) logout.");
              hsp.logout();
            } catch (Exception ignore) {
              ignore.printStackTrace(System.out);
              System.out.println("System didn't appreciate additional logout.");
            }
          }
        }
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
