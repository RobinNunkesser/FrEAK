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
 * Creates a client to access the TestCollector.
 * 
 * Created on: July 9, 2004
 * 
 * @author Andy Pippin
 */

package edu.ucsb.cs.jicos.examples.external;

import java.util.Random;

import edu.ucsb.cs.jicos.services.external.services.CollectorDebug;
import edu.ucsb.cs.jicos.services.external.XmlDocument;

public final class CollectorDemoClient {
	//
	//~~~Constants~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

	// Values for the Fibonacci test.
	//
	private static final String FIB_Class = "edu.ucsb.cs.jicos.examples.xml.fibonacci.Fibonacci";

	private static final int FIB_Number = 8;

	// Values for Traveling SalesPERSON test.
	//
	private static final String TSP_Class = "edu.ucsb.cs.jicos.examples.xml.tsp.TSP";

	private static final int TSP_Nodes = 4;

	private static final int TSP_MaxCost = 50;

	// Defaults
	//
	private static final String DEFAULT_Test = "Fibonacci";

	private static final String DEFAULT_Number = String.valueOf(FIB_Number);

	private static final String DEFAULT_HSP = "localhost";

	// Useful values.
	/**  The end of text (0x03)character.  */
	private static final byte ETX = CollectorDebug.ETX;

	/**  The end of a line string.  */
	private static final String CRLF = "\r\n";

	public static void main(String[] cmdLine) {
		String testName = DEFAULT_Test;
		String number = DEFAULT_Number;
		String hspName = DEFAULT_HSP;

		String testLowerCase3 = null;
		int exitCode = 0;

		// Command line looks like:
		//                                   0:     1:     2:
		//    <java> <TestCollectorClient> [test [number [hsp]]]
		//
		if (0 < cmdLine.length) {
			testName = cmdLine[0];

			if (1 < cmdLine.length) {
				number = cmdLine[1];

				if (2 < cmdLine.length) {
					hspName = cmdLine[2];
				}
			}
		}

		testLowerCase3 = testName.toLowerCase();
		if (3 <= testLowerCase3.length())
			testLowerCase3 = testLowerCase3.substring(0, 3);

		String[] testArgs = new String[2];
		testArgs[0] = number;
		testArgs[1] = hspName;
		//
		if ("fib".equals(testLowerCase3))
			exitCode = testFibonacci(testArgs);
		else if ("tsp".equals(testLowerCase3))
			exitCode = testTSP(testArgs);
		else {
			System.out.flush();
			System.err
					.println("Usage: <java> <TestCollectorClient> [test [number [hsp]]]");
			System.err.println("    where test is \"Fib[onacci]\" or \"TSP\"");
			System.err.flush();

			exitCode = 90;
		}

		System.exit(exitCode);
	}

	//---------------------------------------------------------------------//

	//      testArgs[0] = number  testArg[1] = hspHost
	private static int testFibonacci(String[] testArg) {
		int result = 1;
		int n;

		try {
			n = Integer.parseInt(testArg[0]);
		} catch (NumberFormatException numberFormatException) {
			System.err.println("Unknown number: \"" + testArg[0] + "\"");

			return (91); // *** DOES NOT CONTINUE ***
		}

		String msg = "<?xml version=\"1.1\" encoding=\"UTF-8\" ?>" + CRLF
				+ "<ExternalRequest taskName=\"" + FIB_Class + "\" >" + CRLF
				+ "  <n xsi:type=\"xsd:Integer\">" + n + "</n>" + CRLF
				+ "</ExternalRequest>" + CRLF + "";

		try {
			String response = SendReceive(msg, testArg[1]);
			String FofN = "<no response>";

			if (null != response) {
				XmlDocument xmlResponse = new XmlDocument(response);
				if (null != (FofN = xmlResponse
						.getValue("/ExternalResponse/FofN"))) {
					System.out.println("Fibonacci( " + n + " ) = " + FofN);
					result = 0;
				} else {
					System.out.println("Malformed response:");
					System.out.println(xmlResponse.toXmlString());
					System.out.println();
					result = 2;
				}
			} else {
				System.out.println("No response.");
				System.out.println();
			}

		} catch (java.io.IOException ioException) {
			if ("Connection refused".equals(ioException.getMessage())) {
				System.out.flush();
				System.err.println();
				System.err
						.println("    ** Couldn't connect to the HSP.  Is it turned on?");
				System.err.println();
				System.err.flush();
			} else {
				System.out.println("No response.");
				System.out.println();
				ioException.printStackTrace(System.out);
			}

		} catch (Exception exception) {
			System.out.println("No response.");
			System.out.println();
			exception.printStackTrace(System.out);
		}

		return (result);
	}

