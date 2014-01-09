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
 * <p>Title: JAnnotationImage</p>
 * @author Dmitry Markman, dima@concord.org
 * @version 1.0
 */
package org.concord.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.LinkedList;
import java.util.ListIterator;

import org.concord.swing.JAnnotationImageModel.AnnotationSpot;

/**
 * The class to hold and drawing annotation image data:
 * annotation spot areas, annotations
 * could be used with <code>JAnnotationImageContainer</code>  
 * @see JAnnotationImageContainer
 */

public class JAnnotationImage extends javax.swing.JPanel{
    static final String START_DRAG_PROPERTY = "startDragPoint";
    
    
	private int dragLimit = 20;
    
    Draggable   currentDraggable;
                    
    JAnnotationImageModel model;
        
    ParentComponentTransformer draggableComponentTransformer;
    ParentComponentTransformer draggableImageTransformer;
    
    AnnotatedImageMouseListener annotatedImageMouseListener = new AnnotatedImageMouseListener();
    AnnotatedImageMouseMotionListener annotatedImageMouseMotionListener = new AnnotatedImageMouseMotionListener();
	
	java.util.HashMap properties = new java.util.HashMap();


    boolean pointsModeStarted = false;
    java.util.Vector pointModeDots;
    java.awt.Point  prevPt = null;
    
    JAnnotationImageContainer owner;
    
    boolean htmlSupport = false;
    
/**
 * Default <code>JAnnotationImage</code> constructor
 * it set layout manager to <code>null</code>
 * and add appropriate mouse listeners
 */		
	public JAnnotationImage(JAnnotationImageContainer owner){
	    super();
        this.owner = owner;
		setLayout(null);
		setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		setToolTipText("");
		addMouseListener(annotatedImageMouseListener);
		addMouseMotionListener(annotatedImageMouseMotionListener);
	}
	
/**
 * <code>JAnnotationImage</code> constructor, it creates<br>
 * instance from <code>JAnnotationImageModel</code>
 * @param model  <code>JAnnotationImageModel</code>
 * @see JAnnotationImageModel
 */	
	public JAnnotationImage(JAnnotationImageContainer owner,JAnnotationImageModel model){
	    this(owner);
	    setModel(model);
	}
	
/**
 * <code>JAnnotationImage</code> constructor, it downloads<br>
 * image from <code>resString</code>
 * @param resString  <code>String</code>
 * defines url from where image could be loaded
 * it will try to load image from jar file first
 */	
	public JAnnotationImage(JAnnotationImageContainer owner,String resString){
	    this(owner);
		model = new JAnnotationImageModel();
	    setImageResourceString(resString);
	}
	
/**
 * <code>JAnnotationImage</code> constructor, it creates<br>
 * image from <code>bim</code>
 * @param bim  <code>java.awt.image.BufferedImage</code>
 * @see JAnnotationImageContainer#JAnnotationImageContainer(java.awt.image.BufferedImage,String)
 */	
    public JAnnotationImage(JAnnotationImageContainer owner,java.awt.image.BufferedImage bim){
	    this(owner);
		model = new JAnnotationImageModel(bim);
    }
	
/** 
 * Returns the preferred size of this container.  
 * @return    an instance of <code>Dimension</code> that represents 
 *                the preferred size of this container.<br>
 * if instance of the isn't <code>null</code> it equals to size of the image
 * @see       Component#getPreferredSize
 */
	public Dimension getPreferredSize(){
	    if(model != null){
    	    java.awt.image.BufferedImage bim = model.getMainImage();
    	    if(bim != null){
    	        return new Dimension(bim.getWidth(),bim.getHeight());
    	    }
        }
        return new Dimension(20,20);
	}

/**
 * Returns instance of the <code>JAnnotationImageModel</code>
 * @return an instance of the <code>JAnnotationImageModel</code>
 * <code>JAnnotationImageModel</code> is accessor to many <code>JAnnotationImage</code><br>
 * properties
 * @see JAnnotationImageModel
 * @see #setModel(JAnnotationImageModel)
 */	
	public JAnnotationImageModel getModel(){return model;}
	
/**
 * set <code>JAnnotationImageModel</code> for <code>JAnnotationImage</code>
 * @param model <code>JAnnotationImageModel</code>
 * @see JAnnotationImageModel
 * @see #getModel()
 */
 	public void setModel(JAnnotationImageModel model){
	    if(this.model == model) return;
	    if(this.model != null) this.model.clearAnnotationSpots();
	    this.model = model;
	}
			
/**
 * set editing mode for the <code>JAnnotationImage</code>
 * @param editMode <code>boolean</code>
 * @see #isEditMode()
 */
	public void setEditMode(boolean editMode){
	    if(model != null) model.setEditMode(editMode);
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                java.awt.Component c = javax.swing.SwingUtilities.getWindowAncestor(JAnnotationImage.this);
                if(c != null) c.setCursor(java.awt.Cursor.getDefaultCursor());
                defaultCursor = AnnotationSpot.SPOT_REGION_UNKNOWN;
            }
        });
	    repaint();
	}
	
