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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JToggleButton;

/**
 * ShapeViewToggleButton
 * Class name and description
 *
 * Date created: Oct 3, 2004
 *
 * @author imoncada<p>
 *
 */
public class SelectableToggleButton extends JToggleButton
{
	PropertyChangeListener defaultPropChange;
	
	/**
	 * @param a
	 */
	public SelectableToggleButton()
	{
		super();
	}
	
	/**
	 * @param a
	 */
	public SelectableToggleButton(Action a)
	{
		super(a);
	}
	
	/**
	 * @see javax.swing.AbstractButton#configurePropertiesFromAction(javax.swing.Action)
	 */
	protected void configurePropertiesFromAction(Action a)
	{
		super.configurePropertiesFromAction(a);
		if (a instanceof SelectableAction){
			setSelected(((SelectableAction)a).isSelected());
		}
	}
	/**
	 * @see javax.swing.AbstractButton#createActionPropertyChangeListener(javax.swing.Action)
	 */
	protected PropertyChangeListener createActionPropertyChangeListener(Action a)
	{
		defaultPropChange = super.createActionPropertyChangeListener(a);
		return new PrivatePropertyChangeListener(this);
	}
	
	/**
	 */
	protected void updateProperty(PropertyChangeEvent e)
	{
		String propName = e.getPropertyName();
		
		if (propName.equals("selected")){
			setSelected(((Boolean)e.getNewValue()).booleanValue());
		}
	}
	
	class PrivatePropertyChangeListener 
		implements PropertyChangeListener
	{
		SelectableToggleButton button;
		
		PrivatePropertyChangeListener(SelectableToggleButton button) 
		{
			this.button = button;
		}
		
		public void propertyChange(PropertyChangeEvent e) 
		{
			button.updateProperty(e);
			defaultPropChange.propertyChange(e);
		}
	}
	
}
