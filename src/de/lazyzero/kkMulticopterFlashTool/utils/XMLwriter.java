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

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class XMLwriter {
	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(XMLwriter.class.getName());

	private Document      doc;
	String ENCODING = "UTF-8";
	
	private Desktop desktop;

	public XMLwriter(FirmwareCommit fc) {


		/**
		 *
		 */

		DocumentBuilderFactory docBFac;
		DocumentBuilder docBuild;
		try {
			docBFac = DocumentBuilderFactory.newInstance();
			docBuild = docBFac.newDocumentBuilder();
			doc = docBuild.newDocument();
		} catch (Exception e) {
			logger.log(Level.WARNING, "not able to creat XML");
		}
		if (doc != null) {
			
//			<firmware name="Tricopter" value="1.4g MD">
//		    <author name="Mike Barton (mirrored by LazyZero)" />
//		    <controller name="m328p" />
//		    <zipfile url="http://lazyzero.de/_media/modellbau/kkmulticopterflashtool/firmwares/xx14g_tri.zip" />
//		    <file url="file://XX14g_TRI_M328_MD.hex" />
//		    <md5 value="" />
//		  </firmware>

			Element root = doc.createElement("firmware");
			root.setAttribute("name", fc.getType());
			root.setAttribute("value", fc.getVersion());
			
			Element author = doc.createElement("author");
			author.setAttribute("name", fc.getAuthor());
			root.appendChild(author);
			
			Element controller = doc.createElement("controller");
			controller.setAttribute("name", fc.getController().getCaption());
			root.appendChild(controller);
			
			Element zipfile = doc.createElement("zipfile");
			zipfile.setAttribute("url", fc.getZipfile());
			root.appendChild(zipfile);
			
			Element file = doc.createElement("file");
			file.setAttribute("url", fc.getFilename());
			root.appendChild(file);
			
			Element md5 = doc.createElement("md5");
			md5.setAttribute("value", fc.getMd5());
			root.appendChild(md5);
			
			Element commentURL = doc.createElement("commenturl");
			commentURL.setAttribute("url", fc.getCommentURL());
			root.appendChild(commentURL);
			
			Element comment = doc.createElement("comment");
			comment.appendChild(doc.createTextNode(fc.getComment()));
			root.appendChild(comment);
			
			doc.appendChild(root);
		}

	}
	
	
	
	public boolean saveXML(OutputStream out) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StreamResult streamResult = new StreamResult(out);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer;
			serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, ENCODING);
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			serializer.transform(domSource, streamResult);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/** Prints the specified node, recursively. */
	public void printDOMTree(Node node) {
		int type = node.getNodeType();
		switch (type) {
		// print the document element
		case Node.DOCUMENT_NODE: {
			printDOMTree(((Document) node).getDocumentElement());
			break;
		}

		// print element with attributes
		case Node.ELEMENT_NODE: {
			System.out.print("<");
			System.out.print(node.getNodeName());
			NamedNodeMap attrs = node.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				System.out.print(" " + attr.getNodeName() + "=\""
						+ attr.getNodeValue() + "\"");
			}
			System.out.print(">");

			NodeList children = node.getChildNodes();
			if (children != null) {
				int len = children.getLength();
				for (int i = 0; i < len; i++)
					printDOMTree(children.item(i));
			}

			break;
		}

		// handle entity reference nodes
		case Node.ENTITY_REFERENCE_NODE: {
			System.out.print("&");
			System.out.print(node.getNodeName());
			System.out.println(";");
			break;
		}

		// print cdata sections
		case Node.CDATA_SECTION_NODE: {
			System.out.print("<![CDATA[");
			System.out.print(node.getNodeValue());
			System.out.println("]]>");
			break;
		}

		// print text
		case Node.TEXT_NODE: {
			System.out.print(node.getNodeValue());
			break;
		}

		// print processing instruction
		case Node.PROCESSING_INSTRUCTION_NODE: {
			System.out.println("<?");
			System.out.print(node.getNodeName());
			String data = node.getNodeValue();
			{
				System.out.print(" ");
				System.out.print(data);
			}
			System.out.println("?>");
			break;
		}
		}

		if (type == Node.ELEMENT_NODE) {
			System.out.print("</");
			System.out.print(node.getNodeName());
			System.out.println('>');
		}
	}


	public void sendByEmail() {
		if (Desktop.isDesktopSupported()) {
	        desktop = Desktop.getDesktop();
		}
		String xml = "";
		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT,"yes");
			transformer.transform(domSource, result);
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xml = writer.toString();
		
		
		
		URI mailtoURI;
		try {
			mailtoURI = new URI("mailto", "christian@chrmoll.de?SUBJECT=KKmulticopterFlashTool: commit firmware"+"&BODY=" + xml, null);
			desktop.mail(mailtoURI);
		} catch (URISyntaxException e) {
			logger.log(Level.WARNING, e.getMessage());
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage());
		}
	}


}