	//---------------------------------------------------------------------//

	//      testArgs[0] = number  testArg[1] = hspHost
	private static int testTSP(String[] testArg) {
		int result = 1;
		String hspHost = testArg[1];
		int nodes = 4;
		int[][] distance;

		// java TestCollectorClient 0:collector 1:tsp 2:nodes

		// Nodes
		try {
			nodes = Integer.parseInt(testArg[0]);
		} catch (NumberFormatException numberFormatException) {
			System.err.println("Unknown value: \"" + testArg[2] + "\"");
		}

		distance = new int[nodes][nodes];

		// Create the XML.
		//
		String msg = "<?xml version=\"1.1\" encoding=\"UTF-8\" ?>" + CRLF
				+ "<ExternalRequest taskName=\"" + TSP_Class + "\" >" + CRLF
				+ "  <data nodes=\"" + nodes + "\">" + CRLF;

		java.util.Random rnGen = new Random((new java.util.Date()).getTime());

		for (int src = 0; src < nodes; ++src) {
			msg += "    <row id=\"" + src + "\">";
			for (int dst = 0; dst < nodes; ++dst) {
				if (0 != dst)
					msg += " ";
				distance[src][dst] = rnGen.nextInt(TSP_MaxCost) + 1;
				msg += String.valueOf(distance[src][dst]);
			}
			msg += "</row>" + CRLF;
		}
		msg += "  </data>" + CRLF + "</ExternalRequest>" + CRLF + "";

		// Get the answer.
		//
		try {
			String response = SendReceive(msg, testArg[1]);
			String FofN = "<no response>";

			if (null != response) {
				XmlDocument xmlResponse = new XmlDocument(response);
				if (null != xmlResponse) {
					result = 0;

					// Display the problem.
					//
					System.out.println("Problem:");
					final String spaces = "      ";
					for (int src = 0; src < nodes; ++src) {
						System.out.print("  |");
						for (int dst = 0; dst < nodes; ++dst) {
							String d = String.valueOf(distance[src][dst]);
							System.out.print(spaces.substring(d.length(), 3)
									+ d);
						}
						System.out.println(" |");
					}

					// Display the answer.
					//
					System.out.println();
					System.out.println("Solution:");
					String tourPath = xmlResponse
							.getValue("/ExternalResponse/tour");
					String[] tour = tourPath.split(" ");
					if (0 < tour.length) {
						System.out
								.println("    Cost = "
										+ xmlResponse
												.getValue("/ExternalResponse/tour/@cost"));
						System.out.print("    Tour = " + tour[0]);
						for (int node = 1; node < tour.length; ++node)
							System.out.print(" --> " + tour[node]);
						System.out.println();
					} else {
						System.out.println("Empty tour.  :(");
					}
					System.out.println();
				} else {
					System.out.println("Malformed response:");
					System.out.println(xmlResponse.toXmlString());
					System.out.println();
					result = 2;
				}
			} else {
				System.out.println("No response.");
				System.out.println();
			}

		} catch (java.io.IOException ioException) {
			if ("Connection refused".equals(ioException.getMessage())) {
				System.out.flush();
				System.err.println();
				System.err
						.println("    ** Couldn't connect to the HSP.  Is it turned on?");
				System.err.println();
				System.err.flush();
			} else {
				System.out.println("No response.");
				System.out.println();
				ioException.printStackTrace(System.out);
			}

		} catch (Exception exception) {
			System.out.println("No response.");
			System.out.println();
			exception.printStackTrace(System.out);
		}

		return (result);
	}

