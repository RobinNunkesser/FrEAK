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
 * Base class for collectors.
 * 
 * A collector is a thread that will bind to a particular port, accept
 * connections, convert them to Jicos Task classes, and submit them to the HSP.
 * 
 * Created on: July 13, 2004
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external.services;

import edu.ucsb.cs.jicos.services.ClientToHsp;

public abstract class Collector extends Thread
{

    //
    //-- Variables -----------------------------------------------------------

    /** Host Service Provider for this collector to use. */
    protected ClientToHsp hsp;

    /** Port that this collector is listening. */
    protected int port;

    /** Name of this host. */
    protected String host;

    //
    //-- Constructors --------------------------------------------------------

    /**
     * Default, no argument, constructor.
     */
    public Collector()
    {
        this.initialize();
    }

    /**
     * Create a collector associated with a particular HSP.
     * 
     * @param hsp
     *            The specific HSP to use to send external requests.
     */
    public Collector(ClientToHsp hsp)
    {
        this.initialize();
        this.hsp = hsp;
    }

    /**
     * Create a collector associated with a particular HSP and port.
     * 
     * @param hsp
     *            The specific HSP to use to send external requests.
     * @param port
     *            The TCP/UDP port being listened on.
     */
    public Collector(ClientToHsp hsp, int port)
    {
        this.initialize();
        this.hsp = hsp;
        this.port = port;
    }

    private void initialize()
    {
        this.hsp = null;
        this.port = 0;

        // Get the host name.
        //
        this.host = "localhost";
        try
        {
            host = java.net.InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (java.net.UnknownHostException ignoreUnknownHostException)
        {
        }
    }

    //
    //-- Accessors -----------------------------------------------------------

    /**
     * Get the HSP this collector is associated with.
     * 
     * @return A Hosting Service Provider (HSP).
     */
    public ClientToHsp getHsp()
    {
        return (this.hsp);
    }

    /**
     * Get the port being listened on by this collector.
     * 
     * @return bound port number.
     */
    public int getPort()
    {
        return (this.port);
    }

    /**
     * Return the name of this host.
     * 
     * @return This host name.
     */
    public String getHost()
    {
        return (this.host);
    }

    /**
     * Return a useful combination of hostname and port.
     * 
     * @return The "hostname:port"
     */
    public String getHostPort()
    {
        String hostPort = new String();

        if (null != this.host)
        {
            hostPort += this.host;
        }

        if (0 < this.port)
        {
            hostPort += ':' + String.valueOf( this.port );
        }

        return (hostPort);
    }

    //
    //-- Mutators ------------------------------------------------------------

    /**
     * Set the HSP to associate with.
     * 
     * @param hsp
     *            The HSP.
     */
    public void setHsp( ClientToHsp hsp )
    {
        this.hsp = hsp;
    }

    /**
     * Set the port to listen to.
     * 
     * @param port
     *            The port.
     */
    public void setPort( int port )
    {
        this.port = port;
    }

    /**
     * Set the name of this host (for the purposes of the collector).
     * 
     * @param host
     *            Host name.
     */
    public void setHost( String host )
    {
        this.host = host;
    }

    //
    //-- Inheritable Methods -------------------------------------------------

    /**
     * Force sub-classes to implement this method.
     */
    public abstract void run();

}