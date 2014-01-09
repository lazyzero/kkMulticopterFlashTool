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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.EEpromFirmwareSettings;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.FirmwareSettings.KKQuadSettings;
import de.lazyzero.kkMulticopterFlashTool.utils.Icons;

public class EEpromSettingsPanel extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	private CellConstraints cc;
	private JButton load;
	private JButton save;
	private JComboBox firmwares;
	private KKMulticopterFlashTool parent;
	private JPanel settings;

	public EEpromSettingsPanel(KKMulticopterFlashTool parent) {
		this.parent = parent;
		initGUI(0);
	}
	
	private void initGUI(int selectionIndex) {
		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,fill:pref:grow,3dlu,pref,3dlu";
		String panelRows = "3dlu,pref,3dlu,pref,3dlu,top:122dlu,3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		this.setBorder(new TitledBorder(_("EEPromSettingsPanel.title")));
		
		//Combox for available firmwares.
		firmwares = new JComboBox();
		firmwares.addItem(_("EEPromSettingsPanel.makeSelection"));
		firmwares.addItem(new KKQuadSettings(parent));
		firmwares.setSelectedIndex(selectionIndex);
		firmwares.addActionListener(this);
		
		//Load button
		load = new JButton(_("EEPromSettingsPanel.load"), Icons.getIcon16(Icons.LOADEEPROM));
		load.setEnabled(false);
		load.addActionListener(this);
		
		//save button
		save = new JButton(_("EEPromSettingsPanel.save"), Icons.getIcon16(Icons.RUN));
		save.setEnabled(false);
		save.addActionListener(this);
		
		this.add(new JLabel(_("EEPromSettingsPanel.warning"), Icons.getIcon48(Icons.ABOUT), JLabel.CENTER), cc.xyw(2, 2, 3));
		this.add(firmwares, cc.xy(2, 4));
		this.add(load, cc.xy(4, 4));
		this.add(save, cc.xy(4, 8));
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean hasSettingsLoaded = false;
		if (e.getSource().equals(load)) {
			if (firmwares.getSelectedIndex() > 0) {
				this.settings = null;
				this.removeAll();
				this.initGUI(firmwares.getSelectedIndex());
				settings = ((EEpromFirmwareSettings)firmwares.getSelectedItem()).getPanel();
				hasSettingsLoaded = ((EEpromFirmwareSettings)firmwares.getSelectedItem()).hasSettingsLoaded();
				this.add(settings, cc.xyw(2, 6, 3));
				this.validate();
				if (hasSettingsLoaded) save.setEnabled(true);
				load.setEnabled(true);
			} else {
				save.setEnabled(false);
				load.setEnabled(true);
			}
		} else if (e.getSource().equals(save)) {
			((EEpromFirmwareSettings)firmwares.getSelectedItem()).flashSettings();
		} else if (e.getSource().equals(firmwares)) {
			if (firmwares.getSelectedIndex() > 0) {
				load.setEnabled(true);
			} else {
				load.setEnabled(false);
			}
		}
	}
	
	
}
