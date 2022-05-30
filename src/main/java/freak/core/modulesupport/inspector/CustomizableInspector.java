/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.modulesupport.inspector;

import freak.core.modulesupport.Module;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * An inspector that renders the data in a table. Additionally, customized rendering and data editing for all objects of  a specified class is provided.  
 * @author  Dirk, Heiko
 */
public class CustomizableInspector extends Inspector {

	private CustomizableInspectorTable table;

	/**
	 * Constructs a new <code>Inspector</code> with the specified table.
	 * 
	 * @param table the table used by the inspector to render and edit the inspected data.
	 * @param the module the inspector works on.
	 */
	public CustomizableInspector(CustomizableInspectorTable table, Module module) {
		super(module);

		this.table = table;
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
		table.customize(cls, renderer, editor);
	}

	/**
	 * @return  the table which is placed in the inspector.
	 * @uml.property  name="table"
	 */
	public CustomizableInspectorTable getTable() {
		return table;
	}

}
