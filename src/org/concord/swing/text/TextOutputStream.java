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
// $Date: 2011-02-18 13:39:29 $
// $Author: moll $
//
package org.concord.swing.text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.swing.JTextArea;

public class TextOutputStream extends OutputStream
{
    protected TextWriter writer;

    public TextOutputStream(JTextArea ta)
    {
        writer = new TextWriter(ta);
    }

    public void close() throws IOException
    {
    }

    public void flush() throws IOException
    {
    }
    
    public Writer getWriter()
    {
        return writer;
    }

    public void write(byte b[]) throws IOException
    {
        writer.write(new String(b));
    }

    public void write(byte b[], int off, int len) throws IOException
    {
        writer.write(new String(b, off, len));
    }

    public void write(int b) throws IOException
    {
        writer.write(b);
    }
}

