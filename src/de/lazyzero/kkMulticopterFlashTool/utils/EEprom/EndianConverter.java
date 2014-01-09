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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EndianConverter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(convertLittleEndianToUInt16((byte)0x84, (byte)0x03));
		
		byte[] b = convertUInt16ToBigEndian(900);
		for (byte c : b) {
			System.out.println(c);
		}
		System.out.println(convertLittleEndianToUInt16(b[0], b[1]));
	}

	
	public static int convertLittleEndianToUInt16(byte low, byte high) {
		byte[] b = { low, high,  0x00, 0x00};
		ByteBuffer bb = ByteBuffer.wrap(b);
		bb = bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	
	public static byte[] convertUInt16ToBigEndian(int value) {
		byte[] b = { 0x00, 0x00,  0x00, 0x00};
		b[0] = (byte) (value & 0xFF);
		b[1] = (byte) ((value >> 8) & 0xFF);
		ByteBuffer bb = ByteBuffer.wrap(b);
		bb = bb.order(ByteOrder.BIG_ENDIAN);
		return bb.array();
	}
	
}
