/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.observer;

import freak.Freak;
import freak.core.control.ScheduleInterface;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.fitness.FitnessFunction;
import freak.core.fitness.MultiObjectiveFitnessFunction;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.ModuleCollector;
import freak.core.modulesupport.inspector.StringArrayWrapper;
import freak.core.observer.AbstractObserver;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.postprocessor.Postprocessor;
import freak.module.fitness.booleanfunction.GenericPareto;
import freak.module.searchspace.BooleanFunction;
import freak.module.searchspace.BooleanFunctionGenotype;
import freak.module.searchspace.logictree.Data;
import freak.rinterface.model.IndividualSummary;
import freak.rinterface.model.RReturns;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

/**
 * Gives the final individuals of all runs.
 * 
 * @author Robin
 */
public class ResultObserver extends AbstractObserver implements RunEventListener, Configurable {

    private String testDataPath = "";
    String[] postprocessorNames;
    private int postprocessor = 0;
    private Module[] postprocessors;
    private Postprocessor selectedProcessor;
    private boolean selectPostprocessorFromGui = true;

    /**
     * @return the selectPostprocessorFromGui
     */
    public boolean isSelectPostprocessorFromGui() {
        return selectPostprocessorFromGui;
    }

    /**
     * @param selectPostprocessorFromGui the selectPostprocessorFromGui to set
     */
    public void setSelectPostprocessorFromGui(boolean selectPostprocessorFromGui) {
        this.selectPostprocessorFromGui = selectPostprocessorFromGui;
    }

    /**
     * Constructs a new <code>ResultObserver</code>.
     * 
     * @param schedule a link back to the current schedule.
     */
    public ResultObserver(ScheduleInterface schedule) {
        super(schedule);
        ModuleCollector moduleCollector = new ModuleCollector(schedule);
        postprocessors = moduleCollector.getPostprocessors(schedule.getGenotypeSearchSpace());
        postprocessorNames = new String[postprocessors.length + 1];
        postprocessorNames[0] = "-";
        for (int i = 1; i <= postprocessors.length; i++) {
            postprocessorNames[i] = postprocessors[i - 1].getName();
        }
        setMeasure(RUNS);
    }

    public Class getOutputDataType() {
        return Result.class;
    }

    public String getName() {
        return "Result";
    }

    public String getDescription() {
        return "Gives the final individuals of all runs. There is the possibility to do a rework of the individuals after each batch.";
    }

    @Override
    public void runFinalize(RunEvent evt) {
        Result summary = new Result();

        summary.setTestDataPath(testDataPath);
        IndividualList lastPopulation = getSchedule().getPopulationManager().getPopulation();
        if ((postprocessor != 0) && (selectPostprocessorFromGui)) {
            selectedProcessor = (Postprocessor) postprocessors[postprocessor - 1];
        }
        try {
            summary.setBestIndividuals(lastPopulation.getAllIndividualsWithRank(1));
            if (postprocessor != 0) selectedProcessor.addAllIndividuals(getSchedule(),
                    lastPopulation.getAllIndividualsWithRank(1));
        } catch (Exception e) {
            if (schedule.getFitnessFunction() instanceof MultiObjectiveFitnessFunction) {
                summary.setBestIndividuals(lastPopulation);
                if (postprocessor != 0) selectedProcessor.addAllIndividuals(getSchedule(), lastPopulation);
            }
        }

        summary.setNumberOfGenerations(getSchedule().getCurrentGeneration());
        summary.setRunNumber(evt.getRunIndex().run);
        //  summary.setTestDataPath(testDataPath);

        //Collection classes = getClasses("observer", Observer.class, true);
        if (postprocessor != 0 && getSchedule().isLastRunInBatch()) {
            selectedProcessor.analyse();
            selectedProcessor.reset();
        }
        updateViews(summary);
    }

    @Override
    public void createEvents() {
        schedule.getEventController().addEvent(this, RunEvent.class, schedule);
    }

