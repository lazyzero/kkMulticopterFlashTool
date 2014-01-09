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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.concord.swing.JAnnotationImageModel.AnnotationSpot;

public class JAnnotationImageContainer extends javax.swing.JPanel implements StateOwner{
    private static final Border SELECTED_BORDER=BorderFactory.createLoweredBevelBorder();
    private static final Border EMPTY_BORDER=BorderFactory.createEmptyBorder();
    private static final Border UNSELECTED_BORDER=BorderFactory.createRaisedBevelBorder();

    public static final int NONE_TOOLBAR_MASK           = 0;
    public static final int RECTANGLE_TOOLBAR_MASK      = 1;
    public static final int ELLIPSE_TOOLBAR_MASK        = 2;
    public static final int POLYGON_TOOLBAR_MASK        = 4;
    public static final int DOTS_TOOLBAR_MASK           = 8;
    public static final int ALL_TOOLBAR_MASK            = RECTANGLE_TOOLBAR_MASK |
                                                          ELLIPSE_TOOLBAR_MASK   |
                                                          POLYGON_TOOLBAR_MASK   |
                                                          DOTS_TOOLBAR_MASK;
    

    JAnnotationImage annotationImage;
    ImageContainerState state;
    HashMap availableImageFormats;

    static ResourceBundle resbundle = ResourceBundle.getBundle ("org.concord.swing.annotationimage.localization.JAnnotationImageContainer", Locale.getDefault());
    static javax.swing.ImageIcon barHeader = new javax.swing.ImageIcon(org.concord.swing.JAnnotationImageContainer.class.getResource("/org/concord/swing/images/ToolBarHeaderBar.gif"));
    static javax.swing.ImageIcon iconSeparator = new javax.swing.ImageIcon(org.concord.swing.JAnnotationImageContainer.class.getResource("/org/concord/swing/images/ToolBarSeparator.gif"));


    javax.swing.JLayeredPane layeredPane = new javax.swing.JLayeredPane(){
        public void paint(Graphics g) {
            super.paint(g);
            paintInLayeredPane(g);
        }
    };
    
    public int  toolBarMask = ALL_TOOLBAR_MASK;
    
    HashMap  actions = new HashMap();
    
    JToolBar toolBar;
    
    javax.swing.ButtonGroup choosingModeButtonGroup=new javax.swing.ButtonGroup();

    
    public JAnnotationImageContainer(){
        this(null,null);
    }
    
    public JAnnotationImageContainer(java.io.InputStream is){
        this(null,null);
        restoreFromStream(is);
    }
    
    public JAnnotationImageContainer(Object state){
        super();
        setLayeredContainer();
        setOpaque(true);
        setState(state);
        addKeyHandler();
    }

    public JAnnotationImageContainer(JAnnotationImage annotationImage){
        super();
        setLayeredContainer();
        setOpaque(true);
        setAnnotationImage(annotationImage);
        setAnnotationImageLocation(1,1);
        addKeyHandler();
    }

    public JAnnotationImageContainer(BufferedImage bim, String pathToSave){
        super();
        setLayeredContainer();
        createAvailableOutImageFormats();
        setOpaque(true);
        if(bim != null){
            setAnnotationImage(bim);
            if(pathToSave != null) saveImage(bim,pathToSave);
            setSize(bim.getWidth()+2,bim.getHeight()+2);
        }else{
            setSize(500,500);
        }
        setAnnotationImageLocation(1,1);
        addKeyHandler();
    }
    
    public JAnnotationImageContainer(BufferedImage bim){
        this(bim,null);
    }

    public void setToolBarMask(int toolBarMask){
        this.toolBarMask = toolBarMask;
        if(toolBar != null){
            remove(toolBar);
            if(choosingModeButtonGroup != null){
                int nComponents = toolBar.getComponentCount();
                for(int i = 0; i < nComponents; i++){
                    Object obj = toolBar.getComponent(i);
                    if(obj instanceof AbstractButton){
                        choosingModeButtonGroup.remove((AbstractButton)obj);
                    }
                }
            }
            toolBar.removeAll();
        }
        createToolBar();
    }

    
    public Component getRenderingComponent(){
        return layeredPane;
    }
    
    public BufferedImage getScreenShotWithRobot(){
        Point myLocation = new Point(getLocation());
        Dimension mySize = getSize();
        javax.swing.SwingUtilities.convertPointToScreen(myLocation,getParent());
        try{
            java.awt.Robot robot = new java.awt.Robot();
            BufferedImage snapShotImage = robot.createScreenCapture(new Rectangle(myLocation.x,myLocation.y,mySize.width,mySize.height));
            //org.concord.swing.util.ComponentScreenshot.saveImageAsFile(snapShotImage,"/Users/dima/Desktop/screenshot.png","png");
            return snapShotImage;
        }catch(Throwable t){}
        return null;
    }

    public BufferedImage getScreenShot(){
        try{
            BufferedImage snapShotImage = org.concord.swing.util.ComponentScreenshot.getScreenshot(this);
            //org.concord.swing.util.ComponentScreenshot.saveImageAsFile(snapShotImage,"/Users/dima/Desktop/screenshot1.png","png");
            return snapShotImage;
        }catch(Throwable t){}
        return null;
    }
    
