/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.searchspace;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.core.control.Schedule;
import freak.core.population.Genotype;
import freak.module.searchspace.PointSet.Point;

/**
 * @author ruthe
 *
 */
public class PointSetGenotype extends Genotype {


	PointSet.Point [] m_kPoints;
	
	boolean [] m_kGivenSubSet;
	
//	int h;
	
	
	/*
	private boolean checkSetsEqual(ArrayList<PointSet.Point> s1, ArrayList<PointSet.Point> s2){
		if (s1.size() != s2.size()) return false;
		for (Iterator i = s1.iterator(); i.hasNext(); ){
			
			if (!s2.contains(i.next()))
		}
	}*/
	
	public PointSetGenotype(PointSet.Point [] pPoints,boolean [] pGivenSubSet) {//, int h) {
		// TODO Auto-generated constructor stub
		this.m_kPoints = pPoints;
		this.m_kGivenSubSet = pGivenSubSet;
//		this.h = h;
	}
	
	public void setSubSet( boolean [] selection ){
		m_kGivenSubSet = selection;
	}
	
//	public int getH(){return h;}
	
	public PointSet.Point [] getPoints(){
		return m_kPoints;
	}
	
	public int getPointDimension(){
		return (this.m_kPoints.length != 0)?(this.m_kPoints[0].getDimension()):(-1);
	}
	
	public boolean [] getSubSet(){
		return m_kGivenSubSet.clone();
	}
	
	public PointSet.Point [] getChoosenSubSet(){
		int count = 0;
		for (int i = 0; i < m_kPoints.length;i++){
			if (m_kGivenSubSet[i])count++; 
		}
		int j = 0;
		PointSet.Point [] result = new PointSet.Point[count];
		for (int i = 0; i < m_kPoints.length;i++){
			if (m_kGivenSubSet[i])result[j++]=m_kPoints[i];
		}
		return result;
	}
	
	
	
	/* (non-Javadoc)
	 * @see freak.core.population.Genotype#equals(java.lang.Object)
	 */
	@Override
	
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if (!(o instanceof PointSetGenotype))
			return false;
		PointSetGenotype psg = (PointSetGenotype) o;
		return (this.m_kPoints.equals(psg.m_kPoints))
				&&(this.m_kGivenSubSet.equals(psg.m_kGivenSubSet));
	}

	/* (non-Javadoc)
	 * @see freak.core.population.Genotype#hashCode()
	 */
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see freak.core.population.Genotype#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String result = "[";
		for (int i = 0; i < m_kPoints.length-1; i++){
			result += m_kPoints[i] + ", ";
		}
		result += m_kPoints[m_kPoints.length-1] + "]\n[";
		for (int i = 0; i < m_kGivenSubSet.length-1; i++){
			if (m_kGivenSubSet[i]){
				result += m_kPoints[i] + ", ";
			}
		}
		if (m_kGivenSubSet[m_kPoints.length-1]){
			result += m_kPoints[m_kPoints.length-1];
		}
		result += "]";
		return result;
	}

	public Object clone(){
		PointSetGenotype copy;
		try{
			copy = (PointSetGenotype)super.clone();
		} catch(CloneNotSupportedException e){
			throw new Error(e.toString());
		}
		copy.m_kPoints = (PointSet.Point[])m_kPoints.clone();
		copy.m_kGivenSubSet = (boolean[])m_kGivenSubSet.clone();
		return (PointSetGenotype) copy;
	}
	
	
}