/**
 * Returns current editing mode of the <code>JAnnotationImage</code>
 * @return current editing mode<br>
 * if editing mode is <code>false</code> editing isn't allowed
 * @see #setEditMode(boolean)
 */
	public boolean isEditMode(){return (model != null)?model.isEditMode():false;}
	
	public int getChoosingMode(){return (model != null)?model.getChoosingMode():JAnnotationImageModel.CHOOSING_MODE_RECTANGLE;}
	
	public synchronized void setChoosingMode(int choosingMode){
        if(model != null) model.setChoosingMode(choosingMode);
	}


    public boolean getHtmlSupport(){
        return htmlSupport;
    }

    public void setHtmlSupport(boolean htmlSupport){
        this.htmlSupport = htmlSupport;
    }
	
	public LinkedList getAnnotationSpots(){return (model != null)?model.getAnnotationSpots():null;}
	
	public boolean isAnnotationSpotPopupVisible(){
	    return (model != null)?model.isAnnotationSpotPopupVisible():false;
	}
			
    public javax.swing.JToolTip createToolTip(){
        return new JStyledToolTip();
    }
    
    public String getToolTipText(java.awt.event.MouseEvent event){
        return (model != null)?model.getToolTipText(event):null;
    }
    	
    public void deleteSelectedSpot(){
        if(model != null) model.deleteSelectedSpot();
        repaint();
    }

	public void clearAnnotationSpots(){
        if(model != null) model.clearAnnotationSpots();
        repaint();
	}
    	
    	
    public void setToolTipMode(boolean toolTipMode){
        if(model != null) model.setToolTipMode(toolTipMode);
    }
    
    public boolean isToolTipMode(){
        if(model != null) return model.isToolTipMode();
        return true;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        String eol = "\n";
	    sb.append("<class>"+getClass().getName()+"</class>"+eol);
	    //put here usual JComponent stuff
	    return sb.toString();
    }

    public AnnotationSpot getSelectedAnnotationSpot(){
        return (model != null)?model.getSelectedAnnotationSpot():null;
    }
    
    public void dispose(){
        removeMouseListener(annotatedImageMouseListener);
        removeMouseMotionListener(annotatedImageMouseMotionListener);
        
        if(model != null) model.dispose();
    }

