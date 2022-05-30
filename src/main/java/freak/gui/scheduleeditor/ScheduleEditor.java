/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: $Date: 2007/12/11 17:04:01 $
 */

package freak.gui.scheduleeditor;

import freak.core.control.*;
import freak.core.fitness.*;
import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.core.populationmanager.*;
import freak.core.searchspace.*;
import freak.core.stoppingcriterion.*;
import freak.core.util.*;
import freak.gui.*;
import freak.gui.runframe.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * This Dialog allows the user to create a Schedule Object which contains all information about the fitness function, searchspace and so on.
 * @author  Oliver, Michael
 */
public class ScheduleEditor extends javax.swing.JDialog {
	
	private static String RSaveTo="schedule.freak";

	final int NUM_PANELS = 8;

	private AbstractRunFrame runFrame;

	/** a backup of the edited schedule */
	private Schedule scheduleBackup;

	/** The Schedule object that is edited. */
	private Schedule schedule;

	/** Is it a newly created Schedule or did it already start? */
	private boolean newSchedule = true;

	private int phase;
	private ScheduleEditorPanel activePanel = null;
	private List phaseLabels = new ArrayList();
	private ScheduleDependencyChecker scheduleDependencyChecker;
	
	private boolean rMode = false; 	//indicates whether or not ScheduleEditor was launched from R
									//if so, the Click on the Finish Button will pass a reference to
									//the schedule that has just been edited to the ScheduleConfigurator
									//where it can be retrieved from R

	/** 
	 * Creates a new <code>ScheduleCreationDialog</code>. Creates a new <code>Schedule</code>
	 * object if <code>aSchedule</code> was null and initializes most of the
	 * GUI components.
	 */
	public ScheduleEditor(AbstractRunFrame parent, Schedule aSchedule) {
		super(parent);
		
		initComponents();

		this.runFrame = parent;

		setSize(800, 550);
		setLocationRelativeTo(parent);

		initPanelLabels();

		scheduleDependencyChecker = new ScheduleDependencyChecker(this);

		// -- create new Schedule if needed
		if (aSchedule == null) {
			setupNewSchedule();
		} else {
			setupExistingSchedule(aSchedule);
		}

		updateComponents();
	}
	
	public void setRMode(){
		rMode = true;
	}
	
	public void resetRMode(){
		rMode = false;
	}

	/**
	 * Returns the <code>Schedule</code> object this dialog works with.
	 * @return  the <code>Schedule</code>
	 * @uml.property  name="schedule"
	 */
	public Schedule getSchedule() {
		return schedule;
	}

	/**
	 * Returns the <code>ScheduleDependencyChecker</code> this dialog works with.
	 * @return  the <code>ScheduleDependancyChecker</code>
	 * @uml.property  name="scheduleDependencyChecker"
	 */
	public ScheduleDependencyChecker getScheduleDependencyChecker() {
		return scheduleDependencyChecker;
	}

	/**
	 * This method reads out information from the schedule, displays it in the
	 * dialog and modifies the components to reflect the current schedule setup.
	 * This method is usually called by the <code>ScheduleEditorPanels</code>
	 * when they change the configuration of the <code>Schedule</code>.
	 */
	protected void updateComponents() {
		Util.displayText(tfSearchSpace, schedule.getPhenotypeSearchSpace() != null ? schedule.getPhenotypeSearchSpace().getName() : "<no search space selected>");
		Util.displayText(tfGenotypeSearchSpace, schedule.getGenotypeSearchSpace() != null ? schedule.getGenotypeSearchSpace().getName() : "<no mapper selected>");
		Util.displayText(tfFitnessFunction, schedule.getRealFitnessFunction() != null ? schedule.getRealFitnessFunction().getName() : "<no fitness function selected>");
		setupActivePhase();
	}

	private void setupNewSchedule() {
		labelTitle.setText("Create Schedule");
		setTitle("Create a new Schedule");
		schedule = new Schedule();
		scheduleDependencyChecker.setSchedule(schedule);
		ScheduleDependencyChecker.observerWarningAcknowledged = false;
		createDefaultSchedule();
		enterPhase(0);
	}

