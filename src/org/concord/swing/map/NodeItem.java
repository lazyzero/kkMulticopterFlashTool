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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

public class NodeItem
extends MapItem
{
    protected static int itemNumber = 0;
	protected Rectangle bounds = new Rectangle(0, 0, 0, 0);
    protected String name;
    protected Point currentLocation = new Point(0, 0);
    protected Dimension defaultSize = new Dimension(50, 50);
    protected Color color = Color.cyan;
    protected Color parentColor = Color.green;
    protected Color selectedColor = Color.red;
    protected Color selectedParentColor = Color.yellow;
    protected Vector arcItems = new Vector();
    protected int id;
	
	public NodeItem(MapContainer container)
	{
		super(container);
		setName("node" + getNextItemNumber());
		bounds.setBounds(0, 0, defaultSize.width, defaultSize.height);
	}
	
	public NodeItem(MapContainer container, String name)
	{
		this(container);
		setName(name);
	}
	
	public void delete()
	{
        Vector items = (Vector) arcItems.clone();
        for (int i = 0; i < items.size(); i++)
        {
            ArcItem arcItem = (ArcItem) items.elementAt(i);
            arcItem.removeNode(this);
        }
        Vector subItems = (Vector) nestedNodeItems.clone();
        for (int i = 0; i < subItems.size(); i++)
        {
            NodeItem nodeItem = (NodeItem) subItems.elementAt(i);
            nodeItem.delete();
        }
        subItems = (Vector) nestedArcItems.clone();
        for (int i = 0; i < subItems.size(); i++)
        {
            ArcItem arcItem = (ArcItem) subItems.elementAt(i);
            arcItem.delete();
        }
		if (parent != null)
			parent.removeNodeItem(this);
	}
   
    public void addArc(ArcItem arc)
    {
        arcItems.addElement(arc);
    }
    
    public void removeArc(ArcItem arc)
    {
        arcItems.removeElement(arc);
    }
    
	public Rectangle getBounds()
	{
		return bounds;
	}
	
	public void setBounds(int x, int y, int width, int height)
	{
		bounds.setBounds(x, y, width, height);
	}
	
	public void setSize(Dimension size)
	{
		bounds.setSize(size);
	}
	
    public void translate(int x, int y)
    {
        bounds.translate(x, y);
        Point p = bounds.getLocation();
        p.x = Math.max(p.x, 0);
        p.y = Math.max(p.y, 0);
        bounds.setLocation(p);
    }
	
	public void setLocation(int x, int y)
	{
		bounds.setLocation(x, y);
	}
    
    protected int getNodeCenterX()
    {
        Rectangle bounds = getBounds();
        return bounds.x + bounds.width / 2;
    }
    
    protected int getNodeCenterY()
    {
        Rectangle bounds = getBounds();
        return bounds.y + bounds.height / 2;
    }

    protected Rectangle computeBounds(FontMetrics metrics)
    {
        Rectangle b = getBounds();
		if(b.width == -1 || b.height == -1)
		{
			int width = metrics.stringWidth(getName()+ "  ");
			b.setSize(Math.max(defaultSize.width, width), defaultSize.height);
			return b;
		}
		else
		{
			textX = getNodeCenterX() - metrics.stringWidth(getName()) / 2 ;
			if (image instanceof Image)
			{
				textY = b.y + b.height + metrics.getAscent();
				b.setSize(image.getWidth(observer), defaultSize.height);
				return b;
			}
			else
			{
				textY = getNodeCenterY() - metrics.getHeight() / 2 + metrics.getAscent();
				int extra = 0;
				extra = (textX < b.x) ? 3 * (b.x - textX) : 0;
				b.setBounds(b.x - extra / 2, b.y, b.width + extra, b.height);
				return b;
			}
		}
	}
    
    public void draw(Graphics g, boolean selected)
    {
        Color saveColor = g.getColor();
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        Rectangle b = computeBounds(metrics);
		if ((nestedNodeItems.size() > 0) || (nestedArcItems.size() > 0))
			g.setColor(selected ? selectedParentColor : parentColor);
		else
			g.setColor(selected ? selectedColor : color);
		if (image instanceof Image)
			g.drawImage(image, b.x, b.y, observer);
        else
			g.fillOval(b.x, b.y, b.width, b.height);
		if (image instanceof Image)
		{
			g.drawRect(b.x, b.y, b.width, b.height);
			g.setColor(textColor);
		}
		else
		{
			g.setColor(textColor);
			g.drawOval(b.x, b.y, b.width, b.height);
		}
		g.drawString(getName(), textX, textY);
        g.setColor(saveColor);
    }
    
    public static int getNextItemNumber()
    {
        return itemNumber++;
    }
    
    public static void setItemNumber(int number)
    {
        itemNumber = number;
    }
    
    public static void resetItemNumber()
    {
        itemNumber = 0;
    }
    
    public int getID()
    {
        return id;
    }
}

