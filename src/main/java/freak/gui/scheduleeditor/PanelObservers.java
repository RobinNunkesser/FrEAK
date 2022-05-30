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

import freak.core.modulesupport.*;
import freak.core.observer.*;
import freak.core.observer.Observer;
import freak.core.view.*;
import freak.gui.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * @author  Oliver, Michael
 */
public class PanelObservers extends ScheduleEditorPanel {

	private ObserverManager observerManager;
	/**
	 * @uml.property  name="observers"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	private Module[] observers;
	/**
	 * @uml.property  name="views"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	private Module[] views;
	/** Used for direct access to the root of the observer-tree. */
	private DefaultMutableTreeNode rootNode;
	/** Used for direct access to the TreeModel of the observer-tree. */
	private DefaultTreeModel treeModel;

	/** Creates new JPanel */
	public PanelObservers(ScheduleEditor scheduleEditor) {
		super(scheduleEditor);
		initComponents();
		observerManager = schedule.getObserverManager();
		initTree();
	}

	public static String getDescription() {
		return "Create Observers and Views";
	}

	public String getHelpURL() {
		return "node4.html#SECTION00451000000000000000";
	}

	private void initTree() {
		rootNode = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(rootNode);
		treeObsItems.setModel(treeModel);
		TreeCellRenderer renderer = new ObserverTreeCellRenderer();
		treeObsItems.setModel(treeModel);
		treeObsItems.setCellRenderer(renderer);
		treeObsItems.setRootVisible(false);
		treeObsItems.setShowsRootHandles(true);
		treeObsItems.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeObsItems.setExpandsSelectedPaths(true);
		ToolTipManager.sharedInstance().registerComponent(treeObsItems);
	}

	public boolean advanceVeto() {
		return !scheduleDependencyChecker.isObserverSetupOk();
	}

	public void enter() {
		observers = moduleCollector.getObservers();
		((ModuleList)listObservers).setModules(observers, (Module)null);
		buAddObserver.setEnabled(observers.length > 0);
		parseObservers();
	}

	private void parseObservers() {
		List obs = observerManager.getActiveObservers();
		for (Iterator i = obs.iterator(); i.hasNext();) {
			Observer o = (Observer)i.next();
			ObserverTreeElement obsElement = new ObserverTreeElement(o);
			// -- create a treenode
			DefaultMutableTreeNode obsNode = new DefaultMutableTreeNode(obsElement);
			
			Iterator j = o.getViews().iterator();
			if (j.hasNext()) {
				while (j.hasNext()) {
					View v = (View)j.next();
					ObserverTreeElement vElement = new ObserverTreeElement(v);
					DefaultMutableTreeNode viewNode = new DefaultMutableTreeNode(vElement);
					obsNode.add(viewNode);
				}
			} else {
				DefaultMutableTreeNode viewNode = new DefaultMutableTreeNode(ObserverTreeElement.getNoViewTreeElement());
				obsNode.add(viewNode);
			}
			treeModel.insertNodeInto(obsNode, rootNode, rootNode.getChildCount());
			treeObsItems.expandPath(new TreePath(obsNode.getPath()));
		}
	}

	private void createListOfViews(Observer observer) {
		// get list of all views
		views = moduleCollector.getViews();

		// filter the compatible views from the list
		ArrayList compatibleViews = new ArrayList();
		for (int i = 0; i < views.length; i++) {
			try {
				observer.addView((View)views[i]);

				// no exception has been thrown
				compatibleViews.add(views[i]);
				observer.removeView((View)views[i]);
			} catch (ObserverViewMismatchException e) {
				// observer and view do not match 
			}
		}

		// miViews is re-initialized with the compatible views only
		views = new Module[compatibleViews.size()];
		for (int i = 0; i < compatibleViews.size(); i++) {
			views[i] = (Module)compatibleViews.get(i);
		}

		// create list of compatible views
		 ((ModuleList)listViews).setModules(views, (Module)null);

		buAddView.setEnabled(!compatibleViews.isEmpty());
		listViews.setEnabled(!compatibleViews.isEmpty());
		// reset selection
		listViews.removeSelectionInterval(listViews.getMinSelectionIndex(), listViews.getMaxSelectionIndex());
	}

