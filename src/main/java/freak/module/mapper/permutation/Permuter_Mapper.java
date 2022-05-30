/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.mapper.permutation;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.mapper.AbstractMapper;
import freak.core.population.Genotype;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.Permutation;
import freak.module.searchspace.PermutationGenotype;

/**
 * Permutes the order of chars.
 *
 * @author Michael
 */
public class Permuter_Mapper extends AbstractMapper implements BatchEventListener {
	
	private int[] forwards;
	private int[] backwards;
	
	public Permuter_Mapper(Schedule schedule) {
		super(schedule);

		if (schedule.getPhenotypeSearchSpace() instanceof Permutation) {
			createPermutation();
		}
	}
	
	public void initialize() {
		super.initialize();
		
		int dimension = ((Permutation)schedule.getPhenotypeSearchSpace()).getDimension();
		if (forwards != null && forwards.length != dimension) createPermutation();
	}
	
	public void createPermutation() {
		int dimension = ((Permutation)schedule.getPhenotypeSearchSpace()).getDimension();
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
		Permutation searchspace = ((Permutation)schedule.getPhenotypeSearchSpace());
		int dimension = searchspace.getDimension();
		int[] pheno = new int[dimension];
		int[] geno = ((PermutationGenotype)genotype).getIntArray();
		for (int i = 0; i < dimension; i++) {
			pheno[i] = geno[forwards[i]];
		}
		return new PermutationGenotype(pheno);
	}
	
	public Genotype phenotypeToGenotype(Genotype genotype) {
		// before starting, some modules check whether the optimum exists and this method
		// no Random element at this time, so i can't call createPermutation()
		if (backwards == null)
			return genotype;
		
		Permutation searchspace = ((Permutation)schedule.getPhenotypeSearchSpace());
		int dimension = searchspace.getDimension();
		int[] geno = new int[dimension];
		int[] pheno = ((PermutationGenotype)genotype).getIntArray();
		for (int i = 0; i < dimension; i++) {
			geno[i] = pheno[forwards[i]];
		}
		return new PermutationGenotype(geno);
	}
	
	public SearchSpace getGenotypeSearchSpace() {
		return schedule.getPhenotypeSearchSpace();
	}
	
	public String getName() {
		return "Permutation Permuter";
	}
	
	public String getDescription() {
		return "Permutes the permutation strings in a randomly created order.";
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}
	
	public void batchStarted(BatchEvent evt) {
		createPermutation();
	}

}