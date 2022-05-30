/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.Freak;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.event.BatchEventSource;
import freak.core.event.GenerationEvent;
import freak.core.event.GenerationEventListener;
import freak.core.event.GenerationEventSource;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.event.RunEventSource;
import freak.core.event.ScheduleEvent;
import freak.core.event.ScheduleEventListener;
import freak.core.event.ScheduleEventSource;
import freak.core.fitness.FitnessFunction;
import freak.core.fitness.FitnessTransformer;
import freak.core.graph.FreakGraphModel;
import freak.core.graph.GraphException;
import freak.core.graph.Initialization;
import freak.core.graph.OperatorGraph;
import freak.core.mapper.Mapper;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Configuration;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.Observer;
import freak.core.observer.ObserverManager;
import freak.core.populationmanager.PopulationManager;
import freak.core.random.RandomElementFactory;
import freak.core.random.SimpleRandomElementFactory;
import freak.core.searchspace.SearchSpace;
import freak.core.stoppingcriterion.StoppingCriterion;

/**
 * Schedule represents the complete state of a set of runs. Schedules can be saved, loaded, and stored in replays. The run method of Schedule performs the main simulation.
 * @author  Stefan, Dirk, Matthias
 */
public class Schedule implements GenerationEventSource, RunEventSource, BatchEventSource, ScheduleEventSource,
        Serializable {

    private EventController eventController = new EventController();
    private FreakGraphModel graphModel;
    private Initialization initialization;
    private PopulationManager populationManager;
    private SearchSpace phenotypeSearchSpace;
    private Mapper searchspaceMapper;
    private FitnessFunction fitnessFunction;
    private RandomElementFactory randomElementFactory;
    private RandomElement randomElement;
    private BatchList batchList = new BatchList();
    private Batch defaultBatch;
    private ObserverManager observerManager = new ObserverManager();
    /**
     * @uml.property  name="stoppingCriteria"
     * @uml.associationEnd  multiplicity="(0 -1)"
     */
    private StoppingCriterion[] stoppingCriteria;

    TimeController timeController = new TimeController();

    private Set generationEventListeners = new HashSet();
    private Set runEventListeners = new HashSet();
    private Set batchEventListeners = new HashSet();
    private Set scheduleEventListeners = new HashSet();

    private GenerationIndex currentTimeIndex = GenerationIndex.START;
    private GenerationIndex nextStepTimeIndex = GenerationIndex.FIRST;
    private GenerationIndex nextSkipTimeIndex = GenerationIndex.FIRST;
    private boolean stopRequestRecieved = true;
    private boolean aborted = false;

    // a flag used to prevent recursive calls to callInitialize as this may 
    // result in infinite loops
    private boolean callInitializeRunning = false;

    public Schedule() {
        randomElementFactory = new SimpleRandomElementFactory();
        randomElement = randomElementFactory.getRandomElement();
    }

    /**
     * @return the basic fitness function stripped by all fitness transformers.
     */
    public FitnessFunction getRealFitnessFunction() {
        FitnessFunction result = getFitnessFunction();
        while (result instanceof FitnessTransformer) {
            result = ((FitnessTransformer) result).getFitnessFunction();
        }
        return result;
    }

    /**
     * @return the current operator graph.
     */
    public OperatorGraph getOperatorGraph() {
        if (graphModel == null) {
            return null;
        }
        return graphModel.getOperatorGraph();
    }

    /**
     * @param  initialization
     * @uml.property  name="initialization"
     */
    public void setInitialization(Initialization initialization) {
        this.initialization = initialization;
    }

    /**
     * @return  the current initialization operator.
     * @uml.property  name="initialization"
     */
    public Initialization getInitialization() {
        return initialization;
    }

    /**
     * @return  the current population manager.
     * @uml.property  name="populationManager"
     */
    public PopulationManager getPopulationManager() {
        return populationManager;
    }

    /**
     * @return  the schedule's ObserverManager.
     * @uml.property  name="observerManager"
     */

    public ObserverManager getObserverManager() {
        return observerManager;
    }

    /**
     * @return  the current fitness function.
     * @uml.property  name="fitnessFunction"
     */
    public FitnessFunction getFitnessFunction() {
        return fitnessFunction;
    }

    /**
     * @return the current search space.
     */
    public SearchSpace getGenotypeSearchSpace() {
        if (searchspaceMapper != null) {
            // geno = pheno
            return searchspaceMapper.getGenotypeSearchSpace();
        } else {
            return phenotypeSearchSpace;
        }
    }

    /**
     * @param space  the new search space.
     * @uml.property  name="phenotypeSearchSpace"
     */
    public void setPhenotypeSearchSpace(SearchSpace space) {
        phenotypeSearchSpace = space;
    }

    /**
     * @return  the current phenotype search space.
     * @uml.property  name="phenotypeSearchSpace"
     */
    public SearchSpace getPhenotypeSearchSpace() {
        return phenotypeSearchSpace;
    }

    /**
     * @param mapper the new mapping class for search space mapping
     */
    public void setMapper(Mapper mapper) {
        this.searchspaceMapper = mapper;
    }

    /**
     * @return the current search space mapper.
     */
    public Mapper getMapper() {
        return searchspaceMapper;
    }

    /**
     * @param function  the new fitness function.
     * @uml.property  name="fitnessFunction"
     */
    public void setFitnessFunction(FitnessFunction function) {
        fitnessFunction = function;
    }

    /**
     * @param randomElementFactory  the randomElementFactory to set
     * @uml.property  name="randomElementFactory"
     */
    public void setRandomElementFactory(RandomElementFactory factory) {
        randomElementFactory = factory;
    }

    /**
     * @return  the RandomElementFactory
     * @uml.property  name="randomElementFactory"
     */
    public RandomElementFactory getRandomElementFactory() {
        return randomElementFactory;
    }

    /**
     * @return  the current <code>RandomElement</code>.
     * @uml.property  name="randomElement"
     */
    public RandomElement getRandomElement() {
        return randomElement;
    }

    /**
     * @param  element
     * @uml.property  name="randomElement"
     */
    public void setRandomElement(RandomElement element) {
        randomElement = element;
    }

    /**
     * @param manager  the new population manager.
     * @uml.property  name="populationManager"
     */
    public void setPopulationManager(PopulationManager manager) {
        populationManager = manager;
    }

    /**
     * @param graphModel  the new operator graph model.
     * @uml.property  name="graphModel"
     */
    public void setGraphModel(FreakGraphModel graphModel) {
        this.graphModel = graphModel;
    }

    /**
     * @return  the schedule's EventController.
     * @uml.property  name="eventController"
     */
    public EventController getEventController() {
        return eventController;
    }

    /**
     * Retrieves a list of all possible event sources in the schedule. This list
     * contains the <code>Schedule</code> itself, the <code>PopulationManager</code>
     * and all <code>OutPorts</code> of the graph.
     * @return the array of <code>Objects</code>
     */
    public Object[] getPossibleEventSources() {
        // -- create list of possible eventsources
        List l = new ArrayList();
        // -- the current PopulationManager is a commonly used event source
        if (populationManager != null) l.add(populationManager);
        // -- the schedule is a possible event source
        l.add(this);
        // -- the outports of the graph can fire events, too
        if (graphModel != null) {
            Object[] ports = getOperatorGraph().getAllPossibleEventSources();
            l.addAll(Arrays.asList(ports));
        }
        return l.toArray();
    }

    /**
     * @return  the schedule's StoppingCriteria.
     * @uml.property  name="stoppingCriteria"
     */
    public StoppingCriterion[] getStoppingCriteria() {
        return stoppingCriteria;
    }

    /**
     * @param criterion  the new StoppingCriteria.
     * @uml.property  name="stoppingCriteria"
     */
    public void setStoppingCriteria(StoppingCriterion[] criteria) {
        stoppingCriteria = criteria;
    }

    /**
     * @return the current OperatorGraph model.
     */
    public FreakGraphModel getFreakGraphModel() {
        return graphModel;
    }

    /**
     * Returns the list of batches in this Schedule. A Batch is a set of runs with common configuration for all modules. A BatchList is integral part of a Schedule and cannot be moved to another Schedule.
     * @uml.property  name="batchList"
     */
    public BatchList getBatchList() {
        return batchList;
    }

    /**
     * @return  the default batch used to create new batches if no other batch is selected.
     * @uml.property  name="defaultBatch"
     */
    public Batch getDefaultBatch() {
        return defaultBatch;
    }

    /**
     * Sets the default batch used to create new batches if no other batch is  selected.
     * @uml.property  name="defaultBatch"
     */
    public void setDefaultBatch(Batch batch) {
        defaultBatch = batch;
    }

    /**
     * Creates a batch out of the schedule's current configurations. 
     */
    public Batch createBatchFromCurrentConfigurations() {
        // -- create a new batch 
        Batch batch = new Batch();
        // -- insert current config of search space
        SearchSpace ss = getGenotypeSearchSpace();
        if (ss != null && ss instanceof Configurable) {
            Configuration conf = ((Configurable) ss).getConfiguration();
            batch.putConfiguration((Configurable) ss, conf);
        }
        // -- insert current config of fitness function
        FitnessFunction fitness = getRealFitnessFunction();
        if (fitness != null && fitness instanceof Configurable) {
            Configuration conf = ((Configurable) fitness).getConfiguration();
            batch.putConfiguration((Configurable) fitness, conf);
        }
        // -- insert current config of initialization operator
        Initialization init = getInitialization();
        if (init != null && init instanceof Configurable) {
            Configuration conf = ((Configurable) init).getConfiguration();
            batch.putConfiguration(init, conf);
        }
        return batch;
    }

    /**
     * Returns a set of all modules contained in this schedule.
     */
    public Set getAllModules() {
        HashSet modules = new HashSet();

        if (phenotypeSearchSpace != null) {
            modules.add(phenotypeSearchSpace);
        }

        // add fitness transformers and real fitness function
        if (fitnessFunction != null) {
            FitnessFunction fitness = fitnessFunction;
            while (fitness instanceof FitnessTransformer) {
                modules.add(fitness);
                fitness = ((FitnessTransformer) fitness).getFitnessFunction();
            }
            modules.add(fitness);
        }

        // add mapper and genotype search space if available
        if (searchspaceMapper != null) {
            modules.add(searchspaceMapper);
            modules.add(getGenotypeSearchSpace());
        }

        // add operator graph, operators and parameter controllers
        if (getOperatorGraph() != null) {
            modules.add(getOperatorGraph());
            for (int i = 0; i < getOperatorGraph().getOperatorCount(); i++) {
                modules.add(getOperatorGraph().getOperator(i));
            }
            modules.addAll(getOperatorGraph().getParameterControllers());
        }

        // add stopping criteria
        if (stoppingCriteria != null) {
            for (int i = 0; i < stoppingCriteria.length; i++) {
                modules.add(stoppingCriteria[i]);
            }
        }

        if (populationManager != null) modules.add(populationManager);

        if (initialization != null) modules.add(initialization);

        // add observers and views
        for (Iterator iter = observerManager.getActiveObservers().iterator(); iter.hasNext();) {
            Observer observer = (Observer) iter.next();
            modules.add(observer);
            // add views assigned to the current observer 
            for (Iterator iter2 = observer.getViews().iterator(); iter2.hasNext();) {
                modules.add(iter2.next());
            }
        }

        return modules;
    }

    @Override
    public String toString() {
        return "The Schedule itself";
    }

    /**
     * @return  the currentTimeIndex
     * @uml.property  name="currentTimeIndex"
     */
    public GenerationIndex getCurrentTimeIndex() {
        return currentTimeIndex;
    }

    public int getCurrentBatch() {
        return currentTimeIndex.batch;
    }

    public int getCurrentRun() {
        return currentTimeIndex.run;
    }

    public int getCurrentGeneration() {
        return currentTimeIndex.generation;
    }

    /**
     * Returns true if the schedule has run to its end.
     */
    public boolean isFinished() {
        return nextStepTimeIndex == GenerationIndex.END;
    }

    public boolean isCurrentRunFinished() {
        return stopRequestRecieved;
    }

    /**
     * Returns the real computing time that was used on this schedule.
     */
    public double getRunTime() {
        return timeController.getRuntime();
    }

    public void modulesEdited() {
        fireScheduleEditedEvent();

        // special case: the run number may have been reduced, so that the current batch is suddenly finished
        if (currentTimeIndex.batch != 0) {
            Batch b = batchList.get(currentTimeIndex.batch - 1);
            if (b.getRuns() == currentTimeIndex.run && stopRequestRecieved) {
                if (!b.isFinished()) {
                    b.setFinished(true);
                    fireBatchFinished();
                }
            }
        }

        computeNextStepTimeIndex();
    }

    /**
     * Finishes the current run after the next completion of an operator graph
     * processing. Sends a RunCompletedEvent afterwards.
     *
     * This method should only be called from the simulation or replay thread,
     * not from the AWT event dispatching Thread.
     */
    public void triggerStopCriterion() {
        stopRequestRecieved = true;
        aborted = false;
    }

    public void triggerSkip() {
        if (!stopRequestRecieved) {
            // pretend skip were triggered right before afterStep
            aborted = true;
            stopRequestRecieved = true;
        }
    }

    /**
     * @return  the nextStepTimeIndex
     * @uml.property  name="nextStepTimeIndex"
     */
    GenerationIndex getNextStepTimeIndex() {
        return nextStepTimeIndex;
    }

    GenerationIndex nextSkipTimeIndex() {
        return nextSkipTimeIndex;
    }

    // PROCESSING METHODS ******************************************************

    void skip() {
        if (!stopRequestRecieved) {
            // pretend skip were triggered right before afterStep
            aborted = true;
            stopRequestRecieved = true;
            afterStep();
        }
    }

    void step() throws SimulationException {
        //do one processing step
        if (currentTimeIndex.batch == 0) {
            stepSchedule();
        } else if (stopRequestRecieved && isLastRunInBatch()) {
            stepBatch();
        } else if (stopRequestRecieved) {
            stepRun();
        } else {
            stepGeneration();
        }
    }

    private void stepSchedule() throws SimulationException {
        fireScheduleStartedEvent();
        stepBatch();
    }

    private void stepBatch() throws SimulationException {
        currentTimeIndex = nextStepTimeIndex;

        stopRequestRecieved = false;
        aborted = false;

        randomElement = randomElementFactory.getRandomElement();
        Batch batch = batchList.get(currentTimeIndex.batch - 1);
        batch.applyAllConfigurations();
        batch.setStarted(true);

        callInitialize();

        fireBatchStarted();
        fireRunStartedEvent();

        populationManager.initPopulation(initialization);
        fireGenerationEvent();

        // skip insertion point

        afterStep();
    }

    private void stepRun() throws SimulationException {
        currentTimeIndex = nextStepTimeIndex;

        stopRequestRecieved = false;
        aborted = false;

        randomElement = randomElementFactory.getRandomElement();
        fireRunStartedEvent();

        populationManager.initPopulation(initialization);
        fireGenerationEvent();

        // skip insertion point

        afterStep();
    }

    private void stepGeneration() throws GraphException {
        currentTimeIndex = nextStepTimeIndex;

        populationManager.createNewGeneration();
        fireGenerationEvent();

        // skip insertion point

        afterStep();
    }

    private void afterStep() {
        if (stopRequestRecieved) {
            if (aborted) {
                fireRunAborted();
            } else {
                fireRunCompleted();
            }
            fireRunFinalize();

            if (isLastRunInBatch()) {
                Batch currentBatch = getBatchList().get(currentTimeIndex.batch - 1);
                currentBatch.setFinished(true);

                fireBatchFinished();
            }
        }

        computeNextStepTimeIndex();
    }

    private void computeNextStepTimeIndex() {
        if (stopRequestRecieved) {
            if (currentTimeIndex.batch == 0) nextStepTimeIndex = GenerationIndex.FIRST;

            if (isLastRunInBatch()) {
                if (isLastBatch()) {
                    nextStepTimeIndex = GenerationIndex.END;
                } else {
                    nextStepTimeIndex = currentTimeIndex.nextBatchStart();
                }
            } else {
                nextStepTimeIndex = currentTimeIndex.nextRunStart();
            }
        } else {
            nextStepTimeIndex = currentTimeIndex.nextGeneration();
        }
    }

    public boolean isLastRunInBatch() {
        if (currentTimeIndex.batch == 0) return true;
        return batchList.get(currentTimeIndex.batch - 1).getRuns() == currentTimeIndex.run;
    }

    public boolean isLastBatch() {
        return batchList.size() == currentTimeIndex.batch;
    }

    /**
     * Calls <code>Module.testSchedule(this)</code> and 
     * <code>Module.initialize()</code> for all modules contained within 
     * this schedule.
     * Modules can call this method to indicate that properties of some module 
     * have been changed out of the usual way of things, e.g., if a fitness
     * function or a mapper changes the dimension of a search space. 
     */
    public void callInitialize() {
        // no recursive calls are allowed since this may lead to infinite loops
        // if a modules calls callInitialize in its implementation of initialize.		
        if (!callInitializeRunning) {
            // set flag to prevent recursive calls  
            callInitializeRunning = true;

            Set moduleSet = getAllModules();
            Module[] modules = (Module[]) moduleSet.toArray(new Module[moduleSet.size()]);

            Arrays.sort(modules, new FreakClassComparator());

            try {
                for (int i = 0; i < modules.length; i++) {
                    modules[i].testSchedule(this);
                    modules[i].initialize();
                }
            } catch (UnsupportedEnvironmentException e) {
                callInitializeRunning = false;
                throw new SimulationException(e);
            }
            callInitializeRunning = false;
        }
    }

    // EVENT SENDING METHODS ***************************************************

    public void addGenerationEventListener(GenerationEventListener l) {
        generationEventListeners.add(l);
    }

    public void removeGenerationEventListener(GenerationEventListener l) {
        generationEventListeners.remove(l);
    }

    private void fireGenerationEvent() {
        GenerationEvent evt = new GenerationEvent(this, currentTimeIndex.generation);
        for (Iterator i = generationEventListeners.iterator(); i.hasNext();) {
            GenerationEventListener l = (GenerationEventListener) i.next();
            l.generation(evt);
        }
    }

    public void addRunEventListener(RunEventListener l) {
        runEventListeners.add(l);
    }

    public void removeRunEventListener(RunEventListener l) {
        runEventListeners.remove(l);
    }

    private void fireRunStartedEvent() {
        RunIndex run = currentTimeIndex.toRunIndex();
        RunEvent evt = new RunEvent(this, run);
        for (Iterator i = runEventListeners.iterator(); i.hasNext();) {
            RunEventListener l = (RunEventListener) i.next();
            l.runStarted(evt);
        }
    }

    private void fireRunCompleted() {
        RunEvent evt = new RunEvent(this, currentTimeIndex.toRunIndex());
        for (Iterator i = runEventListeners.iterator(); i.hasNext();) {
            RunEventListener l = (RunEventListener) i.next();
            l.runCompleted(evt);
        }
    }

    private void fireRunAborted() {
        RunEvent evt = new RunEvent(this, currentTimeIndex.toRunIndex());
        for (Iterator i = runEventListeners.iterator(); i.hasNext();) {
            RunEventListener l = (RunEventListener) i.next();
            l.runAborted(evt);
        }
    }

    private void fireRunFinalize() {
        RunEvent evt = new RunEvent(this, currentTimeIndex.toRunIndex());
        for (Iterator i = runEventListeners.iterator(); i.hasNext();) {
            RunEventListener l = (RunEventListener) i.next();
            l.runFinalize(evt);
        }
    }

    public void addBatchEventListener(BatchEventListener l) {
        batchEventListeners.add(l);
    }

    public void removeBatchEventListener(BatchEventListener l) {
        batchEventListeners.remove(l);
    }

    private void fireBatchStarted() {
        BatchEvent evt = new BatchEvent(this, currentTimeIndex.batch);

        for (Iterator i = batchEventListeners.iterator(); i.hasNext();) {
            BatchEventListener l = (BatchEventListener) i.next();
            l.batchStarted(evt);
        }
    }

    private void fireBatchFinished() {
        BatchEvent evt = new BatchEvent(this, currentTimeIndex.batch);

        for (Iterator i = batchEventListeners.iterator(); i.hasNext();) {
            BatchEventListener l = (BatchEventListener) i.next();
            l.batchFinished(evt);
        }
    }

    public void addScheduleEventListener(ScheduleEventListener l) {
        scheduleEventListeners.add(l);
    }

    public void removeScheduleEventListener(ScheduleEventListener l) {
        scheduleEventListeners.remove(l);
    }

    private void fireScheduleStartedEvent() {
        ScheduleEvent evt = new ScheduleEvent(this);
        for (Iterator i = scheduleEventListeners.iterator(); i.hasNext();) {
            ScheduleEventListener l = (ScheduleEventListener) i.next();
            l.scheduleStarted(evt);
        }
    }

    private void fireScheduleEditedEvent() {
        ScheduleEvent evt = new ScheduleEvent(this);
        for (Iterator i = scheduleEventListeners.iterator(); i.hasNext();) {
            ScheduleEventListener l = (ScheduleEventListener) i.next();
            l.scheduleEdited(evt);
        }
    }

    /**
     * @return the graphModel
     */
    public FreakGraphModel getGraphModel() {
        return graphModel;
    }

}
