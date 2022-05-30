/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: $Date: 2008/09/04 06:58:15 $
 */

package freak.rinterface.control;

import jargs.gnu.CmdLineParser;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import freak.Freak;
import freak.core.control.BatchProcessor;
import freak.core.control.FreakFile;
import freak.core.control.Schedule;
import freak.core.modulesupport.PersistenceManager;
import freak.gui.JButtonFactory;
import freak.gui.graph.EditorDialog;
import freak.gui.runframe.AbstractRunFrame;
import freak.gui.runframe.RunFrame;
import freak.gui.runframe.RunFrameLR;
import freak.gui.scheduleeditor.ScheduleEditor;

/**
 * Main entry point for the R Interface to FrEAK. Based upon freak/Freak.java by Stefan and Matthias
 * @author  Dominic
 */
public class RFreak {

    private static boolean logReg = true;
    private static int actionModifierKey;

    //###nur temporaer:
    //public static boolean thisisafield;
    //public boolean thismightbeafieldaswell; 	//in fact, fields muessen nicht notewendigerweise statisch sein,
    //public reicht (geprueft (zumindest fuer Objekte, bei Klassen muessen
    //die Felder sicherlich statisch sein (noch nicht geprueft)))

    /**
     * DEBUG_LEVEL indicates how much debugging information is printed by FrEAK.
     * Higher values of DEBUG_LEVEL produce more output.
     * Set DEBUG_LEVEL = 0 to suppress any output.  
     */
    private static int DEBUG_LEVEL = 0;

    static AbstractRunFrame f;

    public static void viewFilenameInTitle(String s) {
        if (f != null) f.viewFilenameInTitle(s);
    }

    public static void main(String[] arguments) throws Exception {

        System.out.println("der richtige Launcher");

        //showEditorDialog();
        //showScheduleEditor();

        /*//vom alten Projekt
        //Test:
        RFreak rf = new RFreak();
        rf.rMain(arguments);
        */

    }

    public static void showEditorDialog() {
        //method is called from R to bring up the Graph Editor

        // first create default Schedule
        Schedule schedule = freak.rinterface.model.ScheduleConfigurator.createDefaultSchedule();

        java.awt.Frame frame = new java.awt.Frame();
        java.awt.Dialog dialog = new java.awt.Dialog(frame);

        // then launch the editor
        EditorDialog gred = new EditorDialog(dialog, schedule);
        gred.setVisible(true);

    }

    public static void showEditorDialog(Schedule schedule) {

        java.awt.Frame frame = new java.awt.Frame();
        java.awt.Dialog dialog = new java.awt.Dialog(frame);

        // then launch the editor
        EditorDialog gred = new EditorDialog(dialog, schedule);
        gred.setVisible(true);

    }

    public static void exactDNF() {
        Schedule schedule = freak.rinterface.model.ScheduleConfigurator.createDefaultSchedule();
        ExactDNF.doComputation(schedule);
    }

    public static void showScheduleEditor() {
        //method is called from R to bring up the Schedule Editor
        //parameterless version creates a default Schedule

        // first create default Schedule
        Schedule schedule = freak.rinterface.model.ScheduleConfigurator.createDefaultSchedule();

        showScheduleEditor(schedule);

    }

    public static void showScheduleEditor(String schedulePath) {
        //method is called from R to bring up the Schedule Editor
        //use the passed schedule

        RunFrame runframe = new RunFrame();

        Schedule schedule = freak.rinterface.model.ScheduleConfigurator.createDefaultSchedule();

        try {
            (runframe).runControl.fromFile(FreakFile.read(new FileInputStream(schedulePath)));
            schedule = runframe.runControl.getSchedule();
        } catch (Exception exc) {
            schedule = freak.rinterface.model.ScheduleConfigurator.createDefaultSchedule();
            System.out.println("Error loading " + schedulePath + ". Will use a default schedule.");
        }

        runframe.runControl.setNewSchedule(schedule);

        //create the ScheduleEditor
        ScheduleEditor sced = new ScheduleEditor(runframe, schedule);
        sced.setRMode();

        //and pass it to the RunFrame
        runframe.setScheduleEditor(sced);
        runframe.runControl.setNewSchedule(schedule);
        //		ScheduleConfigurator.setEditingFinished(false);
        sced.setVisible(true);

        //		while (!(ScheduleConfigurator.isEditingFinished()));
    }

