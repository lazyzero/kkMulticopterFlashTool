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

package org.concord.swing.about;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class AboutBox extends JButton 
    implements ActionListener
{
	Component parent;
    JFrame frame;
    String interactiveName;
    String aboutFile;
	static String TYPE = "text/html";
    
    public AboutBox(String interactiveName)
    {
        this.interactiveName = interactiveName;
        this.setText("About");
        this.setToolTipText("About " + interactiveName);
        this.setContentAreaFilled(false);       
        //this.setPreferredSize(new Dimension(80,24));
        
        this.addActionListener(this);
        SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
                setUpFrame();
        	}
        });
    }
    
    private void setUpFrame() {
    	parent = this.getParent();
        JButton close;
        JTabbedPane content;
        
        frame = new JFrame("About " + interactiveName);
        frame.setSize(600,400);
    	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    	frame.setLocation(d.width/2 - 225, d.height/2 - 200);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        if(parent != null)
        	frame.setLocation(parent.getLocation());
        
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        JPanel closePanel = new JPanel(new FlowLayout());
        close = new JButton("Close");
        closePanel.add(close);
        close.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.hide();
            }
        });
        
        contentPane.add(closePanel, BorderLayout.SOUTH);
        
        try {
            //content= AboutTextFromXML.getTabbedPane("xml/" + removeSpaces(interactiveName) + ".xml");
			content= AboutTextFromHTML.getTabbedPane(removeSpaces(interactiveName));
			content.add(getSystemInformationTab(), "System Information");
			
           content.setPreferredSize(new Dimension(100, 500));
    
            contentPane.add(content, BorderLayout.CENTER);
        } catch (Exception e) {
            System.err.println("Exception caught parsing file");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void actionPerformed (ActionEvent e) 
    {
    	String message = "Your java vm is not up-todate.\n" +
    			"Version 1.4 or higher is recommended.\n" +
    			"Do you want to continue?";
        if(System.getProperty("java.vm.version").compareTo("1.3.1") <= 0) {
        	int retVal = JOptionPane.showConfirmDialog(null, message, "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
        	if(retVal == JOptionPane.CANCEL_OPTION || 
        			retVal == JOptionPane.CLOSED_OPTION ||
        			retVal == JOptionPane.NO_OPTION)
        		return;
            //JOptionPane.showMessageDialog(this, 
            	//	"You need to upgrage java vm to 1.4!",
            		//"Warning",JOptionPane.INFORMATION_MESSAGE );
            //return;
        }
        frame.show();
    }

  public static String readTextFromJar(String s) {
    String thisLine;;
    StringBuffer everything = new StringBuffer();
    try {
      InputStream is = AboutBox.class.getResourceAsStream(s);
      BufferedReader br = new BufferedReader
         (new InputStreamReader(is));
      while ((thisLine = br.readLine()) != null) {  
       //  System.out.println(thisLine);
         everything.append(thisLine);
         }
      }
    catch (Exception e) {
      e.printStackTrace();
      }
      return everything.toString();
  }

  public String removeSpaces(String s) {
    StringTokenizer st = new StringTokenizer(s," ",false);
    String t="";
    while (st.hasMoreElements()) t += st.nextElement();
    return t;
  }
  
  public static JComponent getSystemInformationTab() {
      StringBuffer sysInfo = new StringBuffer();

      sysInfo.append("<html>");
      
      // operating system
      sysInfo.append("<p><b>Operating System:</b> ");
      sysInfo.append(System.getProperty("os.name") + " "
              + System.getProperty("os.version") + "</p>");
      
      // java version
      sysInfo.append("<p><b>Java VM Version:</b> ");
      sysInfo.append(System.getProperty("java.vm.version") + "</p>");
      
      // java version
      sysInfo.append("<p><b>Java VM Vendor:</b> ");
      sysInfo.append(System.getProperty("java.vm.vendor") + "</p>");
      
      // java version
      sysInfo.append("<p><b>Java VM Path:</b> ");
      sysInfo.append(System.getProperty("java.home") + "</p>");
      
      sysInfo.append("</html>");
      
      JEditorPane retval = new JEditorPane(TYPE, sysInfo.toString());
      retval.setEditable(false);
      
      JScrollPane tab = new JScrollPane(retval, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      return tab;
  }

  public static void main(String args[]) {
    JFrame that = new JFrame("About test");
    that.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    that.getContentPane().add(new AboutBox("FunctionAnalyzer"));
    that.pack();
    that.show();
  }
}

