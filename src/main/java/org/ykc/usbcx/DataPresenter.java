package org.ykc.usbcx;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.TableView;


public class DataPresenter implements Runnable{
	public static final Logger logger = LoggerFactory.getLogger(DataPresenter.class.getName());
	private boolean isRunning = false;
	private DataPage curPage;
	private TableView<MainViewRow> tViewMain;
	private int pageIdx = 0;

	public DataPresenter(TableView<MainViewRow> tViewMain){
		this.tViewMain = tViewMain;
	}
	public DataPage getCurPage() {
		return curPage;
	}

	public void setCurPage(DataPage curPage) {
		logger.info("Page changed");
		tViewMain.getItems().clear();
		this.curPage = curPage;
		pageIdx = 0;
	}

	public void start(){
		isRunning = true;
	}

	public void stop(){
		isRunning = false;
	}

	public void clear(){

	}

	@Override
	public void run() {
		int sleep_counter = 0;
		while(true)
		{
			if(isRunning)
			{
				if(curPage != null){
					int pageSize = curPage.getSize();
					
					while(pageSize > pageIdx)
					{
						logger.info(Integer.toString(pageSize));
						logger.info(Integer.toString(pageIdx));
						sleep_counter++;
						if(sleep_counter > 1000)
						{
							sleep_counter = 0;
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
							}
						}
						try {
							addItemMainView(curPage.getItem(pageIdx));
							pageIdx++;
						} catch (Exception e) {
							logger.error("Error in adding data to MainView model");
						}
					}
				}
			}

			try {
				Thread.sleep(350);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
	}

	private void addItemMainView(byte[] pkt) {
		tViewMain.getItems().add(MainViewPktParser.getRow(pkt, tViewMain));
	}
}
