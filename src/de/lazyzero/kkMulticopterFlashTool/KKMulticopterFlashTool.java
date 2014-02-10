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
package de.lazyzero.kkMulticopterFlashTool;

import static lu.tudor.santec.i18n.Translatrix._;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.SplashScreen;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lu.tudor.santec.i18n.Translatrix;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import avr8_burn_o_mat.AVR;
import avr8_burn_o_mat.AvrdudeConfigFile;
import avr8_burn_o_mat.AvrdudeControl;
import avr8_burn_o_mat.AvrdudeControl.FileFormat;
import avr8_burn_o_mat.AvrdudeProgrammer;
import avr8_burn_o_mat.InterfaceTextOutput;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.lazyzero.kkMulticopterFlashTool.gui.ColorTweaks;
import de.lazyzero.kkMulticopterFlashTool.gui.ControllerPanel;
import de.lazyzero.kkMulticopterFlashTool.gui.EEpromResetPanel;
import de.lazyzero.kkMulticopterFlashTool.gui.EEpromSettingsPanel;
import de.lazyzero.kkMulticopterFlashTool.gui.FirmwarePanel;
import de.lazyzero.kkMulticopterFlashTool.gui.ProgrammerPanel;
import de.lazyzero.kkMulticopterFlashTool.gui.kkMenu;
import de.lazyzero.kkMulticopterFlashTool.utils.ArduinoUpload;
import de.lazyzero.kkMulticopterFlashTool.utils.ButtonsStateListener;
import de.lazyzero.kkMulticopterFlashTool.utils.FileCorruptException;
import de.lazyzero.kkMulticopterFlashTool.utils.Firmware;
import de.lazyzero.kkMulticopterFlashTool.utils.Icons;
import de.lazyzero.kkMulticopterFlashTool.utils.XmlReaderFirmwares;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEprom;
import de.lazyzero.kkMulticopterFlashTool.utils.EEprom.EEpromListener;

/**
 * @author Christian Moll
 * 
 */
