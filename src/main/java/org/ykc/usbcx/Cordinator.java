package org.ykc.usbcx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;

public class Cordinator implements IPageChangeListener{
	public static final Logger logger = LoggerFactory.getLogger(Cordinator.class.getName());
	private DataPresenter dp;
	private USBControl usbControl;
	private TableView<MainViewRow> tViewMain;
	private PageQueue pageQueue;
	private TableView<DataViewRow> tViewData;
	private TreeTableView<DetailsRow> ttViewParseViewer;
	private Label lblStartDelta;
	private XScope lchartData;
	Thread presenterThread;

	public Cordinator(USBControl usbcontrol, TableView<MainViewRow> tViewMain, TableView<DataViewRow> tViewData,
			TreeTableView<DetailsRow> ttViewParseViewer, Label lblStartDelta, XScope lchartData) {
		this.usbControl = usbcontrol;
		this.tViewMain = tViewMain;
		this.tViewData = tViewData;
		this.ttViewParseViewer = ttViewParseViewer;
		this.lblStartDelta = lblStartDelta;
		this.lchartData = lchartData;
		dp = new DataPresenter(tViewMain, tViewData, ttViewParseViewer, lblStartDelta, lchartData);
		presenterThread = new Thread(dp);
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

	public void openScopeData(File scopeFile){
		try {
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(scopeFile));
			ArrayList<DataNode> scopeSamples =(ArrayList<DataNode>)inputStream.readObject();
			inputStream.close();
			lchartData.setDataPoints(scopeSamples);
		} catch (Exception e) {
			logger.error("Deserialization error in opening scope file");
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

	public void terminate() {
		dp.stop();
		dp.terminate();
	}

	public void openScopeLiveData() {
		lchartData.setDataPoints(pageQueue.getScopeSamples());
	}
}
