/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */


// TODO LŠdt bei jedem Aufruf die Daten neu!!!!

package freak.module.operator.mutation.booleanfunction;

import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.population.Individual;
import freak.module.searchspace.BooleanFunctionGenotype;
import freak.module.searchspace.logictree.AndNode;


public class UniformExcludeMonom extends Mutation{

	public UniformExcludeMonom(OperatorGraph graph) {
		super(graph);
	}
	
	protected Individual doMutation (Individual ind){

		// get Genotype from first individual 
		// and create new genotype for new individual with the same parameters as the given one
		BooleanFunctionGenotype bg = (BooleanFunctionGenotype)ind.getGenotype();
		BooleanFunctionGenotype nbg = new BooleanFunctionGenotype(bg.getInputFilePath(),bg.getSchedule());
		
		// if empty trees are forbidden the newly created genotype contains an AndNode
		AndNode delete = nbg.getUsedAndNodeRandomly();
		
		// get random Andnode and insert it into the new individual 
		AndNode an = bg.getUsedAndNodeRandomly();
		nbg.insertAnd(an);
		
		// if there was an AndNode in the tree before, delete it
		if (delete != null)
			nbg.deleteAnd(delete);

		// return new individual
		return new Individual(graph.getSchedule(), nbg, new Individual[] { ind });
		
	}
	
	public String getName(){
		return "UniformExcludeMonom";
	}
	
	public String getDescription(){
		return "Creates a new individual with one of the monoms of the given individual.";
	}
	
}