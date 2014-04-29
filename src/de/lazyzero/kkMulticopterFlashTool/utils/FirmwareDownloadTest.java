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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

public class FirmwareDownloadTest {
	private static String firmwareRepositoryURL;
	private static XmlReaderFirmwares firmwareReader;
	private static Vector<Firmware> firmwares;
	private static XmlWriterFirmwares firmwareWriter;

	public static void main (String[] args) {
	
		firmwareRepositoryURL = System.getProperty("user.dir")+"/firmwares.xml.simonk20140306";//"/tgy_daily.xml";//.simonk10062012";
		
		firmwareReader = new XmlReaderFirmwares();
		try {
			firmwareReader.readXmlFile("file://"+firmwareRepositoryURL);
			firmwares = firmwareReader.getFirmwares();
			
			for (Iterator<Firmware> iterator = firmwares.iterator(); iterator.hasNext();) {
				Firmware firmware = iterator.next();
				
				String md5Calculated = "N/A";
				if (firmware.getZipURL() != null) {
					File f = downloadFile(firmware.getZipURL());
					if (null != f) {
						File fExtract = new File(firmware.getFileURL().toString().substring(7).trim());
						try {
							File file = Zip.unzip(f, fExtract);
							md5Calculated  = MD5.getMD5(file);
						} catch (Exception e) {
							System.err.println("File corrupt.");
						}
					}
				} else if (firmware.getFileURL() != null) {
					File file = downloadFile(firmware.getFileURL());
					if (null != file) {
						md5Calculated = MD5.getMD5(file);
					}
				}
				if (!firmware.getMD5().equals(md5Calculated)) {
					System.err.println("MD5 mismatch: " + firmware.getMD5());
					firmware.setMD5(md5Calculated);
				}
				System.out.println("----------------------------------------------------------------------------------------");
			}
			
			firmwareWriter = new XmlWriterFirmwares(firmwareRepositoryURL, firmwares);
			firmwareWriter.writeXmlFile();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	private static File downloadFile(URL url){
		String filename = null;
		String tmpdir = "/tmp/kktest/";
		
		String urlPath = url.getPath();
		filename = urlPath.substring(urlPath.lastIndexOf("/")+1);
		System.out.println("Download file: "+filename);
		
        filename = tmpdir + filename;
        	
		File tmpFile = new File(filename);
		if ((new File(tmpdir)).mkdir()) System.out.println("tmpdir created");
		
		
		try {
			
			BufferedInputStream in = new BufferedInputStream(url.openStream());
			FileOutputStream fos = new FileOutputStream(tmpFile);
			BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
			byte[] data = new byte[1024];
			int x=0;
			while((x=in.read(data,0,1024))>=0) {
				bout.write(data,0,x);
			}
			bout.close();
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		System.out.println("Download finished: " + filename);
		return tmpFile;
	}
}
