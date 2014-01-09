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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AvrdudeConfigFile {
    
    private Vector <AvrdudeProgrammer> m_avrdudeProgrammerList = new Vector <AvrdudeProgrammer>();
    
    private enum ReadState { NOTHING, PROGRAMMER };
    
    /** Creates a new instance of AvrdudeConfigFileReader */
    public AvrdudeConfigFile() {
        
        m_avrdudeProgrammerList = new Vector <AvrdudeProgrammer>();
    }
    
    public Vector <AvrdudeProgrammer> getProgrammerList() {
        return m_avrdudeProgrammerList;
    }
    
   
    
    public static String getConfigFileByOS() {
    	String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("windows")) {
        	return System.getProperty("user.dir")+"\\lib\\avrdude\\windows\\avrdude.conf";
        } else if (os.contains("mac")) {
        	String path = System.getProperty("java.library.path");
        	path = path.substring(0, path.indexOf(":"));
        	return path+"/lib/avrdude/mac/avrdude.conf";
        } else if (os.contains("linux")) {
        	return System.getProperty("user.dir")+"/lib/avrdude/linux/avrdude.conf";
        }
		return null;
	}
    
  
    
    public void readAvrdudeConfigFile(String filename) throws IOException {
        
        m_avrdudeProgrammerList.clear();
        
        BufferedReader  r = new BufferedReader(new FileReader(filename));
        ReadState       readState = ReadState.NOTHING;
        String          line;
        Matcher         m;
        
        Pattern patProgStart = Pattern.compile("\\s*programmer\\s*");
        Pattern patProgEnd   = Pattern.compile("\\s*;\\s*");
        Pattern patProgVal1  = Pattern.compile("\\s*(\\w+)\\s*=\\s*\"([^\"]+)\"\\s*;\\s*");
        Pattern patProgVal2  = Pattern.compile("\\s*(\\w+)\\s*=\\s*([^\\s;]+)\\s*;\\s*");
        
        String  progId = null,
                progType = null,
                progDesc = null;
        
        line = r.readLine();
        while (line != null) {
            
            switch (readState) {
                case NOTHING:
                    m = patProgStart.matcher(line);
                    if (m.matches()) {
                        readState = ReadState.PROGRAMMER;
                        progId = null;
                        progType = null;
                        progDesc = null;
                    }
                    break;
                    
                case PROGRAMMER:
                    m = patProgEnd.matcher(line);
                    if (m.matches()) {
                        readState = ReadState.NOTHING;
                        
                        if ((progId != null) && (progType != null) && (progDesc != null)) {
                            m_avrdudeProgrammerList.add(new AvrdudeProgrammer(progId,progType,progDesc));
                        }
                            
                    } else {
                        String key = null, val = null;
                        
                        m = patProgVal1.matcher(line);
                        if (m.matches()) {
                            key = m.group(1);
                            val = m.group(2);
                        } else {
                            m = patProgVal2.matcher(line);
                            if (m.matches()) {
                                key = m.group(1);
                                val = m.group(2);
                            }                            
                        }
                        
                        if (key != null) {
                            assert val != null;
                            
                            key = key.toLowerCase();
                            
                            if (key.equals("id"))
                                progId = val;
                            else if (key.equals("desc"))
                                progDesc = val;
                            else if (key.equals("type"))
                                progType = val;
                        }
                    }
                    break;
            }
            
            line = r.readLine();
        }
        
        r.close();
    }

	
}
