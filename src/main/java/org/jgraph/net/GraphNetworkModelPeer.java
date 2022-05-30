/*
 * @(#)Graphpad.java	1.2 11/11/02
 *
 * Copyright (C) 2001 Gaudenz Alder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgraph.net;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.ParentMap;
import org.jgraph.pad.DefaultGraphModelFileFormatXML;
import org.jgraph.utils.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class GraphNetworkModelPeer extends Thread {

	protected GraphNetworkModel networkModel;

	protected ServerSocket serverSocket;

	public GraphNetworkModelPeer(int port, GraphNetworkModel networkModel) {
		this.networkModel = networkModel;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {

		}
		this.start();
	}

	public void run() {
		// wait for client requests and generate client connections
		try {
			while (true) {
				System.out.println("Listening...");
				Socket client_socket = serverSocket.accept();
				System.out.println("New connection...");
				Connection c =
					new Connection(
						client_socket.getInetAddress().toString(),
						client_socket,
						networkModel);
				networkModel.addNetworkModelListener(c);
			}
		} catch (IOException e) {
			System.out.println("Error: "+e.getMessage());
		} catch (NullPointerException ex) {
			System.out.println("NPE while listening: Missing permissions?");	
		}
	}

	static class Connection extends Thread implements GraphNetworkModelListener {
		// each client has a separate connection thread

	
		final static protected DefaultGraphModelFileFormatXML codec = new DefaultGraphModelFileFormatXML();

		protected String name;
		protected Socket client;
		protected BufferedReader client_reader;
		protected PrintWriter client_ostream;
		protected GraphNetworkModel networkModel;

		public Connection(
			String name,
			Socket client_socket,
			GraphNetworkModel netModel) {
			// opens data streams to and from client
			this.name = name;
			networkModel = netModel;

			System.out.println("Entering Connection constructor");
			client = client_socket;
			try {
				client_reader =
					new BufferedReader(
						new InputStreamReader(client.getInputStream()));
				client_ostream = new PrintWriter(client.getOutputStream());
			} catch (IOException e) {
				try {
					client.close();
				} catch (IOException e2) {
					System.err.println(
						"Exception while getting socket streams:" + e);
				}
				return;
			}
			this.start();
			System.out.println("Leaving Connection constructor");
		}

		public String toString() {
			return name;
		}

		public void run() {
			try {
				while (true) {
					System.out.println("waiting for data");
					String change = client_reader.readLine();
					System.out.println("received=" + change);
					if (change != null) {
						GraphModelChange gmc = null;
						try {
							gmc = decode(change);
							if (gmc != null)
								networkModel.networkModelChanged(
									this,
									gmc.getRemoved(),
									gmc.getInserted(),
									gmc.getPreviousAttributes(),
									gmc.getPreviousConnectionSet(),
									gmc.getPreviousParentMap());
						} catch (Exception e1) {
							System.out.println("Error: "+e1.getMessage());
						}
					} else
						break;
				}
				// decode and update netmodel
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					client.close();
					// Note: This was not necessarily added!
					networkModel.removeNetworkModelListener(this);
				} catch (IOException e2) {
					// ignore
				}
				stop();
			}
		}

		/* (non-Javadoc)
		 * @see org.jgraph.jgo.net.JGONetworkModelListener#networkModelChanged(org.jgraph.jgo.net.JGONetworkModelListener, org.jgraph.event.GraphModelEvent.GraphModelChange)
		 */
		public void networkModelChanged(
			GraphNetworkModelListener sender,
			Object[] removed,
			Object[] inserted,
			Map attributes,
			ConnectionSet cs,
			ParentMap pm) {
			String message = encode(removed, inserted, attributes, cs, pm);
			if (message != null) {
				client_ostream.println(message);
				client_ostream.flush();
				System.out.println("sent " + message);
			} else
				System.out.println("Tried to send null message");
		}

		//
		// Encoder Hooks
		//

		public String encode(
			Object[] removed,
			Object[] inserted,
			Map attributes,
			ConnectionSet cs,
			ParentMap pm) {
			String xml = new String("<change>");
			if (removed != null && removed.length > 0) {
				xml += "<remove>";
				for (int i = 0; i < removed.length; i++)
					xml += networkModel.getIdMap().getID(removed[i]) + ",";
				xml = xml.substring(0, xml.length() - 1);
				xml += "</remove>";
			}
			if (inserted != null && inserted.length > 0) {
				xml += "<add>";
				for (int i = 0; i < inserted.length; i++) {
					String tid = networkModel.getIdMap().getID(inserted[i]);
					tid += "_" + GraphCellFactory.getType(inserted[i]);
					xml += tid + ",";
				}
				xml = xml.substring(0, xml.length() - 1);
				xml += "</add>";
			}
			if (attributes != null && attributes.size() > 0) {
				xml += "<attributes>";
				Iterator it = attributes.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					xml += "<map cell=\""
						+ networkModel.getIdMap().getID(entry.getKey())
						+ "\">";
					xml
						+= codec.encodeMap(
							"",
							(Map) entry.getValue(),
							false,
							null,
							false).replaceAll("\n","");
					xml += "</map>";
				}
				xml += "</attributes>";
			}
			if (cs != null && cs.size() > 0) {
				xml += "<connect>";
				Iterator it = cs.connections();
				while (it.hasNext()) {
					ConnectionSet.Connection conn =
						(ConnectionSet.Connection) it.next();
					String tag = "target";
					if (conn.isSource())
						tag = "source";
					xml += "<"
						+ tag
						+ ">"
						+ networkModel.getIdMap().getID(conn.getEdge());
					String id = networkModel.getIdMap().getID(conn.getPort());
					if (id == null)
						id = "-1";
					xml += "," + id + "</" + tag + ">";
				}
				xml += "</connect>";
			}
			if (pm != null && pm.size() > 0) {
				xml += "<group>";
				Iterator it = pm.entries();
				while (it.hasNext()) {
					ParentMap.Entry entry = (ParentMap.Entry) it.next();
					xml += "<parent>"
						+ networkModel.getIdMap().getID(entry.getChild());
					String id =
						networkModel.getIdMap().getID(entry.getParent());
					if (id == null)
						id = "-1";
					xml += "," + id + "</parent>";
				}
				xml += "</group>";
			}
			return xml + "</change>";
		}

		public GraphModelChange decode(String string) throws Exception {
			// Create a DocumentBuilderFactory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// Create a DocumentBuilder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// Parse the input file to get a Document object
			Document doc =
				db.parse(new ByteArrayInputStream(string.getBytes()));
			// Get the first child (the graph-element)
			Node addNode = null;
			Node removeNode = null;
			Node attrsNode = null;
			Node csNode = null;
			Node pmNode = null;

			for (int i = 0;
				i < doc.getDocumentElement().getChildNodes().getLength();
				i++) {
				Node node = doc.getDocumentElement().getChildNodes().item(i);
				if (node.getNodeName().toLowerCase().equals("remove")) {
					removeNode = node;
				} else if (node.getNodeName().toLowerCase().equals("add")) {
					addNode = node;
				} else if (
					node.getNodeName().toLowerCase().equals("attributes")) {
					attrsNode = node;
				} else if (
					node.getNodeName().toLowerCase().equals("connect")) {
					csNode = node;
				} else if (node.getNodeName().toLowerCase().equals("group")) {
					pmNode = node;
				}
			}

			Object[] removed = null;
			if (removeNode != null)
				removed =
					networkModel.getIdMap().getObjects(
						Utilities.tokenize(
							removeNode.getChildNodes().item(0).getNodeValue(),
							","));

			List tmp = new LinkedList();
			Object[] inserted = null;

			if (addNode != null) {
				String[] tid =
					Utilities.tokenize(
						addNode.getChildNodes().item(0).getNodeValue(),
						",");
				for (int i = 0; i < tid.length; i++) {
					int pos = tid[i].indexOf("_");
					String id = tid[i].substring(0, pos);
					String type = tid[i].substring(pos + 1, tid[i].length());
					Object cell = networkModel.getIdMap().getObject(id, type);
					if (cell != null)
						tmp.add(cell);
				}
				inserted = tmp.toArray();
			}

			Map nested = null;
			if (attrsNode != null) {
				nested = new Hashtable();
				for (int i = 0;
					i < attrsNode.getChildNodes().getLength();
					i++) {
					Node child = attrsNode.getChildNodes().item(i);
					if (child.getNodeName().toLowerCase().equals("map")) {
						Node cellID =
							child.getAttributes().getNamedItem("cell");
						if (cellID != null) {
							Object cell =
								networkModel.getIdMap().getObject(
									cellID.getNodeValue(),
									null);
							if (cell != null) {
								Map attr = codec.decodeMap(child, true, true);
								nested.put(cell, attr);
							}
						}
					}
				}
				System.out.println("Attrs=" + nested);
			}

			ConnectionSet cs = null;
			if (csNode != null) {
				cs = new ConnectionSet();
				for (int i = 0; i < csNode.getChildNodes().getLength(); i++) {
					Node child = csNode.getChildNodes().item(i);
					Object nodeName = child.getNodeName().toLowerCase();
					if (nodeName.equals("source")
						|| nodeName.equals("target")) {
						boolean isSource = nodeName.equals("source");
						String[] id =
							Utilities.tokenize(
								child.getChildNodes().item(0).getNodeValue(),
								",");
						if (id.length == 2) {
							Object edge =
								networkModel.getIdMap().getObject(id[0], null);
							Object port =
								networkModel.getIdMap().getObject(id[1], null);
							if (edge != null)
								cs.connect(edge, port, isSource);
						}
					}
				}
			}

			ParentMap pm = null;
			if (pmNode != null) {
				pm = new ParentMap();
				for (int i = 0; i < pmNode.getChildNodes().getLength(); i++) {
					Node child = pmNode.getChildNodes().item(i);
					if (child.getNodeName().toLowerCase().equals("parent")) {
						String[] id =
							Utilities.tokenize(
								child.getChildNodes().item(0).getNodeValue(),
								",");
						if (id.length == 2) {
							Object childCell =
								networkModel.getIdMap().getObject(id[0], null);
							Object parentCell =
								networkModel.getIdMap().getObject(id[1], null);

							if (childCell != null)
								pm.addEntry(childCell, parentCell);
						}
					}
				}
			}

			return new FakeGraphModelChange(removed, inserted, nested, cs, pm);
		}

	}

	public static class FakeGraphModelChange implements GraphModelChange {

		protected Object[] removed, inserted;
		protected Map nested;
		protected ConnectionSet cs;
		protected ParentMap pm;

		public FakeGraphModelChange(
			Object[] removed,
			Object[] inserted,
			Map nested,
			ConnectionSet cs,
			ParentMap pm) {
			this.removed = removed;
			this.inserted = inserted;
			this.nested = nested;
			this.cs = cs;
			this.pm = pm;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getInserted()
		 */
		public Object[] getInserted() {
			return inserted;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getRemoved()
		 */
		public Object[] getRemoved() {
			return removed;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousAttributes()
		 */
		public Map getPreviousAttributes() {
			return nested;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousConnectionSet()
		 */
		public ConnectionSet getPreviousConnectionSet() {
			return cs;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getPreviousParentMap()
		 */
		public ParentMap getPreviousParentMap() {
			return pm;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#putViews(org.jgraph.graph.GraphLayoutCache, org.jgraph.graph.CellView[])
		 */
		public void putViews(GraphLayoutCache view, CellView[] cellViews) {
			// ignore
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphModelChange#getViews(org.jgraph.graph.GraphLayoutCache)
		 */
		public CellView[] getViews(GraphLayoutCache view) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getSource()
		 */
		public Object getSource() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getChanged()
		 */
		public Object[] getChanged() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getAttributes()
		 */
		public Map getAttributes() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.jgraph.event.GraphModelEvent.GraphViewChange#getContext()
		 */
		public Object[] getContext() {
			return null;
		}

	}

	/**
	 * @return
	 */
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	/**
	 * @param socket
	 */
	public void setServerSocket(ServerSocket socket) {
		serverSocket = socket;
	}

}
