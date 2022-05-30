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


public class DeleteAnd extends Mutation{

	public DeleteAnd(OperatorGraph graph) {
		super(graph);
	}
	
	protected Individual doMutation (Individual ind){
		BooleanFunctionGenotype bg = (BooleanFunctionGenotype)ind.getGenotype();
		BooleanFunctionGenotype copy = (BooleanFunctionGenotype)bg.clone();
		
		AndNode an = copy.getUsedAndNodeRandomly();
		copy.deleteAnd(an);
		// copy.deleteAnd();
		return new Individual(graph.getSchedule(), copy, new Individual[] { ind });
		
	}
	
	public String getName(){
		return "DeleteAnd";
	}
	
	public String getDescription(){
		return "Deletes a random AndNode";
	}
	
}