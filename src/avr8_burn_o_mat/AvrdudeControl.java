/*
    AVR8 Burn-O-Mat
 
    Copyright (C) 2007  Torsten Brischalle

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
 */
package avr8_burn_o_mat;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.prefs.Preferences;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

public class AvrdudeControl {
    
    public enum FileFormat { RAW, INTEL_HEX, MOTOROLA_S_RECORD, AUTO_DETECT }
    
    private String  m_avrdude;
    private String  m_avrdudeConfig;
    private String  m_port = "/dev/parport0";
    private Boolean m_disableAutoErase = false;
    private Boolean m_exitSpecActivated = false;
    private Boolean m_leftResetActivated = false;
    private Boolean m_leftVccActivated = false;
    private Boolean m_disableFuseCheck = false;
    
    private InterfaceTextOutput m_intfTextOutput;
    
    private AvrdudeProgrammer   m_programmer = new AvrdudeProgrammer("stk200","par","STK200");
    private AvrdudeConfigFile   m_avrdudeConfigFile = new AvrdudeConfigFile();
    
    private String              m_additionalOptions = "";
 
    
    /** Creates a new instance of AVRDUDECtrl 
     * @param isUSBtiny */
    public AvrdudeControl(InterfaceTextOutput intfTextOutput, boolean isUSBtiny) throws IOException {
        m_intfTextOutput = intfTextOutput;
        
            
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch");
        System.out.println("OS for avrdude settings is: " + os);
        
        if (os.contains("windows") && !isUSBtiny) {
            m_avrdude = System.getProperty("user.dir")+"\\lib\\avrdude\\windows\\avrdude.exe";
            m_avrdudeConfig = System.getProperty("user.dir")+"\\lib\\avrdude\\windows\\avrdude.conf";
        } else if (os.contains("windows") && isUSBtiny) {
        	m_avrdude = System.getProperty("user.dir")+"\\lib\\avrdude\\windows\\usbtiny\\avrdude.exe";
            m_avrdudeConfig = System.getProperty("user.dir")+"\\lib\\avrdude\\windows\\usbtiny\\avrdude.conf";
        } else if (os.contains("mac") && !isUSBtiny) {
        	String path = System.getProperty("java.library.path");
        	path = path.substring(0, path.indexOf(":"));
        	m_avrdude = path + "/lib/avrdude/mac/avrdude";
            m_avrdudeConfig = path + "/lib/avrdude/mac/avrdude.conf";
        } else if (os.contains("mac") && isUSBtiny) {
        	String path = System.getProperty("java.library.path");
        	path = path.substring(0, path.indexOf(":"));
        	m_avrdude = path + "/lib/avrdude/mac/usbtiny/avrdude";
            m_avrdudeConfig = path + "/lib/avrdude/mac/usbtiny/avrdude.conf";
        } else if (os.contains("linux")) {
        	if (arch.equals("amd64")) {
    			m_avrdude = System.getProperty("user.dir")+"/lib/avrdude/linux64/avrdude";
    			m_avrdudeConfig = System.getProperty("user.dir")+"/lib/avrdude/linux64/avrdude.conf";
        	} else {//32-bit branch
        		if (!isUSBtiny) {
	        		m_avrdude = System.getProperty("user.dir")+"/lib/avrdude/linux/avrdude";
	        	    m_avrdudeConfig = System.getProperty("user.dir")+"/lib/avrdude/linux/avrdude.conf";
        		} else {
        			m_avrdude = System.getProperty("user.dir")+"/lib/avrdude/linux/usbtiny/avrdude";
                    m_avrdudeConfig = System.getProperty("user.dir")+"/lib/avrdude/linux/usbtiny/avrdude.conf";
        		}
        	}
        }
        loadPreferences();
    }
    
    
    
    public Vector <AvrdudeProgrammer> getProgrammerList() {
        return m_avrdudeConfigFile.getProgrammerList();
    }
    
