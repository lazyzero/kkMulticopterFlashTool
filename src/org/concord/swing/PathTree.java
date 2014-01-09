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

import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class PathTree
extends JTree
{
    public static final int SINGLE_SELECTION = 0;
    public static final int MULTIPLE_SELECTION = 1;
	protected Hashtable objectTable = new Hashtable();
	protected String separator = "/";
    
    protected DefaultMutableTreeNode top;
	protected String rootName = "Root";
    protected Hashtable nodeTable = new Hashtable();

    public PathTree(String rootName)
    {
        super();
		this.rootName = rootName;
		top  = new DefaultMutableTreeNode(rootName);
        ((DefaultTreeModel) treeModel).setRoot(top);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }
	
	public void setSeparator(String sep)
	{
		separator = sep;
	}
	
	public String getSeparator()
	{
		return separator;
	}
    
    public void setSelectionMode(int selectionMode)
    {
        if (selectionMode == SINGLE_SELECTION)
        {
            getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        else if (selectionMode == MULTIPLE_SELECTION)
        {
            getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        }
    }
    
    public String getPathName()
    {
        String pathName = "";
        TreePath treePath = getSelectionPath();
        if (treePath instanceof TreePath)
        {
            Object [] path = treePath.getPath();
            for (int i = 1; i < path.length; i++)
            {
                pathName += separator + path[i].toString();
            }
        }
        return pathName.substring(1);
    }
	
	public Object getPathObject()
	{
		return objectTable.get(getPathName());
	}
    
    public void addPathObject(Object pathObject)
    {
        StringTokenizer tokens = new StringTokenizer(pathObject.toString(), "\\/.");
        String [] pathElements = new String[tokens.countTokens()];
        int n = 0;
        while (tokens.hasMoreTokens())
        {
            pathElements[n] = tokens.nextToken();
            n++;
        }
		objectTable.put(pathObject.toString(), pathObject);
        String currentPath = rootName;
        for (int i = 0; i < n; i++)
        {
			String parentPath = currentPath;
			currentPath += separator + pathElements[i];
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTable.get(currentPath);
            if (node == null)
            {
                node = new DefaultMutableTreeNode(pathElements[i]);
				nodeTable.put(currentPath, node);
				if (parentPath.equals(rootName))
                {
                    top.add(node);
                }
                else
                {
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) nodeTable.get(parentPath);
                    parent.add(node);
                }
            }
        }
    }
}

