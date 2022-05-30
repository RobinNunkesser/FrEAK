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
 *  Listens for data off a siocket and appends it to the monitor's panel.
 *
 * @author  Andy Pippin
 */

package edu.ucsb.cs.jicos.utilities.monitor;

import  java.net.Socket;
import  java.io.BufferedReader;
import  java.io.InputStreamReader;
import  java.io.IOException;


public final class NetworkListener extends Thread {
    private static final String CRLF = Monitor.CRLF;

    private Socket socket;

    private Monitor monitor;

    NetworkListener(Socket socket, Monitor monitor) {
        this.socket = socket;
        this.monitor = monitor;
        setPriority(NORM_PRIORITY - 1);
        start();
    }

    public void run() {
        BufferedReader bufferedReader = null;

        try {
            // Get an input stream from the socket
            //
            bufferedReader = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));

            // Get the log message
            //
            String reply;
            while (null != (reply = bufferedReader.readLine())) {
                monitor.addInfo(reply + CRLF);
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}