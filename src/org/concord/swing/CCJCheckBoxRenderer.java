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

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public class CCJCheckBoxRenderer extends JCheckBox implements TreeCellRenderer
{
	transient protected Icon closedIcon;
	transient protected Icon openIcon;
	 
	public Component getTreeCellRendererComponent(JTree tree,
			Object value,
			boolean selected,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {
		DefaultMutableTreeNode temp = (DefaultMutableTreeNode) value;

		CCJCheckBoxTree.NodeHolder obj = (CCJCheckBoxTree.NodeHolder)temp.getUserObject();
		
		setBackground((selected)?
				UIManager.getColor("CheckBoxMenuItem.selectionBackground"):
					UIManager.getColor("Tree.textBackground"));
		
		setSelected(obj.checked);
		setText(obj.name);
		setForeground(obj.color);
		return this;
	}
	 
	public void setClosedIcon(Icon newIcon) {
		closedIcon = newIcon;
	}
	 
	public void setOpenIcon(Icon newIcon) {
		openIcon = newIcon;
	}
}
