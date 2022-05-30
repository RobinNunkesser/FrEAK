/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.view;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.event.ScheduleEvent;
import freak.core.event.ScheduleEventListener;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Module;
import freak.core.observer.Observer;
import freak.core.stoppingcriterion.StoppingCriterion;
import freak.core.view.AbstractView;
import freak.core.view.View;
import freak.core.view.swingsupport.FreakSwingModel;
import freak.core.view.swingsupport.UpdateManager;
import freak.gui.scheduleeditor.ObserverTreeCellRenderer;
import freak.gui.scheduleeditor.ObserverTreeElement;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Displays information about the observed schedule in a table.
 * @author  Dirk
 */
public class ScheduleView extends AbstractView implements ScheduleEventListener, FreakSwingModel, BatchEventListener {

	private transient SchedulePanel schedulePanel;
	
	// -- 
	private transient JTextField optimumField;
	
	private transient JPanel wrapper;

	/**
	 * Constructs a new <code>ScheduleView</code>
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public ScheduleView(Schedule schedule) {
		super(schedule);
	}

	public Class[] getInputDataTypes() {
		return new Class[] { Schedule.class };
	}

	public JPanel createPanel() {
		schedulePanel = new SchedulePanel(getSchedule());
		wrapper = new JPanel();
		JPanel scroller = new JPanel();
		scroller.setLayout(new BorderLayout());
		
		FlowLayout f = new FlowLayout();
		f.setAlignment(FlowLayout.LEADING);
		wrapper.setLayout(f);
		wrapper.setBackground(Color.white);
		wrapper.add(schedulePanel);
		scroller.add(new JScrollPane(wrapper));
		
		return scroller;
	}

	public String getName() {
		return "Schedule Information Table";
	}

	public String getDescription() {
		return "Displays information about the schedule in a table.";
	}

	public void scheduleEdited(ScheduleEvent evt) {
		synchronized (this) {
			if (schedulePanel != null) {
				schedulePanel = new SchedulePanel(getSchedule());
				wrapper.removeAll();
				wrapper.add(schedulePanel);
			}
		}

		UpdateManager.markDirty(this);
	}

	public void batchStarted(BatchEvent evt) {
		synchronized (this) {
			if (schedulePanel != null) {
				schedulePanel = new SchedulePanel(getSchedule());
				wrapper.removeAll();
				wrapper.add(schedulePanel);
			}
		}

		UpdateManager.markDirty(this);
	}

	public void createEvents() {
		super.createEvents();
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}

	public void flush() {
		if (schedulePanel != null) {
			schedulePanel.repaint();
		}
	}

	/**
	 * @author  nunkesser
	 */
	class SchedulePanel extends JPanel {
		private Schedule schedule;

		private int row = 0;

		/** Used for direct access to the root of the observer-tree. */
		private DefaultMutableTreeNode rootNode;
		/** Used for direct access to the TreeModel of the observer-tree. */
		private DefaultTreeModel treeModel;
		private JTree treeObsItems;

		public SchedulePanel(Schedule schedule) {
			super();
			this.schedule = schedule;

			setLayout(new GridBagLayout());
			setBackground(Color.white);
			setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(), new EmptyBorder(0, 2, 0, 2)));

			GridBagConstraints gridBagConstraints = new GridBagConstraints();

			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.anchor = GridBagConstraints.WEST;

			if (schedule.getMapper() == null) {
				addModule("Search Space", schedule.getGenotypeSearchSpace());
			} else {
				addModule("Phenotype Search Space", schedule.getPhenotypeSearchSpace());
				addModule("Genotype Search Space", schedule.getGenotypeSearchSpace());
			}
			addModule("Fitness Function", schedule.getRealFitnessFunction());
			addModule("Operator Graph", schedule.getOperatorGraph());

			StoppingCriterion[] stoppingCriteria = schedule.getStoppingCriteria();
			for (int i = 0; i < stoppingCriteria.length; i++) {
				addModule("Stopping Criterion", stoppingCriteria[i]);
			}
			
			addModule("Population Model", schedule.getPopulationManager());
			addModule("Initialization Operator", schedule.getInitialization());

			if (schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction) {
				SingleObjectiveFitnessFunction fitness = (SingleObjectiveFitnessFunction)schedule.getFitnessFunction();

				try {
					Double opt = new Double(fitness.getOptimalFitnessValue());

					// -- function offers optimal value
					gridBagConstraints = new GridBagConstraints();
					gridBagConstraints.gridx = 0;
					gridBagConstraints.gridy = row;
					gridBagConstraints.anchor = GridBagConstraints.WEST;
					gridBagConstraints.insets = new Insets((row != 0 ? 10 : 0), 0, 0, 5);
					gridBagConstraints.weightx = 0.1;
					gridBagConstraints.weighty = 0.1;
					gridBagConstraints.fill = GridBagConstraints.BOTH;

					add(new JLabel("Optimum"), gridBagConstraints);

					gridBagConstraints = new GridBagConstraints();
					gridBagConstraints.gridx = 1;
					gridBagConstraints.gridy = row;
					gridBagConstraints.anchor = GridBagConstraints.WEST;
					gridBagConstraints.insets = new Insets((row != 0 ? 10 : 0), 0, 0, 0);
					gridBagConstraints.weightx = 0.1;
					gridBagConstraints.weighty = 0.1;
					gridBagConstraints.fill = GridBagConstraints.BOTH;

					optimumField = new JTextField(opt.toString());
					optimumField.setEditable(false);
					optimumField.setBorder(new EmptyBorder(2, 2, 2, 2));
					optimumField.setBackground(Color.white);
					add(optimumField, gridBagConstraints);
					row++;
				} catch (UnsupportedOperationException e) {}

				// -- lower and upper bound
				try {
					Object val = new Double(fitness.getLowerBound());
					addMiscInfo("Lower Bound", val);
				} catch (UnsupportedOperationException e) {}

				try {
					Object val = new Double(fitness.getUpperBound());
					addMiscInfo("Upper Bound", val);
				} catch (UnsupportedOperationException e) {}
			}

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = row++;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.anchor = GridBagConstraints.WEST;

