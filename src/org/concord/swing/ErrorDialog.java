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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
	 *  Description of the Class
	 *
	 *@author     ed
	 *@created    May 22, 2002
	 */
public class ErrorDialog extends JDialog
	implements ActionListener
{
	/**
	 *  Description of the Field
	 */
	protected JTextArea messageArea;

	protected JTextArea exceptionText;

	protected JTextArea expTraceArea;
	protected JScrollPane expTraceScroll;
	/**
		 *  Description of the Field
		 */
	protected JPanel buttonPanel;
		
	protected JPanel messagePanel;
	/**
	 *  Description of the Field
	 */
	protected JButton okButton;

	protected JButton detailsButton;

	protected GridBagLayout gridbag;
	protected GridBagConstraints c;

	protected Throwable detailException = null;

	public static void showError(Frame frame, String message, Throwable t)
	{
		ErrorDialog eDialog = new ErrorDialog(frame, message, t, false);
		eDialog.show();
	}

	public ErrorDialog(Frame frame, String message, boolean modal)
	{
		this(frame, message, null, modal);
	}

	/**
		 *  Constructor for the ErrorDialog object
		 *
		 *@param  frame    Description of the Parameter
		 *@param  message  Description of the Parameter
		 *@param  modal    Description of the Parameter
		 */
	public ErrorDialog(Frame frame, String message, Throwable e, boolean modal)
	{
		super(frame, modal);

		detailException = e;
		initialize(message, e);
	}
	
	public ErrorDialog(Frame frame, boolean modal)
	{
		super(frame, modal);
	}
	
	protected void initialize(String message, Throwable e)
	{

		messageArea = new JTextArea(message);
		messageArea.setEditable(false);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setOpaque(false);

		okButton = new JButton("Ok");

		buttonPanel = new JPanel();
		buttonPanel.add(okButton);

		gridbag = new GridBagLayout();
		c = new GridBagConstraints();

		messagePanel = new JPanel(gridbag);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 0.5;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(3, 3, 3, 3);
		gridbag.setConstraints(messageArea, c);
		messagePanel.add(messageArea);

		if(e != null){
			exceptionText = new JTextArea(e.getMessage());
			exceptionText.setEditable(false);
			exceptionText.setLineWrap(true);
			exceptionText.setWrapStyleWord(true);
			exceptionText.setOpaque(false);

			c.gridwidth = GridBagConstraints.RELATIVE;
			c.weighty = 1.0;
			gridbag.setConstraints(exceptionText, c);
			messagePanel.add(exceptionText);
			
			detailsButton = new JButton("Details");
			detailsButton.addActionListener(this);
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.NONE;
			gridbag.setConstraints(detailsButton, c);
			messagePanel.add(detailsButton);
		}
			
		getContentPane().add(messagePanel, "Center");
		getContentPane().add(buttonPanel, "South");
		okButton.addActionListener(this);
		
		setBounds(100, 100, 300, 180);
	}


	/**
		 *  Description of the Method
		 *
		 *@param  event  Description of the Parameter
		 */
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okButton)
		{
			setVisible(false);
		} 
		else if (event.getSource() == detailsButton)
		{
			if(expTraceArea != null)
			{
				return;
			}

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(outStream);
			if(detailException != null)
			{
				detailException.printStackTrace(pw);
				pw.flush();
				expTraceArea = new JTextArea(outStream.toString());
			}
			else 
			{
				expTraceArea = new JTextArea("Sorry no details availabe.");
			}

			expTraceArea.setEditable(false);
			expTraceArea.setLineWrap(true);
			expTraceArea.setWrapStyleWord(true);

			expTraceScroll = new JScrollPane(expTraceArea);

			c.gridwidth = GridBagConstraints.REMAINDER;
			c.weighty = 1.5;
			c.weightx = 1.0;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(3, 3, 3, 3);
			gridbag.setConstraints(expTraceScroll, c);
			messagePanel.add(expTraceScroll);

			detailsButton.setEnabled(false);

			// messagePanel.validate();
			setBounds(getLocation().x, getLocation().y, 600, 400);
			validate();
		}
	}

}

