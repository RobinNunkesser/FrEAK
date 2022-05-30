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

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.Shared;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.XmlConverter;
import edu.ucsb.cs.jicos.services.external.XmlDocument;
import java.util.*;
import java.text.DateFormat;

import org.w3c.dom.Document;

/**
 * Has information about the computational resources used for a login session,
 * including Task statistics, broken out by Task class. These are grouped by
 * Host within TaskServer.
 */
public final class Invoice implements java.io.Serializable, XmlConverter

{
    private static final java.text.DateFormat dateFormatter = new java.text.SimpleDateFormat(
            "MMM d, yyyy @ HH:mm:ss" );

    private long beginTime;

    private long endTime;

    private ServiceTaskStats hspTaskStats;

    private static String[] labels =
        { "\n___________________________________________\n",
                "\n JICOS Invoice \n" };

    /**
     * Not part of the API.
     * 
     * @param beginTime
     *            The time when the consumer registred with the
     *            ProductionNetwork.
     * @param endTime
     *            The time when the consumer unregistered from the
     *            ProductionNetwork.
     * @param stats
     *            Statistics about resources consumed. For example, these
     *            contain the number of Producers that were registered with the
     *            ProductionNetwork <I>when the consumer registered </I>.
     */
    Invoice(long beginTime, long endTime, ServiceTaskStats hspTaskStats)
    {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.hspTaskStats = hspTaskStats;
    }

    /**
     * Get the time the login session began.
     * 
     * @return the time the login session began.
     */
    public long beginTime() { return beginTime; }

    /**
     * Get the time the login session ended.
     * 
     * @return Get the time the login session ended.
     */
    public long endTime() { return endTime; }

    /**
     * Returns a List of ServiceTaskStat objects, 1 for each TaskServer.
     * 
     * @return a List of ServiceTaskStat objects, one for each TaskServer.
     */
    public ServiceTaskStats hspTaskStats()
    {
        return hspTaskStats;
    }

    /**
     * Returns a String representation of the Invoice.
     * 
     * @return a String representation of the Invoice.
     */
    public String toString()
    {
        StringBuffer s = new StringBuffer().append( labels[0] );
        s.append( labels[1] );
        s.append( "\n Invoice date/time: " );
        s.append( DateFormat.getDateTimeInstance( DateFormat.FULL,
                DateFormat.FULL ).format( new Date() ) );
        s.append( "\n Registered:   " );
        Date date = new Date( beginTime );
        s.append( DateFormat.getDateTimeInstance( DateFormat.FULL,
                DateFormat.FULL ).format( date ) );
        s.append( "\n Unregistered: " );
        date = new Date( endTime );
        s.append( DateFormat.getDateTimeInstance( DateFormat.FULL,
                DateFormat.FULL ).format( date ) );
        s.append( "\n Start time = " + (beginTime / 1000) + "."
                + (beginTime % 1000) );
        long elapsedTime = endTime - beginTime;
        s.append( "\n Elapsed compute time = " + (elapsedTime / 1000) + "."
                + (elapsedTime % 1000) + " seconds \n" );

        s.append( "\n Task Statistics\n" );
        ServiceName serviceName = hspTaskStats.serviceName();
        HostTaskStats hspStats = new HostTaskStats( serviceName, false );
        List taskServersTaskStats = hspTaskStats.serviceTaskStats();
        int $hspHosts = 0;

        for (Iterator i = taskServersTaskStats.iterator(); i.hasNext();)
        {
            ServiceTaskStats taskServerTaskStats = (ServiceTaskStats) i.next();
            ServiceName taskServerName = taskServerTaskStats.serviceName();
            HostTaskStats taskServerStats = new HostTaskStats( taskServerName,
                    false );
            List hostsTaskStats = taskServerTaskStats.serviceTaskStats();
            $hspHosts += hostsTaskStats.size() - 1;
            for (Iterator j = hostsTaskStats.iterator(); j.hasNext();)
            {
                HostTaskStats hostTaskStats = (HostTaskStats) j.next();
                s.append( "\n     Host " );
                s.append( hostTaskStats.toString( "         " ) );
                taskServerStats.add( hostTaskStats );
            }
            s.append( "\n   Task Server total " );
            s.append( "Hosts: " );
            s.append( hostsTaskStats.size() - 1 );
            s.append( " " );
            s.append( taskServerStats.toString( "       " ) );
            s.append( "\n" ); // skip a line between TaskServers
            hspStats.add( taskServerStats );
        }
        s.append( "\n HSP total " );
        s.append( "Hosts: " );
        s.append( $hspHosts );
        s.append( " " );
        s.append( hspStats.toString( "    " ) );
        s.append( "\n" );
        s.append( labels[0] );
        return new String( s );
    }

