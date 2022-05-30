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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import javax.swing.DefaultListModel;

import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;

public class GraphNetworkModel implements GraphNetworkModelListener {

	public static final String PROPERTY_DATASOURCE = new String("datasource");

	protected PropertyChangeSupport changeSupport;

	public GraphCellIdentityMap idMap = new GraphCellIdentityMap();

	protected GraphNetworkModelPeer server;

	protected GraphModel graphModel;
	protected GraphNetworkModelListener datasource;
	protected DefaultListModel clients;
	protected boolean isLocalEdit = true;

	public GraphNetworkModel(GraphModel model) {
		changeSupport = new PropertyChangeSupport(this);
		clients = new DefaultListModel();
		graphModel = model;
		graphModel.addGraphModelListener(new GraphModelListener() {
			public void graphChanged(GraphModelEvent e) {
				fireUpdate(e.getChange());
			}
		});
	}

	public void fireUpdate(GraphModelChange change) {
		if (isLocalEdit) {
			ParentMap pm = change.getPreviousParentMap();
			if (change.getInserted() != null
				&& change.getPreviousParentMap() == null)
					pm = ParentMap.create(graphModel, change.getInserted(), false, true);
			networkModelChanged(
				null,
				change.getRemoved(),
				change.getInserted(), //flat,
				change.getPreviousAttributes(),
				change.getPreviousConnectionSet(),
				pm);
		}
	}

	public void execute(
		Object[] removed,
		Object[] inserted,
		Map attributes,
		ConnectionSet cs,
		ParentMap pm) {
		try {
		isLocalEdit = false;
		// Execute
		if (removed != null && removed.length > 0)
			graphModel.remove(removed);
		else if (inserted != null)
			graphModel.insert(inserted, attributes, cs, pm, null);
		else
			graphModel.edit(attributes, cs, pm, null);
		} catch (Exception e) {
			System.out.println("Error: "+e.getMessage());	
		} finally {
			isLocalEdit = true;
		}
	}

	public void networkModelChanged(
		GraphNetworkModelListener sender,
		Object[] removed,
		Object[] inserted,
		Map attributes,
		ConnectionSet cs,
		ParentMap pm) {
		// Execute Locally
		if (sender != null) {
			execute(removed, inserted, attributes, cs, pm);
		}
		// Dispatch
		if (sender != datasource && datasource != null)
			datasource.networkModelChanged(
				this,
				removed,
				inserted,
				attributes,
				cs,
				pm);
		for (int i = 0; i < clients.size(); i++) {
			GraphNetworkModelListener clientModel =
				(GraphNetworkModelListener) clients.get(i);
			if (clientModel != sender)
				clientModel.networkModelChanged(
					this,
					removed,
					inserted,
					attributes,
					cs,
					pm);
		}
	}

	public void addNetworkModelListener(GraphNetworkModelListener listener) {
		clients.addElement(listener);
		// Add data
		Object[] roots = DefaultGraphModel.getRoots(graphModel);
		Object[] flat =
			DefaultGraphModel.getDescendants(graphModel, roots).toArray();
		ConnectionSet cs = ConnectionSet.create(graphModel, flat, false);
		ParentMap pm = ParentMap.create(graphModel, flat, false, false);
		Map attr = GraphConstants.createAttributesFromModel(flat, graphModel);
		listener.networkModelChanged(this, null, flat, attr, cs, pm);
	}

	public void removeNetworkModelListener(GraphNetworkModelListener listener) {
		clients.removeElement(listener);
	}

	public void setDatasource(String exp) {
		try {
			if (datasource instanceof GraphNetworkModelPeer.Connection) {
				((GraphNetworkModelPeer.Connection) datasource).stop();
			}
			int pos = exp.indexOf(":");
			String host = exp.substring(0, pos);
			String port = exp.substring(pos + 1, exp.length());
			Socket socket = new Socket(host, Integer.parseInt(port));
			GraphNetworkModelPeer.Connection conn =
				new GraphNetworkModelPeer.Connection(exp, socket, this);
			Object oldValue = datasource;
			datasource = conn;
			changeSupport.firePropertyChange(
				PROPERTY_DATASOURCE,
				oldValue,
				datasource);
		} catch (IOException e) {
		}
	}
	
	public boolean isConnected() {
		return datasource != null;
	}

	/**
	 * @return
	 */
	public GraphCellIdentityMap getIdMap() {
		return idMap;
	}

	/**
	 * @param map
	 */
	public void setIdMap(GraphCellIdentityMap map) {
		idMap = map;
	}

	/**
	 * @param arg0
	 */
	public synchronized void addPropertyChangeListener(PropertyChangeListener arg0) {
		changeSupport.addPropertyChangeListener(arg0);
	}

	/**
	 * @param arg0
	 */
	public synchronized void removePropertyChangeListener(PropertyChangeListener arg0) {
		changeSupport.removePropertyChangeListener(arg0);
	}

	/**
	 * @return
	 */
	public DefaultListModel getClients() {
		return clients;
	}

	/**
	 * @param model
	 */
	public void setClients(DefaultListModel model) {
		clients = model;
	}

	/**
	 * @return
	 */
	public GraphNetworkModelPeer getServer() {
		return server;
	}

	/**
	 * @param peer
	 */
	public void setServer(GraphNetworkModelPeer peer) {
		server = peer;
	}

	//
	// Main
	//

	public static void main(String[] args) {
		String port = null;
		String source = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-p") && args.length > i + 1) {
				port = args[i + 1];
			} else if (args[i].equals("-s") && args.length > i + 1) {
				source = args[i + 1];
			}
		}
		GraphModel graphModel = new DefaultGraphModel();
		if (port != null) {
			System.out.println("Running server on port " + port);
			GraphNetworkModel netModel = new GraphNetworkModel(graphModel);
			netModel.setServer(
				new GraphNetworkModelPeer(Integer.parseInt(port), netModel));
			if (source != null) {
				System.out.println("Connecting to " + source);
				netModel.setDatasource(source);
			}
		} else
			System.out.println("Usage: JGONetworkModel -p port -s host:port");
	}

}
