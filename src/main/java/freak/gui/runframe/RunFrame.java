/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 07/11/2007
 */

package freak.gui.runframe;

import freak.core.control.*;
import freak.core.modulesupport.*;
import freak.core.observer.Observer;
import freak.core.view.*;
import freak.core.view.swingsupport.*;
import freak.gui.*;
import freak.gui.gpas.GPASDialog;
import freak.gui.scheduleeditor.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * @author  Stefan, Matthias
 */
public class RunFrame extends RunFrameUI implements StateListener {
	private HelpWindow helpWindow;
	private ScheduleEditor scheduleEditor;
	private boolean editingNewSchedule;
	private GPASDialog gpasDialog;
	
	public RunControl runControl = new RunControl(this);
	private LoadSaveManager loadSaveManager = new LoadSaveManager(this);
		
	private Actions.Action lastSent;
	private Schedule displayedSchedule;
	
	private InfoPanelSpinnerModel batchSpinnerModel = new InfoPanelSpinnerModel();
	private InfoPanelSpinnerModel runSpinnerModel = new InfoPanelSpinnerModel();
	private InfoPanelSpinnerModel generationSpinnerModel = new InfoPanelSpinnerModel();
	private FreakSliderModel generationSliderModel = new FreakSliderModel();
	private FreakLabelModel searchSpaceModel = new FreakLabelModel();
	private FreakLabelModel fitnessFunctionModel = new FreakLabelModel();
	
	
	public RunFrame() {
		
		setViews();
		addActions();
		
		modeNoSchedule();
		
		// caution, causes synchroneousFeedback
		runControl.setSpeedLimit(10);
	}
	
	public void setScheduleEditor(ScheduleEditor schedEd){
		//method is needed for the R-Connection to be able to use the ScheduleEditor from R
		scheduleEditor = schedEd;
//		System.out.println("ScheduleEditor for RunFrame has been set.");
	}

	
	private void setViews() {
		generationSliderModel.setMinimum(jSliderGeneration.getMinimum());
		generationSliderModel.setMaximum(jSliderGeneration.getMaximum());
		generationSliderModel.setValue(jSliderGeneration.getValue());
		generationSliderModel.setAutoTicks(true);
		searchSpaceModel.setText(jLabelSearchSpace.getText());
		fitnessFunctionModel.setText(jLabelFitnessFunction.getText());
		
		batchSpinnerModel.setView(jSpinnerBatch);
		runSpinnerModel.setView(jSpinnerRun);
		generationSpinnerModel.setView(jSpinnerGeneration);
		
		generationSliderModel.setView(jSliderGeneration);
		searchSpaceModel.setView(jLabelSearchSpace);
		fitnessFunctionModel.setView(jLabelFitnessFunction);	
	}
	
