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
package de.lazyzero.kkMulticopterFlashTool.utils;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Should work for all ATmega 168 and 328 based Arduino
 * 
 * @author Christian Moll
 *
 */
public class ArduinoUSBLinker {

	public static String D2 = "D2";
	public static String D3 = "D3";
	public static String D4 = "D4";
	public static String D5 = "D5";
	public static String D6 = "D6";
	public static String D7 = "D7";
	public static String D8 = "D8";
	public static String D9 = "D9";
	public static String D10 = "D10";
	public static String D11 = "D11";
	public static String D12 = "D12";
	public static String D13 = "D13";
	
	public static String A0 = "A0";
	public static String A1 = "A1";
	public static String A2 = "A2";
	public static String A3 = "A3";
	public static String A4 = "A4";
	public static String A5 = "A5";
	
	private static String command_base = "$M<";
	private static String command_pin = command_base + "P";
	private static String command_signalRate = command_base + "B";
	private static String command_baudrate = command_base + "R";
	private static String command_write2EEPROM = command_base + "W";
	
	private int default_baud = 19200;
	private int baud = default_baud;
	
	private int default_signalrate = 32;
	private int signalrate = default_signalrate;
	
	private SerialWriter serialWriter;
	
	public static LinkedHashMap<String, Integer> pins = new LinkedHashMap<String, Integer>();

	public ArduinoUSBLinker() {
		init();
		initPins();
	}
	
	private void init() {
		pins.put( D2, 18);
		pins.put( D3, 19);
		pins.put( D4, 20);
		pins.put( D5, 21);
		pins.put( D6, 22);
		pins.put( D7, 23);
		pins.put( D8,  0);
		pins.put( D9,  1);
		pins.put(D10,  2);
		pins.put(D11,  3);
		pins.put(D12,  4);
		pins.put(D13,  5);
		pins.put( A0,  8);
		pins.put( A1,  9);
		pins.put( A2, 10);
		pins.put( A3, 11);
		pins.put( A4, 12);
		pins.put( A5, 13);
	}
	
	/**
	 * Function switch thru all pins to init them as programming pins. This will keep attached ESC in bootloader.
	 */
	private void initPins() {
		for (Integer pin : pins.values()) {
			try {
				serialWriter.sendCommand(command_pin + pin);
				serialWriter.sendCommand(command_write2EEPROM);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setPin(String pin) throws IOException {
		serialWriter.sendCommand(command_pin + pins.get(pin));
		serialWriter.sendCommand(command_write2EEPROM);
	}

	public SerialWriter getSerialWriter() {
		return serialWriter;
	}
	
	public void setSerialWriter(SerialWriter serialWriter) {
		this.serialWriter = serialWriter;
	}

	public void setBaud(int baud) throws IOException {
		serialWriter.sendCommand(command_baudrate + baud);
		serialWriter.sendCommand(command_write2EEPROM);
		this.baud = baud;
	}

	public int getBaud() {
		return baud;
	}

	public void setSignalrate(int signalrate) throws IOException {
		if (signalrate > 4 && signalrate < 136) {
			serialWriter.sendCommand(command_signalRate + signalrate);
			serialWriter.sendCommand(command_write2EEPROM);
			this.signalrate = signalrate;
			return;
		} 
		this.signalrate = default_signalrate;
	}
	
	public int getSignalrate() {
		return signalrate;
	}
}
