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

import freak.core.graph.Operator;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.PropertyManager;
import freak.core.parametercontroller.ParameterController;
import freak.gui.CheckBoxDialog;
import freak.gui.JButtonFactory;
import java.awt.Dialog;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author  Oliver
 */
public class PropertyChooser extends javax.swing.JDialog {

	/**
	 * @author  nunkesser
	 */
	private class PropertyWrapper {
		Operator owner;
		String propertyName;
		String propertyDescription;
		public PropertyWrapper(Operator owner, String propertyName) {
			this.owner = owner;
			this.propertyName = propertyName;
			propertyDescription = PropertyManager.getShortDescriptionFor(owner, propertyName);
			if (propertyDescription == null)
				propertyDescription = propertyName;
		}
		public String toString() {
			return propertyDescription;
		}
	}

	private Class paramType;
	private OperatorGraph operatorGraph;

	private static boolean parameterControllerDependencyWarningAcknowledged = false;

	/** Used for direct access to the root of the observer-tree. */
	private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
	/** Used for direct access to the TreeModel of the observer-tree. */
	private DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

	private PropertyWrapper returnProperty = null;
	
	/** The parameter controller the property is to be chosen for. */
	private ParameterController parameterController = null;

	/** Creates new form PropertyChooser */
	public PropertyChooser(Dialog parent, ParameterController pc, Class paramType, OperatorGraph operatorGraph) {
		super(parent, true);
		initComponents();
		setLocationRelativeTo(parent);

		initTree();

		this.parameterController = pc;
		this.paramType = paramType;
		this.operatorGraph = operatorGraph;
		buildPropertyTree();
	}

	private void initTree() {
		// -- configure tree of observer items
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setOpenIcon(renderer.getLeafIcon());
		renderer.setClosedIcon(renderer.getLeafIcon());
		renderer.setLeafIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
		treeProperties.setCellRenderer(renderer);
		treeProperties.setRootVisible(false);
		treeProperties.setShowsRootHandles(true);
		treeProperties.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeProperties.setExpandsSelectedPaths(true);
		treeProperties.setModel(treeModel);
	}

	private void buildPropertyTree() {
		int numOperators = operatorGraph.getOperatorCount();
		for (int i = 0; i < numOperators; i++) {
			Operator g = operatorGraph.getOperator(i);
			String[] propertyNames = g.getListOfPropertyNamesWithType(paramType);
			if (propertyNames.length > 0) {
				// -- yes, operator g has properties of correct type
				DefaultMutableTreeNode operatorNode = new DefaultMutableTreeNode(g);
				treeModel.insertNodeInto(operatorNode, rootNode, rootNode.getChildCount());
				for (int j = 0; j < propertyNames.length; j++) {
					PropertyWrapper pw = new PropertyWrapper(g, propertyNames[j]);
					DefaultMutableTreeNode propNode = new DefaultMutableTreeNode(pw);
					operatorNode.add(propNode);
				}
				// expand new leaves
				for (Enumeration children = operatorNode.children(); children.hasMoreElements();) {
					TreePath path = new TreePath(((DefaultMutableTreeNode)children.nextElement()).getPath());
					treeProperties.setSelectionPath(path);
				}
			}
			treeProperties.setSelectionPath(null);
		}
	}

	public Operator getOperator() {
		if (returnProperty != null)
			return returnProperty.owner;
		else
			return null;
	}

	public String getPropertyName() {
		if (returnProperty != null)
			return returnProperty.propertyName;
		else
			return null;
	}
	
	private void checkParameterControllerDependencies(ParameterController myController, Operator operator, String propName) {
		if (!parameterControllerDependencyWarningAcknowledged) {
			List concurrentControllers = operator.getOperatorGraph().getParameterControllersForProperty(operator, propName);
			concurrentControllers.remove(myController);
				 				
			if (concurrentControllers.size() > 0) {
				ParameterController[] controllers = new ParameterController[concurrentControllers.size()];
				concurrentControllers.toArray(controllers);
					
				// sort controllers alphabetically by name
				Arrays.sort(controllers, new Comparator() {
					public int compare(Object o1, Object o2) {
						if ((!(o1 instanceof ParameterController)) || (!(o2 instanceof ParameterController))) return 0;
						return ((ParameterController)o1).getName().compareTo(((ParameterController)o2).getName());
					}
				});
					
				// create message
				String descr = PropertyManager.getShortDescriptionFor(operator, propName);
				StringBuffer s = new StringBuffer("The property " + descr + " is already controlled by the following parameter controller" + (controllers.length > 1 ? "s" : "") + ":\n");
				for (int i = 0; i < controllers.length; i++) {
					s.append("\n    " + controllers[i].getName());
				}
				s.append("\n\nAdding multiple controllers to the same property might cause unpredictable results.");
					
				// show dialog
				CheckBoxDialog dialog = new CheckBoxDialog(null, true, s.toString(), "Warning", "don't show this warning again");
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
				parameterControllerDependencyWarningAcknowledged = dialog.isCheckBoxSelected();
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

		jScrollPane1 = new javax.swing.JScrollPane();
		treeProperties = new javax.swing.JTree();
		jLabel1 = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		buOk = JButtonFactory.newButton();
		buCancel = JButtonFactory.newButton();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		treeProperties.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treePropertiesValueChanged(evt);
			}
		});

		jScrollPane1.setViewportView(treeProperties);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
		getContentPane().add(jScrollPane1, gridBagConstraints);

		jLabel1.setText("Select the property you want to control.");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
		getContentPane().add(jLabel1, gridBagConstraints);

		jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

		buOk.setMnemonic('o');
		buOk.setText("OK");
		buOk.setEnabled(false);
		buOk.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buOkActionPerformed(evt);
			}
		});

		jPanel1.add(buOk);

		buCancel.setText("Cancel");
		buCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buCancelActionPerformed(evt);
			}
		});

		jPanel1.add(buCancel);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		getContentPane().add(jPanel1, gridBagConstraints);

		pack();
	} //GEN-END:initComponents

	private void treePropertiesValueChanged(javax.swing.event.TreeSelectionEvent evt) { //GEN-FIRST:event_treePropertiesValueChanged
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeProperties.getLastSelectedPathComponent();
		buOk.setEnabled(node != null && node.isLeaf());
	} //GEN-LAST:event_treePropertiesValueChanged

	private void buOkActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buOkActionPerformed
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeProperties.getLastSelectedPathComponent();
		if (node.isLeaf()) {
			returnProperty = (PropertyWrapper)node.getUserObject();
			checkParameterControllerDependencies(parameterController, returnProperty.owner, returnProperty.propertyName);
		}
		closeDialog(null);
	} //GEN-LAST:event_buOkActionPerformed

	private void buCancelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buCancelActionPerformed
		closeDialog(null);
	} //GEN-LAST:event_buCancelActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) { //GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	} //GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton buCancel;
	private javax.swing.JButton buOk;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTree treeProperties;
	// End of variables declaration//GEN-END:variables

}
