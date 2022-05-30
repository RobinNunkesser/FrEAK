/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.gui.runframe;


/**
 * @author  Dimo
 */
class AboutDialog extends javax.swing.JDialog {

	private java.awt.Window parent;

	/** Creates new form AboutDialog */
	public AboutDialog(javax.swing.JFrame parent, boolean modal) {
		super(parent, modal);
		this.parent = parent;
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        About1Lb = new javax.swing.JLabel();
        backBt = new javax.swing.JButton();
        logoLb = new javax.swing.JLabel();
        About2Lb = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("About FrEAK");
        setBackground(java.awt.Color.white);
        setForeground(java.awt.Color.white);
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        About1Lb.setBackground(new java.awt.Color(255, 255, 255));
        About1Lb.setFont(new java.awt.Font("Dialog", 1, 10));
        About1Lb.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        About1Lb.setText("<html>\n<center>\n<h1>FrEAK - Free Evolutionary Algorithm Kit</h1>\n<p>\nThis program was implemented by the project group 427\nat the University of Dortmund, particularly by<br>\n<table>\n<tr><td>Patrick Briest</td><td> Dimo Brockhoff</td><td>Sebastian Degener</td><td> Matthias Englert</td></tr>\n<tr><td>Christian Gunia</td><td> Oliver Heering</td><td>Michael Leifhelm</td><td> Kai Plociennik</td></tr>\n<tr><td>Heiko R\u00f6glin</td><td> Andrea Schweer</td><td>Dirk Sudholt</td><td> Stefan Tannenbaum</td></tr>\n</table>\n(in alphabetical order)\n</p><br>\n<p>\nThomas Jansen and Ingo Wegener from Ls2.\n</p>\n\n\n</center>\n</html>");
        About1Lb.setFocusable(false);
        About1Lb.setMaximumSize(new java.awt.Dimension(681, 180));
        About1Lb.setMinimumSize(new java.awt.Dimension(681, 180));
        About1Lb.setPreferredSize(new java.awt.Dimension(681, 180));
        About1Lb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        About1Lb.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(About1Lb, gridBagConstraints);

        backBt.setBackground(new java.awt.Color(255, 255, 255));
        backBt.setText("Back to FrEAK");
        backBt.setToolTipText("Go back to main window");
        backBt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(backBt, gridBagConstraints);

        logoLb.setBackground(new java.awt.Color(255, 255, 255));
        logoLb.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logoLb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freak/gui/images/logo.png")));
        logoLb.setFocusable(false);
        logoLb.setMaximumSize(new java.awt.Dimension(681, 196));
        logoLb.setMinimumSize(new java.awt.Dimension(681, 196));
        logoLb.setPreferredSize(new java.awt.Dimension(681, 196));
        logoLb.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(logoLb, gridBagConstraints);

        About2Lb.setBackground(new java.awt.Color(255, 255, 255));
        About2Lb.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        About2Lb.setText("<html>\n<center>\n<h2>This is FrEAK version 0.2</h2>\n<h3>release date: 2004-02-10</h3>\n\n<p>This program is free software.<br>\nFor licensing and copyright information\nplease see the file COPYING<br> in the root directory of your\ndistribution<br> or contact freak@ls2.cs.uni-dortmund.de.\n</p>\n</center>\n</html>");
        About2Lb.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        About2Lb.setMaximumSize(new java.awt.Dimension(681, 150));
        About2Lb.setMinimumSize(new java.awt.Dimension(681, 150));
        About2Lb.setPreferredSize(new java.awt.Dimension(681, 150));
        About2Lb.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(About2Lb, gridBagConstraints);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setMaximumSize(new java.awt.Dimension(280, 25));
        jLabel1.setMinimumSize(new java.awt.Dimension(280, 25));
        jLabel1.setPreferredSize(new java.awt.Dimension(280, 25));
        jLabel1.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jLabel1, gridBagConstraints);

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setMaximumSize(new java.awt.Dimension(280, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(280, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(280, 25));
        jLabel2.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jLabel2, gridBagConstraints);

        pack();
    }//GEN-END:initComponents

	private void backBtActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_backBtActionPerformed
		parent.setVisible(true);
		setVisible(false);
		dispose();
	} //GEN-LAST:event_backBtActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) { //GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	} //GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel About1Lb;
    private javax.swing.JLabel About2Lb;
    private javax.swing.JButton backBt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel logoLb;
    // End of variables declaration//GEN-END:variables

}
