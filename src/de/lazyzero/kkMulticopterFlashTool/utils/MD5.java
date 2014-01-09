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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	
	public static void main (String[] arg) {
//		getMD5(new File(arg[0]));
//		getMD5(new File("/Users/moll/Downloads/QuadControllerV4_5/ich_Quad_03.hex"));
		
		File path = new File("/Users/moll/Library/Preferences/kkMulticopterFlashTool/_conf/");
		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getAbsolutePath().endsWith(".eep")) {
				System.out.print(files[i]+ ": ");
				getMD5((files[i]));
			}
		}
		
	}
	
	public static String getMD5(File file) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
		InputStream fis = new FileInputStream(file);
		DataInputStream dis = new DataInputStream(fis);
		
		while (dis.available()!=0) {
				digest.update(dis.readByte());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] md5sum = digest.digest();
		String md5 = "";
		for (byte b : md5sum) {
			md5 = md5.concat(String.format("%02x", b));
		}
		
		System.out.println("MD5: " + md5);
		return md5;
	}
}
