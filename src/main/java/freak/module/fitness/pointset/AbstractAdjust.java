package freak.module.fitness.pointset;

import java.io.Serializable;

import freak.core.population.Genotype;

public abstract class AbstractAdjust implements Serializable {
	public abstract void adjust(AbstractRobustRegressionFitness toBeAdjusted, Genotype genotype);
}
