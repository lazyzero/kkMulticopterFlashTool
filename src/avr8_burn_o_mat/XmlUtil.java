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

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XmlUtil {
    
    public static String getAttr(Node node, String name) throws DOMException {
        
        assert node != null;
        
        try {
            NamedNodeMap attributes = node.getAttributes();
            if (attributes == null)
                throw new Exception("");
            
            Node attr = attributes.getNamedItem(name);
            if (attr == null)
                throw new Exception("");
            
            String value = attr.getNodeValue();
            if (value == null)
                throw new Exception("");
            
            return value;
            
        } catch (Exception e) {
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                                   String.format("attribute %s is missing at node %s",
                                                 name,
                                                 node.getNodeName()));
        }
        
    }
}
