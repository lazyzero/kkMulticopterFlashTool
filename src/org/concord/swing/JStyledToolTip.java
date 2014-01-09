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

/**
 * <p>Title: JStyledToolTip</p>
 * @author Dmitry Markman, dima@concord.org
 * @version 1.0
 */
package org.concord.swing;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * class that implements styled multiline swing tooltip
 * in order to use it class should have method 
 * <blockquote><pre>
 *  public javax.swing.JToolTip createToolTip(){
 *      return new JStyledToolTip();
 *  }
 * </pre></blockquote>
 * currently class supports text/html style only
 */

public class JStyledToolTip extends JToolTip{
    private static final String uiClassID = "ToolTipUI";
    
    String tipText;
    JComponent component;
    
    public JStyledToolTip() { updateUI();}
    
    public void updateUI() {
        setUI(StyledToolTipUI.createUI(this));
    }
}



class StyledToolTipUI extends BasicToolTipUI {
    static          StyledToolTipUI     sharedInstance = new StyledToolTipUI();
    static          JToolTip            tip;
    protected       CellRendererPane    rendererPane;
    
    private static  JTextPane           textPane;
    
    public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
        return sharedInstance;
    }
    
    public StyledToolTipUI() {
        super();
        textPane = new JTextPane();
        textPane.setContentType("text/html");
    }
        
    public void installUI(JComponent c) {
        super.installUI(c);
        tip = (JToolTip)c;
        rendererPane = new CellRendererPane();
        c.add(rendererPane);
    }
    
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        c.remove(rendererPane);
        rendererPane = null;
    }
        
    public void paint(Graphics g, JComponent c) {
        if(c == null || rendererPane == null) return;
        Dimension size = c.getSize();
        textPane.setBackground(c.getBackground());
        rendererPane.paintComponent(g,textPane,c,1,1,size.width - 1,size.height - 1,true);
    }
        
    public Dimension getPreferredSize(JComponent c) {
        String tipText = ((JToolTip)c).getTipText();
        if (tipText == null) return new Dimension(0,0);
        rendererPane.removeAll();
        
        textPane.setText(tipText);
        rendererPane.add(textPane );
        Dimension dim = textPane.getPreferredSize();
        if(dim == null) dim = new Dimension(100,100);
        dim.width += 5;
        dim.height += 5;
        if(dim.width < 20) dim.width = 20;
        if(dim.height < 20) dim.height = 20;
        return dim;
    }
    
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }
    
    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }
}