    public void setPropertyTestData(String testDataPath) {
        this.testDataPath = testDataPath;
    }

    public String getPropertyTestData() {
        return this.testDataPath;
    }

    public String getShortDescriptionForTestData() {
        return "(BFG only) Path to test data";
    }

    public String getLongDescriptionForTestData() {
        return "(Boolean functions only) Path to test data";
    }

    public void setPropertyPostprocessor(StringArrayWrapper saw) {
        postprocessor = saw.getIndex();
    }

    public StringArrayWrapper getPropertyPostprocessor() {
        return new StringArrayWrapper(postprocessorNames, postprocessor);
    }

    public String getShortDescriptionForPostprocessor() {
        return "Postprocessor";
    }

    public String getLongDescriptionForPostprocessor() {
        return "Should the indivduals of a batch be postprocessed?";
    }

    /**
     * A simple entity class containing the data needed for the results. Results generate the textual output displayed via  <code>toString()</code> 
     * @author  Robin
     */
    public class Result implements Serializable {

        //private String testDataPath;

        private int runNumber;
        private int numberOfGenerations;
        private IndividualList bestIndividuals;
        private String testDataPath;

        /**
         * @return  the numberOfGenerations
         * @uml.property  name="numberOfGenerations"
         */
        public int getNumberOfGenerations() {
            return numberOfGenerations;
        }

        /**
         * @param i  the numberOfGenerations to set
         * @uml.property  name="numberOfGenerations"
         */
        public void setNumberOfGenerations(int i) {
            numberOfGenerations = i;
        }

        /**
         * @return  the bestIndividuals
         * @uml.property  name="bestIndividuals"
         */
        public IndividualList getBestIndividuals() {
            return bestIndividuals;
        }

        /**
         * @param individuals  the bestIndividuals to set
         * @uml.property  name="bestIndividuals"
         */
        public void setBestIndividuals(IndividualList individuals) {
            bestIndividuals = individuals;
        }

