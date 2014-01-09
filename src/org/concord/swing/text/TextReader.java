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

//
// Class : TextOutputStream
//
// Copyright © 1998, The Concord Consortium
//
// Original Author: Edward Burke
//
// $Revision: 1.1 $
// $Date: 2011-02-18 13:39:30 $
// $Author: moll $
//
package org.concord.swing.text;

import java.io.IOException;
import java.io.Reader;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

public class TextReader extends Reader implements DocumentListener
{
	protected JTextArea text;
	protected Document document;
	protected StringBuffer buffer;
	protected Thread inputWaitThread;
	private volatile boolean threadSuspended = false;

	public TextReader(JTextArea ta)
	{
		text = ta;
		document = text.getDocument();
		buffer = new StringBuffer("");
	}

	public void close() throws IOException
	{
	}

	public int read() throws IOException
	{
		char [] cbuf = new char[1];
		read(cbuf);
		return (int) cbuf[0];
	}

	public int read(char cbuf[]) throws IOException
	{
		return read(cbuf, 0, cbuf.length);
	}


	public int read(char cbuf[], int off, int len) throws IOException
	{
		document.addDocumentListener(this);
        try
		{
            synchronized(this)
			{
				threadSuspended = true;
                while (threadSuspended)
                    wait();
            }
        }
		catch (InterruptedException e)
		{
        }
		buffer.getChars(0, len, cbuf, off);
		buffer = new StringBuffer(buffer.toString().substring(len));
		document.removeDocumentListener(this);
		return len;
	}

	public boolean ready() throws IOException
	{
		return buffer.length() > 0;
	}

	public int available() throws IOException
	{
		return buffer.length();
	}

	public void changedUpdate(DocumentEvent docEvent)
	{
		handleDocEvent(docEvent);
	}

	public void insertUpdate(DocumentEvent docEvent)
	{
		handleDocEvent(docEvent);
	}

	public void removeUpdate(DocumentEvent docEvent)
	{
		handleDocEvent(docEvent);
	}

	public void handleDocEvent(DocumentEvent docEvent)
	{
		AbstractDocument doc = (AbstractDocument) docEvent.getDocument();
		int len = docEvent.getLength();
		int i = docEvent.getOffset();
		DocumentEvent.EventType det = docEvent.getType();

		try
		{
			String currentText = doc.getText(0, doc.getLength());
			int n = currentText.length();
			currentText = currentText.substring(n - 1, n);
			buffer.append(currentText);
		    synchronized (this)
			{
				threadSuspended = false;
				notify();
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("handleDOcEvent: " + e.toString());
		}
	}
}