	private void setupExistingSchedule(Schedule aSchedule) {
		labelTitle.setText("Edit Schedule");
		setTitle("Edit a Schedule");
		scheduleBackup = aSchedule;
		// -- backup the schedule
		try {
			schedule = (Schedule)StreamCopy.copy(aSchedule);
			scheduleDependencyChecker.setSchedule(schedule);
		} catch (NotSerializableException exc) {
			throw new RuntimeException(exc);
		}
		newSchedule = false;
		if (Util.scheduleStarted(schedule))
			enterPhase(phaseLabels.size() - 1);
		else
			enterPhase(0);
	}

	private void addPhaseLabel(JLabel label, JPanel panel) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = GridBagConstraints.RELATIVE;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new java.awt.Insets(0, 5, 8, 5);
		constraints.weightx = 1.0;
		panel.add(label, constraints);
		phaseLabels.add(label);
	}

	private void initPanelLabels() {
		addPhaseLabel(new JLabel((phaseLabels.size() + 1) + ". " + PanelSearchSpace.getDescription()), panelDesignLabels);
		addPhaseLabel(new JLabel((phaseLabels.size() + 1) + ". " + PanelFitnessFunction.getDescription()), panelDesignLabels);
		addPhaseLabel(new JLabel((phaseLabels.size() + 1) + ". " + PanelMapper.getDescription()), panelDesignLabels);
		addPhaseLabel(new JLabel((phaseLabels.size() + 1) + ". " + PanelGraph.getDescription()), panelDesignLabels);
		addPhaseLabel(new JLabel((phaseLabels.size() + 1) + ". " + PanelStoppingCriterion.getDescription()), panelDesignLabels);
		addPhaseLabel(new JLabel((phaseLabels.size() + 1) + ". " + PanelPopulation.getDescription()), panelDesignLabels);
		addPhaseLabel(new JLabel((phaseLabels.size() + 1) + ". " + PanelObservers.getDescription()), panelSimulationLabels);
		addPhaseLabel(new JLabel((phaseLabels.size() + 1) + ". " + PanelBatches.getDescription()), panelSimulationLabels);
	}

	private ScheduleEditorPanel getPanelForPhase(int phase) {
		ScheduleEditorPanel panel = null;
		switch (phase) {
			case 0 :
				panel = new PanelSearchSpace(this);
				break;
			case 1 :
				panel = new PanelFitnessFunction(this);
				break;
			case 2 :
				panel = new PanelMapper(this);
				break;
			case 3 :
				panel = new PanelGraph(this);
				break;
			case 4 :
				panel = new PanelStoppingCriterion(this);
				break;
			case 5 :
				panel = new PanelPopulation(this);
				break;
			case 6 :
				panel = new PanelObservers(this);
				break;
			case 7 :
				panel = new PanelBatches(this);
				break;
		}
		if (panel != null) {
			GridBagConstraints gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new java.awt.Insets(5, 5, 5, 5), 0, 0);
			getContentPane().add(panel, gbc);
		}
		return panel;
	}

	private void setupActivePhase() {
		for (int i = 0; i < phaseLabels.size(); i++) {
			JLabel label = (JLabel)phaseLabels.get(i);
			if (i == phase)
				label.setFont(new Font("Dialog", Font.BOLD, 12));
			else
				label.setFont(new Font("Dialog", Font.PLAIN, 12));
		}

		buNext.setEnabled(phase != phaseLabels.size() - 1 && activePanel.mayAdvance());
		buBack.setEnabled(phase != 0);
		buFinish.setEnabled(scheduleDependencyChecker.isComplete());
	}

	private void enterPhase(int newPhase) {
		if (activePanel == null || (newPhase < phase && !activePanel.stepBackVeto()) || (newPhase > phase && !activePanel.advanceVeto())) {
			ScheduleEditorPanel oldPanel = activePanel;
			activePanel = getPanelForPhase(newPhase);
			activePanel.enter();
			phase = newPhase;
			setupActivePhase();
			if (oldPanel != null) {
				oldPanel.setVisible(false);
				getContentPane().remove(oldPanel);
			}
		}
	}

	private void changePhaseTo(int newPhase) {
		if ((newPhase < phase && activePanel.mayStepBack()) || (newPhase > phase && activePanel.mayAdvance()))
			enterPhase(newPhase);
	}
	
	private void finishEditing() {
		// create default batch list if batch list is empty
		if (schedule.getBatchList().size() == 0) {
			scheduleDependencyChecker.createDefaultBatchList();
		}
	}

	private void createDefaultSchedule() {
		Module m;

		try {
			m = new freak.module.searchspace.BitString(schedule);
			m.testSchedule(schedule);
			m.initialize();
			m.createEvents();
			schedule.setPhenotypeSearchSpace((SearchSpace)m);

			m = new freak.module.fitness.bitstring.OneMax(schedule);
			m.testSchedule(schedule);
			m.initialize();
			m.createEvents();
			schedule.setFitnessFunction((FitnessFunction)m);

			schedule.setStoppingCriteria(new StoppingCriterion[0]);

			m = new freak.module.populationmanager.DefaultPopulationManager(schedule);
			m.testSchedule(schedule);
			m.initialize();
			m.createEvents();
			schedule.setPopulationManager((PopulationManager)m);

			scheduleDependencyChecker.createDefaultOperatorGraph();

			m = new freak.module.operator.initialization.RandomInitialization(schedule.getOperatorGraph());
			m.testSchedule(schedule);
			m.initialize();
			m.createEvents();
			((Initialization)m).hideNameProperty();
			schedule.setInitialization((Initialization)m);
		} catch (UnsupportedEnvironmentException e) {
			throw new RuntimeException("Something is wrong with the default Schedule.", e);
		}
		
		scheduleDependencyChecker.createDefaultBatch();
	}

	private boolean isScheduleSetupCorrect() {
		return scheduleDependencyChecker.isComplete() && scheduleDependencyChecker.isCorrect();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() {//GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;
		
		panelHelp = new javax.swing.JPanel();
		labelTitle = new javax.swing.JLabel();
		jPanel14 = new javax.swing.JPanel();
		jLabel8 = new javax.swing.JLabel();
		tfSearchSpace = new javax.swing.JTextField();
		jLabel7 = new javax.swing.JLabel();
		tfFitnessFunction = new javax.swing.JTextField();
		jLabel1 = new javax.swing.JLabel();
		tfGenotypeSearchSpace = new javax.swing.JTextField();
		panelDesignLabels = new javax.swing.JPanel();
		panelSimulationLabels = new javax.swing.JPanel();
		panelButtons = new javax.swing.JPanel();
		buBack = JButtonFactory.newButton();
		buNext = JButtonFactory.newButton();
		buFinish = JButtonFactory.newButton();
		buHelp = JButtonFactory.newButton();
		buCancel = JButtonFactory.newButton();
		
		getContentPane().setLayout(new java.awt.GridBagLayout());
		
		setTitle("Schedule Creation");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});
		
		panelHelp.setLayout(new java.awt.GridBagLayout());
		
		panelHelp.setMinimumSize(new java.awt.Dimension(230, 400));
		panelHelp.setPreferredSize(new java.awt.Dimension(230, 400));
		labelTitle.setFont(new java.awt.Font("Dialog", 3, 22));
		labelTitle.setText("Schedule Creation");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
		panelHelp.add(labelTitle, gridBagConstraints);
		
		jPanel14.setLayout(new java.awt.GridBagLayout());
		
		jPanel14.setBorder(new javax.swing.border.EtchedBorder());
		jLabel8.setText("Phenotype Search Space:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
		jPanel14.add(jLabel8, gridBagConstraints);
		
		tfSearchSpace.setEditable(false);
		tfSearchSpace.setText("<no search space selected>");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 5);
		jPanel14.add(tfSearchSpace, gridBagConstraints);
		
		jLabel7.setText("Fitness Function:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
		jPanel14.add(jLabel7, gridBagConstraints);
		
		tfFitnessFunction.setEditable(false);
		tfFitnessFunction.setText("<no fitness function selected>");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
		jPanel14.add(tfFitnessFunction, gridBagConstraints);
		
		jLabel1.setText("Genotype Search Space (Mapper)");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
		jPanel14.add(jLabel1, gridBagConstraints);
		
		tfGenotypeSearchSpace.setEditable(false);
		tfGenotypeSearchSpace.setText("<no mapper selected>");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 5);
		jPanel14.add(tfGenotypeSearchSpace, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		panelHelp.add(jPanel14, gridBagConstraints);
		
		panelDesignLabels.setLayout(new java.awt.GridBagLayout());
		
		panelDesignLabels.setBorder(new javax.swing.border.TitledBorder("Design Algorithm"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
		panelHelp.add(panelDesignLabels, gridBagConstraints);
		
		panelSimulationLabels.setLayout(new java.awt.GridBagLayout());
		
		panelSimulationLabels.setBorder(new javax.swing.border.TitledBorder("Prepare Simulation"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.weighty = 0.1;
		panelHelp.add(panelSimulationLabels, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
		getContentPane().add(panelHelp, gridBagConstraints);
		
		panelButtons.setLayout(new java.awt.GridLayout(1, 0, 5, 0));
		
		buBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Back16.gif")));
		buBack.setMnemonic('b');
		buBack.setText("Back");
		buBack.setEnabled(false);
		buBack.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buBackActionPerformed(evt);
			}
		});
		
		panelButtons.add(buBack);
		
		buNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Forward16.gif")));
		buNext.setMnemonic('n');
		buNext.setText("Next");
		buNext.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buNextActionPerformed(evt);
			}
		});
		
		panelButtons.add(buNext);
		
		buFinish.setMnemonic('f');
		buFinish.setText("Finish");
		buFinish.setEnabled(false);
		buFinish.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buFinishActionPerformed(evt);
			}
		});
		
		panelButtons.add(buFinish);
		
		buHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Help16.gif")));
		buHelp.setMnemonic('h');
		buHelp.setText("Help");
		buHelp.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buHelpActionPerformed(evt);
			}
		});
		
		panelButtons.add(buHelp);
		
		buCancel.setText("Cancel");
		buCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buCancelActionPerformed(evt);
			}
		});
		
		panelButtons.add(buCancel);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
		getContentPane().add(panelButtons, gridBagConstraints);
		
		pack();
	}//GEN-END:initComponents

	private void buHelpActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buHelpActionPerformed
		runFrame.showHelpPage(activePanel.getHelpURL());
	} //GEN-LAST:event_buHelpActionPerformed

	private void buFinishActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buFinishActionPerformed
		ok();
	} //GEN-LAST:event_buFinishActionPerformed

	private void buBackActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buBackActionPerformed
		changePhaseTo(phase - 1);
	} //GEN-LAST:event_buBackActionPerformed

	private void buNextActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buNextActionPerformed
		changePhaseTo(phase + 1);
	} //GEN-LAST:event_buNextActionPerformed

	public void ok() {
		if (isScheduleSetupCorrect()) {
			finishEditing();						
			dispose();
			runFrame.editorClosed();
			//pass current Schedule to the ScheduleConfigurator, so that it can be retrieved in R and save it
			if(rMode) {
				freak.rinterface.model.ScheduleConfigurator.setCurrentSchedule(schedule);
				try {
					((RunFrame)runFrame).runControl.toFile().write(new FileOutputStream(new File(RSaveTo)));
				} catch (Exception exc) {
					throw new RuntimeException(exc);
				}
				
				if (freak.Freak.isCircumventR()) {
					System.exit(0);
				}
			}			
		}
	}

	public void cancel() {
		schedule = null;
		runFrame.editorClosed();
		dispose();
	}

	private void buCancelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buCancelActionPerformed
		cancel();
	} //GEN-LAST:event_buCancelActionPerformed

	private void exitForm(java.awt.event.WindowEvent evt) { //GEN-FIRST:event_exitForm
		cancel();
	} //GEN-LAST:event_exitForm

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton buBack;
	private javax.swing.JButton buCancel;
	private javax.swing.JButton buFinish;
	private javax.swing.JButton buHelp;
	private javax.swing.JButton buNext;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JPanel jPanel14;
	private javax.swing.JLabel labelTitle;
	private javax.swing.JPanel panelButtons;
	private javax.swing.JPanel panelDesignLabels;
	private javax.swing.JPanel panelHelp;
	private javax.swing.JPanel panelSimulationLabels;
	private javax.swing.JTextField tfFitnessFunction;
	private javax.swing.JTextField tfGenotypeSearchSpace;
	private javax.swing.JTextField tfSearchSpace;
	// End of variables declaration//GEN-END:variables

	/**
	 * @param saveTo the rSaveTo to set
	 */
	public static void setRSaveTo(String saveTo) {
		RSaveTo = saveTo;
	}

}
