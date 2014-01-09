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

import java.util.Iterator;
import java.util.LinkedHashMap;


public class EEpromBooleanItem extends EEpromDataItem {
	
	public EEpromBooleanItem(EEprom eeprom, String type, int startAddress, int length, int dataType, LinkedHashMap<Short, String> dataMapping, String label) {
		super(eeprom, type, startAddress, length, dataType, dataMapping, label);
	}
	
	public boolean getValue() throws EEpromValueException{
		if (value.length == 1) {
			return Boolean.valueOf(dataMapping.get(value[0]));
		} else {
			new EEpromValueException(EEpromValueException.VALUE_TOO_MUCH_ELEMENTS);
			return false;
		}
		 
	}
	
	public boolean setValue(boolean bool) {
		if(dataMapping.containsValue(bool+"")){
			
			for (Iterator iterator = dataMapping.keySet().iterator(); iterator.hasNext();) {
				Short key = (Short) iterator.next();
				if (dataMapping.get(key).equals(bool+"")){
					value[0] = key;
					return true;
				}
			}
		} 

		return false;
	}

}
