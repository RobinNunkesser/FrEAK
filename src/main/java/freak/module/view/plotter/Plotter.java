/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.view.plotter;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.BatchEventListener;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.observer.Observer;
import freak.core.view.HistoryView;
import freak.core.view.ObserverUnknownException;
import freak.core.view.swingsupport.FreakSwingModel;
import freak.core.view.swingsupport.SlidingWindow;
import freak.core.view.swingsupport.UpdateManager;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.TreeSet;

/**
 * A Plotter displaying the received numbers over time. The y-axis shows the values of the numbers received and the x-axis shows the current time scale. Thus, the x values are supposed to be nondecreasing; else, an <code>DecreasingXValuesException</code> is thrown.
 * @author  Dirk
 */
public class Plotter extends HistoryView implements FreakSwingModel, BatchEventListener, RunEventListener {

	private transient JPanel wrapperPanel;
	private transient AbstractPlotterPanel plotterPanel;

	/**
	 * The ideal spaces for the grid.
	 */
	public static final int GRID_XSPACE = 60;
	public static final int GRID_YSPACE = 30;

	/**
	 * The color for the points drawn.
	 */
	Color pointColor;

	/**
	 * Contains the currently displayed points mapped by their x values.
	 */
	SlidingWindow xValues;

	/**
	 * Contains the currently displayed points sorted by their y values.
	 */
	TreeSet yValues;

	/**
	 * Specifies if the y axis is to be shrunk automatically.
	 */
	boolean shrinkYAxis;

	/**
	 * Specifies if the plotter should use a constant x space for 
	 * generations or fit the x axis into its frame.
	 */
	boolean useConstantXSpace;

	/**
	 * Specifies if the plotter should connect points in consecutive lists
	 * having the same index with a line.
	 */
	boolean showLines;

	/* 
	 * Bounds for the ranges of the currently saved points
	 * in x- and y-direction.
	 * 
	 * The left and right bounds for the x-axis are specified by xAxisMin and 
	 * xAxisMax. xAxisMin is nondecreasing since the x-axis represents time,
	 * so all points with smaller x values can be deleted. xAxisMax can only
	 * be decreased in case the number of displayed values is decreased.
	 * 
	 * The upper and lower bounds for the y-axis can be adjusted automatically
	 * according to the value of <code>shrinkYAxis</code>.
	 */ 
	int xAxisMin;
	int xAxisMax;
	double yAxisLowerBound;
	double yAxisUpperBound;
	
	int currentXValue;

	double constantGenXSpace;

	/**
	 * Creates a new <code>Plotter</code> view.
	 */
	public Plotter(Schedule schedule) {
		super(schedule);

		pointColor = Color.black;
		showLines = true;
		shrinkYAxis = true;

		setPropertyNumberOfDisplayedValues(new Integer(500));
		pointColor = Color.black;

		useConstantXSpace = false;
		constantGenXSpace = 1;

		plotterPanel = null;
	}

	public JPanel createPanel() {
		wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new BorderLayout());

		if (useConstantXSpace) {
			setPlotterPanel(new ExpandingPlotterPanel(this));
		} else {
			setPlotterPanel(new AdjustingPlotterPanel(this));
		}

