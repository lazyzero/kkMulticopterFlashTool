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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.JTextPane;



public class JAnnotationImageModel implements StateOwner{
    
    public final static int CHOOSING_MODE_RECTANGLE = 0;
    public final static int CHOOSING_MODE_ELLIPSE   = 1;
    public final static int CHOOSING_MODE_POLYGON   = 2;
    public final static int CHOOSING_MODE_POINTS    = 3;
    public final static int CHOOSING_MODE_MIN       = CHOOSING_MODE_RECTANGLE;
    public final static int CHOOSING_MODE_MAX       = CHOOSING_MODE_POINTS;

	transient private java.awt.Image image;
	transient BufferedImage bim;
	transient BufferedImage bimW;
	transient BufferedImage bimF;
    transient String imageURLString;
    transient AnnotationSpot selectedAnnotationSpot;

    
    int choosingMode = CHOOSING_MODE_RECTANGLE;
    boolean editMode = false;
    String imageResourceString;
    LinkedList annotationSpots = null;
    boolean toolTipMode = false;
    		
    		
    private  ModelState state;		
    		
	public JAnnotationImageModel(){
	}
	
	public JAnnotationImageModel(Object state){
	    this();
	    setState(state);
	}
	
	public JAnnotationImageModel(String resString){
	    this();
	    setImageResourceString(resString);
	}
	
	public JAnnotationImageModel(BufferedImage bim){
	    this();
	    createImage(bim);
	}
	

    public void setImageResourceString(String imageResourceString){
	    this.imageResourceString = imageResourceString;
	    setImageURLString();
    }
    
    public void setImageResourceString1(String imageResourceString){
	    this.imageResourceString = imageResourceString;
    }
    
    public String getImageResourceString(){return imageResourceString;}

	public void setEditMode(boolean editMode){
	    this.editMode = editMode;
	}
	
	public boolean isEditMode(){return editMode;}
	
	public int getChoosingMode(){return choosingMode;}
	
	public synchronized void setChoosingMode(int choosingMode){
	    this.choosingMode = choosingMode;
	    checkChoosingMode();
	}
		
	public LinkedList getAnnotationSpots(){return annotationSpots;}
	
	public void setAnnotationSpots(LinkedList annotationSpots){
	    this.annotationSpots = annotationSpots;
	}
	
	public void clearAnnotationSpots(){
        if(annotationSpots != null){
            ListIterator it = annotationSpots.listIterator();
            while(it.hasNext()){
                AnnotationSpot as = (AnnotationSpot)it.next();
                as.disposeAnnotationToolTip();
            }
            annotationSpots.clear();
        }
        updateState();
	}

	public void deleteSelectedSpot(){
        if(annotationSpots == null || selectedAnnotationSpot == null) return;
        selectedAnnotationSpot.disposeAnnotationToolTip();
        annotationSpots.remove(selectedAnnotationSpot);
        updateState();
    }

    public String getToolTipText(java.awt.event.MouseEvent event){
        if(annotationSpots == null || !isToolTipMode()) return null;
        Point pt = event.getPoint();
        ListIterator it = annotationSpots.listIterator(annotationSpots.size());
        while(it.hasPrevious()){
            AnnotationSpot as = (AnnotationSpot)it.previous();
            if(as.contains(pt)) return as.getAnnotation();
        }
        return null;
    }
    
    public void setSelectedAnnotationSpot(AnnotationSpot as){
        selectedAnnotationSpot = as;
    }
    
    public AnnotationSpot getSelectedAnnotationSpot(){return selectedAnnotationSpot;}
    
    public AnnotationSpot getAnnotationSpotForPoint(Point pt){
        AnnotationSpot foundSpot = null;
	    if(annotationSpots == null) return foundSpot;
        ListIterator it = annotationSpots.listIterator(annotationSpots.size());
        while(it.hasPrevious()){
            AnnotationSpot as = (AnnotationSpot)it.previous();
            int asMode = as.startDrag(null,pt);
            if(asMode != AnnotationSpot.SPOT_REGION_UNKNOWN){
                foundSpot = as;
                break;
            }
        }
        return foundSpot;
    }


    public int getCursorSpotRegionForPoint(Point pt){
        int cursorSpotRegion = AnnotationSpot.SPOT_REGION_UNKNOWN;
        if(annotationSpots != null){
            ListIterator it = annotationSpots.listIterator(annotationSpots.size());
            while(it.hasPrevious()){
                AnnotationSpot as = (AnnotationSpot)it.previous();
                if(isEditMode() && (selectedAnnotationSpot == as)){
                    Rectangle r = as.getBoundsForAdjustCursor();
                    if(r.contains(pt)){
                        cursorSpotRegion = as.getSpotRegion(pt.x,pt.y);
                        break;
                    }
                }else if(!isEditMode()){
                    if(as.contains(pt)){
                        cursorSpotRegion = AnnotationSpot.SPOT_REGION_ALL;
                        break;
                    }
                }
            }
        }
        return  cursorSpotRegion;       
    }
    	  
    	  
    public synchronized void addAnnotationSpot(AnnotationSpot as){
        addAnnotationSpot(as,false);
    }
    
    public synchronized void addAnnotationSpot(AnnotationSpot as,boolean forceAdd){
        if(as == null || (as.isEmpty() && !forceAdd)) return;
        if(annotationSpots == null) annotationSpots = new LinkedList();
        annotationSpots.add(as);
        as.setIndex(annotationSpots.size() - 1);
        as.setToolTipMode(isToolTipMode());
    }
    
    public synchronized void removeAnnotationSpot(AnnotationSpot as){
        if(annotationSpots != null && as != null && annotationSpots.contains(as)){
            as.disposeAnnotationToolTip();
            annotationSpots.remove(as);
        }
    }

    public boolean isAnnotationSpotPopupVisible(){
        boolean retValue = false;
        if(annotationSpots == null) return retValue;
        ListIterator it = annotationSpots.listIterator();
        while(it.hasNext()){
            AnnotationSpot as = (AnnotationSpot)it.next();
            if(as.isPopupVisible()){
                retValue = true;
                break;
            }
        }
        return retValue;
    }

    public void setToolTipMode(boolean toolTipMode){
        this.toolTipMode = toolTipMode;
        if(annotationSpots != null){
            ListIterator it = annotationSpots.listIterator();
            while(it.hasNext()){
                AnnotationSpot as = (AnnotationSpot)it.next();
                as.setToolTipMode(isToolTipMode());
            }
        }
    }
    
