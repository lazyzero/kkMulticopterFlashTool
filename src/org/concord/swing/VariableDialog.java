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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.concord.swing.event.VariableListener;

public class VariableDialog
extends JDialog
implements ActionListener, ListSelectionListener
{
    protected JList variableList = new JList();
    protected JTextArea variableComment = new JTextArea();
    protected JButton doneButton = new JButton("Done");
    protected JButton addButton = new JButton("Add");
    protected JButton modifyButton = new JButton("Modify");
    protected JButton removeButton = new JButton("Remove");
    protected JPanel controlPanel = new JPanel();
    protected JPanel listPanel = new JPanel();
    protected JScrollPane commentScroll = new JScrollPane(variableComment);
    protected JScrollPane listScroll = new JScrollPane(variableList);
    protected ModifyValueDialog modifyDialog;
    protected Properties prefixTable = new Properties();
    protected JPanel contentPane;
    protected String selectedItem;
    protected Map variables;
    protected boolean javaVariable = true;
	protected VariableErrorDialog errorDialog;
	protected final String COMMENT = ".comment";
    
    public VariableDialog(JFrame frame, String title, Map variables)
    {
        super(frame, title, false);
        modifyDialog = createModifyValueDialog(frame);
		errorDialog = new VariableErrorDialog(frame);
		setVariables(variables);
        initialize();	
    }
    
    public VariableDialog(JFrame frame, String title)
    {
    	this(frame, title, null);
    }
    
    public ModifyValueDialog createModifyValueDialog(JFrame frame)
    {
        return new ModifyValueDialog(frame, "Edit value");
    }
    
	public static boolean isVariable(Object object)
	{
		if (object instanceof String)
		{
			String value = (String) object;
			int n = value.length();
			if (n == 0)
				return false;
			if (! Character.isJavaIdentifierStart(value.charAt(0)))
				return false;
			for (int i = 1; i < n; i++)
			{
				if (! Character.isJavaIdentifierPart(value.charAt(i)))
					return false;
			}
			return true;
		}
		return false;
	}
	
    public void initialize()
    {
        controlPanel.setLayout(new GridLayout(0, 1));
        controlPanel.add(doneButton);
        controlPanel.add(addButton);
        controlPanel.add(modifyButton);
        controlPanel.add(removeButton);
        
        listPanel.setLayout(new GridLayout(1, 0));
        listPanel.add(listScroll);
        listPanel.add(commentScroll);

        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(listPanel, "Center");
        contentPane.add(controlPanel, "East");

        doneButton.addActionListener(this);
        addButton.addActionListener(this);
        modifyButton.addActionListener(this);
        removeButton.addActionListener(this);
        
        removeButton.setEnabled(false);
        
		variableComment.setLineWrap(true);
		variableComment.setWrapStyleWord(true);
        variableList.addListSelectionListener(this);
        setLocation(200, 200);
        pack();
        setResizable(false);
    }
    
    public boolean isJavaVariable()
    {
    	return javaVariable;
    }
    
    public void setJavaVariable(boolean value)
    {
    	javaVariable = value;
    }
    
    public void setVariables(Map variables)
    {
        this.variables = variables;
    }
	
	protected Vector getVariableList(Map varTable)
	{
		Vector variableList = new Vector();
		Set keySet = varTable.keySet();
		Iterator keys = keySet.iterator();
		while (keys.hasNext())
		{
			Object key = keys.next();
			if ((! javaVariable) || isVariable(key))
				variableList.add(key);
		}
		return variableList;
	}
    
    public void show()
    {
    	if (variables instanceof Map)
    	{
    		Vector list = getVariableList(variables);
            variableList.setListData(list);
            selectedItem = (String) variableList.getSelectedValue();
            if ((selectedItem instanceof String) && variables.containsKey(selectedItem))
            {
                variableList.setSelectedValue(selectedItem, true);
                modifyButton.setEnabled(true);
            }
            else
            {
                modifyButton.setEnabled(false);
            }
            super.show();
    	}
    }
	
	public boolean showSelectedVariable(String varName)
	{
    	if (variables instanceof Map)
    	{
            variableList.setListData(getVariableList(variables));
    		ListModel listModel = variableList.getModel();
    		for (int i = 0; i < listModel.getSize(); i++)
    		{
    			Object listElement = listModel.getElementAt(i);
    			if (varName.equals((String) listElement))
    			{
    				variableList.setSelectedValue(listElement, true);
    				modifyButton.setEnabled(true);
    				super.show();
    				return true;
    			}
    		}
    	}
		return false;
	}
	
	public void addVariableListener(VariableListener listener)
	{
		modifyDialog.addVariableListener(listener);
	}
	
	public void removeVariableListener(VariableListener listener)
	{
		modifyDialog.removeVariableListener(listener);
	}
	
	public void addVariableValue(String variable, Object value)
	{
    	if (variables instanceof Map)
    	{
    		boolean varExists = false;
    		while (true)
    		{
    			modifyDialog.setModify(false);
    			modifyDialog.setVariable(variable);
    			modifyDialog.setValue(value);
    			modifyDialog.show();
    			variable = modifyDialog.getVariable();
    			value = modifyDialog.getValue();
    			varExists = variables.containsKey(variable);
    			if ((variable instanceof String) && varExists)
    			{
    				errorDialog.setBounds(getBounds());
    				errorDialog.setErrorText("Variable (" + variable + ") already exists.\nChange the variable name or Cancel.");
    				continue;
    			}
    			break;
    		}
            if ((variable instanceof String) && (variable.length() > 0))
            {
    			variables.put(variable, modifyDialog.getValue());
                modifyButton.setEnabled(true);
                removeButton.setEnabled(true);
            }
    		if (variable.length() == 0)
    			variable = null;
    		selectedItem = variable;
    	}
	}
    
    public void actionPerformed(ActionEvent event)
    {
    	if (variables instanceof Map)
    	{
            Object source = event.getSource();
            JButton button = (source instanceof JButton) ? (JButton) source : null;
            if (button == doneButton)
            {
                setVisible(false);
            }
            else if (button == addButton)
            {
    			addVariableValue(modifyDialog.getVariable(), null);
            }
            else if (button == modifyButton)
            {
    			boolean varExists = variables.containsKey(selectedItem);
                if ((selectedItem instanceof String) && varExists)
                {
    				String variable = selectedItem;
    				Object value = variables.get(variable);
    				modifyDialog.setVariable(variable);
    				modifyDialog.setValue(value);
    				modifyDialog.setModify(true);
    				modifyDialog.show();
    				variable = modifyDialog.getVariable();
    				if ((variable instanceof String) && (variable.length() > 0))
    				{
    					variables.put(variable, modifyDialog.getValue());
    				}
    				selectedItem = variable;
    			}
            }
            else if (button == removeButton)
            {
                if (selectedItem instanceof String)
                {
                    variables.remove(selectedItem);
                    selectedItem = null;
                }
            }
            setSelected(selectedItem);
    	}
    }
    
    public void setSelected(String item)
    {
    	if (variables instanceof Map)
    	{
            variableList.removeListSelectionListener(this);
            variableList.setListData(getVariableList(variables));
            if ((selectedItem instanceof String) && variables.containsKey(item))
            {
                variableList.setSelectedValue(item, true);
            }
            variableList.addListSelectionListener(this);
    	}
    }
    
    protected void updateVariableComment(String oldItem, String newItem)
    {
    	String oldItemComment = oldItem + COMMENT;
    	String newItemComment = newItem + COMMENT;
		String comment = variableComment.getText();
		comment = (comment == null) ? "" : comment.trim();
		if (comment.length() > 0)
			variables.put(oldItemComment, comment);
		else
			variables.remove(oldItemComment);
		comment = (String) variables.get(newItemComment);
		variableComment.setText(comment == null ? "" : comment);
    }
    
    public void setSelectedItem(String item)
    {
    	if (variables instanceof Map)
    	{
            removeButton.setEnabled(item instanceof String);
            if (selectedItem instanceof String)
            {
            	// System.out.println("selectedItem = " + selectedItem + " item = " + item);
            	if (selectedItem == item)
            	{
            		String comment = (String) variables.get(item + COMMENT);
            		variableComment.setText((comment == null) ? "" : comment);
	                modifyDialog.setVariable(item);
	                boolean varExists = (item instanceof String) && variables.containsKey(item);
	                if (varExists)
	                {
	                    modifyDialog.setValue(variables.get(item));
	                    modifyButton.setEnabled(true);
	                }
            	}
            	else
            	{
            		updateVariableComment(selectedItem, item);
            	}
            }
            else
            {
                modifyButton.setEnabled(false);
            }
            selectedItem = item;
    	}
    }

    public void valueChanged(ListSelectionEvent event)
    {
        Object object = event.getSource();
        if (object instanceof JList)
        {
            JList list = (JList) object;
            setSelectedItem((String) list.getSelectedValue());
        }
    }
	
	public class VariableErrorDialog
	extends JDialog
	implements ActionListener
	{
		protected JTextArea errorTextArea = new JTextArea();
		protected JButton continueButton = new JButton("Continue");
		protected JPanel buttonPanel = new JPanel();
		
		public VariableErrorDialog(JFrame owner)
		{
			super(owner, "Variable Error", true);
			errorTextArea.setEditable(false);
			errorTextArea.setLineWrap(true);
			errorTextArea.setWrapStyleWord(true);
			errorTextArea.setBackground(buttonPanel.getBackground());
			getContentPane().setLayout(new BorderLayout());
			buttonPanel.add(continueButton);
			getContentPane().add(buttonPanel, "South");
			getContentPane().add(errorTextArea, "Center");
			continueButton.addActionListener(this);
			setSize(300, 150);
			setResizable(false);
		}
		
		public void setErrorText(String errorText)
		{
			errorTextArea.setText(errorText);
			this.show();
		}
		
		public void actionPerformed(ActionEvent event)
		{
			this.hide();
		}
	}
}

