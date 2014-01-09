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
package de.lazyzero.kkMulticopterFlashTool.RepositoryTool;

import java.util.Vector;

import javax.swing.JTable;

import de.lazyzero.kkMulticopterFlashTool.utils.Firmware;

public class FirmwareTablePanel extends JTable{

	private static final long serialVersionUID = 1L;
	private FirmwareTableModel tableModel;

	public FirmwareTablePanel(Vector<Firmware> firmwares) {
		
		this.tableModel = new FirmwareTableModel(firmwares);
		this.setModel(tableModel);
		
		this.setAutoCreateRowSorter(true);
        this.setFillsViewportHeight(true);
		
	}

}
