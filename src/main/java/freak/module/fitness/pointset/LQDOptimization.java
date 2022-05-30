package freak.module.fitness.pointset;

import freak.core.control.Schedule;
import freak.module.fitness.pointset.util.QuickSelectDifferences;

public class LQDOptimization extends AbstractStaticSingleObjectiveRobustRegressionFitness {
	
	QuickSelectDifferences diffSelect = new QuickSelectDifferences();
	
	public LQDOptimization(Schedule schedule){
		super(schedule);
		m_zName = "LQD (Least Quartile Difference)";
	} 
	
	@Override
	protected double evaluateResiduals() {
		// TODO Auto-generated method stub
		java.util.Arrays.sort(m_hResiduals);
		int n = m_hResiduals.length;
		
		double [] v = new double [n];
		for (int i = 0; i < n; i++) {
			v[i] = m_hResiduals[i].squaredResidual;
		}
		
		double result = diffSelect.quickSelectNthSmallestDifference(v, (h*(h-1)/2)-1);
		
		return -result;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return "Insert LQD Description here";
	}

}
