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
 * Supports multi-threaded execution: Multiple hosts can concurrently invoke 
 * TaskServer's Session, which invokes its TaskIdTrie object.
 *  !! Fix: Implement as true Trie
 *
 * @author  Peter Cappello
 */

/*
 * TaskIdTrie.java
 */

package edu.ucsb.cs.jicos.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


final class TaskIdTrie implements Serializable
{
    private Collection taskIdSet = new HashSet();
    
    synchronized void add( TaskId taskId ) { taskIdSet.add( taskId ); }
    
    synchronized boolean contains( TaskId taskId )
    {
        for ( Iterator iterator = taskIdSet.iterator(); iterator.hasNext(); )
         {
             TaskId nextTaskId = (TaskId) iterator.next();
             if ( nextTaskId.isAncestor( taskId ) )
             {
                 return true;
             }
         }
         return false;
    }
    
    public static void main( String[] args )
    {
        TaskIdTrie taskIdTrie = new TaskIdTrie();
        TaskId taskId1 = new TaskId( 1, (short) 1 );
        System.out.println("TaskId: " + taskId1 + " ?in TaskIdTrie: " + taskIdTrie.contains( taskId1 ) );
        taskIdTrie.add( taskId1 );
        System.out.println("TaskId: " + taskId1 + " ?in TaskIdTrie: " + taskIdTrie.contains( taskId1 ) );
        TaskId taskId2 = new TaskId( 1, (short) 2 );
        System.out.println("TaskId: " + taskId2 + " ?in TaskIdTrie: " + taskIdTrie.contains( taskId2 ) );
        taskIdTrie.add( taskId2 );
        System.out.println("TaskId: " + taskId2 + " ?in TaskIdTrie: " + taskIdTrie.contains( taskId2 ) );
    }
}
