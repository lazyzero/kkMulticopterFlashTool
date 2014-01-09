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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class SelectableItem
implements Selectable
{
    protected Component componentItem;
    protected Boundary boundary = new Boundary(this);
    protected boolean selected = false;
    protected SelectableContainer container;
    protected Color defaultColor = Color.BLACK;
    protected Color activeColor = Color.RED;
    
    public SelectableItem(SelectableContainer container)
    {
        this.container = container;
    }
    
    public void draw(Graphics g)
    {
        boundary.draw(g);
    }
    
    public Component getComponent()
    {
        return componentItem;
    }
    
    public void setComponent(Component item)
    {
        componentItem = item;
        if (componentItem != null)
        {
            boundary.setBounds(item.getBounds());
        }
    }
    
    public Boundary getBoundary()
    {
        return boundary;
    }

    /* (non-Javadoc)
     * @see org.concord.swing.Selectable#isSelected()
     */
    public boolean isSelected()
    {
        return selected;
    }

    /* (non-Javadoc)
     * @see org.concord.swing.Selectable#setSelected(boolean)
     */
    public void setSelected(boolean value)
    {
        selected = value;
    }

    /* (non-Javadoc)
     * @see org.concord.swing.Selectable#toggleSelected()
     */
    public void toggleSelected()
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.concord.swing.Selectable#contains(int, int)
     */
    public boolean contains(int x, int y)
    {
        return boundary.contains(x, y);
    }

	public boolean isActive()
	{
		return container.getActiveObject() == this;
	}
	
	public class Handle
	extends Rectangle
	{
		public static final int TOP_LEFT = 0;
		public static final int TOP_MIDDLE = 1;
		public static final int TOP_RIGHT = 2;
		public static final int MIDDLE_RIGHT = 3;
		public static final int BOTTOM_RIGHT = 4;
		public static final int BOTTOM_MIDDLE = 5;
		public static final int BOTTOM_LEFT = 6;
		public static final int MIDDLE_LEFT = 7;
		public static final int DRAG = 8;
		public static final int HANDLE_WIDTH = 6;
		public static final int HANDLE_HEIGHT = 6;
		public static final int MIN_WIDTH = HANDLE_WIDTH - 2;
		public static final int MIN_HEIGHT = HANDLE_HEIGHT - 2;
		public final Rectangle [] scaleFactors =
		{
				new Rectangle( 1,  1, -1, -1),
				new Rectangle( 0,  1,  0, -1),
				new Rectangle( 0,  1,  1, -1),
				new Rectangle( 0,  0,  1,  0),
				new Rectangle( 0,  0,  1,  1),
				new Rectangle( 0,  0,  0,  1),
				new Rectangle( 1,  0, -1,  1),
				new Rectangle( 1,  0, -1,  0),
				new Rectangle( 1,  1,  1,  1)
		};
		protected Rectangle factors;
		protected int position = -1;
		protected Boundary parent;
		protected Rectangle dragBounds = new Rectangle();
		
		
		public Handle(Boundary parent, int position, int w, int h)
		{
			this.width = w;
			this.height = h;
			this.position = position;
			this.parent = parent;
			factors = scaleFactors[position];
		}
		
		public void scale(int dx, int dy)
		{
			Rectangle b = boundary.getBounds();
			b.width += factors.width * dx;
			b.height += factors.height * dy;
			if (b.width < MIN_WIDTH)
			{	
				b.width = MIN_WIDTH;
			}
			else
			{
				b.x += factors.x * dx;
			}
			if (b.height < MIN_HEIGHT)
			{	
				b.height = MIN_HEIGHT;
			}
			else
			{
				b.y += factors.y * dy;
			}
			boundary.setBounds(b.x, b.y, b.width, b.height);
		}
		
		public void setHandleBounds(int x, int y, int w, int h)
		{
			switch (position)
			{
				case TOP_LEFT:
				this.x = x - width - 1;
				this.y = y - height - 1;
				break;
				
				case TOP_MIDDLE:
				this.x = x + w / 2 + 1 - width / 2;
				this.y = y - height - 1;
				break;
				
				case TOP_RIGHT:
				this.x = x + w + 2;
				this.y = y - height - 1;
				break;
				
				case MIDDLE_RIGHT:
				this.x = x + w + 2;
				this.y = y + h / 2 + 1 - height / 2;
				break;
				
				case BOTTOM_RIGHT:
				this.x = x + w + 2;
				this.y = y + h + 2;
				break;
				
				case BOTTOM_MIDDLE:
				this.x = x + w / 2 + 1 - width / 2;
				this.y = y + h + 2;
				break;
				
				case BOTTOM_LEFT:
				this.x = x - width - 1;
				this.y = y + h + 2;
				break;
				
				case MIDDLE_LEFT:
				this.x = x - width - 1;
				this.y = y + h / 2 + 1 - height / 2;
				break;
				
				case DRAG:
				dragBounds.x = x - width - 1;
				dragBounds.y = y - height - 1;
				dragBounds.width = w + 2 * width + 2;
				dragBounds.height = h + 2 * height + 2;
				break;
			}
		}
		
		public boolean contains(int x, int y)
		{
			if (position == DRAG)
			{
				return dragBounds.contains(x, y);
			}
			return super.contains(x, y);
		}
		
		public int getPosition()
		{
			return position;
		}
	}
	
	public class Boundary
	extends Rectangle
	{
		protected int handleWidth = Handle.HANDLE_WIDTH;
		protected int handleHeight = Handle.HANDLE_HEIGHT;
		protected Handle [] handles = new Handle[9];
		protected Selectable parent;
        private Handle selectedHandle;
		
		public Boundary(Selectable selectable)
		{
			parent = selectable;
		}
		
		public void setBounds(Rectangle b)
		{
			setBounds(b.x, b.y, b.width, b.height);
		}
		
		public void setHandleSize(int width, int height)
		{
			handleWidth = width;
			handleHeight = height;
			for (int i = 0; i < handles.length; i++)
			{
				if (handles[i] == null)
				{
					handles[i] = new Handle(this, i, handleWidth, handleHeight);
				}
				else
				{
					handles[i].setSize(handleWidth, handleHeight);
				}
			}
		}
		
		
		public void setBounds(int x, int y, int width, int height)
		{
		    componentItem.setBounds(x, y, width, height);
			x--;
			y--;
			width++;
			height++;
			super.setBounds(x, y, width, height);
			setHandleSize(handleWidth, handleHeight);
			for (int i = 0; i < handles.length; i++)
			{
				handles[i].setHandleBounds(x, y, width, height);
			}
		}
		
		public Rectangle getBounds()
		{
			return componentItem.getBounds();
		}
		
		public Point getLocation()
		{
		    return componentItem.getLocation();
		}
		
		public void setLocation(int x, int y)
		{
		    Dimension size = getSize();
		    setBounds(x, y, size.width, size.height);
		}
		
		public void setLocation(Point location)
		{
		    setLocation(location.x, location.y);
		}
		
		public Dimension getSize()
		{
		    return componentItem.getSize();
		}
		
		public void setSize(int width, int height)
		{
		    Point location = getLocation();
		    setBounds(location.x, location.y, width, height);
		}
		
		public void setSize(Dimension size)
		{
		    setSize(size.width, size.height);
		}
		
		public Handle getSelectedHandle()
		{
		    return selectedHandle;
		}
		
		public Selectable getParent()
		{
			return parent;
		}
		
		public Handle findHandle(int x, int y)
		{
			for (int i = 0; i < handles.length; i++)
			{
				if (handles[i].contains(x, y))
				{
					return handles[i];
				}
			}
			return null;
		}
		
	    public boolean contains(int x, int y)
	    {
	        selectedHandle = findHandle(x, y);
	        return selectedHandle instanceof Handle;
	    }
		
		public void draw(Graphics g)
		{
			if (parent.isActive())
			{
				g.setColor(activeColor);
			}
			g.drawRect(x, y, width, height);
			if (parent.isSelected())
			{
				for (int i = 0; i < handles.length; i++)
				{
					Rectangle b = handles[i];
					if (i == Handle.DRAG)
					{
						b = handles[i].dragBounds;
						g.drawRect(b.x, b.y, b.width, b.height);
					}
					else
						g.fillRect(b.x, b.y, b.width, b.height);
				}
			}
			g.setColor(defaultColor);
		}
	}
}