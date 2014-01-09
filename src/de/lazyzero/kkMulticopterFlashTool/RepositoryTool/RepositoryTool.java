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
package de.lazyzero.kkMulticopterFlashTool.RepositoryTool;

import static lu.tudor.santec.i18n.Translatrix._;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.lazyzero.kkMulticopterFlashTool.utils.Firmware;
import de.lazyzero.kkMulticopterFlashTool.utils.XmlReaderFirmwares;

public class RepositoryTool extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static final String VERSION = "0.1";
	private JTable firmwareTablePanel;
	private Vector<Firmware> firmwares;
	private XmlReaderFirmwares firmwareReader;

	public RepositoryTool() {
		init();
		initGUI();
	}

	private void init() {
		firmwares = new Vector<Firmware>();
		try {
			firmwareReader = new XmlReaderFirmwares(new URL("http://lazyzero.de/_media/firmwares.xml.zip"), new URL("http://lazyzero.de/_media/firmwares.xml.zip"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		firmwares.addAll(firmwareReader.getFirmwares());
		
	}

	private void initGUI() {
		this.setTitle(_("title") + " " + VERSION + ": KKFlashtool stable Version: " + firmwareReader.getActualVersion());
		
		
		firmwareTablePanel = new FirmwareTablePanel(firmwares);
		
		this.add(new JScrollPane(firmwareTablePanel));
		this.pack();
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new RepositoryTool();
	}

}
