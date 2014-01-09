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
 * Created on Aug 10, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.concord.swing;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author swang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CCFilenameFilter implements FilenameFilter{
	public static final int DIRECTORY_AND_FILE = 1;
	public static final int DIRECTORY_ONLY = 2;

	private String acceptableExtension = null;

	public CCFilenameFilter() {
		super();
	}
	
	public CCFilenameFilter(String extension) {
		setAcceptableExtension(extension);
	}
	
	public void setAcceptableExtension(String extension) {
		this.acceptableExtension = extension;		
	}
	public String getAcceptableExtension() {
		return acceptableExtension;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File arg0, String arg1) {
		//System.out.println("pathseparator: " + File.separator);
		File file = new File(arg0 + File.separator + arg1);
		
		if(acceptableExtension == null || acceptableExtension.equals("*")) 
			return true;
		
		if(file.isDirectory()) return true;

		String ext = getExtension(file);		
		//System.out.println(ext);
		
		if(ext == null) return false;
		
		if(ext.equalsIgnoreCase(acceptableExtension)) {
			//System.out.println("equals. accepted " + ext);
			return true;
		}

		return false;
	}
	
	public String getExtension(File file) {
		String filename = file.getName();
		if(filename.length() == 0) return null;
		if(filename.lastIndexOf(".") == -1) return null;
		String ext = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
		//System.out.println(ext);
		return ext;
	}
}
