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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

public class AbstractFile
extends File
{
	public static final String FOLDER_EXT = ".folder";
	public static final String JAR_EXT = ".jar";
	public static final String ZIP_EXT = ".zip";
	protected AbstractFileFinder abstractFileFinder = new AbstractFileFinder();
	protected URL abstractURL;
	protected TreeMap directoryMap;
	protected AbstractFile abstractFileParent;
	
	public static File createFile(File parent, String name, URI uri)
	{
		return new AbstractFile(parent, name, uri);
	}
	
	public static File createFile(File parent, String name)
	{
		if (parent instanceof AbstractFile)
			return new AbstractFile((AbstractFile) parent, name);
		return null;
	}
	
	protected static String getRootName(URI uri)
	{
		String schemePart = uri.getSchemeSpecificPart();
		StringTokenizer tokens = new StringTokenizer(schemePart, "/");
		int n = tokens.countTokens();
		String [] parts = new String[n];
		for (int i = 0; i < n; i++)
		{
			parts[i] = tokens.nextToken();
		}
		return parts[parts.length - 1];
	}
	
	public static File createFile(File folderFile)
	{
		String uriString = null;
		Properties folder = new Properties();
		try
		{
			if (folderFile.getName().endsWith(FOLDER_EXT))
			{
				folder.load(new FileInputStream(folderFile));
				uriString = folder.getProperty("folder.uri", "");
			}
			else if ((folderFile.getName().endsWith(JAR_EXT)) || (folderFile.getName().endsWith(ZIP_EXT)))
			{
				uriString = folderFile.toURL().toString();
				uriString = "jar:" + uriString + "!/";
			}
			if (uriString != null)
			{
				uriString = uriString.replace(' ', '+');
				URI uri = new URI(uriString);
				File filePath = new File(getRootName(uri));
				return new AbstractFile(folderFile.getParentFile(), filePath.getName(), new URI(uriString));				
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected AbstractFile(File parent, String name, URI uri)
	{
		super(parent, name + FOLDER_EXT);
		try
		{
			abstractURL = new URL(uri.toString().replace('+', ' '));
			directoryMap = new TreeMap();
			abstractFileFinder.setDirectoryMap(directoryMap);
			abstractFileFinder.setAbstractDirectory(abstractURL);
			if ((! name.endsWith(".jar!")) && (! name.endsWith(".zip!")))
			{
				Properties folder = new Properties();
				folder.setProperty("folder.uri", uri.toString());
				folder.store(new FileOutputStream(this), "");
			}
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected AbstractFile(AbstractFile parent, String name)
	{
		super(parent, name);
		abstractFileParent = (AbstractFile) parent;
		directoryMap = abstractFileParent.directoryMap;
		try
		{
			abstractURL = new URL(abstractFileParent.abstractURL, name);
			if (name.endsWith("/"))
			{
				abstractFileFinder.setDirectoryMap(directoryMap);
				abstractFileFinder.setAbstractDirectory(abstractURL);
			}
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public URL getURL()
	{
		return abstractURL;
	}
	
	public File getCanonicalFile()
	{
		return this;
	}
	
	public File [] getAbstractFiles()
	{
		List list = new Vector();
		String dirName = getName() + "/";
		String urlString = abstractURL.toString();
		if (urlString.endsWith(dirName))
			dirName = "";
		abstractFileFinder.collectEntries(dirName, list);
		File [] abstractFiles = new File[list.size()];
		for (int i = 0; i < abstractFiles.length; i++)
		{
			abstractFiles[i] = new AbstractFile(this, (String) list.get(i));
		}
		return abstractFiles;
	}
	
	public File getAbstractFileParent()
	{
		return abstractFileParent;
	}
	
	public boolean isDirectory()
	{
		return abstractFileFinder.getAbstractDirectory() != null;
	}
	
	public TreeMap getDirectoryMap()
	{
		return directoryMap;
	}
	
	public void setDirectoryMap(TreeMap map)
	{
		directoryMap = map;
	}
	
	public boolean exists()
	{
		return abstractURL != null;
	}
	
	public String getName()
	{
		String name = super.getName();
		if (name.endsWith(FOLDER_EXT))
		{
			name = name.substring(0, name.length() - FOLDER_EXT.length());
		}
		return name;
	}
}
