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
 *  A Jicos task for a generic TSP branch and bound.  This class is the main
 *  part of the Jicos TSP Branch & Bound Framework.  It is a variation on
 *  version 1 of the generic branch and bound algorithm given in Chapter
 *  10 Branch and bound methods by E. Balas and P. Toth from The Traveling
 *  Salesman Problem, edited by E. L. Lawler, J. K. Lenstra, A. H. G. Rinnooy
 *  Kan, D. B. Shmoys, John Wiley & Sons Ltd., 1985.
 *
 * Please see the Jicos programming tutorial for an explanation of its design.
 *
 * @version 1.0
 * @author Peter Cappello
 * @author Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp;

import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.shared.*;


public final class BranchAndBound extends Task 
{
    private Solution solution;

    /** Used to construct a BranchAndBound task that corresponds to the search
     * subtree represented by the Solution passed to it as its argument.
     * @param solution A partial solution to the problem. It represents a search
     * subtree. Initially, this would be the "starting" solution.
     */    
    public BranchAndBound( Solution solution ) { this.solution = solution; }
    
    /** The method that explores the search tree.
     * @param environment Contains the the problem input & the shared object. 
     * The latter holds the <I>cost</I> of the best solution found so far.
     * @return If no subtasks are constructed, it returns either null, if the 
     * possibly partial solution is no better than its current best, or a 
     * Solution, if it does find a new minimum cost Solution. 
     * <p>When the task does construct subtasks, it returns a MinSolution task 
     * that will receive the results of the spawned subtasks.
     */    
    public Object execute ( Environment environment )
    {
        // boundFromBelow
        int lowerBound = solution.getLowerBound( environment );
        
        // prune ?
        int upperBound =  sharedUpperBound( environment );
        if ( lowerBound > upperBound ) 
        {
            return null; // To successor: "This subtree was pruned."
        }
        if ( lowerBound == upperBound )
        {
            if ( solution.isComplete( environment ) )
            {
                return solution; // currently best (not necessarily unique)
            }
        }
        
        // new upper bound ?
        if ( solution.isComplete( environment ) )
        {
            environment.setShared( new IntUpperBound( upperBound ) );
            return solution; // currently best
        }
        
        // boundFromAbove
        upperBound = solution.getUpperBound( environment );
        if ( upperBound < sharedUpperBound( environment ) )
        {
            environment.setShared( new IntUpperBound(upperBound) ); // propogate
        }
            
        // reduce
        solution.reduce( environment );
        
        // branch
        Q q = solution.getChildren( environment );
        if ( q.isEmpty() )
        {
            return null;
        }
        do
        {
            Solution child = (Solution)q.remove ();
            Task task = new BranchAndBound( child );
            compute( task );
        } 
        while ( ! q.isEmpty() );
        return new MinSolution();
    }
    
    // return value of shared UpperBound
    private int sharedUpperBound( Environment environment )
    {
        return ( (Integer) environment.getShared().get() ).intValue();
    }
}
