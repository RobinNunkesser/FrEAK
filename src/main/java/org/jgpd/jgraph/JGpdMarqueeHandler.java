/*
 *
 *
 * Copyright (C) 2003-2004 David Benson
 *
 * JGpd is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGpd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGpd; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgpd.jgraph;

import org.jgpd.io.ModelExportInterface;
import org.jgraph.pad.GPMarqueeHandler;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.jgraph.GPGraphpad;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.pad.GraphModelProvider;
import org.jgraph.pad.resources.Translator;

public class JGpdMarqueeHandler extends GPMarqueeHandler {

	public JGpdMarqueeHandler(GPGraphpad graphpad) {
		super(graphpad);
	}




	public void addActivity(Rectangle bounds) {

		Map viewMap = new Hashtable();
		Map map;
		GraphModelProvider gmp = graphpad.getCurrentGraphModelProvider();
		GraphModel model = graphpad.getCurrentGraph().getModel();

        // If bounds is smaller than the default activity
        // ensure the default dimensions are used.
        int width = bounds.width;
        int height = bounds.height;

        if ( bounds.getWidth() < ActivityView.default_width)
        {
        	width = ActivityView.default_width;
        }
		if ( bounds.getHeight() < ActivityView.default_height)
		{
			height = ActivityView.default_height;
		}

		bounds.setSize(width,height);

		map = GraphConstants.createMap();
		GraphConstants.setBounds(map, bounds);
		GraphConstants.setOpaque(map, true);
	    GraphConstants.setBackground(map, Color.cyan);
		GraphConstants.setBorderColor(map, Color.black);
		String fontName = Translator.getString("FontName");
		try {
			int fontSize = Integer.parseInt(Translator.getString("FontSize"));
			int fontStyle = Integer.parseInt(Translator.getString("FontStyle"));
			GraphConstants.setFont(
				map,
				new Font(fontName, fontStyle, fontSize));
		} catch (Exception e) {
			// handle error
		}
		List toInsert = new LinkedList();

		// Obtain current export model
		ModelExportInterface exportModel = graphpad.getExportModel();
		Object userObject = null;
		if ( exportModel != null )
		{
			// Create end object in model and associate
			// with this cell as a userObject
			userObject = exportModel.createActivity();
		}

		Object cell =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_VERTEX_ACTIVITY,
				userObject,
				map);

		viewMap.put(cell, map);
		toInsert.add(cell);

		// Create Ports
		int u = GraphConstants.PERMILLE;
		Object port;

        // Work out where middle of arcs are in relative width and height
        int arcSize = ActivityView.getArcSize(bounds.width,bounds.height);

        // Remember to use floating maths.  0.293 is (1-(1/sqrt(2))) which
        // is the proportion a quarter circle arc is from the closer edges
        // of a bounding square at the mid-point of the arc.

        double hortizontalPortOffset = (double)(0.293*arcSize)/(2*bounds.width);
        double verticalPortOffset = (double)(0.293*arcSize)/(2*bounds.height);
		// Floating Center Port
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Center",
				null);
		gmp.addPort(cell, port);
		//port = new DefaultPort("Center");
		//cell.add(port);
		toInsert.add(port);

		// Top Left
		//port = new DefaultPort("Topleft");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(
			map,
			new Point((int) (u * hortizontalPortOffset), (int) (u * verticalPortOffset)));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Topleft",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Top Center
		// port = new DefaultPort("Topcenter");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(map, new Point((int) (u / 2), 0));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Topcenter",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Top Right
		// port = new DefaultPort("Topright");
		// cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(
			map,
			new Point( (int) (u * (1-hortizontalPortOffset)), (int) (u * verticalPortOffset) ) );
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Topright",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Top Center
		//port = new DefaultPort("Middleleft");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(map, new Point(0, (int) (u / 2)));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Middleleft",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Middle Right
		//port = new DefaultPort("Middleright");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(map, new Point(u, (int) (u / 2)));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Middleright",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Bottom Left
		//port = new DefaultPort("Bottomleft");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(
			map,
			new Point((int) (u * hortizontalPortOffset), (int) (u * (1-verticalPortOffset)) ) );
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Bottomleft",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Bottom Center
		//port = new DefaultPort("Bottomcenter");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(map, new Point((int) (u / 2), u));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Bottomcenter",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Bottom Right
		//port = new DefaultPort("Bottomright");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(
			map,
			new Point((int) (u * (1-hortizontalPortOffset)), (int) (u * (1-verticalPortOffset)) ) );
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Bottomright",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		graphpad.getCurrentGraph().getModel().insert(
			toInsert.toArray(),
			viewMap,
			null,
			null,
			null);
	}


	public void addDecision(Rectangle bounds) {

		Map viewMap = new Hashtable();
		Map map;
		GraphModelProvider gmp = graphpad.getCurrentGraphModelProvider();
		GraphModel model = graphpad.getCurrentGraph().getModel();

		// If bounds is smaller than the default activity
		// ensure the default dimensions are used.
		int width = bounds.width;
		int height = bounds.height;

		if ( bounds.getWidth() < DecisionView.default_width)
		{
			width = DecisionView.default_width;
		}
		if ( bounds.getHeight() < DecisionView.default_height)
		{
			height = DecisionView.default_height;
		}

		bounds.setSize(width,height);

		map = GraphConstants.createMap();
		GraphConstants.setBounds(map, bounds);
		GraphConstants.setOpaque(map, true);
	    GraphConstants.setBackground(map, Color.yellow);
		GraphConstants.setBorderColor(map, Color.black);
		String fontName = Translator.getString("FontName");
		try {
			int fontSize = Integer.parseInt(Translator.getString("FontSize"));
			int fontStyle = Integer.parseInt(Translator.getString("FontStyle"));
			GraphConstants.setFont(
				map,
				new Font(fontName, fontStyle, fontSize));
		} catch (Exception e) {
			// handle error
		}
		List toInsert = new LinkedList();

		// Obtain current export model
		ModelExportInterface exportModel = graphpad.getExportModel();
		Object userObject = null;
		if ( exportModel != null )
		{
			// Create end object in model and associate
			// with this cell as a userObject
			userObject = exportModel.createDecision();
		}

		Object cell =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_VERTEX_DECISION,
				userObject,
				map);

		viewMap.put(cell, map);
		toInsert.add(cell);

		// Create Ports
		int u = GraphConstants.PERMILLE;
		Object port;

		// Floating Center Port
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Center",
				null);
		gmp.addPort(cell, port);
		//port = new DefaultPort("Center");
		//cell.add(port);
		toInsert.add(port);

		// Top Left
		//port = new DefaultPort("Topleft");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(
			map,
			new Point((int) (u / 4), (int) (u / 4)));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Topleft",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Top Center
		// port = new DefaultPort("Topcenter");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(map, new Point((int) (u / 2), 0));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Topcenter",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Top Right
		// port = new DefaultPort("Topright");
		// cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(
			map,
			new Point( (int) (u * 3 / 4), (int) (u / 4) ) );
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Topright",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Top Center
		//port = new DefaultPort("Middleleft");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(map, new Point(0, (int) (u / 2)));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Middleleft",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Middle Right
		//port = new DefaultPort("Middleright");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(map, new Point(u, (int) (u / 2)));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Middleright",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Bottom Left
		//port = new DefaultPort("Bottomleft");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(
			map,
			new Point((int) (u / 4), (int) (u * 3 / 4) ) );
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Bottomleft",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Bottom Center
		//port = new DefaultPort("Bottomcenter");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(map, new Point((int) (u / 2), u));
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Bottomcenter",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		// Bottom Right
		//port = new DefaultPort("Bottomright");
		//cell.add(port);
		map = GraphConstants.createMap();
		GraphConstants.setOffset(
			map,
			new Point((int) (u * 3 / 4), (int) (u * 3 / 4) ) );
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Bottomright",
				map);
		gmp.addPort(cell, port);
		viewMap.put(port, map);
		toInsert.add(port);

		graphpad.getCurrentGraph().getModel().insert(
			toInsert.toArray(),
			viewMap,
			null,
			null,
			null);
	}


	public void addStart(Rectangle bounds)
	{
		int defaultWidth = StartView.defaultWidth;
		int defaultHeight = StartView.defaultHeight;
		Color backgroundColour = Color.green;

		ModelExportInterface exportModel = graphpad.getExportModel();
		Object userObject = exportModel.createStart();

		commonAddCircle( bounds,
						 backgroundColour,
						 defaultHeight,
						 defaultWidth,
						 userObject,
						 GraphModelProvider.CELL_VERTEX_START);
	}


	public void addEnd(Rectangle bounds)
	{
		int defaultWidth = EndView.defaultWidth;
		int defaultHeight = EndView.defaultHeight;
		Color backgroundColour = Color.red;

		ModelExportInterface exportModel = graphpad.getExportModel();
		Object userObject = exportModel.createEnd();

		commonAddCircle( bounds,
						 backgroundColour,
						 defaultHeight,
						 defaultWidth,
						 userObject,
						 GraphModelProvider.CELL_VERTEX_END);
	}

	public void commonAddCircle(Rectangle 	bounds,
								Color    	backgroundColour,
								int			defaultHeight,
								int			defaultWidth,
								Object		userObject,
								int			nodeType)
	{
		Map viewMap = new Hashtable();
		Map map;
		GraphModelProvider gmp = graphpad.getCurrentGraphModelProvider();
		GraphModel model = graphpad.getCurrentGraph().getModel();

		// If bounds is smaller than the default activity
		// ensure the default dimensions are used.
		int width = bounds.width;
		int height = bounds.height;

		if ( bounds.getWidth() < defaultWidth)
		{
			width = defaultWidth;
		}
		if ( bounds.getHeight() < defaultHeight)
		{
			height = defaultHeight;
		}

		bounds.setSize(width,height);

		map = GraphConstants.createMap();
		GraphConstants.setBounds(map, bounds);
		GraphConstants.setOpaque(map, true);
		GraphConstants.setBackground(map, backgroundColour);
		GraphConstants.setBorderColor(map, Color.black);
		String fontName = Translator.getString("FontName");
		try {
			int fontSize = Integer.parseInt(Translator.getString("FontSize"));
			int fontStyle = Integer.parseInt(Translator.getString("FontStyle"));
			GraphConstants.setFont(
				map,
				new Font(fontName, fontStyle, fontSize));
		} catch (Exception e) {
			// handle error
		}
		List toInsert = new LinkedList();

		Object cell =
			gmp.createCell(
				model,
				nodeType,
				userObject,
				map);

		viewMap.put(cell, map);
		toInsert.add(cell);

		// Create Ports
		int u = GraphConstants.PERMILLE;
		Object port;

		// Floating Center Port
		port =
			gmp.createCell(
				model,
				GraphModelProvider.CELL_PORT_DEFAULT,
				"Center",
				null);
		gmp.addPort(cell, port);
		//port = new DefaultPort("Center");
		//cell.add(port);
		toInsert.add(port);

		graphpad.getCurrentGraph().getModel().insert(
			toInsert.toArray(),
			viewMap,
			null,
			null,
			null);

	}


	public void addSplit(Rectangle bounds)
	{
		int defaultWidth = EndView.defaultWidth;
		int defaultHeight = EndView.defaultHeight;
		Color backgroundColour = Color.red;

		ModelExportInterface exportModel = graphpad.getExportModel();
		Object userObject = exportModel.createFork();

		commonAddRectangle( bounds,
				backgroundColour,
				defaultHeight,
				defaultWidth,
				userObject,
				GraphModelProvider.CELL_VERTEX_SPLIT);
	}
	
	
	public void addJoin(Rectangle bounds)
	{
		int defaultWidth = EndView.defaultWidth;
		int defaultHeight = EndView.defaultHeight;
		Color backgroundColour = Color.red;

		ModelExportInterface exportModel = graphpad.getExportModel();
		Object userObject = exportModel.createJoin();

		commonAddRectangle( bounds,
				backgroundColour,
				defaultHeight,
				defaultWidth,
				userObject,
				GraphModelProvider.CELL_VERTEX_JOIN);
	}
	
	
	public void commonAddRectangle(Rectangle 	bounds,
			Color    	backgroundColour,
			int			defaultHeight,
			int			defaultWidth,
			Object		userObject,
			int			nodeType)
	{
		Map viewMap = new Hashtable();
		Map map;
		GraphModelProvider gmp = graphpad.getCurrentGraphModelProvider();
		GraphModel model = graphpad.getCurrentGraph().getModel();

		// If bounds is smaller than the default activity
		// ensure the default dimensions are used.
		int width = bounds.width;
		int height = bounds.height;

		if ( bounds.getWidth() < defaultWidth)
		{
			width = defaultWidth;
		}
		if ( bounds.getHeight() < defaultHeight)
		{
			height = defaultHeight;
		}

		bounds.setSize(width,height);

		map = GraphConstants.createMap();
		GraphConstants.setBounds(map, bounds);
		// FIXME, when an image isn't provided use this colour
//		GraphConstants.setOpaque(map, true);
//		GraphConstants.setBackground(map, backgroundColour);
//		GraphConstants.setBorderColor(map, Color.black);
		String fontName = Translator.getString("FontName");
		try {
			int fontSize = Integer.parseInt(Translator.getString("FontSize"));
			int fontStyle = Integer.parseInt(Translator.getString("FontStyle"));
			GraphConstants.setFont(
					map,
					new Font(fontName, fontStyle, fontSize));
		} catch (Exception e) {
			// handle error
		}
		List toInsert = new LinkedList();

		Object cell =
			gmp.createCell(
					model,
					nodeType,
					userObject,
					map);

		viewMap.put(cell, map);
		toInsert.add(cell);

		// Create Ports
		int u = GraphConstants.PERMILLE;
		Object port;

		// Floating Center Port
		port =
			gmp.createCell(
					model,
					GraphModelProvider.CELL_PORT_DEFAULT,
					"Center",
					null);
		gmp.addPort(cell, port);
		//port = new DefaultPort("Center");
		//cell.add(port);
		toInsert.add(port);

		graphpad.getCurrentGraph().getModel().insert(
				toInsert.toArray(),
				viewMap,
				null,
				null,
				null);

	}
}