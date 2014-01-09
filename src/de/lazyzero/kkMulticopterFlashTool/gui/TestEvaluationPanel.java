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
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.gui.widgets.GyroEvaluationPanel;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.PotEvaluationPanel;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.ReceiverEvaluationPanel;
import de.lazyzero.kkMulticopterFlashTool.utils.Icons;
import de.lazyzero.kkMulticopterFlashTool.utils.SendLogFile;

public class TestEvaluationPanel extends JPanel implements ActionListener, SeriealListener{

	private static final long serialVersionUID = 1L;
	private CellConstraints cc;
	private JLabel testFirmwareLabel;
	private JTextField testFirmwareVersionTextField;
	private JLabel authorLabel;
	private JTextField authorTextField;
	private JLabel submitDescriptionLabel;
	private JButton submitButton;
	private JLabel instructionLabel;
	private JButton instructionButton;
	private ReceiverEvaluationPanel receiverEvaluationPanel;
	private PotEvaluationPanel potEvaluationPanel;
	private GyroEvaluationPanel gyroEvaluationPanel;
	private LinkedHashMap<String, String> data;

	public TestEvaluationPanel() {
		initGUI();
	}
	
	private void initGUI() {
		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,pref,3dlu,pref,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu";
		String panelRows = "3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,fill:pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		this.setBorder(new TitledBorder(_("TestEvaluationPanel.title")));
		
		testFirmwareLabel = new JLabel(_("TestEvaluationPanel.testFirmwareLabel") + ":");
		testFirmwareVersionTextField = new JTextField();
		testFirmwareVersionTextField.setEditable(false);
		testFirmwareVersionTextField.setHorizontalAlignment(JTextField.RIGHT);
		
		authorLabel = new JLabel(_("TestEvaluationPanel.authorLabel") + ":");
		authorTextField = new JTextField();
		authorTextField.setEditable(false);
		authorTextField.setHorizontalAlignment(JTextField.RIGHT);
		
		submitDescriptionLabel = new JLabel(_("TestEvaluationPanel.submitDescriptionLabel"));
		submitButton = new JButton(_("submit"), Icons.getIcon16(Icons.MAIL));
		submitButton.addActionListener(this);
		
		instructionLabel = new JLabel(_("TestEvaluationPanel.instructionLabel"));
		instructionButton = new JButton(_("TestEvaluationPanel.instructionButton"), Icons.getIcon16(Icons.INFO));
		instructionButton.addActionListener(this);
		
		receiverEvaluationPanel = new ReceiverEvaluationPanel();
		potEvaluationPanel = new PotEvaluationPanel();
		gyroEvaluationPanel = new GyroEvaluationPanel();
		
		this.add(testFirmwareLabel, cc.xy(2, 2));
		this.add(testFirmwareVersionTextField, cc.xy(4, 2));
		this.add(authorLabel, cc.xy(2, 4));
		this.add(authorTextField, cc.xy(4, 4));
		this.add(submitDescriptionLabel, cc.xyw(2, 6, 3));
		this.add(submitButton, cc.xy(4, 8));
		this.add(instructionLabel, cc.xyw(2, 10, 3));
		this.add(instructionButton, cc.xy(4, 12));
		this.add(receiverEvaluationPanel, cc.xywh(6, 2, 1, 13));
		this.add(potEvaluationPanel, cc.xywh(8, 2, 1, 13));
		this.add(gyroEvaluationPanel, cc.xywh(10, 2, 1, 13));
	}


	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(instructionButton)) {
			kkMenu.openURL("http://www.lazyzero.de/en/modellbau/kkmulticopterflashtool/manual#hardware_test");
		} else if (event.getSource().equals(submitButton)) {
			SendLogFile slf = new SendLogFile();
			if (data != null) {
				slf.sendMail(data);
			} else {
				JOptionPane.showMessageDialog(this, _("TestEvaluationPanel.measureFirstMessage"));
			}
		}
	}

	@Override
	public void dataReceived(LinkedHashMap<String, String> data) {
		this.data = data;
		Set<String> keys = data.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			if (key.equals("Author")) authorTextField.setText(data.get(key).replace("_", " "));
			else if (key.equals("version")) testFirmwareVersionTextField.setText(data.get(key));
			else if (key.equals("rxRoll")) receiverEvaluationPanel.setRoll(data.get(key));
			else if (key.equals("rxPitch")) receiverEvaluationPanel.setPitch(data.get(key));
			else if (key.equals("rxColl")) receiverEvaluationPanel.setThrottle(data.get(key));
			else if (key.equals("rxYaw")) receiverEvaluationPanel.setYaw(data.get(key));
			else if (key.equals("adcRollPot")) potEvaluationPanel.setRollPot(data.get(key));
			else if (key.equals("adcPitchPot")) potEvaluationPanel.setPitchPot(data.get(key));
			else if (key.equals("adcYawPot")) potEvaluationPanel.setYawPot(data.get(key));
			else if (key.equals("adcRollGyro")) gyroEvaluationPanel.setRollGyro(data.get(key));
			else if (key.equals("adcPitchGyro")) gyroEvaluationPanel.setPitchGyro(data.get(key));
			else if (key.equals("adcYawGyro")) gyroEvaluationPanel.setYawGyro(data.get(key));
			else System.out.println(key + "==" + data.get(key));
		}
	}
}
