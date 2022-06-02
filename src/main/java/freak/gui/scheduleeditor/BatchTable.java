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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * @author  Oliver
 */
public class BatchTable extends JTable {

	private ScheduleInterface schedule;
	private BatchList batchList;
	private TableCellRenderer renderer;
	private AbstractTableModel model;

	public BatchTable(ScheduleInterface schedule) {
		this.schedule = schedule;
		batchList = schedule.getBatchList();
		model = new BatchTableModel(schedule);
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		renderer = new BatchTableCellRenderer(schedule);
		// -- adjust widths of columns
		for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
			TableColumn col = getColumnModel().getColumn(i);
			if (i == 0 || i == 4)
				col.setPreferredWidth(40);
		}
	}

	public void updateRows(int from, int to) {
		model.fireTableRowsUpdated(from, to);
	}

	public void appendBatch(Batch batch, boolean applyConfigurationCode) {
		batchList.add(batch, applyConfigurationCode);
		model.fireTableRowsInserted(batchList.size() - 1, batchList.size() - 1);
	}

	public void deleteRow(int row) {
		batchList.removeBatch(row);
		model.fireTableRowsDeleted(row, row);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTable#getCellRenderer(int, int)
	 */
	public TableCellRenderer getCellRenderer(int row, int column) {
		return renderer;
	}

}
