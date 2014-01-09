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
package de.lazyzero.kkMulticopterFlashTool.gui;

import java.awt.Color;
import java.util.Enumeration;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

public class ColorTweaks {
	public static void tweakColors() {
		UIDefaults uiDefaults = UIManager.getDefaults();
		//System.out.println("Current UIDefaults: " + uiDefaults);

		Color bgColor = new ColorUIResource(Color.BLACK);
		for (Enumeration<Object> e = uiDefaults.keys(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof String) {
//				if ( ((String) obj).contains("disable") && uiDefaults.get(obj) instanceof Color)
//						System.out.println((String) obj + uiDefaults.get(obj));
				if (((String) obj).endsWith(".background")
						&& uiDefaults.get(obj) instanceof Color) {
					uiDefaults.put(obj, bgColor);
				}
			}
		}
		
		UIManager.put("TextArea.foreground", Color.BLACK);
		
		UIManager.put("CheckBox.foreground", Color.WHITE);
		UIManager.put("TableHeader.foreground", Color.WHITE);
		UIManager.put("Table.forground", Color.WHITE);
		UIManager.put("CheckBoxMenuItem.foreground", Color.WHITE);
		UIManager.put("Button.select", Color.gray);
		UIManager.put("Label.foreground", Color.WHITE);
		UIManager.put("TextArea.foreground", Color.WHITE);
		UIManager.put("TextField.border", new LineBorder(Color.LIGHT_GRAY));
		UIManager.put("TitledBorder.titleColor", Color.WHITE);
		UIManager.put("ScrollPane.foreground", Color.WHITE);
		UIManager.put("ScrollPane.border", new LineBorder(Color.LIGHT_GRAY));
		UIManager.put("MenuBar.foreground", Color.WHITE);
		UIManager.put("MenuBar.background", Color.BLACK);
		UIManager.put("MenuBar.borderColor", Color.BLACK);
		UIManager.put("MenuItem.foreground", Color.WHITE);
		UIManager.put("Menu.foreground", Color.WHITE);
		UIManager.put("Menu.background", Color.DARK_GRAY);
		UIManager.put("OptionPane.foreground", Color.WHITE);
		UIManager.put("OptionPane.messageForeground", Color.WHITE);
		
		if (System.getProperty("os.name").toLowerCase().contains("mac")){
			UIManager.put("Button.disabledText", Color.white);
			UIManager.put("ComboBox.background", Color.LIGHT_GRAY);
			UIManager.put("TextField.background", Color.WHITE);
			UIManager.put("TitledBorder.border", new LineBorder(Color.LIGHT_GRAY));
			UIManager.put("Table.foreground", Color.white);
			UIManager.put("ToolTip.background", Color.yellow);
			UIManager.put("FileChooser.background", Color.red);
			UIManager.put("Panel.foreground", Color.red);
			UIManager.put("Label.disabledShadow", Color.red);
		} else {
			UIManager.put("ComboBox.border", new LineBorder(Color.LIGHT_GRAY));
			UIManager.put("ComboBox.selectionBackground", Color.GRAY);
			UIManager.put("ComboBox.selectionForeground", Color.WHITE);
			UIManager.put("ComboBox.control", Color.GRAY);
			UIManager.put("ComboBox.controlForeground", Color.GRAY);
			UIManager.put("ComboBox.foreground", Color.WHITE);
			UIManager.put("ComboBox.buttonBackground", Color.GRAY);
			UIManager.put("ComboBox.buttonHighlight", Color.GRAY);
			
			UIManager.put("Separator.foreground", Color.GRAY);
			UIManager.put("TextField.foreground", Color.WHITE);
			UIManager.put("Slider.border", new LineBorder(Color.LIGHT_GRAY));
			UIManager.put("Button.background", Color.BLACK);
			UIManager.put("Button.foreground", Color.white);
			UIManager.put("Button.shadow", Color.BLACK);
			UIManager.put("Panel.foreground", Color.WHITE);
			UIManager.put("List.foreground", Color.white);
			UIManager.put("Viewport.foreground", Color.WHITE);
		}

	}
}
