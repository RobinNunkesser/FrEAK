/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.fitness.pointset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


import freak.module.searchspace.PointSet;

public class LtSFile {
	PointSet.Point [] pointData;
	int pointDim;
	int numberPoints;
	public LtSFile(File inFile) throws IOException{
		FileReader in = new FileReader(inFile);
		BufferedReader reader = new BufferedReader(in);
		readInData(reader);
	}
	
	public int getPointDimension(){
		return pointDim;
	}
	
	public int getDimension(){
		return numberPoints;
	}
	
	private void readInData(BufferedReader in) throws IOException{
		String line = in.readLine();
		int ExpectedDim = -1;
		ArrayList<PointSet.Point> points = new ArrayList<PointSet.Point>();
		while (line != null){
			StringTokenizer strTokzer = new StringTokenizer(line,",");
			ArrayList<Double> komponents = new ArrayList<Double>();
			while(strTokzer.hasMoreTokens()){
				String token = strTokzer.nextToken();
				komponents.add(new Double(token));
			}
			if ((komponents.size() != ExpectedDim)&&(ExpectedDim != -1)){
				System.out.println("Unsuspected line: " + line);
			} else {
				ExpectedDim = komponents.size();
				points.add(new PointSet.Point(komponents));
			}
			line = in.readLine();
		}
		pointData = new PointSet.Point[points.size()];
		int i = 0;
		for (Iterator<PointSet.Point> it = points.iterator(); it.hasNext(); ){
			pointData[i] = it.next();
			i++;
		}
		numberPoints = pointData.length;
		pointDim = ExpectedDim;
	}

	/**
	 * @return the pointData
	 */
	public PointSet.Point[] getPointData() {
		return pointData;
	} 
}
