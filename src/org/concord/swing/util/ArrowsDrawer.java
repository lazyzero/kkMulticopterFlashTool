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

package org.concord.swing.util;

import java.awt.Graphics;


public class ArrowsDrawer {
	public static void drawArrow(Graphics g, float x1, float y1, float x2, float y2, float lineThickness){
		drawArrow(g, x1, y1, x2, y2, lineThickness, 3*lineThickness, 3*lineThickness);
	}

	public static void drawArrow(Graphics g, float x1, float y1, float x2, float y2, float lineThickness, float arrowHeadLength, float arrowHeadWidth){
		drawArrowInternal(g, x1, y1, x2, y2, lineThickness, arrowHeadLength, arrowHeadWidth, false);
	}

	public static void fillArrow(Graphics g, float x1, float y1, float x2, float y2, float lineThickness){
		fillArrow(g, x1, y1, x2, y2, lineThickness, 3*lineThickness, 3*lineThickness);
	}

	public static void fillArrow(Graphics g, float x1, float y1, float x2, float y2, float lineThickness, float arrowHeadLength, float arrowHeadWidth){
		drawArrowInternal(g, x1, y1, x2, y2, lineThickness, arrowHeadLength, arrowHeadWidth, true);
	}

	private static void drawArrowInternal(Graphics g, float x1, float y1, float x2, float y2, float lineThickness, float arrowHeadLength, float arrowHeadWidth, boolean fill){

		int []xpol = new int[7];
		int []ypol = new int[7];

		float lineLen, linePrX, linePrY;
		float wArrowHeadLength, wArrowHeadWidth;
		float x3, y3;

		//Parameters
		linePrX = x2-x1;
		linePrY = y2-y1;
		lineLen = (float)Math.sqrt(linePrX*linePrX + linePrY*linePrY);

		wArrowHeadLength = arrowHeadLength/lineLen;

		x3 = x2 - Math.round(linePrX*wArrowHeadLength);
		y3 = y2 - Math.round(linePrY*wArrowHeadLength);
		
		//Rectangle
		float lineWidth;
		float wLineWidth;
			
		//Parameters
		lineWidth=lineThickness;
		wLineWidth=((lineWidth-1)/2)/lineLen;

		//Arrow line
		xpol[0]=Math.round(x1 + linePrY*wLineWidth);
		ypol[0]=Math.round(y1 - linePrX*wLineWidth);
		
		xpol[6]=Math.round(x1 - linePrY*wLineWidth);
		ypol[6]=Math.round(y1 + linePrX*wLineWidth);
		
		xpol[1]=Math.round(x3 + linePrY*wLineWidth);
		ypol[1]=Math.round(y3 - linePrX*wLineWidth);
		
		xpol[5]=Math.round(x3 - linePrY*wLineWidth);
		ypol[5]=Math.round(y3 + linePrX*wLineWidth);

		
		wArrowHeadWidth = arrowHeadWidth/lineLen;

		//Arrow head
		xpol[2]=Math.round(x3 + linePrY*wArrowHeadWidth/2);
		ypol[2]=Math.round(y3 - linePrX*wArrowHeadWidth/2);
		
		xpol[3]=Math.round(x2);
		ypol[3]=Math.round(y2);
		
		xpol[4]=Math.round(x3 - linePrY*wArrowHeadWidth/2);
		ypol[4]=Math.round(y3 + linePrX*wArrowHeadWidth/2);

		if (fill){
			
			g.drawPolygon(xpol,ypol,7);
			g.fillPolygon(xpol,ypol,7);
		}
		else{

			g.drawPolygon(xpol,ypol,7);
		}
		
	}
	
	public static void drawDashedArrow(Graphics g, float x1, float y1, float x2, float y2, float lineThickness){
		drawDashedArrow(g, x1, y1, x2, y2, lineThickness, 3*lineThickness, 3*lineThickness);
	}

