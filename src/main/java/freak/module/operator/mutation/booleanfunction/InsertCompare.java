/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.operator.mutation.booleanfunction;

import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.population.Individual;
import freak.module.searchspace.BooleanFunctionGenotype;
import freak.module.searchspace.logictree.AndNode;
import freak.module.searchspace.logictree.StaticCompareNode;


public class InsertCompare extends Mutation{

	public InsertCompare(OperatorGraph graph) {
		super(graph);
	}
	
	protected Individual doMutation (Individual ind){
		BooleanFunctionGenotype bg = (BooleanFunctionGenotype)ind.getGenotype();
		BooleanFunctionGenotype copy = (BooleanFunctionGenotype)bg.clone();
		AndNode an = copy.getUsedAndNodeRandomly();
		if (an != null){
			StaticCompareNode cn = copy.getExistingCompareNodeRandomly();
			copy.insertCompare(an,cn);
		}
//		copy.insertCompare();
		return new Individual(graph.getSchedule(), copy, new Individual[] { ind });
		
	}
	
	public String getName(){
		return "InsertCompare";
	}
	
	public String getDescription(){
		return "Creates a new compareNode.";
	}
	
}