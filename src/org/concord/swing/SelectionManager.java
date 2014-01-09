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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

public class SelectionManager
extends MouseMotionAdapter
implements MouseListener
{
	protected int mouseX;
	protected int mouseY;
	protected boolean multiple = false;
	protected boolean dragged = false;
	protected boolean popup = false;
	protected SelectableContainer selectableContainer;
	protected Selectable selectedObject;
	protected Selectable activeObject;
	protected Component popupComponent;
	protected EventRelay eventRelay = new EventRelay();
	protected final String [] mouseNames =
	{
			"mouseEntered",
			"mouseExited",
			"mousePressed",
			"mouseReleased",
			"mouseClicked"
	};
	protected final String [] mouseMotionNames =
	{
			"mouseDragged",
			"mouseMoved"
	};
	
    
    public Selectable getSelectedObject()
    {
        return selectedObject;
    }
    
    public Selectable getActiveObject()
    {
        return activeObject;
    }
    
    public void setSelectableContainer(SelectableContainer container)
    {
        selectableContainer = container;
    }

	public void mousePressed(MouseEvent event)
	{
		mouseX = event.getX();
		mouseY = event.getY();
		if ((activeObject != null) && activeObject.contains(mouseX, mouseY))
		{
			eventRelay.relay("mousePressed", event);
		}
		else
		{
			multiple = event.isShiftDown();
			Selectable next = selectableContainer.findSelectable(event, mouseX, mouseY);
			if ((next == null) || (next != selectedObject))
			{
				activeObject = null;
				eventRelay.reset();
			}
			selectedObject = next;
			if (selectedObject != null)
			    selectableContainer.select(selectedObject, multiple);
			else
			    selectableContainer.deselect();
			popup = event.isPopupTrigger();
		}
	}
	
	public void mouseReleased(MouseEvent event)
	{
		if ((activeObject != null) && activeObject.contains(event.getX(), event.getY()))
		{
			eventRelay.relay("mouseReleased", event);
		}
		else
		{
			if ((popup || event.isPopupTrigger()) && (popupComponent instanceof Component))
			{
				popupComponent.setVisible(true);
				popupComponent.setLocation(mouseX, mouseY);
			}
			else if (dragged)
			{
			    selectableContainer.dragActionDone(selectedObject);
				dragged = false;
			}
			else if (multiple && (selectedObject != null))
			{
			    selectedObject.toggleSelected();
			}
			multiple = false;
		}
	}
	
	public void mouseDragged(MouseEvent event)
	{
		if ((activeObject != null) && activeObject.contains(event.getX(), event.getY()))
		{
			eventRelay.relay("mouseDragged", event);
		}
		else
		{
			int x = event.getX();
			int y = event.getY();
			int deltaX = x - mouseX;
			int deltaY = y - mouseY;
			if ((deltaX != 0) || (deltaY != 0))
			    dragged = true;
			if (dragged && (selectedObject != null))
			{
			    selectableContainer.dragAction(deltaX, deltaY, selectedObject);
			}
			mouseX = x;
			mouseY = y;
		}
	}
	
	public void setPopupComponent(Component popup)
	{
		popupComponent = popup;
	}

    public void mouseClicked(MouseEvent event)
    {
		if ((activeObject != null) && activeObject.contains(event.getX(), event.getY()))
		{
			eventRelay.relay("mouseClicked", event);
		}
		else
		{
	    	if (event.getClickCount() == 2)
	    	{
	    		if (selectedObject != null)
	    		{
	    			activeObject = selectedObject;
	    			Component component = activeObject.getComponent();
	    			if (component != null)
	    			{
	    				eventRelay.reset();
		    			eventRelay.addTarget(component, MouseListener.class, MouseEvent.class, mouseNames);
		    			eventRelay.addTarget(component, MouseMotionListener.class, MouseEvent.class, mouseMotionNames);
		    		    selectableContainer.select(selectedObject, false);
	    			}
	    		}
	    	}
		}
    }
    
    public void mouseMoved(MouseEvent event)
    {
		if ((activeObject != null) && activeObject.contains(event.getX(), event.getY()))
		{
			eventRelay.relay("mouseMoved", event);
		}
    }

    public void mouseEntered(MouseEvent event)
    {
		if ((activeObject != null) && activeObject.contains(event.getX(), event.getY()))
		{
			eventRelay.relay("mouseEntered", event);
		}
    }

    public void mouseExited(MouseEvent event)
    {
		if ((activeObject != null) && activeObject.contains(event.getX(), event.getY()))
		{
			eventRelay.relay("mouseExited", event);
		}
    }
}
