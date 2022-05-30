package freak.module.fitness.pointset;

import java.util.Arrays;

import freak.core.population.Genotype;
import freak.module.fitness.pointset.util.QuickSelect;
import freak.module.fitness.pointset.util.ResidualContainer;
import freak.module.fitness.pointset.util.ResidualHyperPlane;
import freak.module.searchspace.PointSet;
import freak.module.searchspace.PointSetGenotype;

public class LSAdjust extends AbstractAdjust{
	QuickSelect<ResidualContainer> myQs = new QuickSelect<ResidualContainer>();
	
	@Override
	public void adjust(AbstractRobustRegressionFitness toBeAdjusted,
			Genotype genotype) {
	// TODO Auto-generated method stub
		ResidualContainer [] res = toBeAdjusted.getResiduals();
		
		Arrays.sort(res);
		
//		try{
//		myQs.quickSelectNthSmallestElement(res, toBeAdjusted.getPropertyH()-1);
//		}
//		catch (Exception e)
//		{
//			throw new Error(e);
//		}
		ResidualHyperPlane hplane = toBeAdjusted.getResidualHyperPlane();
		
	
		
		PointSet.Point [] allPoints = ((PointSetGenotype)genotype).getPoints();
		
		hplane.computeParamLS(allPoints, res, toBeAdjusted.getPropertyH());
		
		//hplane.updateResiduals(res, allPoints );
	}
	
	
}
