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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.concord.swing.RecordBuffer.RecordEvent;

public class StreamRecordView
extends JTabbedPane
{
	private static final long serialVersionUID = 1L;
	protected StreamRecord streamRecord;
	
	public StreamRecordView(StreamRecord record)
	{
		streamRecord = record;
	}
	
	public String getText(String tabName)
	{
		int index = indexOfTab(tabName);
		RecordView recordView = (RecordView) getComponentAt(index);
		return recordView.getText();
	}
	
	public InputStream addInputStream(InputStream stream, InputStream shared)
	{
		return addInputStream(stream, null, shared);
	}
	
	public InputStream addInputStream(InputStream stream, String name)
	{
		return addInputStream(stream, name, null);
	}
	
	protected InputStream addInputStream(InputStream stream, String name, InputStream shared)
	{
		InputStream result = (InputStream) streamRecord.getStream(stream, shared);
		if ((result instanceof InputStream) && (shared == null) && (name != null))
		{
			RecordView recordView = new RecordView(streamRecord, result);
			insertTab(name, null, recordView, "InputStream", 0);
		}
		return result;
	}
	
	public OutputStream addOutputStream(OutputStream stream, OutputStream shared)
	{
		return addOutputStream(stream, null, shared);
	}
	
	public OutputStream addOutputStream(OutputStream stream, String name)
	{
		return addOutputStream(stream, name, null);
	}
	
	protected OutputStream addOutputStream(OutputStream stream, String name, OutputStream shared)
	{
		OutputStream result = (OutputStream) streamRecord.getStream(stream, shared);
		if ((result instanceof OutputStream) && (shared == null) && (name != null))
		{
			RecordView recordView = new RecordView(streamRecord, result);
			insertTab(name, null, recordView, "OutputStream", 0);
		}
		return result;
	}
	
	public static void main(String [] args)
	{
		JFrame frame = new JFrame("Test RecordView");
		StreamRecord record = new StreamRecord(10000);
		StreamRecordView view = new StreamRecordView(record);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		System.setIn(view.addInputStream(System.in, "System.in"));
		System.setOut((PrintStream) view.addOutputStream(System.out, "System.out"));
		System.setErr((PrintStream) view.addOutputStream(System.err, System.out));
		for (int i = 0; i < 1000; i++)
		{
			System.out.println("This is line # " + i);
			System.err.println("This is error # " + i);
		}
		frame.getContentPane().add(view);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
	
	public static class RecordView
	extends JComponent
	implements RecordBuffer.RecordListener
	{
		private static final long serialVersionUID = 1L;
		protected StreamRecord streamRecord;
		protected Object recordStream;
		protected JTextArea textArea = new JTextArea();
		protected JScrollPane scroll = new JScrollPane(textArea);

		public RecordView(StreamRecord record, Object stream)
		{
			streamRecord = record;
			recordStream = stream;
			setLayout(new BorderLayout());
			add(scroll, "Center");
		}
		
		public void addNotify()
		{
			super.addNotify();
			streamRecord.addRecordListener(recordStream, this);
			textArea.setText(streamRecord.getText(recordStream));
			repaint();
		}
		
		public void removeNotify()
		{
			super.removeNotify();
			streamRecord.removeRecordListener(recordStream, this);
		}
		
		public String getText()
		{
			return streamRecord.getText(recordStream);
		}

		public void valueRecorded(RecordEvent event)
		{
			if ((event.getValue() == '\n') || (event.getValue() == '\r'))
			{
				textArea.setText(streamRecord.getText(recordStream));
				repaint();
			}
		}
	}
}