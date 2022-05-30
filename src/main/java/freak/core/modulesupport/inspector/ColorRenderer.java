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
 * A <code>TableCellRenderer</code> for color objects.
 * 
 * @author Dirk
 */
public class ColorRenderer extends JLabel implements TableCellRenderer {

	/**
	 * Constructs a new <code>ColorRenderer</code>.
	 */
	public ColorRenderer() {
		super();
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setBackground((Color)value);
		if (isSelected) {
			setBorder(BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground()));
		} else {
			setBorder(BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground()));
		}
		return this;
	}

}
