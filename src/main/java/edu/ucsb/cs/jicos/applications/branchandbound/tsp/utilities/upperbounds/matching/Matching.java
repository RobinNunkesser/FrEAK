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
 * UpperBoundMatching.java
 * The matching algorithm assumes an even number of vertices. Thus, when the
 * costs matrix is of odd degree, it is padded with 1 extra DUMMY vertex.
 *
 * Created on June 3, 2003, 9:14 AM
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.matching;

import java.util.*;

import edu.ucsb.cs.jicos.applications.utilities.graph.*; // needed by main

public final class Matching 
{
    //
    // CONSTANTS
    //
    private final static int K = 4; // If |V| <= K, search exhaustively.
    private final static int DUMMY_DISTANCE = 1000000; //Integer.MAX_VALUE;
    
    //
    // ATTRIBUTES
    //
    /* distances for TSP instance, padded with 1 DUMMY vertex, which is needed
     * on occassion.
     */
    private int[][] distances; 
    private int dummyIndex;
    
    /* endpoint[k][l][0] = true means the edge connecting Path k to Path l
     *     has Path[k].i() as an endpoint; 
     *     else it has Path[k].j() as an endpoint.
     * endpoint[k][l][1] = true means the edge connecting Path k to Path l
     *     has Path[l].i() as an endpoint; 
     *     else it has Path[l].j() as an endpoint.
     */
    private boolean[][][] endpointIsI;
    
    // A DUMMY vertex has been inserted to make distances.length % 2 == 0
    private boolean hasDummy;
    
    /* DO NOT USE paths.length; it is reused with successively smaller sets of
     * Path objecs.
     */    
    private Path[]  paths;
    private int[][] pathDistances; // distances between Path objects
    private int     problemSize;   // the CURRENT matching problem's size
    private int[]   tour;
    private int     tourSize;      // the TSP's size
    
    //
    // DEBUG METHODS
    //
    void endpointIsI( boolean[][][] endpointIsI )
    {
        this.endpointIsI = endpointIsI;
    }   
    
    //
    //  PUBLIC METHODS 
    //    
    public Matching( int[][] costs ) 
    {        
        if ( costs.length < 3 )
        {
            throw new IllegalArgumentException("distances.length must be > 2.");
        }        
        tour = new int[ costs.length ];
        tourSize = costs.length;
        dummyIndex = costs.length;
        
        // make room for DUMMY vertex, which is used occassionally        
        int length = costs.length + 1;
        distances = new int[ length ][ length ];
        for ( int i = 0; i < costs.length; i++ )
        {
            for ( int j = 0; j < costs.length; j++ )
            {
                distances[i][j] = costs[i][j];
            }
            distances[ i ][ dummyIndex ] = DUMMY_DISTANCE;
            distances[ dummyIndex ][ i ] = DUMMY_DISTANCE; // causes cache misses & page faults
        }
        distances[ dummyIndex ][ dummyIndex ] = 0; 

        problemSize = costs.length;
        
        // If problemSize <= K, skip DUMMY insert; directly invoke minCostTour()
        if ( costs.length % 2 == 1 && costs.length > K )
        {
            hasDummy = true; // a DUMMY vertex is used in 1st iteration
            problemSize++;
        }
    }
    
    public int[] getTour() { return tour; }
    
    /** 
     * returns a solution: an upper bound for the minimum tour
     */
    public void setTour()
    {    
        for ( initializeMatchProblem(); problemSize > K; setNextMatchProblem() )
        {                       
            Graph graph = new GraphImpl( pathDistances );
            int[] matches = graph.getMinCostMaxMatch();
            
            connectMatedPaths( matches );   
        }    
        assert problemSize <= K;
        
        // find minimal tour connecting K or fewer Paths.
        tour = minCostTour();
        
        /*
        System.out.print("setTour: tour: ");
        for ( int i = 0; i < tour.length; i++ )
        {
            System.out.print(" " + tour[i]);
        }  
         */
    }
    
    //
    // PACKAGE-PRIVATE METHODS
    //
    boolean[][][] endpointIsI() { return endpointIsI; }
    
