/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui;

import freak.core.control.*;
import freak.core.modulesupport.*;
import freak.core.modulesupport.inspector.InspectorVetoException;
import java.awt.*;
import javax.swing.*;

/**
 * This dialog adds itself a JPanel it gets from the Module (or StandardInspectorFactory) and two buttons "Ok" and "Cancel". It will pop up whenever the user wants to configure a Module. The Module is needed to construct the Dialog.
 * @author  Oliver, Matthias
 */
public class PropertyDialog extends javax.swing.JDialog {

	private Module module;

	private EventController eventController;

	private ConfigurationPanel configPanel;

	/**
	 * Creates new form PropertyDialog. Furthermore it initializes the components
	 * of the dialog and creates a backup of the configuration of the module.
	 * @param parent a parent dialog to center on
	 * @param eventController the <code>EventController</code> of the originating <code>Schedule</code>
	 * @param module the <code>Module</code> that is to be configured
	 * @param eventSources possible eventsources the module can register itself at
	 */
	public PropertyDialog(Dialog parent, EventController eventController, Module module, Object[] eventSources, BatchList batchList) {
		super(parent, true);
		initComponents();
		setSize(400, 450);
		setLocationRelativeTo(parent);
		initCommon(eventController, module, eventSources, batchList);
	}

	/**
	 * Creates new form PropertyDialog. Furthermore it initializes the components
	 * of the dialog and creates a backup of the configuration of the module.
	 * @param parent a parent frame to center on
	 * @param eventController the <code>EventController</code> of the originating <code>Schedule</code>
	 * @param module the <code>Module</code> that is to be configured
	 * @param eventSources possible eventsources the module can register itself at
	 */
	public PropertyDialog(JFrame parent, EventController eventController, Module module, Object[] eventSources, BatchList batchList) {
		super(parent, true);
		initComponents();
		setSize(350, 400);
		setLocationRelativeTo(parent);
		initCommon(eventController, module, eventSources, batchList);
	}

	private void initCommon(EventController eventController, Module module, Object[] eventSources, BatchList batchList) {
		this.module = module;
		this.eventController = eventController;

		configPanel = new ConfigurationPanel(module, eventController, eventSources, batchList);
		configPanelContainer.add(configPanel);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() { //GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;

		buClose = JButtonFactory.newButton();
		configPanelContainer = new javax.swing.JPanel();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle("Configure Module...");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		buClose.setMnemonic('c');
		buClose.setText("Close");
		buClose.setToolTipText("Closes the Configuration Dialog.");
		buClose.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buCloseActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
		getContentPane().add(buClose, gridBagConstraints);

		configPanelContainer.setLayout(new javax.swing.BoxLayout(configPanelContainer, javax.swing.BoxLayout.X_AXIS));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		getContentPane().add(configPanelContainer, gridBagConstraints);

		pack();
	} //GEN-END:initComponents

	private void buCloseActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buCloseActionPerformed
		closeDialog(null);
	} //GEN-LAST:event_buCloseActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) { //GEN-FIRST:event_closeDialog
		try {
			configPanel.finishEditing();
		} catch (InspectorVetoException e) {
			// ConfigurationPanel vetoes closing
			JOptionPane.showMessageDialog(this, "This module's properties are invalid.\n" + e.getMessage(), "Invalid Property Settings", JOptionPane.ERROR_MESSAGE);
			return;
		}

		setVisible(false);
		dispose();
	} //GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton buClose;
	private javax.swing.JPanel configPanelContainer;
	// End of variables declaration//GEN-END:variables

}
