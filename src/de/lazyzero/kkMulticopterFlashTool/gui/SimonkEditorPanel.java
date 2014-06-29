package de.lazyzero.kkMulticopterFlashTool.gui;

import static lu.tudor.santec.i18n.Translatrix._;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.GithubPanel;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.GithubPanelListener;
import de.lazyzero.kkMulticopterFlashTool.utils.avra.Avra;
import de.lazyzero.kkMulticopterFlashTool.utils.avra.SimonKAvraCommandLine;
import de.lazyzero.kkMulticopterFlashTool.utils.download.Download;

/**
 * A simple example showing how to use RSyntaxTextArea to add Java syntax
 * highlighting to a Swing application.
 * <p>
 * 
 * This example uses RSyntaxTextArea 2.0.1.
 * <p>
 * 
 * Project Home: http://fifesoft.com/rsyntaxtextarea<br>
 * Downloads: https://sourceforge.net/projects/rsyntaxtextarea
 */
public class SimonkEditorPanel extends JPanel implements ActionListener, GithubPanelListener, DocumentListener {

	private static final long serialVersionUID = 1L;
	KKMulticopterFlashTool parent;
	private RSyntaxTextArea tgyTextArea;
	private JButton compileButton;
	private JButton saveButton;
	private ButtonBarBuilder2 bar;
	private JButton saveCompileButton;
	private CellConstraints cc;
	private GithubPanel githubPanel;
	private JTabbedPane tabs;
	private RSyntaxTextArea incTextArea;
	private boolean tgyChanged;
	private boolean incChanged;

	public SimonkEditorPanel(KKMulticopterFlashTool parent) {
		this.parent = parent;
		
		//create the CellContraints
		cc  = new CellConstraints();
				
		// create the Layout for Panel this
		String panelColumns = "fill:pref:grow";
		String panelRows = "pref,3dlu,fill:pref:grow,3dlu,pref";
		FormLayout formLayout = new FormLayout(panelColumns, panelRows);
		
		this.setLayout(formLayout);

		githubPanel = new GithubPanel("sim-/tgy");
		githubPanel.addGithubPanelListener(this);
				
		tgyTextArea = new RSyntaxTextArea(20, 60);
		tgyTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
		tgyTextArea.setCodeFoldingEnabled(true);
		tgyTextArea.setAntiAliasingEnabled(true);
		RTextScrollPane tgyEditorPane = new RTextScrollPane(tgyTextArea);
		tgyEditorPane.setFoldIndicatorEnabled(true);
		tgyEditorPane.setLineNumbersEnabled(true);
		
		incTextArea = new RSyntaxTextArea(20, 60);
		incTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
		incTextArea.setCodeFoldingEnabled(true);
		incTextArea.setAntiAliasingEnabled(true);
		RTextScrollPane incEditorPane = new RTextScrollPane(incTextArea);
		incEditorPane.setFoldIndicatorEnabled(true);
		incEditorPane.setLineNumbersEnabled(true);
		
		tabs = new JTabbedPane();
		tabs.add("empty",tgyEditorPane);
		tabs.add("empty",incEditorPane);
		
		saveButton = new JButton(_("save"));
		saveButton.addActionListener(this);
		saveButton.setEnabled(false);
		
		compileButton = new JButton(_("compile"));
		compileButton.addActionListener(this);
		compileButton.setEnabled(false);
		
		saveCompileButton = new JButton(_("save") + " & " +_("compile"));
		saveCompileButton.addActionListener(this);
		saveCompileButton.setEnabled(false);
		
		bar = new ButtonBarBuilder2();
		bar.addButton(saveButton);
		bar.addButton(compileButton);
		bar.addGlue();
		bar.addUnrelatedGap();
		bar.addButton(saveCompileButton);
		
		this.add(githubPanel, cc.xy(1, 1));
		this.add(tabs, cc.xy(1, 3));
		this.add(bar.getPanel(), cc.xy(1, 5));

		try {
			Theme theme = Theme.load(SimonkEditorPanel.class.getResourceAsStream("/de/lazyzero/kkMulticopterFlashTool/gui/widgets/dark.xml"));
			theme.apply(tgyTextArea);
			theme.apply(incTextArea);
		} catch (IOException ioe) { // Never happens
			ioe.printStackTrace();
		}
	}
	
