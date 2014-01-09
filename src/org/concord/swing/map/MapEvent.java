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

import java.util.EventObject;
import java.util.Vector;

public class MapEvent
extends EventObject
{
	public static final int ITEM_SELECTED = 0;
	public static final int ITEM_MOVED = 1;
	public static final int ITEM_OPENED = 2;
	public static final int ITEM_CREATED = 3;
	public static final int ITEM_DELETED = 4;
	public static final int ITEM_POPUP = 5;
	protected Object data;
	protected int type = ITEM_SELECTED;
	
	public MapEvent(Object source, Object arg, int type)
	{
		super(source);
		this.type = type;
		data = arg;
	}
	
	public MapItem getSelection()
	{
		if (data instanceof Vector)
		{
			Vector selectionList = (Vector) data;
			return (selectionList.size() == 0) ? null : (MapItem) selectionList.elementAt(0);
		}
		return null;
	}
	
	public boolean isSelectionEvent()
	{
		return data instanceof Vector;
	}
	
	public boolean isCreateEvent()
	{
		return (type == ITEM_CREATED) && (data instanceof MapItem);
	}
	
	public boolean isOpenEvent()
	{
		return (type == ITEM_OPENED) && (data instanceof MapItem);
	}
	
	public boolean isDeleteEvent()
	{
		return (type == ITEM_DELETED) && (data instanceof MapItem);
	}
	
	public boolean isMultipleSelection()
	{
		return (data instanceof Vector) && (((Vector) data).size() > 1);
	}
	
	public Vector getSelectionList()
	{
		return (Vector) data;
	}
	
	public MapItem getMapItem()
	{
		return (MapItem) data;
	}
	
	public Object getData()
	{
		return data;
	}
}