	public static void drawDashedArrow(Graphics g, float x1, float y1, float x2, float y2, float lineThickness, float arrowHeadLength, float arrowHeadWidth){
		drawDashedArrowInternal(g, x1, y1, x2, y2, lineThickness, arrowHeadLength, arrowHeadWidth, false);
	}

	public static void fillDashedArrow(Graphics g, float x1, float y1, float x2, float y2, float lineThickness){
		fillDashedArrow(g, x1, y1, x2, y2, lineThickness, 3*lineThickness, 3*lineThickness);
	}

	public static void fillDashedArrow(Graphics g, float x1, float y1, float x2, float y2, float lineThickness, float arrowHeadLength, float arrowHeadWidth){
		drawDashedArrowInternal(g, x1, y1, x2, y2, lineThickness, arrowHeadLength, arrowHeadWidth, true);
	}

	private static void drawDashedArrowInternal(Graphics g, float x1, float y1, float x2, float y2, float lineThickness, float arrowHeadLength, float arrowHeadWidth, boolean fill){

		int []xpol = new int[3];
		int []ypol = new int[3];

		float lineLen, linePrX, linePrY;
		float wArrowHeadLength;
		float x3, y3;

		//Parameters
		linePrX = x2-x1;
		linePrY = y2-y1;
		lineLen = (float)Math.sqrt(linePrX*linePrX + linePrY*linePrY);

		wArrowHeadLength = arrowHeadLength/lineLen;

		x3 = x2 - linePrX*wArrowHeadLength;
		y3 = y2 - linePrY*wArrowHeadLength;
		
		//Arrow line
		if (fill){
			ArrowsDrawer.fillDashedRect(g, x1, y1, x3, y3, lineThickness, lineThickness, lineThickness/2);
		}
		else{
			ArrowsDrawer.drawDashedRect(g, x1, y1, x3, y3, lineThickness, lineThickness, lineThickness/2);
		}
		
		//Arrow head
		xpol[0]=Math.round(x3 + linePrY*wArrowHeadLength/2);
		ypol[0]=Math.round(y3 - linePrX*wArrowHeadLength/2);
		xpol[1]=Math.round(x2);
		ypol[1]=Math.round(y2);
		xpol[2]=Math.round(x3 - linePrY*wArrowHeadLength/2);
		ypol[2]=Math.round(y3 + linePrX*wArrowHeadLength/2);
		
		if (fill){
			g.fillPolygon(xpol,ypol,3);
		}
		else{
			g.drawPolygon(xpol,ypol,3);
		}
		
	}

	public static void drawRect(Graphics g, float x1, float y1, float x2, float y2, float rectWidth)
	{
		drawRectInternal(g, x1, y1, x2, y2, rectWidth, false);
	}

	public static void fillRect(Graphics g, float x1, float y1, float x2, float y2, float rectWidth)
	{
		drawRectInternal(g, x1, y1, x2, y2, rectWidth, true);
	}

	private static void drawRectInternal(Graphics g, float x1, float y1, float x2, float y2, float rectWidth, boolean fill)
	{
		int []xpol = new int[4];
		int []ypol = new int[4];

		//Rectangle
		float x00,y00,x01,y01,x10,y10,x11,y11;
		float lineLen, linePrX, linePrY;
		float lineWidth;
		float wLineWidth;
			
		//Parameters
		linePrX = x2-x1;
		linePrY = y2-y1;
		lineLen = (float)Math.sqrt(linePrX*linePrX + linePrY*linePrY);
		lineWidth=rectWidth;
		wLineWidth=(lineWidth/2)/lineLen;
		x00 = x1 + linePrY*wLineWidth;
		y00 = y1 - linePrX*wLineWidth;
		x01 = x1 - linePrY*wLineWidth;
		y01 = y1 + linePrX*wLineWidth;	
		x11 = x2 + linePrY*wLineWidth;
		y11 = y2 - linePrX*wLineWidth;
		x10 = x2 - linePrY*wLineWidth;
		y10 = y2 + linePrX*wLineWidth;

		xpol[0]=Math.round(x00);
		ypol[0]=Math.round(y00);
		xpol[1]=Math.round(x01);
		ypol[1]=Math.round(y01);
		xpol[2]=Math.round(x10);
		ypol[2]=Math.round(y10);
		xpol[3]=Math.round(x11);
		ypol[3]=Math.round(y11);

		if (fill){
			g.fillPolygon(xpol,ypol,4);
		}
		else{
			g.drawPolygon(xpol,ypol,4);
		}
	
	}

