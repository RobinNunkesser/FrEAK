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

import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.gui.JButtonFactory;
import freak.gui.graph.EditorDialog;
import freak.gui.runframe.SingleExtensionFileFilter;
import freak.module.support.OperatorGraphCollector;
import freak.module.support.OperatorGraphFile;
import org.jgraph.JGraph;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.jar.JarFile;

/**
 * @author  Oliver, Michael
 */
public class PanelGraph extends ScheduleEditorPanel {

	private FreakGraphModelInterface graphModel;
	private JGraph graphPreview;
	protected OperatorGraphCollector graphCollector;

	/** Creates new JPanel */
	public PanelGraph(ScheduleEditor scheduleEditor) {
		super(scheduleEditor);
		graphModel = schedule.getFreakGraphModel();
		graphCollector = new OperatorGraphCollector(schedule);
		initComponents();
		initPredefinedGraphs();
	}

	public static String getDescription() {
		return "Create Algorithm-Graph";
	}

	public String getHelpURL() {
		return "node4.html#SECTION00444000000000000000";
	}

	private void initPredefinedGraphs() {
		//comboPredefinedGraphs.addItem("- unchanged -");
		ModuleInfo[] mi = graphCollector.getPredefinedGraphs(null);
		if (mi.length == 0) {
			comboPredefinedGraphs.addItem("No predefined graphs found");
			comboPredefinedGraphs.setSelectedIndex(0);
			comboPredefinedGraphs.setEnabled(false);
			labelPredefGraphs.setEnabled(false);
			return;
		} else {
			comboPredefinedGraphs.setEnabled(true);
			labelPredefGraphs.setEnabled(true);
		}
		for (int i = 0; i < mi.length; i++) {
			comboPredefinedGraphs.addItem(mi[i]);
		}
		comboPredefinedGraphs.setSelectedIndex(-1);

		comboPredefinedGraphs.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				eventPredefinedGraphSelected(evt);
			}
		});
		if ((schedule.getFreakGraphModel() == null || schedule.getOperatorGraph().isEmpty()) && mi.length > 0)
			comboPredefinedGraphs.setSelectedIndex(0);
	}

	private void eventPredefinedGraphSelected(java.awt.event.ActionEvent evt) {
		Object o = comboPredefinedGraphs.getSelectedItem();
		// -- if we don't have a ModuleInfo, we've got the first item "Create new graph"
		if (o instanceof ModuleInfo) {
			ModuleInfo mi = (ModuleInfo)o;
			loadPredefinedGraph(mi);
		} else {
			loadPredefinedGraph(null);
		}
	}

	public boolean mayAdvance() {
		return graphModel != null;
	}

	public boolean advanceVeto() {
		return !scheduleDependencyChecker.isGraphCorrect();
	}

	public void enter() {
		super.enter();
		graphPreview = new JGraph();
		graphPreview.setEnabled(false);
		scrollpaneGraph.setViewportView(graphPreview);
		parse();
	}

	private void parse() {
		graphModel = schedule.getFreakGraphModel();
		if (graphModel == null) {
			graphModel = new FreakGraphModel(schedule);
			graphModel.getOperatorGraph().createEvents();
			schedule.setGraphModel(graphModel);
			scheduleDependencyChecker.processNewGraph();
		}

		graphPreview.setModel((FreakGraphModel)graphModel);
	}

	private void loadPredefinedGraph(ModuleInfo mi) {
		if (mi != null) {
			scheduleDependencyChecker.removeGraph();
			try {
				OperatorGraphFile ogFile;

				//jar file
				//String[] classpaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
				String startedFrom=ClassCollector.getStartedFrom();
				// NEW CHECK
				if (mi.getClassName().startsWith("freak") && startedFrom.toLowerCase().endsWith(".jar")) {
					JarFile jf = new JarFile(startedFrom);
					ogFile = OperatorGraphFile.read(jf.getInputStream(jf.getJarEntry(mi.getClassName())));
				} else {
					ogFile = OperatorGraphFile.read(new FileInputStream(new File(mi.getClassName())));
				}
				FreakGraphModel model = ogFile.generateGraph(schedule);
				model.getOperatorGraph().setName(mi.getName());

				schedule.setGraphModel(model);
				scheduleDependencyChecker.processNewGraph();
				scheduleEditor.updateComponents();

				if (graphPreview != null)
					graphPreview.setModel(model);
			} catch (Exception exc) {
				System.out.println("Error loading graph " + mi.getClassName());
			}
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() { //GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;

		panelGraph = new JPanel();
		scrollpaneGraph = new JScrollPane();
		jPanel15 = new JPanel();
		labelPredefGraphs = new JLabel();
		comboPredefinedGraphs = new JComboBox();
		jPanel1 = new JPanel();
		buEditGraph = JButtonFactory.newButton();
		buOpen = JButtonFactory.newButton();

		setLayout(new java.awt.GridBagLayout());

		setBorder(new javax.swing.border.TitledBorder("Create Algorithm Graph"));
		panelGraph.setLayout(new java.awt.GridBagLayout());

		panelGraph.setBorder(new javax.swing.border.TitledBorder("Graph Preview"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		panelGraph.add(scrollpaneGraph, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
		add(panelGraph, gridBagConstraints);

		jPanel15.setLayout(new java.awt.GridBagLayout());

		labelPredefGraphs.setText("Predefined Graphs:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
		jPanel15.add(labelPredefGraphs, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.1;
		jPanel15.add(comboPredefinedGraphs, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		add(jPanel15, gridBagConstraints);

		buEditGraph.setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Edit16.gif")));
		buEditGraph.setMnemonic('e');
		buEditGraph.setText("Edit Graph...");
		buEditGraph.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buEditGraphActionPerformed(evt);
			}
		});

		jPanel1.add(buEditGraph);

		buOpen.setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open16.gif")));
		buOpen.setMnemonic('o');
		buOpen.setText("Open...");
		buOpen.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buOpenActionPerformed(evt);
			}
		});

		jPanel1.add(buOpen);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		add(jPanel1, gridBagConstraints);

	} //GEN-END:initComponents

	private void buOpenActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buOpenActionPerformed
		JFileChooser c = new JFileChooser();
		c.setFileFilter(new SingleExtensionFileFilter(".fop", "Freak Operator Graph"));
		int accepted = c.showOpenDialog(this);
		if (accepted != JFileChooser.APPROVE_OPTION)
			return;

		File f = c.getSelectedFile();
		FreakGraphModel newGraph = Util.loadOperatorGraph(f.getAbsolutePath(), schedule);
		if (newGraph != null) {
			graphModel.getOperatorGraph().removeFromEventController();
			schedule.setGraphModel(newGraph);
			scheduleDependencyChecker.processNewGraph();
			parse();
			scheduleEditor.updateComponents();
		} else
			JOptionPane.showMessageDialog(this, "Error loading operatorgraph\n" + f.getAbsolutePath());
	} //GEN-LAST:event_buOpenActionPerformed

	private void buEditGraphActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buEditGraphActionPerformed
		EditorDialog gred = new EditorDialog(scheduleEditor, schedule);
		gred.setLocationRelativeTo(null);
		gred.setVisible(true);
		// -- update the preview
		comboPredefinedGraphs.setSelectedIndex(-1);
		scheduleDependencyChecker.processNewGraph();
		parse();
		scheduleEditor.updateComponents();
		gred.dispose();
	} //GEN-LAST:event_buEditGraphActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JButton buEditGraph;
	private JButton buOpen;
	private JComboBox comboPredefinedGraphs;
	private JPanel jPanel1;
	private JPanel jPanel15;
	private JLabel labelPredefGraphs;
	private JPanel panelGraph;
	private JScrollPane scrollpaneGraph;
	// End of variables declaration//GEN-END:variables

}