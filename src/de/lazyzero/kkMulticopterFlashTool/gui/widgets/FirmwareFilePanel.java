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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.ButtonsStateListener;
import de.lazyzero.kkMulticopterFlashTool.utils.Firmware;
import de.lazyzero.kkMulticopterFlashTool.utils.Icons;

public class FirmwareFilePanel extends JPanel implements ActionListener, ButtonsStateListener{
	private static final long serialVersionUID = 1L;
	private KKMulticopterFlashTool parent;

	private CellConstraints cc;
	private JTextField firmwareFileField;
	private JButton flashFile;
	private JLabel firmwareFileLabel;
	private JButton load;
	private JFileChooser fc;
	private FileFilter ffilter;
	private File firmwareFile;
	private Logger logger = KKMulticopterFlashTool.getLogger();
	private boolean buttonsEnabled = true;
	
	public FirmwareFilePanel(KKMulticopterFlashTool parent) {
		this.parent = parent;
		
		init();
	}
	
	private void init() {
		fc = new JFileChooser(parent.getSettings().getProperty("last.dir","%HOME%"));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

		ffilter = new FileFilter() {
			
			@Override
			public String getDescription() {
				return "Hex-File (.hex)";
			}
		
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getAbsoluteFile().toString().toLowerCase().endsWith(".hex");
			}
		};
		
		fc.setFileFilter(ffilter);
		
		//create the CellContraints
		cc  = new CellConstraints();
		
		// create the Layout for Panel this
		String panelColumns = "pref,3dlu,fill:pref:grow,3dlu,pref,3dlu,pref";
		String panelRows = "pref,3dlu,pref,3dlu,pref:grow,3dlu,top:pref:grow";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);
		
		firmwareFileLabel = new JLabel(_("firmware.file"));
		firmwareFileLabel.setToolTipText(_("firmware.load.tip"));
		firmwareFileField = new JTextField();
		firmwareFileField.setToolTipText(_("firmware.load.tip"));
		
		load = new JButton(Icons.getIcon16(Icons.LOAD));
		load.setMnemonic(KeyEvent.VK_L);
		load.addActionListener(this);
		load.setToolTipText(_("firmware.load.tip"));
		
		this.setBorder(new TitledBorder(_("firmware.settings")));
		
		flashFile = new JButton(Icons.getIcon16(Icons.RUN));
		flashFile.setMnemonic(KeyEvent.VK_F);
		flashFile.addActionListener(this);
		flashFile.setEnabled(false);
		flashFile.setToolTipText(_("firmware.flashFile.tip"));
		
		this.add(firmwareFileLabel, cc.xy(1, 3));
		this.add(firmwareFileField, cc.xy(3, 3));
		this.add(load, cc.xy(5,3));
		this.add(flashFile, cc.xy(7,3));
	}
	
	@Override
	public void actionPerformed(ActionEvent action) {
		if (action.getSource().equals(load)){
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				setHexFile(fc.getSelectedFile(), true);
			} else {
				resetFile();
			}
		} else if (action.getSource().equals(flashFile)){
			parent.clearText();
			parent.println(_("messages.flashFile"));
			if (KKMulticopterFlashTool.getCountdown() > 0) {
				countdown(KKMulticopterFlashTool.getCountdown());
			} else {
				parent.flashAVR();
			}
		} 
	}
	
	public void setHexFile(File file, boolean clearContent) {
		if (file == null) {
			resetFile();
			return;
		}
		firmwareFile = file;
		firmwareFileField.setText(firmwareFile.getName());
		parent.setFirmware(new Firmware(firmwareFile));
		if (clearContent) {
			parent.clearText();
		}
		parent.println(_("messages.loadFile"));
		flashFile.setEnabled(true);
	}
	
	public void resetFile() {
		firmwareFileField.setText("");
		parent.clearText();
		parent.println(_("messages.nothingLoad"));
		flashFile.setEnabled(false);
	}


	private void countdown(final int countdown) {
		logger.log(Level.INFO, "start Countdown from " + countdown);
		final JPanel glass = (JPanel) parent.getMainframe().getGlassPane();
	    glass.setLayout(new GridBagLayout());
	    
	    final JLabel number = new JLabel("countdown");
	    number.setForeground(Color.RED);
	    number.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 120));
	    number.setOpaque(false);
	    glass.add(number);
	    glass.setVisible(true);
	    
	    Timer timer = new Timer();
        TimerTask task = new TimerTask() {
        	int count = countdown;
            @Override
            public void run() {
            	if (count == -1) {
            		this.cancel();
            		glass.setVisible(false);
            		parent.flashAVR();
            	}
            	glass.removeAll();
            	number.setText(count+"");
            	glass.add(number);
            	number.repaint();
    			logger.log(Level.INFO, count+"");
            	count--;
            }
        };
        timer.schedule(task, 0, 1000);
        try {
			timer.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		parent.getMainframe().repaint();
	}

	@Override
	public void setButtonsEnabled(boolean b) {
		this.buttonsEnabled = b;
	}

	@Override
	public void updateButtons() {
		load.setEnabled(buttonsEnabled);
		flashFile.setEnabled(buttonsEnabled==false?false:(firmwareFileField.getText().length()!=0?true:false));
	}
}
