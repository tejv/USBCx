package org.ykc.usbcx;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import org.ykc.usbcx.DetailsRow.BG;

public class MainWindowController implements Initializable{

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

    @FXML // fx:id="bPopulate"
    private Button bPopulate; // Value injected by FXMLLoader

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

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
			MsgBox.display("About Me", "USBCx -> USBPD Analyzer\nVersion: "+ ver +"\nAuthor: Tejender Sheoran\nEmail: tejendersheoran@gmail.com\nCopyright(C) (2016-2017) Tejender Sheoran\nThis program is free software. You can redistribute it and/or modify it\nunder the terms of the GNU General Public License Ver 3.\n<http://www.gnu.org/licenses/>");

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
}

