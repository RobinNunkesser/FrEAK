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

/** A object that functions as an identifier for a particular computation:
 * Every computation initiated by either a Hsp compute method or Hsp setComputation
 * method has a unique ResultId associated with it.
 * @author Peter Cappello
 */
public class ResultId implements java.io.Serializable
{
   private TaskId taskId;
   
    ResultId( TaskId taskId ) { this.taskId = taskId; }    
    
    /** Used so that ResultId objects can be the key in utility data structures, such as
     * HashMap.
     * @return true if and only if the argument is deemed equal.
     * @param resultId The ResultId object to be compared for equality. */    
    public boolean equals( Object resultId )
    {
        return getTaskId().equals( ((ResultId) resultId).getTaskId() );
    }
    
    private TaskId getTaskId() { return taskId; }
    
    /** Used so that ResultId objects can be the key in utility data structures, such as
     * HashMap.
     * @return An int that is used by Hash utility data structures. */    
    public int hashCode() { if( taskId==null) return 0; else return taskId.hashCode(); }
}