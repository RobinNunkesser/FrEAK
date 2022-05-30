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
 * The matching algorithm assumes an even number of vertices. Thus, when the
 * costs matrix is of odd degree, it is padded with 1 DUMMY vertex.
 *
 * Break cycles based on global information: It performs a min weight max 
 * matching to select a bridge node for a cycle based on global information.
 * Once the bridges are selected, 1 of the cycle edges adjacent to each bridge 
 * node must be deleted. This too is based on global information: A cost matrix
 * is constructed that for each path, uses the nearest endpoint (of the 2
 * potential endpoints defined by the bridge). Then, a min weight max matching 
 * is performed. Based on what edge is matched, we delete the other potential
 * endpoint.
 *
 * Created on September 5, 2003, 6:03 PM
 *
 * @author  Peter Cappello
 */

/* Smart Iterative Matching
 *
 * Performance: 
 *    1. Replace MergeFind with Tarjan's UnionFind.
 *    2. Replace LinkedList in Path with List whose insert & reverse are O(1).
 *    3. Increase K to 5. Write general code, given K.
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello3;

import edu.ucsb.cs.jicos.applications.utilities.set.MergeFindSet;
import edu.ucsb.cs.jicos.applications.utilities.graph.*; // needed by main
import java.util.*;

// imports needed to run Jicos in main
//import java.rmi.*;
//import edu.ucsb.cs.jicos.applications.branchandbound.*;
//import edu.ucsb.cs.jicos.applications.branchandbound.tsp.*;
//import edu.ucsb.cs.jicos.examples.tsp.*;
//import edu.ucsb.cs.jicos.services.*;
//import edu.ucsb.cs.jicos.services.shared.*;

// needed to compare with this heuristic
//import edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.matching.*;


public class UpperBound 
{
    //
    // CONSTANTS
    //
    private final static int K = 4; // if |V| <= K, search exhaustively.
    private final static int BIG_DISTANCE = 1000000;
    
    
    //
    // ATTRIBUTES
    //
    private int[][] costs; // this is input and final
    
    // A DUMMY vertex has been inserted to make distances.length % 2 == 0
    private boolean hasDummy;
    
    private Path[] paths;
    private int[]  tour;
    private int[]  weightsIndex2CostsIndex;
    private int[]  weightsIndex2PathsIndex;

    //
    //  PUBLIC METHODS 
    //    
    public UpperBound(int[][] costs) 
    {        
        if ( costs.length < 3 )
        {
            throw new IllegalArgumentException("distances.length must be > 2.");
        }        
        this.costs = costs;
    }
    
    /** 
     * returns a solution: an upper bound for the minimum tour
     */
    public int[] getTour() 
    { 
        if ( tour == null )
        {
            tour = setTour();
        }
        return tour; 
    }
    
    private int[] setTour()
    {        
        for ( int[][] weights = initializeProblem(); weights.length > K; weights = setNextMatchProblem() )
        {                        
            Graph graph = new GraphImpl( weights );
            int[] mates = graph.getMinCostMaxMatch();            
            connectMatedPaths( mates );
        }
        
        // find minimal tour connecting K or fewer Paths.
        assert paths.length <= K;
        return minCostTour();
    }
    
    //
    //  PRIVATE METHODS 
    //    
    /**
     * Connect mated Path objects & compact them in paths array.
     *
     * If there is a DUMMY, it is someone's mate. 
     * DOES NOT CONNECT THE DUMMY TO ITS MATE.
     */
    private void connectMatedPaths( int[] mates )
    {        
        Set newPaths = new HashSet();
        
        // Used in union-find of paths that are added
        MergeFindSet pathSet = new MergeFindSet( paths.length );
        
        for ( int i = 0; i < mates.length; i++ )
        {            
            if ( i > mates[i] || ( hasDummy && mates[i] == mates.length - 1 ) )
            {
                // already done || mated to DUMMY
                continue;
            }
            
            // determine path indices of mated nodes
            int iPathsIndex = weightsIndex2PathsIndex[ i ];
            int jPathsIndex = weightsIndex2PathsIndex[ mates[i] ];
            
            // get path object that now contains this path
            iPathsIndex = pathSet.find( iPathsIndex );
            jPathsIndex = pathSet.find( jPathsIndex );
            
            if ( iPathsIndex == jPathsIndex )
            {
                // matched edge connects the endpoints of same path
                paths[ iPathsIndex ].cycle();
            }
            else
            {
                // matched edge connects the endpoints of 2 different paths
                
                // determine which endpoints in these Path objects this edge matches
                int iCostsIndex = weightsIndex2CostsIndex[ i ];
                int jCostsIndex = weightsIndex2CostsIndex[ mates[i] ];            

                int iPathEndpoint = paths[ iPathsIndex ].i();
                if ( iCostsIndex != iPathEndpoint )
                {
                    iPathEndpoint = paths[ iPathsIndex ].j();
                }
                int jPathEndpoint = paths[ jPathsIndex ].i();
                if ( jCostsIndex != jPathEndpoint )
                {
                    jPathEndpoint = paths[ jPathsIndex ].j();
                }

                paths[ iPathsIndex ].add( iPathEndpoint, paths[ jPathsIndex ], jPathEndpoint );
            
                // establish new index for this new combined path
                pathSet.merge( iPathsIndex, jPathsIndex );

                // get new index, and set that array reference to the combined path
                int newIndex = pathSet.find( iPathsIndex );
                paths[ newIndex ] = paths[ iPathsIndex ];
            }
        }        
          
        
        // Establish new reduced set of paths
        for ( int i = 0; i < paths.length; i++ )
        {
            // What path does the initial path[i] now belong to?
            int pathIndex = pathSet.find( i );
            
            // put that path in a Set: no duplicates
            newPaths.add( paths[ pathIndex ] );
        }
        
        // the size of this set is the number of distinct paths remaining
        paths = new Path[ newPaths.size() ];
        
        int i = 0;
        for ( Iterator iterator = newPaths.iterator(); iterator.hasNext(); i++ )
        {
            paths[i] = (Path) iterator.next();
        }
    }
    
    private void copyPath2Tour( List list, int[] circuit, int startIndex )
    {
        Iterator iterator = list.iterator();        
        for ( int i = startIndex; iterator.hasNext(); i++ )
        {            
            circuit[i] = ((Integer) iterator.next()).intValue();
        }
    }
    
    /**
     * For each "node", i, in skeleton tour, substitute the path in 
     * paths[ skeletonTour[i] ] according to orientation given by orientation.
     */
    private int[] expandSkeleton( int[] skeletonTour, Orientation orientation )
    {        
        int[] circuit = new int[ costs.length ];
        int orientationBits = orientation.getOrientations();
        
        for ( int i = 0, tourIndex = 0; i < skeletonTour.length; i++ )
        {
            Path path = paths[ skeletonTour[ i ] ];            
            List list = ( orientationBits % 2 == 0 ) ? path.list() : path.reverse();
            
            copyPath2Tour( list, circuit, tourIndex );  
            
            // prepare for next iteration
            tourIndex += list.size(); // add path length to tourIndex
            orientationBits >>= 1;
        }

        return circuit;
    }       
    
    /** Returns the Orientation object that minimizes the circular permutation 
     * of Path objects given by the tour array.
     */
    private Orientation getMinPathOrientation( int[] tour )
    {
        int pathCosts = 0;
        for ( int i = 0; i < tour.length; i++ )
        {
            pathCosts += paths[i].cost();
        }
            
        // Generate 2^n, where n = tour.length
        int bitSet = 1;
        for ( int i = 0; i < tour.length; i++ )
        {
            bitSet *= 2;
        }        
        // assert bitset is 2^(tour.length)
        
        /* Vary the n Path objects over all 2^n distinct orientations.
         * Compute the cost of each corresponding tour, tracking the minimum
         * cost tour and its Path orientations.
         */
        int minCost = Integer.MAX_VALUE;
        int minOrientation = 0; // orientation bit string of min cost tour        
        for ( int j = 0; j < bitSet; j++ )
        {            
            int cost = pathCosts; // init tour cost to cost of Path objects.

            // orient the 1st Path object.
            int firstTourVertex        = ( j % 2 == 0 ) ? paths[tour[0] ].i() : paths[ tour[0] ].j();
            int previousPathLastVertex = ( j % 2 == 0 ) ? paths[tour[0] ].j() : paths[ tour[0] ].i();
                          
            for ( int k = 1, bits = j >> 1; k < tour.length; k++, bits >>= 1 )
            {                    
                // select first and last vertices from current Path object
                int firstVertex = paths[ tour[k] ].i();
                int lastVertex  = paths[ tour[k] ].j();
                int currentPathFirstVertex = ( bits % 2 == 0 ) ? firstVertex : lastVertex;
                
                /* increment cost by distance from last vertex of previous Path
                 * object to first vertex of current Path object
                 */
                cost += costs[ previousPathLastVertex ][ currentPathFirstVertex ];
                
                // set up for next iteration
                previousPathLastVertex = ( bits % 2 == 0 ) ? lastVertex : firstVertex;
            }
            
            /* complete tour: connect last vertex of last Path object to first 
             * vertex of 0th Path object.
             */
            cost += costs[ previousPathLastVertex ][ firstTourVertex ];
                
            if ( cost < minCost )
            {
                minCost = cost;
                minOrientation = j;
            }                   
        }
        return new Orientation( minOrientation, minCost );
    }
   
    private int[][] initializeProblem()
    {            
        // construct Path objects and paths array
        paths = new Path[ costs.length ];
        for ( int i = 0; i < costs.length; i++ )
        {
            paths[i] = new Path( i, costs );
        }
        
        return setNextMatchProblem();
    }  
    
    private Node[] makeNodesArray()
    {
        // compute length of nodes array
        int nodesLength = 0;
        for ( int i = 0; i < paths.length; i++ )
        {
            nodesLength++;
            if ( paths[i].i() != paths[i].j() )
            {
                // path[i] is neither a single node nor a cycle
                nodesLength++;
            }
        }
        
        Node[] nodes = new Node[ nodesLength ];
//        weightsIndex2CostsIndex = new int[ nodes.length ];
        weightsIndex2PathsIndex = new int[ nodes.length ];
        
        // initialize nodes array & mapping arrays
        for ( int i = 0, j = 0; i < paths.length; i++ )
        {
            weightsIndex2PathsIndex[j] = i;
            nodes[j] = paths[i].nodeI();  
//            weightsIndex2CostsIndex[j] = paths[i].i();           
            j++;
            if ( paths[i].i() != paths[i].j() )
            {
                weightsIndex2PathsIndex[j] = i;
                nodes[j] = paths[i].nodeJ();            
                weightsIndex2CostsIndex[j] = paths[i].j();           
                j++;
            }           
        }
        return nodes;
    }
    
    private int[][] makeWeightsArray( Node[] nodes )
    {
        // set length of weigths array
        int weightsLength = nodes.length;
        hasDummy = false;
        
        // is a dummy node needed? If so, incrememt length of weights array
        if ( nodes.length % 2 == 1 && nodes.length > K )
        {
            weightsLength++;
            hasDummy = true;
        }
        
        int[][] weights = new int[weightsLength][weightsLength];
        
        for ( int i = 0; i < nodes.length; i++ )
        {
            weights[i][i] = 0;
            for ( int j = 0; j < i; j++ )
            {
                if ( nodes[i].getMyPath() == nodes[j].getMyPath() )
                {
                    // edge connects endpoints of same path
                    weights[i][j] = weights[j][i] = BIG_DISTANCE;
                }
                else
                {
                    int k = nodes[i].getCostsIndex();
                    int l = nodes[j].getCostsIndex();
                    weights[i][j] = costs[k][l];
                    weights[j][i] = costs[l][k];
                }
            }
        }
        if ( hasDummy )
        {            
            for ( int i = 0; i < weights.length - 1; i++ )
            {
                weights[i][weights.length-1] = weights[weights.length-1][i] = BIG_DISTANCE;
            }       
            weights[weights.length-1][weights.length-1] = 0;
        }
        return weights;
    }
    
    /**
     * returns minimal tour when  2 < |V| < 5.
     * assumes distances array is symmetric.
     */
    private int[] minCostTour()
    { 
        // pre-conditions
        assert 1 <= paths.length && paths.length <= K;
        
        switch ( paths.length )
        {
            case 1:
                return tour1();
            case 2:
                return tour2();
            case 3:
                return tour3();
            case 4:
                return tour4();
            default:
               assert false; 
               return new int[0]; // the compiler made me write this.
        }
    }
    
    /** Update costs & endpointIsI matrices, which now represent half as many
     * Path objects. If "half as many" is odd, insert a DUMMY vertex.
     *
     * !! Currently, I am making new distances and endpointIsI matrices. After I
     *    get this working, I will think about modifying these IN-PLACE.
     */
    private int[][] setNextMatchProblem()
    {       
        // make a Node object for every path endpoint
        Node[] nodes = makeNodesArray(); // from paths array
        
        // make weights array, adding a DUMMY node, if |nodes| is odd
        return makeWeightsArray( nodes );
    }
    
    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append("Tour: ");
        for ( int i = 0; i < tour.length; i++ )
        {
            s.append( tour[i] );
            s.append( " " );
        }
        return new String( s );
    }
    
    private int[] tour1()
    {
        List list = paths[0].getList(); // tour already found
        int[] tour = new int[ list.size() ];
        int i = 0;
        for ( ListIterator iterator = list.listIterator(); iterator.hasNext(); i++ )
        {
            Integer integer = (Integer) iterator.next();
            tour[i] = integer.intValue();
        }
        return tour;
    }
    
    private int[] tour2()
    {
        int[] tour = { 0, 1 };
         Orientation orientation = getMinPathOrientation( tour );
         return expandSkeleton( tour, orientation );
    }
    
    private int[] tour3()
    {
         int[] tour = { 0, 1, 2 };
         Orientation orientation = getMinPathOrientation( tour );
         return expandSkeleton( tour, orientation );
    }
    
    private int[] tour4()
    {      
        int[] tour1 = { 0, 1, 2, 3 };
        Orientation orientation1 = getMinPathOrientation( tour1 );
        int cost1 = orientation1.getCost();
        int[] tour2 = { 0, 1, 3, 2 };
        Orientation orientation2 = getMinPathOrientation( tour2 );
        int cost2 = orientation1.getCost();
        int[] tour3 = { 0, 2, 1, 3 };
        Orientation orientation3 = getMinPathOrientation( tour3 );
        int cost3 = orientation1.getCost();
        
        if ( cost1 <= cost2 )
        {
            if ( cost1 <= cost3 )
            {
                return expandSkeleton( tour1, orientation1 );
            }
            else
            {
                return expandSkeleton( tour3, orientation3 );
            }
        }
        if ( cost2 < cost3 )
        {
            return expandSkeleton( tour2, orientation2 );
        }
        return expandSkeleton( tour3, orientation3 );
    }
   
    /** The orientations of Path objects in a tour
     */
    private class Orientation
    {
        private int orientations;
        private int cost;
        
        Orientation( int orientations, int cost )
        {            
            this.orientations = orientations;
            this.cost = cost;
        }
        
        int getCost() { return cost; }        
        int getOrientations() { return orientations; }
        
        public String toString() { return orientations + " cost: " + cost; }
    }
    
    public static void main( String[] args ) throws Exception
    {
        int start      = Integer.parseInt( args[0] );
        int stop       = Integer.parseInt( args[1] );
        int increment  = Integer.parseInt( args[2] );
        //int general    = Integer.parseInt( args[3] );
        //boolean isGeneral = ( general == 0 ) ? true : false;
        System.out.println("Comparison of Christofides with Cappello's 2nd matching heuristic:");
        compareToMatching( start, stop, increment, false );      
    }
    
    static private void compareToMatching( int start, int stop, int increment, boolean isGeneral )
           throws Exception
    {
        if ( isGeneral )
        {
            System.out.println("Random GENERAL graphs.");
        }
        else
        {
            System.out.println("Random EUCLIDEAN graphs.");
        }
        
        System.out.println("\tNodes: \tUB0 \tTime \tUB1 \tTime \tUB0/UB1");
        
        /*
        // get Hsp machine's domain name from the command line.
        String hspDomainName = "lysander";
        
        // get a reference to a Host Service Provider
        HspAgent agent = new HspAgent( hspDomainName );
        Client2Hsp hsp = agent.getClient2Hsp();
         */
        for ( int i = start; i <= stop; i += increment )
        {                        
            // construct graph
            int $nodes = i;
            int seed = $nodes;
            int magnification = 1000;
            Graph graph;
            if ( isGeneral )
            {
                graph = new GraphImpl( $nodes, seed, magnification);
            }
            else
            {
                graph = new GraphEuclidean( $nodes, seed, magnification);
            }

            int[][] distances = graph.getCosts();
            
            long startTime, stopTime;
            int[] tour;
            int cost;
            
            System.out.print("\t" + i);

            // compute tour via Christofides's algorithm
            edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound ubChristofides = 
            //new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound( distances );
            new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound( graph );
            startTime = System.currentTimeMillis();
            tour = ubChristofides.getTour();
            stopTime = System.currentTimeMillis();
            cost = getCost( tour, distances );
            System.out.print(" \t" + cost + " \t" + ((stopTime - startTime)/1000) + "s. " );
            
            float costC = cost; // saved for ratio

            
            /*
            // compute tour via Cappello's 1st matching algorithm
            Matching mTsp = new Matching( distances );
            startTime = System.currentTimeMillis();
            mTsp.setTour();
            stopTime = System.currentTimeMillis();
            tour = mTsp.getTour();
            cost = getCost( tour, distances );
            System.out.print(" \t" + cost + " \t" + ((stopTime - startTime)/1000) + "s. " );
             */
            
            // compute tour via Cappello's 2nd matching algorithm
            edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello2.UpperBound ub2;
            ub2 = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello2.UpperBound( graph );
            startTime = System.currentTimeMillis();
            tour = ub2.getTour();
            stopTime = System.currentTimeMillis();            
            cost = getCost( tour, distances );
            System.out.print(" \t" + cost + " \t" + ((stopTime - startTime)/1000) + "s. " );
            
            System.out.print(" \t" + (costC / cost) );
            
            System.out.println(" ");
                        
            /*
            // compute optimal tour                                    
            TSP tsp = new TSP( graph );        
            Shared upperBound = new IntUpperBound( mCost );
            Environment environment = new Environment( tsp, upperBound );
            edu.ucsb.cs.jicos.applications.branchandbound.Solution emptySolution = new TspSolution( $nodes );
            Task task = new BranchAndBound( emptySolution );
            hsp.login( environment );
            startTime = System.currentTimeMillis();
            Solution solution = (Solution) hsp.compute( task );
            stopTime = System.currentTimeMillis();
            if ( solution == null )
            {
                System.out.println("\tNULL");
            }
            else
            {
                int optimalCost = ((TspSolution) solution).getCost();
                System.out.print("\t" + optimalCost + "\t" + (stopTime - startTime) );
            
                float ratio = ((float) mCost/optimalCost);
                System.out.println( "\t" + ratio );
                
//                if ( ratio > maxRatio )
//                {
//                    maxRatio = ratio;
//                    System.out.println("Maximum ratio: " + maxRatio);
//                }   
                
            }            
            hsp.logout();
            //System.out.println( invoice ); 
             */ 
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
        cost += distances[ j ][ tour[0] ]; 
        return cost;
    }
}
