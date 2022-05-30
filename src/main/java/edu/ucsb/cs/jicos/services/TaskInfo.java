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
 *  Data container for Task completion information.
 *
 * @author  Peter Cappello
 */
package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;

import java.util.*;

public final class TaskInfo implements java.io.Serializable
{
    private final static int INVALID_INDEX = -2;
        
    private Service host;
    private TaskId taskId;
    private int executeTime;
    private long criticalPathTime;
    private Class className;
    private Exception exception; // execute method caused this exception
    
    // either the task produces children xor a value for its successor
    private List children;
    private TaskId    successorTaskId;
    private int       successorIndex;
    private Object    successorValue;
    
    TaskInfo ( Service host, Task task, int executeTime )
    {
        assert host != null;
        assert task != null;
        
        this.host = host;
        this.taskId = task.getTaskId();        
        className = task.getClass();
        successorIndex = INVALID_INDEX; // invalid value
        this.executeTime = executeTime;
        criticalPathTime = task.getPredecessorCriticalPathTime() + executeTime;
    }
    
    Class className() { return className; }
    
    int executeTime() {  return executeTime; }
    void executeTime( int executeTime ) 
    {
        assert executeTime >= 0;        
        this.executeTime = executeTime; 
    }    
    
    List      getChildren()         { return children; }
    long      getCriticalPathTime() { return criticalPathTime; }
    Service   host()                { return host; }
    int       getSuccessorIndex()   { return successorIndex; }
    TaskId    getSuccessorTaskId()  { return successorTaskId; }
    Object    getValue()            { return successorValue; }

    void set( List children )  
    {
        assert children != null;       
        this.children = children; 
    }
    void set( Exception exception ) 
    {
        assert exception != null;        
        this.exception = exception; 
    }
    
    void set( TaskId successorTaskId, int successorIndex, Object successorValue)
    {
        assert successorTaskId != null;       
        this.successorTaskId = successorTaskId;
        this.successorIndex = successorIndex;
        this.successorValue =  successorValue;
    }
    
    TaskId taskId() { return taskId; }
    
    public String toString()
    {
        return "TaskInfo: " + taskId + " Host: " + host + " executeTime: " + 
            executeTime + " Class: " + className + " successor task Id: " + successorTaskId;
    }
}
