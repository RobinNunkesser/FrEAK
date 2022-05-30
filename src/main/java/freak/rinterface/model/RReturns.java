/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.model;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import freak.Freak;
import freak.module.searchspace.logictree.DNFTree;

// Von Hand in double-Array umwandeln und length und die beiden MCR einzeln rausholen

/**
 * @author nunkesser
 *
 */
public class RReturns {
	
private static int generationFound=Integer.MAX_VALUE;
private static double bestMCRinTestData=Double.MAX_VALUE;
private static Vector<IndividualSummary> allMCRinTestData=new Vector<IndividualSummary>();
private static Vector<IndividualSummary> allMCRinTrainingData=new Vector<IndividualSummary>();
private static Vector<Double> bestMCRForLength=new Vector<Double>();
private static Vector<Double> bestMajorityMCRForLength=new Vector<Double>();
private static Vector<Double> bestMCRTrainingForLength=new Vector<Double>();
private static Vector<Integer> lengths=new Vector<Integer>();
private static int[] bestBitSet;
private static int[] resultBitSet;
private static int lengthOfChosen;
private static double improvement = 0.02;
private static double bestMCRChosen;
private static SDataFrame dataFrame;
private static int[] chosenIndices=null;
private static double[] fittedHyperplane=null;
private static double residual=Double.MAX_VALUE;
private static DNFTree[] allTrees;
private static double majorityMcrTest;
private static double majorityMcrTrain;

public static void clear() {
	Freak.debug("Clearing stored return parameters",4);
	generationFound=Integer.MAX_VALUE;
	bestMCRinTestData=Double.MAX_VALUE;
	allMCRinTestData=new Vector<IndividualSummary>();
	allMCRinTrainingData=new Vector<IndividualSummary>();
	bestMCRForLength=new Vector<Double>();
	bestMajorityMCRForLength=new Vector<Double>();
	bestMCRTrainingForLength=new Vector<Double>();	
	lengths=new Vector<Integer>();
	dataFrame=new SDataFrame();
	fittedHyperplane=null;
	chosenIndices=null;
	residual=Double.MAX_VALUE;
}
/**
 * @return the bestMCRinTestData
 */
public static double getBestMCRinTestData() {
	return bestMCRinTestData;
}

/**
 * @param bestMCRinTestData the bestMCRinTestData to set
 */
public static void setBestMCRinTestData(double bestMCRinTestData) {
	RReturns.bestMCRinTestData = bestMCRinTestData;
}

/**
 * @return the allMCRinTestData
 */
public static Vector<IndividualSummary> getAllMCRinTestData() {
	return allMCRinTestData;
}

/**
 * @return the allMCRinTrainingData
 */
public static Vector<IndividualSummary> getAllMCRinTrainingData() {
	return allMCRinTrainingData;
}

/**
 * @return the best MCR in TestData with respect to the result on the training data
 */
public static double[] getBestMCRinTestDataWRTTraining() {
	Vector<BitSet> testBitsets=new Vector<BitSet>();	
	Collections.sort(allMCRinTrainingData);
	Collections.sort(allMCRinTestData);
	int lastLength = -1;
	double bestMCRTrainLength=Double.MAX_VALUE;
	double bestMCRLength=Double.MAX_VALUE;	
	double bestMCRTrain=Double.MAX_VALUE;
	double bestMCR=Double.MAX_VALUE;
	double bestTrainGecco1=Double.MAX_VALUE;
	double bestMCRGecco1=Double.MAX_VALUE;
	double bestTrainGecco2=Double.MAX_VALUE;
	double bestMCRGecco2=Double.MAX_VALUE;
	Iterator<IndividualSummary> it1=allMCRinTrainingData.iterator();
	Iterator<IndividualSummary> it2=allMCRinTestData.iterator();
	while (it1.hasNext() && it2.hasNext()) {
		IndividualSummary trainIndividual = it1.next();
		IndividualSummary testIndividual = it2.next();
		double trainMCR=trainIndividual.getMcr();
		double trainGecco1=trainIndividual.getGeccoFit1();
		double trainGecco2=trainIndividual.getGeccoFit2();
		double testMCR=testIndividual.getMcr();
		if (trainIndividual.getLength()>lastLength) {
			if (lastLength>0) {
				lengths.add(new Integer(lastLength));
				bestMCRForLength.add(new Double(bestMCRLength));
				bestMCRTrainingForLength.add(new Double(bestMCRTrainLength)); 

				// Hier testBitSets auswerten und neu setzen. Result aus trainIndiv. In Array aufaddieren und mitteln
				double[] meanValues=new double[testIndividual.getRows()];
				Arrays.fill(meanValues, 0.0);
				Iterator<BitSet> bit=testBitsets.iterator();
				while (bit.hasNext()) {
					BitSet bs = bit.next();
//					System.out.println("BitSet for mean: "+bs);
					for (int i=0;i<meanValues.length;i++) if (bs.get(i)) meanValues[i]+=1;											
				}
				BitSet meanBitset=new BitSet(meanValues.length);
				meanBitset.clear();
				for (int i=0;i<meanValues.length;i++) {
					if (meanValues[i]/((double)testBitsets.size())>0.5) meanBitset.set(i);
//					System.out.print(meanValues[i]/((double)testBitsets.size())+",");
				}				
/*				System.out.println();
				System.out.println(meanBitset.toString());
				System.out.println(testIndividual.getResultBitset().toString());*/				
				meanBitset.xor(testIndividual.getResultBitset());
				bestMajorityMCRForLength.add(new Double(((double)meanBitset.cardinality())/((double)meanValues.length)));				
			}
			lastLength=trainIndividual.getLength();
			bestMCRTrainLength=Double.MAX_VALUE;
			bestMCRLength=Double.MAX_VALUE;				
			testBitsets=new Vector<BitSet>();
		}
		// Hier testBitSets aktualisieren
		if (trainMCR<=bestMCRTrainLength) {
			if (trainMCR<bestMCRTrainLength) {
				testBitsets=new Vector<BitSet>();
				testBitsets.add(testIndividual.getValueBitset());
				bestMCRTrainLength=trainMCR;
				bestMCRLength=testMCR;
			} else {
				testBitsets.add(testIndividual.getValueBitset());
			}
		}	
		
		
		if (trainMCR<bestMCRTrain) {
			bestMCRTrain=trainMCR;
			bestMCR=testMCR;
			setBestBitSet(testIndividual.getValueBitset(), testIndividual.getRows());
			setResultBitSet(testIndividual.getResultBitset(),testIndividual.getRows());
		}

		if (trainGecco1<bestTrainGecco1) {
			bestTrainGecco1=trainGecco1;
			bestMCRGecco1=testMCR;
		}

		if (trainGecco2<bestTrainGecco2) {
			bestTrainGecco2=trainGecco2;
			bestMCRGecco2=testMCR;
		}

		if (!it1.hasNext()) {
			lengths.add(new Integer(lastLength));
			bestMCRForLength.add(new Double(bestMCRLength));
			bestMCRTrainingForLength.add(new Double(bestMCRTrainLength));	
			// Hier testBitSets auswerten und neu setzen. Result aus trainIndiv. In Array aufaddieren und mitteln
			// Hier testBitSets auswerten und neu setzen. Result aus trainIndiv. In Array aufaddieren und mitteln
			double[] meanValues=new double[testIndividual.getRows()];
			Arrays.fill(meanValues, 0.0);
			Iterator<BitSet> bit=testBitsets.iterator();
			while (bit.hasNext()) {
				BitSet bs = bit.next();
//				System.out.println("BitSet for mean: "+bs);
				for (int i=0;i<meanValues.length;i++) if (bs.get(i)) meanValues[i]+=1;											
			}
			BitSet meanBitset=new BitSet(meanValues.length);
			meanBitset.clear();
			for (int i=0;i<meanValues.length;i++) {
				if (meanValues[i]/((double)testBitsets.size())>0.5) meanBitset.set(i);
//				System.out.print(meanValues[i]/((double)testBitsets.size())+",");
			}		
			
/*	
		    File bestLongest = new File("best.csv");
		    try {
		    	FileWriter fout = new FileWriter(bestLongest);		    	
		        fout.write(trainIndividual.toString());
		        fout.close();
		    }
		    catch (Exception e) {
		    	System.err.println("Error: I/O error while writing the dot source");
		    }*/
			
/*			System.out.println();
			System.out.println(meanBitset.toString());
			System.out.println(testIndividual.getResultBitset().toString());*/
			meanBitset.xor(testIndividual.getResultBitset());
			bestMajorityMCRForLength.add(new Double(((double)meanBitset.cardinality())/((double)meanValues.length)));
			testBitsets=new Vector<BitSet>();
			
		}
	}	
	lengthOfChosen=0;
	double mcrOfChosenTrain=Double.MAX_VALUE;
	double mcrOfChosen=Double.MAX_VALUE;
	Iterator<Integer> it3=lengths.iterator();
	Iterator<Double> it4=bestMCRTrainingForLength.iterator();
	Iterator<Double> it5=bestMCRForLength.iterator();
	while (it3.hasNext() && it4.hasNext() && it5.hasNext()) {
		int length= it3.next().intValue();
		double trainMCR=it4.next().doubleValue();
		double testMCR=it5.next().doubleValue();
		if (trainMCR<java.lang.Math.pow((1-improvement),(length-lengthOfChosen))*mcrOfChosenTrain) {
			lengthOfChosen=length;
			mcrOfChosenTrain=trainMCR;
			mcrOfChosen=testMCR;
		}
	}
	double[] returnValue= {bestMCR,mcrOfChosen,bestMCRGecco1,bestMCRGecco2};
	return returnValue;
}
/**
 * @return the bestMCRForLength
 */
public static double[] getBestMCRForLength() {
	double[] returnArray = new double[bestMCRForLength.size()];
	Iterator<Double> it = bestMCRForLength.iterator();
	int index=0;
	while (it.hasNext()) {
		returnArray[index]=it.next().doubleValue();
		index++;
	}
	return returnArray;
}
/**
 * @return the bestMCRTrainingForLength
 */
public static double[] getBestMCRTrainingForLength() {
	double[] returnArray = new double[bestMCRTrainingForLength.size()];
	Iterator<Double> it = bestMCRTrainingForLength.iterator();
	int index=0;
	while (it.hasNext()) {
		returnArray[index]=it.next().doubleValue();
		index++;
	}
	return returnArray;
}
/**
 * @return the lengths
 */
public static int[] getLengths() {
	int[] returnArray = new int[lengths.size()];
	Iterator<Integer> it = lengths.iterator();
	int index=0;
	while (it.hasNext()) {
		returnArray[index]=it.next().intValue();
		index++;
	}
	return returnArray;
}
/**
 * @return the chosenLength
 */
public static int getLengthOfChosen() {
	return lengthOfChosen;
}
/**
 * @return the bestMajorityMCRForLength
 */
public static double[] getBestMajorityMCRForLength() {
	double[] returnArray = new double[bestMajorityMCRForLength.size()];
	Iterator<Double> it = bestMajorityMCRForLength.iterator();
	int index=0;
	while (it.hasNext()) {
		returnArray[index]=it.next().doubleValue();
		index++;
	}
	return returnArray;	
}
/**
 * @param allMCRinTestData the allMCRinTestData to set
 */
public static void setAllMCRinTestData(
		Vector<IndividualSummary> allMCRinTestData) {
	RReturns.allMCRinTestData = allMCRinTestData;
}
/**
 * @param allMCRinTrainingData the allMCRinTrainingData to set
 */
public static void setAllMCRinTrainingData(
		Vector<IndividualSummary> allMCRinTrainingData) {
	RReturns.allMCRinTrainingData = allMCRinTrainingData;
}
/**
 * @return the bestBitSet
 */
public static int[] getBestBitSet() {
	return bestBitSet;
}
/**
 * @param bestBitSet the bestBitSet to set
 */
public static void setBestBitSet(BitSet bestBitSetOriginal,int length) {
	int[] bestBitSet = new int[length];
	Arrays.fill(bestBitSet, 0);
	for (int i=0;i<length;i++) if (bestBitSetOriginal.get(i)) bestBitSet[i]=1;
	RReturns.bestBitSet = bestBitSet;
}
/**
 * @return the resultBitSet
 */
public static int[] getResultBitSet() {
	return resultBitSet;
}
/**
 * @param resultBitSet the resultBitSet to set
 */
public static void setResultBitSet(BitSet resultBitSetOriginal,int length) {
	int[] resultBitSet = new int[length];
	Arrays.fill(resultBitSet, 0);
	for (int i=0;i<length;i++) if (resultBitSetOriginal.get(i)) resultBitSet[i]=1;	
	RReturns.resultBitSet = resultBitSet;
}
/**
 * @return the dataFrame
 */
public static SDataFrame getDataFrame() {
	return dataFrame;
}
/**
 * @param dataFrame the dataFrame to set
 */
public static void setDataFrame(SDataFrame dataFrame) {
	RReturns.dataFrame = dataFrame;
}
/**
 * @return the chosenIndices
 */
public static int[] getChosenIndices() {
	return chosenIndices;
}
/**
 * @param chosenIndices the chosenIndices to set
 */
public static void setChosenIndices(int[] chosenIndices) {
	RReturns.chosenIndices = chosenIndices;
}
/**
 * @return the fittedHyperplane
 */
public static double[] getFittedHyperplane() {
	return fittedHyperplane;
}
/**
 * @param fittedHyperplane the fittedHyperplane to set
 */
public static void setFittedHyperplane(double[] fittedHyperplane) {
	RReturns.fittedHyperplane = fittedHyperplane;
}
/**
 * @return the residual
 */
public static double getResidual() {
	return residual;
}
/**
 * @param residual the residual to set
 */
public static void setResidual(double residual) {
	RReturns.residual = residual;
}
/**
 * @return the allTrees
 */
public static DNFTree[] getAllTrees() {
	return allTrees;
}
/**
 * @param allTrees the allTrees to set
 */
public static void setAllTrees(DNFTree[] allTrees) {
	RReturns.allTrees = allTrees;
}

/**
 * @param allTrees the allTrees to set
 */
public static void setAllTrees(Vector<DNFTree> allTrees) {
	RReturns.allTrees= new DNFTree[allTrees.size()];
	allTrees.copyInto(RReturns.allTrees);
}
/**
 * @return the generationFound
 */
public static int getGenerationFound() {
	return generationFound;
}
/**
 * @param generationFound the generationFound to set
 */
public static void setGenerationFound(int generationFound) {
	RReturns.generationFound = generationFound;
}
public static double getMajorityMcrTest() {
	return majorityMcrTest;
}
public static void setMajorityMcrTest(double majorityMcrTest) {
	RReturns.majorityMcrTest = majorityMcrTest;
}
public static double getMajorityMcrTrain() {
	return majorityMcrTrain;
}
public static void setMajorityMcrTrain(double majorityMcrTrain) {
	RReturns.majorityMcrTrain = majorityMcrTrain;
}

}
