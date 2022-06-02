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


public class SetPopulation extends Mutation{

	private int population = 0;
	
	public SetPopulation(OperatorGraph graph) {
		super(graph);
	}
	
	protected Individual doMutation (Individual ind){
		BooleanFunctionGenotype bg = (BooleanFunctionGenotype)ind.getGenotype();
		BooleanFunctionGenotype copy = (BooleanFunctionGenotype)bg.clone();
		
		copy.setPopulation(population);
		//System.out.println("Population set to "+population);
		// copy.deleteAnd();
		return new Individual(graph.getSchedule(), copy, new Individual[] { ind });
		
	}
	
	public String getName(){
		return "SetPopulation";
	}
	
	public String getDescription(){
		return "Sets the population an individual is in.";
	}
	
	public void setPropertyPopulation(Integer p){
		population = p.intValue();
	}
	
	public Integer getPropertyPopulation(){
		return new Integer(population);
	}
	
	public String getLongDescriptionForPopulation() {
		return "The number of the population the indiviual shall be in.";
	}
	
}