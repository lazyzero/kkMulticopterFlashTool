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
// Class : Util
//
// Copyright ? 2002, The Concord Consortium
//
// Original Author: Dmitry Markman
//
// $Revision: 1.1 $
//

package org.concord.swing.util;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

import javax.swing.JFileChooser;

public class Util
{
	public static int getIntColorFromStringColor(String linkColor){
		if(linkColor == null) return 0;
		String str = linkColor;
		if(linkColor.length() > 6){
			str = linkColor.substring(0,6);
		}else if(linkColor.length() < 6){
			int addZero = 6 - linkColor.length();
			for(int i = 0; i < addZero; i++){
				str += "0";
			}
		}
		int color = 0;
		int rColor = getIntFromHexString(str.substring(0,2));
		rColor <<= 16;
		color |= rColor;
		int gColor = getIntFromHexString(str.substring(2,4));
		gColor <<= 8;
		color |= gColor;
		int bColor = getIntFromHexString(str.substring(4,6));
		color |= bColor;
		return color;
	}
	
	public static float getFloatFromString(String str){
		float retValue = 0;
		try{
//			retValue = Float.parseFloat(str);
			retValue = Float.valueOf(str).floatValue();
		}catch(Exception te){
			retValue = Float.NaN;
		}
		return retValue;		
	}
	public static int getIntFromString(String str){
	    return getIntFromString(str,0);
	}
	
	public static int getIntFromString(String str,int defValue){
		int retValue = defValue;
		try{
			retValue = Integer.parseInt(str);
		}catch(Exception te){
			retValue = defValue;
		}
		return retValue;
	}
	
	
	public static boolean getBooleanFromString(String str){
		if(str == null) return false;
		return str.equals("true");
	}
	
	public static int getIntFromHexString(String str){
		int retValue = 0;
		if(str == null || str.length() < 1) return retValue;
		int multiplayer = 1;
		for(int i = str.length() - 1; i >= 0; i--){
			retValue += (multiplayer*getIntFromHexChar(str.charAt(i)));
			multiplayer <<= 4;
		}
		return retValue;
	}
	
	public static int getIntFromHexChar(char c){
		if(c >= '0' && c <= '9') return (int)(c - '0');
		if(c >= 'a' && c <= 'f') return (10 + (int)(c - 'a'));
		if(c >= 'A' && c <= 'F') return (10 + (int)(c - 'A'));
		return 0;
	}

    public static boolean equalWithTolerance(float a,float b,float eps){
        if(a == 0) return Math.abs(a) < eps;
        return Math.abs((a-b)/a) < eps;
    }
    public static boolean equalWithTolerance(double a,double b,double eps){
        if(a == 0) return Math.abs(a) < eps;
        return Math.abs((a-b)/a) < eps;
    }

    public static void setAntialisingWithReflection(java.awt.Graphics g,boolean on){
	    try{
	        Class clss1 = Class.forName("java.awt.RenderingHints");
	        Class clss2 = Class.forName("java.awt.RenderingHints$Key");
            java.lang.reflect.Method m = g.getClass().getMethod("setRenderingHint", new Class[]{clss2,java.lang.Object.class});
		    java.lang.reflect.Field  f1 = clss1.getField("KEY_ANTIALIASING");
		    java.lang.reflect.Field  f2 = (on)?clss1.getField("VALUE_ANTIALIAS_ON"):clss1.getField("VALUE_ANTIALIAS_OFF");
		    m.invoke(g,new Object[]{f1.get(null),f2.get(null)});
		}catch(Throwable t){}
    }
    
