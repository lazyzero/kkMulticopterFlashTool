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
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DefaultComponentFramework
implements ComponentFramework
{
    protected Map managerMap = new HashMap();
    protected Manager defaultManager = new DefaultManager();
    
    public Component loadComponent(Node xmlNode)
    {
        NamedNodeMap xmlAttributes = xmlNode.getAttributes();
        Node attributeNode = xmlAttributes.getNamedItem("manager");
        String managerClassName = attributeNode.getNodeValue();
        Manager manager = registerManager(managerClassName);
        Component component = manager.loadComponent(xmlNode);
        manager.registerComponent(component);
        return component;
    }

    public Component loadComponent(Node xmlNode, Manager manager)
    {
        
        return manager.loadComponent(xmlNode);
    }

    public Node saveComponent(Document document, Component component)
    {
    	defaultManager.registerComponent(component);
        return defaultManager.saveComponent(document, component);
    }

    public Node saveComponent(Document document, Component component, Manager manager)
    {
        manager.registerComponent(component);
        return manager.saveComponent(document, component);
    }

    public Object getSchema(Class objectClass)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Manager registerManager(String managerClassName)
    {
    	try
    	{
			Class managerClass = Class.forName(managerClassName);
			return registerManager(managerClass);
		}
    	catch (ClassNotFoundException e)
    	{
			e.printStackTrace();
		}
        return null;
    }

    public Manager registerManager(Class managerClass)
    {
    	try
    	{
			Manager manager = (Manager) managerMap.get(managerClass.getName());
			if (manager == null)
			{
				manager = (Manager) managerClass.newInstance();
				managerMap.put(managerClass.getName(), manager);
			}
			return manager;
		}
    	catch (InstantiationException e)
    	{
			e.printStackTrace();
		}
    	catch (IllegalAccessException e)
    	{
			e.printStackTrace();
		}
        return null;
    }
    
    public class DefaultManager
    implements Manager
    {

        public Component loadComponent(Node xmlNode)
        {
            NamedNodeMap xmlAttributes = xmlNode.getAttributes();
            Node attributeNode = xmlAttributes.getNamedItem("class");
            String className = attributeNode.getNodeValue();
			try
			{
				Class componentClass = Class.forName(className);
				Component component = (Component) componentClass.newInstance();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (InstantiationException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
            return null;
        }

        public Node saveComponent(Document document, Component component)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public boolean registerComponent(Component component)
        {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
}