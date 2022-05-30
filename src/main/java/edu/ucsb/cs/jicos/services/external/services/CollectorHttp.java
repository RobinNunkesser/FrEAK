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
 * Collect data in normal HTTP format, convert it to a Jicos Task, submit it to
 * the HSP, and either wait for an answer, or return a reference to the answer.
 * 
 * Created on: July 13, 2004
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.services.external.services;

import edu.ucsb.cs.jicos.foundation.LogManager;
import edu.ucsb.cs.jicos.services.ClientToHsp;
import edu.ucsb.cs.jicos.services.Task;
import edu.ucsb.cs.jicos.services.external.ExternalData;
import edu.ucsb.cs.jicos.services.external.ExternalDataList;
import edu.ucsb.cs.jicos.services.external.ExternalRequest;
import edu.ucsb.cs.jicos.services.external.ExternalRequestId;
import edu.ucsb.cs.jicos.services.external.ExternalResult;
import edu.ucsb.cs.jicos.services.external.XmlConverter;
import edu.ucsb.cs.jicos.services.external.XmlDocument;
import edu.ucsb.cs.jicos.services.external.services.ExternalRequestProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CollectorHttp extends Collector
{
    //
    //-- Constants -----------------------------------------------------------

    /** Default port for the CollectorHttp. */
    public static final int DEFAULT_PORT = 8181;

    /** System property that specifies the port number. */
    public static final String PROPERTY_PortNumber = "jicos.services.HttpCollector.port";

    /** Number of seconds to delay until the answer HTML page will refresh. */
    public static final int DELAY = 60;

    // An HTTP header.
    private static final String HTML_Header = "HTTP/1.1 200 OK\r\n"
            + "Content-type: text/html\r\n"
            + "\r\n"
            + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 TRANSITIONAL//EN\">\r\n";

    // HTTP message for a (generic) error.
    private static final String HTML_UnknownError = "<HTML>\r\n"
            + "<HEAD><TITLE>Error</TITLE></HEAD>\r\n"
            + "<BODY><FONT COLOR=red SIZE=\"+2\">Error</FONT></BODY>\r\n"
            + "</HTML>";

    // The two parts of an HTTP message.
    private static final int HEADER = 0;
    private static final int CONTENT = 1;

    private static final Logger logger = LogManager.getLogger();
    private static final Level DEBUG = LogManager.DEBUG;
    private static final Level FINEST = LogManager.FINEST;
    private static final Level INFO = LogManager.INFO;

    //
    //---Variables----------------------------------------------------------

    // What answers are available.
    private Map availableResultsMap;

    //
    //---Constructors-------------------------------------------------------

    /**
     * Default, no argument, constructor.
     */
    public CollectorHttp()
    {
        super();
        this.initialize( true );

        return;
    }

    /**
     * Construct a CollectorHttp associated with a particular HSP.
     * 
     * @param hsp
     *            The HSP
     */
    public CollectorHttp(ClientToHsp hsp)
    {
        super( hsp );
        this.initialize( true );

        return;
    }

    /**
     * Construct a CollectorHttp associated with a particular HSP on a
     * particular port.
     * 
     * @param hsp
     *            The HSP
     * @param port
     *            The port to bind to.
     */
    public CollectorHttp(ClientToHsp hsp, int port)
    {
        super( hsp, port );
        this.initialize( false );

        return;
    }

    // Initialize the collector.
    private void initialize( boolean findPort )
    {
        this.availableResultsMap = new HashMap();

        // Determine the port?
        if (findPort)
        {
            int portNumber = DEFAULT_PORT;

            //Set the port number.
            String portString = System.getProperty( PROPERTY_PortNumber );
            if (null != portString)
            {
                try
                {
                    portNumber = Integer.parseInt( portString );
                }
                catch (NumberFormatException ignoreNumberFormtException)
                {
                }
            }

            setPort( portNumber );
        }
    }

    //
    //-- Methods -------------------------------------------------------------

    /**
     * Start up this thread.
     */
    public void run()
    {
        // Name the thread.
        this.setName( "CollectorHttp" );

        ServerSocket serverSocket = null;

        try
        {
            serverSocket = new ServerSocket( port );
        }
        catch (java.net.BindException bindException)
        {
            logger.log( LogManager.SEVERE,
                            "There is already a socket bound to "
                                    + super.getHostPort(), bindException );

            //          ***********************
            return; // *** DOES NOT CONTINUE ***
            //          ***********************
        }
        catch (java.io.IOException ioException)
        {
            logger.log( LogManager.SEVERE,
                    "Couldn't create CollectorTest running on "
                            + super.getHostPort(), ioException );

            //          ***********************
            return; // *** DOES NOT CONTINUE ***
            //          ***********************
        }

        // Announce that it's ready.
        String logMsg = "Started CollectorHttp at " + super.getHostPort();
        logger.log( LogManager.INFO, logMsg );

        while (true)
        {
            int i = 1;
            try
            {
                Socket clientSocket = serverSocket.accept();
                logMsg = "Got a connection from: "
                        + clientSocket.getInetAddress().getCanonicalHostName();
                logger.log( LogManager.FINE, logMsg );

                processRequest( clientSocket );

            }
            catch (java.io.IOException ioException)
            {
                logMsg = ioException.getMessage();
                logger.log( LogManager.WARNING, logMsg );
            }
        }
    }

    // Process the request.
    private void processRequest( Socket client )
    {

        try
        {
            String[] input = getHttpInput( client );
            String header = input[HEADER];
            String content = input[CONTENT];

            // Submit request.
            //   "POST /ExternalRequest ..."
            if (input[HEADER].startsWith( "POST /ExternalRequest" ))
            {
	        logger.log( FINEST, "POST /ExternalRequest" );
                postExternalRequest( client, content );
            }

            // Dynamic page creation
            //   "GET /ExternalRequest/classname ..."
            else if (input[HEADER].startsWith( "GET /ExternalRequest/" ))
            {
	        logger.log( FINEST, "GET /ExternalRequest" );
                getExternalRequest( client, header );
            }

            // Get result
            //   "GET /ExternalResult&externalRequestId=xyz ..."
            else if (input[HEADER].startsWith( "GET /ExternalResult" ))
            {
	        logger.log( FINEST, "GET /ExternalResult" );
                getExternalResult( client, header );
            }

        }
        catch (Exception anyException)
        {
            String html = (new XmlDocument( anyException )).toHtmlString();
            sendHtml( html, client );
            closeSocket( client );
        }
    }

    // Try and send a response back since the client is waiting for a response.
    private void sendHtml( String html, Socket socket )
    {
        try
        {
            java.io.OutputStream outputStream = socket.getOutputStream();

            String plusHeader = HTML_Header + html;
            byte[] responseBuffer = plusHeader.getBytes();
            outputStream.write( responseBuffer );

        }
        catch (Exception anyException)
        {
            logger.log( LogManager.INFO,
                    "Error sending response", anyException.getCause() );
        }
    }

    private void closeSocket( Socket socket )
    {
        try
        {
            socket.close();
        }
        catch (Exception anyException)
        {
            logger.log( LogManager.WARNING,
		    anyException.getClass().getName() + ": "
			+ anyException.getMessage() );
        }
    }

    /**
     * Handle a "POST /ExternalRequest" - submission of a request. This function
     * returns no output.
     * 
     * @param socket
     *            The client connection.
     * @param input
     *            Data that has already been read from client.
     * @throws Exception
     *             Catch all exception, since I'm too lazy to specify them.
     * @throws RemoteException
     *             This is an RMI class.
     */
    private void postExternalRequest( Socket socket, String input )
            throws Exception, RemoteException
    {
        String txtResponse = null;

        String[] variables = input.replace( '&', '\u2345' ).split( "\u2345" );
        ExternalDataList variableList = new ExternalDataList();
        String[] splitter;
        String key = null;
        String value = null;
        for (int v = 0; v < variables.length; ++v)
        {
            splitter = variables[v].split( "=" );

            if (1 == splitter.length)
            {
                key = htmlUnMangle( splitter[0] );
                value = null;
            }
            else
            {
                key = htmlUnMangle( splitter[0] );
                value = htmlUnMangle( splitter[1] );
            }

            variableList.add( key, value );
        }

        // Determine response method and launch thread.
        HttpResponseMethod responseMethod = new HttpResponseMethod(
                variableList );
        HttpRequestThread requestThread = new HttpRequestThread( socket,
                responseMethod, this.hsp, variableList );
        requestThread.setName( "HttpCollector$HttpRequestThread" );
        requestThread.start();
    }

    /**
     * Handle a "GET /ExternalRequest" - request for a dynamic page.
     * 
     * @param input
     *            The content of the HTTP message (header and content).
     * @return HTML encoded text..
     * @throws Exception
     */
    private void getExternalRequest( Socket socket, String input )
    {
        String html = null;
        String header = input;

        // Check to see if this is a page request.
        //
        header = header.substring( 21, header.indexOf( " HTTP" ) );
        if (1 < header.length())
        {
            if (header.startsWith( "jicos." ))
                header = "edu.ucsb.cs." + header;
            html = requestForPage( header );
        }

        sendHtml( html, socket );
        closeSocket( socket );
    }

    /**
     * Handle a "GET /ExternalResult" - request for an answer of a previously
     * submitted request.
     * 
     * @param input
     *            The content of the HTTP message (header and content).
     * @return HTML encoded text of the response.
     * @throws Exception
     */
    private String getExternalResult( Socket socket, String input )
            throws Exception
    {
        String html = null;

        int ndx = input.indexOf( "requestId=" );

        if (-1 != ndx)
        {
            ndx += 10;
            int space = input.indexOf( " HTTP", ndx );
            String requestId = input.substring( ndx, space );
            String url = "http://" + getHostPort()
                    + "/ExternalResult&requestId=" + requestId;

            ExternalRequestId externalRequestId = new ExternalRequestId(
                    requestId );
            ExternalResult externalResult = getResult( externalRequestId );

            if (null == externalResult)
            {
                html = toHtml( externalRequestId );
            }
            else
            {
                html = toHtml( externalResult );
            }

            sendHtml( html, socket );
            closeSocket( socket );
        }
        else
        {
            html = unknownRequestId();
        }

        return (html);
    }

    /**
     * Get the entire HTTP message.
     * 
     * @param socket
     *            A connection from the web client.
     * @return The HTTP content (header is stripped).
     * @throws IOException
     */
    private String[] getHttpInput( Socket socket ) throws IOException
    {

        int ndx = -1; // Positional index.
        int end = -1; // Positional index.
        String header;
        String content;
        int contentLength;
        int leftToRead = 0;

        // Initialize
        InputStream inputStream = socket.getInputStream();
        byte[] rqstBuffer = new byte[2048];

        // Get the first chunk.
        int bytesRecvd = inputStream.read( rqstBuffer, 0, rqstBuffer.length );
        header = new String( rqstBuffer, 0, bytesRecvd );
        int totalRecvd = bytesRecvd;

        // Definitely don't have the whole thing if we don't have
        // a double carraige return - line feed (\r\n\r\n).
        //
        while (-1 == (end = header.indexOf( "\r\n\r\n" )))
        {
            bytesRecvd = inputStream.read( rqstBuffer, 0, rqstBuffer.length );
            header += new String( rqstBuffer, 0, bytesRecvd );
            totalRecvd += bytesRecvd;
        }

        // See if there is content, or more specifically a Content-Length:
        // header field.
        if (-1 != (ndx = header.indexOf( "Content-Length: " )))
        {
            int endLine = header.indexOf( "\r\n", ndx );

            contentLength = Integer.parseInt( header.substring( ndx + 16,
                    endLine ) );
            content = header.substring( end + 4, totalRecvd );
            header = header.substring( 0, end + 2 );
            
            // Now that the content length is known, the total number of
            // bytes to gather off the socket is known. So read that much.
            leftToRead = contentLength - totalRecvd;
        }
        else
        {
            content = "";
            header = header.substring( 0, end + 2 );
            leftToRead = 0;
        }

        // Keep reading until we've read it all.
        while (0 < leftToRead)
        {
            bytesRecvd = inputStream.read( rqstBuffer, 0, rqstBuffer.length );
            content += new String( rqstBuffer, 0, bytesRecvd );
            leftToRead -= bytesRecvd;
        }

        // I could have done "new String[]{ header, content }" here, but
        // I'm paranoid about changing array layouts. So I create it
        // the slow, but safe, way.
        String[] httpMessage = new String[2];
        httpMessage[HEADER] = header;
        httpMessage[CONTENT] = content;

        return (httpMessage);
    }

    /**
     * Convert an external result to HTML text.
     * 
     * @param externalResult
     *            A result.
     * @return The HTML encoded text.
     * @throws Exception
     *             Because Task.createResult can throw this.
     */
    private String toHtml( ExternalResult externalResult ) throws Exception
    {
        String html = null;

        // Preliminary (error) result.
        html = "<HTML>\r\n" + "<HEAD><TITLE>Unknown Error</TITLE></HEAD>\r\n"
                + "<BODY>" + "<FONT COLOR=red SIZE=\"+2\">Error</FONT><BR>\r\n"
                + "Unknown cause\r\n" + "</BODY>\r\n" + "</HTML>\r\n";

        Task task = externalResult.getTask();
        XmlConverter xmlConverter = (XmlConverter)task;
        Object result = externalResult.getResult().getValue();
        XmlDocument xmlResult = xmlConverter.createResult( result );
        org.w3c.dom.Document xsltStyleSheet;
        if (null != (xsltStyleSheet = xmlConverter
                .getStyleSheet( XmlConverter.STYLESHEET_Html )))
        {
            html = xmlResult.Transform( xsltStyleSheet );
        }

        else if (null == (html = xmlConverter.toHtmlString( xmlResult, getHostPort() )))
        {
            // Raw Result
            html = "<HTML>\r\n"
                    + "<HEAD><TITLE>Result (raw)</TITLE></HEAD>\r\n"
                    + "<BODY>"
                    + "<FONT COLOR=green SIZE=\"+2\">Result</FONT>&nbsp;&nbsp;(raw)<BR>\r\n"
                    + "<PRE>\r\n"
                    + xmlResult.toXmlString().replaceAll( "<", "&lt;" )
                            .replaceAll( ">", "&gt;" ).replaceAll( " ",
                                    "&nbsp;" ) + "</PRE>\r\n" + "</BODY>\r\n"
                    + "</HTML>\r\n";
        }

        return (html);
    }

    /**
     * Convert an external result identifier to HTML page that will allow the
     * client browser to request the answer.
     * 
     * @param externalRequestId
     *            The external request id.
     * @return The HTML encoded text.
     * @throws NullPointerException
     *             If argument is null.
     */
    public String toHtml( ExternalRequestId externalRequestId )
    {

        String url = "http://" + getHostPort() + "/ExternalResult&requestId="
                + externalRequestId.getId();

        java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
        calendar.add( java.util.Calendar.SECOND, DELAY );
        java.util.Date date = calendar.getTime();

        String html = "<HTML>\r\n"
                + "<HEAD>\r\n"
                + "  <TITLE>Response not available</TITLE>\r\n"
                + "  <META HTTP-EQUIV=Refresh CONTENT=\""
                + DELAY
                + "; URL="
                + url
                + "\">\r\n"
                + "</HEAD>\r\n"
                + "<BODY>\r\n"
                + CollectorHttp.jicosHtmlHeader()
                + "<CENTER>\r\n"
                + "<H2><FONT COLOR=\"blue\">Response not (yet) available</FONT></H2>\r\n"
                + "This page will check every " + DELAY
                + " seconds for an answer to request you submitted.<BR>\r\n"
                + "<BR><FONT SIZE=\"-1\">(Next check: " + date.toString()
                + ")</FONT><BR>\r\n" + "<BR><BR>\r\n"
                + "Or, you can check now at:<BR><BR>" + "<A HREF=\"" + url
                + "\">" + url + "</A>\r\n" + "<BR>\r\n" + "<BR>\r\n"
                + CollectorHttp.jicosHtmlFooter() + "</BODY>\r\n"
                + "</HTML>\r\n";

        return (html);
    }

    /**
     * Create a "requestid unknown" HTML page.
     * 
     * @return HTML text.
     */
    public String unknownRequestId()
    {

        String html = "<HTML>\r\n"
                + "<HEAD>\r\n"
                + "  <TITLE>Invalid Query</TITLE>\r\n"
                + "</HEAD>\r\n"
                + "<BODY>\r\n"
                + CollectorHttp.jicosHtmlHeader()
                + "<CENTER>\r\n"
                + "<H2><FONT COLOR=\"red\">Invalid Query</FONT></H2>\r\n"
                + "The page submitted did not specify a external request id\r\n"
                + "<BR>\r\n" + CollectorHttp.jicosHtmlFooter() + "</BODY>\r\n"
                + "</HTML>\r\n";

        return (html);
    }

    // Put a result in.
    private synchronized void saveResult( ExternalResult externalResult )
    {
        if (null != externalResult)
        {
            ExternalRequestId externalRequestId = externalResult
                    .getExternalRequestId();
            if (null == externalRequestId)
            {
                logger.log( LogManager.WARNING,
                        "ExternalResult has no ExternalRequestId" );
            }
            else
            {
                String key = externalRequestId.getId();
                this.availableResultsMap.put( key, externalResult );
            }
        }
    }

    // Get (and remove) a result out.
    private synchronized ExternalResult getResult(
            ExternalRequestId externalRequestId )
    {

        ExternalResult externalResult = null;
        String key = externalRequestId.getId();

        if (null != this.availableResultsMap.get( key ))
        {
            externalResult = (ExternalResult) this.availableResultsMap
                    .remove( key );
        }

        return (externalResult);
    }

    // Create HTML error text for no class found.
    private String requestForPage( String taskClassName )
    {
        String html = new String();

        try
        {
            Class cl = Class.forName( taskClassName );
            XmlConverter task = (XmlConverter) cl.newInstance();

            if (null == (html = task.toHtmlString( null, getHostPort() )))
            {
                html = "<HTML>\r\n"
                        + "<HEAD><TITLE>No Page Defined</TITLE></HEAD>\r\n"
                        + "<BODY>\r\n"
                        + "<FONT COLOR=yellow SIZE=\"+1\">Error</FONT>\r\n"
                        + "This task (<I>" + taskClassName
                        + "</I>) has no default page defined.\r\n"
                        + "</BODY>\r\n" + "</HTML>";
            }
        }

        // Assume that an exception means no such class.
        catch (Exception ignoreException)
        {
            html = "<HTML>\r\n"
                    + "<HEAD><TITLE>Class Unknown</TITLE></HEAD>\r\n"
                    + "<BODY>\r\n"
                    + "<FONT COLOR=red SIZE=\"+2\">Error</FONT>\r\n"
                    + "Couldn't find task: <CODE>" + taskClassName
                    + "</CODE>\r\n" + "</BODY>\r\n" + "</HTML>";
        }

        return (html);
    }

    //
    //-- HTTP Manipulation Methods -------------------------------------------

    /**
     * Unmangle a value from HTTP (translating all the %xx --> characters).
     * 
     * @param mangled
     *            The Mangled string.
     * @return The "unmangled" string.
     */
    public static String htmlUnMangle( String mangled )
    {
        StringBuffer unMangled = null;

        if (null != mangled)
        {
            mangled = mangled.replace( '+', ' ' );
            int len = mangled.length();
            int ndx = mangled.indexOf( '%' );
            int begin = 0;
            unMangled = new StringBuffer();

            while (-1 != ndx)
            {

                // Save the text up to here.
                if (begin < ndx)
                    unMangled.append( mangled.substring( begin, ndx ) );
                begin = ndx;

                // If we have enough characters, then try and convert.
                if (ndx < len - 2)
                {
                    try
                    {
                        String hex = mangled.substring( ndx + 1, ndx + 3 );
                        char c = (char) Integer.parseInt( hex, 16 );
                        unMangled.append( c );
                        begin += 3;
                    }
                    catch (NumberFormatException ignoreNumberFormatException)
                    {
                    }
                }

                // Go get the next one.
                ndx = mangled.indexOf( '%', begin );
            }

            // Add whatever is left.
            if (begin < len)
            {
                unMangled.append( mangled.substring( begin ) );
            }
        }

        return (new String( unMangled ));
    }

    /**
     * Create HTML for the "respond by" box.
     * 
     * @param respondOptions
     *            An array of possible options.
     * @return HTML text
     */
    public static String createResponseSelect( String[] respondOptions )
    {

        // Make a local copy so we don't mess with the values.
        String[] myRespondOptions = respondOptions;
        // Fix nulls
        if (null == myRespondOptions)
        {
            myRespondOptions = ExternalRequest.RESPONSE_ActionList;
        }

        // Create HTML
        String html = "<!>\r\n"
                + "<!-- =============== What to do on a response. =============== -->\r\n"
                + "<!>\r\n"
                + "<HR WIDTH=\"90%\">\r\n"
                + "<CENTER>\r\n"
                + "<TABLE>\r\n"
                + "  <TR>\r\n"
                + "    <TD BGCOLOR=\"blue\">\r\n"
                + "      <TABLE BORDER=0 CELLPADDING=\"3px\" BGCOLOR=\"white\">\r\n"
                + "        <TR>\r\n"
                + "          <TD ALIGN=\"right\">Respond:</TD>\r\n";

        if (0 < myRespondOptions.length)
        {
            html += "" + "          <TD><SELECT NAME=\""
                    + ExternalRequest.RESPONSE_Action + "\">\r\n"
                    + "                <OPTION SELECTED>" + myRespondOptions[0]
                    + "</OPTION>\r\n";

            for (int option = 1; option < myRespondOptions.length; ++option)
            {
                html += "                <OPTION>" + myRespondOptions[option]
                        + "</OPTION>\r\n";
            }
            html += "              </SELECT>\r\n" + "            </TD>\r\n";
        }
        else
        {
            html += "            <TD>&nbsp;</TD>";
        }

        html += "" + "          <TD>&nbsp;</TD>\r\n"
                + "          <TD>E-Mail:&nbsp;<INPUT TYPE=\"text\"\r\n"
                + "                     NAME=\""
                + ExternalRequest.RESPONSE_EMail + "\"\r\n"
                + "                     SIZE=30></INPUT>\r\n"
                + "          </TD>\r\n" + "        </TR>\r\n"
                + "      </TABLE>\r\n" + "    </TD>\r\n" + "  </TR>\r\n"
                + "</TABLE>\r\n" + "<BR>\r\n"
                + "<INPUT TYPE=\"reset\" VALUE=\"Reset all values\">\r\n"
                + "</CENTER>\r\n" + "<!>\r\n"
                + "<!-- End of response selector -->\r\n";

        return (html);

    }

    // Print the request (for debugging)
    private void dumpRequest( String request, java.util.List list )
    {
        dumpRequest( request, list, "" );
    }

    // Print the request (for debugging) preceeded with a label.
    private void dumpRequest( String request, java.util.List list, String label )
    {
        final String dashes = "-------------------------------------------------------------------------------------------------------------------------------------------";
        final String squiggles = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";

        try
        {
            String homeDir = (String) System.getProperty( "user.home", "." );
            File logFile = new File( homeDir + "/HttpCollector.log" );
            System.err
                    .println( "DEBUG HttpCollector.RequestHandlingThread.dumpRequest()'logFile = " );
            java.io.PrintStream printStream = new java.io.PrintStream(
                    new java.io.FileOutputStream( logFile, true ) );
            String tmp = "--( " + label + ":  "
                    + (new java.util.Date()).toString() + ")" + dashes;

            printStream.println();
            printStream.println();
            printStream.println( tmp.substring( 0, 80 ) );
            printStream.println( "-->" );
            printStream.print( request );
            printStream.println();
            printStream.println( "<--" );

            if (null != list)
            {
                printStream.println();
                printStream.println( ("~~ Variables " + squiggles).substring(
                        0, 80 ) );
                printStream.println();

                Iterator iterator = list.iterator();

                while (iterator.hasNext())
                {
                    String[] keyValue = (String[]) iterator.next();
                    if (1 < keyValue.length)
                    {
                        printStream.println( "  " + keyValue[0] + " = \""
                                + keyValue[1] + "\"" );
                    }
                }
            }

            printStream.println();
            printStream.println( dashes.substring( 0, 80 ) );
            printStream.println();

            System.out.println( "Dumped request to "
                    + logFile.getAbsolutePath() );

        }
        catch (java.io.IOException ioException)
        {
            ioException.printStackTrace( System.err );
        }
    }

    /**
     * Create the traditional Jicos header for a dynamically created web page.
     * 
     * @return HTML text.
     */
    public static String jicosHtmlHeader()
    {
        String html = ""
                + "<!-- Stylesheets -->\r\n\r\n"
                + "<STYLE TYPE=\"text/css\">\r\n"
                + "\r\n<!--\r\n\r\n"

                + "A.none {\r\n"
                + "    text-decoration: none;\r\n"
                + "}\r\n\r\n"

                + "BODY {\r\n"
                + "    color: black;\r\n"
                + "    background: white;\r\n"
                + "    font-family: Helvetica,sans-serif;\r\n"
                + "}\r\n\r\n"

                + "DIV.footer {\r\n"
                + "    border-left: none;\r\n"
                + "    border-right: none;\r\n"
                + "    border-top: solid;\r\n"
                + "    border-top-width: thin;\r\n"
                + "    border-color: rgb(0,0,203);\r\n"
                + "    border-bottom: solid;\r\n"
                + "    border-bottom-width: thin;\r\n"
                + "    border-color: rgb(0,0,203);\r\n"
                + "    margin-left: 0;\r\n"
                + "    margin-right: 0;\r\n"
                + "}\r\n\r\n"

                + "\r\n-->\r\n\r\n"
                + "</STYLE>\r\n"
                + "<!-- Begin pseudo-Jicos header -->\r\n"
                + "<!>"
                + "<CENTER>\r\n"
                + "<TABLE BORDER=\"0\" CELLSPACING=\"0\" WIDTH=\"800\">\r\n"
                + "  <TR BGCOLOR=\"#DDFFDD\">\r\n"
                + "    <TD WIDTH=\"100%\" HEIGHT=\"30\" ALIGN=\"center\" VALIGN=\"center\">"
                + "<B><FONT COLOR=\"#0000CC\" SIZE=\"+1\">"
                + "J &nbsp; I &nbsp; C &nbsp; O &nbsp; S"
                + "</FONT></B></TD>\r\n"
                + "  </TR>\r\n"
                + "  <TR BGCOLOR=\"DDDDFF\">\r\n"
                + "    <TD WIDTH=\"100%\">&nbsp;</TD>"
                + "  </TR>\r\n"
                + "  <TR><TD>\r\n"
                + "<!-- All content should be contained in this table... -->\r\n"
                + "<!>\r\n";

        return (html);
    }

    /**
     * Create the traditional Jicos footer for a web page.
     * 
     * @return HTML text.
     */
    public static String jicosHtmlFooter()
    {
        String html = ""
                + "<!-- Footer -->\r\n"
                + "<!>\r\n"
                + "<DIV CLASS=\"footer\">\r\n"
                + "<TABLE WIDTH=\"800\">\r\n"
                + "  <TR>\r\n"
                + "    <TD WIDTH=\"3%\">&nbsp;</TD>\r\n"
                + "    <TD WIDTH=\"47%\" ALIGN=\"left\">\r\n"
                + "      <A CLASS=\"none\" HREF=\"http://www.cs.ucsb.edu/projects/jicos/\">"
                + "<FONT SIZE=\"-1\">http://www.cs.ucsb.edu/projects/jicos</FONT></A></TD>\r\n"
                + "    <TD WIDTH=\"47%\" ALIGN=\"right\"><FONT SIZE=\"-1\">"
                + (new Date()).toString() + "</FONT></TD>\r\n"
                + "    <TD WIDTH=\"3%\">&nbsp;</TD>\r\n" + "  </TR>\r\n"
                + "</TABLE>\r\n" + "</DIV>\r\n"
                + "<!--  End of content table -->\r\n"
                + "</TR></TD></TABLE>\r\n" + "</CENTER>\r\n";

        return (html);
    }

    //========================================================================
    //== Inner Classes =======================================================
    //========================================================================

    // The thread that actually extends the ERP thread.

    private class HttpRequestThread extends ExternalRequestProcessor
    {

        //
        //-- Variables ---------------------------------------------------

        private Socket socket;
        private HttpResponseMethod httpResponseMethod;
        private ExternalData externalData;

        //
        //-- Constructors ------------------------------------------------

        private HttpRequestThread(Socket socket,
                HttpResponseMethod httpResponseMethod, ClientToHsp clientToHsp,
                ExternalData externalData) throws RemoteException
        {

            super( clientToHsp );

            this.socket = socket;
            this.httpResponseMethod = httpResponseMethod;
            this.externalData = externalData;
        }

        //
        //-- Methods -----------------------------------------------------

        public void run()
        {
            String html = null;

            ExternalRequest externalRequest = null;
            ExternalRequestId externalRequestId = null;
            ExternalResult externalResult = null;
            boolean processRequest = true;

            try
            {
                switch (httpResponseMethod.getAction())
                {

                    case ExternalRequest.ACTION_ByWeb:
                        try
                        {
                            externalRequest = makeRequest( externalData );
                            externalRequestId = externalRequest
                                    .getExternalRequestId();
                            html = toHtml( externalRequestId );
                        }
                        catch (Exception anyException)
                        {
                            html = XmlDocument.toHtmlString( anyException );
                            processRequest = false;
                        }
                        sendHtml( html, this.socket );
                        closeSocket( this.socket );

                        if (processRequest)
                        {
                            externalResult = waitForResult( externalRequest );
                            saveResult( externalResult );
                        }
                        break;

                    case ExternalRequest.ACTION_ByEMail:
                        try
                        {
                            externalRequest = makeRequest( externalData );
                            externalRequestId = externalRequest
                                    .getExternalRequestId();
                            html = toHtml( externalRequestId );
                        }
                        catch (Exception anyException)
                        {
                            html = XmlDocument.toHtmlString( anyException );
                            processRequest = false;
                        }
                        sendHtml( html, this.socket );
                        closeSocket( this.socket );

                        if (processRequest)
                        {
                            externalResult = waitForResult( externalRequest );
                            externalResult.mailTo( httpResponseMethod
                                    .getEMail() );
                        }
                        break;

                    default:
                    case ExternalRequest.ACTION_Immediate:
                        try
                        {
                            XmlConverter task = null;
                            externalRequest = makeRequest( externalData );
                            
                            if( externalRequest.isReady() )
                            {
	                            externalResult = waitForResult( externalRequest );
	                            html = externalResult.toHtmlString( getHostPort() );
                            }
                            else if( null != (task = (XmlConverter)externalRequest.getTask()) )
                            {
                                html = task.toHtmlString( null, getHostPort() );
                            }
                        }
                        catch (Exception anyException)
                        {
                            html = XmlDocument.toHtmlString( anyException );
                        }
                        sendHtml( html, this.socket );
                        closeSocket( this.socket );
                        break;
                }

            }
            catch (Exception anyException)
            {
                html = (new XmlDocument( anyException )).toHtmlString();
                sendHtml( html, this.socket );
                closeSocket( this.socket );
            }
        }

    }

    //== End HttpRequestThread ===============================================

    //== Begin HttpResponseMethod ============================================
    /**
     * Define actions to be taken upon response.
     */
    private class HttpResponseMethod
    {
        private int action;

        private String eMail;

        //
        //-- Constructors ------------------------------------------------

        public HttpResponseMethod()
        {
            this.Initialize();
        }

        public HttpResponseMethod(String eMail)
        {
            this.action = ExternalRequest.ACTION_ByEMail;
            this.eMail = eMail;
        }

        public HttpResponseMethod(ExternalData externalData)
        {
            this.Initialize();

            // Get notification action, and (if necessary) e-mail address.
            String actionName = externalData
                    .getValue( ExternalRequest.RESPONSE_Action );

            //
            if (null == actionName)
            {
            }
            else if (actionName.equals( ExternalRequest.RESPONSE_Immediate ))
            {
                this.action = ExternalRequest.ACTION_Immediate;
            }
            else if (actionName.equals( ExternalRequest.RESPONSE_ByWeb ))
            {
                this.action = ExternalRequest.ACTION_ByWeb;
            }
            else if (actionName.equals( ExternalRequest.RESPONSE_ByEMail ))
            {
                this.action = ExternalRequest.ACTION_ByEMail;
                this.eMail = externalData
                        .getValue( ExternalRequest.RESPONSE_EMail );
            }
        }

        private void Initialize()
        {
            this.eMail = null;
            this.action = ExternalRequest.ACTION_Immediate;
        }

        //
        //-- Accessors ---------------------------------------------------

        public int getAction()
        {
            return (this.action);
        }

        public String getEMail()
        {
            return (this.eMail);
        }

        public String toString()
        {
            String string = new String( this.getClass().getName() + "[" );

            string += "action=" + this.action + ",";
            string += "eMail=" + this.eMail + "]";

            return (string);
        }

        public String toXmlString( String prefix )
        {

            String xml = new String();
            final String CRLF = System.getProperty( "line.separator" );
            final String thisClassName = "Collector.ResponseMethod";

            String actionName = null;
            switch (this.action)
            {
                case ExternalRequest.ACTION_Immediate:
                case ExternalRequest.ACTION_ByWeb:
                case ExternalRequest.ACTION_ByEMail:
                    actionName = ExternalRequest.RESPONSE_ActionList[this.action];
                    break;
            }

            xml += prefix + "<" + thisClassName + ">" + CRLF;

            xml += prefix + "  <Action code=\"" + this.action + '"';
            if (null == actionName)
            {
                xml += "/>";
            }
            else
            {
                xml += '>' + actionName + "</Action>" + CRLF;
            }

            xml += prefix + "  <EMail";
            if (null == this.eMail)
            {
                xml += "/>";
            }
            else
            {
                xml += ">" + this.eMail + "</EMail>";
            }

            xml += prefix + "</" + thisClassName + ">" + CRLF;

            return (xml);
        }

        //
        //-- Mutators ----------------------------------------------------

        public void setAction( int action )
        {
            this.action = action;
        }

        public void setEMail( String eMail )
        {
            this.eMail = eMail;
        }
    }
    //== End HttpResponseMethod ==============================================

}
