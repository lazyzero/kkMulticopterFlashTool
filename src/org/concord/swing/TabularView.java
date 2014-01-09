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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;


public class TabularView
extends JComponent
{
    protected Vector rows = new Vector();
    protected int columnCount = 0;
    protected int [] columnWidths;
    protected Dimension totalSize = new Dimension(0, 0);
    protected Dimension pad = new Dimension(10, 5);

    public TabularView()
    {
        setLayout(null);
        setPreferredSize(totalSize);
        setFont(new Font("SanSerif", Font.PLAIN, 12));
        
    }
    
    public void addRow(String [] values)
    {
        FontMetrics metrics = getFontMetrics(getFont());
        if (columnWidths == null)
        {
            columnCount = values.length;
            columnWidths = new int[columnCount];
        }
        if (values.length == columnCount)
        {
            JLabel [] row = new JLabel[columnCount];
            rows.addElement(row);
            for (int i = 0; i < columnCount; i++)
            {
                row[i] = new JLabel(values[i]);
                row[i].setFont(getFont());
                row[i].setForeground(getForeground());
                columnWidths[i] = Math.max(columnWidths[i], metrics.stringWidth(values[i]));
                add(row[i]);
            }
            int n = rows.size();
            int offsetX = 0;
            int offsetY = 0;
            for (int i = 0; i < n; i++)
            {
                row = (JLabel []) rows.elementAt(i);
                for (int j = 0; j < columnCount; j++)
                {
                    row[j].setSize(columnWidths[j] + pad.width, metrics.getHeight() + pad.height);
                    row[j].setLocation(offsetX, offsetY);
                    offsetX += columnWidths[j] + pad.width;
                }
                offsetY += metrics.getHeight() + pad.height;
                offsetX = 0;
            }
            totalSize.width = offsetX;
            totalSize.height = offsetY;
        }
    }
    
    public void setColumnAlignment(int column, int align)
    {
        if (column < columnCount)
        {
            int n = rows.size();
            for (int i = 0; i < n; i++)
            {
                JLabel [] row = (JLabel []) rows.elementAt(i);
                row[column].setHorizontalAlignment(align);
            }
        }
    }
    
    public void setPadding(int h, int v)
    {
        pad.width = h;
        pad.height = v;
    }
    
    public Dimension getPadding()
    {
        return pad;
    }
    
    public Dimension getPreferredSize()
    {
        return totalSize;
    }
    
    public void clear()
    {
        removeAll();
        rows.removeAllElements();
        columnCount = 0;
        totalSize.width = 0;
        totalSize.height = 0;
        columnWidths = null;
    }
}

