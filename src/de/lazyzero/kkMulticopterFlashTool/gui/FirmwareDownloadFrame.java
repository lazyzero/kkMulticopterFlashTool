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

import static lu.tudor.santec.i18n.Translatrix._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.concord.swing.SpringUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.Firmware;
import de.lazyzero.kkMulticopterFlashTool.utils.XmlReaderFirmwares;
import de.lazyzero.kkMulticopterFlashTool.utils.download.Download;
import de.lazyzero.kkMulticopterFlashTool.utils.download.DownloadsTableModel;
import de.lazyzero.kkMulticopterFlashTool.utils.download.ProgressRenderer;

public class FirmwareDownloadFrame extends JDialog implements ActionListener, ChangeListener{
	private static final long serialVersionUID = 1L;
	private Vector<Firmware> firmwares;
	private JPanel panel;
	private CellConstraints cc;
	private Dimension preferredSize = new Dimension(450, 550);
	private DownloadsTableModel downloadTableModel;
	private JTable table;
	private JPanel downloadsPanel;
	private JButton closeButton;
	private JButton downloadButton;
	private HashMap<String, Boolean> firmwareTyps;

	public FirmwareDownloadFrame(Vector<Firmware> firmwares) {
		this.firmwares = firmwares;
		initGUI();
		
		this.pack();
		this.setModal(true);
		this.setVisible(true);
	}

	private void initGUI() {
		panel = new JPanel();
//		this.setPreferredSize(preferredSize);


		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,fill:pref:grow,3dlu,pref,3dlu,pref,3dlu";
		String panelRows = "3dlu,pref:grow,3dlu,fill:pref,3dlu,pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		panel.setLayout(panelLayout);
		
		downloadTableModel = new DownloadsTableModel();
        table = new JTable(downloadTableModel);
        
        // Set up ProgressBar as renderer for progress column.
        ProgressRenderer renderer = new ProgressRenderer(0, 100);
        renderer.setStringPainted(true); // show progress text
        table.setDefaultRenderer(JProgressBar.class, renderer);
        table.setForeground(Color.WHITE);
        
        // Set table's row height large enough to fit JProgressBar.
        table.setRowHeight((int) renderer.getPreferredSize().getHeight());
        
        
        // Set up downloads panel.
        downloadsPanel = new JPanel();
        downloadsPanel.setPreferredSize(new Dimension(preferredSize.width, 200));
        downloadsPanel.setBorder(
                BorderFactory.createTitledBorder(_("downloads.downloads")));
        downloadsPanel.setLayout(new BorderLayout());
        downloadsPanel.add(new JScrollPane(table),
                BorderLayout.CENTER);
        
        closeButton = new JButton(_("close"));
        closeButton.addActionListener(this);
        
        downloadButton = new JButton(_("downloads.download"));
        downloadButton.addActionListener(this);
        
        panel.add(getFirmwareTypePanel(), cc.xyw(2, 2, 5));
        panel.add(downloadsPanel, cc.xyw(2, 4, 5));
        panel.add(downloadButton, cc.xy(4, 6));    
        panel.add(closeButton, cc.xy(6, 6));    
        
        JScrollPane scrollPane = new JScrollPane(panel);
        
        this.add(scrollPane, BorderLayout.CENTER);
		this.setTitle(_("downloads.title"));
		
		
	}

	private JPanel getFirmwareTypePanel() {
		JPanel ftp = new JPanel();
		SpringLayout sl = new SpringLayout();
		ftp.setLayout(sl);
		ftp.setBorder(new TitledBorder(_("firmware")));
		
		firmwareTyps = Firmware.getCategoriesBooleanHashMap(firmwares);
		
		//sort the entries
		SortedSet<String> sortedset= new TreeSet<String>(firmwareTyps.keySet());
		
		Iterator<String> it = sortedset.iterator();
		while (it.hasNext()) {
			String type = (String) it.next();
			JCheckBox cb = new JCheckBox(type);
			cb.setSelected(firmwareTyps.get(type));
			cb.addChangeListener(this);
			ftp.add(cb);
		}
		
		SpringUtilities.makeCompactGrid(ftp,
				(firmwareTyps.size()/4), 4, //rows, cols
				2, 2,        //initX, initY
				2, 2);       //xPad, yPad
		
		
		return ftp;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(closeButton)) {
			this.dispose();
		}
		if (e.getSource().equals(downloadButton)) {
			downloadTableModel.clearAllDownloads();
			Vector<URL> downloadedURLs = new Vector<URL>();
			table.setModel(downloadTableModel);
			downloadsPanel.repaint();
			
			Iterator<String> it = firmwareTyps.keySet().iterator();
			while (it.hasNext()) {
				String type = it.next();
				for (int i = 0; i < firmwares.size(); i++) {
					if (firmwares.get(i).getName().equals(type) && firmwareTyps.get(type) && !downloadedURLs.contains(firmwares.get(i).getFileURL())){
						downloadTableModel.addDownload(new Download((firmwares.get(i).getZipURL()!=null ? firmwares.get(i).getZipURL() : firmwares.get(i).getFileURL())));
						downloadedURLs.add(firmwares.get(i).getFileURL());
					}
				}				
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (firmwareTyps.containsKey(((JCheckBox)e.getSource()).getText())){
			firmwareTyps.put(((JCheckBox)e.getSource()).getText(),((JCheckBox)e.getSource()).isSelected());
		}
	}
}
