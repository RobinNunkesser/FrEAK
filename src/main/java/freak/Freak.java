/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 07/12/2007
 */

package freak;

import freak.core.control.*;
import freak.core.modulesupport.*;
import freak.gui.*;
import freak.gui.runframe.*;
import freak.gui.scheduleeditor.ScheduleEditor;
import freak.rinterface.control.LogRegInterface;
import freak.rinterface.control.RFreak;
import jargs.gnu.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import javax.swing.*;

/**
 * Main entry point for FrEAK. This class contains the main method and platform dependent bug fix setup.
 * @author  Stefan, Matthias
 */
public class Freak {
//	private static boolean logReg=true;
	private static boolean circumventR=false;
	private static int actionModifierKey;
	
	/**
	 * DEBUG_LEVEL indicats how much debugging information is printed by FrEAK.
	 * Higher values of DEBUG_LEVEL produce more output.
	 * Set DEBUG_LEVEL = 0 to suppress any output.  
	 */
	private static int DEBUG_LEVEL=0;
	
	static AbstractRunFrame f;
	
	public static void viewFilenameInTitle(String s)
	{
		if (f!=null) f.viewFilenameInTitle(s);
	}
        	
	public static void main(String[] arguments) throws Exception {
		// processArguments	
		File loadFile = null;
		File saveFile = null;
		File[] tempFiles = null;
		
		CmdLineParser parser = new CmdLineParser();
		
		CmdLineParser.Option fileOption = parser.addStringOption('f', "temp-file-base");
		CmdLineParser.Option intervalOption = parser.addIntegerOption('i', "temp-file-interval");
		CmdLineParser.Option debugOption = parser.addIntegerOption('d', "debug-level");

		CmdLineParser.Option startEditorOption = parser.addStringOption('e', "edit-schedule");
		CmdLineParser.Option startEditorSaveOption = parser.addStringOption('s', "save-edited-schedule");

		
		try {
			parser.parse(arguments);
		} catch (CmdLineParser.IllegalOptionValueException e) {
			printUsage();
			return;
		} catch (CmdLineParser.UnknownOptionException e) {
			printUsage();
			return;
		}
		
		String scheduleToEdit = (String) parser.getOptionValue(startEditorOption);
		String saveEditedSchedule = (String) parser.getOptionValue(startEditorSaveOption);

		if (scheduleToEdit != null) {
			circumventR=true;
			if (saveEditedSchedule != null) ScheduleEditor.setRSaveTo(saveEditedSchedule);
			if (scheduleToEdit.equals("NULL")) {
				RFreak.showScheduleEditor();
			} else {
				RFreak.showScheduleEditor(scheduleToEdit);				
			}
			return;
		}

		String tempFileBase = (String) parser.getOptionValue(fileOption);
		if (tempFileBase != null) {
			File tempFile1 = new File(tempFileBase + "1.freak");
			File tempFile2 = new File(tempFileBase + "2.freak");
			tempFiles = new File[]{tempFile1, tempFile2};
		}
		
		DEBUG_LEVEL = (parser.getOptionValue(debugOption) != null) ? ((Integer)parser.getOptionValue(debugOption)).intValue() : 0;
		long interval = 1000 * ((parser.getOptionValue(debugOption) != null) ? ((Integer)parser.getOptionValue(intervalOption)).intValue() : 3600);
		
		arguments = parser.getRemainingArgs();
		if (arguments.length > 0) {
			loadFile = new File(arguments[0]); 
		}
		if (arguments.length > 1) {
			saveFile = new File(arguments[1]);
		}
		if (arguments.length > 2) {
			printUsage();
			
			return;
		}
		
		// Launch FrEAK
		Locale.setDefault(Locale.US);

		// The method init is called in order to build up a list of all
		// available persistence handlers. 
		PersistenceManager.init();
		
		if (loadFile == null) {
			// Launch GUI mode
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

						String lookAndFeel = UIManager.getLookAndFeel().getName();

						actionModifierKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
						if (lookAndFeel.equals("Mac OS X Aqua")) JButtonFactory.activateBugFix();

						//RunFrame f = new RunFrame();
/*						if (logReg) {
							f = new RunFrameLR();
						} else {*/
							f = new RunFrame();
							
//						}
						f.setLocationRelativeTo(null);
						f.setVisible(true);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			});
		} else {
			// Launch batch mode
/*			if (logReg) {
				LogRegInterface batchProcessor = new LogRegInterface(loadFile, saveFile, tempFiles, interval);
				batchProcessor.run();
			} else {*/
				BatchProcessor batchProcessor = new BatchProcessor(loadFile, saveFile, tempFiles, interval);
				batchProcessor.run();				
//			}
		}
	}
	
	private static void printUsage() {
		System.out.println(
			"freak [options]\n" + 
			"freak [options] loadSchedule\n" + 
			"freak [options] loadSchedule saveSchedule\n" + 
			"\n" + 
			"Options:\n" + 
			"  -d, --debug-level:           verbosity; default = 4" +
			"  -f, --temp-file-base:        start of file name for backup files" + 
			"  -i, --temp-file-interval:    backup interval in seconds; default = 3600"
		);
	}
	
	/**
	 * @return  the actionModifierKey
	 * @uml.property  name="actionModifierKey"
	 */
	public static int getActionModifierKey() {
		return actionModifierKey;
	}
	
	/**
	 * Outputs the <code>message</code> to <code>System.out</code> 
	 * if <code>level</code> is lower than <code>DEBUG_LEVEL</code>.
	 * 
	 * @param message the message to print.
	 * @param level indicates how critical the message is. Higher value menas less critical. Must be greater or equal to 1.
	 */
	public static void debug(String message, int level) {
		if (level <= DEBUG_LEVEL) {
			System.out.println(message);
		}
	}

	/**
	 * @param debug_level the dEBUG_LEVEL to set
	 */
	public static void setDebugLevel(int debug_level) {
		DEBUG_LEVEL = debug_level;
	}

	/**
	 * @return the circumventR
	 */
	public static boolean isCircumventR() {
		return circumventR;
	}
}
