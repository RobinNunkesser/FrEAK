/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.crossover.common;

import freak.core.control.Schedule;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.searchspace.SearchSpace;
import freak.module.operator.crossover.MultiPairwiseCrossover;
import freak.module.searchspace.BooleanFunction;
import freak.module.searchspace.BooleanFunctionGenotype;
import freak.module.searchspace.logictree.OperatorNodeVector;


/**
 * Each character is taken with probability 1/2 from the first parent and with
 * probability 1/2 from the second.
 *
 * This Operator works on bitstring and generalstring.
 *
 * @author Michael
 */
public class MergeCrossover extends MultiPairwiseCrossover implements CompatibleWithDifferentSearchSpaces {
	
	public MergeCrossover(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!(searchspace instanceof BooleanFunction))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}
	
	/**
	 * Performs a uniform crossover of ind1 and ind2.
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		
		BooleanFunctionGenotype bf1 = (BooleanFunctionGenotype)ind1.getGenotype();
		BooleanFunctionGenotype bf2 = (BooleanFunctionGenotype)ind2.getGenotype();

		BooleanFunctionGenotype bfout = (BooleanFunctionGenotype)bf1.clone();
		
		OperatorNodeVector onodes = bf2.getCopyOfAllUsedAndNodes();
		
		bfout.insertListOfAndNodes(onodes);
		//bfout.mergeWith(bf2);

//		System.out.println(bf1+" "+bf2+" "+bfout);
		
		return new Individual(graph.getSchedule(), bfout, new Individual[] { ind1, ind2 });
		
	}
	
	public String getDescription() {
		return "Merges the AndNodes of two individuals";
	}
	
	public String getName() {
		return "Crossover by Merging";
	}
	
}
