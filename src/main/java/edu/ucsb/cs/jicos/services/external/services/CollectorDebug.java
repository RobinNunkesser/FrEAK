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
 * Implements a listener on an unused port (12345) and waits for network
 * connections that send XML requests.
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
import edu.ucsb.cs.jicos.services.external.ExternalRequest;
import edu.ucsb.cs.jicos.services.external.ExternalResult;
import edu.ucsb.cs.jicos.services.external.XmlConverter;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

import java.net.Socket;

public class CollectorDebug extends Collector
{
    //-- Constants -----------------------------------------------------------

    /** Port this Collector is listening on. */
    public static final int PORT = 12345;

    /** The end of message character. */
    public static final byte ETX = (byte) '\u0003';

    //-- Constructors --------------------------------------------------------

    public CollectorDebug()
    {
        super();
        this.initialize();

        return;
    }

    public CollectorDebug(ClientToHsp hsp)
    {
        super( hsp );
        this.initialize();

        return;
    }

    private void initialize()
    {
        setPort( PORT );
    }

    //
    //-- Thread entry point --------------------------------------------------

    /**
     * Start up this thread.
     */
    public void run()
    {
        java.net.ServerSocket serverSocket = null;
        java.util.logging.Logger logger = LogManager.getLogger();

        // Try and get the local host name.
        String hostName = "localhost";
        try
        {
            hostName = java.net.InetAddress.getLocalHost()
                    .getCanonicalHostName();
        }
        catch (java.net.UnknownHostException ignoreUnknownHostException)
        {
        }
        String hostPort = hostName + ":" + String.valueOf( PORT );

        try
        {
            serverSocket = new java.net.ServerSocket( PORT );
        }
        catch (java.net.BindException bindException)
        {
            logger.log( LogManager.SEVERE,
                    "There is already a socket bound to " + hostPort,
                    bindException );

            //           ***********************
            return; //  *** DOES NOT CONTINUE *** //
            //           ***********************
        }
        catch (java.io.IOException ioException)
        {
            logger.log( LogManager.SEVERE,
                    "Couldn't create TestCollector running on " + hostPort,
                    ioException );

            //           ***********************
            return; //  *** DOES NOT CONTINUE *** //
            //           ***********************
        }

        // Announce creation.
        logger.info( "Started TestCollector on " + hostPort );

        // Loop forever
        //
        while (true)
        {
            try
            {
                // Allow a client to connect.
                //
                Socket clientSocket = serverSocket.accept();

                String clientName = clientSocket.getInetAddress().getHostName()
                        + "  ("
                        + clientSocket.getInetAddress().getHostAddress() + ")";

                logger.info( "Got a connection from " + clientName );

                RequestHandlingThread requestHandlingThread = new RequestHandlingThread(
                        clientSocket, super.hsp );
                requestHandlingThread.start();

            }
            catch (Exception exception)
            {
                logger.log( LogManager.WARNING,
                        "Error while client was connecting.", exception );
            }
        }
    }

    //
    //-- Inner Classes -------------------------------------------------------

    public class RequestHandlingThread extends ExternalRequestProcessor
    {
        //
        //-- Variables -------------------------------------------------------

        private Socket socket;

        private ClientToHsp clientToHsp;

        /**
         * Construct a request Handling thread.
         * 
         * @throws RemoteException
         *             If couldn't connect to HSP.
         */
        public RequestHandlingThread(Socket socket, ClientToHsp clientToHsp)
                throws java.rmi.RemoteException
        {
            super( clientToHsp );
            this.socket = socket;
        }

        /**
         * Handle the request.
         */
        public void run()
        {

            boolean recvdRequest = false;
            String txtResponse = String.valueOf( ETX );

            try
            {
                // Read the request.

                java.io.InputStream inputStream = this.socket.getInputStream();

                // Create the input buffers.
                //
                int bytesRecv = 0;
                byte[] rqstBuffer = new byte[1024];
                String requestStr = new String();
                int totalBytesRcvd = 0;
                boolean gotEOT = false;

                // Read until an end of text.
                while (!gotEOT)
                {
                    bytesRecv = inputStream.read( rqstBuffer, 0,
                            rqstBuffer.length );

                    // Abort on error.
                    if (-1 == bytesRecv)
                        break;

                    if (ETX == rqstBuffer[bytesRecv - 1])
                    {
                        requestStr += new String( rqstBuffer, 0, bytesRecv - 1 );
                        gotEOT = true;
                    }
                    else
                    {
                        requestStr += new String( rqstBuffer, 0, bytesRecv );
                    }
                }

                recvdRequest = true;

                // Log the request.
                //
                LogManager.getLogger()
                        .finest( "Request:\n" + requestStr + '\n' );

                // Get Jicos's response via RequestTranslaterImpl
                ExternalData externalData = new XmlDocument( requestStr );
                ExternalRequest externalRequest = makeRequest( externalData );
                ExternalResult externalResult = waitForResult( externalRequest );

                Task task = externalResult.getTask();
                XmlConverter xmlConverter = (XmlConverter) task;
                XmlDocument xmlResponse = xmlConverter
                        .createResult( externalResult.getResult().getValue() );
                txtResponse = xmlResponse.toXmlString();

            }
            catch (Exception anyException)
            {
                txtResponse = (new XmlDocument( anyException )).toXmlString();
            }

            finally
            {
                if (recvdRequest)
                {

                    // Try and send a response back since the client is waiting
                    // for a response.
                    try
                    {
                        java.io.OutputStream outputStream = this.socket
                                .getOutputStream();

                        byte[] responseBuffer = txtResponse.getBytes();
                        outputStream.write( responseBuffer );
                        outputStream.write( ETX );
                    }
                    catch (Exception anyException)
                    {
                        LogManager.getLogger().log( LogManager.INFO,
                                "Error sending response",
                                anyException.getCause() );
                    }
                }

                if (null != this.socket)
                {
                    try
                    {
                        this.socket.close();
                    }
                    catch (java.io.IOException ignore)
                    {
                    }
                }
            }

            return;
        }

        private void sendString( Socket socket, XmlDocument xmlDocument )
        {
        }
    }
}

//== End of TestCollector.java =================================================