    //
    //  PRIVATE METHODS 
    //    
    /**
     * Connect mated Path objects & compact them in paths array; 
     * halve problemSize.
     *
     * If there is a DUMMY, it is someone's mate. 
     * DOES NOT CONNECT THE DUMMY TO ITS MATE.
     */
    private void connectMatedPaths( int[] mates )
    {
        // connect mated Path objects
        for ( int i = 0; i < mates.length; i++ )
        {
            // If there is a DUMMY, do not connect to it 
            if ( i < mates[i] && ( ! hasDummy || mates[i] != mates.length - 1 ) )
            {
                // add the 2 path lists; sum their costs
                paths[i].add( paths[ mates[i] ] );
                
                // add the cost of the connecting edge
                int connectingCost = distances[ i ] [ mates[ i ] ];
                paths[i].addCost( connectingCost );
            }
        }
        
        /* Move Path objecs to bottom of paths array. 
         * There are exactly half as many Path objects.
         */
        int j = 0;
        for ( int i = 0; i < mates.length; i++ )
        {
            if ( i < mates[i] /*&& ( ! hasDummy || mates[i] != mates.length - 1 )*/ )
            {
                paths[j] = paths[i];
                paths[j].index( j++ );
            }
        }
        assert j == mates.length / 2;
        problemSize /= 2;
           
        /*
        System.out.println("connectMatedPaths: problemSize: " + problemSize);
        for ( int i = 0; i < problemSize; i++ )
        {
            System.out.println("connectMatedPaths: i: " + i + " path.index: " + paths[i].index()
            + " path.cost: " + paths[i].cost() + " start vertex: " + paths[i].i() +
            " end vertex: " + paths[i].j() );
        }
         */
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
     * For each "vertex", i, in skeleton tour, substitute the path in 
     * paths[ skeletonTour[i] ] according to orientation given by orientation.
     */
    private int[] expandSkeleton( int[] skeletonTour, Orientation orientation )
    {        
        int[] circuit = new int[ tourSize ];
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
        
        /* Vary the n Path objects over all 2^n distinct orientations.
         * Compute the cost of each corresponding tour, tracking the minimum
         * cost tour and its Path orientations.
         */
        int minCost = Integer.MAX_VALUE;
        int minOrientation = 0; // orientation bit string of min cost tour        
        for ( int j = 0; j < bitSet; j++ )
        {            
            int cost = pathCosts; // init to cost of Path objects.

            //orient the 1st Path object.
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
                cost += distances[ previousPathLastVertex ][ currentPathFirstVertex ];
                
                // set up for next iteration
                previousPathLastVertex = ( bits % 2 == 0 ) ? lastVertex : firstVertex;
            }
            
            /* complete tour: connect last vertex of last Path object to first 
             * vertex of 0th Path object.
             */
            cost += distances[ previousPathLastVertex ][ firstTourVertex ];
                
            if ( cost < minCost )
            {
                minCost = cost;
                minOrientation = j;
            }                   
        }
        return new Orientation( minOrientation, minCost );
    }
   
    private void initializeMatchProblem()
    {
        initializePaths();
        
        // construct adjacency matrix between Path objects & set endpoints
        pathDistances = new int[ problemSize ][ problemSize ];  
        
        initializePathDistances( pathDistances, endpointIsI );
    } 
    
    /** for every 2-set of Path objects, set their distance & endpointIsI values
     *
     * Does these things for DUMMY Path object, if it is part of this problem.
     *
     * !! Currently, I am making a new endpointIsI matrix. After I
     *    get this working, I will think about modifying it IN-PLACE.
     */
    private void initializePathDistances( int[][]    newDistances,
                                          boolean[][][] newEndpointIsI )
    {
        for ( int i = 0;     i < problemSize; i++ )
        for ( int j = i + 1; j < problemSize; j++ )  
        {
            Edge edge = minEdgeBetweenPaths( paths[i], paths[j] );
            newDistances[i][j] = edge.cost();
            newDistances[j][i] = edge.cost(); // make a full distances matrix
            
            newEndpointIsI[i][j][0] = edge.i();
            newEndpointIsI[i][j][1] = edge.j();
            
            newEndpointIsI[j][i][0] = edge.j();
            newEndpointIsI[j][i][1] = edge.i();
        }
    }    
    
    /**
     * For each vertex, i, construct the ith Path object, initialized to the 
     * 0-length path from i to i.
     *
     * If there is a DUMMY vertex, a Path object is constructed for it.
     */
    private void initializePaths()
    {
        endpointIsI = new boolean[ problemSize ][ problemSize ][ 2 ];
        paths = new Path[ distances.length ];
        for ( int i = 0; i < distances.length; i++ )
        {
            paths[i] = new Path( this, i );
        }
    }
    