	private void addActions() {
		jMenuItemNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				newClicked();
			}
		});
		jMenuItemOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openClicked();
			}
		});
		jMenuItemSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				saveClicked();
			}
		});
		jMenuItemSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				saveAsClicked();
			}
		});
		jMenuItemExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				quitClicked();
			}
		});
		jMenuItemEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				editClicked();
			}
		});
		jMenuItemStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				startClicked();
			}
		});
		jMenuItemSuspend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				suspendClicked();
			}
		});
		jMenuItemSkipToStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				skipToStartClicked();
			}
		});
		jMenuItemSkipBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				skipBackClicked();
			}
		});
		jMenuItemSkipForward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				skipForwardClicked();
			}
		});
		jMenuItemSkipToEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				skipToEndClicked();
			}
		});
		jMenuItemGoTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				goToClicked();
			}
		});	
		jMenuItemTile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				tileClicked();
			}
		});
		jMenuItemCascade.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cascadeClicked();
			}
		});
		jMenuItemMinimize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				minimizeAllClicked();
			}
		});
		jMenuItemRestore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				restoreAllClicked();
			}
		});
		jMenuItemClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				closeAllClicked();
			}
		});
		jMenuItemHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				helpClicked();
			}
		});
		jMenuItemAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				aboutClicked();
			}
		});
		jMenuItemConfigureView.setAction(configureViewAction);
		
		jButtonStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				startClicked();
			}
		});
		jButtonSuspend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				suspendClicked();
			}
		});
		jButtonSkipToStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				skipToStartClicked();
			}
		});	
		jButtonSkipBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				skipBackClicked();
			}
		});	
		jButtonSkipForward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				skipForwardClicked();
			}
		});	
		jButtonSkipToEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				skipToEndClicked();
			}
		});
		
		jButtonQuickStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				quickStartClicked();
			}
		});
		
		batchSpinnerModel.setListener(new InfoPanelSpinnerModel.InfoPanelSpinnerModelListener() {
			public void absoluteChangeRequest(int newValue) {
				batchAbsoluteChangeRequest(newValue);
			}
			public void relativeChangeRequest(int change) {
				batchRelativeChangeRequest(change);
			}
		});
		runSpinnerModel.setListener(new InfoPanelSpinnerModel.InfoPanelSpinnerModelListener() {
			public void absoluteChangeRequest(int newValue) {
				runAbsoluteChangeRequest(newValue);
			}
			public void relativeChangeRequest(int change) {
				runRelativeChangeRequest(change);
			}
		});
		generationSpinnerModel.setListener(new InfoPanelSpinnerModel.InfoPanelSpinnerModelListener() {
			public void absoluteChangeRequest(int newValue) {
				generationAbsoluteChangeRequest(newValue);
			}
			public void relativeChangeRequest(int change) {
				generationRelativeChangeRequest(change);
			}
		});
		jSliderGeneration.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				generationSliderClicked();
			}
		});
		speedLimitCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				speedCheckBoxClicked();
			}
		});
		jTextFieldSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				speedTextFieldClicked();
			}
		});
		jTextFieldSpeed.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}
			public void focusLost(FocusEvent e) {
				speedTextFieldClicked();
			}
		});
		jSliderSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				speedSliderClicked();
			}
		});
	}
	
	
// MODES ***********************************************************************	

	private void modeNoSchedule() {
		enableMode(true, false, false, false, false);
		enableWindowMode(false);
	}
	
	private void modeRunning() {
		enableMode(false, false, false, true, true);
	}
	
	private void modeSuspended() {
		enableMode(true, true, true, true, false);
	}
	
	private void modeEdit() {
		enableMode(true, true, false, false, false);
	}

	private void enableMode(boolean newOpen, boolean saveEdit, boolean config, boolean controls, boolean blink) {
		jButtonStart.setEnabled(controls);
		jButtonSuspend.setEnabled(controls);
		jButtonSkipToStart.setEnabled(controls);
		jButtonSkipBack.setEnabled(controls);
		jButtonSkipForward.setEnabled(controls);
		jButtonSkipToEnd.setEnabled(controls);
		jSliderGeneration.setEnabled(controls);
		speedLimitCheckBox.setEnabled(controls);
		jSliderSpeed.setEnabled(controls && speedLimitCheckBox.isSelected());
		jTextFieldSpeed.setEnabled(controls && speedLimitCheckBox.isSelected());
		jSpinnerBatch.setEnabled(controls);
		jSpinnerRun.setEnabled(controls);
		jSpinnerGeneration.setEnabled(controls);
		
		jButtonQuickStart.setEnabled(newOpen);
		jMenuItemNew.setEnabled(newOpen);
		jMenuItemOpen.setEnabled(newOpen);
		jMenuItemSave.setEnabled(saveEdit);
		jMenuItemSaveAs.setEnabled(saveEdit);
		jMenuItemEdit.setEnabled(saveEdit);
		jMenuItemStart.setEnabled(controls);
		jMenuItemSuspend.setEnabled(controls);
		jMenuItemSkipToStart.setEnabled(controls);
		jMenuItemSkipBack.setEnabled(controls);
		jMenuItemSkipForward.setEnabled(controls);
		jMenuItemSkipToEnd.setEnabled(controls);
		jMenuItemGoTo.setEnabled(controls);
		configureViewAction.setEnabledFromRunControl(config);
		
		blinkingR.setBlink(blink);
	}   

	private void enableWindowMode(boolean windows) {
		jMenuItemTile.setEnabled(windows);
		jMenuItemCascade.setEnabled(windows);
		jMenuItemMinimize.setEnabled(windows);
		jMenuItemClose.setEnabled(windows);
		jMenuItemRestore.setEnabled(windows);
	}

