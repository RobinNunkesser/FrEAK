/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.view.plotter;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for the plotter adjusting its size automatically so that it
 * fits into the current window in x direction.
 * 
 * @author Dirk
 */
public class AdjustingPlotterPanel extends AbstractPlotterPanel {

	/**
	 * Constructs a new plotter. 
	 */
	public AdjustingPlotterPanel(Plotter plotter) {
		super(plotter);

		setBackground(Color.white);

		xAxis = new XAxis();
		xAxis.setPreferredSize(new Dimension(100, BOTTOM_MARGIN));
		yAxis = new YAxis();
		yAxis.setPreferredSize(new Dimension(LEFT_MARGIN, 100));

		plotArea = new PlotArea(xAxis, yAxis);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BorderLayout());
		subPanel.add(xAxis, BorderLayout.SOUTH);
		subPanel.add(plotArea, BorderLayout.CENTER);

		subPanel.setBackground(Color.white);
		subPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		setLayout(new BorderLayout());
		add(subPanel, BorderLayout.CENTER);
		add(yAxis, BorderLayout.WEST);
	}

	int computeDisplayXCoordinate(int x) {
		return (int) ((x - view.xAxisMin) * plotArea.getWidth() / (double) (view.xAxisMax - view.xAxisMin));
	}

	public void flush() {
		repaint();
	}

	public int getStartingGenerations() {
		if (view.getPropertyNumberOfDisplayedValues().intValue() == 0) {
			return 100;
		} else {
			return view.getPropertyNumberOfDisplayedValues().intValue();
		}
	}

}