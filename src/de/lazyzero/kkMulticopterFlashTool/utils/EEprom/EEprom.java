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

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Logger;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

public class EEprom {
	
	static boolean DEBUG = false;
	Logger logger = KKMulticopterFlashTool.getLogger();
	private Vector<Short> eeprom = new Vector<Short>();
	
	private String ifileName = KKMulticopterFlashTool.getTempFolder() + "tmp_i_eeprom.txt";
	private String ofileName = KKMulticopterFlashTool.getTempFolder() + "tmp_o_eeprom.txt";
	
	public EEprom() {
		readEEprom(this.ifileName);
	}

	public EEprom(String ifileName) {
		this.ifileName = ifileName;
		readEEprom(this.ifileName);
	}
	
	public EEprom(int size) {
		for (int i = 0; i < size; i++) {
			short s = Short.decode("0xff");
			eeprom.add(s);
			if (DEBUG) System.out.println(i + " : " + s + " : " + String.format("0x%02x", s));
		}
	}

	private void readEEprom(String ifileName) {
		String rom = new String();
		try {
			 FileInputStream fs = new FileInputStream(ifileName);
			 DataInputStream in = new DataInputStream(fs);
//			 BufferedReader br = new BufferedReader(new InputStreamReader(in));
			 int s;
			 while ((s = in.read()) != -1)   {
				 if (DEBUG) System.out.println(s);
				 rom = rom.concat(s + ",");
			 }
			 in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		decodeRawEEprom(rom);
	}
	
	/**
	 * writes the eeprom content to the default eeprom file in temp folder as RAW
	 */
	public void writeRawEEprom() {
		writeRawEEprom(ofileName);
	}
	
	/**
	 * writes the eeprom content to the default eeprom file in temp folder as HEX
	 */
	public void writeEEprom() {
		writeEEprom(ofileName);
	}
	
	private void writeEEprom(String ofileName) {
		try {
			FileOutputStream fos = new FileOutputStream(ofileName);
			
			String cells = encodeEEprom();
			
			fos.write(cells.getBytes());
			fos.write(System.getProperty("line.separator").getBytes());
			
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			 
	}
	
	private void writeRawEEprom(String ofileName) {
		try {
			FileOutputStream fos = new FileOutputStream(ofileName);
			
			byte[] cells = encodeRawEEprom();
			
			fos.write(cells);
			fos.write(System.getProperty("line.separator").getBytes());
			
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			 
	}

	private void decodeRawEEprom(String rom) {
		logger.info("decode raw eeprom");
		eeprom = new Vector<Short>();
		String[] cells = rom.split(",");
		System.out.println("cells: " + cells.length);
		for (int i = 0; i < cells.length; i++) {
			short s = Short.decode(cells[i]);
			eeprom.add(s);
			if (DEBUG) System.out.println(i + " : " + s + " : " + String.format("0x%02x", s));
		}
		System.out.println();
	}
	
	private void decodeEEprom(String rom) {
		eeprom = new Vector<Short>();
		String[] cells = rom.split(",");
		for (int i = 0; i < cells.length; i++) {
			short s = Short.decode(cells[i]);
			eeprom.add(s);
			if (DEBUG) System.out.println(i + " : " + s + " : " + String.format("0x%02x", s));
		}
	}
	
	private byte[] encodeRawEEprom() {
		byte[] cells = new byte[eeprom.size()];
		for (int i = 0; i < eeprom.size(); i++) {
			cells[i] =(byte)(eeprom.get(i) & 0xff);
		}
		return cells;
	}
	
	private String encodeEEprom() {
		String cells = "";
		for (int i = 0; i < eeprom.size(); i++) {
			cells = cells.concat(String.format("0x%02x", eeprom.get(i)));
			if (i < eeprom.size()-1) {
				cells = cells.concat(",");
			}
		}
		return cells;
	}
	
	public Short[] getCells(int startAddress, int length) {
		Short[] s = new Short[length];
		
		for (int i = 0; i < length; i++) {
			s[i] = eeprom.get(i+startAddress);
		}
		return s;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DEBUG = true;
		
		EEprom eeprom = new EEprom();
//		EEprom eeprom = new EEprom("./eeprom_readout/eeprom.txt");
//		eeprom.readEEprom("./eeprom_readout/eeprom.txt");
		
//		System.out.println(eeprom.encodeEEprom());
//		
//		System.out.println(eeprom.eeprom.size());
//		eeprom.writeEEprom("./eeprom_readout/o_eeprom.txt");
		
		
		LinkedHashMap<Short, String> dataMapping = new LinkedHashMap<Short, String>();
		dataMapping.put((short) 127, "true");
		dataMapping.put((short) 255, "false");
		
		EEpromBooleanItem eeDataItem = new EEpromBooleanItem(eeprom, "Yaw gyro", 0, 1, EEpromBooleanItem.BOOLEAN, dataMapping , "invert");
		try {
			System.out.println(eeDataItem.getValue());
		} catch (EEpromValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getOutputFile() {
		return ofileName;
	}

	public static String getDefaultInputFile() {
		return KKMulticopterFlashTool.getTempFolder() + "tmp_i_eeprom.txt";
	}

	public void setCell(int index, Short value) {
		eeprom.set(index, value);
	}

	public static String getDefaultOutputFile() {
		return KKMulticopterFlashTool.getTempFolder() + "tmp_o_eeprom.txt";
	}
	

	
}
