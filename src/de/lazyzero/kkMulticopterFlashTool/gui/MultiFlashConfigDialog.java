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

import javax.swing.JDialog;
import javax.swing.JLabel;

import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

/**
 * @author Christian Moll
 *
 */
public class MultiFlashConfigDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FormDebugPanel panel;
	private CellConstraints cc;
	
	public MultiFlashConfigDialog() {
		super();
		initGUI();
		
	}

	private void initGUI() {
		//create the CellContraints
		cc  = new CellConstraints();
		
		// create the Layout for Panel this
		String panelColumns = "pref,3dlu,80dlu,3dlu,pref,3dlu,50dlu,3dlu,pref,fill:pref:grow,3dlu,pref";
		String panelRows = "pref,3dlu,pref";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		
		panel = new FormDebugPanel();
		panel.setLayout(panelLayout);
		
		panel.add(new JLabel("TEST"), cc.xy(1, 1));
		
		this.add(panel);
		this.pack();
		this.setLocationRelativeTo(KKMulticopterFlashTool.getInstance());
	}

}
