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
import freak.core.control.EventController;
import freak.core.control.EventInfo;
import freak.core.control.Schedule;
import freak.core.fitness.FitnessFunction;
import freak.core.graph.FreakGraphModel;
import freak.core.graph.GraphSyntaxException;
import freak.core.graph.Operator;
import freak.core.graph.OperatorGraph;
import freak.core.mapper.Mapper;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Configuration;
import freak.core.modulesupport.IncompatibleModuleException;
import freak.core.modulesupport.IncompatibleModulePropertyException;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.ModuleCollector;
import freak.core.modulesupport.SpecificModuleRequiredException;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.Observer;
import freak.core.parametercontroller.ParameterController;
import freak.core.populationmanager.PopulationManager;
import freak.core.searchspace.SearchSpace;
import freak.core.stoppingcriterion.StoppingCriterion;
import freak.core.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * This class provides methods to check a schedule for correctness and to do mandatory things when some element of the schedule changes.
 * @author  Oliver, Michael, Dirk
 */
public class ScheduleDependencyChecker {
	
	private Schedule schedule;
	private ModuleCollector moduleCollector;
	private EventController eventController;
	
	// a link back to the schedule editor (used to display GUI dialogs)
	private ScheduleEditor scheduleEditor;
	
	static boolean observerWarningAcknowledged = false;
	
	/**
	 * Creates a new ScheduleDependencyChecker. It has to be fed with a schedule
	 * via <code>setSchedule()</code> to work correctly.
	 */
	public ScheduleDependencyChecker(ScheduleEditor editor) {
		this.scheduleEditor = editor;
	}
	
	/**
	 * Returns a <code>List</code> of <code>Observers</code> that don't have
	 * any <code>View</code> attached. Returns an empty <code>List</code> if no
	 * such <code>Observer</code> exists.
	 * @return the list of observers
	 */
	private List getObserversWithoutView() {
		List l = schedule.getObserverManager().getActiveObservers();
		List resultList = new ArrayList();
		for (Iterator i = l.iterator(); i.hasNext();) {
			Observer o = (Observer)i.next();
			if (o.getViews().isEmpty())
				resultList.add(o);
		}
		return resultList;
	}
	
	private void updateDefaultBatch() {
		Batch batch = schedule.getDefaultBatch();
		if (schedule.getPhenotypeSearchSpace() instanceof Configurable) {
			Configurable confObj = (Configurable)schedule.getPhenotypeSearchSpace();
			Configuration config = confObj.getConfiguration();
			batch.putConfiguration(confObj, config);
		}
		if (schedule.getRealFitnessFunction() instanceof Configurable) {
			Configurable confObj = (Configurable)schedule.getRealFitnessFunction();
			Configuration config = confObj.getConfiguration();
			batch.putConfiguration(confObj, config);
		}
		if (schedule.getInitialization() instanceof Configurable) {
			Configurable confObj = (Configurable)schedule.getInitialization();
			Configuration config = confObj.getConfiguration();
			batch.putConfiguration(confObj, config);
		}
	}
	
