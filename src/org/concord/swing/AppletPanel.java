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

import java.applet.Applet;
import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JPanel;

public class AppletPanel
extends JPanel
{
	protected Applet applet;
	protected int addCount = 0;
	
	public void addNotify()
	{
		if (addCount == 0)
			super.addNotify();
		addCount++;
		if (applet instanceof Applet)
			applet.setVisible(true);
	}
	
	public void removeNotify()
	{
		if (applet instanceof Applet)
			applet.setVisible(false);
	}

	
	public void setApplet(Applet applet)
	{
		this.applet = applet;
		removeAll();
		addCount = 0;
		if (applet instanceof Applet)
		{
			try
			{
				ViewerStub stub = new ViewerStub(applet);
				URL base = new URL("file://" + System.getProperty("user.dir"));
				stub.setDocumentBase(base);
				stub.setCodeBase(base);
				applet.setStub(stub);
				applet.setLayout(new BorderLayout());
				add(applet);
			}
			catch (Exception e)
			{
				System.out.println("AppletPanel.setApplet: " + e);
			}
		}
	}
}

