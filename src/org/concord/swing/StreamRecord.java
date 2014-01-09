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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Vector;

import org.concord.swing.RecordBuffer.RecordListener;

public class StreamRecord
{
	protected int bufferSize;
	protected HashMap streamTable = new HashMap();
	protected HashMap bufferTable = new HashMap();
	protected Vector listeners = new Vector();
	
	public StreamRecord(int size)
	{
		bufferSize = size;
	}
	
	protected Object createStream(Object stream, RecordStream recordStream)
	{
		if ((stream instanceof InputStream) || (stream instanceof OutputStream))
		{
			Class [] parameterTypes = new Class[1];
			if (stream instanceof InputStream)
				parameterTypes[0] =  InputStream.class;
			else
				parameterTypes[0] = OutputStream.class;
			Class streamClass = stream.getClass();
			try
			{
				Object [] parameters = { recordStream };
				Constructor constructor = streamClass.getConstructor(parameterTypes);
				Object resultStream = constructor.newInstance(parameters);
				if (resultStream != null)
				{
					streamTable.put(recordStream, stream);
					bufferTable.put(resultStream, recordStream);
				}
				return resultStream;
			}
			catch (Exception e)
			{
			}
		}
		return null;
	}
	
	protected RecordStream createRecordStream(Object stream, RecordBuffer buffer)
	{
		RecordStream recordStream = null;
		if (stream instanceof OutputStream)
		{
			recordStream = new RecordOutputStream((OutputStream) stream, buffer);
		}
		else if (stream instanceof InputStream)
		{
			recordStream = new RecordInputStream((InputStream) stream, buffer);
		}
		return recordStream;
	}
	
	protected RecordStream createRecordStream(Object stream, int bufferSize)
	{
		RecordStream recordStream = null;
		if (stream instanceof OutputStream)
		{
			recordStream = new RecordOutputStream((OutputStream) stream, bufferSize);
		}
		else if (stream instanceof InputStream)
		{
			recordStream = new RecordInputStream((InputStream) stream, bufferSize);
		}
		return recordStream;
	}
	
	public Object getStream(Object stream)
	{
		RecordStream recordStream = createRecordStream(stream, bufferSize);
		return createStream(stream, recordStream);
	}
	
	public Object getStream(Object stream, Object sharedStream)
	{
		if (sharedStream == null)
			return getStream(stream);
		
		RecordStream recordStream = (RecordStream) bufferTable.get(sharedStream);
		RecordBuffer buffer = recordStream.getRecordBuffer();
		return createStream(stream, createRecordStream(stream, buffer));
	}
	
	public String getText(Object stream)
	{
		if (stream instanceof OutputStream)
		{
			RecordOutputStream record = (RecordOutputStream) bufferTable.get(stream);
			return record.getText();
		}
		else if (stream instanceof InputStream)
		{
			RecordInputStream record = (RecordInputStream) bufferTable.get(stream);
			return record.getText();
		}
		return null;
	}
	
	public void addRecordListener(Object stream, RecordListener listener)
	{
		RecordStream recordStream = (RecordStream) bufferTable.get(stream);
		if ((listener != null) && (recordStream != null))
			recordStream.getRecordBuffer().addRecordListener(listener);
	}
	
	public void removeRecordListener(Object stream, RecordListener listener)
	{
		RecordStream recordStream = (RecordStream) bufferTable.get(stream);
		if ((listener != null) && (recordStream != null))
			recordStream.getRecordBuffer().removeRecordListener(listener);
	}

	public interface RecordStream
	{
		public String getText();
		public RecordBuffer getRecordBuffer();
	}
	
	public static class RecordInputStream
	extends InputStream
	implements RecordStream
	{
		protected RecordBuffer buffer;
		protected InputStream in;
		
		public RecordInputStream(InputStream inputStream, int size)
		{
			buffer = new RecordBuffer(size);
			in = inputStream;
		}
		
		public RecordInputStream(InputStream inputStream, RecordBuffer shareBuffer)
		{
			buffer = shareBuffer;
			in = inputStream;
		}
		
		public RecordBuffer getRecordBuffer()
		{
			return buffer;
		}
		
		public String getText()
		{
			return buffer.getText();
		}

		public int read()
		throws IOException
		{
			int value = in.read();
			buffer.put(value);
			return value;
		}
	}
	
	public static class RecordOutputStream
	extends OutputStream
	implements RecordStream
	{
		protected RecordBuffer buffer;
		protected OutputStream out;
		
		public RecordOutputStream(OutputStream outputStream, int size)
		{
			buffer = new RecordBuffer(size);
			out = outputStream;
		}
		
		public RecordOutputStream(OutputStream outputStream, RecordBuffer shareBuffer)
		{
			buffer = shareBuffer;
			out = outputStream;
		}
		
		public RecordBuffer getRecordBuffer()
		{
			return buffer;
		}
		
		public String getText()
		{
			return buffer.getText();
		}
		
		public void write(int value)
		throws IOException
		{
			out.write(value);
			buffer.put(value);
		}
	}
}