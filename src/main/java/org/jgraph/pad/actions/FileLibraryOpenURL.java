/*
 * @(#)FileLibraryOpen.java	1.2 29.01.2003
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

import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;

import org.jgraph.GPGraphpad;
import org.jgraph.pad.resources.Translator;

/**
 * Action that opens a library from a file.
 *
 * @author sven.luzar
 */
public class FileLibraryOpenURL extends AbstractActionFile {

	/**
	 * Constructor for FileLibraryOpen.
	 * @param graphpad
	 * @param name
	 */
	public FileLibraryOpenURL(GPGraphpad graphpad) {
		super(graphpad);
	}

	public void actionPerformed(ActionEvent e) {
		String name =
			JOptionPane.showInputDialog(Translator.getString("URLDialog", new Object[]{"test.lib"}));
		if (name != null) {
			try {
				URL location = new URL(name);
				InputStream f = location.openStream();
				f = new GZIPInputStream(f);
				ObjectInputStream in = new ObjectInputStream(f);
				getCurrentDocument().getLibraryPanel().openLibrary(
					in.readObject());
				in.close();
				// Display Library
				getCurrentDocument().getSplitPane().resetToPreferredSizes();
			} catch (Exception ex) {
				graphpad.error(ex.toString());
			} finally {
				graphpad.repaint();
			}
		}
	}

}
