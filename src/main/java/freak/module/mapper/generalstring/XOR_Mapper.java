/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.mapper.generalstring;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.mapper.AbstractMapper;
import freak.core.population.Genotype;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * Value + RandomCreatedValue % numChar
 *
 * @author Michael
 */
public class XOR_Mapper extends AbstractMapper implements BatchEventListener {
	
	private int[] addition;
	
	public XOR_Mapper(Schedule schedule) {
		super(schedule);

		if (schedule.getPhenotypeSearchSpace() instanceof GeneralString) {
			createAddition();
		}
	}
	
	public void initialize() {
		super.initialize();
		
		int dim = ((GeneralString)schedule.getPhenotypeSearchSpace()).getDimension();
		if (addition != null && addition.length != dim) createAddition();
	}
	
	public void createAddition() {
		GeneralString searchspace = ((GeneralString)schedule.getPhenotypeSearchSpace());
		addition = ((GeneralStringGenotype)searchspace.getRandomGenotype()).getIntArray();
	}
	
	public Genotype genotypeToPhenotype(Genotype genotype) {
		GeneralString searchspace = ((GeneralString)schedule.getPhenotypeSearchSpace());
		int dimension = searchspace.getDimension();
		int numChars = searchspace.getNumChars();
		int[] pheno = new int[dimension];
		int[] geno = ((GeneralStringGenotype)genotype).getIntArray();
		for (int i = 0; i < dimension; i++) {
			pheno[i] = (geno[i]+addition[i])%numChars;
		}
		return new GeneralStringGenotype(pheno, numChars);
	}
	
	public Genotype phenotypeToGenotype(Genotype genotype) {
		// before starting, some modules check whether the optimum exists and this method
		// no Random element at this time, so i can't call createPermutation()
		if (addition == null)
			return genotype;
		
		GeneralString searchspace = ((GeneralString)schedule.getPhenotypeSearchSpace());
		int dimension = searchspace.getDimension();
		int numChars = searchspace.getNumChars();
		int[] geno = new int[dimension];
		int[] pheno = ((GeneralStringGenotype)genotype).getIntArray();
		for (int i = 0; i < dimension; i++) {
			geno[i] = (pheno[i]-addition[i]+numChars)%numChars;
		}
		return new GeneralStringGenotype(geno, numChars);
	}
	
	public SearchSpace getGenotypeSearchSpace() {
		return schedule.getPhenotypeSearchSpace();
	}
	
	public String getName() {
		return "General String XOR";
	}
	
	public String getDescription() {
		return "(Value + RandomlyCreatedValue(0..., numChar-1)) % numChar";
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}
	
	public void batchStarted(BatchEvent evt) {
		createAddition();
	}

}