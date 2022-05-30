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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

import freak.core.modulesupport.inspector.StandardInspectorFactory.*;

/**
 * A subclass of <code>JTable</code> that returns the default 
 * <code>TableCellRenderer</code> and <code>TableCellEditor</code> for 
 * each cell depending on the class of the cell's object. <br>
 * Moreover, the class prevents the user from accidentally losing data if 
 * editing a <code>JTextComponent</code> is not finished properly with the 
 * enter key.
 * 
 * @author Dirk, Heiko
 */
public class CustomizableInspectorTable extends JTable {

	public CustomizableInspectorTable(TableModel dm) {
		super(dm);
	}

	public CustomizableInspectorTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
	}

	public CustomizableInspectorTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
	}

	public CustomizableInspectorTable(int numRows, int numColumns) {
		super(numRows, numColumns);
	}

	public CustomizableInspectorTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
	}

	public CustomizableInspectorTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
	}

	/**
	 * Customizes the handling of object of the specified class by
	 * setting the specified <code>TableCellRenderer</code> and 
	 * <code>TableCellEditor</code> as default renderer and default editor.
	 * 
	 * @param cls the class to be customized. 
	 * @param renderer a <code>TableCellRenderer</code> used to render objects of the specified class.
	 * @param editor a <code>TableCellEditor</code> used to edit objects of the specified class.
	 */
	public void customize(Class cls, TableCellRenderer renderer, TableCellEditor editor) {
		setDefaultRenderer(cls, renderer);
		setDefaultEditor(cls, editor);		
		setRowHeight(22);		
	}

	/**
	 * Returns the default renderer for the class of the object in the cell
	 * specified by <code>row</code>, <code>column</code>.
	 * 
	 * @return the default <code>TableCellRenderer</code>.
	 */
	public TableCellRenderer getCellRenderer(int row, int column) {
		Object value = getValueAt(row, column);
		if (value == null) {
			return getDefaultRenderer(Object.class);
		} else {
			return getDefaultRenderer(value.getClass());
		}
	}

	/**
	 * Returns the default editor for the class of the object in the cell
	 * specified by <code>row</code>, <code>column</code>.
	 * 
	 * @return the default <code>TableCellEditor</code>.
	 */
	public TableCellEditor getCellEditor(int row, int column) {
		Object value = getValueAt(row, column);
		if (value == null) {
			return getDefaultEditor(Object.class);
		} else {
			return getDefaultEditor(value.getClass());
		}
	}

	/**
	 * If the editorComponent is an instance of <code>JTextComponent</code>,
	 * a <code>FocusListener</code> is added to the editor component 
	 * so the <code>StandardInspectorTable</code> will be 
	 * notified if the editor component loses focus. <br>
	 * This is done to prevent the user from accidentally losing data if 
	 * editing is not finished properly with the enter key.
	 */
	public Component prepareEditor(TableCellEditor editor, int row, int column) {
		Component editorComponent = editor.getTableCellEditorComponent(this, getValueAt(row, column), true, row, column);
		if (editorComponent instanceof JTextComponent)
			editorComponent.addFocusListener(new EditorComponentFocusListener());
		return super.prepareEditor(editor, row, column);
	}

	/**
	 * This method updates the table. All properties are reloaded.	 
	 */
	public void updateTable() {
		StandardInspectorTableModel model = (StandardInspectorTableModel) (getModel());
		model.updateAllFields();
	}

	/**
	 * A <code>FocusListener</code> for an editor component telling
	 * the editor to stop editing if the editor component loses focus.
	 * 
	 * @author Dirk
	 */
	class EditorComponentFocusListener implements FocusListener {

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			int row = getEditingRow();
			int col = getEditingColumn();
			if (row != -1 && col != -1) {
				TableCellEditor editor = getCellEditor(row, col);
				editor.stopCellEditing();
			}
			((Component)e.getSource()).removeFocusListener(this);
		}

	}

}
