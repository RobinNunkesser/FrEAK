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
 * An extension of the ModuleList providing JCheckBoxes for all entries.
 * The methods setSelectedIndices, getSelectedIndices, and getSelectedValues are
 * overwritten so that the selected JCheckBoxes are referred instead of the 
 * selection of list entries. That way, when SINGLE_SELECTION mode is used, 
 * one list entry can be selected and highlighted independently of the 
 * JCheckBoxes.   
 *
 * @author Dirk
 */
public class ModuleCheckBoxList extends ModuleList {
	
	private JCheckBox[] checkBoxes;
	
	public ModuleCheckBoxList() {
		checkBoxes = new JCheckBox[0];
		setCellRenderer(new ModuleCheckBoxListCellRenderer());
	}
	
	/**
	 * Creates a new model from the specified modules, integrating
	 * the current modules into the model. All instances of the same class
	 * as the current module are replaced by the current modules.
	 *
	 * @param modules the new elements of the list.
	 * @param currentModules the modules all other instances are to be replaced with. May be <code>null</code>.
	 */
	public void setModules(Module[] modules, Module[] currentModules) {
		DefaultListModel listModel = new DefaultListModel();

		boolean[] isSelected = new boolean[modules.length];
		int numberOfSelectedIndices = 0;

		for (int i = 0; i < modules.length; i++) {
			
			// look for module of the same class in currentModules
			Module currentModule = null;
			if (currentModules != null) {
				for (int j = 0; j < currentModules.length; j++) {
					if (currentModules[j] != null && sameClass(modules[i], currentModules[j])) currentModule = currentModules[j]; 
				}
			}
			if (currentModule != null) {
				listModel.addElement(currentModule);
				isSelected[i] = true;
				numberOfSelectedIndices++;
			} else {
				listModel.addElement(modules[i]);
				isSelected[i] = false;
			}
		}

		setModel(listModel);
		
		checkBoxes = new JCheckBox[modules.length];
		for (int i = 0; i < checkBoxes.length; i++) {
			checkBoxes[i] = new JCheckBox();
			checkBoxes[i].setSelected(isSelected[i]);
		}
	}

	/**
	 * Returns an array of all values in the list whose associated CheckBox is 
	 * checked.
	 */
	public Object[] getCheckedValues() {
		// count number of checked boxes
		int number = 0;
		for (int i = 0; i < checkBoxes.length; i++) {
			if (checkBoxes[i].isSelected()) number++;
		}
		
		int index = 0;
		Object[] result = new Object[number];
		for (int i = 0; i < checkBoxes.length; i++) {
			if (checkBoxes[i].isSelected()) result[index++] = getModel().getElementAt(i);
		}
		
		return result;
	}

	public JCheckBox getCheckBox(int i) {
		return checkBoxes[i];
	}

	private class ModuleCheckBoxListCellRenderer extends DefaultListCellRenderer {
		
		private String moduleName = "N/A";
		
		public String getText() {
			return moduleName;
		}
		
		public Component getListCellRendererComponent(JList arg0, Object arg1, int index, boolean arg3, boolean arg4) {
			if (arg1 != null) { 
				moduleName = ((Module)arg1).getName();
			} else {
				moduleName = "(none)";
			}
			
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			
			Component comp = super.getListCellRendererComponent(arg0, arg1, index, arg3, arg4); 
			checkBoxes[index].setBackground(comp.getBackground());
			panel.add(checkBoxes[index], BorderLayout.WEST);
			panel.add(comp, BorderLayout.CENTER);
			
			return panel;
		}
	}

}
