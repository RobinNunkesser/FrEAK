/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.initialization;
import freak.core.graph.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.core.searchspace.*;
/**
 * 
 * This class implements the <code>RandomInitialization</code>. <br>
 * 
 * <code>Size</code> individuals are created uniform randomly.
 * 
 * @author Christian
 */
public class RandomInitialization extends Initialization implements Configurable, CompatibleWithDifferentSearchSpaces {
	/*
	 * Number of individuals which are created randomly. 
	 */
	private int size = 1;
	/**
	 * The constructor of the class <code>RandomSplit</code>.
	 * 
	 * @param graph a link to the current <code>OperatorGraph</code>.
	 */
	public RandomInitialization(OperatorGraph graph) {
		super(graph);
	}
	/**
	 * This method creates <code>size</code> individuals
	 * uniform randomly using the method 
	 * <code>SearchSpace.getRandomGenotype</code>.
	 *
	 * @param origin it may be null. The method is not going to use it .
	 * @return only the first entry is initialized (with the resulting 
	 * individuals).
	 */
	public IndividualList[] process(IndividualList[] origin) {
		// create the resulting array of individual lists
		IndividualList[] result = new IndividualList[1];
		result[0] = new Population(graph.getSchedule(), size);
		// now create the individuals	   
		SearchSpace sp = graph.getSchedule().getGenotypeSearchSpace();
		for (int i = 0; i < size; i++) {
			result[0].addIndividual(new Individual(graph.getSchedule(), sp.getRandomGenotype(), null));
		}
		return result;
	}
	
	/**
	 * Returns the number of individuals to be created.
	 * 
	 * @return a wrapped integer containing the number of individuals to be
	 * created.
	 */
	public Integer getPropertySize() {
		return new Integer(size);
	}
	
	/**
	 * Sets the number of individuals to be created.
	 * 
	 * @param no a wrapped integer containing the number of individuals to be 
	 * created.
	 */
	public void setPropertySize(Integer no) {
		if (no.intValue() > 0)
			size = no.intValue();
	}
	public String getLongDescriptionForSize() {
		return "The number of individuals to be created.";
	}
	
	public String getName() {
		return "RandomInitialization";
	}
	
	public String getDescription() {
		return "Creates individuals uniform randomly distributed in the chosen search space.";
	}
	
}
