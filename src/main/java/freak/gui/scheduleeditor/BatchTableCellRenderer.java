/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui.scheduleeditor;

import freak.core.control.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * This class renders the cells of the BatchTable.
 * @author  Oliver
 */
public class BatchTableCellRenderer extends DefaultTableCellRenderer {
	private ScheduleInterface schedule;
	private GenerationIndex time;
	private static final Color COLOR_FINISHED = new Color(255, 200, 200);
	private static final Color COLOR_RUNNING = new Color(255, 255, 200);
	private static final Color COLOR_COMINGUP = new Color(200, 255, 200);

	public BatchTableCellRenderer(ScheduleInterface schedule) {
		this.schedule = schedule;
		time = schedule.getCurrentTimeIndex();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (isSelected) {
			setFont(new Font("Dialog", Font.BOLD, 12));
		} else {
			setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		setBorder(noFocusBorder);

		Batch currentBatch = schedule.getBatchList().get(row);

		if (currentBatch.isFinished())
			setBackground(COLOR_FINISHED);
		else if (currentBatch.isStarted())
			setBackground(COLOR_RUNNING);
		else
			setBackground(COLOR_COMINGUP);

		if (column == 4) {
			setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return this;
	}
}
