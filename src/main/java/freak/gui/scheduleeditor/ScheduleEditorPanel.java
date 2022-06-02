/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: $Date: 2007/11/28 13:41:46 $
 */

package freak.gui.scheduleeditor;

import freak.core.control.*;
import freak.core.modulesupport.*;

import javax.swing.*;

/**
 * An abstract JPanel for use in the <code>ScheduleEditor</code>. It contains links to useful classes that nearly every Panel will need. The <code>ScheduleEditor</code> communicates with <code>ScheduleEditorPanels</code> the following way: - to display a panel, it calls the <code>enter()</code> method which calls <code>initForStartedSchedule()</code> if the schedule did already start. This method should disable the components that are not meant to be changed after the schedule started. - to change panels, it asks if it is ok to leave the current panel by issuing <code>mayLeave()</code>. This method should warn the user and return <code>false</code> if needed configuration steps aren't done yet. Then the new panel is created and shown via <code>enter()</code>. If the new panel is a successor of this panel, <code>mayAdvance()</code> is called to see if it is syntactically correct to change to a later panel.
 * @author  Oliver
 */
public abstract class ScheduleEditorPanel extends JPanel {

	protected ScheduleInterface schedule;
	protected ScheduleEditor scheduleEditor;
	protected ScheduleDependencyChecker scheduleDependencyChecker;
	protected ModuleCollector moduleCollector;
	protected EventController eventController;

	/**
	 * Constructs a new ScheduleEditorPanel and initializes its private fields
	 * from the owning <code>ScheduleEditor</code>.
	 * @param scheduleEditor the owning <code>ScheduleEditor</code>
	 */
	public ScheduleEditorPanel(ScheduleEditor scheduleEditor) {
		super();
		this.scheduleEditor = scheduleEditor;
		schedule = scheduleEditor.getSchedule();
		scheduleDependencyChecker = scheduleEditor.getScheduleDependencyChecker();
		moduleCollector = new ModuleCollector(schedule);
		eventController = schedule.getEventController();
	}

	/**
	 * Some things should not be allowed to change after a schedule starts. This
	 * method is called to disable components that may not be used in this case.
	 * You have to overwrite this method if your panel wants to react differently
	 * on an already started schedule.
	 */
	protected void initForStartedSchedule() {
	}

	/**
	 * This method is called whenever a <code>ScheduleEditorPanel</code> is created
	 * to be shown. Component-initialization should go here, for example filling
	 * a <code>JList</code> with possible options, setting text components, and so on.
	 */
	public void enter() {
		if (Util.scheduleStarted(schedule))
			initForStartedSchedule();
	}

	/**
	 * Determines if all needed configuration steps are done to advance to a
	 * later panel. For example you cannot be allowed to advance to a later panel
	 * if you didn't select a fitness function.
	 * @return true if the current configuration is syntactical correct
	 */
	public boolean mayAdvance() {
		return true;
	}

	/**
	 * Determines if it is allowed to leave the configuration phase as it is
	 * to step back to a previous phase. In most cases you will allow that.
	 */
	public boolean mayStepBack() {
		return true;
	}

	/**
	 * If for any reason you don't want the user to advance to the next panel,
	 * return true here. This is usually combined with option panes where
	 * the user is asked something.
	 * @return true if the panel vetos entering the next phase
	 */
	public boolean advanceVeto() {
		return false;
	}

	/**
	 * If for any reason you don't want the user to step back to the previous panel,
	 * return true here. This is usually combined with option panes where
	 * the user is asked something.
	 * @return true if the panel vetos entering the previous phase
	 */
	public boolean stepBackVeto() {
		return false;
	}

	/**
	 * @return a short description of what the panel does
	 */
	public static String getDescription() {
		return "no description";
	}

	public String getHelpURL() {
		if (Util.scheduleStarted(schedule))
			return "node5.html#SECTION00530000000000000000";
		else
			return "node4.html";
	}

}
