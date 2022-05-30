/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.operator.selection;

import java.util.Iterator;

import freak.core.control.Schedule;
import freak.core.fitness.AbstractMultiObjectiveFitnessFunction;
import freak.core.fitness.MultiObjectiveFitnessFunction;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.GraphException;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Selection;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.module.searchspace.BooleanFunctionGenotype;

/**
 * This selection operator iterates through all individuals in the second inport.
 * It first checks whether the next individual in the list is dominated by any
 * individual contained in the current population or not.
 * In the latter case the individual is added to the population and all
 * individuals that are dominated by it are removed.
 * @author Heiko, modified for more than one individual in second inport by Melanie
 */
public class ParetoFrontSelection extends Selection implements CompatibleWithDifferentSearchSpaces {

	public ParetoFrontSelection(OperatorGraph graph) {
		super(graph);
		
		super.addInPort();
		super.addInPort();
		super.addOutPort();
	}

	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof MultiObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This operator works on multi objective fitness functions only.");
		}
	}

	
	public IndividualList[] process(IndividualList[] input) throws GraphException {

		// create resulting list
		IndividualList[] result = new IndividualList[1];
		
		// no new individuals -> no change
		if (input[1].size() == 0) {
			result[0] = input[0];
			return result;
		}

		// the current-list contains all individuals of the population
		// after the ith iteration, i.e. the left inport before 
		// the first iteration
		IndividualList[] current = new IndividualList[1];
		current[0] = input[0];

		// iterate through all new individuals
		Iterator it2 = input[1].iterator();
		while (it2.hasNext()){
						
			boolean isDominated = false;
			
			// get individual and fitness
			Individual currentInd = (Individual)it2.next();
			MultiObjectiveFitnessFunction ff = (MultiObjectiveFitnessFunction)getOperatorGraph().getSchedule().getFitnessFunction();
			double[] fitness = ff.evaluate(currentInd,input[0]);
			int[] comp = new int[current[0].size()];
;
			
//			BooleanFunctionGenotype bfgcurrent = (BooleanFunctionGenotype)currentInd.getGenotype();
//			System.out.println("neues Ind: "+bfgcurrent.toString()+" "+fitness[0]+" "+fitness[1]);
			
			// iterate through all individuals in the current population
			Iterator it = current[0].iterator();
			int i = 0;
			while(it.hasNext()) {
				Individual ind = (Individual)it.next();
				double[] fitness2 = ff.evaluate(ind,current[0]);
				comp[i] = AbstractMultiObjectiveFitnessFunction.compare(fitness2,fitness);
				if (comp[i] >= 0) {
				// individual is dominated and will not be added to the population	
					isDominated = true;
				}
				i++;
//				BooleanFunctionGenotype bfg = (BooleanFunctionGenotype)ind.getGenotype();
//				System.out.println(bfg.toString()+" "+fitness2[0]+" "+fitness2[1]);
			}
			
			if (isDominated) {
				// no change in the population
			}
			else {
				
				// create new population:
				// first add new individual (which is not dominated by the old ones)
				// then add all old individuals that are not dominated be the new one

				Population pop = new Population(getOperatorGraph().getSchedule());
				IndividualList[] current2 = new IndividualList[1];
				current2[0] = pop;
				pop.addIndividual(currentInd);
				it = current[0].iterator();
				i = 0;
				while (it.hasNext()) {
					Individual ind = (Individual) it.next();
					if (comp[i] == AbstractMultiObjectiveFitnessFunction.UNCOMPARABLE) {
						pop.addIndividual(ind);
					}
					i++;
				}
				
				// set new population to current population
				current[0] = current2[0];
			}			
		}

		result = current;
		//System.out.println();
		return result;
	}

	public String getName() {
		return "Pareto Front Selection";
	}

	public String getDescription() {
		return "This selection operator iterates through all individuals in the second inport."+
				"It first checks whether the next individual in the list is dominated by any "+
				"individual contained in the current population or not. "+
				"In the latter case the individual is added to the population and all "+
				"individuals that are dominated by it are removed.";
	}

}
