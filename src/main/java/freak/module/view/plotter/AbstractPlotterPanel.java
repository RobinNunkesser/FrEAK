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

import javax.swing.*;
import java.awt.*;

/**
 * An abstract super class for the plotter's panels.
 * @author  Dirk
 */
public abstract class AbstractPlotterPanel extends JPanel {

	final Plotter view;

	public static final int LEFT_MARGIN = 40;
	public static final int RIGHT_MARGIN = 15;
	public static final int TOP_MARGIN = 10;
	public static final int BOTTOM_MARGIN = 25;

	transient XAxis xAxis;
	transient YAxis yAxis;
	transient PlotArea plotArea;

	public AbstractPlotterPanel(Plotter view) {
		this.view = view;
	}

	/**
	 * Updates and repaints the panel.
	 */
	public abstract void flush();

	/**
	 * Returns the number of generations displayed at the start of a run.
	 */
	public abstract int getStartingGenerations();

	public void paint(Graphics g) {
		synchronized (view) {
			super.paint(g);
			view.xValues.synchronizeViews();
		}
	}

	/**
	 * Rounds the specified double value to a nice value. Nice is defined
	 * from an aesthetical point of view as a number that appears round 
	 * because it contains many zeroes in the decimal system. 
	 * From a mathematical point of view, a nice value is a power of ten 
	 * divided by a (small) power of two.
	 * E.g., 1000, 500, 250, 125, 100, 50, 25 are nice values.
	 * 
	 * It is guaranteed that the original value is approximated within
	 * a factor of at most sqrt(2).
	 */
	public static double roundToNiceValue(double value) {
		// compute the number of decimal digits
		double numberOfDecimalDigits = Math.log(value) / Math.log(10);
		// compute next power of ten 
		double result = Math.pow(10, Math.ceil(numberOfDecimalDigits));
		// divide the power of ten by 2 until input value is approximated
		// with factor sqrt(2)
		while (value * 1.4142135623730951 < result) {
			result /= 2;
		}
		return result;
	}

	/**
	 * Check for ugly numbers such as 0.7999999999999999 resulting from
	 * numerical inaccuracies and round them to nicer values.
	 */
	public static String roundToString(double value) {
		String s = String.valueOf(value);

		// heuristical detection of numerical inaccuracies 
		if (s.indexOf('.') > -1 && (s.length() - s.indexOf('.') - 1 > 7)) {
			// simple workaround. More complex calculations lead to new inaccurencies.
			return String.valueOf(Math.round(value * 1000000) / 1000000.0);
		}
		return s;
	}

	Color getColorForPointIndex(int index, int number) {
		return new Color(Color.HSBtoRGB((float)index / number, 1.0f, 0.75f));
	}

	abstract int computeDisplayXCoordinate(int x);

	int computeDisplayYCoordinate(double y) {
		int baseLine = (int)plotArea.getBounds().getMaxY() - plotArea.getInsets().bottom - PlotArea.BOTTOM_INSETS;
		return (int) (baseLine - ((y - view.yAxisLowerBound) * yAxis.height / (view.yAxisUpperBound - view.yAxisLowerBound)));
	}
	
	/**
	 * Returns <code>true</code> if the specified x position is visible, i.e., 
	 * if the specified x position will be drawn by the PlotArea.
	 */
	boolean isXPositionVisible(int currentXValue) {
		return true;
//		return plotArea.isXPositionVisible(currentXValue);
	}


	class XAxis extends JComponent {

		transient int minVisibleX;
		transient int maxVisibleX;

		transient int xGenStepWidth;
		transient int xGenStart;

		transient Graphics graphics;

