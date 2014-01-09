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
package de.lazyzero.kkMulticopterFlashTool.gui;

import static lu.tudor.santec.i18n.Translatrix._;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import avr8_burn_o_mat.AVR;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

public class ControllerPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	public static final String CONTROLLER_CHANGED = "controllerChanged";
	private JComboBox controllerCombobox;
	private AVR controller;
	private Vector<AVR> controllers = new Vector<AVR>();
	private KKMulticopterFlashTool parent;
	private CellConstraints cc;
	private JLabel controllerLabel;
	private JCheckBox forceCheckbox;
	

	public ControllerPanel(KKMulticopterFlashTool parent, Vector<AVR> controllers) {
		this.parent = parent;
		this.controllers = controllers;
		
		init();
	}

	private void init() {
		//create the CellContraints
		cc  = new CellConstraints();
		
		// create the Layout for Panel this
		String panelColumns = "pref,3dlu,fill:pref, 3dlu, pref";
		String panelRows = "pref";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		
		this.setBorder(new TitledBorder(_("controller.settings")));
	
		controllerLabel = new JLabel(_("controller"));
		controllerCombobox = new JComboBox(controllers);
		controllerCombobox.setSelectedIndex(0);
		controllerCombobox.setToolTipText(_("controller.tooltip"));
		controller = (AVR)controllerCombobox.getSelectedItem();
		parent.setController(controller);
		controllerCombobox.addActionListener(this);
		forceCheckbox = new JCheckBox(_("controller.force"), false);
		forceCheckbox.addActionListener(this);
		
		this.add(controllerLabel, cc.xy(1, 1));
		this.add(controllerCombobox, cc.xy(3, 1));
//		this.add(forceCheckbox, cc.xy(5, 1));
	}

	@Override
	public void actionPerformed(ActionEvent action) {

		if (action.getSource().equals(controllerCombobox)){
		    controller = (AVR)controllerCombobox.getSelectedItem();
		    parent.setController(controller);
			System.out.println("Controller switched to: "+controller.toString());
			parent.firePropertyChange(CONTROLLER_CHANGED, 0, 1);
		}
		if (action.getSource().equals(forceCheckbox)) {
			parent.setForceFlashing(forceCheckbox.isSelected());
		}
	}

	public void setController(String name) {
		Iterator<AVR> iter = controllers.iterator();
		for (; iter.hasNext();) {
			AVR avr = iter.next();
			if (avr.getName().equals(name)){
				controller = avr;
				parent.setController(controller);
				controllerCombobox.setSelectedItem(controller);
				parent.firePropertyChange(CONTROLLER_CHANGED, 0, 1);
			}
		}
	}

}
