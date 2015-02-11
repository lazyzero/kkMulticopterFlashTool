/*
    AVR8 Burn-O-Mat
 
    Copyright (C) 2007  Torsten Brischalle

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
 */

package de.lazyzero.kkMulticopterFlashTool.utils;

import static lu.tudor.santec.i18n.Translatrix._;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import avr8_burn_o_mat.XmlUtil;
import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;


public class XmlReaderFirmwares {
    
    private Vector <Firmware> firmwares = new Vector <Firmware>();
    private double actualVersion;
	private LinkedHashMap<String,String> firmwareRepositories = new LinkedHashMap<String,String>();
	private Logger logger = KKMulticopterFlashTool.getLogger();
	private String changelog;
	
    
    /** Creates a new instance of FuseReader */
    public XmlReaderFirmwares() {
    }
    
    public XmlReaderFirmwares(LinkedHashMap<String,String> firmwareRepositories) {
    	firmwares.removeAllElements();
    	if (KKMulticopterFlashTool.isOfflineMode()){
    		
    		String url = "file://"+KKMulticopterFlashTool.getTempFolder()+"firmwares.xml";
    		this.firmwareRepositories.put("",url); 
    		try {
				readXmlFile(url);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, _("error.offlineRepository"));
				e.printStackTrace();
				logger.log(Level.WARNING, e.getMessage());
			}
    	} else { //from the online repository
    		this.firmwareRepositories = firmwareRepositories;
    		try {
    			Iterator<String> keys = firmwareRepositories.keySet().iterator();
    			while (keys.hasNext()) {
    				String key = keys.next();
    				boolean load = false;
    				if (key.equals("tgydaily")) {
    					if (KKMulticopterFlashTool.isShowDailyTGYEnabled()) {
    						load = true;
    					} else {
    						load = false;
    					}
    				} else {    
    					load = true;
    				}
    				if (load) {
    					String url = firmwareRepositories.get(key);
    					System.out.println("repository URL: " + url);
    					
    					downloadFirmwareDescription(new URL(url));
    					readXmlFile(getLocalXMLFile(url.substring(url.lastIndexOf('/')+1)));
    				}
				}
    		} catch (Exception e) {
//    			try {
//    				System.out.println("repository mirror URL: " + firmwareRepositoryMirrorURL.toString());
//    				url = this.firmwareRepositoryMirrorURL;
//    				downloadFirmwareDescription(new URL(url));
//    				readXmlFile(url);
//    			} catch (Exception e2) {
//    				JOptionPane.showMessageDialog(null, _("error.repository"));
//    			}
    			e.printStackTrace();
    		}
    	}
	}
    
    
	public Vector<Firmware> getFirmwares() {
		return firmwares;
	}


	
	public Vector<Firmware> getFirmwares(String controllerName) {
		System.out.println("Adding firmwares for controller: " + controllerName);
		if(controllerName.endsWith("p")){
			controllerName=controllerName.replace("p", "");
			System.out.println(controllerName);
		} else if (controllerName.endsWith("pa")){
			controllerName=controllerName.replace("pa", "");
			System.out.println(controllerName);
		}
		Vector <Firmware> fw = new Vector<Firmware>();
		Enumeration<Firmware> en = firmwares.elements();
		while (en.hasMoreElements()) {
			Firmware firmware = (Firmware) en.nextElement();
			if (firmware.getController().contains(controllerName)){
				if (KKMulticopterFlashTool.isHideDecprecatedEnabled()) {
					if (!firmware.isDeprecated()) {
						fw.add(firmware);
						System.out.println("add firmware to drop down list " + firmware);
					}
				} else {
					fw.add(firmware);
					System.out.println("add firmware to drop down list " + firmware);
				}
			} 
		}
		
		Collections.sort(fw, new Comparator<Firmware>() {

			@Override
			public int compare(Firmware o1, Firmware o2) {
				// TODO Auto-generated method stub
				return o2.toStringSorting().compareTo(o1.toStringSorting());
			}
		});

		
		return fw;
	}
    
    public void readXmlFile(String uri) throws Exception {
    	//test the connection first
    	if(!KKMulticopterFlashTool.isOfflineMode()) {
	    	try {
	    		URL url = new URL(uri);
	    		if (uri.startsWith("http")) {
	    			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    			conn.setReadTimeout(5000);
	    		} 
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.WARNING, e.getMessage());
				throw e;
			}
    	}
		
		System.out.println("start to parse: " + uri);
