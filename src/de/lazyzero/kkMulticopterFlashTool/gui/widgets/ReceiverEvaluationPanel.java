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

public class ReceiverEvaluationPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private CellConstraints cc;

	private JLabel rxRollLabel;

	private JLabel rxPitchLabel;

	private JLabel rxThrottleLabel;

	private JLabel rxYawLabel;

	private JTextField rxRollValue;

	private JTextField rxPitchValue;

	private JTextField rxThrottleValue;

	private JTextField rxYawValue;

	public ReceiverEvaluationPanel() {
		initGUI();
	}
	
	private void initGUI() {
		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,pref:grow,3dlu";
		String panelRows = "3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		this.setBorder(new TitledBorder(_("ReceiverEvaluationPanel.title")));
		rxRollLabel = new JLabel(_("ReceiverEvaluationPanel.rxRollLabel") + ":");
		rxPitchLabel = new JLabel(_("ReceiverEvaluationPanel.rxPitchLabel") + ":");
		rxThrottleLabel = new JLabel(_("ReceiverEvaluationPanel.rxThrottleLabel") + ":");
		rxYawLabel = new JLabel(_("ReceiverEvaluationPanel.rxYawLabel") + ":");
		
		rxRollValue = new JTextField();
		rxPitchValue = new JTextField();
		rxThrottleValue = new JTextField();
		rxYawValue = new JTextField();
		
		rxRollValue.setEditable(false);
		rxPitchValue.setEditable(false);
		rxThrottleValue.setEditable(false);
		rxYawValue.setEditable(false);
		
		this.add(rxRollLabel, cc.xy(2, 2));
		this.add(rxRollValue, cc.xy(2, 4));
		this.add(rxPitchLabel, cc.xy(2, 6));
		this.add(rxPitchValue, cc.xy(2, 8));
		this.add(rxThrottleLabel, cc.xy(2, 10));
		this.add(rxThrottleValue, cc.xy(2, 12));
		this.add(rxYawLabel, cc.xy(2, 14));
		this.add(rxYawValue, cc.xy(2, 16));
	}

	public void setRoll(String roll) {
		rxRollValue.setText(roll);
	}

	public void setPitch(String pitch) {
		rxPitchValue.setText(pitch);
	}

	public void setThrottle(String throttle) {
		rxThrottleValue.setText(throttle);
	}

	public void setYaw(String yaw) {
		rxYawValue.setText(yaw);
	}
	
	
}
