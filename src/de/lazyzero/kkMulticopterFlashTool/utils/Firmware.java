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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

public class Firmware {
	
	public static final int POT = 1;
	public static final int LCD = 2;
	public static final int PIEZO = 4;
	public static final int MEMS = 8;
	public static final int ACC = 16;
	public static final int CPPM = 32;
	public static final int LVA = 64;
	
	public static final int TARGET_KK = 0;
	public static final int TARGET_ESC = 1;
	public static final int TARGET_OPENAERO = 3;
	public static final int TARGET_CLOSD = 4;
	public static final int TARGET_RX3S = 5;

	private File file;
	private File eepromFile;
	private String version;
	private String author;
	private String name;
	private String controller;
	private String additionalOptions;
	private URL url;
	private String md5Calculated;
	private String md5;
	private String server;
	private URL zipURL;
	private URL commenturl;
	private String svnUrl;
	private String svnName;
	private int features;
	private int target = 0;
	private String versionName;
	
	private Logger logger = KKMulticopterFlashTool.getLogger();
	private String fileName;
	private URL eepromUrl;
	private URL eepromZipUrl;
	private String svnEEpromUrl;
	private String svnEEpromName;
	private String eepromMD5;
	private String eepromMD5Calculated;
	private boolean isDeprecated = false;
	
	

	public Firmware(){}