        // This may be called more than once, e.g. for writing to a file and to the view
        @Override
        public String toString() {
            Freak.debug("Trying to write to a log file.", 3);
            StringBuffer s = new StringBuffer();

            FitnessFunction fitness = getSchedule().getFitnessFunction();
            boolean singleObjective = (fitness instanceof SingleObjectiveFitnessFunction);
            int runs = (((!Data.csvDisable) && (testDataPath.equals(""))) || ((Data.csvDisable) && (Data.testData == null))) ? 1
                    : 2;
            if (this.getRunNumber() == 1) {
                if (runs == 2) s.append("Data set , ");
                s.append("Run , Generation , No of generations , ");
                if (singleObjective) {
                    s.append("Objective value , ");
                } else {
                    int objectives = ((MultiObjectiveFitnessFunction) fitness).getDimensionOfObjectiveSpace();
                    for (int j = 1; j <= objectives; j++) {
                        s.append("Objective value " + j + ", ");
                    }
                }
                s.append("Individual\n");
            }
            for (int k = 0; k < runs; k++) {
                if (runs == 2) {
                    if (k == 1) {
                        if (Data.csvDisable) {
                            Data.setRData(Data.trainingData);
                            RReturns.setAllMCRinTrainingData(new Vector<IndividualSummary>());
                        } else Data.setDataLocation(((BooleanFunction) schedule.getGenotypeSearchSpace())
                                .getPropertyInputPath());
                    } else {
                        if (Data.csvDisable) {
                            Data.setRData(Data.testData);
                            RReturns.setAllMCRinTestData(new Vector<IndividualSummary>());
                        } else Data.setDataLocation(testDataPath);
                    }
                    try {
                        Data.readData();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                //              Data.printTable();
                //              System.out.println(Data.getResultBitSet());
                Iterator i = getBestIndividuals().iterator();

                /*              Individual test = (Individual)getBestIndividuals().iterator().next();
                                StaticCompareNode scn=(StaticCompareNode) ((AndNode)((OrNode)((BooleanFunctionGenotype)test.getGenotype()).getDnfbaum().getRoot()).getChildren().get(0)).getChildren().get(0);
                                System.out.println("Testing literal "+scn.toString()+" of "+test.getGenotype());
                                System.out.println(scn.getValueBitset());
                                System.out.println("First update...");
                                scn.updateBitset();
                                System.out.println(scn.getValueBitset());
                                System.out.println("Second update...");
                                scn.updateBitset();
                                System.out.println(scn.getValueBitset());*/
                while (i.hasNext()) {
                    Individual individual = (Individual) i.next();
                    // Necessary if we change the data path
                    //                  if ((!testDataPath.equals("")) && (individual.getGenotype() instanceof BooleanFunctionGenotype)) {
                    if ((runs == 2) && (individual.getGenotype() instanceof BooleanFunctionGenotype)) {
                        individual.setLatestKnownFitnessValue(null);
                        ((BooleanFunctionGenotype) individual.getGenotype()).getDnfbaum().update();
                        if (Data.csvDisable) {
                            if (k == 1) {
                                //                              System.out.println("Update was called for training model "+individual.getGenotype());
                                s.append("training , ");
                            } else {
                                //                              System.out.println("Update was called for test model "+individual.getGenotype());
                                s.append("test , ");
                            }
                        } else s.append(Data.getDataLocation() + " , ");
                    }
                    // MCR Berechnungen f�r Aufruf aus R!!!!
                    // TODO Ansteuerbar aus R machen, also Test auf Data.csvDisable durch etwas sinnvolleres ersetzen
                    if ((Data.csvDisable) && (individual.getGenotype() instanceof BooleanFunctionGenotype)) {
                        int length = ((BooleanFunctionGenotype) individual.getGenotype()).evaluateSize();
                        double mcr = 100.0 * (1.0 - ((double) ((BooleanFunctionGenotype) individual.getGenotype())
                                .evaluate()) / ((double) Data.getNumRows()));
                        double geccoFit1 = 0.0;
                        double geccoFit2 = 0.0;
                        if (fitness instanceof GenericPareto) {
                            geccoFit1 = ((BooleanFunctionGenotype) individual.getGenotype()).evaluate1s()
                                    / (double) Data.getNum1Rows() / 3.0
                                    + (((BooleanFunctionGenotype) individual.getGenotype()).evaluate0s())
                                    / (double) Data.getNum0Rows() / 3.0
                                    + (double) (((GenericPareto) fitness).getPropertySizePruning() - length + 1)
                                    / (double) (((GenericPareto) fitness).getPropertySizePruning()) / 3.0;
                            geccoFit2 = ((BooleanFunctionGenotype) individual.getGenotype()).evaluate1s()
                                    / (double) Data.getNum1Rows()
                                    + (((BooleanFunctionGenotype) individual.getGenotype()).evaluate0s())
                                    / (double) Data.getNum0Rows()
                                    - (length)
                                    / ((double) ((GenericPareto) fitness).getPropertySizePruning() * (double) Data
                                            .getNumRows());
                        }
                        if (k == 0) {
                            RReturns.getAllMCRinTestData().add(
                                    new IndividualSummary(individual.getDateOfBirth(), length, mcr,
                                            ((BooleanFunctionGenotype) individual.getGenotype())
                                                    .getCharacteristicBitSet(), Data.getResultBitSet(), Data
                                                    .getNumRows(), geccoFit1, geccoFit2));
                            if (mcr < RReturns.getBestMCRinTestData()) RReturns.setBestMCRinTestData(mcr);
                        } else {
                            RReturns.getAllMCRinTrainingData().add(
                                    new IndividualSummary(individual.getDateOfBirth(), length, mcr,
                                            ((BooleanFunctionGenotype) individual.getGenotype())
                                                    .getCharacteristicBitSet(), Data.getResultBitSet(), Data
                                                    .getNumRows(), geccoFit1, geccoFit2));
                        }
                        s.append(mcr + " , ");
                    }
                    /*                  if (fitness instanceof GenericPareto) {
                                            s.append(((GenericPareto)fitness).getPropertySubsets()+" , ");
                                            s.append(((GenericPareto)fitness).getPropertySubsetsUpdate()+" , ");
                                            s.append(((GenericPareto)fitness).getPropertyBagging()+" , ");
                                            s.append(((GenericPareto)fitness).getPropertyBaggingUpdate()+" , ");
                                        }*/
                    s.append(this.getRunNumber() + " , ");
                    s.append(individual.getDateOfBirth() + " , ");
                    s.append(this.getNumberOfGenerations() + " , ");

                    //                  int testValue=0;

                    if (singleObjective) {
                        //                      s.append(new Double(((SingleObjectiveFitnessFunction)fitness).evaluate(individual, getBestIndividuals()))).toString();
                        //TODO war da ein Fehler????
                        s.append(((SingleObjectiveFitnessFunction) fitness).evaluate(individual, getBestIndividuals())
                                + " , ");
                    } else {
                        double[] result = ((MultiObjectiveFitnessFunction) fitness).evaluate(individual,
                                getBestIndividuals());
                        for (int j = 0; j < result.length; j++) {
                            s.append(result[j]);
                            if (j < result.length - 1) {
                                s.append(" , ");
                            }
                        }
                        s.append(" , ");
                        //                      testValue=(int) (result[0]+result[1]);
                    }
                    /*                  s.append(((BooleanFunctionGenotype)individual.getGenotype()).getDnfbaum().getFullfilledLines().cardinality()+" , ");
                                        BitSet test = ((BooleanFunctionGenotype)individual.getGenotype()).getDnfbaum().getRoot().getValueBitset();
                                        test.and(Data.getResultBitSet());
                                        s.append(test.cardinality()+" , ");
                                        s.append(((BooleanFunctionGenotype)individual.getGenotype()).getBitSet().cardinality()+" , ");
                                        if (testValue!=((BooleanFunctionGenotype)individual.getGenotype()).getBitSet().cardinality()) {
                                            s.append("\n"+"ERROR"+"\n");
                                        }*/

                    if (i.hasNext()) {
                        s.append(individual.getGenotype() + "\n");
                    } else {
                        s.append(individual.getGenotype());
                        if ((runs == 2) && (k == 0)) s.append("\n");
                    }
                    //                  s.append(((BooleanFunctionGenotype)individual.getGenotype()).getDnfbaum().getRoot().getValueBitset()+"\n");
                    //                  s.append(Data.getResultBitSet()+"\n");
                }

            }
            return s.toString();
        }

        /**
         * @return  the runNumber
         * @uml.property  name="runNumber"
         */
        public int getRunNumber() {
            return runNumber;
        }

        /**
         * @param i  the runNumber to set
         * @uml.property  name="runNumber"
         */
        public void setRunNumber(int i) {
            runNumber = i;
        }

        /**
         * @return the testDataPath
         */
        public String getTestDataPath() {
            return testDataPath;
        }

        /**
         * @param testDataPath the testDataPath to set
         */
        public void setTestDataPath(String testDataPath) {
            this.testDataPath = testDataPath;
        }

        /**
         * @param testDataPath the testDataPath to set
         */
        /*      public void setTestDataPath(String testDataPath) {
                    this.testDataPath = testDataPath;
                }*/

    }

    /**
     * @param selectedProcessor the selectedProcessor to set
     */
    public void setSelectedProcessor(Postprocessor selectedProcessor) {
        this.selectedProcessor = selectedProcessor;
    }

    /**
     * @param postprocessor the postprocessor to set
     */
    public void setPostprocessor(int postprocessor) {
        this.postprocessor = postprocessor;
    }

}
