/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.fitness.transformer;

import freak.core.control.Schedule;
import freak.core.fitness.AbstractSingleObjectiveFitnessFunction;
import freak.core.fitness.FitnessFunction;
import freak.core.fitness.FitnessTransformer;
import freak.core.fitness.SingleObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.searchspace.HasMetric;
import java.util.Iterator;

/**
 * Fitness sharing derates the individual fitness by dividing it by the individual niche count. The niche count is the sum of shared function values, calulated by distances between the individual and all population elements. Many similar individuals were punished, unique individuals get an advantage.
 * @author  Michael
 */
public class FitnessSharing extends AbstractSingleObjectiveFitnessFunction implements FitnessTransformer, Configurable {
	
	private FitnessFunction delegate;
	
	private double alpha = 1;
	
	private double sigma_share = 5;
	
	private boolean active = true;
	
	/** Creates a new instance of FitnessSharing */
	public FitnessSharing(Schedule schedule) {
		super(schedule);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (this.getFitnessFunction() == null) {
			// check if this transformer can be wrapped around the current
			// fitness function
			if (!(schedule.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
				throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
			}
		} else {
			// check if this transformer is wrapped around a valid
			// fitness function
			if (!(this.getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
				throw new UnsupportedEnvironmentException("This module works on single objective fitness functions only.");
			}
		}
		
		if (!(schedule.getPhenotypeSearchSpace() instanceof HasMetric))
			throw new UnsupportedEnvironmentException("Search space has no metric.");
		
		try {
			// test if the lower bound is defined
			((SingleObjectiveFitnessFunction)schedule.getRealFitnessFunction()).getLowerBound();
		} catch (UnsupportedOperationException e) {
			throw new UnsupportedEnvironmentException("no lower bound");
		}
	}
	
	public void setFitnessFunction(FitnessFunction fitness) {
		delegate = fitness;
	}
	
	public FitnessFunction getFitnessFunction() {
		return delegate;
	}
	
	public Double getPropertyAlpha() {
		return new Double(alpha);
	}
	
	public void setPropertyAlpha(Double a) {
		if (a.doubleValue() > 0)
			alpha = a.doubleValue();
	}
	
	public String getLongDescriptionForAlpha() {
		return "Regulates the shape of the sharing function.";
	}
	
	public Double getPropertySigma_share() {
		return new Double(sigma_share);
	}
	
	public void setPropertySigma_share(Double ss) {
		if (ss.doubleValue() > 0)
			sigma_share = ss.doubleValue();
	}
	
	public String getLongDescriptionForSigma_share() {
		return "Two individuals do not affect each other, if the distance between them is greater or equal to sigma_share.";
	}
	
	public Boolean getPropertyActive() {
		return new Boolean(active);
	}
	
	public void setPropertyActive(Boolean a) {
		active = a.booleanValue();
	}
	
	public String getLongDescriptionForActive() {
		return "Enables or disables fitness sharing. May be used by parameter controllers to activate or deactivate fitness sharing.";
	}
	/**
	 * The sharing function
	 */
	private double sh(double distance) {
		if (distance < sigma_share) {
			return 1 - Math.pow((distance / sigma_share), alpha);
		} else {
			return 0;
		}
	}
	
	public double evaluate(Individual ind, IndividualList p) {
		if (!active || p == null)
			return ((SingleObjectiveFitnessFunction)delegate).evaluate(ind, p);
		double add = 0;
		// need positive fitness value
		try {
			double low = ((SingleObjectiveFitnessFunction)delegate).getLowerBound();
			if (low < 0)
				add = 0-low;
		} catch (UnsupportedOperationException ex) {
		}
		
		double niche_count = 0;
		HasMetric metric = (HasMetric)getSchedule().getPhenotypeSearchSpace();
		Genotype gt = ind.getPhenotype();
		Iterator it = p.iterator();
		while (it.hasNext()) {
			Individual i = (Individual)it.next();
			niche_count += sh(metric.getDistance(gt, i.getPhenotype()));
		}
		return ((((SingleObjectiveFitnessFunction)delegate).evaluate(ind, p)+add)/niche_count)-add;
	}
	
	public String getDescription() {
		return "Fitness sharing derates the individual fitness by dividing it by the individual niche count. The niche count is the sum of shared function values, calulated by distances between the individual and all population elements.";
	}
	
	public String getName() {
		return "Fitness Sharing";
	}
	
	public double getLowerBound() throws UnsupportedOperationException {
		return 0;
	}
	
	public double getUpperBound() throws UnsupportedOperationException {
		return ((SingleObjectiveFitnessFunction)delegate).getUpperBound();
	}
	
	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
}
