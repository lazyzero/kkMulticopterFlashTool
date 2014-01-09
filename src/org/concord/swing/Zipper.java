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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JOptionPane;

public class Zipper {
	public static final int ZIP_MODE_SINGLE_FILE = 1;
	public static final int ZIP_MODE_MULTIPLE_FILES = 2;
	public static final int ZIP_MODE_FOLDER = 3;
	
	Vector inputs;
	File output;
	
	public Zipper() {}
	
	public static File zip(Vector files) {
    	String msg = "Please specify the zipped file";
    	JOptionPane.showMessageDialog(null, msg, "", 
    			JOptionPane.INFORMATION_MESSAGE);
		File output = null;
		MostRecentFileDialog mrfd = new MostRecentFileDialog();
		mrfd.setFilenameFilter("zip");
		int retval = mrfd.showSaveDialog(null, "Save as");
		if(retval == MostRecentFileDialog.APPROVE_OPTION) {
			output = mrfd.getSelectedFile();
			zip(files, output);
		}
		return output;
	}
	
	public static void zip(Vector files, File output) {
		if(files == null || files.size() == 0) return;
		
	    int size = files.size();
	    String[] fns = new String[size];
		for(int i = 0; i < size; i++) {
			Object ele = files.elementAt(i);
			if(ele instanceof String) {
				fns[i] = ele.toString();
			} else if(ele instanceof File) {
				try {
					fns[i] = ((File)ele).getCanonicalPath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				System.err.println("Zip entry must be " +
						"either File or File name (String)");
			}
		}
		zip(fns, output);
	}
	
	public static void zip(String[] files, String output) {
		zip(files, new File(output));
	}
	
	public static void zip(String input, String output) {
		zip(new File(input), new File(output));
	}
	
	public static void zip(String input, File output) {
		zip(new File(input), output);
	}
	
	public static void zip(File input, String output) {
		Vector v = new Vector(1);
		v.addElement(input);
		zip(v, new File(output));
	}
	
	public static void zip(File input, File output) {
		Vector v = new Vector(1);
		v.addElement(input);
		zip(v, output);
	}
	
	public static void zip(File[] files, File output) {
		if(files == null || files.length == 0) return;

		FileOutputStream fos;
		
		try {
			fos = new FileOutputStream(output);
			ZipOutputStream zout = new ZipOutputStream(fos);
		    byte b[] = new byte[512];
			
			for(int i = 0; i < files.length; i++) {
				
				if(files[i] != null && files[i].exists()) {
					String fName = files[i].getCanonicalPath().replace(File.separatorChar, '/');
					FileInputStream fis = new FileInputStream(files[i]);
					ZipEntry ze = new ZipEntry(fName);
					zout.putNextEntry(ze);
					int len=0;
					while((len=fis.read(b)) != -1) {
						zout.write(b,0,len);
					}
				    zout.closeEntry();
				    print(ze);
				    fis.close();
				}
			}
			zout.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void zip(File[] files, String output) {
		zip(files, new File(output));
	}
	
	public static void zip(String[] files, File output) {
		if(files == null || files.length == 0) return;
		
		int length = files.length;
		File[] fs = new File[length];
		
		for(int i = 0; i < length; i++) {
			fs[i] = new File(files[i]);
		}
		
		zip(fs, output);
	}
	
	public static void zip(Vector files, String output) {
		zip(files, new File(output));
	}
	
	  public static void print(ZipEntry e){
		  System.err.print("added " + e.getName());
		  if (e.getMethod() == ZipEntry.DEFLATED) {
		      long size = e.getSize();
		      if (size > 0) {
		    	  long csize = e.getCompressedSize();
		    	  long ratio = ((size-csize)*100) / size;
		    	  System.err.println(" (deflated " + ratio + "%)");
		      }
		      else {
		    	  System.err.println(" (deflated 0%)");
		      }
		  }
		  else {
			  System.err.println(" (stored 0%)");
		  }
	  }

	  public static void main(String[] args) {
		File f1 = new File("src/org/concord/swing/Zipper.java");
		File f2 = new File("lib/org/concord/swing/Zipper.class");
		File fo = new File("out.zip");
		
		File[] files = {f1, f2};
		Vector vector = new Vector();
		vector.addElement(f1);
		vector.addElement(f2);
		
		String[] strs = {f1.getAbsolutePath(), f2.getAbsolutePath()};
		
		Zipper.zip(strs, "out.zip");
	}
}
