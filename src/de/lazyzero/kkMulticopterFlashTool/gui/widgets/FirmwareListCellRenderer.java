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
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.Firmware;

public class FirmwareListCellRenderer implements ListCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private boolean hasPot;
	private boolean hasLCD;
	private boolean hasPIEZO;
	private boolean hasMEMS;
	private boolean hasACC;
	private boolean hasCPPM;
	private boolean hasLVA;

	@Override
    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus)
    {
		JPanel panel = new JPanel();
		// create the CellContraints
		CellConstraints cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,fill:pref:grow";
		String panelRows = "1dlu,pref:grow,2dlu,pref:grow,2dlu,pref:grow,2dlu,pref:grow,1dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		panel.setLayout(panelLayout);
		
    	if (value == null) {
			value = _("firmware.FirmwareCombobox.nonAvailable");
		}
    	Color bg = isSelected ? list.getSelectionBackground() : list.getBackground().brighter();
    	Color fg = Color.black;
    	if (value instanceof Firmware) setRequirementsAndFeatures((Firmware)value);
    	panel.setBackground(bg);
  		if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
  			fg = isSelected ? list.getSelectionForeground() : list.getForeground();
  			panel.setForeground(fg);
  		}
  		
  		panel.setFont(list.getFont());
    	if (value instanceof Firmware) {
    		Firmware f = ((Firmware)value);
    		JLabel line1;
    		if (f.getController().equals(KKMulticopterFlashTool.WIIESC) || f.getController().equals(KKMulticopterFlashTool.WIIESC_EEPROM)){
    			line1 = new JLabel(f.getName() +  " " + f.getVersion() + " " + f.getVersionName() + " by " + f.getAuthor());
    		} else {
    			line1 = new JLabel(f.getName() +  " V" + f.getVersion() + " " + f.getVersionName() + " by " + f.getAuthor());
    		}
    		line1.setForeground(fg);
    		JLabel line2 = new JLabel(f.getServer());
    		line2.setFont(line2.getFont().deriveFont(line2.getFont().getSize2D()-2));
    		line2.setForeground(fg);
    		panel.add(line1, cc.xy(2, 2));
    		panel.add(line2, cc.xy(2, 4));
    		if (f.getTarget() == Firmware.TARGET_KK) {
    			JPanel line3 = getLine(f, fg, bg);
    			panel.add(line3, cc.xy(2, 6));
    		}
    		if (f.getTarget() == Firmware.TARGET_OPENAERO) {
    			JPanel line3 = getLine(f, fg, bg);
    			JLabel line4 = new JLabel(f.getFileName());
    			line4.setFont(line4.getFont().deriveFont(line4.getFont().getSize2D()-2));
        		line4.setForeground(fg);
        		panel.add(line3, cc.xy(2, 6));
    			panel.add(line4, cc.xy(2, 8));
    		}
    		if (f.getTarget() == Firmware.TARGET_RX3S) {
    			JPanel line3 = getLine(f, fg, bg);
    			panel.add(line3, cc.xy(2, 6));
    		}
    	} else {
    		JLabel warning = new JLabel((String) value);
    		warning.setForeground(fg);
    		panel.add(warning, cc.xy(2, 2));
    	}
    	
    	return panel;
    }
	
	
	
	private JPanel getLine(Firmware firmware, Color fg, Color bg) {
		int target = firmware.getTarget();
		JPanel panel = new JPanel();
		// create the CellContraints
		CellConstraints cc = new CellConstraints();

		// create the Layout for Panel this
		
		String panelRows = "pref";
		
		if  (target == Firmware.TARGET_KK || target == Firmware.TARGET_OPENAERO) {
			String panelColumns = "pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref,3dlu,pref";
			FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
			panel.setLayout(panelLayout);
			panel.setForeground(fg);
			panel.setBackground(bg);
			panel.add(new ToggledLable("pot", hasPot, fg, bg), cc.xy(1, 1));
			panel.add(new ToggledLable("LCD", hasLCD, fg, bg), cc.xy(3, 1));
			panel.add(new ToggledLable("piezo", hasPIEZO, fg, bg), cc.xy(5, 1));
			panel.add(new ToggledLable("MEMS", hasMEMS, fg, bg), cc.xy(7, 1));
			panel.add(new ToggledLable("acc", hasACC, fg, bg), cc.xy(9, 1));
			panel.add(new ToggledLable("CPPM", hasCPPM, fg, bg), cc.xy(11, 1));
			panel.add(new ToggledLable("LVA", hasLVA, fg, bg), cc.xy(13, 1));
		} else if  (target == Firmware.TARGET_RX3S) {
			String panelColumns = "pref";
			FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
			panel.setLayout(panelLayout);
			panel.setForeground(fg);
			panel.setBackground(bg);
			panel.add(new ToggledLable(_("flightstab.voltageWarning"), true, fg, bg), cc.xy(1, 1));
		} else {
			
		}
		panel.setOpaque(true);
		return panel;
	}

	private void setRequirementsAndFeatures(Firmware firmware) {
		int features = firmware.getFeatures();
		int target = firmware.getTarget();
		
		if (target == Firmware.TARGET_KK || target == Firmware.TARGET_OPENAERO) {
			if (features != -1) {
				hasPot = (features & Firmware.POT) != 0;
				hasLCD = (features & Firmware.LCD) != 0;
				hasPIEZO = (features & Firmware.PIEZO) != 0;
				hasMEMS = (features & Firmware.MEMS) != 0;
				hasACC = (features & Firmware.ACC) != 0;
				hasCPPM = (features & Firmware.CPPM) != 0;
				hasLVA = (features & Firmware.LVA) != 0;
			} else {
				hasPot = false;
				hasLCD = false;
				hasPIEZO = false;
				hasMEMS = false;
				hasACC = false;
				hasCPPM = false;
				hasLVA = false;
			}
		} else if (target == Firmware.TARGET_ESC) {
			
		}
	}
	
	
}
