/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.selection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import freak.core.control.Schedule;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.GraphException;
import freak.core.graph.OperatorGraph;
import freak.core.graph.Selection;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;

/**
 * This operator removes one individual with the worst fitness from the
 * population. Let W be the multi-set of individuals which have the worst
 * fitness. An indivdual from W is removed whose genotype has the largest number
 * of copies in W.
 * 
 * @author Heiko
 */
public class SteadyStateSelection extends Selection implements CompatibleWithDifferentSearchSpaces {

	public SteadyStateSelection(OperatorGraph graph) {
		super(graph);

		super.addInPort();
		super.addOutPort();
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			throw new UnsupportedEnvironmentException("This operator works on single objective fitness functions only.");
		}
	}

	public IndividualList[] process(IndividualList[] input) throws GraphException {		
		SingleObjectiveFitnessFunction ff = (SingleObjectiveFitnessFunction)getOperatorGraph().getSchedule().getFitnessFunction();
		double worst = ff.evaluate(input[0].getIndividualWithRank(input[0].size()),input[0]);
		Individual[] list = input[0].toArray();

		HashMap map = new HashMap();
		for (int i = 0; i < list.length; i++) {
			if (ff.evaluate(list[i], input[0]) == worst) {
				Genotype g = list[i].getGenotype();
				Object o = map.get(g);
				if (o == null) {
					Vector v = new Vector();
					v.add(new Integer(i));
					map.put(g, v);
				} else {
					Vector v = (Vector)o;
					v.add(new Integer(i));
				}
			}
		}
		Iterator genotypes = map.values().iterator();
		int max = -1;
		Vector maxVector = null;
		while (genotypes.hasNext()) {
			Vector v = (Vector)genotypes.next();
			int size = v.size();
			if (size > max) {
				max = size;
				maxVector = v;
			}
		}
		int random = getOperatorGraph().getSchedule().getRandomElement().choose(0, max - 1);
		// The element maxVector(random) is removed from the list.
		int remove = ((Integer)maxVector.elementAt(random)).intValue();
		Population pop = new Population(getOperatorGraph().getSchedule());
		for (int i = 0; i < remove; i++) {
			pop.addIndividual(list[i]);
		}
		for (int i = remove + 1; i < list.length; i++) {
			pop.addIndividual(list[i]);
		}
		IndividualList[] result = { pop };
		return result;
	}

	public String getDescription() {
		return "Removes one individual with the worst fitness from the population. " + "If multiple individuals have a worst fitness value, an individual " + "among them is removed whose genotype has the largest number of clones within the individuals.";
	}

	public String getName() {
		return "Steady State Selection";
	}
}
