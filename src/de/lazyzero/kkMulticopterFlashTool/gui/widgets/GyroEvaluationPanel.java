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

import static lu.tudor.santec.i18n.Translatrix._;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class GyroEvaluationPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private CellConstraints cc;
	private JLabel adcRollGyroLabel;
	private JLabel adcPitchGyroLabel;
	private JLabel adcYawGyroLabel;
	private JTextField adcRollGyroValue;
	private JTextField adcPitchGyroValue;
	private JTextField adcYawGyroValue;

	public GyroEvaluationPanel() {
		initGUI();
	}
	
	private void initGUI() {
		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,pref:grow,3dlu";
		String panelRows = "3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		this.setBorder(new TitledBorder(_("GyroEvaluationPanel.title")));
		adcRollGyroLabel = new JLabel(_("GyroEvaluationPanel.adcRollGyro") + ":");
		adcPitchGyroLabel = new JLabel(_("GyroEvaluationPanel.adcPitchGyro") + ":");
		adcYawGyroLabel = new JLabel(_("GyroEvaluationPanel.adcYawGyro") + ":");
		
		adcRollGyroValue = new JTextField();
		adcPitchGyroValue = new JTextField();
		adcYawGyroValue = new JTextField();
		
		adcRollGyroValue.setEditable(false);
		adcPitchGyroValue.setEditable(false);
		adcYawGyroValue.setEditable(false);
		
		this.add(adcRollGyroLabel, cc.xy(2, 2));
		this.add(adcRollGyroValue, cc.xy(2, 4));
		this.add(adcPitchGyroLabel, cc.xy(2, 6));
		this.add(adcPitchGyroValue, cc.xy(2, 8));
		this.add(adcYawGyroLabel, cc.xy(2, 10));
		this.add(adcYawGyroValue, cc.xy(2, 12));
	}

	public void setRollGyro(String roll) {
		adcRollGyroValue.setText(roll);
	}

	public void setPitchGyro(String pitch) {
		adcPitchGyroValue.setText(pitch);
	}

	public void setYawGyro(String yaw) {
		adcYawGyroValue.setText(yaw);
	}
	
}
