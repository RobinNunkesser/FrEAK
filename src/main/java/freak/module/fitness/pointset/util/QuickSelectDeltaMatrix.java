package freak.module.fitness.pointset.util;
//
//  QuickSearch2D.java
//  JavaQn
//
//  Created by Sebastian Ruthe on 14.02.08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.io.Serializable;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;


class InvalidLengthException extends Exception{
	public InvalidLengthException (String msg){
		super(msg);
	}
}

class WMedianElement implements Comparable<WMedianElement>{
	private double value;
	private int weight;
	public WMedianElement(){
		this.value = 0;
		this.weight = 0;
	}
	
	public WMedianElement(double value, int weight){
		this.value = value;
		this.weight = weight;
	} 
	public WMedianElement(final WMedianElement copy){
		this.value = copy.value;
		this.weight = copy.weight;
	}
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int compareTo(WMedianElement o) {
		// TODO Auto-generated method stub
		if (value < o.value) return -1;
		if (value > o.value) return 1;
		return 0;
	}
}


class MedianComputation {
	//returns the low median of line i restricted to [leftborder, rightborder[ 
	//works for SORTED vectors only!!
	
	double computeMedianForSortedArrays( double [] x, double [] y, int line, int leftborder, int rightborder) throws ArrayIndexOutOfBoundsException{
		if ((line < x.length) && (leftborder < rightborder)){
			int column = (leftborder + rightborder-1) / 2;
			
			double lmed = x[line] +  y[column];
			
			return lmed;
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}
	
	
	
}

class WeightedMedianComputation extends MedianComputation{
	QuickSelect<WMedianElement> myQS = new QuickSelect<WMedianElement>();

	
	private long sumWeights(WMedianElement [] array, int i_start, int i_end){
		long sum = 0;
		for (int i = i_start; i < i_end; i++) {
			sum+=array[i].getWeight();
		}
		return sum;
	}
	
	private double weightedMedianRecursive(WMedianElement[] array, int i_start, int i_end, long Wl, long Wr, double W){
		int n = i_end - i_start;
		
		try{
			myQS.quickSelectNthSmallestElement(array, n/2, i_start, i_end);
		} catch (InvalidNthElementException e){
			e.printStackTrace();
		}
		
		double m = array[i_start + n/2].getValue();
		
		int i_left_start = i_start;
		int i_left_end = i_start + n/2;
		int i_right_start = i_start + n/2 + 1;
		int i_right_end = i_end;
		
		long weightsOfSmaller = sumWeights(array, i_left_start, i_left_end);
		long weightsOfBigger = sumWeights(array, i_right_start, i_right_end);
		
		
		if ((weightsOfSmaller + Wl <= W) && (weightsOfBigger + Wr <= W)){
			return m;
			
		} else {
			if (weightsOfSmaller + Wl <= W){
				Wl += weightsOfSmaller + array[i_start + n/2].getWeight();
				return weightedMedianRecursive(array, i_right_start, i_right_end, Wl, Wr, W);
			} else {
				Wr += weightsOfBigger + array[i_start + n/2].getWeight();
				return weightedMedianRecursive(array, i_left_start, i_left_end, Wl, Wr, W);
			}
			
		}
	}
	public double weightedMedian(WMedianElement array[]){
		double W = 0.5 * (double) sumWeights(array, 0, array.length);
		return weightedMedianRecursive(array, 0, array.length, 0, 0, W);
	}
}


class RandomGrabBag {
	private Random re;
	private int min,max;
	ArrayList<Integer> bag;
	public RandomGrabBag (Random re, int min, int max){
		this.re = re;
		this.min = min;
		this.max = max;
		resetBag();
	}
	public RandomGrabBag(int min, int max){
		this.re = new Random();
		this.min = min;
		this.max = max;
		resetBag();
	}
	
	void resetBag(){
		if (bag == null) bag = new ArrayList<Integer>((int)(max-min)); else bag.clear();
		for (int i = min; i < max; i++){
			bag.add(i);
		}		
	}
	
	int grab(){
		return bag.remove(re.nextInt(bag.size())).intValue();
	}
}



public class QuickSelectDeltaMatrix implements Serializable{
	private final double EQUALITY_THRESHOLD = Double.MIN_VALUE * 5;
	private boolean randomised = false;
	
	public int DEBUG_LEVEL = -1; 
	 
	QuickSelect<Double> myQS = new QuickSelect<Double>();
	
	public double findNthGreatestElementOfXplusYHalf(double [] x, double [] y, long nthElement,M_HALFS half)throws InvalidLengthException{
		int lb [] = new int[x.length];
		int rb [] = new int[x.length];
		switch (half){
		case LEFT_UPPER:{ /*left upper*/
			for (int i = 0; i < rb.length; i++) {
				rb[i] = rb.length-i;
			}
			break;
		}
		case RIGHT_UPPER:{
			for (int i = 0; i < rb.length; i++) {
				rb[i] = rb.length;
				lb[i] = i;
			}
			break;
		}
		case LEFT_LOWER:{
			for (int i = 0; i < rb.length; i++) {
				rb[i] = i+1;
			}
			break;
		}
		case RIGHT_LOWER:{
			for (int i = 0; i < rb.length; i++) {
				rb[i] = rb.length;
				lb[i] = rb.length-i-1;
			}
			break;
		}
		}
		
		return findNthGreatestElementOfXplusY(x, y, nthElement, lb, rb);
	}
	
