/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui.scheduleeditor;

import freak.core.control.*;
import freak.core.fitness.*;
import freak.core.modulesupport.*;
import freak.core.modulesupport.Module;
import freak.gui.JButtonFactory;
import freak.gui.ModuleList;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

/**
 * This Dialog allows the user to add/remove fitness transformer to his current schedule setup.
 * @author  Oliver
 */
public class FitnessTransformerDialog extends JDialog {

	private ScheduleInterface schedule;
	private ModuleCollector moduleCollector;
	/**
	 * @uml.property  name="fitnessTransformers"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	private Module[] fitnessTransformers;
	private List currentFitnessTransformers;

	/** Creates new form FitnessTransformerDialog */
	public FitnessTransformerDialog(Dialog parent, ScheduleInterface schedule) {
		super(parent, true);
		initComponents();

		setSize(530, 350);
		setLocationRelativeTo(parent);

		this.schedule = schedule;
		moduleCollector = new ModuleCollector(schedule);
		fitnessTransformers = moduleCollector.getFitnessTransformers();
		currentFitnessTransformers = Util.getFitnessTransformersFrom(schedule);
		initFitnessTransformers();
	}

	/**
	 * @return  the edited list of fitness transformers
	 * @uml.property  name="fitnessTransformers"
	 */
	public List getFitnessTransformers() {
		return currentFitnessTransformers;
	}

