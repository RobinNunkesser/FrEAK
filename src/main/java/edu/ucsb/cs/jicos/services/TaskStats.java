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
 *  Contains task execution time information for a particular Task Class.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;


public final class TaskStats implements java.io.Serializable 
{
    private int $tasksExecuted;
    private int totalExecutionTime;
    
    void add ( int executionTime )
    {
        $tasksExecuted++;
        totalExecutionTime += executionTime;
    }
    
    void add ( TaskStats taskStats )
    {
        $tasksExecuted     += taskStats.$tasksExecuted();
        totalExecutionTime += taskStats.totalExecutionTime();
    }
    
    /** Get the number of Task objects executed.
     * @return the number of Task objects executed.
     */    
    public int $tasksExecuted() { return $tasksExecuted; }
    
    /** Get the total execution time for these Task objects.
     * @return the total execution time for these Task objects.
     */    
    public int totalExecutionTime() { return totalExecutionTime; }
}
