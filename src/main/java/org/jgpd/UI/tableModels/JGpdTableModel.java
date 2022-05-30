/*
 *
 * Copyright (C) 2004 David Benson
 *
 * JGpd is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGpd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGpd; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgpd.UI.tableModels;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * @author bensond
 *
 * JGpdTableModel creates a flexible table model allowing the model
 * details to be altered dynamically avoiding the need to create a
 * new model repeatly for a table
 */
public class JGpdTableModel extends DefaultTableModel
{
	/**
	 * <code>numStaticRows</code> holds the number of fixed rows in the
	 * table with may not be added to or deleted.  The table will initially
	 * show these rows at a minimum.
	 */
	protected int numStaticRows = 0;
	/**
	 * <code>columnEditable</code> is an array of booleans indicate whether
	 * each column can be edited or not
	 */
	protected boolean[] columnEditable;
	/**
	 * <code>addDeletingAllowed</code> indicate whether or not the dynamic rows
	 * ( those rows other than the complusory static ones ) can be added to or
	 * deleted
	 */
	protected boolean addDeletingAllowed = false;
	/**
	 * <code>maxNumDynamicRows</code> sets the limit of the number of dynamic
	 * rows allowed in the table
	 */
	protected int maxNumDynamicRows = 0;
	
	/**
	 * <code>defaultCellEntries[]</code> is an array of the text entries
	 * in each column when a row is added 
	 */
	protected Object[] defaultCellEntries;
	
	/**
	 * 
	 */
	public JGpdTableModel()
	{
		super();
	}
	/**
	 * @param columnNames
	 * @param rowCount
	 */
	public JGpdTableModel(Object[] columnNames, int rowCount)
	{
		super(columnNames, rowCount);
	}
	
	/**
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col) {
		//Note that the data/cell address is constant,
		//no matter where the cell appears onscreen.
		if (getColumnEditable(col))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * @see javax.swing.table.DefaultTableModel#addRow(java.lang.Object[])
	 */
	public void addRow(Object[] rowData)
	{
		// Check if this table model can add rows
		if ( isAddDeletingAllowed() )
		{
			super.addRow(getDefaultCellEntries());
		}
	}
	
	/**
	 * @see javax.swing.table.DefaultTableModel#removeRow(int)
	 */
	public void removeRow(int row)
	{
		// Check if this table model can delete rows
		if ( isAddDeletingAllowed() )
		{
			// Check it isn't one of the static rows we are trying to delete
			if ( row < getNumStaticRows() )
			{
				// Do nothing, cannot remove a static row
			}
			else
			{
				// Allow the remove
				super.removeRow(row);
			}
		}
	}
	/**
	 * @return Returns whether the dynamic rows in this table may be added
	 * to or removed
	 */
	public boolean isAddDeletingAllowed() {
		return addDeletingAllowed;
	}

	/**
	 * @param addDeletingAllowed The new state of whether the dynamic rows in
	 * this table may be added to or removed
	 */
	public void setAddDeletingAllowed(boolean addDeletingAllowed) {
		this.addDeletingAllowed = addDeletingAllowed;
	}

	/**
	 * @param index The index of the column being queried
	 * 
	 * @return Returns whether the specified column can be edited
	 */
	public boolean getColumnEditable(int index) {
		return columnEditable[index];
	}

	/**
	 * @param columnEditable The array of which columns can be edited
	 */
	public void setColumnEditable(boolean[] columnEditable) {
		this.columnEditable = columnEditable;
	}

	/**
	 * @return Returns the maximum allowed number of dynamic rows
	 */
	public int getMaxNumDynamicRows() {
		return maxNumDynamicRows;
	}

	/**
	 * @param maxNumDynamicRows The new maximum allowed number of dynamic rows
	 */
	public void setMaxNumDynamicRows(int maxNumDynamicRows) {
		this.maxNumDynamicRows = maxNumDynamicRows;
	}

	/**
	 * @return Returns the number of fixed rows in the table
	 */
	public int getNumStaticRows() {
		return numStaticRows;
	}

	/**
	 * @param numStaticRows The number of fixed rows in the table to set.
	 */
	public void setNumStaticRows(int numStaticRows) {
		this.numStaticRows = numStaticRows;
		
		// Check these are the only rows present
		if (this.getRowCount() > numStaticRows)
		{
			this.setRowCount(numStaticRows);
		}
	}

	/**
	 * @return
	 */
	public Object[] getDefaultCellEntries() {
		return defaultCellEntries;
	}

	/**
	 * @param defaultCellEntries
	 */
	public void setDefaultCellEntries(Object[] defaultCellEntries) {
		this.defaultCellEntries = defaultCellEntries;
	}

	public void configureTable(JTable table)
	{}
}