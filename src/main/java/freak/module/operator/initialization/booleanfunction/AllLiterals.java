/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.operator.initialization.booleanfunction;

import freak.core.control.Schedule;
import freak.core.fitness.AbstractMultiObjectiveFitnessFunction;
import freak.core.fitness.MultiObjectiveFitnessFunction;
import freak.core.graph.Initialization;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.module.searchspace.BooleanFunction;
import freak.module.searchspace.logictree.Data;

import java.util.Iterator;

public class AllLiterals extends Initialization  {
	
	public AllLiterals(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		if (!(graph.getSchedule().getGenotypeSearchSpace() instanceof BooleanFunction)){
				throw new UnsupportedEnvironmentException("Works only on the SearchSpace Boolean Function.");			
		}
	}
	
	public IndividualList[] process(IndividualList[] origin) {
		// create the resulting array of individual lists
		IndividualList[] result = new IndividualList[1];
		BooleanFunction sp = (BooleanFunction) graph.getSchedule().getGenotypeSearchSpace();		
		result[0] = new Population(graph.getSchedule());//, Data.getNumCompareNodes());
		result[0].addIndividual(new Individual(graph.getSchedule(), sp.getRandomGenotype(), null)); //only to in initialize data reading
		// now create the individuals	   
		for (int i = 0; i < Data.getNumCompareNodes(); i++) {	
			MultiObjectiveFitnessFunction ff = (MultiObjectiveFitnessFunction)getOperatorGraph().getSchedule().getFitnessFunction();
			Individual individual=new Individual(graph.getSchedule(), sp.getLiteral(i), null);
			if (i%1000==0) System.out.println(i+" "+individual.toString());
			double[] fitness = ff.evaluate(individual,result[0]);
			int[] comp = new int[result[0].size()];
			Iterator it = result[0].iterator();
			int j = 0;
			boolean add=true;
			while(it.hasNext()) {
				Individual ind = (Individual)it.next();
				double[] fitness2 = ff.evaluate(ind,result[0]);
				comp[j] = AbstractMultiObjectiveFitnessFunction.compare(fitness2,fitness);
				if (comp[j] >= 0) {
					add=false;
					break; 	
				}
				j++;
			}
			
			if (add) {
				Population pop = new Population(getOperatorGraph().getSchedule());
				pop.addIndividual(individual); 
				it = result[0].iterator();
				j = 0;
				while(it.hasNext()) {
					Individual ind = (Individual)it.next();
					if (comp[j] == AbstractMultiObjectiveFitnessFunction.UNCOMPARABLE) {
						pop.addIndividual(ind);		
					}
					j++;		
				}	
				result[0] = pop;			
			}
			//result[0].addIndividual(new Individual(graph.getSchedule(), sp.getLiteral(i), null));
		}
		return result;
	}
			
	public String getName() {
		return "All Literals";
	}
	
	public String getDescription() {
		return "Creates all possible not dominated literals";
	}
}
