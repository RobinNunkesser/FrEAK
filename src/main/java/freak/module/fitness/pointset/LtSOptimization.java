/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.fitness.pointset;

/*import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import javax.print.attribute.SupportedValuesAttribute;*/


import freak.core.control.Schedule;
/*import freak.core.fitness.AbstractStaticSingleObjectiveFitnessFunction;
import freak.core.modulesupport.ClassCollector;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.inspector.StringArrayWrapper;
import freak.core.population.Genotype;
import freak.module.fitness.pointset.math.EqSolvers;
import freak.module.fitness.pointset.math.EquationSolver;
import freak.module.fitness.pointset.math.matrix.Matrix;
import freak.module.fitness.pointset.math.matrix.Vector;
import freak.module.fitness.pointset.math.matrix.view.MatrixSimpleOperation;
import freak.module.fitness.pointset.math.matrix.view.NormalView;
import freak.module.fitness.pointset.math.matrix.view.TransposeView;
import freak.module.fitness.pointset.math.matrix.view.VectorView;
import freak.module.fitness.pointset.util.HyperPlane;
import freak.module.fitness.pointset.util.ResidualContainer;
import freak.module.fitness.pointset.util.ResidualHyperPlane;
import freak.module.searchspace.Cycle;
import freak.module.searchspace.PointSet;
import freak.module.searchspace.PointSetGenotype;*/
import freak.core.modulesupport.Configurable;
import freak.module.searchspace.PointSetGenotype;


public class LtSOptimization extends AbstractAdjustableStaticSingleObjectiveRR implements Configurable {
	
	public LtSOptimization(Schedule schedule){
		super(schedule,new LTSAdjust());
		m_zName = "LTS (Least Trimmed Squares)";
	} 
	
	protected double evaluateResiduals(){
		java.util.Arrays.sort(m_hResiduals);

		double valueOfSolution = 0;
		for(int i = 0; i < h; i++){
			valueOfSolution += m_hResiduals[i].squaredResidual;
		}
		
		return -valueOfSolution;
	}
	
	public String getDescription() {
		// TODO Auto-generated method stub
		return "The fitness value of an individual is the least squares error of the " +
				" subset of points represented by the given individual.\n We restrict the search to subsets of size d for d-dimensional points, because they uniquely describe possible solutions.";
	} 
	
}




/*
public class LtSOptimization extends
		AbstractStaticSingleObjectiveFitnessFunction implements Configurable {

	public LtSOptimization(Schedule schedule){
		super(schedule);
		if (schedule.getPhenotypeSearchSpace() instanceof PointSet) {
			PointSet ps = (PointSet) schedule.getPhenotypeSearchSpace();
			h=computeH(ps.getDimension(),ps.getPointDimension());
		}
	}
	private int[] chosenIndices=null;
	private double[] fittedHyperplane=null;

	int offset = -5000; // is used to divide
	
	//String ltsFileListCache[] = null;
	//Object[] ltsFileArrayCache = null;
	
	String m_zName = "LTS (Least Trimmed Squares)";
	String m_zInstanceName = "";
//	int m_zLtSChoice = 0;
	boolean interceptAdjust = false;
	int h;	
	
	public ResidualContainer [] m_hResiduals = null;

	private int computeH(int dimension,int pointDimension){
		double alpha = 0.5;
		return (int)(2.0*(int)(((double)(dimension+pointDimension+1))/2.0)-((double)dimension)+2.0*alpha*(((double)dimension)-(int)(((double)(dimension+pointDimension+1))/2.0)));
	}

	@Override
	protected double evaluate(Genotype genotype) {
		// TODO Auto-generated method stub
		
		PointSet.Point [] supportPoints = ((PointSetGenotype)genotype).getChoosenSubSet();
		
		PointSet.Point [] allPoints = ((PointSetGenotype)genotype).getPoints();
		
/*		if (schedule.getCurrentGeneration()==2) {
			for (int i=0;i<allPoints.length;i++) {
				System.out.println(allPoints[i].toString());
			}
		}*/
/*		
		int pointDim = ((PointSetGenotype)genotype).getPointDimension();
		
		int quantile = h;//((PointSetGenotype)genotype).getH();
		
		if (supportPoints.length == 0){
			return -pointDim +offset;
		}
		
		if (supportPoints.length != supportPoints[0].getDimension()){
			return -(supportPoints.length - pointDim) +offset; 
		}
				
		ResidualHyperPlane hyperPlane = new ResidualHyperPlane(supportPoints);
		
		m_hResiduals = new ResidualContainer[allPoints.length];
		
		hyperPlane.updateResiduals(m_hResiduals, allPoints);
		
		java.util.Arrays.sort(m_hResiduals);
		
				
		if (interceptAdjust){
			hyperPlane.computeParamLS(allPoints, m_hResiduals, quantile);
		}
		
		double valueOfSolution = 0;
		chosenIndices=new int[quantile];
		for(int i = 0; i < quantile; i++){
			valueOfSolution += m_hResiduals[i].squaredResidual;
			chosenIndices[i]=m_hResiduals[i].pointIndexInPointSet;
		}
		int dim=hyperPlane.getParameter().getDimension();
		fittedHyperplane=new double[dim];
		for (int i=0;i<dim;i++) {
			try {
				fittedHyperplane[i]=hyperPlane.getParameter().get(i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return -valueOfSolution;
	}
	
	public String getShortDescriptionForAdjust() {
		return "Adjust Intercept";
	}


	public String getLongDescriptionForAdjust() {
		return "Whether to perform intercept adjustment at each step.";
	}

	public void setPropertyAdjust(Boolean adjust){
		interceptAdjust = adjust.booleanValue();
	}
	
	public Boolean getPropertyAdjust(){
		return new Boolean(interceptAdjust);
	} 
	
	
	public String getDescription() {
		// TODO Auto-generated method stub
		return "The fitness value of an individual is the least squares error of the " +
				" subset of points represented by the given individual.\n We restrict the search to subsets of size d for d-dimensional points, because they uniquely describe possible solutions.";
	} 


	
/*	public String getShortDescriptionForPoints() {
		return "Data";
	}*/
/*	
	public double getOptimalFitnessValue()throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}
	
	public double getLowerBound() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public double getUpperBound() throws UnsupportedOperationException {
		return getOptimalFitnessValue();
	}
	
	public String getName() {
		return m_zName + ((!m_zInstanceName.equals(""))?(": "+m_zInstanceName):(""));
	}
	
	public Genotype getPhenotypeOptimum() throws UnsupportedOperationException{
		throw new UnsupportedOperationException();
	}
	
	public String getShortDescriptionForH() {
		return "h";
	}

	public String getLongDescriptionForH() {
		return "Size of the subset of points.";
	}

	
	public Integer getPropertyH(){
		return new Integer(h);
	}
	
	public void setPropertyH(Integer H){
		if (H.intValue() > 0)
			h = H.intValue();
	}

	/**
	 * @return the chosenIndices
	 */
/*	public int[] getChosenIndices() {
		return chosenIndices;
	}

	/**
	 * @return the fittedHyperplane
	 */
/*	public double[] getFittedHyperplane() {
		return fittedHyperplane;
	}
	
}
*/
