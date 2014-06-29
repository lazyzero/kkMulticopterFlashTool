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
 * <p>Title: QuickHull</p>
 * @author Dmitry Markman, dima@concord.org
 * @version 1.0
 */
package org.concord.swing;
import java.awt.Point;
import java.util.Vector;

/**
 * class that implements QuickHull algorithm to construct convex hull polygon<br>
 * usage example:
 * <blockquote><pre>
 * int nDots = 500;
 * int offset = 50;
 * int sizeX = 400;
 * int sizeY = 400;
 * Point []dots = new Point[nDots];
 * for(int i =0; i < dots.length; i++){
 *     int px = (int)Math.round(offset + (sizeX - 2*offset)*Math.random());
 *     int py = (int)Math.round(offset + (sizeY - 2*offset)*Math.random());
 *     dots[i] = new Point(px,py);
 * }
 * qh = new QuickHull(dots);
 * Point []dots = qh.getOriginalPoints();
 * Vector outPoints = qh.getHullPointsAsVector();
 * </pre></blockquote>
 */


public class QuickHull{
    Point []originalPoints;
    int fullSteps = 0;
    Vector hullPoints = new Vector();
/*
 * constructor for <code>QuickHull</code> class
 * @param originalPoints {@link Point}[] initial points 
 */
    public QuickHull(Point []originalPoints){
        this.originalPoints = originalPoints;
        qhull(originalPoints,0,0);
        reorderPoints(hullPoints);
    }

/**
 * Returns original {@link Point} array.
 * @return original {@link Point} array
 */
    public Point []getOriginalPoints(){
        return originalPoints;
    }
    
/**
 * Returns convex hull points as {@link Vector}.
 * @return convex hull points as {@link Vector}.
 */
    public Vector getHullPointsAsVector(){
        return (Vector)hullPoints.clone();
    }

/**
 * Returns convex hull points as {@link Point}[].
 * @return convex hull points as {@link Point}[].
 */
    public Point []getHullPointsAsArray(){
        if(hullPoints == null) return null;
        Point []hulldots = new Point[hullPoints.size()];
        for(int i = 0; i < hulldots.length; i++){
            hulldots[i] = (Point)hullPoints.elementAt(i);
        }
        return hulldots;
    }


    void reorderPoints(Vector v){
        AngleWrapper []angleWrappers = new AngleWrapper[v.size()];
        double xc = 0;
        double yc = 0;
        for(int i = 0; i < v.size(); i++){
            Point pt = (Point)v.elementAt(i);
            xc += pt.x;
            yc += pt.y;
        }

        xc /= v.size();
        yc /= v.size();

        for(int i = 0; i < angleWrappers.length; i++){
            angleWrappers[i] = createAngleWrapper((Point)v.elementAt(i),xc,yc);
        }
        java.util.Arrays.sort(angleWrappers,new AngleComparator());
        v.removeAllElements();
        for(int i = 0; i < angleWrappers.length; i++){
            v.add(angleWrappers[i].pt);
        }        
    }

    void qhull(Object []dots0,int up,int step){
        fullSteps++;
        if(dots0 == null || dots0.length < 1 || step > 200) return;
        if(dots0.length < 2){
            addHullPoint((Point)dots0[0]);
            return;
       }
        try{
            int leftIndex = 0;
            int rightIndex = 0;
            for(int i = 1; i < dots0.length; i++){
                if(((Point)dots0[i]).x < ((Point)dots0[leftIndex]).x){
                    leftIndex = i;
                }
                if(((Point)dots0[i]).x > ((Point)dots0[rightIndex]).x){
                    rightIndex = i;
                }
            }
            Point leftPoint = (Point)dots0[leftIndex];
            Point rightPoint = (Point)dots0[rightIndex];
            addHullPoint(leftPoint);
            addHullPoint(rightPoint);
            if(dots0.length == 3){
                int middlePoint = -1;
                for(int i = 0; i < dots0.length; i++){
                    if(i == leftIndex || i == rightIndex) continue;
                    middlePoint = i;
                    break;
                } 
                addHullPoint((Point)dots0[middlePoint]);
            }else if(dots0.length > 3){
                Vector vIn = new Vector();
                Vector vOut = new Vector();
                if(up >= 0){
                    int upIndex = selectPoints(dots0,leftPoint,rightPoint,true,vIn);
                    if(upIndex >= 0 && vIn.size() > 0){
                        Point upPoint = (Point)vIn.elementAt(upIndex);
                        vOut.removeAllElements();
                        selectPoints(vIn,leftPoint,upPoint,true,vOut);
                        qhull(vOut.toArray(),1,step+1);
                        vOut.removeAllElements();
                        selectPoints(vIn,upPoint,rightPoint,true,vOut);
                        qhull(vOut.toArray(),1,step+1);
                    }
                }
                if(up <= 0){
                    vIn.removeAllElements();
                    int downIndex = selectPoints(dots0,rightPoint,leftPoint,false,vIn);
                    if(downIndex >= 0 && vIn.size() > 0){
                        Point downPoint = (Point)vIn.elementAt(downIndex);
                        vOut.removeAllElements();
                        selectPoints(vIn,rightPoint,downPoint,false,vOut);
                        qhull(vOut.toArray(),-1,step+1);
                        vOut.removeAllElements();
                        selectPoints(vIn,downPoint,leftPoint,false,vOut);
                        qhull(vOut.toArray(),-1,step+1);
                    }
                }
            }  
        }catch(Throwable t){}
    }
    
    void addHullPoint(Point pt){
        if(!hullPoints.contains(pt)) hullPoints.add(pt);
    }
    
