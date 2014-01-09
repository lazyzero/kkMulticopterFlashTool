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

package org.concord.swing.beans;

/**
 * @author dima
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

/**
 *  Description of the Class
 *
 *@author     ed
 *@author     dima
 *@created    May 22, 2002
 *
 *imoncada: moved to util and added hashtable
 *			since it is in util now, it doesn't use ClientBeanInfo anymore
 */
public class PropertySheet extends Box
{
	private Box box;
	private JScrollPane scroll;
	private int nRealProperties;
	private String panelName = "";
	protected Object object;
	protected Class objectClass;
	protected Hashtable writeMethods = new Hashtable();
	private Object[] args = new Object[1];
	private boolean clientMode;
	private Vector propertyHolders = new Vector();
	static
	{
		PropertyEditorManager.registerEditor(Integer.TYPE, IntegerEditor.class);
		PropertyEditorManager.registerEditor(Float.TYPE, FloatEditor.class);
		PropertyEditorManager.registerEditor(Long.TYPE, IntegerEditor.class);
		PropertyEditorManager.registerEditor(Double.TYPE, FloatEditor.class);
		PropertyEditorManager.registerEditor(Boolean.TYPE, BooleanEditor.class);
		PropertyEditorManager.registerEditor(String.class, StringEditor.class);
		PropertyEditorManager.registerEditor(Integer.class, IntegerEditor.class);
		PropertyEditorManager.registerEditor(Float.class, FloatEditor.class);
		PropertyEditorManager.registerEditor(Long.class, IntegerEditor.class);
		PropertyEditorManager.registerEditor(Double.class, FloatEditor.class);
		PropertyEditorManager.registerEditor(Boolean.class, BooleanEditor.class);
		PropertyEditorManager.registerEditor(Color.class, ColorEditor.class);
		PropertyEditorManager.registerEditor(Font.class, FontEditor.class);
	}

	/**
	 *  Constructor for the PropertySheet object
	 *
	 *@param  o     Object whose properties are to be displayed
	 *@param  panelName  Name of panel
	 */
	public PropertySheet(Object o, String panelName)
	{
		this(o, panelName, true);
	}
	
	/**
	 *  Constructor for the PropertySheet object
	 *
	 *@param  o     Object whose properties are to be displayed
	 *@param  panelName  Name of panel
	 */
	 
	public PropertySheet(Object o, String panelName, boolean clientMode)
	{
		this(o,panelName,clientMode,null);
	}

	/**
	 *  Constructor for the PropertySheet object
	 *
	 *@param  o     Object whose properties are to be displayed
	 *@param  panelName  Name of panel
	 */
	public PropertySheet(Object o, String panelName, boolean clientMode, Hashtable editableProps)
	{
		super(BoxLayout.Y_AXIS);
		this.clientMode = clientMode;
		this.panelName = panelName;
		initialize(o,editableProps);
	}

	public void initialize(Object o)
	{
		initialize(o,null);
	}
	
