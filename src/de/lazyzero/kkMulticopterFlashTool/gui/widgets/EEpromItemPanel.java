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

import static lu.tudor.santec.i18n.Translatrix._;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEprom;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEpromBooleanItem;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEpromDataItem;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEpromUInt8Item;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEpromValueException;

public class EEpromItemPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private CellConstraints cc;
	private EEpromDataItem item;
	public final static int BOOLEAN_ITEM = 0;
	public final static int UINT8_ITEM = 1;

	public EEpromItemPanel(EEpromDataItem item) {
		this.item = item;
		switch (item.getDataType()) {
		//boolean
		case BOOLEAN_ITEM :
			createBooleanItem();
			break;
		case UINT8_ITEM :
			createUInt8Item();
			break;
		default:
			break;
		}
		
	}

	private void createUInt8Item() {
		int value = 0;
		
		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,pref,3dlu,fill:pref:grow,3dlu";
		String panelRows = "3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		this.setBorder(new TitledBorder(_(item.getType())));
		value = ((EEpromUInt8Item) item).getValue();
		
		final JLabel label = new JLabel(_(item.getLabel()));
		final JTextField field = new JTextField(value+"");
		field.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.getSource());
				try {
					boolean updated = ((EEpromUInt8Item) item).setValue(Integer.valueOf(field.getText()));
				} catch (NumberFormatException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (EEpromValueException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		this.add(label, cc.xy(2, 2));
		this.add(label, cc.xy(4, 2));
	}
	
	private void createBooleanItem() {
		boolean isSelected = false;
		
		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,fill:pref:grow,3dlu";
		String panelRows = "3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		this.setBorder(new TitledBorder(_(item.getType())));
		try {
			isSelected = ((EEpromBooleanItem) item).getValue();
		} catch (EEpromValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final JCheckBox checkBox = new JCheckBox(_(item.getLabel()), isSelected);
		checkBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.getSource());
				boolean updated = ((EEpromBooleanItem) item).setValue(checkBox.isSelected());
			}
		});
		this.add(checkBox, cc.xy(2, 2));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EEprom eeprom = new EEprom("./eeprom_readout/eeprom.txt");
//		eeprom.readEEprom("./eeprom_readout/eeprom.txt");
		
		LinkedHashMap<Short, String> dataMapping = new LinkedHashMap<Short, String>();
		dataMapping.put((short) 127, "true");
		dataMapping.put((short) 255, "false");
		
		EEpromBooleanItem eeDataItem = new EEpromBooleanItem(eeprom, "Gyro", 0, 1, EEpromBooleanItem.BOOLEAN, dataMapping , "Yaw gyro inverted");
		EEpromItemPanel eei = new EEpromItemPanel(eeDataItem);
		
		JFrame jf = new JFrame();
		jf.add(eei);
		
		jf.pack();
		jf.setVisible(true);
	}

}
