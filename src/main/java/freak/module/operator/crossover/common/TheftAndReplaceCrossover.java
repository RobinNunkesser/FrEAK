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
import freak.module.searchspace.logictree.AndNode;


/**
 * Each character is taken with probability 1/2 from the first parent and with
 * probability 1/2 from the second.
 *
 * This Operator works on bitstring and generalstring.
 *
 * @author Michael
 */
public class TheftAndReplaceCrossover extends MultiPairwiseCrossover implements CompatibleWithDifferentSearchSpaces {
	
	public TheftAndReplaceCrossover(OperatorGraph graph) {
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
		
		// save a reandom and
		
		AndNode anDel = bfout.getUsedAndNodeRandomly();
		
		// insert an and from the other tree
		
		AndNode an = bf2.getUsedAndNodeRandomly();
		bfout.insertAnd(an);
		
//		System.out.println(bf1+" "+bf2+" "+bfout);
		
		// delete the marked and (can now be deleted even if emptytreeforbidden because new and is there)
		
		bfout.deleteAnd(anDel);
		
		return new Individual(graph.getSchedule(), bfout, new Individual[] { ind1, ind2 });
		
	}
	
	public String getDescription() {
		return "Adds one AndNode from the second individual to the first individual and deletes an AndNode in the first one.";
	}
	
	public String getName() {
		return "TheftCrossoverWithReplace";
	}
	
}
