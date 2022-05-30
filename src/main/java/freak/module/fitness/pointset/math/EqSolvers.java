/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.fitness.pointset.math;



import freak.module.fitness.pointset.math.matrix.Matrix;
import freak.module.fitness.pointset.math.matrix.Vector;
import freak.module.fitness.pointset.math.matrix.view.MatrixSimpleOperation;
import freak.module.fitness.pointset.math.matrix.view.NormalView;
import freak.module.fitness.pointset.math.matrix.view.VectorView;


/**
 * @author Basti LT
 *
 */



class GaussianElimination extends EquationSolver{
	
	public GaussianElimination(Matrix m, Vector b) throws Exception {
		super(m,b);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	void solve(NormalView pMat , VectorView pB) throws Exception{
					
		// TODO Auto-generated method stub
					// M rows N cols
		if ((!pMat.isSquared()) || pB.getDimension() != pMat.getCol())
            throw new RuntimeException("Illegal matrix dimensions.");

		// Evtl. muessen hier Kopien der Matrizen erstellt werden
					
		
		MatrixSimpleOperation matSOP = new MatrixSimpleOperation(pMat);
		MatrixSimpleOperation bSOP = new MatrixSimpleOperation(pB);
		
        // Gaussian elimination with partial pivoting
        for (int i = 0; i < pMat.getCol(); i++) {

            // find pivot row and swap
            int max = i;
            for (int j = i + 1; j < pMat.getCol(); j++)
                if (Math.abs(pMat.get(j,i) ) > Math.abs(pMat.get(max,i)))
                    max = j;
            
            
            if (i != max){
            	matSOP.swapRows(i, max);
            	bSOP.swapRows(i, max);
            }
            // singular
            if (pMat.get(i,i) == 0.0) 
            	throw new RuntimeException("Matrix is singular.");

            // pivot within b
            for (int j = i + 1; j < pMat.getCol(); j++)
                pB.set(j, pB.get(j)  -  pB.get(i) * pMat.get(j,i) / pMat.get(i,i));

            // pivot within A
            for (int j = i + 1; j < pMat.getCol(); j++) {
                double m = pMat.get(j,i) / pMat.get(i,i);
                for (int k = i+1; k < pMat.getCol(); k++) {
                    pMat.set(j,k , pMat.get(j,k) - pMat.get(i,k) * m);
                }
                pMat.set(j,i,0.0);
            }
        }

        // back substitution
        result = new Vector(pMat.getCol());
        VectorView resView = new VectorView(result);
        for (int j = pMat.getCol() - 1; j >= 0; j--) {
            double t = 0.0;
            for (int k = j + 1; k < pMat.getCol(); k++)
                t += pMat.get(j,k) * resView.get(k);
            resView.set(j, (  pB.get(j) - t) / pMat.get(j,j));
        }
        
	}
	
	@Override
	protected boolean willSolveChangeTheMatrix() {
		// TODO Auto-generated method stub
		return true;
	}
}


class ConjugrateGradientSolver extends EquationSolver {
    
	
	
	public ConjugrateGradientSolver(Matrix m, Vector b) throws Exception{
		super(m,b);
	}
	
