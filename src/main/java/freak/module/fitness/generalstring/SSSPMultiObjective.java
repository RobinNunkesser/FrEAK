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
import freak.core.fitness.AbstractStaticMultiObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;
import java.util.Iterator;

/**
 * This fitness function represents the single source shortest path problem. Each entry in the distance matrix is choosen uniformly at random from the set {1,...,maxDistance}. The components of the fitness vector are the path lengths represented by the search point. 
 * @author  Heiko
 */
public class SSSPMultiObjective extends AbstractStaticMultiObjectiveFitnessFunction implements RunEventListener, Configurable  {

	private SSSPUniformInstance instance = null;
	private int maxWeight = 10;
	private boolean instanceCreated = false;

	public SSSPMultiObjective(Schedule schedule) {
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

	protected double[] evaluate(Genotype genotype) {
		int[] gt = ((GeneralStringGenotype)genotype).getIntArray();
		try {
			double[] result = instance.getMultiFitness(gt);
			for (int i = 0; i < result.length; i++) {
				result[i] = -result[i];
			}
			return result;
		} catch (UnsupportedEnvironmentException e) {
			throw new RuntimeException("Fehler mit den SSSP-Dimensionen.");
		}
	}

	public int getDimensionOfObjectiveSpace() {
		GeneralString sp = (GeneralString)getSchedule().getGenotypeSearchSpace();
		return sp.getDimension();
	}

	public boolean containsParetoFront(IndividualList list) throws UnsupportedOperationException {
		if (!instanceCreated) {
			// This only happens when the list of available stopping criteria
			// is shown in the GUI.  
			return false;
		}
		Iterator it = list.iterator();
		double[] opt = instance.getOptimum();
		while(it.hasNext()) {
			Individual ind = (Individual)it.next();
			double[] fit = evaluate(ind,null);
			
			for (int i = 0; i < fit.length; i++) {
				if (fit[i] != -opt[i]) {
					return false;
				}
			}
		}
		return true;
	}
	
	public double[] getOptimalFitnessValue() throws UnsupportedOperationException {
		if (!instanceCreated) {
			// This only happens when the list of available stopping criteria
			// is shown in the GUI.  
			return new double[] {0};
		}
		double[] result = instance.getOptimum();
		for (int i = 0; i < result.length; i++) {
			result[i] = -result[i];
		}
		return result;
	}

	public String getName() {
		return "Multi Objective SSSP";
	}

	public String getDescription() {
		return "This fitness function represents the single source shortest path problem. "+
 			"Each entry in the distance matrix is choosen uniformly at random from the set "+
 			"{1,...,maxDistance}. The components of the fitness vector are the path lengths "+
 			"represented by the search point.";
	}
	
	public void runStarted(RunEvent evt) {
		instance.setRandomElement(getSchedule().getRandomElement());
		instance.nextInstance();
		instanceCreated = true;
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}
	
	public Integer getPropertyMaxDistance() {
		return new Integer(maxWeight);
	}
	
	public void setPropertyMaxDistance(Integer i) {
		maxWeight = i.intValue();
		instance.setMaxDistance(maxWeight);
	}

}
