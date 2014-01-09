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

package org.concord.swing.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.JComponent;

public class OverlayDraw
extends JComponent
implements MouseListener, MouseMotionListener
{
	public static final int MARK_RECT = 0;
	public static final int MARK_OVAL = 1;

	public static final int MARK_NO_ACTION = -1;
	public static final int MARK_CREATED = 0;
	public static final int MARK_CHANGED = 1;
	public static final int MARK_MOVED = 2;

	protected int createType = MARK_OVAL;
	protected Vector markList = new Vector();
	protected Vector markListeners = new Vector();
	protected Color markColor = Color.black;
	protected int markAction;
	protected Mark selectedMark;
	protected Rectangle selectedHandle;
	protected Point pressed;
	protected Component component;
	protected boolean creating = false;
	protected int handleWidth = 8;
	protected int handleHeight = 8;
	
	public OverlayDraw(Component component)
	{
		this.component = component;
		setEnabled(true);
	}
	
	public boolean isCreating()
	{
		return creating;
	}
	
	public void setCreating(boolean value)
	{
		creating = value;
	}
	
	public void setEnabled(boolean enabled)
	{
		Container container = component.getParent();
		if (container instanceof Container)
		{
			if (enabled)
			{
				container.add(this, 0);
				setBounds(component.getBounds());
				addMouseListener(this);
				addMouseMotionListener(this);
			}
			else
			{
				removeMouseListener(this);
				removeMouseMotionListener(this);
				component.setBounds(getBounds());
				container.remove(this);
			}
		}
	}
	
	public boolean isEnabled()
	{
		Container parent = getParent();
		if (parent == null)
			return false;
		Component [] components = parent.getComponents();
		for (int i = 0; i < components.length; i++)
			if (components[i] == this)
				return true;
		return false;
	}
	
	public Color getColor()
	{
		return markColor;
	}
	
	public void setColor(Color color)
	{
		markColor = color;
	}
	
	public Mark findMark(Point point)
	{
		for (int i = 0; i < markList.size(); i++)
		{
			Mark mark = (Mark) markList.elementAt(i);
			if (mark.contains(point))
				return mark;
		}
		return null;
	}
		
	public void setHandleSize(int w, int h)
	{
		handleWidth = w;
		handleHeight = h;
	}
	
	protected void paintComponent(Graphics g)
	{
		for (int i = 0; i < markList.size(); i++)
		{
			Mark mark = (Mark) markList.elementAt(i);
			mark.draw(g);
		}
	}
	
	public void mouseClicked(MouseEvent e)
	{
	}
	
	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
		pressed = e.getPoint();
		if (creating)
		{
			Mark mark = new Mark(createType, pressed);
			markList.addElement(mark);
			selectedMark = mark;
			creating = false;
			notifyMarkListeners(mark, MARK_CREATED);
		}
		else
		{
			selectedMark = findMark(pressed);
			if (selectedMark instanceof Mark)
				selectedHandle = selectedMark.findHandle(pressed);
			else
				selectedHandle = null;
		}
		if (getParent() != null)
			getParent().repaint();
	}

	public void mouseReleased(MouseEvent e)
	{
		if ((selectedMark != null) && (markAction > MARK_NO_ACTION))
			notifyMarkListeners(selectedMark, markAction);
	}

	public void mouseDragged(MouseEvent e)
	{
		int deltaWidth = e.getX() - pressed.x;
		int deltaHeight = e.getY() - pressed.y;
		markAction = MARK_NO_ACTION;
		if (selectedMark instanceof Mark)
		{
			if (selectedHandle instanceof Rectangle)
			{
				selectedMark.changeSize(deltaWidth, deltaHeight);
				markAction = MARK_CHANGED;
			}
			else
			{
				selectedMark.moveCenter(deltaWidth, deltaHeight);
				markAction = MARK_MOVED;
			}
		}
		pressed = e.getPoint();
		if (getParent() != null)
			getParent().repaint();
	}
	
	public void mouseMoved(MouseEvent e)
	{
	}
	
	public class Mark
	extends Rectangle
	{
		protected int type;
		protected boolean selected;
		protected Point center;
		protected int halfWidth = 25;
		protected int halfHeight = 25;
		protected Rectangle lowerRight = new Rectangle(0, 0, handleWidth, handleHeight);
		
		public Mark(int type, Point point)
		{
			this.type = type;
			center = new Point(point);
			setLocation();
		}
		
		public void setLocation()
		{
			x = center.x - halfWidth;
			y = center.y - halfHeight;
			width = 2 * halfWidth;
			height = 2 * halfHeight;
			selected = true;
			lowerRight.setLocation((x + width) - handleWidth, (y + height) - handleHeight);
		}
		
		public void changeSize(int deltaWidth, int deltaHeight)
		{
			halfWidth += deltaWidth;
			halfHeight += deltaHeight;
			if (halfWidth < handleWidth)
				halfWidth = handleWidth;
			if (halfHeight < handleHeight)
				halfHeight = handleHeight;
			setLocation();
		}
		
		public void moveCenter(int deltaWidth, int deltaHeight)
		{
			center.x += deltaWidth;
			center.y += deltaHeight;
			setLocation();
		}
		
		public Rectangle findHandle(Point point)
		{
			if (lowerRight.contains(point))
				return lowerRight;
			return null;
		}
		
		protected void draw(Graphics g)
		{
			Color saveColor = g.getColor();
			g.setColor(markColor);
			switch (type)
			{
				case MARK_RECT:
					g.drawRect(x, y, width, height);
				break;
				
				case MARK_OVAL:
					g.drawOval(x, y, width, height);
				break;
			}
			if (selectedMark == this)
			{
				g.fillRect(lowerRight.x, lowerRight.y, handleWidth, handleHeight);
			}
			g.setColor(saveColor);
		}
	}
	
	public class MarkEvent
	extends EventObject
	{
		public MarkEvent(Object source)
		{
			super(source);
		}
	}
	
	public interface MarkListener
	extends EventListener
	{
		public void markCreated(MarkEvent e);
		public void markChanged(MarkEvent e);
		public void markMoved(MarkEvent e);
	}
	
	public void addMarkListener(MarkListener listener)
	{
		if (! markListeners.contains(listener))
			markListeners.addElement(listener);
	}
	
	public void removeMarkListener(MarkListener listener)
	{
		if (markListeners.contains(listener))
			markListeners.removeElement(listener);
	}
	
	public void notifyMarkListeners(Mark mark, int type)
	{
		for (int i = 0; i < markListeners.size(); i++)
		{
			MarkListener listener = (MarkListener) markListeners.elementAt(i);
			switch (type)
			{
				case MARK_CREATED:
					listener.markCreated(new MarkEvent(mark));
				break;
				case MARK_CHANGED:
					listener.markChanged(new MarkEvent(mark));
				break;
				case MARK_MOVED:
					listener.markMoved(new MarkEvent(mark));
				break;
			}
		}
	}
}