	public void initialize(Object o, Hashtable validProps)
	{
		PropertyDescriptor[] pd = null;
		int nProperties = 0;
		if (o == null)
		{
			throw new IllegalArgumentException("PropertySheet initialize argument cannot be null");
		}

		objectClass = o.getClass();
		object = o;
		try
		{
			BeanInfo bi = getBeanInfo(objectClass);
			pd = bi.getPropertyDescriptors();
			nProperties = pd.length;
			nRealProperties = 0;
			for (int i = 0; i < pd.length; i++)
			{
				if (!pd[i].isHidden())
				{
					if (validProps==null){
						nRealProperties++;
					}
					else{
						if (validProps.get(pd[i].getName())!=null){
							nRealProperties++;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			nRealProperties = 0;
		}
		if (nRealProperties < 1)
		{
			throw new IllegalArgumentException("PropertySheet there are no properties to show");
		}
		if (box == null)
		{
			box = Box.createVerticalBox();
			if (clientMode)
			{
				scroll = new JScrollPane(box);
				TitledBorder outerBorder = new TitledBorder(new BevelBorder(BevelBorder.RAISED), panelName);
				outerBorder.setTitleColor(Color.black);
				scroll.setBorder(outerBorder);
				add(scroll);
			}
			else
			{
				add(box);
			}
		}
		else
			box.removeAll();
		
		
		for (int i = 0; i < nProperties; i++)
		{
			if (pd[i].isHidden())
				continue;

			if (validProps!=null)
			{
				String label;
				
				label=(String)validProps.get(pd[i].getName());
				if (label == null)
				{
					//The property is NOT in the hashtable of valid properties!
					continue;
				}
				else if (!label.equals(""))
				{
					pd[i].setDisplayName(label);
				}
			}

			PropertyEditor pEditor = null;
			Component customEditor = null;
			if (pd[i].getPropertyType() != null)
			{
				pEditor = PropertyEditorManager.findEditor(pd[i].getPropertyType());
			}
			String[] tags = null;
			Method wMethod = pd[i].getWriteMethod();
			PropertyHolder ph = null;
			try
			{
				if (pEditor != null)
				{
					ph = new PropertyHolder(this,o,pd[i],pEditor);
					propertyHolders.addElement(ph);
					pEditor.addPropertyChangeListener(ph);
					if (pEditor.supportsCustomEditor())
					{
						customEditor = pEditor.getCustomEditor();
					}
					else if (pEditor.getTags() != null)
					{
						tags = pEditor.getTags();
					}
					if (wMethod != null)
						writeMethods.put(pEditor, wMethod);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				pEditor = null;
				customEditor = null;
			}
			Method rMethod = pd[i].getReadMethod();
			Object val = null;
			if (rMethod != null)
			{
				try
				{
					if (pEditor instanceof PropertyEditor)
					{
						val = rMethod.invoke(o, null);
					}
				}
				catch (InvocationTargetException ite)
				{
				}
				catch (IllegalAccessException ie)
				{
				}
			}
			else
			{
				pEditor = null;
			}

			if (pEditor != null)
			{
				pEditor.setValue(val);
				box.add(Box.createVerticalGlue());
				JPanel pWrapper = new JPanel();
				pWrapper.setLayout(new BorderLayout());

				if (customEditor != null)
				{
					pWrapper.add("Center", customEditor);
					if (wMethod == null)
						customEditor.setEnabled(false);
				}
				else if (tags != null)
				{
					JComboBox choice = new JComboBox();
					for(int it = 0; it < tags.length; it++)
					{
						choice.addItem(tags[it]);
					}
					pWrapper.add("Center", choice);
					choice.setSelectedItem(pEditor.getAsText());
					if(ph != null)
					{
						choice.addActionListener(ph);
					}
					if (wMethod == null)
						choice.setEnabled(false);
					
				}
				TitledBorder titledBorder = new TitledBorder(pd[i].getDisplayName());
				titledBorder.setTitleColor(Color.black);
				pWrapper.setBorder(titledBorder);
				box.add(pWrapper);
				box.add(Box.createVerticalStrut(2));
			}
		}

		box.add(Box.createVerticalGlue());
		box.add(Box.createVerticalStrut(5));
	}


	protected BeanInfo getBeanInfo(Class objectClass)
	throws IntrospectionException
	{
		java.beans.BeanInfo beanInfo;
		
		try
		{
			//imoncada: SUPER HACK because the stupid Java 1.4 beans cannot load the bean info
			//of GUIPanel correctly due to its method addElement, that is considered
			//an event add method, so it crashes when it tries to get a substring
			//from addElement, thinking that it has more than 3+8 characters 
			//Thinking it is in the form addXXXListener
			//The exception is caused in the method getTargetEventInfo() in getBeanInfo()
			//in the Introspector class.
			//I didn't do anything better to fix this, so I just did this super hack :p
			//Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4896879
			//This bug is fixed in Java 1.5
			if (objectClass.getName().equals("org.concord.collisions.ui.GUIPanel"))
			{
				Class cl = Class.forName("org.concord.collisions.ui.GUIPanelBeanInfo");
				beanInfo = (BeanInfo) cl.newInstance();
			}
			else
			{
				beanInfo = Introspector.getBeanInfo(objectClass);
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			beanInfo = null;
		}
		
		if (beanInfo != null)
		{
			return beanInfo;
		}
		else
		{
			throw new IntrospectionException("Can't find beanInfo for class " + objectClass);
		}
	}

	/**
	 *  Gets the panelName attribute of the PropertySheet object
	 *
	 *@return    The panelName value
	 */
	public String getPanelName()
	{
		return panelName;
	}

	public Object getObject()
	{
		return object;
	}
	
	public Class getObjectClass()
	{
		return objectClass;
	}

	/**
	 *  Gets the preferredSize attribute of the PropertySheet object
	 *
	 *@return    The preferredSize value
	 */
	public Dimension getPreferredSize()
	{
		Dimension dsuper = super.getPreferredSize();
		Insets insets = getInsets();
		if (box == null)
		{
			return dsuper;
		}
		Dimension d = box.getPreferredSize();
		Dimension minSize = (scroll != null)?((TitledBorder) scroll.getBorder()).getMinimumSize(this):null;

		d.height += (nRealProperties + 2) * 5;
		if(minSize != null) d.width = Math.max(d.width, minSize.width);
		if(d.width < 200) d.width = 200;//that's not good solution but fast :-(

		return d;
	}

	
	public void restoreOldValues()
	{
		if (propertyHolders == null || propertyHolders.size() < 1)
			return;
		for(int i = 0; i < propertyHolders.size(); i++)
		{
			PropertyHolder ph = (PropertyHolder) propertyHolders.elementAt(i);
			ph.restoreOldValue();
		}
	}
	public void writeNewValues()
	{
		if (propertyHolders == null || propertyHolders.size() < 1)
			return;
		for(int i = 0; i < propertyHolders.size(); i++)
		{
			PropertyHolder ph = (PropertyHolder) propertyHolders.elementAt(i);
			ph.writeNewValue();
		}
	}
	
	public void propertyValueChanged(PropertyHolder ph)
	//Fired when a property changes value
	{
		firePropertyChange(ph.descriptor.getName(),ph.oldValue,ph.newValue);
	}
	
	public static class CustomEditor
	extends PropertyEditorSupport
	{
		protected Component customEditor;
		
		public CustomEditor()
		{
			customEditor = new JTextField();
		}
		
		public Component getCustomEditor()
		{
			return customEditor;
		}
		
		public boolean supportsCustomEditor()
		{
			return true;
		}
	}
	
	public static class StringEditor
	extends CustomEditor
	{
		protected JFormattedTextField stringEditor;
		protected KeyAdapter keyListener = new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
				firePropertyChange();
			}
		};
		
		public StringEditor(Format format)
		{
			setFormat(format);
		}
		
		public StringEditor()
		{
			setFormat(null);
		}
		
		protected void setFormat(Format format)
		{
			if (format == null)
				stringEditor = new JFormattedTextField();
			else
				stringEditor = new JFormattedTextField(format);
			stringEditor.addKeyListener(keyListener);
			customEditor = stringEditor;
		}

		public String getAsText()
		{
			return stringEditor.getText();
		}
		
		public void setAsText(String text)
		{
			stringEditor.setText(text);
		}
		
		public Object getValue()
		{
			return getAsText();
		}
		
		public void setValue(Object value)
		{
			if (value == null)
				value = "";
			stringEditor.setText(value.toString());
		}
	}
	
	public static class IntegerEditor
	extends StringEditor
	{
		DecimalFormat format = new DecimalFormat();
		
		public IntegerEditor()
		{
			format.setMaximumIntegerDigits(10);
			format.setMinimumIntegerDigits(1);
			format.setMaximumFractionDigits(0);
			format.setMinimumFractionDigits(0);
			setFormat(format);
		}
		
		public Object getValue()
		{
			return Integer.valueOf(stringEditor.getText());
		}
	}
	
	public static class FloatEditor
	extends StringEditor
	{
		DecimalFormat format = new DecimalFormat();
		
		public FloatEditor()
		{
			format.setMaximumIntegerDigits(10);
			format.setMinimumIntegerDigits(1);
			format.setMaximumFractionDigits(10);
			format.setMinimumFractionDigits(1);
		}
		
		public Object getValue()
		{
			return Float.valueOf(stringEditor.getText());
		}
	}
	
	public static class BooleanEditor
	extends CustomEditor
	{
		protected JPanel buttonPanel = new JPanel();
		protected JRadioButton trueButton = new JRadioButton("true");
		protected JRadioButton falseButton = new JRadioButton("false");
		protected ButtonGroup group = new ButtonGroup();
		protected ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				firePropertyChange();
			}
		};
		
		public BooleanEditor()
		{
			buttonPanel.setLayout(new FlowLayout());
			buttonPanel.add(trueButton);
			buttonPanel.add(falseButton);
			group.add(trueButton);
			group.add(falseButton);
			trueButton.addActionListener(actionListener);
			falseButton.addActionListener(actionListener);
			customEditor = buttonPanel;
		}
		
		public String getAsText()
		{
			return trueButton.isSelected() ? "true" : "false";
		}
		
		public void setAsText(String value)
		{
			if (value.toLowerCase().equals("true"))
				trueButton.setSelected(true);
			else
				falseButton.setSelected(true);
		}

		public Object getValue()
		{
			return new Boolean(trueButton.isSelected());
		}
		
		public void setValue(Object value)
		{
			if (value instanceof Boolean)
				setAsText(value.toString());
		}
	}
	
	public static class ColorEditor
	extends CustomEditor
	implements ActionListener
	{
		protected JButton colorButton = new JButton("Color");
		protected JColorChooser colorChooser = new JColorChooser();
		
		public ColorEditor()
		{
			colorButton.addActionListener(this);
			customEditor = colorButton;
		}

		public void actionPerformed(ActionEvent e)
		{
			Color color = colorButton.getBackground();
			color = JColorChooser.showDialog(colorButton, "Choose Color", color);
			colorButton.setBackground(color);
			firePropertyChange();
		}
		
		public Object getValue()
		{
			return colorButton.getBackground();
		}
		
		public void setValue(Object value)
		{
			if (value instanceof Color)
			{
				colorButton.setBackground((Color) value);
			}
		}
	}

	public static class FontEditor
	extends CustomEditor
	{
		protected static final GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
		protected static final String [] families = graphics.getAvailableFontFamilyNames();
		protected static final int [] styles =
		{
			Font.PLAIN, Font.BOLD, Font.ITALIC
		};
		protected static final String [] styleNames =
		{
			"Plain", "Bold", "Italic"
		};
		protected static final int [] sizes =
		{
			6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 24, 36, 48
		};
		protected JPanel fontPanel = new JPanel();
		protected JComboBox fontFamily = new JComboBox();
		protected JComboBox fontStyle = new JComboBox();
		protected JComboBox fontSize = new JComboBox();
		protected ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				firePropertyChange();
			}
			
		};
		
		public FontEditor()
		{
			fontPanel.setLayout(new FlowLayout());
			fontPanel.add(fontFamily);
			fontPanel.add(fontStyle);
			fontPanel.add(fontSize);
			for (int i = 0; i < families.length; i++)
			{
				fontFamily.addItem(families[i]);
			}
			for (int i = 0; i < styles.length; i++)
			{
				fontStyle.addItem(styleNames[i]);
			}
			for (int i = 0; i < sizes.length; i++)
			{
				fontSize.addItem("" + sizes[i]);
			}
			fontFamily.addActionListener(actionListener);
			fontStyle.addActionListener(actionListener);
			fontSize.addActionListener(actionListener);
			customEditor = fontPanel;
		}

		public void actionPerformed(ActionEvent e)
		{
			firePropertyChange();
		}
		
		public Object getValue()
		{
			String family = (String) fontFamily.getSelectedItem();
			int style = styles[fontStyle.getSelectedIndex()];
			int size = Integer.valueOf((String) fontSize.getSelectedItem()).intValue();
			return new Font(family, style, size);
		}
		
		public void setValue(Object value)
		{
			if (value instanceof Font)
			{
				Font font = (Font) value;
				String styleName = null;
				for (int i = 0; i < styles.length; i++)
				{
					if (font.getStyle() == styles[i])
					{
						styleName = styleNames[i];
						break;
					}
				}
				String family = font.getFamily();
				fontFamily.setSelectedItem(family);
				if (styleName != null)
					fontStyle.setSelectedItem(styleName);
				int size = font.getSize();
				fontSize.setSelectedItem("" + font.getSize());
			}
		}
	}
	
