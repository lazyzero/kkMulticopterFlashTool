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
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;
import java.util.Properties;

/*
// header - edit "Data/yourJavaHeader" to customize
// contents - edit "EventHandlers/Java file/onCreate" to customize
//
*/
public class ViewerStub
implements AppletStub
{
	static private ViewerContext context;
	
	private URL documentBase;
	private URL codeBase;
	private Applet applet;
	private Properties parameters;
	
	public ViewerStub(Applet applet)
	{
		if (context == null)
			context = new ViewerContext();
		this.applet = applet;
		context.addApplet(applet, applet.getName());
	}
	
	public boolean isActive()
	{
		return true;
	}

	public void setDocumentBase(URL base)
	{
		documentBase = base;
	}
	
	public URL getDocumentBase()
	{
		return documentBase;
	}
	
	public void setCodeBase(URL base)
	{
		codeBase = base;
	}
	
	public URL getCodeBase()
	{
		return codeBase;
	}
	
	public void setParameters(Properties properties)
	{
		parameters = properties;
	}
	
	public String getParameter(String name)
	{
		return (String) parameters.getProperty(name.toLowerCase());
	}
	
	public AppletContext getAppletContext()
	{
		return context;
	}
	
	public void appletResize(int width, int height)
	{
		applet.setSize(width, height);
	}
	
}

