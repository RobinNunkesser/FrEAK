/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.view.swingsupport;

import javax.swing.*;
import javax.swing.table.*;

/**
 * A FreakModel specifically optimized for large tables, collecting huge amounts of data.
 * @author  Stefan
 */
public class FreakLargeTableModel implements FreakTableModel {
	private transient ReadCopyModel readCopy;
	private transient JTable table;

	private String[] columnNames;
	private SlidingWindow data = new SlidingWindow();
	private Object defaultValue = "";

	private boolean structureChanged = true;

	public FreakLargeTableModel(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public void setView(JTable view) {
		table = view;

		readCopy = new ReadCopyModel(columnNames, data, defaultValue);
		data.synchronizeViews();
		structureChanged = false;

		table.setModel(readCopy);
	}

	public void addRow(Object[] rowData) {
		data.add(rowData);
	}

	public void removeFirstRow() {
		data.removeFirst();
	}

	public Object[] getRow(int rowNumber) {
		return (Object[])data.get(rowNumber);
	}

	public void clear() {
		data.clear();
	}

	/**
	 * @return  the columnNames
	 * @uml.property  name="columnNames"
	 */
	public String[] getColumnNames() {
		return columnNames;
	}

	/**
	 * @param columnNames  the columnNames to set
	 * @uml.property  name="columnNames"
	 */
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
		structureChanged = true;
	}

	/**
	 * @return  the defaultValue
	 * @uml.property  name="defaultValue"
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue  the defaultValue to set
	 * @uml.property  name="defaultValue"
	 */
	public void setDefaultValue(Object object) {
		defaultValue = object;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.size();
	}

	public void flush() {
		if (readCopy == null)
			return;

		// bugfix: defer updates, if columns are dragged
		if (table.getTableHeader().getDraggedColumn() != null) {
			UpdateManager.markDirty(this);
			return;
		}

		// update readCopyModel
		readCopy.columnNames = columnNames;
		readCopy.defaultValue = defaultValue;
		data.synchronizeViews();

		// fire the change event
		if (structureChanged) {
			readCopy.fireTableStructureChanged();
			structureChanged = false;
		} else {
			readCopy.fireTableDataChanged();
		}
	}

	/**
	 * @author  nunkesser
	 */
	private static class ReadCopyModel extends AbstractTableModel {
		String[] columnNames;
		SlidingWindow data;
		Object defaultValue;

		public ReadCopyModel(String[] columnNames, SlidingWindow data, Object defaultValue) {
			this.columnNames = columnNames;
			this.data = data;
			this.defaultValue = defaultValue;
		}

		public int getRowCount() {
			return data.sizeInSwingView();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public String getColumnName(int column) {
			return columnNames[column];
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Object[] rowData = (Object[])data.getInSwingView(rowIndex);

			// return the requested value, or default, if the row is to short
			if (columnIndex < rowData.length) {
				return rowData[columnIndex];
			} else {
				return defaultValue;
			}
		}
	}
}