	private void addObserver(Observer o) {
		// -- add observer to ObserverManager
		observerManager.addObserver(o);
		// -- create a treenode
		DefaultMutableTreeNode obsNode = new DefaultMutableTreeNode(new ObserverTreeElement(o));
		// -- add the default "empty" leaf
		obsNode.add(new DefaultMutableTreeNode(ObserverTreeElement.getNoViewTreeElement()));
		// -- insert node into tree
		treeModel.insertNodeInto(obsNode, rootNode, rootNode.getChildCount());
		// -- select newly added node
		TreePath path = new TreePath(obsNode.getPath());
		treeObsItems.setSelectionPath(path);
	}

	private void addView(View v, DefaultMutableTreeNode obsNode) {
		// -- create a treenode
		DefaultMutableTreeNode viewNode = new DefaultMutableTreeNode(new ObserverTreeElement(v));

		// -- insert view to related observer
		ObserverTreeElement ote = (ObserverTreeElement)obsNode.getUserObject();
		if (ote != null) {
			Module mod = ote.getModule();
			if (mod instanceof Observer) {
				Observer obs = (Observer)mod;
				try {
					obs.addView(v);
				} catch (ObserverViewMismatchException e) {
					// -- view doesn't match observer
					e.printStackTrace();
					System.out.println("view doesn't match observer!");
				}
			} else {
				// -- parent is not an observer
				System.out.println("parent is not an observer");
			}
		} else {
			// -- parent is null (root) - possible?
			System.out.println("parent is null?!?");
		}

		// -- remove the NoView-Element if there is one
		if (obsNode.getChildAt(0).toString().equals(ObserverTreeElement.getNoViewTreeElement().toString())) {
			treeModel.removeNodeFromParent((MutableTreeNode)obsNode.getChildAt(0));
		}
		treeModel.insertNodeInto(viewNode, obsNode, obsNode.getChildCount());
		// -- select newly added node
		TreePath path = new TreePath(viewNode.getPath());
		treeObsItems.setSelectionPath(path);
	}

	private void deleteNode(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();
		// -- select the previous node
		treeObsItems.setSelectionRow(treeObsItems.getSelectionRows()[0] - 1);
		// -- if it is a view, remove it from observer. if it is an observer, remove it from ObserverManager
		ObserverTreeElement nodeObj = (ObserverTreeElement)node.getUserObject();
		if (nodeObj.getModule() instanceof View) {
			View v = (View)nodeObj.getModule();
			Observer o = (Observer) ((ObserverTreeElement)parentNode.getUserObject()).getModule();
			o.removeView(v);
			schedule.getEventController().removeModule(v);
		} else if (nodeObj.getModule() instanceof Observer) {
			Observer o = (Observer)nodeObj.getModule();
			observerManager.removeObserver(o);
			// -- unregister all views and the observer from eventsources
			for (Iterator i = o.getViews().iterator(); i.hasNext();) {
				View v = (View)i.next();
				schedule.getEventController().removeModule(v);
			}
			schedule.getEventController().removeModule(o);
		}
		// -- remove the node from tree
		treeModel.removeNodeFromParent(node);
		// -- insert element to show that the observer has no view if necessary
		if (parentNode != rootNode && parentNode.getChildCount() == 0) {
			treeModel.insertNodeInto(new DefaultMutableTreeNode(ObserverTreeElement.getNoViewTreeElement()), parentNode, 0);
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() { //GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;

		jPanel7 = new javax.swing.JPanel();
		jScrollPane4 = new javax.swing.JScrollPane();
		treeObsItems = new javax.swing.JTree();
		jPanel12 = new javax.swing.JPanel();
		tfOptionsObserver = new javax.swing.JTextField();
		buObsItemConfig = JButtonFactory.newButton();
		jLabel1 = new javax.swing.JLabel();
		buObsItemDelete = JButtonFactory.newButton();
		jPanel9 = new javax.swing.JPanel();
		jLabel13 = new javax.swing.JLabel();
		buAddObserver = JButtonFactory.newButton();
		jLabel14 = new javax.swing.JLabel();
		jScrollPane2 = new javax.swing.JScrollPane();
		listObservers = new ModuleList();
		jScrollPane3 = new javax.swing.JScrollPane();
		listViews = new ModuleList();
		buAddView = JButtonFactory.newButton();
		jScrollPane15 = new javax.swing.JScrollPane();
		taDescrObserver = new javax.swing.JTextArea();
		jScrollPane16 = new javax.swing.JScrollPane();
		taDescrView = new javax.swing.JTextArea();

		setLayout(new java.awt.GridBagLayout());

		setBorder(new javax.swing.border.TitledBorder("Add Observers and Views"));
		jPanel7.setLayout(new java.awt.GridBagLayout());

		jPanel7.setBorder(new javax.swing.border.TitledBorder("Current Observer Setup"));
		treeObsItems.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
		treeObsItems.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				treeObsItemsValueChanged(evt);
			}
		});

