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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Resources {
	/**
		 * @param resourceAsStream
		 * @param outFile
		 * @throws Exception
		 */
		public static void writeStreamToFile(InputStream resourceAsStream, File 
	outFile) throws Exception {
			/* ================================================== */
		    FileOutputStream fos = new FileOutputStream(outFile);
		    try {
		        byte[] buf = new byte[1024];
		        int i = 0;
		        while ((i = resourceAsStream.read(buf)) != -1) {
		            fos.write(buf, 0, i);
		        }
		    } 
		    catch (Exception e) {
		        throw e;
		    }
		    finally {
		        fos.close();
		    }
			/* ================================================== */
		}
	
}

