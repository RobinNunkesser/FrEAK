/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.mapper.pointset;

import freak.core.control.Schedule;
import freak.core.mapper.AbstractMapper;
import freak.core.population.Genotype;
import freak.core.searchspace.SearchSpace;
import freak.module.searchspace.BitString;
import freak.module.searchspace.BitStringGenotype;
import freak.module.searchspace.PointSet;
import freak.module.searchspace.PointSetGenotype;

/**
 * @author ruthe
 *
 */
public class BitStringMapper extends AbstractMapper {
	
	private BitString genotypeSearchSpace;
	
	public BitStringMapper(Schedule schedule) {
		super(schedule);
	}
	
	/**** TTTTTTTTTOOOOOOOOOODDDDDDDDOOOOOOOOOOOO   Initialize programmieren ***/
	public void initialize(){
		super.initialize();
		PointSet space = (PointSet)schedule.getPhenotypeSearchSpace();
		try{
			int dimension = space.getDimension();
			genotypeSearchSpace = new BitString(schedule,dimension);
		} catch(IllegalStateException exc){
			genotypeSearchSpace = new BitString(schedule);
		}
	}
	
	/* (non-Javadoc)
	 * @see freak.core.mapper.Mapper#genotypeToPhenotype(freak.core.population.Genotype)
	 */
	public Genotype genotypeToPhenotype(Genotype genotype) {
		// TODO Auto-generated method stub
		PointSet.Point [] ps =((PointSet)schedule.getPhenotypeSearchSpace()).getPoints();
	//	int h = ((PointSet)schedule.getPhenotypeSearchSpace()).getH();
		boolean selection [] = ((BitStringGenotype) genotype).getBooleanArray();
		
		PointSetGenotype psg = new PointSetGenotype(ps,selection);//,h);
		return psg;
	}

	public Genotype phenotypeToGenotype(Genotype phenotype) throws UnsupportedOperationException{
		if (phenotype == null) return null;
		
		PointSetGenotype psg = ((PointSetGenotype) phenotype);
		
		BitStringGenotype bsg = new BitStringGenotype(psg.getSubSet());
		return bsg;
	}
	
	/* (non-Javadoc)
	 * @see freak.core.mapper.Mapper#getGenotypeSearchSpace()
	 */
	public SearchSpace getGenotypeSearchSpace() {
		// TODO Auto-generated method stub
		return genotypeSearchSpace;
	}

	/* (non-Javadoc)
	 * @see freak.core.modulesupport.Module#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Maps Phenotype Point Set to Genotype Bit String.";
	}

	/* (non-Javadoc)
	 * @see freak.core.modulesupport.Module#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return "Point Set to Bit String";
	}

}
