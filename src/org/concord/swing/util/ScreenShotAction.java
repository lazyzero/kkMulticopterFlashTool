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
 * $Date: 2011-02-18 13:39:35 $
 * $Author: moll $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/
package org.concord.swing.util;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;


/**
 * ScreenShotAction
 * Class name and description
 *
 * Date created: Nov 16, 2004
 *
 * @author scott<p>
 *
 */
public class ScreenShotAction extends AbstractAction
{
	Component component;
	private float xScale;
	private float yScale;

	public ScreenShotAction(Component comp)
	{
		this(comp, 1, 1);
	}
	
	public ScreenShotAction(Component comp, float xScale, float yScale)
	{
		setName("Save Screen Shot...");
		component = comp;
		this.xScale = xScale;
		this.yScale = yScale;
	}
	
	public void setName(String name)
	{
		putValue(Action.NAME, name);		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0)
	{
		Util.makeScreenShot(component, xScale, yScale);
	}
}
