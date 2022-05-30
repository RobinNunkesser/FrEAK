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

import java.io.*;
/** 
 * SessionInfo.java - A container of Client's session information.
 * @author Peter Cappello
 * @version 1.0
 */
public class SessionInfo implements Serializable
{
    // constants
    final static long NOBODYLOGGEDIN = -1;
    
    private long sessionId;
    private Environment environment;
    
    public SessionInfo() { sessionId = NOBODYLOGGEDIN; }
    
    SessionInfo( long sessionId, Environment environment )
    {
        this.sessionId = sessionId;
        this.environment = environment;
    }
    
    /* This copy is to get around the odd situation when a Host is instantiated
     * as part of a TaskServer instantiation within the same JVM (hence sharing
     * the same heap), & this one joins later. It's environment.host field is 
     * set, when it should be null.
     */
    SessionInfo copy()
    {
        SessionInfo copy = new SessionInfo();
        copy.sessionId = sessionId;
        copy.environment = environment.copy();
        return copy;
    }
    
    long        getSessionId  () { return sessionId; }
    Environment getEnvironment() { return environment; }
    
    public void setEnvironment( Environment environment ) { this.environment = environment; }

    /** Not part of the API.
     * (Used by the ProductionNetwork to creates new ConsumerId).
     * @param registrationId Distinguished this consumer registration (session) from all other consumer
     * sessions that concurrently exist.
     */    
    void setSessionId( long sessionId ) { this.sessionId = sessionId; }
    
    public String toString() { return getClass() + ": " + sessionId + ", Environment ..."; }
}