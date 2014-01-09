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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


public class HelpView
extends JPanel
{
    protected JEditorPane helpHTML;
    protected JEditorPane glossaryHTML;
    protected JScrollPane helpScroll;
    
    public HelpView()
    {		
        setLayout(new BorderLayout());
        helpHTML = new JEditorPane();
        helpScroll = new JScrollPane(helpHTML);

        add(helpScroll, "Center");
        
        glossaryHTML = new JEditorPane();
        glossaryHTML.setContentType("text/html");
        glossaryHTML.setEditable(false);
        
        add(glossaryHTML, "South");

        glossaryHTML.setBackground(new Color(200,200,200));
        
        HyperlinkListener listener = new LocalHyperlinkAdapter();
        helpHTML.addHyperlinkListener(listener);
    }
    
    public void setPage(URL url)
    {
        try
        {
            helpHTML.setPage(url);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
    
    public static void main(String [] args)
    {
        javax.swing.JFrame frame = new javax.swing.JFrame("Test HelpView");
        frame.addWindowListener(new WindowCloser());
        HelpView view = new HelpView();
        try
        {
            view.setPage(new URL(args[0]));
            frame.getContentPane().add(view, "Center");
            frame.setSize(800, 600);
            frame.setVisible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    static class WindowCloser
    extends WindowAdapter
    {
        public void windowClosing(java.awt.event.WindowEvent event)
        {
        	System.exit(0);;
        }
    }
    
    public class LocalHyperlinkAdapter
    implements HyperlinkListener
    {
        public void hyperlinkUpdate(HyperlinkEvent event)
        {
            try
            {
                glossaryHTML.setVisible(false);
                if(event.getEventType().equals(HyperlinkEvent.EventType.ENTERED) || 
                   event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                {
                    glossaryHTML.setPage(event.getURL());
                    glossaryHTML.setVisible(true);
                }                    
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
    }
}

