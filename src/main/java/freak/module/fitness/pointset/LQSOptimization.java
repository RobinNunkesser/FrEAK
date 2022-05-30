package freak.module.fitness.pointset;

import freak.core.control.Schedule;
import freak.module.fitness.pointset.util.InvalidNthElementException;
import freak.module.fitness.pointset.util.QuickSelect;
import freak.module.fitness.pointset.util.ResidualContainer;
import freak.module.searchspace.PointSet;

public class LQSOptimization extends AbstractStaticSingleObjectiveRobustRegressionFitness{
	public LQSOptimization(Schedule schedule){
		super(schedule);
		m_zName = "LQS";
	} 
	
	protected double evaluateResiduals(){
		
		double valueOfSolution = 0;
		
		QuickSelect<ResidualContainer> myQS = new QuickSelect<ResidualContainer>();
		ResidualContainer resultResidual = null;
		try {
			resultResidual = myQS.quickSelectNthSmallestElement(m_hResiduals, h-1); 
		} catch(InvalidNthElementException e){
			System.out.println("Catched an exception at LQSOptimization maybe h wrong ?");
			e.printStackTrace();
			return -offset;
		}
		valueOfSolution = resultResidual.squaredResidual;
		
		return -valueOfSolution;
	}
	
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Insert LQS Description here";
	}

}
