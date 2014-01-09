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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
    
public class PathDialog
extends JDialog
implements ActionListener, ListSelectionListener
{
    public final static String JAVA_HOME = "java.home";
    public final static String USER_DIR = "user.dir";
    public final static String OS_NAME = "os.name";
    public final static String PREFIX_BREAK = "$";
    protected JList pathList = new JList();
    protected Vector paths = new Vector();
    protected JButton doneButton = new JButton("Done");
    protected JButton addButton = new JButton("Add");
    protected JButton modifyButton = new JButton("Modify");
    protected JButton removeButton = new JButton("Remove");
    protected JButton upButton = new JButton("Move Up");
    protected JButton downButton = new JButton("Move Down");
    protected JButton javaHomeButton = new JButton();
    protected JPanel controlPanel = new JPanel();
    protected String javaHome;
    protected JScrollPane listScroll = new JScrollPane(pathList);
    protected JFileChooser selectPathChooser = new JFileChooser();
    protected ModifyPathDialog modifyPathDialog;
    protected Properties prefixTable = new Properties();
    protected JPanel contentPane;
    protected String selectedItem;
    
    public PathDialog(JFrame frame)
    {
        super(frame, "Add and Remove Path Components", true);
        
        addPrefix(USER_DIR, System.getProperty(USER_DIR, ""));
        
        setJavaHome(System.getProperty(JAVA_HOME, ""));
        modifyPathDialog = new ModifyPathDialog(frame);
        
        controlPanel.setLayout(new GridLayout(0, 1));
        controlPanel.add(doneButton);
        controlPanel.add(addButton);
        controlPanel.add(modifyButton);
        controlPanel.add(removeButton);
        controlPanel.add(upButton);
        controlPanel.add(downButton);

        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(listScroll, "Center");
        contentPane.add(controlPanel, "East");
        contentPane.add(javaHomeButton, "South");

        doneButton.addActionListener(this);
        addButton.addActionListener(this);
        modifyButton.addActionListener(this);
        removeButton.addActionListener(this);
        upButton.addActionListener(this);
        downButton.addActionListener(this);
        javaHomeButton.addActionListener(this);
        
        removeButton.setEnabled(false);
        upButton.setEnabled(false);
        downButton.setEnabled(false);
        
        pathList.addListSelectionListener(this);
        selectPathChooser.setMultiSelectionEnabled(false);
        selectPathChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        setLocation(200, 200);
        pack();
    }
    
    public void addPrefix(String prefix, String value)
    {
        prefixTable.put(prefix, value);
    }
    
    protected String substitutePrefix(String element)
    {
        StringTokenizer tokens = new StringTokenizer(element, PREFIX_BREAK);
        if (tokens.hasMoreTokens())
        {
            String prefix = tokens.nextToken();
            if (tokens.hasMoreTokens())
                element = tokens.nextToken();
            String pathPrefix = prefixTable.getProperty(prefix);
            if (pathPrefix == null)
                pathPrefix = System.getProperty(prefix, "");
            return pathPrefix + element;
        }
        return element;
    }
    
    protected String removePrefix(String element, String toRemove)
    {
        StringTokenizer tokens = new StringTokenizer(element, PREFIX_BREAK);
        if (tokens.hasMoreTokens())
        {
            String prefix = tokens.nextToken();
            if (toRemove.equals(prefix) && tokens.hasMoreTokens())
                element = tokens.nextToken().substring(1);
        }
        return element;
    }
    
    protected String matchPrefix(String element)
    {
        Enumeration enumer = prefixTable.keys();
        while (enumer.hasMoreElements())
        {
            String key = (String) enumer.nextElement();
            String value = (String) prefixTable.get(key);
            if (element.startsWith(value))
            {
                element = element.substring(value.length());
                element = key + PREFIX_BREAK + element;
                break;
            }
        }
        return element;
    }
    
    public String getPath(boolean expand)
    {
        return getPath(expand, null);
    }
    
    public String getPath(boolean expand, String toRemove)
    {
        String path = "";
        int n = paths.size();
        for (int i = 0; i < n; i++)
        {
            String pathElement = (String) paths.elementAt(i);
            if (toRemove instanceof String)
                pathElement = removePrefix(pathElement, toRemove);
            if (expand)
                pathElement = substitutePrefix(pathElement);
            path += pathElement;
            if (i < (n - 1))
                path += File.pathSeparator;
        }
        System.out.println(path);
        return path;
    }
    
    public String getPath()
    {
        return getPath(true, "user.dir");
    }
    
    public void setPath(String path)
    {
        if ((path instanceof String) && (path.length() > 0))
        {
            StringTokenizer tokens = new StringTokenizer(path, ";");
            paths.removeAllElements();
            while (tokens.hasMoreTokens())
            {
                paths.addElement(tokens.nextToken());
            }
            selectedItem = null;
            pathList.setListData(paths);
        }
    }
    
    public void addToPath(String pathElement)
    {
        if (pathElement instanceof String)
        {
            pathElement = matchPrefix(pathElement);
            paths.addElement(pathElement);
            pathList.setListData(paths);
        }
    }
    
    public void addToPath(String prefix, String partialPath)
    {
        if (partialPath instanceof String)
        {
            paths.addElement(prefix + PREFIX_BREAK + partialPath);
            pathList.setListData(paths);
        }
    }
    
    public void setJavaHome(String path)
    {
        File javaDir = new File(path);
        if (javaDir.exists() && javaDir.isDirectory())
        {
            javaHome = path;
            javaHomeButton.setText("Set Java Home Directory: " + javaHome);
            prefixTable.put(JAVA_HOME, path);
        }
    }
    
    public String getJavaHome()
    {
        return javaHome;
    }
    
    public void actionPerformed(ActionEvent event)
    {
        Object source = event.getSource();
        JButton button = (source instanceof JButton) ? (JButton) source : null;
        if (button == doneButton)
        {
            setVisible(false);
        }
        else if (button == addButton)
        {
            selectPathChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            selectPathChooser.showDialog(this, "Select Path");
            File selectedFile = selectPathChooser.getSelectedFile();
            try
            {
                selectedItem = selectedFile.getCanonicalPath();
                selectedItem = matchPrefix(selectedItem);
                paths.addElement(selectedItem);
                pathList.setListData(paths);
                pathList.setSelectedValue(selectedItem, true);
            }
            catch (Exception e)
            {
            }
        }
        else if (button == modifyButton)
        {
            int index = paths.indexOf(selectedItem);
            modifyPathDialog.setPathElement(selectedItem);
            modifyPathDialog.show();
            selectedItem = modifyPathDialog.getPathElement();
            paths.setElementAt(selectedItem, index);
            pathList.setSelectedValue(selectedItem, true);
        }
        else if (button == removeButton)
        {
            if (selectedItem instanceof String)
            {
                paths.removeElement(selectedItem);
                selectedItem = null;
                pathList.setListData(paths);
            }
        }
        else if (button == upButton)
        {
            if (selectedItem instanceof String)
            {
                int index = paths.indexOf(selectedItem);
                if (index > 0)
                {
                    paths.removeElementAt(index);
                    paths.insertElementAt(selectedItem, index - 1);
                    pathList.setSelectedValue(selectedItem, true);
                }
            }
        }
        else if (button == downButton)
        {
            if (selectedItem instanceof String)
            {
                int index = paths.indexOf(selectedItem);
                int n = paths.size();
                if ((index > -1) && (index < (n - 1)))
                {
                    paths.removeElementAt(index);
                    paths.insertElementAt(selectedItem, index + 1);
                    pathList.setSelectedValue(selectedItem, true);
                }
            }
        }
        else if (button == javaHomeButton)
        {
            selectPathChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            selectPathChooser.showDialog(this, "Select Java Home");
            File selectedFile = selectPathChooser.getSelectedFile();
            try
            {
                String javaDir = selectedFile.getCanonicalPath();
                if (javaDir.indexOf(PREFIX_BREAK) > -1)
                {
                    int length = selectPathChooser.getCurrentDirectory().toString().length();
                    javaDir = selectedItem.substring(length + 1);
                }
                setJavaHome(javaDir);
            }
            catch (Exception e)
            {
                setJavaHome(System.getProperty("java.home"));
            }
        }
    }

    public void valueChanged(ListSelectionEvent event)
    {
        Object object = event.getSource();
        if (object instanceof JList)
        {
            JList list = (JList) object;
            selectedItem = (String) list.getSelectedValue();
            boolean haveSelection = (selectedItem instanceof String);
            removeButton.setEnabled(haveSelection);
            upButton.setEnabled(haveSelection);
            downButton.setEnabled(haveSelection);
        }
    }
}

class ModifyPathDialog
extends JDialog
implements ActionListener
{
    protected JTextField field = new JTextField();
    protected JLabel label = new JLabel("Modify Path Element:                      ");
    protected JButton doneButton = new JButton("Done");
    protected JButton cancelButton = new JButton("Cancel");
    protected JPanel buttonPanel = new JPanel();
    protected String pathElement;
    
    public ModifyPathDialog(JFrame parent)
    {
        super(parent, "Modify Path Element String", true);
        buttonPanel.add(doneButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(label, "North");
        getContentPane().add(field, "Center");
        getContentPane().add(buttonPanel, "South");
        doneButton.addActionListener(this);
        cancelButton.addActionListener(this);
        setLocation(200, 200);
        pack();
    }
    
    public void actionPerformed(ActionEvent event)
    {
        Object source = event.getSource();
        JButton button = (source instanceof JButton) ? (JButton) source : null;
        if (button == doneButton)
        {
            pathElement = field.getText();
        }
        setVisible(false);
    }
    
    public void setPathElement(String element)
    {
        pathElement = element;
        field.setText(element);
    }
    
    public String getPathElement()
    {
        return pathElement;
    }
}
