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
 *  Task encapsulates computational tasks. User-defined tasks
 *  <CODE>extend</CODE> it.
 *
 * @author  Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import  edu.ucsb.cs.jicos.foundation.*;

import java.util.*;

public abstract class Task implements java.io.Serializable
{    
    // DEBUG
//    public int numAssignments;
    
    // constants
    static final int RESULT = -1;
    static public final int ALL_INPUTS = -1;
    
    //** to become transient attributes
    private short $unsetArgs; // # of arguments to be set before task is ready
    private Service host; // host assigned to execute this Task
    
    //** Serialized attributes
    private TaskId taskId;
    private boolean cached = false; // set true in Host; read by Session spawn             
    private short level;    
    private ArrayList children = new ArrayList();
    private Object[] inputs;   // execute method inputs, from predecessor tasks.
    private Service spawningHost;   // Host that spawned this Task
    
    // successor information
    private TaskId successorTaskId; // The taskId of the successor task
    private int successorIndex;  // successorTask.input[successorIndex] = output
    private ServiceName taskServerServiceName;
    private Service taskServer;     // This task's output is sent here
    private long predecessorCriticalPathTime;
    
    /** 
     * if I am my parent's children[1]
             set to my parent's totalTaskTime + my executeTime
     * else if I am the compose task (i.e., my parent's children[0], 
     *      then set to the sum of my input tasks' totalTaskTime + my executionTime
     *  else set to my execution time.
     */
    private long totalTaskTime; 
        
    /** A no argument constructor is needed by RMI when unmarshalling arguments
     * and/or return values.
     */        
    public Task() {}
               
    /** This is used to "dispatch" a <I>subtask</I> (its argument).
     * The subtask is ready for immediate execution.
     *
     * This method is <I>not</I> used to dispatch compose tasks. 
     * They are conveyed as the <CODE>execute</CODE> method's returned value.
     * @param task A subtask of <CODE>this</CODE> task. 
     *   It has all its inputs, thus is ready for execution. Its successor is a Compose task.
     */    
    public final void compute( Task task )
    {
        // send output to compose task whose originating taskServer == this.outputTaskServer.
        task.successorTaskId = new TaskId( taskId, (short) 0 );
        task.successorIndex = children.size();
        task.taskId = new TaskId ( taskId, (short) ( children.size() + 1 ));
        task.level = (short) ( level + 1 );        
        children.add( task );
    }
    
    /** This method implements the computation encapsulated by this task. The method is
     * the Task class's reason for being.
     * @return The returned <CODE>Object</CODE> is either the execute method's computed value
     * <B>xor</B>
     * a (compose) Task. In the latter case, the compose task takes the output from this
     * task's subtasks, and composes it to create this task's output.
     * This, at least, is the intent of this method's returned value.
     * @param environment The task's environment: The computation's immutable input &
     * its Shared Object, if any.
     */
    abstract public Object execute( Environment environment );
    
    /** This method is used to process inputs as they arrive, instead of waiting
     * until they all have arrived.
     * @return The returned <CODE>Object</CODE> is either the execute method's 
     * computed value <B>xor</B>
     * a (compose) Task - which takes the output from this task's subtasks, and 
     * composes it to create this task's output. This, at least, is the intent 
     * of this method's returned value
     * <B>xor</B> a <i>ContinueSignal</> "Task" - which indicates that the task
     * is not complete, and is waiting for more inputs.
     * @param environment The task's environment: The computation's immutable input &
     * its Shared Object, if any.
     */
    public Object execute( Environment environment, int inputIndex)
    {
//        System.err.println("Task: " + this + ": incremental execute method invoked but not implemented.");
        
        return null;
    }
    
    /** Invoke this task's execute method on the originating TaskServer if and only if
     * this method evaluates to true.
     * @param environment The Environment for this session.
     * @return true if and only if the task's execute method is to be invoked by the
     * originating TaskServer.
     */    
    public boolean executeOnServer( Environment environment ) { return false; }
    
    /** Returns input[i].
     * @param i The index of the desired input array element.
     * @return input[i].
     */    
    public final Object getInput( int i ) { return inputs[ i ]; }
    
    public final int numUnsetInputs() { return $unsetArgs; }
    
    /** get the TaskId of this Task.
     *
     * Not necessary for application deployment, but may be useful when testing.
     * @return the TaskId of this Task.
     */    
    public final TaskId getTaskId() { return taskId; }
    
