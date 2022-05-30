package freak.module.fitness.pointset.util;

public class QuickSelectDifferences extends QuickSelectDeltaMatrix {
	public double quickSelectNthSmallestDifference(double values [], int nthDifference) {
		double mvalues [] = new double [values.length];
		for (int i = 0; i < values.length; i++){
			mvalues[i] = -values[i];
		}
		double result = 0; 
		
		try{ 
			result = findNthGreatestElementOfXplusYHalf(values, mvalues, nthDifference, M_HALFS.LEFT_UPPER);
		} catch (InvalidLengthException e){
			e.printStackTrace();
			throw new Error("Something gone extremly wrong here ...");
		}
		return result;
	}
	
	
	public static void main(String[] args) {
		final int n = 100;
		double v [] = new double[n];
		for (int i = 0; i < v.length; i++) {
			v[i] = i % (n/10) + i;
		}               
		QuickSelectDifferences qsd = new QuickSelectDifferences();
		
		
		for (int i = 0; i < n*n/2; i++){
			System.out.println(qsd.quickSelectNthSmallestDifference(v,i)+", ");
		}
		               
	}
}
