/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.initialization;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.graph.Initialization;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;

import java.util.BitSet;

/**
 * @author Heiko
 */
public class BiasedRandomInitialization extends Initialization {

	private int size = 1;
	private double probOne = 0.5;

	public BiasedRandomInitialization(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getGenotypeSearchSpace() instanceof BitString)) {
			throw new UnsupportedEnvironmentException("Works only on the search space bit string.");			
		}
	}

	public IndividualList[] process(IndividualList[] origin) {
		// create the resulting array of individual lists
		IndividualList[] result = new IndividualList[1];
		result[0] = new Population(graph.getSchedule(), size);

		// now create the individuals	   		
		for (int i=0; i<size; i++) {
			int dimension = ((BitString)graph.getSchedule().getGenotypeSearchSpace()).getDimension(); 
			BitSet bs = new BitSet(dimension);
			RandomElement re = graph.getSchedule().getRandomElement();
			if (re == null) throw new NullPointerException("no random element");
			for (int j = 0; j < dimension; j++) {
				double choice = re.uniform(0,1);				
				if (choice <= probOne) bs.set(j);
			}
			BitStringGenotype bsg = new BitStringGenotype(bs, dimension);			
			result[0].addIndividual(new Individual(graph.getSchedule(),bsg,new Individual[0]));
	   }
	   return result;
	}


	public String getName() {
		return "Biased Random Initialization";
	}

	public String getDescription() {
		return "Biased Random Initialization";
	}
	
	public Integer getPropertySize() {
		return new Integer(size);
	}

	public void setPropertySize(Integer no) {
		if (no.intValue() > 0) size = no.intValue();
	}
	
	public String getLongDescriptionForSize() {
		return "The number of individuals to be created.";
	}

	public Double getPropertyProbOne() {
		return new Double(probOne);
	}
	
	public void setPropertyProbOne(Double prop) {
		double p = prop.doubleValue();
		if ((p >= 0) && (p <= 1)) {
			probOne = p;
		}
	}
	
	public String getShortDescriptionForProbOne() {
		return "Bias";
	}
	
	public String getLongDescriptionForProbOne() {
		return "The property with which a bit is set to 1";
	}

}