    public void savePreferences() {
        
        try {
            Preferences prefs = Preferences.userNodeForPackage(this.getClass());
            
            prefs.put("Avrdude",m_avrdude);
            prefs.put("AvrdudeConfig",m_avrdudeConfig);
            
            prefs.put("Programmer",m_programmer.getId());
            prefs.put("Port",m_port);
            prefs.put("AdditionalOptions",m_additionalOptions);
            
            prefs.putBoolean("DisableAutoErase",m_disableAutoErase);
            prefs.putBoolean("ExitSpecActivated",m_exitSpecActivated);
            prefs.putBoolean("LeftResetActivated",m_leftResetActivated);
            prefs.putBoolean("LeftVccActivated",m_leftVccActivated);
            prefs.putBoolean("DisableFuseCheck",m_disableFuseCheck);
            
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Error while saving preferences: " + e.getMessage());
        }
        
    }
    
    public void loadPreferences() {
        
        try {
            Preferences prefs = Preferences.userNodeForPackage(this.getClass());
            
//            m_avrdude = prefs.get("Avrdude",m_avrdude);
//            m_avrdudeConfig = prefs.get("AvrdudeConfig",m_avrdudeConfig);
            
            m_port = prefs.get("Port","/dev/parport0");
            
            m_additionalOptions = prefs.get("AdditionalOptions","");
            
            m_disableAutoErase = prefs.getBoolean("DisableAutoErase",false);
            m_exitSpecActivated = prefs.getBoolean("ExitSpecActivated",false);
            m_leftResetActivated = prefs.getBoolean("LeftResetActivated",false);
            m_leftVccActivated = prefs.getBoolean("LeftVccActivated",false);

            m_disableFuseCheck = prefs.getBoolean("DisableFuseCheck",false);
            
            String programmer = prefs.get("Programmer","");
            
            m_avrdudeConfigFile.readAvrdudeConfigFile(m_avrdudeConfig);
            
            Vector <AvrdudeProgrammer> progList = m_avrdudeConfigFile.getProgrammerList();
            
            for (AvrdudeProgrammer p : progList) {
                if (p.equals(programmer)) {
                    m_programmer = p;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error while loading preferences: " + e.getMessage());
        }
        
    }
    
    public String getAvrdude() {
        return m_avrdude;
    }
    
    public void setAvrdude(String avrdude) {
        m_avrdude = avrdude;
    }
    
    public AvrdudeProgrammer getProgrammer() {
        return m_programmer;
    }
    
    public void setProgrammer(AvrdudeProgrammer programmer) {
        m_programmer = programmer;
    }
    
    public String getPort() {
        return m_port;
    }
    
    public void setPort(String port) {
        m_port = port;
    }
    
    public String getAdditionalOptions() {
        return m_additionalOptions;
    }
    
    public void setAdditionalOptions(String additionalOptions) {
        m_additionalOptions = additionalOptions;
    }
    
    public String getAvrdudeConfig() {
        return m_avrdudeConfig;
    }
    
    public void setAvrdudeConfig(String avrdudeConfig) {
        m_avrdudeConfig = avrdudeConfig;
    }
    
    public void addAdditionalOption(String option) {
    	m_additionalOptions = m_additionalOptions.concat(m_additionalOptions.length()>0?" "+option:option);
    }
    
    public void addAndUpdateAdditionalOptions(String additionalOptions) {
    	
    	if (null != additionalOptions){
    		String[] addOptions = additionalOptions.split(" ");
    		
    		for (int i = 0; i < addOptions.length; i++) {
    			if (addOptions[i].startsWith("-")){
    				if (m_additionalOptions.contains(addOptions[i])) {
    					//replace existing option
    					if (i < addOptions.length-1) {
    						//check if the option has parameter
    						if(!addOptions[i+1].startsWith("-")) {
    							int start = m_additionalOptions.indexOf(addOptions[i]);
    							int end = (start==0?m_additionalOptions.indexOf("-", 1):m_additionalOptions.indexOf("-", start)) -1;
    							m_additionalOptions = (start==0?"":m_additionalOptions.substring(0, start)) + (addOptions[i] + " " + addOptions[i+1]) + m_additionalOptions.substring(end);
    						} 
    					}
    				} else {
    					if (i < addOptions.length-1) {
    						//check if the option has parameter
    						if(!addOptions[i+1].startsWith("-")) {
    							addAdditionalOption(addOptions[i] + " " + addOptions[i+1]);
    						} else {
    							addAdditionalOption(addOptions[i]);
    						}
    					} else {
    						addAdditionalOption(addOptions[i]);
    					}
    				}
    			}
    		}
    	}
	}
    
    private void addAdditionalOptions(Vector <String> cmd) {
        
        String[ ] options = m_additionalOptions.split(" ");
        
        for (String o : options) {
            cmd.add(o);
        }
    }
    
    private Vector <String> getCmd(AVR avr) {
        
        Vector <String> cmd = new Vector <String>();
        
        cmd.add(m_avrdude);

        //cmd.add("-q");

        if (m_disableFuseCheck)
            cmd.add("-u");
        
        if (m_avrdudeConfig.length() > 0) {
            cmd.add("-C");
            cmd.add(m_avrdudeConfig);
        }
        
        cmd.add("-p");
        cmd.add(avr.getCaptionAliasFree());
        
        cmd.add("-P");
        cmd.add(m_port);
        
        
        cmd.add("-c");
        cmd.add(m_programmer.getId());
        
        if (m_disableAutoErase) {
            cmd.add("-D");
        }

        if (m_exitSpecActivated) {
            String exitSpec;

            if (m_leftResetActivated)
                exitSpec = "reset";
            else
                exitSpec = "noreset";

            if (m_leftVccActivated)
                exitSpec += ",vcc";
            else
                exitSpec += ",novcc";

            cmd.add("-E");
            cmd.add(exitSpec);
        }

        addAdditionalOptions(cmd);
        
        return cmd;
    }
    
    public void readFuses(AVR avr, String path) throws Exception {
        
        // Kommandostring zusammen bauen
        
        Vector <String> cmd = getCmd(avr);
        
//        if (!m_disableFuseCheck)
//            cmd.add("-u");
     
        cmd.add("-U");
        cmd.add(String.format("lfuse:r:%s:r", path + "/lfuse.hex"));
    
        cmd.add("-U");
        cmd.add(String.format("hfuse:r:%s:r", path + "/hfuse.hex"));
        
        // AVRDUDE starten
        
        int exitValue = startAvrdudeFusesReadWrite(cmd);
        
        if (exitValue != 0) {
            throw new Exception("Error reading fuses!");
        }
        
		// Fuses aus Datei lesen

		FileInputStream fs = new FileInputStream(path + "/lfuse.hex");
		int data = fs.read();
		avr.setLfuse(data + "");
		fs.close();

		fs = new FileInputStream(path + "/hfuse.hex");
		data = fs.read();
		avr.setHfuse(data + "");
		fs.close();

		
    }
    
    public void writeFuses(AVR avr) throws Exception {
        
       
        // Kommandostring zusammen bauen
        Vector <String> cmd = getCmd(avr);
//        
//        if (!m_disableFuseCheck)
//            cmd.add("-u");

        cmd.add("-U");
        cmd.add(String.format("lfuse:w:%s:m",avr.getLfuse()));
        
        cmd.add("-U");
        cmd.add(String.format("hfuse:w:%s:m",avr.getHfuse()));
        
        if (avr.getEfuse() != null) {
        	cmd.add("-U");
        	cmd.add(String.format("efuse:w:%s:m", avr.getEfuse()));
        }
        
        
        // AVRDUDE starten
        int exitValue = startAvrdudeFusesReadWrite(cmd);
        
        if (exitValue != 0) {
            throw new Exception("Error writing fuses!");
        }
        
    }
    
    public void verifyFuses(AVR avr) throws Exception {
        
        // Kommandostring zusammen bauen
        Vector <String> cmd = getCmd(avr);
        
        //if (!m_disableFuseCheck)
        //    cmd.add("-u");

            
            cmd.add("-U");
            cmd.add(String.format("lfuse:v:0x%s:m",avr.getLfuse()));
            
            cmd.add("-U");
            cmd.add(String.format("hfuse:v:0x%s:m",avr.getHfuse()));
        
        // AVRDUDE starten
        int exitValue = startAvrdudeFusesReadWrite(cmd);
        
        if (exitValue != 0) {
            throw new Exception("Error verifying fuses!");
        }
        
    }
    
    public void readFlash(AVR avr, String filename, FileFormat fileFormat) throws Exception {
        
        // Kommandostring zusammen bauen
        
        Vector <String> cmd = getCmd(avr);
        
        cmd.add("-U");
        cmd.add(String.format("flash:r:%s:%s",filename,FileFormat2AvrdudeOption(fileFormat)));
        
        // AVRDUDE starten
        
        int exitValue = startAvrdudeFlashReadWrite(cmd);
        
        if (exitValue != 0) {
            throw new Exception("Error reading flash!");
        }
        
    }
    
    public void writeFlash(AVR avr, String filename, FileFormat fileFormat) throws Exception {
        
        // Kommandostring zusammen bauen
        
        Vector <String> cmd = getCmd(avr);
        
        cmd.add("-U");
        cmd.add(String.format("flash:w:%s:%s",filename,FileFormat2AvrdudeOption(fileFormat)));
        
        // AVRDUDE starten
        
        int exitValue = startAvrdudeFlashReadWrite(cmd);
        
        if (exitValue != 0) {
            throw new Exception("Error writing flash!");
        }
        
    }
    
    public void verifyFlash(AVR avr, String filename, FileFormat fileFormat) throws Exception {
        
        // Kommandostring zusammen bauen
        
        Vector <String> cmd = getCmd(avr);
        
        cmd.add("-U");
        cmd.add(String.format("flash:v:%s:%s",filename,FileFormat2AvrdudeOption(fileFormat)));
        
        // AVRDUDE starten
        
        int exitValue = startAvrdudeFlashReadWrite(cmd);
        
        if (exitValue != 0) {
            throw new Exception("Error verifying flash!");
        }
        
    }
    
    public void readEEPROM(AVR avr, String filename, FileFormat fileFormat) throws Exception {
        
        // Kommandostring zusammen bauen
        
        Vector <String> cmd = getCmd(avr);
        
        cmd.add("-U");
        cmd.add(String.format("eeprom:r:%s:%s",filename,FileFormat2AvrdudeOption(fileFormat)));
        
        // AVRDUDE starten
        
        int exitValue = startAvrdudeFlashReadWrite(cmd);
        
        if (exitValue != 0) {
            throw new Exception("Error reading EEPROM!");
        }
        
    }
    
    public void writeEEPROM(AVR avr, String filename, FileFormat fileFormat) throws Exception {
        
        // Kommandostring zusammen bauen
        
        Vector <String> cmd = getCmd(avr);
        
        cmd.add("-U");
        cmd.add(String.format("eeprom:w:%s:%s",filename,FileFormat2AvrdudeOption(fileFormat)));
        
        // AVRDUDE starten
        
        int exitValue = startAvrdudeFlashReadWrite(cmd);
        
        if (exitValue != 0) {
            throw new Exception("Error writing EEPROM!");
        }
        
    }
    
    public void verifyEEPROM(AVR avr, String filename, FileFormat fileFormat) throws Exception {
        
        // Kommandostring zusammen bauen
        
        Vector <String> cmd = getCmd(avr);
        
        cmd.add("-U");
        cmd.add(String.format("eeprom:v:%s:%s",filename,FileFormat2AvrdudeOption(fileFormat)));
        
        // AVRDUDE starten
        
        int exitValue = startAvrdudeFlashReadWrite(cmd);
        
        if (exitValue != 0) {
            throw new Exception("Error verifying EEPROM!");
        }
        
    }
    
    private int startAvrdudeFusesReadWrite(Vector <String> cmd) throws Exception {
        
        //m_intfTextOutput.clearText();
    	m_intfTextOutput.println("");
        
        for (String s : cmd) {
            m_intfTextOutput.print(s + " ");
        }
       
        m_intfTextOutput.println("");
        
        String [] cmdArray = new String [cmd.size()];
        cmd.toArray(cmdArray);
        
       
        
        final Process p = Runtime.getRuntime().exec(cmdArray);
        new Thread() {
        	public void run() {
        		
        		int line;
        		BufferedInputStream bis =  new BufferedInputStream(p.getErrorStream());
        		
        		try {
					while ((line = bis.read()) != -1) {
						m_intfTextOutput.print(((char)line)+"");
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
						m_intfTextOutput.print(((char)line)+"");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }.start();
        KKMulticopterFlashTool.getLogger().log(Level.INFO, m_intfTextOutput.getText());
        System.out.println("start waiting for the process to finish.");
        p.waitFor();
        System.out.println("process finished.");
        return p.exitValue();
    }
    
    private int startAvrdudeFlashReadWrite(Vector <String> cmd) throws Exception {
        
        m_intfTextOutput.println("");
        
        for (String s : cmd) {
            m_intfTextOutput.print(s + " ");
        }
        m_intfTextOutput.println("");
        
        String [] cmdArray = new String [cmd.size()];
        cmd.toArray(cmdArray);
        
        final Process p = Runtime.getRuntime().exec(cmdArray);
        new Thread() {
        	public void run() {
        		
        		int line;
        		BufferedInputStream bis =  new BufferedInputStream(p.getErrorStream());
        		
        		try {
					while ((line = bis.read()) != -1) {
						m_intfTextOutput.print(((char)line)+"");
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
						m_intfTextOutput.print(((char)line)+"");
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
    
    private String FileFormat2AvrdudeOption(FileFormat fileFormat) {
        switch (fileFormat) {
            case RAW: return "r";
            case INTEL_HEX: return "i";
            case MOTOROLA_S_RECORD: return "s";
            case AUTO_DETECT: return "a";
            default:
                assert false;
                return "";
        }
    }
    
    static public FileFormat String2FileFormat(String fileFormat) {
        if (fileFormat.equals("Intel Hex"))
            return FileFormat.INTEL_HEX;
        else if (fileFormat.equals("Motorola S-record"))
            return FileFormat.MOTOROLA_S_RECORD;
        else if (fileFormat.equals("raw"))
            return FileFormat.RAW;
        else 
            return FileFormat.AUTO_DETECT;
    }
    
    public Boolean getDisableAutoErase() {
        return m_disableAutoErase;
    }
    
    public void setDisableAutoErase(Boolean disableAutoErase) {
        m_disableAutoErase = disableAutoErase;
    }
    
    public Boolean getExitSpecActivated() {
        return m_exitSpecActivated;
    }

    public void setExitSpecActivated(Boolean exitSpecActivated) {
        m_exitSpecActivated = exitSpecActivated;
    }

    public Boolean getLeftResetActivated() {
        return m_leftResetActivated;
    }
    
    public void setLeftResetActivated(Boolean leftResetActivated) {
        m_leftResetActivated = leftResetActivated;
    }
    
    public Boolean getLeftVccActivated() {
        return m_leftVccActivated;
    }
    
    public void setLeftVccActivated(Boolean leftVccActivated) {
        m_leftVccActivated = leftVccActivated;
    }

    public Boolean getDisableFuseCheck() {
        return m_disableFuseCheck;
    }

    public void setDisableFuseCheck(Boolean disableFuseCheck) {
        m_disableFuseCheck = disableFuseCheck;
    }



	
}