	public double findNthGreatestElementOfXplusY ( double [] x , double [] y, long nthElement) throws InvalidLengthException{
		
		int lb [] = new int[x.length];
		int rb [] = new int[x.length];
		for (int i = 0; i < rb.length; i++) {
			rb[i] = rb.length;
		}
		
		return findNthGreatestElementOfXplusY(x, y, nthElement,lb,rb);
	}
	
	public double findNthGreatestElementOfXplusY ( double [] x , double [] y, long nthElement, int [] lb, int [] rb) throws InvalidLengthException{
		int n = x.length;
		
		if (x.length != y.length) {
			throw new InvalidLengthException("Die Laenge der beiden Arrays muss uebereinstimmen");
		}
		
		
		Arrays.sort(x);
		Arrays.sort(y);
		
		for (int i = 0; i < n/2; i++){
			double temp = x[i];
			x[i] = x[n-i-1];
			x[n-i-1] = temp;
			temp = y[i];
			y[i] = y[n-i-1];
			y[n-i-1] = temp;
		}
		
		
		WMedianElement wmel [] = new WMedianElement[n];
		for (int i = 0; i < wmel.length; i++) {
			wmel[i] = new WMedianElement();
		}
		
		WeightedMedianComputation medComp = new WeightedMedianComputation();
		
		long L = 0;
		long R = n*n;
		
		while (R - L > n) {
			double weightedmed = 0;
			if (randomised) {
				int i;
				RandomGrabBag bag = new RandomGrabBag(0,n);
				do {
					i = bag.grab();
				} while (lb[i]+1 > rb[i]);
				try{
					weightedmed = medComp.computeMedianForSortedArrays(x,y,i,lb[i],rb[i]-1);
				} catch (ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
				}
			} else {
				//Johnson & Mizoguchi Step 3a
				//compute the medians of the candidates in each line:
				for (int i = 0; i < n; i++){
					if (lb[i] < rb[i]){
						wmel[i].setValue(medComp.computeMedianForSortedArrays(x, y, i, lb[i], rb[i]));
						wmel[i].setWeight(rb[i] - lb[i]);
					} else {
						wmel[i].setValue(0);
						wmel[i].setWeight(0);
					}	
				}
				weightedmed = medComp.weightedMedian(wmel);
			}
			int P_i [] = new int [n];
			int Q_i [] = new int [n];
			int j = 0;
			for (int i = n; i > 0; i--){
				double a_mMinusXi = weightedmed - x[i-1];
				
				while ((j < n) && (y[j] > a_mMinusXi + EQUALITY_THRESHOLD)){
					j++;
				}
				P_i[i-1] = j; // spaeter rb[i] = p_i[i]
			}
			j = n;
			for (int i = 0; i < n; i++){
				double a_mMinusXi = weightedmed-x[i];
				while ((j > 0) && (y[j-1] < a_mMinusXi-EQUALITY_THRESHOLD)){
					j--;
				}
				Q_i[i] = j; // spaeter lb[i] = Q_i[i];
			}
			long sumPi = 0; 	// bedeutet anzahl elemente die uebrig bleiben (gezaehlt ohne die linken grenzen) wenn die rechten grenzen gesetzt werden 
			long sumQi =0;		// bedeutet anzahl elemente die auf den linken seiten wegfallen d.h. die groe§er sind
			for (int i = 0; i < n; i++){
				sumPi += P_i[i];
				sumQi += Q_i[i];
			}
			// <=    entfernt und durch < ersetzt in darauffolgender if bedingung
			if (nthElement < sumPi){ // wenn das nth element kleiner oder gleich ist als die Elemente die auf der rechten seite wegfallen
				for (int i = 0; i<n; i++){
					rb[i] = P_i[i]; // dann duerfen sie auch wegfallen
				}
				// >    entfernt und durch >= ersetzt in darauffolgender if bedingung
			} else if (nthElement >= (sumQi)){
				for (int i = 0; i<n; i++){
					lb[i] = Q_i[i];
				}
			} else {
	        	return weightedmed;
			};
			
			long sumLb_i = 0; 
			long sumRb_i = 0;
		 	
		 	for (int i = 0; i < n; i++){
		 		sumLb_i += lb[i];
		 		sumRb_i += rb[i]; 
		 	}
		 	//
		 	L = sumLb_i; R = sumRb_i;
		 	
		 	
		 	if (DEBUG_LEVEL == 0){
			 	for (int i = 0; i < x.length; i++){
			 		System.out.println("");
					for (int k = 0; k < x.length;k++){
						if (k == lb[i]) 
							System.out.print("|");
						if (k == rb[i])
							System.out.print("|");
						System.out.print(x[i]+y[k] + ", ");
					}
				}
			 	System.out.println();
			 	System.out.println("------------");
			 }
		 	
		 	
		}
		n = (int)(R-L);
		Double values [] = new Double [n];
		
		int l = 0;
		for (int i = 0; i < x.length; i++){
			for (int j = lb[i]; j < rb[i];j++){
				values[l] = x[i]+y[j];
				l++;
			}
		}
		try{ 
			return myQS.quickSelectNthSmallestElement(values,(int) (n-(nthElement-L))-1 );
		} catch(InvalidNthElementException e) {
			e.printStackTrace();
			throw new Error("debug");
		}
		//return 0;
	}
	
}
