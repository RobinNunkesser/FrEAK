/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.model;

import java.util.BitSet;


/**
 * @author  nunkesser
 */
public class IndividualSummary implements Comparable {

private int birth;
private int length;	
private double mcr;
private double geccoFit1;
private double geccoFit2;
private BitSet valueBitset;
private BitSet resultBitset;
private int rows;

public IndividualSummary(int birth, int length, double mcr, BitSet valueBitset, BitSet resultBitset, int rows) {
	super();
	this.birth = birth;
	this.length = length;
	this.mcr = mcr;
	this.valueBitset = valueBitset;
	this.resultBitset = resultBitset;
	this.rows = rows;
	this.geccoFit1=0.0;
	this.geccoFit2=0.0;
}
/**
 * @param birth
 * @param length
 * @param mcr
 * @param valueBitset
 * @param resultBitset
 * @param rows
 */
public IndividualSummary(int birth, int length, double mcr, BitSet valueBitset, BitSet resultBitset, int rows,double geccoFit1,double geccoFit2) {
	super();
	this.birth = birth;
	this.length = length;
	this.mcr = mcr;
	this.valueBitset = valueBitset;
	this.resultBitset = resultBitset;
	this.rows = rows;
	this.geccoFit1=geccoFit1;
	this.geccoFit2=geccoFit2;
}

public int compareTo(Object o) {
	try {
		if (this.getLength()==((IndividualSummary)o).getLength()) {
			if (this.getBirth()<((IndividualSummary)o).getBirth()) return -1;
			if (this.getBirth()>((IndividualSummary)o).getBirth()) return 1;			
			return 0;
		}
		if (this.getLength()<((IndividualSummary)o).getLength()) return -1;
		if (this.getLength()>((IndividualSummary)o).getLength()) return 1;
	} catch (Exception e) {
	}
	return -1;
}

public boolean equals(Object o) {
	try {
		if ((this.getBirth()==((IndividualSummary)o).getBirth()) && (this.getLength()==((IndividualSummary)o).getLength())) return true;
	} catch (Exception e) {
	}
	return false;	
}

public int getLength() {
	return length;
}
public void setLength(int length) {
	this.length = length;
}
public int getBirth() {
	return birth;
}
public void setBirth(int birth) {
	this.birth = birth;
}
public double getMcr() {
	return mcr;
}
public void setMcr(double mcr) {
	this.mcr = mcr;
}
/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Override
public String toString() {
	// TODO Auto-generated method stub
	return "("+this.getBirth()+","+this.getLength()+","+this.getMcr()+")";
}
/**
 * @return the resultBitset
 */
public BitSet getResultBitset() {
	return resultBitset;
}
/**
 * @return the valueBitset
 */
public BitSet getValueBitset() {
	return valueBitset;
}

/**
 * @return the rows
 */
public int getRows() {
	return rows;
}

/**
 * @return the geccoFit1
 */
public double getGeccoFit1() {
	return geccoFit1;
}

/**
 * @param geccoFit1 the geccoFit1 to set
 */
public void setGeccoFit1(double geccoFit1) {
	this.geccoFit1 = geccoFit1;
}

/**
 * @return the geccoFit2
 */
public double getGeccoFit2() {
	return geccoFit2;
}

/**
 * @param geccoFit2 the geccoFit2 to set
 */
public void setGeccoFit2(double geccoFit2) {
	this.geccoFit2 = geccoFit2;
}	

}
