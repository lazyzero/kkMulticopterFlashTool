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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

/**
 * ButtonToggleGroup
 * This is a class that "acts" like a ButtonGroup but:
 * - It allows the state where none of the buttons are selected.
 * In the ButtonGroup class, once one button is selected,
 * there is no way to unselect all buttons. This class allows
 * 1 button to be selected OR NONE of them. 
 * <p>
 * - It fires actions every time it deselects other buttons
 * In the ButtonGroup class, when a button is selected, the old
 * selected button gets a setSelected(false), but that doesn't fire
 * action events. This class works with the AbstractButton instead
 * of the ButtonModel, and it does doClick(), so the action is fired.  
 * 
 * Date created: Oct 3, 2004
 *
 * @author imoncada<p>
 *
 */
public class ButtonToggleGroup 
	implements ActionListener
{
	AbstractButton selButton;
	
	public ButtonToggleGroup()
	{
		super();
	}

	public void add(AbstractButton button)
	{
		button.addActionListener(this);
	}
	
	public void remove(AbstractButton button)
	{
		button.removeActionListener(this);
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		AbstractButton button = (AbstractButton)e.getSource();
		
		if (button.isSelected()){
			if (selButton != null){
				selButton.doClick();
				//Instead of:
				//selButton.setSelected(false);
			}
			selButton = (AbstractButton)e.getSource();
		}
		else{
			selButton = null;
		}		
	}
}
