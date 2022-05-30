/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.generalstring;

import cern.jet.random.Poisson;
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
 * This mutation operator works only if one of the two SSSP fitness functions is
 * selected. First, it chooses a random value k from the poisson distribution
 * with parameter lambda = 1. Then, it performs k local mutations
 * (see SSSPLocalMutation).  
 * @author Heiko
 */
public class SSSPGlobalMutation extends Mutation {

	public SSSPGlobalMutation(OperatorGraph graph) {
		super(graph);
	}

	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		FitnessFunction f = schedule.getRealFitnessFunction();
		if (!(f instanceof SSSPMultiObjective) && !(f instanceof SSSPSingleObjective)) {
			throw new UnsupportedEnvironmentException("Only works in connection with a SSSP fitness function.");
		}
	}

	public int[] flipps;

	protected Individual doMutation(Individual ind) {
		GeneralStringGenotype geno = (GeneralStringGenotype)((GeneralStringGenotype)(ind.getGenotype())).clone();
		int[] gt = geno.getIntArray();
		int dimension = ((HasDimension)(graph.getSchedule().getGenotypeSearchSpace())).getDimension();

		RandomElement re = graph.getSchedule().getRandomElement();
		Poisson pd = new Poisson(1,re);
		int steps = pd.nextInt()+1;
		
		for (int i = 0; i < steps; i++) {
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
		}
		
		return new Individual(graph.getSchedule(), geno, new Individual[] { ind });
	}

	public String getName() {
		return "SSSP Global Mutation";
	}

	public String getDescription() {
		return "This operator chooses first a random value k from the poisson distribution "+
            "with parameter lambda = 1. Then, it performs k local mutations (see SSSPLocalMutation).";
	}

}