    protected void addKeyHandler(){
/*
        try{
            java.awt.Robot robot = new java.awt.Robot();
            java.awt.image.BufferedImage bimg = robot.createScreenCapture(new Rectangle(0,0,500,500));
            setAnnotationImage(bimg);
        }catch(Throwable t){}
        KeyboardFocusManager keyFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keyFocusManager.addKeyEventDispatcher(new KeyEventDispatcher(){
            public boolean dispatchKeyEvent(KeyEvent evt){
                if(!isEditMode() || (evt.getID() != KeyEvent.KEY_RELEASED)) return false;
                int keyCode = evt.getKeyCode();
                if(keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE){
                        System.out.println("dispatchKeyEvent");
                        return true;
                }                
                return false;
            }
        });
*/

        addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent evt){
                if(!isEditMode()) return;
                int keyCode = evt.getKeyCode();
                if(keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE){
                    deleteSelectedSpot();
                }                
            }
        });
    }
    
    public boolean isFocusable(){return true;}
    
    public JToolBar getToolBar(){return toolBar;}
    
    public void setToolBar(JToolBar toolBar){
        this.toolBar = toolBar;
    }
    
    protected JButton createActionButton(AnnotationImageAction aa){
        return createActionButton(aa,true);
    }
    
    protected JButton createActionButton(AnnotationImageAction aa,boolean initState){
        JButton button = new JButton(aa);
    	button.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    	button.setFocusPainted(false);
    	button.setBorderPainted(true);
    	button.setRequestFocusEnabled(false);
    	button.setVisible(initState);
    	Icon icon = button.getIcon();
    	if(icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0){
    	    button.setPreferredSize(new Dimension(icon.getIconWidth() + 7,icon.getIconHeight()+7));
    	}
    	return button;
    }
    
    protected void createActions(){
        AnnotationImageAction aa = new AnnotationImageAction(resbundle.getString("ToolbarSave"),
                                                             resbundle.getString("ToolbarSave"),"/org/concord/swing/images/save.gif",
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                            saveAnnotationImage();
                        }
        });        
        actions.put("Save",aa);
        aa = new AnnotationImageAction(resbundle.getString("ToolbarOpen"),
                                       resbundle.getString("ToolbarOpen"),"/org/concord/swing/images/open.gif",
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                            importAnnotationImage();
                        }
        });        
        actions.put("Open",aa);
        aa = new AnnotationImageAction(null,resbundle.getString("ToolbarEllipse"),"/org/concord/swing/images/CallOutEllipse.gif",
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                            setChoosingMode(JAnnotationImageModel.CHOOSING_MODE_ELLIPSE);
                        }
        });        
        actions.put("Ellipse",aa);
        aa = new AnnotationImageAction(null,resbundle.getString("ToolbarRectangle"),"/org/concord/swing/images/CallOutRectangle.gif",
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                            setChoosingMode(JAnnotationImageModel.CHOOSING_MODE_RECTANGLE);
                        }
        });        
        actions.put("Rectangle",aa);
        aa = new AnnotationImageAction(null,resbundle.getString("ToolbarPolygon"),"/org/concord/swing/images/CallOutPolygon.gif",
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                            setChoosingMode(JAnnotationImageModel.CHOOSING_MODE_POLYGON);
                        }
        });        
        actions.put("Polygon",aa);
        aa = new AnnotationImageAction(null,resbundle.getString("ToolbarDots"),"/org/concord/swing/images/CallOutDots.gif",
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                            setChoosingMode(JAnnotationImageModel.CHOOSING_MODE_POINTS);
                        }
        });        
        actions.put("Dots",aa);
        aa = new AnnotationImageAction(null,resbundle.getString("ToolbarSnapshot"),"/org/concord/swing/images/Album.gif",
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                        }
        });        
        actions.put("Snapshot",aa);
        aa = new AnnotationImageAction(null,resbundle.getString("ToolbarAnnotate"),"/org/concord/swing/images/Annotate.gif",
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                            setEditMode(true);
                        }
        });        
        actions.put("Annotate",aa);
        aa = new AnnotationImageAction(null,resbundle.getString("ToolbarNoAnnotate"),"/org/concord/swing/images/NoAnnotate.gif",
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                            setEditMode(false);
                        }
        });        
        actions.put("NoAnnotate",aa);

        aa = new AnnotationImageAction(resbundle.getString("ToolbarAnnotateCheckBoxName"),resbundle.getString("ToolbarAnnotateCheckBox"),
                                                             new java.awt.event.ActionListener(){
                        public void actionPerformed(java.awt.event.ActionEvent evt){
                            setEditMode(!isEditMode());
                        }
        });        

        
        actions.put("AnnotateCheckBox",aa);
    }
    
    protected void createToolBar(){
        setToolBar(new JToolBar(javax.swing.SwingConstants.HORIZONTAL));
	    //toolBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorderPainted(true);
	    toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
        toolBar.setPreferredSize(new Dimension(0,30));
        //toolBar.add(new javax.swing.JLabel(barHeader));
        toolBar.addSeparator(new Dimension(6,0));

        JCheckBox annotationCheckBox=new JCheckBox((AnnotationImageAction)actions.get("AnnotateCheckBox"));
	    annotationCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
	    annotationCheckBox.setSelected(isEditMode());
        toolBar.add(annotationCheckBox);
        
        toolBar.addSeparator(new Dimension(6,0));

        JButton editButton = null;
        if((toolBarMask & RECTANGLE_TOOLBAR_MASK) != 0){
            editButton = createActionButton((AnnotationImageAction)actions.get("Rectangle"));
            editButton.putClientProperty("CHOOSING_MODE",new Integer(JAnnotationImageModel.CHOOSING_MODE_RECTANGLE));
            choosingModeButtonGroup.add(editButton);
            toolBar.add(editButton);
        }
        if((toolBarMask & ELLIPSE_TOOLBAR_MASK) != 0){
            editButton = createActionButton((AnnotationImageAction)actions.get("Ellipse"));
            editButton.putClientProperty("CHOOSING_MODE",new Integer(JAnnotationImageModel.CHOOSING_MODE_ELLIPSE));
            choosingModeButtonGroup.add(editButton);
            toolBar.add(editButton);
        }
        if((toolBarMask & POLYGON_TOOLBAR_MASK) != 0){
            editButton = createActionButton((AnnotationImageAction)actions.get("Polygon"));
            editButton.putClientProperty("CHOOSING_MODE",new Integer(JAnnotationImageModel.CHOOSING_MODE_POLYGON));
            choosingModeButtonGroup.add(editButton);
            toolBar.add(editButton);
        }
        
        if((toolBarMask & DOTS_TOOLBAR_MASK) != 0){
            editButton = createActionButton((AnnotationImageAction)actions.get("Dots"));
            editButton.putClientProperty("CHOOSING_MODE",new Integer(JAnnotationImageModel.CHOOSING_MODE_POINTS));
            choosingModeButtonGroup.add(editButton);
            toolBar.add(editButton);
         }
        add(toolBar, java.awt.BorderLayout.PAGE_START);		
        syncToolBarButtons();
    }
    
    protected void setLayeredContainer(){
        setLayout(new java.awt.BorderLayout());
        add(layeredPane,java.awt.BorderLayout.CENTER);
        createActions();
        createToolBar();
    }
    
    public Object getState(){
        updateState();
        return state;
    }
    
    public void setState(Object s){
        if(!(s instanceof ImageContainerState)) return;
        state = (ImageContainerState)s;
        recreateFromState();
    }

    protected void updateState(){
        if(state == null) state = new ImageContainerState();
        state.containerBounds           = getBounds();
        state.annotationImageBounds      = (annotationImage == null)?null:annotationImage.getBounds();
        state.modelState                = (annotationImage == null)?null:annotationImage.getModel().getState();
    }

    protected void recreateFromState(){
        if(state == null) return;
        JAnnotationImageModel imageModel = new JAnnotationImageModel(state.getModelState());
	    boolean needEditMode = imageModel.isEditMode();
        setAnnotationImage(new JAnnotationImage(this,imageModel));
	    setBounds(state.getContainerBounds());
	    getAnnotationImage().setBounds(state.getAnnotationImageBounds());
	    getAnnotationImage().checkAnnotationToolTips();
	    if(needEditMode != getAnnotationImage().isEditMode()) setEditMode(needEditMode);
        LinkedList annotationSpots = annotationImage.getAnnotationSpots();
        if(annotationSpots == null) return;
        ListIterator it = annotationSpots.listIterator();
        while(it.hasNext()){
            AnnotationSpot as = (AnnotationSpot)it.next();
            as.setAnnotationImage(getAnnotationImage());
        }
    }

    public JAnnotationImage getAnnotationImage(){return annotationImage;}
    
    public void setAnnotationImage(JAnnotationImage annotationImage){
        boolean fromEmpty = (this.annotationImage == null);
        if(annotationImage == this.annotationImage) return;
        boolean oldEditMode = isEditMode();
        int oldChoosingMode = getChoosingMode();
        java.awt.Point oldLocation = (this.annotationImage != null)?this.annotationImage.getLocation():null;
        discardAnnotationImage();
        if(annotationImage == null) return;
        this.annotationImage = annotationImage;
        this.annotationImage.setEditMode(oldEditMode);
		Dimension id = annotationImage.getSize();
		Dimension d = getSize();
		if(oldLocation != null && !fromEmpty){
		    annotationImage.setLocation(oldLocation);
		}else{
		    annotationImage.setLocation((d.width - id.width)/2,(d.height - id.height)/2);
            setAnnotationImageLocation(1,1);
        }
        layeredPane.add(annotationImage);
        setChoosingMode(oldChoosingMode);
        repaint();
    }



    public void setAnnotationImage(java.awt.image.BufferedImage bim){
        setAnnotationImage(bim,null);
    }
    
    public void setAnnotationImage(java.awt.image.BufferedImage bim,String pathToSave){
        discardAnnotationImage();
        JAnnotationImage aim = new JAnnotationImage(this,bim);
        aim.setSize(bim.getWidth(),bim.getHeight());
        setAnnotationImage(aim);
        if(pathToSave != null) saveImage(bim,pathToSave);
        repaint();
    }

    public void setAnnotationImageLocation(java.awt.Point pt){
        if(annotationImage == null || pt == null) return;
		annotationImage.setLocation(pt);
    }

	public void setEditMode(boolean editMode){
	    if(annotationImage != null) annotationImage.setEditMode(editMode);
	    syncToolBarButtons();
	    revalidate();
	}

    protected void syncToolBarButtons(){
        if(toolBar == null) return;
        java.awt.Component []comps = toolBar.getComponents();
        if(comps == null) return;
        
        for (java.util.Enumeration e = choosingModeButtonGroup.getElements();e.hasMoreElements();){
            javax.swing.AbstractButton button = (javax.swing.AbstractButton)e.nextElement();
            int mode = ((Integer)button.getClientProperty("CHOOSING_MODE")).intValue();
            button.setEnabled(isEditMode());
            button.setSelected(mode == getChoosingMode());
            if(button.isSelected()){
                button.setBorder(SELECTED_BORDER);
			    button.setBackground(java.awt.Color.lightGray);
            }else{
                button.setBorder(UNSELECTED_BORDER);
			    button.setBackground(getBackground());
            }
        }        
    }

    public void setToolBarVisible(boolean value){
        if(value == toolBar.isVisible()) return;
        toolBar.setVisible(value);
        syncToolBarButtons();
        layeredPane.revalidate();
    }

    public boolean isEditMode(){
	    if(annotationImage != null) return annotationImage.isEditMode();
	    return false;
    }

    public boolean getHtmlSupport(){
	    return (annotationImage == null)?false:annotationImage.getHtmlSupport();
    }

    public void setHtmlSupport(boolean htmlSupport){
	    if(annotationImage != null) annotationImage.setHtmlSupport(htmlSupport);
    }
	
    
	public void setChoosingMode(int choosingMode){
	    if(annotationImage != null) annotationImage.setChoosingMode(choosingMode);
        syncToolBarButtons();
	}

    public int getChoosingMode(){
	    if(annotationImage != null) return annotationImage.getChoosingMode();
	    return JAnnotationImageModel.CHOOSING_MODE_RECTANGLE;
    }
    
    
	public void setToolTipMode(boolean toolTipMode){
	    if(annotationImage != null){
	        annotationImage.setToolTipMode(toolTipMode);
	        repaint();
	    }
	}
	
	public boolean isToolTipMode(){
	    if(annotationImage != null) return annotationImage.isToolTipMode();
	    return false;
	}
    
    
	public void clearAnnotationSpots(){
	    if(annotationImage != null) annotationImage.clearAnnotationSpots();
	    updateState();
	    repaint();
	}
	
	public void deleteSelectedSpot(){
	    if(annotationImage != null) annotationImage.deleteSelectedSpot();
	    updateState();
	    repaint();
	}

    public void setAnnotationImageLocation(int x,int y){
	    if(annotationImage != null) annotationImage.setLocation(x,y);
    }
    
    public java.awt.Point getAnnotationImageLocation(){
        if(annotationImage != null) return annotationImage.getLocation();
        return null;
    }

    public void serializeJava(java.io.File file){
        try{
	        serializeJava(new java.io.FileOutputStream(file));
	    }catch(Throwable t){}
    }
    
    public void serializeJava(java.io.OutputStream os){
        if(annotationImage == null || annotationImage.getModel() == null) return;
        if(annotationImage.getModel().getImageResourceString() == null){
            saveImage(getAnnotationImage().getModel().bim,null);
        }
            
        try{
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(os);
            oos.writeObject(getState());
            oos.close();
	    }catch(Throwable t){
	        System.out.println("container serializeJava  Throwable "+t);
	        t.printStackTrace();
	    }
    }
    
    public void serializeXML(java.io.File file){
        try{
	        serializeXML(new java.io.FileOutputStream(file));
	    }catch(Throwable t){}
    }
        
    public void serializeXML(java.io.OutputStream os){
        serializeXML(os,true);
    }
    public void serializeXML(java.io.OutputStream os,boolean askSaveImage){
        if(annotationImage == null || annotationImage.getModel() == null) return;
        
        if(annotationImage.getModel().getImageResourceString() == null && askSaveImage){
            saveImage(getAnnotationImage().getModel().bim,null);
        }
        try{
            java.beans.XMLEncoder encoder = new java.beans.XMLEncoder(new java.io.BufferedOutputStream(os));
            encoder.setExceptionListener(new java.beans.ExceptionListener(){
                public void exceptionThrown(Exception e){
                    e.printStackTrace();
                }
            });
            encoder.setPersistenceDelegate(java.awt.geom.Point2D.Double.class,new java.beans.DefaultPersistenceDelegate(new String[]{"x", "y"}));
            encoder.writeObject(getState());
            encoder.close();
	    }catch(Throwable t){
	        System.out.println("serializeJava  Throwable "+t);
	    }
    }
    
    public static JAnnotationImageContainer deserializeXML(java.io.File file){
	    try{
	        return deserializeXML(new java.io.FileInputStream(file));
        }catch(Throwable t){}
        return null;
    }
    public static JAnnotationImageContainer deserializeXML(java.io.InputStream is){
        try{
	        java.beans.XMLDecoder decoder = new java.beans.XMLDecoder(is);
	        return new JAnnotationImageContainer(decoder.readObject());
        }catch(Throwable t){
            System.out.println("deserializeXML Throwable "+t);
        }
        
        return null;	    
    }

    public static JAnnotationImageContainer deserializeJava(java.io.File file){
	    try{
	        return deserializeJava(new java.io.FileInputStream(file));
        }catch(Throwable t){
            System.out.println("deserializeJava Throwable "+t);
        }
        return null;
    }
    
    public static JAnnotationImageContainer deserializeJava(java.io.InputStream is){
        try{
	        java.io.ObjectInputStream decoder = new java.io.ObjectInputStream(is);
	        return new JAnnotationImageContainer(decoder.readObject());
        }catch(Throwable t){
            System.out.println("deserializeJava Throwable "+t);
        }
        return null;	    
    }

    public void dispose(){
        discardAnnotationImage();
    }
    
    protected void discardAnnotationImage(){
        if(annotationImage == null) return;
        layeredPane.remove(annotationImage);
        annotationImage.dispose();
    }


    protected void drawAnnotationImageConnections(Graphics g,AnnotationSpot as){
        java.awt.Point pt = annotationImage.getLocation();
        Rectangle ras = as.getBounds();
        java.awt.Point ptc = as.getAnnotationTipConnectionPoint();
        if(ptc.x == Integer.MIN_VALUE ||
           ptc.y == Integer.MIN_VALUE) return;
        int xc = ptc.x + pt.x;
        int yc = ptc.y + pt.y;
                
        javax.swing.JComponent jc = as.getAnnotationToolTip();
        if(jc == null) return;
        Rectangle rjc = new Rectangle(jc.getBounds());
        if(rjc.contains(xc,yc)) return;
        java.awt.Insets jci = jc.getInsets();
        if(jci == null) jci = new java.awt.Insets(0,0,0,0);
        java.awt.Color lineColor = java.awt.Color.gray;
        try{
            java.lang.reflect.Method m = jc.getBorder().getClass().getMethod("getLineColor",null);
            lineColor = (java.awt.Color)m.invoke(jc.getBorder(),null);
        }catch(Throwable t){}
        
        java.awt.BasicStroke bs = new java.awt.BasicStroke(1.2f);
        
        int x10 = jc.getLocation().x;
        int y10 = jc.getLocation().y - 5;
        int x1 = x10+jci.left;
        int y1 = y10 + (jc.getSize().height - jci.bottom);
        int x2 = x1+5;
        int y2 = y1 + 5;

        boolean leftVisible = isVisibleFromPoint(rjc,xc,yc,RECT_LEFT);
        boolean topVisible = isVisibleFromPoint(rjc,xc,yc,RECT_TOP);
        boolean rightVisible = isVisibleFromPoint(rjc,xc,yc,RECT_RIGHT);
        boolean bottomVisible = isVisibleFromPoint(rjc,xc,yc,RECT_BOTTOM);
        rjc.x += jci.left;
        rjc.y += jci.top;
        rjc.width  -= (jci.left + jci.right);
        rjc.height -= (jci.top + jci.bottom);

        java.awt.Polygon gp = new java.awt.Polygon();
        gp.addPoint(xc,yc);
        if(leftVisible && !topVisible && !rightVisible && !bottomVisible){//only left
            x1 = rjc.x;
            y1 = rjc.y + rjc.height / 2 - 5;
            x2 = rjc.x;
            y2 = rjc.y + rjc.height / 2 + 5;
            gp.addPoint(x1,y1);
            gp.addPoint(x2,y2);
        }else if(leftVisible && topVisible && !rightVisible && !bottomVisible){//left & top
            x1 = rjc.x + 5;
            y1 = rjc.y;
            x2 = rjc.x;
            y2 = rjc.y + 5;
            gp.addPoint(x1,y1);
            gp.addPoint(x2,y1);
            gp.addPoint(x2,y2);
        }else if(!leftVisible && topVisible && !rightVisible && !bottomVisible){//top
            x1 = rjc.x + rjc.width / 2 - 5;
            y1 = rjc.y;
            x2 = rjc.x + rjc.width / 2 + 5;
            y2 = rjc.y;
            gp.addPoint(x1,y1);
            gp.addPoint(x2,y2);
        }else if(!leftVisible && topVisible && rightVisible && !bottomVisible){//top & right
            x1 = rjc.x + rjc.width - 5;
            y1 = rjc.y;
            x2 = rjc.x + rjc.width;
            y2 = rjc.y+5;
            gp.addPoint(x1,y1);
            gp.addPoint(x2,y1);
            gp.addPoint(x2,y2);
        }else if(!leftVisible && !topVisible && rightVisible && !bottomVisible){//right
            x1 = rjc.x + rjc.width;
            y1 = rjc.y + rjc.height / 2 - 5;
            x2 = rjc.x + rjc.width;
            y2 = rjc.y + rjc.height / 2 + 5;
            gp.addPoint(x1,y1);
            gp.addPoint(x2,y2);
        }else if(!leftVisible && !topVisible && rightVisible && bottomVisible){//right & bottom
            x1 = rjc.x + rjc.width - 5;
            y1 = rjc.y + rjc.height;
            x2 = rjc.x + rjc.width;
            y2 = rjc.y + rjc.height - 5;
            gp.addPoint(x1,y1);
            gp.addPoint(x2,y1);
            gp.addPoint(x2,y2);
        }else if(!leftVisible && !topVisible && !rightVisible && bottomVisible){//bottom
            x1 = rjc.x + rjc.width / 2 - 5;
            y1 = rjc.y + rjc.height;
            x2 = rjc.x + rjc.width / 2 + 5;
            y2 = rjc.y + rjc.height;
            gp.addPoint(x1,y1);
            gp.addPoint(x2,y2);
        }else if(leftVisible && !topVisible && !rightVisible && bottomVisible){//bottom & left
            x1 = rjc.x + 5;
            y1 = rjc.y + rjc.height;
            x2 = rjc.x;
            y2 = rjc.y + rjc.height - 5;
            gp.addPoint(x1,y1);
            gp.addPoint(x2,y1);
            gp.addPoint(x2,y2);
        }
        gp.addPoint(xc,yc);
        Graphics2D gc = (Graphics2D)g.create();
        if(gc == null) return;
        java.awt.geom.Area clipArea = (gc.getClip() != null)?new java.awt.geom.Area(gc.getClip()):null;
        gc.setColor(jc.getBackground());
        gc.fillPolygon(gp);
        if(clipArea != null){
            clipArea.subtract(new java.awt.geom.Area(rjc));
            gc.setClip(clipArea);
        }
        gc.setColor(lineColor);
        gc.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        gc.drawLine(xc,yc,x1,y1);
        gc.drawLine(xc,yc,x2,y2);
        gc.dispose();
    }

    public void paintInLayeredPane(Graphics g) {
        if(annotationImage == null) return;
        LinkedList annotationSpots = annotationImage.getAnnotationSpots();
        if(annotationSpots == null) return;
        ListIterator it = annotationSpots.listIterator();
        while(it.hasNext()){
            AnnotationSpot as = (AnnotationSpot)it.next();
            drawAnnotationImageConnections(g,as);
        }
    }
    
    static boolean isVisibleFromPoint(Rectangle r,int xp, int yp,int type){
        if(type < RECT_TOP || type > RECT_LEFT) return false;
        java.awt.Point []pts = new java.awt.Point[5];
        pts[0] = new java.awt.Point(r.x,r.y);
        pts[1] = new java.awt.Point(r.x+r.width,r.y);
        pts[2] = new java.awt.Point(r.x+r.width,r.y+r.height);
        pts[3] = new java.awt.Point(r.x,r.y+r.height);
        pts[4] = pts[0];
        int xk = (pts[type].x + pts[type+1].x)/2;
        int yk = (pts[type].y + pts[type+1].y)/2;
        boolean b = false;
        for(int i = 0; i < 4; i++){
            if(i == type) continue;
            b = b | java.awt.geom.Line2D.linesIntersect(xp,yp,xk,yk,pts[i].x,pts[i].y,pts[i+1].x,pts[i+1].y);
            if(b) break;
        }
        return !b;
    }
    
    final static int RECT_TOP       = 0;
    final static int RECT_RIGHT     = 1;
    final static int RECT_BOTTOM    = 2;
    final static int RECT_LEFT      = 3;
    
    static java.awt.image.BufferedImage testImage = null;
    
    final static boolean DEFAULT_HTML_SUPPORT = false;
    
	public static void main(String args[]){
	
        System.setProperty("apple.laf.useScreenMenuBar", "true");//it's harmless for non apple system
	    frame = new javax.swing.JFrame("Annotation Image");
	    	    	    
//	    if(testImage == null) testImage = createBufferedImageFromFile("file:trypsin.jpg");

        java.awt.image.BufferedImage bim = null;
        try{
            java.awt.Robot robot = new java.awt.Robot();
            bim = robot.createScreenCapture(new Rectangle(0,0,500,500));
        }catch(Throwable t){}
        if(bim != null){
            imageContainer = new JAnnotationImageContainer(bim);
        }else{
            imageContainer = new JAnnotationImageContainer();
        }
        imageContainer.setHtmlSupport(DEFAULT_HTML_SUPPORT);
        imageContainer.setToolBarMask(ALL_TOOLBAR_MASK);
        
		frame.setSize(imageContainer.getSize());
        frame.getContentPane().add(imageContainer,java.awt.BorderLayout.CENTER);
        
        
		frame.addWindowListener(new java.awt.event.WindowAdapter(){
		    public void windowClosing(java.awt.event.WindowEvent evt){
                System.exit(0);
		    }
		    public void windowClosed(java.awt.event.WindowEvent evt){
                System.exit(0);
		    }
		});
		imageContainer.populateMenuBar(frame);
		//imageContainer.setToolBarVisible(false);
		frame.setVisible(true);

	}


    public void populateMenuBar(javax.swing.JFrame frame){
        final JAnnotationImageContainer ic = this;
        final javax.swing.JFrame finalFrame = frame;
        javax.swing.JMenuBar menubar = new javax.swing.JMenuBar();
        frame.setJMenuBar(menubar);

        JMenu filemenu = new JMenu(resbundle.getString("MenuFile"));
        menubar.add(filemenu);
        AnnotationImageAction act = (AnnotationImageAction)actions.get("Open");
	    JMenuItem openMenu = new JMenuItem((AnnotationImageAction)actions.get("Open"));
	    JMenuItem saveMenu = new JMenuItem((AnnotationImageAction)actions.get("Save"));
	    //JMenuItem saveMenu = new JMenuItem(resbundle.getString("ToolbarSave"),
	    //                                   (javax.swing.Icon)((AnnotationImageAction)actions.get("Save")).getValue(javax.swing.AbstractAction.SMALL_ICON));
	    
	   // JMenuItem.java
	    
        filemenu.add(openMenu);
        filemenu.add(saveMenu);
        
        JMenu toolsmenu = new JMenu(resbundle.getString("MenuTools"));
        menubar.add(toolsmenu);
	    JMenuItem menu = new JMenu(resbundle.getString("SubMenuToolbar"));
	    JMenuItem mi = new JMenuItem(resbundle.getString("MenuItemOn"));
        mi.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){
                ic.setToolBarVisible(true);
            }
        });
        menu.add(mi);

        mi = new JMenuItem(resbundle.getString("MenuItemOff"));
        mi.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){
                ic.setToolBarVisible(false);
            }
        });
        menu.add(mi);

        mi = new JMenuItem(resbundle.getString("MenuItemTest"));
        mi.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){
                getScreenShot();
            }
        });
        menu.add(mi);

        toolsmenu.add(menu);

        menu = new JMenu(resbundle.getString("SubMenuEdit"));

	    mi = new JMenuItem(resbundle.getString("SubMenuDeleteCurrentSpot"));
        mi.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){
                ic.deleteSelectedSpot();
            }
        });
        menu.add(mi);
        menu.add(new javax.swing.JSeparator());
	    mi = new JMenuItem(resbundle.getString("SubMenuClearAll"));
        mi.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){
                ic.clearAnnotationSpots();
            }
        });
        menu.add(mi);
        toolsmenu.add(menu);

	    menu = new JMenu(resbundle.getString("SubMenuToolTipMode"));
	    mi = new JMenuItem(resbundle.getString("MenuItemOn"));
        mi.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){
                ic.setToolTipMode(true);
            }
        });
        menu.add(mi);

	    mi = new JMenuItem(resbundle.getString("MenuItemOff"));
        mi.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent evt){
                ic.setToolTipMode(false);
            }
        });
        menu.add(mi);
        toolsmenu.add(menu);


    }

    static java.awt.image.BufferedImage createBufferedImageFromFile(String imageURLString){
        java.awt.image.BufferedImage bim = null;
	    java.awt.Image image = null;
	    try{
	        java.net.URL imageURL = new java.net.URL(imageURLString);
	        image = java.awt.Toolkit.getDefaultToolkit().createImage(imageURL);
    		java.awt.MediaTracker tracker = new java.awt.MediaTracker(new java.awt.Component(){});
    		tracker.addImage(image,0);
    		try{
    			tracker.waitForAll();
    		} catch (Exception e) {}
    		int width = image.getWidth(null);
    		int height = image.getHeight(null);
    	    java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
    	    java.awt.GraphicsDevice gd = ge.getDefaultScreenDevice();
    	    java.awt.GraphicsConfiguration gc = gd.getDefaultConfiguration();
    	    boolean hasAlpha = gc.getColorModel().hasAlpha();
    	    if(hasAlpha){
    	        bim = gc.createCompatibleImage(width,height);
    	    }else{
    	        bim = new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
    	    }
            java.awt.Graphics2D g2d = bim.createGraphics();
                g2d.drawImage(image,0,0,null);
            g2d.dispose();
        }catch(Throwable t){
            //System.out.println("url throwable "+t);
            bim = null;
        }
        return bim;
    }
    
    static javax.swing.JFrame frame;
    static JAnnotationImageContainer imageContainer;

    public static class ImageContainerState implements java.io.Serializable{
        static final long serialVersionUID = 8466245075650346081L;
        
        Rectangle containerBounds;
        Rectangle annotationImageBounds;
        Object    modelState;
        
        public Rectangle getContainerBounds(){return containerBounds;}
        public void setContainerBounds(Rectangle containerBounds){this.containerBounds = containerBounds;}

        public Rectangle getAnnotationImageBounds(){return annotationImageBounds;}
        public void setAnnotationImageBounds(Rectangle annotationImageBounds){this.annotationImageBounds = annotationImageBounds;}

        public Object getModelState(){return modelState;}
        public void setModelState(Object modelState){this.modelState = modelState;}
    }

    void createAvailableOutImageFormats(){
        if(availableImageFormats == null) availableImageFormats = new HashMap();
        String []imageNames = javax.imageio.ImageIO.getWriterFormatNames();
        if(imageNames == null || imageNames.length < 1) return;
        availableImageFormats = new HashMap();
        for(int i = 0; i < imageNames.length; i++){
           // if(!imageNames[i].equalsIgnoreCase("png")) continue;//only png was fine
            String key = imageNames[i].trim().toLowerCase();
            if(!availableImageFormats.containsKey(key)){
                availableImageFormats.put(key,new ImageFileFilter(imageNames[i]));
            }
        }
    }

    void saveImage(java.awt.image.BufferedImage bufferedImage,String pathToSave){
        if(availableImageFormats == null || availableImageFormats.size() < 1) return;
        final java.io.File fileToSave  = (pathToSave == null)?null:new java.io.File(pathToSave);
        
        final java.awt.image.BufferedImage bim = bufferedImage;
        
        if(bim == null) return;
        boolean dispatchThread = javax.swing.SwingUtilities.isEventDispatchThread(); 

        Runnable screenShotRunnable  = new Runnable(){
            public void run(){
                String fileExtension = null;
                java.io.File internalFileToSave = fileToSave;
                if(internalFileToSave != null){
                    int index = internalFileToSave.getAbsolutePath().lastIndexOf('.');
                    if(index > 0) fileExtension = internalFileToSave.getAbsolutePath().substring(index+1);
                }else{
                    JFileChooser chooser = new javax.swing.JFileChooser();
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setAcceptAllFileFilterUsed(false);
                    String userdir = System.getProperty("user.dir");
                    if(userdir != null) chooser.setCurrentDirectory(new java.io.File(userdir));
                
                    java.util.Iterator it = availableImageFormats.keySet().iterator();
                    while(it.hasNext()){
                        chooser.addChoosableFileFilter((ImageFileFilter)availableImageFormats.get(it.next()));
                    }
                    int retValue = chooser.showSaveDialog(javax.swing.SwingUtilities.getRoot(JAnnotationImageContainer.this));
                    if(retValue == JFileChooser.APPROVE_OPTION){
                        internalFileToSave = chooser.getSelectedFile();
                        javax.swing.filechooser.FileFilter ff = chooser.getFileFilter();                        
                        if(ff instanceof ImageFileFilter){
                            fileExtension = ((ImageFileFilter)ff).fileType;
                        }
                        if(fileExtension == null) return;
                        String lowerfilename = internalFileToSave.getName().toLowerCase();
                        if(!lowerfilename.endsWith("."+fileExtension.toLowerCase())){
                            internalFileToSave = new java.io.File(internalFileToSave.getAbsolutePath()+"."+fileExtension);
                        }
                    }

                }
                if(internalFileToSave != null && fileExtension != null){
                    if(!internalFileToSave.exists() || checkForReplace(internalFileToSave)){
                        try{
                            java.io.FileOutputStream fos = new java.io.FileOutputStream(internalFileToSave);
                            javax.imageio.ImageIO.write(bim,fileExtension,fos);
                            fos.close();
                            JAnnotationImageContainer.this.getAnnotationImage().getModel().setImageResourceString1(internalFileToSave.getAbsolutePath());
                        }catch(Throwable t){
                            System.out.println("Save as image THROWABLE "+t);
                            t.printStackTrace();
                        }
                    }
                }
            }
        };

        if(dispatchThread){
            screenShotRunnable.run();
        }else{
            try{
                javax.swing.SwingUtilities.invokeAndWait(screenShotRunnable);
            }catch(Throwable t){
                System.out.println("makeScreenShot throwable "+t);
            }
        }


    }

   boolean checkForReplace(java.io.File file){
        if(file == null || !file.exists()) return false;
        final Object[] options = { "Yes", "No" };
        return javax.swing.JOptionPane.showOptionDialog(null,
                  "The file '" + file.getName() +
                  "' already exists.  " +
                  "Replace existing file?",
                  "Warning",
                  javax.swing.JOptionPane.YES_NO_OPTION,
                  javax.swing.JOptionPane.WARNING_MESSAGE,
                  null,
                  options,
                  options[1]) == javax.swing.JOptionPane.YES_OPTION;

    }

    void importAnnotationImage(){
        boolean dispatchThread = javax.swing.SwingUtilities.isEventDispatchThread(); 

        Runnable importRunnable  = new Runnable(){
            public void run(){
                int fileExtensionType = -1;
                java.io.File internalFileToSave = null;
                CCJFileChooser chooser = new CCJFileChooser("org/concord/swing/JAnnotationImageContainer");
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
                //String userdir = System.getProperty("user.dir");
                //if(userdir != null) chooser.setCurrentDirectory(new java.io.File(userdir));
            
                chooser.addChoosableFileFilter(new ImageFileFilter("png"));
                chooser.addChoosableFileFilter(new ImageFileFilter("gif"));
                chooser.addChoosableFileFilter(new ImageFileFilter("jpeg"));
                chooser.addChoosableFileFilter(new AnnotationImageFileFilter());
                int retValue = chooser.showOpenDialog(javax.swing.SwingUtilities.getRoot(JAnnotationImageContainer.this));
                if(retValue == JFileChooser.APPROVE_OPTION){
                    internalFileToSave = chooser.getSelectedFile();
                    javax.swing.filechooser.FileFilter ff = chooser.getFileFilter();                        
                    if(ff instanceof ImageFileFilter){
                        fileExtensionType = 0;
                    }else if(ff instanceof AnnotationImageFileFilter){
                        fileExtensionType = 1;
                    }
                    if(fileExtensionType < 0) return;
                }

                if(internalFileToSave != null && fileExtensionType >= 0){
                    if(internalFileToSave.exists() && internalFileToSave.isFile()){
                        try{
                            if(fileExtensionType == 0){
	                            java.awt.image.BufferedImage bim = createBufferedImageFromFile("file:"+internalFileToSave.getCanonicalPath());
                                setAnnotationImage(bim);
                            }else if(fileExtensionType == 1){
                                restoreFromFile(internalFileToSave);
                                //restoreFromStream(new java.io.FileInputStream(internalFileToSave));
                                //restoreFromURL("file:"+internalFileToSave.getCanonicalPath());
                            }
                            setHtmlSupport(DEFAULT_HTML_SUPPORT);
                        }catch(Throwable t){
                            System.out.println("Import  image THROWABLE "+t);
                            t.printStackTrace();
                        }
                    }
                }
            }
        };

        if(dispatchThread){
            importRunnable.run();
        }else{
            try{
                javax.swing.SwingUtilities.invokeAndWait(importRunnable);
            }catch(Throwable t){
                System.out.println("Import throwable "+t);
            }
        }
    }
 
    public void restoreFromURL(String urlString){
        try{
            java.net.URL url = new java.net.URL(urlString);
            restoreFromStream(url.openStream());
        }catch(Throwable t){}
    }
    
    public void restoreFromStream(java.io.InputStream is){
        try{
            byte []buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(is);
            boolean done = false;
            Object state = null;
            java.awt.image.BufferedImage bim = null;
            while(!done){
                try{
                    ZipEntry ze = zis.getNextEntry();
                    done = (ze == null);
                    if(done) continue;
                    if(ze.getName().equals("bean.xml")){
                        java.beans.XMLDecoder decoder = new java.beans.XMLDecoder(new java.io.ByteArrayInputStream(getByteArrayFromStream(zis)));
                        state = decoder.readObject();
                   }else if(ze.getName().equals("image.png")){
                        bim = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(getByteArrayFromStream(zis)));
                    }
                    done = (state != null && bim != null);                    
                }catch(Throwable t){
                    System.out.println("restoreFromFile from stream "+t);
                    done = true;
                }
            }
            if(state != null){
                if(bim != null){
                    Object modelState = ((ImageContainerState)state).getModelState();
                    ((JAnnotationImageModel.ModelState)modelState).imageResourceString = null;
                }
                setState(state);
            }
            if(bim != null && annotationImage != null && annotationImage.getModel() != null){
                 annotationImage.getModel().createImage(bim);
            }
        }catch(Throwable t){
        }
    }
    
    public void restoreFromFile(java.io.File file){
        try{
            restoreFromStream(new java.io.FileInputStream(file));
        }catch(Throwable t){}
    }
