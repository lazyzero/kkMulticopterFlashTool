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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Scanner;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import lu.tudor.santec.i18n.Translatrix;
import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;
import de.lazyzero.kkMulticopterFlashTool.gui.widgets.JNamedIntMenuItem;
import de.lazyzero.kkMulticopterFlashTool.utils.Icons;
import de.lazyzero.kkMulticopterFlashTool.utils.SendLogFile;
import de.lazyzero.kkMulticopterFlashTool.utils.XmlReaderFirmwares;

public class kkMenu extends JMenuBar {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JMenu setup;
	private JMenu info;
	private JMenu help;
	private JMenu contribute;
	private JMenu tools;
	private JMenuBar bar;
	
	private KKMulticopterFlashTool parent;
	protected XmlReaderFirmwares firmwareReader;
	private JNamedIntMenuItem countdown;
	private ArduinoUSBLinkerUploader arduinoUploader;

	public kkMenu(KKMulticopterFlashTool parent, XmlReaderFirmwares firmwareReader) {
		this.parent = parent;
		this.firmwareReader = firmwareReader;
		
		initSetupMenu();
		initHelpMenu();
		initInfoMenu();
		initContributeMenu();
		initToolsMenu();
				
		bar = new JMenuBar();
		bar.add(setup);
		bar.add(contribute);
		bar.add(tools);
		bar.add(help);
		bar.add(info);
		
	}

	public JMenuBar getBar() {
		return bar;
	}

	private void initToolsMenu() {
		tools = new JMenu(_("tools"));
//		tools.setMnemonic(_("contribute.key").charAt(0));
		
		
		JMenu arduinoUSBLinkerUploadItemMenu = new JMenu(_("tools.arduinoUSBLinkerUpload"));
		arduinoUSBLinkerUploadItemMenu.setIcon(Icons.getIcon22(Icons.CONFIG));
		tools.add(arduinoUSBLinkerUploadItemMenu);
		createArduinoUSBlinkerUploadMenuItems(arduinoUSBLinkerUploadItemMenu);
	}
	
