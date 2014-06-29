package de.lazyzero.kkMulticopterFlashTool.utils.avra;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

public abstract class AvraCommandLine {
	private String target;
	private String avra;
	private File workingDir;

	public AvraCommandLine(String target, File workingDir) {
		this.setTarget(target);
		this.setWorkingDir(workingDir);
		
		String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("windows")) {
			this.setAvra(getAvraDir()
					+ "avra.exe");
		} else {
			this.setAvra(getAvraDir() + "avra"); 
		}
	}
	
	public String getAvraDir() {
		String os = System.getProperty("os.name").toLowerCase();
		String path = "";

		if (os.contains("windows")) {
			path =  (System.getProperty("user.dir")
					+ "\\lib\\avra\\win\\");
		} else if (os.contains("mac")) {
			String basepath = System.getProperty("java.library.path");
			basepath = basepath.substring(0, basepath.indexOf(":")); 
			path = (basepath + "/lib/avra/mac/");
		}  else if (os.contains("linux")) {
			path = (System.getProperty("user.dir")+"/lib/avra/linux/");
		}
		return path;
	}
	
	public String getAvra() {
		return avra;
	}
	
	private void setAvra(String avra) {
		this.avra = avra;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public File getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	public abstract Vector<String> getCommandLine();

	public abstract void clean() throws IOException;

	public abstract File getHexFile() throws IOException;
}
