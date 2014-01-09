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
import java.applet.AudioClip;
import java.awt.Image;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.ImageIcon;

/*
// header - edit "Data/yourJavaHeader" to customize
// contents - edit "EventHandlers/Java file/onCreate" to customize
//
*/
public class ViewerContext
implements AppletContext
{
	private Hashtable appletsTable = new Hashtable();
	
	public ViewerContext()
	{
	}
	
	public void addApplet(Applet applet, String name)
	{
		appletsTable.put(name, applet);
	}
	
	public AudioClip getAudioClip(URL url)
	{
		return null;
	}
	
	public Image getImage(URL url)
	{
		ImageIcon icon = new ImageIcon(url);
		return icon.getImage();
	}
	
	public Applet getApplet(String name)
	{
		return (Applet) appletsTable.get(name);
	}
	
	public Enumeration getApplets()
	{
		return appletsTable.elements();
	}
	
	public void showDocument(URL url)
	{
	}
	
	public void showDocument(URL url, String target)
	{
	}
	
	public void showStatus(String status)
	{
	}

        public InputStream getStream(String key) {
	    // for JDK 1.4 compatibility
	    throw new UnsupportedOperationException("no getStream()");
	}

        public Iterator getStreamKeys() {
	    // for JDK 1.4 compatibility
	    throw new UnsupportedOperationException("no getStreamKeys()");
	}

        public void setStream(String key, InputStream stream) {
	    // for JDK 1.4 compatibility
	}

}

