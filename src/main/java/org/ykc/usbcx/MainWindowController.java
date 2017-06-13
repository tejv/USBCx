package org.ykc.usbcx;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ykc.usbcx.DetailsRow.BG;

public class MainWindowController implements Initializable{

	public static final Logger logger = LoggerFactory.getLogger(MainWindowController.class.getName());

    @FXML
    private BorderPane bPaneMainWindow;

	 @FXML
    private TableView<MainViewRow> tViewMain;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewSno;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewOk;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewSop;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewMsg;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewId;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewDrole;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewProle;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewCount;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewRev;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewDuration;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewDelta;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewVbus;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewData;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewStartTime;

    @FXML
    private TableColumn<MainViewRow, String> tColMViewEndTime;

    @FXML // fx:id="ttViewParseViewer"
    private TreeTableView<DetailsRow> ttViewParseViewer; // Value injected by FXMLLoader

    @FXML // fx:id="ttColPVName"
    private TreeTableColumn<DetailsRow, String> ttColPVName; // Value injected by FXMLLoader

    @FXML // fx:id="ttColPVValue"
    private TreeTableColumn<DetailsRow, String> ttColPVValue; // Value injected by FXMLLoader

    @FXML // fx:id="ttColPVDecimal"
    private TreeTableColumn<DetailsRow, String> ttColPVDecimal; // Value injected by FXMLLoader

    @FXML // fx:id="ttColPVHex"
    private TreeTableColumn<DetailsRow, String> ttColPVHex; // Value injected by FXMLLoader

    @FXML // fx:id="ttColPVBinary"
    private TreeTableColumn<DetailsRow, String> ttColPVBinary; // Value injected by FXMLLoader

    @FXML // fx:id="ttColPVOffset"
    private TreeTableColumn<DetailsRow, String> ttColPVOffset; // Value injected by FXMLLoader

    @FXML // fx:id="ttColPVLength"
    private TreeTableColumn<DetailsRow, String> ttColPVLength; // Value injected by FXMLLoader

    @FXML // fx:id="txtAreaDataView"
    private TextArea txtAreaDataView; // Value injected by FXMLLoader

    @FXML
    private Button bOpen;

    @FXML
    private Button bSave;

    @FXML
    private Button bStartStop;

    @FXML
    private Button bReset;

    @FXML
    private Button bTrigger;

    @FXML
    private Button bGetVersion;

    @FXML
    private Button bDownload;

    @FXML
    private Button bAbout;

    @FXML // fx:id="bFirstPage"
    private Button bFirstPage; // Value injected by FXMLLoader

    @FXML // fx:id="bPrevPage"
    private Button bPrevPage; // Value injected by FXMLLoader

    @FXML // fx:id="bNextPage"
    private Button bNextPage; // Value injected by FXMLLoader

    @FXML // fx:id="bLastPage"
    private Button bLastPage; // Value injected by FXMLLoader

    @FXML
    private StatusBar statusBar;

    @FXML // fx:id="cBoxDeviceList"
    private ComboBox<String> cBoxDeviceList; // Value injected by FXMLLoader

    @FXML // fx:id="lblStartDelta"
    private Label lblStartDelta; // Value injected by FXMLLoader

    @FXML // fx:id="chkStartSno"
    private CheckBox chkStartSno; // Value injected by FXMLLoader

    @FXML // fx:id="chkEndSno"
    private CheckBox chkEndSno; // Value injected by FXMLLoader

    @FXML // fx:id="chkSop"
    private CheckBox chkSop; // Value injected by FXMLLoader

    @FXML
    private CheckBox chkCount;

    @FXML // fx:id="chkMsgId"
    private CheckBox chkMsgId; // Value injected by FXMLLoader

    @FXML // fx:id="chkMsgType"
    private CheckBox chkMsgType; // Value injected by FXMLLoader

    @FXML // fx:id="txtStartSno"
    private TextField txtStartSno; // Value injected by FXMLLoader

    @FXML // fx:id="txtEndSno"
    private TextField txtEndSno; // Value injected by FXMLLoader

    @FXML // fx:id="txtCount"
    private TextField txtCount; // Value injected by FXMLLoader

    @FXML // fx:id="txtMsgId"
    private TextField txtMsgId; // Value injected by FXMLLoader

    @FXML // fx:id="cBoxSop"
    private ComboBox<String> cBoxSop; // Value injected by FXMLLoader

    @FXML // fx:id="cBoxMsgType"
    private ComboBox<String> cBoxMsgType; // Value injected by FXMLLoader

    @FXML // fx:id="cBoxMsgClass"
    private ComboBox<String> cBoxMsgClass; // Value injected by FXMLLoader

