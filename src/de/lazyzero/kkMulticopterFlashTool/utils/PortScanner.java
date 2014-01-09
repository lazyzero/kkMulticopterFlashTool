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

import gnu.io.CommPortIdentifier;

import java.util.Vector;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

/**
 * @author Christian Moll
 *
 */
public class PortScanner {
	
//	static {
//		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM1");
//		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyUSB0");
//	}
	/**
	 * @return List of all serial ports + the usb port
	 */
	public static Vector<String> listProgrammerPorts(){
		Vector<String> ports = new Vector<String>();
		ports.add("usb");
		ports.addAll(listSerialPorts());
		ports.addAll(listParallelPorts());
		return ports;
	}
	
	
	 /**
	 * @return List all serial ports
	 */
	public static Vector<String> listSerialPorts()
	    {
		 	Vector<String> ports = new Vector<String>();
		 	if (KKMulticopterFlashTool.ENABLE_PORT_CHECK) {
		 		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		 		while ( portEnum.hasMoreElements() ) 
		 		{
		 			CommPortIdentifier portIdentifier = portEnum.nextElement();
		 			if (portIdentifier.getPortType()==CommPortIdentifier.PORT_SERIAL){
		 				if (System.getProperty("os.name").toLowerCase().contains("mac")) {
		 					if (portIdentifier.getName().contains("cu")){
		 						ports.add(portIdentifier.getName());
		 					}
		 				} else {
		 					ports.add(portIdentifier.getName());
		 				}
		 			}
		 		} 
		 	}
	        return ports;
	    }
	    
	 /**
	 * @return List all parallel ports
	 */
	public static Vector<String> listParallelPorts()
	    {
		 	Vector<String> ports = new Vector<String>();
		 	
		 	if (KKMulticopterFlashTool.ENABLE_PORT_CHECK) {
		 		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		 		while ( portEnum.hasMoreElements() ) 
		 		{
		 			CommPortIdentifier portIdentifier = portEnum.nextElement();
		 			if (portIdentifier.getPortType()==CommPortIdentifier.PORT_PARALLEL){
//	            	if (System.getProperty("os.name").toLowerCase().contains("mac")) {
//	            		if (portIdentifier.getName().contains("cu")){
//	            			ports.add(portIdentifier.getName());
//	            		}
//	            	} else {
		 				if (System.getProperty("os.name").toLowerCase().contains("windows")) {
		 					ports.add(portIdentifier.getName().toLowerCase());
		 				} else {
		 					ports.add(portIdentifier.getName());
		 				}
//	            	}
		 			}
		 		} 
		 	}
	        return ports;
	    }
	
}

