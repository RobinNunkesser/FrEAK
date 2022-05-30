package freak.module.fitness.pointset;

import freak.core.control.Schedule;
import freak.module.searchspace.PointSet;

public class LTAOptimization extends AbstractAdjustableStaticSingleObjectiveRR {

	public LTAOptimization(Schedule schedule){
		super(schedule,new LTSAdjust());
		m_zName = "LTA (Least Trimmed Abs)";
	} 
	
	protected double evaluateResiduals(){
		//if (!interceptAdjust){
			java.util.Arrays.sort(m_hResiduals);
		//}
		
		double valueOfSolution = 0;
		for(int i = 0; i < h; i++){
			valueOfSolution += Math.abs(m_hResiduals[i].signedResidual);
		}
		
		return -valueOfSolution;
	}
	
	public String getDescription() {
		// TODO Auto-generated method stub
		return "The fitness value of an individual is the least absolute error of the " +
		" subset of points represented by the given individual.\n We restrict the search to subsets of size d for d-dimensional points, because they uniquely describe possible solutions.";
	}

}
