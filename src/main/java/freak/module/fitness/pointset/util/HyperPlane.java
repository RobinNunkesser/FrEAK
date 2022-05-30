package freak.module.fitness.pointset.util;

import freak.module.fitness.pointset.math.EqSolvers;
import freak.module.fitness.pointset.math.matrix.Matrix;
import freak.module.fitness.pointset.math.matrix.Vector;
import freak.module.fitness.pointset.math.matrix.view.MatrixSimpleOperation;
import freak.module.fitness.pointset.math.matrix.view.NormalView;
import freak.module.fitness.pointset.math.matrix.view.TransposeView;
import freak.module.fitness.pointset.math.matrix.view.VectorView;
import freak.module.searchspace.PointSet;
import java.io.Serializable;

public class HyperPlane implements Serializable {
	protected int dimension;
	protected VectorView parameter;
	
	public HyperPlane(){
		
	}
	
	public HyperPlane(PointSet.Point [] supportPoints){
		computeParam(supportPoints);
	}
	
	
	
	public int getDimension() {
		return dimension;
	}



	public VectorView getParameter() {
		return parameter;
	}



	public void computeParam(PointSet.Point [] supportPoints){
		
		dimension = supportPoints[0].getDimension();
		
		if (supportPoints.length < dimension){
			throw new Error("Not enough points to determine the hyperplane.");
		}
		Matrix m = new Matrix(dimension);
		Vector b = new Vector(dimension);
		
		NormalView vm = new NormalView(m);
		VectorView vb = new VectorView(b);
		
		try{
		
			for (int i = 0; i < dimension; i++){
				vm.set(i,0,1.0);
				vb.set(i,supportPoints[i].getK(0));
					for (int j = 1; j < dimension; j++){
						vm.set(i,j,supportPoints[i].getK(j));
					}
			}
			
			/* Next Steps Computes TM*M , TM*B */
			TransposeView tm = new TransposeView(m);
			
			MatrixSimpleOperation sop = new MatrixSimpleOperation(tm);
			
			NormalView tmm = sop.multMatrix(vm); // Calcs TM*M
			
						
			NormalView tmbm = sop.multMatrix(vb); // Calcs TM * B (result Matrix)
			
			Vector tmb = new Vector(tmbm); // Converts Matrix to Vector
						
			EqSolvers eqSolver = new EqSolvers(tmm.getMatrix(),tmb);
			//EqSolvers eqSolver = new EqSolvers(m,b);
			parameter = new VectorView(eqSolver.solve());
		} catch (Exception e){
			throw new Error(e);
		}
				
	}		
}
