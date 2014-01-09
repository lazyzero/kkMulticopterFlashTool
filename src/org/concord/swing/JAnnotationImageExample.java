/*
 *  Copyright (C) 2004  The Concord Consortium, Inc.,
 *  10 Concord Crossing, Concord, MA 01742
 *
 *  Web Site: http://www.concord.org
 *  Email: info@concord.org
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * END LICENSE */

package org.concord.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

class JAnnotationImageExample extends JPanel {

    private static final Border SELECTED_BORDER=BorderFactory.createLoweredBevelBorder();
    private static final Border EMPTY_BORDER=BorderFactory.createEmptyBorder();

    private JToolBar toolbar;
    private JTextArea textArea;
    private JAnnotationImageContainer annotationContainer;
    private JCheckBox annotationCheckBox;
    private ImageIcon image;

    JAnnotationImageExample(){

	super(new BorderLayout(0, 0));
	setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	textArea=new JTextArea();
	textArea.setPreferredSize(new Dimension(300, 100));
	JScrollPane sp = new JScrollPane(textArea);
	//add(sp, BorderLayout.SOUTH);

	JLabel label=new JLabel("<html>Please make note of the above snapshot image in the following box (it will be automatically included in your report) :</html>");
	label.setAlignmentX(0.5f);

	label.setPreferredSize(new Dimension(300, 50));
	//add(label, BorderLayout.CENTER);

	JPanel panel=new JPanel(new BorderLayout(0, 0));
	panel.setBorder(BorderFactory.createEtchedBorder());
	//add(panel, BorderLayout.NORTH);

	annotationContainer=new JAnnotationImageContainer();
    //annotationContainer.setToolBarVisible(false);
	annotationContainer.setBorder(BorderFactory.createLoweredBevelBorder());
	panel.add(annotationContainer, BorderLayout.CENTER);
	
	javax.swing.Box box = javax.swing.Box.createVerticalBox();
	
	add(box,BorderLayout.CENTER);
	box.add(new JScrollPane(panel));
	box.add(label);
	box.add(sp);
	
    }

    String showInputDialog(Component parent, ImageIcon image){

	this.image=image;

	annotationContainer.setAnnotationImage((BufferedImage)image.getImage());
	annotationContainer.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
    if(annotationCheckBox != null) annotationCheckBox.setSelected(false);
    textArea.setText("");

	final JDialog dialog=new JDialog(JOptionPane.getFrameForComponent(parent), "Add snapshot", true);
	dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

	dialog.getContentPane().add(this, BorderLayout.CENTER);
	
	JPanel panel=new JPanel();
	dialog.getContentPane().add(panel, BorderLayout.SOUTH);

	JButton button=new JButton("OK");
	button.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
            dialog.dispose();
		}
	    });
	panel.add(button);

	dialog.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e){
		    dialog.dispose();
		}
		public void windowActivated(WindowEvent e){
		   textArea.requestFocus();
		}
	    });

	dialog.pack();
	dialog.setLocationRelativeTo(parent);
	dialog.setVisible(true);

	return textArea.getText();

    }

    private void createAnnotationToolBar(){

	toolbar=new JToolBar(SwingConstants.HORIZONTAL);
	toolbar.setFloatable(false);
	toolbar.setMargin(new Insets(0, 0, 0, 0));
	toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
	toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);

	Dimension buttonDimension=new Dimension(24, 24);

	final List disableList=new ArrayList();

	annotationCheckBox=new JCheckBox("Annotation");
	annotationCheckBox.setToolTipText("Enable the tool for annotating the snapshot image");
	annotationCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
	annotationCheckBox.setSelected(false);
	annotationCheckBox.addItemListener(new ItemListener(){
		public void itemStateChanged(ItemEvent e){
		    final boolean b=e.getStateChange()==ItemEvent.SELECTED;
		    SwingUtilities.invokeLater(new Runnable(){
			    public void run(){
				annotationContainer.setEditMode(b);
				for(Iterator it=disableList.iterator(); it.hasNext();){
				    ((AbstractButton)it.next()).setEnabled(b);
				}
			    }
			});
		}
	    });
	toolbar.add(annotationCheckBox);

	ButtonGroup bg=new ButtonGroup();
	
	JRadioButton rb=new JRadioButton(new ImageIcon(getClass().getResource("images/CallOutRectangle.gif")));
	rb.setSelected(true);
	rb.setToolTipText("Annotate a selected rectangular area");
	rb.setPreferredSize(buttonDimension);
	rb.setBorderPainted(true);
	rb.setHorizontalAlignment(SwingConstants.CENTER);
	rb.addItemListener(new ItemListener(){
		public void itemStateChanged(ItemEvent e){
		    JRadioButton rbb=(JRadioButton)e.getSource();
		    if(e.getStateChange()==ItemEvent.SELECTED){
			annotationContainer.setChoosingMode(JAnnotationImageModel.CHOOSING_MODE_RECTANGLE);
			rbb.setBorder(SELECTED_BORDER);
			rbb.setBackground(Color.white);
		    } else {
			rbb.setBorder(EMPTY_BORDER);
			rbb.setBackground(getBackground());
		    }
		}
	    });
	bg.add(rb);
	toolbar.add(rb);
	disableList.add(rb);

	rb=new JRadioButton(new ImageIcon(getClass().getResource("images/CallOutEllipse.gif")));
	rb.setToolTipText("Annotate a selected circular area");
	rb.setPreferredSize(buttonDimension);
	rb.setBorderPainted(true);
	rb.setHorizontalAlignment(SwingConstants.CENTER);
	rb.addItemListener(new ItemListener(){
		public void itemStateChanged(ItemEvent e){
		    JRadioButton rbb=(JRadioButton)e.getSource();
		    if(e.getStateChange()==ItemEvent.SELECTED){
			annotationContainer.setChoosingMode(JAnnotationImageModel.CHOOSING_MODE_ELLIPSE);
			rbb.setBorder(SELECTED_BORDER);
			rbb.setBackground(Color.white);
		    } else {
			rbb.setBorder(EMPTY_BORDER);
			rbb.setBackground(getBackground());
		    }
		}
	    });
	bg.add(rb);
	toolbar.add(rb);
	disableList.add(rb);
	
	for(Iterator it=disableList.iterator(); it.hasNext();) ((AbstractButton)it.next()).setEnabled(false);
	
    }
    
    public static void main(String []args){
        JAnnotationImageExample snapshotComment=new JAnnotationImageExample();
        try{
            java.awt.Robot robot = new java.awt.Robot();
            java.awt.image.BufferedImage bimg = robot.createScreenCapture(new java.awt.Rectangle(0,0,300,300));
            snapshotComment.showInputDialog(null, new ImageIcon(bimg));
        }catch(Throwable t){
            System.out.println("Throwable "+t);
            t.printStackTrace();
        }
    }

}
