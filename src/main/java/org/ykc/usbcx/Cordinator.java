package org.ykc.usbcx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.TableView;

public class Cordinator implements IPageChangeListener{
	public static final Logger logger = LoggerFactory.getLogger(Cordinator.class.getName());
	private DataPresenter dp;
	private USBControl usbControl;
	private TableView<MainViewRow> tViewMain;
	private PageQueue pageQueue;

	public Cordinator(USBControl usbcontrol, TableView<MainViewRow> tViewMain) {
		this.usbControl = usbcontrol;
		this.tViewMain = tViewMain;
		dp = new DataPresenter(tViewMain);
		Thread presenterThread = new Thread(dp);
		presenterThread.start();
		pageQueue = usbcontrol.getUsbTransferTask().getPageQueue();
		pageQueue.addListener(this);
	}

	public void openPage(File partFile){
		try {
			dp.stop();
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(partFile));
			DataPage page=(DataPage)inputStream.readObject();
			inputStream.close();
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
			dp.setCurPage(page);
			dp.start();
		} catch (Exception e) {
			logger.error("Deserialization error in opening part file");
		}
	}

	public void clearView(){
		dp.stop();
		try {
			Thread.sleep(50);
		} catch (Exception e) {
		}
		dp.setCurPage(null);
		tViewMain.getItems().clear();

	}

	public void openPage(DataPage page){
		try {
			dp.stop();
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
			dp.setCurPage(page);
			dp.start();
		} catch (Exception e) {
			logger.error("Deserialization error in opening part file");
		}
	}

	@Override
	public void pageChanged(DataPage newPage) {
		openPage(newPage);
	}
}
