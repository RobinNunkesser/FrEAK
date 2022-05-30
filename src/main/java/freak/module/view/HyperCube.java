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
import java.util.*;
import javax.swing.*;

/**
 * A schematic visualization of a boolean hypercube. The individuals are drawn as small circles. The <code>y</code>-position of such a circle is determined by the number of ones in the genotype, the <code>x</code>-position is determined by the position of the ones in the genotype: the leftmost position of all individuals with <code>i</code> ones belongs to the genotype <code>1^i 0^n-i</code>, the rightmost position belongs to the genotype <code>0^n-i 1^i</code>.
 * @author  Dirk
 */
public class HyperCube extends AbstractView implements Configurable, FreakSwingModel {
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
	public HyperCube(Schedule schedule) {
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
		
		if (!(schedule.getGenotypeSearchSpace() instanceof BitString)) {
			throw new UnsupportedEnvironmentException("Unsupported searchspace.");
		}
	}

	public JPanel createPanel() {
		panel = new HyperCubePanel(this);
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
		return "Boolean Hypercube";
	}

	public String getDescription() {
		return "A schematic visualization of a boolean hypercube.";
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
		return "Displays the global optimum in the hypercube. This option can only be enabled if a global optimum exists and if the fitness function knows the optimum.";
	}

	/**
	 * Sets the maximal number of individuals displayed in the hypercube.
	 */
	public void setPropertyMaximalNumberOfIndividuals(Integer number) {
		if (number.intValue() > 0)
			this.maximalNumberOfIndividuals = number.intValue();
	}

	/**
	 * Returns the maximal number of individuals displayed in the hypercube.
	 */
	public Integer getPropertyMaximalNumberOfIndividuals() {
		return new Integer(maximalNumberOfIndividuals);
	}

	public String getShortDescriptionForMaximalNumberOfIndividuals() {
		return "Maximal number of individuals";
	}

	public String getLongDescriptionForMaximalNumberOfIndividuals() {
		return "Sets the maximal number of individuals displayed in the hypercube. " +
			"If more individuals are to be displayed, only the first ones are shown.";
	}

	/**
	 * The <code>JPanel</code> belonging to <code>HyperCube</code>.
	 * @author  Dirk
	 */
	class HyperCubePanel extends JPanel {

		// scaling factor in x direction;
		public static final double X_SCALE = 0.8;
		public static final int MAX_POINT_SIZE = 8;

		private Insets insets;
		private int currentWidth;
		private int currentHeight;
		private Graphics graphics;
		private HyperCube hyperCube;

		public HyperCubePanel(HyperCube hyperCube) {
			super();
			this.hyperCube = hyperCube;
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

			synchronized (hyperCube) {
				individuals = hyperCube.getIndividualList();

				// draw individuals
				if (individuals != null) {
					Iterator iter = individuals.iterator();
					int counter = 0;
					while (iter.hasNext() && counter < maximalNumberOfIndividuals) {
						Individual individual = (Individual)iter.next();
						g.setColor(hyperCube.getColorByFitness(individual));
						drawIndividual(((BitStringGenotype)individual.getGenotype()).getBooleanArray());
						counter++;
					}
				}

				// try to draw global optimum, if this option is enabled
				if (hyperCube.displayOptimum) {
					try {
						Genotype optimum = hyperCube.getSchedule().getFitnessFunction().getGenotypeOptimum();
						drawOptimum(((BitStringGenotype)optimum).getBooleanArray());
					} catch (UnsupportedOperationException ex) {
						// nevermind.
					}
				}
			}
		}

		private void drawSearchSpace() {
			graphics.setColor(Color.black);
			int centerx = insets.left + currentWidth / 2;
			for (int y = 0; y <= currentHeight; y++) {
				int xdev = getXDeviation(y);
				drawPixel(centerx - xdev, y + insets.top);
				drawPixel(centerx + xdev, y + insets.top);
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

		private void drawIndividual(boolean[] b) {
			int radius = getPointSize();
			Point point = getDisplayCoordinates(b);
			graphics.fillOval((int)point.getX() - (radius + 1) / 2 + 1, (int)point.getY() - (radius + 1) / 2, radius, radius);
		}

		private void drawOptimum(boolean[] b) {
			int length = getPointSize() * 2 / 3;
			Point point = getDisplayCoordinates(b);
			graphics.setColor(Color.blue);
			graphics.drawLine((int)point.getX() - length, (int)point.getY() - length, (int)point.getX() + length, (int)point.getY() + length);
			graphics.drawLine((int)point.getX() - length, (int)point.getY() + length, (int)point.getX() + length, (int)point.getY() - length);
			graphics.drawLine((int)point.getX(), (int)point.getY() - length, (int)point.getX(), (int)point.getY() + length);
			graphics.drawLine((int)point.getX() - length, (int)point.getY(), (int)point.getX() + length, (int)point.getY());
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