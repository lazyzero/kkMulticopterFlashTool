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
package de.lazyzero.kkMulticopterFlashTool.gui.widgets.FirmwareSettings;

import static lu.tudor.santec.i18n.Translatrix._;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.EEpromFirmwareSettings;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.EEpromItemPanel;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEpromBooleanItem;

public class KKQuadSettings extends EEpromFirmwareSettings {

	Logger logger = KKMulticopterFlashTool.getLogger();
	private EEpromBooleanItem rollGyro;
	private EEpromBooleanItem nickGyro;
	private EEpromBooleanItem yawGyro;
	private EEpromBooleanItem potDirection;

	public KKQuadSettings(KKMulticopterFlashTool parent) {
		super(parent);
	}

	@Override
	public String toString() {
		return "KapteinKuk Quadrocopter \u22644.7";
	}

	@Override
	protected JPanel addEEpromDataItems() {
		readEEprom(this);
		
		JPanel settingsPanel = new JPanel();
		LinkedHashMap<Short, String> dataMapping = new LinkedHashMap<Short, String>();
		dataMapping.put((short) 127, "true");
		dataMapping.put((short) 255, "false");
		
		wait4EEprom();
		
		// create the CellContraints
		CellConstraints cc = new CellConstraints();
		
		// create the Layout for Panel this
		String panelColumns = "fill:pref:grow,3dlu,fill:pref:grow,3dlu,fill:pref:grow";
		String panelRows = "pref,3dlu,pref";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		settingsPanel.setLayout(panelLayout);
		
		if (eeprom != null) {
			rollGyro = new EEpromBooleanItem(eeprom, "Roll gyro", 0, 1, EEpromBooleanItem.BOOLEAN, dataMapping , "inverted");
			nickGyro = new EEpromBooleanItem(eeprom, "Nick gyro", 1, 1, EEpromBooleanItem.BOOLEAN, dataMapping , "inverted");
			yawGyro = new EEpromBooleanItem(eeprom, "Yaw gyro", 2, 1, EEpromBooleanItem.BOOLEAN, dataMapping , "inverted");
			
			potDirection = new EEpromBooleanItem(eeprom, "Pot direction", 3, 1, EEpromBooleanItem.BOOLEAN, dataMapping , "inverted");
			
			settingsPanel.add(new EEpromItemPanel(rollGyro), cc.xy(1, 1));
			settingsPanel.add(new EEpromItemPanel(nickGyro), cc.xy(3, 1));
			settingsPanel.add(new EEpromItemPanel(yawGyro), cc.xy(5, 1));
			settingsPanel.add(new EEpromItemPanel(potDirection), cc.xy(1, 3));
		} else {
			settingsPanel.add(new JLabel(_("EEPromSettingsPanel.failed2read")), cc.xyw(1, 1, 5));
		}
		
		return settingsPanel;
	}

	@Override
	public void flashSettings() {
		if (rollGyro != null && nickGyro != null && yawGyro != null) {
			logger.info("Write back settings to eeprom object.");
			eeprom = rollGyro.updateEEprom(eeprom);
			eeprom = nickGyro.updateEEprom(eeprom);
			eeprom = yawGyro.updateEEprom(eeprom);
			eeprom = potDirection.updateEEprom(eeprom);
			
			writeEEprom(this);
		}
	}


}
