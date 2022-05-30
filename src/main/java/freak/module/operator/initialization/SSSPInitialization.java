/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.initialization;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.fitness.FitnessFunction;
import freak.core.graph.GraphException;
import freak.core.graph.Initialization;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.module.fitness.generalstring.SSSPMultiObjective;
import freak.module.fitness.generalstring.SSSPSingleObjective;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * This initialization differs from the random initialization on general strings
 * in only one point. Namely, there will be no indices i such that x_i is set to
 * i. This special initialization is used for SSSP. 
 * 
 * @author Heiko
 */
public class SSSPInitialization extends Initialization {

	private int size = 1;

	public SSSPInitialization(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		FitnessFunction f = schedule.getRealFitnessFunction();
		if (!(f instanceof SSSPMultiObjective) && !(f instanceof SSSPSingleObjective)) {
			throw new UnsupportedEnvironmentException("Only works in connection with a SSSP fitness function.");
		}
		if (!(schedule.getGenotypeSearchSpace() instanceof GeneralString)) {
			throw new UnsupportedEnvironmentException("Only works on the search space GeneralString.");
		}
		GeneralString sp = (GeneralString)schedule.getGenotypeSearchSpace();
		if (sp.getPropertyNumberOfChars().intValue()!=sp.getDimension()+1) {
			throw new UnsupportedEnvironmentException("The number of characters must equals the dimension+1.");
		}
	}

	public IndividualList[] process(IndividualList[] input) throws GraphException {
		IndividualList[] result = new IndividualList[1];
		result[0] = new Population(graph.getSchedule(), size);
		// now create the individuals	   
		GeneralString sp = (GeneralString) graph.getSchedule().getGenotypeSearchSpace();
		int numChar = sp.getPropertyNumberOfChars().intValue();
		int dimension = sp.getDimension();
		RandomElement re = graph.getSchedule().getRandomElement();
		for (int i = 0; i < size; i++) {
			// create a random genotype fulfilling the desired property
			int[] gt = new int[dimension]; 
			for (int j = 0; j < dimension; j++) {
				gt[j] = re.choose(0, numChar - 2);
				if (gt[j]>=j) {
					gt[j]++;
				}
			}
			GeneralStringGenotype genotype = new GeneralStringGenotype(gt,numChar); 
			result[0].addIndividual(new Individual(graph.getSchedule(), genotype, new Individual[] {} ));
		}
		return result;
	}

	public String getName() {
		return "SSSP Initialization";
	}

	public String getDescription() {
		return "This initialization differs from the random initialization on general strings "+
 			"in only one point. Namely, there will be no indices i such that x_i is set to i"; 			
	}

	public Integer getPropertySize() {
		return new Integer(size);
	}

	public void setPropertySize(Integer no) {
		if (no.intValue() > 0)
			size = no.intValue();
	}
	
	public String getLongDescriptionForSize() {
		return "The number of individuals to be created.";
	}


}