//		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
//			uri = uri.substring(7, uri.length());
//		}
		
        DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
        DocumentBuilder        builder  = factory.newDocumentBuilder();
        Document               document = builder.parse(uri);
        
        Node node = document.getDocumentElement().getFirstChild();
        
        
        while (node != null) {
            if (node.getNodeName().equals("firmware")) {
                readFirmware(node);
            }
            if (node.getNodeName().equals("version")) {
                readVersion(node);
                readChangelog(node);
            }
            
            node = node.getNextSibling();
        }
        
    }
    
    private void readChangelog(Node node) {
    	Node nodeChangelog = node.getFirstChild();
    	changelog = "<ul>";
    	while (nodeChangelog != null) {
    		if (nodeChangelog.getNodeName().equals("change")) {
        		String line = XmlUtil.getAttr(nodeChangelog, "value");
        		changelog = changelog.concat("<li>" + line + "</li>");
            }
    		nodeChangelog = nodeChangelog.getNextSibling();
    	}
    	changelog = changelog.concat("<li>and more...</li>");
		changelog = changelog.concat("</ul>");
	}

	private void readVersion(Node node) {
		actualVersion = Double.parseDouble(XmlUtil.getAttr(node, "name"));
	}

	private void readFirmware(Node node) throws Exception {
        
        Node nodeFirmware = node.getFirstChild();
        String author = new String();
        String versionname = new String();
        String controller = new String();
        String md5 = new String();
        String eepromMd5 = new String();
        String additionalOptions = new String();
        String server = new String();
        URL url = null;
        URL zipurl = null;
        URL eepromUrl = null;
        URL eepromZipurl = null;
        String svnUrl = null;
        String svnEEpromUrl = null;
        String svnName = null;
        String svnEEpromName = null;
        URL commenturl = null;
        int features = -1;
        int target = 0;
        String name = XmlUtil.getAttr(node, "name");
        String version = XmlUtil.getAttr(node, "value");
        
        boolean isDeprecated = false;
		while (nodeFirmware != null) {
            
        	if (nodeFirmware.getNodeName().equals("author")) {
        		author = XmlUtil.getAttr(nodeFirmware, "name");
            }
        	if (nodeFirmware.getNodeName().equals("versionname")) {
        		versionname = XmlUtil.getAttr(nodeFirmware, "name");
            }
        	if (nodeFirmware.getNodeName().equals("controller")) {
        		controller = XmlUtil.getAttr(nodeFirmware, "name");
            }
			try {
	        	if (nodeFirmware.getNodeName().equals("controller")) {
	        		target = Integer.parseInt(XmlUtil.getAttr(nodeFirmware, "target"));
	            }
        	} catch (Exception e) {
        		//if target is missing or NumberformatException the target is set to kk.
				target = 0;
			}
        	if (nodeFirmware.getNodeName().equals("file")) {
        		url = new URL(XmlUtil.getAttr(nodeFirmware, "url"));
            }
        	try {
        		if (nodeFirmware.getNodeName().equals("zipfile")) {
        			zipurl = new URL(XmlUtil.getAttr(nodeFirmware, "url"));
        		}
			} catch (Exception e) {
				zipurl = null;
			}
			if (nodeFirmware.getNodeName().equals("eepromfile")) {
        		eepromUrl = new URL(XmlUtil.getAttr(nodeFirmware, "url"));
            }
        	try {
        		if (nodeFirmware.getNodeName().equals("eepromzipfile")) {
        			eepromZipurl = new URL(XmlUtil.getAttr(nodeFirmware, "url"));
        		}
			} catch (Exception e) {
				eepromZipurl = null;
			}
        	try {
        		if (nodeFirmware.getNodeName().equals("svn")) {
        			svnUrl = new String(XmlUtil.getAttr(nodeFirmware, "url"));
        			svnName = new String(XmlUtil.getAttr(nodeFirmware, "name"));
        		}
			} catch (Exception e) {
				svnUrl = null;
				svnName = null;
			}
			try {
        		if (nodeFirmware.getNodeName().equals("svn")) {
        			svnEEpromUrl = new String(XmlUtil.getAttr(nodeFirmware, "eepromurl"));
        			svnEEpromName = new String(XmlUtil.getAttr(nodeFirmware, "eepromname"));
        		}
			} catch (Exception e) {
				svnEEpromUrl = null;
				svnEEpromName = null;
			}
			try {
        		if (nodeFirmware.getNodeName().equals("commenturl")) {
        			commenturl = new URL(XmlUtil.getAttr(nodeFirmware, "url"));
        		}
			} catch (Exception e) {
				commenturl = null;
			}
			try {
        		if (nodeFirmware.getNodeName().equals("additionalOptions")) {
        			additionalOptions = XmlUtil.getAttr(nodeFirmware, "option");
        		}
			} catch (Exception e) {
				additionalOptions = new String();
			}
        	if (nodeFirmware.getNodeName().equals("md5")) {
        		md5 = new String(XmlUtil.getAttr(nodeFirmware, "value"));
            }
        	if (nodeFirmware.getNodeName().equals("eeprommd5")) {
        		eepromMd5 = new String(XmlUtil.getAttr(nodeFirmware, "value"));
            }
        	if (nodeFirmware.getNodeName().equals("features")) {
        		features = Integer.parseInt(XmlUtil.getAttr(nodeFirmware, "value"));
        	}
        	if (nodeFirmware.getNodeName().equals("server")) {
        		server = new String(XmlUtil.getAttr(nodeFirmware, "name"));
            }
        	if (nodeFirmware.getNodeName().equals("deprecated")) {
        		isDeprecated = Boolean.parseBoolean(XmlUtil.getAttr(nodeFirmware, "value"));
            }
            
            nodeFirmware = nodeFirmware.getNextSibling();
        }
        
        Firmware firmware = new Firmware(url);
        firmware.setName(name);
        firmware.setFileName(url.toString());
        firmware.setVersionName(versionname);
        firmware.setVersion(version);
        firmware.setController(controller);
        firmware.setAuthor(author);
        firmware.setZipURL(zipurl);
        firmware.setEEpromURL(eepromUrl);
        firmware.setEEpromZipURL(eepromZipurl);
        firmware.setSVN(svnUrl);
        firmware.setSVNname(svnName);
        firmware.setEEpromSVN(svnEEpromUrl);
        firmware.setEEpromSVNname(svnEEpromName);
        firmware.setMD5(md5);
        firmware.setEEpromMD5(eepromMd5);
        firmware.setCommentURL(commenturl);
        firmware.setAdditionalOptions(additionalOptions);
        firmware.setFeatures(features);
        firmware.setTargetPlatform(target);
        firmware.setServer(server);
        firmware.setDeprecated(isDeprecated);
        
       //firmware.getFile();
        
        firmwares.add(firmware);
    }
	
	private File downloadFirmwareDescription(URL url){
		String filename = null;
		String tmpdir = null;
		
		String urlPath = url.getPath();
		filename = urlPath.substring(urlPath.lastIndexOf("/")+1);
		System.out.println("Download firmware description file: "+filename);
		System.out.println("from: " + url);
		
        tmpdir = KKMulticopterFlashTool.getTempFolder();
        filename = tmpdir + filename;
        	
		File tmpFile = new File(filename);
		if ((new File(tmpdir)).mkdir()) System.out.println("tmpdir created");
		
		
    		try {
    			long time = System.currentTimeMillis();
//    			ZipInputStream in  = new ZipInputStream(url.openStream());
    			BufferedInputStream in = new BufferedInputStream(url.openStream());
    			FileOutputStream fos = new FileOutputStream(tmpFile);
    			BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
    			byte[] data = new byte[1024];
    			int x=0;
//    			in.getNextEntry();
    			while((x=in.read(data,0,1024))>=0) {
    				bout.write(data,0,x);
    			}
    			bout.close();
    			in.close();
    			System.out.println("Download finished in milli seconds " + (System.currentTimeMillis()-time));
    		} catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		//unzip
		try {
			long time = System.currentTimeMillis();
			int BUFFER = 2048;
			BufferedOutputStream dest = null;
			BufferedInputStream is = null;
			ZipEntry entry;
			ZipFile zipfile = new ZipFile(filename);
			@SuppressWarnings("rawtypes")
			Enumeration e = zipfile.entries();
			System.out.println("zip: " + e.hasMoreElements());
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();
				System.out.println("Extracting: " + entry);
				is = new BufferedInputStream(zipfile.getInputStream(entry));
				int count;
				byte data[] = new byte[BUFFER];
				FileOutputStream fos = new FileOutputStream(filename.replace(".zip", ""));
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				is.close();
			}
			System.out.println("Download extracted in milli seconds " + (System.currentTimeMillis()-time));
		} catch (Exception e) {
			e.printStackTrace();
		}
