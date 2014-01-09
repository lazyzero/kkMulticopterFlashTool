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


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public class FormattedText extends JTextField
{
	
	public static final int ONLY_INTEGER = 0;
	public static final int ONLY_DOUBLE = 1;
	public static final int ONLY_STRING = 2;
	
	private int inputMode;
	
	//if only accept integer, 
	private int intMax= Integer.MAX_VALUE;
;
	private int intMin = Integer.MIN_VALUE;
	
	private int intValue = intMin;
	
	//if only accept double;
	private double dbMin= Double.NEGATIVE_INFINITY ;
	private double dbMax = Double.POSITIVE_INFINITY;
	private double dbValue = dbMin;
	
	private String strValue = "";
	
	private String currentText;
	public FormattedText(){
			addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent evt){
			}
	
			public void keyTyped(KeyEvent evt){
			}
	
			public void keyReleased(KeyEvent evt)
			{
				
				switch (inputMode)
				{
					case FormattedText.ONLY_INTEGER:
						String oldIntText = ""+intValue;
						try{
								if (getText().equals("-")) return;
								if (getText().equals("")) return;
								int value = Integer.valueOf(getText()).intValue();
						
								if (value>=intMin && value<=intMax)
								{
									int oldValue = intValue;
									intValue = value;
									dbValue = (double)intValue;
									setText(String.valueOf(value));
									//firePropertyChange("intValue",new Integer(oldValue), new Integer(intValue));
									fireActionPerformed();
                                }
								else
								{
/*
									JOptionPane op =new JOptionPane();
									op.setMessage("You cannot input a value less than "+intMin+" or greate than "+intMax+"!");
									JDialog messageBox = op.createDialog(getParent(), "Error!");
									messageBox.show();
*/
									Toolkit.getDefaultToolkit().beep();
									setText(oldIntText);
								}
							}catch (Exception e)
							{
/*
								JOptionPane op =new JOptionPane();
								op.setMessage("You can only input integer in this textField!");
								JDialog messageBox = op.createDialog(getParent(), "Error!");
								messageBox.show();
*/
								Toolkit.getDefaultToolkit().beep();
								setText(oldIntText);
								
							}
							break;
					case FormattedText.ONLY_DOUBLE:
						double oldDbValue = dbValue;
						String oldDbText = ""+dbValue;
						
						try{
							if (getText().equals("-")) return;
							if (getText().equals("")) return;
							double value = Double.valueOf(getText()).doubleValue();
							double oldValue = dbValue;
							dbValue = value;
							
							if (value<dbMin || value>dbMax)
							{
								
								intValue = (int)Math.round(dbValue);
//								JOptionPane op =new JOptionPane();
//								op.setMessage("You cannot input a value less than "+dbMin+" or greate than "+dbMax+"!");
//								JDialog messageBox = op.createDialog(getParent(), "Error!");
//								messageBox.show();
								Toolkit.getDefaultToolkit().beep();
								
								setText(oldDbText);
							}
							else if ((getText().toLowerCase()).endsWith("d") || (getText().toLowerCase()).endsWith("f"))
							{
								Toolkit.getDefaultToolkit().beep();
								setText(oldDbText);
							}
							else
							{
								fireActionPerformed();
								//firePropertyChange("dbValue",new Double(oldValue), new Double(dbValue));
							}
						}catch (Exception e)
						{
							/*JOptionPane op =new JOptionPane();
							op.setMessage("You can only input double in this textField!");
							JDialog messageBox = op.createDialog(getParent(), "Error!");
							setText(String.valueOf(dbMin));
							messageBox.show();*/
							Toolkit.getDefaultToolkit().beep();
						    if (oldDbValue == Double.NEGATIVE_INFINITY)
						   		setText(""+Integer.MIN_VALUE);
						    else if (oldDbValue ==Double.POSITIVE_INFINITY)
						       	setText(""+Integer.MAX_VALUE);
						    else
								setText(oldDbText);
						}
						break;
		
					case FormattedText.ONLY_STRING:
						String oldStrText = strValue;
						try{
							String value =getText();
							strValue=value;
						}catch (Exception e)
						{
						    /*JOptionPane op =new JOptionPane();
							op.setMessage("You can only input string in this textField!");
							JDialog messageBox = op.createDialog(getParent(), "Error!");
							messageBox.show();*/
							Toolkit.getDefaultToolkit().beep();
							setText(oldStrText);
						}
						break;
					default:
					  	break;
				}
			}
		});
		
	}
	protected void fireActionPerformed(){
		super.fireActionPerformed();
	}
	
	public void setIntValue(int value){
		if(value >= intMin && value <= intMax){
			intValue = value;
			dbValue = intValue;
		}

	}
	public void setDbValue(double value){
		if(value >= dbMin && value <= dbMax){
			dbValue = value;
			intValue = (int)Math.round(dbValue);
		}
	}
	
	public void setLimitation (int min,int max)
	{
		System.out.println("Integer");
		if (inputMode == FormattedText.ONLY_INTEGER)
		{
			intMin = min;
			intMax = max;
			if(intValue < intMin){
				setIntValue(intMin);
			}else if(intValue > intMax){
				setIntValue(intMax);
			}
		}
		else if (inputMode == FormattedText.ONLY_DOUBLE)
		{
			dbMin = (double)min;
			dbMax =(double) max;
			if(dbValue < dbMin){
				setDbValue(dbMin);
			}else if(dbValue > dbMax){
				setDbValue(dbMax);
			}
		}
		else
			return;
	}
	
	public void setLimitation (double min,double max)
	{
		if (inputMode == FormattedText.ONLY_INTEGER)
		{
			intMin =(int)Math.round(min);
			intMax = (int)Math.round(max);
			if(intValue < intMin){
				setIntValue(intMin);
			}else if(intValue > intMax){
				setIntValue(intMax);
			}
		}
		else if (inputMode == FormattedText.ONLY_DOUBLE)
		{
			dbMin = min;
			dbMax = max;
			if(dbValue < dbMin){
				setDbValue(dbMin);
			}else if(dbValue > dbMax){
				setDbValue(dbMax);
			}
		}
		else
			return;
	}
	
	public void setInputMode(int i)
	{
		if (i!=FormattedText.ONLY_DOUBLE && i!=FormattedText.ONLY_INTEGER && i!=FormattedText.ONLY_STRING) return;
 		
		this.inputMode = i;
		
	}
	public int getInputMode()
	{
		return inputMode;
	}
	
	
	/*public void paintComponent(Graphics g){
		if(g == null) return;
		Color oldColor = g.getColor();
		g.setColor(Color.blue);
		g.fillRect(0,0,getSize().width,getSize().height);
		g.setColor(oldColor);
	}*/
	
	public Dimension getPreferredSize(){
		return new Dimension(100,100);
		
	}
	
	
}

