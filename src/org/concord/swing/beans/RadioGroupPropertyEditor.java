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

package org.concord.swing.beans;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class RadioGroupPropertyEditor extends PropertyEditorSupport implements ActionListener {
	JPanel radioPanel;
	ButtonGroup radioGroup;
	Hashtable radioButtonValues;

	public RadioGroupPropertyEditor()
	{
		radioPanel=new JPanel();
		radioGroup=new ButtonGroup();
        radioPanel.setLayout(new GridLayout(1, 5));
        radioButtonValues=new Hashtable();
        
		initValues();
	}

	public Component getCustomEditor()
	{
		return radioPanel;
	}
	
	public boolean supportsCustomEditor()
	{
		return true;
	}

	public void actionPerformed(ActionEvent e) 
	{
		firePropertyChange();
	}

	public Object getValue()
	{
		Enumeration values;
		Integer val;

		values=radioButtonValues.keys();
		while (values.hasMoreElements()){
			val=(Integer)values.nextElement();
			if (((JRadioButton)radioButtonValues.get(val)).isSelected()){
				return val;
			}
		}
		return new Integer(0);
	}

	public void setValue(Object value)
	{
		JRadioButton butt;
		
		butt = (JRadioButton)radioButtonValues.get(value);
		if (butt!=null){
			butt.setSelected(true);
		}
	}
	
	protected void initValues()
	{
		addRadio("test",0);
	}
	
	public void addRadio(String label, int value)
	{
		JRadioButton radio;

		radio =	new JRadioButton();
		radio.setText(label);
		radio.addActionListener(this);

		radioButtonValues.put(new Integer(value),radio);		
		
		radioGroup.add(radio);

        radioPanel.add(radio);
        
        radioPanel.validate();  
	}
}