    public static void showScheduleEditor(Schedule schedule) {
        //method is called from R to bring up the Schedule Editor
        //use the passed schedule

        RunFrame runframe = new RunFrame();
        runframe.runControl.setNewSchedule(schedule);

        //create the ScheduleEditor
        ScheduleEditor sced = new ScheduleEditor(runframe, schedule);
        sced.setRMode();

        //and pass it to the RunFrame
        runframe.setScheduleEditor(sced);
        runframe.runControl.setNewSchedule(schedule);
        //		ScheduleConfigurator.setEditingFinished(false);
        sced.setVisible(true);
        //		while (!(ScheduleConfigurator.isEditingFinished()));
    }

    /*	public static void showScheduleEditor(Schedule schedule,String saveTo) {
    		showScheduleEditor(schedule);
    	}*/

    /*	public static void rMain() throws Exception {
    		rMain(null);
    	}*/

    /*	public static String rjiGPASInteractions(double[] resp, double[] bin, int runs, int generations, String savegraph  ){
    		int occurences=10;
    		double ratio=0.1;
    		
    	//	ScheduleConfigurator.createSchedule(jTextField.getText(),runs,generations,"./export/resource/freak/module/graph/booleanfunction/ZuFaulZumKlicken.fop");
    	//	ScheduleConfigurator.setInteraction(runs,jTextField4.getText(),occurences,ratio,savegraph);
    	//	Schedule schedule=ScheduleConfigurator.getCurrentSchedule();
    		return "Test erfolgreich";
    		
    	}*/

    public static boolean isStartable() {
        return (!(System.getProperty("java.vm.name").equalsIgnoreCase("Java HotSpot(TM) Server VM")));
    }

    public static void rMain(String[] arguments) throws Exception {
        // processArguments	
        File loadFile = null;
        File saveFile = null;
        File[] tempFiles = null;

        CmdLineParser parser = new CmdLineParser();

        CmdLineParser.Option fileOption = parser.addStringOption('f', "temp-file-base");
        CmdLineParser.Option intervalOption = parser.addIntegerOption('i', "temp-file-interval");
        CmdLineParser.Option debugOption = parser.addIntegerOption('d', "debug-level");

        try {
            parser.parse(arguments);
        } catch (CmdLineParser.IllegalOptionValueException e) {
            printUsage();
            return;
        } catch (CmdLineParser.UnknownOptionException e) {
            printUsage();
            return;
        }

        String tempFileBase = (String) parser.getOptionValue(fileOption);
        if (tempFileBase != null) {
            File tempFile1 = new File(tempFileBase + "1.freak");
            File tempFile2 = new File(tempFileBase + "2.freak");
            tempFiles = new File[] { tempFile1, tempFile2 };
        }

        DEBUG_LEVEL = (parser.getOptionValue(debugOption) != null) ? ((Integer) parser.getOptionValue(debugOption))
                .intValue() : 4;
        long interval = 1000 * ((parser.getOptionValue(debugOption) != null) ? ((Integer) parser
                .getOptionValue(intervalOption)).intValue() : 3600);

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
                        if (logReg) {
                            f = new RunFrameLR();
                        } else {
                            f = new RunFrame();

                        }
                        f.setLocationRelativeTo(null);
                        f.setVisible(true);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            });
        } else {
            // Launch batch mode
            if (logReg) {
                LogRegInterface batchProcessor = new LogRegInterface(loadFile, saveFile, tempFiles, interval);
                batchProcessor.run();
            } else {
                BatchProcessor batchProcessor = new BatchProcessor(loadFile, saveFile, tempFiles, interval);
                batchProcessor.run();
            }
        }
        return;
    }

    private static void printUsage() {
        System.out.println("freak [options]\n" + "freak [options] loadSchedule\n"
                + "freak [options] loadSchedule saveSchedule\n" + "\n" + "Options:\n"
                + "  -d, --debug-level:           verbosity; default = 4"
                + "  -f, --temp-file-base:        start of file name for backup files"
                + "  -i, --temp-file-interval:    backup interval in seconds; default = 3600");
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
}
