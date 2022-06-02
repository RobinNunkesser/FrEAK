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

import freak.core.control.ScheduleInterface;
import freak.core.view.HistoryView;
import freak.core.view.swingsupport.FreakScrollBarAdjustmentListener;
import freak.core.view.swingsupport.FreakTextAreaModel;
import freak.core.view.swingsupport.SlidingWindow;
import freak.core.view.swingsupport.UpdateManager;
import freak.module.observer.ResultObserver;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;

public class StdView extends HistoryView {
	
	private FreakTextAreaModel model;
	
	private SlidingWindow stringLengths;
	
	private StringBuffer stringBuffer;

	public StdView(ScheduleInterface schedule) {
		super(schedule);
		
		model = new FreakTextAreaModel();
		stringLengths = new SlidingWindow();
		stringBuffer = new StringBuffer();
		numberOfDisplayedValues = 1;
	}

	public JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JTextArea text = new JTextArea() {
			public void scrollRectToVisible(Rectangle R) {
				/* do nothing to prevent the visible rect to be moved 
				 * to its default position every time the view is updated.
				 */
			}
		};
		text.setEditable(false);
		model.setView(text);

		JScrollPane scrollPane = new JScrollPane(text);
		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
		scrollBar.addAdjustmentListener(new FreakScrollBarAdjustmentListener(scrollBar, true));

		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	public String getName() {
		return "Standard View";
	}

	public String getDescription() {
		return "Displays textual data in a frame.";
	}

	public Class[] getInputDataTypes() {
		return new Class[] { Object.class };
	}

	/**
	 * Extracts text from the specified object.
	 * If the object is a simple object, <code>o.toString()</code> is returned.
	 * If the object is an array, the elements therein are processed
	 * in the correct order. 
	 * This is done recursively, so that multi-dimensional arrays are 
	 * handled properly.
	 * 
	 * @param o an object.
	 * @return the text extracted from the object. 
	 */
	protected String extractTextFromObject(Object o) {
		/* The object is an array, so the elements of the first dimension are 
		 * processed.
		 */
		if (o.getClass().isArray()) {

			StringBuffer s = new StringBuffer();

			for (int i = 0; i < Array.getLength(o); i++) {
				if (i > 0)
					s.append(", ");

				Object element = Array.get(o, i);

				s.append(extractTextFromObject(element));
			}

			return s.toString();
		} else {
			return o.toString();
		}
	}

	/*
	 * Used by FileWriter
	 */
	protected void setText(String text) {
		synchronized (model) {
			model.setText(text);
		}
		UpdateManager.markDirty(model);
	}

	public void update(Object o) {
		super.update(o);

		String newText = extractTextFromObject(o) + "\n";

		// add new string
		stringLengths.add(new Integer(newText.length()));
		stringBuffer.append(newText);
		
		if ((numberOfDisplayedValues != 0)&&(!(getObserver() instanceof ResultObserver))) {
			// remove first strings if necessary
			while (stringLengths.size() > numberOfDisplayedValues) {
				int length = ((Integer)stringLengths.get(0)).intValue();
				stringBuffer.delete(0, length);
				stringLengths.removeFirst();
			}
		}
		
		setText(stringBuffer.toString());
	}
}