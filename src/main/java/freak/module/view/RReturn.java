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

import freak.core.control.*;
import freak.core.fitness.FitnessFunction;
import freak.core.fitness.MultiObjectiveFitnessFunction;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.population.Individual;
import freak.core.view.swingsupport.*;
import freak.module.observer.ResultObserver.Result;
import freak.module.searchspace.BooleanFunction;
import freak.module.searchspace.BooleanFunctionGenotype;
import freak.module.searchspace.logictree.DNFTree;
import freak.module.searchspace.logictree.Data;
import freak.rinterface.model.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * An extension of <code>StdView</code> that writes the displayed data in an R data.frame.
 * @author  Robin
 */
public class RReturn extends StdView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SDataFrame dataFrame=new SDataFrame();
	private SAtomicCharacterVector dataSetVector=new SAtomicCharacterVector();
	private SAtomicIntegerVector runVector=new SAtomicIntegerVector();
	private SAtomicIntegerVector generationsVector=new SAtomicIntegerVector();
	private SAtomicCharacterVector individualsVector=new SAtomicCharacterVector();
	private SAtomicDoubleVector[] objectivesArray;
	private double bestValue=-Double.MAX_VALUE;
	private Vector<DNFTree> allTrees= new Vector<DNFTree>();
	
	// a counter used to generate unique default file names

	private FreakTitledBorderModel borderModel = new FreakTitledBorderModel();

	private boolean recreate;

	/**
	 * Constructs a new <code>FileWriter</code>.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public RReturn(ScheduleInterface schedule) {
		super(schedule);
	}

	public String getDescription() {
		return "Creates summarys of the Result Observer that are easy to read from R.";
	}

	public String getName() {
		return "R Return";
	}

	public void update(Object o) {
		super.update(o);		

		Result result=(Result) o;
		
		FitnessFunction fitness = getSchedule().getFitnessFunction();
		boolean singleObjective = (fitness instanceof SingleObjectiveFitnessFunction);

		int runs=(((!Data.csvDisable) && (result.getTestDataPath().equals(""))) || ((Data.csvDisable) && (Data.testData==null)))?1:2;
		
//		dataFrame.addColname("No of generations");
//		SAtomicIntegerVector noGenerationsVector=new SAtomicIntegerVector();
		
		
		if (result.getRunNumber()==1) {
			if (runs==2) dataFrame.addColname("Data Set");		
			dataFrame.addColname("Run");
			dataFrame.addColname("Generation");
			if (singleObjective) {
				dataFrame.addColname("Objective value");
				objectivesArray=new SAtomicDoubleVector[1];
				objectivesArray[0]=new SAtomicDoubleVector();
			} else {
				int objectives=((MultiObjectiveFitnessFunction)fitness).getDimensionOfObjectiveSpace();
				objectivesArray=new SAtomicDoubleVector[objectives];
				for (int j=1;j<=objectives;j++) {
					dataFrame.addColname("Objective value "+j);
					objectivesArray[j-1]=new SAtomicDoubleVector();
				}
			}			
			dataFrame.addColname("Individual");
		}
		
		
		for (int k=0;k<runs;k++) {

			// Distinguish between Test and Training
			
			if (runs==2) {
				if (k==1) {
					if (Data.csvDisable) {
						Data.setRData(Data.trainingData);
//						RReturns.setAllMCRinTrainingData(new Vector<IndividualSummary>());
					}
					else Data.setDataLocation(((BooleanFunction)schedule.getGenotypeSearchSpace()).getPropertyInputPath());
				}
				else {
					if (Data.csvDisable) {
						Data.setRData(Data.testData);
//						RReturns.setAllMCRinTestData(new Vector<IndividualSummary>());
					}					
					else Data.setDataLocation(result.getTestDataPath());
				}
				try {
					Data.readData();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			Iterator i = result.getBestIndividuals().iterator();

			while (i.hasNext()) {
				Individual individual = (Individual)i.next();					
				// Necessary if we change the data path
				if ((runs==2) && (individual.getGenotype() instanceof BooleanFunctionGenotype)) {					
					individual.setLatestKnownFitnessValue(null);
					((BooleanFunctionGenotype)individual.getGenotype()).getDnfbaum().update();
					if (Data.csvDisable) {
						if (k==1) {
							dataSetVector.add("training");
						} else {
							dataSetVector.add("test");
						}
					}
					else {
						dataSetVector.add(Data.getDataLocation());
					}
				}	
				
				runVector.add(result.getRunNumber());
				generationsVector.add(individual.getDateOfBirth());
//				noGenerationsVector.add(result.getNumberOfGenerations());

				if (singleObjective) {
					objectivesArray[0].add(((SingleObjectiveFitnessFunction)fitness).evaluate(individual, result.getBestIndividuals()));
				} else {
					double[] results = ((MultiObjectiveFitnessFunction)fitness).evaluate(individual, result.getBestIndividuals());
					for (int j = 0; j < results.length; j++) {
						objectivesArray[j].add(results[j]);
					}
				}
				individualsVector.add(individual.getGenotype().toString());
				if (individual.getGenotype() instanceof BooleanFunctionGenotype) {
					allTrees.add(((BooleanFunctionGenotype)individual.getGenotype()).getDnfbaum());
				}
/*				if ((individual.getGenotype() instanceof BitStringGenotype) && (fitness instanceof AbstractStaticSingleObjectiveRobustRegressionFitness)) {
					double fitnessValue=((AbstractStaticSingleObjectiveRobustRegressionFitness)fitness).evaluate(individual, result.getBestIndividuals());
					if (fitnessValue>this.bestValue) {
						this.bestValue=fitnessValue;
						RReturns.setResidual(-fitnessValue);
						RReturns.setFittedHyperplane(((AbstractStaticSingleObjectiveRobustRegressionFitness)fitness).getFittedHyperplane());
						RReturns.setChosenIndices(((AbstractStaticSingleObjectiveRobustRegressionFitness)fitness).getChosenIndices());
					}
				}*/
			}

		}
		
		if (getSchedule().isLastRunInBatch()) {
	
	
			if (runs==2) dataFrame.add(dataSetVector);			
			dataFrame.add(runVector);
			dataFrame.add(generationsVector);
	//		dataFrame.add(noGenerationsVector);
			for (int i=0;i<objectivesArray.length;i++) dataFrame.add(objectivesArray[i]);
			dataFrame.add(individualsVector);
			RReturns.setDataFrame(dataFrame);
			RReturns.setAllTrees(allTrees);
		}		
		
	}


	public JPanel createPanel() {
		// TODO Stefan may be headless
		recreate = true;
		JPanel panel = super.createPanel();
		TitledBorder border = new TitledBorder("");
		panel.setBorder(border);
		borderModel.setView(border);
		borderModel.setTitle("Writing to auto-generated file");
		return panel;
	}


	public void finalize() throws Throwable {
		super.finalize();
	}

	public Class[] getInputDataTypes() {
		return new Class[] { Result.class };	}

}
