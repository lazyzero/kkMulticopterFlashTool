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
package de.lazyzero.kkMulticopterFlashTool.utils.download;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;

// This class manages the download table's data.
public class DownloadsTableModel extends AbstractTableModel
        implements Observer {
    
	private static final long serialVersionUID = 1L;

	// These are the names for the table's columns.
    private static final String[] columnNames = {"Name", "Size", "Speed",
    "Progress", "Status"};
    
    // These are the classes for each column's values.
    private static final Class[] columnClasses = {String.class, String.class, String.class, JProgressBar.class, String.class};
    
    // The table's list of downloads.
    private ArrayList downloadList = new ArrayList();
    
    // Add a new download to the table.
    public void addDownload(Download download) {
       
    	// Register to be notified when the download changes.
        download.addObserver(DownloadsTableModel.this);
        
        downloadList.add(download);
        
        // Fire table row insertion notification to table.
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
        
    }
    
    // Get a download for the specified row.
    public Download getDownload(int row) {
        return (Download) downloadList.get(row);
    }
    
    // Remove a download from the list.
    public void clearDownload(int row) {
        downloadList.remove(row);
        
        // Fire table row deletion notification to table.
        fireTableRowsDeleted(row, row);
    }
    
    // Get table's column count.
    public int getColumnCount() {
        return columnNames.length;
    }
    
    // Get a column's name.
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    // Get a column's class.
    public Class getColumnClass(int col) {
        return columnClasses[col];
    }
    
    // Get table's row count.
    public int getRowCount() {
        return downloadList.size();
    }
    
    // Get value for a specific row and column combination.
    public Object getValueAt(int row, int col) {
    	DecimalFormat formatter = new DecimalFormat("#.#");
    	String unit = " Byte";
        
        Download download = (Download) downloadList.get(row);
        switch (col) {
            case 0: // URL
                return download.getName();
            case 1: // Size
                double size = download.getSize();
                if (size > 999999) {
                	size /= 1024*1024;
                	unit = " MB";
                } else if (size > 999){
                	size /= 1024;
                	unit = " kB";
                }
                return ((size == -1) ? "" : formatter.format(size)) + unit;
            case 2:
            	return formatter.format(download.getSpeed()) + " kB/sec";
            case 3: // Progress
                return new Float(download.getProgress());
            case 4: // Status
                return Download.STATUSES[download.getStatus()];
        }
        return "";
    }
    
  /* Update is called when a Download notifies its
     observers of any changes */
    public void update(Observable o, Object arg) {
        int index = downloadList.indexOf(o);
        
        // Fire table row update notification to table.
        fireTableRowsUpdated(index, index);
    }

	public void clearAllDownloads() {
		try {
			downloadList.clear();
			fireTableRowsDeleted(0, downloadList.size()-1);
		} catch (IndexOutOfBoundsException e) {
			// TODO: handle exception
		}
	}
}