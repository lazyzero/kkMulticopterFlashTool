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

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.gui.ControllerPanel;
import de.lazyzero.kkMulticopterFlashTool.utils.ButtonsStateListener;
import de.lazyzero.kkMulticopterFlashTool.utils.Firmware;
import de.lazyzero.kkMulticopterFlashTool.utils.Icons;
import de.lazyzero.kkMulticopterFlashTool.utils.XmlReaderFirmwares;

public class FirmwareRepositoryPanel extends JPanel implements ActionListener, PropertyChangeListener, ButtonsStateListener{

	private static final long serialVersionUID = 1L;
	private XmlReaderFirmwares firmwareLoader;
	private KKMulticopterFlashTool parent;
	private JComboBox firmwareCombobox;
	private JComboBox firmwareCategoryCombobox;
	private JButton reload;
	private JButton flash; 
	
	private Vector<Firmware> firmwares = new Vector<Firmware>();
	private TitledBorder firmwareBorder;
	private CellConstraints cc;
	private JLabel firmwareLabel;
	private Vector<String> categories = new Vector<String>();
	private JButton info;
	private Logger logger = KKMulticopterFlashTool.getLogger();
	private boolean buttonsEnabled = true;
	private int item;

	public FirmwareRepositoryPanel(KKMulticopterFlashTool parent,
			XmlReaderFirmwares firmwareLoader) {
		this.parent = parent;
		this.firmwareLoader = firmwareLoader;
		init();
		
		this.addPropertyChangeListener(this);
	}
	
	private void init() {
		//create the CellContraints
		cc  = new CellConstraints();
		
		// create the Layout for Panel this
		String panelColumns = "pref,3dlu,fill:pref:grow,3dlu,pref,3dlu,pref";
		String panelRows = "pref,3dlu,pref,3dlu,pref:grow,3dlu,top:pref:grow";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		JLabel helpLabel = new JLabel(_("firmware.help"));
		
		firmwareLabel = new JLabel(_("firmware"));
		firmwareLabel.setToolTipText(_("firmware.FirmwareCombobox.tip"));
		
		this.setBorder(new TitledBorder(_("firmware.settings")));
		
		this.categories.add(_("firmware.allCategories"));
		this.categories.addAll(Firmware.getCategories(firmwareLoader.getFirmwares()));
		firmwareCategoryCombobox = new JComboBox(categories);
		firmwareCategoryCombobox.setSelectedIndex(0);
		firmwareCategoryCombobox.addActionListener(this);
		firmwareCategoryCombobox.setToolTipText(_("firmware.CategoryCombobox.tip"));
		
		firmwareCombobox = new JComboBox();
		firmwareCombobox.setModel(new DefaultComboBoxModel());
		firmwareCombobox.setBorder(firmwareBorder);
		firmwareCombobox.setToolTipText(_("firmware.FirmwareCombobox.tip"));
		firmwareCombobox.addActionListener(this);
		firmwareCombobox.setRenderer(new FirmwareListCellRenderer());
		
		reload = new JButton(Icons.getIcon16(Icons.RELOAD));
		reload.setMnemonic(KeyEvent.VK_R);
		reload.addActionListener(this);
		reload.setToolTipText(_("reload.tip"));
		
		
		flash = new JButton(Icons.getIcon16(Icons.RUN));
		flash.setMnemonic(KeyEvent.VK_F);
		flash.addActionListener(this);
		flash.setEnabled(false);
		flash.setToolTipText(_("firmware.flash.tip"));
		
		
		info = new JButton(Icons.getIcon16(Icons.INFO));
		info.setMnemonic(KeyEvent.VK_I);
		info.addActionListener(this);
		info.setEnabled(false);
		info.setToolTipText(_("firmware.info.tip"));
		
		this.add(helpLabel, cc.xyw(1, 1, 7));
		
		this.add(firmwareCategoryCombobox, cc.xy(3,5));
		this.add(firmwareLabel, cc.xy(1, 7));
		this.add(firmwareCombobox, cc.xy(3,7));
		this.add(reload, cc.xy(5,5));
		this.add(flash, cc.xy(7,7));
		this.add(info, cc.xy(5,7));
	}
	