	public static void drawDashedRect(Graphics g, float x1, float y1, float x2, float y2, float rectWidth, float dashLength, float spaceLength)
	{
		drawDashedRectInternal(g, x1, y1, x2, y2, rectWidth, dashLength, spaceLength, false);
	}

	public static void fillDashedRect(Graphics g, float x1, float y1, float x2, float y2, float rectWidth, float dashLength, float spaceLength)
	{
		drawDashedRectInternal(g, x1, y1, x2, y2, rectWidth, dashLength, spaceLength, true);
	}

	private static void drawDashedRectInternal(Graphics g, float x1, float y1, float x2, float y2, float rectWidth, float dashLength, float spaceLength, boolean fill)
	{
		int []xpol = new int[4];
		int []ypol = new int[4];

		//Rectangle (dashed)
		float x00,y00,x01,y01,x10,y10,x11,y11;
		float xi;
		float lineLen, linePrX, linePrY;
		float lineWidth, dashLen, spaceLen;
		float wLineWidth, wDashLen, wSpaceLen;
			
		//Parameters
		linePrX = x2-x1;
		linePrY = y2-y1;
		lineLen = (float)Math.sqrt(linePrX*linePrX + linePrY*linePrY);
		dashLen=dashLength;
		spaceLen=spaceLength;
		lineWidth=rectWidth;
		wDashLen=dashLen/lineLen;
		wSpaceLen=spaceLen/lineLen;
		wLineWidth=(lineWidth/2)/lineLen;
		x00 = x1 + linePrY*wLineWidth;
		y00 = y1 - linePrX*wLineWidth;
		x01 = x1 - linePrY*wLineWidth;
		y01 = y1 + linePrX*wLineWidth;
		////////////////////

		xi=0;
//		while ( xi < Math.ceil(lineLen/(dashLen+spaceLen)) ){
		while ( xi < lineLen ){
		
			x10 = x01 + linePrX*wDashLen;
			y10 = y01 + linePrY*wDashLen;
			x11 = x00 + linePrX*wDashLen;
			y11 = y00 + linePrY*wDashLen;
			if ( xi+dashLen > lineLen){
				x11 = x2 + linePrY*wLineWidth;
				y11 = y2 - linePrX*wLineWidth;
				x10 = x2 - linePrY*wLineWidth;
				y10 = y2 + linePrX*wLineWidth;
			}
			
			xi=xi+dashLen;
			
			xpol[0]=Math.round(x00);
			ypol[0]=Math.round(y00);
			xpol[1]=Math.round(x01);
			ypol[1]=Math.round(y01);
			xpol[2]=Math.round(x10);
			ypol[2]=Math.round(y10);
			xpol[3]=Math.round(x11);
			ypol[3]=Math.round(y11);
//System.out.println("one:  "+xpol[0]+","+ypol[0]+","+xpol[1]+","+ypol[1]+","+xpol[2]+","+ypol[2]+","+xpol[3]+","+ypol[3]);

			if (fill){
				g.fillPolygon(xpol,ypol,4);
			}
			else{
				g.drawPolygon(xpol,ypol,4);
			}

			x00 = x11 + linePrX*wSpaceLen;
			y00 = y11 + linePrY*wSpaceLen;
			x01 = x10 + linePrX*wSpaceLen;
			y01 = y10 + linePrY*wSpaceLen;
			
			xi=xi+spaceLen;
		}

	
	}
	
}