    public java.awt.geom.Area getToolTipAreaForClip(java.awt.Component destination){
        if(isToolTipMode() || annotationSpots == null || annotationSpots.size() < 1) return null;
        java.awt.geom.Area area = new java.awt.geom.Area();
        ListIterator it = annotationSpots.listIterator();
        while(it.hasNext()){
            AnnotationSpot as = (AnnotationSpot)it.next();
            java.awt.Component c = as.getAnnotationToolTip();
            if(c == null) continue;
            Rectangle r = c.getBounds();
            java.awt.Container parent = c.getParent();
            if(parent == null) continue;
            area.add(new java.awt.geom.Area(javax.swing.SwingUtilities.convertRectangle(parent,r,destination)));
        }
        return area;
    }
    
    public boolean isToolTipMode(){
        return toolTipMode;
    }

    public void dispose(){
	    if(image != null) image.flush();
	    if(bim != null) bim.flush();
	    if(bimW != null) bimW.flush();
	    if(bimF != null) bimF.flush();
	    image = null;
	    bim = null;
	    bimW = null;
	    bimF = null;
	    clearAnnotationSpots();
    }

    public Object getState(){
        updateState();
        return state;
    }

    protected void updateState(){
        if(state == null) state = new ModelState();
        state.choosingMode          = choosingMode;
        state.editMode              = editMode;
        state.imageResourceString   = imageResourceString;
        state.image                 = bim;
        state.updateAnnotationSpots(annotationSpots);
    }

    public void setState(Object s){
        if(!(s instanceof ModelState)) return;
        state = (ModelState)s;
        recreateFromState();
    }
    
    protected void recreateFromState(){
        if(state == null) return;
        choosingMode            = state.getChoosingMode();
        editMode                = state.isEditMode();
        imageResourceString     = state.getImageResourceString();
	    setImageURLString();
        annotationSpots = null;
        Vector v = state.getAnnotationSpots();
        if(v == null || v.isEmpty()) return;
        if(annotationSpots == null) annotationSpots = new LinkedList();
        else                        annotationSpots.clear();
        for(int i = 0; i < v.size(); i++){
            annotationSpots.add(new AnnotationSpot(v.elementAt(i)));
        }
    }

	protected void setImageURLString(){
        imageURLString = null;
        if(imageResourceString == null) return;
        String jarString = createJARURLString(imageResourceString);
        if(checkForResource(jarString)){
            imageURLString = jarString;
        }else if(checkForResource(imageResourceString)){
            imageURLString = imageResourceString;
        }else{
            imageURLString = "file:"+imageResourceString;
        }
        createImage();
	}
	
	protected void checkChoosingMode(){
	    if(choosingMode >= CHOOSING_MODE_MIN && choosingMode <= CHOOSING_MODE_MAX) return;
	    choosingMode = CHOOSING_MODE_RECTANGLE;
	}
	
	protected boolean checkForResource(String str){
	    boolean retValue = false;
	    if(str == null) return retValue;
	    try{
	        java.net.URL url = new java.net.URL(str);
            java.io.InputStream is = url.openStream();
            is.close();
            retValue = true;
        }catch(Throwable t){
            retValue = false;
        }
        return retValue;
	}
	
	protected String createJARURLString(String str){
	    if(str == null) return str;
        StringBuffer sb = new StringBuffer();
        String classItemName = getClass().getName().replace('.','/');
        if(!classItemName.startsWith("/")) sb.append("/");
        sb.append(classItemName);
        sb.append(".class");
        java.net.URL urlClass = getClass().getResource(sb.toString());
        String externalForm = urlClass.toExternalForm();
        int index = externalForm.lastIndexOf('!');
        if(index < 0){
            return str;
        }else{
            String partialPath = externalForm.substring(0,index+1);
            try{
                sb.setLength(0);
                sb.append(partialPath);
                if(!str.startsWith("/")) sb.append("/");
                sb.append(str);
            }catch(Throwable t){
            }
        }
        return sb.toString();
	}
	
	protected void createImage(){
	    createImage(null);
	}
	
	protected void createImage(BufferedImage bufferedImage){
	    if(image != null) image.flush();
	    if(bim != null) bim.flush();
	    if(bimW != null) bimW.flush();
	    if(bimF != null) bimF.flush();
	    image = null;
	    bim = null;
	    bimW = null;
	    bimF = null;
	    if(bufferedImage == null && imageURLString == null) return;
		int width = 0;
		int height = 0;
	    if(bufferedImage != null){
	        //bim = bufferedImage;
	        image = bufferedImage;
	        width = bufferedImage.getWidth();
	        height = bufferedImage.getHeight();
	    }else{
    	    if(!checkForResource(imageURLString)){
    	        System.out.println("resource is unavailable");
    	        return;
    	    }
    	    try{
    	        java.net.URL imageURL = new java.net.URL(imageURLString);
    	        image = javax.imageio.ImageIO.read(imageURL);
    		    width = image.getWidth(null);
    		    height = image.getHeight(null);
    	    }catch(Throwable t){
                System.out.println("url throwable "+t);
            }
/*
    	    try{
    	        java.net.URL imageURL = new java.net.URL(imageURLString);
    	        image = java.awt.Toolkit.getDefaultToolkit().createImage(imageURL);
            }catch(Throwable t){
                System.out.println("url throwable "+t);
            }
    	    
    		java.awt.MediaTracker tracker = new java.awt.MediaTracker(new java.awt.Component(){});
    		tracker.addImage(image,0);
    		try{
    			tracker.waitForAll();
    		} catch (Exception e) {}
    		width = image.getWidth(null);
    		height = image.getHeight(null);
*/
        }
	    java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
	    java.awt.GraphicsDevice gd = ge.getDefaultScreenDevice();
	    java.awt.GraphicsConfiguration gc = gd.getDefaultConfiguration();
	    boolean hasAlpha = gc.getColorModel().hasAlpha();
	    if(hasAlpha){
	        if(image != null) bim = gc.createCompatibleImage(width,height);
            bimW = gc.createCompatibleImage(width,height);
            bimF = gc.createCompatibleImage(width,height);
//	        if(image != null) bim = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB_PRE);
//	        bimW = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB_PRE);
//	        bimF = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB_PRE);
//            System.out.println("type "+bim.getType());
	    }else{
	        if(image != null) bim = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	        bimW = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	        bimF = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	    }
        java.awt.Graphics2D g2d = null;
        if(image != null){
            g2d = bim.createGraphics();
                g2d.drawImage(image,0,0,null);
            g2d.dispose();
        }
        g2d = bimW.createGraphics();
            g2d.setColor(java.awt.Color.white);
            g2d.fillRect(0,0,width,height);
            g2d.drawImage(bim,new TranslucentGlassOp(0,0,-1,-1),0,0);
        g2d.dispose();
	}
	
