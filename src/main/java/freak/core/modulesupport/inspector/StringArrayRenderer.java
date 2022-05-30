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
 * A <code>TableCellRenderer</code> for arrays of <code>String</code>.
 *
 * @author Michael
 */
public class StringArrayRenderer extends DefaultTableCellRenderer {
	
	/**
	 * Creates a new <code>StringArrayRenderer</code> object.
	 */
	public StringArrayRenderer() {
		super();
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		StringArrayWrapper sWrapper = (StringArrayWrapper)value;
/*
		JComboBox cbox = new JComboBox(sWrapper.getStringArray());
		cbox.setSelectedIndex(sWrapper.getIndex());
		return cbox;
 */
		return super.getTableCellRendererComponent(table, sWrapper.getStringArray()[sWrapper.getIndex()], isSelected, hasFocus, row, column);
	}
}
