/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.fitness.booleanfunction;

import java.util.BitSet;

import freak.Freak;
import freak.core.control.Schedule;
import freak.core.fitness.AbstractStaticMultiObjectiveFitnessFunction;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.inspector.StringArrayWrapper;
import freak.core.population.Genotype;
import freak.core.util.FreakMath;
import freak.module.searchspace.BooleanFunctionGenotype;
import freak.module.searchspace.logictree.AndNode;
import freak.module.searchspace.logictree.Data;
import freak.module.searchspace.logictree.OperatorNode;
import freak.module.searchspace.logictree.OperatorNodeVector;

/**
 * @author  Robin
 */
public class GenericPareto extends AbstractStaticMultiObjectiveFitnessFunction implements Configurable  {
	
	private static int bestLengthFound=42;

	private static int[][] subsetsArray;
	private static int[] baggingArray;
	private static long lastBatchUpdated=-1;	
	private static long lastRunUpdated=-1;
	private static long nextUpdate=1;
	
	public static final int OBJECTIVE_NONE = 0;
	public static final int OBJECTIVE_CASES = 1;
	public static final int OBJECTIVE_CONTROLS = 2;
	public static final int OBJECTIVE_CASESCONTROLS = 3; // liefert das gleiche wie F�lle-falsche Kontrollen
	public static final int OBJECTIVE_LENGTH = 4;
	public static final int OBJECTIVE_STANDARDISED_FITNESS = 5;
	public static final int OBJECTIVE_STANDARDISED_FITNESS_2X_CONTROL = 6;
	public static final int OBJECTIVE_GINI = 7;			// f�hrt dazu richtige F�lle und falsche Kontrollen anzugleichen :(
	public static final int OBJECTIVE_GINI_NEG = 8;
	public static final int OBJECTIVE_GINI_ON_AND_NODES = 9;
	public static final int OBJECTIVE_GINI_NEG_ON_AND_NODES = 10;
	public static final int OBJECTIVE_AVGRATIO = 11;
	public static final int OBJECTIVE_STANDARDISED_FITNESS_LENGTH = 12;
	public static final int OBJECTIVE_STANDARDISED_FITNESS_IMPLICIT_LENGTH = 13;
	
	String[] objectives = new String[] { "Cases", "Controls", "Cases+Controls","Length","Standardised Fitness","Standardised Fitness 2xControl","Gini","Gini (neg)","Gini on And-Nodes","Gini (neg) on And-Nodes","AvgRatio","Standardised Fitness with length","Standardised Fitness with implicit length"  };

	private static Schedule staticSchedule;

	private static int subsets=1;
	private int subsetsUpdate=0;
	private static int bagging=0;
	private int baggingUpdate=0;	
	private int length;
	
	private int dimensionOfObjectiveSpace=2;	
	
	private boolean predictingModelFound=false;
	
	private int[] paretoObjective=new int[3];
	private int[] paretoObjectiveForMonomials=new int[3];
	private boolean treatMonomialsDifferent=false;
	private int sizePruning=Integer.MAX_VALUE;
	private int monomialSizePruning=Integer.MAX_VALUE;;
	private int monomialPruning=Integer.MAX_VALUE;;
	
	
	
	public GenericPareto(Schedule schedule) {
		super(schedule);
		staticSchedule=schedule;
		paretoObjective[0]=OBJECTIVE_CASES;
		paretoObjective[1]=OBJECTIVE_CONTROLS;
		paretoObjective[2]=OBJECTIVE_NONE;
		paretoObjectiveForMonomials[0]=OBJECTIVE_CASES;
		paretoObjectiveForMonomials[1]=OBJECTIVE_CONTROLS;
		paretoObjectiveForMonomials[2]=OBJECTIVE_NONE;		
	}
	