		public void paintComponent(Graphics graphics) {
			this.graphics = graphics;

			divideXAxis();
			computeVisibleBounds();

			int ypos = 0;
			int minX = 0;
			int maxX = (int)getBounds().getWidth();

			graphics.setColor(Color.black);
			graphics.drawLine(minX - 5, ypos, maxX + 5, ypos);

			// compute nice starting number for drawing the lines
			xGenStart = minVisibleX - (minVisibleX % xGenStepWidth);
			for (int x = xGenStart; x <= maxVisibleX; x += xGenStepWidth) {
				int xpos = computeDisplayXCoordinate(x);
				drawTickOnXAxis(String.valueOf(x), xpos, ypos);
			}
		}

		void computeVisibleBounds() {
			minVisibleX = view.xAxisMin;
			maxVisibleX = view.xAxisMax;
		}

		void drawTickOnXAxis(String text, int xpos, int ypos) {
			int height = graphics.getFontMetrics().getHeight();
			int width = graphics.getFontMetrics().stringWidth(text);
			graphics.drawLine(xpos, ypos, xpos, ypos + 5);
			graphics.drawString(text, xpos - (width / 2), ypos + height + 5);
		}

		void divideXAxis() {
			// compute step width so that a line is drawn every GRID_YSPACE pixels          
			double xStepWidthRaw = Plotter.GRID_XSPACE * (view.xAxisMax - view.xAxisMin) / plotArea.getWidth();
			xGenStepWidth = Math.max(1, (int)roundToNiceValue(xStepWidthRaw));
		}

	}

	class YAxis extends JComponent {

		transient double yGridStepWidth;
		transient double yGridStart;

		transient int newWidth;

		transient int height;

		transient Graphics graphics;

		public void paintComponent(Graphics graphics) {
			divideYAxis();

			this.graphics = graphics;
			Rectangle bounds = this.getBounds();
			newWidth = getWidth();

			int xpos = (int)bounds.getMaxX() - 0;
			int minY = (int)plotArea.getBounds().getMinY() + plotArea.getInsets().top;
			int maxY = (int)plotArea.getBounds().getMaxY() - plotArea.getInsets().bottom;

			graphics.setColor(Color.black);
			graphics.drawLine(xpos, minY - 5, xpos, maxY + 5);

			if (view.yAxisLowerBound == view.yAxisUpperBound) {
				// range is empty, just draw minimal and maximal ticks with the same value
				drawTickOnYAxis(roundToString(view.yAxisLowerBound), xpos, maxY);
				drawTickOnYAxis(roundToString(view.yAxisUpperBound), xpos, minY);
			} else {
				for (double y = yGridStart; y <= view.yAxisUpperBound; y += yGridStepWidth) {
					int ypos = computeDisplayYCoordinate(y) + 1;
					drawTickOnYAxis(roundToString(y), xpos, ypos);
				}
			}
			// broaden axis if necessary
			if (newWidth > getWidth()) {
				setPreferredSize(new Dimension(newWidth, getPreferredSize().height));
				revalidate();
			}
		}

		void drawTickOnYAxis(String text, int xpos, int ypos) {
			int offset = graphics.getFontMetrics().getAscent() / 2;
			int width = graphics.getFontMetrics().stringWidth(text);
			graphics.drawLine(xpos - 5, ypos, xpos, ypos);
			graphics.drawString(text, xpos - width - 10, ypos + offset);

			// compute maximal width to resize the axis later if necessary
			if (width + 15 > newWidth) {
				newWidth = width + 15;
			}
		}

		void divideYAxis() {
			height = plotArea.getHeight() - plotArea.getInsets().top - plotArea.getInsets().bottom - PlotArea.TOP_INSETS - PlotArea.BOTTOM_INSETS;
			// compute step width so that a line is drawn every GRID_YSPACE pixels			
			double yStepWidthRaw = Plotter.GRID_YSPACE * (view.yAxisUpperBound - view.yAxisLowerBound) / height;
			yGridStepWidth = roundToNiceValue(yStepWidthRaw);
			// compute nice starting number for drawing the lines
			yGridStart = Math.ceil(view.yAxisLowerBound / yGridStepWidth) * yGridStepWidth;
		}

	}