// Actions ***********************************************************************

	private void newClicked() {
		if (scheduleEditor != null) {
			if (editingNewSchedule) {
				return;
			} else {
				scheduleEditor.cancel();
			}
		}
		
		scheduleEditor = new ScheduleEditor(RunFrame.this, null);
		scheduleEditor.setVisible(true);
		if (runControl.getSchedule() != null) {
			modeEdit();
		}
		editingNewSchedule = true;
	}
	
	private void editClicked() {
		if (scheduleEditor != null) {
			if (editingNewSchedule) {
				scheduleEditor.cancel();
			} else {
				return;
			}
		}
		
		scheduleEditor = new ScheduleEditor(RunFrame.this, runControl.getSchedule());
		scheduleEditor.setVisible(true);
		modeEdit();
		editingNewSchedule = false;
	}
	
	private void openClicked() {
		if (scheduleEditor != null) {
			scheduleEditor.cancel();
		}
		FreakFile file = loadSaveManager.load();
		if (file != null) {
			runControl.fromFile(file);
		}
	}
		
	private void saveClicked() {
		if (scheduleEditor != null) {
			scheduleEditor.cancel();
		}
		loadSaveManager.save(runControl.toFile());
	}
	
	private void saveAsClicked() {
		if (scheduleEditor != null) {
			scheduleEditor.cancel();
		}
		loadSaveManager.saveAs(runControl.toFile());
	}
	
	public void editorClosed() {
		Schedule schedule = scheduleEditor.getSchedule();
		scheduleEditor = null;
		if (schedule != null) {
			schedule.modulesEdited(); 
			if (editingNewSchedule) {
				loadSaveManager.newObject();
				runControl.setNewSchedule(schedule);
			} else {
				runControl.scheduleEdited(schedule);
			}
		} else {
			if (runControl.getSchedule() != null) {
				modeSuspended();
			}
		}
	}
	
	private void quitClicked() {
		System.exit(0);
	}

	private void startClicked() {
		requestAction(new Actions.StartAction());
	}

	private void suspendClicked() {
		requestAction(new Actions.SuspendAction());
	}

	private void skipToStartClicked() {
		requestAction(new Actions.SeekToStartAction());
	}
	
	private void skipBackClicked() {
		requestAction(new Actions.stepBackAction());
	}
	
	private void skipForwardClicked() {
		requestAction(new Actions.StepForwardAction());
	}
	
	private void skipToEndClicked() {
		requestAction(new Actions.SeekToReplayEndAction());
	}

	private void quickStartClicked() {
		if (gpasDialog != null) {
			gpasDialog.cancel();
		}
		gpasDialog = new GPASDialog(this);
		gpasDialog.setVisible(true);
	}

	private void goToClicked() {
		int batch = ((Integer)jSpinnerBatch.getValue()).intValue();
		int run = ((Integer)jSpinnerRun.getValue()).intValue();
		int generation = ((Integer)jSpinnerGeneration.getValue()).intValue();
		GenerationIndex aboutNow = new GenerationIndex(batch, run, generation);
		
		GoToDialog dialog = new GoToDialog(this, aboutNow);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		dialog.dispose();
		
		GenerationIndex target = dialog.goTo;
		if (target != null) {
			requestAction(new Actions.SeekToTargetAction(target));
		}
	}
	
	private void batchAbsoluteChangeRequest(int newValue) {
		int run = Math.max(1, ((Integer)jSpinnerRun.getValue()).intValue());
		int generation = Math.max(1, ((Integer)jSpinnerGeneration.getValue()).intValue());
			
		if (newValue <= 0) {
			requestAction(new Actions.SeekToTargetAction(GenerationIndex.START));
		} else {
			requestAction(new Actions.SeekToTargetAction(new GenerationIndex(newValue, run, generation)));
		}
	}
	
	private void batchRelativeChangeRequest(int change) {		
		if (change == -1) {
			requestAction(new Actions.SeekToLastBatchAction());
		} else if (change ==  +1) {
			requestAction(new Actions.SeekToNextBatchAction());
		}
	}
	
	private void runAbsoluteChangeRequest(int newValue) {
		int batch = ((Integer)jSpinnerBatch.getValue()).intValue();
		int generation = ((Integer)jSpinnerGeneration.getValue()).intValue();
		
		if (batch > 0 && newValue > 0) {
			requestAction(new Actions.SeekToTargetAction(new GenerationIndex(batch, newValue, generation)));
		}
	}
	
	private void runRelativeChangeRequest(int change) {
		if (change == -1) {
			requestAction(new Actions.SeekToLastRunAction());
		} else if (change ==  +1) {
			requestAction(new Actions.SeekToNextRunAction());
		}
	}
	
	private void generationAbsoluteChangeRequest(int newValue) {
		int batch = ((Integer)jSpinnerBatch.getValue()).intValue();
		int run = ((Integer)jSpinnerRun.getValue()).intValue();
		
		if (batch > 0 && newValue > 0) {
			requestAction(new Actions.SeekToTargetAction(new GenerationIndex(batch, run, newValue)));
		}
	}
	
	private void generationRelativeChangeRequest(int change) {
		if (change == -1) {
			requestAction(new Actions.SeekToLastGenerationAction());
		} else if (change ==  +1) {
			requestAction(new Actions.SeekToNextGenerationAction());
		}
	}
	
	private boolean firstClick = true;
	private int batch;
	private int run;
	private void generationSliderClicked() {
		if (generationSliderModel.isProgrammaticalChange()) return;
		
		if (firstClick) {
			requestAction(new Actions.StartSeekSequenceAction());
			batch = ((Integer)jSpinnerBatch.getValue()).intValue();
			run = ((Integer)jSpinnerRun.getValue()).intValue();
			firstClick = false;
		} else if (!jSliderGeneration.getValueIsAdjusting()) {
			requestAction(new Actions.EndSeekSequenceAction());
			firstClick = true;
		}
		
		GenerationIndex target = new GenerationIndex(batch, run, jSliderGeneration.getValue());
		requestAction(new Actions.SeekToTargetAction(target));
	}


	private static NumberFormat speedFormat;
	private boolean ignoreSlider;
	private double speed = 10;
	static {
		speedFormat = NumberFormat.getNumberInstance();
		speedFormat.setMinimumFractionDigits(2);
		speedFormat.setMaximumFractionDigits(2);
	}
	private void speedCheckBoxClicked() {
		boolean clicked = speedLimitCheckBox.isSelected();
		jSliderSpeed.setEnabled(clicked);
		jTextFieldSpeed.setEnabled(clicked);
		if (clicked) {
			requestAction(new Actions.SetSpeedAction(speed));
		} else {
			requestAction(new Actions.SetSpeedAction(Double.POSITIVE_INFINITY));
		}
	}

	private void speedSliderClicked() {
		if (ignoreSlider) return;
		speed = sliderToSpeed(jSliderSpeed.getValue());
		jTextFieldSpeed.setText(speedFormat.format(speed));
		requestAction(new Actions.SetSpeedAction(speed));
	}

	private void speedTextFieldClicked() {
		String speedText = jTextFieldSpeed.getText();
		try {
			speed = speedFormat.parse(speedText).doubleValue();
			if (speed <= 0) throw new ParseException(null, 0);
			jTextFieldSpeed.setText(speedFormat.format(speed));
			ignoreSlider = true;
			jSliderSpeed.setValue(speedToSlider(speed));
			ignoreSlider = false;
			requestAction(new Actions.SetSpeedAction(speed));
		} catch (ParseException exc) {
			jTextFieldSpeed.setText(speedFormat.format(speed));
		}
	}

	private double sliderToSpeed(int slider) {
		return (Math.pow(10, slider / 100.0));
	}
	
	private int speedToSlider(double speed) {
		int slider = (int)Math.round(100 * Math.log(speed) / Math.log(10));
		if (slider < -100) return -100;
		if (slider > 500) return 500;
		return slider;
	}


	private void requestAction(Actions.Action action) {
		lastSent = action;
		
		runControl.request(action);
		modeRunning();
	}


