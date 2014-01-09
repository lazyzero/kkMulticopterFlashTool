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

/*
 * Last modification information:
 * $Revision: 1.1 $
 * $Date: 2011-02-18 13:39:32 $
 * $Author: moll $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/
package org.concord.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * CustomDialog
 * This class can be used to pop up a dialog
 * that already has the OK and Cancel or
 * OK, Cancel and Apply buttons 
 * on it but you can specify a custom JComponent
 * to show above the buttons. 
 * 
 * Use the static method showOKCancelDialog 
 * or showOKCancelApplyDialog 
 * specifying the JComponent you want to show
 *
 * Date created: Sep 13, 2004
 *
 * @author imoncada<p>
 *
 */
public class CustomDialog extends JDialog
	implements ActionListener
{

	protected JComponent middlePanel;
	protected JPanel buttonPanel;
	protected Dimension defaultButtonSize;
		
	public static final int OK_OPTION = JOptionPane.OK_OPTION;
	public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
	public static final int APPLY_OPTION = -10;
	public static final int CLOSED_OPTION = JOptionPane.CLOSED_OPTION;

	private int returnCode = CLOSED_OPTION;
	
	protected Vector actionListeners;
	
	public CustomDialog(Frame parentComponent)
	{
		super(parentComponent);
		getContentPane().setLayout(new BorderLayout());
		buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
        try{
        	setLocationRelativeTo(parentComponent);
        }
        catch(Throwable ex){
        }		
	}
	
	/**
	 * Shows an Ok Cancel modal non-resizable message dialog
	 * @param parentComponent
	 * @param middlePanel
	 * @param title
	 * @return
	 */
	public static int showOKCancelDialog(Component parentComponent, JComponent middlePanel, String title)
	{
		return showOKCancelDialog(parentComponent, middlePanel, title, false, true);
	}
	
	/**
	 * Shows an Ok Cancel message dialog
	 * @param parentComponent
	 * @param middlePanel
	 * @param title
	 * @param resizable
	 * @param modal
	 * @return
	 */
	public static int showOKCancelDialog(Component parentComponent, JComponent middlePanel, String title, boolean resizable, boolean modal)
	{
		CustomDialog dialog = createOKCancelDialog(parentComponent, middlePanel, title, resizable, modal);
		if (dialog == null) return -1;
		
        dialog.pack();
		dialog.show();
		
		return dialog.getReturnCode();
	}
	
	/**
	 * Shows an Ok Cancel message dialog
	 * @param parentComponent
	 * @param middlePanel
	 * @param title
	 * @param resizable
	 * @param modal
	 * @return
	 */
	public static int showOKCancelApplyDialog(Component parentComponent, JComponent middlePanel, String title, boolean resizable, boolean modal)
	{
		return showOKCancelApplyDialog(parentComponent, middlePanel, title, resizable, modal, null);
	}
	/**
	 * Shows an Ok Cancel message dialog
	 * @param parentComponent
	 * @param middlePanel
	 * @param title
	 * @param resizable
	 * @param modal
	 * @return
	 */
	public static int showOKCancelApplyDialog(Component parentComponent, JComponent middlePanel, String title, boolean resizable, boolean modal, ActionListener actionListener)
	{
		CustomDialog dialog = createOKCancelDialog(parentComponent, middlePanel, title, resizable, modal);
		if (dialog == null) return -1;
		
		dialog.addButton("Apply", CustomDialog.APPLY_OPTION);
		if (actionListener != null){
			dialog.addActionListener(actionListener);
		}
		
        dialog.pack();
		dialog.show();
		
		return dialog.getReturnCode();
	}
	
	public static CustomDialog createOKCancelDialog(Component parentComponent, JComponent middlePanel, String title, boolean resizable, boolean modal)
	{
		Component c = null;
		
		c = parentComponent;
		while (c != null && !(c instanceof Frame)){
			c = c.getParent();
		}
		if (c == null) return null;
		
		CustomDialog dialog = new CustomDialog((Frame)c);
		dialog.setTitle(title);
		dialog.setMiddlePanel(middlePanel);
		dialog.addDefaultButtonsPanel();
		
		dialog.setModal(modal);
		
		dialog.setResizable(resizable);
		
		return dialog;
	}
	
	public static void main(String[] args)
	{
	}
	
	/**
	 * @return Returns the middlePanel.
	 */
	public JComponent getMiddlePanel()
	{
		return middlePanel;
	}
	/**
	 * @param middlePanel The middlePanel to set.
	 */
	public void setMiddlePanel(JComponent middlePanel)
	{
		this.middlePanel = middlePanel;
		getContentPane().add(middlePanel);
	}
	/**
	 * @return Returns the buttonPanel.
	 */
	public JPanel getButtonPanel()
	{
		return buttonPanel;
	}
	
	protected void addDefaultButtonsPanel()
	{
		JButton cancelButton = createButton("Cancel", CANCEL_OPTION);
		defaultButtonSize = cancelButton.getPreferredSize();		
		JButton okButton = createButton("OK", OK_OPTION);
		
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
	}
	
	/**
	 * @param buttonPanel The buttonPanel to set.
	 */
	public void setButtonPanel(JPanel buttonPanel)
	{
		this.buttonPanel = buttonPanel;
	}

	public void addButton(String text, int actionCommand)
	{
		JPanel p = getButtonPanel();
		p.add(createButton(text, actionCommand));
	}
	
	protected JButton createButton(String text, int actionCommand)
	{
		JButton b = new JButton(text);
		if (defaultButtonSize != null){
			b.setPreferredSize(defaultButtonSize);
		}
		b.setActionCommand(Integer.toString(actionCommand));
		b.addActionListener(this);
		return b;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		try{
			returnCode = Integer.parseInt(e.getActionCommand());
		}
		catch(Throwable ex){
			System.err.println("Warning: Action command " + e.getActionCommand() + " is not an integer");
			ex.printStackTrace();
			return;
		}
		
		if (returnCode == OK_OPTION || 
				returnCode == CANCEL_OPTION || 
				returnCode == CLOSED_OPTION){
			this.hide();
		}
		
		notifyActionListeners(returnCode);
	}
	
	/**
	 * @return Returns the returnCode.
	 */
	public int getReturnCode()
	{
		return returnCode;
	}
	
	public void addActionListener(ActionListener l)
	{
		if (actionListeners == null){
			actionListeners = new Vector();
		}
		if (!actionListeners.contains(l)){
			actionListeners.add(l);
		}
	}
	
	public void removeActionListener(ActionListener l)
	{
		if (actionListeners == null) return;
		actionListeners.remove(l);
	}
	
	protected void notifyActionListeners(int retcode)
	{
		if (actionListeners == null) return;
		ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, String.valueOf(retcode));
		for (int i=0; i<actionListeners.size(); i++){
			ActionListener l = (ActionListener)actionListeners.elementAt(i);
			l.actionPerformed(evt);
		}
	}
}
