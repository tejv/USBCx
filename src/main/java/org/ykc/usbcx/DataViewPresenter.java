package org.ykc.usbcx;

import javafx.scene.control.TableView;

public class DataViewPresenter {
	public static void updateDataView(int idx, TableView<DataViewRow> tViewData, DataPage curPage, XScope lchartData ) {
		try {
			byte[] pkt = curPage.getItem(idx);
			tViewData.getItems().clear();
			lchartData.displaySpecificTimeWindow(Utils.castLongtoUInt(PDUtils.get32bitValue(pkt, PktCollecter.TIME_START_BYTE0_IDX)));
			Long hdr = PDUtils.get32bitValue(pkt, PktCollecter.HEADER_BYTE0_IDX);
			int startIndex = PktCollecter.DATA_BYTE0_IDX;
			if(PDUtils.get_field_extended(hdr)){
				startIndex = PktCollecter.EXTD_DATA_BYTE0_IDX;
			}
			int byteIndex = 0;
			for(int i = startIndex; i < pkt.length; i++, byteIndex++){
				tViewData.getItems().add(new DataViewRow(byteIndex, "0x" + String.format("%02X", Utils.getUnsignedInt(pkt[i]))));
			}
		} catch (Exception e) {

		}
	}

}
