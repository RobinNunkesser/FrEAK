package freak.module.fitness.pointset.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;


import freak.module.fitness.pointset.math.EqSolvers;
import freak.module.fitness.pointset.math.matrix.Matrix;
import freak.module.fitness.pointset.math.matrix.Vector;
import freak.module.fitness.pointset.math.matrix.view.MatrixSimpleOperation;
import freak.module.fitness.pointset.math.matrix.view.NormalView;
import freak.module.fitness.pointset.math.matrix.view.VectorView;
import freak.module.searchspace.PointSet;

public class ResidualHyperPlane extends HyperPlane implements Serializable{
	
	public ResidualHyperPlane(){
		
	}
	
	public ResidualHyperPlane(PointSet.Point [] supportPoints){
		super(supportPoints);
	}
	
	public void updateResiduals(Collection<ResidualContainer> col, PointSet.Point [] pAllPoints){
		
		int i = 0;
		for (Iterator<ResidualContainer> it = col.iterator(); it.hasNext();){
			ResidualContainer aktRes = it.next();
			
			
			// comute Residual value
			double resValue = 0;
			try{
				resValue = pAllPoints[i].getK(0) - parameter.get(0);
				for (int j = 1; j < dimension; j++){
					resValue -= parameter.get(j) * pAllPoints[i].getK(j); 
				}
			} catch (Exception e){throw new Error(e);}		
			aktRes.pointIndexInPointSet 	= i;
			aktRes.signedResidual 	= resValue;
			aktRes.squaredResidual 	= resValue * resValue;
			i++;
		}
		
	}
	
	private void updateResidual(PointSet.Point [] pAllPoints, ResidualContainer residual){
		double resValue = 0;
		try{
			resValue = pAllPoints[residual.pointIndexInPointSet].getK(0) - parameter.get(0);
			for (int j = 1; j < dimension; j++){
				resValue -= parameter.get(j) * pAllPoints[residual.pointIndexInPointSet].getK(j); 
			}
		} catch (Exception e){throw new Error(e);}
		
		residual.signedResidual  = resValue;
		residual.squaredResidual = resValue*resValue;
	}
	
	public void updateResiduals(ResidualContainer[] col, PointSet.Point [] pAllPoints){
		
		for (int i = 0; i < col.length;i++){
			// comute Residual value
			double resValue = 0;
			if (col[i] != null){
				col[i].pointIndexInPointSet 	= i;
			} else {
				col[i] = new ResidualContainer(i,0,0);
			}
			updateResidual(pAllPoints, col[i]);
		}
	}
	
	public void computeParamLS(double [] weighted_diag, PointSet.Point[] pAllPoints, ResidualContainer [] residuals, int quantile){
		/*
		 * 
		 * X = (n x p) Matrix n = anzahl Punkte; p = Dimension;
		 */
		
		Matrix m = new Matrix(dimension);
		NormalView vm = new NormalView(m);
		
		for (int i = 0; i < dimension; i++){
			for (int j = 0; j < dimension; j++){
				double w = 0;
				for (int k = 0; k < quantile; k++){
					double a,b;
					if (i == 0) {
						a = 1.0  * weighted_diag[k]; 
					} else {
						a = pAllPoints[residuals[k].pointIndexInPointSet].getK(i)* weighted_diag[k];
					}
					if (j == 0){
						b = 1.0  * weighted_diag[k];
					} else {
						b = pAllPoints[residuals[k].pointIndexInPointSet].getK(j) * weighted_diag[k];
					}
					w+=a*b;
				}
				try{
					vm.set(i, j, w);
				} catch(Exception e){throw new Error(e);}
			}
		}
		Vector b = new Vector(dimension);
		VectorView vb = new VectorView(b);
		for (int i = 0; i < dimension; i++){
			double w = 0; 
			for (int k = 0; k < quantile; k++){
				double a = 0;
				if (i == 0){
					a = 1.0 * weighted_diag[k];
				} else {
					a = pAllPoints[residuals[k].pointIndexInPointSet].getK(i)* weighted_diag[k];
				}
				w+= a * pAllPoints[residuals[k].pointIndexInPointSet].getK(0)* weighted_diag[k];
			}
			try{
				vb.set(i, w);
			} catch(Exception e){throw new Error(e);}
		}
		
		try{
			EqSolvers solver = new EqSolvers(m,b);
		
			parameter = new VectorView(solver.solve());
			
			for (int i = 0; i < quantile; i++){
				updateResidual(pAllPoints, residuals[i]);
			}
		} catch (Exception e){
			throw new Error(e);
		}
		
	}
	
	
	public void computeParamLS(PointSet.Point[] pAllPoints, ResidualContainer [] residuals, int quantile){
		computeParamLS(pAllPoints,residuals,1,quantile);
	}	

