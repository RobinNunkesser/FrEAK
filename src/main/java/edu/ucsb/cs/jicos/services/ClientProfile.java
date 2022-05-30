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
 *  Information about its currently waiting clients, if any, and its running
 *  client, if any, that is publicly available from the Hsp.
 *
 * @author  Peter Cappello
 */

/*
 * ClientProfile.java
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;
import java.util.*;


public final class ClientProfile implements java.io.Serializable
{
    private String userName;
    private String date;
    private String ipAddress;
    private String domainName;
    private long loginTime;
    
    ClientProfile( Client client ) 
    {
        userName = System.getProperty("user.name");
        date = new Date().toString();
        ServiceName serviceName = client.serviceName();
        ipAddress = serviceName.ipAddress();
        domainName = serviceName.domainName();
        loginTime = System.currentTimeMillis();
    }    
    
    /** Gets the user name associated with the client.
     * @return The user name associated with the client (e.g., on Unix, this is the login
     * name).
     */    
    public String getUserName() { return userName; }
     /** Get the date that the client accessed the Hsp.
      * @return The date that the client accessed the Hsp.
      */     
    public String getDate() { return date; } 
    /** Get the IP address of the machine from which the client logged in to the Hsp.
     * @return The IP address of the machine from which the client logged in to the Hsp.
     */    
    public String getIpAddress() { return ipAddress; }
    /** Get the domain name of the machine from which the client logged in to the Hsp.
     * @return The domain name of the machine from which the client logged in to the Hsp.
     */    
    public String getDomainName() { return domainName; }
    /** The time on the Hsp that the client login command was processed.
     * @return The time on the Hsp that the client login command was processed.
     */    
    public long getLoginTime() { return loginTime; }
    
    /** A string representation of the object.
     * @return userName + " at " + ipAddress + "(" + domainName + ") on " + date +
     * " login time: " + loginTime
     */    
    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append( userName );
        s.append( " at " );
        s.append( ipAddress );
        s.append( "(" );
        s.append( domainName );
        s.append( ") on " );
        s.append( date );
        s.append( " login time: " );
        s.append( loginTime );
        return new String( s );
    }
}
