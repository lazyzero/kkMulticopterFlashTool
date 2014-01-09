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

package org.concord.swing.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Plot a function of time.
 */
public class LineGraph
extends JComponent
implements ValueGraph
{
    final static double DEFAULT_ORIGIN = 0.0;
    final static double DEFAULT_MAXIMUM = 100.0;
    final static int DEFAULT_CELL_SIZE = 20;
    protected Font smallFont = new Font("Arial",Font.PLAIN,9);
    protected Image bi;
    protected Graphics big;
    protected int width = 0, height = 0;
    protected Vector functions = new Vector();
    protected Vector visibleFunctions = new Vector();
    protected Vector colorList = new Vector();
    protected double yOrigin = DEFAULT_ORIGIN, yMax= DEFAULT_MAXIMUM;
    protected boolean showingNegative = false;
    protected int cellSize = DEFAULT_CELL_SIZE;
    protected int cursorIndex = -1;
    protected Color cursorColor = Color.red;

    protected Color backColor, gridColor, textColor;

    protected boolean drawBoundsValues = false;

    protected DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance();
    protected ChangeListener sliderChanged = new ChangeListener()
    {
        public void stateChanged(ChangeEvent event)
        {
            if (slider instanceof JSlider)
                setScaleMax(slider.getMaximum() - slider.getValue());
            LineGraph.this.repaint();
        }
    };
    protected JSlider slider;
    
    protected ComponentAdapter sizeChanged = new ComponentAdapter()
    {
        public void componentResized(ComponentEvent event)
        {
            setLineGraphSize(LineGraph.this.getSize());
        }
    };

    public LineGraph()
    {
        setBackColor(Color.black);
        setGridColor(new Color(0,100,0));
        setTextColor(Color.green);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        DecimalFormatSymbols dfs=new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(dfs);
        addComponentListener(sizeChanged);
        drawBoundsValues = true;
    }
    
    public void setBackColor(Color color)
    {
        backColor = color;
    }
    
    public void setGridColor(Color color)
    {
        gridColor = color;
    }
    
    public void setTextColor(Color color)
    {
        textColor = color;
    }
    
    public boolean isShowingNegative()
    {
        return showingNegative;
    }
    
    public int getCursorIndex()
    {
    	return cursorIndex;
    }
    
    public Color getCursorColor()
    {
    	return cursorColor;
    }
    
    public void setCursorIndex(int value)
    {
    	cursorIndex = value;
    }
    
    public void setCursorColor(Color value)
    {
    	cursorColor = value;
    }
    
    public void setShowingNegative(boolean negative)
    {
        showingNegative = true;
    }
    
    public int getCellSize()
    {
        return cellSize;
    }
    
    public void setCellSize(int cell)
    {
        cellSize = cell;
    }

    /**
     * Sets the color associated with a particular index.
     * <p>
     * @param color - Color of graph element.
     * @param index - int index of graph element.
     */
    public void setColor(Color color, int index)
    {
        int n = colorList.size();
        if (index >= n)
        {
            colorList.setSize(index + 1);
        }
        colorList.setElementAt(color, index);
    }
    
    /**
     * Returns color associated with a particular index.
     * <p>
     * @param index - int index of graph element.
     * @return -  Color color of graph element.
     */
    public Color getColor(int index)
    {
        int n = colorList.size();
        Color color = colors[index % colors.length];
        if (index < n)
        {
            Object object = colorList.elementAt(index);
            if (object instanceof Color)
                return (Color) object;
        }
        return color;
    }
    
    public JSlider getSlider()
    {
        if (slider == null)
        {
            setSlider(new JSlider());
        }
        return slider;
    }
    
    public void setSlider(JSlider slider)
    {
        if (this.slider instanceof JSlider)
        {
            this.slider.removeChangeListener(sliderChanged);
        }
        this.slider = slider;
        if (this.slider instanceof JSlider)
        {
            this.slider.addChangeListener(sliderChanged);
        }
    }
    
    public void setYOrigin(double yOrigin)
    {
        this.yOrigin = yOrigin;
    }
    
    public void setYOrigin(float yOrigin)
    {
        setYOrigin((double) yOrigin);
    }
    
    public double getMax()
    {
        return yMax;
    }
    
    public void setMax(double yMax)
    {
        this.yMax = yMax;
    }
    
    public void setScaleMax(int scale)
    {
        double value = (((double) scale) - 50.0d) / 10.0d;
        yMax = Math.pow(10.0d, value);
    }
    
    protected double log10(double x)
    {
        return Math.log(x) / Math.log(10.0d);
    }
    
    public void setSliderValue()
    {
        int value = (int) (10.0d * log10(getMax()) + 50.0d);
        value = slider.getMaximum() - value;
        if (value < 0)
            value = 0;
        slider.setValue(value);
    }

//    public void setSliderValue()
//    {
//    	double targetMax = getMax();
//    	int value = (int) (10.0d * log10(getMax()) + 50.0d);
//    	value = slider.getMaximum() - value;
//    	if (value < 0)
//    		value = 0;
//    	slider.setValue(value);
//    	while (targetMax > getMax())
//    	{
//    		slider.setValue(slider.getValue()+1);
//    	}
//    }
    
    public void setMax(float yMax)
    {
        setMax((double) yMax);
    }

    public void setBoundsValuesVisible(boolean visible)
    {
        drawBoundsValues = visible;
    }
    
    public void reset()
    {
        functions.removeAllElements();
        colorList.removeAllElements();
    }
    
    protected void setLineGraphSize(Dimension size)
    {
        bi = createImage(size.width, size.height);
    }
	
	protected double [] getFunction(int index, double x)
	{
        int n = functions.size();
        if (index >= n)
        {
            functions.setSize(index + 1);
			visibleFunctions.setSize(index + 1);
        }
        double [] function = (double []) functions.elementAt(index);
        if (function == null)
        {
            function = new double[getSize().width];
            functions.setElementAt(function, index);
			visibleFunctions.setElementAt(function, index);
            for (int i = 0; i < function.length; i++)
            {
                function[i] = x;
            }
        }
		return function;
	}
	
	public void setFunctionVisible(boolean visible, int index)
	{
		double [] function = getFunction(index, 0.0);
		visibleFunctions.setElementAt(visible ? function : null, index);
	}
	
	public boolean isFunctionVisible(int index)
	{
		return (index < functions.size()) && (visibleFunctions.elementAt(index) != null);
	}
    
    /**
     * Updates graph element at index with new value.
     * Uses index mapping to determine the graph element.
     * @param x - double value to update.
     * @param index - int index of graph element.
     */
    public void updateValue(double x, int index)
    {
        double [] function = getFunction(index, x);
        for (int i = 1; i < function.length; i++)
        {
            function[i-1] = function[i];
        }
        function[function.length - 1] = x;
        repaint();
    }
    
    public void updateValue(double x)
    {
        updateValue(x, 0);
    }
    
    public void updateValue(float x, int index)
    {
        updateValue((double) x, index);
    }
    
    public void updateValue(float x)
    {
        updateValue((double) x);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        update(g);
    }

    public void update(Graphics g)
    {	
        Dimension dim = getSize();
        width = dim.width;
        height = dim.height;
    
        int nx = (width / cellSize) + 1;
        int ny = (height / cellSize) + 1;
        
        if (bi == null)
            bi = createImage(width, height);
        big = bi.getGraphics();
    
        big.setColor(backColor);
        big.fillRect(0, 0, width, height);
    
        big.setColor(gridColor);
        for(int i = 0; i < nx; i++)
        {
            big.drawLine(cellSize * i, 1, cellSize * i, height - 1);
        }
        for(int i = 0; i < ny; i++)
        {
            big.drawLine(1, cellSize * i, width - 1, cellSize * i);
        }
    
        double yUnit = showingNegative ? yOrigin + (double) (height / (2 * yMax)) : (double) (height / yMax);
        double yMin = showingNegative ? -yMax : yOrigin;
        for (int f = 0; f < functions.size(); f++)
        {
        	Color fColor = getColor(f);
            big.setColor(fColor);
            double [] function = (double []) visibleFunctions.elementAt(f);
            if (function != null)
            {
                int x1 = 0;
                int y1 = (int) ((yMax - function[0]) * yUnit);
                for (int i = 1; i < function.length; i++)
                {
                    int x2 = i;
                    int y2 = (int) ((yMax - function[i]) * yUnit);
                    big.drawLine(x1, y1, x2, y2);
                    if (i == cursorIndex)
                    {
                    	big.setColor(cursorColor);
                    	big.drawLine(x1, 0, x1, height);
                    	big.setColor(fColor);
                    }
                    x1 = x2;
                    y1 = y2;
                }
            }
        }
        
        if (drawBoundsValues)
        {
            big.setColor(textColor);
            if (showingNegative)
                big.drawString(format.format(yOrigin),  0, height / 2);
            big.drawString(format.format(yMax), 0, 15);
            big.drawString(format.format(yMin), 0, height);
        }
        g.drawImage(bi, 0, 0, null);
    }
    
    public void sleep(int time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (Exception e)
        {
        }
    }
    
    public static void main(String [] args)
    {
        LineGraph graph = new LineGraph();
        JFrame frame = new JFrame();
        frame.getContentPane().add(graph, "Center");
        frame.getContentPane().add(graph.getSlider(), "South");
        graph.setShowingNegative(true);
        graph.setMax(2.0);
        frame.setSize(400, 300);
        frame.setVisible(true);
        double unit = 3.14159d / 100;
        for (int i = 0; i < 400; i++)
        {
            double sine = Math.sin(unit * i);
            double cosine = Math.cos(unit * i);
            graph.setCursorIndex(600 - i);
            graph.updateValue(sine, 0);
            graph.updateValue(cosine, 1);
            graph.updateValue(sine + cosine, 2);
            graph.sleep(50);
        }
    }
}
