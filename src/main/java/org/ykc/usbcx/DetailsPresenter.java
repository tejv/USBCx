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
			PDParser.run(list, pkt);
			DetailsLoader.run(list, ttViewParseViewer);
		} catch (Exception e) {

		}
	}
}
