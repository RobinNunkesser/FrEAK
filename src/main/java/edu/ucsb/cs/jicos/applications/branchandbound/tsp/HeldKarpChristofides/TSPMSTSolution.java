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
 * A JICOS branch and bound solution attempt for the TSP.
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides;

import edu.ucsb.cs.jicos.services.shared.IntUpperBound;
import java.util.Vector;
import edu.ucsb.cs.jicos.applications.utilities.graph.Graph;
import edu.ucsb.cs.jicos.applications.branchandbound.tsp.Q;


public class TSPMSTSolution extends edu.ucsb.cs.jicos.applications.branchandbound.tsp.AbstractSolution
{
    private boolean hasRun = false;
    private int bound;
    private boolean tour = false;
    private HeldKarpIterative hki;
    private int included[];
    private int includedT[];
    private int excluded[];
    private int excludedT[];
    private int iterations;
    private double weight[];
    

    /** Creates a new (root) instance of TSPMSTSolution
     * @param iterations Number of Held Karp iterations
     */    
    public TSPMSTSolution(int iterations)
    {
//        System.out.println("NODE CREATED");
        this.included = new int[0];
        this.includedT = new int[0];
        this.excluded = new int[0];
        this.excludedT = new int[0];
        this.iterations = iterations;
    }
    
    /** Creates a new (child) instance of TSPMSTSolution
     * @param included An array of edges to include. The from nodes. Edges represented as
     * (included[i],includedT[i]).
     * @param includedT An array of edges to include. The to nodes. Edges represented as
     * (included[i],includedT[i]).
     * @param excluded An array of edges to exclude. The from nodes. Edges represented as
     * (excluded[i],excludedT[i]).
     * @param excludedT An array of edges to exclude. The to nodes. Edges represented as
     * (excluded[i],excludedT[i]).
     * @param iterations Number of Held Karp iterations.
     * @param weight An array of initial weights for Held Karp.
     */
    public TSPMSTSolution(int[] included, int[] includedT, int[] excluded, int[] excludedT, int iterations, double[] weight)
    {
//        System.out.println("CHILD NODE CREATED");
        this.included = included;
        this.includedT = includedT;
        this.excluded = excluded;
        this.excludedT = excludedT;
        this.iterations = iterations;
        this.weight = weight;
    }
    
    /** Accessor method for the tour, represented as an array of ints.
     * @return An array of ints (node id's) representing the tour. null if the partial
     * solution is not a tour.
     */    
    public int[] getTour() {
        return hki.getIntTour();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
    }
    
    /* returns an LinkedList of the Solution objects that are the children of
     * this Solution.
     * @param problem An object that represents the input of this branch & bound
     * search.
     * For example, in a traveling salesman problem, it could be a distance matrix.
     * @param upperBound The best known cost of a Solution for this problem.
     * @return an Q of [partial] Solution objects that represent the children
     * of the node in the search tree represented by this [partial] Solution.
     */
    
    /** Accessor for the cost of this partial solution.
     * @return The cost of this partial solution.
     */    
    public int getCost() { return hki.getBound(); }
    
    /** returns the lower bound on the cost of any complete Solution that is an
     * extension of this partial Solution.
     * @return the lower bound on the cost of any complete Solution that is an
     * extension of this partial Solution.
     */
    private int getLowerBound(Object problem, int upperbound)
    {
        Graph tsp = (Graph)problem;
        if (weight == null)
            weight = new double[tsp.size()];
        hki = new HeldKarpIterative( tsp, included, includedT, excluded, excludedT, iterations, weight, upperbound);
        hki.compute();
        tour = hki.isTour();
        bound = hki.getBound();
        hasRun = true;
//        System.out.println(hki.debugDump());
        return bound;
    }
    
    public int getUpperBound( edu.ucsb.cs.jicos.services.Environment environment)
    {
        Graph tsp = (Graph)(environment.getInput());
        IntUpperBound iub = (IntUpperBound)environment.getShared();
        int upper = ((Integer)iub.get()).intValue();
        int cost = Integer.MAX_VALUE;
        int[] tour = null;
//    Begin commenting here to skip Christofides
        edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound ub = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.christofides.UpperBound(tsp, hki.getArrayListRepresentation());
        try{
            cost = 0;
            tour = ub.getTour();
            int j = tour[0];
    //        System.out.print(" Tour: \n   " + j );
            for ( int i = 1; i < tour.length; i++ )
            {
//                output.append(" " + tour[i] );
    //            System.out.print(" " + tour[i] );
                cost += tsp.getCost( j, tour[i] );
                j = tour[i];
            }        
            cost += tsp.getCost( j, tour[0] );
        }
        catch (java.lang.ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Christofides went out of bounds");
            System.out.println(hki.debugDump());
            cost = Integer.MAX_VALUE;
            System.exit(0);
        }
//        return cost;
//    End commenting out to skip Christofides
        int costcycle = 0;
        int[] startcycle = hki.getCycle();
        int[] tourcycle = null;
        Vector cycle = new Vector();
        for (int i=0;i<startcycle.length;i++) {
            if (i > 2 && startcycle[i] == 0) break;
            cycle.add(new Integer(startcycle[i]));
        }
        cycle.add(new Integer(startcycle[0]));
        edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cycle.UpperBound up = new edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cycle.UpperBound(tsp, cycle);
        tourcycle = up.getTour();
        int k = tourcycle[0];
        for ( int i = 1; i < tourcycle.length; i++ )
        {
            costcycle += tsp.getCost( k, tourcycle[i] );
            k = tourcycle[i];
        }        
        costcycle += tsp.getCost( k , tourcycle[0] );
        if (Math.min(cost,costcycle) < upper) {
            if (costcycle < cost)
            {
                System.out.println("Cycle cost LESS than Christofides. " + costcycle + ", " + cost);
            }
            else
            {
                System.out.println("Cycle cost MORE than Christofides. " + costcycle + ", " + cost);            
            }
        }
        return Math.min(cost, costcycle);
    }
    
