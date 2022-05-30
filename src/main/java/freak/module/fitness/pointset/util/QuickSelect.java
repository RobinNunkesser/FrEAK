package freak.module.fitness.pointset.util;



import java.io.Serializable;
import java.util.Random;





public class QuickSelect<Type extends Comparable> implements Serializable{
	static int t = 0;
	static Random re = new Random(); 
	
	// ##############################################################################
	public static void main(String[] args) {
		Integer a[] = { 2, 21, 4, 5, 7, 14, 2, 12, 1, 56, 23, 45, 67, 67, 67,
				67, 12, 45, 22, 34, 64, 2 };
		// 1,2,2,2,4,5,7,12,12,14,22,23,34,45,45,56,64,67,67,67,67
		//ArrayUndRang aR = new ArrayUndRang(a, 8); 
		//double aa = quickSelect(aR);
		Integer aa = null;
		QuickSelect<Integer> myQS = new QuickSelect<Integer>();
		try {
			aa = myQS.quickSelectNthSmallestElement(a, a.length-1);
		} catch (InvalidNthElementException e){
			System.out.println("Something is wrong here");
		}
		System.out.println("\n\nERGEBNIS--------------" + aa + "  in  " + t
				+ " Schritten\n");
		for (Integer b:a){
			System.out.print(b+" ");
		}
	}

	// ##############################################################################

	public Type quickSelectNthSmallestElement(Type [] comp, int nthElement) throws InvalidNthElementException{
		if ((nthElement < 0) || (nthElement >= comp.length)){
			throw new InvalidNthElementException(nthElement,0,comp.length);
		}
		
		Type result = null;
		try {
			result = helperQuickSelectNthSmallest(comp, nthElement, 0, comp.length);
		} catch (Exception e){
			e.printStackTrace();
		}
		return result; 
	} 
	
	public Type quickSelectNthSmallestElement(Type [] comp, int nthElement, int left, int right) throws InvalidNthElementException{
		if ((nthElement < 0) || (nthElement >= right - left)){
			throw new InvalidNthElementException(nthElement,0,right - left);
		}
		
		Type result = null;
		try {
			result = helperQuickSelectNthSmallest(comp, nthElement, left, right);
		} catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	private void swapUnsafe(Type [] comp, int i1, int i2){
		if (i1 == i2) return;
		Type temp = comp[i1];
		comp[i1] = comp[i2];
		comp[i2] = temp;
	}
	
	private Type helperQuickSelectNthSmallest(Type [] comp, int tempNthElement, int left, int right) throws Exception{
		t++;
		int iPivot = re.nextInt(right - left) + left;
		int l = left;
		int r = right-1;
		while (l!=r){
			for (;((l!=iPivot)&&(comp[l].compareTo(comp[iPivot])!=1));l++);
			for (;((r!=iPivot)&&(comp[r].compareTo(comp[iPivot])!=-1));r--);
			if (l != r){
				swapUnsafe(comp, l, r);
				if (l == iPivot) iPivot = r;
				else
				if (r == iPivot) iPivot = l;
			}
		};
		
		int leftSideSize = iPivot - left;
		//int rightSideSize = (right-1) - iPivot;
		// leftSize + rightSize + 1 = number of Elements between left(inclusive) and right(exclusive) !
		
		if (leftSideSize == tempNthElement){
			return comp[iPivot];
		} else if (tempNthElement < leftSideSize){
			/*Search in the leftSide*/
			return helperQuickSelectNthSmallest(comp, tempNthElement, left, iPivot);
		} else if (tempNthElement > leftSideSize){
			/*Search in the rightSide*/
			return helperQuickSelectNthSmallest(comp, tempNthElement-(leftSideSize+1), iPivot+1, right);
		}
		throw new Exception("This Case Cannot be");
	}
	


}


