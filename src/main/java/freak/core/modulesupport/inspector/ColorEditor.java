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

import javax.swing.*;

/**
 * A <code>TableCellEditor</code> for color objects.
 * 
 * @author Dirk
 */
public class ColorEditor extends DefaultCellEditor {

	private Color color = Color.magenta;
	private JButton chooserButton;
	private JColorChooser chooser;
	private JDialog dialog;

	/**
	 * Constructs a new <code>ColorEditor</code>.
	 */
	public ColorEditor() {
		super(new JCheckBox());

		setClickCountToStart(1);

		chooser = new JColorChooser();
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				color = chooser.getColor();
			}
		};
		dialog = JColorChooser.createDialog(chooserButton, "Pick a color", true, chooser, okListener, null);

		chooserButton = new JButton();
		chooserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setColor(color);
				dialog.setVisible(true);
				fireEditingStopped();
			}
		});

		editorComponent = chooserButton;
	}

	public Object getCellEditorValue() {
		return color;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		color = (Color)value;
		return editorComponent;
	}

}
