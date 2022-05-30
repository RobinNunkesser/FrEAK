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

import javax.swing.*;

/**
 * A <code>TableCellEditor</code> for arrays of <code>Double</code>.
 * 
 * @author Heiko
 */
public class DoubleArrayEditor extends DefaultCellEditor {

	private JTextField textField;
	private Double[] old;

	public DoubleArrayEditor() {
		super(new JTextField());
		textField = new JTextField("");
		editorComponent = textField;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		old = (Double[])value;
		textField.setText(DoubleArrayRenderer.doubleArrayToString(old));
		return textField;
	}

	public Object getCellEditorValue() {
		if (old.length == 0) {
			return old;
		}
		Double[] newValue = new Double[old.length];
		String text = textField.getText();
		if ((text.charAt(0) != '(') || (text.charAt(text.length() - 1) != ')')) {
			return old;
		}
		text = text.substring(1, text.length() - 1);
		boolean last = false;
		int count = 0;
		while (!last) {
			if (text.indexOf(",") == -1) {
				last = true;
				try {
					newValue[count] = new Double(text);
					count++;
				} catch (NumberFormatException e) {
					return old;
				}
			} else {
				String s = text.substring(0, text.indexOf(","));
				text = text.substring(text.indexOf(",") + 1);
				try {
					newValue[count] = new Double(s);
					count++;
				} catch (NumberFormatException e) {
					return old;
				}
			}
		}
		if (count != old.length) {
			return old;
		}
		return newValue;
	}

}
