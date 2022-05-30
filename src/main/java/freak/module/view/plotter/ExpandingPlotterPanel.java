/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.view.plotter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import freak.core.view.swingsupport.FreakScrollBarAdjustmentListener;

/**
 * A panel for the plotter that expands in x direction automatically
 * and provides a scroll bar to navigate along the x axis.
 * 
 * @author Dirk
 */
public class ExpandingPlotterPanel extends AbstractPlotterPanel {

	JPanel scrollClient;
	JScrollPane scrollPane;

	/**
	 * Constructs a new plotter panel. 
	 */
	public ExpandingPlotterPanel(Plotter plotter) {
		super(plotter);

		setBackground(Color.white);

		xAxis = new ExpandingXAxis();
		xAxis.setPreferredSize(new Dimension(100, BOTTOM_MARGIN));
		yAxis = new YAxis();
		yAxis.setPreferredSize(new Dimension(LEFT_MARGIN, 100));

		plotArea = new PlotArea(xAxis, yAxis);

		scrollClient = new MyScrollClient();
		scrollClient.setLayout(new BorderLayout());
		scrollClient.add(xAxis, BorderLayout.SOUTH);
		scrollClient.add(plotArea, BorderLayout.CENTER);

		scrollClient.setBackground(Color.white);

		scrollPane = new JScrollPane(scrollClient);
		scrollPane.setRowHeaderView(yAxis);
		scrollPane.getHorizontalScrollBar().addAdjustmentListener(new FreakScrollBarAdjustmentListener(scrollPane.getHorizontalScrollBar(), true));

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(yAxis, BorderLayout.WEST);

		flush();
	}

	public void flush() {
		scrollClient.setPreferredSize(new Dimension((int) ((view.xAxisMax - view.xAxisMin) * view.constantGenXSpace), scrollClient.getHeight()));

		scrollClient.revalidate();
		repaint();
	}

	int computeDisplayXCoordinate(int x) {
		return (int) ((x - view.xAxisMin) * view.constantGenXSpace);
	}

	public int getStartingGenerations() {
		if (view.getPropertyNumberOfDisplayedValues().intValue() == 0) {
			return (int) (scrollPane.getWidth() / view.constantGenXSpace) + 1;
		} else {
			return view.getPropertyNumberOfDisplayedValues().intValue();
		}
	}

	class ExpandingXAxis extends AbstractPlotterPanel.XAxis {

		void computeVisibleBounds() {
			Rectangle visibleBounds = graphics.getClipBounds();
			minVisibleX = view.xAxisMin + (int) (((int)visibleBounds.getMinX()) / view.constantGenXSpace);
			maxVisibleX = Math.min((int) (((int)visibleBounds.getMaxX()) / view.constantGenXSpace) + 1 + view.xAxisMin, view.xAxisMax);

			// draw up to neighbored ticks			
			minVisibleX = Math.max(view.xAxisMin, minVisibleX - xGenStepWidth);
			maxVisibleX = Math.min(view.xAxisMax, maxVisibleX + xGenStepWidth);
		}

	}

	class ExpandingPlotArea extends AbstractPlotterPanel.PlotArea {

		public ExpandingPlotArea(XAxis xAxis, YAxis yAxis) {
			super(xAxis, yAxis);
		}

		void computeVisibleBounds() {
			Rectangle visibleBounds = graphics.getClipBounds();
			minVisibleX = (int) (((int)visibleBounds.getMinX()) / view.constantGenXSpace);
			maxVisibleX = Math.min((int) (((int)visibleBounds.getMaxX()) / view.constantGenXSpace) + 1, view.xAxisMax);
		}

	}

	/**
	 * A scrolling client implementing the <code>Scrollable</code> interface.
	 * 
	 * @author Dirk
	 */
	class MyScrollClient extends JPanel implements Scrollable {

		public Dimension getPreferredScrollableViewportSize() {
			return null;
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return xAxis.xGenStepWidth;
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return (int)visibleRect.getWidth();
		}

		public boolean getScrollableTracksViewportWidth() {
			return false;
		}

		public boolean getScrollableTracksViewportHeight() {
			return true;
		}

	}

}