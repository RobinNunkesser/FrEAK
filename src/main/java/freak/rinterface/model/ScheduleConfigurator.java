/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.jar.JarFile;

import freak.Freak;
import freak.core.control.Batch;
import freak.core.control.BatchList;
import freak.core.control.Schedule;
import freak.core.fitness.FitnessFunction;
import freak.core.graph.FreakGraphModel;
import freak.core.graph.Initialization;
import freak.core.mapper.Mapper;
import freak.core.modulesupport.ClassCollector;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.ModuleInfo;
import freak.core.modulesupport.OperatorGraphCollector;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.observer.Observer;
import freak.core.observer.ObserverManager;
import freak.core.observer.ObserverViewMismatchException;
import freak.core.populationmanager.PopulationManager;
import freak.core.searchspace.SearchSpace;
import freak.core.stoppingcriterion.StoppingCriterion;
import freak.core.util.GraphViz;
import freak.gui.graph.OperatorGraphFile;
import freak.gui.scheduleeditor.ScheduleDependencyChecker;
import freak.module.fitness.booleanfunction.GenericPareto;
import freak.module.fitness.pointset.LMSOptimization;
import freak.module.fitness.pointset.LQDOptimization;
import freak.module.fitness.pointset.LQSOptimization;
import freak.module.fitness.pointset.LTAOptimization;
import freak.module.fitness.pointset.LtSOptimization;
import freak.module.fitness.pointset.LtSParetoOptimization;
import freak.module.fitness.pointset.LtSParetoQuantileOptimization;
import freak.module.observer.ResultObserver;
import freak.module.operator.initialization.LTSInitialization;
import freak.module.postprocessor.booleanfunction.Count;
import freak.module.searchspace.BooleanFunction;
import freak.module.searchspace.PointSet;
import freak.module.stoppingcriterion.Duration;
import freak.module.stoppingcriterion.GenerationCount;
import freak.module.stoppingcriterion.NoNewIndividualForManyGenerations;
import freak.module.stoppingcriterion.PredictingModelFound;

/**
 * A class to create and partly configure Schedules via .jcalls directly from R
 * @author  Dominic,Robin
 */
public class ScheduleConfigurator {

    //	public static String[] regressionMethods={"lms","lqd","lqs","lta","lts"};
    static Schedule currentSchedule;

    static boolean editingFinished = false;

    public static Schedule getCurrentSchedule() {
        return currentSchedule;
    }

    public static void setCurrentSchedule(Schedule sched) {
        //this method is used to pass a schedule that has just been edited with the graphical ScheduleEditor
        //to the Schedule Configurator, thus it can be retrieved from R
        currentSchedule = sched;
        editingFinished = true;
        /*System.out.println("\n\nThe Schedule that has just been edited, is now available in the \nScheduleConfigurator.");
        System.out.println("To retrieve it from R you can use:");
        System.out.println("[destination object] <- getCurrentSched()\n");**/
    }

    private static void updateBatchForSchedule(Schedule schedule, int runs) {
        // Batches und Runs
        Batch batch = schedule.createBatchFromCurrentConfigurations();
        batch.setRuns(runs);
        BatchList batchList = schedule.getBatchList();
        batchList.clear();
        batchList.add(batch.copy(), true);
        schedule.setDefaultBatch(batch);
    }

    // called from R
    public static void setDiscriminationR(int runs, int generations) {
        setDiscriminationR(runs, generations, 12);
    }

    public static void setDiscriminationR(int runs, int generations, String graphFile) {
        setDiscriminationR(runs, generations, 12, false, graphFile);
    }

    public static void setDiscriminationR(int runs, int generations, int sizePruning) {
        setDiscriminationR(runs, generations, sizePruning, false);
    }

    public static void setDiscriminationR(int runs, int generations, int sizePruning, boolean stoppingCriterion) {
        setDiscriminationR(runs, generations, sizePruning, stoppingCriterion, "GPASGraph.fop");
    }

    public static void setDiscriminationR(int runs, int generations, int sizePruning, boolean stoppingCriterion,
            String graphFile) {
        setDiscrimination("ignored", runs, generations, "ignored", "", sizePruning, stoppingCriterion, graphFile);
    }

    // called from "Quick Start GPAS"
    public static void setDiscrimination(String path, int runs, int generations, String testDataPath, String saveTo) {
        setDiscrimination(path, runs, generations, testDataPath, saveTo, 12);
    }