public class KKMulticopterFlashTool extends JFrame implements
		InterfaceTextOutput, PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String VERSION = "0.77";
	private static boolean isBeta = true;
	private static int betaVersion = 7;
	public static final String MODE_CHANGED = "changed";
	public static final String KKPLUSBOOT = "kkplusboot";
	public static final String FLYCAM_BLACKBOARD = "flycam_black";
	public static final String FLYCAM_BLACKBOARD_P = "flycam_blackp";
	public static final String SMARTLCD = "smartlcd";
	public static final String ESC = "esc";
	public static final String ESCBOOTLOADER = "escp";
	public static final String WIIESC = "wii_bl";
	public static final String WIIESC_EEPROM = "wii_eeprom";
	public static final Object WIIESC_BOOTLOADER = "wii_boot";
	public static final String ESC_LIGHT = "esc_light";
	public static final String OPENFLIGHT_V1 = "openflightV1";
	public static final String OPENFLIGHT_V2 = "openflightV2";
	public static final String OPENFLIGHT_V2_SM = "openflightSM";
	public static final String OPENFLIGHTPROG = "openflightPROG";
	public static final String i86 = "i86";
	public static final String i86l = "i86l";
	private static KKMulticopterFlashTool kk;

	private Locale						locale;
	private JPanel						mainPanel;
	private Properties					settings		 = new Properties();
	public static File SETTINGS_FILE	= new File(System.getProperty("user.dir"), "kkMulticopterFlashTool.properties");
	public static File LOG_FILE	= new File(System.getProperty("user.dir"), "kkLogging.txt");
	
	private JFrame						mainframe;
	private Dimension preferredSize	= new Dimension(830, 650);
	private AvrdudeConfigFile			 avrConfig;
	private AvrdudeProgrammer			 programmer;
	private AvrdudeControl				control;
	private boolean forceFlashing = false;

	private Vector<AvrdudeProgrammer>	 programmers;
	private static Vector<AVR>				   avrs			 = new Vector<AVR>();
	private AVR						   controller;

	private JTextArea					 output;
	private Color defaultFG;
	private JScrollPane				   outputPane;

	private ProgrammerPanel			   programmerPanel;
	private ControllerPanel			   controllerPanel;
	private FirmwarePanel				 firmwarePanel;
	private LinkedHashMap<String,String> firmwareRepositoryURL = new LinkedHashMap<String,String>();
	private Firmware firmware;
	private XmlReaderFirmwares firmwareReader;
	private boolean offlineMode;
	private String mode;
	
	private FileHandler logFile;
	private Logger logger = Logger.getLogger(KKMulticopterFlashTool.class.getName());
	private boolean enableTweak = true;
	public static boolean ENABLE_PORT_CHECK = true;
	private static boolean isPopupsEnabled;
	private static int countdown;
	private static boolean isHideDeprecated;
	private static boolean isShowDailyTGYEnabled;
	private JTabbedPane tabbedPane = new JTabbedPane();
	//private TestPanel testPanel;
	private JPanel programmingPanel;
	private JPanel eepromPanel;
	private Component eepromResetPanel;
	private EEpromSettingsPanel eepromSettingsPanel;
	protected boolean successful;
	private Vector<ButtonsStateListener> listeners = new Vector<ButtonsStateListener>();

	public KKMulticopterFlashTool(String[] args) {

		kk = this;
		evaluateCommandlineOptions(args);
		
		//check if the OS is Mac OS X
		if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			SETTINGS_FILE = new File(System.getProperty("user.home")+"/Library/Preferences/","kkMulticopterFlashTool.properties");
			LOG_FILE = new File(getTempFolder(),"kkLogging.txt");
		}
		
		initLogger();
		
		SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash==null) {
			logger.log(Level.INFO, "Splash not loaded.");
		}
		
		
		logger.log(Level.INFO, System.getProperty("user.home"));
		logger.log(Level.INFO, System.getProperty("os.name"));
		

		loadSettings();
		loadTranslation();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				saveSettings();
			}
		});
		
		firmwareReader = new XmlReaderFirmwares(firmwareRepositoryURL);

		this.init();
		if (enableTweak) ColorTweaks.tweakColors();
		this.initGUI();
		
		this.addPropertyChangeListener(this);
		
		checkVersion();
	}

	
	private void evaluateCommandlineOptions(String[] args) {
		
	        Options options = new Options();
	        
	        options.addOption("h", "help", false, "Show this help message.");
	        options.addOption("c", "color", false, "Use default colors of the Java VM.");
	        options.addOption("p", "portcheck", false, "Disable checking for port checking.");
	     
	        try {
	            CommandLineParser parser = new PosixParser();
	            CommandLine cmd = parser.parse(options, args);
	            
	            if (cmd.hasOption("h")) {
	                HelpFormatter formatter = new HelpFormatter();
	                formatter.printHelp("KKmulticopter Flash Tool Version " + VERSION, options, true);
	                System.exit(0);
	            }
	           
	            if (cmd.hasOption("c")) {
	            	enableTweak = false;
	            }
	            
	            if (cmd.hasOption("p")) {
	            	ENABLE_PORT_CHECK = false;
	            }
	            
	            	
	        } catch (ParseException exp) {
	           
	        }
	}


	private void initLogger() {
		try {
			logFile = new FileHandler(LOG_FILE.getAbsolutePath(), false);
			logFile.setFormatter(new SimpleFormatter());
			logFile.setLevel(Level.INFO);
			logger.addHandler(logFile);
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.setLevel(Level.INFO);
	}


	
	
		
	private void checkVersion() {
		logger.log(Level.INFO, "Check the version: " + VERSION + (isBeta?" beta "+betaVersion:"") + " online is version: " + firmwareReader.getActualVersion());
		if (firmwareReader.getActualVersion()>Double.parseDouble(VERSION)) {
//			JOptionPane.showMessageDialog(this, _("update"));
			String[] choices = {_("downloads.download"), _("Cancel")};
			int result = JOptionPane.showOptionDialog(this, _("update"), "",
				      JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, 
				      null, choices, choices[0]);
			if (result == 0) {
				kkMenu.openURL("http://kkflashtool.de");
				System.exit(0);
			}
		}
	}


	public void flashAVR() {
		boolean abort = check4USBlinker();
		if (!abort) {
			new Thread(){
				public void run(){
					
					boolean notFailed = true;
					boolean isUSBtiny = programmer.getType().equals("usbtiny")?true:false;
					firmwarePanel.setButtonsEnabled(false);
					updateButtons();
					
					
					try {
						control = new AvrdudeControl(KKMulticopterFlashTool.this, isUSBtiny);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						logger.log(Level.WARNING, e1.getMessage());
					}
					control.setPort(programmerPanel.getPort());
					control.setProgrammer(programmer);
					if (programmerPanel.useBaudRate()) {
						control.addAdditionalOption("-b " + programmerPanel.getRate());
					}
					logger.log(Level.INFO, programmer.getType());
//					if (!programmer.getType().equals("stk500v2")){
//					if (!programmer.getType().equals("usbtiny")){
//						control.addAdditionalOption("-B 8");
//						logger.log(Level.INFO, "set additional Option -B 8");
//					}
//					}
					//if (controller.getCaption().equals("m48pa")||controller.getCaption().equals("m168p")||controller.getCaption().equals("m328p")){
					control.addAdditionalOption("-e");
					logger.log(Level.INFO, "set additional Option -e");
//					}
					
					if (forceFlashing) {
						control.addAdditionalOption("-F");
					}
					control.setDisableFuseCheck(false);
					
					//import additionalOptions from firmware
					control.addAndUpdateAdditionalOptions(firmware.getAdditionalOptions());
					//for BL-ESC the fuses are null and the next step is skipped.
					if (controller.getLfuse() != null && controller.getHfuse() != null) {
						//write the fuses.
						try {
							logger.log(Level.INFO, "write fuses: ");
							control.writeFuses(controller);
						} catch (Exception e) {
							notFailed = false; 
							e.printStackTrace();
							logger.log(Level.WARNING, "error.writefuses");
							err(_("error.writefuses"));
						}
						
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							logger.log(Level.WARNING, e1.toString());
						}
					}
					
					//TODO add code to manipulate fuses
					//special code for enabling bootloader on simonk ESC
					if (controller.getCaption().equals(ESCBOOTLOADER) && notFailed) {
						logger.log(Level.INFO, "enable bootloader on simonk ESC: " + controller.getBootloaderAddress());
						
						try {
							control.readFuses(controller, getTempFolder());
							logger.log(Level.INFO, "hfuse: " + Integer.toHexString(Integer.parseInt(controller.getHfuse())) + " lfuse: " + Integer.toHexString(Integer.parseInt(controller.getLfuse())));
							if (controller.getBootloaderAddress()==512){
								controller.changeFuseNibble(AVR.HFUSE, AVR.LOWER_NIBBLE, "a");
								logger.log(Level.INFO, "fuses changed to: hfuse: " + Integer.toHexString(Integer.parseInt(controller.getHfuse())) + " lfuse: " + Integer.toHexString(Integer.parseInt(controller.getLfuse())));
							}
							
							//added a delay to test if avrispmkii will then work
							try {
								Thread.sleep(1500);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								logger.log(Level.WARNING, e1.toString());
							}
							
							logger.log(Level.INFO, "write new fuses: ");
							control.writeFuses(controller);
							
						} catch (Exception e) {
							notFailed = false;
							err(_("error.readhfuse"));
							logger.log(Level.WARNING, "error.replacefuse");
							e.printStackTrace();
						}
						
						//clean up
						controller.setHfuse(null);
						controller.setLfuse(null);
						
						File fuseFile = new File(getTempFolder() + "/hfuse.hex");
						boolean isDeleted = false;
						if(fuseFile.exists() && fuseFile.isFile()){
							isDeleted = fuseFile.delete();
							logger.log(Level.INFO, "hfuse.hex deleted: " + isDeleted);
						}
						
						fuseFile = new File(getTempFolder() + "/lfuse.hex");
						if(fuseFile.exists() && fuseFile.isFile()){
							isDeleted = fuseFile.delete();
							logger.log(Level.INFO, "lfuse.hex deleted: " + isDeleted);
						}
						
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							logger.log(Level.WARNING, e1.toString());
						}
					}
					
					try {
						if (notFailed) {
							logger.log(Level.INFO, "flash firmware");
							//TODO add flashing of eeprom here.
							try {	
								File file = firmware.getFile();
								println(_("flash.file") + ": "
										+ file.getAbsolutePath());
								control.writeFlash(controller, file.getAbsolutePath(), FileFormat.INTEL_HEX);
								if (isPopupsEnabled) {
									JOptionPane.showMessageDialog(kk, _("flash.successfull"), _("info"), JOptionPane.INFORMATION_MESSAGE);
								}
								println(_("flash.successfull"));
							} catch (FileCorruptException e) {
								err(_("flash.abort") + " " +_("flash.filecorrupt"));
							}
						} else {
							err(_("flash.abort"));
							logger.log(Level.WARNING, "flash.abort");
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						err(_("error.writeflash"));
						logger.log(Level.WARNING, "error.writeflash");
					}
					firmwarePanel.setButtonsEnabled(true);
					updateButtons();
				}
			}.start();
			
		} else {
			err(_("flash.abort"));
		}
	}
	
	public void flashEEprom(final EEpromListener eepromListener) {
		logger.info("WRITE the EEprom!!!");
		clearText();
		boolean abort = check4USBlinker();
		if (!abort) {
			new Thread(){
				public void run(){
					boolean isUSBtiny = programmer.getType().equals("usbtiny")?true:false;
					
					try {
						control = new AvrdudeControl(KKMulticopterFlashTool.this, isUSBtiny);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						logger.log(Level.WARNING, e1.getMessage());
					}
					control.setPort(programmerPanel.getPort());
					control.setProgrammer(programmer);
					if (programmerPanel.useBaudRate()) {
						control.addAdditionalOption("-b " + programmerPanel.getRate());
					}
					logger.log(Level.INFO, programmer.getType());
//					if (!programmer.getType().equals("usbtiny")){
//						control.addAdditionalOption("-B 8");
//						logger.log(Level.INFO, "set additional Option -B 8");
//					}
					if (forceFlashing) {
						control.addAdditionalOption("-F");
					}
					control.setDisableFuseCheck(false);
					
					logger.log(Level.INFO, "flash eeprom");
					try {
						if (controller.getCaption().equals(WIIESC_EEPROM)) {
							File file = firmware.getFile();
							println(_("flash.eeprom") + ": "
									+ file.getAbsolutePath());
							control.writeEEPROM(controller,  file.getAbsolutePath(), FileFormat.AUTO_DETECT);
						} else {
							println(_("flash.eeprom") + ": "
									+ EEprom.getDefaultOutputFile());
							control.writeEEPROM(controller,  EEprom.getDefaultOutputFile(), FileFormat.RAW);
							eepromListener.EEpromState(EEpromListener.WROTE);
						}
					} catch (FileCorruptException e) {
						err(_("flash.abort") + " " +_("flash.filecorrupt"));
						eepromListener.EEpromState(EEpromListener.FILE_CORRUPT);
					} catch (Exception e) {
						if (eepromListener != null) {
							eepromListener.EEpromState(EEpromListener.FLASH_FAILED);
						}
						err(_("flash.abort"));
						logger.warning(e.getMessage());
//					e.printStackTrace();
					}
				}
			}.start();
		} else {
			err(_("flash.abort"));
		}
		
	}
	
	public void readEEprom(final EEpromListener eepromListener) {
		logger.info("READ the EEprom!!!");
		clearText();
		boolean abort = check4USBlinker();
		if (!abort) {
			new Thread(){
				public void run(){
					boolean isUSBtiny = programmer.getType().equals("usbtiny")?true:false;
					
					try {
						control = new AvrdudeControl(KKMulticopterFlashTool.this, isUSBtiny);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						logger.log(Level.WARNING, e1.getMessage());
					}
					control.setPort(programmerPanel.getPort());
					control.setProgrammer(programmer);
					if (programmerPanel.useBaudRate()) {
						control.addAdditionalOption("-b " + programmerPanel.getRate());
					}
					logger.log(Level.INFO, programmer.getType());
					if (!programmer.getType().equals("usbtiny")){
						control.addAdditionalOption("-B 8");
						logger.log(Level.INFO, "set additional Option -B 8");
					}
					if (forceFlashing) {
						control.addAdditionalOption("-F");
					}
					control.setDisableFuseCheck(false);
					
					logger.log(Level.INFO, "flash eeprom");
					try {		
						println(_("read.eeprom") + ": "
								+ EEprom.getDefaultInputFile());
						control.readEEPROM(controller,  EEprom.getDefaultInputFile(), FileFormat.RAW);
						eepromListener.EEpromState(EEpromListener.READ);
					} catch (Exception e) {
						eepromListener.EEpromState(EEpromListener.FAILED);
						err(_("read.abort"));
						logger.warning(e.getMessage());
	//					e.printStackTrace();
					}
				}
			}.start(); 
		} else {
			eepromListener.EEpromState(EEpromListener.FAILED);
			err(_("read.abort"));
		}
		
	}
	

	private boolean check4USBlinker() {
		if (programmer.getId().equals("tgyusblinker") || programmer.getId().equals("arduinousblinker") || programmer.getId().equals("afrousb")) {
			if (programmerPanel.getPort().equals("usb")) {
				err(_("flash.LinkerWrongPort"));
				return true;
			}
		}
		return false;
	}


	public void loadSettings() {
		try {
			settings.load(new FileInputStream(SETTINGS_FILE));
			
			firmwareRepositoryURL.put("firmwareRepositoryURL", settings.getProperty("firmwareRepositoryURL", "http://www.lazyzero.de/_media/firmwares.xml.zip"));
			//check for depricated settings!!!
			if (firmwareRepositoryURL.containsValue("http://www.lazyzero.de/_media/firmwares.xml")) {
				firmwareRepositoryURL.put("firmwareRepositoryURL", "http://www.lazyzero.de/_media/firmwares.xml.zip");
			}
			firmwareRepositoryURL.put("tgydaily",settings.getProperty("tgydaily", "http://www.lazyzero.de/_media/tgy_daily.xml.zip"));
			
			offlineMode = new Boolean(settings.getProperty("offlineMode","false"));
			isPopupsEnabled = new Boolean(settings.getProperty("isPopupEnabled","true"));
			isHideDeprecated = new Boolean(settings.getProperty("isHideDeprecated","false"));
			isShowDailyTGYEnabled = new Boolean(settings.getProperty("isShowDailyTGYEnabled", "false"));
			countdown = Integer.parseInt(settings.getProperty("countdown", "0"));
			
			if (isOfflineMode()) {
				mode = _("offline");
			} else {
				mode = _("online");
			}
			
			
			try {
				this.setLocale((String) settings.getProperty("locale.language",
						"en"), (String) settings.getProperty("locale.country",
						"US"));
			} catch (Exception e) {
			}

		} catch (Exception e) {
			
			e.printStackTrace();
			firmwareRepositoryURL.put("firmwareRepositoryURL", "http://www.lazyzero.de/_media/firmwares.xml.zip");
			offlineMode = false;
			
			saveSettings();
			e.printStackTrace();
		}
	}

	private void saveSettings() {
		try {
			settings.put("locale.language", locale.getLanguage());
			settings.put("locale.country", locale.getCountry());

			settings.put("offlineMode", offlineMode+"");
			settings.put("isPopupEnabled", isPopupsEnabled+"");
			settings.put("isHideDeprecated", isHideDeprecated+"");
			settings.put("isShowDailyTGYEnabled", isShowDailyTGYEnabled+"");
			
			settings.put("countdown", countdown+"");
			
			settings.put("programmer", programmer.getId());
			settings.put("port", programmerPanel.getPort());
			settings.put("rate", programmerPanel.getRate());
			settings.put("defaultRate", programmerPanel.isDefaultRate()+"");
			settings.put("controller", controller.getName());

			Iterator<String> keys = firmwareRepositoryURL.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				settings.put(key, firmwareRepositoryURL.get(key));
			}
			
			try {
				settings.put("last.dir", firmware.getFile().getPath());
			} catch (Exception e) {
				// TODO: handle exception
			}

			settings.store(new FileOutputStream(SETTINGS_FILE), "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() {
		avrConfig = new AvrdudeConfigFile();
		try {
			avrConfig.readAvrdudeConfigFile(AvrdudeConfigFile.getConfigFileByOS());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, _("error.avrdudeconfig"));
			e.printStackTrace();
		}
		programmers = avrConfig.getProgrammerList();

		avrs.add(new AVR("HobbyKing KK2.1", "64kB flash", "m644p", 2048, null, null));
		
		avrs.add(new AVR("HobbyKing KK2.0", "32kB flash", "m324pa", 1024, null, null));
		avrs.add(new AVR("HobbyKing KK2.0 (fuse restore)", "32kB flash", "m324pa", 1024, "0xf7", "0xd7", "0xfc"));
		avrs.add(new AVR("HobbyKing KK2.0 (brown out of 2.7V)", "32kB flash", "m324pa", 1024, "0xf7", "0xd7", "0xfd"));
		avrs.add(new AVR("HobbyKing KK2.0 (ATmega1284P)", "128kB flash", "m1284p", 4096, "0xd7", "0xd1", "0xfc"));//(Low=D7, High=D1, Ext=FC)
		
		avrs.add(new AVR("atmega 8-based brushless ESC", "8kB flash", ESC, 512, null, null));
		avrs.add(new AVR("atmega 8-based brushless ESC + enable Bootloader", "8kB flash", ESCBOOTLOADER, 512, null, null, 512));
		avrs.add(new AVR("atmega 8-based brushless ESC (fuse restore, external clock", "8kB flash", ESCBOOTLOADER, 512, "0x3f", "0xca"));
		avrs.add(new AVR("atmega 8-based brushless ESC (fuse restore, internal clock", "8kB flash", ESCBOOTLOADER, 512, "0x24", "0xda"));
		avrs.add(new AVR("WiiESC", "8kB flash", WIIESC, 512, null, null));
		avrs.add(new AVR("WiiESC Settings", "8kB flash", WIIESC_EEPROM, 512, null, null));
		avrs.add(new AVR("EscLight", "8kB flash", ESC_LIGHT, 512, null, null));
		
		avrs.add(new AVR("Orange RX3S V2/V3", "16kB flash", OPENFLIGHT_V2, 512, null, null));
		avrs.add(new AVR("Orange RX3SM", "16kB flash", OPENFLIGHT_V2_SM, 512, null, null));
		avrs.add(new AVR("Orange RX3S V1", "16kB flash", OPENFLIGHT_V1, 512, null, null));
		avrs.add(new AVR("OpenFlight Programming Box", "8kB flash", OPENFLIGHTPROG, 512, null, null));
		
		avrs.add(new AVR("KK Blackboard 168", "16kB flash", "m168", 512,"0xe2", "0xdd")); //Niall efuse:FF lfuse:E2 hfuse:DE
		avrs.add(new AVR("KK Blackboard 168P/PA", "16kB flash", "m168p", 512, "0xe2", "0xdd"));
		avrs.add(new AVR("KK Blackboard 328P", "32kB flash", "m328p", 1024, "0xe2", "0xd9"));
		avrs.add(new AVR("KK Plus 5.5d/e", "16kB flash", "m168p", 512, "0xe2", "0xde"));
//		avrs.add(new AVR("KK Plus 5.5d/e Bootloader restore", "16kB flash", KKPLUSBOOT, "0xe2", "0xde"));//Extended : 0xFF High: 0xDE Low: 0xE2
		avrs.add(new AVR("HobbyKing Quadcopter Control Board V3", "32kB flash", "m328p", 1024, "0xe2", "0xd9"));
		avrs.add(new AVR("HobbyKing Quadcopter Control Board V2/V2.1", "16kB flash", "m168p", 512, "0xe2", "0xdd"));
		avrs.add(new AVR("HobbyKing Quadcopter Control Board V1", "4kB flash", "m48p", 256, "0xe2", "0xdd"));
		avrs.add(new AVR("HobbyKing Hobbyking i86 or Eagle N6 V1", "16kB flash", i86, 512, "0xe2", "0xdd"));	
		avrs.add(new AVR("HobbyKing Hobbyking i86L or Eagle N6 V2", "16kB flash", i86l, 512, null, null));		
		avrs.add(new AVR("Flycam Blackboard", "16kB flash", FLYCAM_BLACKBOARD, 512, "0xe2", "0xdd"));//Extended : 0xFF High: 0xDE Low: 0xE2
		avrs.add(new AVR("Flycam Blackboard (Goodluckbuy version)", "16kB flash", FLYCAM_BLACKBOARD_P, 512, "0xe2", "0xdd"));//Extended : 0xFF High: 0xDE Low: 0xE2
		avrs.add(new AVR("Korean Blueboard", "16kB flash", "m168", 512, "0xe2", "0xdd"));
		avrs.add(new AVR("Korean Redboard (without chip update)", "4kB flash", "m48", 256, "0xe2", "0xdd"));
		
		avrs.add(new AVR("SmartLCD Pro", "4kB flash", SMARTLCD, 256, null, null));
		
//		avrs.add(new AVR("atmega 48", "4kB flash", "m48", 256, "0xe2", "0xdd"));
//		avrs.add(new AVR("atmega 48PA", "4kB flash", "m48p", 256, "0xe2", "0xdd"));
//		avrs.add(new AVR("atmega 88", "8kB flash", "m88", 512, "0xe2", "0xdd"));
//		avrs.add(new AVR("atmega 88P/PA", "8kB flash", "m88p", 512, "0xe2", "0xdd"));
//		avrs.add(new AVR("atmega 168", "16kB flash", "m168", 512, "0xe2", "0xdd"));
//		avrs.add(new AVR("atmega 168P/PA", "16kB flash", "m168p", 512, "0xe2", "0xdd"));
//		avrs.add(new AVR("atmega 328P", "32kB flash", "m328p", 1024, "0xe2", "0xd9"));
//		avrs.add(new AVR("atmega 328", "32kB flash", "m328", 1024, "0xe2", "0xd9"));
		
//		avrs.add(new AVR(ArduinoUSBLinkerUploader.ARDUINO, "32kB flash", "m328", 1024, null, null));
	}

	private void initGUI() {
		mainPanel = new JPanel();

		// create the CellContraints
		CellConstraints cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "fill:pref:grow";
		String panelRows = "3dlu,pref,3dlu,pref,3dlu,pref:grow";
		FormLayout panelLayout = new FormLayout(panelColumns, panelRows);
		mainPanel.setLayout(panelLayout);
		programmingPanel = ceateProgrammingPanel();
		eepromPanel = createEEpromPanel();
		
		tabbedPane.add(programmingPanel);
		
		programmerPanel = new ProgrammerPanel(this, programmers);
		controllerPanel = new ControllerPanel(this, avrs);

		mainPanel.add(programmerPanel, cc.xy(1, 2));
		mainPanel.add(controllerPanel, cc.xy(1, 4));
		mainPanel.add(tabbedPane, cc.xy(1, 6));

		logger.log(Level.INFO, "Set the port from the properties [usb]: " + settings.getProperty("port", "usb"));
		
		programmerPanel.setPort(settings.getProperty("port", "usb"));
		programmerPanel.setRate(settings.getProperty("rate", "19200"));
		programmerPanel.setDefaultRate(new Boolean(settings.getProperty("defaultRate", "true")));
		programmerPanel.setProgrammer(settings.getProperty("programmer",
				"usbasp"));
		controllerPanel.setController(settings.getProperty("controller",
				"Blackboard"));
		
//		testPanel = new TestPanel();
//		
		tabbedPane.addTab(_("programmingPanel.title"), programmingPanel);
		tabbedPane.setBorder(new LineBorder(Color.WHITE));
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		
		
		tabbedPane.addTab(_("eepromPanel.title"), eepromPanel);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent event) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) event.getSource();
				if (sourceTabbedPane.getSelectedComponent().equals(eepromPanel)) {
					logger.info("changed to eepromPanel");
					eepromResetPanel.firePropertyChange(ControllerPanel.CONTROLLER_CHANGED, 0, 1);
				} else if (sourceTabbedPane.getSelectedComponent().equals(programmingPanel)) {
					logger.info("changed to programmingPanel");
				}
				
			}
		});
		
		
		mainframe = new JFrame(_("title") + " " + VERSION + (isBeta?" beta " + betaVersion:"") + " - " + mode);
