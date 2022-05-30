/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.gui;

import java.awt.*;

import javax.swing.*;

import freak.core.modulesupport.*;

/**
 * A <code>JList</code> rendering modules by <code>Module.getName()</code>
 * instead of <code>Object.toString</code>.
 *
 * @author Dirk, Michael
 */
public class ModuleList extends JList {
	
	public ModuleList() {
		super();
		setCellRenderer(new ModuleListCellRenderer());
	}
	
	/**
	 * Creates a new model from the specified modules, integrating
	 * the current module into the model. All instances of the same class
	 * as the current module are replaced by the current module.
	 * In addition, the last instance is set as selected index.
	 *
	 * @param modules the new elements of the list.
	 * @param currentModule the module all other instances are to be replaced with. May be <code>null</code>.
	 */
	public void setModules(Module[] modules, Module currentModule) {
		int selected = getSelectedIndex();
		DefaultListModel listModel = new DefaultListModel();
		for (int i = 0; i < modules.length; i++) {
			if (currentModule != null && sameClass(modules[i], currentModule)) {
				listModel.addElement(currentModule);
				selected = i;
			} else {
				listModel.addElement(modules[i]);
			}
		}
		if (selected == -1 && modules.length > 0)
			selected = 0;
		setModel(listModel);
		setSelectedIndex(selected);
	}
	
	boolean sameClass(Module m1, Module m2) {
		if (m1 == null && m2 == null) return true;
		if (m1 == null || m2 == null) return false;
		return m1.getClass().equals(m2.getClass());
	}
	
	private class ModuleListCellRenderer extends DefaultListCellRenderer {
		
		private String moduleName = "N/A";
		
		public String getText() {
			return moduleName;
		}
		
		public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
			if (arg1 != null) { 
				moduleName = ((Module)arg1).getName();
			} else {
				moduleName = "(none)";
			}
			return super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
		}
	}
}
