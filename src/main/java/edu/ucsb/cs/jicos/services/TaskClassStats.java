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
 *  Extends java.util.HashMap.  Key is Task Class; value is a TaskStats object.
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import java.util.*;


public final class TaskClassStats extends HashMap 
{
    void add ( Class className, int executeTime )
    {                
        TaskStats taskStats = get ( className );
        if ( taskStats == null )
        {
            taskStats = new TaskStats ();
        }
        taskStats.add ( executeTime );
        put ( className, taskStats );
    }
    
    void add ( Class className, TaskStats taskStats )
    {
        TaskStats thisTaskStats = get ( className );
        if ( thisTaskStats == null )
        {
            thisTaskStats = new TaskStats ();
        }
        thisTaskStats.add ( taskStats );
        put ( className, thisTaskStats );
    }
    
    void add ( TaskClassStats taskClassStats )
    {
        Set keySet = taskClassStats.keySet();                
        for ( Iterator i = keySet.iterator(); i.hasNext(); )
        {
            Class className = (Class) i.next();
            TaskStats taskStats = taskClassStats.get ( className );
            add ( className, taskStats );            
        }
    }
    
    TaskClassStats copy() { return (TaskClassStats) new HashMap ( this ); }
    
    /** Get the TaskStats object associated with a particular Task Class.
     * @param className The Class of the Task whose TaskStat object is sought.
     * @return A TaskStat object.
     */    
    public TaskStats get ( Class className )
    {
        return (TaskStats) super.get ( className );
    }
    
    /** Returns a String representation of a TaskStat object.
     * @param pad A String that is prepended to the returned String.
     * @return a String representation of a TaskStat object, including the number of Tasks of
     * this Class, the total execution time for these Task objects, the average
     * execution time for these Task objects, and the name of the Task Class.
     */    
    public String toString( String pad )
    {        
        StringBuffer s = new StringBuffer();         
        Set keySet = keySet(); 
        for ( Iterator i = keySet.iterator(); i.hasNext(); )
        {
            Class className = (Class) i.next();
            s.append( pad );                        
            TaskStats taskStats = get ( className );
            int $tasksExecuted = taskStats.$tasksExecuted();
            int totalExecutionTime = taskStats.totalExecutionTime();
            int averageExecutionTime = totalExecutionTime / $tasksExecuted;
            s.append ( " Tasks: " );
            s.append ( $tasksExecuted );
            s.append ( " Time (ms): " );
            s.append ( totalExecutionTime );
            s.append ( " Average time: " );
            s.append ( averageExecutionTime );
            s.append ( "ms " );
            s.append ( className );
            s.append ( "\n" );
        }
        return new String ( s );
    }
}
