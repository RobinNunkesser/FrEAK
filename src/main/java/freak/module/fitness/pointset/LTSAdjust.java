package freak.module.fitness.pointset;

import java.util.Arrays;

import freak.core.population.Genotype;
import freak.module.fitness.pointset.util.ResidualContainer;
import freak.module.fitness.pointset.util.ResidualHyperPlane;
import freak.module.searchspace.PointSet;
import freak.module.searchspace.PointSetGenotype;

public class LTSAdjust extends AbstractAdjust{
	@Override
	public void adjust(AbstractRobustRegressionFitness toBeAdjusted,
			Genotype genotype) {
	// TODO Auto-generated method stub
		ResidualContainer [] res = toBeAdjusted.getResiduals();
		Arrays.sort(res);
		ResidualHyperPlane hplane = toBeAdjusted.getResidualHyperPlane();
		
		
		int n = res.length;
		
		try{
		for(int i = 0; i < n; i++){
			res[i].signedResidual += hplane.getParameter().get(0);
		}
		} catch (Exception e){
			throw new Error("bla");
		}
			
		int k = n / 2 + 1; 
		double minSumSq = 0;
		double assoziatedMean = 0;
		
		double lastMinSumSq = 0;
		double lastAssoziatedMean = 0;
		
		for (int i = 0; i < res.length-k+1;i++){
			if (i == 0){
				for (int j = 0; j < k; j++){
					assoziatedMean += res[j].signedResidual;
				}
				assoziatedMean /= k;
				for (int j = 0; j < k; j++){
					minSumSq += (res[j].signedResidual-assoziatedMean)*
						(res[j].signedResidual-assoziatedMean);
				}
				lastAssoziatedMean = assoziatedMean;
				lastMinSumSq = minSumSq;
			} else {
				double nextMean = (k * lastAssoziatedMean - res[i-1].signedResidual + res[i+k-1].signedResidual)/k; 
				double nextSumSq = lastMinSumSq - res[i-1].signedResidual*res[i-1].signedResidual
				                                + res[i+k-1].signedResidual*res[i+k-1].signedResidual
				                                - k*nextMean*nextMean + k*lastAssoziatedMean*lastAssoziatedMean;
				
				if (minSumSq > nextSumSq){
					minSumSq = nextSumSq;
					assoziatedMean = nextMean;
				}
				lastAssoziatedMean = nextMean;
				lastMinSumSq = nextSumSq;
			}
		}
		try {
			hplane.getParameter().set(0,assoziatedMean);
		} catch (Exception e){
			e.printStackTrace();
			
		}
		
		PointSet.Point [] allPoints = ((PointSetGenotype)genotype).getPoints();
		
		hplane.updateResiduals(res, allPoints );
	}
	
	
	public static void main (String[] args){
		double [] res = new double [] {90,93,86,92,95,83,75,40,88,80};
		Arrays.sort(res);
		
		int n = res.length;
		int k = n / 2 + 1;
		double minSumSq = 0;
		double assoziatedMean = 0;
		
		double lastMinSumSq = 0;
		double lastAssoziatedMean = 0;
		
		for (int i = 0; i < res.length-k+1;i++){
			if (i == 0){
				for (int j = 0; j < k; j++){
					assoziatedMean += res[j];
				}
				assoziatedMean /= k;
				for (int j = 0; j < k; j++){
					minSumSq += (res[j]-assoziatedMean)*
						(res[j]-assoziatedMean);
				}
				lastAssoziatedMean = assoziatedMean;
				lastMinSumSq = minSumSq;
			} else {
				double nextMean = (k * lastAssoziatedMean - res[i-1] + res[i+k-1])/k; 
				double nextSumSq = lastMinSumSq - res[i-1]*res[i-1]
				                                + res[i+k-1]*res[i+k-1]
				                                - k*nextMean*nextMean + k*lastAssoziatedMean*lastAssoziatedMean;
				
				if (minSumSq > nextSumSq){
					minSumSq = nextSumSq;
					assoziatedMean = nextMean;
				}
				lastAssoziatedMean = nextMean;
				lastMinSumSq = nextSumSq;
			}
		}
		System.out.println(assoziatedMean);
	}
}
