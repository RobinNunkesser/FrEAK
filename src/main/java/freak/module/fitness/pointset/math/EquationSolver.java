/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.fitness.pointset.math;

import freak.module.fitness.pointset.math.matrix.Matrix;
import freak.module.fitness.pointset.math.matrix.Vector;
import freak.module.fitness.pointset.math.matrix.view.NormalView;
import freak.module.fitness.pointset.math.matrix.view.VectorView;
import sun.security.x509.IssuerAlternativeNameExtension;

/**
 * @author Basti LT
 *
 */
abstract public class EquationSolver {
	
	protected Matrix A; 
	
	protected Vector B;
	
	protected Vector result;
	
	int dimension;
	protected boolean canBeChanged;
	
	protected boolean upToDate;
	
	public EquationSolver(int dimension) {
		this.dimension = dimension;
		A = new Matrix(dimension,dimension);
		B = new Vector(dimension);
	}
	
	public EquationSolver(Matrix A, Vector B) throws Exception {
		if (!(A.isSquared())) throw new Exception("Nicht unterstuetzter Typ");
		this.A = A;
		this.B = B;
	}
	
	public void alloweChanging(){canBeChanged = true;}
	public void forbitChanging(){canBeChanged = false;}
	
	public Vector solve() throws Exception{
		if (!upToDate){
			
			if (canBeChanged || (!willSolveChangeTheMatrix())){
				solve(new NormalView(A),new VectorView(B));
			} else {
				NormalView nvA = new NormalView(A);
				NormalView nvB = new NormalView(B);
				nvA.setMatrix(nvA.copyModelFromView());
				nvB.setMatrix(nvB.copyModelFromView());
				solve(nvA,new VectorView(nvB));
			}
			upToDate = true;
		}
		return result;
	}
	
	abstract protected boolean willSolveChangeTheMatrix();
	
	
	abstract void solve(NormalView pMat , VectorView pB) throws Exception;
		
}