//		mainframe.setPreferredSize(preferredSize);
//		mainframe.setSize(preferredSize);

//		mainframe.add(tabbedPane);
		mainframe.add(mainPanel);
		mainframe.setJMenuBar((new kkMenu(this, firmwareReader)).getBar());
		mainframe.setIconImage(Icons.getImage(Icons.app, 16));

		mainframe.pack();
		mainframe.setLocationRelativeTo(getRootPane());

	    mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		firmwarePanel.firePropertyChange(ControllerPanel.CONTROLLER_CHANGED, 0, 1);
		
	}

	private JPanel createEEpromPanel() {
		JPanel panel = new JPanel();
		CellConstraints cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "pref:grow";
		String panelRows = "pref,3dlu,fill:pref:grow,3dlu,pref";
		FormLayout panelLayout =  new FormLayout(panelColumns, panelRows);
		panel.setLayout(panelLayout);
		
		eepromResetPanel = new EEpromResetPanel(this);
		eepromSettingsPanel = new EEpromSettingsPanel(this);
		
		panel.add(eepromResetPanel, cc.xy(1, 1));
		panel.add(eepromSettingsPanel, cc.xy(1, 3));
		// TODO Auto-generated method stub
		return panel;
	}


	private JPanel ceateProgrammingPanel() {
		JPanel panel = new JPanel();
		CellConstraints cc = new CellConstraints();

		// create the Layout for Panel this
		String panelColumns = "pref:grow";
		String panelRows = "pref,3dlu,fill:pref";
		FormLayout panelLayout =  new FormLayout(panelColumns, panelRows);
		panel.setLayout(panelLayout);
		
		firmwarePanel = new FirmwarePanel(this, firmwareReader);
		addButtonsStateListener(firmwarePanel);
		output = new JTextArea();
		output.setColumns(20);
		output.setEditable(false);
		output.setRows(15);
		defaultFG = output.getForeground();
		outputPane = new JScrollPane(output);
		outputPane.setAutoscrolls(true);
		outputPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		panel.add(firmwarePanel, cc.xy(1, 1));
		panel.add(outputPane, cc.xy(1, 3));
		
//		panel.setBorder(new LineBorder(Color.WHITE));
		
		return panel;
	}


	public void setLocale(String language, String country) {
		this.locale = new Locale(language, country);
		logger.log(Level.INFO, language + "_" + country);
	}
	
	public Locale getLocale() {
		return locale;
	}

	private void loadTranslation() {
		// load the supported locales and the default locale
		Translatrix
				.loadSupportedLocales("de.lazyzero.kkMulticopterFlashTool.gui.resources.supportedLocales");
		Translatrix.setLocale(Translatrix.getDefaultLocale());
		// load resources for core package
		@SuppressWarnings("rawtypes")
		Vector<Locale> locales = new Vector<Locale>(Translatrix.getSupportedLocales());

		if (locales.contains(locale)) {
			Translatrix.setLocale(locale);
		} else {
			locale = Translatrix.getLocale();
		}
		Translatrix.addBundle("de.lazyzero.kkMulticopterFlashTool.gui.resources.translatrix");

	}

	public boolean restart() {
		int response = JOptionPane.showConfirmDialog(mainframe,
				_("restart.dialog"), _("restart.title"),
				JOptionPane.YES_NO_OPTION);
		if (response == 0)
			return true;
		return false;
	}

	/**
	 * @return the mainframe
	 */
	public JFrame getMainframe() {
		return mainframe;
	}

	/**
	 * @return the settings
	 */
	public Properties getSettings() {
		return settings;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new KKMulticopterFlashTool(args);

		kk.mainframe.setVisible(true);
	}

	public void print(final String line) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				output.append(line);
			}
		});
	}

	public void println(final String line) {
		logger.log(Level.INFO, line);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				output.append(line);
				output.append("\n");
			}
		});
	}
	
	public void err(final String line) {
		logger.log(Level.WARNING, line);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				output.setForeground(Color.red);
				output.append(line);
				output.append("\n");
			}
		});
		if (isPopupsEnabled) {
			JOptionPane.showMessageDialog(this, line, _("error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void clearText() {

		output.setForeground(defaultFG);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				output.setText("");
			}
		});
	}
	
	@Override
	public String getText() {
		return output.getText();
	}


	/**
	 * @return the programmer
	 */
	public AvrdudeProgrammer getProgrammer() {
		return programmer;
	}

	/**
	 * @param programmer
	 *            the programmer to set
	 */
	public void setProgrammer(AvrdudeProgrammer programmer) {
		this.programmer = programmer;
	}

	/**
	 * @return the controller
	 */
	public AVR getController() {
		return controller;
	}

	/**
	 * @param controller
	 *            the controller to set
	 */
	public void setController(AVR controller) {
		this.controller = controller;
	}

	public void setFirmware(Firmware firmware) {
		this.firmware = firmware;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ControllerPanel.CONTROLLER_CHANGED)) {
			firmwarePanel.firePropertyChange(ControllerPanel.CONTROLLER_CHANGED, 0, 1);
			eepromResetPanel.firePropertyChange(ControllerPanel.CONTROLLER_CHANGED, 0, 1);
		}
		if (evt.getPropertyName().equals(MODE_CHANGED)) {
			firmwarePanel.firePropertyChange(ControllerPanel.CONTROLLER_CHANGED, 0, 1);
			if (isOfflineMode()) {
				mode = _("offline");
			} else {
				mode = _("online");
			}
			this.mainframe.setTitle(_("title") + " " + VERSION + " - " + mode);
		}
		if (evt.getPropertyName().equals(ProgrammerPanel.PROGRAMMER_CHANGED)) {
			//TODO Make controller dependent from Programmer.
		}
		
	}



	public static String getTempFolder() {
		String tmpdir;
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            tmpdir = System.getProperty("user.dir")+"\\tmp\\";
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			tmpdir = System.getProperty("user.home")+"/Library/Preferences/kkMulticopterFlashTool/";
		} else {
        	tmpdir = System.getProperty("user.dir")+"/tmp/";
        }
		KKMulticopterFlashTool.getLogger().log(Level.INFO, tmpdir);
		return tmpdir;
	}


	public static boolean isOfflineMode() {
		try {
			return kk.offlineMode;
		} catch (Exception e) {
			return false;
		}
	}


	public static void setOfflineMode(boolean b) {
		kk.offlineMode = b;
	}


	public Vector<Firmware> getFirmwares() {
		return firmwareReader.getFirmwares();
	}

	public boolean isForceFlashing() {
		return forceFlashing;
	}


	public void setForceFlashing(boolean forceFlashing) {
		this.forceFlashing = forceFlashing;
	}
	
	public static Logger getLogger() {
		try {
			return kk.logger;
		} catch (Exception e) {
			return Logger.getLogger("Default-Logger");
		}
	}

	public static Vector<AVR> getControllers() {
		return avrs;
	}


	public void setSelectedTabIndex(int i) {
		tabbedPane.setSelectedIndex(i);
	}


	public static KKMulticopterFlashTool getInstance() {
		return kk;
	}


	public static void setPopupEnabled(boolean enablePopups) {
		isPopupsEnabled = enablePopups;
	}


	public static boolean isPopupsEnabled() {
		return isPopupsEnabled;
	}
	
	public static void setHideDeprecatedEnabled(boolean hideDeprecated) {
		isHideDeprecated = hideDeprecated;
	}


	public static boolean isHideDecprecatedEnabled() {
		return isHideDeprecated;
	}

	public static void setShowDailyTGYEnabled(boolean showDailyTGYEnabled) {
		isShowDailyTGYEnabled = showDailyTGYEnabled;
	}

	public static boolean isShowDailyTGYEnabled() {
		return isShowDailyTGYEnabled;
	}


	public static int getCountdown() {
		System.out.println("Countdown is " + countdown);
		return countdown;
	}


	public static void setCountdown(int value) {
		countdown = value;
	}
	
	public void addButtonsStateListener(ButtonsStateListener myListener) {
		listeners.add(myListener);
	}
	
	private void updateButtons() {
		for (ButtonsStateListener listener : listeners) {
			listener.updateButtons();
		}
	}


	public void flashArduino(ArduinoUpload arduinoUpload, Firmware arduinoFirmware, String arduinoPort) {
		programmerPanel.setProgrammer(arduinoUpload.getAvrProgrammer().getId());
		programmerPanel.setPort(arduinoPort);
		programmerPanel.setRate(arduinoUpload.getSpeed()+"");
		programmerPanel.setDefaultRate(false);
		
		setController(arduinoUpload.getAVR());
		
		setFirmware(arduinoFirmware);
		
		flashAVR();
		
		firePropertyChange(ControllerPanel.CONTROLLER_CHANGED, false, false);
	}


	

	

}
