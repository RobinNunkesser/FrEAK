/*
 *
 *
 * Copyright (C) 2003-2004 David Benson
 *
 * JGpd is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGpd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGpd; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgpd.UI.utils;

import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;

import org.jgpd.UI.utils.wizard.File_PackageWizard;
import org.jgpd.io.utils.FilePackage;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileWizardEditor extends AbstractCellEditor
implements TableCellEditor,
ActionListener {
	FilePackage currentFile = null;
	JButton button;
	File_PackageWizard dialog;
	Frame frame;
	protected static final String EDIT = "edit";

	public FileWizardEditor(Frame f)
	{
		frame = f;
		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);
	}

	/**
	 * Handles events from the editor button and from
	 * the dialog's OK button.
	 */
	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			//The user has clicked the cell, so
			//bring up the dialog.
//			button.setBackground(currentColor);
//			colorChooser.setColor(currentColor);
			//Set up the dialog that the button brings up.
			dialog = new File_PackageWizard(frame, // owner frame
											new String("File Selection Wizard"),
											600,  // width
											350,  // height
											5,   // number of wizard screens
											this);
			dialog.setModal(true);
			dialog.setVisible(true);

			//Make the renderer reappear.
			fireEditingStopped();

		}
	}

	//Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue()
	{
		if (currentFile == null)
		{
			return new String("Not defined");
		}
		else
		{
			return currentFile;
		}
	}

	//Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column) {
//		currentColor = (Color)value;
		return button;
	}

	public void setCurrentFile(FilePackage file)
	{
		currentFile = file;
	}
}