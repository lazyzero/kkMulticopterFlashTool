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
package de.lazyzero.kkMulticopterFlashTool.gui.widgets;

import java.awt.Color;
import java.awt.event.FocusListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class JNamedIntMenuItem extends JPanel {

	private static final long serialVersionUID = 1L;
	private CellConstraints cc;
	private String label;
	private JLabel marker;
	private JFormattedTextField valueField;
	private MaskFormatter mf;
	private FocusListener listener;

	public JNamedIntMenuItem(String label, FocusListener listener) {
		this.label = label;
		this.listener = listener;
		init();
	}

	private void init() {
		//create the CellContraints
		cc  = new CellConstraints();
		
		// create the Layout for Panel this
		String panelColumns = "pref,3dlu,fill:pref:grow";
		String panelRows = "pref";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		this.marker = new JLabel(label+":");
		this.valueField = new JFormattedTextField();
		if (this.valueField.getBackground().equals(Color.BLACK)) {
			this.valueField.setBackground(Color.WHITE);
		}
		
		try {
		        mf = new MaskFormatter ("##");
		} catch (java.text.ParseException e) {
			
		}
		javax.swing.text.DefaultFormatterFactory dff = new  DefaultFormatterFactory(mf);
		valueField.setFormatterFactory(dff);
		valueField.addFocusListener(listener);
		
		this.add(marker, cc.xy(1, 1));
		this.add(valueField, cc.xy(3,1));
	}
	
	public void setValue(int value) {
		valueField.setText(value+"");
	}
	
	public int getValue() {
		return Integer.parseInt(valueField.getText().trim());
	}
}
