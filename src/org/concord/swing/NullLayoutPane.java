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
import java.awt.Container;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JViewport;

import org.concord.swing.ViewEditor.SelectionHandle;

public class NullLayoutPane
extends JPanel
implements SelectableContainer
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3548766787761400582L;
	public static final Point ORIGIN = new Point(0, 0);
    protected boolean editable = false;
    protected SelectionManager selectionManager;
    protected Map itemMap = new HashMap();
    protected List selectableItemFreeList = new Vector();
    protected JPanel cover = new JPanel();
    protected JViewport viewport;
	protected ComponentAdapter paneListener = new ComponentAdapter()
	{
		public void componentMoved(ComponentEvent event)
		{
			setLocation(NullLayoutPane.this.getLocation());
		}
		public void componentResized(ComponentEvent event)
		{
			setSize(NullLayoutPane.this.getSize());
		}
		public void componentHidden(ComponentEvent event)
		{
			setVisible(false);
		}
		public void componentShown(ComponentEvent event)
		{
			setVisible(true);
		}
	};
    
    public NullLayoutPane()
    {
        setLayout(null);
        setOpaque(false);
        cover.setOpaque(false);
        setEditable(true);
    }
    
    public final void setLayout(LayoutManager manager)
    {
    }
    
    protected JViewport getViewport()
    {
        Container parent = getParent();
        while (parent != null)
        {
            if (parent instanceof JViewport)
                return (JViewport) parent;
            parent = parent.getParent();
        }
        return null;
    }
    
    public Point getOffset()
    {
        if (viewport instanceof JViewport)
            return viewport.getViewPosition();
        return ORIGIN;
    }
    
    public void addNotify()
    {
        super.addNotify();
        viewport = getViewport();
        Rectangle bounds = getBounds();
    	cover.setBounds(bounds);
    }
    
    public boolean isEditable()
    {
        return editable;
    }
    
    public void setEditable(boolean value)
    {
        editable = value;
        if (editable)
        {
            if (selectionManager == null)
            {
                selectionManager = new SelectionManager();
                selectionManager.setSelectableContainer(this);
            }
            add(cover, 0);
            cover.addComponentListener(paneListener);
            cover.addMouseListener(selectionManager);
            cover.addMouseMotionListener(selectionManager);
        }
        else
        {
            remove(cover);
            cover.removeComponentListener(paneListener);
            cover.removeMouseListener(selectionManager);
            cover.removeMouseMotionListener(selectionManager);
        }
    }
    
    public Component add(Component item)
    {
        SelectableItem selectable = (SelectableItem) itemMap.get(item);
        if (selectable == null)
        {
            if (selectableItemFreeList.isEmpty())
            {
                selectable = new SelectableItem(this);
            }
            else
            {
                selectable = (SelectableItem) selectableItemFreeList.get(0);
                selectableItemFreeList.remove(selectable);
            }
            itemMap.put(item, selectable);
        }
        super.add(item);
        selectable.setComponent(item);
        repaint();
        return item;
    }
    
    public void remove(Component item)
    {
        Selectable selectable = (Selectable) itemMap.get(item);
        if (selectable != null)
        {
            itemMap.remove(item);
            super.remove(item);
            if (selectable instanceof SelectableItem)
                selectableItemFreeList.add(selectable);
            repaint();
        }
    }
	
	protected void paintComponent(Graphics g)
	{
	    Iterator selectables = itemMap.values().iterator();
		while (selectables.hasNext())
		{
			SelectableItem item = (SelectableItem) selectables.next();
			item.draw(g);
		}
	}

    /* (non-Javadoc)
     * @see org.concord.swing.SelectableContainer#findSelectable(int, int)
     */
    public Selectable findSelectable(MouseEvent event, int x, int y)
    {
	    Iterator selectables = itemMap.values().iterator();
		while (selectables.hasNext())
		{
			SelectableItem item = (SelectableItem) selectables.next();
			if (item.contains(x, y))
			    return item;
		}
		return null;
    }

    /* (non-Javadoc)
     * @see org.concord.swing.Selectable#select(boolean)
     */
    public void select(Selectable selectable, boolean multiple)
    {
        if (! multiple)
        {
            deselect();
        }
        selectable.setSelected(true);
        repaint();
    }

    /* (non-Javadoc)
     * @see org.concord.swing.SelectableContainer#deselect()
     */
    public void deselect()
    {
	    Iterator selectables = itemMap.values().iterator();
		while (selectables.hasNext())
		{
			SelectableItem item = (SelectableItem) selectables.next();
			item.setSelected(false);
		}
        repaint();
    }
    
    public void moveAction(int dx, int dy, Selectable selectable)
    {
        SelectableItem item = (SelectableItem) selectable;
        SelectableItem.Boundary boundary = item.getBoundary();
		if (selectable.isSelected())
		{
			Rectangle b = boundary.getBounds();
			boundary.setLocation(b.x + dx, b.y + dy);
		}
    }

    /* (non-Javadoc)
     * @see org.concord.swing.SelectableContainer#dragAction(int, int, org.concord.swing.Selectable)
     */
    public void dragAction(int dx, int dy, Selectable selectable)
    {
	    SelectableItem item = (SelectableItem) selectable;
	    SelectableItem.Boundary boundary = item.getBoundary();
	    SelectableItem.Handle handle = boundary.getSelectedHandle();
		if ((selectable != null) && (handle != null))
		{
		    if (handle.getPosition() == SelectionHandle.DRAG)
		        moveAction(dx, dy, selectable);
		    else
		        handle.scale(dx, dy);
		}
	    else
	    {
		    Iterator selectables = itemMap.values().iterator();
			while (selectables.hasNext())
			{
				moveAction(dx, dy, (Selectable) selectables.next());
			}
	    }
       repaint();
    }

    /* (non-Javadoc)
     * @see org.concord.swing.SelectableContainer#dragActionDone(org.concord.swing.Selectable)
     */
    public void dragActionDone(Selectable selectable)
    {
        // TODO Auto-generated method stub
        repaint();
    }

	public Selectable getActiveObject()
	{
		return selectionManager.getActiveObject();
	}
}