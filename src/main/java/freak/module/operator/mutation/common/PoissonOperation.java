/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.common;

import cern.jet.random.Poisson;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.Cycle;
import freak.module.searchspace.Permutation;

/**
 * If we use the StandardMutation operator on the searchspace {0,1}^n the number
 * of flipping bits is asymptotically poisson distributed. This abstract class
 * is designed to construct operators for permutations/cycles which also perform
 * a number of single operations which is poisson distributed.
 *
 * @author Michael, Heiko
 */
abstract public class PoissonOperation extends Mutation implements Configurable, CompatibleWithDifferentSearchSpaces {

	/**
	 * The number of local operation is poisson distributed to the parameter 
	 * lambda.
	 */
	private double lambda;

	/**
	 * Creates a new PoissonOperation object.
	 */
	public PoissonOperation(OperatorGraph graph) {
		super(graph);
		lambda = 1;
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!((searchspace instanceof Permutation) || (searchspace instanceof Cycle)))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}

	/**
	 * Sets the property lambda.
	 */
	public void setPropertyLambda(Double lambda) {
		this.lambda = lambda.doubleValue();
	}

	/**
	 * Returns the value of lambda.
	 */
	public Double getPropertyLambda() {
		return new Double(lambda);
	}

	public String getLongDescriptionForLambda() {
		return "The number of local operations is poisson distributed to this parameter.";
	}

	/**
	 * First the number k of local operations is chosen poisson distributed to
	 * the parameter lambda. Then k+1 single operations are performed, which
	 * means that the method performSingleMutation is called k+1 times.
	 */
	protected Individual doMutation(Individual ind) {
		RandomElement re = graph.getSchedule().getRandomElement();
		Poisson p = new Poisson(lambda, re);
		int count = p.nextInt() + 1;
		for (int i = 0; i < count; i++) {
			ind = performSingleMutation(ind);
		}
		return ind;
	}

	/**
	 * This method must be overriden by the subclasses. The local single
	 * mutation has to be defined here.
	 */
	abstract protected Individual performSingleMutation(Individual ind);
}