	/**
	 * The area where points and grid lines are drawn.
	 * @author  Dirk
	 */
	class PlotArea extends JComponent {

		public static final int TOP_INSETS = 10;
		public static final int BOTTOM_INSETS = 5;

		transient int minVisibleX;
		transient int maxVisibleX;

		transient int pointSize;

		private Rectangle bounds;

		transient Graphics graphics;

		private XAxis xAxis;
		private YAxis yAxis;

		public PlotArea(XAxis xAxis, YAxis yAxis) {
			this.xAxis = xAxis;
			this.yAxis = yAxis;

			pointSize = 2;
		}

		public void paintComponent(Graphics graphics) {
			this.graphics = graphics;
			bounds = this.getBounds();

			xAxis.divideXAxis();
			yAxis.divideYAxis();
			computeVisibleBounds();

			drawGrid();
			drawPoints();
		}

		void computeVisibleBounds() {
			minVisibleX = view.xAxisMin;
			maxVisibleX = view.xAxisMax;
		}

		void drawPoints() {
			graphics.setColor(view.pointColor);

			if (xAxis.xGenStepWidth < 50) {
				pointSize = 2;
			} else {
				pointSize = 1;
			}

			// draw at most 10 point lists between neighbored grid lines
			int pointXStepWidth = Math.max(1, xAxis.xGenStepWidth / 10);

			PointList oldList = null;
			int xStart = minVisibleX - (minVisibleX % xAxis.xGenStepWidth);
			
			for (int i = xStart; i <= maxVisibleX; i += pointXStepWidth) {
				PointList list = null;
				try {
					list = (PointList)view.xValues.get(i - minVisibleX);
				} catch (RuntimeException ex) {
				}
				if (list != null) {
					for (int j = 0; j < list.data.length; j++) {
						drawPoint(list.data[j]);
						if (view.showLines && (oldList != null) && (j < oldList.data.length)) {
							graphics.setColor(getColorForPointIndex(j, list.data.length));
							graphics.drawLine(oldList.data[j].xpos, oldList.data[j].ypos, list.data[j].xpos, list.data[j].ypos);
							graphics.setColor(view.pointColor);
						}
					}
					oldList = list;
				}
			}
		}

		void drawPoint(Point point) {
			point.xpos = computeDisplayXCoordinate((int)point.x);
			point.ypos = computeDisplayYCoordinate(point.y);

			graphics.drawLine(point.xpos - pointSize, point.ypos - pointSize, point.xpos + pointSize, point.ypos + pointSize);
			graphics.drawLine(point.xpos - pointSize, point.ypos + pointSize, point.xpos + pointSize, point.ypos - pointSize);
		}

		void drawGrid() {
			graphics.setColor(Color.lightGray);

			// draw horizontal lines
			for (double y = yAxis.yGridStart; y < view.yAxisUpperBound + yAxis.yGridStepWidth; y += yAxis.yGridStepWidth) {
				int ypos = computeDisplayYCoordinate(y);
				graphics.drawLine(0, ypos, getWidth(), ypos);
			}

			// draw vertical lines
			int xStart = (int)Math.ceil(minVisibleX / (double)xAxis.xGenStepWidth) * xAxis.xGenStepWidth;
			for (int x = xStart; x <= maxVisibleX; x += xAxis.xGenStepWidth) {
				int xpos = computeDisplayXCoordinate(x);
				graphics.drawLine(xpos, 0, xpos, getHeight());
			}
		}
		
		boolean isXPositionVisible(int x) {
			xAxis.divideXAxis();
			
			int pointXStepWidth = Math.max(1, xAxis.xGenStepWidth / 10);
			int xStart = minVisibleX - (minVisibleX % xAxis.xGenStepWidth);
			
			if (x < xStart || x > maxVisibleX) return false;
			return (x - xStart) % pointXStepWidth == 0;
		}
	}

}
