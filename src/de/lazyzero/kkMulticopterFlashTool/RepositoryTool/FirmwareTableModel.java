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

import java.util.Iterator;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import de.lazyzero.kkMulticopterFlashTool.utils.Firmware;

public class FirmwareTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	private String[] colNames = {"Type", "Name", "Version", "Author", "Server", "Controller", "Target",
			"Flash-Options", "Features", "Zip URL", "File URL", "MD5", "Comment URL"};
	private Object[][] data;
	private int rowCount;

	public FirmwareTableModel(Vector<Firmware> firmwares) {
		rowCount = firmwares.size();
		data = new Object[firmwares.size()][15];
		
		int row = 0;
		int col=0;
		for (Iterator<Firmware> iterator = firmwares.iterator(); iterator.hasNext();) {
			col = 0;
			Firmware firmware = iterator.next();
			data[row][col++] = firmware.getName();
			data[row][col++] = firmware.getVersionName();
			data[row][col++] = firmware.getVersion();
			data[row][col++] = firmware.getAuthor();
			data[row][col++] = firmware.getServer();
			data[row][col++] = firmware.getController();
			data[row][col++] = firmware.getTarget();
			data[row][col++] = firmware.getAdditionalOptions();
			data[row][col++] = firmware.getFeatures();
			data[row][col++] = firmware.getZipURL();
			data[row][col++] = firmware.getFileURL();
			
			data[row][col++] = firmware.getMD5();
			data[row][col++] = firmware.getCommentURL();
			
			row++;
		}
	}

	

	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public String getColumnName(int col) {
		return colNames[col].toString();
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return data[row][col];
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

}
