package freak.module.fitness.pointset;

import java.util.Arrays;

import freak.core.population.Genotype;
import freak.module.fitness.pointset.util.ResidualContainer;
import freak.module.fitness.pointset.util.ResidualHyperPlane;
import freak.module.searchspace.PointSetGenotype;

// TODO This gives very bad intercepts. Why?
public class LMSAdjust extends AbstractAdjust {

	@Override
	public void adjust(AbstractRobustRegressionFitness toBeAdjusted,
			Genotype genotype) {
		// TODO Auto-generated method stub
		ResidualContainer resCont [] = toBeAdjusted.getResiduals();
		
		Arrays.sort(resCont);
		double min = 0;
		int j = 0;
		int k = resCont.length / 2 + 1;
		for (int i = 0; i < resCont.length-k; i++) {
			double dist = resCont[i+k].squaredResidual - resCont[i].squaredResidual;
			if ((i == 0)||(min > dist)){
				min = dist;
				j = i;
			}
		}
		
		double lms = (resCont[j+k].squaredResidual + resCont[j].squaredResidual) / 2.0;
		
		ResidualHyperPlane hyperPlane = toBeAdjusted.getResidualHyperPlane();
		
		try {
			hyperPlane.getParameter().set(0, lms);
			
			hyperPlane.updateResiduals(resCont, ((PointSetGenotype)genotype).getPoints());
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		
	}

	public static void main (String [] args){
		double [] resCont = new double [] {90,93,86,92,95,83,75,40,88,80};
		
		Arrays.sort(resCont);
		double min = 0;
		int j = 0;
		int k = resCont.length / 2 + 1;
		for (int i = 0; i < resCont.length-k+1; i++) {
			double dist = resCont[i+k-1] - resCont[i];
			if ((i == 0)||(min > dist)){
				min = dist;
				j = i;
			}
		}
		
		double lms = (resCont[j+k-1] + resCont[j]) / 2.0;
		
		System.out.println(lms);
	}
	
	
}
