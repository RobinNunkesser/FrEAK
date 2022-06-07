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

import freak.core.control.ScheduleInterface;
import freak.core.graph.*;
import freak.core.graph.Port;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.ModuleInfo;
import freak.core.modulesupport.inspector.InspectorVetoException;
import freak.gui.ConfigurationPanel;
import freak.gui.runframe.SingleExtensionFileFilter;
import freak.module.support.OperatorGraphFile;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.graph.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author  Andrea, Matthias, Heiko
 */
public class EditorDialog extends EditorDialogUI {

	private GraphModelListener graphModelListener;

	private static SingleExtensionFileFilter filter = new SingleExtensionFileFilter(".fop", "Freak Operator Graph");

	private File lastFile;
	
	/**
	 * @param owner
	 * @param schedule
	 */
	public EditorDialog(Dialog owner, ScheduleInterface schedule) {
		super(owner, schedule);

		insertButton.setEnabled(false);
		operatorTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)operatorTree.getLastSelectedPathComponent();
				if (node == null || !node.isLeaf()) {
					insertButton.setEnabled(false);
				} else {
					insertButton.setEnabled(true);
				}
			}
		});
		operatorTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = operatorTree.getRowForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					if(e.getClickCount() == 2) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)operatorTree.getLastSelectedPathComponent();
						if (node != null && node.isLeaf()) {
							insert(null);
							e.consume();
						}
					}
				}
			}
		});
		TreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if (leaf) {
					Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
					if (userObject instanceof ModuleInfo) {
						String description = ((ModuleInfo)userObject).getDescription();
						setToolTipText(description);
					}
				} else {
					setToolTipText(null); //no tool tip
				}

				return this;
			}
		};
		operatorTree.setCellRenderer(renderer);
		ToolTipManager.sharedInstance().registerComponent(operatorTree);

		graphModelListener = new FreakGraphModelListener(schedule);

		graph.setMarqueeHandler(new FreakMarqueeHandler(graph, this));
		graph.getModel().addGraphModelListener(graphModelListener);

		//((FreakGraphModel)graph.getModel()).adjustLayout(graph);
		GraphLayoutCache layoutCache = graph.getGraphLayoutCache();
		CellView[] allViews = layoutCache.getAllDescendants(layoutCache.getRoots());
		graph.getGraphLayoutCache().refresh(allViews, false);
	}

	/** Insert a new node into the Graph. May only be called if 
	 * operatorTree.getLastSelectedPathComponent returns a leaf node 
	 * (i.e. the returned node is not null and a leaf of the tree). */
	protected void insert(ActionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)operatorTree.getLastSelectedPathComponent();
		if (node == null || !node.isLeaf()) {
			throw new IllegalStateException("No leaf selected");
		}

		Object userObject = node.getUserObject();
		Operator operator;

		if (userObject instanceof ModuleInfo) {
			ModuleInfo info = (ModuleInfo)userObject;
			operator = manager.newOperator(info);
		} else {
			throw new RuntimeException("invalid operator, cannot be inserted: " + userObject);
		}

		operator.createEvents();

		GraphCell cell = new OperatorGraphCell(operator);
		Map map = cell.getAttributes();

		// Snap the Point to the Grid
		Point point = graph.snap(new Point(10, 10));

		// Fit vertex size to text
		int height = graph.getGraphics().getFontMetrics().getHeight();
		int width = graph.getGraphics().getFontMetrics().stringWidth(operator.toString());
		Dimension size = new Dimension(width + 10, height + 8);

		GraphConstants.setBounds(map, new Rectangle(point, size));
		GraphConstants.setOpaque(map, true);

		Hashtable attributes = new Hashtable();
		attributes.put(cell, map);

		Object[] toInsert = new Object[] { cell };
		graph.getModel().insert(toInsert, attributes, null, null, null);
		graph.getModel().toFront(toInsert);
		graph.setSelectionCells(toInsert);
	}

	protected void connect(org.jgraph.graph.Port source, org.jgraph.graph.Port target) {
		// Connections that will be inserted into the Model
		ConnectionSet cs = new ConnectionSet();
		// Construct Edge with no label
		FreakEdge edge = new FreakEdge((FreakPort)source, (FreakPort)target);
		// Create Connection between source and target using edge
		cs.connect(edge, source, target);
		// Create a Map thath holds the attributes for the edge
		Map map = GraphConstants.createMap();
		GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
		// Construct a Map from cells to Maps (for insert)
		Hashtable attributes = new Hashtable();
		// Associate the Edge with its Attributes
		attributes.put(edge, map);
		// Insert the Edge and its Attributes
		graph.getGraphLayoutCache().insert(new Object[] { edge }, attributes, cs, null, null);
	}

	/**
	 * Deletes the selected elements in the graph. Operators for which 
	 * isOperatorRemovable in OperatorGraph returns false are not removed.
	 */
	protected void delete(ActionEvent e) {
		if (!graph.isSelectionEmpty()) {
			Object[] cells = graph.getSelectionCells();
			cells = graph.getDescendants(cells);
			List toRemove = new Vector(Arrays.asList(cells));
			List notToRemove = new Vector();
			for (Iterator iter = toRemove.iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof OperatorGraphCell) {
					OperatorGraphCell cell = (OperatorGraphCell)element;
					if (!(graphModel.getOperatorGraph().isOperatorRemovable(cell.getOperator()))) {
						notToRemove.addAll(Arrays.asList(graph.getDescendants(new Object[] { cell })));
					}
				}
			}
			toRemove.removeAll(notToRemove);
			
			//check for event dependencies
			// TODO matthias consider extending the method isOperatorRemovable instead
			for (Iterator iter = toRemove.iterator(); iter.hasNext();) {
				Object tmp_element = iter.next();
				Object element = null;
				if (tmp_element instanceof FreakPort) {
					element = ((FreakPort)tmp_element).getPort();
				} else if (tmp_element instanceof OperatorGraphCell) {
					element = ((OperatorGraphCell)tmp_element).getOperator();					
				} else {
					element = tmp_element;
				}
				if (schedule.getEventController().isEventSource(element)) {
					String message = "There are components which need to get events from one of the operators you tried to delete.\n" 
					               + "Do you want to continue?";
					int answer = JOptionPane.showConfirmDialog(this, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

					if (answer == JOptionPane.NO_OPTION) return;
				}
				if (tmp_element instanceof OperatorGraphCell) {
					OperatorGraphCell cell = (OperatorGraphCell)tmp_element;
					
					for (Iterator ports = cell.getPortIterator(); ports.hasNext();) {
						FreakPort port = (FreakPort)ports.next();
						for (Iterator edges = port.edges(); edges.hasNext();) {
							FreakEdge edge = (FreakEdge)edges.next();

							FreakPort source = edge.getSourcePort();
							FreakPort target = edge.getTargetPort();
							Port sourcePort = (Port)source.getUserObject();
							Port targetPort = (Port)target.getUserObject();

							if (sourcePort.getOperator() instanceof HasFloatingNumberOfOutPorts) {
								if (schedule.getEventController().isEventSource(sourcePort)) {
									JOptionPane.showMessageDialog(this, "There are components which need to get events from one of the operators you tried to delete.");
									return;									
								}
							}
							if (targetPort.getOperator() instanceof HasFloatingNumberOfInPorts) {
								if (schedule.getEventController().isEventSource(targetPort)) {
									JOptionPane.showMessageDialog(this, "There are components which need to get events from one of the operators you tried to delete.");
									return;
								}
							}
						}
					}
				}	
			}
			
			graph.getModel().remove(toRemove.toArray());
		}

		GraphLayoutCache layoutCache = graph.getGraphLayoutCache();
		CellView[] allViews = layoutCache.getAllDescendants(layoutCache.getRoots());
		graph.getGraphLayoutCache().refresh(allViews, false);
	}

	protected void editParameterControllers(ActionEvent e) {
		ParameterControllerDialog pcDialog = new ParameterControllerDialog(this, schedule);
		pcDialog.setVisible(true);
	}

	public void valueChanged(GraphSelectionEvent e) {
		// Update button states based on current selection
		boolean enabled = !graph.isSelectionEmpty();
		Object[] selected = graph.getSelectionCells();

		// only one Operator selected => display inspector
		if (selected.length == 1 && selected[0] instanceof OperatorGraphCell) {
			displayObjectInspector(((OperatorGraphCell)selected[0]).getOperator());
		}

		// update buttons
		boolean deletableCells = false;
		for (int i = 0; i < selected.length; i++) {
			Object object = selected[i];
			if (object instanceof OperatorGraphCell) {
				if (graphModel.getOperatorGraph().isOperatorRemovable(((OperatorGraphCell)object).getOperator())) {
					deletableCells = true;
					break;
				}
			} else if (object instanceof Edge) {
				deletableCells = true;
				break;
			}
		}

		delete.setEnabled(enabled && deletableCells);
	}

	public void displayObjectInspector(Module m) {
		// -- create new ConfigurationPanel for Object and show it
		ConfigurationPanel configurationPanel = new ConfigurationPanel(m, schedule.getEventController(), schedule.getPossibleEventSources(), schedule.getBatchList());
		configurationPanel.setPreferredSize(new Dimension(120, 20));
		configurationPanel.setMinimumSize(new Dimension(120, 20));
		// -- show panel
		displayToRight(configurationPanel);
	}
	
	/**
	 * @author  nunkesser
	 */
	private class FreakGraphModelListener implements GraphModelListener {
		ScheduleInterface schedule;

		public FreakGraphModelListener(ScheduleInterface schedule) {
			this.schedule = schedule;
		}

		public void graphChanged(GraphModelEvent e) {
			e.getChange();

			Object[] inserted = e.getChange().getInserted();
			Object[] removed = e.getChange().getRemoved();
			Object[] changed = e.getChange().getChanged();

			if (inserted != null) {
				insert(inserted);
			}

			if (removed != null) {
				remove(removed);
			}

			if (changed != null) {
				change(changed);
			}
		}

		protected void remove(Object[] toRemove) {

			for (int i = 0; i < toRemove.length; i++) {
				Object object = toRemove[i];

				if (object instanceof FreakEdge) {
					FreakEdge edge = (FreakEdge)object;

					FreakPort source = edge.getSourcePort();
					FreakPort target = edge.getTargetPort();
					freak.core.graph.Port sourcePort = (Port)source.getUserObject();
					freak.core.graph.Port targetPort = (Port)target.getUserObject();

					graphModel.getOperatorGraph().disconnect(sourcePort, targetPort);
					if (sourcePort.getOperator() instanceof HasFloatingNumberOfOutPorts && sourcePort.getNumberOfPartners() == 0) {
						((HasFloatingNumberOfOutPorts)sourcePort.getOperator()).removeOutPort(sourcePort.getNumber());
					}
					if (targetPort.getOperator() instanceof HasFloatingNumberOfInPorts && targetPort.getNumberOfPartners() == 0) {
						((HasFloatingNumberOfInPorts)targetPort.getOperator()).removeInPort(targetPort.getNumber());
					}
				} else if (object instanceof OperatorGraphCell) {
					OperatorGraphCell cell = (OperatorGraphCell)object;
					Operator operator = cell.getOperator();
					//					for (Iterator iter = cell.getPortIterator(); iter.hasNext();) {
					//						FreakPort port = (FreakPort)iter.next();
					//						for (Iterator edgeIter = port.edges(); edgeIter.hasNext();) {
					//							Object edge = iter.next();
					//							System.out.println("have to remove edge " + edge + " of class " + edge.getClass());							
					//						}
					//					}
					graphModel.getOperatorGraph().removeOperator(operator);
				} else if (object instanceof FreakPort) {
				} else {
					throw new RuntimeException("don't know how to remove " + object + " of class " + object.getClass());
				}
			}
		}

		protected void insert(Object[] toInsert) {
			for (int i = 0; i < toInsert.length; i++) {
				Object object = toInsert[i];

				if (object instanceof Edge) {
					Edge edge = (Edge)object;
					FreakPort source = (FreakPort)edge.getSource();
					FreakPort target = (FreakPort)edge.getTarget();

					freak.core.graph.Port sourcePort = source.getPort();
					freak.core.graph.Port targetPort = target.getPort();

					try {
						if (sourcePort != null && targetPort != null) {
							if (sourcePort instanceof OutPort && targetPort instanceof InPort) {
								graphModel.getOperatorGraph().connect(sourcePort, targetPort);
							} else {
								throw new IllegalArgumentException("source port must be outport and target port must be inport");
							}
						} else {
							throw new IllegalArgumentException("source and target ports must not be null");
						}
					} catch (PortConnectException e) {
						e.printStackTrace();
					}

					if (sourcePort.getOperator() instanceof HasFloatingNumberOfOutPorts && sourcePort.getNumberOfPartners() == 1) {
						((HasFloatingNumberOfOutPorts)sourcePort.getOperator()).addOutPort();
					}
					if (targetPort.getOperator() instanceof HasFloatingNumberOfInPorts && targetPort.getNumberOfPartners() == 1) {
						((HasFloatingNumberOfInPorts)targetPort.getOperator()).addInPort();
					}
				} else if (object instanceof OperatorGraphCell) {
					OperatorGraphCell cell = (OperatorGraphCell)object;
					Operator operator = cell.getOperator();
					graphModel.getOperatorGraph().addOperator(operator);
				} else {
					System.out.println("don't know how to insert " + object + " of class " + object.getClass());
				}
			}
		}

		protected void change(Object[] changed) {
		}
	}

	protected DefaultTreeModel createOperatorTreeModel() {
		DefaultTreeModel modules = manager.getTreeModelOfOperators(schedule.getGenotypeSearchSpace());

		return modules;
	}

	/* (non-Javadoc)
	 * @see freak.gui.graph.EditorDialogUI#open(java.awt.event.ActionEvent)
	 */
	protected void open(ActionEvent e) {
		Preferences pref = Preferences.userNodeForPackage(EditorDialog.class);
		String graphDirectory = pref.get("GraphDirectory", null);
		
		JFileChooser c = new JFileChooser();
		if (graphDirectory != null) c.setCurrentDirectory(new File(graphDirectory));
		c.setFileFilter(filter);
		int accepted = c.showOpenDialog(this);
		if (accepted != JFileChooser.APPROVE_OPTION)
			return;

		File f = c.getSelectedFile();
		try {
			OperatorGraphFile ogFile = OperatorGraphFile.read(new FileInputStream(f));
			FreakGraphModel model = ogFile.generateGraph(schedule);
			graphModel.getOperatorGraph().removeFromEventController();

			graph.getModel().removeGraphModelListener(graphModelListener);

			graphModel = model;
			graph.setModel(model);
			graph.getModel().addGraphModelListener(graphModelListener);
			schedule.setGraphModel(model);

			graph.stopEditing();
			graph.clearSelection();

			GraphLayoutCache layoutCache = graph.getGraphLayoutCache();
			CellView[] allViews = layoutCache.getAllDescendants(layoutCache.getRoots());
			graph.getGraphLayoutCache().refresh(allViews, false);
			lastFile = f;
			pref.put("GraphDirectory", f.getParent());
			clearDisplayedConfigurationPanel();
			displayObjectInspector(null);
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(this, "Failed: " + exc.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
			newGraph(e);
		}
	}

	private boolean doSave(File f) {
		FreakGraphModel model = (FreakGraphModel)graph.getModel();

		try {
			OperatorGraphFile ogFile = new OperatorGraphFile(model);
			ogFile.write(new FileOutputStream(f));

			// model.write(new FileOutputStream(f));
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(this, "Failed: " + exc.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see freak.gui.graph.EditorDialogUI#save(java.awt.event.ActionEvent)
	 */
	protected void save(ActionEvent e) {
		if (lastFile == null) {
			saveAs(e);
		} else {
			doSave(lastFile);
		}

	}

	/* (non-Javadoc)
	 * @see freak.gui.graph.EditorDialogUI#saveAs(java.awt.event.ActionEvent)
	 */
	protected void saveAs(ActionEvent e) {
		Preferences pref = Preferences.userNodeForPackage(EditorDialog.class);
		String graphDirectory = pref.get("GraphDirectory", null);
		
		JFileChooser c = new JFileChooser();
		if (graphDirectory != null) c.setCurrentDirectory(new File(graphDirectory));
		c.setFileFilter(filter);

		int accepted = c.showSaveDialog(this);
		if (accepted != JFileChooser.APPROVE_OPTION) return;

		// ask if an existing file is to be replaced
		if (c.getSelectedFile().exists()) {
			Object[] options = { "OK", "Cancel" };
			int answer = JOptionPane.showOptionDialog(null, "Replace existing file " + c.getSelectedFile() + "?", "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

			if (answer != JOptionPane.OK_OPTION) return;
		} 
		
		File f = c.getSelectedFile().getAbsoluteFile();
		f = filter.fixExtension(f);

		if (doSave(f)) {
			lastFile = f;
			pref.put("GraphDirectory", f.getParent());
		}
	}

	/* (non-Javadoc)
	 * @see freak.gui.graph.EditorDialogUI#saveAs(java.awt.event.ActionEvent)
	 */
	protected void close(ActionEvent e) {
		// finish currently displayed ConfigurationPanel
		try {
			ConfigurationPanel configurationPanel = getDisplayedConfigurationPanel();
			if (configurationPanel != null) {
				configurationPanel.finishEditing();
			}
		} catch (InspectorVetoException ex) {
			String message = "The properties of module " + getDisplayedConfigurationPanel().getConfiguredModule().getName() + " are invalid.\n" + ex.getMessage(); 
			JOptionPane.showMessageDialog(this, message, "Invalid Property Settings", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try {
			graphModel.getOperatorGraph().checkSyntax();
		} catch (GraphSyntaxException gse) {
			String[] options = new String[] { "Close", "Cancel" };
			int result = JOptionPane.showOptionDialog(this, "You won't be able to run the simulation.\nClose anyway?", "Invalid Graph Syntax", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (result == JOptionPane.NO_OPTION) {
				//((FreakGraphModel)graph.getModel()).refreshFloatingPorts();
				return;
			}
		}
		graph.getModel().removeGraphModelListener(graphModelListener);
		super.close(e);
	}

	/* (non-Javadoc)
	 * @see freak.gui.graph.EditorDialogUI#newGraph(java.awt.event.ActionEvent)
	 */
	protected void newGraph(ActionEvent e) {
		FreakGraphModel model = new FreakGraphModel(schedule);
		model.getOperatorGraph().createEvents();
		model.refreshFloatingPorts();
		graphModel.getOperatorGraph().removeFromEventController();

		graph.getModel().removeGraphModelListener(graphModelListener);

		graphModel = model;
		graph.setModel(model);
		graph.getModel().addGraphModelListener(graphModelListener);
		schedule.setGraphModel(model);

		graph.stopEditing();
		graph.clearSelection();

		GraphLayoutCache layoutCache = graph.getGraphLayoutCache();
		CellView[] allViews = layoutCache.getAllDescendants(layoutCache.getRoots());
		graph.getGraphLayoutCache().refresh(allViews, false);
		lastFile = null;
		clearDisplayedConfigurationPanel();
		displayObjectInspector(null);
	}

}
