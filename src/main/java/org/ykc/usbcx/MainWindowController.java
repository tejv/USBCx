package org.ykc.usbcx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
import org.ykc.usbcx.XScope;

public class MainWindowController implements Initializable{

	public static final Logger logger = LoggerFactory.getLogger(MainWindowController.class.getName());

    @FXML
    private BorderPane bPaneMainWindow;

    @FXML
    private TabPane tabPaneMain;

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

    @FXML
    private TableView<DataViewRow> tViewData;

    @FXML
    private TableColumn<DataViewRow, Integer> tColDataViewIndex;

    @FXML
    private TableColumn<DataViewRow, String> tColDataViewValue;

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
    private Button bCollapse;

    @FXML
    private Button bExpand;

    @FXML
    private StatusBar statusBar;

    @FXML // fx:id="cBoxDeviceList"
    private ComboBox<String> cBoxDeviceList; // Value injected by FXMLLoader

    @FXML // fx:id="lblStartDelta"
    private Label lblStartDelta; // Value injected by FXMLLoader

    @FXML
    private Label lblVolt;

    @FXML
    private Label lblCur;

    @FXML
    private Label lblCC1;

    @FXML
    private Label lblCC2;

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

    @FXML
    private RadioButton rbCC1Rp;

    @FXML
    private ToggleGroup tgGroupCC1;

    @FXML
    private RadioButton rbCC1Rd;

    @FXML
    private RadioButton rbCC2Rp;

    @FXML
    private ToggleGroup tgGroupCC2;

    @FXML
    private RadioButton rbCC1Ra;

    @FXML
    private RadioButton rbCC2Ra;

    @FXML
    private RadioButton rbCC2Rd;

    @FXML
    private RadioButton rbCC1None;

    @FXML
    private RadioButton rbCC2None;

    @FXML
    private RadioButton rbCC1Select;

    @FXML
    private ToggleGroup tgGroupCCSelect;

    @FXML
    private RadioButton rbCC2Select;

    @FXML
    private RadioButton rbEnableRpMonitor;

    @FXML
    private TextField txtCCDebounce;

    @FXML
    private Button bSetTerm;

    @FXML
    private Button bClearTerm;

    @FXML
    private RadioButton rbEnablexScope;

    // xScope
    XScope lGraph;

    @FXML
    private LineChart<Number,Number> lchartData;

    @FXML
    private NumberAxis xAxis ;

    @FXML
    private NumberAxis yAxis ;

    @FXML
    private CheckBox chkGraphCC1;

    @FXML
    private CheckBox chkGraphCC2;

    @FXML
    private CheckBox chkGraphVbus;

    @FXML
    private CheckBox chkGraphAmp;

    @FXML
    private ComboBox<String> cboxGraphXScale;

    @FXML
    private Label lblGraphYValue;

    @FXML
    private Label lblGraphXValue;

    @FXML
    private Label lblGraphDeltaY;

    @FXML
    private Label lblGraphDeltaX;

    @FXML
    private Button bGraphScrollLeft;

    @FXML
    private Button bGraphScrollRight;
    // xScope

    private USBControl usbcontrol;
    private Stage myStage;
    private Cordinator cordinator;
    private ObservableList<File> partFileList;
    private int partListIdx = 0;
    private File scopeFile;

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
		bCollapse.setGraphic(new ImageView(new Image("/collapse.png")));
		bCollapse.setTooltip(new Tooltip("Collapse Items"));
		bExpand.setGraphic(new ImageView(new Image("/expand.png")));
		bExpand.setTooltip(new Tooltip("Expand Items"));
		bGraphScrollLeft.setGraphic(new ImageView(new Image("/arrow_left.png")));
		bGraphScrollLeft.setTooltip(new Tooltip("Previous plot"));
		bGraphScrollRight.setGraphic(new ImageView(new Image("/arrow_right.png")));
		bGraphScrollRight.setTooltip(new Tooltip("Next plot"));

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
                    	style += "-fx-text-fill:red;";
                    	break;
                    case GREEN:
                    	style += "-fx-text-fill:green;";
                    	break;
                    case BLUE:
                    	style += "-fx-text-fill:blue;";
                    	break;
                    case YELLOW:
                    	style += "-fx-text-fill:yellow;";
                    	break;
                    case PINK:
                    	style += "-fx-text-fill:pink;";
                    	break;
                    default:
                    	style += "-fx-text-fill:white;"
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

	    tColDataViewIndex.setCellValueFactory(new PropertyValueFactory<DataViewRow,Integer>("index"));
	    tColDataViewValue.setCellValueFactory(new PropertyValueFactory<DataViewRow,String>("value"));