    //----------------------------------------------------------------------
    /**
     * Encode a Java class into an XML-encoded string.
     * 
     * @param prefix
     *            Prepend to each line of output (null --> blank).
     */
    public String toXml( String prefix )
    {
        String NS = XmlConverter.NAMESPACE;
        String CRLF = "\r\n";

        // Convert a null to a space.
        prefix = (null != prefix) ? prefix : "";

        // Append a colon to the namespace if it exists.
        if (null == NS)
            NS = "";
        else if (!NS.equals( "" ))
            NS += ':';

        StringBuffer buffer = new StringBuffer( 1000 );

        buffer.append( prefix + "<" + NS + "Invoice>" + CRLF );

        buffer.append( prefix + "  <" + NS + "BeginTime" + " epoch=\""
                + String.valueOf( this.beginTime ) + "\"" + ">"
                + dateFormatter.format( new Date( this.beginTime ) ) + "</"
                + NS + "BeginTime>" + CRLF );

        buffer.append( prefix + "  <" + NS + "EndTime" + " epoch=\""
                + String.valueOf( this.endTime ) + "\"" + ">"
                + dateFormatter.format( new Date( this.endTime ) ) + "</" + NS
                + "EndTime>" + CRLF );

        if (null != this.hspTaskStats)
            buffer.append( this.hspTaskStats.toXml( "  " ) );

        buffer.append( prefix + "</" + NS + "Invoice>" + CRLF );

        return (new String( buffer ));
    }

    /**
     * Populate a Java class from an XML document.
     * 
     * @param externalData
     *            The data.
     * @return Success (<CODE>true</CODE>), or failure (<CODE>false
     *         </CODE>).
     */
    public boolean fromXml( ExternalData externalData )
    {
        return (false);
    }

    /**
     * Create an appropriate input object for this task.
     * 
     * @param externalData
     *            The data.
     * @return Input object (may be null).
     */
    public Object createInput( ExternalData externalData )
    {
        Object input = null;
        return (input);
    }

    /**
     *  Create the appropriate shared object for this task.
     *
     * @param   externalData  The data.
     * @return  Sharedc object (may be null).
     */
    public Shared createShared( ExternalData externalData )
    {
        Shared shared = null;
        return (shared);
    }

    /**
     *  Create an XML encoding for the result of the computation of this task.
     *
     * @param   result   The result from a compute.
     * @return  An DOM object, or null on error.
     */
    public XmlDocument createResult( Object result )
    {
        XmlDocument xmlDocument = null;
        return (xmlDocument);
    }

    public Document getStyleSheet( int styleSheetType )
    {
        Document xsltStyleSheet = null;

        switch (styleSheetType)
        {
            case XmlConverter.STYLESHEET_Unknown:
            case XmlConverter.STYLESHEET_Xml:
            case XmlConverter.STYLESHEET_Html:
                break;
        }

        return ((Document) null);
    }

    public String toHtmlString( XmlDocument result, String hostPort )
    {
        return ((String) null);
    }

}