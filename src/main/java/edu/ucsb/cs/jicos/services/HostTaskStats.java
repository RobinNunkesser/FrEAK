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
import edu.ucsb.cs.jicos.services.external.XmlConverter;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

import java.io.*;

/** Contains task execution statistics for 1 Host that are associated  with a single
 * login session.
 * @author Peter Cappello
 */
public class HostTaskStats extends ServiceTaskStats implements Serializable,
        XmlConverter {
    private int $tasksExecuted;

    private TaskClassStats taskClassStats = new TaskClassStats();

    private boolean isHost; // Should averages be output by toString?

    HostTaskStats(ServiceName serviceName, boolean isHost)
    {
        super( serviceName );
        this.isHost = isHost;
    }

    // copy constructor
    HostTaskStats(HostTaskStats hostTaskStats)
    {
        super( hostTaskStats.serviceName() );
        $tasksExecuted = hostTaskStats.$tasksExecuted;
        $tasksExecuted = hostTaskStats.$tasksExecuted;
        taskClassStats = (TaskClassStats) hostTaskStats.taskClassStats.clone();
        isHost = hostTaskStats.isHost;
    }

    /** Returns the number of Task objects executed by this Service.
     * @return the number of Task objects executed by this Service.
     */
    public int $tasksExecuted()
    {
        return $tasksExecuted;
    }

    /**
     * Aggregate the Host statistics.
     */
    void add( HostTaskStats hostTaskStats )
    {
        $tasksExecuted += hostTaskStats.$tasksExecuted();
        taskClassStats.add( hostTaskStats.taskClassStats() );
    }

    /**
     *  Invoke when the host completes a Task.
     */
    void add( TaskInfo taskInfo )
    {
        $tasksExecuted++;
        taskClassStats.add( taskInfo.className(), taskInfo.executeTime() );
    }

    void clear()
    {
        $tasksExecuted = 0;
        taskClassStats.clear();
    }

    /** Returns the TaskClassStats object associated with this Service.
     * @return the TaskClassStats object associated with this Service.
     */
    public TaskClassStats taskClassStats()
    {
        return taskClassStats;
    }

    /** Returns a String representation of this object.
     * @param pad A String that is prepended to the returned String.
     * @return a String representation of this object, including the Service name information,
     * the number of Task objects executed, [if the Service is a Host, the idle time
     * and the idle time per task], and the TaskClassStats.
     */
    public String toString( String pad )
    {
        StringBuffer s = new StringBuffer();
        s.append( serviceName().toString() );
        s.append( " Tasks executed: " );
        s.append( $tasksExecuted );
        s.append( "\n" );
        s.append( taskClassStats.toString( pad ) );
        return new String( s );
    }

    public String toXml( String prefix )
    {
        StringBuffer buffer = new StringBuffer( 1000 );
        String NS = edu.ucsb.cs.jicos.services.external.XmlConverter.NAMESPACE;
        String CRLF = "\r\n";

        // Convert a null to a space.
        prefix = (null != prefix) ? prefix : "";

        // Append a colon to the namespace if it exists.
        if (null == NS)
            NS = "";
        else if (!NS.equals( "" ))
            NS += ':';

        int listSize = (null != taskClassStats) ? taskClassStats.size() : 0;

        buffer.append( prefix + "<" + NS + "HostTaskStats" + " tasks=\""
                + String.valueOf( this.$tasksExecuted ) + "\"" + " numStats=\""
                + String.valueOf( listSize ) + "\""
                + ((0 == listSize) ? "/" : "") + ">" + CRLF );

        if (0 < listSize)
        {
            // Iterate through the list of keys.
            //
            java.util.Set keySet = this.taskClassStats.keySet();
            java.util.Iterator keyIterator = keySet.iterator();

            while (keyIterator.hasNext())
            {
                String key = (String) keyIterator.next();
                Integer value = (Integer) this.taskClassStats.get( key );

                buffer.append( prefix + "  <" + NS + "Stat>" + CRLF );

                buffer.append( prefix + "    <" + NS + "Task.Classname>" + key
                        + "</Task.ClassName>" + CRLF );
                buffer.append( prefix + "    <" + NS + "Task.ExecuteTime>"
                        + key + "</Task.ExecuteTime>" + CRLF );

                buffer.append( prefix + "  </" + NS + "Stat>" + CRLF );
            }
        }

        buffer.append( prefix + "</" + NS + "HostTaskStats>" + CRLF );

        return (new String( buffer ));
    }

    //----------------------------------------------------------------------
    /**
     *  Populate a Java class from an XML document.
     *
     * @param   xmlDocument   The XML DOM.
     * @return  Success (<CODE>true</CODE>), or failure (<CODE>false</CODE>).
     */
    public boolean fromXml( XmlDocument xmlDocument )
    {
        return (false);
    }

    //----------------------------------------------------------------------
    /**
     *  Create an appropriate input object for this task.
     *
     * @param   xmlDocument   The XML DOM.
     * @return  Input object (may be null).
     */
    public Object createInput( XmlDocument xmlDocument )
    {
        Object input = null;
        return (input);
    }

    //----------------------------------------------------------------------
    /**
     *  Create the appropriate shared object for this task.
     *
     * @param   xmlDocument   The XML DOM.
     * @return  Sharedc object (may be null).
     */
    public edu.ucsb.cs.jicos.services.Shared createShared(
            XmlDocument xmlDocument )
    {
        edu.ucsb.cs.jicos.services.Shared shared = null;
        return (shared);
    }

    //----------------------------------------------------------------------
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

}