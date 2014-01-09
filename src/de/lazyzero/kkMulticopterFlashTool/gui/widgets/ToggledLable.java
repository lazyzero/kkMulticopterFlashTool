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
package de.lazyzero.kkMulticopterFlashTool.gui.widgets;

import java.awt.Color;

import javax.swing.JLabel;

public class ToggledLable extends JLabel {

	private static final long serialVersionUID = 1L;
	private Color fontColor = Color.LIGHT_GRAY;
	
	public ToggledLable(String s, boolean isEnabled, Color fg, Color bg) {
		super(s);
		if (bg.getBlue() == 3 && bg.getRed() == 3 && bg.getGreen() == 3) {
			fontColor = Color.DARK_GRAY;
		}
		this.setForeground(isEnabled?fg:fontColor);
		this.setOpaque(true);
		this.setBackground(bg);
		this.setFont(this.getFont().deriveFont(this.getFont().getSize2D()-2));
	}
	
}