	private boolean setLinetoFirstof(RSyntaxTextArea area, String text) {
		SearchContext context = new SearchContext();
		if (text.length() == 0) {
			return false;
		}

		area.setCaretPosition(0);
		context.setSearchFor(text);
		context.setMatchCase(true);
		context.setRegularExpression(false);
		context.setSearchForward(true);
		context.setWholeWord(false);
		return SearchEngine.find(area, context).wasFound();
	}

	private String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}

		return stringBuilder.toString();
	}
	
	private boolean writeFile(RSyntaxTextArea rSysntaxTextArea, String filename) {
		byte dataToWrite[] = rSysntaxTextArea.getText().getBytes();
		FileOutputStream out;
		try {
			out = new FileOutputStream(filename);
			out.write(dataToWrite);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(saveButton)) {
			if (tgyChanged) {
				tgyChanged = !writeFile(tgyTextArea, githubPanel.getPath() + File.separatorChar + "tgy.asm"); 
				tabs.setTitleAt(0, "tgy.asm - " + githubPanel.getVersion());
			} 
			if (incChanged) {
				incChanged = !writeFile(incTextArea, githubPanel.getPath() + File.separatorChar + githubPanel.getTarget() + ".inc");
				tabs.setTitleAt(1, githubPanel.getTarget() + ".inc - " + githubPanel.getVersion());
			}
			enableSave();
		} else if (e.getSource().equals(compileButton)) {
			parent.clearText();
			parent.println(_("compile.simonk.start"));
			
			File sources = new File(githubPanel.getPath());
			Avra avra = new Avra(this.parent, new SimonKAvraCommandLine(githubPanel.getTarget(), sources));
			if(avra.compile()) {
//				parent.clearText();
				parent.println(_("compile.simonk.success"));
				parent.setHexFile(avra.getHexFile(), false);
				avra.clean();
			} else {
				parent.err("Compiling failed.");
				parent.switch2ProgrammingTab();
			}
		} else if (e.getSource().equals(saveCompileButton)) {
			saveButton.doClick();
			compileButton.doClick();
		}
	}

	@Override
	public void githubPanelChanged(int state) {
		System.out.println("downloadStateChanged: " + state);
		if (state == Download.COMPLETE) {
			try {
				System.out.println("Open File from: " + githubPanel.getPath());
				tgyTextArea.setText(readFile(githubPanel.getPath() + File.separatorChar + "tgy.asm"));
				setLinetoFirstof(tgyTextArea, ".equ");
				tabs.setTitleAt(0, "tgy.asm - " + githubPanel.getVersion());
				tgyTextArea.getDocument().addDocumentListener(this);
				tgyChanged = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (state == Download.DOWNLOADING) {
			tgyTextArea.setText("");
			tgyTextArea.revalidate();
			tgyTextArea.repaint();
			incTextArea.setText("");
			incTextArea.revalidate();
			incTextArea.repaint();
			tabs.setTitleAt(0, _("empty"));
			tabs.setTitleAt(1, _("empty"));
			
			tgyTextArea.getDocument().removeDocumentListener(this);
			incTextArea.getDocument().removeDocumentListener(this);
			
			tgyChanged = false;
			incChanged = false;
		} else if (state == GithubPanel.TARGET_CHANGED) {
			try {
				incTextArea.setText(readFile(githubPanel.getPath() + File.separatorChar + githubPanel.getTarget() + ".inc"));
				setLinetoFirstof(incTextArea, ".equ");
				tabs.setTitleAt(1, githubPanel.getTarget() + ".inc - " + githubPanel.getVersion());
				incTextArea.getDocument().addDocumentListener(this);
				incChanged = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		enableSave();
	}

	private void enableSave() {
		saveButton.setEnabled(incChanged || tgyChanged);
		saveCompileButton.setEnabled(incChanged || tgyChanged);
		enableCompile();
	}
	
	private void enableCompile() {
		compileButton.setEnabled(!incChanged && !tgyChanged);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		if (e.getDocument().equals(tgyTextArea.getDocument())) {
			tabs.setTitleAt(0, "*tgy.asm - " + githubPanel.getVersion());
			tgyChanged = true;
		} else if (e.getDocument().equals(incTextArea.getDocument())) {
			tabs.setTitleAt(1, "*" + githubPanel.getTarget() + ".inc - " + githubPanel.getVersion());
			incChanged = true;
		}
		System.out.println("changedUpdate: " + e.getLength());
		enableSave();
	}

}
