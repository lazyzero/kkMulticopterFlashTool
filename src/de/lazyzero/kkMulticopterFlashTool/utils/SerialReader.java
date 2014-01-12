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
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

import de.lazyzero.kkMulticopterFlashTool.gui.SeriealListener;

public class SerialReader extends Thread{
	private SerialPort serialPort;
	private InputStream in;
	private int baud = 9600;
	private String port;
	InputStreamReader isr;
	BufferedReader br;
	final private LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();String line;
	private SeriealListener listener;

	public SerialReader(int baud, String port, SeriealListener listener) {
		this.baud = baud;
		this.port = port;
		this.listener = listener;
		
		try {
			openPort();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	private void openPort() throws Exception{
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
		CommPort commPort = portIdentifier.open("LightController",2000);
        
        if ( commPort instanceof SerialPort )
        {
            serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(baud,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            
            in = serialPort.getInputStream();
           
            isr = new InputStreamReader(in);
            br = new BufferedReader(isr);
        }
	}
	
	
	public void run(){
	
		try {
			while ((line = br.readLine()) != null && !isInterrupted()) { // while loop begins here
				String[] values = line.trim().split("=");
				if (values.length > 1) {
					if (values[0].length()>0) {
						//System.out.println(values[0] + " : " + values[1]);
						data.put(values[0], values[1]);
						listener.dataReceived(data);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
			
	public LinkedHashMap<String, String> getData() {
		return data;
	}

	
	public void close() throws IOException {
		in.close();
		serialPort.removeEventListener();
		serialPort.close();
	}
	
	
	
	public static void main(String[] args) {
//		final SerialReader sr = new SerialReader(9600, "/dev/cu.usbserial-A700eC94");
//		final SerialReader sr = new SerialReader(9600, "/dev/cu.SLAB_USBtoUART");
//		sr.start();
//		
//		
//		for (int i = 0; i < 10; i++) {
//			try {
//				System.out.println("tick");
//				Set<String> keys = sr.data.keySet();
//				for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
//					String key = (String) iterator.next();
//					System.out.println(key + "==" + sr.data.get(key));
//				}
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		System.out.println("Interrupt Thread");
//		sr.interrupt();
//		System.exit(0);
	}
}