		jScrollPane4.setViewportView(treeObsItems);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
		jPanel7.add(jScrollPane4, gridBagConstraints);

		jPanel12.setLayout(new java.awt.GridBagLayout());

		tfOptionsObserver.setEditable(false);
		tfOptionsObserver.setText("<no element selected>");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
		gridBagConstraints.weightx = 0.1;
		jPanel12.add(tfOptionsObserver, gridBagConstraints);

		buObsItemConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
		buObsItemConfig.setMnemonic('o');
		buObsItemConfig.setText("Configure...");
		buObsItemConfig.setEnabled(false);
		buObsItemConfig.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buObsItemConfigActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		jPanel12.add(buObsItemConfig, gridBagConstraints);

		jLabel1.setText("Options:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		jPanel12.add(jLabel1, gridBagConstraints);

		buObsItemDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif")));
		buObsItemDelete.setEnabled(false);
		buObsItemDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buObsItemDeleteActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
		jPanel12.add(buObsItemDelete, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
		jPanel7.add(jPanel12, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.2;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
		add(jPanel7, gridBagConstraints);

		jPanel9.setLayout(new java.awt.GridBagLayout());

		jPanel9.setMinimumSize(new java.awt.Dimension(200, 0));
		jPanel9.setPreferredSize(new java.awt.Dimension(200, 0));
		jLabel13.setText("Available Observers:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		jPanel9.add(jLabel13, gridBagConstraints);

		buAddObserver.setFont(new java.awt.Font("Dialog", 0, 12));
		buAddObserver.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Find16.gif")));
		buAddObserver.setMnemonic('a');
		buAddObserver.setText("Add Observer");
		buAddObserver.setEnabled(false);
		buAddObserver.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buAddObserverActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		jPanel9.add(buAddObserver, gridBagConstraints);

		jLabel14.setText("Available Views:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		jPanel9.add(jLabel14, gridBagConstraints);

		listObservers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		listObservers.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				listObserversValueChanged(evt);
			}
		});
		listObservers.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				listObserversMouseClicked(evt);
			}
		});

		jScrollPane2.setViewportView(listObservers);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
		jPanel9.add(jScrollPane2, gridBagConstraints);

		listViews.setEnabled(false);
		listViews.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				listViewsValueChanged(evt);
			}
		});
		listViews.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				listViewsMouseClicked(evt);
			}
		});

		jScrollPane3.setViewportView(listViews);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
		jPanel9.add(jScrollPane3, gridBagConstraints);

		buAddView.setFont(new java.awt.Font("Dialog", 0, 12));
		buAddView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/PrintPreview16.gif")));
		buAddView.setMnemonic('u');
		buAddView.setText("Add View");
		buAddView.setEnabled(false);
		buAddView.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buAddViewActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		jPanel9.add(buAddView, gridBagConstraints);

		jScrollPane15.setMinimumSize(new java.awt.Dimension(0, 50));
		jScrollPane15.setPreferredSize(new java.awt.Dimension(0, 50));
		taDescrObserver.setEditable(false);
		taDescrObserver.setLineWrap(true);
		taDescrObserver.setToolTipText("Here you can read the description of the selected observer.");
		taDescrObserver.setWrapStyleWord(true);
		jScrollPane15.setViewportView(taDescrObserver);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
		jPanel9.add(jScrollPane15, gridBagConstraints);

		jScrollPane16.setMinimumSize(new java.awt.Dimension(0, 50));
		jScrollPane16.setPreferredSize(new java.awt.Dimension(0, 50));
		taDescrView.setEditable(false);
		taDescrView.setLineWrap(true);
		taDescrView.setToolTipText("Here you can read the description of the selected view.");
		taDescrView.setWrapStyleWord(true);
		jScrollPane16.setViewportView(taDescrView);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
		jPanel9.add(jScrollPane16, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
		add(jPanel9, gridBagConstraints);

	} //GEN-END:initComponents

	private void buObsItemConfigActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buObsItemConfigActionPerformed
		TreePath tp = treeObsItems.getSelectionModel().getSelectionPath();
		if (tp != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
			ObserverTreeElement ote = (ObserverTreeElement)node.getUserObject();
			Module m = ote.getModule();
			String s = Util.configureModule(scheduleEditor, m, schedule);
			if (s != null) {
				Util.displayText(tfOptionsObserver, s);
			}
			scheduleDependencyChecker.processNewModuleConfiguration(m);
		}
	} //GEN-LAST:event_buObsItemConfigActionPerformed

	private void buObsItemDeleteActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buObsItemDeleteActionPerformed
		if (!treeObsItems.isSelectionEmpty()) {
			TreePath path = treeObsItems.getSelectionPath();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
			if (!node.getUserObject().toString().equals(ObserverTreeElement.getNoViewTreeElement().toString())) {
				deleteNode(node);
			}
		}
	} //GEN-LAST:event_buObsItemDeleteActionPerformed

	private void buAddViewActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buAddViewActionPerformed
		if (!listViews.isSelectionEmpty()) {
			DefaultMutableTreeNode obsNode = null;
			TreePath parentPath = treeObsItems.getSelectionPath();
			// -- fetch the owning observer
			obsNode = (DefaultMutableTreeNode) (parentPath.getPathComponent(1));
			// -- create a new instance of the selected view
			View selectedView = (View)listViews.getSelectedValue();
			View v = null;
			try {
				v = (View)moduleCollector.newModule(selectedView.getClass(), new Object[] { schedule });
			} catch (UnsupportedEnvironmentException e) {
				throw new RuntimeException("View " + selectedView.getClass() + " is not supported even though checked before.");
			} catch (InvocationTargetException e) {
				throw new RuntimeException("View " + selectedView.getClass() + " could not be instantiated even though checked before.");
			}
			v.createEvents();
			addView(v, obsNode);
		}
	} //GEN-LAST:event_buAddViewActionPerformed

	private void buAddObserverActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buAddObserverActionPerformed
		if (!listObservers.isSelectionEmpty()) {
			// -- create a new instance of the selected observer
			Observer selectedObserver = (Observer)listObservers.getSelectedValue();
			Observer obs = null;
			try {
				obs = (Observer)moduleCollector.newModule(selectedObserver.getClass(), new Object[] { schedule });
			} catch (UnsupportedEnvironmentException e) {
				throw new RuntimeException("Observer " + selectedObserver.getClass() + " is not supported even though checked before.");
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Observer " + selectedObserver.getClass() + " could not be instantiated even though checked before.");
			}
			obs.createEvents();
			addObserver(obs);
		}
	} //GEN-LAST:event_buAddObserverActionPerformed

	private void listViewsMouseClicked(java.awt.event.MouseEvent evt) { //GEN-FIRST:event_listViewsMouseClicked
		if (listViews.isEnabled() && evt.getClickCount() == 2)
			buAddViewActionPerformed(null);
	} //GEN-LAST:event_listViewsMouseClicked

	private void listObserversMouseClicked(java.awt.event.MouseEvent evt) { //GEN-FIRST:event_listObserversMouseClicked
		if (listObservers.isEnabled() && evt.getClickCount() == 2)
			buAddObserverActionPerformed(null);
	} //GEN-LAST:event_listObserversMouseClicked

	private void treeObsItemsValueChanged(javax.swing.event.TreeSelectionEvent evt) { //GEN-FIRST:event_treeObsItemsValueChanged
		TreePath tp = treeObsItems.getSelectionModel().getSelectionPath();
		if (tp != null) {
			buObsItemDelete.setEnabled(true);
			// -- reload the Views-List
			DefaultMutableTreeNode obsNode = (DefaultMutableTreeNode)tp.getPath()[1];
			ObserverTreeElement ote = (ObserverTreeElement)obsNode.getUserObject();
			createListOfViews((Observer)ote.getModule());
			// -- check for Configurable Modules
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
			ote = (ObserverTreeElement)node.getUserObject();
			Module m = ote.getModule();
			if (m instanceof Configurable) {
				buObsItemConfig.setEnabled(true);
				Configuration conf = ((Configurable)m).getConfiguration();
				Util.displayText(tfOptionsObserver, conf.getDescription());
			} else {
				buObsItemConfig.setEnabled(false);
				Util.displayText(tfOptionsObserver, "<not configurable>");
			}
			buObsItemDelete.setEnabled(true);
		} else {
			listViews.setEnabled(false);
			buAddView.setEnabled(false);
			buObsItemConfig.setEnabled(false);
			buObsItemDelete.setEnabled(false);
			Util.displayText(tfOptionsObserver, "<no element selected>");
		}
	} //GEN-LAST:event_treeObsItemsValueChanged

	private void listViewsValueChanged(javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_listViewsValueChanged
		if (evt.getValueIsAdjusting())
			return;
		if (!listViews.isSelectionEmpty()) {
			int i = listViews.getSelectedIndex();
			Util.displayText(taDescrView, views[i].getDescription());
		} else {
			Util.displayText(taDescrView, "<no view selected>");
		}
	} //GEN-LAST:event_listViewsValueChanged

	private void listObserversValueChanged(javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_listObserversValueChanged
		if (evt.getValueIsAdjusting())
			return;
		if (!listObservers.isSelectionEmpty()) {
			int i = listObservers.getSelectedIndex();
			Util.displayText(taDescrObserver, observers[i].getDescription());
		} else {
			Util.displayText(taDescrObserver, "<no observer selected>");
		}
	} //GEN-LAST:event_listObserversValueChanged

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton buAddObserver;
	private javax.swing.JButton buAddView;
	private javax.swing.JButton buObsItemConfig;
	private javax.swing.JButton buObsItemDelete;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel13;
	private javax.swing.JLabel jLabel14;
	private javax.swing.JPanel jPanel12;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JScrollPane jScrollPane15;
	private javax.swing.JScrollPane jScrollPane16;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JScrollPane jScrollPane4;
	private javax.swing.JList listObservers;
	private javax.swing.JList listViews;
	private javax.swing.JTextArea taDescrObserver;
	private javax.swing.JTextArea taDescrView;
	private javax.swing.JTextField tfOptionsObserver;
	private javax.swing.JTree treeObsItems;
	// End of variables declaration//GEN-END:variables

}
