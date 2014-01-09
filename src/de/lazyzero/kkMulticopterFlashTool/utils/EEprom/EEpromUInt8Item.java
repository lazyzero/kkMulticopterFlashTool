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


public class EEpromUInt8Item extends EEpromDataItem {
	

	public EEpromUInt8Item(EEprom eeprom, String type, int startAddress,
			int length, int dataType, boolean isEditable,
			String label) {
		super(eeprom, type, startAddress, length, dataType, isEditable, label);
	}

	/**
	 * set's the value on the EEpromDataItem using a mapping or direct values.
	 * @param number
	 * @return true, if the Item was updated.
	 */
	public boolean setValue(int number) throws EEpromValueException{
		
		if (value.length == 1) {
			value[0] = (short)number;
		} else {
			new EEpromValueException(EEpromValueException.VALUE_TOO_MUCH_ELEMENTS);
			return false;
		}
		return true;
	}

	public int getValue() {
		return value[0];
	}
	

}