    protected void removeTransientFields(){
        try{
            Class myClass = getClass();
            java.lang.reflect.Field []fields = myClass.getDeclaredFields();
            Vector transientFields = new Vector();
            for(int i = 0; i < fields.length; i++){
                java.lang.reflect.Field f = fields[i];
                String modifiers = java.lang.reflect.Modifier.toString(f.getModifiers());
                if(modifiers != null && modifiers.toLowerCase().indexOf("transient") >= 0){
                    transientFields.add(f.getName());
                }
            }
                        
            java.beans.BeanInfo info = java.beans.Introspector.getBeanInfo(getClass());
            java.beans.PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; ++i) {
                java.beans.PropertyDescriptor pd = propertyDescriptors[i];
                if(transientFields.contains(pd.getName())){
                    pd.setValue("transient", Boolean.TRUE);
                }
            }
        }catch(Throwable t){}
    }

    protected void prepareForSerialization(){
        removeTransientFields();
        if(annotationSpots != null){
            ListIterator it = annotationSpots.listIterator();
            while(it.hasNext()){
                AnnotationSpot as = (AnnotationSpot)it.next();
                as.prepareForSerialization();
            }
        }
    }
    	  
    protected BufferedImage getMainImage(){return bim;} 
    	    
    protected BufferedImage getMainEditImage(){return bimW;} 	    
    
    protected BufferedImage getBackgroundImageImage(){return bimF;} 	    

    
    protected void checkAnnotationToolTips(){
	    if(annotationSpots != null){
	        ListIterator it = annotationSpots.listIterator();
            while(it.hasNext()){
                AnnotationSpot as = (AnnotationSpot)it.next();
                as.checkAnnotationToolTip();
            }
        }
    }

    
     public static class ModelState implements java.io.Serializable{
        static final long serialVersionUID = -1118530011344190681L;

        int     choosingMode;
        boolean editMode;
        String  imageResourceString;
        Vector  annotationSpots;
        transient BufferedImage  image;
        
        protected void updateAnnotationSpots(LinkedList list){
            if(annotationSpots == null) annotationSpots = new Vector();
            annotationSpots.removeAllElements();
            if(list != null){
                ListIterator it = list.listIterator();
                while(it.hasNext()){
                    JAnnotationImageModel.AnnotationSpot as = (JAnnotationImageModel.AnnotationSpot)it.next();
                    annotationSpots.add(as.getState());
                }
            }
        }
        
        public int getChoosingMode(){return choosingMode;}
        public void setChoosingMode(int choosingMode){this.choosingMode = choosingMode;}
        
        public boolean isEditMode(){return editMode;}
        public void setEditMode(boolean editMode){this.editMode = editMode;}
        
        public String getImageResourceString(){return imageResourceString;}
        public void setImageResourceString(String imageResourceString){this.imageResourceString = imageResourceString;}
        
        public Vector getAnnotationSpots(){return annotationSpots;}
        public void setAnnotationSpots(Vector annotationSpots){this.annotationSpots = (Vector)annotationSpots.clone();}
                
        public BufferedImage getImage(){return image;}
        public void setImage(BufferedImage image){this.image = image;}
    }


 
    public static class AnnotationSpot implements Shape, Draggable, StateOwner{
        public final static int SPOT_REGION_UNKNOWN         = 0;
        public final static int SPOT_REGION_TOP_LEFT        = 1;
        public final static int SPOT_REGION_TOP_RIGHT       = 2;
        public final static int SPOT_REGION_BOTTOM_LEFT     = 3;
        public final static int SPOT_REGION_BOTTOM_RIGHT    = 4;
        public final static int SPOT_REGION_LEFT            = 5;
        public final static int SPOT_REGION_RIGHT           = 6;
        public final static int SPOT_REGION_TOP             = 7;
        public final static int SPOT_REGION_BOTTOM          = 8;
        public final static int SPOT_REGION_ALL             = 9;
        public final static int SPOT_REGION_DEFINE          = 10;
        public final static int SPOT_REGION_TOOL_TIP        = 11;
        
        static final int VICINITY_CONSTANT = 5;
        
        String          annotation;
        Point2D         []points;
        Rectangle       enclosedRectangle;
        boolean         toolTipMode = true;
        Point           annotationToolTipLocation;
        Rectangle       bounds;
        java.awt.Color  spotColor = java.awt.Color.blue;
        int choosingMode = JAnnotationImageModel.CHOOSING_MODE_RECTANGLE;

        transient double        startDragX;
        transient double        startDragY;
        transient GeneralPath   startDragArea;
        transient int           dragMode;
        transient GeneralPath   spot;
        transient StaticAnnotationToolTip       annotationToolTip;
        transient int index = 0;
        transient boolean draggable = true;
        transient Point   annotationTipConnectionPoint;
        transient private boolean choosingModeWasCalled = false;
        
        Vector pickedPoints = null;
        int pickedPointIndex = -1;

        AnnotationSpotState state;
        JPopupMenu textAreaMenu;
	    private javax.swing.JTextArea textArea = new javax.swing.JTextArea();
        JAnnotationImage annotationImage;
        CoordinateTransformer currentTransformer;
        
        transient private boolean popupMenuVisible = false;
        
        

        protected AnnotationSpot(Object state){
            this();
            setState(state);
        }
        
        public AnnotationSpot(){
        }
        
        public AnnotationSpot(Shape spot){
            this("",spot);
        }
        
        public AnnotationSpot(String annotation,Shape spot){
            this();
            this.annotation = annotation;
            this.spot = new GeneralPath(spot);
        }

        public JAnnotationImage getAnnotationImage(){return annotationImage;}
        public void setAnnotationImage(JAnnotationImage annotationImage){this.annotationImage = annotationImage;}

        public boolean getHtmlSupport(){
            return (annotationImage == null)?false:annotationImage.getHtmlSupport();
        }

        public synchronized boolean isPopupVisible(){
            return popupMenuVisible;
        }

        public synchronized void setPopupVisible(boolean val){
            if(val == popupMenuVisible) return;
            if(!popupMenuVisible){
                popupMenuVisible = val;
                return;
            };
            javax.swing.Timer swingTimer = new javax.swing.Timer(500,new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent evt){
                    synchronized(AnnotationSpot.this){
                        popupMenuVisible = false;
                    }
                }
            });
            swingTimer.setRepeats(false);
            swingTimer.start();
        }

        public Object getState(){
            updateState();
            return state;
        }

        protected void updateState(){
            prepareForSerialization();
            if(state == null) state = new AnnotationSpotState();
            state.index                         = index;
            state.choosingMode                  = choosingMode;
            state.annotation                    = annotation;
            state.annotationToolTipLocation     = annotationToolTipLocation;
            state.bounds                        = bounds;
            state.spotColor                     = spotColor;
            state.toolTipMode                   = toolTipMode;
            state.points                        = null;
            if(points != null){
                state.points = (Point2D[])points.clone();
            }
        }

        public void setState(Object s){
            if(!(s instanceof AnnotationSpotState)) return;
            state = (AnnotationSpotState)s;
            recreateFromState();
        }

        protected void recreateFromState(){
            if(state == null) return;
            index                       = state.getIndex();
            choosingMode                = state.getChoosingMode();
            annotation                  = state.getAnnotation();
            spotColor                   = state.getSpotColor();
            toolTipMode                 = state.isToolTipMode();
            setPoints((Point2D[])state.getPoints());
            setAnnotationToolTipLocation(state.getAnnotationToolTipLocation());
            checkAnnotationToolTip();
            setBounds(state.getBounds());
            calculateAnnotationTipConnectionPoint();
        }

      public String toString(){
            prepareForSerialization();
            StringBuffer sb = new StringBuffer();
    	    sb.append("<class>"+getClass().getName()+"</class>\n");
    	    sb.append("<annotation>");
    	    if(annotation != null) sb.append(annotation);
    	    sb.append("</annotation>/n");
    	    sb.append("<enclosedRectangle>");
    	    if(enclosedRectangle != null){
    	        sb.append("<x>"+enclosedRectangle.x+"/x\n");
    	        sb.append("<y>"+enclosedRectangle.x+"/y"+"\n");
    	        sb.append("<width>"+enclosedRectangle.x+"/width\n");
    	        sb.append("<height>"+enclosedRectangle.x+"/height\n");
            }
    	    sb.append("</enclosedRectangle>\n");
    	    sb.append("<points>");
            if(points == null){
    	        sb.append("<size>-1</size>\n");
            }else{
    	        sb.append("<size>"+points.length+"</size>\n");
                for(int i = 0; i < points.length; i++){
    	            sb.append("<point> x=\""+points[i].getX()+"\" y=\""+points[i].getY()+"\"</point>\n");
                }
            }
    	    sb.append("</points>\n");
    	    return sb.toString();
        }
    
        public int getIndex(){return index;}
        
        public void setIndex(int index){this.index = index;}
    
        public void setEnclosedRectangle(Rectangle r){
            enclosedRectangle = r;
        }
        
        public Rectangle getEnclosedRectangle(){return enclosedRectangle;}
        
        public void setAnnotationToolTip(StaticAnnotationToolTip annotationToolTip){
            this.annotationToolTip = annotationToolTip;
        }
        
        public javax.swing.JComponent getAnnotationToolTip(){
            if(isToolTipMode()) return null;
            return annotationToolTip;
        }

        public void checkAnnotationToolTipPosition(){
            if(annotationToolTip == null) createAnnotationToolTip();
            if(annotationToolTip == null) return;
            Rectangle rb = getBounds();
            Rectangle r = annotationToolTip.getBounds();
            Point pta = annotationToolTip.getLocation();
            annotationToolTip.setLocation(rb.x + rb.width  / 2 - r.width  / 2,
                                          rb.y + rb.height / 2 - r.height / 2);
            
        }

        public void checkAnnotationToolTip(){
            if(annotationToolTip == null) createAnnotationToolTip();
        }


        protected void calculateAnnotationTipConnectionPoint(){
            if(isEmpty()){
                annotationTipConnectionPoint = new Point(Integer.MIN_VALUE,Integer.MIN_VALUE);
                return;
            }
            Rectangle rb = getBounds();
            int xc = rb.x + rb.width / 2;
            int yc = rb.y + rb.height / 2;
            annotationTipConnectionPoint = new Point(xc,yc);
            if(contains(annotationTipConnectionPoint)) return;
            java.awt.geom.PathIterator pit = spot.getPathIterator(null);
            double []tempArray = new double[6];
            double d2 = java.lang.Double.MAX_VALUE;
            while(!pit.isDone()){
                int type = pit.currentSegment(tempArray);
                if(type == java.awt.geom.PathIterator.SEG_MOVETO || type == java.awt.geom.PathIterator.SEG_LINETO){
                    double temp = Point2D.distanceSq(xc,yc,tempArray[0],tempArray[1]);
                    if(temp < d2){
                        d2 = temp;
                        annotationTipConnectionPoint = new Point((int)Math.round(tempArray[0]),(int)Math.round(tempArray[1]));
                    }
                 }
                pit.next();
            }
        }

        public Point getAnnotationTipConnectionPoint(){
            if(annotationTipConnectionPoint == null) calculateAnnotationTipConnectionPoint();
            if(!isEmpty() && annotationTipConnectionPoint != null){
                if(annotationTipConnectionPoint.x == Integer.MIN_VALUE ||
                   annotationTipConnectionPoint.y == Integer.MIN_VALUE) calculateAnnotationTipConnectionPoint();
            }
            return annotationTipConnectionPoint;
        }

        public void createAnnotationToolTip(){
            if(annotationToolTip == null){
                annotationToolTip = new StaticAnnotationToolTip(getHtmlSupport());
            }
            annotationToolTip.setText(annotation);
            annotationToolTip.setOwner(this);
            if(annotationToolTipLocation == null) annotationToolTipLocation = new java.awt.Point();
            setAnnotationToolTipLocation(annotationToolTipLocation);
        }                
        
        public void disposeAnnotationToolTip(){
            if(annotationToolTip == null) return;
            java.awt.Container parent = annotationToolTip.getParent();
            if(parent != null){
                parent.remove(annotationToolTip);
            }
            annotationToolTip = null;
        }
        
        public Point getAnnotationToolTipLocation(){return annotationToolTipLocation;}
        
        public void setAnnotationToolTipLocation(java.awt.Point annotationToolTipLocation){
            this.annotationToolTipLocation = annotationToolTipLocation;
            if(annotationToolTip != null) annotationToolTip.setLocation(annotationToolTipLocation);
        }
                
        public GeneralPath getSpot(){return spot;}

    	public int getChoosingMode(){return choosingMode;}
    	
    	public synchronized void setChoosingMode(int choosingMode){
    	    this.choosingMode = choosingMode;
    	    if(!choosingModeWasCalled){
    	        choosingModeWasCalled = true;
    	        setBounds(bounds);
    	    }
    	}
    	
        public Rectangle getBoundsForAdjustCursor(){
            Rectangle r = getBounds();
            if(r == null) return r;
            r.x -= VICINITY_CONSTANT;
            r.y -= VICINITY_CONSTANT;
            r.width += 2*VICINITY_CONSTANT;
            r.height += 2*VICINITY_CONSTANT;
            return r;
        }

        public java.awt.Color getSpotColor(){return spotColor;}
        public void getSpotColor(java.awt.Color spotColor){this.spotColor = spotColor;}


        public String getAnnotation(){return annotation;}
        
        public void setAnnotation(String annotation){
            this.annotation = annotation;
            if(this.annotation != null && annotationToolTip != null){
                annotationToolTip.setText(annotation);
             }
        }
        
        public int getSpotRegion(Point pt){
            return getSpotRegion(pt.getX(),pt.getY());
        }

        public int getSpotRegion(double x,double y){
            Rectangle r = getBounds();
            if(r == null) return SPOT_REGION_UNKNOWN;
            if(isEmpty()) return SPOT_REGION_DEFINE;

            if(annotationToolTip != null && !isToolTipMode()){
                Rectangle annotationToolTipBounds = annotationToolTip.getBounds();
                if(annotationToolTipBounds.contains(x,y)) return SPOT_REGION_TOOL_TIP;
            }

            if(r.width >= 0 && r.height >= 1){
                int rightX = r.x + r.width;
                int bottomY = r.y + r.height;
                if(r.x > x && r.x - x < VICINITY_CONSTANT && r.y > y && r.y - y < VICINITY_CONSTANT)                return SPOT_REGION_TOP_LEFT;
                if(x > rightX && x - rightX < VICINITY_CONSTANT && r.y > y && r.y - y < VICINITY_CONSTANT)          return SPOT_REGION_TOP_RIGHT;
                if(r.x > x && r.x - x < VICINITY_CONSTANT && y > bottomY && y - bottomY < VICINITY_CONSTANT)        return SPOT_REGION_BOTTOM_LEFT;
                if(x > rightX && x - rightX < VICINITY_CONSTANT && y > bottomY && y - bottomY < VICINITY_CONSTANT)  return SPOT_REGION_BOTTOM_RIGHT;
                if(r.x > x && r.x - x < VICINITY_CONSTANT && y > r.y && y < bottomY)                                return SPOT_REGION_LEFT;
                if(x > rightX && x - rightX < VICINITY_CONSTANT && y > r.y && y < bottomY)                          return SPOT_REGION_RIGHT;
                if(r.y > y && r.y - y < VICINITY_CONSTANT && x > r.x && x < rightX)                                 return SPOT_REGION_TOP;
                if(y > bottomY && y - bottomY < VICINITY_CONSTANT && x > r.x && x < rightX)                         return SPOT_REGION_BOTTOM;

/*
                if(Math.abs(x-r.x) < VICINITY_CONSTANT && Math.abs(y-r.y) < VICINITY_CONSTANT)            return SPOT_REGION_TOP_LEFT;
                if(Math.abs(x-rightX) < VICINITY_CONSTANT && Math.abs(y-r.y) < VICINITY_CONSTANT)         return SPOT_REGION_TOP_RIGHT;
                if(Math.abs(x-r.x) < VICINITY_CONSTANT && Math.abs(y-bottomY) < VICINITY_CONSTANT)        return SPOT_REGION_BOTTOM_LEFT;
                if(Math.abs(x-rightX) < VICINITY_CONSTANT && Math.abs(y-bottomY) < VICINITY_CONSTANT)     return SPOT_REGION_BOTTOM_RIGHT;
                if(Math.abs(x-r.x) < VICINITY_CONSTANT && y > r.y && y < bottomY)                         return SPOT_REGION_LEFT;
                if(Math.abs(x-rightX) < VICINITY_CONSTANT && y > r.y && y < bottomY)                      return SPOT_REGION_RIGHT;
                if(Math.abs(y-r.y) < VICINITY_CONSTANT && x > r.x && x < rightX)                          return SPOT_REGION_TOP;
                if(Math.abs(y-bottomY) < VICINITY_CONSTANT && x > r.x && x < rightX)                      return SPOT_REGION_BOTTOM;
*/

                if(spot.contains(x,y))                                                                    return SPOT_REGION_ALL;
            }       
            return SPOT_REGION_UNKNOWN;
        }

        public int startDrag(CoordinateTransformer transformer,Point pt){
            if(pickedPoints == null) pickedPoints = new Vector();
            else                    pickedPoints.removeAllElements();
            pickedPointIndex = -1;
            startDragX = pt.getX();
            startDragY = pt.getY();
            dragMode = getSpotRegion(pt);
            startDragArea = (spot != null)?(GeneralPath)spot.clone():null;
            currentTransformer = transformer;
            
            if(dragMode < SPOT_REGION_TOP_LEFT || dragMode > SPOT_REGION_BOTTOM){
                pickedPointIndex = checkClickOnLine(pt,pickedPoints);
                recreateSpotWithPickedLine(pt);
                if(pickedPoints.size() > 0 &&  pickedPointIndex >= 0 && dragMode == SPOT_REGION_UNKNOWN) dragMode = SPOT_REGION_ALL;
            }
            return dragMode;
        }

        public boolean contains(Point pt){
            if(pt == null || spot == null) return false;
            return spot.contains(pt);
        }

        public boolean isEmpty(){
            if(spot == null) return true;
            return spot.getBounds().isEmpty();
        }

        public void doDrag(Point pt){
            if(dragMode == SPOT_REGION_UNKNOWN) return;
            double newX = pt.getX();
            double newY = pt.getY();
            if(enclosedRectangle != null){
                if(newX < enclosedRectangle.x) newX = enclosedRectangle.x;
                if(newX > enclosedRectangle.x + enclosedRectangle.width) newX = enclosedRectangle.x + enclosedRectangle.width;
                if(newY < enclosedRectangle.y) newY = enclosedRectangle.y;
                if(newY > enclosedRectangle.y + enclosedRectangle.height) newY = enclosedRectangle.y + enclosedRectangle.height;
            }
            double dx = newX - startDragX;
            double dy = newY - startDragY;
           if(dragMode == SPOT_REGION_DEFINE && spot != null){
                double x0 = Math.min(newX,startDragX);
                double y0 = Math.min(newY,startDragY);
                double w0 = Math.abs(dx);
                double h0 = Math.abs(dy);
                switch(choosingMode){
                    case JAnnotationImageModel.CHOOSING_MODE_POINTS:
                        break;
                    case JAnnotationImageModel.CHOOSING_MODE_POLYGON:
                        spot.lineTo((float)newX,(float)newY);
                        break;
                    case JAnnotationImageModel.CHOOSING_MODE_ELLIPSE:
                        spot.reset();
                        spot.append(new GeneralPath(new java.awt.geom.Ellipse2D.Double(x0,y0,w0,h0)),true);
                        break;
                    default:
                        spot.reset();
                        spot.append(new GeneralPath(new java.awt.geom.Rectangle2D.Double(x0,y0,w0,h0)),true);
                        break;
                }
                
            }else if(dragMode == SPOT_REGION_ALL){
                if(pickedPointIndex >= 0 && pickedPoints != null && pickedPoints.size() > 1){
                    recreateSpotWithPickedLine(new Point((int)Math.round(newX),(int)Math.round(newY)));
                    return;
                }else{
                    GeneralPath newSpot = (GeneralPath)startDragArea.clone();
                    newSpot.transform(java.awt.geom.AffineTransform.getTranslateInstance(dx,dy));
                    if(enclosedRectangle == null){
                        spot = newSpot;
                        return;
                    }else if(newSpot.intersects(enclosedRectangle)){
                        spot = newSpot;
                    }
                }
                return;
            }
            
            Rectangle startRect = startDragArea.getBounds();
            double kx = 0;
            double ky = 0;
            double  w0 = startRect.width;
            double  h0 = startRect.height;
            double  w = w0;
            double  h = h0;
            
            boolean doTransform = (spot != null);
                    
            if(doTransform){
                switch(dragMode){
                    case SPOT_REGION_TOP_LEFT:
                        kx = 1;
                        ky = 1;
                        w = startRect.x + w0 - newX;
                        h = startRect.y + h0 - newY;
                        break;
                    case SPOT_REGION_TOP_RIGHT:
                        ky = 1;
                        w = newX - startRect.x;
                        h = startRect.y + h0 - newY;
                        break;
                    case SPOT_REGION_BOTTOM_LEFT:
                        kx = 1;
                        w = startRect.x + w0 - newX;
                        h = newY - startRect.y;
                        break;
                    case SPOT_REGION_BOTTOM_RIGHT:
                        w = newX - startRect.x;
                        h = newY - startRect.y;
                        break;
                    case SPOT_REGION_LEFT:
                        kx = 1;
                        w = startRect.x + w0 - newX;
                        break;
                    case SPOT_REGION_RIGHT:
                        w = newX - startRect.x;
                        break;
                    case SPOT_REGION_TOP:
                        ky = 1;
                        h = startRect.y + h0 - newY;
                        break;
                    case SPOT_REGION_BOTTOM:
                        h = newY - startRect.y;
                        break;
                    default:
                        doTransform = false;
                        break;
                }
            }
            if(doTransform){
                if(w < 5) w = 5;
                if(h < 5) h = 5;
                double m00 = w/w0;
                double m01 = 0;
                double m02 = (w0 - w)*(kx + startRect.x/w0);
                double m10 = 0;
                double m11 = h/h0;
                double m12 = (h0 - h)*(ky + startRect.y/h0);
                spot = (GeneralPath)startDragArea.clone();
                spot.transform(new java.awt.geom.AffineTransform(m00,m10,m01,m11,m02,m12));
            } 
        }

        public void endDrag(Point pt){
            startDragArea = null;
            calculateAnnotationTipConnectionPoint();
            if(choosingMode == JAnnotationImageModel.CHOOSING_MODE_POINTS) return;
            if(choosingMode == JAnnotationImageModel.CHOOSING_MODE_POLYGON || (pickedPointIndex >= 0)) normalize();
        }

        public Point2D []getPoints(){return points;}
        
        public void setPoints(Point2D []points){
            this.points = points;
            if(this.points == null) return;
            if(spot != null) spot.reset();
            else             spot = new GeneralPath();
            for(int i = 0; i < points.length; i++){
                if(i == 0){
                    spot.moveTo((float)points[i].getX(),(float)points[i].getY());
                }else{
                    spot.lineTo((float)points[i].getX(),(float)points[i].getY());
                }
            }
            spot.closePath();
        }
        
        public void setToolTipMode(boolean toolTipMode){
            this.toolTipMode = toolTipMode;
            if(annotationToolTip != null){
                annotationToolTip.setVisible(!toolTipMode);
            }
        }
        
        public boolean isToolTipMode(){
            return toolTipMode;
        }

