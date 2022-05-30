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

/**
 * A FreakModel to use JTables. There are currently two implementations of this inteface. They provide identical features, but different performance. If in doubt, use FreakLargeTableModel. 
 * @author  Stefan
 */
public interface FreakTableModel extends FreakSwingModel {
	/**
	 * Link the values of the JTable view to this model. The table will be
	 * adapted once immidiately and after that on every call to flush.
	 */
	public abstract void setView(JTable view);

	/**
	 * Add a row to the bottom of the table. The array and its contents may not
	 * be changed after this call.
	 */
	public abstract void addRow(Object[] rowData);
	/**
	 * Remove one row from the top of the table.
	 */
	public abstract void removeFirstRow();
	/**
	 * Remove all rows, but keep the column names.
	 */
	public abstract void clear();
	public Object[] getRow(int rowNumber);
	public int getRowCount();

	/**
	 * Sets the column header. The visible table size is adapted to the length of the array, but rows are not actually cropped nor extended. The array may not be changed after this call.
	 * @uml.property  name="columnNames"
	 */
	public abstract void setColumnNames(String[] columnNames);
	/**
	 * @return
	 * @uml.property  name="columnNames"
	 */
	public String[] getColumnNames();
	public int getColumnCount();

	/**
	 * Sets the value, that will be shown, if rows do not contain data for all columns. The rows are not actually changed. The object may not be changed after this call.
	 * @uml.property  name="defaultValue"
	 */
	public abstract void setDefaultValue(Object defaultValue);
	/**
	 * @return
	 * @uml.property  name="defaultValue"
	 */
	public Object getDefaultValue();

	public abstract void flush();
}