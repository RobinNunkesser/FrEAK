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


public final class SessionStatistics implements java.io.Serializable
{
    private int $tasksSpawned;
    private int $tasksExecuted;
    private int $hosts;
    
    public SessionStatistics() {}

    // STOP USING THIS
    public SessionStatistics( SessionStatistics stats ) 
    {
        this.$tasksSpawned = stats.$tasksSpawned;
        this.$tasksExecuted = stats.$tasksExecuted;
        this.$hosts = stats.$hosts;
    }

    public void print()
    {
        System.out.println("Total tasks = " + $tasksSpawned );
        System.out.println("Hosts registered when client registered = " + $hosts );
    }

    public void set$producers( int $hosts ) { this.$hosts = $hosts; }

    public void set$tasksSpawned( int $tasksSpawned )
    {
        this.$tasksSpawned = $tasksSpawned;
    }

    public void set$tasksExecuted( int $tasksExecuted )
    {
        this.$tasksExecuted = $tasksExecuted;
    }
}