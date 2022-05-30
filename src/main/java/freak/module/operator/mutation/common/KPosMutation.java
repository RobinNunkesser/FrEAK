/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.common;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.core.searchspace.SearchSpace;
import freak.core.util.FreakMath;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * Exactly k randomly chosen positions are altered. Altering a
 * position means that another randomly chosen character is assigned to this
 * position.
 *
 * This Operator works on bitstring and generalstring.
 *
 * @author Michael
 */
public class KPosMutation extends Mutation implements Configurable, CompatibleWithDifferentSearchSpaces {
	
	private int k;
	
	public KPosMutation(OperatorGraph graph) {
		super(graph);

		k = 1;
	}
	
	public void initialize() {
		super.initialize();
		
		int dim = ((HasDimension)getGenotypeSearchSpace()).getDimension();
		if (k > dim) k = dim; 
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!((searchspace instanceof GeneralString) || (searchspace instanceof BitString)))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}
	
	protected Individual doMutation(Individual ind) {
		int dimension = ((HasDimension)graph.getSchedule().getGenotypeSearchSpace()).getDimension();
		// get positions to mutate
		int[] flippos = FreakMath.getKofN(graph.getSchedule(), k, dimension);
		
		Genotype gt = ind.getGenotype();
		
		if (gt instanceof BitStringGenotype) {
			BitStringGenotype bs = (BitStringGenotype) ((BitStringGenotype)gt).clone();
			for (int i = 0; i < k; i++) {
				bs.flip(flippos[i]);
			}
			return new Individual(graph.getSchedule(), bs, new Individual[] { ind });
			
		} else {
			
			GeneralStringGenotype geno = (GeneralStringGenotype) ((GeneralStringGenotype)gt).clone();
			RandomElement re = graph.getSchedule().getRandomElement();
			// mutate the k positions
			for (int i = 0; i < k; i++) {
				geno.flip(flippos[i], re);
			}
			return new Individual(graph.getSchedule(), geno, new Individual[] { ind });
		}
	}
	
	/**
	 * Sets the number k of positions to flip.
	 */
	public void setPropertyK(Integer num) {
		if ((num.intValue() >= 0) && (num.intValue() <= ((HasDimension)getGenotypeSearchSpace()).getDimension()))
			k = num.intValue();
	}
	
	/**
	 * Returns the number k of positions to flip.
	 */
	public Integer getPropertyK() {
		return new Integer(k);
	}
	
	public String getLongDescriptionForkK() {
		if (graph.getSchedule().getGenotypeSearchSpace() instanceof BitString) {
			return "Number of positions to flip.";
		} else {
			return "Number of positions to alter.";
		}
	}
	
	public String getShortDescriptionForK() {
		return "k";
	}
	
	public String getDescription() {
		if (graph.getSchedule().getGenotypeSearchSpace() instanceof BitString) {
			return "Exactly k randomly chosen bits are flipped.";
		} else {
			return "Exactly k randomly chosen positions are altered. " +
			"Altering a position means that a character randomly chosen " +
			"among the remaining characters is assigned to this position.";
		}
		
	}
	
	public String getName() {
		if (graph.getSchedule().getGenotypeSearchSpace() instanceof BitString) {
			return "k-Bit Mutation";
		} else {
			return "k-Char Mutation";
		}
	}
	
}
