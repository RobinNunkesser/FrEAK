/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.operator.split.booleanfunction;

import freak.core.graph.*;
import freak.core.population.*;
import freak.module.searchspace.BooleanFunctionGenotype;

import java.util.Iterator;

/**
 * This class implements the splitting of a <code>IndividualList</code>. One
 * <code>individualList</code> is given and <code>noOfListsToGenerate</code>
 * lists are returned. The class takes the individuals of <code>origin</code> and 
 * splits them according to the population they are in.
 * 
 * @author Christian, Heiko, Michael modified by Melanie
 */
public class PopulationSplit extends Split implements HasFloatingNumberOfOutPorts {

	/**
	 * The constructor of the class <code>PopulationSplit</code>.
	 * 
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public PopulationSplit(OperatorGraph graph) {
		super(graph);
	}

	/**
	 * This method does the splitting described above.
	 * 
	 * @param origin the original list of individuals to be splitted.
	 * @return a list of lists of individuals.
	 */
	public IndividualList[] process(IndividualList[] origin) {
		int noOfListsToGenerate = getNumberOfOutPorts();
		if (noOfListsToGenerate == 0) {
			return new IndividualList[0];
		}
		if (noOfListsToGenerate == 1) {
			return origin;
		}

		// IndividualList should be just one List (==> array of length 1) !!!
		Iterator iterator = origin[0].iterator();

		// create enough lists
		IndividualList result[] = new IndividualList[noOfListsToGenerate];
		for (int i = 0; i < noOfListsToGenerate; i++) {
			result[i] = new Population(graph.getSchedule());
		}

		// now run through the list of individuals and put each individual in
		// correct list
		Individual ind = null;
		
		while (iterator.hasNext()) {
			// take the next individual ...
			ind = (Individual)iterator.next();

			// ... calculate the correct list ...
			BooleanFunctionGenotype bfg = (BooleanFunctionGenotype) ind.getGenotype();
			int list =  bfg.getPopulation();

			if (list >= noOfListsToGenerate) list = noOfListsToGenerate-1;
			
			// ... and put it in there
			result[list].addIndividual(ind);
		}

		return result;
	}

	public String getName() {
		return "PopulationSplit";
	}

	public String getDescription() {
		return "Splits the individual list into lists according to their population." +
				"The first outport gets a list of all individuals from population 0," +
				"the second all from population 1 and so on." +
				"The last output additionally contains all individuals from populations that have a number" +
				">= the number of outputs.";
	}

}
