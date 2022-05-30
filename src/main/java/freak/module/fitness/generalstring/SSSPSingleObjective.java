/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.fitness.generalstring;

import freak.core.control.Schedule;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Genotype;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * This fitness function represents the single source shortest path problem. Each entry in the distance matrix is choosen uniformly at random from the set {1,...,maxDistance}. The fitness of a search point x is the sum of the lengths of the paths, if x represents a tree. Otherwise, for all nodes not connected to the source s a penalty term is added.
 * @author  Heiko
 */
public class SSSPSingleObjective extends AbstractStaticSingleObjectiveFitnessFunction implements RunEventListener, Configurable {

	private SSSPUniformInstance instance = null;
	private int maxWeight = 10;
	private boolean instanceCreated = false;

	public SSSPSingleObjective(Schedule schedule) {
		super(schedule);

		GeneralString sp = (GeneralString)getSchedule().getGenotypeSearchSpace();
		instance = new SSSPUniformInstance(sp.getDimension()+1,schedule.getRandomElement(),10);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		GeneralString sp = (GeneralString)getSchedule().getGenotypeSearchSpace();
		if (sp.getPropertyNumberOfChars().intValue()!=sp.getDimension()+1) {
			throw new UnsupportedEnvironmentException("The number of characters must equals the dimension+1.");
		}
	}

	protected double evaluate(Genotype genotype) {
		try {		
			int[] gt = ((GeneralStringGenotype)genotype).getIntArray();
			double[] distances = instance.getMultiFitness(gt);
			double result = 0;
			int numNonPath = 0;
			for (int i = 0; i < distances.length; i++) {
				if (distances[i] == Double.POSITIVE_INFINITY) {
					numNonPath++;
				} else {
					result = result + distances[i];
				}
			}
			return -(result+numNonPath*instance.getLargestMatrixEntry()*(gt.length+1));
		} catch(Exception e) {
			System.out.println(e.getStackTrace());
			throw new RuntimeException("Fehler mit den SSSP-Dimensionen.");			
		}
	}

	public String getName() {
		return "Single Objective SSSP";
	}

	public String getDescription() {
		return "This fitness function represents the single source shortest path problem. "+
			"Each entry in the distance matrix is choosen uniformly at random from the set {1,...,maxDistance}. "+
			"The fitness of a search point x is the sum of the lengths of the paths, if "+ 
 			"x represents a tree. Otherwise, for all nodes not connected to the source s a "+
 			"penalty term is added.";
	}
	
	public Integer getPropertyMaxDistance() {
		return new Integer(maxWeight);
	}
	
	public void setPropertyMaxDistance(Integer i) {
		maxWeight = i.intValue();
		instance.setMaxDistance(maxWeight);
	}
	
	public double getOptimalFitnessValue() {
		if (!instanceCreated) {
			// This only happens when the list of available stopping criteria
			// is shown in the GUI.  
			return 0;
		}
		double[] opt = instance.getOptimum();
		double result = 0;
		for (int i = 0; i < opt.length; i++) {
			result = result + opt[i];	
		}
		return -result;
	}

	public void runStarted(RunEvent evt) {
		instance.setRandomElement(getSchedule().getRandomElement());
		instance.nextInstance();
		instanceCreated = true;
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

}
