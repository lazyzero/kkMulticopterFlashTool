/**
 * KKMulticopterFlashTool, a avrdude GUI for flashing KK boards and other
 *   equipment.
 *   Copyright (C) 2011 Christian Moll
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lazyzero.kkMulticopterFlashTool.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import lu.tudor.santec.i18n.Translatrix;
import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

public class Zip {
	
	static KKMulticopterFlashTool kk = KKMulticopterFlashTool.getInstance();

	public static File unzip(File zipFile, File file) {
		System.out.println("path to zipFile: " + zipFile.getPath());
		System.out.println("file to extract: " + file.getPath());
		String fileName = null;
		
		try {
   			zipFile.mkdir();
	         BufferedOutputStream dest = null;
	         BufferedInputStream is = null;
	         ZipEntry entry;
	         ZipFile zipfile = new ZipFile(zipFile);
	         Enumeration e = zipfile.entries();
	         while(e.hasMoreElements()) {
	            entry = (ZipEntry) e.nextElement();
	           // System.out.println(entry.getName());
	            if (entry.getName().substring(entry.getName().indexOf("/")+1).equals(file.getName())) {
	         //   if (entry.getName().contains(file.getName())){
	            	System.out.println("firmware to extract found.");
	            	
	            	String tempFolder = System.getProperty("user.dir")+File.separatorChar+"tmp"+File.separatorChar;
	            	if (System.getProperty("os.name").toLowerCase().contains("mac")) {
	            		tempFolder = System.getProperty("user.home")+"/Library/Preferences/kkMulticopterFlashTool/";
	        		} 
	            	
	            	String newDir;
	            	if (entry.getName().indexOf("/")==-1){
	            		newDir = zipFile.getName().substring(0,zipFile.getName().indexOf("."));
	            	} else {
	            		newDir = entry.getName().substring(0, entry.getName().indexOf("/"));
	            	}
	            	String folder = tempFolder + newDir;
	            	System.out.println("Create folder: " + folder);
	            	if ((new File(folder).mkdir())){
	            		System.out.println("Done.");;
	            	}
	            	
	            	System.out.println("Extracting: " +entry);
	            	is = new BufferedInputStream(zipfile.getInputStream(entry));
	            	int count;
	            	byte data[] = new byte[2048];
	            	fileName = tempFolder+entry.getName();
	            	FileOutputStream fos = new FileOutputStream(tempFolder+entry.getName());
	            	dest = new BufferedOutputStream(fos, 2048);
	            	while ((count = is.read(data, 0, 2048)) 
	            			!= -1) {
	            		dest.write(data, 0, count);
	            	}
	            	dest.flush();
	            	dest.close();
	            	is.close();
	            	break;
	            }
	         }
	      } catch (ZipException e) {
	    	  zipFile.delete();
	    	  kk.err(Translatrix._("error.zipfileDamaged"));
	    	  kk.err(Translatrix._("reportProblem"));
	      }	catch(Exception e) {
	         e.printStackTrace();
	      }
		
		
		
		
		return new File(fileName);
	}

}
