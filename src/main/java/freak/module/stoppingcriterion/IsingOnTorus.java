/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.stoppingcriterion;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import freak.core.control.Schedule;
import freak.core.event.GenerationEvent;
import freak.core.modulesupport.UnsupportedEnvironmentException;
import freak.core.population.Individual;
import freak.core.population.IndividualList;
import freak.core.stoppingcriterion.AbstractGenerationStoppingCriterion;
import freak.module.fitness.generalstring.IsingModelTorus;
import freak.module.searchspace.GeneralStringGenotype;

/**
 * This stopping criterion is checked periodically each time after a specified
 * number of generations have passed. If the population contains an optimal
 * individual at that time the algorithm is stopped.  
 * 
 * @author Christian
 */
public class IsingOnTorus extends AbstractGenerationStoppingCriterion {

	private int[] gene; 
	private int w;
	private int h;
	private int opt;
	private boolean horiz = false;
	private boolean vertical = false;
	private boolean diag1 = false;
	private int nrCut;
	private int cut[][];
	private int ringSize[];
	private boolean eightNeighbours;
	private String fileName;
	
	/**
	 * @param schedule a reference to the schedule.
	 */
	public IsingOnTorus(Schedule schedule) {
		super(schedule);
	}
	
	public void initialize() {
		super.initialize();
		try {
			opt = (int) Math.round(((IsingModelTorus) schedule.getFitnessFunction()).getOptimalFitnessValue());
		} catch(Exception e) {}
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getFitnessFunction() instanceof IsingModelTorus)) {
			throw new UnsupportedEnvironmentException("This module works on Ising Model Torus only.");
		}
	}

	public String getName() {
		return "Ising on Torus";
	}

	public String getDescription() {
		return "Stops a run if no islands or unstable rings are left.";
	}

	/**
	 * This method is called when a new generation is completely created.
	 */
	public void checkCriterion(GenerationEvent evt) {
		if (fileName == null) { // first call?
			fileName = "run.lst";
			File f = new File(fileName);
			try {
				f.delete(); 
				f.createNewFile();
			} catch(IOException e) {System.out.println("Can´t delete or create output file");}
		}
	
		if (getSchedule().getFitnessFunction() instanceof IsingModelTorus) { 
			IsingModelTorus ff = (IsingModelTorus)getSchedule().getFitnessFunction();
			int height = ff.getPropertyTorusHeight().intValue();
			int width = ff.getPropertyTorusWidth().intValue();
			boolean edge = ff.getPropertyDiagonalEdges().booleanValue();
			if (height != h || width != w || edge != eightNeighbours) {
				String edges = ff.getPropertyDiagonalEdges().booleanValue()?" with":" without";
				write("Size of Torus : "+width+"x"+height+edges+ " diagonal edges");
			}
		}

		IndividualList p = getSchedule().getPopulationManager().getPopulation();
		Individual ind = p.getIndividual(0);
		gene = ((GeneralStringGenotype) ind.getGenotype()).getIntArray();
		
		// optimum reached?
		IsingModelTorus ff = (IsingModelTorus)(getSchedule().getFitnessFunction());
		w = ff.getPropertyTorusWidth().intValue();
		h = ff.getPropertyTorusHeight().intValue();
		eightNeighbours = ff.getPropertyDiagonalEdges().booleanValue();
		
		double fitness = ff.evaluate(ind,p);
		if(fitness == opt) {
			String output = "Run \t"+getSchedule().getCurrentRun() + "\t stopped" +
							" in generation \t"+getSchedule().getCurrentGeneration();
			System.out.println(output);
			write(output);
			
			stopRun();
			return;
		}

		// (possibly-)stable state reached?
		boolean onlystableRings = onlystableRings();
		if (onlystableRings) {
			String output = "Run \t"+getSchedule().getCurrentRun()+"\t stopped " + 
							"in generation \t"+ getSchedule().getCurrentGeneration() +
							"\t : ";
			if (diag1) output += "diagonal \t(";
				else output += (horiz?"horizontal":"vertical")+" \t(";
				
			for (int i=0;i<ringSize.length;i++) {
				output += ringSize[i];
				if (i+1 < ringSize.length) output += ",";
			}
			output += ")";
			System.out.println(output);
			write(output);

			stopRun();
		} 
	}
	
	/**
	 * This method checks if only stable rings are given. 
	 * If so it return true, else false
	 * 
	 */
	private boolean onlystableRings() {	
		for (int r=0;r<h;r++) { 	// for each row ...
			for (int c=0;c<w;c++) { // for each column ...
				boolean tempHoriz = false;
				boolean tempVertical = false;
				boolean tempDiag1 = false;
				boolean tempDiag2 = false;

				// rings?
				tempHoriz = gene[map(r,c)] == gene[map(r,c-1)] && gene[map(r,c)]== gene[map(r,c+1)];
				tempVertical = gene[map(r,c)]== gene[map(r-1,c)] && gene[map(r,c)] == gene[map(r+1,c)];
				if (eightNeighbours) {
					tempDiag1 = gene[map(r,c)] == gene[map(r+1,c-1)] && gene[map(r,c)] == gene[map(r-1,c+1)];
					tempDiag2 = gene[map(r,c)] == gene[map(r-1,c-1)] && gene[map(r,c)] == gene[map(r+1,c+1)];
				}				
				if (!(tempHoriz || tempVertical || tempDiag1 || tempDiag2)) return false;
			}
		}
		
		if (eightNeighbours){
			boolean vert1, horiz2;
			
			// check horizontal
			vert1 = true; //horiz1 = false;
			for (int r=0;r<h;r++) 
				if (gene[map(r,0)] != gene[map(r+1,0)]) { 
					vert1 = false; //horiz1 = true;
				}
			// check vertical
			horiz2 = true; //vert2 = false; 
			for (int c=0;c<w;c++) 
				if (gene[map(0,c)] != gene[map(0,c+1)]) { 
					horiz2 = false; //vert2 = true;
			}
			
			if (vert1 == false && horiz2 == false) { // diagonal ring found
				diag1 = true; vertical = horiz = false;
			}
			
			if (vert1 == true && horiz2 == false) { // vertical ring found
				diag1 = horiz = false; vertical = true;
			}
			if (vert1 == false && horiz2 == true) { // horizontal ring found
				diag1 = vertical = false; horiz = true;
			}
		}
		else {
			// horizontal or vertical ring?
			vertical = true; horiz = false;
			for (int r=0;r<h;r++) 
				if (gene[map(r,0)] != gene[map(r+1,0)]) { 
					vertical = false; horiz = true;
				} 
		}
		
		// Ok, only stable rings were found, but how big are they?
		if (horiz) findVerticalCuts();
		if (vertical) findHorizontalCuts();
		if (diag1) {
			if (w <= h) findHorizontalCuts();
				else findVerticalCuts();
		}

		calculateRingSizes();
		
		return true;
	}
	
	/**
	 *  This method searchs for vertical borders between colors within the first
	 *  column.
	 */
	private void findVerticalCuts() {
		
		cut = new int[h][];
		nrCut = 0;
		
		cut[0] = new int[2];
		for (int r=0;r<h;r++) { // traverse the rows
			if (gene[map(r,0)] != gene[map(r+1,0)]) { // cut found
					cut[nrCut][1] = r; // close last cut
					nrCut++;
					cut[nrCut] = new int[2]; // create new cut
					cut[nrCut][0] = r+1;	// save starting point of new cut
				}
			}
		// correct the "overflow"
		if (gene[map(h-1,0)] == gene[map(0,0)]) { 	// overflow
			cut[0][0] = cut[nrCut][0];
		} else {										// no overflow
			cut[0][0] = 0;
		}
		
		//for (int r=0;r<nrCut;r++)
		//	System.out.println("Cut "+r+":"+cut[r][0]+"bis"+cut[r][1]);
	}
	
	/**
	 *  This method searchs for horizontal borders between colors within the first
	 *  row.
	 */
	private void findHorizontalCuts() {
		nrCut = 0;
		
		cut[0] = new int[2];
		for (int c=0;c<w;c++) { // traverse the rows
			if (gene[map(0,c)] != gene[map(0,c+1)]) { // cut found
					cut[nrCut][1] = c; // close last cut
					nrCut++;
					cut[nrCut] = new int[2]; // create new cut
					cut[nrCut][0] = c+1;	// save starting point of new cut
				}
			}
		// correct the "overflow"
		if (gene[map(0,w-1)] == gene[map(0,0)]) { 	// overflow
			cut[0][0] = cut[nrCut][0];
		} else {										// no overflow
			cut[0][0] = 0;
		}
		
		//for (int c=0;c<nrCut;c++)
		//	System.out.println("Cut "+c+":"+cut[c][0]+"bis"+cut[c][1]);
	}
	
	/**
	 * This method calculates the sizes of the rings. The rings "are chosen" to
	 * minimize the sum of the rings.
	 *
	 */
	private void calculateRingSizes() {
		int sum1 = 0,sum2 = 0;
		for (int i=0;i<nrCut/2;i++)
			sum1 += dist(cut[2*i][0],cut[2*i][1]);
		
		for (int i=0;i<nrCut/2;i++)
			sum2 += dist(cut[2*i+1][0],cut[2*i+1][1]);
			
//		System.out.println(sum1+" vs "+sum2);
			
		ringSize = new int[nrCut/2];
		if (sum1 <= sum2) { // even cuts are rings
			for (int i=0;i<nrCut/2;i++) 
				ringSize[i] = dist(cut[2*i][0],cut[2*i][1]);
		}
		else {				// odd cuts are rings
			for (int i=0;i<nrCut/2;i++) 
				ringSize[i] = dist(cut[2*i+1][0],cut[2*i+1][1]);
		}
	}
	
	/**
	 * This method calculates the distance between the starting point of the
	 * ring an its ending point.
	 * @param i Starting point of the ring
	 * @param j Ending point of the ring
	 * @return size of the ring
	 */
	private int dist(int i,int j) {
		if (horiz) {
			if (i <= j) return j-i+1;
				else return h-i+j+1;		
		} else {
			if (i <= j) return j-i+1;
				else return w-i+j+1;
		}
	}

/*	private int distVert(int i,int j) {
		if (i <= j) return j-i+1;
			else return h-i+j+1;
	}*/
	
	private void write(String s) {
		try {
			File f = new File(fileName);
			RandomAccessFile raf = new RandomAccessFile(f, "rw");

			raf.seek(raf.length());
			raf.writeBytes(s+"\r");
			raf.close();
		} catch (IOException e) {System.out.println("Writing failed");}
	}
	
	/**
	 * This function maps the 2-dimensional values of the torus onto the linear
	 * coordinates of the BitSet or GeneralString, respectivly.
	 * @param i Row of the coordinate
	 * @param j Column of the coordinate
	 * @return position inside the BitSet.
	 */
	private int map(int i,int j) {
		i %= h; j%= w;
		if (i<0) i+=h;
		if (j<0) j+=w;
		
		return i*w+j;
	}
}
