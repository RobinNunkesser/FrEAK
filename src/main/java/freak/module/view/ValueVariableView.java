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
import freak.core.fitness.*;
import freak.core.modulesupport.*;
import freak.core.population.*;
import freak.core.view.*;
import freak.core.view.swingsupport.*;
import freak.module.searchspace.*;
import java.awt.*;
import javax.swing.*;

/**
 * A schematic visualization of a boolean hypercube. The individuals are drawn as small circles. The <code>y</code>-position of such a circle is determined by the number of ones in the genotype, the <code>x</code>-position is determined by the position of the ones in the genotype: the leftmost position of all individuals with <code>i</code> ones belongs to the genotype <code>1^i 0^n-i</code>, the rightmost position belongs to the genotype <code>0^n-i 1^i</code>.
 * @author  Dirk
 */
public class ValueVariableView extends AbstractView implements Configurable, FreakSwingModel {
	private transient JPanel panel;

	private static final float MAXIMAL_HUE = 0.8f;
	private static float SATURATION = 1.0f;
	private static float BRIGHTNESS = 1.0f;

	private static Color DEFAULT_COLOR = Color.red;

	private IndividualList individuals;

	private boolean colorByFitness;
	private boolean displayOptimum;

	private int maximalNumberOfIndividuals;

	/**
	 * Constructs a new <code>HyperCube</code> with a link back to the current
	 * schedule.
	 *
	 * @param schedule a link back to the current schedule.
	 */
	public ValueVariableView(Schedule schedule) {
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
		panel = new ValuePanel(this);
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
		super.update(data);

		synchronized (this) {
			individuals = (IndividualList)data;
		}
		UpdateManager.markDirty(this);
	}

	public void flush() {
		if (panel == null)
			return;

		panel.repaint();
	}

	public String getName() {
		return "Value View";
	}

	public String getDescription() {
		return "";
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
		return "Displays the global optimum in the ValueView. This option can only be enabled if a global optimum exists and if the fitness function knows the optimum.";
	}

	/**
	 * Sets the maximal number of individuals displayed in the ValueView.
	 */
	public void setPropertyMaximalNumberOfIndividuals(Integer number) {
		if (number.intValue() > 0)
			this.maximalNumberOfIndividuals = number.intValue();
	}

	/**
	 * Returns the maximal number of individuals displayed in the ValueView.
	 */
	public Integer getPropertyMaximalNumberOfIndividuals() {
		return new Integer(maximalNumberOfIndividuals);
	}

	public String getShortDescriptionForMaximalNumberOfIndividuals() {
		return "Maximal number of individuals";
	}

	public String getLongDescriptionForMaximalNumberOfIndividuals() {
		return "Sets the maximal number of individuals displayed in the ValueView. " +
			"If more individuals are to be displayed, only the first ones are shown.";
	}

	/**
	 * The <code>JPanel</code> belonging to <code>ValueView</code>.
	 * @author  Dirk
	 */
	class ValuePanel extends JPanel {

		// scaling factor in x direction;
		public static final double X_SCALE = 0.8;
		public static final int MAX_POINT_SIZE = 8;

		private Insets insets;
		private int currentWidth;
		private int currentHeight;
		private Graphics graphics;
		private ValueVariableView ValueView;

		public ValuePanel(ValueVariableView ValueView) {
			super();
			this.ValueView = ValueView;
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

			synchronized (ValueView) {
				individuals = ValueView.getIndividualList();

				// draw individuals
				if (individuals != null) {
					
					for(int i = 0; i< individuals.size(); i++){
					//while (iter.hasNext() && counter < maximalNumberOfIndividuals) {
						//Individual individual = (Individual)iter.next();
						Individual individual = (Individual)individuals.getIndividual(i);
						g.setColor(ValueView.getColorByFitness(individual));
						//drawIndividual(((BooleanFunctionGenotype)individual.getGenotype()).getOptFitness(), ((BooleanFunctionGenotype)individual.getGenotype()).evaluate(), ((BooleanFunctionGenotype)individual.getGenotype()).evaluateSize());
					}
				}

				// try to draw global optimum, if this option is enabled
				if (ValueView.displayOptimum) {
					try {
						Genotype optimum = ValueView.getSchedule().getFitnessFunction().getGenotypeOptimum();
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
				drawPixel(insets.left+10, y + insets.top);
			}
			
			//x-Achse (Fitness)
			for(int x = 0; x<=currentWidth; x++){
				drawPixel(x+insets.left+10, currentHeight+insets.top);
			}
			
			//x-Achsenbeschriftung
			int j =1;
			for(int i = insets.left+15; i<= currentWidth; i+=currentWidth/35){
				if(j<=35){
					String s = Integer.toString(j);
					String x = "x";
					String w = x+s;
					graphics.drawString(w, i, currentHeight+insets.top+10 );
					graphics.drawLine(i,currentHeight+insets.top, i, currentHeight+insets.top-5);
				}
				j++;
			}
			
			//y-Achsenbeschriftung
			int l=6;
			for(float i = 2; i<=13; i+=2){
			    graphics.drawLine(insets.left+5, (int)((float)insets.top+currentHeight* ((12.5-i)/12.0)), insets.left+15, (int)((float)insets.top+currentHeight* ((12.5-i)/12.0)));
				String gleich = "= ";
				String zahl = Integer.toString((int)((float)l));
				String w = gleich + zahl;
			    graphics.drawString(w,insets.left-10, (int)((float)insets.top+currentHeight* ((12.5-i)/12.0)));
				
			    graphics.drawLine(insets.left+5, (int)((float)insets.top+currentHeight* ((12.0-i+1.5)/12.0)), insets.left+15, (int)((float)insets.top+currentHeight* ((12.0-i+1.5)/12.0)));
				String ungleich = "!= ";
				zahl = Integer.toString((int)((float)l));
				w = ungleich + zahl;
				graphics.drawString(w,insets.left-10, (int)((float)insets.top+currentHeight* ((12.0-i+1.5)/12.0)));
				l--;
			}
						
			graphics.drawLine(currentWidth+insets.left+5,currentHeight+insets.top-5,currentWidth+insets.left+10,currentHeight+insets.top);
			graphics.drawLine(currentWidth+insets.left+5,currentHeight+insets.top+5,currentWidth+insets.left+10,currentHeight+insets.top);
			graphics.drawString("Variable",currentWidth-30, currentHeight);
			
			graphics.drawLine(insets.left+5, insets.top+5, insets.left+10, insets.top);
			graphics.drawLine(insets.left+15, insets.top+5, insets.left+10, insets.top);
			graphics.drawString("Belegung", insets.left+20, insets.top+10);

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

		private void drawIndividual(int maxFitness, int fitness, int size) {
			int radius = getPointSize();	
			Point point = new Point((int)((float)fitness/(float)maxFitness * (float)currentWidth), (int)((float)(currentHeight+insets.top-size*20 )));
			graphics.fillOval((int)point.getX() - (radius + 1) / 2 + 1, (int)point.getY() - (radius + 1) / 2, radius, radius);
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