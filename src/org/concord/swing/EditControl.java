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
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class EditControl
extends JFrame
implements ActionListener
{
	protected JLabel modeLabel = new JLabel("Mode: ");
	protected JRadioButton edit = new JRadioButton("Edit");
	protected JRadioButton run = new JRadioButton("Run");
	protected JPanel buttonPanel = new JPanel();
	protected ViewEditor editor;
	
	public EditControl()
	{
		getContentPane().setLayout(new BorderLayout());
		buttonPanel.setLayout(new FlowLayout());
		getContentPane().add(buttonPanel, "North");
		buttonPanel.add(modeLabel);
		buttonPanel.add(edit);
		buttonPanel.add(run);
		pack();
	}
	
	public EditControl(ViewEditor viewEditor)
	{
		this();
		setEditor(viewEditor);
	}
	
	public void setVisible(boolean value)
	{
		if (! value)
		{
			edit.removeActionListener(this);
			run.removeActionListener(this);
		}
		edit.setSelected(! value);
		run.setSelected(! value);
		if (value)
		{
			edit.addActionListener(this);
			run.addActionListener(this);
		}
		updateContainer();
		super.setVisible(value);
	}
	
	public void setEditor(ViewEditor viewEditor)
	{
		editor = viewEditor;
	}
	
	public void updateContainer()
	{
		if (editor != null)
		{
			Container container = editor.getContainer();
			if (edit.isSelected())
				container.add(editor, 0);
			else
				container.remove(editor);
			container.repaint();
		}
	}
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == edit)
		{
			edit.setSelected(true);
			run.setSelected(false);
		}
		if (event.getSource() == run)
		{
			edit.setSelected(false);
			run.setSelected(true);
		}
		updateContainer();
	}
}
