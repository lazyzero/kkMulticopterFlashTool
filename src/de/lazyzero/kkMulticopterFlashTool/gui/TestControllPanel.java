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
import gnu.io.PortInUseException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import avr8_burn_o_mat.AVR;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.Icons;
import de.lazyzero.kkMulticopterFlashTool.utils.PortScanner;
import de.lazyzero.kkMulticopterFlashTool.utils.SerialReader;

public class TestControllPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private Logger logger = KKMulticopterFlashTool.getLogger();
	
	private Vector<AVR> avrs;
	private Vector<String> ports = new Vector<String>();
	
	private CellConstraints cc;
	private JLabel portLabel;
	private JLabel controllerLabel;
	private JComboBox portComboBox;
	private JComboBox controllerComboBox;
	private JButton startButton;
	private JButton stopButton;
	private int baud = 9600;
	private SerialReader serialReader;
	private TestPanel parent;


	public TestControllPanel(TestPanel parent) {
		this.parent = parent;
		init();
		intiGUI();
		updateSerialPorts();
	}

	private void intiGUI() {
		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,pref, 3dlu, fill:pref:grow,3dlu, pref, 3dlu,pref,3dlu";
		String panelRows = "3dlu,pref,3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		this.setBorder(new TitledBorder(_("controller.settings")));

		portLabel = new JLabel(_("port"));
		controllerLabel = new JLabel(_("controller"));
		
		portComboBox = new JComboBox(ports);
		controllerComboBox = new JComboBox(avrs);
		
		startButton = new JButton(_("start"), Icons.getIcon16(Icons.RUN));
		stopButton = new JButton(_("stop"), Icons.getIcon16(Icons.STOP));
		
		startButton.addActionListener(this);
		stopButton.addActionListener(this);
		
		this.add(controllerLabel, cc.xy(2, 2));
		this.add(portLabel, cc.xy(2, 4));
		this.add(controllerComboBox, cc.xyw(4, 2, 5));
		this.add(portComboBox, cc.xy(4, 4));
		this.add(startButton, cc.xy(6, 4));
		this.add(stopButton, cc.xy(8, 4));
		
		stopButton.setEnabled(false);
		startButton.setEnabled(true);
	}

	private void init() {
		avrs = KKMulticopterFlashTool.getControllers();
		ports  = PortScanner.listSerialPorts();
		
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(startButton)) {
			try {
				startAction();
			} catch (PortInUseException e) {
				JOptionPane.showMessageDialog(this, _("serialPort.portInUseException"));
				logger.log(Level.WARNING, e.getMessage());
			}
		} else if (event.getSource().equals(stopButton)) {
			stopAction();
		}
	}

	private void stopAction() {
		logger.info("stop serialReader");
		stopButton.setEnabled(false);
		startButton.setEnabled(true);
		serialReader.interrupt();
	}

	private void startAction() throws PortInUseException {
		logger.info("start serialReader");
		stopButton.setEnabled(true);
		startButton.setEnabled(false);
		
		serialReader = new SerialReader(baud, (String)portComboBox.getSelectedItem(), parent.getEvaluationPanel());
		serialReader.start();
		
	}
	
	private void updateSerialPorts() {
		Thread t = new Thread() {
			public void run() {
				String selectedItem;
				Vector<String> list;
				
				while(true) {
					selectedItem = (String)portComboBox.getSelectedItem();
					list = PortScanner.listSerialPorts();
					
					for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
						String port = iterator.next();
						if (!ports.contains(port)) {
							System.out.println("add port to list: " + port);
							ports.add(port);
							portComboBox.setModel(new DefaultComboBoxModel(ports));
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

}
