/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.operator.initialization.bitstring;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.graph.GraphException;
import freak.core.graph.Initialization;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.population.Population;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.PointSet;

public class KBitInitialization extends Initialization implements Configurable {

	protected int kBits = 1;
	
	protected int size = 1;
	
	public KBitInitialization(OperatorGraph graph) {
		super(graph);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(graph.getSchedule().getGenotypeSearchSpace() instanceof BitString)){
				throw new UnsupportedEnvironmentException("Works only on the SearchSpace BitString.");			
		}
	}
	
	
	@Override
	public IndividualList[] process(IndividualList[] input)
			throws GraphException {
		IndividualList[] result = new IndividualList[1];
		result[0] = new Population(graph.getSchedule(), size);
		
		int dimension = ((BitString)graph.getSchedule().getGenotypeSearchSpace()).getDimension();
		
		RandomElement re = graph.getSchedule().getRandomElement();
		
		for (int i = 0; i < size; i++){
			boolean [] bs = new boolean[dimension];
			for (int k = 0; k < kBits; k++){
				int p = re.choose(0,dimension-1);
				while (bs[p]){
					p = re.choose(0,dimension-1);
				}
				bs[p] = true;
			}
			BitStringGenotype geno = new BitStringGenotype(bs);
			result[0].addIndividual(new Individual(graph.getSchedule(),geno,null));
		}
				
		return result;
	}
	
	
	public Integer getPropertyNoOfBitsSetted() {
		return new Integer(kBits);
	}
	
	public void setPropertyNoOfBitsSetted(Integer no) {
		if (no.intValue() > 0)
			kBits = no.intValue();
	}

	public String getShortDescriptionForNoOfBitsSetted() {
		return "No of set Bits";
	}

	public String getLongDescriptionForNoOfBitsSetted() {
		return "The number of randomised set Bits in the Bitstring";
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
		return "KBitInitialization";
	}
	
	public String getDescription() {
		return "Creates BitString Invdividauls with k Bits setted";
	}
}
