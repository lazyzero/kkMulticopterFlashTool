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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

public class MouseInterceptPanel
extends JComponent
implements MouseListener, MouseMotionListener
{
	protected Component targetComponent;
	protected boolean mousePassThrough = true;
	protected boolean mouseMotionPassThrough = true;
	protected MouseListener mouseIntercept;
	protected MouseMotionListener mouseMotionIntercept;
	protected ComponentAdapter componentAdapter = new ComponentAdapter()
	{
		public void componentResized(ComponentEvent event)
		{
			MouseInterceptPanel.this.setSize(targetComponent.getSize());
		}
		
		public void componentMoved(ComponentEvent event)
		{
			MouseInterceptPanel.this.setLocation(targetComponent.getLocation());
		}
	};
	
	public MouseInterceptPanel()
	{
		super();
		setOpaque(false);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public MouseInterceptPanel(Component component)
	{
		this();
		setTarget(component);
	}
	
	public void setTarget(Component component)
	{
		if (targetComponent instanceof Component)
		{
			targetComponent.removeComponentListener(componentAdapter);
		}
		if (component instanceof Component)
		{
			targetComponent = component;
			targetComponent.addComponentListener(componentAdapter);
			Container parent = targetComponent.getParent();
			if (parent instanceof Container)
			{
				parent.add(this, 0);
				setBounds(targetComponent.getBounds());
			}
		}
	}
	
	public void setMouseIntercept(MouseListener listener)
	{
		if (mouseIntercept instanceof MouseListener)
			removeMouseListener(mouseIntercept);
		mouseIntercept = listener;
		if (mouseIntercept instanceof MouseListener)
		{
			removeMouseListener(this);
			addMouseListener(mouseIntercept);
			mousePassThrough = false;
		}
		else
		{
			addMouseListener(this);
			mousePassThrough = true;
		}
	}
	
	public void setMouseMotionIntercept(MouseMotionListener listener)
	{
		if (mouseMotionIntercept instanceof MouseMotionListener)
			removeMouseMotionListener(mouseMotionIntercept);
		mouseMotionIntercept = listener;
		if (mouseMotionIntercept instanceof MouseMotionListener)
		{
			removeMouseMotionListener(this);
			addMouseMotionListener(mouseMotionIntercept);
			mouseMotionPassThrough = false;
		}
		else
		{
			addMouseMotionListener(this);
			mouseMotionPassThrough = true;
		}
	}

	public void mouseClicked(MouseEvent event)
	{
		if (mousePassThrough)
			targetComponent.dispatchEvent(event);
	}

	public void mouseEntered(MouseEvent event)
	{
		if (mouseMotionPassThrough)
			targetComponent.dispatchEvent(event);
	}

	public void mouseExited(MouseEvent event)
	{
		if (mouseMotionPassThrough)
			targetComponent.dispatchEvent(event);
	}

	public void mousePressed(MouseEvent event)
	{
		if (mousePassThrough)
			targetComponent.dispatchEvent(event);
	}

	public void mouseReleased(MouseEvent event)
	{
		if (mousePassThrough)
			targetComponent.dispatchEvent(event);
	}

	public void mouseDragged(MouseEvent event)
	{
		if (mouseMotionPassThrough)
			targetComponent.dispatchEvent(event);
	}

	public void mouseMoved(MouseEvent event)
	{
		if (mouseMotionPassThrough)
			targetComponent.dispatchEvent(event);
	}
}