    static int selectPoints(Object []pIn,Point pLeft,Point pRight,boolean up,Vector vOut){
        int retValue = -1;
       if(pIn == null || vOut == null) return retValue;
        double k = (double)(pRight.y - pLeft.y)/(double)(pRight.x - pLeft.x);
        double A = -k;
        double B = 1;
        double C = k*pLeft.x-pLeft.y;
        double dup = 0;
        for(int i = 0; i < pIn.length; i++){
            Point pt = (Point)pIn[i];
            if(pt.equals(pLeft) || pt.equals(pRight)) continue;
            int px = pt.x;
            int py = pt.y;
            double y = pLeft.y + k*(px - pLeft.x);
            if((!up && y < py) || (up && y > py)){
                vOut.add(pt);
                double d = (A*px+B*py+C);
                if(d < 0) d = -d;
                if(d > dup){
                    dup = d;
                    retValue = vOut.size() - 1;
                }
            }
        }
        vOut.add(pLeft);
        vOut.add(pRight);
        return retValue;
    }
    
    static int selectPoints(Vector vIn,Point pLeft,Point pRight,boolean up,Vector vOut){
        int retValue = -1;
        if(vIn == null || vOut == null) return retValue;
        double k = (double)(pRight.y - pLeft.y)/(double)(pRight.x - pLeft.x);
        double A = -k;
        double B = 1;
        double C = k*pLeft.x-pLeft.y;
        double dup = 0;
        for(int i = 0; i < vIn.size(); i++){
            Point pt = (Point)vIn.elementAt(i);
            if(pt.equals(pLeft) || pt.equals(pRight)) continue;
            int px = pt.x;
            int py = pt.y;
            double y = pLeft.y + k*(px - pLeft.x);
            if((!up && y < py) || (up && y > py)){
                vOut.add(pt);
                double d = (A*px+B*py+C);
                if(d < 0) d = -d;
                if(d > dup){
                    dup = d;
                    retValue = vOut.size() - 1;
                }
            }
        }
        vOut.add(pLeft);
        vOut.add(pRight);
        return retValue;
    }
    
    static AngleWrapper createAngleWrapper(Point pt,double xc,double yc){
        double angle = Math.atan2(pt.y-yc,pt.x-xc);
        if(angle < 0) angle += 2*Math.PI;
        return new AngleWrapper(angle,new Point(pt));
    }

    static class AngleComparator implements java.util.Comparator{
        public int compare(Object obj1,Object obj2){
            if(!(obj1 instanceof AngleWrapper) || !(obj2 instanceof AngleWrapper)) return 0;
            AngleWrapper ac1 = (AngleWrapper)obj1;
            AngleWrapper ac2 = (AngleWrapper)obj2;
            return (ac1.angle < ac2.angle)?-1:1;
        }
    }

    static class AngleWrapper implements Comparable{
        double angle;
        Point pt;
        AngleWrapper(double angle,Point pt){
            this.angle = angle;
            this.pt = pt;
        }
        public int compareTo(Object obj){
            if(!(obj instanceof AngleWrapper)) return 0;
            AngleWrapper ac = (AngleWrapper)obj;
            return (ac.angle < angle)?-1:1;
        }
    }


    static int nDots = 500;
    static int offset = 50;
    static int sizeX = 400;
    static int sizeY = 400;
    static double r = (double)sizeX/2 - offset;
    static double xc = (double)sizeX / 2;
    static double yc = (double)sizeY / 2;
    static QuickHull qh;
    
    


    static void initDots(){
        Point []dots = new Point[nDots];
        for(int i =0; i < dots.length; i++){
            int px = (int)Math.round(offset + (sizeX - 2*offset)*Math.random());
            int py = (int)Math.round(offset + (sizeY - 2*offset)*Math.random());
            
           // double angle = (double)i*Math.PI*2/(double)dots.length;
           // int px = (int)Math.round(xc + r*Math.cos(angle));
           // int py = (int)Math.round(yc + r*Math.sin(angle));
            
            dots[i] = new Point(px,py);
        }
        
        qh = new QuickHull(dots);
        System.out.println("hullPoints "+qh.hullPoints.size()+" fullSteps "+qh.fullSteps);
    }


    static void drawDots(java.awt.Graphics g){
        if(qh == null) return;
        g.setColor(java.awt.Color.gray);
        Point []dots = qh.getOriginalPoints();
        for(int i = 0; i < dots.length; i++){
            Point pt = dots[i];
            g.fillRect(pt.x,pt.y,3,3);
        }
        g.setColor(java.awt.Color.red);
        Vector outPoints = qh.getHullPointsAsVector();
        for(int i = 0; i < outPoints.size(); i++){
            Point pt = (Point)outPoints.elementAt(i);
            g.fillRect(pt.x,pt.y,3,3);
            if(i > 0){
                Point ptPrev = (Point)outPoints.elementAt(i-1);
                g.drawLine(ptPrev.x,ptPrev.y,pt.x,pt.y);
            }
            if(i == outPoints.size() - 1){
                Point ptPrev = (Point)outPoints.elementAt(0);
                g.drawLine(ptPrev.x,ptPrev.y,pt.x,pt.y);
            }
        }
    }

	public static void main(String args[]){
	    javax.swing.JFrame frame1 = new javax.swing.JFrame("test"){
	        boolean wasInited = false;
	        public void paint(java.awt.Graphics g){
	            super.paint(g);
	            if(!wasInited){
	                initDots();
	                wasInited = true;
	            }
	            drawDots(g);
	        }
		};
		frame1.setSize(sizeX,sizeY);
		frame1.setVisible(true);
    }
}