    public static String getSharedFolderPath(){
        int   kSharedUserDataFolder		  = 0x73646174;//sdat
        return getFolderPath(kSharedUserDataFolder);
    }
    public static String getApplicationFolderPath(){
        int   kApplicationFolder		  = 0x61707073;//apps 
        return getFolderPath(kApplicationFolder);
    }
    public static String getGlobalApplicationFolderPath(){
        int   kApplicationFolder		  = 0x61707073;//apps 
        short kSystemDomain                 = -32766;
        return getFolderPath(kSystemDomain,kApplicationFolder);
    }
    public static String  getFolderPath(int folderKind){
        try{//MAC OS X 1.4.1
            Class clazz = Class.forName("com.apple.eio.FileManager");
            java.lang.reflect.Method m = clazz.getMethod("findFolder",new Class[]{int.class});
            return (String)m.invoke(null,new Object []{new Integer(folderKind)});
        }catch(Throwable t){}
        try{//MAC OS X 1.3.1
            Class clazz = Class.forName("com.apple.mrj.MRJFileUtils");
            Class macOsTypeClazz = Class.forName("com.apple.mrj.MRJOSType");
            java.lang.reflect.Constructor c = macOsTypeClazz.getConstructor(new Class[]{int.class});
            Object macOsType = c.newInstance(new Object []{new Integer(folderKind)});
            java.lang.reflect.Method m = clazz.getMethod("findFolder",new Class[]{macOsTypeClazz});
            return ((java.io.File)m.invoke(null,new Object []{macOsType})).getCanonicalPath();
        }catch(Throwable t){}
        return null;
    }
    public static String  getFolderPath(short domain,int folderKind){
        try{//MAC OS X 1.4.1
            Class clazz = Class.forName("com.apple.eio.FileManager");
            java.lang.reflect.Method m = clazz.getMethod("findFolder",new Class[]{short.class,int.class});
            return (String)m.invoke(null,new Object []{new Short(domain),new Integer(folderKind)});
        }catch(Throwable t){}
        try{//MAC OS X 1.3.1
            Class clazz = Class.forName("com.apple.mrj.MRJFileUtils");
            Class macOsTypeClazz = Class.forName("com.apple.mrj.MRJOSType");
            java.lang.reflect.Constructor c = macOsTypeClazz.getConstructor(new Class[]{int.class});
            Object macOsType = c.newInstance(new Object []{new Integer(folderKind)});
            java.lang.reflect.Method m = clazz.getMethod("findFolder",new Class[]{short.class,macOsTypeClazz});
            return ((java.io.File)m.invoke(null,new Object []{new Short(domain),macOsType})).getCanonicalPath();
        }catch(Throwable t){}
        return null;
    }
    
	public static void sortInsert(Vector list, Object item)
	{
		int n = list.size();
		for (int i = 0; i < n; i++)
		{
			Object element = list.elementAt(i);
			if (item.toString().compareTo(element.toString()) > 0)
				continue;
			else
			{
				list.insertElementAt(item, i);
				return;
			}
		}
		list.addElement(item);
	}

	public static void makeScreenShot(Component component){
		makeScreenShot(component, 1, 1);
	}
	
	public static void makeScreenShot(Component component, float xScaleArg, float yScaleArg){
	    if(component == null) return;
	    
        if(component instanceof javax.swing.JFrame){
            component = ((javax.swing.JFrame)component).getContentPane();
        }
        
        final java.awt.Component compForPicture = component;
        final float xScale = xScaleArg;
        final float yScale = yScaleArg;
        
        boolean dispatchThread = javax.swing.SwingUtilities.isEventDispatchThread(); 
        Runnable screenShotRunnable  = new Runnable(){
            public void run(){
                //javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                org.concord.swing.CCJFileChooser chooser = new org.concord.swing.CCJFileChooser(compForPicture.getClass().getName());
                chooser.setMultiSelectionEnabled(false);
                //String userdir = System.getProperty("user.dir");
                //if(userdir != null) chooser.setCurrentDirectory(new File(userdir));
                chooser.setFileFilter(new ImageFileFilter());
                int retValue = chooser.showSaveDialog(javax.swing.SwingUtilities.getRoot(compForPicture));
                if(retValue == JFileChooser.APPROVE_OPTION){
                    File fileToSave = chooser.getSelectedFile();
                    if(!fileToSave.getName().toLowerCase().endsWith(".png")){
                        fileToSave = new File(fileToSave.getAbsolutePath()+".png");
                    }
                    if(!fileToSave.exists() || checkForReplace(fileToSave)){
                        try{
                        	BufferedImage bim = 
                        		ComponentScreenshot.makeComponentImageAlpha(compForPicture,
                        				xScale, yScale);

                        	ComponentScreenshot.saveImageAsFile(bim, fileToSave, "png");

                        }catch(Throwable t){
                            System.out.println("make screenshot Throwable "+t);
                        }
                    }
                }
            }
            boolean checkForReplace(File file){
                if(file == null || !file.exists()) return false;
                final Object[] options = { "Yes", "No" };
                return javax.swing.JOptionPane.showOptionDialog(null,
                          "The file '" + file.getName() +
                          "' already exists.  " +
                          "Replace existing file?",
                          "Warning",
                          javax.swing.JOptionPane.YES_NO_OPTION,
                          javax.swing.JOptionPane.WARNING_MESSAGE,
                          null,
                          options,
                          options[1]) == javax.swing.JOptionPane.YES_OPTION;

            }
        };
        
        if (dispatchThread)
        {
            screenShotRunnable.run();
        }else{
            try{
                javax.swing.SwingUtilities.invokeAndWait(screenShotRunnable);
            }catch(Throwable t){
                System.out.println("makeScreenShot throwable "+t);
            }
        }
    }


}

class ImageFileFilter extends javax.swing.filechooser.FileFilter{
    public boolean accept(File f){
        if(f == null) return false;
        if (f.isDirectory())  return true;

        return (f.getName().toLowerCase().endsWith(".png"));
    }
    public String getDescription(){
        return "PNG images";
    }
    
}

