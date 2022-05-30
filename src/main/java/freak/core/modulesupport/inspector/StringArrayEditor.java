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
 * A <code>TableCellEditor</code> for arrays of <code>String</code>.
 *
 * @author Michael
 */
public class StringArrayEditor extends DefaultCellEditor {

	JComboBox cbox;

	public StringArrayEditor() {
		super(new JComboBox());
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		StringArrayWrapper sWrapper = (StringArrayWrapper)value;
		cbox = new JComboBox(sWrapper.getStringArray());
		cbox.setSelectedIndex(sWrapper.getIndex());

		// -- now make the combobox respond to user interaction
		cbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});

		return cbox;
	}

	public Object getCellEditorValue() {
		return new StringArrayWrapper(new String[] {(String)cbox.getSelectedItem()}, cbox.getSelectedIndex());
	}
}
