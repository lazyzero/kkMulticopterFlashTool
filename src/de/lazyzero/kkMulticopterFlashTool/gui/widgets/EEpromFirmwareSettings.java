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

import java.util.logging.Logger;

import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEprom;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEpromListener;

public abstract class EEpromFirmwareSettings implements EEpromListener{

	private Logger logger = KKMulticopterFlashTool.getLogger();
	protected JPanel panel;
	private KKMulticopterFlashTool parent;
	protected EEprom eeprom;
	private CellConstraints panelCC;
	private boolean hasSettingsLoaded = false;
	
	
	public EEpromFirmwareSettings(KKMulticopterFlashTool parent) {
		this.parent = parent;
	}
	public abstract String toString();
	protected abstract JPanel addEEpromDataItems();
	public abstract void flashSettings();
	
	


	public JPanel getPanel() {
		panel = new JPanel();
		
		// create the CellContraints
		panelCC = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,pref:grow,3dlu,pref,3dlu";
		String panelRows = "3dlu,pref,3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		panel.setLayout(panelLayout);
		
		panel.add(addEEpromDataItems(), panelCC.xyw(2, 4, 3));
		panel.revalidate();
		
		return panel;
	}
	
	protected void readEEprom(EEpromListener listener) {
		//TODO Disable all
		parent.readEEprom(listener);
		//TODO Enable all
	}
	
	protected void writeEEprom(EEpromListener listener) {
		parent.flashEEprom(listener);
	}
	
	public void EEpromState(int state) {
		logger.info(state + " :EEPROM read. Building GUI for KK Quad");
		if (state == EEpromListener.READ){
			hasSettingsLoaded = true;
			eeprom = new EEprom();
		} else if (state == EEpromListener.FAILED) {
			hasSettingsLoaded = false;
			eeprom = null;
		} else if (state == EEpromListener.FLASH_FAILED) {
			parent.setSelectedTabIndex(0);
		}
	}
	
	protected void wait4EEprom() {
		int timeout = 10;
		while(timeout != 0){
			if (eeprom != null) break;
			try {
				logger.info("timeout = " + timeout);
				Thread.sleep(300);
				timeout--;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return if the eeprom was read or failed
	 */
	public boolean hasSettingsLoaded() {
		return hasSettingsLoaded ;
	}
	
}