/*
    public void restoreFromFile(java.io.File file){
        try{
            ZipFile zf = new ZipFile(file);
            Object state = null;
            java.awt.image.BufferedImage bim = null;
            try{
                ZipEntry ze = zf.getEntry("bean.xml");
                java.io.InputStream is = zf.getInputStream(ze);
                java.beans.XMLDecoder decoder = new java.beans.XMLDecoder(is);
                state = decoder.readObject();
            }catch(Throwable t){
                state = null;
                System.out.println("Import  image THROWABLE state "+t);
            }
            try{
                ZipEntry ze = zf.getEntry("image.png");
                java.io.InputStream is = zf.getInputStream(ze);
                bim = javax.imageio.ImageIO.read(is);
            }catch(Throwable t){
                bim = null;
                System.out.println("Import  image THROWABLE bim (2)  "+t);
            }
            if(state != null){
                if(bim != null){
                    Object modelState = ((ImageContainerState)state).getModelState();
                    ((JAnnotationImageModel.ModelState)modelState).imageResourceString = null;
                }
                setState(state);
            }
            if(bim != null && annotationImage != null && annotationImage.getModel() != null){
                 annotationImage.getModel().createImage(bim);
            }
        }catch(Throwable t){}                            
    }
*/    
    void saveAnnotationImage(){
        saveAnnotationImage(null);
    }
    
    
    public java.awt.image.BufferedImage getImageFromState(){
        java.awt.image.BufferedImage bimg = null;
        Object containerState = getState();
        if(containerState instanceof ImageContainerState){
            ImageContainerState ics = (ImageContainerState)containerState;
            Object modelState = ics.getModelState();
            if(modelState instanceof JAnnotationImageModel.ModelState){
                JAnnotationImageModel.ModelState ms = (JAnnotationImageModel.ModelState)modelState;
                bimg = ms.getImage();
            }
        }
        return bimg;
    }
    
    public void saveAnnotationImage(String pathToSave){
        boolean dispatchThread = javax.swing.SwingUtilities.isEventDispatchThread(); 
        final java.io.File fileToSave  = (pathToSave == null)?null:new java.io.File(pathToSave);
        
       
        Runnable saveRunnable  = new Runnable(){
            public void run(){
                String fileExtension = null;
                java.io.File internalFileToSave = fileToSave;
                if(internalFileToSave == null){
                    javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setAcceptAllFileFilterUsed(false);
                    String userdir = System.getProperty("user.dir");
                    if(userdir != null) chooser.setCurrentDirectory(new java.io.File(userdir));
                    chooser.addChoosableFileFilter(new AnnotationImageFileFilter());
                    
                    int retValue = chooser.showSaveDialog(javax.swing.SwingUtilities.getRoot(JAnnotationImageContainer.this));
                    if(retValue == JFileChooser.APPROVE_OPTION){
                        internalFileToSave = chooser.getSelectedFile();

                        String lowerfilename = internalFileToSave.getName().toLowerCase();
                        if(!lowerfilename.endsWith(AnnotationImageFileFilter.FILE_EXTENSION)){
                            internalFileToSave = new java.io.File(internalFileToSave.getAbsolutePath()+AnnotationImageFileFilter.FILE_EXTENSION);
                        }
                    }
                }
                if(internalFileToSave != null){
                    if(!internalFileToSave.exists() || checkForReplace(internalFileToSave)){
                        try{
                            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                            serializeXML(bos,false);
                            ZipOutputStream zos = new ZipOutputStream(new java.io.FileOutputStream(internalFileToSave));
                            ZipEntry zipEntry = new ZipEntry("bean.xml");
                            zos.putNextEntry(zipEntry);
                            byte []bytes = bos.toByteArray();
                            zos.write(bytes,0,bytes.length);
                            zos.closeEntry();
                            if(annotationImage != null && annotationImage.getModel() != null){
                                java.awt.image.BufferedImage bim = annotationImage.getModel().bim;
                                if(bim != null){
                                    zipEntry = new ZipEntry("image.png");
                                    zos.putNextEntry(zipEntry);
                                    javax.imageio.ImageIO.write(bim,"png",zos);
                                    zos.closeEntry();
                                }
                            }
                            zos.close();
                        }catch(Throwable t){
                            System.out.println("Save Annotation Image THROWABLE "+t);
                            t.printStackTrace();
                        }
                    }
                }
            }
         };

        if(dispatchThread){
            saveRunnable.run();
        }else{
            try{
                javax.swing.SwingUtilities.invokeAndWait(saveRunnable);
            }catch(Throwable t){
                System.out.println("saveRunnable throwable "+t);
            }
        }


    }

    private byte []getByteArrayFromStream(java.io.InputStream is) throws java.io.IOException{
        byte []buffer = new byte[1024];
        int rb;
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        while((rb = is.read(buffer,0,buffer.length)) > 0){
            bos.write(buffer,0,rb);
        }
        bos.close();
        return bos.toByteArray();
    }

}

