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

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import avr8_burn_o_mat.AvrdudeProgrammer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.MultiFlashSettings;
import de.lazyzero.kkMulticopterFlashTool.utils.PortScanner;

public class ProgrammerPanel extends JPanel implements ActionListener, FocusListener {

	private static final long serialVersionUID = 1L;
	public static final String PROGRAMMER_CHANGED = "PROGRAMMER_CHANGED";
	private JComboBox programmerCombobox;
	private AvrdudeProgrammer programmer;
	private Vector<AvrdudeProgrammer> programmers;
	private String port;
	private String rate;
	private KKMulticopterFlashTool parent;
	private CellConstraints cc;
	private JLabel programmerLabel;
	private JLabel portLabel;
	private JComboBox portComboBox;
	private JTextField baudRateTextField;
	private JLabel baudRateLabel;
	private JCheckBox baudRateChangeCB;
	private JCheckBox multiFlashCB;
	private Vector<String> availablePorts;
	private JButton multiFlashButton;
	private boolean multiFlash = false;
	private MultiFlashSettings mfs;
	
	
	public ProgrammerPanel(KKMulticopterFlashTool parent, Vector<AvrdudeProgrammer> programmers) {
		this.parent = parent;
		this.programmers = programmers;
		
		init();
	}
	
	private void init() {
		this.mfs = new MultiFlashSettings();
		
		//create the CellContraints
		cc  = new CellConstraints();
		
		// create the Layout for Panel this
		String panelColumns = "pref,3dlu,80dlu,3dlu,pref,3dlu,50dlu,3dlu,pref,fill:pref:grow,3dlu,pref";
		String panelRows = "pref,3dlu,pref";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		
		this.setBorder(new TitledBorder(_("programmer.settings")));
		
		programmerLabel = new JLabel(_("programmer"));
		programmerCombobox = new JComboBox(programmers);
		
		if (programmer!=null){
			programmerCombobox.setSelectedItem(programmer);
		} else {
			programmerCombobox.setSelectedIndex(0);
			programmer = (AvrdudeProgrammer)programmerCombobox.getSelectedItem();
			parent.setProgrammer(programmer);
		}
		programmerCombobox.addActionListener(this);
		
		portLabel = new JLabel(_("port"));
		availablePorts = PortScanner.listProgrammerPorts();
		portComboBox = new JComboBox(availablePorts);
		updateSerialPorts();
//		portComboBox.addFocusListener(this);
		portComboBox.addActionListener(this);
//		portComboBox.setEditable(true);
		

		baudRateChangeCB = new JCheckBox(_("port.enableRate"), true);
		baudRateChangeCB.addActionListener(this);
		
		baudRateLabel = new JLabel(_("port.rate"));
		baudRateTextField = new JTextField(rate);
		baudRateTextField.addFocusListener(this);
		if (!baudRateChangeCB.isSelected() && baudRateChangeCB.isEnabled()) {
			baudRateTextField.setEnabled(true);
		} else {
			baudRateTextField.setEnabled(false);
		}
		
		multiFlashCB = new JCheckBox(_("multiflash.enable"), false);
		multiFlashCB.addActionListener(this);
		
		multiFlashButton = new JButton(_("multiflash.configure"));
		multiFlashButton.addActionListener(this);
		multiFlashButton.setEnabled(false);
		
		this.add(programmerLabel,cc.xy(1, 1));
		this.add(programmerCombobox, cc.xyw(3, 1, 10));
		this.add(portLabel, cc.xy(1, 3));
		this.add(portComboBox, cc.xy(3, 3));
		this.add(baudRateLabel, cc.xy(5, 3));
		this.add(baudRateTextField, cc.xy(7, 3));
		this.add(baudRateChangeCB, cc.xy(9, 3));
		this.add(multiFlashCB, cc.xy(10, 3));
		this.add(multiFlashButton, cc.xy(12, 3));
		
	}
	
