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

import java.util.Iterator;

/**
 * This selection operator first checks whether the newly created individuals is
 * dominated by an individual contained in the current population or not. In the
 * latter case the individual is added to the population and all individuals
 * which are dominated by the new individual are removed from the population.
 * 
 * @author Heiko
 */
public class DominationSelection extends Selection implements CompatibleWithDifferentSearchSpaces {

//	public static boolean excludeEqual=false;
	public DominationSelection(OperatorGraph graph) {
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
		boolean excludeEqual=true;
		IndividualList[] result = new IndividualList[1];
		if (input[1].size() != 1) {
			throw new GraphException("The number of individuals sent to inport 2 of "+ name + " must be exactly 1 not "+input[1].size()+".");
		}
		
		MultiObjectiveFitnessFunction ff = (MultiObjectiveFitnessFunction)getOperatorGraph().getSchedule().getFitnessFunction();
		double[] fitness = ff.evaluate(input[1].getIndividual(0),input[0]);
		int[] comp = new int[input[0].size()];
		
		Iterator it = input[0].iterator();
		int i = 0;
		while(it.hasNext()) {
			Individual ind = (Individual)it.next();
			double[] fitness2 = ff.evaluate(ind,input[0]);
			comp[i] = AbstractMultiObjectiveFitnessFunction.compare(fitness2,fitness);
			// Special treatment for Equality
			// The test for equality is not complete but intended to be fast
			// For BooleanFunctionGenotypes equality is defined by consisting of the same literals
			if ((!excludeEqual) && (comp[i]==0) && (!ind.getGenotype().toString().equals(input[1].getIndividual(0).getGenotype().toString()))) { //(ind.getDateOfBirth()!=input[1].getIndividual(0).getDateOfBirth())) {
				comp[i] = AbstractMultiObjectiveFitnessFunction.UNCOMPARABLE;
				if (ind.getGenotype() instanceof BooleanFunctionGenotype) {
					if (((BooleanFunctionGenotype)ind.getGenotype()).getDnfbaum().getLiteralBitSet().equals(((BooleanFunctionGenotype)input[1].getIndividual(0).getGenotype()).getDnfbaum().getLiteralBitSet())) comp[i]=0;  
				}
			}
			if (comp[i] >= 0) {
				result[0] = input[0];
				return result; 	
			}
			i++;
		}
		Population pop = new Population(getOperatorGraph().getSchedule());
		result[0] = pop;
		pop.addIndividual(input[1].getIndividual(0)); 
		it = input[0].iterator();
		i = 0;
		while(it.hasNext()) {
			Individual ind = (Individual)it.next();
			if (comp[i] == AbstractMultiObjectiveFitnessFunction.UNCOMPARABLE) {
				pop.addIndividual(ind);		
			}
			i++;		
		}
		return result;
	}

	public String getName() {
		return "Domination Selection";
	}

	public String getDescription() {
		return "This selection operator first checks whether the newly created individuals is "+
			"dominated by an individual contained in the current population or not. In the "+
			"latter case the individual is added to the population and all individuals "+
			"which are dominated by the new individual are removed from the population.";
	}

}
