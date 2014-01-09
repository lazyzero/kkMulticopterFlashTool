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

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;


public class Icons {

	
	public static final String app = "KK.png";
	public static final String LICENSE = "3floppy_unmount.png";
	public static final String SAVE = "3floppy_unmount.png";
	public static final String EXIT = "exit.png";
	public static final String ABOUT = "error.png";
	public static final String SETTINGS = "package_utilities.png";
	public static final String LANGUAGE = "babelfish.png";
	public static final String GERMAN = "de.png";
	public static final String ENGLISH = "gb.png";
	public static final String FRENCH = "fr.png";
	public static final String ITALIAN = "it.png";
	public static final String DUTCH = "nl.png";
	public static final String CHINESE = "cn.png";
	public static final String FAROESE = "fo.png";
	public static final String DANISH = "dk.png";
	public static final String BRAZILIAN = "br.png";
	public static final String RUSSIAN = "ru.png";
	public static final String TURKISH = "tr.png";
	public static final String SWEDISH = "se.png";
	public static final String POLISH = "pl.png";
	public static final String SPANISH = "es.png";
	public static final String PORTUGESE = "pt.png";
	public static final String KOREAN = "kr.png";
	public static final String SLOVAKIAN = "sk.png";
	public static final String CZECH = "cz.png";
	public static final String HEBREW = "il.png";
	public static final String CROATIAN = "hr.png";
	public static final String GREEK = "gr.png";
	public static final String ROMANIAN = "ro.png";
	public static final String NORWEGIAN = "no.png";
	public static final String SLOVENIAN = "si.png";
	public static final String JAPANESE = "jp.png";
	public static final String HUNGARIAN = "hu.png";
	public static final String SERBIAN = "serbia.png";
	public static final String BOSNIAN = "ba.png";
	
	public static final String RUN = "runit.png"; 
	public static final String STOP = "not.png"; 
	public static final String CONFIG = "agt_utilities.png";
	public static final String TRASH = "trash.png";
	public static final String EXPORT = "cal.png";
	public static final String OPEN = "folder_green.png";
	public static final String LOAD = "floppy_unmount.png";
	public static final String LOADEEPROM = "kcmmemory.png";
	
	private static final String iconpath = "gui/resources/icons/";
	public static final String RELOAD = "agt_reload.png";
	public static final String KR = "minsoo.png";
	public static final String LZ = "LZ.png";
	public static final String MAIL = "mail.png";
	public static final String INFO = "info.png";
	
	public static final String POT = "mems.png";
	public static final String LCD = "mems.png";
	public static final String PIEZO = "mems.png";
	public static final String MEMS = "mems.png";
	public static final String ACC = "mems.png";
	public static final String CPPM = "mems.png";
	
	
	
	
	public static ImageIcon getIcon22(String iconname) {
        return getIcon(iconname, 22);
    }
    public static ImageIcon getIcon32(String iconname) {
        return getIcon(iconname, 32);
    }
    public static ImageIcon getIcon16(String iconname) {
        return getIcon(iconname, 16);
    }
    public static ImageIcon getIcon48(String iconname) {
        return getIcon(iconname, 48);
    }
    public static ImageIcon getIcon64(String iconname) {
        return getIcon(iconname, 64);
    }
    public static ImageIcon getIcon(String iconname) {
        
        java.net.URL imgURL = KKMulticopterFlashTool.class.getResource(iconpath + iconname);
        
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } 
        return null;
    }
    
    public static ImageIcon getIcon(String iconname, int size) {
        
        java.net.URL imgURL = KKMulticopterFlashTool.class.getResource(iconpath + iconname);
        
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(size,size,Image.SCALE_SMOOTH));
        } 
        return null;
    }
    public static Image getImage(String iconname, int height) {
        
        java.net.URL imgURL = KKMulticopterFlashTool.class.getResource(iconpath + iconname);
        
        if (imgURL != null) {
        	return new ImageIcon(imgURL).getImage().getScaledInstance(-1,height,Image.SCALE_SMOOTH);
        }
        return null;     
    }
	public static Icon getIconFlag(String country) {
		java.net.URL imgURL = KKMulticopterFlashTool.class.getResource(iconpath + country);
        
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } 
        return null;
	}
}
