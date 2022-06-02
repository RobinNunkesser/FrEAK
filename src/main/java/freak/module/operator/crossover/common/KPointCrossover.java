/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.operator.crossover.common;

import freak.core.control.Schedule;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.searchspace.HasDimension;
import freak.core.searchspace.SearchSpace;
import freak.core.util.FreakMath;
import freak.module.operator.crossover.MultiPairwiseCrossover;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

import java.util.BitSet;

/**
 * Chooses K crossing points at random and performs a k point crossover.
 *
 * This Operator works on bitstring and generalstring.
 *
 * @author Michael
 */
public class KPointCrossover extends MultiPairwiseCrossover implements Configurable, CompatibleWithDifferentSearchSpaces {
	
	protected int k; //the number of crossing points
	
	/**
	 * Creates a new KPointCrossover object.
	 */
	public KPointCrossover(OperatorGraph graph) {
		super(graph);

		if (graph.getSchedule().getGenotypeSearchSpace() instanceof HasDimension) {
			setPropertyK(new Integer(1));
		}
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
	
	/**
	 * Performs a k-Point crossover of ind1 and ind2
	 */
	protected Individual doCrossover(Individual ind1, Individual ind2) {
		SearchSpace searchspace = graph.getSchedule().getGenotypeSearchSpace();
		
		int dimension = ((HasDimension)searchspace).getDimension();
		// get k positions
		int[] breaks = FreakMath.getKofN(graph.getSchedule(), k, dimension);
		int bi = 0;
		boolean first = true;
		
		if (searchspace instanceof BitString) {
			BitSet bs1 = ((BitStringGenotype)ind1.getGenotype()).getBitSet();
			BitSet bs2 = ((BitStringGenotype)ind2.getGenotype()).getBitSet();
			BitSet bsOut = (BitSet)bs1.clone();
			
			// for each bit
			for (int i = 0; i < dimension; i++) {
				if (breaks[bi] == i) {
					//toggle
					first = !first;
					//max bi = breaks.length-1
					if (bi < breaks.length - 1)
						bi++;
				}
				if (!first)
					bsOut.set(i, bs2.get(i));
			}
			return new Individual(graph.getSchedule(), new BitStringGenotype(bsOut, dimension), new Individual[] { ind1, ind2 });
			
		} else {
			
			int[] gs1 = ((GeneralStringGenotype)ind1.getGenotype()).getIntArray();
			int[] gs2 = ((GeneralStringGenotype)ind2.getGenotype()).getIntArray();
			int[] gsOut = (int[])gs1.clone();
			
			int numChars = ((GeneralString)graph.getSchedule().getGenotypeSearchSpace()).getNumChars();
			
			// for each pos
			for (int i = 0; i < dimension; i++) {
				if (breaks[bi] == i) {
					//toggle
					first = !first;
					//max bi = breaks.length-1
					if (bi < breaks.length - 1)
						bi++;
				}
				if (!first)
					gsOut[i] = gs2[i];
			}
			return new Individual(graph.getSchedule(), new GeneralStringGenotype(gsOut, numChars), new Individual[] { ind1, ind2 });
		}
	}
	
	/**
	 * Sets number k of points to crossover
	 */
	public void setPropertyK(Integer num) {
		if (num.intValue() >= 0 && (num.intValue() <= ((HasDimension)getGenotypeSearchSpace()).getDimension()))
			k = num.intValue();
	}
	
	/**
	 * Returns number k of points to crossover.
	 */
	public Integer getPropertyK() {
		return new Integer(k);
	}
	
	public String getLongDescriptionForK() {
		return "Number of randomly chosen crossover points.";
	}
	
	public String getShortDescriptionForK() {
		return "k";
	}
	
	public String getDescription() {
		return "Chooses k crossing points at random and performs a k point crossover.";
	}
	
	public String getName() {
		return "k-Point Crossover";
	}
}
