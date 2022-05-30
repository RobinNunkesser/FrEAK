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

import freak.Freak;
import freak.core.control.Schedule;
import freak.core.graph.FreakGraphModel;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.ModuleCollector;
import freak.core.modulesupport.inspector.InspectorVetoException;
import freak.gui.ConfigurationPanel;
import freak.gui.JButtonFactory;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphUndoManager;
import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;

/**
 * Dialog to edit an OperatorGraph.
 * @author  Andrea, Matthias
 */
public abstract class EditorDialogUI extends JDialog implements GraphSelectionListener {
	protected FreakGraphModel graphModel;
	protected Schedule schedule;
	protected ModuleCollector manager;

	protected JGraph graph;

	protected Action delete;
	protected Action zoom;
	protected Action zoomIn;
	protected Action zoomOut;
	protected Action newGraph;
	protected Action save;
	protected Action saveAs;
	protected Action open;
	protected Action close;
	protected Action editParameterControllers;
	protected JButton insertButton;
	protected javax.swing.JTree operatorTree;

	/** The Panel where <code>ParameterController</code> can be edited */
	protected ParameterControllerDialog parameterControllerPanel;

	private EditorDialogUI.EditorPanel editor;

	private ConfigurationPanel displayedConfigurationPanel;

	public EditorDialogUI(Dialog owner, Schedule schedule) {
		super(owner, "Edit Operator Graph", true);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close(null);
			}
		});
		super.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.schedule = schedule;
		graphModel = this.schedule.getFreakGraphModel();
		graphModel.refreshFloatingPorts();

		manager = new ModuleCollector(this.schedule);
		editor = new EditorDialogUI.EditorPanel();
		getContentPane().add(editor, BorderLayout.CENTER);
		setJMenuBar(createMenuBar());
		this.setSize(880, 580);
	}

	/**
	 * @return the GraphModel of the Dialog with changes of the user.
	 */
	public GraphModel getModel() {
		return graph.getModel();
	}

	/**
	 * @return the OperatorGraph of the dialog with changes of the user.
	 */
	public OperatorGraph getOperatorGraph() {
		return graphModel.getOperatorGraph();
	}

	/**
	 * @return A menu bar for this dialog
	 */
	protected JMenuBar createMenuBar() {
		JMenuBar menu = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');

		ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/New16.gif"));
		newGraph = new AbstractAction("New", icon) {
			public void actionPerformed(ActionEvent e) {
				newGraph(e);
			}
		};
		newGraph.putValue(Action.SMALL_ICON, icon);
		newGraph.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
		newGraph.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Freak.getActionModifierKey()));
		newGraph.setEnabled(true);

		fileMenu.add(newGraph);

		fileMenu.addSeparator();

		icon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
		open = new AbstractAction("Open...", icon) {
			public void actionPerformed(ActionEvent e) {
				open(e);
			}
		};
		open.putValue(Action.SMALL_ICON, icon);
		open.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		open.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Freak.getActionModifierKey()));
		open.setEnabled(true);

		fileMenu.add(open);

		icon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Save16.gif"));
		save = new AbstractAction("Save", icon) {
			public void actionPerformed(ActionEvent e) {
				save(e);
			}
		};
		save.putValue(Action.SMALL_ICON, icon);
		save.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		save.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Freak.getActionModifierKey()));
		save.setEnabled(true);

		fileMenu.add(save);

		icon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/SaveAs16.gif"));
		saveAs = new AbstractAction("Save As...", icon) {
			public void actionPerformed(ActionEvent e) {
				saveAs(e);
			}
		};
		saveAs.putValue(Action.SMALL_ICON, icon);
		saveAs.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		saveAs.setEnabled(true);

		fileMenu.add(saveAs);

		fileMenu.addSeparator();

		close = new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				close(e);
			}
		};
		close.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		close.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, Freak.getActionModifierKey()));
		close.setEnabled(true);

		fileMenu.add(close);
		menu.add(fileMenu);

		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		editMenu.add(delete);

		editMenu.addSeparator();
		editMenu.add(editParameterControllers);
		menu.add(editMenu);

		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		viewMenu.add(zoom);
		viewMenu.add(zoomIn);
		viewMenu.add(zoomOut);
		menu.add(viewMenu);

		return menu;
	}

	protected abstract void newGraph(ActionEvent e);

	protected abstract void open(ActionEvent e);

	protected abstract void save(ActionEvent e);

	protected abstract void saveAs(ActionEvent e);

	/**
	 * Hides the EditorDialog. Subclasses may override this method to perform
	 * syntax or semantics checks of the operator graph.
	 * @param e
	 */
	protected void close(ActionEvent e) {
		super.setVisible(false);
	}

	/**
	 * @param e
	 */
	protected abstract void insert(ActionEvent e);

	protected abstract void connect(Port source, Port target);

	protected abstract void delete(ActionEvent e);

	protected abstract void editParameterControllers(ActionEvent e);

	protected void zoom(ActionEvent e) {
		graph.setScale(1.0);
	}

	protected void zoomIn(ActionEvent e) {
		graph.setScale(2 * graph.getScale());
	}

	protected void zoomOut(ActionEvent e) {
		graph.setScale(graph.getScale() / 2.0);
	}

	/**
		* @return a DefaultTreeModel containing all operators that can be inserted
		*  into this graph. The Entries are of the type <code>ModuleInfo</code> (for modules) or String (for general operators).
		*/
	protected abstract DefaultTreeModel createOperatorTreeModel();

	/**
	 * @see com.jgraph.event.GraphSelectionListener#valueChanged(com.jgraph.event.GraphSelectionEvent)
	 */
	public abstract void valueChanged(GraphSelectionEvent e);

	/**
	 * A Panel to edit an operator graph.
	 * @author  Andrea
	 */
	protected class EditorPanel extends javax.swing.JPanel {

		// Undo Manager
		protected GraphUndoManager undoManager;

		/**
		 */
		public EditorPanel() {
			initComponents();

			//FreakGraphModel graphModel = new FreakGraphModel(operatorGraph);

			graph = new JGraph(graphModel) {
				/**
				 * Constructs a PortView view for the specified object.
				 */
				protected PortView createPortView(Object p, CellMapper cm) {
					return new FreakPortView(p, this, cm);
				}

			};

			graph.setBendable(false);
			graph.setEditable(false);
			graph.setConnectable(true);
			graph.setDisconnectable(false);
			graph.setDisconnectOnMove(true);
			graph.setTolerance(6);
			graph.setGridEnabled(false);
			graph.setGridVisible(false);
			graph.setPortsVisible(true);

			innerSplitPane.setLeftComponent(new JScrollPane(graph));

			operatorTree.setModel(createOperatorTreeModel());

			operatorTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			undoManager = new GraphUndoManager() {
				public void undoableEditHappened(UndoableEditEvent e) {
					super.undoableEditHappened(e);
				}
			};
			graph.getSelectionModel().addGraphSelectionListener(EditorDialogUI.this);
			graph.getModel().addUndoableEditListener(undoManager);

			addActions();
		}

		private void addActions() {
			ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif"));
			delete = new AbstractAction("Delete", icon) {
				public void actionPerformed(ActionEvent e) {
					delete(e);
				}
			};
			delete.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
			delete.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			editToolbar.add(delete);
			delete.setEnabled(false);

			icon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Zoom16.gif"));
			zoom = new AbstractAction("Zoom", icon) {
				public void actionPerformed(ActionEvent e) {
					zoom(e);
				}
			};
			zoom.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, Freak.getActionModifierKey()));
			zoom.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Z));
			zoomToolbar.add(zoom);

			icon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ZoomIn16.gif"));
			zoomIn = new AbstractAction("Zoom In", icon) {
				public void actionPerformed(ActionEvent e) {
					zoomIn(e);
				}
			};
			zoomIn.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, Freak.getActionModifierKey()));
			zoomIn.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
			zoomToolbar.add(zoomIn);

			icon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ZoomOut16.gif"));
			zoomOut = new AbstractAction("Zoom Out", icon) {
				public void actionPerformed(ActionEvent e) {
					zoomOut(e);
				}
			};
			zoomOut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Freak.getActionModifierKey()));
			zoomOut.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
			zoomToolbar.add(zoomOut);

			editParameterControllers = new AbstractAction("Edit Parameter Controllers", null) {
				public void actionPerformed(ActionEvent e) {
					editParameterControllers(e);
				}
			};
			editParameterControllers.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
			//editParameterControllers.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Freak.getActionModifierKey()));
			editParameterControllers.setEnabled(true);
			freakToolbar.add(editParameterControllers);

		}

		/** This method is called from within the constructor to
		 * initialize the form.
		 * WARNING: Do NOT modify this code. The content of this method is
		 * always regenerated by the Form Editor.
		 */
		private void initComponents() { //GEN-BEGIN:initComponents
			toolbarPanel = new javax.swing.JPanel();
			editToolbar = new javax.swing.JToolBar();
			zoomToolbar = new javax.swing.JToolBar();
			freakToolbar = new JToolBar();
			outerSplitPane = new javax.swing.JSplitPane();
			innerSplitPane = new JSplitPane();
			jPanel1 = new javax.swing.JPanel();
			jScrollPane1 = new javax.swing.JScrollPane();
			operatorTree = new javax.swing.JTree();
			jPanel5 = new javax.swing.JPanel();
			insertButton = JButtonFactory.newButton();
			inspectorScrollPane = new JScrollPane();
			inspectorScrollPane.setPreferredSize(new Dimension(240, 200));
			inspectorScrollPane.setMinimumSize(new Dimension(240, 200));

			setLayout(new java.awt.BorderLayout());

			toolbarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

			toolbarPanel.add(editToolbar);
			toolbarPanel.add(zoomToolbar);
			toolbarPanel.add(freakToolbar);

			add(toolbarPanel, java.awt.BorderLayout.NORTH);

			jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

			jScrollPane1.setViewportView(operatorTree);

			jPanel1.add(jScrollPane1);

			insertButton.setText("Insert Selected");
			insertButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insert(e);
				}
			});
			insertButton.setEnabled(false);
			jPanel5.add(insertButton);

			jPanel1.add(jPanel5);
			jPanel1.setPreferredSize(new Dimension(200, 200));

			outerSplitPane.setLeftComponent(jPanel1);
			outerSplitPane.setRightComponent(innerSplitPane);
			outerSplitPane.setOneTouchExpandable(true);

			innerSplitPane.setRightComponent(inspectorScrollPane);

			innerSplitPane.setOneTouchExpandable(true);
			innerSplitPane.setResizeWeight(1);

			add(outerSplitPane, BorderLayout.CENTER);

		} //GEN-END:initComponents

		// Variables declaration - do not modify//GEN-BEGIN:variables
		private javax.swing.JToolBar editToolbar;
		private javax.swing.JScrollPane jScrollPane1;
		private javax.swing.JSplitPane outerSplitPane;
		private JSplitPane innerSplitPane;
		JScrollPane inspectorScrollPane;
		private javax.swing.JPanel jPanel5;
		private javax.swing.JToolBar zoomToolbar;
		private javax.swing.JPanel toolbarPanel;
		private javax.swing.JPanel jPanel1;
		private javax.swing.JToolBar freakToolbar;
		// End of variables declaration//GEN-END:variables
	}

	/**
	 * Displays the specified ConfigurationPanel on the right side of the 
	 * GraphEditor.
	 * If a ConfigurationPanel is already displayed at the time this method is 
	 * called, then <code>finishEditing</code> is called to the old 
	 * ConfigurationPanel. If this call yields an 
	 * <code>InspectorVetoException</code>, the old panel is left in place so 
	 * the user can change the invalid properties in the old panel. 
	 */
	protected void displayToRight(ConfigurationPanel panelToDisplay) {
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
		
		displayedConfigurationPanel = panelToDisplay;

		editor.inspectorScrollPane.setViewportView(panelToDisplay);
	}
	
	/**
	 * @return  the displayedConfigurationPanel
	 * @uml.property  name="displayedConfigurationPanel"
	 */
	ConfigurationPanel getDisplayedConfigurationPanel() {
		return displayedConfigurationPanel;
	}
	
	/**
	 * Resets the currently displayed ConfigurationPanel to <code>null</code> 
	 * without calling <code>finishEditing</code> to the old panel.
	 * This method is called when the current graph is replaced by another 
	 * graph. 
	 */
	void clearDisplayedConfigurationPanel() {
		displayedConfigurationPanel = null;
	}
	
}
