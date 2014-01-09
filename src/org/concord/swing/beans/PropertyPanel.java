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

/**
 * @author dima
 *
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.JPanel;

/**
 *  Description of the Class
 * Panel that has a property sheet on it, and a hash table
 *
 *@author     imoncada
 *@created    March 26, 2003
 *
 */

public class PropertyPanel extends JPanel implements PropertyChangeListener
{
	private GridBagConstraints gbc;
	
	private PropertySheet propertiesBox;

	public PropertyPanel()
	{
		super();
		
		setLayout(new GridBagLayout());

		gbc=new GridBagConstraints();
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.NORTHEAST;
		gbc.weightx=1;
		gbc.weighty=1;
	}

	public void showProperties(Object obj, Hashtable validProps)
	{
		if (propertiesBox!=null){
			propertiesBox.removePropertyChangeListener(this);
		}

		removeAll();
		
		if (obj!=null) {

			propertiesBox = new PropertySheet(obj,"Properties",false,validProps);
			
			((GridBagLayout)getLayout()).setConstraints(propertiesBox,gbc);
			
			add(propertiesBox);
			
			propertiesBox.addPropertyChangeListener(this);
		}

		validate();
		repaint();			
	}

	public void propertyChange(PropertyChangeEvent evt) 	
	{
	
		propertiesBox.writeNewValues();
		
		firePropertyChange(evt.getPropertyName(),evt.getOldValue(),evt.getNewValue());
	}

}