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
 *  An implementation of the Graph interface, where the vertices are points in the
 * Euclidean plane.
 *
 * Created on July 17, 2003, 9:51 AM
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.utilities.graph;

//import edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides.HeldKarpIterative;
//import edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides.MSOneTPrim;
//import edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides.TSP;
import edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides.TSPMSTSolution;

import edu.ucsb.cs.jicos.applications.branchandbound.tsp.BranchAndBound;
//import edu.ucsb.cs.jicos.applications.branchandbound.tsp.Solution;

import edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound;
//import edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.matching.*;
//import edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello2.*;
//import edu.ucsb.cs.jicos.examples.tsp.TspSolution;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.shared.*;
import java.util.Random;
import java.awt.*;


public final class GraphEuclidean implements Graph 
{
    // constants
    private final static int VERTEX_DIAMETER = 6;
    public final static int METRIC_GEO = 1;
    public final static int METRIC_EUC_2D = 2;
    private Vertex[] vertices;
    private int[][] distances;
    private int magnification;
    private int metric;
    private String name;
    private String comment;
    
    private int[] tour;
    private int[] upperBoundTour; // tour that is not necessarily of minimum cost

    /** Constructs a random graph, using seed as the seed for the random number
     * generator. The graph has n vertices in the unit square, which are
     * magnified by magnification. For example, if the x coordinate of a vertex
     * is 0.76543 and the magnification is 1000, then the magnified x coordinate
     * is 765 and the unit square maps to a 1000 X 1000 int grid.
     * @param $nodes Number of nodes in the graph.
     * @param seed The seed to use for the random number generator.
     * @param magnification The magnification factor. For example, if the x coordinate of a vertex
     * is 0.76543 and the magnification is 1000, then the magnified x coordinate
     * is 765 and the unit square maps to a 1000 X 1000 int grid.
     */
    public GraphEuclidean(int $nodes, int seed, int magnification )
    {
        setMetric( METRIC_EUC_2D );
        this.magnification = magnification;
        
        // construct randomly placed vertices
        Random random = new Random( seed );
        vertices = new Vertex[ $nodes ];
        for ( int i = 0; i < vertices.length; i++ )
        {
            vertices[i] = new Vertex( random.nextFloat(), random.nextFloat() );
        }
    }
    
    public GraphEuclidean(int $nodes, int metric, String name, String description) {
        setName(name);
        setComment(description);
        setMetric(metric);
        magnification = 1;
        vertices = new Vertex[ $nodes ];
    }
    
    public void setVertex(int index, Vertex vert) {
        vertices[index] = vert;
    }
    
    public void setMetric(int value) {
        metric = value;    
    }
    
    public int getMetric() { return metric; }
    public void setName(String value) { name = value; }
    public String getName() { return name; }
    public void setComment(String value) { comment = value; }
    public String getComment() { return comment; }
    
    //
    // public methods
    //
    
    public String costs2String()
    {
        int[][] distanceMatrix = getCosts();
        StringBuffer s = new StringBuffer();
        s.append( "Graph costs: \n" );        
        for ( int i = 0; i < vertices.length; i++)
        {
            s.append ( "Row " );
            s.append ( i );
            s.append ( ": " );
            for ( int j = 0; j < vertices.length; j++ )
            {
                s.append ( " " );
                s.append ( distanceMatrix[i][j] );
            }
            s.append ( "\n" );
        }
        return new String ( s );
    }
    
    /** Return the distance between nodes whose node numbers are the arguments.
     * @param i the 1st node number.
     * @param j the other node number.
     * @return The Euclidean distance between these nodes.
     */ 
    public int getCost( int i, int j ) 
    {
        float deltaX;
        float deltaY;
        switch(metric) {
            case METRIC_EUC_2D:
                // !! Storing magnified coordinates would save 1 multiplication below
                deltaX = vertices[i].getX() - vertices[j].getX();
                deltaY = vertices[i].getY() - vertices[j].getY();
                return (int) ( Math.sqrt( deltaX*deltaX + deltaY*deltaY ) * magnification + 0.5);
            case METRIC_GEO:
                float rrr = 6378.388f;
                float q1 = (float)Math.cos( vertices[i].getX() - vertices[j].getX());
                float q2 = (float)Math.cos( vertices[i].getY() - vertices[j].getY());
                float q3 = (float)Math.cos( vertices[i].getY() + vertices[j].getY());
                return (int) (rrr * Math.acos(0.5 * ((1.0+q1)*q2-(1.0-q1)*q3))+1.0);
            default:
                // !! Storing magnified coordinates would save 1 multiplication below
                deltaX = vertices[i].getX() - vertices[j].getX();
                deltaY = vertices[i].getY() - vertices[j].getY();
                return (int) ( Math.sqrt( deltaX*deltaX + deltaY*deltaY ) * magnification );            
        }
    }
    
