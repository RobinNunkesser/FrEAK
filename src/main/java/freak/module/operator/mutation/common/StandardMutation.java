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
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.GeneralString;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * For bitstring:
 * Each position is flipped independently with probability mutationProb.
 *
 * For generalstring:
 * Each position is altered independently with probability mutationProb.
 * Altering a position means that another randomly chosen character is
 * assigned to this position.
 *
 * This Operator works on bitstring and generalstring.
 *
 * @author Michael, Stefan
 */
public class StandardMutation extends Mutation implements Configurable, CompatibleWithDifferentSearchSpaces {
	
	private double mutationProb;
	private int nextPos;
	//index where the random number is too imprecisely
	private int doubleImprecision;
	private double logbasis;
	
	// should the standard mutation probability 1/n be used
	private boolean standardMutationProb;
	
	
	public StandardMutation(OperatorGraph graph) {
		super(graph);
		
		if (graph.getSchedule().getGenotypeSearchSpace() instanceof HasDimension) {
			setPropertyStandardMutationProb(new Boolean(true));
		}
	}
	
	public void initialize() {
		super.initialize();

		if (standardMutationProb) {
			double p = 1 / (double)((HasDimension)getOperatorGraph().getSchedule().getGenotypeSearchSpace()).getDimension();
			setNewMutationProb(new Double(p));
		}
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		SearchSpace searchspace = schedule.getGenotypeSearchSpace();
		if (!((searchspace instanceof GeneralString) || (searchspace instanceof BitString)))
			throw new UnsupportedEnvironmentException("Wrong searchspace");
	}
	
	private void setNewMutationProb(Double prob) {
		mutationProb = prob.doubleValue();
		nextPos = -1;
		logbasis = Math.log(1 - mutationProb);
		if (logbasis == 0) {
			doubleImprecision = Integer.MAX_VALUE;
		} else {
			doubleImprecision = (int)Math.floor(Math.log(Math.pow(2, -47)) / logbasis);
		}
	}
	
	/**
	 * Sets the probability to flip independently each bit.
	 */
	public void setPropertyMutationProb(Double prob) {
		if (prob.doubleValue() >= 0 && prob.doubleValue() <= 1) {
			setNewMutationProb(prob);
		}
	}
	
	/**
	 * Returns the probability to flip independently each bit.
	 */
	public Double getPropertyMutationProb() {
		return new Double(mutationProb);
	}
	
	public String getLongDescriptionForMutationProb() {
		if (graph.getSchedule().getGenotypeSearchSpace() instanceof BitString) {
			return "The probability for a bit to flip.";
		} else {
			return "The probability to alter independently each char.";
		}
	}
	
	public String getShortDescriptionForMutationProb() {
		return "Mutation probability";
	}
	
	/**
	 * If this property is set to true then the mutation probability is
	 * automatically set to 1/n, where n is the dimension of the search space.
	 * @param b the value to which the property is set.
	 */
	public void setPropertyStandardMutationProb(Boolean b) {
		standardMutationProb = b.booleanValue();
		if (standardMutationProb) {
			double p = 1 / (double)((HasDimension)getOperatorGraph().getSchedule().getGenotypeSearchSpace()).getDimension();
			setNewMutationProb(new Double(p));
		}
	}
	
	/**
	 * @return the value of the property StandardMutationProb.
	 */
	public Boolean getPropertyStandardMutationProb() {
		return new Boolean(standardMutationProb);
	}
	
	public String getShortDescriptionForStandardMutationProb() {
		return "Set mutation prob. to 1/n";
	}
	
	public String getLongDescriptionForStandardMutationProb() {
		return "If checked, the mutation probability will be automatically set to 1/n, where n is the dimension of the search space.";
	}
	
	/** Save position information for the next query.
	 */
	private int savePos(int pos, int max) {
		if (pos > max) {
			nextPos = pos - max - 1;
			return -1;
		} else {
			nextPos = -1;
			return pos;
		}
	}
	
	/** Returns the next position to mutate.
	 *
	 * @param max is the maximum return value
	 */
	protected int getNextPosition(int max) {
		//recycle position information
		if (nextPos >= 0) {
			return savePos(nextPos, max);
		}
		RandomElement re = graph.getSchedule().getRandomElement();
		
		/* the prob that no position will mutate is (1^-pm)^n
		 * the prob that position k will mutate is (1-pm)^k*pm
		 * so for all possibilities:
		 * sum(0<=k<=n-1){(1-pm)^k*pm}+(1^-pm)^n=1
		 * the random number will fall into one of these possibilities intervals
		 * for position k the interval ends at
		 *    sum(0<=i<=k){(1-pm)^k*pm}
		 *  = sum(0<=i<=k){(1-pm)^k-(1-pm)^(k+1)}
		 *  = 1 - (1-pm)^(k+1)
		 * => k+1 = ceil(log_(1-pm)(1-random number))
		 * => k = floor(log_(1-pm)(random number))
		 * so the random number will assigned to a k
		 */
		int nextPosition;
		// fix bug, because value/0 = negative??? in java
		if (logbasis == 0) {
			nextPosition = -1;
		} else {
			nextPosition = (int)Math.floor(Math.log(re.raw()) / logbasis);
		}
		/* the random generator returns a double between 0 and 1
		 * the mantissa of the type double has ~51 bits
		 * the random number will be very imprecisely after the position
		 * doubleImprecision
		 */
		if (doubleImprecision < nextPosition) {
			if (max > doubleImprecision) {
				nextPosition = getNextPosition(max - doubleImprecision - 1) + doubleImprecision + 1;
			} else {
				nextPosition = -1;
			}
		}
		return savePos(nextPosition, max);
	}
	
	/**
	 * Performs a standard mutation.
	 */
	protected Individual doMutation(Individual ind) {
		int dimension = ((HasDimension)graph.getSchedule().getGenotypeSearchSpace()).getDimension();
		int startindex = 0;
		int nextPos = getNextPosition(dimension - 1);
		
		Genotype gt = ind.getGenotype();
		
		if (gt instanceof BitStringGenotype) {
			
			BitStringGenotype bs = (BitStringGenotype) ((BitStringGenotype)gt).clone();
			
			while (nextPos != -1) {
				bs.flip(startindex + nextPos);
				startindex += nextPos + 1;
				nextPos = getNextPosition(dimension - startindex - 1);
			}
			return new Individual(graph.getSchedule(), bs, new Individual[] {ind});
		} else {
			
			GeneralStringGenotype geno = (GeneralStringGenotype) ((GeneralStringGenotype)gt).clone();
			
			RandomElement re = graph.getSchedule().getRandomElement();
			while (nextPos != -1) {
				geno.flip(startindex + nextPos, re);
				startindex += nextPos + 1;
				nextPos = getNextPosition(dimension - startindex - 1);
			}
			return new Individual(graph.getSchedule(), geno, new Individual[] { ind });
		}
	}
	
	public String getDescription() {
		if (graph.getSchedule().getGenotypeSearchSpace() instanceof BitString) {
			return "Each bit is flipped independently with the specified mutation probability.";
		} else {
			return "Each position is altered independently with the specified mutation probability.";
		}
	}
	
	public String getName() {
		return "Standard Mutation";
	}
	
}
