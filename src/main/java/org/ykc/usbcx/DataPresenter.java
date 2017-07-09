package org.ykc.usbcx;


import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ykc.usbmanager.USBManager;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;


public class DataPresenter implements Runnable{
	public static final Logger logger = LoggerFactory.getLogger(DataPresenter.class.getName());
	private volatile boolean isRunning = false;
	private volatile boolean isTerminated = false;
	private DataPage curPage;
	private TableView<MainViewRow> tViewMain;
	private TableView<DataViewRow> tViewData;
	private TreeTableView<DetailsRow> ttViewParseViewer;
	private Label lblStartDelta;
	private int pageIdx = 0;

	public DataPresenter(TableView<MainViewRow> tViewMain, TableView<DataViewRow> tViewData,
			TreeTableView<DetailsRow> ttViewParseViewer, Label lblStartDelta){
		this.tViewMain = tViewMain;
		this.tViewData = tViewData;
		this.ttViewParseViewer = ttViewParseViewer;
		this.lblStartDelta = lblStartDelta;
		tViewMain.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		    if (newSelection != null) {
		    	int idx = tViewMain.getSelectionModel().getSelectedIndex();
		    	DataViewPresenter.updateDataView(idx, tViewData, curPage);
		    	DetailsPresenter.updateView(idx, ttViewParseViewer, curPage);
		    	StartDeltaCalculator.setDelta(curPage.getItem(idx), lblStartDelta);
		    }
		});
		TableColumn<MainViewRow, String> tColMViewOk = (TableColumn<MainViewRow, String>) tViewMain.getColumns().get(1);


		tColMViewOk.setCellFactory(column -> {
	        return new TableCell<MainViewRow, String>() {
	            @Override
	            protected void updateItem(String value, boolean empty) {
	                super.updateItem(value, empty);

	                setText(empty ? "" : getItem().toString());
	                setGraphic(null);

	                TableRow<MainViewRow> currentRow = getTableRow();
	                TableCell<MainViewRow, String> cell = this;
	                String status = "";
	                if(currentRow.getItem() != null){
	                	status = currentRow.getItem().getOk();
	                }

	                switch(status)
	                {
	                case "Ok":
	                	cell.setStyle("-fx-text-fill: green");
	                	break;
	                case "ER_CRC":
	                	cell.setStyle("-fx-text-fill: yellow");
	                	break;
	                case "ER":
	                case "ER_CRC_EOP":
	                	cell.setStyle("-fx-text-fill: red");
	                	break;
	                case "ER_EOP":
	                	cell.setStyle("-fx-text-fill: blue");
	                	break;
	                case "VBUS_DN":
	                	cell.setStyle("-fx-text-fill: orange");
	                	break;
	                case "VBUS_UP":
	                	cell.setStyle("-fx-text-fill: cyan");
	                	break;

	                case "CC1_DEF":
	                case "CC1_1_5A":
	                case "CC1_3A":
	                case "CC2_DEF":
	                case "CC2_1_5A":
	                case "CC2_3A":
	                	cell.setStyle("-fx-text-fill: pink");
	            		break;
	                case "DETACH":
	                	cell.setStyle("-fx-text-fill: navajowhite");
	                	break;
	                default:
	                	break;
	                }

	            }
	        };
	    });


	}

	public DataPage getCurPage() {
		return curPage;
	}

	public void setCurPage(DataPage curPage) {
//		logger.info("Page changed");
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
			if(isTerminated){
				break;
			}
			if(isRunning)
			{
				try {
					if(curPage != null){
						int pageSize = curPage.getSize();

						while(pageSize > pageIdx)
						{
//							logger.info(Integer.toString(pageSize));
//							logger.info(Integer.toString(pageIdx));
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
//					logger.info("Error in adding data to MainView model");
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

	public void terminate() {
		isTerminated = true;

	}

}
