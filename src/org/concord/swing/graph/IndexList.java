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
import java.util.Vector;

public class IndexList
{
	protected Vector list = new Vector();
	
	public void add(int i)
	{
		list.addElement(new Integer(i));
	}
	
	public void insert(int i, int n)
	{
		list.insertElementAt(new Integer(n), i);
	}
	
	public void remove(int i)
	{
		list.removeElementAt(i);
	}
	
	public void removeValue(int i)
	{
		for (int n = 0; n < list.size(); n++)
		{
			if (get(n) == i)
			{
				remove(n);
				break;
			}
		}
	}
	
	public void removeAll()
	{
		list.removeAllElements();
	}
	
	public int get(int i) 
	{
		return ((Integer) list.elementAt(i)).intValue();
	}
	
	public void set(int i, int n)
	{
		list.setElementAt(new Integer(n), i);
	}
	
	public void addTo(int i, int n)
	{
		set(i, get(i) + n);
	}
	
	public int[] getArray()
	{
		int[] is = new int[list.size()];
		for (int i = 0; i < is.length; i++)
			is[i] = get(i);
		return is;
	}
	
	public Vector getVector()
	{
		return (Vector) list.clone();
	}
	
	public int size()
	{
		return list.size();
	}
	
	public String toString()
	{
		String string = "[";
		for (int i = 0; i < list.size(); i++)
		{
			if (i > 0)
				string += ", ";
			string += get(i);
		}
		string += "]";
		return string;
	}
}
