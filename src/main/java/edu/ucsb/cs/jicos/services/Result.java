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
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

/** A data container for a computation's result.
 * It essentially has 1) an identifier, so the application can know to which
 * computation this result corresponds, and 2) the value, which is an Object.
 */
public final class Result implements java.io.Serializable		
{
    private ResultId resultId;
    private Object value;
    private long criticalPathTime;
    private long totalTaskTime;

    /** Sets the Result's data members.
     * @param computationId the int computation identifier the application supplied 
     * when it constructed the computation's root <CODE>Task</CODE>.
     * It is returned with the result value, enabling the application to detect which
     * computation this Result is for.
     * @param value the actual computed value for the computation.
     */        
    Result( TaskId taskId, Object value, long criticalPathTime ) 
    { 
        resultId = new ResultId( taskId );
        this.value = value;
        this.criticalPathTime = criticalPathTime;
    }
    
    /** Get the critical path time: A critical directed path is a path of tasks whose
     * execution times sum to a number that is at least as long as any other directed
     * path of tasks. This method returns that time, in milliseconds.
     * @return critical path time, in milliseconds.
     */    
    public long getCriticalPathTime() { return criticalPathTime; }

    /** get the computation identifier: The identifier that distinguished this 
     * computation from all computations associated with the client's current
     * registration session. 
     * @return the computation identifier.
     */    
    public ResultId getId() { return resultId; }
    
    /** Not currently implemented
     * @return Not currently implemented
     */    
    public long getTotalTaskTime() { return totalTaskTime; }

    /** get the value.
     * @return an Object, the computation's value.
     */    
    public Object getValue() { return value; }
}