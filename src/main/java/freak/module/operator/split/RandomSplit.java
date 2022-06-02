/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.split;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.core.modulesupport.Module;
import freak.core.population.*;

import java.util.Iterator;

/**
 * This class implements the splitting of a <code>IndividualList</code>. One
 * <code>individualList</code> is given and <code>noOfListsToGenerate</code>
 * list are returned. It takes the individuals of <code>origin</code> and 
 * chooses uniform randomly the individuals for each <code>individualList</code>.
 * 
 * Remark: The number of individuals in each list is also a random number.
 *
 * @author Christian, Heiko, Michael
 */
public class RandomSplit extends Split implements HasFloatingNumberOfOutPorts,
		Module, CompatibleWithDifferentSearchSpaces {

	/**
	 * The constructor of the class <code>RandomSplit</code>.
	 * 
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public RandomSplit(OperatorGraph graph) {
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
		RandomElement randomGen = graph.getSchedule().getRandomElement();

		while (iterator.hasNext()) {
			// take the next individual ...
			ind = (Individual)iterator.next();

			// ... calculate the correct list ...
			int list = randomGen.choose(0, noOfListsToGenerate - 1);

			// ... and put it in there
			result[list].addIndividual(ind);
		}

		return result;
	}

	public String getName() {
		return "RandomSplit";
	}

	public String getDescription() {
		return "Splits the incoming individuals into several output-lists.";
	}

}
