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
 * Spawns off threads for each particular instance of a Collector.
 * 
 * Created on: July 13, 2004
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external.services;

import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.ClientToHsp;

import java.security.AccessControlException;

public class CollectorManager
{
    //
    //-- Constants -----------------------------------------------------------

    /** Property containing the default collectors to start up. */
    public static final String PROPERTY_StartList = "jicos.services.CollectorManager.startList";

    /** A space-separated list of default collectors to start up. */
    public static final String DEFAULT_StartList = "http";

    //
    //-- Variables -----------------------------------------------------------

    /** Reference to the Host Service Provider. */
    protected ClientToHsp hsp;

    /** An array of the names of collectors to be started up. */
    private String[] startList;

    //
    //-- Constructors --------------------------------------------------------

    /**
     * No-argument constructor of Collector. Nobody should use it, so it is
     * private.
     */
    private CollectorManager()
    {
        this.startList = null;

        return;
    }

    //
    //-- Accessors -----------------------------------------------------------

    /**
     * Return the list of collectors that were specified to be started.
     */
    public String getStartList()
    {
        String result = null;

        if (null != this.startList)
        {
            StringBuffer stringBuffer = new StringBuffer();

            if (0 < this.startList.length)
            {
                stringBuffer.append( this.startList[0] );

                for (int c = 1; c < this.startList.length; ++c)
                {
                    stringBuffer.append( " " );
                    stringBuffer.append( this.startList[c] );
                }
            }

            result = stringBuffer.toString();
        }
        return (result);
    }

    /**
     * Return the HSP that the collectors are being associated with.
     */
    public ClientToHsp getHsp()
    {
        return (this.hsp);
    }

    //
    //-- Mutators ------------------------------------------------------------

    /**
     * Set the start list.
     * 
     * @param startList
     *            Space-separated list of collectors.
     */
    public void setStartList( String startList )
    {

        // Get the default list if necessary.
        String myStartList = startList;
        if (null == myStartList)
        {
            try
            {
                myStartList = (String) System.getProperty( PROPERTY_StartList,
                        DEFAULT_StartList );
            }
            catch (AccessControlException accessControlException)
            {
                myStartList = DEFAULT_StartList;
            }
        }

        // Break up the list into an array of strings.
        String[] startArray = myStartList.trim().replace( ',', ' ' ).split(
                "\\s" );
        java.util.Vector vector = new java.util.Vector();

        for (int index = 0; index < startArray.length; ++index)
        {
            // Handle two whitespaces in a row.
            if (!"".equals( startArray[index] ))
            {
                vector.add( startArray[index] );
            }
        }

        // Set the member variables.
        this.startList = (String[]) vector.toArray( startArray );

        return;
    }

    /**
     * Set the HSP to associate collectors with.
     * 
     * @param hsp
     *            The HSP
     */
    public void setHsp( ClientToHsp hsp )
    {
        this.hsp = hsp;
    }

    //
    //-- Methods -------------------------------------------------------------

    /**
     * Static constructor - launches the default Collectors.
     * 
     * @param hsp
     *            The HSP to associate the collectors with.
     */
    public static void startCollectors( ClientToHsp hsp )
    {
        CollectorManager collectorManager = new CollectorManager();
        collectorManager.setHsp( hsp );
        collectorManager.setStartList( null );
        collectorManager.startCollectors();
    }

    /**
     * Static constructor - Launch the specified Collectors using the specified
     * Extrernal request processor.
     * 
     * @param hsp
     *            The HSP to associate the collectors with.
     * @param startList
     *            Space-separated list of collectors.
     */
    public static void startCollectors( ClientToHsp hsp, String startList )
    {
        CollectorManager collectorManager = new CollectorManager();
        collectorManager.setHsp( hsp );
        collectorManager.setStartList( startList );
        collectorManager.startCollectors();
    }

    /**
     * Start up the specified collectors.
     */
    private void startCollectors()
    {
        // Get the logger
        //
        final java.util.logging.Logger logger = LogManager
                .getLogger( CollectorManager.class );

        for (int index = 0; index < this.startList.length; ++index)
        {
            String collectorName = this.startList[index];
            Collector collector = null;

            if (collectorName.toLowerCase().equals( "none" ))
            {
                collector = null;
                collectorName = null;
            }

            else if (collectorName.toLowerCase().endsWith( "http" ))
            {
                collector = new CollectorHttp( hsp );
                collectorName = "CollectorHttp";
            }

            else if (collectorName.toLowerCase().endsWith( "soap" ))
            {
                collector = new CollectorSoap( hsp );
                collectorName = "CollectorSoap";
            }

            else if (collectorName.toLowerCase().endsWith( "debug" ))
            {
                collector = new CollectorDebug( hsp );
                collectorName = "CollectorDebug";
            }

            else
            {
                // See if the person knows what they are doing.
                //
                try
                {
                    Class collClass;
                    Object collObject;

                    if (null == (collClass = Class.forName( collectorName )))
                    {
                        logger.warning( "Couldn't locate collector \""
                                + collectorName + "\"" );
                    }
                    else if (null == (collObject = collClass.newInstance()))
                    {
                        logger.warning( "Couldn't create collector \""
                                + collectorName + "\"" );
                    }
                    else if (!(collObject instanceof Collector))
                    {
                        logger.warning( "Class \"" + collectorName
                                + "\" is not a collector." );
                    }
                    else
                    {
                        // Holy smokes! They do!
                        collector = (Collector) collObject;
                        collector.setHsp( hsp );

                        // Get the base name
                        String[] name = collectorName.replace( '.', '\u2345' )
                                .split( "\u2345" );
                        collectorName = name[name.length - 1];
                    }

                }
                catch (Exception exception)
                {
                    logger.log( LogManager.WARNING,
                            "Couldn't start collector \"" + collectorName
                                    + "\"", exception );
                }
            }

            // If we determined what the collector was, start it up.
            //
            if (null != collector)
            {
                try
                {
                    Thread collectorThread = new Thread( collector );
                    collectorThread.setName( collectorName );
                    collectorThread.start();
                    logger.fine( "Starting \"" + collectorName + "\"...." );
                }
                catch (Exception exception)
                {
                    logger.log( LogManager.WARNING,
                            "Couldn't start collector \"" + collectorName
                                    + "\"", exception );
                }
            }
        }

        return;
    }
}