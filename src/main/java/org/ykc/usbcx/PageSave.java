package org.ykc.usbcx;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.paint.Stop;

public class PageSave implements Runnable{
	public static final Logger logger = LoggerFactory.getLogger(PageSave.class.getName());
	private String logDir = null;
	private String logFileName = null;
	private PageQueue pQueue;
	private int index = 0;
	private boolean isRunning = false;
	
	public String getLogDir() {
		return logDir;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void start(String logDir, String logFileName, PageQueue pQueue){
		this.logDir = logDir;
		this.logFileName = logFileName;
		this.pQueue = pQueue;
		isRunning = true;
		index = 0;
	}

	@Override
	public void run() {
		while(true){
			if(isRunning == true){
				if(!pQueue.isEmpty()){
					DataPage page = pQueue.dequeue();
					savePage(page);
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}			
		}
	}
	
	public void stop(){
		isRunning = false;
	}
	
	public boolean isAllDataWritten(){
		if(pQueue.isEmpty()){
			return true;
		}
		return false;
	}
	
	public void saveLastPage(){
		savePage(pQueue.getCurPage());
	}
	
	private void savePage(DataPage page){
	    try {
	        String filePath = logDir + "/" + logFileName + "_" + index + ".part";
	        logger.info(filePath);
	        FileOutputStream fos = new FileOutputStream(filePath);
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(page);
	        oos.close();
	        index++;

	    } catch (FileNotFoundException e) {
	        logger.error("File Not found while saving part file");
	    } catch (IOException e) {
	    	logger.error("IO exception while saving part file");
	    }
	}	
	
}
