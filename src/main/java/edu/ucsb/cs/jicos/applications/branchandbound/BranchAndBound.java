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

/** BranchAndBound.java - a Jicos task for a generic branch and bound
 * framework. This class is the main part of the Jicos Branch and Bound
 * Framework.  Please see the programming tutorial for an explanation of
 * its design.
 *
 * @version 1.0
 * @author Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound;

import edu.ucsb.cs.jicos.applications.branchandbound.Q;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.shared.*;
import edu.ucsb.cs.jicos.services.tasks.*;
import java.util.*;

//import edu.ucsb.cs.jicos.applications.branchandbound.tsp.pete.*; // !! DEBUG ONLY



public class BranchAndBound extends Task 
{
    private Solution solution;

    /** Used to construct a BranchAndBound task that corresponds to the search
     * subtree represented by the Solution passed to it as its argument.
     * @param solution A partial solution to the problem. It represents a search
     * subtree. Initially, this would be the "starting" solution.
     */    
    public BranchAndBound( Solution solution ) { this.solution = solution; }
    
    /** The method that searches the search tree. If the search subtree
     * represented by this task is sufficiently small, it explores it, returning
     * the best solution found, if it is better than the best known solution;
     * otherwise, it "branches": constructs subtasks that correspond to subtrees
     * of this search tree.
     * @param environment Contains the the problem input & the shared object. The latter
     * holds the <I>cost</I> of the best solution found so far.
     * @return If the task is atomic (i.e., does not construct subtasks), it
     * returns either null, if it finds no solution better than what
     * it already knows about, or a Solution, if it does find a new
     * minimum cost Solution. When the task does construct subtasks,
     * it returns a MinSolution task that will receive the results of
     * the spawned subtasks.
     */    
    public Object execute ( Environment environment )
    {
        /* Prune? The upper bound may have gone down after this task was 
         * constructed, but before its execute method is invoked.
         */
        int lowerBound = solution.getLowerBound( environment.getInput() );
        if ( lowerBound > sharedUpperBound( environment ) ) 
        {
            return null; // To successor: "This subtree was pruned."
        }
        if ( solution.isAtomic( this ) )
        {
            return exploreSubTree( environment );
        }
        else
        {
            return branch( environment );
        }
    }
    
    // return value of shared UpperBound
    private int sharedUpperBound( Environment environment )
    {
        return ( (Integer) environment.getShared().get() ).intValue();
    }

    /* getChildren returns only children that should be explored:
     *     child's computed lower bound < upperBound.
     */
    private MinSolution branch( Environment environment )
    {        
        Object problem = environment.getInput();
        int upperBound = sharedUpperBound( environment );
        Q q = solution.getChildren( environment );
        if ( q.isEmpty() ) // Have all of the children been pruned?
        {
            return null; // To successor: "This search subtree was pruned."
        }           

        // construct, spawn children tasks
        while ( ! q.isEmpty() ) 
        {
            Solution child = (Solution)q.remove ();
            Task task = new BranchAndBound( child );
            compute( task );
        }
        return new MinSolution();
    }

    private Solution exploreSubTree( Environment environment )
    {
        // Since this task does not construct subTasks, prefetch a Task.
        environment.fetchTask();

        Solution currentBest = null;
        Object input = environment.getInput();

        assert ! solution.isComplete(); 
        Stack stack = new Stack();
        for ( stack.push( solution ); ! stack.isEmpty(); )
        {
            solution = stack.pop();
            int lowerBound = solution.getLowerBound( input );
            
            // Prune?
            if ( lowerBound > sharedUpperBound( environment ) ) 
            {        
                continue; // prune
            }
            Q q = solution.getChildren( environment );
            while ( ! q.isEmpty() ) 
            {
                Solution child = (Solution)q.remove ();                                
                if ( child.isComplete() ) 
                {   
                    int cost = child.getLowerBound( input );
                    if ( cost <= sharedUpperBound( environment ) )
                    { 
                        // new upper bound
                        environment.setShared( new IntUpperBound( cost ) );
                        currentBest = child;
                    }
                }
                else 
                {
                    stack.push( child );
                }
            }
        }                          
        return currentBest; // report result to successor
    }

    /**
	 * @return Returns the solution.
	 */
	public Solution getSolution() {
		return solution;
	}
	/**
	 * @param solution The solution to set.
	 */
	public void setSolution(Solution solution) {
		this.solution = solution;
	}
    
    /* I am not using java.util.Stack because it extends Vector, 
     * whose operations are synchronized, an unnecessary overhead in this case.
     * LinkedList operations are not synchronized. However, every push requires
     * memory allocation, unfortunately.
     *
     * It might be better to define a data structure, based on a LinkedList,
     * where pop returns the top value and "empties" the top node and moves the
     * top pointer to the previous node, but does not free the unused node. Push
     * behaves analogously, actually getting a new node only when there are no
     * "empty" nodes left. Would this perform better overall?
     */
    private class Stack
    {
        LinkedList stack = new LinkedList();
        
        boolean isEmpty() { return ( stack.size() == 0 ) ? true : false; }
        
        Solution pop()
        {
            assert ! isEmpty();
            return (Solution) stack.removeFirst();
        }
        
        void push ( Solution solution ) { stack.addFirst( solution ); }
    }
}
