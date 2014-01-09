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

import java.io.File;
import java.util.prefs.Preferences;

public class CCJFileChooser extends javax.swing.JFileChooser{
String preferencesKey;
final static String PREFERENCE_DOMAIN ="org/concord/swing/CCFileDialog";
final static String DEFAULT_NAME ="org.concord.swing.CCFileDialog";
private boolean preferencesKeyWasSet = false;

/**
 * Constructs a <code>CCJFileChooser</code> pointing to the directory
 * stored in the preferences for the key: org.concord.swing.CCFileDialog
 * 
 */

public CCJFileChooser() {
        this(DEFAULT_NAME);
    }
    
/**
 * Constructs a <code>CCJFileChooser</code> pointing to the directory
 * stored in the preferences for the key: preferencesKey
 * @param preferencesKey  a <code>String</code> key for storing/retreiving
 * last visited directory. could be class's name
 */

   public CCJFileChooser(String preferencesKey) {
	    super();
	    setPreferencesKey(preferencesKey);
	    checkingPreferences();
    }
    public CCJFileChooser(String currentDirectoryPath,String preferencesKey) {
	    super(currentDirectoryPath);
	    setPreferencesKey(preferencesKey);
        settingPreferences();
    }
    public CCJFileChooser(File currentDirectory,String preferencesKey) {
	    super(currentDirectory);
	    setPreferencesKey(preferencesKey);
        settingPreferences();
    }
    public CCJFileChooser(javax.swing.filechooser.FileSystemView fsv,String preferencesKey) {
	    super(fsv);
	    setPreferencesKey(preferencesKey);
        settingPreferences();
    }
    public CCJFileChooser(File currentDirectory, javax.swing.filechooser.FileSystemView fsv,String preferencesKey) {
	    super(currentDirectory,fsv);
	    setPreferencesKey(preferencesKey);
        settingPreferences();
    }
    public CCJFileChooser(String currentDirectoryPath, javax.swing.filechooser.FileSystemView fsv,String preferencesKey) {
	    super(currentDirectoryPath,fsv);
	    setPreferencesKey(preferencesKey);
        settingPreferences();
    }
        
    public void setCurrentDirectory(File dir) {
        super.setCurrentDirectory(dir);
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
                suggestedPath = getCurrentDirectory().getCanonicalPath();
            }catch(Throwable t){}
            if(suggestedPath == null) suggestedPath = System.getProperty("user.dir");
        }
        File suggestedFile = new File(suggestedPath);
        if(suggestedFile != null && suggestedFile.exists() && suggestedFile.isDirectory()){
            super.setCurrentDirectory(suggestedFile);
        }
    }
    protected void settingPreferences(){
        if(preferencesKey == null)  preferencesKey = DEFAULT_NAME;
        
        File currDirectory = getCurrentDirectory();
        String canonicalPath  = null;
        try{
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
    
}
