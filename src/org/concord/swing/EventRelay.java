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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class EventRelay
{
	protected Map methodListsTable = new HashMap();

	public void reset()
	{
		methodListsTable.clear();
	}
	
	public void addTarget(Component target, Class listenerClass, Class eventClass, String [] methodNames)
	{
		EventListener [] listeners = target.getListeners(listenerClass);
		Class [] argClass = { eventClass };
		for (int i = 0; i < methodNames.length; i++)
		{
			List methodList = new Vector();
			for (int j = 0; j < listeners.length; j++)
			{
				EventListener listener = listeners[j];
				Class objectClass = listener.getClass();
				try
				{
					Method method = objectClass.getMethod(methodNames[i], argClass);
					methodList.add(method);
					methodList.add(listener);
				}
				catch (NoSuchMethodException e)
				{
				}
			}
			methodListsTable.put(methodNames[i], methodList);
		}
	}

	protected void relay(String methodName, EventObject event)
	{
		Object [] args = { null };
		List methodList = (List) methodListsTable.get(methodName);
		Iterator methods = methodList.iterator();
		while (methods.hasNext())
		{
			Method method = (Method) methods.next();
			Object object = methods.next();
			args[0] = event;
			try
			{
				method.invoke(object, args);
			}
			catch (IllegalAccessException e)
			{
			}
			catch (IllegalArgumentException e)
			{
			}
			catch (InvocationTargetException e)
			{
			}
		}
	}
}
