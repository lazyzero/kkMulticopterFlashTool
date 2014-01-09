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
package de.lazyzero.kkMulticopterFlashTool.utils.EEprom;

import java.util.LinkedHashMap;

public abstract class EEpromDataItem {
	public static int BOOLEAN = 0;
	public static int INTEGER = 1;
	
	protected String type = "";
	protected int startAddress = 0;
	protected int length = 1;
	protected String label = "";
	protected int dataType = 0;
	protected Short[] value;
	protected LinkedHashMap<Short, String> dataMapping;
	protected boolean isEditable;
	
	public EEpromDataItem (EEprom eeprom, String type, int startAddress, int length, int dataType, LinkedHashMap<Short, String> dataMapping, String label) {
			setType(type);
			setStartAddress(startAddress);
			setLength(length);
			setDataType(dataType);
			setDataMapping(dataMapping);
			setLabel(label);
			setValues(eeprom);
			setEditable(true);
		}
	
	
	


	public EEpromDataItem(EEprom eeprom, String type, int startAddress,
			int length, int dataType, boolean isEditable, String label) {
		this(eeprom, type, startAddress, length, dataType, null, label);
		setEditable(isEditable);
	}
	
	private void setEditable(boolean editable) {
		isEditable = editable;
	}

	protected void setValues(EEprom eeprom) {
		value = eeprom.getCells(startAddress, length);
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getStartAddress() {
		return startAddress;
	}
	public void setStartAddress(int startAddress) {
		this.startAddress = startAddress;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	protected void setDataMapping(LinkedHashMap<Short, String> dataMapping) {
		this.dataMapping = dataMapping;
	}
	
	public EEprom updateEEprom(EEprom eeprom) {
		for (int i = 0; i < value.length; i++) {
			eeprom.setCell(i+startAddress, value[i]);
		}
		eeprom.writeRawEEprom();
		return eeprom;
	}
	
	

}
