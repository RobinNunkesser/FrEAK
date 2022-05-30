package freak.module.fitness.pointset;

import freak.core.control.Schedule;
import freak.core.population.Genotype;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.module.searchspace.PointSetGenotype;

public abstract class AbstractAdjustableStaticMultiObjectiveRR extends AbstractStaticMultiObjectiveRobustRegressionFitness  {

	private AbstractAdjust abstractAdjust; 
	
	public AbstractAdjustableStaticMultiObjectiveRR(Schedule schedule, AbstractAdjust adjust){
		super(schedule);
		abstractAdjust = adjust;
	}

	public String getLongDescriptionForAdjust() {
		return "Whether to perform intercept adjustment at each step.";
	}
	

	public String getShortDescriptionForAdjust() {
		return "Adjust Intercept";
	}
	
	public void setPropertyAdjust(Boolean adjust){
		interceptAdjust = adjust.booleanValue();
	}
	
	public Boolean getPropertyAdjust(){
		return new Boolean(interceptAdjust);
	} 
	
	protected void adjustIntercept(Genotype genotype) {
		if (abstractAdjust != null){
			abstractAdjust.adjust(this,genotype);
		}
	}

	
}
