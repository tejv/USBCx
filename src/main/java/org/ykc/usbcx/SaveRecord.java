package org.ykc.usbcx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.stage.FileChooser;
import javafx.stage.Window;

public class SaveRecord {
	public static final Logger logger = LoggerFactory.getLogger(SaveRecord.class.getName());

	private static File saveFile(Window win, String typeString1, String typeString2){
		FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Save USBCx Record");

	    if(Preferences.getLastSaveDirectory() != null)
	    {
	    	fileChooser.setInitialDirectory(Preferences.getLastSaveDirectory());
	    }

	    fileChooser.getExtensionFilters().addAll(
	            new FileChooser.ExtensionFilter(typeString1, typeString2)
	        );

	    File file = fileChooser.showSaveDialog(win);
        if (file != null) {
        	if(!file.getName().contains(".")) {
        		file = new File(file.getAbsolutePath() + ".ucx1");
        		}
        	Preferences.setLastSaveDirectory(file.getParentFile());
        }
	    return file;
	}

	public static void save(Window win, USBControl usbControl, StatusBar statusBar){
		File file = saveFile(win, "USBCx Files(*.ucx1)", "*.ucx1");
        if (file != null) {
        	if(zipRecord(file, usbControl, statusBar) == true)
        	{
        		statusBar.setText("Save Success");
        	}
        	else{
        		statusBar.setText("Save Failed");
        	}
        }
        else
        {
        	statusBar.setText("Operation Cancelled");
        }
	}

	private static boolean zipRecord(File file, USBControl usbControl, StatusBar statusBar){
		try {
        	if(usbControl.isHwCapturing()){
    			usbControl.sendStopCommand();
    			try {
    				Thread.sleep(1000);
    			} catch (Exception e) {
    			}
        	}
			PageSave saveHandler = usbControl.getUsbTransferTask().getPageSave();
			saveHandler.saveLastPage();
			File logDir = new File(saveHandler.getLogDir());
			FileOutputStream fout = new FileOutputStream(file);
			ZipOutputStream zout = new ZipOutputStream(fout);
			for (File partFile: logDir.listFiles())
			{
			    ZipEntry ze = new ZipEntry(partFile.getName());
			    zout.putNextEntry(ze);
			    FileInputStream in = new FileInputStream(partFile);
	    		int len;
	    		byte[] buffer = new byte[1024];
	    		while ((len = in.read(buffer)) > 0) {
	    			zout.write(buffer, 0, len);
	    		}
	    		in.close();
			    zout.closeEntry();
			}
			zout.close();

			return true;
		} catch (Exception e) {
			logger.error("Error in zipping records and saving ucx1 file");
			return false;
		}
	}
}
