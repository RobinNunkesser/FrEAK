/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui.graph;

import freak.core.control.Schedule;
import freak.core.graph.Operator;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.ModuleCollector;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.modulesupport.inspector.InspectorVetoException;
import freak.core.parametercontroller.ParameterController;
import freak.gui.ConfigurationPanel;
import freak.gui.JButtonFactory;
import freak.gui.ModuleList;
import freak.gui.scheduleeditor.Util;
import java.awt.Dialog;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * @author  Oliver, Matthias
 */
public class ParameterControllerDialog extends javax.swing.JDialog {

	private Schedule schedule;
	private List parameterController;
	/**
	 * @uml.property  name="parameterControllers"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	private Module[] parameterControllers;
	private ModuleCollector moduleCollector;
	
	private ConfigurationPanel displayedConfigurationPanel;

	/** Creates new form ParameterControllerDialog */
	public ParameterControllerDialog(Dialog parent, Schedule schedule) {
		super(parent, true);
		initComponents();
		setLocationRelativeTo(parent);
		this.schedule = schedule;
		if (schedule.getOperatorGraph().getParameterControllers() == null)
			throw new NullPointerException("ParameterControllerPanel needs a list of parameter controllers.");
		moduleCollector = new ModuleCollector(schedule);

		setup();
	}

