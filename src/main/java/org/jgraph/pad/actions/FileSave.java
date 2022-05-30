package org.jgraph.pad.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import org.jgraph.GPGraphpad;
import org.jgraph.pad.GPDocument;
import org.jgraph.pad.GPGraph;

/**
 * Action opens a dialog to select the file.
 * After that the action saves the current graph to
 * the selected file.
 *
 * @author sven.luzar
 *
 */
public class FileSave extends AbstractActionFile {

	/**
	 * Constructor for FileSave.
	 * @param graphpad
	 * @param name
	 */
	public FileSave(GPGraphpad graphpad)
	{
		super(graphpad);
	}

	/** Invokes the export model save function(s)
	 */
	public void actionPerformed(ActionEvent e)
	{
		GPDocument doc = getCurrentDocument();
		URL filename = doc.getFilename();
		GPGraph gpGraph = doc.getGraph();

		doc.getExportModel().saveFile( graphpad, doc, filename, gpGraph);
	}

}
