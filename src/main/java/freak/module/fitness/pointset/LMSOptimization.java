package freak.module.fitness.pointset;

import freak.core.control.Schedule;
import freak.module.fitness.pointset.util.InvalidNthElementException;
import freak.module.fitness.pointset.util.QuickSelect;
import freak.module.fitness.pointset.util.ResidualContainer;
import freak.module.searchspace.PointSet;

public class LMSOptimization extends AbstractAdjustableStaticSingleObjectiveRR {
	private int k;
	
	public LMSOptimization(Schedule schedule){
		super(schedule,new LMSAdjust()); 
		m_zName = "LMS";
		if (schedule.getPhenotypeSearchSpace() instanceof PointSet) {
			PointSet ps = (PointSet) schedule.getPhenotypeSearchSpace();
			k=ps.getDimension()/2 + (ps.getPointDimension()+1)/2;
		}
	}
	
	@Override
	protected double evaluateResiduals() {
		// TODO Auto-generated method stub
		double valueOfSolution = 0;
		
		QuickSelect<ResidualContainer> myQS = new QuickSelect<ResidualContainer>();
		ResidualContainer resultResidual = null;
		try {
			resultResidual = myQS.quickSelectNthSmallestElement(m_hResiduals, k-1);
		} catch(InvalidNthElementException e){
			System.out.println("Catched an exception at LQSOptimization maybe k wrong ?");
			e.printStackTrace();
			return -offset;
		}
		valueOfSolution = resultResidual.squaredResidual;
		
		return -valueOfSolution;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return "Insert LMS Description here";
	}

}