    /**
     * returns minimal tour when  2 < |V| < 5.
     * assumes distances array is symmetric.
     */
    private int[] minCostTour()
    { 
        // pre-conditions
        assert 2 < problemSize && problemSize <= K;
        
        switch ( problemSize )
        {
            case 3:
                return tour3();
            case 4:
                return tour4();
            default:
               assert false; 
               return new int[0]; // the compiler made me write this.
        }
    }
    
    private Edge minEdgeBetweenPaths( Path pathI, Path pathJ )
    {
        // establish the costs of the 4 edges between the 2 Path objects
        int ii = distances[ pathI.i() ][ pathJ.i() ];
        int ij = distances[ pathI.i() ][ pathJ.j() ];
        int ji = distances[ pathI.j() ][ pathJ.i() ];
        int jj = distances[ pathI.j() ][ pathJ.j() ];
        
        //System.out.println("minEdgeBetweenPaths: " + ii + " " + ij + " " + ji + " " + jj);
        
        // find smallest edge between the 2 Path objects
        int min = ii;
        if ( ij < min ) min = ij;
        if ( ji < min ) min = ji;
        if ( jj < min ) min = jj;
        
        //System.out.println("minEdgeBetweenPaths: min: " + min);
        
        // set endpointIsI values
        boolean i, j;
        if ( min == ii )
        {
            i = true;
            j = true;
        }
        else if ( min == ij )
        {
            i = true;
            j = false;
        }
        else if ( min == ji )
        {
            i = false;
            j = true;
        }
        else // min == jj
        {
            i = false;
            j = false;
        }        
       
        return new Edge( min, i, j);
    }  
    
    /** Update costs & endpointIsI matrices, which now represent half as many
     * Path objects. If "half as many" is odd, insert a DUMMY vertex.
     *
     * !! Currently, I am making new distances and endpointIsI matrices. After I
     *    get this working, I will think about modifying these IN-PLACE.
     */
    private void setNextMatchProblem()
    {       
        // Will next iteration need a DUMMY vertex?
        if ( problemSize % 2 == 0 || problemSize <= K )
        {
            hasDummy = false;            
        }
        else
        {
            // make DUMMY
            hasDummy = true;            
            paths[problemSize] = paths[ dummyIndex ];
            problemSize++; // reduced problem needs an even number of nodes.
        }                 
    
        int[][] newDistances = new int[problemSize][problemSize];
        boolean[][][] newEndpointIsI = new boolean[problemSize][problemSize][2];
            
        initializePathDistances( newDistances, newEndpointIsI );
        pathDistances = newDistances;
        endpointIsI = newEndpointIsI;
    }
    
    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append("Matching tour: ");
        for ( int i = 0; i < tour.length; i++ )
        {
            s.append( tour[i] );
            s.append( " " );
        }
        return new String( s );
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
    }
    
    public static void main( String[] args ) throws Exception
    {
        int start      = Integer.parseInt( args[0] );
        int stop       = Integer.parseInt( args[1] );
        int increment  = Integer.parseInt( args[2] );
        //int general    = Integer.parseInt( args[3] );
        //boolean isGeneral = ( general == 0 ) ? true : false;
        System.out.println("Comparison of Christofides with Cappello's 1st matching heuristic:");
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
        
        System.out.println("\tNodes: \tUB0 \tTime \tUB1 \tTime");
        
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
            
            // compute tour via Cappello's 1st matching algorithm
            Matching mTsp = new Matching( distances );
            startTime = System.currentTimeMillis();
            mTsp.setTour();
            stopTime = System.currentTimeMillis();
            tour = mTsp.getTour();
            cost = getCost( tour, distances );
            System.out.print(" \t" + cost + " \t" + ((stopTime - startTime)/1000) + "s. " );
            
            /*
            // compute tour via Cappello's 2nd matching algorithm
            edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello2.UpperBound ub2;
            ub2 = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello2.UpperBound( distances );
            startTime = System.currentTimeMillis();
            tour = ub2.getTour();
            stopTime = System.currentTimeMillis();            
            cost = getCost( tour, distances );
            System.out.print(" \t" + cost + " \t" + ((stopTime - startTime)/1000) + "s. " );
             */
            
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
