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
 * XMEditor - class for wysiwyg editing of simple styled text
 * and images.
 */
package org.concord.swing.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XMEditor
extends JTextPane
{
    protected AbstractDocument doc;
	protected DocumentBuilderFactory builderFactory;
	protected DocumentBuilder builder;
	protected XMEditorKit editorKit;
    //protected static final int MAX_CHARACTERS = 300;
    protected JTextArea changeLog;
    protected String newline = "\n";
    protected HashMap actions;
	protected JPanel statusPane;
    protected JMenu editMenu;
    protected JMenu styleMenu;
	protected JPopupMenu popupMenu;
	protected boolean popupMenuEnabled = true;

    //undo helpers
    protected UndoAction undoAction;
    protected RedoAction redoAction;
    protected UndoManager undo = new UndoManager();
	protected MouseListener popupListener = new MouseAdapter()
	{
		boolean maybePopup = false;
		public void mousePressed(MouseEvent event)
		{
			maybePopup = event.isPopupTrigger();
		}
		
		public void mouseReleased(MouseEvent event)
		{
			if ((maybePopup || event.isPopupTrigger()) && popupMenuEnabled)
				popupMenu.show(XMEditor.this, event.getX(), event.getY());
		}
	};

    public XMEditor()
	{
		builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setValidating(false);
		try
		{
			builder = builderFactory.newDocumentBuilder();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		editorKit = new XMEditorKit();
		setEditorKit(editorKit);
		editorKit.install(this);
        setCaretPosition(0);
        setMargin(new Insets(5,5,5,5));
        StyledDocument styledDoc = getStyledDocument();
        if (styledDoc instanceof AbstractDocument)
		{
            doc = (AbstractDocument)styledDoc;
            //doc.setDocumentFilter(new DocumentSizeFilter(MAX_CHARACTERS));
        }
		else
		{
            System.err.println("Text pane's document isn't an AbstractDocument!");
            System.exit(-1);
        }

        //Create the status area.
        statusPane = new JPanel(new GridLayout(1, 1));
        CaretListenerLabel caretListenerLabel = new CaretListenerLabel("Caret Status");
        statusPane.add(caretListenerLabel);
		
        createActionTable(this);
        editMenu = createEditMenu();
        styleMenu = createStyleMenu();
		popupMenu = new JPopupMenu("Editor Menu");
		popupMenu.add(editMenu);
		popupMenu.add(styleMenu);

        addBindings();

		//Start watching for undoable edits and caret changes.
        doc.addUndoableEditListener(new MyUndoableEditListener());
        addCaretListener(caretListenerLabel);
        doc.addDocumentListener(new MyDocumentListener());
		addMouseListener(popupListener);
    }
	
	public boolean isPopupMenuEnabled()
	{
		return popupMenuEnabled;
	}
	
	public void setPopupMenuEnabled(boolean value)
	{
		popupMenuEnabled = value;
	}
	
	public void write(Writer writer)
	{
		Element root = doc.getDefaultRootElement();
		org.w3c.dom.Document document = builder.newDocument();
		writeData(document, document);
		writeTree(root, document, document);
		try
		{
   		    Transformer t = TransformerFactory.newInstance().newTransformer();
           	t.setOutputProperty(OutputKeys.INDENT,"yes");
	        t.setOutputProperty(OutputKeys.METHOD, "xml");
	        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
	        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");
        		t.transform(new DOMSource(document),new StreamResult(writer));
		}
		catch (Exception e)
		{
			System.out.println("write = " + e);
		}
	}
	
	protected void writeData(org.w3c.dom.Document document, org.w3c.dom.Node parent)
	{
		try
		{
			StringWriter writer = new StringWriter();
			super.write(writer);
			String data = writer.toString();
			org.w3c.dom.Node dataNode = document.createCDATASection(data);
			parent.appendChild(dataNode);
		}
		catch (Exception e)
		{
			System.out.println("writeData = " + e);
		}
	}
	
	protected void writeTree(Element element, org.w3c.dom.Document document, org.w3c.dom.Node parent)
	{
		org.w3c.dom.Element xmlElement = document.createElement(element.getName());
		parent.appendChild(xmlElement);
		if (parent == document)
			writeData(document, xmlElement);
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
/*	
	public void read(Reader reader, Object desc)
	{
		doc = (AbstractDocument) editorKit.createDefaultDocument();
		Element root = doc.getDefaultRootElement();
		org.w3c.dom.Document document = builder.parse(new InputSource(reader));
		readTree(root, document, document);
	}
	
	public void readData(org.w3c.dom.Node node)
	{
		String text = node.getNodeValue();
		StringReader reader = new StringReader(text);
		super.read(reader, null);
	}
	
	public void readTree(Element element, org.w3c.dom.Document document, org.w3c.dom.Node node)
	{
		if (node instanceof CDATASection)
		{
			readData(node);
		}
		else
		{
			org.w3c.dom.Node attributeNode = null;
			if (node.getNodeName().equals(AbstractDocument.SectionElementName))
			{
				NamedNodeMap attributes = node.getAttributes();
				for (int i = 0; i = attributes.getLength(); i++)
				{
					attributeNode = attributes.item(i);
					((AbstractDocument.AbstractElement) element).addAttribute(attributeNode.getNodeName(), attributeNode.getNodeValue());
				}
			}
			else if (node.getNodeName().equals(AbstractDocument.ParagraphElementName))
			{
				AttributeSet attributeSet = new StyleConstants.ParagraphConstants();
				AbstractDocument.BranchElement branch = new AbstractDocument.BranchElement(element, attributeSet);
				NamedNodeMap attributes = node.getAttributes();
				for (int i = 0; i = attributes.getLength(); i++)
				{
					attributeNode = attributes.item(i);
					branch.addAttribute(attributeNode.getNodeName(), attributeNode.getNodeValue());
				}
				element = branch;
			}
			else if (node.getNodeName().equals(AbstractDocument.ContentElementName))
			{
				NamedNodeMap attributes = node.getAttributes();
				attributeNode = attributes.getNamedItem("start");
				int start = Integer.decode(attributeNode.getNodeValue()).intValue();
				attributeNode = attributes.getNamedItem("end");
				int end = Integer.decode(attributeNode.getNodeValue()).intValue();
				AttributeSet attributeSet = new StyleConstants.ParagraphConstants();
				AbstractDocument.LeafElement leaf = new AbstractDocument.LeafElement(element, attributeSet, start, end);
				for (int i = 0; i = attributes.getLength(); i++)
				{
					attributeNode = attributes.item(i);
					if (attributeNode.getNodeName().equals("start"))
						continue;
					if (attributeNode.getNodeName().equals("end"))
						continue;
					leaf.addAttribute(attributeNode.getNodeName(), attributeNode.getNodeValue());
				}
				element = leaf;
			}
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++)
			{
				node = list.item(i);
				readTree(element, document, node);
			}
		}
	}
*/	
    //This listens for and reports caret movements.
    protected class CaretListenerLabel
	extends JLabel
    implements CaretListener
	{
        public CaretListenerLabel(String label)
		{
            super(label);
        }

        //Might not be invoked from the event dispatching thread.
        public void caretUpdate(CaretEvent e)
		{
            displaySelectionInfo(e.getDot(), e.getMark());
        }

        //This method can be invoked from any thread.  It 
        //invokes the setText and modelToView methods, which 
        //must run in the event dispatching thread. We use
        //invokeLater to schedule the code for execution
        //in the event dispatching thread.
        protected void displaySelectionInfo(final int dot,  final int mark)
		{
            SwingUtilities.invokeLater(new Runnable()
			{
                public void run()
				{
                    if (dot == mark)
					{  // no selection
                        try
						{
                            Rectangle caretCoords = XMEditor.this.modelToView(dot);
                            //Convert it to view coordinates.
							String text = "caret: text position: ";
							text += dot + ", view location = [";
							text += caretCoords.x + ", ";
							text += caretCoords.y + "]";
							text += newline;
                            setText(text);
                        }
						catch (BadLocationException ble)
						{
                            setText("caret: text position: " + dot + newline);
                        }
                    }
					else if (dot < mark)
					{
                        setText("selection from: " + dot + " to " + mark + newline);
                    }
					else
					{
                        setText("selection from: " + mark + " to " + dot + newline);
                    }
                }
            });
        }
    }

    //This one listens for edits that can be undone.
    protected class MyUndoableEditListener
    implements UndoableEditListener
	{
        public void undoableEditHappened(UndoableEditEvent e)
		{
            //Remember the edit and update the menus.
            undo.addEdit(e.getEdit());
            undoAction.updateUndoState();
            redoAction.updateRedoState();
        }
    }

    //And this one listens for any changes to the document.
    protected class MyDocumentListener
    implements DocumentListener
	{
        public void insertUpdate(DocumentEvent e)
		{
            displayEditInfo(e);
        }
        public void removeUpdate(DocumentEvent e)
		{
            displayEditInfo(e);
        }
        public void changedUpdate(DocumentEvent e)
		{
            displayEditInfo(e);
        }
        private void displayEditInfo(DocumentEvent e)
		{
            Document document = (Document)e.getDocument();
			if (document instanceof Document)
			{
				int changeLength = e.getLength();
				if (changeLog instanceof JTextArea)
				{
					String text = e.getType().toString() + ": ";
					text += " character";
					text +=  (changeLength == 1) ? ". " : "s. ";
					text += " Text length = " + document.getLength();
					text += "." + newline;
					changeLog.append(text);
				}
			}
        }
    }

    //Add a couple of emacs key bindings for navigation.
    protected void addBindings()
	{
        InputMap inputMap = getInputMap();

        //Ctrl-b to go backward one character
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.backwardAction);

        //Ctrl-f to go forward one character
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.forwardAction);

        //Ctrl-p to go up one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.upAction);

        //Ctrl-n to go down one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.downAction);

        //Ctrl-z to undo
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);
        inputMap.put(key, undoAction);

        //Ctrl-y to redo
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK);
        inputMap.put(key, redoAction);

        //Ctrl-x to cut to clipboard
        key = KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.cutAction);

        //Ctrl-c to copy to clipboard
        key = KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.copyAction);

        //Ctrl-v to paste from clipboard
        key = KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.pasteAction);
    }

    //Create the edit menu.
    protected JMenu createEditMenu()
	{
        JMenu menu = new JMenu("Edit");

        //Undo and redo are actions of our own creation.
        undoAction = new UndoAction();
        menu.add(undoAction);

        redoAction = new RedoAction();
        menu.add(redoAction);

        menu.addSeparator();

        //These actions come from the default editor kit.
        //Get the ones we want and stick them in the menu.
        menu.add(getActionByName(DefaultEditorKit.cutAction));
        menu.add(getActionByName(DefaultEditorKit.copyAction));
        menu.add(getActionByName(DefaultEditorKit.pasteAction));

        menu.addSeparator();

        menu.add(getActionByName(DefaultEditorKit.selectAllAction));

        menu.addSeparator();
		
		menu.add(new XMEditorKit.InsertImageAction("Insert image..."));
        return menu;
    }

    //Create the style menu.
    protected JMenu createStyleMenu()
	{
        JMenu menu = new JMenu("Style");

        Action action = new StyledEditorKit.BoldAction();
        action.putValue(Action.NAME, "Bold");
        menu.add(action);

        action = new StyledEditorKit.ItalicAction();
        action.putValue(Action.NAME, "Italic");
        menu.add(action);

        action = new StyledEditorKit.UnderlineAction();
        action.putValue(Action.NAME, "Underline");
        menu.add(action);

        menu.addSeparator();
		
		JMenu subMenu = new JMenu("Font Family");
		menu.add(subMenu);

        subMenu.add(new StyledEditorKit.FontFamilyAction("Serif", "Serif"));
        subMenu.add(new StyledEditorKit.FontFamilyAction("SansSerif", "SansSerif"));
        subMenu.add(new StyledEditorKit.FontFamilyAction("Monospaced", "Monospaced"));

		subMenu = new JMenu("Font Size");
		menu.add(subMenu);
		
        subMenu.add(new StyledEditorKit.FontSizeAction("8", 8));
        subMenu.add(new StyledEditorKit.FontSizeAction("10", 10));
        subMenu.add(new StyledEditorKit.FontSizeAction("12", 12));
        subMenu.add(new StyledEditorKit.FontSizeAction("14", 14));
        subMenu.add(new StyledEditorKit.FontSizeAction("18", 18));
        subMenu.add(new StyledEditorKit.FontSizeAction("20", 20));
        subMenu.add(new StyledEditorKit.FontSizeAction("24", 24));
        subMenu.add(new StyledEditorKit.FontSizeAction("36", 36));


        menu.addSeparator();
		menu.add(new XMEditorKit.TextColorAction("Text Color..."));
		menu.add(new XMEditorKit.TextBackgroundAction("Text Background Color..."));
		menu.add(new XMEditorKit.BackgroundAction("Background Color..."));

        return menu;
    }

    public void initDocument()
	{
        String initString[] =
                { "Use the mouse to place the caret.",
                  "Use the edit menu to cut, copy, paste, and select text.",
                  "Also to undo and redo changes.",
                  "Use the style menu to change the style of the text.",
                  "Use these emacs key bindings to move the caret:",
                  "ctrl-f, ctrl-b, ctrl-n, ctrl-p." };

        SimpleAttributeSet[] attrs = initAttributes(initString.length);

        try
		{
            for (int i = 0; i < initString.length; i ++)
			{
                doc.insertString(doc.getLength(), initString[i] + newline,
                        attrs[i]);
            }
        }
		catch (BadLocationException ble)
		{
            System.err.println("Couldn't insert initial text.");
        }
    }

    protected SimpleAttributeSet[] initAttributes(int length)
	{
        //Hard-code some attributes.
        SimpleAttributeSet[] attrs = new SimpleAttributeSet[length];

        attrs[0] = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs[0], "SansSerif");
        StyleConstants.setFontSize(attrs[0], 16);

        attrs[1] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setBold(attrs[1], true);

        attrs[2] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setItalic(attrs[2], true);

        attrs[3] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setFontSize(attrs[3], 20);

        attrs[4] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setFontSize(attrs[4], 12);

        attrs[5] = new SimpleAttributeSet(attrs[0]);
        StyleConstants.setForeground(attrs[5], Color.red);

        return attrs;
    }

    //The following two methods allow us to find an
    //action provided by the editor kit by its name.
    private void createActionTable(JTextComponent textComponent)
	{
        actions = new HashMap();
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++)
		{
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
    }

    private Action getActionByName(String name)
	{
        return (Action)(actions.get(name));
    }

    class UndoAction
	extends AbstractAction
	{
        public UndoAction()
		{
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e)
		{
            try
			{
                undo.undo();
            }
			catch (CannotUndoException ex)
			{
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState()
		{
            if (undo.canUndo())
			{
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            }
			else
			{
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction
	extends AbstractAction
	{
        public RedoAction()
		{
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e)
		{
            try
			{
                undo.redo();
            } catch (CannotRedoException ex)
			{
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState()
		{
            if (undo.canRedo())
			{
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            }
			else
			{
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI()
	{
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
		final JFrame frame = new JFrame("Extensible Markup Editor");

        //Create and set up the window.
        final XMEditor editor = new XMEditor();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JScrollPane scrollPane = new JScrollPane(editor);
        scrollPane.setPreferredSize(new Dimension(200, 200));

        //Create the text area for the status log and configure it.
        editor.changeLog = new JTextArea(5, 30);
        editor.changeLog.setEditable(false);
        JScrollPane scrollPaneForLog = new JScrollPane(editor.changeLog);

        //Create a split pane for the change log and the text area.
        JSplitPane splitPane = new JSplitPane(
                                       JSplitPane.VERTICAL_SPLIT,
                                       scrollPane, scrollPaneForLog);
        splitPane.setOneTouchExpandable(true);

        //Add the components.
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.getContentPane().add(editor.statusPane, BorderLayout.PAGE_END);

        //Put the initial text into the text pane.
        editor.initDocument();

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    //The standard main method.
    public static void main(String[] args)
	{
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
