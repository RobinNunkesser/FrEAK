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
import freak.core.modulesupport.*;
import freak.core.modulesupport.inspector.*;
import freak.core.view.swingsupport.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.io.*;

/**
 * An extension of <code>StdView</code> that writes the displayed data into a file.
 * @author  Dirk, Stefan
 */
public class FileWriter extends StdView implements Configurable, CustomInspectable {
	// a counter used to generate unique default file names
	private static int fileNameCounter = 0;

	private File file;
	private long writePosition;
	private transient RandomAccessFile out;

	private FreakTitledBorderModel borderModel = new FreakTitledBorderModel();

	private boolean recreate;

	/**
	 * Constructs a new <code>FileWriter</code>.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public FileWriter(ScheduleInterface schedule) {
		super(schedule);

		setPropertyFile(new File(""));
	}

	public String getDescription() {
		return "Writes textual data into a file.";
	}

	public String getName() {
		return "File Writer";
	}

	public void update(Object o) {
		super.update(o);

		if (recreate) {
			recreate = false;
			
			try {
				out = new RandomAccessFile(file, "rw");
				out.seek(writePosition);
			} catch (IOException exc) {
				out = null;
			}
		}

		if (out != null) {
			String newText = extractTextFromObject(o);
			try {
				out.write(encode(newText + "\n"));
				writePosition = out.getFilePointer();
				setText(newText);
			} catch (IOException exc) {
				out = null;
				setText("Error writing to file " + file + ".\nNo data is being written.");
			}
		} else {
			setText("Error writing to file " + file + ".\nNo data is being written.");
		}
	}

	public JPanel createPanel() {
		// TODO Stefan may be headless
		recreate = true;

		JPanel panel = super.createPanel();
		TitledBorder border = new TitledBorder("");
		panel.setBorder(border);
		borderModel.setView(border);
		return panel;
	}

	/**
	 * Sets the file the output is to be written.
	 * 
	 * @param newFile a <code>File</code> object.
	 */
	public void setPropertyFile(File newFile) {
		if (newFile.equals(file))
			return;

		file = newFile;
		if (file.getName().equals("")) {
			file = createNewFile();
			borderModel.setTitle("Writing to auto-generated file");
		} else {
			borderModel.setTitle("Writing to " + file);
		}
		borderModel.flush();

		writePosition = 0;
		recreate = true;
	}

	/**
	 * Returns the file the output is written to.
	 */
	public File getPropertyFile() {
		return file;
	}

	public String getShortDescriptionForFile() {
		return "Log file";
	}

	public String getLongDescriptionForFile() {
		return "The name of the log file the output is to be written.";
	}

	public void finalize() throws Throwable {
		super.finalize();
		if (out != null)
			out.close();
	}

	public Inspector getInspector() {
		CustomizableInspector inspector = (CustomizableInspector)super.getInspector();
		inspector.customize(File.class, new JTable().getDefaultRenderer(String.class), new FileEditor(FileEditor.SAVE));
		return inspector;
	}

	private static File createNewFile() {
		File result = null;
		do {
			String fileName = String.valueOf(fileNameCounter);
			while (fileName.length() < 4)
				fileName = "0" + fileName;
			fileName = "data" + fileName + ".log";

			result = new File(fileName);
			fileNameCounter++;
		}
		while (result.exists());

		return result;
	}

	private static byte[] encode(String data) {
		try {
			ByteArrayOutputStream sink = new ByteArrayOutputStream();
			OutputStreamWriter push = new OutputStreamWriter(sink);
			push.write(data);
			push.close();
			return sink.toByteArray();
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}
}