//non public methods

    protected void setImageResourceString(String imageResourceString){
        if(model != null) model.setImageResourceString(imageResourceString);
        repaint();
    }
    
    protected String getImageResourceString(){return (model != null)?model.getImageResourceString():null;}

    ParentComponentTransformer getDraggableComponentTransformer(){return draggableComponentTransformer;}
    
    void setDraggableComponentTransformer(ParentComponentTransformer draggableComponentTransformer){
        this.draggableComponentTransformer = draggableComponentTransformer;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        internalPaintComponent(g);
    }

    protected void internalPaintComponent(Graphics g){
        if(model == null) return;
        java.awt.image.BufferedImage bim = (isEditMode())?model.getMainEditImage():model.getMainImage();
        java.awt.image.BufferedImage backgroundImage = model.getBackgroundImageImage();
        if(bim == null || backgroundImage == null) return;
        Graphics2D g2d = (Graphics2D)g;
        Graphics2D gi2d = backgroundImage.createGraphics();
            setImageClip(gi2d);
            gi2d.drawImage(bim,null,0,0);
            drawAnnotationSpots(gi2d);
        gi2d.dispose();
        g2d.drawImage(backgroundImage,null,0,0);
    }


   
    protected void setImageClip(Graphics g){
        g.setClip(getImageClip());
    }
   
    protected java.awt.Shape getImageClip(){
        java.awt.Insets insets = getInsets();
        if(insets == null) insets = new java.awt.Insets(0,0,0,0);
        java.awt.geom.Area clipTobe = new java.awt.geom.Area(new Rectangle(insets.left,insets.top,getSize().width - insets.left - insets.right,getSize().height - insets.top - insets.bottom));
        java.awt.geom.Area toolTipArea = (model != null)?model.getToolTipAreaForClip(this):null;
        if(toolTipArea != null) clipTobe.subtract(toolTipArea);
        return clipTobe;
    }

    protected void drawAnnotationSpots(Graphics2D g2d){
        if(model == null) return;
        LinkedList annotationSpots = model.getAnnotationSpots();
        if(annotationSpots != null){
            ListIterator it = annotationSpots.listIterator();
            while(it.hasNext()){
                AnnotationSpot as = (AnnotationSpot)it.next();
                drawAnnotationSpot(g2d,as);
            }
        }
        if(pointsModeStarted && pointModeDots != null && pointModeDots.size() > 0){
            Graphics2D gcopy = (Graphics2D)g2d.create();
            gcopy.setColor(Color.gray);
            gcopy.setStroke(bs);
            for(int i = 0; i < pointModeDots.size(); i++){
                java.awt.Point pt = (java.awt.Point)pointModeDots.elementAt(i);
                if(i > 0){
                    java.awt.Point ppt = (java.awt.Point)pointModeDots.elementAt(i-1);
                    gcopy.drawLine(ppt.x,ppt.y,pt.x,pt.y);
                }
                gcopy.fillRect(pt.x-2,pt.y-2,5,5);
            }
            gcopy.dispose();
        }
    }


    protected void drawAnnotationSpot(Graphics2D g2d,AnnotationSpot as){
        if(as == null || model == null || g2d == null) return;
        java.awt.image.BufferedImage bim = model.getMainImage();
        if(bim == null) return;
        Rectangle r = as.getBounds();
        int cropW = r.width;
        int cropH = r.height;
        Object oldHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        if(cropW > 0 && cropH > 0){
            java.awt.image.CropImageFilter cif = new java.awt.image.CropImageFilter(r.x,r.y,cropW,cropH);
            java.awt.image.ImageProducer producer = new java.awt.image.FilteredImageSource(bim.getSource(),cif);
            java.awt.Shape oldClip = g2d.getClip();
            if(as.getSpot() != null) g2d.setClip(as.getSpot());
            g2d.drawImage(createImage(producer),r.x,r.y,null);
            g2d.setClip(oldClip);
            java.awt.Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(bs);
            java.awt.Paint oldPaint = g2d.getPaint();
            if(as.getSpotColor() != null){
                g2d.setPaint(as.getSpotColor());
            }else{
                g2d.setPaint(Color.gray);
            }
            g2d.draw(as.getSpot());
            if(oldPaint != null) g2d.setPaint(oldPaint);
            g2d.setStroke(oldStroke);
            if(isEditMode() && getSelectedAnnotationSpot() == as){
                g2d.setColor(Color.red);
                g2d.drawRect(r.x,r.y,r.width,r.height);
            }
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,oldHint);
    }


    protected boolean handleDoubleClick(java.awt.event.MouseEvent e){
	    if(e.getClickCount() == 2){
	        if(!pointsModeStarted){
//	            setupPopupMenu();
                AnnotationSpot selectedSpot = getSelectedAnnotationSpot();
                if(selectedSpot == null || selectedSpot.isEmpty()) return false;
                selectedSpot.setupPopupMenu();
	            return true;
	        }else{
	            pointsModeStarted = false;
	            getNewAnnotationSpot(e.getPoint());
       		    if(owner != null) owner.repaint();
	            return true;
	        }
	        
	    }
	    return false;
    }
    
	DoubleClickThread doubleClickthread = null;
    class AnnotatedImageMouseListener extends java.awt.event.MouseAdapter{
    	public void mousePressed(java.awt.event.MouseEvent e)
    	{
    	    try{	
    	        owner.requestFocusInWindow();
    	    }catch(Throwable t){}
    	    if(model == null || !isEditMode() || isAnnotationSpotPopupVisible()) return;
    	    int modifiers = e.getModifiers();
            boolean wasShift = ((modifiers & java.awt.Event.SHIFT_MASK) != 0);
    	    AnnotationSpot currentAnnotationSpot = null;


    	    java.awt.Component source = e.getComponent();
            if(draggableComponentTransformer == null){
                draggableComponentTransformer = new ParentComponentTransformer();
                draggableComponentTransformer.setDestinationComponent(owner);
            }
            if(draggableImageTransformer == null){
                draggableImageTransformer = new ParentComponentTransformer(owner.layeredPane,JAnnotationImage.this);
            }
	        properties.put(START_DRAG_PROPERTY,e.getPoint());//dima from old code so creating without shift is allowed
	        //dima new code had  properties.put(START_DRAG_PROPERTY,e.getPoint()); in the startPressedTimer method

    	    if(!wasShift){
    	    
        	    if(getChoosingMode() == JAnnotationImageModel.CHOOSING_MODE_POINTS && (source instanceof JAnnotationImage)){
        	        if(!pointsModeStarted){
        	            if(pointModeDots == null)   pointModeDots = new java.util.Vector();
        	            else                        pointModeDots.removeAllElements();
        	            pointsModeStarted = true;
        	        }else{
        	            return;
        	        }
        	    }else{
        	        pointsModeStarted = false;
        	        if(pointModeDots != null) pointModeDots.removeAllElements();
        	        prevPt = null;
        	    }
        	    
        	    model.setSelectedAnnotationSpot(null);
                draggableComponentTransformer.setSourceComponent(source);
                if(source instanceof JAnnotationImage){
        	        currentDraggable = null;
                }else if(source instanceof StaticAnnotationToolTip){
                    AnnotationSpot spot = ((StaticAnnotationToolTip)source).getOwner();
                    if(spot == null) return;
                    currentDraggable = getStaticAnnotationSpotDraggable(spot);
                    if(currentDraggable == null) return;
                }else{
                    return;
                }
            }
            if(currentDraggable == null || wasShift){
                if(!wasShift){
                    currentAnnotationSpot = model.getAnnotationSpotForPoint(e.getPoint());
                    model.setSelectedAnnotationSpot(currentAnnotationSpot);
                    if(currentAnnotationSpot != null) currentDraggable = currentAnnotationSpot;
                }else{
    	            startPressedTimer(e.getPoint());
    	            return;
    	        }
    	    }
            if(currentDraggable != null){
    	        pointsModeStarted = false;
    	        if(pointModeDots != null) pointModeDots.removeAllElements();
                currentDraggable.startDrag(draggableComponentTransformer,e.getPoint());
                prevPt = null;
            }
    	}
    	
    	public void mouseReleased(java.awt.event.MouseEvent e)
    	{
    	    prevPt = null;
    	    properties.remove(START_DRAG_PROPERTY);
            disposePressedTimer();
    	    if(!isEditMode() || model == null) return;
    	    
    	    if(pointsModeStarted){
    	        pointModeDots.add(e.getPoint());
    	    }
    	    
    	    if(handleDoubleClick(e)) return;
    	    
    	    if(currentDraggable != null){
    	        currentDraggable.endDrag(e.getPoint());
                currentDraggable = null;
       		}
       		if(owner != null) owner.repaint();
    	}
	
    }
    
    class AnnotatedImageMouseMotionListener extends java.awt.event.MouseMotionAdapter{
        public void mouseDragged(java.awt.event.MouseEvent e){
            if(pointsModeStarted) return;
	        //if(currentDraggable == null && pressedTimer != null){dima new source code
	        //    disposePressedTimer();dima new source code
	        if(currentDraggable == null){//dima from old code
	            if(pressedTimer != null) disposePressedTimer();//dima from old code
	            java.awt.Point startDragPoint = (java.awt.Point)properties.get(START_DRAG_PROPERTY);
	            AnnotationSpot currentAnnotationSpot = getNewAnnotationSpot(startDragPoint);
	            currentDraggable = currentAnnotationSpot;
	            if(currentDraggable != null) currentDraggable.startDrag(null,createPointFromPoint2D(startDragPoint));
	        }
    		if(currentDraggable != null){
    		    currentDraggable.doDrag(e.getPoint());
                Graphics gc = JAnnotationImage.this.getGraphics();
                if(gc == null){
                    repaint();
                }else{
                    //setImageClip(gc); dima why I was need it ?????? it looks like it works much better without that
                    //internalPaintComponent(gc);
                    gc.dispose();
                }
                if(owner != null) owner.repaint();
    	    }
        }
        public void mouseMoved(java.awt.event.MouseEvent e){
            adjustCursor(e.getPoint());
    	    if(getChoosingMode() == JAnnotationImageModel.CHOOSING_MODE_POINTS && (e.getSource() instanceof JAnnotationImage) && 
    	       pointsModeStarted && pointModeDots != null && pointModeDots.size() > 0){
    	        Graphics2D gc = (Graphics2D)getGraphics();
                java.awt.Point dpt = (java.awt.Point)pointModeDots.elementAt(pointModeDots.size() - 1);
                gc.setXORMode(java.awt.Color.red);
                if(prevPt != null){
                    gc.drawLine(dpt.x,dpt.y,prevPt.x,prevPt.y);
                }
    	        prevPt = e.getPoint();
                gc.drawLine(dpt.x,dpt.y,prevPt.x,prevPt.y);
    	        gc.dispose();
    	    }
        }
    }


    public Draggable getStaticAnnotationSpotDraggable(AnnotationSpot as){
        if(as == null || as.isToolTipMode() || as.getAnnotationToolTip() == null) return null;
        Draggable draggable = new JLayeredPaneDraggable(as,as.getAnnotationToolTip());
        return draggable;
    }



    protected AnnotationSpot getNewAnnotationSpot(java.awt.Point pt){
        AnnotationSpot newAnnotationSpot = null;
        if(pt == null) return newAnnotationSpot;
        int px = (int)Math.round(pt.getX());	
        int py = (int)Math.round(pt.getY());	
        
        switch(model.getChoosingMode()){
            case JAnnotationImageModel.CHOOSING_MODE_POINTS:
                if(pointModeDots == null || pointModeDots.size() < 3) return null;
                int []xs = new int[pointModeDots.size()+1];
                int []ys = new int[pointModeDots.size()+1];
                for(int i = 0; i < pointModeDots.size(); i++){
                    java.awt.Point dpt = (java.awt.Point)pointModeDots.elementAt(i);
                    xs[i] = dpt.x;
                    ys[i] = dpt.y;
                }
                xs[xs.length - 1] = xs[0];
                ys[ys.length - 1] = ys[0];
	            newAnnotationSpot = new AnnotationSpot(new java.awt.Polygon(xs,ys,xs.length));//
                break;
            case JAnnotationImageModel.CHOOSING_MODE_POLYGON:
	            newAnnotationSpot = new AnnotationSpot(new java.awt.Polygon(new int[]{px},new int[]{py},1));//
                break;
            case JAnnotationImageModel.CHOOSING_MODE_ELLIPSE:
	            newAnnotationSpot = new AnnotationSpot(new java.awt.geom.Ellipse2D.Double(px,py,0,0));//
                break;
            default:
	            newAnnotationSpot = new AnnotationSpot(new Rectangle(px,py,0,0));
                break;
        }
        model.setSelectedAnnotationSpot(newAnnotationSpot);
        newAnnotationSpot.setChoosingMode(model.getChoosingMode());
        newAnnotationSpot.setEnclosedRectangle(new Rectangle(dragLimit,dragLimit,getSize().width - 2*dragLimit,getSize().height - 2*dragLimit));
        java.awt.geom.Point2D aToolLocation = (draggableComponentTransformer != null)?draggableComponentTransformer.transform(pt):pt;
        newAnnotationSpot.setAnnotationToolTipLocation(createPointFromPoint2D(aToolLocation));
        newAnnotationSpot.setAnnotation(TEST_TOOL_TIP);
        newAnnotationSpot.setAnnotationImage(this);
        model.addAnnotationSpot(newAnnotationSpot,true);
        java.awt.Container container = owner.layeredPane;
        if(container instanceof javax.swing.JLayeredPane){
            newAnnotationSpot.createAnnotationToolTip();
            int layerIndex = javax.swing.JLayeredPane.DRAG_LAYER.intValue();
            javax.swing.JComponent jc = newAnnotationSpot.getAnnotationToolTip();
            if(jc != null){
                ((javax.swing.JLayeredPane)container).add(jc,new Integer(layerIndex+newAnnotationSpot.getIndex()));
                jc.addMouseListener(annotatedImageMouseListener);
                jc.addMouseMotionListener(annotatedImageMouseMotionListener);
                newAnnotationSpot.checkAnnotationToolTipPosition();
            }
        }
        return newAnnotationSpot;
    }

    protected void checkAnnotationToolTips(){
        if(model == null) return;
        java.awt.Container container = getParent();
        if(container instanceof javax.swing.JLayeredPane){
            LinkedList annotationSpots = model.getAnnotationSpots();
            if(annotationSpots != null){
                int layerIndex = javax.swing.JLayeredPane.DRAG_LAYER.intValue();
                ListIterator it = annotationSpots.listIterator();
                while(it.hasNext()){
                    AnnotationSpot as = (AnnotationSpot)it.next();
                    javax.swing.JComponent jc = as.getAnnotationToolTip();
                    if(jc == null || container.isAncestorOf(jc)) continue;
                    as.checkAnnotationToolTip();
                    ((javax.swing.JLayeredPane)container).add(jc,new Integer(layerIndex+as.getIndex()));
                    jc.removeMouseListener(annotatedImageMouseListener);//just in case
                    jc.removeMouseMotionListener(annotatedImageMouseMotionListener);//just in case
                    jc.addMouseListener(annotatedImageMouseListener);
                    jc.addMouseMotionListener(annotatedImageMouseMotionListener);
                }
            }
        }
    }

    protected void startPressedTimer(java.awt.Point pt){
	    //properties.put(START_DRAG_PROPERTY,pt);dima new source code 
	    //old code diesn't have that line here so creating without shift is allowed
        disposePressedTimer();
        final java.awt.Point startPoint = createPointFromPoint2D(pt);
        pressedTimer = new javax.swing.Timer(1000,new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){
                disposePressedTimer();
                currentDraggable = new JLayeredPaneDraggable(JAnnotationImage.this);
                currentDraggable.startDrag(draggableImageTransformer,(java.awt.Point)properties.get(START_DRAG_PROPERTY));
            }
        });
        pressedTimer.start();
    }

    protected void disposePressedTimer(){
	    if(pressedTimer != null){
	        if(pressedTimer.isRunning()) pressedTimer.stop();
	        pressedTimer = null;
	    }
    }
    
    
    javax.swing.Timer pressedTimer;

    protected void adjustCursor(java.awt.Point pt){
        if(model == null) return;
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        if(!(w instanceof java.awt.Frame)) return;
        final java.awt.Frame frame = (java.awt.Frame)w;
        
        int cursorSpotRegion = model.getCursorSpotRegionForPoint(pt);

        final int runnableCursorSpotRegion = cursorSpotRegion;
        if((cursorSpotRegion == AnnotationSpot.SPOT_REGION_ALL) && (defaultCursor != cursorSpotRegion)){
            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    frame.setCursor(java.awt.Cursor.getPredefinedCursor(isEditMode()?java.awt.Cursor.HAND_CURSOR:java.awt.Cursor.CROSSHAIR_CURSOR));
                    defaultCursor = runnableCursorSpotRegion;
                }
            });
        }
        if((cursorSpotRegion == AnnotationSpot.SPOT_REGION_UNKNOWN || cursorSpotRegion == AnnotationSpot.SPOT_REGION_DEFINE) && (defaultCursor != cursorSpotRegion)){
            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    frame.setCursor(java.awt.Cursor.getDefaultCursor());
                    defaultCursor = runnableCursorSpotRegion;
                }
            });
        }
        if(cursorSpotRegion >=  AnnotationSpot.SPOT_REGION_TOP_LEFT && cursorSpotRegion <= AnnotationSpot.SPOT_REGION_BOTTOM && (defaultCursor != cursorSpotRegion)){
            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    int cursorType = java.awt.Cursor.HAND_CURSOR;
                    switch(runnableCursorSpotRegion){
                        case AnnotationSpot.SPOT_REGION_TOP_LEFT:      cursorType = java.awt.Cursor.NW_RESIZE_CURSOR; break;
                        case AnnotationSpot.SPOT_REGION_TOP_RIGHT:     cursorType = java.awt.Cursor.NE_RESIZE_CURSOR; break;
                        case AnnotationSpot.SPOT_REGION_BOTTOM_LEFT:   cursorType = java.awt.Cursor.SW_RESIZE_CURSOR; break;
                        case AnnotationSpot.SPOT_REGION_BOTTOM_RIGHT:  cursorType = java.awt.Cursor.SE_RESIZE_CURSOR; break;
                        case AnnotationSpot.SPOT_REGION_LEFT:          cursorType = java.awt.Cursor.W_RESIZE_CURSOR; break;
                        case AnnotationSpot.SPOT_REGION_RIGHT:         cursorType = java.awt.Cursor.E_RESIZE_CURSOR; break;
                        case AnnotationSpot.SPOT_REGION_TOP:           cursorType = java.awt.Cursor.N_RESIZE_CURSOR; break;
                        case AnnotationSpot.SPOT_REGION_BOTTOM:        cursorType = java.awt.Cursor.S_RESIZE_CURSOR; break;
                    }
                    frame.setCursor(java.awt.Cursor.getPredefinedCursor(cursorType));
                    defaultCursor = runnableCursorSpotRegion;
                }
            });
        }
    }

    protected int defaultCursor = 1;

    
    static java.awt.Point createPointFromPoint2D(java.awt.geom.Point2D p2d){
        if(p2d == null) return null;
        return new java.awt.Point((int)Math.round(p2d.getX()),(int)Math.round(p2d.getY()));
    }

    //final static String TEST_TOOL_TIP = "<font color=\"#FF0000\">xxxxxxxxx<font color=\"#00FF00\">zzzzz dsfsdfsd sdfsdfdsfsdf <br>sdfsdfsdfsd sdfsdfdsf sdfdsfdsf fsdfsd </font>yyyyy</font>";
    final static String TEST_TOOL_TIP = "Annotation";

    java.awt.BasicStroke bs = new java.awt.BasicStroke(1.25f);
    
    public static class JLayeredPaneDraggable implements Draggable{
        java.awt.geom.Point2D startDragPoint;
        java.awt.geom.Point2D startParentDragPoint;
        java.awt.geom.Point2D startComponentLocation;
        java.awt.Component owner;
        AnnotationSpot annotationSpot;
        CoordinateTransformer currentTransformer;
        
        JLayeredPaneDraggable(java.awt.Component owner){
            this(null,owner);
        }
        
        JLayeredPaneDraggable(AnnotationSpot annotationSpot,java.awt.Component owner){
            this.owner = owner;
            this.annotationSpot = annotationSpot;
        }
        
    	public int startDrag(CoordinateTransformer transformer,java.awt.Point pt){
    	    if(owner == null) return 0;
            startComponentLocation =owner.getLocation();
            startParentDragPoint = (transformer != null)?transformer.transform(pt):pt;
            startDragPoint = pt;
            currentTransformer = transformer;
    	    return 0;
    	}
    	
    	public void doDrag(java.awt.Point pt){
    	    if(owner == null) return;
            java.awt.geom.Point2D currPoint = (currentTransformer != null)?currentTransformer.transform(pt):pt;
            int newx = (int)Math.round(startComponentLocation.getX() + (currPoint.getX() - startParentDragPoint.getX()));
            int newy = (int)Math.round(startComponentLocation.getY() + (currPoint.getY() - startParentDragPoint.getY()));
            if(newx < 0) newx = 0;
            if(newy < 0) newy = 0;
            if(newx + owner.getSize().width > owner.getParent().getSize().width) newx = owner.getParent().getSize().width - owner.getSize().width;
            if(newy + owner.getSize().height > owner.getParent().getSize().height) newy = owner.getParent().getSize().height - owner.getSize().height;
            
            final int newix = newx;
            final int newiy = newy;
            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    owner.setLocation(newix,newiy);
                    if(owner.getParent() != null) owner.getParent().repaint();
                }
            });
    	}
    	
    	public void endDrag(java.awt.Point pt){
    	    if(annotationSpot == null || owner == null) return;
    	    annotationSpot.setAnnotationToolTipLocation(owner.getLocation());
    	}
    	
    	public void setDraggable(boolean draggable){}
    	public boolean isDraggable(){return true;}
    	
    }
}