class ImageFileFilter extends javax.swing.filechooser.FileFilter{
String fileUpperType;
String fileType;
String fileExtension;
    ImageFileFilter(String fileType){
        this.fileType = fileType;
        fileUpperType = this.fileType.toUpperCase();
        fileExtension = "."+fileType;
    }
    public boolean accept(java.io.File f){
        if(f == null) return false;
        if (f.isDirectory())  return true;
        String str = f.getName().toUpperCase();
        if (str.endsWith(fileExtension.toUpperCase())) return true;
        if(fileUpperType.equals("JPG") || fileUpperType.equals("JPEG")){
            return (str.endsWith(".JPG") || str.endsWith(".JPEG"));
        }
        return false;
    }
    public String getDescription(){
        return fileUpperType+" images";
    }
    
}

class AnnotationImageFileFilter extends javax.swing.filechooser.FileFilter{
static final String FILE_EXTENSION = ".annimg";
    AnnotationImageFileFilter(){}
    
    public boolean accept(java.io.File f){
        if(f == null) return false;
        if (f.isDirectory())  return true;
        return (f.getName().toLowerCase().endsWith(FILE_EXTENSION));
    }
    public String getDescription(){
        return "Annotation image files";
    }
}

class AnnotationImageAction extends javax.swing.AbstractAction{
java.awt.event.ActionListener listener;

    AnnotationImageAction(String name,String toolTip,String urlString, java.awt.event.ActionListener l){
        super(name,new javax.swing.ImageIcon(org.concord.swing.JAnnotationImageContainer.class.getResource(urlString)));
        putValue(javax.swing.AbstractAction.SHORT_DESCRIPTION,toolTip);        
        listener = l;
    }
        
    AnnotationImageAction(String name,String toolTip,java.awt.event.ActionListener l){
        super(name,null);
        putValue(javax.swing.AbstractAction.SHORT_DESCRIPTION,toolTip);        
        listener = l;
    }
        
    public void actionPerformed(java.awt.event.ActionEvent event){
		if(listener != null) listener.actionPerformed(new java.awt.event.ActionEvent((Object)this, 
		                                            event.getID(), 
												    event.getActionCommand(), 
    										        event.getModifiers()));
    }
}