    private USBControl usbcontrol;
    private Stage myStage;
    private Cordinator cordinator;
    private ObservableList<File> partFileList;
    private int partListIdx = 0;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Preferences.genTempFolders();
		Preferences.loadPreferences();
		bOpen.setGraphic(new ImageView(new Image("/open.png")));
		bOpen.setTooltip(new Tooltip("Open ucx1 file"));
		bSave.setGraphic(new ImageView(new Image("/save.png")));
		bSave.setTooltip(new Tooltip("Save ucx1 file"));
		bStartStop.setGraphic(new ImageView(new Image("/start_stop.png")));
		bStartStop.setTooltip(new Tooltip("Start/Stop Capture"));
		bReset.setGraphic(new ImageView(new Image("/reset.png")));
		bReset.setTooltip(new Tooltip("Reset and clear"));
		bTrigger.setGraphic(new ImageView(new Image("/trigger.png")));
		bTrigger.setTooltip(new Tooltip("Set Trigger"));
		bGetVersion.setGraphic(new ImageView(new Image("/version.png")));
		bGetVersion.setTooltip(new Tooltip("Get Version"));
		bDownload.setGraphic(new ImageView(new Image("/download.png")));
		bDownload.setTooltip(new Tooltip("Download FW"));
		bAbout.setGraphic(new ImageView(new Image("/info.png")));
		bAbout.setTooltip(new Tooltip("About USBCx"));
		bFirstPage.setGraphic(new ImageView(new Image("/double_arrow_left.png")));
		bFirstPage.setTooltip(new Tooltip("Go to First Page"));
		bPrevPage.setGraphic(new ImageView(new Image("/arrow_left.png")));
		bPrevPage.setTooltip(new Tooltip("Go to Previous Page"));
		bNextPage.setGraphic(new ImageView(new Image("/arrow_right.png")));
		bNextPage.setTooltip(new Tooltip("Go to Next Page"));
		bLastPage.setGraphic(new ImageView(new Image("/double_arrow_right.png")));
		bLastPage.setTooltip(new Tooltip("Go to Last Page"));

		ttColPVName.setCellValueFactory(new TreeItemPropertyValueFactory<DetailsRow, String>("name"));
		ttColPVValue.setCellValueFactory(new TreeItemPropertyValueFactory<DetailsRow, String>("value"));
		ttColPVDecimal.setCellValueFactory(new TreeItemPropertyValueFactory<DetailsRow, String>("decval"));
		ttColPVHex.setCellValueFactory(new TreeItemPropertyValueFactory<DetailsRow, String>("hexval"));
		ttColPVBinary.setCellValueFactory(new TreeItemPropertyValueFactory<DetailsRow, String>("binaryval"));
		ttColPVLength.setCellValueFactory(new TreeItemPropertyValueFactory<DetailsRow, String>("len"));
		ttColPVOffset.setCellValueFactory(new TreeItemPropertyValueFactory<DetailsRow, String>("offset"));
	    TreeItem<DetailsRow> rootItem = new TreeItem<DetailsRow> ();
	    ttViewParseViewer.setRoot(rootItem);

