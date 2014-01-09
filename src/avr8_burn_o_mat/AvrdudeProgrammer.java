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

public class AvrdudeProgrammer implements Comparable <AvrdudeProgrammer> {
    
    private String  m_id;
    private String  m_type;
    private String  m_desc;
    
    /** Creates a new instance of AvrdudeProgrammer */
    public AvrdudeProgrammer(String id, String type, String desc) {

        assert id != null;
        assert type != null;
        assert desc != null;
        
        m_id = id;
        m_type = type;
        m_desc = desc;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)",getShortDesc(), getId());
    }
    
    private String getShortDesc() {
		String desc = getDesc();
		if (desc.length()>60) {
			desc = desc.substring(0, 60);
		}
		return desc;
	}

	@Override
    public boolean equals(Object o) {
        
        if (o == null)
            return false;
        
        try {
            
            AvrdudeProgrammer p = (AvrdudeProgrammer)o;
            return m_id.equals(p.m_id) && m_type.equals(p.m_type) && m_desc.equals(p.m_desc);
            
        } catch (ClassCastException ex1) {
            try {
                
                String s = (String)o;
                return s.equals(getId());
                
            } catch (ClassCastException ex2) {
                return false;
            }
        }
    }
    
    public int compareTo(AvrdudeProgrammer p) {
        return getId().compareTo(p.getId());
    }
    
    public String getId() {
        return m_id;
    }
    
    public String getType() {
        return m_type;
    }
    
    public String getDesc() {
        return m_desc;
    }

	public boolean isSerial() {
		if (m_type.trim().equals("serbb")) return true;
		return false;
	}
	
	public boolean isParallel() {
		if (m_type.trim().equals("par")) return true;
		return false;
	}
    
}
