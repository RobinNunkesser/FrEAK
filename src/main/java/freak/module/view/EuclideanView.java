/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.view;

import freak.core.control.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.core.view.*;
import freak.core.view.swingsupport.*;
import freak.module.searchspace.*;
import java.awt.*;
import javax.swing.*;

/**
 * @author Michael
 */
public class EuclideanView extends SingleIndividualView implements Configurable, FreakSwingModel {
	
	private boolean displayNumbers = true;
	private Color numberColor = Color.blue;
	private Color pointColor = Color.red;
	private Color lineColor = Color.darkGray;
	
	public EuclideanView(Schedule schedule) {
		super(schedule);
		
		setDisplayStrategy(RANDOM);
	}
	
	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getRealFitnessFunction() instanceof freak.module.fitness.cycle.tsp.TSP)) {
			throw new UnsupportedEnvironmentException("Unsupported fitness function.");
		}
	}
	
	public JPanel createIndividualPanel() {
		return new E2DPanel(this);
	}
	
	public String getName() {
		return "Euclidean TSP View";
	}
	
	public String getDescription() {
		return "Graphical view of an euclidean TSP.";
	}
	
	public Class[] getInputDataTypes() {
		return new Class[] { IndividualList.class };
	}
	
	/**
	 * @author  nunkesser
	 */
	class E2DPanel extends JPanel {
		private Insets insets;
		private int currentWidth;
		private int currentHeight;
		private double vertical;
		private double horizontal;
		private Graphics graphics;
		private EuclideanView eview;
		
		private int[] cycle;
		private double[][] points;
		private double[] borderRectangle;
		
		public E2DPanel(EuclideanView eview) {
			super();
			this.eview = eview;
			setBackground(Color.white);
			setBorder(BorderFactory.createEmptyBorder(25, 15, 15, 15));
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			graphics = g;
			
			insets = getInsets();
			currentWidth = getWidth() - insets.left - insets.right;
			currentHeight = getHeight() - insets.top - insets.bottom;
			
			synchronized (eview) {
				freak.module.fitness.cycle.tsp.TSP etsp = (freak.module.fitness.cycle.tsp.TSP)eview.getSchedule().getRealFitnessFunction();
				points = etsp.getPoints();
				
				if (points != null) {
					borderRectangle = etsp.getBorderRectangle();
					
					vertical = currentWidth / (borderRectangle[2] - borderRectangle[0]);
					horizontal = currentHeight / (borderRectangle[3] - borderRectangle[1]);
					
					graphics.setColor(eview.lineColor);
					// draw individual
					
					Individual individual = getIndividualToDisplay();
					if (individual != null) {
						Genotype geno = individual.getGenotype();
						
						char[] fitness = (etsp.getTSPName() + ": " + Double.toString(0 - etsp.evaluate(geno))).toCharArray();
						
						graphics.drawChars(fitness, 0, fitness.length, insets.left + 10, insets.top - 10);
						cycle = ((PermutationGenotype)geno).getIntArray();
						for (int i = 0; i < points.length; i++) {
							drawLine(points[cycle[i] - 1][0], points[cycle[i] - 1][1], points[cycle[(i + 1) % cycle.length] - 1][0], points[cycle[(i + 1) % cycle.length] - 1][1]);
						}
					}
					
					for (int i = 0; i < points.length; i++) {
						drawPoint(points[i][0], points[i][1], (new Integer(i + 1)).toString().toCharArray());
					}
				}
			}
		}
		
		private void drawLine(double x1, double y1, double x2, double y2) {
			graphics.drawLine(insets.left + (int) ((x1 - borderRectangle[0]) * vertical), insets.top + (int) ((y1 - borderRectangle[1]) * horizontal), insets.left + (int) ((x2 - borderRectangle[0]) * vertical), insets.top + (int) ((y2 - borderRectangle[1]) * horizontal));
		}
		
		private void drawPoint(double x, double y, char[] c) {
			x -= borderRectangle[0];
			y -= borderRectangle[1];
			graphics.setColor(eview.pointColor);
			graphics.fillOval(insets.left + (int) (x * vertical) - 1, insets.top + (int) (y * horizontal) - 1, 2, 2);
			if (eview.displayNumbers) {
				graphics.setColor(eview.numberColor);
				graphics.drawChars(c, 0, c.length, insets.left + (int) (x * vertical) + 2, insets.top + (int) (y * horizontal) + 10);
			}
		}
	}
	
	public void setPropertyDisplayNumbers(Boolean dn) {
		displayNumbers = dn.booleanValue();
		UpdateManager.markDirty(this);
	}
	
	public Boolean getPropertyDisplayNumbers() {
		return new Boolean(displayNumbers);
	}
	
	public String getShortDescriptionForDisplayNumbers() {
		return "Display numbers";
	}
	
	public String getLongDescriptionForDisplayNumbers() {
		return "Displays the permutation number for every city.";
	}
	
	public void setPropertyNumberColor(Color color) {
		if (color != null) {
			numberColor = color;
			UpdateManager.markDirty(this);
		}
	}
	
	public Color getPropertyNumberColor() {
		return numberColor;
	}
	
	public String getShortDescriptionForNumberColor() {
		return "Color of the numbers";
	}
	
	public String getLongDescriptionForNumberColor() {
		return "Sets the color for the displayed numbers.";
	}
	
	public void setPropertyPointColor(Color color) {
		if (color != null) {
			pointColor = color;
			UpdateManager.markDirty(this);
		}
	}
	
	public Color getPropertyPointColor() {
		return pointColor;
	}
	
	public String getShortDescriptionForPointColor() {
		return "Color of the points";
	}
	
	public String getLongDescriptionForPointColor() {
		return "Sets the color for the displayed city points.";
	}
	
	public void setPropertyLineColor(Color color) {
		if (color != null) {
			lineColor = color;
			UpdateManager.markDirty(this);
		}
	}
	
	public Color getPropertyLineColor() {
		return lineColor;
	}
	
	public String getShortDescriptionForLineColor() {
		return "Color of the lines";
	}
	
	public String getLongDescriptionForLineColor() {
		return "Sets the color for the displayed lines between the cities.";
	}
}
