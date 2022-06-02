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
import freak.core.population.*;

/**
 * This operators splits the set of incoming individuals randomly into a given
 * number of subsets of equal size. 
 * 
 * @author Heiko
 */
public class RandomPartition extends Split implements HasFloatingNumberOfOutPorts, CompatibleWithDifferentSearchSpaces {

	private boolean exact;

	public RandomPartition(OperatorGraph graph) {
		super(graph);
	}

	public IndividualList[] process(IndividualList[] input) throws GraphException {
		// First, check some special cases
		int outPorts = getNumberOfOutPorts();
		if (outPorts == 0) {
			return new IndividualList[0];
		}
		if (outPorts == 1) {
			return input;
		}
		// The number of incoming individuals must be multiple of the number of
		// out ports.
		int inputLength = input[0].size();
		if (exact && (inputLength % outPorts != 0)) {
			throw new GraphException("The number of individuals (" + inputLength + ") is not a multiple of the number of outports (" + outPorts + ").");
		}
		// Now the random partition is performed.
		int sizeOfSubSet = inputLength / outPorts;
		int bound = inputLength;
		RandomElement re = getOperatorGraph().getSchedule().getRandomElement();
		// For better performance, the individuals are copied to an array.
		Individual[] ind = input[0].toArray();
		// Now the elements are rearranged randomly.
		for (int i = 0; i < bound; i++) {
			int pos = re.choose(i, inputLength - 1);
			Individual temp = ind[i];
			ind[i] = ind[pos];
			ind[pos] = temp;
		}
		// Now the split is performed.
		IndividualList[] output = new IndividualList[outPorts];
		for (int i = 0; i < outPorts; i++) {
			output[i] = new Population(getOperatorGraph().getSchedule());
			for (int j = 0; j < sizeOfSubSet; j++) {
				output[i].addIndividual(ind[i * sizeOfSubSet + j]);
			}
		}
		if (!exact) {
			// There are still inputLength-sizeOfSubSet*outPorts many elements
			// which must be distributed to the outports. 
			int remaining = inputLength - sizeOfSubSet * outPorts;
			int a[] = new int[outPorts];
			for (int i = 0; i < outPorts; i++) {
				a[i] = i;
			}
			for (int i = 0; i < remaining; i++) {
				int change = re.choose(i, outPorts - 1);
				int tmp = a[change];
				a[change] = a[i];
				a[i] = tmp;
				output[a[i]].addIndividual(ind[sizeOfSubSet * outPorts + i]);
			}

		}
		return output;
	}

	public String getDescription() {
		return "This operators splits the set of incoming individuals randomly into a given " + "number of subsets of equal size.";
	}

	public String getName() {
		return "Random Partition";
	}

	public Boolean getPropertyExact() {
		return new Boolean(exact);
	}

	public void setPropertyExact(Boolean exact) {
		this.exact = exact.booleanValue();
	}

	public String getShortDescriptionForExact() {
		return "Exact";
	}

	public String getLongDescriptionForExact() {
		return "If checked, the number of incoming individuals must be a multiple of the " + "number of outports. If unchecked and the number of incoming individuals is " + "not a multiple of the number of outports, some randomly chosen outports get " + "one individual more than the others.";
	}

}
