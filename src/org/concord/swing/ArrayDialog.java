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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class ArrayDialog
extends VariableDialog
{
    public ArrayDialog(JFrame frame, Hashtable variables)
    {
        super(frame, "Add and Remove Array Objects", variables);
    }
	
	public ModifyValueDialog createModifyValueDialog(JFrame frame)
	{
		return new ModifyArrayDialog(frame);
	}
	
	class ModifyArrayDialog
	extends ModifyValueDialog
	implements ActionListener
	{
		protected JList sinkList = new JList();
		protected JList sourceList = new JList();
		protected JScrollPane sinkListScroll = new JScrollPane(sinkList);
		protected JScrollPane sourceListScroll = new JScrollPane(sourceList);
		protected JPanel buttonPanel = new JPanel();
		protected JPanel editListPanel = new JPanel();
		protected JButton addButton = new JButton("Add");;
		protected JButton removeButton = new JButton("Remove");
		protected final Vector EMPTY = new Vector();
		
		public ModifyArrayDialog(JFrame frame)
		{
			super(frame, "Modify Array Object");
			editListPanel.setLayout(new BorderLayout());
			buttonPanel.setLayout(new FlowLayout());
			editListPanel.add(sinkListScroll, "West");
			editListPanel.add(sourceListScroll, "East");
			editListPanel.add(buttonPanel, "Center");
			sinkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			sourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		
		public void actionPerformed(ActionEvent event)
		{
		}
		
		public void setSourceList(Vector values)
		{
			sourceList.setListData(values);
			sinkList.setListData(EMPTY);
		}
		
		public void setValue(Object value)
		{
			super.setValue(value);
			Vector arrayValues = (Vector) value;
			sinkList.setListData(arrayValues);
		}
	}
}