    /** Returns a symmetric cost matrix. Entry [i,j] is the distance between node i and
     * node j.
     * @return Returns a symmetric cost matrix. Entry [i,j] is the distance between node i and
     * node j.
     */    
    public int[][] getCosts() 
    {
        if ( distances == null )
        {
            // construct Euclidean distance matrix
            distances = new int[ vertices.length ][ vertices.length ];   
            for ( int i = 0; i < vertices.length; i++ )
            {
                for ( int j = 0; j < i; j++)
                {
                    distances[i][j] = distances[j][i] = getCost( i, j );
                }
            }
        }
        return distances; 
    }
    
    /** Get the minimum cost maximum matching in the graph.
     * @return Returns an array of matched nodes. The ith entry is the node number of the node
     * that is matched to the node numbered i. Node numbers are 0, ..., n-1, where n
     * is the number of nodes in the graph.
     */    
    public int[] getMinCostMaxMatch() 
    {
        WeightedMatch weightedMatch = new WeightedMatch( getCosts() );
        int[] mates = weightedMatch.weightedMatch( WeightedMatch.MINIMIZE);
        
        /* WeightedMatch.weightedMatch returns mates, indexed and valued
         * 1, ..., V. Shift the index to 0, ... , V-1 and put the values in
         * this range too (i.e., decrement them). 
         */
        return weightedMatch.getMatched( mates );
    }
    
    /** Get the maximum cost maximum matching in the graph.
     * @return Returns an array of matched nodes. The ith entry is the node number of the node
     * that is matched to the node numbered i.
     */    
    public int[] getMaxCostMaxMatch() 
    {
        WeightedMatch weightedMatch = new WeightedMatch( getCosts() );
        int[] mates = weightedMatch.weightedMatch( WeightedMatch.MAXIMIZE);
        
        /* WeightedMatch.weightedMatch returns mates, indexed and valued
         * 1, ..., V. Shift the index to 0, ... , V-1 and put the values in
         * this range too (i.e., decrement them). 
         */
        return weightedMatch.getMatched( mates );
    }
    
    /** Get the number of nodes in the graph.
     * @return the number of nodes in the graph.
     */    
    public int size() { return vertices.length; }
    
    /** Draw a minimum cost maximum matching of this graph.
     * @param g The Graphics object on which the drawing takes place.
     * @param x The horizontal offset.
     * @param y The vertical offset.
     * @param extent The edge length, in pixels, of the square containing the drawing.
     */    
    public void drawMatching( Graphics g, int x, int y, int extent )
    {
        g.setColor( Color.red );
        WeightedMatch weightedMatch = new WeightedMatch( getCosts() );
        int[] mates = weightedMatch.weightedMatch( WeightedMatch.MINIMIZE );
        
        for ( int i = 1; i <= vertices.length; i++ )
        {
           if ( i < mates[i] )
           {
               int ix1 = (int) ( vertices[ i-1 ].getX() * extent );
               int iy1 = (int) ( vertices[ i-1 ].getY() * extent );                
               int ix2 = (int) ( vertices[ mates[i] - 1 ].getX() * extent );
               int iy2 = (int) ( vertices[ mates[i] - 1 ].getY() * extent ); 
               g.drawLine(x + ix1, y + iy1, x + ix2, y + iy2 );
           }
        }
    }
    
    public void drawCappello2Approximation( Graphics g, int x, int y, int extent )
    {        
        // construct TSP
        //edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello3.UpperBound graph;
        //graph = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello3.UpperBound( getCosts() );
        edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello2.UpperBound graph;
        graph = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cappello2.UpperBound( getCosts() );
        // compute TSP solution
        int[] tour = graph.getTour();
        
        System.out.println("GraphEuclidean.drawCappello2Approximation: tour.length: " + tour.length);
                
        int cost = 0;
        int j = tour[0];
        System.out.print(" " + j );
        g.setColor( Color.red );
        int i;
        for ( i = 1; i < tour.length; i++ )
        {
            int k = tour[i];
            System.out.print(" " + k );
            cost += getCost( j, k);
            
             int ix1 = (int) ( vertices[ j ].getX() * extent );
             int iy1 = (int) ( vertices[ j ].getY() * extent );                
             int ix2 = (int) ( vertices[ k ].getX() * extent );
             int iy2 = (int) ( vertices[ k ].getY() * extent ); 
             g.drawLine(x + ix1, y + iy1, x + ix2, y + iy2 );
             
            j = k;
        }        
        int ix1 = (int) ( vertices[ j ].getX() * extent );
        int iy1 = (int) ( vertices[ j ].getY() * extent );                
        int ix2 = (int) ( vertices[ tour[0] ].getX() * extent );
        int iy2 = (int) ( vertices[ tour[0] ].getY() * extent ); 
        g.drawLine(x + ix1, y + iy1, x + ix2, y + iy2 );
    }
    
