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
 * HeldKarpIterative.java
 *
 * Created on June 8, 2003, 6:36 PM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.HeldKarpChristofides;

import edu.ucsb.cs.jicos.applications.utilities.graph.Graph;


public class HeldKarpIterative implements java.io.Serializable
{
    private boolean tour = false;
    private int bound;
    private int upperbound;
    private MSOneTPrim onetree;
    private int iterations;
    private int iter;
    private double t;
    private double t0;

//    private int distance[][];
    private Graph tsp;

    private int included[];
    private int includedT[];
    private int excluded[];
    private int excludedT[];
    
    private double weight[];
    public int degree[];
    private int lastdegree[];
    
    private void setOneTree(MSOneTPrim onetree)
    {
        this.onetree = onetree;
    }

    private MSOneTPrim getOneTree()
    {
        return onetree;
    }

    /** Creates a new instance of HeldKarpIterative */
    public HeldKarpIterative(Graph tsp, int[] included, int[] includedT, int[] excluded, int[] excludedT, int iterations, double[] weight, int upperbound)
    {
        this.iterations = iterations;
        this.included = included;
        this.includedT = includedT;
        this.excluded = excluded;
        this.excludedT = excludedT;
        this.weight = weight;
        this.upperbound = upperbound;

        this.tsp = tsp;

        //this.weight = new double[distance.length];
        degree = new int[tsp.size()];
        lastdegree = new int[tsp.size()];
        
        
        //// TODO: Remember java well enough to remember if this step is redundant
        for (int i = 0; i < tsp.size(); i++)
        {
        //    weight[i] = 0;
        //    degree[i] = 0;
            lastdegree[i] = 2;
        }
    }
    
    public int getMaxAttachedFree(int i)
    {
        return getOneTree().getMaxAttachedFree(i);
    }
    
    public java.util.ArrayList[] getArrayListRepresentation()
    {
        return getOneTree().getArrayListRepresentation();
    }
    
    public int[] getCycle()
    {
        return getOneTree().getCycle();
    }
    
    private void initT0()
    {
        t0 = getOneTree().getUnweightedCost() / (2.0 * tsp.size());
    }
    
    public boolean isTour()
    {
        return tour;
    }
    
    public void whyNotTour()
    {
        getOneTree().whyNotTour();
    }
    
    public String getTour()
    {
        StringBuffer output = new StringBuffer();
        output.append(getOneTree().debugDump());
        return new String( output );    
    }
    
    /** Accessor method for the tour, represented as an array of ints.
     * @return An array of ints (node id's) representing the tour. null if the partial
     * solution is not a tour.
     */    
    public int[] getIntTour() {
        return getOneTree().getTour();
    }
    
    public void compute()
    {        
        doMST();
        updateBound();
        for (iter = 1; iter < iterations && !isTour() && getBound() <= upperbound ; iter++)
        {
            updateWeights();
            doMST();
            updateBound();
//            if (getBound() > upperbound)
//            {
//                System.out.println("Bound: " + getBound() + " is greater than " + upperbound);
//            }
        }
        if (getBound() <= upperbound)
        {
//            System.out.println("Bound: " + getBound());
        }
//        System.out.println("\nDistance: \n" + getOneTree().distanceString());
    }
    
    private void updateBound()
    {
        int temp = getOneTree().getUnweightedCost();
//        System.out.println("\nBound: *" + bound + "*");
//        System.out.println("Attempt: *" + temp + "*");
//        System.out.println(getOneTree().debugDump());
        if (temp >= bound)
        {
//            if ( temp == 5567 )
//                System.out.println(debugDump());
//            System.out.println("Bound: *" + temp + "*");
//            System.out.println(getOneTree().debugDump());
            if (getOneTree().isTour())
            {
                if (getOneTree().getReevaluatedTourCost() != temp)
                {
                    System.out.println("\n*** Aggregious Error ***\n");
                    System.out.println(temp + " should have been evaluated as " + getOneTree().getReevaluatedTourCost());
                }
//                System.out.println("Tour found");
                tour = true;
            }
        }
        else
        {
            if (getOneTree().isTour())
            {
                System.out.println("Tour found with bound: " + temp + ". Best is " + bound + ". WTF?");
                System.out.println(getOneTree().debugDump());
                System.exit(0);
                tour = true;
            }
        }
        bound = (temp > bound ? temp : bound);
    }
    
    public int getBound()
    {
        return bound;
    }
    
    private void doMST()
    {
        System.arraycopy(degree,0,lastdegree,0,degree.length);
        setOneTree(new MSOneTPrim(tsp, included, includedT, excluded, excludedT, weight, degree));
        getOneTree().compute();
    }
    
    private void updateWeights()
    {
        computeT();
        for (int i = 1; i < weight.length; i++)
        {
            if (degree[i] == 2)
                continue;
            //System.out.println("    Updating Weight " + i);
            weight[i] = weight[i] + (0.6*t*(degree[i]-2) + 0.4*t*(lastdegree[i]-2));
        }
    }
    
    private void computeT()
    {
        if (iter == 1)
        {
            initT0();
            t = t0;
        }
        else
        {
            t = (iter-1)*((2*iterations -5)/(2.0 * (iterations - 1)))*t0 - (iter-2)*t0 + (iter-1)*(iter-2)/(2.0*(iterations-1)*(iterations-2))*t0;
        }
    }
    
    public String debugDump()
    {
        return getOneTree().debugDump();
    }
    
    public static void main(String[] args)
    {
        int temp[] = new int[0];
        for (int seed = 0; seed < 10; seed++)
        {
            System.out.print("Seed: ");
            System.out.println(seed);
            java.util.Random rand = new java.util.Random(seed);
            int n = 100;
            int cost[][] = new int[n][n];
            double weight[] = new double[n];

            for (int i = 0; i < n; i++)
            {
                cost[i][i] = Integer.MAX_VALUE;
                for (int j = i+1; j < n; j++)
                {
                    cost[i][j] = rand.nextInt(100000);
//                    cost[i][j] = java.lang.Math.abs(i-j);
                    cost[j][i] = cost[i][j];
                }
            }
            System.out.println("REDO");
//            HeldKarpIterative hki = new HeldKarpIterative(cost, temp, temp, temp, temp, 500, weight, Integer.MAX_VALUE);
//            hki.compute();
        }
    }
}
