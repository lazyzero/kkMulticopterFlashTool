package de.lazyzero.kkMulticopterFlashTool.gui.widgets;

import static lu.tudor.santec.i18n.Translatrix._;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.io.FileExistsException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.utils.Zip;
import de.lazyzero.kkMulticopterFlashTool.utils.download.Download;

public class GithubPanel extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;
	public static final int TARGET_CHANGED = 9999;
	private String repositoryName;
	private GitHub github;
	private GHRepository repository;
	private PagedIterable<GHTag> tags;
	private Vector<String> versions = new Vector<String>();
	private Vector<String> targets = new Vector<String>();
	private JComboBox<String> versionsCombobox;
	private JComboBox<String> targetsCombobox;
	private JButton downloadVersion;
	private Vector<GithubPanelListener> githubPanelListeners = new Vector<GithubPanelListener>();

	public GithubPanel(String repositoryName) {
		this.repositoryName = repositoryName;
		
		try {
			github = GitHub.connectAnonymously();
			repository = github.getRepository(this.repositoryName);
			loadVersions();
						
//			downloadArchive(repository.getMasterBranch());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		initGUI();
	}
	
	private void loadVersions() throws IOException {
		versions.removeAllElements();
		versions.add(repository.getMasterBranch());
		
		tags = repository.listTags();
		
		for (GHTag tag : tags) {
			System.out.println(tag.getName() + "/" + tag.getOwner() + "/" + tag.getCommit().getSHA1());
			System.out.println(repository.getUrl()+"/archive/"+tag.getName()+".zip");
			versions.add(tag.getName());
		}
	}

	private void initGUI() {
		//create the CellContraints
		CellConstraints cc = new CellConstraints();
				
		// create the Layout for Panel this
		String panelColumns = "pref,3dlu,fill:pref:grow, 3dlu,pref";
		String panelRows = "pref,3dlu,pref,3dlu,pref";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		
		this.setLayout(panelLayout);
		
		//row1
		JLabel repositoryNameLabel = new JLabel(_("github.repository.intro") + " " + this.repositoryName);
		//row3
		JLabel versionLabel = new JLabel(_("github.selectversion"));
		versionsCombobox = new JComboBox<String>(versions);
		downloadVersion = new JButton(_("downloads.download"));
		downloadVersion.addActionListener(this);
		//row5
		targetsCombobox = new JComboBox<String>(targets);
		targetsCombobox.addActionListener(this);
		
		this.add(repositoryNameLabel, cc.xyw(1,1,5));
		this.add(versionLabel, cc.xy(1, 3));
		this.add(versionsCombobox, cc.xy(3, 3));
		this.add(downloadVersion, cc.xy(5, 3));
		this.add(targetsCombobox, cc.xy(3, 5));
	}

	private void downloadArchive(String version) throws MalformedURLException, FileExistsException {
		URL zipURL = new URL(repository.getUrl()+"/archive/"+version+".zip");
		Download dl;
		int counter = 0;
		do {
			dl = new Download(zipURL, version);
			while(dl.getStatus() == Download.DOWNLOADING) {
				notifyGithubPanelListener(Download.DOWNLOADING);
				System.out.println("downloading :" + dl.getProgress());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			counter++;
		} while (dl.getStatus() == Download.ERROR && counter  < 3);
		if (dl.getStatus() == Download.COMPLETE) {
			String tmpFolder = KKMulticopterFlashTool.getTempFolder();
			File zipFile = new File(tmpFolder, version+".zip" );
			File toFolder = new File(tmpFolder, "github");
			Zip.unzip2folder(zipFile, toFolder);
			updateTargets();
			notifyGithubPanelListener(Download.COMPLETE);
		} 
	}

	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(downloadVersion)) {
			System.out.println("download: " + (String)versionsCombobox.getSelectedItem());
			try {
				downloadArchive((String)versionsCombobox.getSelectedItem());
			} catch (FileExistsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getSource().equals(targetsCombobox)) {
			notifyGithubPanelListener(TARGET_CHANGED);
		}
	}
	
	private void updateTargets() {
		File f = new File(getPath());
		String[] fileList = f.list(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".inc");
		    }
		});
		
		targets.removeAllElements();
		for (String name : fileList) {
			if (!name.startsWith("boot")) {
				targets.add(name.split(".inc")[0]);
			}
		}
		
		Collections.sort(targets, new Comparator<String>() {
			   public int compare(String s1, String s2){
			      return s1.compareTo(s2);
			   }
			});
		
		targetsCombobox.setSelectedIndex(0);
	}
	
	/**
	 * @param state could be constant of Download or GithubPanel
	 * 
	 */
	private void notifyGithubPanelListener(int state) {
		System.out.println("notifyGithubPanelListener: " + state);
		for (GithubPanelListener githubPanelListener : githubPanelListeners) {
			githubPanelListener.githubPanelChanged(state);
		}
	}

	public void addGithubPanelListener(GithubPanelListener githubPanelListener) {
		githubPanelListeners.add(githubPanelListener);
	}

	public String getPath() {
		return KKMulticopterFlashTool.getTempFolder() + "github"  + File.separatorChar + "tgy-" + (String)versionsCombobox.getSelectedItem();
	}
	
	public String getVersion() {
		return (String)versionsCombobox.getSelectedItem();
	}

	public String getTarget() {
		return (String)targetsCombobox.getSelectedItem();
	}
}
