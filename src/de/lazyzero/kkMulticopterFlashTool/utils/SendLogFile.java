/**
 * KKMulticopterFlashTool, a avrdude GUI for flashing KK boards and other
 *   equipment.
 *   Copyright (C) 2011 Christian Moll
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lazyzero.kkMulticopterFlashTool.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

public class SendLogFile {
	private Desktop desktop;
	

	public SendLogFile() {
		if (Desktop.isDesktopSupported()) {
	        desktop = Desktop.getDesktop();
		}
		
		
	}
	
	public boolean sendMail(String version, File file, String output)  {
		String filecontent = output;
		filecontent=filecontent.concat("\n------------------------------------------------------------------\n");
		
		try {
			FileReader fr = new FileReader(file);
			int c;
			while((c = fr.read())!=-1) {
				filecontent=filecontent.concat(((char)c)+"");
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			
			String defaultContent = "Please add here some additional information to help me solving the problem.\n\n";
			URI mailtoURI = new URI("mailto", "christian@chrmoll.de?SUBJECT=KKmulticopterFlashTool: "+version+"&BODY=" + defaultContent + filecontent, null);
			desktop.mail(mailtoURI);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean sendMail(LinkedHashMap<String, String> data) {
		Set<String> keys = data.keySet();
		String content = "";
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			content=content.concat(key + "==" + data.get(key)+"\n");
		}
		
		try {
			URI mailtoURI = new URI("mailto", "christian@chrmoll.de?SUBJECT=KKmulticopterFlashTool kkcheck: "+KKMulticopterFlashTool.VERSION+"&BODY=" +  content, null);
			desktop.mail(mailtoURI);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static void main (String args[]) {
		SendLogFile slf = new SendLogFile();
		slf.sendMail("0.21", new File("kkLogging.txt"), "something");
	}

	
}
