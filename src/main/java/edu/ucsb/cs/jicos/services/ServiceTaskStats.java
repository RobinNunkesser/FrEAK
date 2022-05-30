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
 * Contains Service name information and a List of Task statistics for a set of
 * subordinate Janet Services.
 * 
 * @author Peter Cappello
 */

package edu.ucsb.cs.jicos.services;

import edu.ucsb.cs.jicos.foundation.*;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.XmlDocument;
import edu.ucsb.cs.jicos.services.external.XmlConverter;

import java.util.*;

import org.w3c.dom.Document;

public class ServiceTaskStats implements java.io.Serializable, XmlConverter
{
    private ServiceName serviceName;

    private List serviceTaskStats = new LinkedList();

    ServiceTaskStats(ServiceName serviceName)
    {
        this.serviceName = serviceName;
    }

    void add( ServiceTaskStats statistics )
    {
        serviceTaskStats.add( statistics );
    }

    void addAll( List statistics )
    {
        serviceTaskStats.addAll( statistics );
    }

    void clear()
    {
        serviceTaskStats.clear();
    }

    /**
     * Returns a List of objects that contain Task statistics for a subordinate
     * Service. For an Hsp, it is a List of ServiceTaskStats, 1 for each
     * TaskServer; for a TaskServer it is a List of HostTaskStats objects, 1 for
     * each of its Hosts.
     * 
     * @return a List of ServiceTaskStat objects that contain Task statistics
     *         for a subordinate Service.
     */
    public List serviceTaskStats()
    {
        return serviceTaskStats;
    }

    /**
     * Returns the ServiceName object associated with this Service.
     * 
     * @return the ServiceName object associated with this Service.
     */
    public ServiceName serviceName()
    {
        return serviceName;
    }

    //
    //== Implements XmlConverter =============================================
    
    public String toXml( String prefix )
    {
        StringBuffer buffer = new StringBuffer( 1000 );
        String NS = XmlConverter.NAMESPACE;
        String CRLF = "\r\n";

        // Convert a null to a space.
        prefix = (null != prefix) ? prefix : "";

        // Append a colon to the namespace if it exists.
        if (null == NS)
            NS = "";
        else if (!NS.equals( "" ))
            NS += ':';

        String name = (null != this.serviceName) ? this.serviceName
                .domainName() : "unknown";
        int listSize = (null != this.serviceTaskStats) ? this.serviceTaskStats
                .size() : 0;

        buffer.append( prefix + "<" + NS + "ServiceTaskStats"
                + " serviceName=\"" + name + "\"" + " numStats=\""
                + String.valueOf( listSize ) + "\""
                + ((0 == listSize) ? "/" : "") + ">" + CRLF );

        if (0 < listSize)
        {
            java.util.Iterator statIterator = this.serviceTaskStats.iterator();
            String xml;

            while (statIterator.hasNext())
            {
                Object statistics = statIterator.next();
                xml = null;

                if (statistics instanceof HostTaskStats)
                    xml = ((HostTaskStats) statistics).toXml( prefix + "  " );
                else if (statistics instanceof ServiceTaskStats)
                    xml = ((ServiceTaskStats) statistics).toXml( prefix + "  " );

                if (null != xml)
                    buffer.append( xml );
            }
        }

        buffer.append( prefix + "</" + NS + "ServiceTaskStats>" + CRLF );

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
     * Create the appropriate shared object for this task.
     * 
     * @param externalData
     *            The data.
     * @return Sharedc object (may be null).
     */
    public Shared createShared( ExternalData externalData )
    {
        Shared shared = null;
        return (shared);
    }

    /**
     * Create an XML encoding for the result of the computation of this task.
     * 
     * @param result
     *            The result from a compute.
     * @return An DOM object, or null on error.
     */
    public XmlDocument createResult( Object result )
    {
        return (null);
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