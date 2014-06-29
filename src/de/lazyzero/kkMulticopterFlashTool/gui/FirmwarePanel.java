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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.FirmwareFilePanel;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.FirmwareRepositoryPanel;
import de.lazyzero.kkMulticopterFlashTool.utils.ButtonsStateListener;
import de.lazyzero.kkMulticopterFlashTool.utils.XmlReaderFirmwares;

public class FirmwarePanel extends JTabbedPane implements ButtonsStateListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	private XmlReaderFirmwares firmwareLoader;
	private KKMulticopterFlashTool parent;
	
	private boolean buttonsEnabled = true;
	private FirmwareFilePanel firmwarefilePanel;
	private FirmwareRepositoryPanel firmwareRepositoryPanel;

	public FirmwarePanel(KKMulticopterFlashTool parent,
			XmlReaderFirmwares firmwareLoader) {
		this.parent = parent;
		this.firmwareLoader = firmwareLoader;
		init();
		this.addPropertyChangeListener(this);
	}

	private void init() {
		firmwarefilePanel = new FirmwareFilePanel(parent);
		firmwareRepositoryPanel = new FirmwareRepositoryPanel(parent, firmwareLoader);
		
		this.addTab(_("firmwarepanel.repository"), firmwareRepositoryPanel);
		this.addTab(_("firmwarepanel.file"), firmwarefilePanel);
		
		this.setBorder(new TitledBorder(_("Flashing")+"..."));
		
		this.setSelectedIndex(0);
	}

	@Override
	public void setButtonsEnabled(boolean b) {
		this.buttonsEnabled = b;
	}

	@Override
	public void updateButtons() {
		firmwarefilePanel.setButtonsEnabled(this.buttonsEnabled);
		firmwareRepositoryPanel.setButtonsEnabled(this.buttonsEnabled);
		firmwarefilePanel.updateButtons();
		firmwareRepositoryPanel.updateButtons();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ControllerPanel.CONTROLLER_CHANGED)) {
			firmwareRepositoryPanel.firePropertyChange(ControllerPanel.CONTROLLER_CHANGED, 0, 1);
		}
	}

	public void setHexFile(File file, boolean clearContent) {
		firmwarefilePanel.setHexFile(file, clearContent);
		this.setSelectedIndex(this.indexOfTab(_("firmwarepanel.file")));
	}

}
