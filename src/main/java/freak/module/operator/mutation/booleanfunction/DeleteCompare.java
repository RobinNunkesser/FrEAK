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


public class DeleteCompare extends Mutation{

	public DeleteCompare(OperatorGraph graph) {
		super(graph);
	}
	
	protected Individual doMutation (Individual ind){
		BooleanFunctionGenotype bg = (BooleanFunctionGenotype)ind.getGenotype();
		BooleanFunctionGenotype copy = (BooleanFunctionGenotype)bg.clone();
		AndNode an = copy.getUsedAndNodeRandomly();
		StaticCompareNode cn;
		if (an!=null)  cn = copy.getUsedCompareNodeRandomly(an); else cn=null;
		if (cn!=null) copy.deleteCompare(an,cn);
//		copy.deleteCompare();
		return new Individual(graph.getSchedule(), copy, new Individual[] { ind });
		
	}
	
	public String getName(){
		return "DeleteCompare";
	}
	
	public String getDescription(){
		return "Deletes a random CompareNode";
	}
	
}