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
 * Simple TSP using branch &amp; bound
 *
 * @version 1.0
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.pete;

//import edu.ucsb.cs.jicos.applications.branchandbound.*;
import edu.ucsb.cs.jicos.applications.branchandbound.tsp.*;
import edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides.*;
import edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.*;
import edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.matching.*;
import edu.ucsb.cs.jicos.applications.utilities.graph.*;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.shared.*;


public class Application 
{
    public static void main( String[] args ) throws Exception
    {
        int start      = Integer.parseInt( args[0] );
        int stop       = Integer.parseInt( args[1] );
        int increment  = Integer.parseInt( args[2] );
        compareToMatching( start, stop, increment );  
    }
    
    static private void compareToMatching( int start, int stop, int increment )
           throws Exception
    {
        float maxRatio = 0;
        
        System.out.println("Random GENERAL graphs.");        
        System.out.println("\tNodes\tChrist\tTime\tMatch\tTime\tMatch/Christ\tOPT\tTime\tMatching/Opt");        
        
        // get Hsp machine's domain name from the command line.
        String hspDomainName = "lysander";
        
        // get a reference to a Host Service Provider
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();
        for ( int $nodes = start; $nodes <= stop; $nodes += increment )
        {
            long startTime, stopTime;
            int[] tour;           
            
            // construct graph
            int seed = $nodes;
            int magnification = 1000;
            Graph graph = new GraphImpl( $nodes, seed, magnification);
            int[][] distances = graph.getCosts();
            
            System.out.print("\t" + $nodes);
            
            // compute TSP for Christofides's algorithm
            UpperBound cTsp = new UpperBound( graph );
            startTime = System.currentTimeMillis();
            tour = cTsp.getTour();
            stopTime = System.currentTimeMillis();
            int cCost = getCost( tour, distances );
            System.out.print("\t" + cCost + "\t" + (stopTime - startTime) );
            
            // compute TSP for Matching algorithm
            Matching mTsp = new Matching( distances );
            startTime = System.currentTimeMillis();
            mTsp.setTour();
            stopTime = System.currentTimeMillis();
            tour = mTsp.getTour();
            //printTour( tour );
            int mCost = getCost( tour, distances );
            float matchingChristofidesRatio = (float) mCost / cCost;
            System.out.print("\t" + mCost + "\t" + (stopTime - startTime)  +
                                             "\t" + matchingChristofidesRatio );
                       
            // compute optimal tour                                    
            //TSP tsp = new TSP( graph );
            Graph tsp = new GraphEuclidean( $nodes, $nodes, 1000 );
            //edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides.TSP tsp = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides.TSP( $nodes ); 
            //Shared upperBound = new IntUpperBound( mCost );
            Shared upperBound = new IntUpperBound( 2724 );
            Environment environment = new Environment( tsp, upperBound );
            //edu.ucsb.cs.jicos.applications.branchandbound.Solution emptySolution = new TspSolution( $nodes );
            TSPMSTSolution emptySolution = new TSPMSTSolution( 2000 );
            Task task = new BranchAndBound( emptySolution );
            hsp.login( environment );
            startTime = System.currentTimeMillis();
            //TspSolution solution = (TspSolution) hsp.compute( task );
            TSPMSTSolution solution = (TSPMSTSolution) hsp.compute( task );
            stopTime = System.currentTimeMillis();
            int oCost = mCost;
            if ( solution != null )
            {
                oCost = solution.getCost();
            }
            float ratio = (float) mCost / oCost;
            System.out.println("\t" + oCost + "\t" + 
                                        (stopTime - startTime) + "\t" + ratio );                             
            hsp.logout();
        }        
    }
    
    // compute cost of tour
    static private int getCost( int[] tour, int[][] distances )
    {
        int cost = 0;
        int j = tour[0];
        //System.out.print(" Tour: \n   " + j );
        for ( int i = 1; i < tour.length; i++ )
        {
            //System.out.print(" " + tour[i] );
            cost += distances[ j ][ tour[i] ];
            j = tour[i];
        }        
        cost += distances[ tour[ tour.length - 1] ][ tour[0] ];
        //System.out.println("\n Cost: " + cost );  
        return cost;
    }
    
    static private void printTour( int[] tour )
    {
        System.out.print("\n Tour: ");
        for ( int i = 0; i < tour.length; i++ )
        {
            System.out.print( tour[i] + " " );
        }
        System.out.println("");
    }
}
