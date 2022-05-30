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

package org.jgpd.io.jbpm.io;

import java.awt.event.ActionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.MalformedURLException;

import org.jgpd.io.jbpm.ModelExportJBpm;
import org.jgraph.GPGraphpad;
import org.jgraph.pad.actions.AbstractActionFile;
import org.jgraph.pad.resources.Translator;
import org.jgraph.pad.GPDocument;
import org.jgraph.pad.GPGraph;

public class FileExportJBPMProcessXML extends AbstractActionFile {
	
	/* File type to pass to ImageIO. Default is "xml". */
	protected transient String fileType = "xml";
	
	/**
	 * Constructor for FileExportJBPM.
	 * @param graphpad
	 */
	public FileExportJBPMProcessXML(GPGraphpad graphpad) {
		this(graphpad, "xml");
	}
	
	public FileExportJBPMProcessXML(GPGraphpad graphpad, String fileType) {
		super(graphpad);
		this.fileType = fileType;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		GPDocument doc = getCurrentDocument();
		GPGraph gpGraph = doc.getGraph();
		URL file;
		String jbpm_xml = "processdefinition";
		
		// show the file chooser
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(Translator.getString("FileSaveAsLabel"));
		
		int result = chooser.showSaveDialog(graphpad);
		if (result == JFileChooser.CANCEL_OPTION)
			return;
		
		// get the file format
		FileFilter fileFilter = chooser.getFileFilter();
		
		try {
			file = chooser.getSelectedFile().toURL();

			if (file.toString().indexOf(".") < 0)
			{
				file = new URL(file+".xml");
			}
			
		} catch (MalformedURLException eurl) {
			JOptionPane.showMessageDialog(
					graphpad,
					eurl.getLocalizedMessage(),
					Translator.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try
		{		
			if (getCurrentDocument().getExportModel() instanceof ModelExportJBpm)
			{
				writeFile((ModelExportJBpm)(getCurrentDocument().getExportModel()),
						gpGraph,
						file);
			}
		} catch (Exception ex)
		{
			JOptionPane.showMessageDialog(
					graphpad,
					ex.getLocalizedMessage(),
					Translator.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void writeFile(	ModelExportJBpm model,
							GPGraph graph,
							URL file) throws Exception
	{
		OutputStream out = null;
		out = new FileOutputStream(file.getFile());
		out = new BufferedOutputStream(out);
		out.write((model.exportModelXML(graph)).getBytes());
		out.flush();
		out.close();
		
	}
}