    /** The host that is invoking this task's execute method will pre-fetch a task if
     * and only if this method evaluates to true.
     * @param environment The session environment.
     * @return true if the task should signal the its host to pre-fetch another task before
     * invoking this task's execute method.
     */    
    public boolean isAtomic( Environment environment ) { return false; }
    
    /** If true, invoke execute( Environment environment, int inputIndex) as
     * each input is received.
     */
    public boolean executeIncrementally( Environment environment ) { return false; }
    
    //END API
    
    final short     get$UnsetArgs()      { return $unsetArgs; }
    final boolean   isCached()          { return cached; }
    final ArrayList getChildren()        { return children; }
    final Service   getHost()            { return host; }
    /** Returns a reference to the Task's array of input Objects.
     * @return a reference to the Task's array of input Objects.
     */    
    final short     getLevel()           { return level; }
    final long      getPredecessorCriticalPathTime() 
    { 
        return predecessorCriticalPathTime; 
    }
    final Service   getSpawningHost()    { return spawningHost; }
    final int       getSuccessorIndex()  { return successorIndex; }  
    final TaskId    getSuccessorTaskId() { return successorTaskId; }
    final Service   getTaskServer()      { return taskServer; }    
    
    // boolean-valued methods
    
    final boolean isCompose() { return taskId.getChild() == 0; }
    
    // increment/decrement methods        
    //final short decr$UnsetArgs() { return --$unsetArgs; }
    
    final void setHost ( Service host ) { this.host = host; }   
    final void setCached( boolean cached ) { this.cached = cached; }
    
    /** set this task's ith input to value; decrement the unset inputs counter
     */
    final void setInput( int i, Object value ) 
    { 
        inputs[ i ] = value;
        --$unsetArgs;
    }   
    
    final void setPredecessorCriticalPathTime( long predecessorCriticalPathTime)
    {
        this.predecessorCriticalPathTime = predecessorCriticalPathTime;
    }
    
    final void setSpawningHost( Service spawningHost ) { this.spawningHost = spawningHost; }
    
    final void setTaskServer( Service taskServer ) { this.taskServer = taskServer; }    
    
    final void init( long sessionId, short computationId )
    {
        taskId = new TaskId ( sessionId, computationId );
        successorTaskId = taskId;
        successorIndex = RESULT;
        level = 0;
    }
    
    /**
     * Assumption: parent != null
     * Assumption: 0 < $unsetArgs; fits in short
     *
     * This is used to construct <I>composition</I> tasks. These tasks take the
     * arguments computed by a task's subtasks and compose them into a larger result.
     * Generally, this larger result is itself sent via setArg to a composition task
     * that is higher in the computation's spawn tree. In the boundary case, the
     * larger result is the result value for the entire computation.
     * subtasks <I>should</I> construct a composition task with exactly <I>n</I>
     * arguments, 1 for each subtask's computed output computed.
     *@param compose task that composes the outputs from this task's subtasks.
     */    
    final void initCompose( Task compose )
    {
        compose.taskServer = taskServer;
        compose.taskServerServiceName = taskServerServiceName;
        compose.successorTaskId = successorTaskId;
        compose.successorIndex = successorIndex;
        compose.$unsetArgs = (short) children.size();
        compose.taskId = new TaskId ( taskId, (short) 0 );
        compose.level = level;
        assert children.size() > 0;
        compose.inputs = new Object[ children.size() ];  
        children.add( compose );
    }
    
    /** Returns the number of inputs to this computational task.
     * @return the number of inputs to this computational task.
     */    
    public final int numInputs() { return inputs.length; }
    
    // !! replace with taskServerServiceId
    final ServiceName taskServerServiceName() { return taskServerServiceName; }
    
    final void taskServerServiceName( ServiceName taskServerServiceName ) 
    { 
        this.taskServerServiceName = taskServerServiceName; 
    }

    /** returns a String representation of the <CODE>Task</CODE>.
     * @return a String representation of the <CODE>Task</CODE>:
     *
     */    
    public String toString ()
    {
        StringBuffer taskString = new StringBuffer();
        taskString.append( taskId.toString() );
        taskString.append( ": $unsetArgs: " ).append($unsetArgs).append(" ");
        if ( inputs == null ) 
        {
            taskString.append( "NONE ").append( getClass() ).append( " " );
        }
        else 
        {
            for ( int i = 0; i < inputs.length; i++ ) 
            {
                taskString.append( i ).append( ": " );
                if (inputs[i] == null ) 
                {
                    taskString.append( "null " );
                }
                else 
                {
                    taskString.append( inputs[i].toString() );
                }
            }
        }        
        return new String ( taskString );
    }
	
}