	protected class PropertyHolder
	implements PropertyChangeListener, ActionListener
	{
		protected PropertyDescriptor descriptor;
		protected Method writeMethod;
		protected Method readMethod;
		protected Object oldValue;
		protected boolean valueChanged = false;
		protected Object newValue;
		protected Object propertyOwner;
		private Object[] args = new Object[1];
		private PropertyEditor pEditor;
		
		private PropertySheet owner;
		
		public PropertyHolder(PropertySheet owner, Object propertyOwner, PropertyDescriptor descriptor, PropertyEditor pEditor)
		{
			this.owner=owner;
			this.propertyOwner = propertyOwner;
			this.descriptor 	= descriptor;
			this.pEditor 		= pEditor;
			defineMethods();
		}
		
		void defineMethods()
		{
			if (propertyOwner == null || descriptor == null)
			{
				return;
			}
			writeMethod = descriptor.getWriteMethod();
			readMethod = descriptor.getReadMethod();
			try
			{
				oldValue = readMethod.invoke(propertyOwner,null);
			}
			catch(java.lang.reflect.InvocationTargetException e)
			{
				System.out.println("PropertyHolder defineMethods Exception "+e);
			}
			catch(java.lang.IllegalAccessException e)
			{
				System.out.println("PropertyHolder defineMethods Exception "+e);
			}
			catch(Exception e)
			{
			}
		}
		public void propertyChange(PropertyChangeEvent evt)
		{
			if (!(evt.getSource() instanceof PropertyEditor))
			{
				return;
			}
			valueChanged = true;
			newValue = ((PropertyEditor) evt.getSource()).getValue();
			owner.propertyValueChanged(this);
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof JComboBox)
			{
				JComboBox comboBox = (JComboBox) e.getSource();
				pEditor.setAsText((String) comboBox.getSelectedItem());
			}
		}
		
		public void restoreOldValue()
		{
			writeValue(oldValue);
		}
		
		public void writeNewValue()
		{
			writeValue(newValue);
		}
		
		private void writeValue(Object value)
		{
			if (writeMethod != null && valueChanged)
			{
				try
				{
					args[0] = value;
					writeMethod.invoke(propertyOwner, args);
					valueChanged = false;
				}
				catch (InvocationTargetException ie)
				{
					if (ie.getTargetException() instanceof PropertyVetoException)
					{
						Toolkit.getDefaultToolkit().beep();
					}
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}

