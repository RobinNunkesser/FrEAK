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
import freak.module.searchspace.logictree.StaticCompareNode;


/**
 * Each character is taken with probability 1/2 from the first parent and with
 * probability 1/2 from the second.
 *
 * This Operator works on bitstring and generalstring.
 *
 * @author Michael
 */
public class MonomMerge extends MultiPairwiseCrossover implements CompatibleWithDifferentSearchSpaces {
	
	public MonomMerge(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!(searchspace instanceof BooleanFunction))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}
	
	/**
	 * Performs a merge of two and-nodes from two individuals.
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		
		BooleanFunctionGenotype bf1_alt = (BooleanFunctionGenotype)ind1.getGenotype();
		BooleanFunctionGenotype bf2_alt = (BooleanFunctionGenotype)ind2.getGenotype();
		
		BooleanFunctionGenotype bf1 = (BooleanFunctionGenotype) bf1_alt.clone();
		BooleanFunctionGenotype bf2 = (BooleanFunctionGenotype) bf2_alt.clone();

		AndNode an1 = bf1.getUsedAndNodeRandomly();
		AndNode an2 = bf2.getUsedAndNodeRandomly();
				
		StaticCompareNode cn;
		
		while(an2.getSubtreeSize()>1)
		{
		  cn=an2.getRandomChildComp();
	      bf2.deleteCompare(an2,cn);
    	  bf1.insertCompare(an1,cn);
		}
		// the last CompareNode cannot be deleted
        cn=an2.getRandomChildComp(); 
    	bf1.insertCompare(an1,cn);	
		
		//System.out.println("Merge: "+bf1_alt+" "+bf2_alt+" "+bf1);
		
		return new Individual(graph.getSchedule(), bf1, new Individual[] { ind1, ind2 });
		
	}
	
	public String getDescription() {
		return "Gets Monoms from two Individuals and merges them. " +
				"The old monoms are deleted and the new monom is put into the left individual";
	}
	
	public String getName() {
		return "MonomMerge";
	}
	
}
