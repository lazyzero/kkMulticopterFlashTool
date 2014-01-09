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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;


public class AbstractFileFinder
{
	class HTMLDirectoryParser
	extends HTMLEditorKit
	{
		class Callback
		extends ParserCallback
		{
			protected List entries = new Vector();
			protected Stack attributeStack = new Stack();
			
			public Callback()
			{
			}
			
			public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
			{
				super.handleStartTag(t, a, pos);
				if (t == HTML.Tag.A)
				{
					String href = (String) a.getAttribute(HTML.Attribute.HREF);
					entries.add(href);
				}
			}
			
			public void handleEndTag(HTML.Tag t, int pos)
			{
			}
			
			public void clearEntries()
			{
				entries.clear();
			}
			
			public List getEntries()
			{
				return entries;
			}
		}
		protected URL base;
		protected Parser parser;
		protected Callback callback;
		
		public HTMLDirectoryParser(URL url)
		{
			base = url;
			parser = getParser();
			callback = new Callback();
		}
		
		protected Reader getURLReader(String dir)
		{
			URL url;
			Reader reader = null;
			try
			{
				url = new URL(abstractDirectory, dir);
				reader = new InputStreamReader(url.openStream());
			}
			catch (MalformedURLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return reader;
		}
		
		public void parse(String dirName)
		{
			try
			{
				callback.clearEntries();
				parser.parse(getURLReader(dirName), callback, false);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public List getEntries()
		{
			return callback.getEntries();
		}
	}
	
	protected URL abstractDirectory;
	protected HTMLDirectoryParser htmlDirectoryParser;
	protected Map directoryMap;
	protected TreeSet directorySet;
	
	public AbstractFileFinder()
	{
	}
	
	public URL getAbstractDirectory()
	{
		return abstractDirectory;
	}
	
	protected void addDirectorySetEntry(String entryName, URL url)
	{
		File entryFile = new File(entryName);
		String parentPath = entryFile.getParent();
		parentPath = (parentPath == null) ? "" : parentPath + "/";
		parentPath = parentPath.replace(File.separatorChar, '/');
		String key = "jar:" + url.getFile() + parentPath;
		TreeSet set = findDirectorySet(key, url);
		set.add(entryFile);
		File file = new File(parentPath);
		entryName = file.getParent();
		if (entryName != null)
			addDirectorySetEntry(entryName + "/", url);	
	}
	
	protected TreeSet findDirectorySet(String key, URL url)
	{
		TreeSet set = (TreeSet) directoryMap.get(key);
		if (set == null)
		{
			set = new TreeSet();
			directoryMap.put(key, set);
		}
		return set;
	}
	
	protected TreeSet initializeDirectorySet(URL url)
	{
		TreeSet set = (TreeSet) directoryMap.get(url.toString());
		if (set == null)
		{
			try
			{
				JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
				JarFile jarFile = jarConnection.getJarFile();
				Enumeration jarEntries = jarFile.entries();
				while (jarEntries.hasMoreElements())
				{
					JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
					String entryName = jarEntry.getName();
					addDirectorySetEntry(entryName, url);
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			set = (TreeSet) directoryMap.get(url.toString());
		}
		return set;
	}
	
	public void setAbstractDirectory(URL url)
	{
		abstractDirectory = url;
		if (abstractDirectory.getProtocol().toLowerCase().equals("http"))
		{
			htmlDirectoryParser = new HTMLDirectoryParser(abstractDirectory);
		}
		if (abstractDirectory.getProtocol().toLowerCase().equals("jar"))
		{
			directorySet = initializeDirectorySet(abstractDirectory);
		}
	}
	
	public void setDirectoryMap(Map map)
	{
		directoryMap = map;
	}
	
	public void collectEntries(String dirName, List entries)
	{
		if (abstractDirectory.getProtocol().toLowerCase().equals("http"))
			collectWebEntries(dirName, entries);
		if (abstractDirectory.getProtocol().toLowerCase().equals("jar"))
			collectJarEntries(dirName, entries);
	}
	
	public void collectJarEntries(String dirName, List entries)
	{
		String key = "jar:" + abstractDirectory.getFile() + dirName;
		directorySet = (TreeSet) directoryMap.get(key);
		Iterator directoryEntries = directorySet.iterator();
		while (directoryEntries.hasNext())
		{
			File entryFile = (File) directoryEntries.next();
			String entryName = entryFile.getName();
			if (directoryMap.get(key + entryName + "/") != null)
				entryName += "/";
			entries.add(entryName);
		}
	}
	
	public void collectWebEntries(String dirName, List entries)
	{
		htmlDirectoryParser.parse(dirName);
		List tagList = htmlDirectoryParser.getEntries();
		for (int i = 0; i < tagList.size(); i++)
		{
			String href = (String) tagList.get(i);
			if (href.startsWith("/"))
				continue;
			if (href.startsWith("?"))
				continue;
			URI uri;
			try
			{
				uri = new URI(href);
    			if (uri.isAbsolute())
    				continue;
			}
			catch (URISyntaxException e)
			{
				continue;
			}
			entries.add(href); 
		}
	}
}