	private void updateSerialPorts() {
		Thread t = new Thread() {
			public void run() {
				String selectedItem;
				Vector<String> list;
				
				while(true) {
					selectedItem = (String)portComboBox.getSelectedItem();
					list = PortScanner.listProgrammerPorts();
					
					for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
						String port = iterator.next();
						if (!availablePorts.contains(port)) {
							System.out.println("add port to list: " + port);
							availablePorts.add(port);
							portComboBox.setModel(new DefaultComboBoxModel(availablePorts));
							portComboBox.setSelectedItem(selectedItem);
						}
					}
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}
	
	private void openMultiFlashConfigDialog() {
		MultiFlashConfigDialog mfd = new MultiFlashConfigDialog(this.mfs);
		mfd.setModalityType(ModalityType.APPLICATION_MODAL);
		mfd.setVisible(true);
	}

	public JPanel getPanel() {
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		if (action.getSource().equals(programmerCombobox)){
		    programmer = (AvrdudeProgrammer)programmerCombobox.getSelectedItem();
		    parent.setProgrammer(programmer);
		    parent.firePropertyChange(PROGRAMMER_CHANGED, 0, 1);
			System.out.println("Programmer switched to: "+programmer.getType());
			System.out.println(programmer.getId());
			System.out.println(programmer.getType());
			System.out.println(programmer.getDesc());
			if (programmer.getId().equals("arduinousblinker")) {
				multiFlashCB.setEnabled(true);
				multiFlashButton.setEnabled(multiFlashCB.isSelected());
				setMultiFlash(multiFlashCB.isSelected());
			} else {
				multiFlashButton.setEnabled(false);
				multiFlashCB.setEnabled(false);
				setMultiFlash(false);
			}
		} else if (action.getSource().equals(baudRateChangeCB)) {
			if (!baudRateChangeCB.isSelected() && baudRateChangeCB.isEnabled()) {
				baudRateTextField.setEnabled(true);
			} else {
				baudRateTextField.setEnabled(false);
			}
		} else if (action.getSource().equals(portComboBox)) {
			port=(String)portComboBox.getSelectedItem();
			if (!port.equals("usb")) {
				baudRateChangeCB.setEnabled(true);
			} else {
				baudRateChangeCB.setEnabled(false);
			}
			if (!baudRateChangeCB.isSelected() && baudRateChangeCB.isEnabled()) {
				baudRateTextField.setEnabled(true);
			} else {
				baudRateTextField.setEnabled(false);
			}
			System.out.println("port is now: " + port);
		} else if (action.getSource().equals(multiFlashCB)) {
			multiFlashButton.setEnabled(multiFlashCB.isSelected());
			setMultiFlash(multiFlashCB.isSelected());
		} else if (action.getSource().equals(multiFlashButton)) {
			openMultiFlashConfigDialog();
		}
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void focusLost(FocusEvent focus) {
		if (focus.getSource().equals(portComboBox)) {
			port=(String)portComboBox.getSelectedItem();
			if (!port.equals("usb")) {
				baudRateChangeCB.setEnabled(true);
			} else {
				baudRateChangeCB.setEnabled(false);
			}
			if (!baudRateChangeCB.isSelected() && baudRateChangeCB.isEnabled()) {
				baudRateTextField.setEnabled(true);
			} else {
				baudRateTextField.setEnabled(false);
			}
			System.out.println("port is now: " + port);
		} else if (focus.getSource().equals(baudRateTextField)) {
			rate=baudRateTextField.getText();
			System.out.println("rate is now: " + rate);
		}
	}

	public void setPort(String port) {
		this.port = port.trim();
		this.portComboBox.setSelectedItem(port);
		if (!port.equals("usb")) {
			baudRateChangeCB.setEnabled(true);
		} else {
			baudRateChangeCB.setEnabled(false);
		}
		System.out.println("Port is set to: " + port);
	}

	public String getPort() {
		return port;
	}
	
	public void setRate(String rate) {
		this.rate = rate.trim();
		this.baudRateTextField.setText(this.rate);
		System.out.println("Rate is set to: " + rate);
	}

	public String getRate() {
		return rate;
	}
	
	public void setDefaultRate(boolean b) {
		baudRateChangeCB.setSelected(b);
		if (!baudRateChangeCB.isSelected() && baudRateChangeCB.isEnabled()) {
			baudRateTextField.setEnabled(true);
		} else {
			baudRateTextField.setEnabled(false);
		}
	}
	
	private void setMultiFlash(boolean selected) {
		this.multiFlash  = selected;
	}
	
	public boolean isMultiFlash() {
		return this.multiFlash;
	}
	
	public boolean isDefaultRate() {
		return baudRateChangeCB.isSelected();
	}
	
	public boolean useBaudRate() {
		if (!baudRateChangeCB.isSelected() && baudRateChangeCB.isEnabled()) {
			return true;
		} else {
			return false;
		}
	}

	public void setProgrammer(String id) {
		Iterator<AvrdudeProgrammer> iter = programmers.iterator();
		for (; iter.hasNext();) {
			AvrdudeProgrammer progger = iter.next();
			if (progger.getId().equals(id)){
				programmer = progger;
				parent.setProgrammer(programmer);
				programmerCombobox.setSelectedItem(programmer);
			}
		}
	}
	
	

}
