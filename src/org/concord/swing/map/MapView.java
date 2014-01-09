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

package org.concord.swing.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.ImageObserver;
import java.util.Vector;

import javax.swing.JComponent;

public class MapView
extends JComponent
implements MapSelection, ImageObserver
{
	protected boolean creatingNode = false;
	protected boolean creatingArc = false;
	protected MapContainer containerItem;
	protected Dimension preferredSize = new Dimension(800, 600);
	protected Vector listeners = new Vector();
	protected Vector selected = new Vector();
	protected Vector deselected = new Vector();
	protected Rectangle selectBox = new Rectangle(0, 0, 0, 0);
	protected boolean nesting = true;
	protected boolean selecting = true;
	protected boolean moving = true;
	protected boolean inView = false;
	protected int pressedX;
	protected int pressedY;
	protected int xMove;
	protected int yMove;
	protected boolean dragged = false;
	protected boolean dragMove = false;
	protected int [] initialXArc = { 0, 50, 50, 65, 50, 50, 0 };
	protected int [] initialYArc = { 6, 6, 0, 8, 16, 10, 10 };
	protected int [] xArc = new int[initialXArc.length];
	protected int [] yArc = new int[initialYArc.length];
	protected Polygon arcPolygon = new Polygon();
	protected ArcItem newArc;
	protected Point arcPoint;
	protected ArcItem movingArc;
	protected final Runnable runUpdate = new Runnable()
	{
		public void run()
		{
			Font font = MapView.this.getFont();
			if ((containerItem != null) && (font != null))
			{
				int minX = 0;
				int minY = 0;
				int maxX = 0;
				int maxY = 0;
				FontMetrics metrics = getFontMetrics(font);
	
				Vector nodeItems = containerItem.getNodeItems();
				for (int i = 0; i < nodeItems.size(); i++)
				{
					NodeItem nodeItem = (NodeItem) nodeItems.elementAt(i);
					Rectangle bounds = nodeItem.computeBounds(metrics);
					nodeItem.setSize(bounds.getSize());
					Rectangle b = nodeItem.getBounds();
					minX = Math.min(minX, b.x);
					minY = Math.min(minY, b.y);
					maxX = Math.max(maxX, b.x + b.width);
					maxY = Math.max(maxY, b.y + b.height);
					preferredSize.width = Math.max(preferredSize.width, maxX - minX);
					preferredSize.height = Math.max(preferredSize.height,maxY - minY);
				}
	
				if ((minX < 0) || (minY < 0))
				{				
					int moveX = minX < 0 ? -minX : 0;
					int moveY = minY < 0 ? -minY : 0;
					for (int i = 0; i < nodeItems.size(); i++)
					{
						NodeItem nodeItem = (NodeItem) nodeItems.elementAt(i);
						nodeItem.translate(moveX, moveY);
						Rectangle b = nodeItem.getBounds();
						preferredSize.width = Math.max(preferredSize.width, b.x + b.width);
						preferredSize.height = Math.max(preferredSize.height, b.y + b.height);
					}
					Vector arcItems = containerItem.getArcItems();
					for (int i = 0; i < arcItems.size(); i++)
					{
						ArcItem arcItem = (ArcItem) arcItems.elementAt(i);
						arcItem.translate(moveX, moveY);
					}
				}
			}
		}
	};
	
	public MapView()
	{
		super();
		containerItem = new NodeItem(null, "Root");
		setPreferredSize(preferredSize);
		addMouseListener(new MapViewMouseAdapter());
		addMouseMotionListener(new MapViewMouseMotionAdapter());
	}
	
	public void addNotify()
	{
		super.addNotify();
		updateView();
	}
 
	public void updateView()
	{
		try
		{
			
			if (! EventQueue.isDispatchThread())
			{
				EventQueue.invokeLater(runUpdate);
				return;
			}
		}
		catch (Exception e)
		{
		}
		runUpdate.run();
		repaint();
	}
	
	public void setNestingEnabled(boolean nest)
	{
		nesting = nest;
	}
	
	public boolean isNestingEnabled()
	{
		return nesting;
	}
	
	public void setSelectingEnabled(boolean select)
	{
		selecting = select;
	}
	
	public boolean isSelectingEnabled()
	{
		return selecting;
	}
	
	public void setMovingEnabled(boolean move)
	{
		moving = move;
	}
	
	public boolean isMovingEnabled()
	{
		return moving;
	}
	
	public void addMapItemListener(MapItemListener listener)
	{
		if (listener instanceof MapItemListener)
			listeners.addElement(listener);
	}
	
	public void removeMapItemListener(MapItemListener listener)
	{
		if (listener instanceof MapItemListener)
			listeners.removeElement(listener);
	}
	
	public void callMapItemListeners(Object arg, int type)
	{
		MapEvent event = new MapEvent(this, arg, type);
		for (int i = 0; i < listeners.size(); i++)
		{
			MapItemListener listener = (MapItemListener) listeners.elementAt(i);
			switch (type)
			{
				case MapEvent.ITEM_SELECTED:
				listener.mapItemSelection(event);
				break;
				
				case MapEvent.ITEM_MOVED:
				listener.mapItemMove(event);
				break;
				
				case MapEvent.ITEM_OPENED:
				listener.mapItemOpen(event);
				break;
				
				case MapEvent.ITEM_CREATED:
				listener.mapItemCreate(event);
				break;
				
				case MapEvent.ITEM_DELETED:
				listener.mapItemDelete(event);
				break;
				
				case MapEvent.ITEM_POPUP:
				listener.mapItemPopup(event);
				break;
			}
		}
	}
	
	public void setSelected(MapItem item, boolean multiple)
	{
		if (multiple)
		{
			if (selected.contains(item) && ! deselected.contains(item))
				deselected.addElement(item);
		}
		else
		{
			if (! selected.contains(item))
				selected.removeAllElements();
		}
		if ((item instanceof MapItem) && ! selected.contains(item))
			selected.addElement(item);
	}
	
	public void checkSelected(MapItem item, boolean multiple)
	{
		for (int i = 0; i < deselected.size(); i++)
			selected.removeElement(deselected.elementAt(i));
		deselected.removeAllElements();
	}
	
	public void reselect()
	{
		deselected.removeAllElements();
	}

	public boolean isMultiple(MouseEvent event)
	{
		return event.isShiftDown();
	}
	
	public void setContainerItem(MapContainer item)
	{
		setContainerItem(item, true);
	}

	public void setContainerItem(MapContainer item, boolean setSelected)
	{
		if(item != null)
		{
			containerItem = item;

			if (setSelected)
			{
				setSelected(null, false);
			}
		}
		updateView();
	}
	
	public MapContainer getContainerItem()
	{
		return containerItem;
	}
       
    public void paintComponent(Graphics g)
    {
		Vector nodeItems = containerItem.getNodeItems();
		Vector arcItems = containerItem.getArcItems();
        if (containerItem instanceof MapContainer)
        {
            Dimension size = getSize();
            g.clearRect(0, 0, size.width, size.height);
			for (int test = 0; test < (selecting ? 2 : 1); test++)
			{
				boolean drawSelected = (test == 1);
				for (int i = 0; i < arcItems.size(); i++)
				{
					ArcItem arcItem = (ArcItem) arcItems.elementAt(i);
					if (drawSelected && selected.contains(arcItem))
						arcItem.drawConnect(g, true);
					else
						arcItem.drawConnect(g, false);
				}
				for (int i = 0; i < nodeItems.size(); i++)
				{
					NodeItem nodeItem = (NodeItem) nodeItems.elementAt(i);
					if (drawSelected && selected.contains(nodeItem))
						nodeItem.draw(g, true);
					else
						nodeItem.draw(g, false);
				}
				for (int i = 0; i < arcItems.size(); i++)
				{
					ArcItem arcItem = (ArcItem) arcItems.elementAt(i);
					if (drawSelected && selected.contains(arcItem))
						arcItem.draw(g, true);
					else
						arcItem.draw(g, false);
				}
			}
			if (selecting && dragged && ! dragMove)
				g.drawRect(selectBox.x, selectBox.y, selectBox.width, selectBox.height);
			if (creatingNode && inView)
			{
				g.setColor(Color.blue);
				g.fillOval(xMove - 15, yMove - 15, 30, 30);
				g.setColor(Color.black);
				g.drawOval(xMove - 15, yMove - 15, 30, 30);
			}
			if (creatingArc && inView)
			{
				g.setColor(Color.black);
				for (int i = 0; i < initialXArc.length; i++)
				{
					xArc[i] = initialXArc[i];
					yArc[i] = initialYArc[i];
				}
				arcPolygon.npoints = xArc.length;
				arcPolygon.xpoints = xArc;
				arcPolygon.ypoints = yArc;
				arcPolygon.translate(xMove - 30, yMove - 8);
				g.fillPolygon(arcPolygon);
			}
        }
    }
	
	protected MapItem findMapItem(int x, int y)
	{
		Vector [] lists =
		{
			containerItem.getArcItems(),
			containerItem.getNodeItems()
		};
		MapItem item = null;
		for (int n = 0; n < lists.length; n++)
		{
			Vector items = lists[n];
			item =  findSingleMapItem(items, x, y);
			if (item instanceof MapItem)
				break;
		}
		return item;
	}
	
	protected MapItem findSingleMapItem(Vector items, int x, int y)
	{
		for (int i = 0; i < items.size(); i++)
		{
			MapItem item = (MapItem) items.elementAt(i);
			if (item.getBounds().contains(x, y))
				return item;
			if (item instanceof ArcItem)
			{
				ArcItem arcItem = (ArcItem) item;
				if (arcItem.isNearSourceLocation(x, y) || arcItem.isNearSinkLocation(x, y))
					return arcItem;
			}
		}
		return null;
	}
	
	protected NodeItem findNodeItem(int x, int y)
	{
		return (NodeItem) findSingleMapItem(containerItem.getNodeItems(), x, y);
	}
	
	protected ArcItem findArcItem(int x, int y)
	{
		return (ArcItem) findSingleMapItem(containerItem.getArcItems(), x, y);
	}
	
	protected Vector findMapItems(Rectangle box, Vector list, boolean multiple)
	{
		Vector [] lists =
		{
			containerItem.getNodeItems(),
			containerItem.getArcItems()
		};
		if (! multiple)
			list.removeAllElements();
		for (int n = 0; n < lists.length; n++)
		{
			Vector items = lists[n];
			for (int i = 0; i < items.size(); i++)
			{
				MapItem item = (MapItem) items.elementAt(i);
				if (item.getBounds().intersects(box))
				{
					list.addElement(item);
				}
			}
		}
		return list;
	}
	
	 public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	 {
		if (infoflags == ImageObserver.ALLBITS)
		{
			updateView();
			return false;
		}
		return true;
	 }
	
	protected class MapViewMouseAdapter
	extends MouseAdapter
	{
		protected MapItem item;
		protected boolean maybePopup;
		
		public void mousePressed(MouseEvent event)
		{
			pressedX = event.getX();
			pressedY = event.getY();
			item = findMapItem(pressedX, pressedY);
			setSelected(item, isMultiple(event));
			selectBox.setLocation(event.getPoint());
			dragMove = (selected.size() > 0);
			if (creatingNode)
			{
				NodeItem nodeItem = getContainerItem().createNodeItem("New Node", pressedX, pressedY);
				callMapItemListeners(nodeItem, MapEvent.ITEM_CREATED);
				creatingNode = false;
			}
			if (newArc instanceof MapItem)
			{
				NodeItem sinkNode = (item instanceof NodeItem) ? (NodeItem) item : null;
				if (sinkNode instanceof NodeItem)
					newArc.setSinkNode(sinkNode);
				else
					newArc.setSinkLocation(pressedX, pressedY);
				callMapItemListeners(newArc, MapEvent.ITEM_CREATED);
				newArc = null;
			}
			if (creatingArc)
			{
				NodeItem sourceNode = (item instanceof NodeItem) ? (NodeItem) item : null;
				newArc = MapView.this.getContainerItem().createArcItem(sourceNode, null, pressedX, pressedY);
				creatingArc = false;
			}
			if (item instanceof ArcItem)
			{
				ArcItem arcItem = (ArcItem) item;
				if (arcItem.isNearSourceLocation(pressedX, pressedY))
					arcPoint = arcItem.getSourcePoint();
				else if (arcItem.isNearSinkLocation(pressedX, pressedY))
					arcPoint = arcItem.getSinkPoint();
				if (arcPoint instanceof Point)
				{
					selected.removeElement(arcItem);
					movingArc = arcItem;
				}
			}
			MapView.this.repaint();
			maybePopup = event.isPopupTrigger();
		}
		
		public void mouseReleased(MouseEvent event)
		{
			dragged = false;
			dragMove = false;
			checkSelected(item, isMultiple(event));
			callMapItemListeners(selected, MapEvent.ITEM_SELECTED);
			if (maybePopup || event.isPopupTrigger())
				callMapItemListeners(event, MapEvent.ITEM_POPUP);
			if (arcPoint instanceof Point)
			{
				NodeItem nodeItem = findNodeItem(arcPoint.x, arcPoint.y);
				if (nodeItem instanceof NodeItem)
				{
					if (movingArc.getSourcePoint() == arcPoint)
						movingArc.setSourceNode(nodeItem);
					else if (movingArc.getSinkPoint() == arcPoint)
						movingArc.setSinkNode(nodeItem);
				}
			}
			movingArc = null;
			arcPoint = null;
			MapView.this.repaint();
		}
		
		public void mouseClicked(MouseEvent event)
		{
			int x = event.getX();
			int y = event.getY();
			MapItem item = findMapItem(x, y);
			if (event.getClickCount() > 1)
			{
				if (item instanceof MapItem)
					if (nesting)
					{
						setSelected(null, false);
						containerItem = item;
						updateView();
					}
					else
						setSelected(item, false);
				else
				{
					if (containerItem instanceof MapItem)
					{
						item = (MapItem) containerItem.getItemParent();
						if (item instanceof MapItem)
						{
							setSelected((MapItem) containerItem, false);
							containerItem = item;
							updateView();
						}
					}
				}
				callMapItemListeners(item, MapEvent.ITEM_OPENED);
			}
			MapView.this.repaint();
		}
		
		public void mouseEntered(MouseEvent event)
		{
			inView = true;
			MapView.this.repaint();
		}
		
		public void mouseExited(MouseEvent event)
		{
			inView = false;
			MapView.this.repaint();
		}
	}
	
	protected class MapViewMouseMotionAdapter
	extends MouseMotionAdapter
	{
		public void mouseDragged(MouseEvent event)
		{
			int x = event.getX();
			int y = event.getY();
			int width = Math.abs(x - pressedX);
			int height = Math.abs(y - pressedY);
			if (dragMove)
			{
				if (! dragged)
					reselect();
				if (moving)
				{
					if (arcPoint instanceof Point)
						arcPoint.setLocation(x, y);
					for (int i = 0; i < selected.size(); i++)
					{
						MapItem item = (MapItem) selected.elementAt(i);
						item.translate(x - pressedX, y - pressedY);
					}
					callMapItemListeners(selected, MapEvent.ITEM_MOVED);
				}
				updateView();
				pressedX = x;
				pressedY = y;
			}
			else
			{
				if (selecting)
				{
					selectBox.x = (x < pressedX) ? x : pressedX;
					selectBox.y = (y < pressedY) ? y : pressedY;
					selectBox.setSize(width, height);
					findMapItems(selectBox, selected, event.isShiftDown());
					MapView.this.repaint();
				}
			}
			dragged = true;
		}
		
		public void mouseMoved(MouseEvent event)
		{
			if (creatingNode || creatingArc  || (newArc instanceof ArcItem))
			{
				xMove = event.getX();
				yMove = event.getY();
				if (newArc instanceof ArcItem)
					newArc.setSinkLocation(xMove, yMove);
				MapView.this.repaint();
			}
		}
	}

	public void setCreatingNode(boolean value)
	{
		creatingNode = value;
	}
	
	public void setCreatingArc(boolean value)
	{
		creatingArc = value;
	}
}