	public void computeParamLS(PointSet.Point[] pAllPoints, ResidualContainer [] residuals, int startpoint, int endpoint){
		/*
		 * 
		 * X = (n x p) Matrix n = anzahl Punkte; p = Dimension;
		 */
		Matrix m = new Matrix(dimension);
		NormalView vm = new NormalView(m); // vm := X^T * X
		for (int i = 0; i < dimension; i++){
			for (int j = 0; j < dimension; j++){
				double w = 0;
				for (int k = startpoint-1; k < endpoint; k++){
					double a,b;
					if (i == 0) {
						a = 1.0;
					} else {
						a = pAllPoints[residuals[k].pointIndexInPointSet].getK(i);
					}
					if (j == 0){
						b = 1.0;
					} else {
						b = pAllPoints[residuals[k].pointIndexInPointSet].getK(j);
					}
					w+=a*b;
				}
				try{
					vm.set(i, j, w);
				} catch(Exception e){throw new Error(e);}
			}
		}
		Vector b = new Vector(dimension);
		VectorView vb = new VectorView(b);
		for (int i = 0; i < dimension; i++){
			double w = 0; 
			for (int k =startpoint-1; k < endpoint; k++){
				double a = 0;
				if (i == 0){
					a = 1.0;
				} else {
					a = pAllPoints[residuals[k].pointIndexInPointSet].getK(i);
				}
				w+= a * pAllPoints[residuals[k].pointIndexInPointSet].getK(0);
			}
			try{
				vb.set(i, w);
			} catch(Exception e){throw new Error(e);}
		}
		
		try{
			EqSolvers solver = new EqSolvers(m,b);
		
			parameter = new VectorView(solver.solve());
			
			for (int i = startpoint-1; i < endpoint; i++){
				updateResidual(pAllPoints, residuals[i]);
			}
		} catch (Exception e){
			throw new Error(e);
		}
		
	}
	
	public void computeParamChebyshev(PointSet.Point[] pSubPoints){
		/* Note: The Array pSubPoints should contain the p Points that the Algo. has choosen 
		 * plus one randomly choosen point 
		 * */
		
		ResidualContainer [] residuals = new ResidualContainer[pSubPoints.length];
		for (int i = 0; i < residuals.length; i++){
			residuals[i] = new ResidualContainer(i,0,0);
		}
		
		Matrix m = new Matrix(dimension);
		
		NormalView vm = new NormalView(m); // vm := X * X^T
		for (int i = 0; i < dimension; i++){
			for (int j = 0; j < dimension; j++){
				double w = 0;
				for (int k = 0; k < pSubPoints.length; k++){
					double a,b;
					if (i == 0) {
						a = 1.0;
					} else {
						a = pSubPoints[k].getK(i);
					}
					if (j == 0){
						b = 1.0;
					} else {
						b = pSubPoints[k].getK(j);
					}
					w+=a*b;
				}
				try{
					vm.set(i, j, w);
				} catch(Exception e){throw new Error(e);}
			}
		}
		Vector b = new Vector(dimension);
		VectorView vb = new VectorView(b);
		for (int i = 0; i < dimension; i++){
			double w = 0; 
			for (int k = 0; k < pSubPoints.length; k++){
				double a = 0;
				if (i == 0){
					a = 1.0;
				} else {
					a = pSubPoints[k].getK(i);
				}
				w+= a * pSubPoints[k].getK(0);
			}
			try{
				vb.set(i, w);
			} catch(Exception e){throw new Error(e);}
		}
		
		try{
			EqSolvers solver = new EqSolvers(m,b);
		
			parameter = new VectorView(solver.solve());
			
			for (int i = 0; i < residuals.length; i++){
				updateResidual(pSubPoints, residuals[i]);
			}
		} catch (Exception e){
			throw new Error(e);
		}
		double epsilon = 0;
		double sum_sq = 0;
		double sum_abs = 0;
		for (int i = 0; i < pSubPoints.length; i++){
			sum_sq += residuals[i].squaredResidual;
			sum_abs+= Math.abs(residuals[i].signedResidual);
		}
		epsilon = sum_sq / sum_abs;
		double [] S = new double [pSubPoints.length];
		for (int i = 0; i < pSubPoints.length; i++){
			S[i] = (residuals[i].signedResidual < 0)?(-epsilon):(epsilon);
		}
		
		Vector Y = new Vector(dimension);
		VectorView Yb = new VectorView(Y);
		for (int i = 0; i < dimension; i++){
			double w = 0; 
			for (int k = 0; k < pSubPoints.length; k++){
				double a = 0;
				if (i == 0){
					a = 1.0;
				} else {
					a = pSubPoints[k].getK(i);
				}
				w+= a * S[k];   //pAllPoints[residuals[k].pointIndexInPointSet].getK(0);
			}
			try{
				Yb.set(i, w);
			} catch(Exception e){throw new Error(e);}
		}
		MatrixSimpleOperation vb_Y = new MatrixSimpleOperation(vb);
		
		try{
			vb_Y.subMatrix(Yb);
			
			EqSolvers solver = new EqSolvers(m,b);
		
			parameter = new VectorView(solver.solve());
			
			/*
			for (int i = 0; i < pSubPoints.length; i++){
				updateResidual(pSubPoints, residuals[i]);
			}*/
		} catch (Exception e){
			throw new Error(e);
		}
	}
	

	public static void main (String[] args){
		PointSet.Point [] testPoints = new PointSet.Point [] {
			new PointSet.Point(2),new PointSet.Point(2)
		};
		testPoints[0].setK(0, 1);
		testPoints[0].setK(1, 1);
		
		testPoints[1].setK(0, 2);
		testPoints[1].setK(1, 2);
		
		ResidualHyperPlane testPlane = new ResidualHyperPlane(testPoints);
		
		System.out.println("Done");
		
		PointSet.Point testPoint = new PointSet.Point(2);
		testPoint.setK(0, 1);
		testPoint.setK(1, 1);
		
		double resValue = -1;
		try{
			resValue = testPoint.getK(0) - testPlane.parameter.get(0);
			for (int j = 1; j < testPlane.dimension; j++){
				resValue -= testPlane.parameter.get(j) * testPoint.getK(j); 
			}
		} catch(Exception e){
			throw new Error(e);
		}
		
		System.out.println(resValue);
		
	}
		
}
