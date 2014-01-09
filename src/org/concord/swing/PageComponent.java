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
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.XMLDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class PageComponent
extends JComponent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2278470225463856026L;
	protected Object object;
	protected Component component = new JLabel("Object");
	protected Cover cover = new Cover();
	protected SelectionManager selectionManager;
	protected boolean editable = true;
	
	public PageComponent(URL url)
	{
		InputStream input;
		setLayout(null);
		try
		{
			input = url.openStream();
			XMLDecoder decoder = new XMLDecoder(input);
			object = decoder.readObject();
			if (object instanceof Component)
			{
				component = (Component) object;
				cover.setComponent(component);
				add(cover, 0);
				add(component, 1);
				component.setLocation(0, 0);
				validate();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void addNotify()
	{
		super.addNotify();
		Dimension size = component.getSize();
		cover.setSize(size);
		setSize(size);
	}
	
	public void setEditable(boolean value)
	{
		editable = value;
		if (editable)
			add(cover, 0);
		else
			remove(cover);
		validate();
	}
	
	public void setSelectionManager(SelectionManager manager)
	{
		if (selectionManager != null)
		{
			cover.removeMouseListener(selectionManager);
			cover.removeMouseMotionListener(selectionManager);
		}
		selectionManager = manager;
		if (selectionManager != null)
		{
			cover.addMouseListener(selectionManager);
			cover.addMouseMotionListener(selectionManager);
		}
			
	}
	
	public Component getComponent()
	{
		if (object instanceof Component)
			return (Component) object;
		return component;
	}
	
	public Object getObject()
	{
		return object;
	}
	
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		component.setBounds(0, 0, width, height);
		cover.setBounds(0, 0, width, height);
	}
	
	public Dimension getMinimumSize()
	{
		return component.getMinimumSize();
	}
	
	public Dimension getMaximumSize()
	{
		return component.getMaximumSize();
	}
	
	public Dimension getPreferredSize()
	{
		return component.getPreferredSize();
	}
	
	public static class Cover
	extends JComponent
	implements ComponentListener, Selectable
	{
		protected Component component;
		protected boolean selected = false;
		
		public Cover()
		{
			this.setOpaque(false);
		}
		
		protected void draw(Graphics g)
		{
		}
		
		public void paintComponent(Graphics g)
		{
			if (selected)
			{
				Rectangle b = getBounds();
				g.setXORMode(Color.white);
				g.fillRect(b.x, b.y, b.width, b.height);
				g.setPaintMode();
			}
		}

		public Component getComponent()
		{
			return component;
		}
		
		public void setComponent(Component c)
		{
			component = c;
		}
		
		public void componentHidden(ComponentEvent e)
		{
		}

		public void componentMoved(ComponentEvent e)
		{
			setLocation(component.getLocation());
		}

		public void componentResized(ComponentEvent e)
		{
			setSize(component.getSize());
		}

		public void componentShown(ComponentEvent e)
		{
		}
		
		public Dimension getPreferredSize()
		{
			return component.getPreferredSize();
		}

		public boolean isActive()
		{
			return getParent() == null;
		}

		public boolean isSelected()
		{
			return selected;
		}

		public void setSelected(boolean value)
		{
			selected = value;
			repaint();
		}

		public void toggleSelected()
		{
			selected = ! selected;
			repaint();
		}
	}
}
