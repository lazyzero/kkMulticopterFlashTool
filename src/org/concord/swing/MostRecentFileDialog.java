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

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public class MostRecentFileDialog 
{
    /**
     * Return value if cancel is chosen.
     */
    public static final int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;

    /**
     * Return value if approve (yes, ok) is chosen.
     */
    public static final int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;

    /**
     * Return value if an error occured.
     */
    public static final int ERROR_OPTION = JFileChooser.ERROR_OPTION;

    /** Instruction to display only files. */
    public static final int FILES_ONLY = JFileChooser.FILES_ONLY;

    /** Instruction to display only directories. */
    public static final int DIRECTORIES_ONLY = JFileChooser.DIRECTORIES_ONLY;
    
    public static final int ACCESS_MODE_READ = 0;
    
    public static final int ACCESS_MODE_WRITE = 1;

    /** Instruction to display both files and directories. */
    public static final int FILES_AND_DIRECTORIES = JFileChooser.FILES_AND_DIRECTORIES;
    
    final static String PREFERENCE_DOMAIN ="org/concord/swing/MostRecentFileDialog";
    final static String DEFAULT_PREFERENCES_KEY ="org.concord.swing.MostRecentFileDialog";     
    
    // you can change this to false if you want to debug the awt
    // version of the code on a platform that usually uses swing
    public static boolean USE_SWING_DIALOG_DEFAULT = true;
    
    String preferencesKey;

    String extension;
    boolean swingOnly = false;
    File currentDirectory;
    File selectedFile;
    private int fileSelectionMode = FILES_ONLY;
    private int fileAccessMode = ACCESS_MODE_READ;
    
    private Component parent;
    private String title;

    public MostRecentFileDialog(String preferencesKey, boolean swingOnly)
    {
        this.preferencesKey = preferencesKey;
        this.swingOnly = swingOnly;  
        
        initCurrentDirectory();
    }

    public MostRecentFileDialog(String preferencesKey)
    {
        this(preferencesKey, false);
    }

    public MostRecentFileDialog(boolean swingOnly)
    {
        this(null, swingOnly);
    }
    
    public MostRecentFileDialog()
    {
        this(null, false);
    }
    
    public void setFileSelectionMode(int fileSelectionMode) 
    {
        if(fileSelectionMode != FILES_ONLY &&
                fileSelectionMode != FILES_AND_DIRECTORIES &&
                fileSelectionMode != DIRECTORIES_ONLY)
            throw new IllegalArgumentException(
                    "Must be either MostRecentFileDialog.FILES_ONLY, " +
                    "MostRecentFileDialog.FILES_AND_DIRECTORIES, or " +
                    "MostRecentFileDialog.DIRECTORIES_ONLY");
        this.fileSelectionMode = fileSelectionMode;
    }

    public void setFilenameFilter(String extension)
    {
        this.extension = extension.toLowerCase();
        
    }

    protected void setCurrentDirectoryInternal(File directory)
    {
        currentDirectory = directory;
    }

    public void setCurrentDirectory(File directory)
    {        
        currentDirectory = directory;
        saveCurrentDirectory(currentDirectory);
    }
    
    public void setSelectedFile(File currentFile)
    {
    	if(useSwingDialog() && fileAccessMode == ACCESS_MODE_WRITE) {
    		if(currentFile.exists()) {
    			int retval = JOptionPane.showConfirmDialog(parent, 
    					"The file " + currentFile.getName() + " exists. " +
    					"Do you want to overwrite it?", 
    					"Warning", JOptionPane.WARNING_MESSAGE);
    			if(retval == JOptionPane.NO_OPTION ||
    					retval == JOptionPane.CANCEL_OPTION)
    				showSaveDialog(parent, title);
    			else
    	    		selectedFile = currentFile;    				
    		} else {
    			String name = currentFile.getAbsolutePath();
    			if(extension != null && !name.endsWith(extension))
    				name = name + "." + extension;
    			File temp = new File(name);
    			if(temp.exists())
    				setSelectedFile(temp);
    			else selectedFile = temp;
    		}
    	} else {
    		selectedFile = currentFile;
    	}
    }
    
    public int showOpenDialog(Component parent)
    {
    	return showOpenDialog(parent, "Open");
    }

    public int showOpenDialog(Component parent, String title)
    {
    	this.parent = parent;
    	this.title = title;
        fileAccessMode = ACCESS_MODE_READ;
        if(useSwingDialog()) {
            JFileChooser fileChooser = new JFileChooser(getCurrentDirectory());
            if(fileSelectionMode == DIRECTORIES_ONLY)
            	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            else
            	if(extension != null) fileChooser.setFileFilter(new FileNameFilter(extension));
            
            int returnVal = fileChooser.showOpenDialog(parent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                setSelectedFile(fileChooser.getSelectedFile());
                setCurrentDirectory(fileChooser.getCurrentDirectory());
                return returnVal;
            }
                        
            return returnVal;
        } else {
        	if(parent == null) parent = new Frame();
            Frame frame = (Frame)SwingUtilities.getRoot(parent);
            FileDialog dialog = new FileDialog(frame, "Open", FileDialog.LOAD);    
            if(extension != null) {
            	dialog.setFilenameFilter(new FileNameFilter(extension));
            }
            
            File currentDir = getCurrentDirectory();
            String startingPath = ".";
            if(currentDir != null) {
            	startingPath = currentDir.getAbsolutePath();
            }
            
            dialog.setDirectory(startingPath);
            dialog.show();
            
    		//  the docs for FileDialog.getDirectory say it can
    		// return null.  This could happen if the directory
            // set above is invalid (it has been deleted perhaps?)
            String selectedDirectoryStr = dialog.getDirectory();
            
            // Save the selected directory
            File selectedDirectory = null;
            if(selectedDirectoryStr != null) {
                selectedDirectory = new File(selectedDirectoryStr);
                setCurrentDirectory(selectedDirectory);
            }
                        
            File selectedFile = null;
            if(fileSelectionMode == DIRECTORIES_ONLY) {
            	if(selectedDirectory != null) {
            		selectedFile = selectedDirectory;
            	} else {            		
            		// we don't have a valid directory so I guess we
            		// set the selected File to null
            		// we don't need an if for this but it makes
            		// it more clear.
            		selectedFile = null;
            	}
            } else {
                String selectedFileStr = dialog.getFile();
                
                // If the cancel button is pressed this the selectedFileString 
                // will be null
                if(selectedFileStr != null) {
                	if(selectedDirectory != null) {
                		selectedFile = new File(selectedDirectory, selectedFileStr);
                	} else {
                		// we don't have a valid directory, but might have 
                		// a valid file.  I'll take a guess to great the file
                		// with the working direcotry
                		selectedFile = new File(selectedFileStr);
                	}
                }
            }
            dialog.dispose();
            if(selectedFile != null) {
                setSelectedFile(selectedFile);
                return APPROVE_OPTION;
            }
            
            return CANCEL_OPTION;
        }
        // return ERROR_OPTION;
    }

    public int showSaveDialog(Component parent)
    {
    	return showSaveDialog(parent, "Save");
    }
    
    public int showSaveDialog(Component parent, String title)
    {
    	this.parent = parent;
    	this.title = title;
        fileAccessMode = ACCESS_MODE_WRITE;
        if(useSwingDialog()) {
            JFileChooser fileChooser = new JFileChooser(getCurrentDirectory());
            if(extension != null) fileChooser.setFileFilter(new FileNameFilter(extension));
            int returnVal = fileChooser.showSaveDialog(parent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                setSelectedFile(fileChooser.getSelectedFile());
                setCurrentDirectory(fileChooser.getCurrentDirectory());
                return returnVal;
            }
                        
            return returnVal;
        } else {
        	if(parent == null) parent = new Frame();
            Frame frame = (Frame)SwingUtilities.getRoot(parent);
            FileDialog dialog = new FileDialog(frame, title, FileDialog.SAVE);    
            if(extension != null) dialog.setFilenameFilter(new FileNameFilter(extension));
            dialog.show();
            String selectedFileStr = null;
            
            String selectedDirectoryStr = dialog.getDirectory();
            File selectedDirectory = null;
            if(selectedDirectoryStr != null) {
                selectedDirectory = new File(selectedDirectoryStr);
                setCurrentDirectory(selectedDirectory);
            }
                        
            if(fileSelectionMode == DIRECTORIES_ONLY) {
                selectedFileStr = selectedDirectoryStr;
            } else {
                selectedFileStr = dialog.getFile();
            }
            dialog.dispose();
            if(selectedFileStr != null) {
                setSelectedFile(new File(selectedFileStr));
                return APPROVE_OPTION;
            }
            
            return CANCEL_OPTION;
        }
    }
    
    public File getSelectedFile()
    {
        return selectedFile;
    }
    
    public File getCurrentDirectory()
    {
        return currentDirectory;
    }
    
    public boolean useSwingDialog()
    {
        if(swingOnly) {
            return true;
        }
        
        if(System.getProperty("os.name").startsWith("Mac")) {
            return false;
        }
        
        return USE_SWING_DIALOG_DEFAULT;
    }
    
    
    protected void setPreferencesKey(String preferencesKey)
    {
        this.preferencesKey = preferencesKey;
    }

    protected String getPreferencesKey()
    {
        if(preferencesKey != null) {
            return preferencesKey;
        } 
        
        return DEFAULT_PREFERENCES_KEY;
    }
    
    protected void initCurrentDirectory()
    {
        String suggestedPath = null;
        String savedPath = null;
        try{
            Preferences userPreferences = Preferences.userRoot();
            Preferences dialogPreferences = null;
            
            dialogPreferences = userPreferences.node(PREFERENCE_DOMAIN);
            
            if(dialogPreferences != null) {
                savedPath = dialogPreferences.get(preferencesKey,null);
            }

            
        }catch(Throwable t){
            // can't load the saved path
        }
        
        suggestedPath = savedPath;
        
        // Make sure the suggested path is valid
        if(suggestedPath != null){
        	try {
        		File tmpSuggestedFile = new File(suggestedPath);
        		if(!tmpSuggestedFile.exists() || !tmpSuggestedFile.isDirectory()) {
        			suggestedPath = null;
        		}        	
        	} catch (Throwable t){
        		suggestedPath = null;
        	}
        }
        
        // there is no saved path
        if(suggestedPath == null){            
            try{
                // set the suggested path to be the current path of
                // this file dialog object
                suggestedPath = getCurrentDirectory().getCanonicalPath();
            }catch(Throwable t){                
            }
        }

        // the file dialog doesn't have a current path
        if(suggestedPath == null){
            try {
                suggestedPath = System.getProperty("user.dir");
            } catch (Throwable t) {                
            }
        }

        File suggestedFile = null;
        if(suggestedPath != null) {
            suggestedFile = new File(suggestedPath);
        } else {
            suggestedFile = new File(".");
        }

        // Last check to make sure the path is valid, just incase the user.dir 
        // was invalid, or "." was invalid.
        // This does mean that 
        if(suggestedFile.exists() && suggestedFile.isDirectory()) {
            setCurrentDirectoryInternal(suggestedFile);
        }
    }
    
    protected void saveCurrentDirectory(File currentDirectory)
    {
        String canonicalPath  = null;
        try{
            canonicalPath = currentDirectory.getCanonicalPath();

            Preferences userPreferences = Preferences.userRoot();        
            Preferences dialogPreferences = 
                userPreferences.node(PREFERENCE_DOMAIN);
            dialogPreferences.put(getPreferencesKey(),canonicalPath);
        }catch(Throwable t){            
        }        
    }
    
    class FileNameFilter extends FileFilter implements FilenameFilter {

    	String filter;
    	public FileNameFilter(String filter) {
    		this.filter = filter;
    	}
		public boolean accept(File dir, String name) {
			if(dir.isDirectory()) return true;
			if(name.lastIndexOf(".") != -1)
				if(name.substring(name.lastIndexOf(".")).equalsIgnoreCase("." + filter)) 
					return true;
			
			return false;
		}
		public boolean accept(File file) {
			
			String fileName = file.getAbsolutePath();
			
			if(file.isDirectory()) return true;
			if(fileName.lastIndexOf(".") != -1)
				if(fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase("." + filter))
					return true;
		
			return false;
		}
		public String getDescription() {
			if(filter != null && filter.length() > 0) return filter + " files";
			return "all files";
		}
    }
}
