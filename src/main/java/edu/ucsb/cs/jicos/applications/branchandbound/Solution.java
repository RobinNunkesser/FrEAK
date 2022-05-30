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
 * The interface of an object that represents a solution to a branch and
 * bound search.
 *
 * @author Peter Cappello
 * @version 1.0
 */

package edu.ucsb.cs.jicos.applications.branchandbound;

import edu.ucsb.cs.jicos.applications.branchandbound.Q;
import edu.ucsb.cs.jicos.services.*;


public interface Solution extends java.io.Serializable
{            
    /** returns an LinkedList of the Solution objects that are the children of
     * this Solution.
     * @param environment The environment of the current Task, which contains 
     * the computation input and shared object. 
     * For example, in a traveling salesman problem, the input could be a 
     * distance matrix and the shared is or has the current upperBound for this
     * problem.
     * @return an Q of [partial] Solution objects that represent the children
     * of the node in the search tree represented by this [partial] Solution.
     */    
    public Q getChildren( Environment environment );
    
    /** returns the lower bound on the cost of any complete Solution that is an
     * extension of this partial Solution.
     * @param input An Object, typically environment.getInput(), which may be 
     * used in the computation of the lower bound.
     * @return the lower bound on the cost of any complete Solution that is an
     * extension of this partial Solution.
     */    
    public int getLowerBound( Object input );
        
    /** returns true if and only if this solution should be explored directly by
     * a Host (as opposed to being further decomposed).
     * @return true if and only if this solution should be explored directly by
     * a Host (as opposed to being further decomposed).
     * @param myTask a reference to the task encapsulating this partial solution.
     * This may be used when the question of whether or not the solution
     * is atomic depends on task properties, such as whether this task
     * has been assigned more than once (suggesting that it may be big).
     */    
    public boolean isAtomic( Task myTask );
    
    /** returns true if and only if the partial Solution is, in fact, complete.
     * @return true if and only if the partial Solution is, in fact, complete.
     */    
    public boolean isComplete();

    /** returns a String representation of this partial solution.
     * @return a String representation of this partial solution.
     */    
    public String toString();
}
