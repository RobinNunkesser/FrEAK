/*
 * @(#)FileNew.java	1.2 30.01.2003
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

import javax.swing.JOptionPane;

import org.jgraph.GPGraphpad;
import org.jgraph.net.GraphNetworkModelPeer;
import org.jgraph.pad.GPDocument;

/**
 * 
 * @author sven.luzar
 * @version 1.0
 *
 */
public class FileSharingShare extends AbstractActionCheckBox {

	/**
	 * Constructor for FileNew.
	 * @param graphpad
	 * @param name
	 */
	public FileSharingShare(GPGraphpad graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		GPDocument doc = getCurrentDocument();
		if (doc != null) {
			String port = JOptionPane.showInputDialog("Port Number");
			doc
				.getNetworkModel()
				.setServer(
					new GraphNetworkModelPeer(
						Integer.parseInt(port),
						doc.getNetworkModel()));
		}
	}

	public void update() {
		GPDocument doc = getCurrentDocument();
		setEnabled(doc != null);
	}

	public boolean isSelected(String actionCommand) {
		GPDocument doc = getCurrentDocument();
		if (doc == null)
			return false;
		return doc.getNetworkModel().getServer() != null;
	}

}
