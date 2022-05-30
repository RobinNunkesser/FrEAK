/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.fitness.pointset;

import freak.core.control.Schedule;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.module.fitness.pointset.util.ResidualContainer;
import freak.module.fitness.pointset.util.ResidualHyperPlane;
import freak.module.searchspace.PointSet;
import freak.module.searchspace.PointSetGenotype;

/**
 * This class can be used as abstract superclass of all multi objective fitness
 * functions which determine the fitness of an individual independently from the
 * other individuals in the population and independently from the age of the
 * individual. For these fitness functions the fitness value of an individual
 * can be cached since it doesn't depend on the current environment. 
 * 
 * @author Heiko
 */
public abstract class AbstractStaticMultiObjectiveRobustRegressionFitness extends AbstractMultiObjectiveRobustRegressionFitness {

	/**
	* Constructs a new <code>AbstractStaticMultiObjectiveFitnessFunction</code>
	* with a link back to the current schedule.
	* 
	* @param schedule a link back to the current schedule.
	*/
	public AbstractStaticMultiObjectiveRobustRegressionFitness(Schedule schedule) {
		super(schedule);
	}

	/**
	 * This method implements the use of a cache for fitness values.
	 */
	public final double[] evaluate(Individual individual, IndividualList list) {
		// First, check whether the fitness value is cached.		
		Double[] d = individual.getLatestKnownFitnessValue();
		if (!(d == null)) {
			int bound = d.length;
			double[] result = new double[bound];			
			for (int i = 0; i < bound; i++) {
				result[i] = d[i].doubleValue();
			}
			return result;
		}
		double[] fitness = evaluate(individual.getPhenotype());
		int bound = fitness.length;
		Double[] cache = new Double[bound];
		for (int i = 0; i < bound; i++) {
			cache[i] = new Double(fitness[i]); 
		}
		individual.setLatestKnownFitnessValue(cache);		
		return fitness;
	}
	
	protected double evaluateOLSFitOnH(Genotype genotype) {
		double value=0;
		
		PointSet.Point [] allPoints = ((PointSetGenotype)genotype).getPoints();
		PointSet.Point [] supportPoints = ((PointSetGenotype)genotype).getChoosenSubSet();

		int pointDim = ((PointSetGenotype)genotype).getPointDimension();

		if (supportPoints.length == 0){
			value=Double.NEGATIVE_INFINITY;
			return value;
		}
		
		if (supportPoints.length != supportPoints[0].getDimension()){
			value=Double.NEGATIVE_INFINITY;
			return value;
		}
				
		ResidualHyperPlane hyperPlane = new ResidualHyperPlane(supportPoints);
		ResidualContainer[] residuals = new ResidualContainer[allPoints.length];
		hyperPlane.updateResiduals(residuals, allPoints);	
		java.util.Arrays.sort(residuals);
		hyperPlane.computeParamLS(allPoints, residuals, h);
//		hyperPlane.updateResiduals(residuals, allPoints);

		for(int i = 0; i < allPoints.length; i++){
			value += -residuals[i].squaredResidual;
		}
		return value;
	}
	
	/**
	 * This method must be overriden by subclasses. It must determine the
	 * fitness value of the given individual independently from the its age.
	 * @param individual the individual whose fitness is to be evaluated.
	 * @return the fitness value of the given individual.
	 */
	abstract protected double[] evaluate(Genotype genotype);

	protected double evaluateResiduals(int quantile){
		java.util.Arrays.sort(m_hResiduals);
			
		double valueOfSolution = 0;
		for(int i = 0; i < quantile; i++){
			valueOfSolution += m_hResiduals[i].squaredResidual;
		}
		
		return -valueOfSolution;
	}
	

}
