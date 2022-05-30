/*
 * @(#)FileOpen.java 1.0 04.08.2003
 *
 * Copyright (C) 2003 sven_luzar
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

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jgraph.GPGraphpad;
import org.jgraph.graph.GraphModel;
import org.jgraph.pad.GPDocument;
import org.jgraph.pad.GPFileChooser;
import org.jgraph.pad.GPGraph;
import org.jgraph.pad.GraphModelFileFormat;
import org.jgraph.pad.GraphModelProvider;
import org.jgraph.pad.GraphModelProviderRegistry;
import org.jgraph.pad.resources.Translator;

/**
 * @author sven.luzar
 *
 */
public class FileOpen extends AbstractActionFile {

	/**
	 * Constructor for FileOpen.
	 * @param graphpad
	 * @param name
	 */
	public FileOpen(GPGraphpad graphpad) {
		super(graphpad);
	}

	/** Shows a file chooser with the
	 *  file filters from the file formats
	 *  to select a file.
	 *
	 *  Furthermore the method uses the selected
	 *  file format for the read process.
	 *
	 *  @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 *  @see GraphModelProviderRegistry
	 */
	public void actionPerformed(ActionEvent e) {
		// show the file chooser
		GPFileChooser chooser = new GPFileChooser(null);
		chooser.setDialogTitle(Translator.getString("openLabel"));

		// return if cancel
		int result = chooser.showOpenDialog(graphpad);
		if (result == JFileChooser.CANCEL_OPTION)
			return;

		// check if already open
		URL name;
		try {
			name = chooser.getSelectedFile().toURL();
		} catch (MalformedURLException eurl) {
			return;
		}
		if (name != null) {
			// get all open files to test against
			GPDocument[] docs = graphpad.getAllDocuments();

			if (docs != null) {
				for (int i = 0; i < docs.length; i++) {
					URL docname = docs[i].getFilename();
					// check if names are the same
					if (docname != null && name.equals(docname)) {
						int r =
							JOptionPane.showConfirmDialog(
								graphpad,
								Translator.getString("FileAlreadyOpenWarning"),
								Translator.getString("Title"),
								JOptionPane.YES_NO_OPTION);
						// if YES then user wants to revert to previously saved version
						if (r == JOptionPane.YES_OPTION) {
							// close existing internal frame without saving
							graphpad.removeGPInternalFrame(
								docs[i].getInternalFrame());

							// open old version to revert to
							addDocument(chooser);
							return;
						}
						// doesn't want to revert and already open so cancel open
						else
							return;
					}
				}
			}
		} else
			return;

		// test confirms file not already open so open
		addDocument(chooser);
	}

	protected void addDocument(GPFileChooser chooser) {
		// get the file format, provider
		final File file = chooser.getSelectedFile();

		final GraphModelProvider graphModelProvider =
			GraphModelProviderRegistry.getGraphModelProvider(file);
		final GraphModelFileFormat graphModelFileFormat =
			GraphModelProviderRegistry.getGraphModelFileFormat(file);

		// error message if no file format found
		if (graphModelFileFormat == null) {
			JOptionPane.showMessageDialog(
				graphpad,
				Translator.getString("Error.No_GraphModelFileFormat_available"),
				Translator.getString("Error"),
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		// extract the read properties
		final Hashtable props =
			graphModelFileFormat.getReadProperties(chooser.getAccessory());

		Thread t = new Thread("Read File Thread") {

			public void run() {
					// create a new and clean graph
	GPGraph gpGraph =
		graphModelProvider.createCleanGraph(
			graphModelProvider.createCleanGraphModel());

				// try to read the graph model
				GraphModel model = null;

				URL fileURL;
				try {
					fileURL = file.toURL();

					try {
						model =
							graphModelFileFormat.read(fileURL, props, gpGraph);

					} catch (Exception ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(
							graphpad,
							ex.getLocalizedMessage(),
							Translator.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// posibility to cancel the load 
					// process
					if (model == null)
						return; 

					// FIX: A small hack to reset the filename
					// if it has no standard extension
					/*
					 * Sorry, we can't do this!
					 *  
					 * We have got a multi file format support
					 * and it can be that it is not the jgx
					 * file extension and thats absolutly 
					 * correct!
					 * 
					if (!fileURL.toString().toLowerCase().endsWith(".jgx")) {
					    graphpad.error(Translator.getString("OldFileFormat"));
					    fileURL = null;
					}
					*/

					// add the new document with the new graph and the new model
					graphpad.addDocument(
						fileURL,
						graphModelProvider,
						gpGraph,
						model,
						null);

					graphpad.update();
				} catch (MalformedURLException e) {
				}
			}
		};
		t.start();

	}

	/** Empty implementation.
	 *  This Action should be available
	 *  each time.
	 */
	public void update() {
	};

}