	    cBoxMsgClass.getItems().addAll(PDUtils.MSG_CLASS);
	    cBoxMsgClass.getSelectionModel().select(0);
	    cBoxMsgType.getItems().addAll(PDUtils.CTRL_MSG_TYPE);
	    cBoxMsgType.getSelectionModel().select(1);
	    cBoxSop.getItems().addAll(PDUtils.SOP_TYPE);
	    cBoxSop.getSelectionModel().select(0);

		bFirstPage.setDisable(true);
		bLastPage.setDisable(true);
		bPrevPage.setDisable(true);
		bNextPage.setDisable(true);

		lGraph = new XScope(lchartData, xAxis, yAxis, cboxGraphXScale, bGraphScrollLeft, bGraphScrollRight,
				chkGraphCC1, chkGraphCC2, chkGraphVbus, chkGraphAmp, lblGraphYValue, lblGraphXValue, lblGraphDeltaY,
				lblGraphDeltaX);

	    usbcontrol = new USBControl(cBoxDeviceList, statusBar, lblVolt, lblCur, lblCC1, lblCC2);
	    cordinator = new Cordinator(usbcontrol, tViewMain, tViewData, ttViewParseViewer, lblStartDelta, lGraph);


		Platform.runLater(() -> {
			 handleArgs();
        });
	}

    private void handleArgs() {
		if(Main.arg.length > 0){
			File f = new File(Main.arg[0]);
			if(f.exists()){
				if(Utils.getFileExtension(f).equals("ucx1")){
            		partFileList = OpenRecord.open(f, usbcontrol, statusBar);
            		loadRecord();
            	}
			}
		}
	}

	@FXML
    void openOnDragOver(DragEvent event) {
    	 Dragboard db = event.getDragboard();
         if (db.hasFiles()) {
             event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
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
            		partFileList = OpenRecord.open(file, usbcontrol, statusBar);
            		loadRecord();
            		break;
            	}
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    void filterScopeData(){
    	ObservableList<File> pktList = FXCollections.observableArrayList();
    	if(partFileList != null){
    		for(int i = 0 ; i < partFileList.size(); i++){
    			File xFile = partFileList.get(i);
    			if(Utils.getFileExtension(xFile).contains("part")){
    				pktList.add(xFile);
    			}
    			else{
    				scopeFile = xFile;
    			}
    		}
    		partFileList = pktList;
    	}
    }

    void loadRecord(){
    	tViewData.getItems().clear();
    	ttViewParseViewer.getRoot().getChildren().clear();
    	lGraph.clear();
		bFirstPage.setDisable(true);
		bLastPage.setDisable(true);
		bPrevPage.setDisable(true);
		bNextPage.setDisable(true);
		filterScopeData();
		if(scopeFile != null){
			cordinator.openScopeData(scopeFile);
		}
    	if(partFileList != null){
	    	partListIdx = 0;
	    	cordinator.openPage(partFileList.get(partListIdx));
	    	if(partFileList.size() > 1){
	    		bLastPage.setDisable(false);
	    		bNextPage.setDisable(false);
	    	}
	    	statusBar.setText("First Page-> Page 0 of " + (partFileList.size() - 1) + " pages.");
    	}
    }

    @FXML
    void openRecord(ActionEvent event) {
    	partFileList = OpenRecord.open(bPaneMainWindow.getScene().getWindow(), usbcontrol, statusBar);
    	loadRecord();
    }

    @FXML
    void gotoFirstPage(ActionEvent event) {
    	if(partFileList != null){
    		if(partListIdx != 0){
		    	partListIdx = 0;
		    	cordinator.openPage(partFileList.get(partListIdx));
		    	statusBar.setText("First Page-> Page 0 of " + (partFileList.size() - 1) + " pages.");
	    		bFirstPage.setDisable(true);
	    		bPrevPage.setDisable(true);
	    		bLastPage.setDisable(false);
	    		bNextPage.setDisable(false);
    		}
    	}
    }

    @FXML
    void gotoLastPage(ActionEvent event) {
    	if(partFileList != null){
    		if(partListIdx != partFileList.size() - 1){
		    	partListIdx = partFileList.size() - 1;
		    	cordinator.openPage(partFileList.get(partListIdx));
		    	statusBar.setText("Last Page-> Page "+ partListIdx);
	    		bFirstPage.setDisable(false);
	    		bPrevPage.setDisable(false);
	    		bLastPage.setDisable(true);
	    		bNextPage.setDisable(true);
    		}
    	}
    }

    @FXML
    void gotoNextPage(ActionEvent event) {
    	if(partFileList != null){
	    	partListIdx++;
    		cordinator.openPage(partFileList.get(partListIdx));
    		statusBar.setText("Page: " + partListIdx + " of " + (partFileList.size() - 1) + " pages.");
    		if(partListIdx == partFileList.size()-1){
        		bLastPage.setDisable(true);
        		bNextPage.setDisable(true);
    		}
    		bFirstPage.setDisable(false);
    		bPrevPage.setDisable(false);
    	}
    }

    @FXML
    void gotoPreviousPage(ActionEvent event) {
    	if(partFileList != null){
	    	partListIdx--;
    		statusBar.setText("Page: " + partListIdx + " of " + (partFileList.size() - 1) + " pages.");
    		if(partListIdx == 0){
        		bFirstPage.setDisable(true);
        		bPrevPage.setDisable(true);
    		}
    		bLastPage.setDisable(false);
    		bNextPage.setDisable(false);
	    	cordinator.openPage(partFileList.get(partListIdx));
    	}
    }

    @FXML
    void saveRecord(ActionEvent event) {
    	SaveRecord.save(bPaneMainWindow.getScene().getWindow(), usbcontrol, statusBar);
    }

    Long getStartConfig(){
    	Long config = 0L;
    	if(rbEnableRpMonitor.isSelected()){
    		short debounce = Utils.uint16_get_lsb(Utils.castLongtoUInt(Utils.parseStringtoNumber(txtCCDebounce.getText())));
    		if(debounce == 0){
    			debounce = 3;
    		}
    		debounce = (short) (debounce * 200);
    		config |= 1;
    		if(rbCC2Select.isSelected()){
    			config |= 2;
    		}
    		config |= debounce << 16;
    	}
    	if(rbEnablexScope.isSelected()){
    		config |= 4;
    	}
    	return config;
    }

    @FXML
    void startStopCapture(ActionEvent event) {
    	if(usbcontrol.isHwCapturing() == false){
        	ttViewParseViewer.getRoot().getChildren().clear();
        	lGraph.clear();
        	tViewData.getItems().clear();
    		bFirstPage.setDisable(true);
    		bLastPage.setDisable(true);
    		bPrevPage.setDisable(true);
    		bNextPage.setDisable(true);
    	}
    	else{
//    		bFirstPage.setDisable(false);
//    		bLastPage.setDisable(false);
//    		bPrevPage.setDisable(false);
//    		bNextPage.setDisable(false);
    	}

    	usbcontrol.startStopCapture(getStartConfig());
    	if(usbcontrol.isHwCapturing() == false){
    		cordinator.openScopeLiveData();
    	}
    }

    @FXML
    void resetCapture(ActionEvent event) {
    	tViewData.getItems().clear();
    	ttViewParseViewer.getRoot().getChildren().clear();
    	lGraph.clear();
    	usbcontrol.resetCapture(getStartConfig());
    	tabPaneMain.getSelectionModel().select(0);
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
    void collapseDetailView(ActionEvent event) {
    	DetailsLoader.collapseTreeView(ttViewParseViewer.getRoot());
    }

    @FXML
    void expandDetailView(ActionEvent event) {
    	DetailsLoader.expandTreeView(ttViewParseViewer.getRoot());
    }

	private void displayAboutMe() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = getClass().getResource("/version.properties").openStream();
			prop.load(input);
			String ver = prop.getProperty("MAJOR_VERSION") + "."+ prop.getProperty("MINOR_VERSION") + "." + prop.getProperty("BUILD_NO");
			MsgBox.display("About Me", "USBCx -> USBPD Analyzer\nVersion: "+ ver +"\nAuthor: Tejender Sheoran\nEmail: tejendersheoran@gmail.com, teju@cypress.com\nCopyright(C) (2016-2018) Tejender Sheoran\nThis program is free software. You can redistribute it and/or modify it\nunder the terms of the GNU General Public License Ver 3.\n<http://www.gnu.org/licenses/>");

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

    @FXML
    void setTerminations(ActionEvent event) {
    	byte cc1 = 4;
    	byte cc2 = 4;
    	if(rbCC1Rp.isSelected()){
    		cc1 = 1;
    	}
    	else if(rbCC1Ra.isSelected()){
    		cc1 =2;
    	}
    	else if(rbCC1Rd.isSelected()){
    		cc1 =3;
    	}

    	if(rbCC2Rp.isSelected()){
    		cc2 = 1;
    	}
    	else if(rbCC2Ra.isSelected()){
    		cc2 =2;
    	}
    	else if(rbCC2Rd.isSelected()){
    		cc2 =3;
    	}
    	usbcontrol.setTerm(cc1, cc2);
    }

    @FXML
    void clearTerminations(ActionEvent event) {
    	byte cc1 = 4;
    	byte cc2 = 4;
    	usbcontrol.setTerm(cc1, cc2);
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
			usbcontrol.terminate();
			cordinator.terminate();
			try {
				Thread.sleep(50);
			  } catch (InterruptedException e) {
			 }
			Platform.exit();
			System.exit(0);
	     }
		  });
	}
}




