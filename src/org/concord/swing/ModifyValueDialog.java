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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.concord.swing.event.VariableEvent;
import org.concord.swing.event.VariableListener;

public class ModifyValueDialog
extends JDialog
implements ActionListener
{
    protected JTextField variableField = new JTextField();
    protected JLabel label = new JLabel("Variable name:");
    protected JButton doneButton = new JButton("Done");
    protected JButton cancelButton = new JButton("Cancel");
    protected JPanel buttonPanel = new JPanel();
    protected JPanel topPanel = new JPanel();
    protected JPanel valuePanel = new JPanel();
    protected JTextField valueField = new JTextField();
	protected Object value;
	protected Vector listeners = new Vector();
	protected boolean changed = false;
    
	public final static String VARIABLE_CHANGED = "VARIABLE_CHANGED";

    public ModifyValueDialog(JFrame parent, String title)
    {
        super(parent, title, true);
        topPanel.setLayout(new BorderLayout());
        topPanel.add(label, "West");
        topPanel.add(variableField, "Center");
        valuePanel.setLayout(new BorderLayout());
        valuePanel.add(valueField, "North");
        buttonPanel.add(doneButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(topPanel, "North");
        getContentPane().add(valuePanel, "Center");
        getContentPane().add(buttonPanel, "South");
        doneButton.addActionListener(this);
        cancelButton.addActionListener(this);
        setBounds(200, 200, 400, 104);
        setResizable(false);
    }
    
    public void actionPerformed(ActionEvent event)
    {
        Object source = event.getSource();
        JButton button = (source instanceof JButton) ? (JButton) source : null;
        if (button == cancelButton)
        {
			setVariable(null);
			setValue("");
        }
		else if (button == doneButton)
		{
			if (changed)
			{
				notifyVariableValueChanged(getValue());
				changed = false;
			}
		}

        setVisible(false);
    }
    
    public void setModify(boolean modify)
    {
        variableField.setEditable(! modify);
        variableField.setEnabled(! modify);
    }
    
    public boolean getModify()
    {
        return ! variableField.isEditable();
    }
	
    public String getVariable()
    {
        return variableField.getText();
    }
    
    public void setVariable(String variable)
    {
        variableField.setText(variable);
    }
    
    public void setValue(Object value)
    {
     	valueField.setText(value == null ? "" : value.toString());
        this.value = value;
    }
    
    public Object getValue()
    {
    	value = valueField.getText();
        return value;
    }
		
	public boolean isChanged()
	{
		return changed;
	}
	
	public void setChanged(boolean value)
	{
		changed = value;
	}
	
	public void notifyVariableValueChanged(Object newValue)
	{
		for (int i = 0; i < listeners.size(); i++)
		{
			VariableListener listener = (VariableListener) listeners.elementAt(i);
			listener.variableValueChanged(new VariableEvent(this, newValue));
		}
	}
	
	public void notifyVariableNameChanged(String newName)
	{
		for (int i = 0; i < listeners.size(); i++)
		{
			VariableListener listener = (VariableListener) listeners.elementAt(i);
			listener.variableNameChanged(new VariableEvent(this, newName));
		}
	}
	
	public void addVariableListener(VariableListener listener)
	{
		if (! listeners.contains(listener))
			listeners.addElement(listener);
	}
	
	public void removeVariableListener(VariableListener listener)
	{
		if (listeners.contains(listener))
			listeners.removeElement(listener);
	}
}
