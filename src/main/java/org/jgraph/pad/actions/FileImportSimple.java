/*
 * @(#)FileExportGXL.java	1.2 01.02.2003
 *
 * Copyright (C) 2003 sven.luzar
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
package org.jgraph.pad.actions;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.jgraph.GPGraphpad;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.pad.GPGraph;
import org.jgraph.pad.GPUserObject;
import org.jgraph.pad.resources.Translator;

/**
 *
 * @author sven.luzar
 * @version 1.0
 *
 */
public class FileImportSimple extends AbstractActionDefault {

	/**
	 * Constructor for FileExportGXL.
	 * @param graphpad
	 */
	public FileImportSimple(GPGraphpad graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		FileDialog f = new FileDialog(graphpad.getFrame(), Translator.getString("CustomFile"/*#Finished:Original="Custom File"*/), FileDialog.LOAD);
		f.show();
		if (f.getFile() == null)
			return;

		try {
			String file = f.getDirectory() + f.getFile();
			String delimeter =
				JOptionPane.showInputDialog(
					Translator.getString("DelimeterDialog"));
			String edgeLabel =
				JOptionPane.showInputDialog(
					Translator.getString("EdgeLabelDialog"));
			GPGraph graph = getCurrentGraph();
			parseSimpleFileInto(file, delimeter, graph, edgeLabel);
		} catch (Exception ex) {
			graphpad.error(ex.toString());
		}
	}


	//
	// Simple Format a:b
	//

	public static void parseSimpleFileInto(
		String file,
		String delim,
		GPGraph graph,
		String edgeLabel) {
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(file);

			// Convert our input stream to a
			// DataInputStream
			DataInputStream in = new DataInputStream(fstream);

			// Continue to read lines while
			// there are still some left to read
			// Map from keys to vertices
			Hashtable map = new Hashtable();
			// Link to Existing Vertices!
			Object[] items = graph.getVertices(graph.getAll());
			if (items != null) {
				for (int i = 0; i < items.length; i++)
					if (items[i] != null && items[i].toString() != null) {
						map.put(items[i].toString(), items[i]);
					}
			}
			// Vertices and Edges to insert
			Hashtable adj = new Hashtable();
			java.util.List insert = new ArrayList();
			ConnectionSet cs = new ConnectionSet();
			while (in.available() != 0) {
				// Print file line to screen
				String s = in.readLine();
				StringTokenizer st = new StringTokenizer(s, delim);
				if (st.hasMoreTokens()) {
					String srckey = st.nextToken().trim();
					// Get or create source vertex
					Object source = getVertexForKey(map, srckey);
					if (!graph.getModel().contains(source)
						&& !insert.contains(source))
						insert.add(source);
					if (st.hasMoreTokens()) {
						String tgtkey = st.nextToken().trim();
						// Get or create source vertex
						Object target = getVertexForKey(map, tgtkey);
						if (!graph.getModel().contains(target)
							&& !insert.contains(target))
							insert.add(target);
						// Create and insert Edge
						Set neighbours = (Set) adj.get(srckey);
						if (neighbours == null) {
							neighbours = new HashSet();
							adj.put(srckey, neighbours);
						}
						String label =
							(st.hasMoreTokens())
								? st.nextToken().trim()
								: edgeLabel;
						if (!(neighbours.contains(tgtkey))) {
							Object edge =
								new DefaultEdge(label);
							Object sourcePort =
								graph.getModel().getChild(source, 0);
							Object targetPort =
								graph.getModel().getChild(target, 0);
							if (sourcePort != null && targetPort != null) {
								cs.connect(edge, sourcePort, targetPort);
								insert.add(edge);
								neighbours.add(tgtkey);
							}
						}
					}
				}
			}
			in.close();
			graph.getModel().insert(insert.toArray(), null, cs, null, null);
		} catch (Exception e) {
			System.err.println("File input error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static Object getVertexForKey(Hashtable map, String key) {
		Object cell = map.get(key);
		if (cell == null) {
			DefaultGraphCell dgc =
				new DefaultGraphCell(new GPUserObject(key));
			dgc.add(new DefaultPort());
			cell = dgc;
			map.put(key, cell);
		}
		return cell;
	}
}

