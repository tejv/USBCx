package org.ykc.usbcx;

import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.TableView;

public class MainViewPktParser {
	public static final Logger logger = LoggerFactory.getLogger(MainViewPktParser.class.getName());

	public static MainViewRow getRow(byte[] pkt, TableView<MainViewRow> tViewMain){
		MainViewRow row = new MainViewRow();
		logger.info("Pkt length: " + pkt.length);
		/* Start Time */
		Long startTime = PDUtils.get32bitValue(pkt, PktCollecter.TIME_START_BYTE0_IDX);
		row.setStime(Long.toString(startTime));

		/* Delta */
		int mTableRowCount = tViewMain.getItems().size();
		if(mTableRowCount != 0)
		{
			Long endTime;
			try
			{
				endTime = Long.parseLong(tViewMain.getItems().get(mTableRowCount - 1).getEtime());
			}
			catch(NumberFormatException ex)
			{
				endTime = 0L;
			}
			Long delta = startTime - endTime;
			row.setDelta(NumberFormat.getNumberInstance(Locale.US).format(delta));
		}

		/* End Time */
		Long endTime = PDUtils.get32bitValue(pkt, PktCollecter.TIME_END_BYTE0_IDX);
		row.setEtime(Long.toString(endTime));

		/* Duration */
		Long duration = endTime - startTime ;
		row.setDuration(NumberFormat.getNumberInstance(Locale.US).format(duration));

		/* VBus */
		Long vbus =  PDUtils.get32bitValue(pkt, PktCollecter.VBUS_BYTE0_IDX) ;
		vbus = (vbus  * 5000 * 11) / 255;
		row.setVbus(NumberFormat.getNumberInstance(Locale.US).format(vbus));

		/* Sno */
		Long hdr = PDUtils.get32bitValue(pkt, PktCollecter.HEADER_BYTE0_IDX);
		Long sno = PDUtils.get32bitValue(pkt, PktCollecter.SNO_BYTE0_IDX);
		int sno_int = (int)(sno & 0xFFFFFFFF);
		if(PDUtils.get_field_pkt_type(hdr) == PDUtils.enumPktType.VOLT_PKT)
		{
			PDUtils.enumVoltPktEvt pevt =  PDUtils.enumVoltPktEvt.values()[sno_int];
			row.setOk(pevt.name());
			row.setDuration("");
			return row;
		}
		row.setSno(Long.toString(sno));

		/* Ok */
		String ok = "Ok";
		if(PDUtils.get_field_ok(hdr) == false)
		{
			ok = "ER";
			if(PDUtils.get_field_crc_error(hdr) == true)
			{
				ok = ok + "_CRC";
			}
			if(PDUtils.get_field_eop_error(hdr) == true)
			{
				ok = ok + "_EOP";
			}
		}
		row.setOk(ok);

		/* SOP */
		PDUtils.enumSOPType sopType =  PDUtils.get_field_sop_type(hdr);
		row.setSop(sopType.name());

		if((sopType == PDUtils.enumSOPType.HARD_RESET) || (sopType == PDUtils.enumSOPType.CABLE_RESET))
		{
			return row;
		}

		/* TODO: Data */
		String data = "0x" + Long.toHexString(PDUtils.get16bitValue(pkt, PktCollecter.HEADER_BYTE0_IDX)).toUpperCase();

		if(PDUtils.get_field_extended(hdr)){
			int extdHdr = Utils.get_uint16(pkt[PktCollecter.EXTD_HEADER_BYTE0_IDX], pkt[PktCollecter.EXTD_HEADER_BYTE0_IDX + 1]);
			Integer byteCount = PDUtils.get_field_extended_count(extdHdr);
			data += " 0x" + Long.toHexString(PDUtils.get16bitValue(pkt, PktCollecter.EXTD_HEADER_BYTE0_IDX)).toUpperCase();
			data += " " + byteCount.toString() + " bytes";
			if(PDUtils.get_field_is_chunked(extdHdr)){
				data += " Chunk " + PDUtils.get_field_extended_chunk_no(extdHdr);
				if(PDUtils.get_field_is_request_chunk(extdHdr)){
					data += " req";
				}
				else{
					data += " resp";
				}
			}
			else{
				data += " Unchunked";
			}
		}
		else{
			int count = PDUtils.get_field_msg_count(hdr);
			for(int i = 0; i < count; i++){
				data += " 0x" + Long.toHexString(PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i *4)).toUpperCase();
			}
		}

		row.setData(data);

		/* Message */
		row.setMsg(PDUtils.get_field_msg_type(hdr));

		/* Message ID */
		row.setId(Integer.toString(PDUtils.get_field_msg_id(hdr)));

		/* Data Role */
		row.setDrole(PDUtils.get_field_data_role(hdr));

		/* Power role */
		row.setProle(PDUtils.get_field_power_role(hdr));

		/* Count */
		row.setCount(Integer.toString((PDUtils.get_field_msg_count(hdr))));

		/* Rev */
		row.setRev(PDUtils.get_field_spec_rev(hdr).toString());

		return row;
	}
}
