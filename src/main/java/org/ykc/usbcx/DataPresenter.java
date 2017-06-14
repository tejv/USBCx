package org.ykc.usbcx;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ykc.usbmanager.USBManager;

import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


public class DataPresenter implements Runnable{
	public static final Logger logger = LoggerFactory.getLogger(DataPresenter.class.getName());
	private boolean isRunning = false;
	private DataPage curPage;
	private TableView<MainViewRow> tViewMain;
	private TableView<DataViewRow> tViewData;
	private int pageIdx = 0;

	public DataPresenter(TableView<MainViewRow> tViewMain, TableView<DataViewRow> tViewData){
		this.tViewMain = tViewMain;
		this.tViewData = tViewData;
		tViewMain.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		    if (newSelection != null) {
		    	int idx = tViewMain.getSelectionModel().getSelectedIndex();
		    	DataViewPresenter.updateDataView(idx, tViewData, curPage);
		    }
		});
		TableColumn<MainViewRow, String> tColMViewOk = (TableColumn<MainViewRow, String>) tViewMain.getColumns().get(1);


//		tColMViewOk.setCellFactory(new Callback<TableColumn, TableCell>() {
//	        public TableCell call(TableColumn param) {
//	            return new TableCell<TableData, String>() {
//
//	                @Override
//	                public void updateItem(String item, boolean empty) {
//	                    super.updateItem(item, empty);
//	                    if (!isEmpty()) {
//	                        this.setTextFill(Color.RED);
//	                        // Get fancy and change color based on data
//	                        if(item.contains("@"))
//	                            this.setTextFill(Color.BLUEVIOLET);
//	                        setText(item);
//	                    }
//	                }
//	            };
//	        }
//	    });


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
				try {
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
							addItemMainView(curPage.getItem(pageIdx));
							pageIdx++;
						}
					}
				}
				catch (Exception e) {
					logger.info("Error in adding data to MainView model");
				}
			}

			try {
				Thread.sleep(350);
			} catch (InterruptedException e) {
			}
		}
	}

	private void addItemMainView(byte[] pkt) {
		tViewMain.getItems().add(MainViewPktParser.getRow(pkt, tViewMain));
	}



}