	private void initFitnessTransformers() {
		((ModuleList)listAvailableTransformers).setModules(fitnessTransformers, (Module)null);
		listAvailableTransformers.setEnabled(fitnessTransformers.length > 0);
		buAddTransformer.setEnabled(fitnessTransformers.length > 0);

		DefaultListModel listModel = new DefaultListModel();
		for (Iterator i = currentFitnessTransformers.iterator(); i.hasNext();) {
			FitnessTransformer ft = (FitnessTransformer)i.next();
			String name = ft.getName();
			if (ft instanceof Configurable) {
				Configurable confobj = (Configurable)ft;
				String confstr = confobj.getConfiguration().getDescription();
				name = name + " (" + confstr + ")";
			}
			listModel.addElement(name);
		}
		listCurrentTransformer.setModel(listModel);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() { //GEN-BEGIN:initComponents
		GridBagConstraints gridBagConstraints;

		jPanel10 = new JPanel();
		jPanel12 = new JPanel();
		jScrollPane11 = new JScrollPane();
		listAvailableTransformers = new ModuleList();
		jScrollPane12 = new JScrollPane();
		taDescrFitnessTransformer = new JTextArea();
		buAddTransformer = JButtonFactory.newButton();
		jPanel13 = new JPanel();
		jScrollPane17 = new JScrollPane();
		listCurrentTransformer = new JList();
		buConfigFitnessTransformer = JButtonFactory.newButton();
		buRemoveTransformer = JButtonFactory.newButton();
		jPanel1 = new JPanel();
		buHelp = JButtonFactory.newButton();
		buClose = JButtonFactory.newButton();

		getContentPane().setLayout(new GridBagLayout());

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Fitness Transformer");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel10.setLayout(new GridBagLayout());

		jPanel10.setBorder(new javax.swing.border.TitledBorder("Fitness Transformer Selection"));
		jPanel12.setLayout(new GridBagLayout());

		jPanel12.setBorder(new javax.swing.border.TitledBorder("Available Fitness Transformers"));
		jPanel12.setMinimumSize(new Dimension(220, 0));
		jPanel12.setPreferredSize(new Dimension(220, 0));
		listAvailableTransformers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listAvailableTransformers.setEnabled(false);
		listAvailableTransformers.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				listAvailableTransformerValueChanged(evt);
			}
		});
		listAvailableTransformers.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				listAvailableTransformerMouseClicked(evt);
			}
		});

		jScrollPane11.setViewportView(listAvailableTransformers);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new Insets(0, 5, 0, 5);
		jPanel12.add(jScrollPane11, gridBagConstraints);

		jScrollPane12.setMinimumSize(new Dimension(0, 80));
		jScrollPane12.setPreferredSize(new Dimension(0, 80));
		taDescrFitnessTransformer.setEditable(false);
		taDescrFitnessTransformer.setLineWrap(true);
		taDescrFitnessTransformer.setText("<no fitness transformer selected>");
		taDescrFitnessTransformer.setWrapStyleWord(true);
		jScrollPane12.setViewportView(taDescrFitnessTransformer);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		jPanel12.add(jScrollPane12, gridBagConstraints);

		buAddTransformer.setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Forward16.gif")));
		buAddTransformer.setMnemonic('a');
		buAddTransformer.setText("Add");
		buAddTransformer.setEnabled(false);
		buAddTransformer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buAddTransformerActionPerformed(evt);
			}
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		jPanel12.add(buAddTransformer, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new Insets(0, 5, 0, 5);
		jPanel10.add(jPanel12, gridBagConstraints);

		jPanel13.setLayout(new GridBagLayout());

		jPanel13.setBorder(new javax.swing.border.TitledBorder("Current Fitness Transformers"));
		listCurrentTransformer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listCurrentTransformer.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				listCurrentTransformerValueChanged(evt);
			}
		});
		listCurrentTransformer.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				listCurrentTransformerMouseClicked(evt);
			}
		});

		jScrollPane17.setViewportView(listCurrentTransformer);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		jPanel13.add(jScrollPane17, gridBagConstraints);

		buConfigFitnessTransformer.setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
		buConfigFitnessTransformer.setMnemonic('o');
		buConfigFitnessTransformer.setText("Configure...");
		buConfigFitnessTransformer.setEnabled(false);
		buConfigFitnessTransformer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buConfigFitnessTransformerActionPerformed(evt);
			}
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		jPanel13.add(buConfigFitnessTransformer, gridBagConstraints);

		buRemoveTransformer.setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif")));
		buRemoveTransformer.setMnemonic('r');
		buRemoveTransformer.setText("Remove");
		buRemoveTransformer.setEnabled(false);
		buRemoveTransformer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buRemoveTransformerActionPerformed(evt);
			}
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		jPanel13.add(buRemoveTransformer, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new Insets(0, 0, 0, 5);
		jPanel10.add(jPanel13, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new Insets(5, 5, 0, 5);
		getContentPane().add(jPanel10, gridBagConstraints);

		buHelp.setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Help16.gif")));
		buHelp.setMnemonic('h');
		buHelp.setText("Help");
		buHelp.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buHelpActionPerformed(evt);
			}
		});

		jPanel1.add(buHelp);

		buClose.setMnemonic('c');
		buClose.setText("Close");
		buClose.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buCloseActionPerformed(evt);
			}
		});

		jPanel1.add(buClose);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		getContentPane().add(jPanel1, gridBagConstraints);

		pack();
	} //GEN-END:initComponents

	private void buHelpActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buHelpActionPerformed
		HelpDialog help = new HelpDialog(FitnessTransformerDialog.this, "Fitness transformers provide additional functionality to your fitness function. They are applied to your fitness function and transform the computed fitness values. You may add multiple transformers which will be applied in the specified order in the list.");
		help.setVisible(true);
	} //GEN-LAST:event_buHelpActionPerformed

	private void buCloseActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buCloseActionPerformed
		closeDialog(null);
	} //GEN-LAST:event_buCloseActionPerformed

	private void buRemoveTransformerActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buRemoveTransformerActionPerformed
		if (!listCurrentTransformer.isSelectionEmpty()) {
			int i = listCurrentTransformer.getSelectedIndex();
			DefaultListModel model = (DefaultListModel)listCurrentTransformer.getModel();
			model.remove(i);
			currentFitnessTransformers.remove(i);
		}
	} //GEN-LAST:event_buRemoveTransformerActionPerformed

	private void buConfigFitnessTransformerActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buConfigFitnessTransformerActionPerformed
		if (!listCurrentTransformer.isSelectionEmpty()) {
			int i = listCurrentTransformer.getSelectedIndex();
			FitnessTransformer ft = (FitnessTransformer)currentFitnessTransformers.get(i);
			if (ft instanceof Configurable) {
				DefaultListModel model = (DefaultListModel)listCurrentTransformer.getModel();
				String s = Util.configureModule(this, ft, schedule);
				if (s != null)
					model.set(i, new String(ft.getName() + " (" + s + ")"));
			}
		}
	} //GEN-LAST:event_buConfigFitnessTransformerActionPerformed

	private void listCurrentTransformerMouseClicked(java.awt.event.MouseEvent evt) { //GEN-FIRST:event_listCurrentTransformerMouseClicked
		if (evt.getClickCount() == 2)
			buConfigFitnessTransformerActionPerformed(null);
	} //GEN-LAST:event_listCurrentTransformerMouseClicked

	private void listCurrentTransformerValueChanged(javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_listCurrentTransformerValueChanged
		if (listCurrentTransformer.isSelectionEmpty()) {
			buRemoveTransformer.setEnabled(false);
			buConfigFitnessTransformer.setEnabled(false);
		} else {
			buRemoveTransformer.setEnabled(true);
			FitnessTransformer ft = (FitnessTransformer)currentFitnessTransformers.get(listCurrentTransformer.getSelectedIndex());
			buConfigFitnessTransformer.setEnabled(ft instanceof Configurable);
		}
	} //GEN-LAST:event_listCurrentTransformerValueChanged

	private void buAddTransformerActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buAddTransformerActionPerformed
		if (!listAvailableTransformers.isSelectionEmpty()) {
			// -- create new instance of the selected transformer and store it
			FitnessTransformer selectedTransformer = (FitnessTransformer)listAvailableTransformers.getSelectedValue();
			FitnessTransformer ft = null;
			try {
				ft = (FitnessTransformer)moduleCollector.newModule(selectedTransformer.getClass(), new Object[] { schedule });
			} catch (UnsupportedEnvironmentException e) {
				throw new RuntimeException("Fitness Transformer " + selectedTransformer.getClass() + " is not supported even though checked before.");
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Fitness Transformer " + selectedTransformer.getClass() + " could not be instantiated even though checked before.");
			}
			ft.createEvents();
			currentFitnessTransformers.add(ft);
			// -- create descriptive string
			String name = ft.getName();
			if (ft instanceof Configurable) {
				Configurable confobj = (Configurable)ft;
				String confstr = confobj.getConfiguration().getDescription();
				name = name + " (" + confstr + ")";
			}
			// -- add the object to the "current" list
			DefaultListModel model = (DefaultListModel)listCurrentTransformer.getModel();
			model.addElement(name);
			listCurrentTransformer.setSelectedValue(name, true);
		}
	} //GEN-LAST:event_buAddTransformerActionPerformed

	private void listAvailableTransformerMouseClicked(java.awt.event.MouseEvent evt) { //GEN-FIRST:event_listAvailableTransformerMouseClicked
		if (evt.getClickCount() == 2)
			buAddTransformerActionPerformed(null);
	} //GEN-LAST:event_listAvailableTransformerMouseClicked

	private void listAvailableTransformerValueChanged(javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_listAvailableTransformerValueChanged
		if (listAvailableTransformers.isSelectionEmpty()) {
			Util.displayText(taDescrFitnessTransformer, "<no fitness transformer selected>");
		} else {
			int i = listAvailableTransformers.getSelectedIndex();
			Util.displayText(taDescrFitnessTransformer, fitnessTransformers[i].getDescription());
		}
	} //GEN-LAST:event_listAvailableTransformerValueChanged

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) { //GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	} //GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JButton buAddTransformer;
	private JButton buClose;
	private JButton buConfigFitnessTransformer;
	private JButton buHelp;
	private JButton buRemoveTransformer;
	private JPanel jPanel1;
	private JPanel jPanel10;
	private JPanel jPanel12;
	private JPanel jPanel13;
	private JScrollPane jScrollPane11;
	private JScrollPane jScrollPane12;
	private JScrollPane jScrollPane17;
	private JList listAvailableTransformers;
	private JList listCurrentTransformer;
	private JTextArea taDescrFitnessTransformer;
	// End of variables declaration//GEN-END:variables
}
