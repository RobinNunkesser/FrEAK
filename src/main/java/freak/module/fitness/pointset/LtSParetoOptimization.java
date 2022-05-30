/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.fitness.pointset;


import freak.core.control.Schedule;
import freak.core.modulesupport.Configurable;
import freak.core.population.Genotype;
import freak.module.searchspace.PointSetGenotype;


public class LtSParetoOptimization extends AbstractAdjustableStaticMultiObjectiveRR implements Configurable {
	
	public LtSParetoOptimization(Schedule schedule){
		super(schedule,new LTSAdjust());
		m_zName = "LTS (Least Trimmed Squares) Pareto";
	} 
		
	public String getDescription() {
		// TODO Auto-generated method stub
		return "The fitness value of an individual is the least squares error of the " +
				" subset of points represented by the given individual.\n We restrict the search to subsets of size d for d-dimensional points, because they uniquely describe possible solutions.";
	}

	public int getDimensionOfObjectiveSpace() {
		// TODO Auto-generated method stub
		return 2;
	} 

	/**
	 * This method must be overriden by subclasses. It must determine the
	 * fitness value of the given individual independently from the its age.
	 * @param individual the individual whose fitness is to be evaluated.
	 * @return the fitness value of the given individual.
	 */
	protected double[] evaluate(Genotype genotype) {
		double[] fitness=new double[2];
		try {
			calculateResiduals(genotype);	
		} catch(WrongNumberOfPointsException e){
			fitness[0]=e.getErrorFitnessValue() + offset;
			fitness[1]=Double.NEGATIVE_INFINITY;
			return fitness;
		}

		adjustHyperplane(genotype);
		
		if (interceptAdjust){
			adjustIntercept(genotype);
		}

		//java.util.Arrays.sort(m_hResiduals);
		//hyperPlane.computeParamLS(((PointSetGenotype)genotype).getPoints(), m_hResiduals, h);
		
		rememberChoosenParameters();
		fitness[0]=evaluateResiduals();
		fitness[1]=evaluateOLSFitOnH(genotype);
		
		return fitness;
	}

	protected double evaluateResiduals(){
		return evaluateResiduals(h);
	}
	
}




