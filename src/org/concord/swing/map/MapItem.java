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

package org.concord.swing.map;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.util.Vector;

public abstract class MapItem
implements MapContainer
{
	protected String name;
	protected Vector nestedNodeItems = new Vector();
	protected Vector nestedArcItems = new Vector();
	protected MapContainer parent;
	protected Image image;
	protected ImageObserver observer;
	protected Rectangle imageBounds = new Rectangle(0, 0, 50, 50);
    protected Color textColor = Color.black;
    protected int textX = 0;
    protected int textY = 0;
	protected Object data;
	
	protected MapItem(MapContainer container)
	{
		parent = container;
	}
	
    public abstract void delete();

    public void setName(String itemName)
	{
		name = itemName;
	}
	
    public String getName()
	{
		return name;
	}
    
    public String getPathName()
    {
        MapContainer root = this;
        String path = "";
        while (root != null)
        {
            if (path.length() > 0)
                path = "/" + path;
            path = root.getName() + path;
            if (root == root.getItemParent())
                break;
            root = root.getItemParent();
        }
        return path;
    }
	
    public abstract int getID();
	
	public MapContainer getItemParent()
	{
		return parent;
	}
	
	public Object getData()
	{
		return data;
	}
	
	public void setData(Object data)
	{
		this.data = data;
	}
	
	public void setItemParent(MapContainer container)
	{
		parent = container;
	}
	
	public void setImage(Image image, ImageObserver observer)
	{
		this.observer = observer;
		this.image = image.getScaledInstance(-1, imageBounds.height, Image.SCALE_SMOOTH);
		imageBounds.width = this.image.getWidth(observer);
		imageBounds.height = this.image.getHeight(observer);
	}

    public abstract void translate(int x, int y);
	
	public abstract Rectangle getBounds();
	
	public void initializeNode(NodeItem node, String name, int x, int y)
	{
		node.setName(name);
		node.setLocation(x, y);
		nestedNodeItems.addElement(node);
	}
	
	public NodeItem createNodeItem(String name, int x, int y)
	{
		NodeItem node = new NodeItem(this);
		initializeNode(node, name, x, y);
		return node;
	}
	
	public void initializeArc(ArcItem arc, NodeItem source, NodeItem sink, int x, int y)
	{
		if ((source == null) && (sink == null))
		{
			arc.setSourceLocation(x, y);
			arc.setSinkLocation(x + 50, y + 50);
		}
		else
		{
			if (source instanceof NodeItem)
				arc.setSourceNode(source);
			else
				arc.setSourceLocation(x, y);
			if (sink instanceof NodeItem)
				arc.setSinkNode(sink);
			else
				arc.setSinkLocation(x, y);
		}
		nestedArcItems.addElement(arc);
	}
	
	public ArcItem createArcItem(NodeItem source, NodeItem sink, int x, int y)
	{
		ArcItem arc = new ArcItem(this);
		initializeArc(arc, source, sink, x, y);
		return arc;
	}
	
	public Vector getArcItems()
	{
		return nestedArcItems;
	}
	
	public Vector getNodeItems()
	{
		return nestedNodeItems;
	}
	
	public void removeArcItem(ArcItem arc)
	{
		nestedArcItems.removeElement(arc);
	}
	
	public void removeNodeItem(NodeItem node)
	{
		nestedNodeItems.removeElement(node);
	}
}

