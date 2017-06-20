package org.ykc.usbcx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;

public class DetailsPresenter {
	public static void updateView(int idx, TreeTableView<DetailsRow> ttViewParseViewer, DataPage curPage ) {
		try {
			ObservableList<DetailsRow> list = FXCollections.observableArrayList();
			byte[] pkt = curPage.getItem(idx);
			Long hdr = PDUtils.get32bitValue(pkt, PktCollecter.HEADER_BYTE0_IDX);
			if(PDUtils.get_field_pkt_type(hdr) == PDUtils.enumPktType.VOLT_PKT){
				ttViewParseViewer.getRoot().getChildren().clear();
				return;
			}
			try {
				PDParser.run(list, pkt);
				} catch (Exception e) {
			}
			DetailsLoader.run(list, ttViewParseViewer);
		} catch (Exception e) {

		}
	}
}
