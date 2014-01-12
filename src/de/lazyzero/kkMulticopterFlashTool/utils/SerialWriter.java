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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;

public class SerialWriter {
	private SerialPort serialPort;
	private OutputStream out;
	private int baud = 9600;
	private String port;
	
	public SerialWriter(int baud, String port) {
		this.baud = baud;
		this.port = port;
		
		try {
			openPort();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendCommand(String command) throws IOException {
		out.write(command.getBytes());
		out.flush();
	}

	private void openPort() throws IOException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException{
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
		CommPort commPort = portIdentifier.open("LightController",2000);
        
        if ( commPort instanceof SerialPort )
        {
            serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(baud,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            
            this.out = serialPort.getOutputStream();
        }
	}
	
	public void close() throws IOException {
		out.flush();
		out.close();
		serialPort.removeEventListener();
		serialPort.close();
	}
	

}