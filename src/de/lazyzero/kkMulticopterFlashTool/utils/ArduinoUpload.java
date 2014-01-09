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

import avr8_burn_o_mat.AVR;
import avr8_burn_o_mat.AvrdudeProgrammer;

public class ArduinoUpload {
private String name;
private AVR avr;
private AvrdudeProgrammer avrProgrammer;
private int speed;
	
	public ArduinoUpload(String name, AVR avr, AvrdudeProgrammer avrProgrammer, int speed) {
		this.setName(name);
		this.setAVR(avr);
		this.setAvrProgrammer(avrProgrammer);
		this.setSpeed(speed);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AVR getAVR() {
		return avr;
	}

	public void setAVR(AVR avr) {
		this.avr = avr;
	}

	public AvrdudeProgrammer getAvrProgrammer() {
		return avrProgrammer;
	}

	public void setAvrProgrammer(AvrdudeProgrammer avrProgrammer) {
		this.avrProgrammer = avrProgrammer;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public String toString() {
		return name + "; " + avr.getCaptionAliasFree() + "; " + avrProgrammer + "; " + speed;
	}
}