	public Firmware(File firmwareFile) {
		this.file = firmwareFile;
		
		try {
			System.out.println(this.getFile().getAbsolutePath());
		} catch (FileCorruptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Firmware(URL firmwareURL) throws NullPointerException{
		if (firmwareURL!=null){
			this.url = firmwareURL;
		}

	}
	
	public URL getFileURL() {
		return url;
	}
	
	public URL getZipURL() {
		return zipURL;
	}

	public File getFile() throws FileCorruptException{
		if (KKMulticopterFlashTool.isOfflineMode()) {
			if (zipURL != null) {
				String filename = null;
				String tmpdir = null;
				
				String urlPath = url.getPath();
				filename = urlPath.substring(urlPath.lastIndexOf("/")+1);
				System.out.println("Download file: "+filename);
				
		        tmpdir = KKMulticopterFlashTool.getTempFolder();
		        	
				File tmpFile = new File(tmpdir + filename);
				File fExtract = new File(url.toString().substring(7).trim());
				this.file = Zip.unzipFile(tmpFile, fExtract);
				this.md5Calculated = MD5.getMD5(file);
			} else if (url != null) {
				String filename = null;
				String tmpdir = null;
				
				String urlPath = url.getPath();
				filename = urlPath.substring(urlPath.lastIndexOf("/")+1);
				System.out.println("Load file from temp folder: "+filename);
				
		        tmpdir = KKMulticopterFlashTool.getTempFolder();
		        	
				this.file = new File(tmpdir + filename);
				this.md5Calculated = MD5.getMD5(file);
				System.out.println(md5Calculated);
			} else if (eepromZipUrl != null) {
				String filename = null;
				String tmpdir = null;
				
				//extract file name of eeprom file to know which file has to be extracted
				String urlPath = eepromUrl.getPath();
				filename = urlPath.substring(urlPath.lastIndexOf("/")+1);
				System.out.println("Download file: "+filename);
				
		        tmpdir = KKMulticopterFlashTool.getTempFolder();
		        	
				File tmpFile = new File(tmpdir + filename);
				File fExtract = new File(url.toString().substring(7).trim());
				this.eepromFile = Zip.unzipFile(tmpFile, fExtract);
				this.eepromMD5Calculated = MD5.getMD5(eepromFile);
			} else if (eepromUrl != null) {
				String filename = null;
				String tmpdir = null;
				
				String urlPath = eepromUrl.getPath();
				filename = urlPath.substring(urlPath.lastIndexOf("/")+1);
				System.out.println("Load file from temp folder: "+filename);
				
		        tmpdir = KKMulticopterFlashTool.getTempFolder();
		        	
				this.eepromFile = new File(tmpdir + filename);
				this.eepromMD5Calculated = MD5.getMD5(eepromFile);
				System.out.println(eepromMD5Calculated);
			}
		} else { // this condition is for downloading the files
			if (zipURL != null) {
				File f = this.downloadFile(zipURL);
				File fExtract = new File(url.toString().substring(7).trim());
				this.file = Zip.unzipFile(f, fExtract);
				this.md5Calculated = MD5.getMD5(file);
			} else if (svnUrl != null) {
				System.out.println("debug svn: " + svnUrl.toString());
				try {
					SVN svn = new SVN(svnUrl.toString(), svnName);
					this.file = svn.getFile();
					this.md5 = svn.getFileMD5();
					this.md5Calculated = MD5.getMD5(file);
					logger.log(Level.INFO, "MD5 of SVN: " + md5 + " MD5 calculated from file: " + md5Calculated);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Problem during SVN action");
					e.printStackTrace();
					throw new FileCorruptException();
				}
			} else if (url != null) {
				this.file=downloadFirmware(url);
				this.md5Calculated = MD5.getMD5(file);
				System.out.println(md5Calculated);
			} else if (eepromZipUrl != null) {
				File f = this.downloadFile(eepromZipUrl);
				File fExtract = new File(eepromUrl.toString().substring(7).trim());
				this.eepromFile = Zip.unzipFile(f, fExtract);
				this.eepromMD5Calculated = MD5.getMD5(eepromFile);
			} else if (svnEEpromUrl != null) { 
				System.out.println("debug svn: " + svnEEpromUrl.toString());
				try {
					SVN svn = new SVN(svnEEpromUrl.toString(), svnEEpromName);
					this.eepromFile = svn.getFile();
					this.eepromMD5 = svn.getFileMD5();
					this.eepromMD5Calculated = MD5.getMD5(eepromFile);
					logger.log(Level.INFO, "MD5 of SVN: " + eepromMD5 + " MD5 calculated from EEPROM file: " + eepromMD5Calculated);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Problem during SVN action");
					e.printStackTrace();
					throw new FileCorruptException();
				}
			} else if (eepromUrl != null) {
				this.eepromFile=downloadFirmware(eepromUrl);
				this.eepromMD5Calculated = MD5.getMD5(eepromFile);
				System.out.println(eepromMD5Calculated);
			}
		}
		
		//check the md5
		if(null != md5 && !md5.equals("")) {
			logger.log(Level.INFO, "check MD5");
			logger.log(Level.INFO, "MD5: " + md5 + " MD5 calculated from file: " + md5Calculated);
			if (!md5.equals(md5Calculated)){
				logger.log(Level.SEVERE, "The file is corrupted.");
				throw new FileCorruptException();
			} else {
				logger.log(Level.INFO, "File is okay.");
			}
		} else {
			logger.log(Level.INFO, "check MD5: no MD5 available. Check aborted.");
		}
		if (null != eepromMD5 && !eepromMD5.equals("")) {
			logger.log(Level.INFO, "check EEPROM MD5");
			if (!eepromMD5.equals(eepromMD5Calculated)){
				logger.log(Level.SEVERE, "The EEPROM file is corrupted.");
				throw new FileCorruptException();
			} else {
				logger.log(Level.INFO, "EEPROM File is okay.");
			}
		} else {
			logger.log(Level.INFO, "check EEPROM MD5: no MD5 available. Check aborted.");
		}
		
		return file;
	}

	private File downloadFile(URL url){
		String filename = null;
		String tmpdir = null;
		boolean reload = false;
		
		String urlPath = url.getPath();
		filename = urlPath.substring(urlPath.lastIndexOf("/")+1).trim();
		System.out.println("Download file: "+filename);
		
        tmpdir = KKMulticopterFlashTool.getTempFolder();
        	
        filename = tmpdir + filename;
        File tmpFile = new File(filename);
		if (filename.endsWith("tgy-daily.zip")) {
			reload = true;
			System.out.println("reload file: " + tmpFile.getName());
		} 
		
		if ((new File(tmpdir)).mkdir()) System.out.println("tmpdir created");
		
		if (!tmpFile.exists() || reload) {
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
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
		} else {
			System.out.println("skip download. File already loaded.");
		}

		System.out.println("Download finished: " + filename);
		return tmpFile;
	}
	
	private File downloadFirmware(URL firmwareURL){
		String filename, tmpdir;
		
		String urlPath = firmwareURL.getPath();
		filename = urlPath.substring(urlPath.lastIndexOf("/")+1);
		System.out.println("Download file: "+filename);
		
        tmpdir = KKMulticopterFlashTool.getTempFolder();
        filename = tmpdir+filename;
		
		File tmpFile = new File(filename);
		if ((new File(tmpdir)).mkdir()) System.out.println("tmpdir created");
		
		if (!tmpFile.exists()){
			try {
				BufferedInputStream in = new BufferedInputStream(firmwareURL.openStream());
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("skip download. File already loaded.");
		}

		System.out.println("Download finished: " + filename);
		return tmpFile;
	}
	
	public String toString() {
		return name + " ( V" + version + " [" + controller + "]" + " " + author + " )";
	}
	
	public String toString2() {
		return name+version+author;
	}
	
	public String toHTMLString() {
		return "<html>" + name + " (V" + version + " [" + controller + "])" + "<br>" + author ;
//		return "<html>" + name + " V" + version + " by " + author + (server.equals("")?"":("<br>" + server)) ;
	}
	
	public String getController() {
		return controller;
	}
	
	public URL getCommentURL() {
		return commenturl;
	}
	
	public boolean hasCommentURL() {
		System.out.println(commenturl);
		return (null != commenturl);
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(String controller) {
		this.controller = controller;
	}
	
	public void setMD5(String md5) {
		this.md5 = md5; 
	}
	
	public String getMD5() {
		return this.md5;
	}

	public void setZipURL(URL zipurl) {
		this.zipURL = zipurl;
	}
	
	public void setCommentURL(URL commenturl) {
		this.commenturl = commenturl;
	}
	
	public static void main(String[] args) {
		Firmware f = null;
		
		try {
			f = new Firmware();
			File ff = f.downloadFirmware(new URL("http://www.kkmulticopter.kr/multicopter/firmware/XXcontrol_KR_DualCopter_v1_5.hex"));
			MD5.getMD5(ff);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		try {
//			f = new Firmware(new URL("http://www.lazyzero.de/_media/firmware.hex"));
//			f.setAuthor("ich");
//			f.setController("m48");
//			f.setName("Quad");
//			f.setVersion("0.3");
//		} catch (NullPointerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		f.getFile();
		//f.downloadFirmware(f.url);
	}

	public static Vector<Firmware> getOfflineAvailableFirmwares(Vector<Firmware> inputFirmwares) {
		Vector<Firmware> firmwares = new Vector<Firmware>();
		String tmpFolder = KKMulticopterFlashTool.getTempFolder();
		
		for (Firmware firmware : inputFirmwares) {
			
			if (firmware.zipURL != null) {
				String urlPath = firmware.zipURL.getPath();
				String filename = urlPath.substring(urlPath.lastIndexOf("/")+1);
				if (new File(tmpFolder+filename).exists()) {
					firmwares.add(firmware);
				}
			} else if (firmware.url != null) {
				String urlPath = firmware.url.getPath();
				String filename = urlPath.substring(urlPath.lastIndexOf("/")+1);
				if (new File(tmpFolder+filename).exists()) {
					firmwares.add(firmware);
				}
			}
		}
		
		
		return firmwares;
	}

	public static Vector<String> getCategories(Vector<Firmware> firmwares) {
		Vector<String>firmwareTyps = new Vector<String>();
		Enumeration<Firmware> en = firmwares.elements();
		while (en.hasMoreElements()) {
			Firmware firmware = (Firmware) en.nextElement();
			if (!firmwareTyps.contains(firmware.getName())) {
				firmwareTyps.add(firmware.getName());
			}
		}
		return firmwareTyps;
	}

	public static HashMap<String,Boolean> getCategoriesBooleanHashMap(Vector<Firmware> firmwares) {
		HashMap<String, Boolean>firmwareTyps = new HashMap<String,Boolean>();
		Enumeration<Firmware> en = firmwares.elements();
		while (en.hasMoreElements()) {
			Firmware firmware = (Firmware) en.nextElement();
			if (!firmwareTyps.containsKey(firmware.getName())) {
				firmwareTyps.put(firmware.getName(), false);
			}
		}
		return firmwareTyps;
	}

	public static Vector<Firmware> filter(String category, Vector<Firmware> firmwares) {
		Vector<Firmware> f = new Vector<Firmware>();
		for (Iterator<Firmware> iterator = firmwares.iterator(); iterator.hasNext();) {
			Firmware firmware = iterator.next();
			if (category.toString().equals(firmware.getName())) {
				f.add(firmware);
			}
		}
		
		return f;
	}

	public void setAdditionalOptions(String additionalOptions) {
		this.additionalOptions = additionalOptions;
	}

	
	public String getAdditionalOptions() {
		return this.additionalOptions;
	}
	
	public void setFeatures(int features) {
		this.features = features;
	}

	public int getFeatures() {
		return this.features;
	}

	public String getServer() {
		return server;
	}
	
	public void setServer(String server) {
		this.server = server;
	}

	public void setTargetPlatform(int target) {
		this.target = target;
	}
	
	public int getTarget() {
		return target;
	}

	public String getAuthor() {
		return author;
	}

	public String getVersion() {
		return version;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	
	public String getVersionName() {
		return this.versionName;
	}
	
	public String getSVN() {
		return this.svnUrl;
	}

	public void setSVN(String svnUrl) {
		this.svnUrl = svnUrl;
	}
	
	public String getSVNname() {
		return this.svnName;
	}

	public void setSVNname(String svnName) {
		this.svnName = svnName;
	}

	public String getFileName() {
		return fileName ;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName.substring(fileName.lastIndexOf('/')+1);
	}

	public void setEEpromURL(URL eepromUrl) {
		this.eepromUrl = eepromUrl;
	}

	public void setEEpromZipURL(URL eepromZipUrl) {
		this.eepromZipUrl = eepromZipUrl;
	}

	public void setEEpromSVN(String svnEEpromUrl) {
		this.svnEEpromUrl = svnEEpromUrl;
	}

	public void setEEpromMD5(String md5) {
		this.eepromMD5 = md5;
	}

	public void setEEpromSVNname(String svnEEpromName) {
		this.svnEEpromName = svnEEpromName;
	}

	public void setDeprecated(boolean isDeprecated) {
		this.isDeprecated = isDeprecated;
	}

	public boolean isDeprecated() {
		return isDeprecated;
	}
	
}
