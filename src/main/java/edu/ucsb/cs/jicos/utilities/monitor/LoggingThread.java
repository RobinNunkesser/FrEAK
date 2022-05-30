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
 *  Thread to monitor logging.
 *
 * @author  Andy Pippin
 */

package edu.ucsb.cs.jicos.utilities.monitor;

import java.net.ServerSocket;
import java.io.IOException;

public final class LoggingThread extends Thread {
    private int port;

    private Monitor monitor;

    public LoggingThread(Monitor monitor, Integer port) {
        this.monitor = monitor;
        if (null == port)
            this.port = Monitor.DEFAULT_LogPort;
        else
            this.port = port.intValue();
    }

    public void run() {
        if (null != monitor) {
            try {
                ServerSocket serverSocket = new ServerSocket(this.port);

                // Start a new thread for each request.
                //
                while (true) {
                    new NetworkListener(serverSocket.accept(), this.monitor);
                }
            } catch (IOException ioException) {
                System.out.flush();
                ioException.printStackTrace();
                System.err.flush();
            }
        }

    }
}