	protected double[] evaluate(Genotype genotype) {
		int objectivesToFulfill=2;
		// staticSchedule wird wohl nicht mit synchronisiert
		if (staticSchedule==null) staticSchedule=schedule;
		if ((subsets>1)||(bagging>0)) {
			if ((lastRunUpdated<schedule.getCurrentRun())||(lastBatchUpdated<schedule.getCurrentBatch())) {
				// M�sste normalerweise auf 1 gesetzt werden, aber aus ungekl�rtem Grund startet nicht jeder Batch/Run in Generation 1 
				nextUpdate=schedule.getCurrentGeneration();
				lastBatchUpdated=schedule.getCurrentBatch();
				lastRunUpdated=schedule.getCurrentRun();
			}
			if ((subsets>1) && (nextUpdate==schedule.getCurrentGeneration())) 
			{					
				GenericPareto.newSubsets();
				if (subsetsUpdate==0) nextUpdate=-1; else nextUpdate+=subsetsUpdate;
	
			} else {
				if ((bagging>0) && (nextUpdate==schedule.getCurrentGeneration())) 
				{			
					GenericPareto.newBagging();
					if (baggingUpdate==0) nextUpdate=-1; else nextUpdate+=baggingUpdate;	
				}
			}
		}
		if (paretoObjective[2]!=0) {
			dimensionOfObjectiveSpace=3;
			objectivesToFulfill=3;
		}
		double[] rueckgabe = new double[dimensionOfObjectiveSpace];
		if (genotype instanceof BooleanFunctionGenotype) {
			BooleanFunctionGenotype bfg = (BooleanFunctionGenotype)genotype;
			length=bfg.evaluateSize();
			GenericPareto.Fulfillment polynomFulfillment=null;
			boolean prune = false;
			if ((length>sizePruning)||(bfg.getNoOfMonomials()>monomialPruning)||(bfg.getMaximumMonomialSize()>monomialSizePruning)) prune=true;
			if (!prune) {
				if ((subsets>1) || (bagging>0)) {
					polynomFulfillment = this.doSubsetBaggingComputations(bfg.getCharacteristicBitSet());
				}
				for (int i=0;i<dimensionOfObjectiveSpace;i++) {
					int objective;
					if ((bfg.getNoOfMonomials()==1) && (treatMonomialsDifferent)) {
						objective=paretoObjectiveForMonomials[i];
					} else {
						objective=paretoObjective[i];
					}
					switch (objective) 
					{
						case OBJECTIVE_CASES: 
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=bfg.evaluate1s();
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									if (polynomFulfillment.cases[j]<rueckgabe[i]) rueckgabe[i]=polynomFulfillment.cases[j];																
								}
							}
							// This check is only a quick and dirty one
							if (rueckgabe[i]==Data.getNum1Rows()) objectivesToFulfill--;
							break;
						}
						case OBJECTIVE_CONTROLS:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=bfg.evaluate0s();
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									if (polynomFulfillment.controls[j]<rueckgabe[i]) rueckgabe[i]=polynomFulfillment.controls[j];																
								}
							}
							// This check is only a quick and dirty one
							if (rueckgabe[i]==Data.getNum0Rows()) objectivesToFulfill--;							
							break;
						}
						case OBJECTIVE_CASESCONTROLS:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=bfg.evaluate();
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									if (polynomFulfillment.casescontrols[j]<rueckgabe[i]) rueckgabe[i]=polynomFulfillment.casescontrols[j];																
								}
							}
							// This check is only a quick and dirty one
							if (rueckgabe[i]==Data.getNumRows()) objectivesToFulfill--;														
							break;
						}
						case OBJECTIVE_LENGTH:
						{
							rueckgabe[i]=-length;
							// This check is only a quick and dirty one
							//if (rueckgabe[i]==-1) objectivesToFulfill--;
							objectivesToFulfill--;
							break;
						}
						case OBJECTIVE_STANDARDISED_FITNESS:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=bfg.evaluate1s()/(double)Data.getNum1Rows()/2.0+(bfg.evaluate0s())/(double)Data.getNum0Rows()/2.0;
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									double standardisedFitness=polynomFulfillment.cases[j]/(double)polynomFulfillment.casesInData[j]/2.0+polynomFulfillment.controls[j]/(double)polynomFulfillment.controlsInData[j]/2.0;
									if (standardisedFitness<rueckgabe[i]) rueckgabe[i]=standardisedFitness;																
								}
							}
							break;						
						}
						case OBJECTIVE_STANDARDISED_FITNESS_2X_CONTROL:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=bfg.evaluate1s()/(double)Data.getNum1Rows()*55.0/100.0+(bfg.evaluate0s())/(double)Data.getNum0Rows()*45.0/100.0;
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									double standardisedFitness=polynomFulfillment.cases[j]/(double)polynomFulfillment.casesInData[j]/3.0+polynomFulfillment.controls[j]/(double)polynomFulfillment.controlsInData[j]*2.0/3.0;
									if (standardisedFitness<rueckgabe[i]) rueckgabe[i]=standardisedFitness;																
								}
							}
							break;						
						}
						case OBJECTIVE_GINI:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=getObjectiveGini(bfg,bfg.evaluate1s(),bfg.evaluate0s(),Data.getNum1Rows(),Data.getNum0Rows());
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									double gini=getObjectiveGini(bfg,polynomFulfillment.cases[j],polynomFulfillment.controls[j],polynomFulfillment.casesInData[j],polynomFulfillment.controlsInData[j]);;
									if (gini<rueckgabe[i]) rueckgabe[i]=gini;																
								}
							}						
							break;
						}
						case OBJECTIVE_GINI_NEG:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=-getObjectiveGini(bfg,bfg.evaluate1s(),bfg.evaluate0s(),Data.getNum1Rows(),Data.getNum0Rows());
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									double gini=-getObjectiveGini(bfg,polynomFulfillment.cases[j],polynomFulfillment.controls[j],polynomFulfillment.casesInData[j],polynomFulfillment.controlsInData[j]);;
									if (gini<rueckgabe[i]) rueckgabe[i]=gini;																
								}
							}						
							break;
						}
						case OBJECTIVE_GINI_ON_AND_NODES:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=getObjectiveGiniOnAndNodes(bfg,bfg.evaluate1s(),bfg.evaluate0s(),Data.getNum1Rows(),Data.getNum0Rows());
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									double gini=getObjectiveGiniOnAndNodes(bfg,polynomFulfillment.cases[j],polynomFulfillment.controls[j],polynomFulfillment.casesInData[j],polynomFulfillment.controlsInData[j]);;
									if (gini<rueckgabe[i]) rueckgabe[i]=gini;																
								}
							}						
							break;
						}
						case OBJECTIVE_GINI_NEG_ON_AND_NODES:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=-getObjectiveGiniOnAndNodes(bfg,bfg.evaluate1s(),bfg.evaluate0s(),Data.getNum1Rows(),Data.getNum0Rows());
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									double gini=-getObjectiveGiniOnAndNodes(bfg,polynomFulfillment.cases[j],polynomFulfillment.controls[j],polynomFulfillment.casesInData[j],polynomFulfillment.controlsInData[j]);;
									if (gini<rueckgabe[i]) rueckgabe[i]=gini;																
								}
							}						
							break;
						}
						case OBJECTIVE_AVGRATIO:
						{
							rueckgabe[i]=getObjectiveAvgRatio(bfg);
							break;						
						}
						case OBJECTIVE_STANDARDISED_FITNESS_LENGTH:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=bfg.evaluate1s()/(double)Data.getNum1Rows()/3.0+(bfg.evaluate0s())/(double)Data.getNum0Rows()/3.0+(double)(sizePruning-length+1)/(double)(sizePruning)/3.0;
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									double standardisedFitness=polynomFulfillment.cases[j]/(double)polynomFulfillment.casesInData[j]/3.0+polynomFulfillment.controls[j]/(double)polynomFulfillment.controlsInData[j]/3.0+(double)(sizePruning-length+1)/(double)(sizePruning)/3.0;
									if (standardisedFitness<rueckgabe[i]) rueckgabe[i]=standardisedFitness;																
								}
							}
							break;						
						}						
						case OBJECTIVE_STANDARDISED_FITNESS_IMPLICIT_LENGTH:
						{
							if ((subsets==1) && (bagging==0)) {
								rueckgabe[i]=bfg.evaluate1s()/(double)Data.getNum1Rows()+(bfg.evaluate0s())/(double)Data.getNum0Rows()-(double)(length)/((double)sizePruning*(double)Data.getNumRows());
							} else {
								rueckgabe[i]=Integer.MAX_VALUE;
								for (int j=0;j<subsets;j++) {
									double standardisedFitness=polynomFulfillment.cases[j]/(double)polynomFulfillment.casesInData[j]+polynomFulfillment.controls[j]/(double)polynomFulfillment.controlsInData[j]-(double)(length)/((double)sizePruning*(double)Data.getNumRows());
									if (standardisedFitness<rueckgabe[i]) rueckgabe[i]=standardisedFitness;																
								}
							}
							break;						
						}						
					}
				}
				if (schedule.getCurrentGeneration()==1) {
					//System.out.println(System.currentTimeMillis()+";"+schedule.getCurrentGeneration()+";"+bfg.evaluate1s()+";"+bfg.evaluate0s()+";"+length+";"+bfg.toString());	
				}				
				if (objectivesToFulfill==0) {
					predictingModelFound=true;
					if (length<bestLengthFound) {
						bestLengthFound=length;
						//System.out.println(System.currentTimeMillis()+";"+schedule.getCurrentGeneration()+";"+bfg.evaluate1s()+";"+bfg.evaluate0s()+";"+length+";"+bfg.toString());							
					}
				}
			} else {
				rueckgabe[0]=Integer.MIN_VALUE;
				rueckgabe[1]=Integer.MIN_VALUE;		
				if (dimensionOfObjectiveSpace==3) rueckgabe[2]=Integer.MIN_VALUE;						
			}
		} else {
			rueckgabe[0]=0;
			rueckgabe[1]=0;		
			if (dimensionOfObjectiveSpace==3) rueckgabe[2]=0;		
		}
		return rueckgabe;
	}

	private GenericPareto.Fulfillment doSubsetBaggingComputations(BitSet treeBitSet){
		GenericPareto.Fulfillment f = new GenericPareto.Fulfillment(subsets);				
		BitSet resultBitSet=Data.getResultBitSet();
		for (int i=0;i<subsets;i++) {
			f.cases[i]=0;
			f.controls[i]=0;
			f.casesInData[i]=0;
			f.controlsInData[i]=0;
			int[] subset;
			if (subsets==1) subset=GenericPareto.baggingArray; else subset=GenericPareto.subsetsArray[i]; 			
			for (int j=0;j<subset.length;j++) {
				if (subset[j]!=FreakMath.NO_ELEMENT) {
					if (resultBitSet.get(subset[j])) {
						f.casesInData[i]++;
						if (treeBitSet.get(subset[j])) f.cases[i]++;
					} else {
						f.controlsInData[i]++;
						if (!treeBitSet.get(subset[j])) f.controls[i]++;
					}
				}
			}
			f.casescontrols[i]=f.cases[i]+f.controls[i];
		}
		return f;
	}
	
	private double getObjectiveGini(BooleanFunctionGenotype bfg,int num1s,int num0s,int numCases,int numControls) {
		double gini=0,p=0;
		if (bfg.getNoOfMonomials()==0)
		{
			gini=-1;
		}
		else
		{			
			//debug
//			System.out.println(bfg.getDnfbaum().toString());
			
//			System.out.println("1:"+num1s+" 0:"+num0s+" cases:"+numCases+" controls:"+numControls);
			
			
			double right1s=num1s/(double)numCases;
			double wrong0s=(numControls-num0s)/(double)numControls;
			if ((right1s+wrong0s)>0)
			     p=right1s/(double)(right1s+wrong0s);
			gini=2*p*(1-p);
			
//			System.out.println("gini:"+gini+"\n");
		}
		return gini;
	}