	/**
	 * @param schedule  the schedule.
	 * @uml.property  name="schedule"
	 */
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
		eventController = schedule.getEventController();
		moduleCollector = new ModuleCollector(schedule);
	}
	
	public boolean isObserverSetupOk() {
		if (!observerWarningAcknowledged) {
			if (schedule.getObserverManager().getActiveObservers().isEmpty()) {
				boolean answer = (JOptionPane.showConfirmDialog(null, "Please note, that you don't have any observers in your schedule.\nYou won't see anything while your algorithm runs.\n\nDo you want to continue?", "No Observers found", JOptionPane.YES_NO_OPTION) == 0);
				observerWarningAcknowledged = answer;
				return answer;
			}
			List l = getObserversWithoutView();
			if (!l.isEmpty()) {
				String obs = "";
				for (Iterator i = l.iterator(); i.hasNext();) {
					Observer o = (Observer)i.next();
					obs = obs + " - " + o.getName() + "\n";
				}
				boolean answer = (JOptionPane.showConfirmDialog(null, "The following observers don't have any view attached. You won't see any result of these observers:\n\n" + obs + "\nDo you want to continue?", "No View for Observers", JOptionPane.YES_NO_OPTION) == 0);
				observerWarningAcknowledged = answer;
				return answer;
			}
		}
		return true;
	}
	
	public boolean isGraphCorrect() {
		try {
			schedule.getOperatorGraph().checkSyntax();
			return true;
		} catch (GraphSyntaxException e) {
			JOptionPane.showMessageDialog(null, "Your graph has syntax errors. You have to correct the graph first.");
			return false;
		}
	}
	
	public boolean isEventSetupCorrect() {
		if (!eventController.allEventsRegistered()) {
			List l = eventController.getUnregisteredEvents();
			String msg = "The following events have not yet been assigned.\n\n";
			for (Iterator i = l.iterator(); i.hasNext();) {
				EventInfo evt = (EventInfo)i.next();
				String moduleName;
				if (evt.getOwner() instanceof Module)
					moduleName = ((Module)evt.getOwner()).getName();
				else
					moduleName = evt.getOwner().toString();
				msg = msg + " - " + moduleName + " (" + evt.getEventName() + ")\n";
			}
			msg = msg + "\nRunning such a schedule will not make sense.\nYou have to assign proper event sources to these events before you can continue.";
			JOptionPane.showMessageDialog(null, msg);
			return false;
		}
		return true;
	}
	
	public boolean isCorrect() {
		return isEventSetupCorrect() && isObserverSetupOk() && isGraphCorrect();
	}
	
	/**
	 * @return true if the schedule contains everything to start a run. It does
	 * NOT check whether the elements of the schedule make sense at all.
	 */
	public boolean isComplete() {
		return (schedule.getGenotypeSearchSpace() != null &&
		schedule.getFitnessFunction() != null &&
		schedule.getOperatorGraph() != null &&
		schedule.getStoppingCriteria() != null &&
		schedule.getInitialization() != null &&
		schedule.getPopulationManager() != null &&
		schedule.getOperatorGraph() != null &&
		!schedule.getOperatorGraph().isEmpty());
	}
	
	/**
	 * Creates a new default <code>Batch</code>.
	 */
	public void createDefaultBatch() {
		// create new default batch inside the schedule
		schedule.setDefaultBatch(schedule.createBatchFromCurrentConfigurations());
	}
	
	/**
	 * Creates a whole new <code>BatchList</code> with a copy of the 
	 * default <code>Batch</code>.
	 */
	public void createDefaultBatchList() {
		BatchList batchList = schedule.getBatchList();
		batchList.clear();
		
		// add a copy of the default batch to the batch list
		batchList.add(schedule.getDefaultBatch().copy(), true);
	}
	
	private void clearBatchList() {
		BatchList batchList = schedule.getBatchList();
		batchList.clear();
	}
	
	public void createDefaultOperatorGraph() {
		FreakGraphModel operatorGraph = new FreakGraphModel(schedule);
		operatorGraph.getOperatorGraph().createEvents();

		schedule.setGraphModel(operatorGraph);
	}
	
	/**
	 * Invokes necessary changes to the schedule if the user changes the search
	 * space.
	 */
	public void processNewSearchSpace() {
		checkCurrentSchedule(false);
		schedule.callInitialize();

		createDefaultBatch();
		clearBatchList();
	}
	
	/**
	 * Invokes necessary changes to the schedule if the user changes the fitness
	 * function.
	 */
	public void processNewFitnessFunction() {
		checkCurrentSchedule(false);
		schedule.callInitialize();

		createDefaultBatch();
		clearBatchList();
	}
	
	/**
	 * Invokes necessary changes to the schedule if the user changes the mapper.
	 */
	public void processNewMapper() {
		checkCurrentSchedule(false);
		schedule.callInitialize();
	}

	/**
	 * Invokes necessary changes to the schedule if the user changes the
	 * operator graph.
	 */
	public void processNewGraph() {
		checkCurrentSchedule(false);
		schedule.callInitialize();
	}
	
	/**
	 * Invokes necessary changes to the schedule if the user changes the stopping
	 * criterion.
	 */
	public void processNewStoppingCriterion() {
		checkCurrentSchedule(false);
		schedule.callInitialize();
	}
	
	/**
	 * Invokes necessary changes to the schedule if the user changes the
	 * population manager.
	 */
	public void processNewPopulationManager() {
		checkCurrentSchedule(false);
		schedule.callInitialize();
	}
	
	/**
	 * Invokes necessary changes to the schedule if the user changes the
	 * initialization operator.
	 */
	public void processNewInitializationOperator() {
		checkCurrentSchedule(false);
		schedule.callInitialize();

		createDefaultBatch();
		clearBatchList();
	}
	
	/**
	 * Invokes necessary changes to the schedule if the user changes the
	 * configuration of a module.
	 * 
	 * @param module the module whose configuration has been changed.
	 */
	public void processNewModuleConfiguration(Module module) {
		updateDefaultBatch();

		checkCurrentSchedule(true);
		schedule.callInitialize();
	}
	
	/**
	 * Removes the fitness function and all fitness transformers from the schedule
	 */
	public void removeFitnessFunction() {
		eventController.removeAllOfClass(FitnessFunction.class);
		schedule.setFitnessFunction(null);
	}
	
	/**
	 * Removes the mapper from the schedule.
	 */
	public void removeMapper() {
		eventController.removeAllOfClass(Mapper.class);
		schedule.setMapper(null);
	}

	/**
	 * Removes the <code>OperatorGraph</code> from the schedule.
	 */
	public void removeGraph() {
		if (schedule.getOperatorGraph() != null) {
			schedule.getOperatorGraph().removeFromEventController();
			schedule.setGraphModel(null);
		}
	}
	
	/**
	 * Removes the specified module from the schedule. Associated modules 
	 * (e.g. operators within operator graphs or views within observers) are 
	 * removed recursively.
	 * 
	 * @param module the module to be removed.
	 */
	private void removeModuleFromSchedule(Module module) {
		// remove module as event source and event listener
		eventController.removeModule(module);
		
		if (module instanceof SearchSpace) {
			schedule.setPhenotypeSearchSpace(null);
		} else if (module instanceof FitnessFunction) {
			removeFitnessFunction();
		} else if (module instanceof Mapper) {
			removeMapper();
		} else if (module instanceof OperatorGraph || (module instanceof Operator && !(module == schedule.getInitialization())) || module instanceof ParameterController) {
			// if a single operator or parameter controller is to be removed, 
			// the whole graph is removed instead
			removeGraph();
		} else if (module instanceof PopulationManager) {
			schedule.setPopulationManager(null);
		} else if (module instanceof StoppingCriterion) {
			// remove stopping criterion from array
			StoppingCriterion[] stoppingCriteria = schedule.getStoppingCriteria();
			if (stoppingCriteria != null && stoppingCriteria.length > 0) {
				StoppingCriterion[] newCriteria = new StoppingCriterion[stoppingCriteria.length - 1];
				// cut off the last stopping criterion
				System.arraycopy(stoppingCriteria, 0, newCriteria, 0, newCriteria.length);
				
				for (int i = 0; i < newCriteria.length; i++) {
					if (newCriteria[i] == module) {
						// exchange module with the last stopping criterion
						newCriteria[i] = stoppingCriteria[stoppingCriteria.length - 1];
					}
				}
				schedule.setStoppingCriteria(newCriteria);
			}
		} else if (module == schedule.getInitialization()) {
			schedule.setInitialization(null);
		} else if (module instanceof Observer) {
			// remove its associated views recursively
			
			// we use an array here instead of an iterator to avoid 
			// ConcurrentModificationExceptions when removing views from the 
			// list
			View[] views = (View[])((Observer)module).getViews().toArray(new View[((Observer)module).getViews().size()]);
			for (int i = 0; i < views.length; i++) {
				removeModuleFromSchedule(views[i]);
			}
			// remove observer from schedule
			schedule.getObserverManager().removeObserver((Observer)module);
		} else if (module instanceof View) {
			// remove view from observer
			Observer observer = ((View)module).getObserver();
			if (observer != null) {
				observer.removeView((View)module);
			}
		} else throw new RuntimeException("Unkown module class: " + module);
	}

	/**
	 * Checks the current schedule for incompatible modules and calls 
	 * processIncompatibleModule if such a module is found.
	 * The check is iterated until no incompatible module is found anymore.
	 * 
	 * @param showWarning specifies if GUI dialogs are to be shown in case an incompatible module is found.
	 */
	protected void checkCurrentSchedule(boolean showWarnings) {
		for (Iterator iter = schedule.getAllModules().iterator(); iter.hasNext();) {
			Module module = (Module)iter.next();
			
			try {
				checkModule(module, showWarnings);
				
				// catch UnsupportedEnvironmentExceptions and all other 
				// exceptions that may occur due to an incomplete schedule, e.g.
				// NullPointerExceptions if the fitness function has been 
				// removed.   
			} catch (Exception e) {

				processIncompatibleModule(module);
				
				// restart check
				checkCurrentSchedule(showWarnings);
				break;
			}
		}
	}
	
	private void processIncompatibleModule(Module module) {
		if (module == schedule.getPhenotypeSearchSpace()) {
			removeModuleFromSchedule(module);
			// handle this case separately because of the package structure
			// based on search spaces 
			processNewSearchSpace();
		} else if (module == schedule.getMapper()) {
			removeModuleFromSchedule(module);
			// handle this case separately because of the package structure
			// based on search spaces 
			processNewMapper();
		} else {
			// the typical case: simply remove the module from the schedule
			removeModuleFromSchedule(module);
		} 

		// update the panel inside the Schedule Editor displaying the current
		// search spaces and the fitness function 
		scheduleEditor.updateComponents();
	}

	/**
	 * Checks if the specified module can be used within the current schedule by 
	 * calling <code>module.testSchedule()</code>.
	 * If an <code>UnsupportedEnvironmentException</code> is caught, a dialog is 
	 * displayed in the GUI and the exception is forwarded so the module can be 
	 * processed in the method that called <code>checkModule</code>.
	 *   
	 * @param module the module to be checked.
	 * @param showWarnings specified if GUI dialogs are to be shown in case the module is found to be incompatible.
	 * @throws UnsupportedEnvironmentException exceptions thrown by <code>module.testSchedule</code>.
	 */
	protected void checkModule(Module module, boolean showWarnings) throws UnsupportedEnvironmentException {
		try {
			// check modules in search space-dependent packages if they can 
			// still be found by the module collector in case the search space 
			// or the mapper has been exchanged 
			moduleCollector.checkPackageOfModule(module);
			
			module.testSchedule(schedule);
			
		} catch (IncompatibleModulePropertyException e) {
			if (showWarnings) JOptionPane.showMessageDialog(scheduleEditor, "Module " + module.getName() + " is not compatible with property " + e.getProperty() + " of module " + e.getModule().getName() + ".", "Incompatible module property", JOptionPane.ERROR_MESSAGE);
			throw e;
		} catch (IncompatibleModuleException e) {
			if (showWarnings) JOptionPane.showMessageDialog(scheduleEditor, "Module " + module.getName() + " is not compatible with module " + e.getModule().getName() + ".", "Incompatible modules", JOptionPane.ERROR_MESSAGE);
			throw e;
		} catch (SpecificModuleRequiredException e) {
			if (showWarnings) JOptionPane.showMessageDialog(scheduleEditor, "Module " + module.getName() + " requires an instance of " + e.getModuleClass() + " to run.", "Specific module required", JOptionPane.ERROR_MESSAGE);
			throw e;
		} catch (UnsupportedEnvironmentException e) {
			if (showWarnings) JOptionPane.showMessageDialog(scheduleEditor, "Module " + module.getName() + " cannot be used within the current schedule.", "Incompatible module", JOptionPane.ERROR_MESSAGE);
			throw e;
		}
	}
	
}
