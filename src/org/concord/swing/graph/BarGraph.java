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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Bar graph component.
 * This component in its default form provides a simple bar graph. Setting a value
 * at a particular index will show a graphic bar at that height at that offset. This
 * component is built out of an array of gauges, each of which has it's own set of
 * properties.
 * <p>
 * In addition, there is a more complicated use of this component that involves remapping
 * the index values used in the updateValue, setColor and getColor methods. Since each gauge
 * also has indexed values and color, updating the values of the bar graph with a single
 * index requires some special setup. This setup is done with index mapping. Using
 * the addMapEntry method, the developer can specify that the graph index 'i' will
 * reference sub-bar 'k' in gauge 'j'. This would be done by calling 'addMapEntry(i, j, k)'.
 * In the absence of mapping for a paticular index, the gauge at that index will
 * be treated as a simple, single bar.
 */
public class BarGraph
extends JComponent
implements ValueGraph
{
    public static Vector oldGauges = new Vector();
    public static final int DEFAULT_GAUGE_WIDTH = 100;
    public static final int DEFAULT_NUMBER_OF_GAUGES = 5;
    protected int gaugeWidth = DEFAULT_GAUGE_WIDTH;
    protected int numberOfGauges = DEFAULT_NUMBER_OF_GAUGES;
    protected Color background;
    protected Gauge [] gauges;
    protected boolean editable = false;
    protected Vector indexMap = new Vector();
    protected ChangeListener sliderChanged = new ChangeListener()
    {
        public void stateChanged(ChangeEvent event)
        {
            if (slider instanceof JSlider)
                setScaleMax(slider.getMaximum() - slider.getValue());
            BarGraph.this.repaint();
        }
    };
    protected JSlider slider;
    
    protected ComponentAdapter sizeChanged = new ComponentAdapter()
    {
        public void componentResized(ComponentEvent event)
        {
            setBarGraphSize(BarGraph.this.getSize());
        }
    };
    

    /**
     * Bar graph constructor.
     * Sets the default number of gauges for this graph.
     */
    public BarGraph()
    {
        super();
        setLayout(null);
        addComponentListener(sizeChanged);
        setNumberOfGauges(DEFAULT_NUMBER_OF_GAUGES);
        background = getBackground();
    }
    

    /**
     * Add or replace an index map entry.
     * If the values provided to the graph are shown non-sequentially,
     * then index mapping must be done to assign the value to the correct
     * gauge and sub-bar.<p>
     * @param index - int index of the graph to be associated with a value.
     * @param i - index refers to the offset of a gauge within the graph.
     * @param j - index refers to the sub-bar within a particular gauge.
     */
    public void addMapEntry(int index, int i, int j)
    {
        if (index >= indexMap.size())
        {
            indexMap.setSize(index + 1);
        }
        Point point = (Point) indexMap.elementAt(index);
        if (point == null)
        {
            point = new Point(i, j);
            indexMap.setElementAt(point, index);
        }
        else
        {
            point.x = i;
            point.y = j;
        }
    }
    

    /**
     * Removes an index map for a particular index.
     * <p>
     * @param index - int index of the graph to be associated with a value.
     * @param i - index refers to the offset of a gauge within the graph.
     * @param j - index refers to the sub-bar within a particular gauge.
     */
    public void removeMapEntry(int index, int i, int j)
    {
        if (index < indexMap.size())
        {
            indexMap.setElementAt(null, index);
        }
    }
    

    /**
     * Resets the index mapping.
     */
    public void clearMap()
    {
        indexMap.removeAllElements();
    }
    
    public Color getBackground()
    {
        return background;
    }
    
    public void setBackground(Color color)
    {
        background = color;
    }

    /**
     * Sets the number of gauges in this graph. Recylcles gauges
     * from previous setting, creating new ones as necessary.
     * <p>
     * @param number - int number of gauges.
     */
    public void setNumberOfGauges(int number)
    {
        removeAll();
        if (gauges != null)
        {
            for (int i = 0; i < gauges.length; i++)
            {
                oldGauges.addElement(gauges[i]);
                gauges[i] = null;
            }
        }
        numberOfGauges = number;
        gauges = new Gauge[number];
        for (int i = 0; i < number; i++)
        {
            if (oldGauges.size() > 0)
            {
                gauges[i] = (Gauge) oldGauges.elementAt(0);
                oldGauges.removeElementAt(0);
            }
            else
                gauges[i] = new Gauge();
            add(gauges[i]);
        }
        setBarGraphSize(getSize());
    }

    public void setMax(double value)
    {
        for (int i = 0; i< gauges.length; i++)
        {
            if (gauges[i] instanceof Gauge)
                gauges[i].setMax(value);
        }
    }
    
    public void setScaleMax(int scale)
    {
        double value = (((double) scale) - 50.0d) / 10.0d;
        setMax(Math.pow(10.0d, value));
    }
    
    public double getMax()
    {
        return gauges[0].getMax();
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

    /**
     * Returns editable state.
     * Gauge values can be set with pointer device interaction.
     * This method returns whether or not that feature is enabled.<p>
     */
    public boolean isEditable()
    {
        return editable;
    }
    

    /**
     * Make gauges editable.
     * Gauge values can be set with pointer device interaction.
     * This method enables and disables that feature.<p>
     */
    public void setEditable(boolean value)
    {
        editable = value;
        for (int i = 0; i < numberOfGauges; i++)
            gauges[i].setEditable(value);
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
    

    /**
     * Returns color associated with a particular index.
     * <p>
     * @param index - int index of graph element.
     * @return -  Color color of graph element.
     */
    public Color getColor(int index)
    {
        if (index < indexMap.size())
        {
            Point point = (Point) indexMap.elementAt(index);
            if (point instanceof Point)
            {
                return gauges[point.x].getColor(point.y);
            }
        }
        return gauges[index].getColor();
    }
    

    /**
     * Sets the color associated with a particular index.
     * <p>
     * @param color - Color of graph element.
     * @param index - int index of graph element.
     */
    public void setColor(Color color, int index)
    {
        if (index < indexMap.size())
        {
            Point point = (Point) indexMap.elementAt(index);
            if (point instanceof Point)
            {
                gauges[point.x].setColor(color, point.y);
            }
        }
        else
        {
            gauges[index].setColor(color);
        }
        repaint();
    }
    

    /**
     * Sets value that determines pixel spaceing between gauges.
     * <p>
     * @param gap - int gap between gauges.
     */
    public void setGaugeWidth(int width)
    {
        gaugeWidth = width;
        setBarGraphSize(getSize());
    }
    

    /**
     * Gets value that determines pixel spaceing between gauges.
     * <p>
     * @return - int gap between gauges.
     */
    public int getGaugeWidth()
    {
        return gaugeWidth;
    }
    

    /**
     * Returns the gauge component at a particular index.
     * This method does not use index mapping.
     * <p>
     * @param index - int index of gauge.
     * @return - Gauge gauge at index.
     */
    public Gauge getGauge(int index)
    {
        return gauges[index];
    }

    protected void setBarGraphSize(Dimension size)
    {
        int gaugeGap = (size.width - numberOfGauges * gaugeWidth) / (numberOfGauges + 1);
        int width = gaugeWidth;
        if (gaugeGap <= 0)
        {
            width = (size.width / numberOfGauges) - 1;
            gaugeGap = 1;
        }
        for (int i = 0; i < numberOfGauges; i++)
        {
            gauges[i].setBounds((i + 1) * gaugeGap +  i * width, 0, width, size.height);
            gauges[i].setEditable(editable);
        }
    }
    

    /**
     * Get value of graph element at index.
     * Uses index mapping to determine the graph element.
     * @param index - int index of graph element.
     */
    public double getValue(int index)
    {
        if (index < indexMap.size())
        {
            Point point = (Point) indexMap.elementAt(index);
            if (point instanceof Point)
            {
                return gauges[point.x].getValue(point.y);
            }
        }
        return gauges[index].getValue();
    }
    
    public double getValue()
    {
        return getValue(0);
    }
    
    /**
     * Updates graph element at index with new value.
     * Uses index mapping to determine the graph element.
     * @param x - double value to update.
     * @param index - int index of graph element.
     */
    public void updateValue(double x, int index)
    {
        if (index < indexMap.size())
        {
            Point point = (Point) indexMap.elementAt(index);
            if (point instanceof Point)
            {
                gauges[point.x].updateValue(x, point.y);
                return;
            }
        }
        gauges[index].updateValue(x);
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
        Rectangle b = getBounds();
        g.setColor(background);
        g.fillRect(0, 0, b.width, b.height);
        super.paintComponent(g);
    }
    
    public static void main(String [] args)
    {
        JFrame frame = new JFrame("Test bar graph");
        BarGraph barGraph = new BarGraph();
        barGraph.setBackground(Color.black);
        barGraph.setEditable(true);
        frame.getContentPane().add(barGraph, "Center");
        frame.setSize(600, 500);
        frame.setVisible(true);
    }
}

