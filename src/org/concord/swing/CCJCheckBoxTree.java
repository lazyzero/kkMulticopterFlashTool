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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class CCJCheckBoxTree extends JTree implements TreeSelectionListener{
    protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel treeModel;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();

    // allnodes in the tree
    private Vector allNodes = new Vector();
	
	private HashMap nodesMap = new HashMap();
	private TreePath lastSelectedPath = null;
	
	private MouseListener[] mouseListeners;
		
	public CCJCheckBoxTree() {
		this(new DefaultMutableTreeNode());
	}
	
	public CCJCheckBoxTree(TreeNode node) {
		this(node, false);
	}
	
	public CCJCheckBoxTree(String rootName) {
		this(new DefaultMutableTreeNode(new NodeHolder(rootName), true));
	}
	
	public CCJCheckBoxTree(TreeNode node, boolean asksAllowsChildren) {
		super(node, asksAllowsChildren);

		rootNode = (DefaultMutableTreeNode)node;
		NodeHolder rootBox = (NodeHolder) rootNode.getUserObject();
		allNodes.addElement(rootBox);
		nodesMap.put(rootBox, rootNode);
		
		treeModel = new DefaultTreeModel(rootNode);
		//treeModel.addTreeModelListener(new MyTreeModelListener());
		
		cellRenderer = new CCJCheckBoxRenderer();

		setModel(treeModel);
		setCellRenderer(cellRenderer);
		setEditable(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setShowsRootHandles(true);
		
		setToolTipText("Click to select; double-click to check/uncheck");

		mouseListeners = this.getMouseListeners();
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				for(int i = 0; i < mouseListeners.length; i++)
					removeMouseListener(mouseListeners[i]);				
			}
			
			public void mouseReleased(MouseEvent e) {
				TreePath path = getPathForLocation(e.getX(), e.getY());
				
				Rectangle bounds = getPathBounds(path);

				if(path != null) {	// selected a path
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
					
					Object obj = node.getUserObject();
					if(obj instanceof CCJCheckBoxTree.NodeHolder) {
						CCJCheckBoxTree.NodeHolder nodeHolder = (CCJCheckBoxTree.NodeHolder)obj;
						
						// if clicked on the checkbox:
						//	  if nothing highlighted, highlight it;
						//	  if it is checked and highlighted, uncheck it and highlight nothing;
						//	  if it is checked and not hightlight, just check it;
						//	  if it is unchecked, check it
						//
						// if clicked on the text:
						//	  highlight the path;
						//	  if unchecked, check it
						//
						// if clicked anywhere else:
						//	  highlight nothing
						
						if(e.getX() < bounds.x + 20 &&	// clicked on checkbox: 
								e.getX() > bounds.x &&
								e.getY() < bounds.getCenterY() + bounds.height/2 &&
								e.getY() > bounds.getCenterY() - bounds.height/2) {

							nodeHolder.checked = !nodeHolder.checked;
							
							// highlight nothing if a highlighted path is unchecked
							if(!nodeHolder.checked) {
								if(lastSelectedPath == path) {
									lastSelectedPath = null;
								}
							} else {
								if(lastSelectedPath == null) {
									lastSelectedPath = path;
								}
							}

							setSelectionPath(lastSelectedPath);
							
						} else if(e.getX() > bounds.x + 20 && // clicked on text:
								e.getX() < bounds.x + bounds.getWidth() &&
								e.getY() < bounds.getCenterY() + bounds.height/2 &&
								e.getY() > bounds.getCenterY() - bounds.height/2) {
							
							for(int i = 0; i < mouseListeners.length; i++)
								addMouseListener(mouseListeners[i]);
							
							if(!nodeHolder.checked) nodeHolder.checked = true;

							lastSelectedPath = path;
							
							setSelectionPath(lastSelectedPath);
						} else {	// clicked elsewhere:
							for(int i = 0; i < mouseListeners.length; i++)
								addMouseListener(mouseListeners[i]);
							
							if(e.getY() > getVisibleHeight()) 
								lastSelectedPath = null;
							else 
								lastSelectedPath = path;
							
							setSelectionPath(lastSelectedPath);
						}
						checkRelatedNodeFor(node);
					}

					repaint();					
				} else {	// not selected any path: remove current selected path
					for(int i = 0; i < mouseListeners.length; i++)
						addMouseListener(mouseListeners[i]);
					
					lastSelectedPath = null;
					setSelectionPath(lastSelectedPath);
				}
			}
		});
	}
	
    public String renameCurrentNode() {
    	String name = null;
        TreePath currentSelection = getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            CCJCheckBoxTree.NodeHolder box = (CCJCheckBoxTree.NodeHolder)currentNode.getUserObject();
            String newText = JOptionPane.showInputDialog(this, "Enter new name", box.name);
            if(newText != null && newText.trim().length() > 0) {
            	box.name = newText;
            	name = newText;
            	repaint();
            }
        }
        return name;
    }

    
    
    public Object removeCurrentNode() {
    	getUI().cancelEditing(this);
    	//SwingUtilities.invokeLater(new Runnable() {
    		//public void run() {
    	        TreePath currentSelection = getSelectionPath();
    	        if (currentSelection != null) {
    	            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
    	                         (currentSelection.getLastPathComponent());
    	            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
    	            if (parent != null) {
    	            	Object obj = currentNode.getUserObject();
   	            		treeModel.removeNodeFromParent(currentNode);
   	            		allNodes.removeElement(obj);
   	            		nodesMap.remove(obj);
   	            		return obj;
    	            }
    	        }
    	        // Either there was no selection, or the root was selected.
    	        toolkit.beep();
    		//}
    	//});
    	        return null;
    }

    /** Add child to the currently selected node. */
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = getSelectionPath();

        if (parentPath == null) {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode)
                         (parentPath.getLastPathComponent());
        }

        NodeHolder nodeHolder = null;
        if(!(child instanceof NodeHolder)) {
        	nodeHolder = new NodeHolder(child.toString());
        } else {
        	nodeHolder = (NodeHolder)child;
        }

        return addObject(parentNode, nodeHolder, true);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {
        return addObject(parent, child, false);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child, 
                                            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode = 
                new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = rootNode;
        }

        treeModel.insertNodeInto(childNode, parent, 
                                 parent.getChildCount());

        if (shouldBeVisible) {
            scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        
        allNodes.addElement(child);
        nodesMap.put(child, childNode);

        return childNode;
    }
    
    public Vector getCheckedNodes() {
    	int size = allNodes.size();
    	Vector checkedBoxes = new Vector();
    	
    	for(int i = 0; i < size; i++) {
    		NodeHolder box = (NodeHolder)allNodes.elementAt(i);
    		if(box.checked) {
    			checkedBoxes.addElement(box);
    		}
    	}

    	return checkedBoxes;
    }
    
    public Vector getAllNodes() {
    	return allNodes;
    }
    
    public HashMap getNodesMap() {
    	return nodesMap;
    }
    
    class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            DefaultMutableTreeNode node;
            node = (DefaultMutableTreeNode)
                     (e.getTreePath().getLastPathComponent());
            try {
                int index = e.getChildIndices()[0];
                node = (DefaultMutableTreeNode)
                       (node.getChildAt(index));
            } catch (NullPointerException exc) {}
        }
        public void treeNodesInserted(TreeModelEvent e) {
        	System.out.println("node inserted");
        }
        public void treeNodesRemoved(TreeModelEvent e) {
        	System.out.println("node deleted");
        }
        public void treeStructureChanged(TreeModelEvent e) {
        	System.out.println("tree changed");
        }
    }
    
	//When a node is checked, all the children should be checked;
	// when it is unchecked, all the parents should be unchecked.
    private void checkRelatedNodeFor(DefaultMutableTreeNode node) {
    	
    	Object obj = node.getUserObject();
    	NodeHolder nodeHolder = (NodeHolder)obj;

    	if(nodeHolder.checked) {
    		Enumeration enu = node.depthFirstEnumeration();
    		while(enu.hasMoreElements()) {
    			DefaultMutableTreeNode childNode = 
    				(DefaultMutableTreeNode)enu.nextElement();
    			((NodeHolder)childNode.getUserObject()).checked = 
    				nodeHolder.checked;    			    			
    		}
    	} else {
    		TreeNode[] parents = (TreeNode[])node.getPath();
    		if(parents != null && parents.length > 0) {
    			for(int i = 0; i < parents.length; i++) {
    				DefaultMutableTreeNode newNode = (DefaultMutableTreeNode)parents[i];
        			((CCJCheckBoxTree.NodeHolder)newNode.getUserObject()).checked = nodeHolder.checked;    				
    			}
    		}
    	}
    }
    
    public void valueChanged(TreeSelectionEvent e) {
    	
    	//System.out.println("tree selectyion changed to: dima " + e.getPath());
    	Object path = e.getPath();
    	System.out.println(path.getClass());
    	if(path instanceof CCJCheckBoxTree.NodeHolder)
    		((CCJCheckBoxTree.NodeHolder)path).checked = !((CCJCheckBoxTree.NodeHolder)path).checked;
    }
    
    public TreeNode getRootNode() {
    	return rootNode;
    }
    
    public static class NodeHolder extends JPanel{
    	public String name;
    	public boolean checked;
    	public Color color;
    	
    	public NodeHolder(String name, boolean checked, Color color) {
    		this.name = name;
    		this.checked = checked;
    		this.color = color;
    	}
    	
    	public NodeHolder(String name, boolean checked) {
    		this(name, checked, Color.BLACK);
    	}
    	
    	public NodeHolder(String name) {
    		this(name, false);
    	}
    	
    	public String toString() {
    		return name;
    	}
    }
    
    public double getVisibleHeight() {
    	Rectangle rect = this.getPathBounds(this.getPathForRow(this.getRowCount()-1));

    	if(rect != null)
    		return rect.getCenterY() + rect.height/2; 
    	return 0;
    }
    
    public void setPathChecked(TreePath path, boolean checked) {
    	DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPathComponent(0);
    	NodeHolder holder = (NodeHolder)node.getUserObject();
    	holder.checked = checked;
    }
    public boolean isPathChecked(TreePath path) {
       	DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
       	NodeHolder holder = (NodeHolder)node.getUserObject();
       	return holder.checked;
    }
}
