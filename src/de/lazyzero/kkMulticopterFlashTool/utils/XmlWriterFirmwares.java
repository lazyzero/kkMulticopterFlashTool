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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.codec.EncoderException;

import de.lazyzero.kkMulticopterFlashTool.utils.xml.XMLElement;

public class XmlWriterFirmwares {

	private String uri;
	private XMLElement xmlData;
	private Vector<Firmware> firmwares;

	public XmlWriterFirmwares(String uri, Vector<Firmware> firmwares) {

		this.uri = uri + "test";
		this.firmwares = firmwares;

		try {
			xmlData = createXML();
		} catch (EncoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private XMLElement createXML() throws EncoderException {

		XMLElement xml = new XMLElement();
		xml.setName("firmwares");
		
		for (Firmware firmware : firmwares) {
			
//			<firmware name="TGY" value="2012-06-10">
			XMLElement firmwareE = new XMLElement();
			firmwareE.setName("firmware");
			firmwareE.setAttribute("name", firmware.getName());
			firmwareE.setAttribute("value", firmware.getVersion());
			
//			<author name="Simon Kirby" />
			XMLElement author = new XMLElement();
			author.setName("author");
			author.setAttribute("name", firmware.getAuthor());
			
//			<server name="mirrored by LazyZero" />
			XMLElement server = new XMLElement();
			server.setName("server");
			server.setAttribute("name", firmware.getServer());
			
//			<controller name="esc" target="1"/>
			XMLElement controller = new XMLElement();
			controller.setName("controller");
			controller.setAttribute("name", firmware.getController());
			controller.setAttribute("target", firmware.getTarget());
			
			// <svn url="svn://code.google.com/p/wii-esc/source/browse/#svn%2Frelease\low_side_pwm" name="bs_nfet.hex"/>
			XMLElement svn = new XMLElement();
			if (firmware.getSVN()!=null) {
				svn.setName("svn");
				svn.setAttribute("url", firmware.getSVN());
				svn.setAttribute("name", firmware.getSVNname());
			}
			
//			<zipfile url="http://lazyzero.de/_media/modellbau/kkmulticopterflashtool/esc_firmware/tgy_2012-06-10_d8f53c2.zip" />
			XMLElement zipfile = new XMLElement();
			zipfile.setName("zipfile");
			zipfile.setAttribute("url", firmware.getZipURL());
			
//			<file url="file://tgy.hex" />
			XMLElement file = new XMLElement();
			file.setName("file");
			file.setAttribute("url", firmware.getFileURL());
			
//			<md5 value="4f94950200b77f2cb29d6055f8627f03" />
			XMLElement md5 = new XMLElement();
			md5.setName("md5");
			md5.setAttribute("value", firmware.getMD5());
			
//			<additionalOptions option="-e" />
			XMLElement additionalOptions = new XMLElement();
			additionalOptions.setName("additionalOptions");
			additionalOptions.setAttribute("option", firmware.getAdditionalOptions());
			
//			<commenturl url="http://tinyurl.com/7z9xdjq" />
			XMLElement commentURL = new XMLElement();
			commentURL.setName("commenturl");
			commentURL.setAttribute("url", firmware.getCommentURL());
			
//			<features value="5" />
			XMLElement feature = new XMLElement();
			feature.setName("feature");
			feature.setAttribute("value", firmware.getFeatures());
			
//		 <firmware name="TGY" value="2012-06-10">
//		    <author name="Simon Kirby" />
//		    <server name="mirrored by LazyZero" />
//		    <controller name="esc" target="1"/>
//		    <zipfile url="http://lazyzero.de/_media/modellbau/kkmulticopterflashtool/esc_firmware/tgy_2012-06-10_d8f53c2.zip" />
//		    <file url="file://tgy.hex" />
//		    <md5 value="4f94950200b77f2cb29d6055f8627f03" />
//		    <additionalOptions option="-e" />
//		    <commenturl url="http://tinyurl.com/7z9xdjq" />
//			<features value="5" />
//		  </firmware>
			
			
			firmwareE.addChild(author);
			firmwareE.addChild(server);
			firmwareE.addChild(controller);
			if (firmware.getSVN()!="") {
				firmwareE.addChild(svn);
			}
			if (firmware.getZipURL()!=null) firmwareE.addChild(zipfile);
			firmwareE.addChild(file);
			firmwareE.addChild(md5);
			firmwareE.addChild(additionalOptions);
			firmwareE.addChild(commentURL);
			if (firmware.getFeatures() != -1) firmwareE.addChild(feature);
			
			xml.addChild(firmwareE);
			
		}

		return xml;
	}

	public void writeXmlFile() {

		byte[] buffer = xmlData.toString().getBytes();
		FileOutputStream fout;
		try {
			File file = new File(uri);
			if (!file.getParentFile().exists())
			    file.getParentFile().mkdirs();

			if (!file.exists()) {
				file.createNewFile();
			}
			fout = new FileOutputStream(file);
			fout.write(buffer);
			fout.flush();
			fout.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
