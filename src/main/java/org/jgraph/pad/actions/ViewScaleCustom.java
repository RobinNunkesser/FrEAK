/*
 * @(#)ViewScaleCustom.java	1.2 30.01.2003
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
import org.jgraph.pad.resources.Translator;

/**
 * Opens a dialog to set the graph scale.
 * 
 * @author sven.luzar
 * @version 1.0
 *
 */
public class ViewScaleCustom extends AbstractActionDefault {

	/**
	 * Constructor for ViewScaleCustom.
	 * @param graphpad
	 * @param name
	 */
	public ViewScaleCustom(GPGraphpad graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			double s =
				Double.parseDouble(
					JOptionPane.showInputDialog(
						Translator.getString("ScaleDialog")));
			getCurrentDocument().setResizeAction(null);
			getCurrentDocument().setScale(s / 100);
		} catch (NullPointerException npe) {
			// ignore
		} catch (Exception ex) {
			graphpad.error(ex.getMessage());
		}
	}

}
