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

import java.awt.Color;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.MultiFlashSettings;
import static lu.tudor.santec.i18n.Translatrix._;

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
	private MultiFlashSettings mfs;
	private JSpinner nbrESCSpinner;
	private JLabel nbrESCLabel;
	
	public MultiFlashConfigDialog(MultiFlashSettings mfs) {
		super();
		this.mfs = mfs;
		initGUI();
		
	}

	private void initGUI() {
		//create the CellContraints
		cc  = new CellConstraints();
		
		// create the Layout for Panel this
		String panelColumns = "3dlu,pref,3dlu,50dlu,3dlu,pref,fill:pref:grow,3dlu,pref";
		String panelRows = "3dlu,pref,3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
				
		panel = new FormDebugPanel();
		panel.setLayout(panelLayout);
		
		panel.add(new JLabel("TEST"), cc.xy(1, 1));
		
		
		this.nbrESCLabel = new JLabel(_("multiflash.configure.nbrESC"));
		SpinnerModel model = new SpinnerNumberModel(mfs.getNbrESC(), //initial value
		                               2, //min
		                               8, //max
		                               1);  
		this.nbrESCSpinner = new JSpinner(model);
		
		
		this.setTitle(_("multiflash.configure.title"));
		panel.add(nbrESCLabel, cc.xy(2, 2));
		panel.add(nbrESCSpinner, cc.xy(4, 2));
		this.add(panel);
		this.pack();
		this.setLocationRelativeTo(KKMulticopterFlashTool.getInstance());
	}

}
