/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.util;

import java.util.Arrays;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;

/**
 * A class containing some mathematical functions.
 * 
 * @author Michael, Dirk, Robin
 */
public class FreakMath {

	public static final int NO_ELEMENT = -1;
	
	/**
	 * Returns k different numbers in {0, ..., n-1}.
	 * 
	 * @param schedule the current schedule used to retrieve a pseudo random number generator.
	 * @param k the number of values to draw.
	 * @param n defines the range {0, ..., n-1}.
	 */
	public static int[] getKofN(Schedule schedule, int k, int n) {
		/*
		 * Array of 0,0,0,0,0 interpreted as 0,1,2,3,4,5,...,n-1
		 * each new entry will be saved as value+1
		 */
		int[] a = new int[n];
		int[] result = new int[k];
		int j;
		RandomElement re = schedule.getRandomElement();
		
		for (int i = 0; i < k; i++) {
			j = (i < n - 1 ? re.choose(i, n - 1) : i);
			result[i] = (a[j] == 0 ? j : a[j] - 1);
			a[j] = (a[i] == 0 ? i + 1 : a[i]);
		}
		
		// if k = n, a permutation is requested
		if (k < n)
			Arrays.sort(result);
		return result;
	}
	
	public static int[][] getKDisjointSetsOfNNumbersExcludeH(Schedule schedule, int k, int n,int h) {
		/*
		 * Array of 0,0,0,0,0 interpreted as 0,1,2,3,4,5,...,n-1
		 * each new entry will be saved as value+1
		 */
		int[] a = new int[n];
		int setSize=(n-h)/k;
		int noBiggerSets=(n-h)%k;
		if (noBiggerSets>0) setSize++;
		int[][] result = new int[k][setSize];
		int j;
		int set=0;
		int position=0;
		RandomElement re = schedule.getRandomElement();		
		for (int i = 0; i < n; i++) {
			if (i==(n-h)) {
				break;
			}
			j = (i < n - 1 ? re.choose(i, n - 1) : i);
			result[set][position] = (a[j] == 0 ? j : a[j] - 1);
			a[j] = (a[i] == 0 ? i + 1 : a[i]);
			set++;
			set%=k;
			if (set==0) position++;
		}
		if (noBiggerSets!=0) {
			for (int i=noBiggerSets;i<k;i++) result[i][position]=FreakMath.NO_ELEMENT;
		}
		return result;		
	}
	/**
	 * Returns k disjoint sets containing all numbers in {0, ..., n-1}.
	 * 
	 * @param schedule the current schedule used to retrieve a pseudo random number generator.
	 * @param k the number of sets to divide into.
	 * @param n defines the range {0, ..., n-1}.
	 */
	public static int[][] getKDisjointSetsOfNNumbers(Schedule schedule, int k, int n) {
		return FreakMath.getKDisjointSetsOfNNumbersExcludeH(schedule, k, n, 0);
	}
	

}
