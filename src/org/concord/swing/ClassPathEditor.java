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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

public class ClassPathEditor
extends JPanel
implements ListSelectionListener, ActionListener
{
	public static final File JAVA_HOME = new File(System.getProperty("java.home"));
	public static final String BOOT_CLASS_PATH = System.getProperty("sun.boot.class.path");
	public static final String CLASS_PATH = System.getProperty("java.class.path");
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");
	public static final String FOLDER_PREFIX = "!.folder";
	public static final String CLASS_SUFFIX = ".class";
	
	public final File userDir = new File(System.getProperty("user.dir"));
	public final File userHome = new File(System.getProperty("user.home"));
	protected LocalClassLoader localClassLoader = new LocalClassLoader();
	protected JFileChooser classFileChooser = new JFileChooser();
	protected JList jarList = new JList();
	protected JScrollPane jarListScroll = new JScrollPane(jarList);
	protected JDialog dialog;
	protected Map bootClassPathURLS = new TreeMap();
	protected Map classPathURLS = new TreeMap();
	protected String systemPrefix;
	protected Class selectedClass;
	
	public ClassPathEditor()
	{
		setLayout(new BorderLayout());
		classFileChooser.setFileSystemView(new AbstractSystemView());
		classFileChooser.setFileFilter(new ClassFileFilter());
		classFileChooser.addActionListener(this);
		systemPrefix = System.class.getResource("System.class").getPath();
		systemPrefix = systemPrefix.substring(0, systemPrefix.indexOf("!"));
		systemPrefix = systemPrefix.substring(0, systemPrefix.lastIndexOf("/") + 1);
		initializeClassPath(BOOT_CLASS_PATH, bootClassPathURLS);
		initializeClassPath(CLASS_PATH, classPathURLS);
		classPathURLS.putAll(bootClassPathURLS);
		Vector jars = new Vector((Collection) classPathURLS.keySet());
		jarList.setListData(jars);
		jarList.addListSelectionListener(this);
		jarList.setSelectedIndex(0);
		add(jarListScroll, "Center");
		add(classFileChooser, "South");
	}
	
	protected void initializeClassPath(String classPath, Map classPathMap)
	{
		StringTokenizer pathTokens = new StringTokenizer(classPath, PATH_SEPARATOR);
		while (pathTokens.hasMoreTokens())
		{
			String fileName = pathTokens.nextToken();
			File file = new File(fileName);
			if (fileName.endsWith(".jar") || file.isDirectory())
			{
				if (file.exists())
				{
					try
					{
						URL pathURL = file.toURL();
						classPathMap.put(pathURL.toString(), file);
					}
					catch (IOException e)
					{
						System.out.println("File to URL failure: " + file.toString());
					}
				}
			}
		}
	}
	
	protected boolean isBootPathURL(URL url)
	{
		return bootClassPathURLS.get(url) instanceof URL;
	}
	
	public String chooseClassName()
	{
		File file = classFileChooser.getSelectedFile();
		File classRoot = (File) classPathURLS.get(jarList.getSelectedValue());
		String rootPath = classRoot.toString();
		if (file != null)
		{
			String filePath = file.toString();
			if (filePath.startsWith(rootPath))
			{
				if (filePath.endsWith(CLASS_SUFFIX))
				{
					filePath = filePath.substring(rootPath.length()).replace('\\', '/');
					filePath = filePath.replace('/', '.');
					filePath = filePath.substring(0, filePath.length() - CLASS_SUFFIX.length());
					if (filePath.startsWith(FOLDER_PREFIX))
					{
						filePath = filePath.substring(FOLDER_PREFIX.length());
					}
					filePath = filePath.substring(1);
					return filePath;
				}
			}
		}
		return null;
	}
	
	public Class chooseClass()
	{
		String className = chooseClassName();
		if (className != null)
		{
			try
			{
				return localClassLoader.loadClass(className);
			}
			catch (ClassNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
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
		ClassPathEditor classPathEditor = new ClassPathEditor();
		JDialog dialog = new JDialog((JFrame) null, "Class Path Editor Test");
		classPathEditor.showClassPathEditor(dialog);
		System.out.println("Selected class = " + classPathEditor.getSelectedClass());
	}
	
	public void showClassPathEditor(JDialog dialog)
	{
		this.dialog = dialog;
		dialog.setModal(true);
		dialog.getContentPane().add(this);
		dialog.setBounds(50, 50, 800, 600);
		dialog.setVisible(true);
	}

	public void valueChanged(ListSelectionEvent e)
	{
		Object selection = jarList.getSelectedValue();
		File file = (File) classPathURLS.get(selection);
		classFileChooser.setSelectedFile(file);
	}
	
	public class ClassFileFilter
	extends FileFilter
	{

		public boolean accept(File file)
		{
			if (file.isDirectory())
				return true;
			if (file.getName().toLowerCase().endsWith(".jar!"))
				return true;
			if (file.getName().toLowerCase().endsWith(".zip!"))
				return true;
			return file.getName().toLowerCase().endsWith(CLASS_SUFFIX);
		}

		public String getDescription()
		{
			return "Java class file (*.class)";
		}
		
	}
	
	public class LocalClassLoader
	extends URLClassLoader
	{
		public LocalClassLoader()
		{
			super(new URL[0], ClassPathEditor.class.getClassLoader());
		}
		
		public void addURLPathElement(URL url)
		{
			addURL(url);
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		selectedClass = chooseClass();
		dialog.setVisible(false);
	}
	
	public Class getSelectedClass()
	{
		return selectedClass;
	}
}