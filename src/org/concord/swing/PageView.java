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

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class PageView
extends JPanel
implements HyperlinkListener, SwingConstants
{
	private static final long serialVersionUID = 1L;
	protected JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
	protected JMenuBar menuBar = new JMenuBar();
	protected JFileChooser imageFileChooser = new JFileChooser();
	protected JFileChooser componentFileChooser = new JFileChooser();
	protected JFileChooser openFileChooser = new JFileChooser();
	protected JFileChooser saveFileChooser = new JFileChooser();
	protected Editor editor;
	protected boolean editable = true;
	protected File currentOpenFile;
	protected JScrollPane scrollPane;
	protected JFrame viewFrame;
	protected PageView viewPage;
	protected Map actionMap = new HashMap();
	protected Map menuMap = new HashMap();
	protected Action insertImage = new AbstractAction()
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event)
		{
			if (imageFileChooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION)
			{
				File imageFile = imageFileChooser.getSelectedFile();
				try
				{
					URL iconURL = imageFile.toURL();
					editor.insertIcon(iconURL);
				}
				catch (Exception e)
				{
					System.out.println("" + e);
				}
			}
		}
	};
	protected Action insertComponent = new AbstractAction()
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event)
		{
			if (componentFileChooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION)
			{
				File componentFile = componentFileChooser.getSelectedFile();
				try
				{
					URL componentURL = componentFile.toURL();
					editor.insertComponent(componentURL);
				}
				catch (Exception e)
				{
					System.out.println("" + e);
				}
			}
		}
	};
	protected Action saveAction = new AbstractAction()
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event)
		{
			if (saveFileChooser.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION)
			{
				File file = saveFileChooser.getSelectedFile();
				try
				{
					OutputStream output = new FileOutputStream(file);
					editor.write(output);
					output.close();
				}
				catch (Exception e)
				{
					System.out.println("" + e);
				}
			}
		}
	};
	protected Action openAction = new AbstractAction()
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event)
		{
			if (openFileChooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION)
			{
				File file = openFileChooser.getSelectedFile();
				try
				{
					currentOpenFile = file;
					InputStream input = new FileInputStream(file);
					editor.read(input, null);
					input.close();
				}
				catch (Exception e)
				{
					System.out.println("" + e);
				}
			}
		}
	};
	protected Action viewAction = new AbstractAction()
	{
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event)
		{
			if (currentOpenFile != null)
			{
				if (viewPage == null)
				{
					viewFrame = new JFrame("PageView");
					viewPage = new PageView();
					viewFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
					viewFrame.getContentPane().add(viewPage);
					viewFrame.setBounds(100, 100, 800, 600);
				}
			}
			try
			{
				viewPage.setEditable(true);
				viewPage.readInput(new FileInputStream(currentOpenFile));
				viewPage.setEditable(false);
			}
			catch (FileNotFoundException e)
			{
			}
			viewFrame.setVisible(true);
		}
	};
	protected String [][] commands =
	{
		{ "OpenAction", "Open", null, "Actions", "images/open.gif" },
		{ "SaveAction", "Save", null, "Actions", "images/save.gif" },
		{ "ViewAction", "View", null, "Actions", null },
		{ "copy-to-clipboard", "Copy", null, "Edit", "images/copy.gif" },
		{ "cut-to-clipboard", "Cut", null, "Edit", "images/cut.gif" },
		{ "paste-from-clipboard", "Paste", null, "Edit", "images/paste.gif" },
		{ "separator", null, null, "Edit", null },
		{ "select-all", "Select All", null, "Edit", null },
		{ "left-justify", "Left", "Edit", "Align", "images/AlignLeft.gif" },
		{ "center-justify", "Center", "Edit", "Align", "images/AlignCenter.gif" },
		{ "right-justify", "Right", "Edit", "Align", "images/AlignRight.gif" },
		{ "font-size-8", "8", "Font", "Size", null },
		{ "font-size-10", "10", "Font", "Size", null },
		{ "font-size-12", "12", "Font", "Size", null },
		{ "font-size-14", "14", "Font", "Size", null },
		{ "font-size-16", "16", "Font", "Size", null },
		{ "font-size-18", "18", "Font", "Size", null },
		{ "font-size-24", "24", "Font", "Size", null },
		{ "font-size-36", "36", "Font", "Size", null },
		{ "font-size-48", "48", "Font", "Size", null },
		{ "font-family-Serif", "Serif", "Font", "Family", null },
		{ "font-family-SansSerif", "SansSerif", "Font", "Family", null },
		{ "font-family-Monospaced", "Monospaced", "Font", "Family", null },
		{ "font-bold", "Bold", "Font", "Style", "images/Bold.gif" },
		{ "font-italic", "Italic", "Font", "Style", "images/Italic.gif" },
		{ "font-underline", "Underline", "Font", "Style", "images/Underline.gif" },
		{ "InsertImage", "Image", null, "Insert", "images/InsertPicture.gif" },
		{ "InsertComponent", "Component", null, "Insert", "images/InsertComponent.gif" },
	};
	
	public PageView()
	{
		this(true);
	}
	
	public PageView(boolean editable)
	{
		setLayout(new BorderLayout());
		editor = new Editor(editable);
		scrollPane = new JScrollPane(editor);
		initializeCommands();
		setEditable(editable);
	}
	
	protected void initializeCommands()
	{
		actionMap.put("InsertImage", insertImage);
		actionMap.put("InsertComponent", insertComponent);
		actionMap.put("OpenAction", openAction);
		actionMap.put("SaveAction", saveAction);
		actionMap.put("ViewAction", viewAction);
		Action [] actions = editor.getEditorKit().getActions();
		for (int i = 0; i < actions.length; i++)
		{
			String name = actions[i].getValue(Action.NAME).toString();
			actionMap.put(name, actions[i]);
		}
		for (int i = 0; i < commands.length; i++)
		{
			String [] command = commands[i];
			Action action = (Action) actionMap.get(command[0]);
			URL imageURL = null;
			JMenu parentMenu = null;
			JMenu menu = null;
			JMenuItem item = null;
			if (command[2] != null)
			{
				parentMenu = (JMenu) menuMap.get(command[2]);
				if (parentMenu == null)
				{
					parentMenu = new JMenu(command[2]);
					menuMap.put(command[2], parentMenu);
					menuBar.add(parentMenu);
				}
			}
			menu = (JMenu) menuMap.get(command[3]);
			if (menu == null)
			{
				menu = new JMenu(command[3]);
				menuMap.put(command[3], menu);
				if (parentMenu != null)
					parentMenu.add(menu);
				else
					menuBar.add(menu);
			}
			if (command[1] != null)
			{
				item = menu.add(action);
				item.setText(command[1]);
			}
			if (command[4] != null)
			{
				imageURL = this.getClass().getResource(command[4]);
				JButton button = toolBar.add(action);
				button.setText(null);
				button.setIcon(new ImageIcon(imageURL));
				if (item != null)
					item.setIcon(button.getIcon());
			}
			if (command[0].equals("separator"))
			{
				menu.addSeparator();
			}
		}
		toolBar.setFloatable(true);
		add(scrollPane, "Center");
	}
	
	public void readInput(InputStream input)
	{
		editor.read(input, null);
		try
		{
			input.close();
		}
		catch (IOException e)
		{
		}
	}
	
	public boolean isEditable()
	{
		return editable;
	}
	
	public void setEditable(boolean value)
	{
		editable = value;
		if (editable)
		{
			add(toolBar, "West");
			add(menuBar, "North");
		}
		else
		{
			remove(toolBar);
			remove(menuBar);
		}
		editor.setEditable(editable);
		validate();
	}

	public void hyperlinkUpdate(HyperlinkEvent event)
	{
	}
	
	public static void main(String [] args)
	{
		JFrame frame = new JFrame("PageView");
		PageView view = new PageView();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(view);
		frame.setBounds(100, 100, 800, 600);
		frame.setVisible(true);
	}
	
	public static class StyleMethods
	{
		protected Map getters = new HashMap();
		protected Map setters = new HashMap();
		protected final String SET = "set";
		protected final String GET = "get";
		protected final String IS = "is";
		protected final String FONT = "font";
		protected final Class [][] primitives =
		{
			{ Integer.TYPE, Integer.class },
			{ Long.TYPE, Long.class },
			{ Short.TYPE, Short.class },
			{ Byte.TYPE, Byte.class },
			{ Float.TYPE, Float.class },
			{ Double.TYPE, Double.class },
			{ Character.TYPE, Character.class },
			{ Boolean.TYPE, Boolean.class }
		};
		protected Map typeMap = new HashMap();
		
		public StyleMethods()
		{
			Method [] methods = StyleConstants.class.getMethods();
			for (int i = 0; i < methods.length; i++)
			{
				Method method = methods[i];
				String methodName = method.getName();
				if (methodName.startsWith(GET))
				{
					createProperty(methodName, GET.length(), method, getters);
				}
				else if (methodName.startsWith(IS))
				{
					createProperty(methodName, IS.length(), method, getters);
				}
				else if (methodName.startsWith(SET))
				{
					createProperty(methodName, SET.length(), method, setters);
				}
			}
			for (int i = 0; i < primitives.length; i++)
			{
				typeMap.put(primitives[i][0], primitives[i][1]);
			}
		}
		
		protected Class getTypeClass(Class type)
		{
			Class typeClass = (Class) typeMap.get(type);
			if (typeClass == null)
				return type;
			return typeClass;
		}
		
		protected void createProperty(String methodName, int prefixLength, Method method, Map table)
		{
			String propertyName = methodName.substring(prefixLength);
			propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
			table.put(propertyName, method);
			if (propertyName.startsWith(FONT))
			{
				propertyName = propertyName.substring(FONT.length());
				propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
				table.put(propertyName, method);
			}
		}
		
		public Object get(AttributeSet attributeSet, String propertyName)
		{
			propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
			Method method = (Method) getters.get(propertyName);
			if (method != null)
			{
				try
				{
					return method.invoke(null, new Object [] { attributeSet });
				}
				catch (IllegalArgumentException e)
				{
					System.out.println(e);
				}
				catch (IllegalAccessException e)
				{
					System.out.println(e);
				}
				catch (InvocationTargetException e)
				{
					System.out.println(e);
				}
			}
			return null;
		}
		
		public void set(MutableAttributeSet attributeSet, String propertyName, Object value)
		{
			propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
			Method method = (Method) setters.get(propertyName);
			if (method != null)
			{
				try
				{
					if (value instanceof String)
					{
						Class [] argClasses = method.getParameterTypes();
						Class argType = getTypeClass(argClasses[1]);
						Constructor constructor = argType.getConstructor(new Class [] { String.class });
						value = constructor.newInstance(new Object [] { value });
					}
					method.invoke(null, new Object [] { attributeSet, value });
				}
				catch (IllegalArgumentException e)
				{
					System.out.println(e);
				}
				catch (IllegalAccessException e)
				{
					System.out.println(e);
				}
				catch (InvocationTargetException e)
				{
					System.out.println(e);
				}
				catch (NoSuchMethodException e)
				{
					System.out.println(e);
				}
				catch (InstantiationException e)
				{
					System.out.println(e);
				}
			}
		}
	}
	
	
	public static class Editor
	extends JTextPane
	implements SelectableContainer, CaretListener
	{
		private static final long serialVersionUID = 1L;
		protected DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		protected DocumentBuilder builder;
		protected StyleContext styleContext = StyleContext.getDefaultStyleContext();
		protected Style sectionStyle = styleContext.addStyle("Section", null);
		protected Style paragraphStyle = styleContext.addStyle("Paragraph", sectionStyle);
		protected Style contentStyle = styleContext.addStyle("Content", paragraphStyle);
		protected StyleMethods styleMethods = new StyleMethods();
        protected Map componentMap = new HashMap();
        protected Map iconMap = new HashMap();
        protected Map locationMap = new HashMap();
        protected SelectionManager selectionManager = new SelectionManager();
        protected ComponentFramework state = new DefaultComponentFramework();
		
		public Editor(boolean editable)
		{
			builderFactory.setValidating(false);
	        setMargin(new Insets(5,5,5,5));
			super.setEditable(true);
			selectionManager.setSelectableContainer(this);
			addCaretListener(this);
			try
			{
				builder = builderFactory.newDocumentBuilder();
			}
			catch (Exception e)
			{
				System.out.println(e);
			}
		}
		
		public void setEditable(boolean value)
		{
			super.setEditable(value);
			if (componentMap != null)
			{
				Iterator components = componentMap.keySet().iterator();
				while (components.hasNext())
				{
					PageComponent pageComponent = (PageComponent) components.next();
					pageComponent.setEditable(value);
				}
			}
		}
		
		public void insertIcon(URL iconURL)
		{
			ImageIcon icon = new ImageIcon(iconURL);
			super.insertIcon(icon);
			iconMap.put(icon, iconURL);
		}
		
		public void insertComponent(URL componentURL)
		{
			PageComponent component = new PageComponent(componentURL);
			component.setSelectionManager(selectionManager);
			super.insertComponent(component);
			componentMap.put(component, componentURL);
		}
		
		public void write(OutputStream outputStream)
		{
		    AbstractDocument doc = (AbstractDocument) getDocument();
			Element root = doc.getDefaultRootElement();
			Document document = builder.newDocument();
			writeTree(root, document, document);
			try
			{
	   		    Transformer t = TransformerFactory.newInstance().newTransformer();
	           	t.setOutputProperty(OutputKeys.INDENT,"yes");
		        t.setOutputProperty(OutputKeys.METHOD, "xml");
		        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
		        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");
	        	t.transform(new DOMSource(document),new StreamResult(outputStream));
			}
			catch (Exception e)
			{
				System.out.println("write = " + e);
			}
		}
		
		protected void writeElement(Element element, Document document, Node parent, Object key, Map map)
		{
            AttributeSet attributes = element.getAttributes();
            Object value = attributes.getAttribute(key);
			URL url = (URL) map.get(value);
			org.w3c.dom.Element xmlElement = document.createElement(key.toString());
			parent.appendChild(xmlElement);
			xmlElement.setAttribute("href", url.toString());
			xmlElement.setAttribute("start", "" + element.getStartOffset());
			xmlElement.setAttribute("end", "" + element.getEndOffset());
		}
		
		protected void writeIconElement(Element element, Document document, Node parent)
		{
			Object iconKey = StyleConstants.CharacterConstants.IconAttribute;
			writeElement(element, document, parent, iconKey, iconMap);
		}
		
		protected void writeComponentElement(Element element, Document document, Node parent)
		{
            Object componentKey = StyleConstants.CharacterConstants.ComponentAttribute;
            writeElement(element, document, parent, componentKey, componentMap);
		}
		
		protected void writeTree(Element element, Document document, Node parent)
		{
		    String elementName = element.getName();
		    if (elementName.equals("icon"))
		    	writeIconElement(element, document, parent);
		    else if (elementName.equals("component"))
		    	writeComponentElement(element, document, parent);
		    else
		    {
				org.w3c.dom.Element xmlElement = document.createElement(elementName);
				parent.appendChild(xmlElement);
				if (parent == document)
				{
					try
					{
						StringWriter writer = new StringWriter();
						super.write(writer);
						String data = writer.toString();
						Node dataNode = document.createCDATASection(data);
						document.getDocumentElement().appendChild(dataNode);
					}
					catch (Exception e)
					{
						System.out.println("writeData = " + e);
					}
				}
				Enumeration eNames = ((AttributeSet) element).getAttributeNames();
				while (eNames.hasMoreElements())
				{
					Object name = eNames.nextElement();
					if (name.toString().equals("resolver"))
						continue;
					Object value = ((AttributeSet) element).getAttribute(name);
					xmlElement.setAttribute(name.toString(), value.toString());
				}
				if (element.isLeaf())
				{
					xmlElement.setAttribute("start", "" + element.getStartOffset());
					xmlElement.setAttribute("end", "" + element.getEndOffset());
				}
				else
				{
					for (int i = 0; i < element.getElementCount(); i++)
					{
						Element childElement = element.getElement(i);
						writeTree(childElement, document, xmlElement);
					}
				}
		    }
		}
		
		protected void installComponents()
		{
			Iterator componentUrls = locationMap.keySet().iterator();
			while (componentUrls.hasNext())
			{
				String urlString = (String) componentUrls.next();
				Integer location = (Integer) locationMap.get(urlString);
				int start = location.intValue();
				DefaultStyledDocument doc = (DefaultStyledDocument) getDocument();
				try
				{
					URL componentURL = new URL(urlString);
					doc.remove(start, 1);
					int dot = getCaret().getDot();
					getCaret().setDot(start);
					insertComponent(componentURL);
					getCaret().setDot(dot);
				}
				catch (MalformedURLException e)
				{
				}
				catch (BadLocationException e)
				{
				}
			}
		}
		
		public void read(InputStream input, Object desc)
		{
			removeCaretListener(this);
			try
			{
				Document document = builder.parse(new InputSource(input));
				readTree(document, document);
				installComponents();
			}
			catch (Exception e)
			{
				
			}
			addCaretListener(this);
		}
		
		protected void readDocumentElement(Node node, Style style)
		{
			style.removeAttributes(style);
			NamedNodeMap attributes = node.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++)
			{
				Node attributeNode = attributes.item(i);
				String nodeName = attributeNode.getNodeName();
				Object nodeValue = attributeNode.getNodeValue();
				styleMethods.set(style, nodeName, nodeValue);
			}
		}
		
		protected void readContentElement(Node node)
		{
			contentStyle.removeAttributes(contentStyle);
			NamedNodeMap attributes = node.getAttributes();
			Node attributeNode = attributes.getNamedItem("start");
			int start = Integer.decode(attributeNode.getNodeValue()).intValue();
			attributeNode = attributes.getNamedItem("end");
			int end = Integer.decode(attributeNode.getNodeValue()).intValue();
			DefaultStyledDocument doc = (DefaultStyledDocument) getDocument();
			for (int i = 0; i < attributes.getLength(); i++)
			{
				attributeNode = attributes.item(i);
				String nodeName = attributeNode.getNodeName();
				Object nodeValue = attributeNode.getNodeValue();
				if (nodeName.equals("start"))
					continue;
				if (nodeName.equals("end"))
					continue;
				styleMethods.set(contentStyle, nodeName, nodeValue);
			}
			try
			{
				int length = end - start;
				if ((length > 0) && (end <= doc.getLength()))
				{
					String text = doc.getText(start, length);
					doc.replace(start, length, text, contentStyle);
					doc.setParagraphAttributes(start, length, paragraphStyle, false);
				}
			}
			catch (DOMException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (BadLocationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		protected void readIconElement(Node node)
		{
			NamedNodeMap xmlAttributes = node.getAttributes();
			Node attributeNode = xmlAttributes.getNamedItem("href");
			String urlString = attributeNode.getNodeValue();
			attributeNode = xmlAttributes.getNamedItem("start");
			int start = Integer.decode(attributeNode.getNodeValue()).intValue();
			DefaultStyledDocument doc = (DefaultStyledDocument) getDocument();
			try
			{
				URL iconURL = new URL(urlString);
				doc.remove(start, 1);
				int dot = getCaret().getDot();
				getCaret().setDot(start);
				insertIcon(iconURL);
				getCaret().setDot(dot);
			}
			catch (MalformedURLException e)
			{
			}
			catch (BadLocationException e)
			{
			}
		}
		
		protected void readComponentElement(Node node)
		{
			NamedNodeMap xmlAttributes = node.getAttributes();
			Node attributeNode = xmlAttributes.getNamedItem("href");
			String urlString = attributeNode.getNodeValue();
			attributeNode = xmlAttributes.getNamedItem("start");
			int start = Integer.decode(attributeNode.getNodeValue()).intValue();
			locationMap.put(urlString, new Integer(start));
		}
		
		public void readTree(Document document, Node node)
		{
			if (node instanceof CDATASection)
			{
				try
				{
					String text = node.getNodeValue();
					StringReader reader = new StringReader(text);
					super.read(reader, null);
				}
				catch (Exception e)
				{
					
				}
			}
			else
			{
				if (node.getNodeName().equals(AbstractDocument.SectionElementName))
				{
					readDocumentElement(node, sectionStyle);
				}
				else if (node.getNodeName().equals(AbstractDocument.ParagraphElementName))
				{
					readDocumentElement(node, paragraphStyle);
				}
				else if (node.getNodeName().equals(AbstractDocument.ContentElementName))
				{
					readContentElement(node);
				}
				else if (node.getNodeName().equals("icon"))
				{
					readIconElement(node);
				}
				else if (node.getNodeName().equals("component"))
				{
					readComponentElement(node);
				}
				NodeList list = node.getChildNodes();
				for (int i = 0; i < list.getLength(); i++)
				{
					node = list.item(i);
					readTree(document, node);
				}
			}
		}

		public Selectable findSelectable(MouseEvent event, int x, int y)
		{
			return (Selectable) event.getSource();
		}

		public Selectable getActiveObject()
		{
			return null;
		}

		public Point getOffset()
		{
			return null;
		}

		public void select(Selectable selectable, boolean multiple)
		{
			selectable.setSelected(true);
		}

		public void deselect()
		{
		}

		public void dragAction(int dx, int dy, Selectable selectable)
		{
		}

		public void dragActionDone(Selectable selectable)
		{
		}

		public void caretUpdate(CaretEvent e)
		{
			if (selectionManager != null)
			{
				Selectable selected = selectionManager.getSelectedObject();
				if (selected != null)
					selected.setSelected(false);
			}
		}
	}
}

