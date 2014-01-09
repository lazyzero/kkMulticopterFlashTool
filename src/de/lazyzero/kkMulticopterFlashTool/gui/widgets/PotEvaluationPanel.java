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

public class PotEvaluationPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private CellConstraints cc;
	private JLabel adcRollPotLabel;
	private JLabel adcPitchPotLabel;
	private JTextField adcRollPotValue;
	private JLabel adcYawPotLabel;
	private JTextField adcPitchPotValue;
	private JTextField adcYawPotValue;

	public PotEvaluationPanel() {
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
		
		this.setBorder(new TitledBorder(_("PotEvaluationPanel.title")));
		adcRollPotLabel = new JLabel(_("PotEvaluationPanel.adcRollPot") + ":");
		adcPitchPotLabel = new JLabel(_("PotEvaluationPanel.adcPitchPot") + ":");
		adcYawPotLabel = new JLabel(_("PotEvaluationPanel.adcYawPot") + ":");
		
		adcRollPotValue = new JTextField();
		adcPitchPotValue = new JTextField();
		adcYawPotValue = new JTextField();
		
		adcRollPotValue.setEditable(false);
		adcPitchPotValue.setEditable(false);
		adcYawPotValue.setEditable(false);
		
		this.add(adcRollPotLabel, cc.xy(2, 2));
		this.add(adcRollPotValue, cc.xy(2, 4));
		this.add(adcPitchPotLabel, cc.xy(2, 6));
		this.add(adcPitchPotValue, cc.xy(2, 8));
		this.add(adcYawPotLabel, cc.xy(2, 10));
		this.add(adcYawPotValue, cc.xy(2, 12));
	}

	public void setRollPot(String roll) {
		adcRollPotValue.setText(roll);
	}

	public void setPitchPot(String pitch) {
		adcPitchPotValue.setText(pitch);
	}

	public void setYawPot(String yaw) {
		adcYawPotValue.setText(yaw);
	}
	
}
