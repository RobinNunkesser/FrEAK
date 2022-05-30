/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.modulesupport.inspector;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

/**
 * A <code>TableCellEditor</code> for file objects.
 * 
 * @author Dirk, Stefan
 */
public class FileEditor extends DefaultCellEditor {

	public static final int OPEN = 0;
	public static final int SAVE = 1;

	private File file;
	private JButton chooserButton;
	private int action;

	/**
	 * Constructs a new <code>FileEditor</code>.
	 */
	public FileEditor(final int action) {
		super(new JCheckBox());

		this.action = action;

		setClickCountToStart(1);
		file = new File("");

		chooserButton = new JButton();
		chooserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(file);
				int result = JFileChooser.CANCEL_OPTION;

				// show dialog depending on the specified action
				switch (action) {
					case OPEN :
						result = chooser.showOpenDialog(chooserButton);
						break;
					case SAVE :
						result = chooser.showSaveDialog(chooserButton);
						break;
				}

				// evaluate result depending on the specified action
				if (result == JFileChooser.APPROVE_OPTION) {

					switch (action) {

						case OPEN :
							{
								if (chooser.getSelectedFile().exists()) {
									file = chooser.getSelectedFile();
								} else {
									JOptionPane.showMessageDialog(null, "File " + chooser.getSelectedFile() + " can not be found.", "File not found", JOptionPane.ERROR_MESSAGE);
								}
								break;
							}

						case SAVE :
							{
								// ask if an existing file is to be replaced
								if (chooser.getSelectedFile().exists()) {
									Object[] options = { "OK", "Cancel" };
									int answer = JOptionPane.showOptionDialog(null, "Replace existing file " + chooser.getSelectedFile() + "?", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

									if (answer == JOptionPane.OK_OPTION) {
										file = chooser.getSelectedFile();
										file.delete();
									}
								} else {
									file = chooser.getSelectedFile();
								}
								break;
							}
					}
				}
				fireEditingStopped();
			}
		});

		editorComponent = chooserButton;
	}

	public Object getCellEditorValue() {
		return file;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		file = (File)value;
		return editorComponent;
	}

}
