package org.jgraph.pad.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.jgraph.GPGraphpad;
import org.jgraph.layout.LayoutAlgorithm;
import org.jgraph.layout.LayoutController;
import org.jgraph.layout.LayoutDialog;

/**
 * Calls a frame to select the Layoutalgorithm.
 * After selecting the action applies the
 * algorithm to the current graph.
 * 
 * @author sven.luzar
 *
 */
public class GraphApplyLayoutAlgorithm extends AbstractActionDefault {

	/**
	 * Constructor for GraphApplyLayoutAlgorithm.
	 * 
	 * @param graphpad
	 * @param name
	 */
	public GraphApplyLayoutAlgorithm(GPGraphpad graphpad) {
		super(graphpad);
	}

	/**Implementation
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Frame f = JOptionPane.getFrameForComponent(graphpad);
		final LayoutDialog dlg = new LayoutDialog(f);
		dlg.show();

		if (dlg.isCanceled())
			return;

		final LayoutController controller = dlg.getSelectedLayoutController();
		if (controller == null)
			return;
		Thread t = new Thread("Layout Algorithm " + controller.toString()) {
			public void run() {
				LayoutAlgorithm algorithm = controller.getLayoutAlgorithm();
				algorithm.perform(
					graphpad.getCurrentGraph(),
					dlg.isApplyLayoutToAll(),
					controller.getConfiguration());
			}
		};
		t.start();

	}
}