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

import java.util.EventListener;
import java.util.EventObject;
import java.util.Vector;

public class RecordBuffer
{
	protected byte [] bytes = null;
	protected char [] chars = null;
	protected int index = 0;
	protected int count = 0;
	protected RecordEvent recordEvent;
	protected Vector listeners = new Vector();
	
	public RecordBuffer(Class type, int size)
	{
	    if (type == Byte.TYPE)
	        bytes = new byte[size];
	    else
	        chars = new char[size];
		recordEvent = new RecordEvent(this);
	}
	
	public RecordBuffer(int size)
	{
	    this(Byte.TYPE, size);
	}
	
	protected void notifyRecordListeners(int value)
	{
		for (int i = 0; i < listeners.size(); i++)
		{
			RecordListener listener = (RecordListener) listeners.get(i);
			recordEvent.setValue(value);
			listener.valueRecorded(recordEvent);
		}
	}
	
	public void addRecordListener(RecordListener listener)
	{
		if (listeners.contains(listener))
			return;
		listeners.add(listener);
	}
	
	public void removeRecordListener(RecordListener listener)
	{
		listeners.remove(listener);
	}
	
	protected String getBufferString(int i, int n)
	{
	    return (bytes != null) ? new String(bytes, i, n) : new String(chars, i, n);
	}
	
	public String getText()
	{
		String text = getBufferString(0, index);
		int length = (bytes != null) ? bytes.length : chars.length;
		if (count >= length)
		{
			text = getBufferString(index, length - index) + text;
		}
		return text;
	}
	
	public void put(int value)
	{
	    int length = 0;
	    if (bytes != null)
	    {
	        bytes[index] = (byte) value;
	        length = bytes.length;
	    }
	    else
	    {
	        chars[index] = (char) value;
	        length = chars.length;
	    }
		index = (index >= (length - 1)) ? 0 : index + 1;
		count++;
		notifyRecordListeners(value);
	}
	
	public static class RecordEvent
	extends EventObject
	{
		private static final long serialVersionUID = 1L;
		protected int value = 0;

		public RecordEvent(Object source)
		{
			super(source);
		}
		
		public int getValue()
		{
			return value;
		}
		
		public void setValue(int n)
		{
			value = n;
		}
	}
	
	public interface RecordListener
	extends EventListener
	{
		public void valueRecorded(RecordEvent event);
	}
}
