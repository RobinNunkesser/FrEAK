/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.operator.mutation.common;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.ScheduleInterface;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.Mutation;
import freak.core.graph.OperatorGraph;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.module.fitness.pointset.AbstractRobustRegressionFitness;
import freak.module.fitness.pointset.util.ResidualContainer;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.PointSet;

import java.util.Arrays;
import java.util.Comparator;


class vglSignedResiduals implements Comparator<ResidualContainer> {
	public int compare(ResidualContainer arg0, ResidualContainer arg1) {
		// TODO Auto-generated method stub
		if (arg0.signedResidual < arg1.signedResidual) return -1;
		if (arg0.signedResidual > arg1.signedResidual) return 1;
		return 0;
		
	}
}

public class LTSMutation extends Mutation implements CompatibleWithDifferentSearchSpaces,Configurable{

	AbstractRobustRegressionFitness m_kFitnessFunktion;
	double percentMovement = 2; // richtiger Paremeter zum Einstellen
	
	public LTSMutation(OperatorGraph graph){
		super(graph);
	}
	
		
	@Override
	protected Individual doMutation(Individual ind) {
		// TODO Auto-generated method stub
		RandomElement re = graph.getSchedule().getRandomElement();
		// get both positions
		
		int dimension = ((BitString)graph.getSchedule().getGenotypeSearchSpace()).getDimension();
		int pointDimension = ((PointSet)graph.getSchedule().getPhenotypeSearchSpace()).getPointDimension();
		BitStringGenotype bs = (BitStringGenotype) ((BitStringGenotype)ind.getGenotype()).clone();
		
		
		
		ResidualContainer residuals [] = m_kFitnessFunktion.m_hResiduals;
		
		if (residuals == null){
			for (int i = 0; i < bs.size(); i++){
				bs.set(i, false);
			}
			for (int j = 0; j < pointDimension; j++){
				int r = re.choose(0, dimension - 1);
				while (bs.get(r)){
					r = re.choose(0,dimension -1);
				}
				bs.set(r, true);
			}
			return new Individual(graph.getSchedule(), bs, new Individual[] {ind});
		}
			
		int r = re.choose(0, dimension - 1);
		while (bs.get(r)){
			r = re.choose(0,dimension -1);
		} // r enth�lt den index eines punktes der noch nicht angew�hlt ist
		if (re.uniform(1.0, 100.0) <= percentMovement){
			// movement-operator
            // take d points with similar residual
			// from start index r backwards in the sorted order
            Arrays.sort(residuals, new vglSignedResiduals());
            
            if (r < pointDimension)
            	r = pointDimension;
            
            for (int i = 0; i < dimension; i++){
            	bs.set(i, false );
            }
            for (int i = 0; i < pointDimension; i++){
            	bs.set(residuals[r-i].pointIndexInPointSet,true);
            }
            
		} else {
			int s = re.choose(0,dimension-1);
			while(!bs.get(s)){ 
				s = re.choose(0,dimension-1);
			} // s enth�lt den index eines punktes der in der auswahl enthalten ist
			bs.set(s, false); // swap
			bs.set(r, true);  // swap 
		}
		return new Individual(graph.getSchedule(), bs, new Individual[] {ind});
		
	}

	@Override
	public void testSchedule(ScheduleInterface schedule) throws UnsupportedEnvironmentException {
		// TODO Auto-generated method stub
		super.testSchedule(schedule);
		if (!((schedule.getRealFitnessFunction() instanceof AbstractRobustRegressionFitness) 
				&& (schedule.getGenotypeSearchSpace() instanceof BitString))) {
			throw new UnsupportedEnvironmentException();			
		}
		m_kFitnessFunktion = (AbstractRobustRegressionFitness) schedule.getRealFitnessFunction();
	}
	
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Mutation designed for LtSOptimization";
	}

	public void setPropertyPercentMovement(Double movement){
		percentMovement = movement.doubleValue();
	}
	
	public Double getPropertyPercentMovement(){
		return new Double(percentMovement);
	} 
	
	public String getName() {
		// TODO Auto-generated method stub
		return "LTSMutation";
	}

}
