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

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * A FreakModel specifically optimized for small, fast changing tables. If the table is cleared on every generation, this is probably the right choice.
 * @author  Stefan
 */
public class FreakSmallTableModel implements FreakTableModel {
	private transient ReadCopyModel readCopy;
	private transient JTable table;

	private String[] columnNames;
	private List data = new LinkedList();
	private Object defaultValue = "";

	private boolean structureChanged = true;

	public FreakSmallTableModel(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public void setView(JTable view) {
		table = view;
		readCopy = new ReadCopyModel();

		flush();
		table.setModel(readCopy);
	}

	public void addRow(Object[] rowData) {
		data.add(rowData);
	}

	public void removeFirstRow() {
		data.remove(0);
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

		// mark the change in table structure for next flush
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
		if (table.getTableHeader().getDraggedColumn() != null) {
			UpdateManager.markDirty(this);
			return;
		}

		// convert the data to plain arrays and write to the readCopy
		readCopy.data = (Object[][])data.toArray(new Object[data.size()][]);
		readCopy.columnNames = columnNames;
		readCopy.defaultValue = defaultValue;

		// fire the change event
		if (structureChanged) {
			readCopy.fireTableStructureChanged();
			structureChanged = false;
		} else {
			readCopy.fireTableDataChanged();
		}
	}

	private static class ReadCopyModel extends AbstractTableModel {
		// simply store all data in arrays
		Object[][] data;
		String[] columnNames;
		Object defaultValue;

		public int getRowCount() {
			return data.length;
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
			// get the row
			Object[] rowData = data[rowIndex];

			if (columnIndex < rowData.length) {
				// if the row is long enough, return the stored data
				return rowData[columnIndex];
			} else {
				// else return the table default value
				return defaultValue;
			}
		}
	}
}