//Draggable methods
                
	    public void setDraggable(boolean draggable){
	        this.draggable = draggable;
	    }
	    
	    public boolean isDraggable(){
	        return (dragMode != SPOT_REGION_UNKNOWN) && draggable;
	    }
	    
	    
//Shape methods

        public boolean contains(double x, double y){
            if(spot == null) return false;
            return spot.contains(x,y);
        }
        
        public boolean contains(double x, double y,double w,double h){
            if(spot == null) return false;
            return spot.contains(x,y,w,h);
        }
        
        public boolean contains(Point2D p2d){
            if(p2d == null || spot == null) return false;
            return spot.contains(p2d);
        }

        public boolean contains(Rectangle2D r2d){
            if(r2d == null || spot == null) return false;
            return spot.contains(r2d);
        }

        public Rectangle getBounds(){
            bounds = (spot == null)?null:spot.getBounds();
            return bounds;
        }

        public void setBounds(Rectangle r){
            if(r == null) return;
            if(spot == null) spot = new GeneralPath();
            switch(choosingMode){
                case JAnnotationImageModel.CHOOSING_MODE_RECTANGLE:
                    spot.reset();
                    spot.append(new GeneralPath(new java.awt.geom.Rectangle2D.Double(r.x,r.y,r.width,r.height)),true);
                    break;
                case JAnnotationImageModel.CHOOSING_MODE_ELLIPSE:
                    spot.reset();
                    spot.append(new GeneralPath(new java.awt.geom.Ellipse2D.Double(r.x,r.y,r.width,r.height)),true);
                    break;
                case JAnnotationImageModel.CHOOSING_MODE_POLYGON:
                case JAnnotationImageModel.CHOOSING_MODE_POINTS:
                    if(isEmpty()) break;
                    Rectangle rb = getBounds();
                    double sx = (double)r.width/(double)rb.width;
                    double sy = (double)r.height/(double)rb.height;
                    if(Math.abs(sx - 1) < 1e-5 && Math.abs(sy - 1) < 1e-5) break;
                    GeneralPath newSpot = (GeneralPath)spot.clone();
                    newSpot.transform(java.awt.geom.AffineTransform.getScaleInstance(sx,sy));
                    spot = newSpot;
                    break;
            }
            bounds = getBounds();
        }

        public Rectangle2D getBounds2D(){
            if(spot != null) return spot.getBounds2D();
            return null;
        }

        public java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform at){
            if(spot != null) return spot.getPathIterator(at);
            return null;
        }

        public java.awt.geom.PathIterator getPathIterator(java.awt.geom.AffineTransform at,double flatness){
            if(spot != null) return spot.getPathIterator(at,flatness);
            return null;
        }
        public boolean intersects(Rectangle2D r2d){
            if(r2d == null || spot == null) return false;
            return spot.intersects(r2d);
        }
       
        public boolean intersects(double x, double y,double w,double h){
            if(spot == null) return false;
            return spot.intersects(x,y,w,h);
        }
        
       
        protected void normalize(){
            if(spot == null) return;
            java.awt.geom.PathIterator pit = spot.getPathIterator(null);
            double []tempArray = new double[6];
            Vector ptsv = new Vector();
            while(!pit.isDone()){
                int type = pit.currentSegment(tempArray);
                if(type == java.awt.geom.PathIterator.SEG_MOVETO || type == java.awt.geom.PathIterator.SEG_LINETO){
                    ptsv.add(new Point((int)Math.round(tempArray[0]),(int)Math.round(tempArray[1])));
                }
                pit.next();
            }
            Point []pts = new Point[ptsv.size()];
            for(int i = 0; i < pts.length; i++){
                pts[i] = (Point)ptsv.elementAt(i);
            }
            QuickHull qh = new QuickHull(pts);
            java.awt.Polygon pol = new java.awt.Polygon();
            for(int i = 0; i < qh.hullPoints.size(); i++){
                Point pt = (Point)qh.hullPoints.elementAt(i);
                pol.addPoint(pt.x,pt.y);
            }
            spot.reset();
            spot.append(new GeneralPath(pol),true);
        }

        void recreateSpotWithPickedLine(Point pt){
            if(pickedPoints.size() < 1 ||  pickedPointIndex < 0) return;
            spot.reset();
            pickedPoints.remove(pickedPointIndex);
            pickedPoints.insertElementAt(pt,pickedPointIndex);
            for(int i = 1; i < pickedPoints.size(); i++){
                java.awt.geom.Line2D.Double line = new java.awt.geom.Line2D.Double((Point)pickedPoints.elementAt(i-1),(Point)pickedPoints.elementAt(i));
                spot.append(line,true);
            }
        }

        protected int checkClickOnLine(Point pt,Vector ptsv){
            int pointIndex = -1;
            if(pt == null || isEmpty() || ptsv == null ) return pointIndex;
            ptsv.removeAllElements();
            java.awt.geom.PathIterator pit = spot.getPathIterator(null);
            double []tempArray = new double[6];
            while(!pit.isDone()){
                int type = pit.currentSegment(tempArray);
                if(type == java.awt.geom.PathIterator.SEG_MOVETO || type == java.awt.geom.PathIterator.SEG_LINETO){
                    ptsv.add(new Point((int)Math.round(tempArray[0]),(int)Math.round(tempArray[1])));
                }
                pit.next();
            }
            int nPoints = ptsv.size();
            Point2D.Double p2d = new Point2D.Double(pt.x,pt.y);
            if(nPoints >= 2){
                Point ptS = (Point)ptsv.elementAt(0);
                Point ptE = (Point)ptsv.elementAt(nPoints - 1);
                if(!ptS.equals(ptE)){
                    ptsv.add(ptS);
                    nPoints++;
                }
                for(int i = 1; i < nPoints; i++){
                    java.awt.geom.Line2D.Double line = new java.awt.geom.Line2D.Double((Point)ptsv.elementAt(i-1),(Point)ptsv.elementAt(i));
                     if(p2d.distance(line.getP1()) < 3){
                        pointIndex = i - 1;
                    }else if(p2d.distance(line.getP2()) < 3){
                        pointIndex = i;
                    }else if(line.ptSegDist(p2d) < 3){
                        ptsv.insertElementAt(pt,i); 
                        pointIndex = i;
                    }
                    if(pointIndex >= 0) break;
                }
            }   
            return pointIndex;
        }

        protected void prepareForSerialization(){
            if(spot == null) return;
            java.awt.geom.PathIterator pit = spot.getPathIterator(null);
            double []tempArray = new double[6];
            Vector ptsv = new Vector();
            while(!pit.isDone()){
                int type = pit.currentSegment(tempArray);
                if(type == java.awt.geom.PathIterator.SEG_MOVETO || type == java.awt.geom.PathIterator.SEG_LINETO){
                    ptsv.add(new Point2D.Double(tempArray[0],tempArray[1]));
                }
                pit.next();
            }
            points = new Point2D.Double[ptsv.size()];
            for(int i = 0; i < points.length; i++){
                points[i] = (Point2D)ptsv.elementAt(i);
            }

        }        
    
        public static class AnnotationSpotState implements java.io.Externalizable{
            static final long serialVersionUID = -3924729488905394223L;
            
            int             index;
            int             choosingMode;
            String          annotation;
            Point           annotationToolTipLocation;
            Rectangle       bounds;
            java.awt.Color  spotColor;
            boolean         toolTipMode;
            Point2D         []points;
            
            public int getIndex(){return index;}
            public void setIndex(int index){this.index = index;}
            
            public int getChoosingMode(){return choosingMode;}
            public void setChoosingMode(int choosingMode){this.choosingMode = choosingMode;}
            
            public String getAnnotation(){return annotation;}
            public void setAnnotation(String annotation){this.annotation = annotation;}
            
            public Point getAnnotationToolTipLocation(){return annotationToolTipLocation;}
            public void setAnnotationToolTipLocation(Point annotationToolTipLocation){this.annotationToolTipLocation = annotationToolTipLocation;}
            
            public Rectangle getBounds(){return bounds;}
            public void setBounds(Rectangle bounds){this.bounds = bounds;}

            public java.awt.Color getSpotColor(){return spotColor;}
            public void setSpotColor(java.awt.Color spotColor){this.spotColor = spotColor;}

            public boolean isToolTipMode(){return toolTipMode;}
            public void setToolTipMode(boolean toolTipMode){this.toolTipMode = toolTipMode;}

            public Point2D []getPoints(){return points;}
            public void setPoints(Point2D []points){this.points = (Point2D [])points.clone();}

            public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException{
                out.writeInt(index);
                out.writeInt(choosingMode);
                out.writeUTF(annotation);
                out.writeObject(annotationToolTipLocation);
                out.writeObject(bounds);
                out.writeObject(spotColor);
                out.writeBoolean(toolTipMode);
                if(points == null){
                    out.writeInt(-1);
                }else{
                    out.writeInt(points.length);
                    for(int i = 0; i < points.length; i++){
                        Point2D pt = points[i];
                        out.writeDouble(pt.getX());
                        out.writeDouble(pt.getY());
                    }
                }
            }

            public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException{
                index                       = in.readInt();
                choosingMode                = in.readInt();
                annotation                  = in.readUTF();
                annotationToolTipLocation   = (Point)in.readObject();
                bounds                      = (Rectangle)in.readObject();
                spotColor                   = (java.awt.Color)in.readObject();
                toolTipMode                 = in.readBoolean();
                int nPoints = in.readInt();
                points = null;
                if(nPoints >= 0){
                    points = new Point2D[nPoints];
                    for(int i = 0; i < nPoints; i++){
                        double x = in.readDouble();
                        double y = in.readDouble();
                        points[i] = new Point2D.Double(x,y);
                    }
                }
            }


        }
        
        protected void setAnnotationFromPopUp(JPopupMenu pm){
            if(pm == null) return;
            java.awt.Component jc = pm.getComponent(0);
            if(!(jc instanceof javax.swing.JScrollPane)) return;
            javax.swing.JScrollPane sp = (javax.swing.JScrollPane)jc;
            java.awt.Component view = sp.getViewport().getView();
            if(!(view instanceof javax.swing.JTextArea)) return;
            setAnnotation(((javax.swing.JTextArea)view).getText());
            textAreaMenu = null;
        }

        protected void setupPopupMenu(){
            if(isEmpty() || annotationImage == null || !annotationImage.isEditMode()) return;
    		textAreaMenu = new JPopupMenu();			
    		textAreaMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener(){
                public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e){
                    setPopupVisible(true);
                }
                
                public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e){
                }
                
                public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e){
                    setPopupVisible(false);
                    final Object obj = e.getSource();
                    if(!(obj instanceof JPopupMenu)) return;
                    JPopupMenu pm = (JPopupMenu)obj;
                    setAnnotationFromPopUp((JPopupMenu)obj);
                }
    		});
        	textAreaMenu.setBackground(new java.awt.Color(200, 200, 200));
            textAreaMenu.setOpaque(true);

            textArea.setText(getAnnotation());
            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
    	    scrollPane.setPreferredSize(new java.awt.Dimension(200,200));
    	    textAreaMenu.add(scrollPane);
    	    textAreaMenu.pack();
    	    Point ptMenu = annotationToolTipLocation;
    	    if(ptMenu == null){
    	        ptMenu = new Point(getBounds().x, getBounds().y);
    	    }else{
    	        ptMenu = javax.swing.SwingUtilities.convertPoint(annotationImage.getParent(),ptMenu,annotationImage);
    	    }
            textAreaMenu.show(annotationImage, ptMenu.x, ptMenu.y);
            textArea.requestFocus();
        }


    }

    class TranslucentGlassOp implements java.awt.image.BufferedImageOp{


    int x;
    int y;
    int w=-1;
    int h=-1;

        TranslucentGlassOp(int x, int y, int w, int h){
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

        }
        public synchronized BufferedImage filter(BufferedImage src, BufferedImage dest){
            if(dest == null) dest = createCompatibleDestImage(src,null);
       
            int iw = src.getWidth();
            int ih = src.getHeight();



            for(int ix = 0; ix < iw; ix++){
                for(int iy = 0; iy < ih; iy++){
                    int px = src.getRGB(ix,iy);
                    if(ix < x || ix >= x+w || iy < y || iy >= y+h){
                        px = 0x44000000 | (px & 0xFFFFFF);
                    }
                    dest.setRGB(ix,iy,px);
                 }
            }
            return dest;
        }
        public java.awt.geom.Rectangle2D getBounds2D (BufferedImage src){
            return src.getRaster().getBounds();
        }
        public BufferedImage createCompatibleDestImage (BufferedImage src,java.awt.image.ColorModel destCM){
            if(destCM == null){
                destCM = src.getColorModel();
                if (destCM instanceof java.awt.image.IndexColorModel) {
                    destCM = java.awt.image.ColorModel.getRGBdefault();
                }
            }
            int w = src.getWidth();
            int h = src.getHeight();
            return new BufferedImage(destCM,destCM.createCompatibleWritableRaster(w,h),destCM.isAlphaPremultiplied(),null);            
        }

        public Point2D getPoint2D (Point2D srcPt, Point2D dstPt){
            if(dstPt == null) dstPt = new Point2D.Float();
            dstPt.setLocation(srcPt);
            return dstPt;
        }

        public java.awt.RenderingHints getRenderingHints(){return null;}

        public void setX(int x){this.x = x;}
        public void setY(int y){this.y = y;}
        public void setW(int w){this.w = w;}
        public void setH(int h){this.h = h;}
        
        public int getX(){return x;}
        public int getY(){return y;}
        public int getW(){return w;}
        public int getH(){return h;}
    }
}

class StaticAnnotationToolTip extends JTextPane{
JAnnotationImageModel.AnnotationSpot owner;
boolean needResize = false;
    StaticAnnotationToolTip(boolean htmlSupport){
        super();
        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        setBackground(java.awt.Color.yellow);
        if(htmlSupport){
            setContentType("text/html");
        }else{
            setContentType("text/plain");
        }
        setEditable(false);
        setEnabled(false);
        addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseReleased(java.awt.event.MouseEvent evt){
	            if(evt.getClickCount() == 2){
	                if(owner != null) owner.setupPopupMenu();
	            }
            }
        });
    }
    
    public void setText(String text){
        super.setText(text);
        setSize(getPreferredSize());
        repaint();
        needResize = true;
    }
    
    public void paint(java.awt.Graphics g){
        if(needResize){
            needResize = false;
            setSize(getPreferredSize());
        }
        super.paint(g);
    }
    
    public void setOwner(JAnnotationImageModel.AnnotationSpot owner){
        this.owner = owner;
    }
    public JAnnotationImageModel.AnnotationSpot getOwner(){return owner;}

}


