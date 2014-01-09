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

/**
 * Last modification information:
 * $Revision: 1.1 $
 * $Date: 2011-02-18 13:39:35 $
 * $Author: moll $
 *
 * Copyright 2004 The Concord Consortium
*/
package org.concord.swing.util;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A class that takes a screen shot of a Component
 * and saves it as a jpg, png or gif image
 * @author Ingrid Moncada<p>
 *
   $Revision: 1.1 $
*/

public class ComponentScreenshot
{	
	static private Hashtable extImageWriters = new Hashtable();
	
	public static void registerImageWriter(String type, ImageWriter writer)
	{
		extImageWriters.put(type, writer);
	}
	
	public static void saveImageAsFile(BufferedImage image, String filename)
		throws Throwable
	{
		saveImageAsFile(image, filename, "");
	}
	
	public static void saveImageAsFile(BufferedImage image, String filename, String type)
		throws Throwable
	{		
		try{
			File file = new File(filename);
			saveImageAsFile(image, file, type);

		}catch(Throwable e){
			e.printStackTrace();
			throw e;
		}
	}

	public static void saveImageAsFile(BufferedImage image, File file, String type)
		throws Throwable
	{
		FileOutputStream fos;
	
		fos = new FileOutputStream(file); 
		
		if (type==null || type.equals("")){
			saveImageAsOutputStream(image, fos);
		}
		else{
			saveImageAsOutputStream(image, fos, type);
		}
			
		fos.close();		
	}

	/**
	 * Saves the image in the output stream in the "best" format
	*/
	public static void saveImageAsOutputStream(BufferedImage image, OutputStream out)
		throws Throwable
	{
		//Try to save it as .png first
		try{
			saveImageAsOutputStream(image, out, "png");
		}catch(Throwable e){
		//If it fails, try to save it as .gif
			saveImageAsOutputStream(image, out, "gif");
		}
	}

	/**
	 * Saves the image in the output stream in the given format
	*/
	public static void saveImageAsOutputStream(BufferedImage image, OutputStream out, String type)
		throws Throwable
	{				
		try{
			ImageWriter writer = (ImageWriter)extImageWriters.get(type);
			if(writer != null) {
				writer.write(image, type, out);
			} else {
				// To keep clear compatibility with java 1.3
                Class clazz = Class.forName("javax.imageio.ImageIO");
                Method m = clazz.getMethod("write",new Class[]{Class.forName("java.awt.image.RenderedImage"),String.class,java.io.OutputStream.class});
                m.invoke(null,new Object[]{image,type,out});
			}
				
		}catch(Throwable e){
			e.printStackTrace();
			throw e;
		}
	}

	public static void saveScreenshotAsImageFile(Component c, String filename, String type)
		throws Throwable
	{
		BufferedImage image = getScreenshot(c);
		saveImageAsFile(image, filename, type);
	}

	public static BufferedImage getScreenshot(Component c)
		throws Exception
	{
		return getScreenshot(c,false);
	}
	
	public static BufferedImage getScreenshot(Component comp, boolean includeParents)
		throws Exception
	{
		Component c;
		
		c = comp;
		if (includeParents){
			//Hack?
			while (c.getParent()!=null && c.getParent() instanceof JPanel){
				c = c.getParent();
			}
		}
		
		return makeComponentImage(c, 1, 1);		
	}
	
	/**
	 * Given a graphics context and a component.  Paint this component
	 * on the graphics context.  This handles issues of dobule buffering
	 * without resizing or invalidating the component.
	 * 
	 * @param g
	 * @param c
	 * @param xScale
	 * @param yScale
	 */
	public static void paintScaledComponent(Graphics2D g, Component c, 
			float xScale, float yScale)
	{
		g.scale(xScale, yScale);
		
		if(c instanceof JComponent){
			JComponent jComp = (JComponent)c;
        	boolean dBuffered = jComp.isDoubleBuffered();
        	
        	// Double buffering must be turned off otherwise the paint method
        	// just copies the buffered image which causes pixelation.
        	// With buffering off the Graphics2D methods like drawLine, drawText
        	// are called so the resulting lines and fonts are hi-res
        	jComp.setDoubleBuffered(false);
            jComp.paint(g);
        	jComp.setDoubleBuffered(dBuffered);
		} else {
			c.paint(g);				
		}		
	}

