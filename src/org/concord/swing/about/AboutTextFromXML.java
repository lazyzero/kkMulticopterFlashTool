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

/*
 * Created on Sep 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.concord.swing.about;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author ebrownmunoz
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AboutTextFromXML {
    
    static String TYPE = "text/html";
    
    public static JTabbedPane getTabbedPane(String filename) {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        Document mydoc = read(filename);
        parseTopLevelNode(tabbedPane, mydoc);
    
        return tabbedPane;
    }
    
    private static Document read(String filename) {
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        InputStream urlStream;
        
        try {
            
            urlStream = AboutTextFromXML.class.getResourceAsStream(filename);

            factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {

                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    try {
                        return new InputSource(AboutTextFromXML.class.getResourceAsStream("xml/about.dtd"));
                    } catch (Throwable t ) {
                        System.out.println(t);
                    }
                    return null;
                }
                
            });
            builder.setErrorHandler(new ParseErrorHandler());
            
            document = builder.parse( urlStream );
            
        } catch (Exception parseException ) {
            System.err.println("Parse Exception " + parseException);
            System.err.println(parseException.getMessage());
            parseException.printStackTrace();
        }
        return document;
    }
    
    // This parses any node that is not subordinate to tab.
    // Right now this means <about>.
    private static void parseTopLevelNode (JTabbedPane tabbedPane, Node aNode) {
        NodeList nlist = aNode.getChildNodes();
        int length = nlist.getLength();
        
        for (int i = 0 ; i < length; i++) {
            Node tNode = nlist.item(i);
            if (tNode.getNodeName().equals("about")) {
                parseTopLevelNode(tabbedPane, tNode);
            } else if (tNode.getNodeName().equals("tab")) {
                parseTab(tabbedPane, tNode);
            }
        }
    }

    // Parse <tab> elements
    private static JComponent parseTab (JTabbedPane parent, Node aNode) {
        JEditorPane retval = null;
        JScrollPane scroll;
        
        String tabName = "";
        String contents;
        
        Node attributeNode = aNode.getAttributes().getNamedItem("name");
        if (attributeNode != null)
            tabName = attributeNode.getNodeValue();

        
        contents = "<html>" + parseContents(aNode) + "</html>";
        retval = new JEditorPane(TYPE, contents);
        retval.setEditable(false);
        
        scroll = new JScrollPane(retval, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        

        if (parent != null) {
            parent.add(tabName, scroll);
        }
        
        return retval;
    }
    
    private static  String parseContents(Node aNode) {

        NodeList nlist = aNode.getChildNodes();
        int length = nlist.getLength();
        StringBuffer contents = new StringBuffer();
        String name = aNode.getNodeName();
        String retval;
        
        if (name.equals("#text")) {
            contents.append(aNode.getNodeValue());
            return contents.toString();
        } else if (name.equals("foo")) {
            
        } else {
            
            for (int i = 0 ; i < length; i++) {
                Node tNode = nlist.item(i);
                if (tNode.getNodeName().equals("#text")) {
                    contents.append(tNode.getNodeValue());
                } else {
                    contents.append(parseContents(tNode));
                }
            }
        }
        
        if (name.equals("tab")) {
            retval = contents.toString();
        } else {
            retval =  "<" + name + ">" + contents.toString() + "</" + name + ">";
                
        }
    return retval;
    }
    
    public static void main(String [] args) {
        JFrame frame = new JFrame("About Box");
        JTabbedPane contents = getTabbedPane("SampleAboutText.xml");
        
        frame.getContentPane().add(contents);
        frame.pack();
        frame.show();
    }
}

class ParseErrorHandler extends DefaultHandler {
    public void error(SAXParseException spe){
        System.err.println("SAX Parse error:" + spe.getMessage());
        System.err.println("SAX Parse error:" + spe.getPublicId());
        
    }
    
    public void fatalError(SAXParseException spe){
        System.err.println("Fatal SAX parse error:" + spe.getMessage());
        System.err.println("Fatal SAX parse error:" + spe.getPublicId());
        
    }
}