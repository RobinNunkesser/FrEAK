/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.operator.split.booleanfunction;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.graph.*;
import freak.core.population.*;

import java.util.Iterator;

/**
 * This class implements the splitting of a <code>IndividualList</code>. One
 * <code>individualList</code> is given and two
 * lists are returned. The first list only contains one random individual.
 * The other list contains all other individuals.
 * 
 * @author Christian, Heiko, Michael modified by Melanie
 */
public class SplitOneLeft extends Split {

	/**
	 * The constructor of the class <code>SplitOneLeft</code>.
	 * 
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public SplitOneLeft(OperatorGraph graph) {
		super(graph);
		addOutPort();
		addOutPort();
	}

	/**
	 * This method does the splitting described above.
	 * 
	 * @param origin the original list of individuals to be splitted.
	 * @return a list of lists of individuals.
	 */
	public IndividualList[] process(IndividualList[] origin) {
		
		// create output lists
		IndividualList result[] = new IndividualList[2];
		result[0] = new Population(graph.getSchedule());
		result[1] = new Population(graph.getSchedule());

		// randomly choose an individual
		RandomElement re = getOperatorGraph().getSchedule().getRandomElement();
		
		int chosenInd = re.choose(0,origin[0].size()-1);
		
		// send individuals to the assigned lists
		
		Iterator iterator = origin[0].iterator();
		Individual ind = null;
		int i = 0;
		
		while (iterator.hasNext()) {
			// take the next individual ...
			ind = (Individual)iterator.next();

			// send ind to left outport if it is the chosen individual
			// and to the right outport elsewise
			if (i == chosenInd) 
				result[0].addIndividual(ind);
			else
				result[1].addIndividual(ind);
			i++;
		}

		return result;
	}

	public String getName() {
		return "SplitOneLeft";
	}

	public String getDescription() {
		return "Splits the individual list into two lists." +
				"The first outport gets one random individual. " +
				"All other individuals are send to the second outport.";
	}

}
