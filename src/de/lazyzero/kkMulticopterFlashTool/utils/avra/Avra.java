/**
 * 
 * @author Christian Moll
 *
 */
package de.lazyzero.kkMulticopterFlashTool.utils.avra;

import static lu.tudor.santec.i18n.Translatrix._;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

public class Avra {
	
	private KKMulticopterFlashTool parent;
	private AvraCommandLine commandLine;

	
	public Avra(KKMulticopterFlashTool parent, AvraCommandLine commandline) {
		this.parent = parent;
		this.commandLine = commandline;
	}
	
	public boolean compile() {
		boolean success = true;
		
		try {
			int exitValue = runCommand();
			if (exitValue != 0) {
				success = false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}
	
	public void clean() {
		try {
			this.commandLine.clean();
		} catch (IOException e) {
			parent.print(_("compile.avra.unable2delete"));
			e.printStackTrace();
		}
	}
	
    private int runCommand() throws Exception {
    	parent.println("working dir: " + this.commandLine.getWorkingDir().getAbsolutePath());
    	Vector<String> cmd = this.commandLine.getCommandLine();
        
        for (String s : cmd) {
        	parent.print(s + " ");
        }
        parent.println("");
        
        String [] cmdArray = new String [cmd.size()];
        cmd.toArray(cmdArray);
        
        final Process p = Runtime.getRuntime().exec(cmdArray, null, this.commandLine.getWorkingDir());
        new Thread() {
        	public void run() {
        		
        		int line;
        		BufferedInputStream bis =  new BufferedInputStream(p.getErrorStream());
        		
        		try {
					while ((line = bis.read()) != -1) {
						parent.print(((char)line)+"");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }.start();
        new Thread() {
        	public void run() {
        		
        		int line;
        		BufferedInputStream bis =  new BufferedInputStream(p.getInputStream());
        		
        		try {
					while ((line = bis.read()) != -1) {
						parent.print(((char)line)+"");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }.start();
        p.waitFor();
        return p.exitValue();
    }

	public File getHexFile() {
		try {
			return commandLine.getHexFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