			initTree();
			treeObsItems.setBorder(new TitledBorder("Observers and Views"));
			add(treeObsItems, gridBagConstraints);
		}

		/**
		 * Adds miscellaneous text in form of "label" "value" to the panel.
		 * 
		 * @param label
		 * @param value
		 */
		private void addMiscInfo(String label, Object value) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = row;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets((row != 0 ? 10 : 0), 0, 0, 5);
			gridBagConstraints.weightx = 0.1;
			gridBagConstraints.weighty = 0.1;
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			add(new JLabel(label), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = row;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets((row != 0 ? 10 : 0), 0, 0, 0);
			gridBagConstraints.weightx = 0.1;
			gridBagConstraints.weighty = 0.1;
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			JTextField valueField = new JTextField(value.toString());
			valueField.setEditable(false);
			valueField.setBorder(new EmptyBorder(2, 2, 2, 2));
			valueField.setBackground(Color.white);
			add(valueField, gridBagConstraints);
			row++;
		}

		private void addModule(String type, Module module) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = row;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets((row != 0 ? 10 : 0), 0, 0, 5);
			gridBagConstraints.weightx = 0.1;
			gridBagConstraints.weighty = 0.1;
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			add(new JLabel(type), gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = row;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets((row != 0 ? 10 : 0), 0, 0, 0);
			gridBagConstraints.weightx = 0.1;
			gridBagConstraints.weighty = 0.1;
			gridBagConstraints.fill = GridBagConstraints.BOTH;

			JTextField moduleNameTextField = new JTextField(module.getName());
			moduleNameTextField.setEditable(false);
			moduleNameTextField.setBorder(new EmptyBorder(2, 2, 2, 2));
			moduleNameTextField.setBackground(Color.white);
			add(moduleNameTextField, gridBagConstraints);

			row++;

			if (module instanceof Configurable) {
				gridBagConstraints = new GridBagConstraints();
				gridBagConstraints.gridx = 1;
				gridBagConstraints.gridy = row;
				gridBagConstraints.anchor = GridBagConstraints.WEST;
				gridBagConstraints.weightx = 0.1;
				gridBagConstraints.weighty = 0.1;
				gridBagConstraints.fill = GridBagConstraints.BOTH;

				JTextField configurationTextField = new JTextField(((Configurable)module).getConfiguration().getDescription());
				configurationTextField.setEditable(false);
				configurationTextField.setBorder(new EmptyBorder(2, 2, 2, 2));
				configurationTextField.setBackground(Color.white);
				add(configurationTextField, gridBagConstraints);

				row++;
			}

		}

		private void initTree() {
			treeObsItems = new JTree();
			treeObsItems.setEditable(false);

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

			for (Iterator iter = getSchedule().getObserverManager().getActiveObservers().iterator(); iter.hasNext();) {
				Observer o = (Observer)iter.next();
				// -- create a treenode
				DefaultMutableTreeNode obsNode = new DefaultMutableTreeNode(new ObserverTreeElement(o));
				// -- add the default "empty" leaf
				obsNode.add(new DefaultMutableTreeNode(ObserverTreeElement.getNoViewTreeElement()));
				// -- insert node into tree
				treeModel.insertNodeInto(obsNode, rootNode, rootNode.getChildCount());
				// -- select newly added node
				TreePath path = new TreePath(obsNode.getPath());
				treeObsItems.setSelectionPath(path);

				for (Iterator views = o.getViews().iterator(); views.hasNext();) {
					addView((View)views.next(), obsNode);
				}
			}
			treeObsItems.setSelectionPath(null);
		}

		private void addView(View v, DefaultMutableTreeNode obsNode) {
			// -- create a treenode
			DefaultMutableTreeNode viewNode = new DefaultMutableTreeNode(new ObserverTreeElement(v));

			// -- remove the NoView-Element if there is one
			if (obsNode.getChildAt(0).toString().equals(ObserverTreeElement.getNoViewTreeElement().toString())) {
				treeModel.removeNodeFromParent((MutableTreeNode)obsNode.getChildAt(0));
			}
			treeModel.insertNodeInto(viewNode, obsNode, obsNode.getChildCount());
			// -- select newly added node
			TreePath path = new TreePath(viewNode.getPath());
			treeObsItems.setSelectionPath(path);
		}

	}

}
