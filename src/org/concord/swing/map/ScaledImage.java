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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class ScaledImage
extends JComponent
implements ImageObserver
{
	Image image;
	
	public ScaledImage()
	{
	}
	
	public void setImage(Image image)
	{
		this.image = image;
	}
	
	public Image getImage()
	{
		return image;
	}
	
	public void setImage(String imageFileName)
	{
		ImageIcon imageIcon = new ImageIcon(imageFileName);
		setImage(imageIcon.getImage());
	}
	
	public void setImage(URL imageURL)
	{
		ImageIcon imageIcon = new ImageIcon(imageURL);
		setImage(imageIcon.getImage());
	}
	
	public boolean imageUpdate(Image image, int flags, int x, int y, int width, int height)
	{
		return (flags & ImageObserver.ALLBITS) == ImageObserver.ALLBITS;
	}
	
	public void paintComponent(Graphics g)
	{
		Dimension size = getSize();
		if (image instanceof Image)
		{
			int width = image.getWidth(this);
			int height = image.getHeight(this);
			g.drawImage(image, 0, 0, size.width, size.height, 0, 0, width, height, this);
		}
	}
}