	private void setup() {
		// -- setup the splitplane
		jSplitPane1.setRightComponent(null);
		jSplitPane1.setResizeWeight(1);
		jSplitPane1.setDividerLocation(-1);

		// -- setup the table
		DefaultTableModel tm = new DefaultTableModel(null, new Object[] { "Parameter", "Operator", "Property" }) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tableProperties.setModel(tm);
		tableProperties.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel sm = tableProperties.getSelectionModel();
		sm.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				tablePropertiesValueChanged(e);
			}
		});

		// -- populate the list of available ParameterControllers
		parameterControllers = moduleCollector.getParameterControllers();
		((ModuleList)listParameterController).setModules(parameterControllers, (Module)null);

		// -- populate the list of active ParameterControllers
		parameterController = schedule.getOperatorGraph().getParameterControllers();
		DefaultListModel lm = Util.createListModelFrom(parameterController);
		listActivePCs.setModel(lm);

		displayInspector(null);
	}

	private void displayInspector(Module m) {
		// finish the old panel
		try {
			if (displayedConfigurationPanel != null) {
				displayedConfigurationPanel.finishEditing();
			}
		} catch (InspectorVetoException ex) {
			String message = "The properties of module " + displayedConfigurationPanel.getConfiguredModule().getName() + " are invalid.\n" + ex.getMessage(); 
			JOptionPane.showMessageDialog(this, message, "Invalid Property Settings", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JPanel inspector = null;
		if (m != null) {
			// -- create new ConfigurationPanel for Object and show it
			inspector = new ConfigurationPanel(m, schedule.getEventController(), schedule.getPossibleEventSources(), schedule.getBatchList());
		} else {
			inspector = new JPanel();
		}
		inspector.setPreferredSize(new Dimension(200, 0));
		inspector.setMinimumSize(new Dimension(200, 20));

		displayedConfigurationPanel = (inspector instanceof ConfigurationPanel ? (ConfigurationPanel)inspector : null);

		// -- show panel
		jSplitPane1.setRightComponent(inspector);
		jSplitPane1.setDividerLocation(-1);

	}

	private void updateParameterTable(ParameterController pc) {
		DefaultTableModel tm = (DefaultTableModel)tableProperties.getModel();
		tm.setNumRows(0);
		if (pc != null) {
			String[] parameters = pc.getListOfParameters();
			for (int i = 0; i < parameters.length; i++) {
				String gateName = "<none yet>";
				Operator source = pc.getOperatorForParameter(parameters[i]);
				if (source != null)
					gateName = source.getPropertyName();
				String propName = pc.getPropDescriptionForParameter(parameters[i]);
				if (propName == null)
					propName = "<none yet>";
				Object[] data = new Object[] { parameters[i], gateName, propName };
				tm.addRow(data);
			}
		}
	}

	private void tablePropertiesValueChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		buChoose.setEnabled(!lsm.isSelectionEmpty());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() { //GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;

		jSplitPane1 = new javax.swing.JSplitPane();
		jPanel3 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jScrollPane2 = new javax.swing.JScrollPane();
		listParameterController = new ModuleList();
		buAdd = JButtonFactory.newButton();
		jPanel1 = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		listActivePCs = new javax.swing.JList();
		jScrollPane4 = new javax.swing.JScrollPane();
		tableProperties = new javax.swing.JTable();
		buRemove = JButtonFactory.newButton();
		buChoose = JButtonFactory.newButton();
		jPanel4 = new javax.swing.JPanel();
		buClose = JButtonFactory.newButton();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle("Parameter Controller Setup");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel3.setLayout(new java.awt.GridBagLayout());

		jPanel2.setLayout(new java.awt.GridBagLayout());

		jPanel2.setBorder(new javax.swing.border.TitledBorder("Available"));
		jScrollPane2.setMinimumSize(new java.awt.Dimension(200, 0));
		jScrollPane2.setPreferredSize(new java.awt.Dimension(200, 0));
		listParameterController.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				listParameterControllerValueChanged(evt);
			}
		});
		listParameterController.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				listParameterControllerMouseClicked(evt);
			}
		});

		jScrollPane2.setViewportView(listParameterController);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
		jPanel2.add(jScrollPane2, gridBagConstraints);

		buAdd.setText("Add Parameter Controller");
		buAdd.setToolTipText("Add parameter controller to active setup.");
		buAdd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buAddActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		jPanel2.add(buAdd, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weighty = 0.1;
		jPanel3.add(jPanel2, gridBagConstraints);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(new javax.swing.border.TitledBorder("Active"));
		listActivePCs.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(java.awt.event.FocusEvent evt) {
				listActivePCsFocusGained(evt);
			}
		});
		listActivePCs.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				listActivePCsValueChanged(evt);
			}
		});

		jScrollPane3.setViewportView(listActivePCs);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
		jPanel1.add(jScrollPane3, gridBagConstraints);

		jScrollPane4.setViewportView(tableProperties);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
		jPanel1.add(jScrollPane4, gridBagConstraints);

		buRemove.setMnemonic('r');
		buRemove.setText("Remove");
		buRemove.setToolTipText("Remove selected parameter controller from active setup.");
		buRemove.setEnabled(false);
		buRemove.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buRemoveActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
		jPanel1.add(buRemove, gridBagConstraints);

		buChoose.setMnemonic('c');
		buChoose.setText("Choose Property");
		buChoose.setToolTipText("Choose a property to control from an operator of the graph.");
		buChoose.setEnabled(false);
		buChoose.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buChooseActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		jPanel1.add(buChoose, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		jPanel3.add(jPanel1, gridBagConstraints);

		jSplitPane1.setLeftComponent(jPanel3);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(jSplitPane1, gridBagConstraints);

		buClose.setMnemonic('c');
		buClose.setText("Close");
		buClose.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buCloseActionPerformed(evt);
			}
		});

		jPanel4.add(buClose);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		getContentPane().add(jPanel4, gridBagConstraints);

		pack();
	} //GEN-END:initComponents

	private void buCloseActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buCloseActionPerformed
		closeDialog(null);
	} //GEN-LAST:event_buCloseActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) { //GEN-FIRST:event_closeDialog
		// finish currently displayed ConfigurationPanel
		try {
			if (displayedConfigurationPanel != null) {
				displayedConfigurationPanel.finishEditing();
			}
		} catch (InspectorVetoException ex) {
			String message = "The properties of module " + displayedConfigurationPanel.getConfiguredModule().getName() + " are invalid.\n" + ex.getMessage(); 
			JOptionPane.showMessageDialog(this, message, "Invalid Property Settings", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		setVisible(false);
		dispose();
	} //GEN-LAST:event_closeDialog

	private void listActivePCsFocusGained(java.awt.event.FocusEvent evt) { //GEN-FIRST:event_listActivePCsFocusGained
	} //GEN-LAST:event_listActivePCsFocusGained

	private void listParameterControllerMouseClicked(java.awt.event.MouseEvent evt) { //GEN-FIRST:event_listParameterControllerMouseClicked
		if (evt.getClickCount() == 2)
			buAddActionPerformed(null);
	} //GEN-LAST:event_listParameterControllerMouseClicked

	private void listActivePCsValueChanged(javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_listActivePCsValueChanged
		if (!listActivePCs.isSelectionEmpty()) {
			ParameterController pc = (ParameterController)parameterController.get(listActivePCs.getSelectedIndex());
			updateParameterTable(pc);
			buRemove.setEnabled(true);
			displayInspector(pc);
		} else {
			updateParameterTable(null);
			buRemove.setEnabled(false);
			displayInspector(null);
		}
	} //GEN-LAST:event_listActivePCsValueChanged

	private void buChooseActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buChooseActionPerformed
		if (!listActivePCs.isSelectionEmpty() && !tableProperties.getSelectionModel().isSelectionEmpty()) {
			ParameterController pc = (ParameterController)parameterController.get(listActivePCs.getSelectedIndex());
			String paramName = (String)tableProperties.getModel().getValueAt(tableProperties.getSelectedRow(), 0);
			Class paramType = pc.getParameterType(paramName);
			PropertyChooser d = new PropertyChooser(this, pc, paramType, schedule.getOperatorGraph());
			d.setVisible(true);
			Operator g = d.getOperator();
			if (g != null) {
				String propName = d.getPropertyName();
				pc.setPropertyForParameter(paramName, g, propName);
				updateParameterTable(pc);
			}
		}
	} //GEN-LAST:event_buChooseActionPerformed

	private void buRemoveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buRemoveActionPerformed
		if (!listActivePCs.isSelectionEmpty()) {
			int index = listActivePCs.getSelectedIndex();
			ParameterController pc = (ParameterController)parameterController.get(index);
			schedule.getEventController().removeModule(pc);
			parameterController.remove(pc);
			DefaultListModel lm = (DefaultListModel)listActivePCs.getModel();
			lm.removeElementAt(index);
		}
	} //GEN-LAST:event_buRemoveActionPerformed

	private void listParameterControllerValueChanged(javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_listParameterControllerValueChanged
		if (!listParameterController.isSelectionEmpty()) {
			buAdd.setEnabled(true);
		} else {
			buAdd.setEnabled(false);
		}
	} //GEN-LAST:event_listParameterControllerValueChanged

	private void buAddActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buAddActionPerformed
		if (!listParameterController.isSelectionEmpty()) {
			ParameterController selectedController = (ParameterController)listParameterController.getSelectedValue();
			ParameterController pc = null;
			try {
				pc = (ParameterController)moduleCollector.newModule(selectedController.getClass(), new Object[] { schedule.getOperatorGraph()});
			} catch (UnsupportedEnvironmentException e) {
				throw new RuntimeException("Parameter Controller " + selectedController.getClass() + " not supported even though checked before.");
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Parameter Controller " + selectedController.getClass() + " could not be instantiated even though checked before.");
			}
			pc.createEvents();
			parameterController.add(pc);
			DefaultListModel lm = (DefaultListModel)listActivePCs.getModel();
			lm.addElement(pc.getName());
			
			// select new parameter controller in the list of active controllers
			listActivePCs.setSelectedIndex(lm.getSize() - 1);
			listActivePCs.ensureIndexIsVisible(listActivePCs.getSelectedIndex());
		}
	} //GEN-LAST:event_buAddActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton buAdd;
	private javax.swing.JButton buChoose;
	private javax.swing.JButton buClose;
	private javax.swing.JButton buRemove;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JScrollPane jScrollPane4;
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JList listActivePCs;
	private javax.swing.JList listParameterController;
	private javax.swing.JTable tableProperties;
	// End of variables declaration//GEN-END:variables

}
