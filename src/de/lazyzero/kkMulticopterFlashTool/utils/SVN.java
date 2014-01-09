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
package de.lazyzero.kkMulticopterFlashTool.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

/**
 * @author Christian Moll
 * 
 */
public class SVN {
	private String url;
	private String file;
	private String name = "anonymous";
	private String password = "anonymous";
	private long startRevision = 0;
	private long endRevision = -1;// HEAD (the latest) revision
	private SVNRepository repository;
	private Logger logger = KKMulticopterFlashTool.getLogger();
	
	private String stateOpenRepositorySuccessfull;
	private String stateNumbersFirmware;
	private String stateFetchRepositorySuccessfull;
	private String stateOpenRepositoryFailed;
	private String stateFetchRevisionFailed;
	private String stateFetchChangelogFailed;
	private String stateFirmwareNotFound;
	private String stateFirmwareDownloadFailed;
	private String stateFirmwareStartDownload;
	private String stateFirmwareFinishedDownload;
	private String stateFirmwareSaveFailed;
	private String stateFirmwareCRCfailed;
	private String svnCRC;
	private KKMulticopterFlashTool instance;

	public SVN(String url, String file) throws Exception {
		this.url = url;
		this.file = file;
		instance = KKMulticopterFlashTool.getInstance();
		

		DAVRepositoryFactory.setup();

		long time = System.currentTimeMillis();
		
		openRepository();
		logger.log(Level.INFO, "openRepostiory [ms]: " + (System.currentTimeMillis() - time));
		
		//fetchRevisions();
		try {
			endRevision = repository.getLatestRevision();
			logger.log(Level.INFO, "latest Revision is: " + endRevision);
			instance.println("latest Revision is: " + endRevision);
		} catch (SVNException svne) {
			logger.log(Level.WARNING,
					"error while fetching the latest repository revision: "
							+ svne.getMessage(), true);
//			StateBar.setState(Icons.ERROR, stateFetchRevisionFailed);
			instance.println("Can't fetch latest revision!!!");
			throw new Exception("Can't fetch latest revision!!!");
		}
		logger.log(Level.INFO, "fetchRevisions [ms]: " + (System.currentTimeMillis() - time));
		
	}

	private void openRepository() throws Exception {
		repository = null;
		try {
			repository = SVNRepositoryFactory.create(SVNURL
					.parseURIEncoded(this.url));
		} catch (SVNException svne) {
			logger.log(Level.WARNING,
					"error while creating an SVNRepository for the location '"
							+ this.url + "': " + svne.getMessage(), true);
//			StateBar.setState(Icons.ERROR, stateOpenRepositoryFailed);
			throw new Exception("Can't access svn!!!");
		}
		ISVNAuthenticationManager authManager = SVNWCUtil
				.createDefaultAuthenticationManager(name, password);
		repository.setAuthenticationManager(authManager);

	}

	

	public File getFile() {
		
		SVNProperties fileProperties = new SVNProperties();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		File svnFile = new File(file);

		try {
			SVNNodeKind nodeKind = repository.checkPath(svnFile.getPath(), -1);

			if (nodeKind == SVNNodeKind.NONE) {
				logger.log(Level.WARNING, "There is no entry at '" + url + "'.");
				
				return null;
			} else if (nodeKind == SVNNodeKind.DIR) {
				logger.log(Level.WARNING, "The entry at '" + url
						+ "' is a directory while a file was expected.");
				return null;
			}
			
			repository.getFile(svnFile.getPath(), endRevision, fileProperties,
					baos);
			System.out.println("SVN File PAth: "+svnFile.getPath());
			
		} catch (SVNException svne) {
			logger.log(Level.WARNING,
					"error while fetching the file contents and properties: "
							+ svne.getMessage());
			return null;
		}
		
		String[] splitUrl = url.split("/");
		String folder = splitUrl[splitUrl.length - 1];
		new File(KKMulticopterFlashTool.getTempFolder() + File.separatorChar + folder).mkdir();
		
		File saveFile = new File(KKMulticopterFlashTool.getTempFolder() + File.separatorChar + folder + File.separatorChar + file);
		System.out.println("saveFile: " + saveFile.getPath());
		try {
			FileOutputStream fos = new FileOutputStream(saveFile);
			baos.writeTo(fos);
			fos.close();
			logger.log(Level.INFO, "File saved: " + saveFile);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		this.svnCRC = fileProperties.getStringValue("svn:entry:checksum");
		
		instance.println("File " + saveFile.getName() + " downloaded. SVN file revision is r" + endRevision);
		
		return saveFile;
		
	}

	public String getFileMD5() {
		return this.svnCRC;
	}
	
}
