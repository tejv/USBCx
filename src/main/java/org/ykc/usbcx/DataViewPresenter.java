package org.ykc.usbcx;

import javafx.scene.control.TableView;

public class DataViewPresenter {
	public static void updateDataView(int idx, TableView<DataViewRow> tViewData, DataPage curPage ) {
		try {
			byte[] pkt = curPage.getItem(idx);
			tViewData.getItems().clear();
			Long hdr = get32bitValue(pkt, PktCollecter.HEADER_BYTE0_IDX);
			int startIndex = PktCollecter.DATA_BYTE0_IDX;
			if(PDUtils.get_field_extended(hdr)){
				startIndex = PktCollecter.EXTD_DATA_BYTE0_IDX;
			}
			int byteIndex = 0;
			for(int i = startIndex; i < pkt.length; i++, byteIndex++){
				tViewData.getItems().add(new DataViewRow(byteIndex, "0x" + Long.toHexString(Utils.getUnsignedInt(pkt[i]))));
			}
		} catch (Exception e) {

		}
	}
	
	private static Long get32bitValue(byte[] pkt, int idx){
		return Utils.getUnsignedInt(Utils.get_uint32(pkt[idx], pkt[idx + 1], pkt[idx + 2], pkt[idx + 3]));
	}	
}
