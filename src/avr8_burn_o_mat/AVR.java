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

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;


public class AVR {
    
	public static final int LOWER_NIBBLE = 0;
	public static final int HIGHER_NIBBLE = 1;
	public static final String HFUSE = "hfuse";
	public static final String LFUSE = "lfuse";
	public static final String EFUSE = "efuse";
	private String lfuse; 
	private String hfuse;
	private String efuse;
	
    private String m_name;
    
    private String m_caption;
	private String desc;
	private int eepromSize;
	private String hfuseMask;
	private String lfuseMask;
	private int bootloaderAddress = 1024;
    
  
    
    /** Creates a new instance of AVR */
    public AVR() {
    }
    
    public AVR(String name, String caption) {
        m_name = name;
        m_caption = caption;
    }
    
    public AVR(String name, String desc, String caption, int eepromSize, String lfuse, String hfuse) {
       this(name, desc, caption, eepromSize, lfuse, hfuse, null);
    }
    
    public AVR(String name, String desc, String caption, int eepromSize, String lfuse, String hfuse, String efuse) {
    	 m_name = name;
         m_caption = caption;
         this.desc = desc;
         this.lfuse = lfuse;
         this.hfuse = hfuse;
         this.setEfuse(efuse);
         this.eepromSize = eepromSize;
       
    }

    public AVR(String name, String desc, String caption, int eepromSize, String lfuse, String hfuse, int bootloaderAddress) {
        m_name = name;
        m_caption = caption;
        this.desc = desc;
        this.lfuse = lfuse;
        this.hfuse = hfuse;
        this.eepromSize = eepromSize;
        this.bootloaderAddress = bootloaderAddress;
      
    }

	public String getDescription() {
    	return desc;
    }

	@Override
	public String toString() {
		String name = this.getName() + (null!=this.getDescription()?" (" + this.getDescription() + ")":"");
		return name;
	}

	public String getName() {
        return m_name;
    }
	
	public String getCaption() {
	        return m_caption;
	    }

    
    public String getCaptionAliasFree() {
    	if (m_caption.equals(KKMulticopterFlashTool.KKPLUSBOOT)) {
    		return "m168p";
    	} else if (m_caption.equals(KKMulticopterFlashTool.FLYCAM_BLACKBOARD_P)) {
    		return "m168p";
    	} else if (m_caption.equals(KKMulticopterFlashTool.FLYCAM_BLACKBOARD)) {
    		return "m168";
    	} else if (m_caption.equals(KKMulticopterFlashTool.ESC)) {
    		return "m8";
    	} else if (m_caption.equals(KKMulticopterFlashTool.WIIESC)) {
        	return "m8";
    	} else if (m_caption.equals(KKMulticopterFlashTool.WIIESC_EEPROM)) {
            return "m8";
    	} else if (m_caption.equals(KKMulticopterFlashTool.WIIESC_BOOTLOADER)) {
    		return "m8"; 
    	} else if (m_caption.equals(KKMulticopterFlashTool.ESCBOOTLOADER)) {
    		return "m8";
    	} else if (m_caption.equals(KKMulticopterFlashTool.ESC_LIGHT)) {
    		return "m8";
    	} else if (m_caption.equals(KKMulticopterFlashTool.SMARTLCD)) {
    		return "m48p";
    	} else if (m_caption.equals(KKMulticopterFlashTool.i86)) {
    		return "m168p";
    	} else if (m_caption.equals(KKMulticopterFlashTool.i86l)) {
    		return "m168p";
    	} else if (m_caption.equals(KKMulticopterFlashTool.OPENFLIGHT_V1)) {
    		return "m168p";
    	} else if (m_caption.equals(KKMulticopterFlashTool.OPENFLIGHT_V2)) {
    		return "m168p";
    	} else if (m_caption.equals(KKMulticopterFlashTool.OPENFLIGHT_V2_SM)) {
    		return "m168p";
    	} else if (m_caption.equals(KKMulticopterFlashTool.OPENFLIGHTPROG)) {
    		return "m8";
    	}
        return m_caption;
    }

	/**
	 * @return the lfuse
	 */
	public String getLfuse() {
		return lfuse;
	}

	/**
	 * @param lfuse the lfuse to set
	 */
	public void setLfuse(String lfuse) {
		this.lfuse = lfuse;
	}

	/**
	 * @return the hfuse
	 */
	public String getHfuse() {
		return hfuse;
	}

	/**
	 * @param hfuse the hfuse to set
	 */
	public void setHfuse(String hfuse) {
		this.hfuse = hfuse;
	}

	public int getEepromSize() {
		return eepromSize;
	}

	public void setEepromSize(int eepromSize) {
		this.eepromSize = eepromSize;
	}

	public int getBootloaderAddress() {
		return bootloaderAddress;
	}

	public void setBootloaderAddress(int bootloaderAddress) {
		this.bootloaderAddress = bootloaderAddress;
	}

	public void changeFuseNibble(String fuse, int nibble, String exchangeBy) {
		String value = "";
		if (fuse.equals(HFUSE)) {
			value = getHfuse();
		} else if (fuse.equals(LFUSE)) {
			value = getLfuse();
		}
		String fuseAsString = Integer.toHexString(Integer.parseInt(value));
		
		if (nibble == LOWER_NIBBLE) {
			fuseAsString = fuseAsString.substring(0, 1).concat(exchangeBy);
		} else {
			fuseAsString = exchangeBy.concat(fuseAsString.substring(1));
		}
		
		
		if (fuse.equals(HFUSE)) {
			setHfuse(Integer.parseInt(fuseAsString, 16)+"");
		} else if (fuse.equals(LFUSE)) {
			setLfuse(Integer.parseInt(fuseAsString, 16)+"");
		}
		
		
	}

	public String getEfuse() {
		return efuse;
	}

	public void setEfuse(String efuse) {
		this.efuse = efuse;
	}
    

    
}
