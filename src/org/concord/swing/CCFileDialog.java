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

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class CCFileDialog extends FileDialog {
	public final static String PREFERENCE_DOMAIN ="org/concord/swing/CCFileDialog";
	public final static String DEFAULT_NAME ="org.concord.swing.CCFileDialog";
	public final static int FILES_ONLY = 1;
	public final static int FILES_AND_DIRECTORIES = 2;
	public final static int DIRECTORIES_ONLY = 3;
	
	private String preferencesKey;
	private boolean preferencesKeyWasSet = false;
	private int fileSelectionMode = 1;

	public CCFileDialog() {
		this(DEFAULT_NAME);
	}
	
	public CCFileDialog(String preferencesKey) {
		super(new Frame());
		//Frame frame = (Frame)SwingUtilities.getRoot(this);
	    setPreferencesKey(preferencesKey);
	    checkingPreferences();		
	}

	public CCFileDialog(Frame parent) {
		this(parent, null, DEFAULT_NAME);
	}
	
	public CCFileDialog(Frame parent, String title) {
		this(parent, title, DEFAULT_NAME);
	}
	
	public CCFileDialog(Frame parent, String title, String preferencesKey) {
		super(parent, title);
	    setPreferencesKey(preferencesKey);
        settingPreferences();
	}
	
	public CCFileDialog(Frame parent, String title, int mode) {
		this(parent, title, mode, DEFAULT_NAME);
	}

	public CCFileDialog(Frame parent, String title, int mode, String preferencesKey) {
		super(parent, title, mode);
	    setPreferencesKey(preferencesKey);
        settingPreferences();
	}
	
    public void setDirectory(String dir) {
        super.setDirectory(dir);
        if(preferencesKeyWasSet){
            settingPreferences();
        }
    }

    protected void setPreferencesKey(String preferencesKey){
        this.preferencesKey = (preferencesKey == null)?DEFAULT_NAME:preferencesKey;
        preferencesKeyWasSet = true;
    }

    protected void checkingPreferences(){
        Preferences userPreferences = Preferences.userRoot();
        Preferences dialogPreferences = null;
        try{
            dialogPreferences = userPreferences.node(PREFERENCE_DOMAIN);
        }catch(Throwable t){
            dialogPreferences = null;    
        }
        if(dialogPreferences == null) return;
        String suggestedPath = dialogPreferences.get(preferencesKey,null);
        if(suggestedPath == null){
            try{
                suggestedPath = (new File(getDirectory())).getCanonicalPath();
            }catch(Throwable t){}
            if(suggestedPath == null) suggestedPath = System.getProperty("user.dir");
        }
        File suggestedFile = new File(suggestedPath);
        if(suggestedFile != null && suggestedFile.exists() && suggestedFile.isDirectory()){
            try {
				super.setDirectory(suggestedFile.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    protected void settingPreferences(){
        if(preferencesKey == null)  preferencesKey = DEFAULT_NAME;
        
        String dir = getDirectory();
        File currDirectory = null;
        if(dir == null) {
        	checkingPreferences();
        	dir = getDirectory();
        }
        if(dir != null) currDirectory = new File(dir);
        String canonicalPath  = null;
        try{
        	if(currDirectory != null)
        		canonicalPath = currDirectory.getCanonicalPath();
        }catch(Throwable t){}
        if(canonicalPath == null) canonicalPath = System.getProperty("user.dir");
        
        Preferences userPreferences = Preferences.userRoot();
        
        Preferences dialogPreferences = null;
        try{
            dialogPreferences = userPreferences.node(PREFERENCE_DOMAIN);
        }catch(Throwable t){
            dialogPreferences = null;    
        }
        if(dialogPreferences == null) return;
        dialogPreferences.put(preferencesKey,canonicalPath);
    }
    
    public void setFileSelectionMode(int fileSelectionMode) {
    		if(fileSelectionMode != FILES_ONLY &&
    				fileSelectionMode != FILES_AND_DIRECTORIES &&
    				fileSelectionMode != DIRECTORIES_ONLY)
    			throw new IllegalArgumentException(
    					"Must be either CCFileDialog.FILES_ONLY, " +
    					"CCFileDialog.FILES_AND_DIRECTORIES, or " +
    					"CCFileDialog.DIRECTORIES_ONLY");
    		this.fileSelectionMode = fileSelectionMode;
    }
    public int getFileSelectionMode() {
    		return fileSelectionMode;
    }
    
    public void show() {
    		if(fileSelectionMode == DIRECTORIES_ONLY) {
    			String message = "Since you are supposed to open a folder,\n " +
    					"all the acceptable files in the same folder\n " +
    					"will be selected.";
    			JOptionPane.showMessageDialog(null, message, "FYI", JOptionPane.INFORMATION_MESSAGE);
    		}
    		super.show();
    }
    
    public String getFile() {
    		if(fileSelectionMode == DIRECTORIES_ONLY) {
    			return super.getDirectory();
    		}
    		return super.getFile();
    }
    
    public static void main(String[] args) {
    		JFrame frame = new JFrame("CCFileDialog Test");
    		JButton openButton = new JButton("Open");
    		openButton.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
	    	    		CCFileDialog fileDialog = new CCFileDialog();
	    	    		CCFilenameFilter filenameFilter = new CCFilenameFilter();
	    	    		filenameFilter.setAcceptableExtension("jnlp");
	    	    		//filenameFilter.setFileSelectionMode(CCFilenameFilter.DIRECTORY_ONLY);
	    	    		fileDialog.setFilenameFilter(filenameFilter);
	    	    		fileDialog.setFileSelectionMode(DIRECTORIES_ONLY);
	    	    		fileDialog.show();
	    	    		System.out.println("dir: " + fileDialog.getFile());
	    	    		String dir = fileDialog.getFile();
	    	    		File fd = new File(dir);
	    	    		File[] fs = fd.listFiles();
	    	    		for(int i = 0; i < fs.length; i++) {
	    	    			String filename = fs[i].getName();
	    	    			if(filename.lastIndexOf(".") == -1) return;
	    	    			String ext = filename.substring(filename.lastIndexOf(".")+1, filename.length());
	    	    			if(ext.equalsIgnoreCase(filenameFilter.getAcceptableExtension()))
	    	    				System.out.println(fs[i].getName());
	    	    		}
	    	    		//fileDialog.show();
	    	    		//System.out.println("file: " + fileDialog.getFile());
    			}
    		});
    		frame.getContentPane().add(openButton);
    		frame.pack();
    		frame.show();
    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