// FEEDBACK ********************************************************************

	public void simulationCompleted(final Actions.Action lastProcessed) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (lastSent != lastProcessed) return;
				
				if (runControl.getSchedule() == null) {
					modeNoSchedule();
				} else {
					modeSuspended();
				}
				UpdateManager.flushAll();
			}
		});
	}

	public void simulationException(final Exception exc) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(RunFrame.this, exc, "Exception in Simulation", JOptionPane.ERROR_MESSAGE);
			}
		});
		exc.printStackTrace();
	}
	
	public void terminated(Actions.Action lastProcessedBeforeTermination) {
	}

	public void synchroneousFeedback(Schedule activeSchedule, Replay replay) {
		if (activeSchedule != displayedSchedule) {
			createInternalFrames(activeSchedule);
			updateLabels(activeSchedule);
			displayedSchedule = activeSchedule;
		} 
		
		updateTimeIndexComponents(activeSchedule, replay);
		if (activeSchedule != null) {
			modeSuspended();
			// TODO Stefan not Thread-Safe
			blinkingR.setVisible(isReplayMode(activeSchedule, replay));
		} else {
			modeNoSchedule();
			// TODO Stefan not Thread-Safe
			blinkingR.setVisible(false);
		}
		UpdateManager.flushAll();
	}


	public void asynchroneousFeedback(Schedule activeSchedule, Replay replay) {
		if (activeSchedule != displayedSchedule) {
			invokeAndWaitCreateInternalFrames(activeSchedule);
			updateLabels(activeSchedule);
			displayedSchedule = activeSchedule;
		}
		
		// TODO Stefan not Thread-Safe and slow!
		blinkingR.setVisible(isReplayMode(activeSchedule, replay));
		updateTimeIndexComponents(activeSchedule, replay);
	}
	
	private boolean isReplayMode(Schedule schedule, Replay replay) {
		if (schedule == null) return false;
		return schedule.getCurrentTimeIndex().isBefore(replay.getLastPoint());
	}
	
	private void updateLabels(Schedule schedule) {
		if (schedule == null) return;
		
		synchronized(searchSpaceModel) {
			searchSpaceModel.setText(schedule.getPhenotypeSearchSpace().getName());
		}
		UpdateManager.markDirty(searchSpaceModel);
		
		synchronized(fitnessFunctionModel) {
			fitnessFunctionModel.setText(schedule.getRealFitnessFunction().getName());
		}
		UpdateManager.markDirty(fitnessFunctionModel);
	}
	
	private void updateTimeIndexComponents(Schedule schedule, Replay replay) {
		if (schedule == null) return;
		
		synchronized (batchSpinnerModel) {
			batchSpinnerModel.setSimulationValue(schedule.getCurrentBatch());
		}
		UpdateManager.markDirty(batchSpinnerModel);
		
		synchronized (runSpinnerModel) {
			runSpinnerModel.setSimulationValue(schedule.getCurrentRun());
		}
		UpdateManager.markDirty(runSpinnerModel);
		
		
		GenerationIndex now = schedule.getCurrentTimeIndex();
		int currentGeneration = now.generation;
		int minimum;
		int maximum;
		if (now.batch == 0) {
			minimum = 0;
			maximum = 0;
		} else {
			minimum = 1;
			maximum = replay.getLastPointInRun(now.toRunIndex()).generation;
		}
		
		synchronized (generationSpinnerModel) {
			generationSpinnerModel.setSimulationValue(currentGeneration);
		}
		UpdateManager.markDirty(generationSpinnerModel);
		
		synchronized (generationSliderModel) {			
			generationSliderModel.setMinimum(minimum);
			generationSliderModel.setMaximum(maximum);
			generationSliderModel.setValue(currentGeneration);
		}
		UpdateManager.markDirty(generationSliderModel);
	}

