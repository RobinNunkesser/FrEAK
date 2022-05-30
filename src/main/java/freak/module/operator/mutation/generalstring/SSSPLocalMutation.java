/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.generalstring;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.fitness.FitnessFunction;
import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.module.fitness.generalstring.SSSPMultiObjective;
import freak.module.fitness.generalstring.SSSPSingleObjective;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * This mutation works operator only if one of the two SSSP fitness functions is
 * selected. It performs one local mutation. It differs from the 1-Char-Mutation
 * only in the point that the value of the i-th character is never set to i. 
 * 
 * @author Heiko
 */
public class SSSPLocalMutation extends Mutation {

	public SSSPLocalMutation(OperatorGraph graph) {
		super(graph);
	}

	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		FitnessFunction f = schedule.getRealFitnessFunction();
		if (!(f instanceof SSSPMultiObjective) && !(f instanceof SSSPSingleObjective)) {
			throw new UnsupportedEnvironmentException("Only works in connection with a SSSP fitness function.");
		}
	}

	protected Individual doMutation(Individual ind) {
		GeneralStringGenotype geno = (GeneralStringGenotype)((GeneralStringGenotype)(ind.getGenotype())).clone();
		int[] gt = geno.getIntArray();
		int dimension = ((HasDimension)(graph.getSchedule().getGenotypeSearchSpace())).getDimension();

		RandomElement re = graph.getSchedule().getRandomElement();
		// first the position to be mutated is choosen
		int pos = re.choose(0,dimension-1);
		// now the new value is choosen randomly
		int min, max;
		if (gt[pos] > pos) {
			min = pos;
			max = gt[pos];
		} else {
			min = gt[pos];
			max = pos;			
		}
		int newValue = re.choose(0,dimension-2);
		if (newValue >= min) {
			newValue++;
		}
		if (newValue >= max) {
			newValue++;
		}
		gt[pos] = newValue;
		
		return new Individual(graph.getSchedule(), geno, new Individual[] { ind });
	}

	public String getName() {
		return "SSSP Local Mutation";
	}

	public String getDescription() {
		return "This operator differs from the 1-Char-Mutation only in the point "+
		   "that the value of the i-th character is never set to i.";
	}

}
