/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.mapper.bitstring;

import java.util.BitSet;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.mapper.AbstractMapper;
import freak.core.population.Genotype;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;

/**
 * Permutes the order of bits.
 *
 * @author Michael
 */
public class Permuter_Mapper extends AbstractMapper implements BatchEventListener {
	
	private int[] forwards;
	private int[] backwards;
	
	public Permuter_Mapper(Schedule schedule) {
		super(schedule);
		
		if (schedule.getPhenotypeSearchSpace() instanceof BitString) {
			createPermutation();
		}
	}
	
	public void initialize() {
		super.initialize();
		
		int dimension = ((BitString)schedule.getPhenotypeSearchSpace()).getDimension();
		if (forwards != null && forwards.length != dimension) createPermutation();
	}
	
	public void createPermutation() {
		int dimension = ((BitString)schedule.getPhenotypeSearchSpace()).getDimension();
		forwards = new int[dimension];
		backwards = new int[dimension];
		RandomElement re = schedule.getRandomElement();
		for (int i = 0; i < dimension; i++) {
			forwards[i] = i;
		}
		for (int i = 0; i < dimension; i++) {
			int j = re.choose(i, dimension - 1);
			int tmp = forwards[j];
			forwards[j] = forwards[i];
			forwards[i] = tmp;
			backwards[tmp] = i;
		}
	}
	
	public Genotype genotypeToPhenotype(Genotype genotype) {
		int dimension = ((BitString)schedule.getPhenotypeSearchSpace()).getDimension();
		BitSet phenoBS = new BitSet(dimension);
		BitSet genoBS = ((BitStringGenotype)genotype).getBitSet();
		for (int i = 0; i < dimension; i++) {
			phenoBS.set(i, genoBS.get(forwards[i]));
		}
		return new BitStringGenotype(phenoBS, dimension);
	}
	
	public Genotype phenotypeToGenotype(Genotype genotype) {
		// before starting, some modules check whether the optimum exists and this method
		// no Random element at this time, so i can't call createPermutation()
		if (backwards == null)
			return genotype;
		
		int dimension = ((BitString)schedule.getPhenotypeSearchSpace()).getDimension();
		BitSet genoBS = new BitSet(dimension);
		BitSet phenoBS = ((BitStringGenotype)genotype).getBitSet();
		for (int i = 0; i < dimension; i++) {
			genoBS.set(i, phenoBS.get(backwards[i]));
		}
		return new BitStringGenotype(genoBS, dimension);
	}
	
	public SearchSpace getGenotypeSearchSpace() {
		return schedule.getPhenotypeSearchSpace();
	}
	
	public String getName() {
		return "Bit String Permuter";
	}
	
	public String getDescription() {
		return "Permutes the bits in a randomly created order.";
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}
	
	public void batchStarted(BatchEvent evt) {
		createPermutation();
	}

}