    public static void setDiscrimination(String path, int runs, int generations, String testDataPath, String saveTo,
            int sizePruning) {
        setDiscrimination(path, runs, generations, testDataPath, saveTo, sizePruning, false);
    }

    public static void setDiscrimination(String path, int runs, int generations, String testDataPath, String saveTo,
            int sizePruning, boolean stoppingCriterion) {
        setDiscrimination(path, runs, generations, testDataPath, saveTo, sizePruning, false, "GPASGraph.fop");
    }

    public static void setDiscrimination(String path, int runs, int generations, String testDataPath, String saveTo,
            int sizePruning, boolean stoppingCriterion, String graphFile) {
        createSchedule(path, runs, generations, stoppingCriterion, graphFile);
        Module m;
        try {
            currentSchedule.setFitnessFunction(null);
            // Fitnessfunktion
            m = new GenericPareto(currentSchedule);
            ((GenericPareto) m).setParetoObjective(new int[] { GenericPareto.OBJECTIVE_CASES,
                    GenericPareto.OBJECTIVE_CONTROLS, GenericPareto.OBJECTIVE_LENGTH });
            ((GenericPareto) m).setPropertySubsets(new Integer(1));
            ((GenericPareto) m).setPropertySizePruning(new Integer(sizePruning));
            m.testSchedule(currentSchedule);
            m.initialize();
            m.createEvents();
            currentSchedule.setFitnessFunction((FitnessFunction) m);
        } catch (UnsupportedEnvironmentException e) {
            throw new RuntimeException("Something is wrong with the default Schedule.", e);
        }
        // Observer, View and Postprocessor		
        ObserverManager om = currentSchedule.getObserverManager();
        m = new ResultObserver(currentSchedule);
        ((ResultObserver) m).setPropertyTestData(testDataPath);
        //View
        if (saveTo.trim().equals("")) {
            freak.module.view.RReturn rReturn = new freak.module.view.RReturn(currentSchedule);
            try {
                ((Observer) m).addView(rReturn);
            } catch (ObserverViewMismatchException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            freak.module.view.FileWriter output = new freak.module.view.FileWriter(currentSchedule);
            output.setPropertyFile(new File(saveTo));
            try {
                ((Observer) m).addView(output);
            } catch (ObserverViewMismatchException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        m.initialize();
        m.createEvents();
        om.addObserver((Observer) m);

        updateBatchForSchedule(currentSchedule, runs);
    }

    public static void setGeccoFitnessFunction(int fitnessfunction) {
        setGeccoFitnessFunction(fitnessfunction, 12);
    }

    public static void setGeccoFitnessFunction(int fitnessfunction, int sizePruning) {
        Module m;
        try {
            currentSchedule.setFitnessFunction(null);
            // Fitnessfunktion
            m = new GenericPareto(currentSchedule);
            switch (fitnessfunction) {
                case 1: {
                    ((GenericPareto) m).setParetoObjective(new int[] {
                            GenericPareto.OBJECTIVE_STANDARDISED_FITNESS_LENGTH,
                            GenericPareto.OBJECTIVE_STANDARDISED_FITNESS_LENGTH, GenericPareto.OBJECTIVE_NONE });
                    break;
                }
                case 2: {
                    ((GenericPareto) m)
                            .setParetoObjective(new int[] {
                                    GenericPareto.OBJECTIVE_STANDARDISED_FITNESS_IMPLICIT_LENGTH,
                                    GenericPareto.OBJECTIVE_STANDARDISED_FITNESS_IMPLICIT_LENGTH,
                                    GenericPareto.OBJECTIVE_NONE });
                    break;
                }
                case 3: {
                    ((GenericPareto) m).setParetoObjective(new int[] { GenericPareto.OBJECTIVE_STANDARDISED_FITNESS,
                            GenericPareto.OBJECTIVE_LENGTH, GenericPareto.OBJECTIVE_NONE });
                    break;
                }
                case 4: {
                    ((GenericPareto) m).setParetoObjective(new int[] { GenericPareto.OBJECTIVE_CASES,
                            GenericPareto.OBJECTIVE_CONTROLS, GenericPareto.OBJECTIVE_LENGTH });
                    break;
                }
            }
            ((GenericPareto) m).setPropertySubsets(new Integer(1));
            ((GenericPareto) m).setPropertySizePruning(new Integer(sizePruning));
            m.testSchedule(currentSchedule);
            m.initialize();
            m.createEvents();
            currentSchedule.setFitnessFunction((FitnessFunction) m);
        } catch (UnsupportedEnvironmentException e) {
            throw new RuntimeException("Something is wrong with the default Schedule.", e);
        }
    }

    // called from R
    public static void setInteractionR(int runs, int generations, String graphFile, int occurences, double ratio) {
        setInteraction("ignored", runs, generations, graphFile, occurences, ratio, "");
    }

    // called from "Qucik Start GPAS"	
    public static void setInteraction(String path, int runs, int generations, String graphFile, int occurences,
            double ratio, String saveTo) {
        createSchedule(path, runs, generations);
        Module m;
        try {
            currentSchedule.setFitnessFunction(null);
            // Fitnessfunktion
            m = new GenericPareto(currentSchedule);
            ((GenericPareto) m).setParetoObjective(new int[] { GenericPareto.OBJECTIVE_CASESCONTROLS,
                    GenericPareto.OBJECTIVE_CONTROLS, GenericPareto.OBJECTIVE_LENGTH });
            ((GenericPareto) m).setPropertySubsets(new Integer(1));
            ((GenericPareto) m).setPropertySizePruning(new Integer(12));
            m.testSchedule(currentSchedule);
            m.initialize();
            m.createEvents();
            currentSchedule.setFitnessFunction((FitnessFunction) m);
        } catch (UnsupportedEnvironmentException e) {
            throw new RuntimeException("Something is wrong with the default Schedule.", e);
        }

        // Observer, View and Postprocessor		
        ObserverManager om = currentSchedule.getObserverManager();
        m = new ResultObserver(currentSchedule);
        ((ResultObserver) m).setPropertyTestData("");
        //View
        if (saveTo.trim().equals("")) {
            freak.module.view.RReturn rReturn = new freak.module.view.RReturn(currentSchedule);
            try {
                ((Observer) m).addView(rReturn);
            } catch (ObserverViewMismatchException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            freak.module.view.FileWriter output = new freak.module.view.FileWriter(currentSchedule);
            output.setPropertyFile(new File(saveTo));
            try {
                ((Observer) m).addView(output);
            } catch (ObserverViewMismatchException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        //Postprocessor
        ((ResultObserver) m).setPostprocessor(-1);
        ((ResultObserver) m).setSelectPostprocessorFromGui(false);
        Count postprocessor = new Count(currentSchedule);
        Count.fileName = graphFile;
        Count.minCount = occurences;
        Count.minPercent = ratio;
        ((ResultObserver) m).setSelectedProcessor(postprocessor);
        m.initialize();
        m.createEvents();
        om.addObserver((Observer) m);

        updateBatchForSchedule(currentSchedule, runs);
    }

    public static void setGenerationCount(int gencount) {
        try {
            GenerationCount generationCount = new GenerationCount(currentSchedule);
            generationCount.setPropertyCount(new Integer(gencount));
            generationCount.testSchedule(currentSchedule);
            generationCount.initialize();
            generationCount.createEvents();

            StoppingCriterion[] stoppingCriteria = { generationCount };
            currentSchedule.setStoppingCriteria(stoppingCriteria);

        } catch (UnsupportedEnvironmentException e) {
            throw new RuntimeException("Something is wrong with the default Schedule.", e);
        }
        System.out.println("GenerationCount has been set from R");
    }

    // TODO muss noch erg�nzt werden um BatchUpdate (da Configurable) und vielleicht auch um schedule.modulesEdited
    public static void setOperatorGraph(String ogLocation) {
        File f = new File(ogLocation);
        try {
            OperatorGraphFile ogFile = OperatorGraphFile.read(new FileInputStream(f));
            FreakGraphModel model = ogFile.generateGraph(currentSchedule);
            currentSchedule.setGraphModel(model);

        } catch (Exception exc) {
            exc.printStackTrace();
        }
        System.out.println("OperatorGraph has been set from R");
    }

    public static void printOperatorGraph() {
        GraphViz gv = new GraphViz();
        FreakGraphModel model = currentSchedule.getGraphModel();
        String dotSource = model.getOperatorGraph().toDotGraph();
        // Save Graph in dot format
        File graphDot = new File("operatorgraph.dot");
        try {
            FileWriter fout = new FileWriter(graphDot);
            fout.write(dotSource);
            fout.close();
        } catch (Exception e) {
            System.err.println("Error: I/O error while writing the dot source");
        }
        // Save Graph as Gif
        File graphGif = new File("operatorgraph.gif");
        GraphViz.DOT = "C:\\Program Files\\graphviz\\Graphviz\\bin\\dot";
        try {
            gv.writeGraphToFile(gv.getGraph(dotSource), graphGif);
        } catch (Exception e1) {
            GraphViz.DOT = "/usr/local/graphviz-2.12/bin/dot";
            try {
                gv.writeGraphToFile(gv.getGraph(dotSource), graphGif);
            } catch (Exception e2) {
                System.out.println("DOT not found");
            }
        }
    }

    public static Schedule getLTSSchedule(RDoubleMatrix data, int h, boolean adjust, int runs, int generationCountStop) {
        return getLTSSchedule(data, h, adjust, runs, generationCountStop, 0, 0);
    }

    public static Schedule getLTSSchedule(RDoubleMatrix data, int h, boolean adjust, int runs, int generationCountStop,
            int duration, String fitnessFunction) {
        int function = 0;
        if (fitnessFunction.equals("lts")) function = 0;
        if (fitnessFunction.equals("lms")) function = 1;
        if (fitnessFunction.equals("lta")) function = 6;
        if (fitnessFunction.equals("lqs")) function = 7;
        if (fitnessFunction.equals("lqd")) function = 8;
        return getLTSSchedule(data, h, adjust, runs, generationCountStop, duration, function);
    }

    public static Schedule getLTSSchedule(RDoubleMatrix data, int h, boolean adjust, int runs, int generationCountStop,
            int duration, int fitnessFunction) {
        Schedule schedule = new Schedule();

        ScheduleDependencyChecker scheduleDependencyChecker = new ScheduleDependencyChecker(null);
        scheduleDependencyChecker.setSchedule(schedule);

        Module m;
        try {
            // Suchraum
            m = new freak.module.searchspace.PointSet(schedule);
            int noOfPoints = data.getDim()[0];
            int dimension = data.getDim()[1];
            PointSet.Point[] points = new PointSet.Point[noOfPoints];
            for (int i = 0; i < noOfPoints; i++) {
                points[i] = new PointSet.Point(dimension);
                for (int j = 0; j < dimension; j++) {
                    points[i].setK(j, data.getValues()[i + j * noOfPoints]);
                }
            }
            ((PointSet) m).setPoints(points);
            m.testSchedule(schedule);
            m.initialize();
            m.createEvents();
            schedule.setPhenotypeSearchSpace((SearchSpace) m);

            // Fitnessfunktion
            switch (fitnessFunction) {
                case 0:
                    m = new freak.module.fitness.pointset.LtSOptimization(schedule);
                    ((LtSOptimization) m).setPropertyH(new Integer(h));
                    ((LtSOptimization) m).setPropertyAdjust(adjust);
                    break;
                case 1:
                    m = new freak.module.fitness.pointset.LMSOptimization(schedule);
                    ((LMSOptimization) m).setPropertyH(new Integer(h));
                    ((LMSOptimization) m).setPropertyAdjust(adjust);
                    break;
                case 2:
                    m = new freak.module.fitness.pointset.LtSParetoQuantileOptimization(schedule);
                    ((LtSParetoQuantileOptimization) m).setPropertyH(new Integer(h));
                    ((LtSParetoQuantileOptimization) m).setPropertyAdjust(adjust);
                    break;
                case 3:
                    m = new freak.module.fitness.pointset.LtSParetoOptimization(schedule);
                    ((LtSParetoOptimization) m).setPropertyH(new Integer(h));
                    ((LtSParetoOptimization) m).setPropertyAdjust(adjust);
                    break;
                case 4:
                    m = new freak.module.fitness.pointset.LtSParetoQuantileOptimization(schedule);
                    ((LtSParetoQuantileOptimization) m).setPropertyH(new Integer(h));
                    ((LtSParetoQuantileOptimization) m).setPropertyAdjust(adjust);
                    break;
                case 5:
                    m = new freak.module.fitness.pointset.LtSParetoOptimization(schedule);
                    ((LtSParetoOptimization) m).setPropertyH(new Integer(h));
                    ((LtSParetoOptimization) m).setPropertyAdjust(adjust);
                    break;
                case 6:
                    m = new freak.module.fitness.pointset.LTAOptimization(schedule);
                    ((LTAOptimization) m).setPropertyH(new Integer(h));
                    ((LTAOptimization) m).setPropertyAdjust(adjust);
                    break;
                case 7:
                    m = new freak.module.fitness.pointset.LQSOptimization(schedule);
                    ((LQSOptimization) m).setPropertyH(new Integer(h));
                    //					((LQSOptimization)m).setPropertyAdjust(adjust);
                    break;
                case 8:
                    m = new freak.module.fitness.pointset.LQDOptimization(schedule);
                    ((LQDOptimization) m).setPropertyH(new Integer(h));
                    //					((LQDOptimization)m).setPropertyAdjust(adjust);					
                    break;
                case 9:
                    m = new freak.module.fitness.pointset.LMSOptimization(schedule);
                    ((LMSOptimization) m).setPropertyH(new Integer(h));
                    ((LMSOptimization) m).setPropertyAdjust(adjust);
                    ((LMSOptimization) m).setAdjustMethodeIndex(1);
                    break;
                case 10:
                    m = new freak.module.fitness.pointset.LMSOptimization(schedule);
                    ((LMSOptimization) m).setPropertyH(new Integer(h));
                    ((LMSOptimization) m).setPropertyAdjust(adjust);
                    ((LMSOptimization) m).setAdjustMethodeIndex(2);
                    break;
                case 11:
                    m = new freak.module.fitness.pointset.LMSOptimization(schedule);
                    ((LMSOptimization) m).setPropertyH(new Integer(h));
                    ((LMSOptimization) m).setPropertyAdjust(adjust);
                    ((LMSOptimization) m).setAdjustMethodeIndex(3);
                    break;
                case 12:
                    m = new freak.module.fitness.pointset.LMSOptimization(schedule);
                    ((LMSOptimization) m).setPropertyH(new Integer(h));
                    ((LMSOptimization) m).setPropertyAdjust(adjust);
                    ((LMSOptimization) m).setAdjustMethodeIndex(4);
                    break;
                case 13:
                    m = new freak.module.fitness.pointset.LtSOptimization(schedule);
                    ((LtSOptimization) m).setPropertyH(new Integer(h));
                    ((LtSOptimization) m).setPropertyAdjust(adjust);
                    break;
                case 14:
                    m = new freak.module.fitness.pointset.LMSOptimization(schedule);
                    ((LMSOptimization) m).setPropertyH(new Integer(h));
                    ((LMSOptimization) m).setPropertyAdjust(adjust);
                    break;

            }
            m.testSchedule(schedule);
            m.initialize();
            m.createEvents();
            schedule.setFitnessFunction((FitnessFunction) m);

            // Mapper
            m = new freak.module.mapper.pointset.BitStringMapper(schedule);
            m.testSchedule(schedule);
            m.initialize();
            m.createEvents();
            schedule.setMapper((Mapper) m);

            // Stoppkriterium
            if (generationCountStop > 0) {
                GenerationCount generationCount = new GenerationCount(schedule);
                generationCount.setPropertyCount(new Integer(generationCountStop));
                generationCount.testSchedule(schedule);
                generationCount.initialize();
                generationCount.createEvents();

                StoppingCriterion[] stoppingCriteria = { generationCount };
                schedule.setStoppingCriteria(stoppingCriteria);
            }

            if (duration > 0) {
                Duration durationObject = new Duration(schedule);
                durationObject.setPropertyDuration(new Integer(duration));
                durationObject.testSchedule(schedule);
                durationObject.initialize();
                durationObject.createEvents();

                StoppingCriterion[] stoppingCriteria = { durationObject };
                schedule.setStoppingCriteria(stoppingCriteria);
            }

            // Populationmanager
            m = new freak.module.populationmanager.DefaultPopulationManager(schedule);
            m.testSchedule(schedule);
            m.initialize();
            m.createEvents();
            schedule.setPopulationManager((PopulationManager) m);

            // Operatorgraph
            OperatorGraphCollector collector = new OperatorGraphCollector(schedule);

            ModuleInfo[] graphs = null;
            if (fitnessFunction >= 13) {
                graphs = collector.getPredefinedGraphs("LTSRestart.fop");
            } else if ((fitnessFunction <= 1) || (fitnessFunction > 5)) {
                graphs = collector.getPredefinedGraphs("LTSGraph.fop");
            } else if (fitnessFunction <= 3) {
                graphs = collector.getPredefinedGraphs("LTSCrossover.fop");
            } else if (fitnessFunction <= 5) {
                graphs = collector.getPredefinedGraphs("LTSCrossMove.fop");
            }
            if (graphs != null) {
                try {
                    OperatorGraphFile ogFile;

                    String startedFrom = ClassCollector.getStartedFrom();
                    // NEW CHECK
                    if (graphs[0].getClassName().startsWith("freak") && startedFrom.toLowerCase().endsWith(".jar")) {
                        JarFile jf = new JarFile(startedFrom);
                        ogFile = OperatorGraphFile.read(jf.getInputStream(jf.getJarEntry(graphs[0].getClassName())));
                    } else {
                        ogFile = OperatorGraphFile.read(new FileInputStream(new File(graphs[0].getClassName())));
                    }
                    FreakGraphModel model = ogFile.generateGraph(schedule);
                    model.getOperatorGraph().setName(graphs[0].getName());

                    schedule.setGraphModel(model);

                } catch (Exception exc) {
                    System.out.println("Error loading graph " + graphs[0].getClassName());
                    exc.printStackTrace();
                }
            }

            // Initialisierung
            m = new freak.module.operator.initialization.LTSInitialization(schedule.getOperatorGraph());
            ((LTSInitialization) m).setPropertyNoOfBitsSetted(dimension);
            m.testSchedule(schedule);
            m.initialize();
            m.createEvents();
            schedule.setInitialization((Initialization) m);
			
            // Observer, View and Postprocessor		
            ObserverManager om = schedule.getObserverManager();
            m = new ResultObserver(schedule);

            //View
            freak.module.view.RReturn rReturn = new freak.module.view.RReturn(schedule);
            try {
                ((Observer) m).addView(rReturn);
            } catch (ObserverViewMismatchException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            m.initialize();
            m.createEvents();
            om.addObserver((Observer) m);

        } catch (UnsupportedEnvironmentException e) {
            throw new RuntimeException("Something is wrong with the default Schedule.", e);
        }

        updateBatchForSchedule(schedule, runs);

        // Update signalisieren (hat nur Auswirkungen auf den Operatorgraph)
        schedule.modulesEdited();

        return schedule;
    }

    //ScheduleConfigurator.createSchedule(jTextField.getText(),runs,generations,"./export/resource/freak/module/graph/booleanfunction/ZuFaulZumKlicken.fop");

    // USED FROM THE GPAS QUICK START GUI
    //creates the same schedule as the method createSchedule in class LogRegInterface		

    public static Schedule createSchedule(String path, int runs, int generationCountStop) {
        return createSchedule(path, runs, generationCountStop, false);
    }

    public static Schedule createSchedule(String path, int runs, int generationCountStop, boolean stoppingCriterion) {
        return createSchedule(path, runs, generationCountStop, false, "GPASGraph.fop");
    }

    public static Schedule createSchedule(String path, int runs, int generationCountStop, boolean stoppingCriterion,
            String graphFile) {

        Schedule schedule = new Schedule();

        ScheduleDependencyChecker scheduleDependencyChecker = new ScheduleDependencyChecker(null);
        scheduleDependencyChecker.setSchedule(schedule);

        Module m;
        try {
            // Suchraum
            m = new freak.module.searchspace.BooleanFunction(schedule);
            ((BooleanFunction) m).setPropertyInputPath(path);
            m.testSchedule(schedule);
            m.initialize();
            m.createEvents();
            schedule.setPhenotypeSearchSpace((SearchSpace) m);

            // Fitnessfunktion
            m = new freak.module.fitness.booleanfunction.GenericPareto(schedule);
            m.testSchedule(schedule);
            m.initialize();
            m.createEvents();
            schedule.setFitnessFunction((FitnessFunction) m);

            // Stoppkriterium
            if ((generationCountStop > 0) || (stoppingCriterion)) {
                NoNewIndividualForManyGenerations generationCount = new NoNewIndividualForManyGenerations(schedule);
                generationCount.setPropertyGenerations(new Integer(generationCountStop));
                //
                //				GenerationCount generationCount = new GenerationCount(schedule);
                //				generationCount.setPropertyCount(new Integer(generationCountStop));
                generationCount.testSchedule(schedule);
                generationCount.initialize();

                PredictingModelFound predictionFound = new PredictingModelFound(schedule);
                predictionFound.testSchedule(schedule);
                predictionFound.initialize();

                StoppingCriterion[] stoppingCriteria = null;
                if ((generationCountStop > 0) && (stoppingCriterion)) {
                    stoppingCriteria = new StoppingCriterion[2];
                    stoppingCriteria[0] = generationCount;
                    generationCount.createEvents();
                    stoppingCriteria[1] = predictionFound;
                    predictionFound.createEvents();

                } else if (generationCountStop > 0) {
                    stoppingCriteria = new StoppingCriterion[1];
                    stoppingCriteria[0] = generationCount;
                    generationCount.createEvents();
                } else {
                    stoppingCriteria = new StoppingCriterion[1];
                    stoppingCriteria[0] = predictionFound;
                    predictionFound.createEvents();
                }
                schedule.setStoppingCriteria(stoppingCriteria);
            }
            // Populationmanager
            m = new freak.module.populationmanager.DefaultPopulationManager(schedule);
            m.testSchedule(schedule);
            m.initialize();
            m.createEvents();
            schedule.setPopulationManager((PopulationManager) m);

            // Operatorgraph
            OperatorGraphCollector collector = new OperatorGraphCollector(schedule);

            ModuleInfo[] graphs = collector.getPredefinedGraphs(graphFile);
            if (graphs != null) {
                try {
                    OperatorGraphFile ogFile;

                    String startedFrom = ClassCollector.getStartedFrom();
                    // NEW CHECK
                    if (graphs[0].getClassName().startsWith("freak") && startedFrom.toLowerCase().endsWith(".jar")) {
                        JarFile jf = new JarFile(startedFrom);
                        ogFile = OperatorGraphFile.read(jf.getInputStream(jf.getJarEntry(graphs[0].getClassName())));
                    } else {
                        ogFile = OperatorGraphFile.read(new FileInputStream(new File(graphs[0].getClassName())));
                    }

                    FreakGraphModel model = ogFile.generateGraph(schedule);
                    model.getOperatorGraph().setName(graphs[0].getName());

                    schedule.setGraphModel(model);

                } catch (Exception exc) {
                    System.out.println("Error loading graph " + graphs[0].getClassName());
                    exc.printStackTrace();
                }
            }

            // Initialisierung
            m = new freak.module.operator.initialization.RandomInitialization(schedule.getOperatorGraph());
            m.testSchedule(schedule);
            m.initialize();
            m.createEvents();
            schedule.setInitialization((Initialization) m);
        } catch (UnsupportedEnvironmentException e) {
            throw new RuntimeException("Something is wrong with the default Schedule.", e);
        }

        updateBatchForSchedule(schedule, runs);

        // Update signalisieren (hat nur Auswirkungen auf den Operatorgraph)
        schedule.modulesEdited();

        currentSchedule = schedule; //the static attribute is set for further editing
        return schedule; //as well as the schedule we just created is return to the caller (ie. R)
                         //for the case that no further editing is planned 

    }

    //TODO R Return View und OBSERVER durch Methode einbauen!!!!

    //creates a default schedule
    //### die Methode baut momentan noch denselben Schedule wie die Methode createSchedule
    //### Wie sollen die Default-Werte aussehen?

    // USED WHEN STARTING THE SCHEDULE EDITOR FROM R
    public static Schedule createDefaultSchedule() {
        currentSchedule = ScheduleConfigurator.createSchedule("./data/snap.csv", 1, 0);
        Module m;
        // Observer, View and Postprocessor		
        ObserverManager om = currentSchedule.getObserverManager();
        m = new ResultObserver(currentSchedule);

        //View
        freak.module.view.RReturn rReturn = new freak.module.view.RReturn(currentSchedule);
        try {
            ((Observer) m).addView(rReturn);
        } catch (ObserverViewMismatchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        m.initialize();
        m.createEvents();
        om.addObserver((Observer) m);

        updateBatchForSchedule(currentSchedule, 1);
        return currentSchedule; //as well as the schedule we just created is return to the caller (ie. R)
    }

    /**
     * @return the editingFinished
     */
    public static boolean isEditingFinished() {
        return editingFinished;
    }

    /**
     * @param editingFinished the editingFinished to set
     */
    public static void setEditingFinished(boolean editingFinished) {
        ScheduleConfigurator.editingFinished = editingFinished;
    }

}
