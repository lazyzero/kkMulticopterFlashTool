/**
 * KKMulticopterFlashTool, a avrdude GUI for flashing KK boards and other
 *   equipment.
 *   Copyright (C) 2011 Christian Moll
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lazyzero.kkMulticopterFlashTool.gui;

import static lu.tudor.santec.i18n.Translatrix._;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TestPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private String title;
	private CellConstraints cc;
	private TestControllPanel testControllPanel;
	private TestEvaluationPanel testEvaluationPanel;

	public TestPanel() {
		super();
		setTitle(_("TestPane.title"));
		
		initGUI();
	}
	
	private void initGUI() {
		// create the CellContraints
		cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "3dlu,fill:pref:grow,3dlu";
		String panelRows = "3dlu,pref,3dlu,fill:pref,3dlu";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		this.setLayout(panelLayout);

		testControllPanel = new TestControllPanel(this);
		testEvaluationPanel = new TestEvaluationPanel();
		
		this.add(testControllPanel, cc.xy(2, 2));
		this.add(testEvaluationPanel, cc.xy(2, 4));
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}

	public TestEvaluationPanel getEvaluationPanel() {
		return testEvaluationPanel;
	}
	
	public static void main(String[] args){
		JFrame jf = new JFrame();
		
		jf.add(new TestPanel());
		jf.setVisible(true);
	}

}
