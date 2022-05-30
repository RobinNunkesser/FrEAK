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

/**
 * This class extends the general, abstract class FitnessTransformer. It takes the "original" fitnessfunction (pointed to by "delegate") and modifies the fitness-value f(x) of a given individual x (and the population p it comes from) as follows: f'(x,p)=a*f(x,p)+b, which means it does an affine transform of the fitness-value specified by the parameters a and b.
 * @author  Christian, Michael
 */
public class AffineTransformer extends AbstractSingleObjectiveFitnessFunction implements FitnessTransformer, Configurable {

	private FitnessFunction delegate;

	private double a = 1;
	private double b = 0;

	/**
	 * The constructor of <coed>AffineTransformer</code>.
	 * 
	 * @param schedule a back-link to the currently used schedule.
	 */
	public AffineTransformer(Schedule schedule) {
		super (schedule);		
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
	}

	public String getDescription() {
		return "This transformer modifies the fitness value according to the formula f'=a*f+b";
	}

	public String getName() {
		return "AffineTransformer";
	}
	
	public void setFitnessFunction(FitnessFunction fitness) {
		delegate = fitness;
	}
	
	public FitnessFunction getFitnessFunction() {
		return delegate;
	}

	/**
	 * Returns the value of attribute <code>a</code>.
	 * 
	 * @return the wrapped value of attribute <code>a</code>.
	 */
	public Double getPropertyA() {
		return new Double(a);
	}

	/**
	 * Sets the value of attribute <code>a</code>.
	 * 
	 * @param a the wrapped value attribute <code>a</code> should be set to.
	 */
	public void setPropertyA(Double a) {
		this.a = a.doubleValue();
	}

	/**
	 * Returns the value of attribute <code>b</code>.
	 * 
	 * @return the wrapped value of attribute <code>b</code>.
	 */
	public Double getPropertyB() {
		return new Double(b);
	}

	/**
	 * Sets the value of attribute <code>b</code>.
	 * 
	 * @param a the wrapped value attribute <code>b</code> should be set to.
	 */
	public void setPropertyB(Double b) {
		this.b = b.doubleValue();
	}

	public double evaluate(Individual individual, IndividualList list) {
		// call the fitnessfunction
		double temp = ((SingleObjectiveFitnessFunction)delegate).evaluate(individual,list);
		// now do the transformation
		return a * temp + b;
	}


	public double getOptimalFitnessValue() throws UnsupportedOperationException {
		if (a < 0) 
			throw new UnsupportedOperationException();
		else 
			return a*((SingleObjectiveFitnessFunction)delegate).getOptimalFitnessValue()+b;
	}


	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException {
		if (a <= 0)
			throw new UnsupportedOperationException();
		else
			return delegate.getPhenotypeOptimum();
	}

	public double getLowerBound() throws UnsupportedOperationException {
		return (a < 0) ? (a * ((SingleObjectiveFitnessFunction)delegate).getUpperBound() + b) : (a* ((SingleObjectiveFitnessFunction)delegate).getLowerBound() + b);
	}

	public double getUpperBound() throws UnsupportedOperationException {
		return (a > 0) ? (a * ((SingleObjectiveFitnessFunction)delegate).getUpperBound() + b) : (a* ((SingleObjectiveFitnessFunction)delegate).getLowerBound() + b);
	}

}
