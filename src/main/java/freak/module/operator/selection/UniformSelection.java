/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.selection;

import edu.cornell.lassp.houle.RngPack.*;
import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.core.population.*;

/**
 * 
 * This class does the <code>UniformSelection</code>. It chooses 
 * <code>noOfIndividualsToSelect</code> individuals uniform randomly from
 * the given list of individuals "original" and puts then into the returned
 * <code>individualList</code>.
 * 
 * @author Christian, Heiko
 */
public class UniformSelection extends Selection implements Configurable, CompatibleWithDifferentSearchSpaces {

	/**
	 * Number of individuals to select from the given list of individuals.
	 */
	private int noOfIndividualsToSelect = 1;

	/**
	 * The constructor of the class <code>UniformSelection</code>.
	 * 
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public UniformSelection(OperatorGraph graph) {
		super(graph);
		super.addInPort();
		super.addOutPort();
	}

	/**
	 * This method does the selection described above.
	 * 
	 * @param original this is the individualList the individuals are chosen
	 * from.
	 * @return this is the list of the chosen individuals.
	 */
	public IndividualList[] process(IndividualList[] original) {
		// check if IndividualList is empty
		for (int i = 0; i < original.length; i++)
			if (original[i] == null)
				throw new NoSuchIndividualException();

		// create the resulting lists ...
		IndividualList result[] = new IndividualList[1];

		// create a new IndividualList
		result[0] = new Population(graph.getSchedule(), noOfIndividualsToSelect);

		// convert the original-list to array (==> faster)
		Individual[] origin = original[0].toArray();

		//if (origin.length == 0) throw new NoSuchIndividualException();
		if (origin.length == 0)
			return result;

		// now select noOfIndidualsToSelect individuals
		RandomElement randomGen = graph.getSchedule().getRandomElement();
		for (int i = 0; i < noOfIndividualsToSelect; i++) {
			// create a randomnumber between 0 and origin.length-1;
			int number = randomGen.choose(0, origin.length - 1);

			// and put it into the selection
			result[0].addIndividual(origin[number]);
		}

		return result;
	}

	/**
	 * 
	 * Returns the number of individuals which are chosen.
	 * 
	 * @return a wrapped integer containing the number of individuals to be
	 * chosen.
	 */
	public Integer getPropertyNoOfIndividualsToSelect() {
		return new Integer(noOfIndividualsToSelect);
	}

	/**
	 * Sets the number of individuals which will be chosen. At least one 
	 * individual has to be chosen, so setting to a value less or equal to
	 * zero will set the value to 1.
	 * 
	 * @param noOfIndividualsToSelect a wrapped integer containing the number
	 * of individuals which are chosen
	 */
	public void setPropertyNoOfIndividualsToSelect(Integer noOfIndividualsToSelect) {
		this.noOfIndividualsToSelect = noOfIndividualsToSelect.intValue();
		if (this.noOfIndividualsToSelect <= 0)
			this.noOfIndividualsToSelect = 1;
	}

	public String getShortDescriptionForNoOfIndividualsToSelect() {
		return "Selected individuals";
	}

	public String getLongDescriptionForNoOfIndividualsToSelect() {
		return "The number of individuals to be selected.";
	}

	public String getName() {
		return "Uniform Selection";
	}

	public String getDescription() {
		return "Selects individuals uniform random distributed among the individuals.";
	}

}
