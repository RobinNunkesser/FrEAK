package org.jgpd.io.jbpm.io;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.jgraph.GPGraphpad;
import org.jgraph.pad.GPDocument;
import org.jgraph.pad.GPFileChooser;
import org.jgraph.pad.GPGraph;
import org.jgraph.pad.GraphModelFileFormat;
import org.jgraph.pad.GraphModelProviderRegistry;
import org.jgraph.pad.actions.AbstractActionFile;
import org.jgraph.pad.resources.Translator;

/**
 * Action opens a dialog to select the file.
 * After that the action saves the current graph to
 * the selected file.
 *
 * @author sven.luzar
 *
 */
public class FileSaveJbpm extends AbstractActionFile
{
	protected GPDocument document;
	protected URL filename;
	protected GPGraph graph;
	
	/**
	 * Constructor for FileSave.
	 * @param graphpad
	 * @param name
	 */
	public FileSaveJbpm(GPGraphpad graphpad,
						GPDocument doc,
						URL file,
						GPGraph gpGraph)
	{
		super(graphpad);
		
		document = doc;
		filename = file;
		graph = gpGraph;
	}

	/** Shows a file chooser if the filename from the
	 *  document is null. Otherwise the method
	 *  uses the file name from the document.
	 *
	 *  Furthermore the method uses the registered
	 *  file formats for the save process.
	 */
	public void actionPerformed(ActionEvent e)
	{
	}
	
	public void saveFile()
	{
		// +TODO check if file already exists, if so, then prompt warning "There is already a file by this name, do you want to replace the existing file?  If you select Yes, all contents of the existing file will be permanently lost."; if No then reopen filechooser+
		URL file;
		if (filename == null) {
			// show the file chooser
			GPFileChooser chooser = new GPFileChooser(document);
			chooser.setDialogTitle(Translator.getString("FileSaveAsLabel"));

			int result = chooser.showSaveDialog(graphpad);
			if (result == JFileChooser.CANCEL_OPTION)
				return;

			// get the file format
			FileFilter fileFilter = chooser.getFileFilter();

			try {
				file = chooser.getSelectedFile().toURL();
				/*
				 * Bug Fix: do not add jgx by default!
				 * use the file extension from the 
				 * file format see (1) 
				 * 
				 if (file.toString().indexOf(".") < 0)
				 file = new URL(file+".jgx");
				 */
			} catch (MalformedURLException eurl) {
				JOptionPane.showMessageDialog(
						graphpad,
						eurl.getLocalizedMessage(),
						Translator.getString("Error"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			GraphModelFileFormat graphModelFileFormat =
				GraphModelProviderRegistry.getGraphModelFileFormat(
						file.toString());

			// if no file format was found try to specify the
			// file format by using the file filter
			if (graphModelFileFormat == null) {
				graphModelFileFormat =
					GraphModelProviderRegistry.getGraphModelFileFormat(
							fileFilter);
				try {
					// (1) if the file has no file extension
					// add the correct file extension
					file =
						new URL(
								file.toString()
								+ "."
								+ graphModelFileFormat.getFileExtension());
				} catch (MalformedURLException eurl) {
					JOptionPane.showMessageDialog(
							graphpad,
							eurl.getLocalizedMessage(),
							Translator.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}

			// sets the writeProperties
			// for the next write process
			if (chooser.getAccessory() != null)
				graph.setWriteProperties(
						graphModelFileFormat.getWriteProperties(
								chooser.getAccessory()));

			// memorize the filename
			filename = file;

		} else {
			// exract the file object
			file = filename;
		}

		// exract the provider and the file format
		//		GraphModelProvider graphModelProvider =
		//			GraphModelProviderRegistry.getGraphModelProvider(file);
		GraphModelFileFormat graphModelFileFormat =
			GraphModelProviderRegistry.getGraphModelFileFormat(file.toString());

		// extract the writer properties
		Hashtable props = graph.getWriteProperties();

		// try to write the file
		try {
			graphModelFileFormat.write(
					file,
					props,
					graph,
					graph.getModel());
			document.setFilename(filename);
			document.setModified(false);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(
					graphpad,
					ex.getLocalizedMessage(),
					Translator.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
		} finally {
			graphpad.update();
			graphpad.invalidate();
		}

	}

}
