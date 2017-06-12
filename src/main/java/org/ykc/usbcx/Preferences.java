package org.ykc.usbcx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.Tab;

public class Preferences {
	public static final Logger logger = LoggerFactory.getLogger(Preferences.class.getName());
	private static File lastSaveDirectory = new File(System.getProperty("user.home"));
	private static File lastOpenDirectory = new File(System.getProperty("user.home"));
	private static File logDir = null;
	private static File tempDir = null;


	public static File getLogDir() {
		return logDir;
	}

	public static File getTempDir() {
		return tempDir;
	}

	public static File getLastSaveDirectory() {
		return lastSaveDirectory;
	}

	public static void setLastSaveDirectory(File lastSaveDirectory) {
		Preferences.lastSaveDirectory = lastSaveDirectory;
	}

	public static File getLastOpenDirectory() {
		return lastOpenDirectory;
	}

	public static void setLastOpenDirectory(File lastOpenDirectory) {
		Preferences.lastOpenDirectory = lastOpenDirectory;
	}

	public static boolean storePreferences()
	{
	    try {
			File prefDir = new File(System.getProperty("user.home"), "USBCx/preferences");
			if (! prefDir.exists()) {
				prefDir.mkdirs();
			}
			File prefFile = new File(System.getProperty("user.home"), "USBCx/preferences/app.xml");
			if (! prefFile.exists()) {
				prefFile.createNewFile();
			}
			return createPreferences(prefFile);
		} catch (Exception e) {

		}
		return false;
	}

	public static org.jdom2.Document getJDOM2Doc(File input) {
		try {
			SAXBuilder saxBuilder = new SAXBuilder();
			org.jdom2.Document document = saxBuilder.build(input);
			return document;
		}
		catch(Exception ex){

		}
		return null;
	}

	public static boolean loadPreferences(){

	    try {
			File prefFile = new File(System.getProperty("user.home"), "USBCx/preferences/app.xml");
			if (! prefFile.exists()) {
				return false;
			}
			Document prefDoc = getJDOM2Doc(prefFile);
			if(prefDoc == null)
			{
				return false;
			}
			Element prefElement = prefDoc.getRootElement();
			String lastSaveDir = prefElement.getChildText("lastSaveDirectory");
			if((!lastSaveDir.equals("null")) && (new File(lastSaveDir).exists()))
			{
				lastSaveDirectory = new File(lastSaveDir);
			}

			String lastOpenDir = prefElement.getChildText("lastOpenDirectory");
			if((!lastOpenDir.equals("null")) && (new File(lastOpenDir).exists()))
			{
				lastOpenDirectory = new File(lastOpenDir);
			}

			return true;
		} catch (Exception e) {

		}
		return false;
	}

	private static boolean createPreferences(File preFile){
		Document doc = new Document();
		Element theRoot = new Element("preferences");
		doc.setRootElement(theRoot);

		Element lastSaveDir = new Element("lastSaveDirectory");
		if(lastSaveDirectory != null){
			lastSaveDir.setText(lastSaveDirectory.getAbsolutePath());
		}
		else{
			lastSaveDir.setText("null");
		}
		theRoot.addContent(lastSaveDir);

		Element lastOpenDir = new Element("lastOpenDirectory");
		if(lastOpenDirectory != null){
			lastOpenDir.setText(lastOpenDirectory.getAbsolutePath());
		}
		else{
			lastOpenDir.setText("null");
		}
		theRoot.addContent(lastOpenDir);

		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		try {
			FileOutputStream x = new FileOutputStream(preFile);
			xmlOutput.output(doc, x);
			x.close();
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public static void genTempFolders(){
		ZonedDateTime time = ZonedDateTime.now();
		String timeString = String.format("%04d", time.getYear())  + "_" +
							String.format("%02d", time.getMonthValue()) + "_" +
				            String.format("%02d", time.getDayOfMonth()) + "_" +
				            String.format("%02d", time.getHour()) + "_" +
				            String.format("%02d", time.getMinute()) + "_" +
				            String.format("%02d", time.getSecond());

		logDir = new File(System.getProperty("user.home"), "USBCx/log/s_"+ timeString);
		tempDir = new File(System.getProperty("user.home"), "USBCx/tmp/s_"+ timeString);

		try {
			if (! logDir.exists()) {
				logDir.mkdirs();
			}

			if (! tempDir.exists()) {
				tempDir.mkdirs();
			}
		} catch (Exception e) {
			logger.error("Error while creating temp and log directories");
		}
	}
}