//    	   }
//			try {
//				long time = System.currentTimeMillis();
//				int BUFFER = 2048;
//				ZipInputStream zin = new ZipInputStream(url.openStream());
//				ZipEntry entry;
//				while ((entry = zin.getNextEntry()) != null) {
//					int count;
//					byte data[] = new byte[BUFFER];
//					FileOutputStream fos = new FileOutputStream(entry.getName());
//					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
//					while ((count = zin.read(data, 0, BUFFER)) != -1) {
//						// System.out.write(x);
//						dest.write(data, 0, count);
//					}
//					dest.flush();
//					dest.close();
//	
//				}
//				zin.close();
//				System.out.println("Download extracted in milli seconds " + (System.currentTimeMillis()-time));
//			} catch (FileNotFoundException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    		} catch (IOException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    		}

		System.out.println("Download finished: " + filename);
		return tmpFile;
	}
	
	public static void main(String[] args) {
		XmlReaderFirmwares f = new XmlReaderFirmwares();
		try {
			f.readXmlFile("./firmwares.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public LinkedHashMap<String, String> getURL() {
		return this.firmwareRepositories;
	}

	/**
	 * @return the actualVersion
	 */
	public double getActualVersion() {
		return actualVersion;
	}
	
	public String getChangelog() {
		return changelog;
	}

	public void reloadXmlFile(LinkedHashMap<String, String> urls) throws Exception {
		this.firmwareRepositories = urls;
		this.firmwares.removeAllElements();
	
		Iterator<String> keys = firmwareRepositories.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String url = firmwareRepositories.get(key);
			boolean load = false;
			if (key.equals("tgydaily")) {
				if (KKMulticopterFlashTool.isShowDailyTGYEnabled()) {
					load = true;
				} else {
					load = false;
				}
			} else {    
				load = true;
			}
			if (load) {
				downloadFirmwareDescription(new URL(url));
				String uri = getLocalXMLFile(url.substring(url.lastIndexOf('/') + 1));
				readXmlFile(uri);
			}
		}
	}

	private String getLocalXMLFile(String file) {
		String url = file.substring(0, file.length()-4);
		
		String uri = KKMulticopterFlashTool.getTempFolder()+url;
		uri = "file:////".concat(uri);
		return uri;
	}

	

	
	
    
}
