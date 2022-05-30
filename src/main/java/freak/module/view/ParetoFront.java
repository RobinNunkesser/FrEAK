/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.module.view;

import freak.core.control.*;
import freak.core.fitness.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.core.view.*;
import freak.core.view.swingsupport.*;
import freak.module.searchspace.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class ParetoFront extends AbstractView implements Configurable, FreakSwingModel {
	private transient JPanel panel;

	private static final float MAXIMAL_HUE = 0.8f;
	private static float SATURATION = 1.0f;
	private static float BRIGHTNESS = 1.0f;

	private static Color DEFAULT_COLOR = Color.red;

	private IndividualList individuals;

	private boolean colorByFitness;
	private boolean displayOptimum;

	private int maximalNumberOfIndividuals;
	int counter = 1;
	
	/**
	 * Constructs a new <code>HyperCube</code> with a link back to the current
	 * schedule.
	 *
	 * @param schedule a link back to the current schedule.
	 */
	public ParetoFront(Schedule schedule) {
		super(schedule);

		displayOptimum = supportsDisplayOptimum();
		colorByFitness = supportsUseColor();
		maximalNumberOfIndividuals = 1000;
	}
	
	public void initialize() {
		super.initialize();
		// test if the options are still supported
		displayOptimum &= supportsDisplayOptimum();
		colorByFitness &= supportsUseColor();
	}

	public void testSchedule(Schedule schedule) throws UnsupportedEnvironmentException {
		super.testSchedule(schedule);
		
		if (!(schedule.getGenotypeSearchSpace() instanceof BooleanFunction)) {
			throw new UnsupportedEnvironmentException("Unsupported searchspace.");
		}
	}

	public JPanel createPanel() {
		panel = new ParetoViewPanel(this);
		return panel;
	}

	private boolean supportsUseColor() {
		if (!(getSchedule().getFitnessFunction() instanceof SingleObjectiveFitnessFunction)) {
			return false;
		}

		SingleObjectiveFitnessFunction fitness = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();
		try {
			// check for exceptions, ignore return
			fitness.getLowerBound();
			fitness.getUpperBound();
			return true;
		} catch (UnsupportedOperationException ex) {
			return false;
		}
	}

	private boolean supportsDisplayOptimum() {
		FitnessFunction fitness = getSchedule().getFitnessFunction();
		try {
			// check for exceptions, ignore return
			fitness.getGenotypeOptimum();
			return true;
		} catch (UnsupportedOperationException ex) {
			return false;
		}

	}

	public void update(Object data) {
		int generation = schedule.getCurrentGeneration();//Nummer der Generation wird ausgelesen
		int wert = 10 * counter; //Jede 10. Generation wird gezeichnet
		if(generation == wert | generation == 1){
			super.update(data);

			synchronized (this) {
				individuals = (IndividualList)data;
			}
			UpdateManager.markDirty(this);
			counter++;
		}
	}

	public void flush() {
		if (panel == null)
			return;

		panel.repaint();
	}

	public String getName() {
		return "Pareto Front";
	}

	public String getDescription() {
		return "Computes the Pareto Front.";
	}

	public Class[] getInputDataTypes() {
		return new Class[] { IndividualList.class };
	}

	/**
	 * If <code>colorByFitness</code> is <code>true</code> and the fitness
	 * function supports upper and lower fitness bounds, then the color for the
	 * individual is computed by its fitness on the color scale from
	 * violet (least fitness) to red (highest fitness).
	 * Otherwise, a default color is returned for all individuals.
	 *
	 * @param individual the individual whose fitness determines the color.
	 * @return the color the individual is to be painted with.
	 */
	public Color getColorByFitness(Individual individual) {
		// the user chose not to use fitness dependent colors
		if (!colorByFitness)
			return DEFAULT_COLOR;

		try {
			SingleObjectiveFitnessFunction fitness = (SingleObjectiveFitnessFunction)getSchedule().getFitnessFunction();

			double lowerFitnessBound = fitness.getLowerBound();
			double upperFitnessBound = fitness.getUpperBound();

			// determine fitness value and normalize it to get a value in [0, 1]
			double fitnessValue = fitness.evaluate(individual, individuals);
			double normalizedFitnessValue = (fitnessValue - lowerFitnessBound) / (upperFitnessBound - lowerFitnessBound);

			/* The color is chosen in HSB format where the fitness determines
			 * the hue in the interval [0, MAXIMAL_HUE].
			 */
			Color result = new Color(Color.HSBtoRGB((float) (1 - normalizedFitnessValue) * MAXIMAL_HUE, SATURATION, BRIGHTNESS));

			return result;

		} catch (UnsupportedOperationException ex) {
			/* Since the fitness bounds are unknown, the color interpolation
			 * is impossible. So the default color is returned.
			 */
			return DEFAULT_COLOR;
		}
	}

	public IndividualList getIndividualList() {
		return individuals;
	}

	/**
	 * Enables the use of fitness dependant colors for the circles representing
	 * individuals if the fitness function supports upper and lower fitness
	 * bounds. If this option is enabled, the color of an individual is
	 * interpolated between the fitness bounds depending on its fitness value.
	 * The resulting colors are based on the color scale from violet
	 * (least fitness) to red (highest fitness).
	 *
	 * @param colorByFitness specifies if fitness dependent coloring shall be used (if possible).
	 */
	public void setPropertyColorByFitness(Boolean colorByFitness) {
		this.colorByFitness = colorByFitness.booleanValue() && supportsUseColor();
	}

	/**
	 * Checks if fitness dependent coloring is enabled.
	 *
	 * @return a Boolean wrapper class with boolean value <code>true</code>, if fitness dependent coloring is enabled; <code>false</code> otherwise.
	 */
	public Boolean getPropertyColorByFitness() {
		return new Boolean(colorByFitness);
	}

	public String getShortDescriptionForColorByFitness() {
		return "Color individuals by fitness";
	}

	public String getLongDescriptionForColorByFitness() {
		return "Colors the displayed individuals by their fitness on a scale from violet (least fitness) to red (highest fitness). " + "This feature can only be enabled if the fitness function knows the optimal fitness value.";
	}

	/**
	 * Enables display of the global optimum, if the fitness function supports
	 * provision of the global optimum.
	 *
	 * @param displayOptimum specifies if the global optimum shall be displayed, if known by the fitness function.
	 */
	public void setPropertyDisplayOptimum(Boolean displayOptimum) {
		this.displayOptimum = displayOptimum.booleanValue() && supportsDisplayOptimum();
	}

	/**
	 * Checks if display of the global optimum is enabled.
	 *
	 * @return a Boolean wrapper class with boolean value <code>true</code>, if display of the global optimum is enabled; <code>false</code> otherwise.
	 */
	public Boolean getPropertyDisplayOptimum() {
		return new Boolean(displayOptimum);
	}

	public String getShortDescriptionForDisplayOptimum() {
		return "Display global optimum";
	}

	public String getLongDescriptionForDisplayOptimum() {
		return "Displays the global optimum in the ParetoView. This option can only be enabled if a global optimum exists and if the fitness function knows the optimum.";
	}

	/**
	 * Sets the maximal number of individuals displayed in the ParetoView.
	 */
	public void setPropertyMaximalNumberOfIndividuals(Integer number) {
		if (number.intValue() > 0)
			this.maximalNumberOfIndividuals = number.intValue();
	}

	/**
	 * Returns the maximal number of individuals displayed in the ParetoView.
	 */
	public Integer getPropertyMaximalNumberOfIndividuals() {
		return new Integer(maximalNumberOfIndividuals);
	}

	public String getShortDescriptionForMaximalNumberOfIndividuals() {
		return "Maximal number of individuals";
	}

	public String getLongDescriptionForMaximalNumberOfIndividuals() {
		return "Sets the maximal number of individuals displayed in the ParetoView. " +
			"If more individuals are to be displayed, only the first ones are shown.";
	}

	/**
	 * The <code>JPanel</code> belonging to <code>ParetoView</code>.
	 * @author  Dirk
	 */
	class ParetoViewPanel extends JPanel {

		// scaling factor in x direction;
		public static final double X_SCALE = 0.8;
		public static final int MAX_POINT_SIZE = 8;

		private Insets insets;
		private int currentWidth;
		private int currentHeight;
		private Graphics graphics;
		private ParetoFront ParetoView;

		public ParetoViewPanel(ParetoFront ParetoView) {
			super();
			this.ParetoView = ParetoView;
			setBackground(Color.white);
			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			graphics = g;

			insets = getInsets();
			currentWidth = getWidth() - insets.left - insets.right;
			currentHeight = getHeight() - insets.top - insets.bottom;

			g.setColor(Color.black);
			drawSearchSpace();

			IndividualList individuals;

			synchronized (ParetoView) {
				individuals = ParetoView.getIndividualList();
				// draw individuals
				if (individuals != null) {
					int maxSize = 0;
					int minSize =0;
					int minFitness = 0;
					int maxFitness = 0;
						
					int size;
					int fitness;
					for(int i = 0; i< individuals.size(); i++){
						Individual individual = (Individual)individuals.getIndividual(i);
						size =  ((BooleanFunctionGenotype)individual.getGenotype()).evaluateSize();
						fitness = ((BooleanFunctionGenotype)individual.getGenotype()).evaluate();
						if(i==0){
							minSize = size;
							minFitness = fitness;
						}
						if(size> maxSize){
							maxSize = size;
						}
						if(size < minSize){
							minSize = size;
						}
						if(fitness > maxFitness){
							maxFitness = fitness;
						}
						if(fitness < minFitness){
							minFitness = fitness;
						}
					}
					for(int i = 0; i< individuals.size(); i++){
						Individual individual = (Individual)individuals.getIndividual(i);
						g.setColor(ParetoView.getColorByFitness(individual));
						drawIndividual(maxFitness, minFitness, 
								maxSize, minSize,
								((BooleanFunctionGenotype)individual.getGenotype()).evaluate(), 
								((BooleanFunctionGenotype)individual.getGenotype()).evaluateSize());
					}
				}

				// try to draw global optimum, if this option is enabled
				if (ParetoView.displayOptimum) {
					try {
						Genotype optimum = ParetoView.getSchedule().getFitnessFunction().getGenotypeOptimum();
						//drawOptimum(((BitStringGenotype)optimum).getBooleanArray());
					} catch (UnsupportedOperationException ex) {
						// nevermind.
					}
				}
			}
		}

		private void drawSearchSpace() {
			graphics.setColor(Color.black);
			//Koordinatenachsen zeichnen
			//y-Achse (Größe)
			for (int y = 0; y <= currentHeight; y++) {
				drawPixel(insets.left, y + insets.top);
			}
			
			//x-Achse (Fitness)
			for(int x = 0; x<=currentWidth; x++){
				drawPixel(x+insets.left, currentHeight+insets.top);
			}
			
			graphics.drawLine(currentWidth+insets.left-5,currentHeight+insets.top-5,currentWidth+insets.left,currentHeight+insets.top);
			graphics.drawLine(currentWidth+insets.left-5,currentHeight+insets.top+5,currentWidth+insets.left,currentHeight+insets.top);
			graphics.drawString("Fitness",currentWidth-30, currentHeight);
			
			graphics.drawLine(insets.left-5, insets.top+5, insets.left, insets.top);
			graphics.drawLine(insets.left+5, insets.top+5, insets.left, insets.top);
			graphics.drawString("Größe", insets.left+10, insets.top+10);

		}
		
		private void drawXAchsenBeschriftung(int minFitness, int maxFitness){
			graphics.setColor(Color.black);
			int diff = maxFitness - minFitness;
			
			//X-Achsen-Beschriftung
			for(float i = 0; i<4; i++){
				graphics.drawLine((int)((float)currentWidth * ((i+1)/4.0)), 
						currentHeight+5, 
						(int)((float)currentWidth * ((i+1)/4.0)), 
						currentHeight+10);
				
				if(i==0){
					graphics.drawString(Integer.toString((int)(maxFitness-diff)),
						(int)((float)currentWidth*((i+1)/4.0)-10), currentHeight+20);
				}else{
					graphics.drawString(Integer.toString((int)(maxFitness-diff+(float)diff*(i/3.0))),
							(int)((float)currentWidth*((i+1)/4.0)-10), currentHeight+20);
				}
			}		
		}
		
		private void drawYAchsenBeschriftung(int minSize, int maxSize){
			graphics.setColor(Color.black);
			//y-Achsen-Beschriftung
			int diff = maxSize-minSize+1;
			
			for(float i = 1; i<=diff; i++){
			    graphics.drawLine(insets.left-5, 
			    		(int)((float)insets.top+currentHeight* (((float)diff-i)/(float)diff)), 
			    		insets.left+5, 
			    		(int)((float)insets.top+currentHeight* (((float)diff-i)/(float)diff)));
			    
				graphics.drawString(Integer.toString( maxSize-(diff-(int)i)),insets.left-10, (int)((float)insets.top+currentHeight* (((float)diff-i)/(float)diff)));
			}
			
		}

		/**
		 * Computes the deviation from the vertical center line in x-direction.
		 */
		private int getXDeviation(int y) {
			return (int) (Math.sin(Math.PI * (y / (double)currentHeight)) * getXScale());
		}

		/**
		 * Computes the scalation factor in x direction.
		 */
		private double getXScale() {
			return currentWidth * (X_SCALE / 2);
		}

		/**
		 * Computes the display coordinates for the specified individual.
		 */
		private Point getDisplayCoordinates(boolean[] b) {
			// count the number of ones to get the y coordinate
			int onemax = 0;
			for (int i = 0; i < b.length; i++) {
				if (b[i])
					onemax++;
			}

			/*
			 * Compute the x coordinate by the sum of the indices of ones.
			 * The x coordinate is interpolated between the x coordinates of the
			 * minimal and maximal values for the sums of the indices of ones.
			 */
			int sumOfIndices = 0;
			for (int i = 0; i < b.length; i++) {
				if (b[i])
					sumOfIndices += i;
			}
			// the minimal and maximal values are computed with Gaussian sums.
			int minimalSumOfIndices = ((onemax - 1) * onemax) / 2;
			int maximalSumOfIndices = ((b.length - 1) * b.length) / 2 - ((b.length - 1 - onemax) * (b.length - onemax)) / 2;
			int range = maximalSumOfIndices - minimalSumOfIndices;

			// compute a normalized value in [-range, +range]
			int x = 2 * sumOfIndices - 2 * minimalSumOfIndices - range;

			int centerX = insets.left + currentWidth / 2;
			int yOffset = (int) (((b.length - onemax) * currentHeight) / (double)b.length);
			int xOffset = 0;
			if (range != 0)
				xOffset = (int) ((x * getXDeviation(yOffset)) / (double)range);

			return new Point(centerX + xOffset, yOffset + insets.top);
		}

		private void drawIndividual(int maxFitness, int minFitness, int maxSize, int minSize, int fitness, int size) {
			int radius = getPointSize();
			int diffSize = maxSize-minSize+1;
			if(maxFitness == minFitness ){
				minFitness = minFitness -1;
				
			}
			int diffFitness = maxFitness - minFitness +1;
			
			
			Point point = new Point((int)((float)currentWidth/4.0 + 
					(float)(currentWidth-(float)currentWidth/4.0)/(float)diffFitness * (float)(fitness-(maxFitness-diffFitness))), 
					currentHeight+2*insets.top - 
					((int)((float)((currentHeight+insets.top)/(float)diffSize) * (float)(size-(maxSize-diffSize)))));
			graphics.fillOval((int)point.getX() - (radius + 1) / 2 + 1, (int)point.getY() - (radius + 1) / 2, radius, radius);
			drawYAchsenBeschriftung(minSize, maxSize);
			drawXAchsenBeschriftung(minFitness, maxFitness);
		}

		private void drawPixel(int x, int y) {
			graphics.drawLine(x - 1, y, x + 1, y);
		}

		/**
		 * Computes the radius for the circle representing individuals.
		 */
		private int getPointSize() {
			return Math.max(1, Math.min(MAX_POINT_SIZE, currentHeight / 50));
		}

	}

}