	//--------------------------------------------------------------------//
	/**
	 * Send a request to the test collector, and wait for a response.
	 * 
	 * @param request
	 *            The servers name.
	 * @param collector
	 *            The URL ([//]host[:port][/]) of the collector.
	 * @return The message was sent (<CODE>true</CODE>), or not (<CODE>
	 *         false</CODE>).
	 * @throws java.io.IOException -
	 *             if an I/O exception happens when creating the socket.
	 * @throws java.lang.SecurityException -
	 *             If a security manager exists and its <CODE>
	 *             checkConnect>/CODE> method doesn't allow the operation.
	 */
	public static String SendReceive(String request, String collectorId)
			throws java.io.IOException, java.lang.SecurityException,
			java.net.ConnectException {
		String response = null;
		Object[] collector = null;

		// Bail if the user was stupid.
		//
		if ((null == request) || (null == collectorId))
			return (response);

		// Determine the location of the collector.
		//
		if (null != (collector = FindCollector(collectorId))) {
			java.net.Socket socket = null;
			java.io.InputStream inputStream = null;
			java.io.OutputStream outputStream = null;

			String host = (String) collector[0];
			int port = ((Integer) collector[1]).intValue();

			try {
				// Connect to the collector.
				//
				socket = new java.net.Socket(host, port);
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();

				// Get the bytes to send and transmit.
				//
				byte[] requestBuffer = request.getBytes();
				outputStream.write(requestBuffer);
				outputStream.write(ETX);

				// Keep reading until we get an end of text character.
				//
				byte[] recvBuffer = new byte[1024];
				response = new String();
				int totalBytesRcvd = 0;
				int bytesRecv = 0;
				boolean gotEOT = false;

				while (!gotEOT) {
					bytesRecv = inputStream.read(recvBuffer, 0,
							recvBuffer.length);

					// Abort on error.
					if (-1 == bytesRecv)
						break;

					if (ETX == recvBuffer[bytesRecv - 1]) {
						response += new String(recvBuffer, 0, bytesRecv - 1);
						gotEOT = true;
					} else {
						response += new String(recvBuffer, 0, bytesRecv);
					}
				} // end while.

			} catch (java.net.ConnectException connectException) {
				throw new java.io.IOException(connectException.getMessage());
			} catch (java.net.UnknownHostException unknownHostException) {
				throw new java.io.IOException(unknownHostException.getMessage());
			}

			finally {
				try {
					socket.close();
				} catch (Exception ignore) {
				}
			}
		}

		// Return what we received.
		//
		return (response);
	}

	//------------------------------------------------------------------------//
	/**
	 * Parses the name of the collector to determine the host name and port of
	 * the collector.
	 * 
	 * @returns Object array; [0] is the (String)hostname, [1] is the
	 *          (Integer)port number.
	 */
	private static Object[] FindCollector(String collectorName) {
		Object[] collector = null;

		// Get a connection to the server.
		//
		String hostName = new String(collectorName);
		String portName = null;
		int ndx, ndx2;

		if (hostName.startsWith("//"))
			hostName = hostName.substring(2);

		if (-1 != (ndx = hostName.indexOf(':'))) {
			portName = hostName.substring(ndx + 1);
			if (-1 != (ndx2 = portName.indexOf('/')))
				portName = portName.substring(0, ndx2);

			hostName = hostName.substring(0, ndx);
		} else if (-1 != (ndx = hostName.indexOf('/'))) {
			hostName = hostName.substring(0, ndx);
		}

		int port = CollectorDebug.PORT;

		if (null != portName) {
			try {
				port = Integer.parseInt(portName);
			} catch (NumberFormatException numberFormatException) {
			}
		}

		collector = new Object[2];
		collector[0] = hostName;
		collector[1] = new Integer(port);

		return (collector);
	}

}