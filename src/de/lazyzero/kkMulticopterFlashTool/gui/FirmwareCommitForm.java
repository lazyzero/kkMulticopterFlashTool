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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;

import avr8_burn_o_mat.AVR;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.Firmware;
import de.lazyzero.kkMulticopterFlashTool.utils.FirmwareCommit;
import de.lazyzero.kkMulticopterFlashTool.utils.XMLwriter;
import de.lazyzero.kkMulticopterFlashTool.utils.XmlReaderFirmwares;

public class FirmwareCommitForm extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	private Dimension dimension = new Dimension(640, 540);
	private CellConstraints cc;
	private JLabel typeLabel;
	private XmlReaderFirmwares firmwareLoader;
	private Vector<String> types = new Vector<String>();
	private JComboBox firmwareTypesCombobox;
	private JLabel versionLabel;
	private JTextField versionField;
	private JLabel authorLabel;
	private JTextField authorField;
	private JLabel controllerLabel;
	private JComboBox controllerCombobox;
	private Vector<AVR>  avrs;
	private JLabel fileComment;
	private JLabel fileNameLabel;
	private JTextField fileNameField;
	private JLabel zipFileComment;
	private JLabel zipFileNameLabel;
	private JTextField zipFileNameField;
	private JTextArea comment;
	private JScrollPane commentPane;
	private Component commentLabel;
	private JLabel commentURLlabel;
	private JTextField commentURLField;
	private JLabel commentURLcomment;
	private JButton submitButton;
	private JButton cancelButton;

	
	

	public FirmwareCommitForm(XmlReaderFirmwares firmwareLoader){
		this.firmwareLoader = firmwareLoader;
		init();
		initGUI();
		this.setVisible(true);
	}

	private void init() {
		avrs = new Vector<AVR>();
		avrs.add(new AVR("ATmega48/P/PA","m48"));
		avrs.add(new AVR("ATmega88/P","m88"));
		avrs.add(new AVR("ATmega168/P/PA","m168"));
		avrs.add(new AVR("ATmega328P","m328p"));
	}

	private void initGUI() {
		JPanel panel = new JPanel();
		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,pref,3dlu,pref,3dlu,pref,3dlu,fill:pref:grow,3dlu,pref,3dlu,pref,3dlu";
		String panelRows = "3dlu,pref,3dlu,pref,3dlu,pref,6dlu,pref,3dlu,pref,6dlu,pref,6dlu,pref,3dlu,pref,3dlu,pref,6dlu,pref,3dlu,fill:pref,3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		panel.setLayout(panelLayout);
		
//		<firmware name="1 Sec Clock Test" value="1.0">
//		    <author name="Eric Flynn" />
//		    <controller name="m48" />
//		    <zipfile url="" />
//		    <file url="http://www.kkmulticopter.com/downloads/resources/KK_Clock_Test.hex" />
//		    <md5 value="" />
//	    </firmware>
		
		this.typeLabel = new JLabel(_("FirmwareCommitForm.typeLabel"));
		this.types.add(_("FirmwareCommitForm.selectType"));
		this.types.addAll(Firmware.getCategories(firmwareLoader.getFirmwares()));
		firmwareTypesCombobox = new JComboBox(types);
		firmwareTypesCombobox.setSelectedIndex(0);
		firmwareTypesCombobox.addActionListener(this);
		firmwareTypesCombobox.setEditable(true);
		if (System.getProperty("os.name").toLowerCase().contains("mac")){
			firmwareTypesCombobox.setRenderer(new ListCellRenderer() {
				
				@Override
				public Component getListCellRendererComponent(JList list, Object value,
						int index, boolean isSelected, boolean cellHasFocus) {
					DefaultListCellRenderer df = new DefaultListCellRenderer();
					JLabel r = (JLabel) df.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					r.setBackground(Color.LIGHT_GRAY);
					return r;
				} 
			});
		}
		
		this.versionLabel = new JLabel(_("FirmwareCommitForm.versionLabel"));
		this.versionField = new JTextField();
		this.versionField.setColumns(4);
		
		this.controllerLabel = new JLabel(_("FirmwareCommitForm.controllerLabel"));
		this.controllerCombobox = new JComboBox(avrs);
		this.controllerCombobox.setSelectedIndex(0);
		
		this.authorLabel = new JLabel(_("FirmwareCommitForm.authorLabel"));
		this.authorField = new JTextField();
		this.authorField.setColumns(14);
		
		this.fileComment = new JLabel(_("FirmwareCommitForm.fileComment"));
		this.fileNameLabel = new JLabel(_("FirmwareCommitForm.fileNameLabel"));
		this.fileNameField = new JTextField();
		this.fileNameField.setColumns(14);
		
		this.zipFileComment = new JLabel(_("FirmwareCommitForm.zipFileComment"));
		this.zipFileNameLabel = new JLabel(_("FirmwareCommitForm.zipFileNameLabel"));
		this.zipFileNameField = new JTextField();
		this.zipFileNameField.setColumns(14);
		
		this.commentURLcomment = new JLabel(_("FirmwareCommitForm.commentURLcomment"));
		this.commentURLlabel = new JLabel(_("FirmwareCommitForm.commentURLlabel"));
		this.commentURLField = new JTextField();
		this.commentURLField.setColumns(14);
		
		this.commentLabel = new JLabel(_("FirmwareCommitForm.commentLabel"));
		this.comment = new JTextArea();
		this.comment.setColumns(20);
		this.comment.setEditable(true);
		this.comment.setRows(11);
		this.commentPane = new JScrollPane(this.comment);
		this.commentPane.setAutoscrolls(true);
		this.commentPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		this.submitButton = new JButton(_("FirmwareCommitForm.submitButton"));
		this.submitButton.addActionListener(this);
		
		this.cancelButton = new JButton(_("FirmwareCommitForm.cancelButton"));
		this.cancelButton.addActionListener(this);
		
		this.add(panel);
		panel.add(typeLabel, cc.xy(2,2));
		panel.add(firmwareTypesCombobox, cc.xyw(4,2,9));
		
		panel.add(versionLabel, cc.xy(2, 4));
		panel.add(versionField, cc.xy(4, 4));
		panel.add(controllerLabel, cc.xy(6,4));
		panel.add(controllerCombobox, cc.xyw(8, 4, 5));
		
		panel.add(authorLabel, cc.xy(2, 6));
		panel.add(authorField, cc.xyw(4, 6, 9));
		
		panel.add(fileComment, cc.xyw(2,8,11));
		panel.add(fileNameLabel, cc.xy(2, 10));
		panel.add(fileNameField, cc.xyw(4, 10, 9));
		
		panel.add(zipFileComment, cc.xyw(2,12,11));
		panel.add(zipFileNameLabel, cc.xy(2, 14));
		panel.add(zipFileNameField, cc.xyw(4, 14, 9));
		
		panel.add(commentURLcomment, cc.xyw(2, 16, 11));
		panel.add(commentURLlabel, cc.xy(2, 18));
		panel.add(commentURLField, cc.xyw(4, 18, 9));
		
		panel.add(commentLabel, cc.xyw(2, 20, 11));
		panel.add(commentPane, cc.xyw(2, 22, 11));
		
		panel.add(submitButton, cc.xy(10, 24));
		panel.add(cancelButton, cc.xy(12, 24));
		
		this.setTitle(_("FirmwareCommitForm.title"));
		this.add(panel);
		this.setPreferredSize(dimension);
		this.setLocationRelativeTo(KKMulticopterFlashTool.getFrames()[0]);
		this.pack();
	}

	public void sendMail() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(cancelButton)) {
			this.dispose();
		} else if (event.getSource().equals(submitButton)) {
			FirmwareCommit fc = new FirmwareCommit(
					(String)firmwareTypesCombobox.getSelectedItem(), 
					versionField.getText(),
					(AVR) controllerCombobox.getSelectedItem(),
					authorField.getText(),
					fileNameField.getText(),
					zipFileNameField.getText(),
					commentURLField.getText(),
					comment.getText());
			XMLwriter xml = new XMLwriter(fc);
			xml.sendByEmail();
		}
	}
}
