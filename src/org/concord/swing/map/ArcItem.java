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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

public class ArcItem
extends MapItem
{
    protected final static int RADIUS = 5;
	protected Polygon arrow = new Polygon();
	protected Rectangle bounds = new Rectangle(0, 0, 0, 0);
    protected int id;
    protected Color color = Color.black;
    protected Color selectedColor = Color.red;
    protected NodeItem sourceItem;
    protected NodeItem sinkItem;
    protected Point sourcePoint = new Point(0, 0);
    protected Point sinkPoint = new Point(0, 0);
    protected Point p1 = new Point(0, 0);
    protected Point p2 = new Point(0, 0);
	protected int a = 3;
	protected int b = 4;
	protected int c = 1;
	protected int d = 2;
    protected String name;
    protected static int arcNumber = 0;
	
	public ArcItem(MapContainer container)
	{
		super(container);
	}
	
	public void delete()
	{
		if (sourceItem != null)
			sourceItem.removeArc(this);
		if (sinkItem != null)
			sinkItem.removeArc(this);
		if (parent != null)
			parent.removeArcItem(this);
	}
     
    public void setSourceNode(NodeItem sourceItem)
    {
        if ((this.sourceItem instanceof NodeItem) && (sourceItem == null))
            setSourceLocation(this.sourceItem.getNodeCenterX(), this.sourceItem.getNodeCenterY());
        this.sourceItem = sourceItem;
        if (sourceItem instanceof NodeItem)
            sourceItem.addArc(this);
		String sourceName = (sourceItem == null) ? "self" : sourceItem.getName();
		String sinkName = (sinkItem == null) ? "self" : sinkItem.getName();
        setName("Arc from " + sourceName + " to " + sinkName);		
    }
	
	public NodeItem getSourceNode()
	{
		return sourceItem;
	}
    
    public void setSinkNode(NodeItem sinkItem)
    {
        if ((this.sinkItem instanceof NodeItem) && (sinkItem == null))
            setSinkLocation(this.sinkItem.getNodeCenterX(), this.sinkItem.getNodeCenterY());
        this.sinkItem = sinkItem;
        if (sinkItem instanceof NodeItem)
            sinkItem.addArc(this);
		String sourceName = (sourceItem == null) ? "self" : sourceItem.getName();
		String sinkName = (sinkItem == null) ? "self" : sinkItem.getName();
        setName("Arc from " + sourceName + " to " + sinkName);		
    }
	
	public NodeItem getSinkNode()
	{
		return sinkItem;
	}
	
	public void setArrowRatio(int num, int denom)
	{
		a = num;
		b = denom;
	}
	
	public void setImageRatio(int num, int denom)
	{
		c = num;
		d = denom;
	}
  
    public boolean isNear(int x, int y, Point point, int radius)
    {
        return (Math.abs(x - point.x) <= radius) && (Math.abs(y - point.y) <= radius);
    }
    
    public boolean isNearSourceLocation(int x, int y)
    {
        return (sourceItem == null) && isNear(x, y, sourcePoint, RADIUS);
    }
    
    public Point getSourcePoint()
    {
        return sourcePoint;
    }
    
    public void setSourceLocation(int x, int y)
    {
        sourcePoint.setLocation(x, y);
    }
    
    public boolean isNearSinkLocation(int x, int y)
    {
        return (sinkItem == null) && isNear(x, y, sinkPoint, RADIUS);		
    }
    
    public Point getSinkPoint()
    {
        return sinkPoint;
    }
    
    public void setSinkLocation(int x, int y)
    {
        sinkPoint.setLocation(x, y);
    }
	
	public Rectangle getBounds()
	{
		if (image instanceof Image)
			return imageBounds;
		return bounds;
	}
    
    public void translate(int x, int y)
    {
        if (sourceItem == null)
        {
            sourcePoint.translate(x, y);
        }
        if (sinkItem == null)
        {
            sinkPoint.translate(x, y);
        }
    }
    
    public void removeNode(NodeItem nodeItem)
    {
        if (nodeItem == sourceItem)
            setSourceNode(null);
        if (nodeItem == sinkItem)
            setSinkNode(null);
    }
    
    public void removeNodes()
    {
        if (sourceItem != null)
        {
            sourceItem.removeArcItem(this);
            sourceItem = null;
        }
        if (sinkItem != null)
        {
            sinkItem.removeArcItem(this);
            sinkItem = null;
        }
    }
    
    public boolean isDisconnected()
    {
        return (sourceItem == null) || (sinkItem == null);
    }
    
    protected void transformPolygon(Point p, Point d, float angle)
    {
        bounds.x = Integer.MAX_VALUE;
        bounds.y = Integer.MAX_VALUE;
        bounds.width = Integer.MIN_VALUE;
        bounds.height= Integer.MIN_VALUE;
        int f = d.x > 0 ? 1 : -1;
        for (int i = 0; i < arrow.npoints; i++)
        {
            float x = (float) arrow.xpoints[i];
            float y = (float) arrow.ypoints[i];
            float x1 = (float) (f * x * Math.cos(angle) + y * Math.sin(angle));
            float y1 = (float) (x * Math.sin(angle) + -f * y * Math.cos(angle));
            arrow.xpoints[i] = (int) x1;
            arrow.ypoints[i] = (int) y1;
            bounds.x = Math.min(arrow.xpoints[i], bounds.x);
            bounds.y = Math.min(arrow.ypoints[i], bounds.y);
            bounds.width = Math.max(arrow.xpoints[i], bounds.width);
            bounds.height = Math.max(arrow.ypoints[i], bounds.height);
        }
        bounds.width = bounds.width - bounds.x;
        bounds.height = bounds.height - bounds.y;
        bounds.x += p.x;
        bounds.y += p.y;
        for (int i = 0; i < arrow.npoints; i++)
        {
            arrow.xpoints[i] += p.x;
            arrow.ypoints[i] += p.y;
        }
        
    }
    
    protected void setPoints()
    {
        int [] xpts = { 20, 0, 0 };
        int [] ypts = { 0, 8, -8 };
        arrow.xpoints = xpts;
        arrow.ypoints = ypts;
        arrow.npoints = arrow.ypoints.length;
        if (sourceItem == null)
        {
            p1.setLocation(sourcePoint);
        }
        else
        {
            Rectangle b = sourceItem.getBounds();
            p1.x = b.x + b.width / 2;
            p1.y = b.y + b.height / 2;
        }
        if (sinkItem == null)
        {
            p2.setLocation(sinkPoint);
        }
        else
        {
            Rectangle b = sinkItem.getBounds();
            p2.x = b.x + b.width / 2;
            p2.y = b.y + b.height / 2;
        }
    }
	
	public void drawConnect(Graphics g, boolean selected)
	{
        Color saveColor = g.getColor();
        g.setColor(selected ? selectedColor : color);
        setPoints();
        if (sourceItem == null)
        {
            g.fillOval(p1.x - RADIUS, p1.y - RADIUS, 2 * RADIUS, 2 * RADIUS);
        }
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
        if (sinkItem == null)
        {
            g.fillOval(p2.x - RADIUS, p2.y - RADIUS, 2 * RADIUS, 2 * RADIUS);
        }
        g.setColor(saveColor);
	}
	
	public void draw(Graphics g, boolean selected)
	{
        Color saveColor = g.getColor();
        g.setColor(selected ? selectedColor: color);
        setPoints();
        p2.setLocation(p2.x - p1.x, p2.y - p1.y);
		if (image instanceof Image)
		{
			Rectangle r = imageBounds;
			// Place along arc line
			r.setLocation(p1.x + c * p2.x / d, p1.y + c * p2.y / d);
			r.width = image.getWidth(observer);
			r.height = image.getHeight(observer);
			// Align image center with arc line
			r.setLocation(r.x - (r.width / 2), r.y - (r.height / 2));
			g.drawImage(image, r.x, r.y, observer);
			FontMetrics metrics = g.getFontMetrics(g.getFont());
			textX = r.x + (r.width - metrics.stringWidth(getName())) / 2 ;
			textY = r.y + r.height + metrics.getAscent();
			g.setColor(textColor);
			g.drawString(getName(), textX, textY);
			g.setColor(selected ? selectedColor: color);
		}
		p1.setLocation(p1.x + a * p2.x / b, p1.y + a * p2.y / b);
        float theta = (float) Math.asin(p2.y / Math.sqrt(p2.y * p2.y + p2.x * p2.x));
        transformPolygon(p1, p2, theta);
        g.fillPolygon(arrow);
        g.setColor(saveColor);
	}
    
    public int getID()
    {
        return id;
    }
    
    public String toString()
    {
        return name;
    }
}

