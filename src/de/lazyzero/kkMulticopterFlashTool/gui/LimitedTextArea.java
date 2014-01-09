/*******************************************************************************
 * This file is part of GECAMed.
 * 
 * GECAMed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (L-GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GECAMed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (L-GPL)
 * along with GECAMed.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * GECAMed is Copyrighted by the Centre de Recherche Public Henri Tudor (http://www.tudor.lu)
 * (c) CRP Henri Tudor, Luxembourg, 2008
 *******************************************************************************/
package de.lazyzero.kkMulticopterFlashTool.gui;

import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/*
 *  A limited text area that can only hold a specifi amount of charachters.
 */

public class LimitedTextArea extends JTextArea {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int limit = 1000;

	public LimitedTextArea(int limit) {
		super();
		this.limit = limit;
		setLineWrap(true);
		init();
	}
	
	public LimitedTextArea(String text, int limit) {
		super();
		this.limit = limit;
		this.setText(text);
		setLineWrap(true);
		init();
	}


	private void init() {
		this.setDocument(new LimitValidation());
	}

	public class LimitValidation extends PlainDocument {
		// ~ Static fields/initializers
		// =========================================

		private static final long serialVersionUID = 1L;

		// ~ Constructors
		// =======================================================
		/**
		 * Constructor for the Validationdocument
		 */
		public LimitValidation() {
			super();
		}

		// ~ Methods
		// ============================================================

		public void insertString(int offset, String str, AttributeSet attr)
				throws BadLocationException {
			if ((getLength() + str.length()) <= limit)
				super.insertString(offset, str, attr);
		}
	}
}
