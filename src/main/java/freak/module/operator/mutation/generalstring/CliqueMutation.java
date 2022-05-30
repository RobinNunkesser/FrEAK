/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.mutation.generalstring;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.core.util.FreakMath;
import freak.module.fitness.generalstring.IsingModelCliques;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * An operator for the Ising model on cliques that alters k positions within 
 * each clique.
 * 
 * @author Dirk
 */
public class CliqueMutation extends Mutation implements Configurable {
	
	private int k;
	
	public CliqueMutation(OperatorGraph graph) {
		super(graph);
		
		k = 1;
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		if (!(schedule.getRealFitnessFunction() instanceof IsingModelCliques)) {
			throw new UnsupportedEnvironmentException("Works only with IsingModelCliques.");
		} 
	}
	
	protected Individual doMutation(Individual ind) {
		IsingModelCliques.Clique[] cliques = ((IsingModelCliques)getOperatorGraph().getSchedule().getRealFitnessFunction()).getCliques(); 

		Genotype gt = ind.getGenotype();
		GeneralStringGenotype geno = (GeneralStringGenotype) ((GeneralStringGenotype)gt).clone();
		RandomElement re = graph.getSchedule().getRandomElement();
		
		for (int j = 0; j < cliques.length; j++) {
			// get positions to mutate
			int[] flippos = FreakMath.getKofN(graph.getSchedule(), k, cliques[j].getSize());
		
			// mutate the k positions
			for (int i = 0; i < k; i++) {
				geno.flip(flippos[i] + cliques[j].getStart(), re);
			}
		}

		return new Individual(graph.getSchedule(), geno, new Individual[] { ind });
	}
	
	/**
	 * Sets the number k of positions to flip.
	 */
	public void setPropertyK(Integer num) {
		if ((num.intValue() >= 0) && (num.intValue() <= ((HasDimension)getGenotypeSearchSpace()).getDimension()))
			k = num.intValue();
	}
	
	/**
	 * returns the number k of positions to flip.
	 */
	public Integer getPropertyK() {
		return new Integer(k);
	}
	
	public String getLongDescriptionForkK() {
		return "Number of positions to alter in each clique.";
	}
	
	public String getShortDescriptionForK() {
		return "k";
	}
	
	public String getDescription() {
		return "Exactly k randomly chosen positions are altered within each clique. " +
			"Altering a position means that a character randomly chosen " +
			"among the remaining characters is assigned to this position.";
	}
	
	public String getName() {
		return "Clique Mutation";
	}
	
}