// HELP ************************************************************************

	public void helpClicked() {
		showHelpPage(HelpWindow.INDEX_PAGE);
	}

	public void showHelpPage(String page) {
		if (helpWindow == null) {
			helpWindow = new HelpWindow(this);
			helpWindow.pack();
			helpWindow.setLocationRelativeTo(this);
		}

		helpWindow.goToPage(page);
		helpWindow.setVisible(true);
	}
	
	public void aboutClicked() {
		AboutDialog d = new AboutDialog(RunFrame.this, true);
		d.setLocationRelativeTo(RunFrame.this);
		d.setVisible(true);
	}

	
// Internal Frames *************************************************************	
	private HashMap frameIDs = new HashMap();
	private ConfigureViewAction configureViewAction = new ConfigureViewAction();
	
	private void tileClicked() {
		desktop.tile();
	}		
	private void cascadeClicked() {
		desktop.cascade();
	}		
	private void minimizeAllClicked() {
		desktop.minimizeAll();
	}		
	private void restoreAllClicked() {
		desktop.restoreAll();
	}		
	private void closeAllClicked() {
		desktop.closeAll();
	}		
	
	private void invokeAndWaitCreateInternalFrames(final Schedule schedule) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					createInternalFrames(schedule);
				}
			});
		} catch (InterruptedException exc) {
			throw new RuntimeException(exc);
		} catch (InvocationTargetException exc) {
			throw new RuntimeException(exc);
		}
	}
	
	private void createInternalFrames(Schedule newSchedule) {
		if (newSchedule == null) {
			desktop.getDelegate().removeAll();
			desktop.getDelegate().repaint(); //swing bug
			frameIDs.clear();
			configureViewAction.setActiveView(null);
			return;
		}
		
		Iterator observerIterator = (newSchedule.getObserverManager()).getActiveObservers().iterator();
		HashSet foundViewIDs = new HashSet();
	
		//iterate over all views in schedule
		while (observerIterator.hasNext()) {
			Observer observer = (Observer)observerIterator.next();
			Iterator viewIterator = observer.getViews().iterator();
			while (viewIterator.hasNext()) {
				//view found, get id
				View view = (View)viewIterator.next();
				Integer viewId = new Integer(view.getIdentificationNumber());
				foundViewIDs.add(viewId);
	
				//search for matching frame
				if (frameIDs.containsKey(viewId)) {
					FreakInternalFrame frame = (FreakInternalFrame)frameIDs.get(viewId);
					frame.setView(observer, view);
				} else {
					FreakInternalFrame frame = new FreakInternalFrame();
					frameIDs.put(viewId, frame);
					frame.setView(observer, view);
					desktop.add(frame);
					frame.setVisible(true);
					configureViewAction.setActiveView((FreakInternalFrame)desktop.getDelegate().getSelectedFrame());
	
					// add a InternalFrameListener that enables or disables
					// the Configure View menu item.
					frame.addInternalFrameListener(new InternalFrameAdapter() {
						public void internalFrameActivated(InternalFrameEvent evt) {
							configureViewAction.setActiveView((FreakInternalFrame)desktop.getDelegate().getSelectedFrame());
						}
						public void internalFrameDeactivated(InternalFrameEvent evt) {
							configureViewAction.setActiveView((FreakInternalFrame)desktop.getDelegate().getSelectedFrame());
						}
					});
				}
			}
		}
	
		// create an InternalFrameAcionListener and a MenuItem for each Frame in Vector frames automatically
		jMenuViews.removeAll();
	
		ButtonGroup viewButtonGroup = new ButtonGroup();
	
		int counter = 1;
		Iterator frames = frameIDs.values().iterator();
		while (frames.hasNext()) {
			FreakInternalFrame currentFrame = (FreakInternalFrame)frames.next();
			Integer viewId = new Integer(currentFrame.getViewId());
			if (foundViewIDs.contains(viewId)) {
				JMenuItem m = new JRadioButtonMenuItem();
				viewButtonGroup.add(m);
				m.setText(currentFrame.getTitle());
				m.addActionListener(new InternalFrameActionListener(currentFrame));
				jMenuViews.add(m);
	
				m.setSelected(currentFrame == desktop.getDelegate().getSelectedFrame());
				currentFrame.addInternalFrameListener(new RadioButtonMenuItemInternalFrameAdapter(m));
	
				if (counter <= 9)
					m.setAccelerator(KeyStroke.getKeyStroke("alt " + counter));
				counter++;
			} else {
				desktop.remove(currentFrame);
				// bug in JDesktopPane: the selected frame is not set to null when removed
				desktop.getDelegate().setSelectedFrame(null);
				configureViewAction.setActiveView((FreakInternalFrame)desktop.getDelegate().getSelectedFrame());
				currentFrame.dispose();
				frames.remove();
			}
		}
	
		// append cascade, tile, ...
		int viewCount = foundViewIDs.size();
		if (viewCount > 0) jMenuViews.addSeparator();
		jMenuViews.add(jMenuItemTile);
		jMenuViews.add(jMenuItemCascade);
		jMenuViews.add(jMenuItemMinimize);
		jMenuViews.add(jMenuItemRestore);
		jMenuViews.add(jMenuItemClose);
	
		enableWindowMode(viewCount > 0);
	
		// enable or disable Configure View menu item
		configureViewAction.setActiveView((FreakInternalFrame)desktop.getDelegate().getSelectedFrame());
	
		// Repaint Bug in JDesktopPane
		desktop.getDelegate().revalidate();
		desktop.getDelegate().repaint();
	}
	
	/**
	 * Enables the correct <code>JRadioButtonMenuItem<code> if a new 
	 * <code>InternalFrame</code> is activated. 
	 *  
	 * @author Dirk
	 */
	class RadioButtonMenuItemInternalFrameAdapter extends InternalFrameAdapter {
	
		private JMenuItem menuItem;
	
		public RadioButtonMenuItemInternalFrameAdapter(JMenuItem menuItem) {
			this.menuItem = menuItem;
		}
	
		public void internalFrameActivated(InternalFrameEvent evt) {
			menuItem.setSelected(true);
		}
		public void internalFrameDeactivated(InternalFrameEvent evt) {
			menuItem.setSelected(false);
		}
	}
	
	/**
	 * @author  nunkesser
	 */
	class ConfigureViewAction extends AbstractAction {
		private boolean enabledFromRunControl;
		private FreakInternalFrame activeView;
	
		ConfigureViewAction() {
			putValue(Action.MNEMONIC_KEY, new Integer('C'));
			putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
			putValue(Action.NAME, "Configure Selected View...");
			putValue(Action.SHORT_DESCRIPTION, "Configure the selected view");
		}
	
		public void actionPerformed(ActionEvent evt) {
			if (activeView != null) {
				View view = activeView.getView();
				if (view instanceof Configurable) {
					PropertyDialog pd = new PropertyDialog(RunFrame.this, runControl.getSchedule().getEventController(), activeView.getView(), null, runControl.getSchedule().getBatchList());
					pd.setVisible(true);
					runControl.scheduleEdited(runControl.getSchedule());
					activeView.revalidate();
					activeView.repaint();
				} else {
					JOptionPane.showMessageDialog(RunFrame.this, "Selected view is not configurable.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(RunFrame.this, "No view selected.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	
		/**
		 * @param enabledFromRunControl  the enabledFromRunControl to set
		 * @uml.property  name="enabledFromRunControl"
		 */
		public void setEnabledFromRunControl(boolean enabled) {
			enabledFromRunControl = enabled;
			updateEnabled();
		}
	
		/**
		 * @param activeView  the activeView to set
		 * @uml.property  name="activeView"
		 */
		public void setActiveView(FreakInternalFrame frame) {
			activeView = frame;
			updateEnabled();
		}
	
		public void unsetActiveView(FreakInternalFrame frame) {
			if (activeView == frame)
				activeView = null;
			updateEnabled();
		}
	
		/**
		 * @return  the activeView
		 * @uml.property  name="activeView"
		 */
		public FreakInternalFrame getActiveView() {
			return activeView;
		}
	
		private void updateEnabled() {
			super.setEnabled(enabledFromRunControl && (activeView != null) && (activeView.getView() instanceof Configurable));
	
			if (!enabledFromRunControl) {
				jMenuItemConfigureView.setToolTipText("Configuring is only permitted in suspended mode");
			} else {
				if (activeView == null) {
					jMenuItemConfigureView.setToolTipText("No view selected");
				} else {
					if (!(activeView.getView() instanceof Configurable)) {
						jMenuItemConfigureView.setToolTipText("Selected view is not configurable");
					} else {
						jMenuItemConfigureView.setToolTipText("Configure the selected view");
					}
				}
			}
		}
	}
}
