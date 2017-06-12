package org.ykc.usbcx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;

public class OpenRecord {
	public static final Logger logger = LoggerFactory.getLogger(SaveRecord.class.getName());
	private static File openFile(Window win, ObservableList<ExtensionFilter> extensionFilterslist){
		FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Open USBCx Record");

	    if(Preferences.getLastOpenDirectory() != null)
	    {
	    	fileChooser.setInitialDirectory(Preferences.getLastOpenDirectory());
	    }

	    fileChooser.getExtensionFilters().addAll(extensionFilterslist);
	    File file = fileChooser.showOpenDialog(win);
	    if (file != null) {
	    	Preferences.setLastOpenDirectory(file.getParentFile());
	    }
	    return file;
	}

	public static ObservableList<File> open(Window win, USBControl usbcontrol, StatusBar statusBar){
		
		ObservableList<ExtensionFilter> extensionFilters = FXCollections.observableArrayList();
		extensionFilters.add(new ExtensionFilter("USBCx Files(*.ucx1)", "*.ucx1"));
		File file =  openFile(win, extensionFilters);
        return open(file, usbcontrol, statusBar);
	}
	
	public static ObservableList<File> open(File file,  USBControl usbcontrol, StatusBar statusBar){
		ObservableList<File> partFileList = null;
		if (file != null) {
        	if(usbcontrol.isHwCapturing()){
        		usbcontrol.sendStopCommand();
        	}
        	try {
				partFileList = unzipRecord(file, statusBar);
			} catch (Exception e) {
				statusBar.setText("File open Failed");
			}
        	statusBar.setText("File Open Success : " + file.getName());
        }
        else
        {
        	statusBar.setText("Operation Cancelled");
        }
        return partFileList;		
	}

	private static ObservableList<File> unzipRecord(File file, StatusBar statusBar) {
		ObservableList<File> partFilelist = FXCollections.observableArrayList();
		try {
			File tempDir = new File(Preferences.getTempDir(), FilenameUtils.removeExtension(file.getName()));

			if (tempDir.exists()) {
				FileUtils.deleteDirectory(tempDir);
			}
			tempDir.mkdirs();
			 byte[] buffer = new byte[1024];
	    	ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
	    	ZipEntry ze = zis.getNextEntry();

	    	while(ze!=null){

	    	   String fileName = ze.getName();
	           File newFile = new File(tempDir + File.separator + fileName);

	           new File(newFile.getParent()).mkdirs();

	           FileOutputStream fos = new FileOutputStream(newFile);

	           int len;
	           while ((len = zis.read(buffer)) > 0) {
	       			fos.write(buffer, 0, len);
	           }

	           fos.close();
	           ze = zis.getNextEntry();
	    	}

	        zis.closeEntry();
	    	zis.close();
			return partFilelist;
		} catch (Exception e) {
			logger.error("File Open Unzip failed");
			throw new RuntimeException("Unzip fail");
		}
	}
}
