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

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;

public class ViewEditor
extends JComponent
{
	protected int mouseX;
	protected int mouseY;
	protected int dragX;
	protected int dragY;
	protected boolean selected;
	protected boolean dragged = false;
	protected Vector selectionList = new Vector();
	protected Vector viewList = new Vector();
	protected Vector freeList = new Vector();
	protected Hashtable componentToEditor = new Hashtable();
	protected Hashtable editorToComponent = new Hashtable();
	protected SelectionBoundary boundary = new SelectionBoundary(this);
	protected SelectionHandle handle = null;
	protected Component targetComponent;
	protected Container container;
	protected Component popupComponent;
	protected MouseAdapter mouseAdapter = new MouseAdapter()
	{
		boolean popup = false;
		
		public void mousePressed(MouseEvent event)
		{
			boolean multiple = false;
			mouseX = event.getX();
			mouseY = event.getY();
			handle = findHandle(mouseX, mouseY);
			if (handle == null)
			{
				deselect();
			}
			select(multiple);
			if (handle == null)
			{
				for (int i = 0; i < viewList.size(); i++)
				{
					ViewEditor editor = (ViewEditor) viewList.elementAt(i);
					Rectangle b = editor.getBounds();
					int x = mouseX;
					int y = mouseY;
					if (b.contains(mouseX, mouseY))
					{
						editor.select(multiple);
					}
				}
			}
			ViewEditor.this.getParent().repaint();
			popup = event.isPopupTrigger();
		}
		
		public void mouseReleased(MouseEvent event)
		{
			if ((popup || event.isPopupTrigger()) && (popupComponent instanceof Component))
				popupComponent.setVisible(true);
			else if (dragged && (getParent() instanceof ViewEditor))
			{	
				setBounds(boundary.getBounds());
				dragged = false;
				ViewEditor.this.getParent().repaint();
			}
		}
	};
	protected MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter()
	{
		public void mouseDragged(MouseEvent event)
		{
			dragAction(event);
		}
	};
	protected ComponentAdapter targetAdapter = new ComponentAdapter()
	{
		public void componentResized(ComponentEvent event)
		{
			if (targetComponent instanceof Component)
			{
				targetComponent.setSize(ViewEditor.this.getSize());
				ViewEditor.this.getParent().repaint();
			}
		}
		
		public void componentMoved(ComponentEvent event)
		{
			if (targetComponent instanceof Component)
			{
				targetComponent.setLocation(ViewEditor.this.getLocation());
				ViewEditor.this.getParent().repaint();
			}
		}
	};
	protected ComponentAdapter containerAdapter = new ComponentAdapter()
	{
		public void componentResized(ComponentEvent event)
		{
			if (targetComponent instanceof Component)
			{
				ViewEditor.this.setSize(targetComponent.getSize());
				ViewEditor.this.repaint();
			}
		}
		
		public void componentMoved(ComponentEvent event)
		{
			if (targetComponent instanceof Component)
			{
				ViewEditor.this.setLocation(targetComponent.getLocation());
				ViewEditor.this.repaint();
			}
		}
	};
	
	protected SelectionHandle findHandle(int x, int y)
	{
		handle = null;
		for (int i = 0; i < selectionList.size(); i++)
		{
			ViewEditor view = (ViewEditor) selectionList.elementAt(i);
			handle = view.getBoundary().findHandle(x, y);
			if (handle != null)
			{
				break;
			}
		}
		return handle;
	}
	
	public void deselect()
	{
		for (int i = 0; i < selectionList.size(); i++)
		{
			((ViewEditor) selectionList.elementAt(i)).deselect();
		}
		selectionList.removeAllElements();
		selected = false;
	}
	
	public void select(boolean multiple)
	{
		if (getParent() instanceof ViewEditor)
		{
			ViewEditor parentView = (ViewEditor) getParent();
			Vector list = parentView.selectionList;
			if (! multiple)
			{
				for (int i = 0; i < list.size(); i++)
				{
					((ViewEditor) list.elementAt(i)).selected = false;
				}
				list.removeAllElements();
				selected = true;
				parentView.select(multiple);
			}
			list.addElement(this);
		}
	}
	
	public boolean isSelected()
	{
	    return selected;
	}
	
	public void delete()
	{
		if (getParent() instanceof ViewEditor)
		{
			ViewEditor parentView = (ViewEditor) getParent();
			parentView.remove(this);
			Vector list = (Vector) viewList.clone();
			for (int i = 0; i < list.size(); i++)
			{
				ViewEditor childView = (ViewEditor) list.elementAt(i);
				childView.delete();
			}
			parentView.viewList.removeElement(this);
			parentView.selectionList.removeElement(this);
			viewList.removeAllElements();
			selectionList.removeAllElements();
			boundary = null;
			handle = null;
		}
	}
	
	protected void moveAction(ViewEditor view, int deltaX, int deltaY)
	{
		dragged = true;
		if (view.selected)
		{
			Rectangle b = view.getBounds();
			view.setLocation(b.x + deltaX, b.y + deltaY);
		}
	}
	
	protected void dragAction(MouseEvent event)
	{
		int x = event.getX();
		int y = event.getY();
		int deltaX = x - mouseX;
		int deltaY = y - mouseY;
		if (handle != null)
		{
			if (handle.getPosition() == SelectionHandle.DRAG)
			{
				ViewEditor view = handle.parent.getParent();
				moveAction(view, deltaX, deltaY);
			}
			else
			{
				handle.scale(deltaX, deltaY);
			}
		}
		else
		{
			for (int i = 0; i < viewList.size(); i++)
			{
				ViewEditor view = (ViewEditor) viewList.elementAt(i);
				moveAction(view, deltaX, deltaY);
			}
		}
		mouseX = x;
		mouseY = y;
		getParent().repaint();
	}
	
	protected void paintComponent(Graphics g)
	{
		for (int i = 0; i < viewList.size(); i++)
		{
			ViewEditor view = (ViewEditor) viewList.elementAt(i);
			view.boundary.draw(g);
		}
	}
	
	public ViewEditor(Container container)
	{
		this();
		setContainer(container);
	}
	
	public ViewEditor()
	{
		super();
		setOpaque(false);
	}
	
	public void addNotify()
	{
		super.addNotify();
		Container container = getParent();
		if (container instanceof ViewEditor)
		{
			if (targetComponent instanceof Component)
				setBounds(targetComponent.getBounds());
			addComponentListener(targetAdapter);
		}
		else
		{
			ViewEditor editor;
			for (int i = 0; i < viewList.size(); i++)
			{
				editor = (ViewEditor) viewList.elementAt(i);
				editor.setTarget(null);
				editor.removeComponentListener(targetAdapter);
				freeList.addElement(editor);
			}
			viewList.removeAllElements();
			Component [] components = container.getComponents();
			for (int i = 0; i < components.length; i++)
			{
				if (components[i] == this)
					continue;
				if (freeList.size() > 0)
					editor = (ViewEditor) freeList.remove(0);
				else
					editor = new ViewEditor();
				editor.setTarget(components[i]);
				add(editor);
				viewList.addElement(editor);
			}
		}
	}
	
	public void setContainer(Container container)
	{
		if (this.container instanceof Container)
		{
			this.container.removeComponentListener(containerAdapter);
			this.container.remove(ViewEditor.this);
			removeMouseListener(mouseAdapter);
			removeMouseMotionListener(mouseMotionAdapter);
		}
		setTarget(container);
		this.container = container;
		if (this.container instanceof Container)
		{
			this.container.addComponentListener(containerAdapter);
			ViewEditor.this.addMouseListener(mouseAdapter);
			ViewEditor.this.addMouseMotionListener(mouseMotionAdapter);
			ViewEditor.this.setBounds(this.container.getBounds());
		}
	}
	
	public void setPopupComponent(Component popup)
	{
		popupComponent = popup;
	}
	
	public void addComponentListener(ComponentListener listener)
	{
		if (getParent() instanceof ViewEditor)
		{
			super.addComponentListener(listener);
		}
		else
		{
			for (int i = 0; i < viewList.size(); i++)
			{
				ViewEditor editor = (ViewEditor) viewList.elementAt(i);
				editor.addComponentListener(listener);
			}
		}
	}
	
	public void removeComponentListener(ComponentListener listener)
	{
		if (getParent() instanceof ViewEditor)
		{
			super.removeComponentListener(listener);
		}
		else
		{
			for (int i = 0; i < viewList.size(); i++)
			{
				ViewEditor editor = (ViewEditor) viewList.elementAt(i);
				editor.removeComponentListener(listener);
			}
		}
	}
	
	public Container getContainer()
	{
		return container;
	}
	
	public void setTarget(Component component)
	{
		targetComponent = component;
	}
	
	public Component getTarget()
	{
		return targetComponent;
	}
	
	public Vector getViewList()
	{
		return viewList;
	}
	
	public void setBounds(Rectangle bounds)
	{
		super.setBounds(bounds);
		if (boundary instanceof SelectionBoundary)
		{
			boundary.setBounds(bounds);
		}
	}
	
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		if (boundary instanceof SelectionBoundary)
		{
			boundary.setBounds(x, y, width, height);
		}
	}
	
	public SelectionBoundary getBoundary()
	{
		return boundary;
	}
	
	public void setHandleSize(int width, int height)
	{
		boundary.setHandleSize(width, height);
	}
	
	public static class SelectionHandle
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
		public static final Rectangle [] scaleFactors =
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
		protected SelectionBoundary parent;
		protected Rectangle dragBounds = new Rectangle();
		
		
		public SelectionHandle(SelectionBoundary parent, int position, int w, int h)
		{
			this.width = w;
			this.height = h;
			this.position = position;
			this.parent = parent;
			factors = scaleFactors[position];
		}
		
		public void scale(int dx, int dy)
		{
			ViewEditor view = parent.getParent();
			Rectangle b = view.getBounds();
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
			view.setBounds(b.x, b.y, b.width, b.height);
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
	
	public static class SelectionBoundary
	extends Rectangle
	{
		protected int handleWidth = SelectionHandle.HANDLE_WIDTH;
		protected int handleHeight = SelectionHandle.HANDLE_HEIGHT;
		protected SelectionHandle [] handles = new SelectionHandle[9];
		protected ViewEditor parent;
		
		public SelectionBoundary(ViewEditor view)
		{
			parent = view;
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
					handles[i] = new SelectionHandle(this, i, handleWidth, handleHeight);
				}
				else
				{
					handles[i].setSize(handleWidth, handleHeight);
				}
			}
		}
		
		public void setBounds(int x, int y, int width, int height)
		{
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
			Rectangle b = parent.getBounds();
			b.x = x + 1;
			b.y = y + 1;
			b.width = width - 1;
			b.height = height - 1;
			return b;
		}
		
		public void translate(int dx, int dy)
		{
			super.translate(dx, dy);
			Rectangle b = getBounds();
			for (int i = 0; i < handles.length; i++)
			{
				handles[i].setHandleBounds(b.x + dx, b.y + dy, width, height);
			}
		}
		
		public ViewEditor getParent()
		{
			return parent;
		}
		
		public SelectionHandle findHandle(int x, int y)
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
		
		public void draw(Graphics g)
		{
			g.drawRect(x, y, width, height);
			if (parent.selected)
			{	
				for (int i = 0; i < handles.length; i++)
				{
					Rectangle b = handles[i];
					if (i == SelectionHandle.DRAG)
					{
						b = handles[i].dragBounds;
						g.drawRect(b.x, b.y, b.width, b.height);
					}
					else
						g.fillRect(b.x, b.y, b.width, b.height);
				}
			}
		}
	}
}
