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
// Class : TextWriter
//
// Copyright ? 1998, The Concord Consortium
//
// Original Author: Edward Burke
//
// $Revision: 1.1 $
// $Date: 2011-02-18 13:39:30 $
// $Author: moll $
//
package org.concord.swing.text;

import java.io.Writer;

import javax.swing.JTextArea;

/**
 * The TextWriter class allows a TextArea to be used a an output object
 * which will accept write commands.<p>
 *
 * @version		$Revision: 1.1 $ $Date: 2011-02-18 13:39:30 $
 * @author 		$Author: moll $
**/

public class TextWriter extends Writer
{
    
    JTextArea text;

    public TextWriter(JTextArea ta)
    {
        text = ta;
    }

    public void close()
    {
    }

    public void flush()
    {
    }

    public void write(char [] cbuf)
    {
        String s = new String(cbuf);
        write(s);
    }

    public void write(char [] cbuf, int offset, int count)
    {
        String s = new String(cbuf, offset, count);
        write(s);
    }

    public void write(int chr)
    {
        char [] cbuf = new char[1];
        cbuf[0] = (char) chr;
        String s = new String(cbuf);
        write(s);
    }

    public void write(String str)
    {
        text.append(str);
    }

    public void write(String str, int offset, int count)
    {
        write(str.substring(offset, offset + count));
    }
}
