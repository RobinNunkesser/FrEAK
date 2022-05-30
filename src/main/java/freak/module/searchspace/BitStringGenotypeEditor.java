/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.searchspace;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * A <code>TableCellEditor</code> for <code>BitStringGenotypes</code>.
 * @author  Heiko
 */
public class BitStringGenotypeEditor extends DefaultCellEditor {

	private JTextField textField;
	private int dimension;
	private BitStringGenotype old;

	public BitStringGenotypeEditor() {
		super(new JTextField());
		textField = new JTextField("");
		editorComponent = textField;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		BitStringGenotype bsg = (BitStringGenotype)value;
		dimension = bsg.size();
		old = bsg;
		textField.setText(bsg.toString());
		return textField;
	}

	public Object getCellEditorValue() {
		if (textField.getText().length() != dimension) {
			return old;
		}
		String text = textField.getText();
		BitSet input = new BitSet(dimension);
		for (int i = 0; i < text.length(); i++) {
			if ((text.charAt(i) != '0') && (text.charAt(i) != '1')) {
				return old;
			}
			if (text.charAt(i) == '1') {
				input.set(i);
			}

		}
		BitStringGenotype bsg = new BitStringGenotype(input, dimension);
		return bsg;
	}

}