	    ttColPVName.setCellFactory((TreeTableColumn<DetailsRow, String> param) -> {
            TreeTableCell<DetailsRow, String> cell = new TreeTableCell<DetailsRow, String>(){
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : getItem().toString());
                    TreeTableRow<DetailsRow> ttr = getTreeTableRow();
                    DetailsRow x = ttr.getItem();
                    DetailsRow.BG bg = BG.NORMAL;
                    String style = "";
                    if(x != null){
                    	bg = x.getBcolor();
                    	style += x.getBold() ? "-fx-font-weight:bold;-fx-font-style:italic;":"";
                    }

                    switch(bg){
                    case RED:
                    	style += "-fx-background-color:red;"
                    			+ "-fx-text-fill:white;";
                    	break;
                    case GREEN:
                    	style += "-fx-background-color:green;"
                    			+ "-fx-text-fill:white;";
                    	break;
                    case BLUE:
                    	style += "-fx-background-color:blue;"
                    			+ "-fx-text-fill:white;";
                    	break;
                    case YELLOW:
                    	style += "-fx-background-color:yellow;"
                    			+ "-fx-text-fill:black;";
                    	break;
                    case PINK:
                    	style += "-fx-background-color:pink;"
                    			+ "-fx-text-fill:black;";
                    	break;
                    default:
                    	style += "-fx-text-fill:black;"
                    			+ "-fx-highlight-fill:dodgerblue;"
                    			+ "-fx-highlight-text-fill:white";
                        break;
                    }
                    setStyle(style);
                }
            };
            return cell;
        });

	    tColMViewSno.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("sno"));
	    tColMViewOk.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("ok"));
	    tColMViewSop.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("sop"));
	    tColMViewMsg.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("msg"));
	    tColMViewId.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("id"));
	    tColMViewDrole.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("drole"));
	    tColMViewProle.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("prole"));
	    tColMViewCount.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("count"));
	    tColMViewRev.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("rev"));
	    tColMViewDuration.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("duration"));
	    tColMViewDelta.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("delta"));
	    tColMViewVbus.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("vbus"));
	    tColMViewData.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("data"));
	    tColMViewStartTime.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("stime"));
	    tColMViewEndTime.setCellValueFactory(new PropertyValueFactory<MainViewRow,String>("etime"));

	    cBoxMsgClass.getItems().addAll(PDUtils.MSG_CLASS);
	    cBoxMsgClass.getSelectionModel().select(0);
	    cBoxMsgType.getItems().addAll(PDUtils.CTRL_MSG_TYPE);
	    cBoxMsgType.getSelectionModel().select(1);
	    cBoxSop.getItems().addAll(PDUtils.SOP_TYPE);
	    cBoxSop.getSelectionModel().select(0);


	    usbcontrol = new USBControl(cBoxDeviceList, statusBar);
	    cordinator = new Cordinator(usbcontrol, tViewMain);
	}

    @FXML
    void openOnDragOver(DragEvent event) {
    	 Dragboard db = event.getDragboard();
         if (db.hasFiles()) {
             event.acceptTransferModes(TransferMode.ANY);
         } else {
             event.consume();
         }
    }

    @FXML
    void openOnDragDrop(DragEvent event) {
    	Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {

            String filePath = null;
            for (File file:db.getFiles()) {
            	if(Utils.getFileExtension(file).equals("ucx1")){
            		success = true;
            		OpenRecord.open(file, usbcontrol, statusBar);
            		break;
            	}
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    void openRecord(ActionEvent event) {
    	partFileList = OpenRecord.open(bPaneMainWindow.getScene().getWindow(), usbcontrol, statusBar);
    	if(partFileList != null){
	    	partListIdx = 0;
	    	cordinator.openPage(partFileList.get(partListIdx));
    		bFirstPage.setDisable(false);
    		bLastPage.setDisable(false);
    		bPrevPage.setDisable(false);
    		bNextPage.setDisable(false);
    	}
    }

    @FXML
    void gotoFirstPage(ActionEvent event) {
    	if(partFileList != null){
	    	partListIdx = 0;
	    	cordinator.openPage(partFileList.get(partListIdx));
	    	statusBar.setText("First Page-> Page 0");
    	}
    }

    @FXML
    void gotoLastPage(ActionEvent event) {
    	if(partFileList != null){
	    	partListIdx = partFileList.size() - 1;
	    	cordinator.openPage(partFileList.get(partListIdx));
	    	statusBar.setText("Last Page-> Page "+ partListIdx);
    	}    	
    }

    @FXML
    void gotoNextPage(ActionEvent event) {
    	if(partFileList != null){
	    	partListIdx++;
	    	if(partListIdx >= partFileList.size()){
	    		partListIdx = partFileList.size() - 1;
	    		statusBar.setText("Last Page-> Page "+ partListIdx);
	    	}
	    	else{
	    		statusBar.setText("Page: " + partListIdx);
	    	}
	    	cordinator.openPage(partFileList.get(partListIdx));
    	}
    }

    @FXML
    void gotoPreviousPage(ActionEvent event) {
    	if(partFileList != null){
	    	partListIdx--;
	    	if(partListIdx < 0){
	    		partListIdx = 0;
	    		statusBar.setText("First Page-> Page 0");
	    	}
	    	else{
	    		statusBar.setText("Page: " + partListIdx);
	    	}
	    	cordinator.openPage(partFileList.get(partListIdx));
    	}
    }

    @FXML
    void saveRecord(ActionEvent event) {
    	SaveRecord.save(bPaneMainWindow.getScene().getWindow(), usbcontrol, statusBar);
    }

    @FXML
    void startStopCapture(ActionEvent event) {
    	if(usbcontrol.isHwCapturing() == false){
    		bFirstPage.setDisable(true);
    		bLastPage.setDisable(true);
    		bPrevPage.setDisable(true);
    		bNextPage.setDisable(true);
    	}
    	else{
    		bFirstPage.setDisable(false);
    		bLastPage.setDisable(false);
    		bPrevPage.setDisable(false);
    		bNextPage.setDisable(false);
    	}
    	usbcontrol.startStopCapture();
    }


    @FXML
    void resetCapture(ActionEvent event) {
    	usbcontrol.resetCapture();
    }

    @FXML
    void trgMsgClassChanged(ActionEvent event) {
    	cBoxMsgType.getItems().clear();
    	int selIndex = cBoxMsgClass.getSelectionModel().getSelectedIndex();
    	switch (selIndex){
    	case 0:
    		cBoxMsgType.getItems().addAll(PDUtils.CTRL_MSG_TYPE);
    		break;
    	case 1:
    		cBoxMsgType.getItems().addAll(PDUtils.DATA_MSG_TYPE);
    		break;
    	default:
    		cBoxMsgType.getItems().addAll(PDUtils.EXTD_MSG_TYPE);
    			break;
    	}
    	cBoxMsgType.getSelectionModel().select(0);
    }

    @FXML
    void setTrigger(ActionEvent event) {
    	usbcontrol.setTrigger(chkStartSno, chkEndSno, chkSop, chkMsgType, chkCount, chkMsgId,
    			              txtStartSno, txtEndSno, cBoxSop, cBoxMsgClass, cBoxMsgType, txtCount, txtMsgId);
    }

    @FXML
    void getVersion(ActionEvent event) {
    	usbcontrol.getVersion();
    }

    @FXML
    void downloadFw(ActionEvent event) {
    	usbcontrol.downloadFW();
    }

    @FXML
    void showAboutMe(ActionEvent event) {
    	displayAboutMe();
    }

    @FXML
    void populate(ActionEvent event) {
    	addItems();
    }

	private void displayAboutMe() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = getClass().getResource("/version.properties").openStream();
			prop.load(input);
			String ver = prop.getProperty("MAJOR_VERSION") + "."+ prop.getProperty("MINOR_VERSION") + "." + prop.getProperty("BUILD_NO");
			MsgBox.display("About Me", "USBCx -> USBPD Analyzer\nVersion: "+ ver +"\nAuthor: Tejender Sheoran\nEmail: tejendersheoran@gmail.com\nCopyright(C) (2016-2018) Tejender Sheoran\nThis program is free software. You can redistribute it and/or modify it\nunder the terms of the GNU General Public License Ver 3.\n<http://www.gnu.org/licenses/>");

		} catch (IOException e) {

		}
		finally{
			if(input != null){
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void addItems(){
		ObservableList<DetailsRow> xFields = FXCollections.observableArrayList();
		xFields.add(new DetailsRow.Builder()
				.name("Dead")
				.value("1")
				.level(0)
				.bcolor(BG.NORMAL)
				.bold(true)
				.build());
		xFields.add(new DetailsRow.Builder()
				.name("Beef")
				.value("1")
				.level(1)
				.bcolor(BG.RED)
				.build());
		xFields.add(new DetailsRow.Builder()
				.name("Holy")
				.value("1")
				.level(0)
				.bcolor(BG.GREEN)
				.build());
		xFields.add(new DetailsRow.Builder()
				.name("Cow")
				.value("1")
				.level(1)
				.bcolor(BG.YELLOW)
				.build());
		xFields.add(new DetailsRow.Builder()
				.name("Dead")
				.value("1")
				.level(0)
				.bcolor(BG.BLUE)
				.build());
		xFields.add(new DetailsRow.Builder()
				.name("Ninza")
				.value("1")
				.level(1)
				.bcolor(BG.PINK)
				.build());
		DetailsLoader.run(xFields, ttViewParseViewer);
	}

	private void cleanUpTempFiles(){
		try {
			File logDir = new File(System.getProperty("user.home"), "USBCx/logs/");
			if(logDir.exists()){
				FileUtils.deleteDirectory(logDir);
			}
			File tempDir = new File(System.getProperty("user.home"), "USBCx/temp/");
			if(tempDir.exists()){
				FileUtils.deleteDirectory(tempDir);
			}
		} catch (IOException e) {
			logger.error("IO Error while cleaning up logs and temp directory");
		}
	}

	private void appClosing(){
		try {
			File logDir = Preferences.getLogDir();
			if(logDir.exists()){
				FileUtils.deleteDirectory(logDir);
			}
			File tempDir = Preferences.getTempDir();
			if(tempDir.exists()){
				FileUtils.deleteDirectory(tempDir);
			}
			Preferences.storePreferences();

		} catch (Exception e) {
			logger.error("IO Error while cleaning up logs and temp directory");
		}
	}

	public void setStage(Stage stage) {
	    myStage = stage;
		myStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		      public void handle(WindowEvent we) {
		    	  appClosing();
		      }
		  });
	}

}




