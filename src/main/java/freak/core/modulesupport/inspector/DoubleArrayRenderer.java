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
import javax.swing.table.*;

/**
 * A <code>TableCellRenderer</code> for arrays of <code>Double</code>.
 * 
 * @author Heiko
 */
public class DoubleArrayRenderer extends DefaultTableCellRenderer {

	/**
	 * Creates a new <code>DoubleArrayRenderer</code> object.
	 */
	public DoubleArrayRenderer() {
		super();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Double[] d = (Double[])table.getModel().getValueAt(row, column);
		setText(doubleArrayToString(d));
		return this;
	}

	public static String doubleArrayToString(Double[] d) {
		String s = "(";
		for (int i = 0; i < d.length; i++) {
			s = s + d[i];
			if (i < d.length - 1) {
				s = s + ", ";
			}
		}
		s = s + ")";
		return s;
	}

}