class ParentComponentTransformer implements CoordinateTransformer{
java.awt.Component destinationComponent;
java.awt.Component sourceComponent;
    ParentComponentTransformer(){
    }
    
    ParentComponentTransformer(java.awt.Component destinationComponent,java.awt.Component sourceComponent){
        this.destinationComponent = destinationComponent;
        this.sourceComponent = sourceComponent;
    }
    
    public java.awt.geom.Point2D transform(java.awt.geom.Point2D p2d){
        if(destinationComponent == null || sourceComponent == null || p2d == null) return p2d;
        java.awt.Point pt = new java.awt.Point((int)Math.round(p2d.getX()),(int)Math.round(p2d.getY()));
        return javax.swing.SwingUtilities.convertPoint(sourceComponent,pt,destinationComponent);
    }
    
    public void setSourceComponent(java.awt.Component sourceComponent){
        this.sourceComponent = sourceComponent;
    }
    
    public void setDestinationComponent(java.awt.Component destinationComponent){
        this.destinationComponent = destinationComponent;
    }
    
}

class DoubleClickThread extends Thread{
JAnnotationImage owner;
int interval;
boolean  doubleClick = false;
    DoubleClickThread(JAnnotationImage owner,int interval){
        this.owner = owner;
        this.interval = interval;
        start();
    }
    public void run(){
        try{
            sleep(interval);
            if(doubleClick) System.out.println("WAS 2 click");
            else System.out.println("NO 2 click");
        }catch(Throwable t){}
    }
    public void wasDoubleClick(){
        doubleClick = true;
    }
}
