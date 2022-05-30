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

import java.util.*;
/**
 * SessionHSP.java - HSP view of the Client's session data.
 * @author  Peter Cappello
 * @version 1
 */
final class SessionHSP 
{
    private SessionInfo sessionInfo;
    private HashMap resultTokens = new HashMap(); // A result token is a DAG Id.
    private Vector results = new Vector();
    // startTime also serves as a sessionId.
    // After a client unregisters, there may still be eagerly scheduled
    // tasks being hosted by Producers. The TaskServers need to distinguish
    // outputs (spawns and setArgs) from tasks from a distinct 
    // sessions. To do this, each Task
    // has a sessionId, which is set to startTime.
    // sessionId is set for the root of each dag within the compute method.
    // Within the TaskServer spawn method, the task is given the 
    // sessionId of its parent.
    private long startTime = System.currentTimeMillis();
    private SessionStatistics sessionStatistics = new SessionStatistics();
    
    SessionHSP( Environment environment )
    {
        sessionInfo = new SessionInfo( startTime, environment );
    }
    
    Vector            getResults()           { return results; }
    HashMap           getResultTokens()      { return resultTokens; }
    SessionInfo       getSessionInfo()       { return sessionInfo; }
    SessionStatistics getSessionStatistics() { return sessionStatistics; }
    long              getStartTime()         { return startTime; }
}