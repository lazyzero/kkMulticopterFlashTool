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
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;

public class AbstractSystemView
extends FileSystemView
{
	protected URI abstractRoot;
	protected File abstractRootParent;
	protected Icon folderIcon;
	
	
	public AbstractSystemView()
	{
		super();
	}
	
	public AbstractSystemView(URI rootURI)
	{
		super();
		abstractRoot = rootURI;
		folderIcon = getSystemIcon(getHomeDirectory());
	}
	
	public boolean isFileSystem(File file)
	{
		if (file instanceof AbstractFile)
			return false;
		return super.isFileSystem(file);
	}
	
	public File createNewFolder(File containingDir)
	throws IOException
	{
		if (containingDir instanceof AbstractFile)
			return null;
		else
		{
			abstractRootParent = containingDir;
			File filePath = new File(abstractRoot.getPath());
			return AbstractFile.createFile(abstractRootParent, filePath.getName(), abstractRoot);
		}
	}
	
	public Boolean isTraversable(File dir)
	{
		if (dir instanceof AbstractFile)
			return Boolean.valueOf(dir.isDirectory());
		return super.isTraversable(dir);
	}
	
	public File getParentDirectory(File dir)
	{
		if (dir instanceof AbstractFile)
		{
			File parentDir = ((AbstractFile) dir).getAbstractFileParent();
			if (parentDir == null)
				parentDir = getHomeDirectory();
			return parentDir;
		}
		return super.getParentDirectory(dir);
	}
	
	public Icon getSystemIcon(File file)
	{
		if (file instanceof AbstractFile)
		{
			if (file.isDirectory())
				return UIManager.getIcon("FileView.directoryIcon");
			return UIManager.getIcon("FileView.fileIcon");
		}
		else if (file.getName().endsWith(".jar!") || file.getName().endsWith(".zip!"))
		{
			return UIManager.getIcon("FileView.directoryIcon");
		}
		return super.getSystemIcon(file);
	}
	
	public String getSystemDisplayName(File file)
	{
		if (file instanceof AbstractFile)
			return file.getName();
		return super.getSystemDisplayName(file);
	}
	
	public File [] getFiles(File dir, boolean hiding)
	{
		if (dir instanceof AbstractFile)
		{
			AbstractFile abstractFile = (AbstractFile) dir;
			return abstractFile.getAbstractFiles();
		}
		File [] files = super.getFiles(dir, hiding);
		for (int i = 0; i < files.length; i++)
		{
			File abstractFile = AbstractFile.createFile(files[i]);
			if (abstractFile != null)
				files[i] = abstractFile;
		}
		return files;
	}
	
	public static void main(String [] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (InstantiationException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IllegalAccessException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JFileChooser chooser = new JFileChooser();
		try
		{
			URI uri = new URI(args[0]);
			AbstractSystemView systemView = new AbstractSystemView(uri);
			chooser.setFileSystemView(systemView);
			int result = chooser.showOpenDialog(null);
			switch (result)
			{
				case JFileChooser.CANCEL_OPTION:
				break;
				case JFileChooser.APPROVE_OPTION:
				break;
				case JFileChooser.ERROR_OPTION:
				break;
			}
		}
		catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}