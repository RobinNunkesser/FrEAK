/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 07/11/2007
 */

package freak.core.control;

import freak.*;
import freak.core.control.Actions.*;
import java.io.*;

/**
 * @author  Heiko, Stefan
 */
public class BatchProcessor implements StateListener {
	private RunControl runControl;

	private File loadFile;
	private File saveFile;
	private File[] tempFiles;
	private int nextTempFile;

	private long lastSave;
	private long saveInterval;

	public BatchProcessor(File loadFile, File saveFile, File[] tempFiles, long saveInterval) {
		this.loadFile = loadFile;
		this.saveFile = saveFile;
		this.tempFiles = tempFiles;
		this.saveInterval = saveInterval;
	}

	public void run() throws FileNotFoundException, IOException, ClassNotFoundException {
		runControl = new RunControl(this);

		Freak.debug("Trying to read " + loadFile, 2);
		FreakFile file = FreakFile.read(new FileInputStream(loadFile));
		runControl.fromFile(file);
		lastSave = System.currentTimeMillis();
		Freak.debug("Read successfull " + loadFile, 2);

		Freak.debug("Starting " + loadFile, 2);
		runControl.request(new Actions.StartAction());
	}

	public void asynchroneousFeedback(Schedule schedule, Replay replay) {
		if (tempFiles == null) return;

		try {
			long now = System.currentTimeMillis();
			if (now - lastSave > saveInterval) {
				FreakFile currentState = new FreakFile(replay, schedule);
				File tempFile = tempFiles[nextTempFile];
				Freak.debug("Writing backup to " + tempFile, 1);
				currentState.write(new FileOutputStream(tempFile));
				
				nextTempFile = (nextTempFile + 1) % tempFiles.length;
				lastSave = now;
			}
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	public void synchroneousFeedback(Schedule activeSchedule, Replay replay) {
	}

	public void simulationCompleted(Action lastProcessed) {
		Freak.debug("Run ended " + loadFile, 2);
		
		try {	
			if (saveFile != null) {
				FreakFile save = runControl.toFile();
				save.write(new FileOutputStream(saveFile));
				Freak.debug("Results written to " + saveFile, 1);
			}
		} catch (IOException exc) {
			exc.printStackTrace();
		} finally {
			// Cleanly terminate run control
			runControl.request(new Actions.TerminateAction());
		}
	}

	public void simulationException(Exception exc) {
		saveFile = null; // results may be invalid
		exc.printStackTrace();
	}
	
	public void terminated(Action lastProcessedBeforeTermination) {
		Freak.debug("Bye", 4);
		// Forcibly stop event queue
		System.exit(0);
	}
}