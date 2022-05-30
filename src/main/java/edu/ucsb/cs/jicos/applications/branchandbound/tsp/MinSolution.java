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
 * This is a Jicos Compose task for a generic branch &amp; bound algorithm.
 * There are 2 accompanying task classes: Branch and Atomic.
 *
 * A compositional Task, whose inputs and output are Solution objects,
 * used in branch &amp; bound applications.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp;
//import edu.ucsb.cs.jicos.applications.branchandbound.*;
import edu.ucsb.cs.jicos.services.*;
import edu.ucsb.cs.jicos.services.shared.*;
// import edu.ucsb.cs.jicos.applications.branchandbound.tsp.pete.*; // !! DEBUG ONLY


public final class MinSolution extends Task
{	
    /** This method returns the Solution Object among its inputs, whose IntUpperBound
     * has minimal intValue, provided that it is at least as low as the Shared
     * objects intValue. Otherwise, it returns null, indicating that none of its
     * input Solution objects is the current best.
     * @param environment The environment's Shared Object defines the currently best known
     * minimal intValue associated with a Solution. It is possible,
     * though unlikely, that the Task's input Solutions contain a better
     * one than the TaskServer knows about, as reflected in the
     * Environment's Shared object's intValue. If this occurs, a side-effect
     * of the execute method is to update the Environment's Shared object accordingly.
     * @return the Solution Object among its inputs, whose IntUpperBound
     * has minimal intValue, provided that it is at least as low as the Shared
     * objects intValue. Otherwise, it returns null, indicating that none of its
     * input Solution objects is the current best.
     */    
    public Object execute ( Environment environment )
    {
        Solution currentBest = null;
        for ( int i = 0; i < numInputs(); i++ ) 
        {
            Solution reportedSolution = (Solution) getInput( i );            
            if ( reportedSolution == null ) 
            {
                continue; // subTask: "No better solution found."
            }        
            int lowerBound = reportedSolution.getLowerBound( environment );
            
            /** The == of <= is needed to return the solution associated with
             * the current shared upper bound when that solution is one of this 
             * task's inputs.
             */
            if ( lowerBound <= sharedUpperBoundValue( environment ) ) 
            {
                environment.setShared( new IntUpperBound( lowerBound ) );
                currentBest = reportedSolution;
            }
        }        
        return currentBest;
    }
    
    public boolean executeOnServer( Environment environment ) { return true; }
    
    // return int value of shared IntUpperBound
    private int sharedUpperBoundValue( Environment environment )
    {
        return ( (Integer) environment.getShared().get() ).intValue();     
    }
}