		return wrapperPanel;
	}

	/**
	 * @param plotterPanel  the plotterPanel to set
	 * @uml.property  name="plotterPanel"
	 */
	private void setPlotterPanel(AbstractPlotterPanel plotterPanel) {
		this.plotterPanel = plotterPanel;

		wrapperPanel.removeAll();
		wrapperPanel.add(plotterPanel, BorderLayout.CENTER);
	}

	public Class[] getInputDataTypes() {
		return new Class[] {
			Number.class,
			byte[].class,
			short[].class,
			int[].class,
			long[].class,
			float[].class,
			double[].class };
	}

	public void update(Object o) {
		synchronized (this) {
			super.update(o);

			double[] numbers = null;

			// extract doubles from the received object
			if (o instanceof Number) {
				numbers = new double[] {((Number) o).doubleValue()};
			} else {
				// extract doubles from an array of primitive data types
				int length = Array.getLength(o);
				numbers = new double[length];
				for (int i = 0; i < length; i++) {
					numbers[i] = Array.getDouble(o, i);
				}
			}

			appendData(numbers);
		} // release lock

		UpdateManager.markDirty(this);
	}

	public void flush() {
		plotterPanel.flush();
	}

	public String getName() {
		return "Plotter";
	}

	public String getDescription() {
		return "A simple plotter rendering the observed values over time.";
	}

	public void runStarted(RunEvent evt) {
		try {
			if (getObserver().getMeasure() == Observer.GENERATIONS) {
				synchronized (this) {
					clearData();
					initAxes();
				} // release lock
				UpdateManager.markDirty(this);
			}
		} catch (ObserverUnknownException e) {
			throw new RuntimeException(e);
		}
	}

	public void batchStarted(BatchEvent evt) {
		try {
			if (getObserver().getMeasure() == Observer.RUNS) {
				synchronized (this) {
					clearData();
					initAxes();
				} // release lock
				UpdateManager.markDirty(this);
			}
		} catch (ObserverUnknownException e) {
			throw new RuntimeException(e);
		}
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
		schedule.getEventController().addEvent(this, BatchEvent.class, schedule);
	}

	// --- methods concerning the model of the plotter ---

	/**
	 * Adds new data that is displayed by the plotter.
	 * 
	 * @param data a <code>double</code> array with the y values of the new points.
	 */
	private void appendData(double[] data) {
		try {
			// throw exception if the values are decreasing
			if (getCurrentValue() < currentXValue) throw new DecreasingXValueException("The plotter can only handle nondecreasing x values.");
			
			// increase new value for xAxisMax if necessary 
			int newxAxisMax = Math.max(xAxisMax, getCurrentValue());
			// increase new value for xAxisMin if necessary 
			int newxAxisMin = (numberOfDisplayedValues == 0 ? xAxisMin : Math.max(xAxisMin, newxAxisMax - numberOfDisplayedValues + 1));

			// set new values for the x-Axis
			adjustXAxis(newxAxisMin, newxAxisMax);
			
			currentXValue = getCurrentValue();

			// add new Points if they are visible in the plotterPanel
			if (plotterPanel.isXPositionVisible(currentXValue)) {
				addPoints(currentXValue, data);
			}
			
		} catch (ObserverUnknownException e) {
			// this should not happen
			throw new RuntimeException(e);
		}
	}

	/**
	 * Remove Points in the x-interval [a, b[, i.e., from a (included) to 
	 * b (excluded).
	 */
	private void removePointsInInterval(int a, int b) {
		if (a < b && xValues != null && yValues != null) {
			// remove points from data structures
			while (xValues.size() > 0 && ((PointList)xValues.get(0)).x < b) {
				PointList oldPoints = (PointList) xValues.get(0);

				xValues.removeFirst();

				for (int i = 0; i < oldPoints.data.length; i++) {
					yValues.remove(oldPoints.data[i]);
				}
			}
			
			// re-adjust y axis if shrinkYAxis is set
			if (shrinkYAxis && yValues.size() > 0) {
				adjustYAxis(
					((Point) yValues.first()).y,
					((Point) yValues.last()).y);
			}
		}
	}

	private void addPoints(int x, double[] data) {
		// create Points
		Point[] points = new Point[data.length];
		for (int i = 0; i < data.length; i++) {
			points[i] = new Point(x, data[i]);
			addPoint(points[i]);
		}
		// create PointList and add it to xValues
		PointList list = new PointList(x, points);
		xValues.add(list);
	}

	private void addPoint(Point point) {
		yValues.add(point);

		double lowestValue = ((Point) yValues.first()).y;
		double highestValue = ((Point) yValues.last()).y;

		if (shrinkYAxis) {
			adjustYAxis(lowestValue, highestValue);
		} else {
			adjustYAxis(
				Math.min(yAxisLowerBound, lowestValue),
				Math.max(yAxisUpperBound, highestValue));
		}
	}

	/**
	 * Clears and resets the plotter.
	 */
	public void clearData() {
		xValues = new SlidingWindow();
		yValues = new TreeSet();
	}
	
	public void initAxes() throws ObserverUnknownException, NullPointerException {
		yAxisLowerBound = 0;
		yAxisUpperBound = 1;

		xAxisMin = getCurrentValue();
		if (plotterPanel != null) {
			xAxisMax = xAxisMin + plotterPanel.getStartingGenerations() - 1;
		} else {
			throw new NullPointerException("PlotterPanel is null.");
		}
		
		currentXValue = xAxisMin;
	}

	/**
	 * Adjusts the x axis to the specified bounds.
	 */
	private void adjustXAxis(int lower, int upper) {
		// remove points beyond xAxisMin, i.e., points with smaller x value 
		removePointsInInterval(xAxisMin, lower);			

		xAxisMin = lower;
		xAxisMax = upper;
	}

	/**
	 * Adjusts the y axis to the specified bounds.
	 */
	private void adjustYAxis(double lower, double upper) {
		yAxisLowerBound = lower;
		yAxisUpperBound = upper;

		// create default interval if bounds are equal
		if (yAxisLowerBound == yAxisUpperBound) {
			yAxisLowerBound -= 0.5;
			yAxisUpperBound += 0.5;
		}
	}

	// --- property methods

	/**
	 * Decides the points are to be connected by colored lines.
	 * 
	 * @param lines a <code>Boolean</code> object.
	 */
	public void setPropertyShowLines(Boolean lines) {
		showLines = lines.booleanValue();
	}

	/**
	 * Checks if the points are connected by colored lines.
	 * 
	 * @return <code>true</code> if the points are connected; <code>false</code> otherwise.
	 */
	public Boolean getPropertyShowLines() {
		return new Boolean(showLines);
	}

	public String getShortDescriptionForShowLines() {
		return "Show lines";
	}

	public String getLongDescriptionForShowLines() {
		return "Set to connect the points by colored lines.";
	}

	/**
	 * Sets the color of the points plotted.
	 * 
	 * @param color the new color.
	 */
	public void setPropertyColor(Color color) {
		if (color != null) {
			pointColor = color;
		}
	}

	/**
	 * Returns the current color of the points plotted.
	 * 
	 * @return the current color.
	 */
	public Color getPropertyColor() {
		return pointColor;
	}

	public String getShortDescriptionForColor() {
		return "Color of points";
	}

	public String getLongDescriptionForColor() {
		return "Sets the color for the displayed points.";
	}

	/**
	 * Decides if the y axis is to be shrunk automatically.
	 * 
	 * @param lines a <code>Boolean</code> object.
	 */
	public void setPropertyShrinkYAxis(Boolean shrink) {
		shrinkYAxis = shrink.booleanValue();
	}

	/**
	 * Checks if the y axis is shrunk automatically.
	 */
	public Boolean getPropertyShrinkYAxis() {
		return new Boolean(shrinkYAxis);
	}

	public String getShortDescriptionForShrinkYAxis() {
		return "Shrink y axis automatically";
	}

	public String getLongDescriptionForShrinkYAxis() {
		return "When enabled, the y axis is shrunk automatically every time an old maximum or minimum value perishes. When disabled, the y axis is just expanded automatically.";
	}

	/**
	 * Decides if all points are to be fit into the current window
	 * in x direction.
	 * 
	 * @param lines a <code>Boolean</code> object.
	 */
	public void setPropertyFitWindow(Boolean fit) {
		useConstantXSpace = fit.booleanValue();

		if (plotterPanel != null) {
			if (useConstantXSpace) {
				setPlotterPanel(new ExpandingPlotterPanel(this));
			} else {
				setPlotterPanel(new AdjustingPlotterPanel(this));
			}
		}
	}

	/**
	 * Checks if all values are fit into the current window in x direction.
	 */
	public Boolean getPropertyFitWindow() {
		return new Boolean(useConstantXSpace);
	}

	public String getShortDescriptionForFitWindow() {
		return "Scroll mode";
	}

	public String getLongDescriptionForFitWindow() {
		return "When enabled, only parts of the drawing area are shown. When disabled, all points are fit into the current window.";
	}

	/**
	 * Sets the space between two x values.
	 *
	 * @param lines a <code>Boolean</code> object.
	 */
	public void setPropertyXSpace(Double space) {
		if (space.doubleValue() > 0)
			constantGenXSpace = space.doubleValue();

		if (plotterPanel != null) {
			plotterPanel.flush();
		}
	}

	/**
	 * Returns the space between two x values.
	 */
	public Double getPropertyXSpace() {
		return new Double(constantGenXSpace);
	}

	public String getShortDescriptionForXSpace() {
		return "Space in x direction";
	}

	public String getLongDescriptionForXSpace() {
		return "The space between two x values on the x axis in pixels. This property is only used if scroll mode is enabled.";
	}

	public void setObserver(Observer observer) {
		super.setObserver(observer);
		if (observer != null) {
			try { 
				// call createPanel to instantiate the plotter
				if (plotterPanel == null) createPanel();
				clearData();
				// now the observer is known, we initialize the 
				// x-Axis using the observer's current value
				initAxes();
			} catch (ObserverUnknownException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void setPropertyNumberOfDisplayedValues(Integer number) {
		super.setPropertyNumberOfDisplayedValues(number);
		
		// increase new value for xAxisMax if necessary 
		int newxAxisMax = Math.max(currentXValue, xAxisMin + numberOfDisplayedValues - 1);
		// increase new value for xAxisMin if necessary 
		int newxAxisMin = (numberOfDisplayedValues == 0 ? xAxisMin : Math.max(xAxisMin, newxAxisMax - numberOfDisplayedValues + 1));

		// set new values for the x-Axis
		adjustXAxis(newxAxisMin, newxAxisMax);
	}

}
