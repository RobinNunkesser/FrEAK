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

import freak.core.control.Batch;
import freak.core.control.BatchList;
import freak.core.fitness.FitnessFunction;
import freak.core.graph.Initialization;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.searchspace.SearchSpace;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author  Oliver, Dirk
 */
public class PanelBatches extends ScheduleEditorPanel {

	private BatchTable batchTable;
	private BatchList batchList;

	/** Creates new JPanel */
	public PanelBatches(ScheduleEditor scheduleEditor) {
		super(scheduleEditor);
		initComponents();

		batchList = schedule.getBatchList();

		initTable();
		initSpinner();
	}

	public static String getDescription() {
		return "Create Batches of Runs";
	}

	public String getHelpURL() {
		return "node4.html#SECTION00452000000000000000";
	}

	public boolean mayAdvance() {
		return batchList.size() >= 1;
	}

	public void enter() {
		super.enter();
		
		// create default batch list if batch list is empty
		if (schedule.getBatchList().size() == 0) {
			scheduleDependencyChecker.createDefaultBatchList();
			parse();
		}
	}

	private void initTable() {
		batchTable = new BatchTable(schedule);
		// -- auto-select current/next batch
		batchTable.changeSelection(Math.max(schedule.getCurrentBatch() - 1, 0), 0, false, false);
		parse();

		scrollBatchTable.setViewportView(batchTable);
		// -- make the table respond to selections
		ListSelectionModel rowSM = batchTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				tableBatchesValueChanged(e);
			}
		});
	}

	public void initSpinner() {
		spinnerNumberOfRuns.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				spinnerNumberOfRunsStateChanged(e);
			}
		});
	}

	private void parse() {
		int selectedRow = batchTable.getSelectedRow();

		buCopyBatch.setEnabled(selectedRow >= 0 && selectedRow < batchList.size());
		buDeleteBatch.setEnabled(!isBatchFixed(selectedRow));
		buBatchMoveUp.setEnabled(!isBatchFixed(selectedRow) && !isBatchFixed(selectedRow - 1));
		buBatchMoveDown.setEnabled(!isBatchFixed(selectedRow) && !isBatchFixed(selectedRow + 1));

		if (selectedRow == -1 || selectedRow >= batchList.size()) {
			// -- set all textfields to null-values
			Util.displayText(tfSearchSpace, "<no batch selected>");
			Util.displayText(tfFitnessInstance, "<no batch selected>");
			Util.displayText(tfPopulation, "<no batch selected>");
			spinnerNumberOfRuns.setValue(new Integer(0));
			spinnerNumberOfRuns.setEnabled(false);
			buConfigSearchSpace.setEnabled(false);
			buConfigFitnessFunction.setEnabled(false);
			buConfigInitializationOperator.setEnabled(false);
		} else {
			Batch selectedBatch = batchList.get(selectedRow);
			
			Util.displayText(tfSearchSpace, (String)batchTable.getValueAt(selectedRow, 1));
			Util.displayText(tfFitnessInstance, (String)batchTable.getValueAt(selectedRow, 2));
			Util.displayText(tfPopulation, (String)batchTable.getValueAt(selectedRow, 3));

			spinnerNumberOfRuns.setValue(batchTable.getValueAt(selectedRow, 0));
			spinnerNumberOfRuns.setEnabled(!selectedBatch.isFinished());

			if (selectedBatch.isStarted()) {
				buConfigSearchSpace.setEnabled(false);
				buConfigFitnessFunction.setEnabled(false);
				buConfigInitializationOperator.setEnabled(false);
			} else {
				buConfigSearchSpace.setEnabled(schedule.getPhenotypeSearchSpace() instanceof Configurable);
				buConfigFitnessFunction.setEnabled(schedule.getRealFitnessFunction() instanceof Configurable);
				buConfigInitializationOperator.setEnabled(schedule.getInitialization() instanceof Configurable);
			}
		}
	}

	/**
	 * Returns <code>true</code> if the batch with the specified index in the
	 * batch list can be moved or deleted. Nonexistent batches are always
	 * considered fixed.
	 */
	private boolean isBatchFixed(int batchIndex) {
		if (batchIndex < 0 || batchIndex >= batchList.size()) return true;	
		return schedule.getBatchList().get(batchIndex).isStarted();
	}

	private void configureBatchModule(Configurable module, int row) {
		// -- fetch the batch
		Batch batch = batchList.get(row);
		
		boolean batchLoadingSuccessful = true;
		
		// create a batch from the current configurations as a backup
		Batch backupBatch = schedule.createBatchFromCurrentConfigurations();
		
		// try to load the configurations for the selected batch 
		try {
			// temporarily set configurations of the batch containing the module
			// to be configured
			batch.applyAllConfigurations();
		
			// call testSchedule and initialize to implement the new 
			// configurations
			for (Iterator iter = schedule.getAllModules().iterator(); iter.hasNext();) {
				Module mod = (Module)iter.next();
				mod.testSchedule(schedule);
				mod.initialize();
			}
		
		} catch (UnsupportedEnvironmentException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(scheduleEditor, "The current batch is invalid and cannot be run.", "Invalid Batch", JOptionPane.ERROR_MESSAGE);
			processInvalidBatch(row);
			batchLoadingSuccessful = false;
		}
		
		// try to configure the module
		if (batchLoadingSuccessful) try {
			// -- now configure it
			Util.configureModule(scheduleEditor, module, schedule);
		
			// test if the schedule is still able to run with the configured 
			// module
			for (Iterator iter = schedule.getAllModules().iterator(); iter.hasNext();) {
				Module mod = (Module)iter.next();
				mod.testSchedule(schedule);
				mod.initialize();
			}
			
			// create new batch from updated configurations
			// (This is necessary in case the configured module modified 
			// configurations of other modules on its own.)
			Batch newBatch = schedule.createBatchFromCurrentConfigurations();

			// re-apply configurations in case the configured module changed 
			// something
			newBatch.applyAllConfigurations();
			newBatch = schedule.createBatchFromCurrentConfigurations();

			newBatch.setRuns(batch.getRuns());
			// set updated batch
			batchList.set(row, newBatch);

		} catch (UnsupportedEnvironmentException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(scheduleEditor, "The configured module cannot be used within the current batch.\nThe batch is removed from the schedule.", "Invalid Module Configuration", JOptionPane.ERROR_MESSAGE);
			processInvalidBatch(row);
		}
		
		// try to switch back to the original configurations
		try {
			// fire batchTable events
			((BatchTableModel)batchTable.getModel()).fireTableCellUpdated(row, 1);
			((BatchTableModel)batchTable.getModel()).fireTableCellUpdated(row, 2);
			((BatchTableModel)batchTable.getModel()).fireTableCellUpdated(row, 3);
		
			// restore the schedule's original configuration
			backupBatch.applyAllConfigurations();

			// call testSchedule and initialize again to revert the changes that 
			// were made
			for (Iterator iter = schedule.getAllModules().iterator(); iter.hasNext();) {
				Module mod = (Module)iter.next();
				mod.testSchedule(schedule);
				mod.initialize();
			}
			
		} catch (UnsupportedEnvironmentException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(scheduleEditor, "The current batch is invalid and thus removed from the schedule.", "Invalid Batch", JOptionPane.ERROR_MESSAGE);
			processInvalidBatch(row);
		}
		
		parse();
	}
	
	private void checkBatch(int row) {
		// -- fetch the batch
		Batch batch = batchList.get(row);

		// create a batch from the current configurations as a backup
		Batch backupBatch = schedule.createBatchFromCurrentConfigurations();
		
		// try to load the configurations for the selected batch 
		try {
			// temporarily set configurations of the batch containing the module
			// to be configured
			batch.applyAllConfigurations();
		
			// call testSchedule and initialize to test the new 
			// configurations
			for (Iterator iter = schedule.getAllModules().iterator(); iter.hasNext();) {
				Module mod = (Module)iter.next();
				mod.testSchedule(schedule);
				mod.initialize();
			}
		
		} catch (UnsupportedEnvironmentException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(scheduleEditor, "The current batch is invalid and cannot be run.", "Invalid Batch", JOptionPane.ERROR_MESSAGE);
			processInvalidBatch(row);
		}
		
		// try to switch back to the original configurations
		try {
			// restore the schedule's original configuration
			backupBatch.applyAllConfigurations();

			// call testSchedule and initialize again to revert the changes that 
			// were made
			for (Iterator iter = schedule.getAllModules().iterator(); iter.hasNext();) {
				Module mod = (Module)iter.next();
				mod.testSchedule(schedule);
				mod.initialize();
			}
			
		} catch (UnsupportedEnvironmentException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(scheduleEditor, "The current batch is invalid and thus removed from the schedule.", "Invalid Batch", JOptionPane.ERROR_MESSAGE);
			processInvalidBatch(row);
		}
	}
	
	private void processInvalidBatch(int row) {
		// remove batch from schedule
		batchTable.deleteRow(row);
		batchTable.changeSelection((row == 0 ? 0 : row - 1), 0, false, false);

		// update Next and Finish buttons in the schedule editor
		scheduleEditor.updateComponents();
	}
	
	private void tableBatchesValueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
		parse();
	}

	private void spinnerNumberOfRunsStateChanged(ChangeEvent evt) {
		if (batchTable.getSelectionModel().isSelectionEmpty()) {
			spinnerNumberOfRuns.setValue(new Integer(0));
		} else {
			int newValue = ((Integer)spinnerNumberOfRuns.getValue()).intValue();
			int lowerBound = 1;
			int batchNum = batchTable.getSelectedRow();
			
			Batch selectedBatch = schedule.getBatchList().get(batchNum); 
			
			if (selectedBatch.isFinished()) {
				Batch batch = batchList.get(batchNum);
				spinnerNumberOfRuns.setValue(new Integer(batch.getRuns()));
				return;
			} else if (selectedBatch.isStarted())
				lowerBound = schedule.getCurrentRun();
				
			if (newValue < lowerBound) {
				spinnerNumberOfRuns.setValue(new Integer(lowerBound));
				newValue = lowerBound;
			}
			Batch b = batchList.get(batchTable.getSelectedRow());
			b.setRuns(newValue);
			// update number of runs in default batch
			schedule.getDefaultBatch().setRuns(newValue);
			
			((BatchTableModel)batchTable.getModel()).fireTableCellUpdated(batchNum, 0);
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
		spinnerNumberOfRuns = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        tfFitnessInstance = new javax.swing.JTextField();
        buConfigFitnessFunction = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        tfPopulation = new javax.swing.JTextField();
        buConfigInitializationOperator = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        tfSearchSpace = new javax.swing.JTextField();
        buConfigSearchSpace = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        scrollBatchTable = new javax.swing.JScrollPane();
        buBatchMoveUp = new javax.swing.JButton();
        buBatchMoveDown = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        buNewBatch = new javax.swing.JButton();
        buCopyBatch = new javax.swing.JButton();
        buDeleteBatch = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder("Create Batches of Runs"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(new javax.swing.border.TitledBorder("Batch properties"));
        jLabel9.setText("Number of Runs:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel1.add(jLabel9, gridBagConstraints);

		spinnerNumberOfRuns.setMinimumSize(new java.awt.Dimension(64, 24));
		spinnerNumberOfRuns.setPreferredSize(new java.awt.Dimension(64, 24));
		spinnerNumberOfRuns.setEnabled(false);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
		jPanel1.add(spinnerNumberOfRuns, gridBagConstraints);
        
        jLabel10.setText("Fitness Function:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel1.add(jLabel10, gridBagConstraints);

        tfFitnessInstance.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(tfFitnessInstance, gridBagConstraints);

        buConfigFitnessFunction.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
        buConfigFitnessFunction.setText("Configure...");
        buConfigFitnessFunction.setEnabled(false);
        buConfigFitnessFunction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buConfigFitnessFunctionActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel1.add(buConfigFitnessFunction, gridBagConstraints);

        jLabel12.setText("Initial Population:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel1.add(jLabel12, gridBagConstraints);

        tfPopulation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(tfPopulation, gridBagConstraints);

        buConfigInitializationOperator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
        buConfigInitializationOperator.setText("Configure...");
        buConfigInitializationOperator.setEnabled(false);
        buConfigInitializationOperator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buConfigInitializationOperatorActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel1.add(buConfigInitializationOperator, gridBagConstraints);

        jLabel1.setText("Search Space:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel1.add(jLabel1, gridBagConstraints);

        tfSearchSpace.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(tfSearchSpace, gridBagConstraints);

        buConfigSearchSpace.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
        buConfigSearchSpace.setText("Configure...");
        buConfigSearchSpace.setEnabled(false);
        buConfigSearchSpace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buConfigSearchSpaceActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel1.add(buConfigSearchSpace, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(jPanel1, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        jPanel6.setBorder(new javax.swing.border.TitledBorder("Planned batches"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanel6.add(scrollBatchTable, gridBagConstraints);

        buBatchMoveUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif")));
        buBatchMoveUp.setEnabled(false);
        buBatchMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buBatchMoveUpActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 0.1;
        jPanel6.add(buBatchMoveUp, gridBagConstraints);

        buBatchMoveDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif")));
        buBatchMoveDown.setEnabled(false);
        buBatchMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buBatchMoveDownActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 0.1;
        jPanel6.add(buBatchMoveDown, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        buNewBatch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/New16.gif")));
        buNewBatch.setMnemonic('e');
        buNewBatch.setText("New Batch");
        buNewBatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buNewBatchActionPerformed(evt);
            }
        });

        jPanel2.add(buNewBatch);

        buCopyBatch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Copy16.gif")));
        buCopyBatch.setText("Copy Batch");
        buCopyBatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buCopyBatchActionPerformed(evt);
            }
        });

        jPanel2.add(buCopyBatch);

        buDeleteBatch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif")));
        buDeleteBatch.setText("Remove Batch");
        buDeleteBatch.setEnabled(false);
        buDeleteBatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buDeleteBatchActionPerformed(evt);
            }
        });

        jPanel2.add(buDeleteBatch);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel6.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(jPanel6, gridBagConstraints);

    }//GEN-END:initComponents

    private void buCopyBatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buCopyBatchActionPerformed
		int selectedRow = batchTable.getSelectedRow();

		if (selectedRow >= 0 && selectedRow < batchList.size()) {
			Batch selectedBatch = batchList.get(batchTable.getSelectedRow());
			// copy selected batch
			Batch newBatch = selectedBatch.copy();
		
			batchTable.appendBatch(newBatch, false);
			batchTable.changeSelection(batchList.size() - 1, 0, false, false);
		
			checkBatch(batchList.size() - 1);

			// update Next and Finish buttons in the schedule editor
			scheduleEditor.updateComponents();
		}
    }//GEN-LAST:event_buCopyBatchActionPerformed

	private void buConfigSearchSpaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buConfigSearchSpaceActionPerformed
		if (!batchTable.getSelectionModel().isSelectionEmpty()) {
			SearchSpace ss = schedule.getPhenotypeSearchSpace();
			if (ss == null)
				throw new NullPointerException("Searchspace is null. This should not be!");
			if (ss instanceof Configurable) {
				configureBatchModule((Configurable)ss, batchTable.getSelectedRow());
			} else
				JOptionPane.showMessageDialog(this, "This search space is not configurable.");
		}
	}//GEN-LAST:event_buConfigSearchSpaceActionPerformed

	private void buConfigFitnessFunctionActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buConfigFitnessFunctionActionPerformed
		if (!batchTable.getSelectionModel().isSelectionEmpty()) {
			FitnessFunction fitness = schedule.getRealFitnessFunction();
			if (fitness == null)
				throw new NullPointerException("Fitness function is null. This should not be!");
			if (fitness instanceof Configurable) {
				configureBatchModule((Configurable)fitness, batchTable.getSelectedRow());
			} else
				JOptionPane.showMessageDialog(this, "This fitness function is not configurable.");
		}
	} //GEN-LAST:event_buConfigFitnessFunctionActionPerformed

	private void buConfigInitializationOperatorActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buConfigInitializationOperatorActionPerformed
		if (!batchTable.getSelectionModel().isSelectionEmpty()) {
			Initialization ig = schedule.getInitialization();
			if (ig == null)
				throw new NullPointerException("SCD: initialization operator is null. This should not be!");
			if (ig instanceof Configurable) {
				configureBatchModule(ig, batchTable.getSelectedRow());
			} else
				JOptionPane.showMessageDialog(this, "This initialization operator is not configurable.");
		}
	} //GEN-LAST:event_buConfigInitializationOperatorActionPerformed

	private void buBatchMoveDownActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buBatchMoveDownActionPerformed
		int row = batchTable.getSelectedRow();
		// -- make sure the row isn't the last row and it isn't the currently running batch
		if (!isBatchFixed(row) && !isBatchFixed(row + 1)) {
			Batch batch = batchList.get(row);
			batchList.set(row, batchList.get(row + 1));
			batchList.set(row + 1, batch);
			batchTable.changeSelection(row + 1, 0, false, false);
		} else
			JOptionPane.showMessageDialog(this, "Cannot exchange batch with a running or finished batch.");
	} //GEN-LAST:event_buBatchMoveDownActionPerformed

	private void buBatchMoveUpActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buBatchMoveUpActionPerformed
		int row = batchTable.getSelectedRow();
		// -- make sure the row isn't the last row and it isn't the currently running batch
		if (!isBatchFixed(row) && !isBatchFixed(row - 1)) {
			Batch batch = batchList.get(row);
			batchList.set(row, batchList.get(row - 1));
			batchList.set(row - 1, batch);
			batchTable.changeSelection(row - 1, 0, false, false);
		} else
			JOptionPane.showMessageDialog(this, "Cannot exchange batch with a running or finished batch.");
	} //GEN-LAST:event_buBatchMoveUpActionPerformed

	private void buDeleteBatchActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buDeleteBatchActionPerformed
		if (!batchTable.getSelectionModel().isSelectionEmpty()) {
			int row = batchTable.getSelectedRow();
			if (!isBatchFixed(row)) {
				batchTable.deleteRow(row);
				batchTable.changeSelection((row == 0 ? 0 : row - 1), 0, false, false);

				// update Next and Finish buttons in the schedule editor
				scheduleEditor.updateComponents();
			} else
				JOptionPane.showMessageDialog(this, "Cannot remove this batch.");
		}
	} //GEN-LAST:event_buDeleteBatchActionPerformed

	private void buNewBatchActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_buNewBatchActionPerformed
		// insert a copy of the default batch
		Batch newBatch = schedule.getDefaultBatch().copy();
		
		batchTable.appendBatch(newBatch, true);
		batchTable.changeSelection(batchList.size() - 1, 0, false, false);
		
		checkBatch(batchList.size() - 1);

		// update Next and Finish buttons in the schedule editor
		scheduleEditor.updateComponents();
	} //GEN-LAST:event_buNewBatchActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buBatchMoveDown;
    private javax.swing.JButton buBatchMoveUp;
    private javax.swing.JButton buConfigFitnessFunction;
    private javax.swing.JButton buConfigInitializationOperator;
    private javax.swing.JButton buConfigSearchSpace;
    private javax.swing.JButton buCopyBatch;
    private javax.swing.JButton buDeleteBatch;
    private javax.swing.JButton buNewBatch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane scrollBatchTable;
	private javax.swing.JSpinner spinnerNumberOfRuns;
    private javax.swing.JTextField tfFitnessInstance;
    private javax.swing.JTextField tfPopulation;
    private javax.swing.JTextField tfSearchSpace;
    // End of variables declaration//GEN-END:variables

}
