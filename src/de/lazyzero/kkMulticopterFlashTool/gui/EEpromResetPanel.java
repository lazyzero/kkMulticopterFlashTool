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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEprom;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEpromListener;

public class EEpromResetPanel extends JPanel implements ActionListener, PropertyChangeListener, EEpromListener{
	
	private static final long serialVersionUID = 1L;
	private KKMulticopterFlashTool parent;
	private CellConstraints cc;
	private Logger logger = KKMulticopterFlashTool.getLogger();
	private EEprom eeprom;
	private JLabel warning;
	private JButton resetButton;
	
	public EEpromResetPanel(KKMulticopterFlashTool parent) {
		this.parent = parent;
		
		init();
		this.addPropertyChangeListener(this);
	}

	private void init() {
		//create the CellContraints
		cc  = new CellConstraints();
		
		// create the Layout for Panel this
		String panelColumns = "3dlu,fill:pref:grow,3dlu,pref,3dlu";
		String panelRows = "pref";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		this.setBorder(new TitledBorder(_("EEpromResetPanel.title")));
		
		this.warning = new JLabel(_("EEpromResetPanel.warning"));
		this.resetButton = new JButton(_("EEpromResetPanel.resetButton"));
		this.resetButton.addActionListener(this);
		
		this.add(warning, cc.xy(2, 1));
		this.add(resetButton, cc.xy(4, 1));
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ControllerPanel.CONTROLLER_CHANGED)) {
			this.eeprom = new EEprom(parent.getController().getEepromSize());
			logger.info("EEprom size set to: " + parent.getController().getEepromSize());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(resetButton)){
			logger.info("RESET the EEprom!!!");
			parent.clearText();
			eeprom.writeRawEEprom();
			//TODO Disable all
			parent.flashEEprom(this);
			
			//TODO Enable all
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void EEpromState(int state) {
		if (state < 0) {
			parent.setSelectedTabIndex(0);
		}
	}
}