	private void createArduinoUSBlinkerUploadMenuItems(JMenu arduinoUSBLinkerUploadItemMenu) {
		
		arduinoUploader = new ArduinoUSBLinkerUploader(parent);
		
		Iterator<String> arduinos = arduinoUploader.getArduinos();
		
		while (arduinos.hasNext()) {
			final String name = arduinos.next();
			
			JMenuItem mi = new JMenuItem(name);
			
			mi.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent e)
						{
							arduinoUploader.upload(name);
						}
					}
				);
			
			arduinoUSBLinkerUploadItemMenu.add(mi);
		}
		
	}
	
	private void initContributeMenu() {
		contribute = new JMenu(_("contribute"));
		//contribute.setMnemonic(_("contribute.key").charAt(0));
		
		JMenuItem mailItem = new JMenuItem(_("contribute.mail"));
		mailItem.setMnemonic(_("contribute.mail.key").charAt(0));
		mailItem.setIcon(Icons.getIcon22(Icons.MAIL));
		contribute.add(mailItem);
		
		JMenuItem submitFirmwareItem = new JMenuItem(_("contribute.submitFirmware"));
		submitFirmwareItem.setMnemonic(_("contribute.submitFirmware.key").charAt(0));
		submitFirmwareItem.setIcon(Icons.getIcon22(Icons.MAIL));
		contribute.add(submitFirmwareItem);
		
		submitFirmwareItem.addActionListener(
				new ActionListener(){
					

					public void actionPerformed(ActionEvent e) {
						FirmwareCommitForm fcf = new FirmwareCommitForm(kkMenu.this.firmwareReader);
						fcf.sendMail();
					}
				}
		);
		
		mailItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						SendLogFile slf = new SendLogFile();
						slf.sendMail(KKMulticopterFlashTool.VERSION, KKMulticopterFlashTool.LOG_FILE, parent.getText());
					}
				}
		);
	}

	private void initHelpMenu() {
		help = new JMenu(_("help"));
		//help.setMnemonic(_("help.key").charAt(0));
		
		JMenuItem escdbItem = new JMenuItem(_("help.ESCDB"));
		escdbItem.setMnemonic(_("help.ESCDB.key").charAt(0));
//		escdbItem.setIcon(Icons.getIcon22(Icons.app));
		help.add(escdbItem);
		
		JMenuItem ardLnkItem = new JMenuItem(_("help.ardLnk"));
		ardLnkItem.setMnemonic(_("help.ardLnk.key").charAt(0));
//		escdbItem.setIcon(Icons.getIcon22(Icons.app));
		help.add(ardLnkItem);
		
		help.add(new JSeparator());
		
		JMenuItem KKhomeItem = new JMenuItem(_("help.kkhome"));
		KKhomeItem.setMnemonic(_("help.kkhome.key").charAt(0));
		KKhomeItem.setIcon(Icons.getIcon22(Icons.app));
		help.add(KKhomeItem);
		
		JMenuItem KRManualItem = new JMenuItem(_("help.krManual"));
		KRManualItem.setMnemonic(_("help.krManual.key").charAt(0));
		KRManualItem.setIcon(Icons.getIcon22(Icons.KR));
		help.add(KRManualItem);
		
		help.addSeparator();
		
		JMenuItem helpItem = new JMenuItem(_("help.help"));
		helpItem.setMnemonic(_("help.help.key").charAt(0));
		helpItem.setIcon(Icons.getIcon22(Icons.LZ));
		help.add(helpItem);
		
		
		
		
		
		KRManualItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						openURL("http://www.kkmulticopter.kr/index.html?modea=manual");
					}
				}
		);
		
		KKhomeItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						openURL("http://www.kkmulticopter.com/index.php?option=com_content&view=article&id=55&Itemid=57");
					}
				}
		);
		
		helpItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						openURL("http://www.lazyzero.de/en/modellbau/kkmulticopterflashtool");
					}
				}
		);
		
		escdbItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						openURL("http://wiki.openpilot.org/display/Doc/RapidESC+Database");
					}
				}
		);
		
		ardLnkItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						openURL("https://github.com/c---/ArduinoUSBLinker#readme");
					}
				}
		);
		
	}
	
	private void initInfoMenu() {
		info = new JMenu(_("info"));
		//info.setMnemonic(_("info.key").charAt(1));
		
		JMenuItem licenseItem = new JMenuItem(_("info.license"));
		licenseItem.setMnemonic(_("info.license.key").charAt(0));
		licenseItem.setIcon(Icons.getIcon22(Icons.LICENSE));
		info.add(licenseItem);
		
		JMenuItem aboutItem = new JMenuItem(_("info.about"));
		aboutItem.setMnemonic(_("info.about.key").charAt(0));
		aboutItem.setIcon(Icons.getIcon22(Icons.ABOUT));
		info.add(aboutItem);
		
		licenseItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e)
					{
						JLabel title = new JLabel("<HTML><H2>KKmulticopter Flash Tool " + KKMulticopterFlashTool.VERSION +"</H2></HTML>");
						title.setHorizontalAlignment(JLabel.CENTER);
						String licenseText = null;
						
						try {
							File licenseFile = new File("license.txt");
							if (System.getProperty("os.name").toLowerCase().contains("mac")) {
								String path = System.getProperty("java.library.path");
					        	path = path.substring(0, path.indexOf(":"));
								licenseFile = new File(path,"license.txt");
							}
							FileInputStream licenseIS = new FileInputStream(licenseFile);
							
							StringBuilder text = new StringBuilder();
						    String NL = System.getProperty("line.separator");
						    Scanner scanner = new Scanner(licenseIS);

						    while (scanner.hasNextLine()) {
						    	text.append(scanner.nextLine() + NL);
						    }
						    licenseText = text.toString();
						    
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
						JTextArea license = new JTextArea(licenseText);
						JScrollPane main = new JScrollPane(license);
						
						JDialog about = new JDialog(parent);
						about.setIconImage(Icons.getIcon(Icons.app).getImage());
						about.add(title, BorderLayout.NORTH);
						about.add(main, BorderLayout.CENTER);
						about.setSize(new Dimension(540,400));
						about.setLocationRelativeTo(parent);
						about.setVisible(true);
					}
				}
		);
		
		aboutItem.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e)
					{
						JLabel icon = new JLabel(Icons.getIcon(Icons.app, 128));
						JLabel title = new JLabel("<HTML><H2>KKmulticopter Flash Tool " + KKMulticopterFlashTool.VERSION +"</H2></HTML>");
						title.setHorizontalAlignment(JLabel.CENTER);
						JLabel main = new JLabel(Translatrix.getTranslationString("about.message"));
						main.setHorizontalAlignment(JLabel.CENTER);
						
						JDialog about = new JDialog(parent);
						about.setIconImage(Icons.getIcon(Icons.app).getImage());
						about.add(title, BorderLayout.NORTH);
						about.add(icon, BorderLayout.WEST);
						about.add(main, BorderLayout.CENTER);
						about.setSize(new Dimension(480,240));
						about.setLocationRelativeTo(parent);
						about.setVisible(true);
					}
				}
			);
	}

	private void initSetupMenu() {
		setup = new JMenu(_("settings"));
//		setup.setMnemonic(_("settings.key").charAt(0));
		
		final JCheckBoxMenuItem offlineMenuItem = new JCheckBoxMenuItem(_("settings.offline"));
		offlineMenuItem.setSelected(KKMulticopterFlashTool.isOfflineMode());
		setup.add(offlineMenuItem);
		
		final JMenuItem offlineDownloadMenuItem = new JMenuItem(_("settings.downloadoffline"));
		setup.add(offlineDownloadMenuItem);
		
		setup.addSeparator();
		
		final JCheckBoxMenuItem enablePopupMenuItem = new JCheckBoxMenuItem(_("settings.enablepopup"));
		enablePopupMenuItem.setSelected(KKMulticopterFlashTool.isPopupsEnabled());
		setup.add(enablePopupMenuItem);
		
		countdown = new JNamedIntMenuItem(_("settings.countdown"), new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				KKMulticopterFlashTool.setCountdown(countdown.getValue());
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				KKMulticopterFlashTool.setCountdown(countdown.getValue());
			}

		});
		countdown.setValue(KKMulticopterFlashTool.getCountdown());
		setup.add(countdown);
		
		setup.addSeparator();
		
		JMenu langSubMenu = new JMenu(_("settings.language"));
		langSubMenu.setMnemonic(_("settings.language.key").charAt(0));
		langSubMenu.setIcon(Icons.getIcon22(Icons.LANGUAGE));
		setup.add(langSubMenu);
		
		createLanguageMenuItem("settings.language.english", "en", "US", Locale.US, Icons.ENGLISH, langSubMenu);
		createLanguageMenuItem("settings.language.french", "fr", "FR", Locale.FRANCE, Icons.FRENCH, langSubMenu);
		createLanguageMenuItem("settings.language.german", "de", "DE", Locale.GERMANY, Icons.GERMAN, langSubMenu);
		createLanguageMenuItem("settings.language.italian", "it", "IT", Locale.ITALY, Icons.ITALIAN, langSubMenu);
		createLanguageMenuItem("settings.language.dutch", "nl", "NL", null, Icons.DUTCH, langSubMenu);
		createLanguageMenuItem("settings.language.chinese", "zh", "CN", null, Icons.CHINESE, langSubMenu);
		createLanguageMenuItem("settings.language.turkish", "tr", "TR", null, Icons.TURKISH, langSubMenu);
		createLanguageMenuItem("settings.language.faroese", "fo", "FO", null, Icons.FAROESE, langSubMenu);
		createLanguageMenuItem("settings.language.danish", "da", "DK", null, Icons.DANISH, langSubMenu);
		createLanguageMenuItem("settings.language.swedish", "se", "SE", null, Icons.SWEDISH, langSubMenu);
		createLanguageMenuItem("settings.language.finnish", "fi", "FI", null, Icons.FINNISH, langSubMenu);
		createLanguageMenuItem("settings.language.brazilian", "pt", "BR", null, Icons.BRAZILIAN, langSubMenu);
		createLanguageMenuItem("settings.language.polish", "pl", "PL", null, Icons.POLISH, langSubMenu);
		createLanguageMenuItem("settings.language.spanish", "es", "ES", null, Icons.SPANISH, langSubMenu);
		createLanguageMenuItem("settings.language.russian", "ru", "RU", null, Icons.RUSSIAN, langSubMenu);
		createLanguageMenuItem("settings.language.portugese", "pt", "PT", null, Icons.PORTUGESE, langSubMenu);
		createLanguageMenuItem("settings.language.korean", "kr", "KR", null, Icons.KOREAN, langSubMenu);
		createLanguageMenuItem("settings.language.slovakian", "sk", "SK", null, Icons.SLOVAKIAN, langSubMenu);
		createLanguageMenuItem("settings.language.czech", "cz", "CZ", null, Icons.CZECH, langSubMenu);
		createLanguageMenuItem("settings.language.hebrew", "iw", "IL", null, Icons.HEBREW, langSubMenu);
		createLanguageMenuItem("settings.language.croatian", "hr", "HR", null, Icons.CROATIAN, langSubMenu);
		createLanguageMenuItem("settings.language.greek", "gr", "GR", null, Icons.GREEK, langSubMenu);
		createLanguageMenuItem("settings.language.romanian", "ro", "RO", null, Icons.ROMANIAN, langSubMenu);
		createLanguageMenuItem("settings.language.norwegian", "no", "NO", null, Icons.NORWEGIAN, langSubMenu);
		createLanguageMenuItem("settings.language.slovenian", "sl", "SI", null, Icons.SLOVENIAN, langSubMenu);
		createLanguageMenuItem("settings.language.japanese", "ja", "JP", null, Icons.JAPANESE, langSubMenu);
		createLanguageMenuItem("settings.language.hungarian", "hu", "HU", null, Icons.HUNGARIAN, langSubMenu);
		createLanguageMenuItem("settings.language.bosnian", "bs", "BA", null, Icons.BOSNIAN, langSubMenu);
		createLanguageMenuItem("settings.language.serbian", "cs", "SR", null, Icons.SERBIAN, langSubMenu);
		createLanguageMenuItem("settings.language.bulgarian", "bg", "BG", null, Icons.BULGARIAN, langSubMenu);
		
		offlineMenuItem.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (offlineMenuItem.isSelected()) {
							KKMulticopterFlashTool.setOfflineMode(true);
						} else {
							KKMulticopterFlashTool.setOfflineMode(false);
						}
						parent.firePropertyChange(KKMulticopterFlashTool.MODE_CHANGED, 0, 1);
					}
				}
			);
		
		enablePopupMenuItem.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (enablePopupMenuItem.isSelected()) {
							KKMulticopterFlashTool.setPopupEnabled(true);
						} else {
							KKMulticopterFlashTool.setPopupEnabled(false);
						}
					}
				}
			);
		
		offlineDownloadMenuItem.addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						FirmwareDownloadFrame fdf = new FirmwareDownloadFrame(parent.getFirmwares());
					}
				}
			);
		
	}
	
	private void createLanguageMenuItem(String menuItemString, final String isoLanguage, final String isoCountry, final Locale locale, String icon, JMenu langSubMenu) {
		String localisedString = _(menuItemString);
		String defaultString = Translatrix.getDefaultString(menuItemString);
		
		JMenuItem mi = new JMenuItem(localisedString.equals(defaultString)?localisedString:(localisedString + " (" + defaultString + ")"));
		
		mi.setMnemonic(_(menuItemString + ".key").charAt(0));
		mi.setIcon(Icons.getIconFlag(icon));
		
		mi.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e)
					{
						if (!parent.getLocale().equals(locale==null?new Locale(isoLanguage, isoCountry):locale)){
							parent.setLocale(isoLanguage, isoCountry);
							if (parent.restart()){
								System.exit(0);
							}
						}
					}
				}
			);
		
		langSubMenu.add(mi);
	}

	public static void openURL(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	

}