//
//Nicht den Gini-Koeffizienten �ber die gesamte
//logische Expression berechnen, sondern �ber die einzelnen Teile -- also
//f�r jede Konjunktion L_i. Also, sagen wir mal wir haben die DNF
//L_1 AND L_2 AND L_3 gefunden, dann berechnet man 2p_i(1-p_i), wobei
//
//p_i = Anzahl der durch L_i richtig erkl�rten F�lle / Gesamtanzahl an
//Observationen erkl�rt durch L_i,
//
//ausserdem berechnet man p_0, wobei
//
//p_0 = Anzahl der Kontrollen, die weder durch L_1, noch durch L_2, noch
//durch L_3 erkl�rt werden / Gesamtanzahl der Observationen, die durch keine
//der drei Konjunktionen erkl�rt werden.
//
//Und die Fitnessfunktion w�re dann sum_{i=0}^m 2*p_i(1-p_i), wobei m die
//Anzahl der Konjunktionen ist.
//
	
	private double getObjectiveGiniOnAndNodes(BooleanFunctionGenotype bfg,int num1s,int num0s,int numCases,int numControls)
	{
		double num1s_i,num0s_i;
		double p_i,p_0;
		double sum=0;
		
		
		p_0= bfg.getDnfbaum().getNumControlsExplainedByNoOne() 
		     /(double)(numCases+numControls-num0s-num1s);
		
		sum+=2*p_0*(1-p_0);
		
		for (int i=0;i<bfg.getNoOfMonomials();i++)
		{
		  num0s_i=(numControls-bfg.getDnfbaum().getNumWrongControls(i))/(double)numControls;
		  num1s_i=bfg.getDnfbaum().getNumFullfilledCases(i)/(double)numCases;
		  if (num1s_i>0)
		    p_i=num1s_i /(double)(num0s_i+num1s_i);
		  else
			p_i=0;

		  sum+=2*p_i*(1-p_i);
		}
	    return sum;
	}
	
	private double getObjectiveAvgRatio(BooleanFunctionGenotype bfg) {
		double ratio=0;
		double num1=Data.getNum1Rows();
		double num0=Data.getNum0Rows();
	
		if (bfg.getDnfbaum().getNoOfMonomials()==0)
		{
			ratio=-1;
		}
		else
		{
			if ((subsets==1) && (bagging==0)) {
				for(int i=0;i<bfg.getDnfbaum().getNoOfMonomials();i++)
				{

					int fCases=bfg.getDnfbaum().getNumFullfilledCases(i);
					int wControls=bfg.getDnfbaum().getNumWrongControls(i);
					int length=bfg.getDnfbaum().getNumLiteralsInMonom(i);
					
					ratio+=((double)fCases/num1)/( (double)fCases/num1+ (double)(wControls+1)/num0);
				}
				ratio=ratio/bfg.getDnfbaum().getNoOfMonomials()/bfg.getDnfbaum().getNoOfMonomials();
				ratio=2*ratio*(1-ratio);
			} else {			
				OperatorNodeVector monomials = bfg.getAllUsedAndNodes();
				OperatorNode[] monomialsArray = monomials.toArray();

				ratio=Integer.MAX_VALUE;
				for (int j=0;j<subsets;j++) {
					int subsetRatio=0;
					for (int i=0;i<monomialsArray.length;i++) {
						AndNode nextNode = (AndNode)monomialsArray[i];
						GenericPareto.Fulfillment monomialFulfillment = this.doSubsetBaggingComputations(nextNode.getValueBitset());
						
						subsetRatio+=((double)monomialFulfillment.cases[j]/monomialFulfillment.casesInData[j])/((double)(monomialFulfillment.controlsInData[j]-monomialFulfillment.controls[j]+1)/monomialFulfillment.controlsInData[j]); // /nextNode.getNumberOfChildren();
					}
					subsetRatio=subsetRatio/bfg.getDnfbaum().getNoOfMonomials()/bfg.getDnfbaum().getNoOfMonomials();
					if (subsetRatio<ratio) ratio=subsetRatio;																
				}
				
				
			}			
		}
		return ratio;
	}
		
	/**
	 */
	public static void newBagging() {
		if (GenericPareto.bagging>0) 
		{			
			GenericPareto.baggingArray=FreakMath.getKofN(staticSchedule, Data.getNumRows()-GenericPareto.bagging, Data.getNumRows());
/*			System.out.println("---------------------");			
			System.out.println("Bagging updated to:");
			int[] breaks = GenericPareto.baggingArray;
			for (int k=0;k<breaks.length;k++) {
				System.out.print(breaks[k]+",");	
			}
			System.out.println();
			System.out.println("---------------------");*/			
		}
	}
		
	/**
	 */
	public static void newSubsets() {
		if (GenericPareto.subsets>1) 
		{			
			GenericPareto.subsetsArray=FreakMath.getKDisjointSetsOfNNumbersExcludeH(staticSchedule, GenericPareto.subsets, Data.getNumRows(),GenericPareto.bagging);
/*			System.out.println("---------------------");			
			System.out.println("Subsets updated to:");
			int[][] breaks = GenericPareto.subsetsArray;
				for (int j=0;j<breaks.length;j++) {
					for (int k=0;k<breaks[0].length;k++) {
						System.out.print(breaks[j][k]+",");	
					}
					System.out.println();
				}
			System.out.println("---------------------");*/			
		}
	}
	
	private class Fulfillment {
		private int[] cases;
		private int[] controls;
		private int[] casescontrols;
		private int[] casesInData;
		private int[] controlsInData;
		/**
		 * 
		 */
		public Fulfillment(int subsets) {
			super();
			this.cases=new int[subsets];
			this.controls=new int[subsets];
			this.casescontrols=new int[subsets];
			this.casesInData=new int[subsets];
			this.controlsInData=new int[subsets];
		}		
	}
	/**
	 * @return  the dimensionOfObjectiveSpace
	 * @uml.property  name="dimensionOfObjectiveSpace"
	 */
	public int getDimensionOfObjectiveSpace() {
		return dimensionOfObjectiveSpace;
	}

	public String getName() {
		return "Generic Pareto";
	}

	public String getDescription() {
		return "Generic customisable Pareto fitness";
	}	
	
	public void setPropertyParetoObjective0(StringArrayWrapper saw) {
		paretoObjective[0] = saw.getIndex()+1;
	}
	
	public StringArrayWrapper getPropertyParetoObjective0() {
		return new StringArrayWrapper(objectives, paretoObjective[0]-1);
	}

	public String getShortDescriptionForParetoObjective0() {
		return "1.1 1st Pareto Objective";
	}
	
	public String getLongDescriptionForParetoObjective0() {
		return "First Pareto Objective.";
	}

	public void setPropertyParetoObjective1(StringArrayWrapper saw) {
		paretoObjective[1] = saw.getIndex()+1;
	}
	
	public StringArrayWrapper getPropertyParetoObjective1() {
		return new StringArrayWrapper(objectives, paretoObjective[1]-1);
	}

	public String getShortDescriptionForParetoObjective1() {
		return "1.2 2nd Pareto Objective";
	}
	
	public String getLongDescriptionForParetoObjective1() {
		return "Second Pareto Objective.";
	}
	
	public void setPropertyParetoObjective2(StringArrayWrapper saw) {
		paretoObjective[2] = saw.getIndex();
	}
	
	public StringArrayWrapper getPropertyParetoObjective2() {
		String[] objectivesPlusNone = new String[objectives.length+1];
		objectivesPlusNone[0]= "-";
		for (int i=0;i<objectives.length;i++) objectivesPlusNone[i+1]=objectives[i];
		return new StringArrayWrapper(objectivesPlusNone, paretoObjective[2]);
	}

	public String getShortDescriptionForParetoObjective2() {
		return "1.3 3rd Pareto Objective";
	}
	
	public String getLongDescriptionForParetoObjective2() {
		return "Third Pareto Objective.";
	}
	
	public void setPropertyTreatMonomialsDifferent(boolean treatMonomialsDifferent) {
		this.treatMonomialsDifferent=treatMonomialsDifferent;
	}
	
	
	public boolean getPropertyTreatMonomialsDifferent() {
		return this.treatMonomialsDifferent;
	}

	public String getShortDescriptionForTreatMonomialsDifferent() {
		return "2   Treat monomials different?";
	}
	
	public String getLongDescriptionForTreatMonomialsDifferent() {
		return "Flag to set up if monomials should have different objectives.";
	}
	
	public void setPropertyParetoObjectiveForMonomials0(StringArrayWrapper saw) {
		paretoObjectiveForMonomials[0] = saw.getIndex()+1;
	}
	
	public StringArrayWrapper getPropertyParetoObjectiveForMonomials0() {
		return new StringArrayWrapper(objectives, paretoObjectiveForMonomials[0]-1);
	}

	public String getShortDescriptionForParetoObjectiveForMonomials0() {
		return "2.1 1st Pareto Objective for monomial";
	}
	
	public String getLongDescriptionForParetoObjectiveForMonomials0() {
		return "First Pareto Objective for monomial.";
	}

	public void setPropertyParetoObjectiveForMonomials1(StringArrayWrapper saw) {
		paretoObjectiveForMonomials[1] = saw.getIndex()+1;
	}
	
	public StringArrayWrapper getPropertyParetoObjectiveForMonomials1() {
		return new StringArrayWrapper(objectives, paretoObjectiveForMonomials[1]-1);
	}

	public String getShortDescriptionForParetoObjectiveForMonomials1() {
		return "2.2 2nd Pareto Objective for monomial";
	}
	
	public String getLongDescriptionForParetoObjectiveForMonomials1() {
		return "Second Pareto Objective for monomial.";
	}
	
	public void setPropertyParetoObjectiveForMonomials2(StringArrayWrapper saw) {
		paretoObjectiveForMonomials[2] = saw.getIndex();
	}
	
	public StringArrayWrapper getPropertyParetoObjectiveForMonomials2() {
		String[] objectivesPlusNone = new String[objectives.length+1];
		objectivesPlusNone[0]= "-";
		for (int i=0;i<objectives.length;i++) objectivesPlusNone[i+1]=objectives[i];
		return new StringArrayWrapper(objectivesPlusNone, paretoObjectiveForMonomials[2]);
	}

	public String getShortDescriptionForParetoObjectiveForMonomials2() {
		return "2.3 3rd Pareto Objective for monomial";
	}
	
	public String getLongDescriptionForParetoObjectiveForMonomials2() {
		return "Third Pareto Objective for monomial.";
	}

	public Integer getPropertySubsets() {
		return new Integer(subsets);
	}
	
	public void setPropertySubsets(Integer subsets) {		
		GenericPareto.subsets = subsets.intValue();
		Freak.debug("Subsets set to "+GenericPareto.subsets,4);
	}

	public String getShortDescriptionForSubsets() {
		return "3.1 Subsets";
	}
	
	public String getLongDescriptionForSubsets() {
		return "Number of randomly chosen subsets of the data. Has no effect on the objectives " + objectives[OBJECTIVE_LENGTH-1] + " and " + objectives[OBJECTIVE_AVGRATIO-1]+".";
	}
	
	public Integer getPropertySubsetsUpdate() {
		return new Integer(subsetsUpdate);
	}
	
	public void setPropertySubsetsUpdate(Integer subsetsUpdate) {
		this.subsetsUpdate = subsetsUpdate.intValue();
	}

	public String getShortDescriptionForSubsetsUpdate() {
		return "3.2 Update Subsets";
	}
	
	public String getLongDescriptionForSubsetsUpdate() {
		return "Update Subsets every x generations (x=0: never update).";
	}
	
	public Integer getPropertyBagging() {
		return new Integer(bagging);
	}
	
	public void setPropertyBagging(Integer bagging) {
		GenericPareto.bagging = bagging.intValue();
	}

	public String getShortDescriptionForBagging() {
		return "4.1 Bagging";
	}
	
	public String getLongDescriptionForBagging() {
		return "Number of randomly excluded values of the data. Has no effect on the objectives " + objectives[OBJECTIVE_LENGTH-1] + " and " + objectives[OBJECTIVE_AVGRATIO-1]+".";
	}
	
	public Integer getPropertyBaggingUpdate() {
		return new Integer(baggingUpdate);
	}
	
	public void setPropertyBaggingUpdate(Integer baggingUpdate) {
		this.baggingUpdate = baggingUpdate.intValue();
	}

	public String getShortDescriptionForBaggingUpdate() {
		return "4.2 Update Bagging";
	}
	
	public String getLongDescriptionForBaggingUpdate() {
		return "Update Bagging every x generations (x=0: never update). Gets overridden by Update Subsets if subsets are activated";
	}

	public Integer getPropertySizePruning() {
		return new Integer(sizePruning);
	}
	
	public void setPropertySizePruning(Integer sizePruning) {
		this.sizePruning = sizePruning.intValue();
	}

	public String getShortDescriptionForSizePruning() {
		return "5.1 Size pruning";
	}
	
	public String getLongDescriptionForSizePruning() {
		return "Size bound for pruning.";
	}

	public Integer getPropertyMonomialSizePruning() {
		return new Integer(monomialSizePruning);
	}

	public void setPropertyMonomialSizePruning(Integer monomialSizePruning) {
		this.monomialSizePruning = monomialSizePruning.intValue();
	}

	public String getShortDescriptionForMonomialSizePruning() {
		return "5.2 Monomial size pruning";
	}
	
	public String getLongDescriptionForMonomialSizePruning() {
		return "Size bound for single monomials for pruning.";
	}


	public Integer getPropertyMonomialPruning() {
		return new Integer(monomialPruning);
	}
	
	public void setPropertyMonomialPruning(Integer monomialPruning) {
		this.monomialPruning = monomialPruning.intValue();
	}

	public String getShortDescriptionForMonomialPruning() {
		return "5.3 Monomial number pruning";
	}
	
	public String getLongDescriptionForMonomialPruning() {
		return "Bound on the number of monomials for pruning.";
	}

	/**
	 * @param paretoObjective the paretoObjective to set
	 */
	public void setParetoObjective(int[] paretoObjective) {
		this.paretoObjective = paretoObjective;
	}

	/**
	 * @param paretoObjectiveForMonomials the paretoObjectiveForMonomials to set
	 */
	public void setParetoObjectiveForMonomials(int[] paretoObjectiveForMonomials) {
		this.paretoObjectiveForMonomials = paretoObjectiveForMonomials;
	}

	/**
	 * @return the predictingModelFound
	 */
	public boolean isPredictingModelFound() {
		return predictingModelFound;
	}

	/**
	 * @param predictingModelFound the predictingModelFound to set
	 */
	public void setPredictingModelFound(boolean predictingModelFound) {
		this.predictingModelFound = predictingModelFound;
	}
	
/*	public Double getPropertyControlPruning() {
		return new Double(controlPruning);
	}
	
	public void setPropertyControlPruning(Double controlPruning) {
		this.controlPruning = controlPruning.doubleValue();
	}

	public String getShortDescriptionForControlPruning() {
		return "controlPruning";
	}
	
	public String getLongDescriptionForControlPruning() {
		return "Factor for control pruning.";
	}*/
	

}
