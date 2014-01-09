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
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;


/**
 * Value gauge.<p>
 * Graphic component that shows a simple scalar value as a vertical bar.<p>
 * This component can also represent multiple scalar values additively with
 * stacked vertical rectangles.
 */
public class Gauge
extends JComponent
implements ValueGraph
{	
    public final static double DEFAULT_MIN_VALUE = 0.0d;
    public final static double DEFAULT_MAX_VALUE = 100.0d;
    public final static double DEFAULT_VALUE = DEFAULT_MAX_VALUE / colors.length;
    protected double min = DEFAULT_MIN_VALUE;
    protected double max = DEFAULT_MAX_VALUE;
    protected double [] values = { DEFAULT_VALUE };
    protected double range = max - min;
    protected int gaugeWidth;
    protected int gaugeHeight;
    protected int majorTicks = 0;
    protected int minorTicks = 0;
    protected int tickSpace = 2;
    protected int majorTickLength = 10;
    protected int minorTickLength = 6;
    protected int majorTickValue;  // Spacing of major ticks
    protected int minorTickValue;  // Spacing of minor ticks
    protected String [] valueStrings = { "" + values[0] };
    protected String minString = "" + min;
    protected String maxString = "" + max;
    protected boolean editable = false;
    protected boolean drawBoundary = false;
    protected Vector oldBars = new Vector();
    protected Rectangle [] bars = { new Rectangle() };
    protected Color [] barColors = { Color.red };
    protected int numberOfBars = bars.length;
    protected int [] gaugeValues = new int[bars.length];
    
    protected ComponentAdapter sizeChanged = new ComponentAdapter()
    {
        public void componentResized(ComponentEvent event)
        {
            setGaugeSize(Gauge.this.getSize());
        }
    };
    
    protected MouseMotionAdapter dragGauge = new MouseMotionAdapter()
    {
        public void mouseDragged(MouseEvent event)
        {
            setValueFromMouseEvent(event);
        }
    };
    
    protected MouseAdapter setGauge = new MouseAdapter()
    {
        public void mousePressed(MouseEvent event)
        {
            setValueFromMouseEvent(event);
        }
    };
    

    /**
     * Gauge constructor.
     */
    public Gauge()
    {
        addComponentListener(sizeChanged);
        updateValue(values[0]);
        setEditable(editable);
    }
    
    protected int findBarIndex(int x, int y)
    {
        for (int i = 0; i < bars.length; i++)
        {
            if (bars[i].contains(x, y))
                return i;
        }
        return -1;
    }
    
    protected double getBarValue(int index, int x, int y)
    {
        Rectangle bar = bars[index];
        double barRange = (bar.height * range) / gaugeHeight;
        double barValue = barRange - ((y - bar.y) * barRange) / bar.height;
        return barValue;
    }
    
    protected void setValueFromMouseEvent(MouseEvent event)
    {
        int x = event.getX();
        int y = event.getY();
        int index = findBarIndex(x, y);
        if (index == -1)
            index = bars.length - 1;
        Rectangle bar = bars[index];
        if ((y - bar.y) > (bar.height / 2))
            index--;
        if (index < 0)
            index = 0;
        updateValue(getBarValue(index, x, y), index);
        repaint();
    }
    
    protected void setGaugeSize(Dimension size)
    {
        if ((majorTicks > 0) && (minorTicks > 0))
            gaugeWidth = (int) (0.35 * size.width);
        else
            gaugeWidth = size.width;
        gaugeHeight = size.height;
        for (int i = 0; i < gaugeValues.length; i++)
        {
            gaugeValues[i] = (int) ((values[i] * gaugeHeight) / range);
            updateValue(getValue(i), i);
        }
        setMajorTickMarks(majorTicks);
    }
    
    public void setNumberOfBars(int number)
    {
        if (bars != null)
        {
            for (int i = 0; i < bars.length; i++)
            {
                oldBars.addElement(bars[i]);
                bars[i] = null;
            }
        }
        numberOfBars = number;
        bars = new Rectangle[number];
        barColors = new Color[number];
        values = new double[number];
        gaugeValues = new int[number];
        for (int i = 0; i < number; i++)
        {
            if (oldBars.size() > 0)
            {
                bars[i] = (Rectangle) oldBars.elementAt(0);
                oldBars.removeElementAt(0);
            }
            else
                bars[i] = new Rectangle();
            barColors[i] = colors[i % colors.length];
            values[i] = DEFAULT_VALUE;
        }
        setGaugeSize(getSize());
    }
    

    /**
     * Returns editable state.
     * Gauge value can be set with pointer device interaction.
     * This method returns whether or not that feature is enabled.<p>
     */
    public boolean getEditable()
    {
        return editable;
    }
    

    /**
     * Make gauges editable.
     * Gauge value can be set with pointer device interaction.
     * This method enables and disables that feature.<p>
     */
    public void setEditable(boolean value)
    {
        editable = value;
        if (editable)
        {
            addMouseMotionListener(dragGauge);
            addMouseListener(setGauge);
        }
        else
        {
            removeMouseMotionListener(dragGauge);
            removeMouseListener(setGauge);
        }
    }
    

    /**
     * Returns the range of values shown by this gauge.<p>
     * @return - double range of values.
     */
    public double getRange()
    {
        return range;
    }


    /**
     * Set the minimum value of the gauge.<p>
     * @param value - double minumum value.
     */
    public void setMin(double value)
    {
        min = value;
        minString = " " + min;
        range = Math.abs(max - min);
    }
    

    /**
     * Get the minimum value of the gauge.<p>
     * @return - double minimum value.
     */
    public double getMin()
    {
        return min;
    }
    

    /**
     * Set the maximum value of the gauge.<p>
     * @param value - double maxumum value.
     */
    public void setMax(double value)
    {
        max = value;
        maxString = " " + max;
        range = Math.abs(max - min);
    }
    

    /**
     * Get the maximum value of the gauge.<p>
     * @return double maximum value.
     */
    public double getMax()
    {
        return max;
    }
    
    public void setScaleMax(int scale)
    {
        double value = (((double) scale) - 50.0d) / 10.0d;
        max = Math.pow(10.0d, value);
    }

    /**
     * Updates gauge sub-bar at index with new value.
     * <p>
     * @param x double value to update.
     * @param index int index of sub-bar.
     */
    public void updateValue(double value, int index)
    {
        values[index] = value;
        gaugeValues[index] = (int) ((values[index] * gaugeHeight) / range);
        for (int i = index; i < numberOfBars; i++)
        {
            Rectangle bar = bars[i];
            int gaugeValue = gaugeValues[i];
            bar.x = 0;
            bar.width = gaugeWidth;
            bar.height = gaugeValue;
            if (i == 0)
            {
                bar.y =  gaugeHeight - gaugeValue;
            }
            else
            {
                bar.y = bars[i - 1].y - bar.height;
            }
        }
        repaint();
    }

    public void updateValue(double value)
    {
        updateValue(value, 0);
    }
    
    public void updateValue(float value, int index)
    {
        updateValue((double) value, index);
    }
    
    public void updateValue(float value)
    {
        updateValue((double) value);
    }
    
    /**
     * Get value of gauge sub-bar at index.
     * <p>
     * @param index int index of sub-bar.
     * @return double sub-bar value.
     */
    public double getValue(int index)
    {
        return values[index];
    }
    
    /**
     * Get value of gauge sub-bar.
     * <p>
     * @return double gauge value.
     */
    public double getValue()
    {
        return values[0];
    }
    

    /**
     * Returns color associated with a particular index.
     * <p>
     * @param index int index of sub-bar.
     * @return  Color color of sub-bar.
     */
    public Color getColor(int index)
    {
        return barColors[index];
    }
    


    /**
     * Sets the color associated with a particular index.
     * <p>
     * @param color Color of sub-bar.
     * @param index int index of sub-bar.
     */
    public void setColor(Color color, int index)
    {
        barColors[index] = color;
    }

    /**
     * Returns color associated with a particular index.
     * <p>
     * @return  Color color of gauge.
     */
    public Color getColor()
    {
        return barColors[0];
    }
    
    /**
     * Sets the color.
     * <p>
     * @param color Color of gauge.
     */
    public void setColor(Color color)
    {
        barColors[0] = color;
    }

    public void setMajorTickMarks(int ticks)
    {
        majorTicks = ticks;
        if (majorTicks > 0)
            majorTickValue = gaugeHeight / majorTicks;  // Spacing of major ticks
        setMinorTickMarks(minorTicks);
    }
    
    public void setMinorTickMarks(int ticks)
    {
        minorTicks = ticks;
        if (minorTicks > 0)
            minorTickValue = majorTickValue / minorTicks;  // Spacing of minor ticks
    }
    
    public void paintComponent(Graphics g)
    {
        for(int i = 0; i < numberOfBars; i++)
        {
            g.setColor(barColors[i]);
            g.fillRect(bars[i].x, bars[i].y, bars[i].width, bars[i].height);
        }

        if (drawBoundary)
        {
            g.setColor(Color.black);
            g.drawRect(0, 0, gaugeWidth, gaugeHeight); // Draw the outline of the gauge
        }
        
        if ((majorTicks > 0) && (minorTicks > 0))
        {
            int x1 = gaugeWidth + tickSpace;		
            for (int y1 = gaugeHeight; y1 >= 0; y1 -= majorTickValue)
            {
                int x2 = x1 + majorTickLength;
                g.drawLine(x1, y1, x2, y1);
                x2 = x1 + minorTickLength;
                int min = (y1 - majorTickValue + minorTickValue);;
                for (int y2 = y1 - minorTickValue; y2 >= min; y2 -= minorTickValue)
                {
                    g.drawLine(x1, y2, x2, y2);
                }
            }
            int x = x1 + majorTickLength + tickSpace;
            for (int i = 0; i < gaugeValues.length; i++)
            {
                int y = (gaugeHeight - gaugeValues[i]) + 10;
                g.drawString(valueStrings[i], x, y);
                g.drawString(maxString, x, 10); 
                g.drawString(minString, x, gaugeHeight + 10);
            }
        }
    }

    public static void main(String [] args)
    {
        JFrame frame = new JFrame("Test gauge");
        Gauge gauge = new Gauge();
        gauge.setEditable(true);
        gauge.setNumberOfBars(6);
        frame.getContentPane().add(gauge, "Center");
        frame.setSize(150, 500);
        frame.setVisible(true);
    }
}