	/**
	 * Create a buffered image that disregards any alpha settings
	 * 
	 * @param c
	 * @param xScale
	 * @param yScale
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage makeComponentImage(Component c, float xScale, float yScale)
		throws Exception
	{
		BufferedImage image = null;
		try{
			image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
		
			// Create a graphics contents on the buffered image
			Graphics2D g = image.createGraphics();

			paintScaledComponent(g,c,xScale,yScale);
			g.dispose();
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		return image;		
	}
	
	/**
	 * Slightly modified version of Dima's screen shot
	 * @param compForPicture
	 * @param xScale
	 * @param yScale
	 * @return
	 */
	public static BufferedImage makeComponentImageAlpha(Component compForPicture, float xScale, float yScale)
		throws Exception
	{
        Object bim = null;
        Class clazz = Class.forName("java.awt.GraphicsEnvironment");
        Method m = clazz.getMethod("getLocalGraphicsEnvironment",null);
        Object ge = m.invoke(null,null);
        m = clazz.getMethod("getDefaultScreenDevice",null);
        Object gd = m.invoke(ge,null);
        m = gd.getClass().getMethod("getDefaultConfiguration",null);
        Object gc = m.invoke(gd,null);
        m = gc.getClass().getMethod("getColorModel",null);
        java.awt.image.ColorModel cm = (java.awt.image.ColorModel)m.invoke(gc,null);
	    boolean hasAlpha = cm.hasAlpha();
	    int cw = compForPicture.getSize().width;
	    int ch = compForPicture.getSize().height;
	    int bw = (int)(cw * xScale);
	    int bh = (int)(ch * yScale);
	    if(hasAlpha){
	        m = gc.getClass().getMethod("createCompatibleImage",new Class[]{int.class,int.class});
	        bim = m.invoke(gc,new Object[]{new Integer(bw),new Integer(bh)});
	    }else{
	        clazz = Class.forName("java.awt.image.BufferedImage");
	        Constructor c = clazz.getConstructor(new Class[]{int.class,int.class,int.class});
	        Field f = clazz.getField("TYPE_INT_ARGB");
	        int imageType = f.getInt(null);
	        bim = c.newInstance(new Object[]{new Integer(bw),new Integer(bh),new Integer(imageType)});
	    }
        if(bim == null) {
        	return null;
        }
	    m = bim.getClass().getMethod("createGraphics",null);
        java.awt.Graphics2D og = (java.awt.Graphics2D)m.invoke(bim,null);
        paintScaledComponent(og, compForPicture, xScale, yScale);
        og.dispose();

        return (BufferedImage)bim;
        
        /*
         * Older version of non reflected code
        java.awt.image.BufferedImage bim = null;
	    java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
	    java.awt.GraphicsDevice gd = ge.getDefaultScreenDevice();
	    java.awt.GraphicsConfiguration.java gc = gd.getDefaultConfiguration();
	    boolean hasAlpha = gc.getColorModel().hasAlpha();
	    if(hasAlpha){
	        bim = gc.createCompatibleImage(compForPicture.getSize().width,compForPicture.getSize().height);
	    }else{
	        bim = new java.awt.image.BufferedImage(compForPicture.getSize().width,compForPicture.getSize().height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
	    }
        if(bim == null) return;
        java.awt.Graphics og = bim.getGraphics();
            compForPicture.paint(og);
        og.dispose();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(fileToSave);
        javax.imageio.ImageIO.write(bim,"png",fos);
        fos.close();
        */

/*
 * older version using jimi
        java.awt.Image img = compForPicture.createImage(compForPicture.getSize().width,compForPicture.getSize().height);
        java.awt.Graphics og = img.getGraphics();
        compForPicture.paint(og);
        java.awt.image.ImageProducer ip =img.getSource();
        
        javax.imageio.ImageIO.write(bim,fileExtension,fos);

        
        com.sun.jimi.core.JimiImage jimiimage = com.sun.jimi.core.Jimi.createRasterImage(ip);
        com.sun.jimi.core.encoder.png.PNGEncoder pngencoder = new com.sun.jimi.core.encoder.png.PNGEncoder();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(fileToSave);
        pngencoder .encodeImage(jimiimage,fos);
        fos.close();
        og.dispose();
*/

	}
	
}