    public void drawTour( Graphics g, int x, int y, int extent )
    {   
        int cost = 0;
        int j;
        if ( tour != null )
        {
            // construct input
//            System.out.println(tsp.distanceString());
            System.out.println(toString());

            // compute upper bound & its cost
            UpperBound upperBound = new UpperBound( this );
            upperBoundTour = upperBound.getTour();
            j = upperBoundTour[0];
            for ( int i = 1; i < upperBoundTour.length; i++ )
            {
                cost += getCost( j , upperBoundTour[i] );
                j = upperBoundTour[i];
            }        
            cost += getCost( j , upperBoundTour[0] );
            System.out.println("Initial Upper Bound: " + cost);

            // construct Shared upperBound
            Shared costUpperBound = new IntUpperBound( cost );

            // construct the computation's Environment
            Environment environment = new Environment( this, costUpperBound );

            // construct root task
            TSPMSTSolution emptySolution = new TSPMSTSolution( 2000 );
            Task task = new BranchAndBound( emptySolution );

            TSPMSTSolution solution = null;
            try
            {
                // get a reference to a Hosting Service Provider
                HspAgent agent = new HspAgent( "test" );
                Client2Hsp hsp = agent.getClient2Hsp();

                // login
                hsp.login( environment );

                // compute
                solution = (TSPMSTSolution) hsp.compute( task );

                // logout
                Invoice invoice = hsp.logout();
                System.out.println( invoice );
            }
            catch ( Exception exception )
            {
                exception.printStackTrace();
                System.out.println( exception.getMessage() );
            }

            if ( solution == null )
            {
                System.out.println("GraphEuclidean.drawTour: solution: null.");
            }           
            tour = (int[]) solution.getTour(); // cache tour
            System.out.println( solution );
        }
        
        // draw minimum cost tour               
        cost = 0;
        j = tour[0];
        System.out.print(" Tour: \n   " + j );
        g.setColor( Color.red );
        int i;
        for ( i = 1; i < tour.length; i++ )
        {
            int k = tour[i];
            System.out.print(" " + k );
            cost += getCost( j, k );
            
             int ix1 = (int) ( vertices[ j ].getX() * extent );
             int iy1 = (int) ( vertices[ j ].getY() * extent );                
             int ix2 = (int) ( vertices[ k ].getX() * extent );
             int iy2 = (int) ( vertices[ k ].getY() * extent ); 
             g.drawLine(x + ix1, y + iy1, x + ix2, y + iy2 );
             
            j = k;
        }
        
        System.out.println(" ");
        
        int ix1 = (int) ( vertices[ j ].getX() * extent );
        int iy1 = (int) ( vertices[ j ].getY() * extent );                
        int ix2 = (int) ( vertices[ tour[0] ].getX() * extent );
        int iy2 = (int) ( vertices[ tour[0] ].getY() * extent ); 
        g.drawLine(x + ix1, y + iy1, x + ix2, y + iy2 );
    }
    
    /** Draw the vertices in a square of extent X extent pixels, starting at
     * pixel offset (x, y) on Graphics g.
     * @param g The graphics object on which the drawing is to take place.
     * @param x The horizontal offset in pixels.
     * @param y The vertical offset in pixels.
     * @param extent The edge length, in pixels, of a square region containing the set of nodes.
     */
    public void drawVertices( Graphics g, int x, int y, int extent )
    {        
        g.setColor( Color.blue );
        for ( int i = 0; i < vertices.length; i++ )
        {
            int ix = (int) ( vertices[i].getX() * extent );
            int iy = (int) ( vertices[i].getY() * extent ); 
            g.fillOval( x + ix - VERTEX_DIAMETER/2, y + iy - VERTEX_DIAMETER/2, 
                        VERTEX_DIAMETER, VERTEX_DIAMETER );  
            
            // draw vertex number
            g.drawString( "" + i, x + ix - 2, y + iy - 2 );
        }
    }
    
    public int[] getTour( int[] tour ) { return tour; }
    
    public void setTour( int[] tour ) { this.tour = tour; }
    
    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append("Name: ");
        s.append(getName());
        s.append("\nComment: ");
        s.append(getComment());
        s.append("\n");
        for ( int i = 0; i < vertices.length; i++ )
        {
            s.append( "\n row " );
            s.append( i );
            s.append( ": " );
            for ( int j = 0; j < vertices.length; j++ )
            {
                s.append( "\t" );
                s.append( getCost( i, j ) );
            }
        }
        s.append( "\n" );
        return new String ( s );
    }
    
    
    public static void main( String[] args )
    {
        int $nodes = Integer.parseInt( args[0] );
        int seed = $nodes;
        int magnification = 1000;
        GraphEuclidean graph = new GraphEuclidean( $nodes, seed, magnification);
    }    
}
