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

package org.concord.swing.about;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class AboutTextFromHTML {
	
	static String TYPE = "text/html";
	static final String[] tabNames = {"Version", "License", "Credits", "Acknowledgements"};
	
	public static JTabbedPane getTabbedPane(String interactiveName) {
		JTabbedPane tabbedPane = new JTabbedPane();
		
		for (int i = 0; i < tabNames.length; i++) {
			tabbedPane.add(tabNames[i], read("html/" + interactiveName + tabNames[i] + ".html"));
		}
		
		return tabbedPane;
	}
	
	private static JComponent read(String filename) {
		JEditorPane retval;
		
		URL fileURL;
		InputStream urlStream;
		BufferedReader bReader;
		StringBuffer contents = new StringBuffer();
		
		try {
			
			urlStream = AboutTextFromHTML.class.getResourceAsStream(filename);
			bReader = new BufferedReader(new InputStreamReader(urlStream));
			
			for ( String line = bReader.readLine(); line != null ; line = bReader.readLine() ) {
				contents.append(line);
			}
			
			
		} catch (Exception e) {
		}
		
		retval = new JEditorPane("text/html",contents.toString());
		retval.setFont(new Font("Serif", Font.PLAIN, 12));
		retval.setEditable(false);
		
		JScrollPane scroll = new JScrollPane(retval, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		return scroll;
	}
}
