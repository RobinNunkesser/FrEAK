package freak.module.fitness.pointset;

import freak.rinterface.model.RReturns;
import freak.core.control.Schedule;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.module.searchspace.PointSetGenotype;

public abstract class AbstractStaticSingleObjectiveRobustRegressionFitness extends AbstractSingleObjectiveRobustRegressionFitness  {

	public AbstractStaticSingleObjectiveRobustRegressionFitness(Schedule schedule) {
		super(schedule);
		// TODO Auto-generated constructor stub
	}

	/**
	 * This method implements the use of a cache for fitness values.
	 */
	public final double evaluate(Individual individual, IndividualList list) {
		// First, check whether the fitness value is cached.
		Double[] d = individual.getLatestKnownFitnessValue();
		if (!(d == null)) {
			return d[0].doubleValue();
		}
		double fitness = evaluate(individual.getPhenotype());
		Double[] result = {new Double(fitness)};
		individual.setLatestKnownFitnessValue(result);
		return fitness;
	}
	
	protected double evaluate(Genotype genotype) {
		try {
			calculateResiduals(genotype);	
		} catch(WrongNumberOfPointsException e){
			
			return e.getErrorFitnessValue() + offset;
		}
	
		adjustHyperplane(genotype);
		
		if (interceptAdjust){
			adjustIntercept(genotype);
		}
		double evaluation=evaluateResiduals();
		rememberChoosenParameters();
		if (-evaluation<RReturns.getResidual()) {
			RReturns.setResidual(-evaluation);
			RReturns.setChosenIndices(chosenIndices);
			RReturns.setFittedHyperplane(fittedHyperplane);
		}
		
		return evaluation;
		
	}

	
	protected void adjustIntercept(Genotype genotype) {
		// TODO Auto-generated method stub		
	}

	/**
	 * Returns a value of 1, if individual ind1 is better than individual ind2.
	 * It returns 0 if none of the two individuals is better.
	 * It returns -1 if ind2 better than ind1.
	 */
	public int compareIndividuals(Individual ind1, Individual ind2) throws UnsupportedOperationException {
		if (ind1 == ind2) return 0;
		double f1 = evaluate(ind1, null);
		double f2 = evaluate(ind2, null);
		if (f1 > f2) return 1;
		if (f1 < f2) return -1;
		return 0;
	}

}