	@Override
	public void actionPerformed(ActionEvent action) {
		if (action.getSource().equals(firmwareCategoryCombobox)){
			item = firmwareCategoryCombobox.getSelectedIndex();
			parent.clearText();
			updateComboboxes();
		} else if (action.getSource().equals(reload)){
			parent.clearText();
			parent.println(_("messages.reloadList"));
			
			try {
				firmwareLoader.reloadXmlFile(firmwareLoader.getURL());
			} catch (Exception e) {
				parent.println(_("messages.reloadList.error"));
				e.printStackTrace();
				return;
			}
			updateComboboxes();
			
		} else if (action.getSource().equals(firmwareCombobox)) {
			try {
				if (!KKMulticopterFlashTool.isOfflineMode() && ((Firmware)firmwareCombobox.getSelectedItem()).hasCommentURL()){
					info.setEnabled(true);
				} else {
					info.setEnabled(false);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else if (action.getSource().equals(flash)){
			parent.clearText();
			
			if (KKMulticopterFlashTool.getCountdown() > 0) {
				countdown(KKMulticopterFlashTool.getCountdown());
			} else {
				flash();
			}
		} else if (action.getSource().equals(info)) {
			if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
				try {
					Desktop.getDesktop().browse(((Firmware)firmwareCombobox.getSelectedItem()).getCommentURL().toURI());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.log(Level.WARNING, e.getMessage());
				} 
			} else {
				JOptionPane.showMessageDialog(null, _("error.commentURL") + ((Firmware)firmwareCombobox.getSelectedItem()).getCommentURL().toExternalForm());
			}
		}
		
	}
	
	private void flash() {
		parent.println(_("messages.flashRepository"));
//		firmwareFile = ((Firmware)firmwareCombobox.getSelectedItem()).getFile(); 
		parent.setFirmware(((Firmware)firmwareCombobox.getSelectedItem()));
		if (parent.getController().getCaption().equals(KKMulticopterFlashTool.WIIESC_EEPROM)) {
			parent.flashEEprom(null);
		} else {
			parent.flashAVR();
		}
	}
	
	private void updateComboboxes() {
		String controllerName = parent.getController().getCaption();

		firmwares = firmwareLoader.getFirmwares(controllerName);
		
		firmwareCategoryCombobox.removeAllItems();
		this.categories = new Vector<String>();
		this.categories.add(_("firmware.allCategories"));
		this.categories.addAll(Firmware.getCategories(firmwares));
		for (int i = 0; i < categories.size(); i++) {
			firmwareCategoryCombobox.addItem(categories.get(i));
		}
		if (firmwareCategoryCombobox.getItemCount()>0){
			firmwareCategoryCombobox.setSelectedIndex(0);
		} 
		if (item > 0) {
			firmwareCategoryCombobox.setSelectedIndex(item);
		}
		
		if (KKMulticopterFlashTool.isOfflineMode()){
			firmwares = Firmware.getOfflineAvailableFirmwares(firmwares);
		}
		
		if (!this.firmwareCategoryCombobox.getSelectedItem().equals(_("firmware.allCategories"))) {
			firmwares = Firmware.filter((String)this.firmwareCategoryCombobox.getSelectedItem(), firmwares);
			}
		
		parent.println(_("messages.reloadList.done"));
		firmwareCombobox.removeAllItems();
		System.out.println("firmwares loaded: " + firmwares.size());
		for (int i = 0; i < firmwares.size(); i++) {
			firmwareCombobox.addItem(firmwares.get(i));
		}
		if (firmwareCombobox.getItemCount()>0){
			firmwareCombobox.setSelectedIndex(0);
			flash.setEnabled(true);
		} else {
			flash.setEnabled(false);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ControllerPanel.CONTROLLER_CHANGED)) {
			if (Boolean.valueOf(evt.getNewValue()+"") == false 
					&& Boolean.valueOf(evt.getOldValue()+"") == false) {
			} else {
				parent.clearText();
			}
			updateComboboxes();
		}
	}
	
	private void countdown(final int countdown) {
		logger.log(Level.INFO, "start Countdown from " + countdown);
		final JPanel glass = (JPanel) parent.getMainframe().getGlassPane();
	    glass.setLayout(new GridBagLayout());
	    
	    final JLabel number = new JLabel("countdown");
	    number.setForeground(Color.RED);
	    number.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 120));
	    number.setOpaque(false);
	    glass.add(number);
	    glass.setVisible(true);
	    
	    
	    Timer timer = new Timer();
        TimerTask task = new TimerTask() {
        	int count = countdown;
            @Override
            public void run() {
            	if (count == -1) {
            		this.cancel();
            		glass.setVisible(false);
            		flash();
            	}
            	glass.removeAll();
            	number.setText(count+"");
            	glass.add(number);
            	number.repaint();
    			logger.log(Level.INFO, count+"");
            	count--;
            }
        };
        timer.schedule(task, 0, 1000);
        try {
			timer.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		parent.getMainframe().repaint();
	}

	@Override
	public void setButtonsEnabled(boolean b) {
		this.buttonsEnabled = b;
	}

	@Override
	public void updateButtons() {
		flash.setEnabled(buttonsEnabled);
		reload.setEnabled(buttonsEnabled);
		//lock all other firmware eeprom buttons.
	}

}