    public String toString()
    {
        StringBuffer output = new StringBuffer();
        output.append("TSP SOLUTION OUTPUT\n" );
        output.append("Size: " + hki.degree.length );
        output.append(", cost: " + hki.getBound());
        output.append("\nTOUR: " + hki.getTour());
        return new String( output );
    }
    
    public Q getChildren(edu.ucsb.cs.jicos.services.Environment environment)
    {
        Q q = new Q();
        int i;
        int j=-1;
        int cacheI = -1;
        int cacheJ = -1;
        assert (hki != null);
        assert (hki.degree != null);        
        for (i = 1; i < hki.degree.length; i++)
        {
            if (hki.degree[i] > 2)
            {
                j = hki.getMaxAttachedFree(i);
                if (j != -1)
                {
                    if (timesIncluded(i) != 2 && timesIncluded(j) != 2)
                    {
                        // This can actually happen!
                        if (timesExcluded(i) != hki.degree.length - 2 && timesExcluded(j) != hki.degree.length - 2)
                            break;
                    }
                    else
                    {
                        cacheI = i;
                        cacheJ = j;
                    }
                }
            }
            else if (hki.degree[i] < 2 && timesIncluded(i) == 0)
            {
                j = hki.getMaxAttachedFree(i);
                if (timesIncluded(j) != 2)
                    break;
            }
        }
        if (i == hki.degree.length)
        {
            i = cacheI;
            j = cacheJ;
            if ( i==-1 )
            {
                // signal an error
                System.out.println("No Edge Found with degree != 2");
                System.out.println("Tour: " + tour);
                if (!tour)
                    hki.whyNotTour();
                return q;
            }
        }
        
        // new included edge set
        int[] newIncluded = new int[included.length + 2];
        System.arraycopy(included,0,newIncluded,0,included.length);
        newIncluded[included.length] = i;
        newIncluded[included.length+1] = j;
        int[] newIncludedT = new int[includedT.length + 2];
        System.arraycopy(includedT,0,newIncludedT,0,includedT.length);
        newIncludedT[includedT.length] = j;
        newIncludedT[includedT.length+1] = i;
        // nwe excluded edge set
        int[] newExcluded = new int[excluded.length + 2];
        System.arraycopy(excluded,0,newExcluded,0,excluded.length);
        newExcluded[excluded.length] = i;
        newExcluded[excluded.length+1] = j;
        int[] newExcludedT = new int[excludedT.length + 2];
        System.arraycopy(excludedT,0,newExcludedT,0,excludedT.length);
        newExcludedT[excludedT.length] = j;
        newExcludedT[excludedT.length+1] = i;
        
        // two new children
        q.add( new TSPMSTSolution(included, includedT, newExcluded, newExcludedT, iterations, weight) );
        if (timesIncluded(i) != 2 && timesIncluded(j) != 2)
        {
            q.add( new TSPMSTSolution(newIncluded, newIncludedT, excluded, excludedT, iterations, weight) );
        }
        
        // return a set of children with different augmentations to the distance matrix
        return q;
    }
    
    public int getLowerBound(edu.ucsb.cs.jicos.services.Environment environment)
    {
        if (!hasRun)
            return getLowerBound(environment.getInput(), ( (Integer) environment.getShared().get() ).intValue());

        return bound;
    }
    
    public boolean isComplete(edu.ucsb.cs.jicos.services.Environment environment)
    {
        return tour;
    }
    
    private int timesIncluded(int node)
    {
        int count = 0;
        for (int i = 0; i <  included.length; i++)
        {
            if (included[i] == node)
                count++;
        }
        return count;
    }
    private int timesExcluded(int node)
    {
        int count = 0;
        for (int i = 0; i <  excluded.length; i++)
        {
            if (excluded[i] == node)
                count++;
        }
        return count;
    }
}
