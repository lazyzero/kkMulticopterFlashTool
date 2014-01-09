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

package org.concord.swing.beans;

/**
 * @author dima
 *
 */

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.JScrollBar;

public class PropertyDialog  extends Dialog{
	PropertyInterimPanel pis;
//	JButton cancelButton;
//	JButton applyButton;
	Button cancelButton;
	Button applyButton;
	
	int maxSize = 300;

	public PropertyDialog(Object o,String panelName){
		this(null,o,panelName);
	}
	public PropertyDialog(java.awt.Frame frame,Object o,String panelName)
	{
		this(frame,o,panelName, null);
	}
	public PropertyDialog(java.awt.Frame frame,Object o,String panelName, Hashtable validProps)
	{
		super(frame,true);
		try{
			pis = new PropertyInterimPanel(o,panelName,validProps);
//d			getContentPane().setLayout(null);
//d			getContentPane().add(pis);
			setLayout(null);
			add(pis);
			pis.setLocation(0,0);
			Dimension d = pis.getPreferredSize();
			pis.setSize(d);
			setSize(d.width,d.height);
			setResizable(false);
			setTitle("Properties Dialog");
			//cancelButton = new JButton("Cancel");
			cancelButton = new Button("Cancel");
			//applyButton = new JButton("Apply");
			applyButton = new Button("Apply");
			
//d			getContentPane().add(cancelButton);
//d			getContentPane().add(applyButton);
			add(cancelButton);
			add(applyButton);
			ButtonListener buttonListener = new ButtonListener();
			cancelButton.addActionListener(buttonListener);
			applyButton.addActionListener(buttonListener);
			addWindowListener(new WindowAdapter(){
    			public void windowClosing(WindowEvent e) {
					cancelAll();
    			}
    			public void windowClosed(WindowEvent e) {
					cancelAll();
    			}
			});
			
		}catch(IllegalArgumentException e){
			java.awt.Toolkit.getDefaultToolkit().beep();
			throw e;
		}catch(Exception e){
			System.out.println("PropertyDialog Exception "+e);
			e.printStackTrace();
		}
	}
	
	protected void cancelAll(){
		if(pis != null){
    		pis.restoreOldValues();
    	}
    	setVisible(false);
	}

	public void initialize(Object o)
	{
		pis.initialize(o);
	}

	
	public void addNotify(){
		super.addNotify();
		int insetsH = getInsets().top + getInsets().bottom;
		int needSizeW = getSize().width;
		int needSizeH = getSize().height + insetsH;
		
		if(needSizeH > maxSize){
			needSizeH = maxSize;
		}
		int panelHeight = needSizeH - insetsH;
		
		int addHOffset = insetsH;
		
		pis.setLocation(0,addHOffset);
		pis.defineScrollBar(panelHeight);
		Dimension d = pis.getPreferredSize();
		d.height = panelHeight; 
		pis.setSize(d);
		needSizeW = d.width;
		Dimension dcb = cancelButton.getPreferredSize();
		Dimension dab = applyButton.getPreferredSize();
		setResizable(true);
		setSize(needSizeW,needSizeH + dcb.height + 10);
		setResizable(false);

		int buttonsWidth = dcb.width + dab.width + 10;
		int cancelX = needSizeW / 2 - buttonsWidth / 2;
		int applyX = cancelX + dcb.width + 10;
		applyButton.setLocation(applyX,addHOffset + panelHeight + 5);
		cancelButton.setLocation(cancelX,addHOffset + panelHeight + 5);

		cancelButton.setSize(dcb);
		applyButton.setSize(dab);
		doLayout();
		pis.repaint();
	}
	
	
	class ButtonListener implements ActionListener{
    	public void actionPerformed(ActionEvent e){
    		if(e.getActionCommand().equals("Cancel")){
    			PropertyDialog.this.cancelAll();
    		}else if(e.getActionCommand().equals("Apply")){
    			PropertyDialog.this.pis.writeNewValues();
    			PropertyDialog.this.setVisible(false);
    		}
    	}
	}
	
}

class PropertyInterimPanel extends java.awt.Panel{
	PropertySheet ps;
	JScrollBar vScrollBar;
	int	currScrollValue = 0;
	
	int	deltaScrollbar = 0;
	int maxSize = 300;


	public PropertyInterimPanel(Object o,String panelName){
		this(o,panelName,null);
	}
	public PropertyInterimPanel(Object o,String panelName, Hashtable validProps){
		super();
		try{
			ps = new PropertySheet(o,panelName,false,validProps);
			setLayout(null);
			add(ps,java.awt.BorderLayout.CENTER);
			ps.setLocation(0,0);
			Dimension d = ps.getPreferredSize();
			ps.setSize(d);
			setSize(d.width,d.height);
			
		}catch(IllegalArgumentException e){
			java.awt.Toolkit.getDefaultToolkit().beep();
			throw e;
		}catch(Exception e){
			System.out.println("PropertyDialog Exception "+e);
			e.printStackTrace();
		}
	}

	private boolean wasAddNotify = false;
	private int needSizeH;
	public void addNotify(){
		super.addNotify();
		wasAddNotify = true;
		ps.setLocation(0,0);
		Dimension d = ps.getPreferredSize();
		ps.setSize(d);
		setSize(d.width,d.height);
		synchronized(this){
			defineScrollBar(needSizeH);
		}
	}	

	public synchronized int defineScrollBar(int needSizeH){
		this.needSizeH = needSizeH;
/*
		if(!wasAddNotify){
			return 10;
		}
*/
		if(vScrollBar != null) remove(vScrollBar);
		int insetsH = getInsets().top + getInsets().bottom;
		int panelSize = ps.getSize().height;
		vScrollBar = new JScrollBar();
		int scrollW = vScrollBar.getPreferredSize().width;
		vScrollBar.setLocation(getSize().width + 1,0);
		vScrollBar.setSize(scrollW,needSizeH - insetsH);
		add(vScrollBar);
		
		int visisbleSize = needSizeH - insetsH;
		deltaScrollbar = visisbleSize - panelSize;
		vScrollBar.setMaximum(panelSize);			
		vScrollBar.setVisibleAmount(visisbleSize);
		vScrollBar.setUnitIncrement((int)Math.ceil((float)visisbleSize/20f));
		vScrollBar.setBlockIncrement(visisbleSize / 5);
		
		vScrollBar.getModel().addChangeListener(new javax.swing.event.ChangeListener(){
			public void stateChanged(javax.swing.event.ChangeEvent e){
				if(e.getSource() instanceof javax.swing.BoundedRangeModel){
					javax.swing.BoundedRangeModel model = (javax.swing.BoundedRangeModel)e.getSource();
					int newValue = model.getValue();
					if(newValue == currScrollValue) return;
					currScrollValue = newValue;
					float k = (float)currScrollValue/(float)(vScrollBar.getMaximum() - vScrollBar.getVisibleAmount())*(float)deltaScrollbar;
					ps.setLocation(ps.getLocation().x,Math.round(k));
					invalidate();
					repaint();						
				}
			}
		});
		return (scrollW + 3);
	}
	public void initialize(Object o)
	{
		ps.initialize(o);
	}
	public Dimension getPreferredSize(){
		if(ps == null) return super.getPreferredSize();
		Dimension d = ps.getPreferredSize();
		if(vScrollBar != null) d.width += vScrollBar.getSize().width;
		return d;
	}
	public void restoreOldValues(){
		if(ps != null) ps.restoreOldValues();
	}
	public void writeNewValues(){
		if(ps != null) ps.writeNewValues();
	}
	
}




