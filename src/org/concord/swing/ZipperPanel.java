/*
 *  Copyright (C) 2004  The Concord Consortium, Inc.,
 *  10 Concord Crossing, Concord, MA 01742
 *
 *  Web Site: http://www.concord.org
 *  Email: info@concord.org
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * END LICENSE */

package org.concord.swing;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ZipperPanel extends JPanel {

	CCJCheckBoxTree filesTree;
	MostRecentFileDialog mrfd = 
		new MostRecentFileDialog("org.concord.zip.file.source");
	Hashtable filesHash = new Hashtable();
	Vector filesToZip = new Vector();
	
	public ZipperPanel() {
		super();
		init();
		// TODO Auto-generated constructor stub
	}

	public ZipperPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
		init();
	}

	public ZipperPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
		init();
	}

	public ZipperPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
		init();
	}
	
	private void init() {
		this.setLayout(new BorderLayout());
		
		filesTree = new CCJCheckBoxTree("Files to Zip");
	    filesTree.setCellRenderer(new CCJCheckBoxRenderer());
	    filesTree.setRootVisible(false);
		
		JScrollPane scrollPane = new JScrollPane(filesTree);
		
		this.add(scrollPane, BorderLayout.CENTER);
		
		JButton addFile = new JButton("Add File(s)");
		addFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int retval = mrfd.showOpenDialog(null, "Add a file to compress");
				if(retval == MostRecentFileDialog.APPROVE_OPTION) {
					File f = mrfd.getSelectedFile();
					if(f.isFile() && 
							(!filesHash.contains(f.getAbsolutePath()))) {
						String fn = f.getName();
				    	CCJCheckBoxTree.NodeHolder newNodeHolder = 
				    		new CCJCheckBoxTree.NodeHolder(fn, true);
				    	filesTree.addObject(newNodeHolder);
				    	filesHash.put(newNodeHolder, f.getAbsolutePath());
					}
				}
			}
		});
		
		JButton zipFile = new JButton("Zip File(s)");
		zipFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(filesHash.isEmpty()) {
					String msg = "No files to zip. Please add files first.";
					JOptionPane.showMessageDialog(null, msg, 
							"NO Files", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				filesToZip.removeAllElements();
				Set keys = filesHash.keySet();
				Iterator it = keys.iterator();
				while(it.hasNext()) {
					CCJCheckBoxTree.NodeHolder key = (CCJCheckBoxTree.NodeHolder)it.next();
					if(key.checked)
						filesToZip.addElement((new File(filesHash.get(key).toString())));
				}
				
				if(filesToZip.size() == 0) {
					String msg = "Please select the files to zip.";
					JOptionPane.showMessageDialog(null, msg, 
							"NO Files", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				MostRecentFileDialog mrfd2 = new MostRecentFileDialog("org.concord.zip.file.target");
				int retval = mrfd2.showSaveDialog(null, "Save zipped file");
				if(retval == MostRecentFileDialog.APPROVE_OPTION) {
					File f = mrfd2.getSelectedFile();
					Zipper.zip(filesToZip, f);
				}
			}
		});
		
		JPanel control = new JPanel();
		control.add(addFile);
		control.add(zipFile);
		
		this.add(control, BorderLayout.SOUTH);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame("Zip Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ZipperPanel zp = new ZipperPanel();
		frame.getContentPane().add(zp);
		frame.pack();
		frame.show();
	}
}
