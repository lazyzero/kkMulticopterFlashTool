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
 * $Date: 2011-02-18 13:39:31 $
 * $Author: moll $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/
package org.concord.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Icon;


/**
 * SelectableAction
 * This is an action that can be selected.
 * This can be used in combination with the SelectableToggleButton
 * (who listens to this action to select and deselect itself)
 *
 * Date created: Mar 5, 2005
 *
 * @author imoncada<p>
 *
 */
public class SelectableAction extends AbstractAction
{
	protected boolean selected;
	protected boolean multipleSelection;

	/**
	 * 
	 */
	public SelectableAction()
	{
		super();
	}

	/**
	 * @param name
	 */
	public SelectableAction(String name)
	{
		super(name);
	}

	/**
	 * @param name
	 * @param icon
	 */
	public SelectableAction(String name, Icon icon)
	{
		this(name, icon, false);
	}

	public SelectableAction(String name, Icon icon, boolean multipleSelection)
	{
		super(name, icon);
		setMultipleSelection(multipleSelection);
	}

	/**
	 * @return Returns the selected.
	 */
	public boolean isSelected()
	{
		return selected;
	}
	
	public boolean isMultipleSelection() {
		return multipleSelection;
	}
	
	public void setMultipleSelection(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
	}
	
	/**
	 * @param selected The selected to set.
	 */
	public void setSelected(boolean selected)
	{
		boolean oldSelected = this.selected;
		this.selected = selected;
		firePropertyChange("selected", new Boolean(oldSelected), new Boolean(selected));
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() instanceof AbstractButton){
			setSelected(((AbstractButton)e.getSource()).isSelected());
		}
	}
}