	@Override
	void solve(NormalView pMat, VectorView pB) throws Exception {
		// TODO Auto-generated method stub
		double a_k,b_k;
		if (!pMat.isSquared()) throw new Error("Matrix must be a squared one");
		
		int d = pMat.getRow();
		
		Vector x = new Vector(d);
		VectorView vx = new VectorView(x);
		
		for(int i=0;i<d;i++)
			vx.set(i, 1.0);
		//      x[i]=sqrt(m[f(i,i)]);

		// To solve this linear equation system
		// the Conjugate Gradient (cg) Algorithm
		// of Hestenes and Stiefel
		// is used.
		//  Stoer / Bulirsch
		// "NUmerische Mathematik 2"
		// Seite 304 ff

		//x_0 = (1,...,1)
		//r=b-A * x_0
		//p=r
		Vector r = new Vector(d);
		Vector p = new Vector(d);
		VectorView vr = new VectorView(r);
		VectorView vp = new VectorView(p);
		
		for (int i=0;i<d;i++)
		{
			vr.set(i,0);
			for(int j=0;j<d;j++)
				vr.set(i,vr.get(i) + pMat.get(i, j) *vx.get(i));

			vr.set(i, pB.get(i) - vr.get(i));// r[i]=bb[i]-r[i];
			vp.set(i, vr.get(i)); //p[i]=r[i];
		}

		int iter=0;
		double delta=1;

		while(delta>1e-6)
		{
			iter++;

			double p_mal_Ap_k=0;
			
			Vector Ap_k = new Vector(d);
			VectorView vAp_k = new VectorView(Ap_k);
			
			for (int i=0;i<d;i++)
			{
				double c=0;
				for (int j=0;j<d;j++)
		          c+=pMat.get(i, j)*vp.get(j);
		        vAp_k.set(i, c);
		
		        p_mal_Ap_k+=vp.get(i)*vAp_k.get(i);
		    }
	
			double r_mal_r=0;
			for(int i=0;i<d;i++)
				r_mal_r+=vr.get(i)*vr.get(i);
	
			a_k= (r_mal_r)/( p_mal_Ap_k );
	
			double r_mal_r_neu=0;
	
			
			for (int i=0;i<d;i++)
			{
				vr.set(i, vr.get(i) - a_k * vAp_k.get(i));
				r_mal_r_neu+=vr.get(i)*vr.get(i);
			}
			b_k = (r_mal_r_neu ) / ( r_mal_r );
	
			for (int i=0;i<d;i++)
				vx.set(i, vx.get(i) + a_k * vp.get(i));
	
			for (int i=0;i<d;i++)
				vp.set(i,vr.get(i) + b_k * vp.get(i));
	
			delta=0;
			for(int i=0;i<d;i++)
				delta+=Math.abs(vr.get(i));
	
	    }
	
		result = new Vector(d);
		VectorView resView = new VectorView(result);
		for(int i=0;i<d;i++)
	    {
	
			if (Double.isNaN(vx.get(i)))
			  vx.set(i,0);
			resView.set(i,vx.get(i)); 
	    }
	}
	
	@Override
	protected boolean willSolveChangeTheMatrix() {
		// TODO Auto-generated method stub
		return false;
	}
}
public class EqSolvers extends EquationSolver{
	
	NormalView mat;	
	VectorView b;
	
	EquationSolver aktSolver = null;
	
	/*
	class ConjugateGradientAlgorithm extends EquationSolver{
		public ConjugateGradientAlgorithm(int dimension) {
			super(dimension);
		}	
		/* (non-Javadoc)
		 * @see math.EquationSolver#solve()
		 *//*
		@Override
		void solve(NormalView pMat , VectorView pB) throws Exception{
			// TODO Auto-generated method stub
			
		}
	}*/	
	
	public EqSolvers(int dimension) {
		super(dimension);
		mat = new NormalView(A);
		b = new VectorView(B);
		try{
			aktSolver = new ConjugrateGradientSolver(A,B); // GaussianElimination
		} catch(Exception e){throw new Error("Unsupported Error in EqSolvers(int)");}
	}
	
	public EqSolvers(Matrix m,Vector b) throws Exception {
		super(m,b);
		mat = new NormalView(A);
		this.b = new VectorView(B);
		try{
			aktSolver = new ConjugrateGradientSolver(A,B); // ConjugrateGradientSolver
		} catch(Exception e){throw new Error("Unsupported Error in EqSolvers(int)");}
	}
	
	public void setA(int r , int c, double v){
		try{
			mat.set(r, c, v);
		} catch(Exception e){
			throw new Error(e);
		}
	}
	
	public void setB(int k , double v){
		try{
			b.set(k, v);
		} catch(Exception e){
			throw new Error(e);
		}
	}
	
	
	@Override
	void solve(NormalView pMat, VectorView pB) throws Exception {
		// TODO Auto-generated method stub
		aktSolver.solve(pMat,pB);
		result = aktSolver.result;
	}
	
	@Override
	protected boolean willSolveChangeTheMatrix() {
		// TODO Auto-generated method stub
		return aktSolver.willSolveChangeTheMatrix();
	}
	
	public static void main(String args[] ){
		System.out.println("Testumgebung EqSolvers");
		int d = 2;
		EqSolvers solver = new EqSolvers(d);
		solver.setA(0, 0, 3);  solver.setA(0, 1, 4);  solver.setB(0, 2);
		solver.setA(1, 0, -2); solver.setA(1, 1, 1);  solver.setB(1, 1);
		
		VectorView resView = null;
		try{
			Vector result = solver.solve();
			resView = new VectorView(result);
		} catch (Exception e) {throw new Error(e);}
		for (int i = 0; i < d; i++){
			try { 
			System.out.println("res[" + i +"]: " + resView.get(i));
			} catch(Exception e){throw new Error(e);}
		}
	}
	
	
}
