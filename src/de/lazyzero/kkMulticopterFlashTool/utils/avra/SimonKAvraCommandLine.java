package de.lazyzero.kkMulticopterFlashTool.utils.avra;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

public class SimonKAvraCommandLine extends AvraCommandLine {


	private File targetFile;

	/**
	 * @test -e $*.asm || ln -s tgy.asm $*.asm
	 * @echo "$(ASM) -fI -o $@ -D $*_esc -e $*.eeprom -d $*.obj $*.asm"
	 * @set -o pipefail; $(ASM) -fI -o $@ -D $*_esc -e $*.eeprom -d $*.obj $*.asm 2>&1 | grep -v 'PRAGMA directives currently ignored'
	 * @test -L $*.asm && rm -f $*.asm || true
	 */
	public SimonKAvraCommandLine(String target, File workingDir) {
		super(target, workingDir);
		createWorkingCopy();
	}

	private void createWorkingCopy() {
		File source = new File(getWorkingDir().getPath(), "tgy.asm");
		KKMulticopterFlashTool.getInstance().println("source: " + source.getAbsolutePath());
		this.targetFile = new File(getWorkingDir().getPath(), getTarget()+".asm");
		
		try {
			FileUtils.copyFile(source, this.targetFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void clean() throws IOException {
		FileUtils.deleteQuietly(this.targetFile);
		FileUtils.deleteQuietly(new File(getWorkingDir().getPath(), getTarget()+".cof"));
	}

	@Override
	public Vector<String> getCommandLine() {
		Vector <String> cmd = new Vector <String>();
        
        cmd.add(super.getAvra());
        
        cmd.add("-fI");
        
        cmd.add("-o");
        cmd.add(getTarget());
        
        cmd.add("-D");
        cmd.add(getTarget() + "_esc");
        
        cmd.add("-e");
        cmd.add(getTarget() + ".eeprom");
         
        cmd.add("-d");
        cmd.add(getTarget() + ".obj");
        
        cmd.add(getTarget() + ".asm");
        
		return cmd;
	}

	@Override
	public File getHexFile() throws IOException {
		return new File(getWorkingDir().getPath(), getTarget()+".hex");
	}

	
}
