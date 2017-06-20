package org.ykc.usbcx;

import java.io.UnsupportedEncodingException;

import javax.sound.sampled.ReverbType;

import org.apache.commons.lang3.StringUtils;
import org.ykc.usbcx.DetailsRow.BG;
import org.ykc.usbcx.PDUtils.enumAMAUSBSSSupport;
import org.ykc.usbcx.PDUtils.enumAMAVCONNPower;
import org.ykc.usbcx.PDUtils.enumAPDOType;
import org.ykc.usbcx.PDUtils.enumBattChargingStatus;
import org.ykc.usbcx.PDUtils.enumBistMode;
import org.ykc.usbcx.PDUtils.enumCableActiveTermType;
import org.ykc.usbcx.PDUtils.enumCableLatencyActive;
import org.ykc.usbcx.PDUtils.enumCableLatencyPassive;
import org.ykc.usbcx.PDUtils.enumCableMaxVolt;
import org.ykc.usbcx.PDUtils.enumCablePassiveTermType;
import org.ykc.usbcx.PDUtils.enumCablePlug;
import org.ykc.usbcx.PDUtils.enumCableSSSupport;
import org.ykc.usbcx.PDUtils.enumCableTCPlugToCaptiveRev2;
import org.ykc.usbcx.PDUtils.enumCableTCPlugToCaptiveRev3;
import org.ykc.usbcx.PDUtils.enumCableVbusCur;
import org.ykc.usbcx.PDUtils.enumDPConfSignalling;
import org.ykc.usbcx.PDUtils.enumDPConfigureSelect;
import org.ykc.usbcx.PDUtils.enumDPDiscModePortCap;
import org.ykc.usbcx.PDUtils.enumDPDiscModeReceptacle;
import org.ykc.usbcx.PDUtils.enumDPStatusConn;
import org.ykc.usbcx.PDUtils.enumDPStatusHPD;
import org.ykc.usbcx.PDUtils.enumDPStatusIRQ;
import org.ykc.usbcx.PDUtils.enumExtdSrcCapIOC;
import org.ykc.usbcx.PDUtils.enumExtdSrcCapLoadStep;
import org.ykc.usbcx.PDUtils.enumIDHdrProductTypeCable;
import org.ykc.usbcx.PDUtils.enumIDHdrProductTypeDFP;
import org.ykc.usbcx.PDUtils.enumIDHdrProductTypeUFP;
import org.ykc.usbcx.PDUtils.enumPPSStatusOMF;
import org.ykc.usbcx.PDUtils.enumPktType;
import org.ykc.usbcx.PDUtils.enumPortDataRole;
import org.ykc.usbcx.PDUtils.enumPortPowerRole;
import org.ykc.usbcx.PDUtils.enumSOPType;
import org.ykc.usbcx.PDUtils.enumSVDMCmd;
import org.ykc.usbcx.PDUtils.enumSVDMCmdType;
import org.ykc.usbcx.PDUtils.enumSVDMVersion;
import org.ykc.usbcx.PDUtils.enumSinkFRRequiremnent;
import org.ykc.usbcx.PDUtils.enumSourcePeakCurrent;
import org.ykc.usbcx.PDUtils.enumSpecRev;
import org.ykc.usbcx.PDUtils.enumStatusDCAC;
import org.ykc.usbcx.PDUtils.enumStatusTemperature;
import org.ykc.usbcx.PDUtils.enumSupplyType;
import org.ykc.usbcx.PDUtils.enumVDMType;
import org.ykc.usbcx.PDUtils.enumYesNo;

import javafx.collections.ObservableList;

public class PDParser {
	public static void run(ObservableList<DetailsRow> list, byte[] pkt){
		Long usbcxhdr = PDUtils.get32bitValue(pkt, PktCollecter.HEADER_BYTE0_IDX);
		enumSOPType sopType = PDUtils.get_field_sop_type(usbcxhdr);
		if((sopType.equals(enumSOPType.HARD_RESET)) ||
				(sopType.equals(enumSOPType.CABLE_RESET)) ||
				(sopType.equals(enumSOPType.INVALID)) ){
			return;
		}
		/* Header processing */
		Long hdr = PDUtils.get16bitValue(pkt, PktCollecter.HEADER_BYTE0_IDX);
		int rev = (int)(((hdr) & 0x000000c0) >> 6);
		int count = (int)(((hdr) & 0x00007000) >> 12);
		int msg = (int)(((hdr) & 0x0000000f) >> 0);
		boolean isExtended = PDUtils.get_field_extended(hdr);

		if(sopType.equals(enumSOPType.SOP)){
			if(rev <= 1){
				addMSG_HDR_REV2_SOP(list, hdr);
				if(isExtended == true){
					return;
				}
			}
			else{
				addMSG_HDR_REV3_SOP(list, hdr, isExtended);
			}
		}
		else{
			if(rev <= 1){
				addMSG_HDR_REV2_SOP_PRIME(list, hdr);
				if(isExtended == true){
					return;
				}
			}
			else{
				addMSG_HDR_REV3_SOP_PRIME(list, hdr, isExtended);
			}
		}

		/* Extended header processing */
		if(PDUtils.get_field_extended(hdr) == true){
			addEXTENDED_MSG_HDR(list, PDUtils.get16bitValue(pkt, PktCollecter.EXTD_HEADER_BYTE0_IDX));
			/* Extended message processing */
			try {
				switch(msg){
				case 1:
					processSourceCapExtended(list, pkt);
					break;
				case 2:
					processStatus(list, pkt);
					break;
				case 3:
					processGetBatteryCap(list, pkt);
				case 4:
					processGetBatteryStatus(list, pkt);
					break;
				case 5:
					processBatteryCaps(list, pkt);
					break;
				case 6:
					processGetManuInfo(list, pkt);
					break;
				case 7:
					processManuInfo(list, pkt);
					break;
				case 12:
					processPPSStatus(list, pkt);
					break;
				case 13:
					processCountryInfo(list, pkt);
					break;
				case 14:
					processCountryCodes(list, pkt);
					break;
				default:
					break;
				}
			} catch (Exception e) {

			}
		}
		else if(count != 0){
			/* Data message processing */
			switch(msg){
			case 1:
				processSourceCap(list, pkt, count, rev);
				break;
			case 2:
				processRequest(list, rev, PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX));
				break;
			case 3:
				processBist(list, pkt, count);
			case 4:
				processSinkCap(list, pkt, count, rev);
				break;
			case 5:
				addBATTERY_STATUS(list, PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX));
				break;
			case 6:
				addALERT_ADO(list,PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX));
				break;
			case 7:
				addGET_COUNTRY_INFO(list,PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX));
				break;
			case 15:
				processVDM(list, pkt, count, sopType, rev);
				break;
			default:
				processGenericDataMessage(list, pkt, count);
				break;
			}
		}
	}

	private static void addMSG_HDR_REV2_SOP(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("MSG_HDR_REV2_SOP").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        int valInt;
        String valString;
        val = ((input) & 0x00008000) >> 15;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        int valCount = (int)(((input) & 0x00007000) >> 12);
        valString = Long.toString(valCount);
        list.add(new DetailsRow.Builder().name("data_objects").value(valString).level(1).build());
        val = ((input) & 0x00000e00) >> 9;
        valString = Long.toString(val);
        list.add(new DetailsRow.Builder().name("msg_id").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000100) >> 8);
        valString = enumPortPowerRole.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("power_role").value(valString).level(1).build());
        valInt = (int)(((input) & 0x000000c0) >> 6);
        valString = enumSpecRev.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("spec_rev").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000020) >> 5);
        valString = enumPortDataRole.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("data_role").value(valString).level(1).build());
        val = ((input) & 0x00000010) >> 4;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        int msg = (int)(((input) & 0x0000000f) >> 0);
        if(valCount == 0){
        	valString = PDUtils.CTRL_MSG_TYPE[msg];
        }
        else{
        	valString = PDUtils.DATA_MSG_TYPE[msg];
        }
        list.add(new DetailsRow.Builder().name("msg_type").value(valString).level(1).build());
    }

    private static void addMSG_HDR_REV2_SOP_PRIME(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("MSG_HDR_REV2_SOP_PRIME").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        int valInt;
        String valString;
        val = ((input) & 0x00008000) >> 15;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        int valCount = (int)(((input) & 0x00007000) >> 12);
        valString = Long.toString(valCount);
        list.add(new DetailsRow.Builder().name("data_objects").value(valString).level(1).build());
        val = ((input) & 0x00000e00) >> 9;
        valString = Long.toString(val);
        list.add(new DetailsRow.Builder().name("msg_id").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000100) >> 8);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_plug").value(valString).level(1).build());
        valInt = (int)(((input) & 0x000000c0) >> 6);
        valString = enumSpecRev.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("spec_rev").value(valString).level(1).build());
        val = ((input) & 0x00000030) >> 4;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        int msg = (int)(((input) & 0x0000000f) >> 0);
        if(valCount == 0){
        	valString = PDUtils.CTRL_MSG_TYPE[msg];
        }
        else{
        	valString = PDUtils.DATA_MSG_TYPE[msg];
        }
        list.add(new DetailsRow.Builder().name("msg_type").value(valString).level(1).build());
    }

    private static void addMSG_HDR_REV3_SOP(ObservableList<DetailsRow> list, Long input, boolean isExtended) {
        list.add(new DetailsRow.Builder().name("MSG_HDR_REV3_SOP").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        int valInt;
        String valString;
        valInt = (int)(((input) & 0x00008000) >> 15);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("extended").value(valString).level(1).build());
        int valCount = (int)(((input) & 0x00007000) >> 12);
        valString = Long.toString(valCount);
        list.add(new DetailsRow.Builder().name("data_objects").value(valString).level(1).build());
        val = ((input) & 0x00000e00) >> 9;
        valString = Long.toString(val);
        list.add(new DetailsRow.Builder().name("msg_id").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000100) >> 8);
        valString = enumPortPowerRole.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("power_role").value(valString).level(1).build());
        valInt = (int)(((input) & 0x000000c0) >> 6);
        valString = enumSpecRev.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("spec_rev").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000020) >> 5);
        valString = enumPortDataRole.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("data_role").value(valString).level(1).build());
        int msg = (int)(((input) & 0x0000001f) >> 0);
        if(isExtended){
        	valString = PDUtils.EXTD_MSG_TYPE[msg];
        }
        else{
	        if(valCount == 0){
	        	valString = PDUtils.CTRL_MSG_TYPE[msg];
	        }
	        else{
	        	valString = PDUtils.DATA_MSG_TYPE[msg];
	        }
        }
        list.add(new DetailsRow.Builder().name("msg_type").value(valString).level(1).build());
    }

    private static void addMSG_HDR_REV3_SOP_PRIME(ObservableList<DetailsRow> list, Long input, boolean isExtended) {
        list.add(new DetailsRow.Builder().name("MSG_HDR_REV3_SOP_PRIME").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        int valInt;
        String valString;
        valInt = (int)(((input) & 0x00008000) >> 15);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("extended").value(valString).level(1).build());
        int valCount = (int)(((input) & 0x00007000) >> 12);
        valString = Long.toString(valCount);
        list.add(new DetailsRow.Builder().name("data_objects").value(valString).level(1).build());
        val = ((input) & 0x00000e00) >> 9;
        valString = Long.toString(val);
        list.add(new DetailsRow.Builder().name("msg_id").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000100) >> 8);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_plug").value(valString).level(1).build());
        valInt = (int)(((input) & 0x000000c0) >> 6);
        valString = enumSpecRev.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("spec_rev").value(valString).level(1).build());
        val = ((input) & 0x00000020) >> 5;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        int msg = (int)(((input) & 0x0000001f) >> 0);
        if(isExtended){
        	valString = PDUtils.EXTD_MSG_TYPE[msg];
        }
        else{
	        if(valCount == 0){
	        	valString = PDUtils.CTRL_MSG_TYPE[msg];
	        }
	        else{
	        	valString = PDUtils.DATA_MSG_TYPE[msg];
	        }
        }
        list.add(new DetailsRow.Builder().name("msg_type").value(valString).level(1).build());
    }

    private static void addEXTENDED_MSG_HDR(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("EXTENDED_MSG_HDR").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0x00008000) >> 15);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("chunked").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00007800) >> 11);
        valString = valInt.toString();
        list.add(new DetailsRow.Builder().name("chunk_number").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000400) >> 10);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("request_chunk").value(valString).level(1).build());
        val = ((input) & 0x00000200) >> 9;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x000001ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("data_size").value(valString).level(1).build());
    }

	private static void processGenericDataMessage(ObservableList<DetailsRow> list, byte[] pkt, int count) {
		for(int i = 0; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}

	}

	private static void processBist(ObservableList<DetailsRow> list, byte[] pkt, int count) {
		addBIST_OBJ1(list, PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX ));
		for(int i = 1; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("BIST_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}

	}

    private static void addBIST_OBJ1(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("BIST_OBJ1").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xf0000000) >> 28);
        valString = enumBistMode.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("bist_mode").value(valString).level(1).build());
        val = ((input) & 0x0fffffff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("reserved").value(valString).level(1).build());
    }

    private static void processSourceCap(ObservableList<DetailsRow> list, byte[] pkt, int count, int rev) {
		for(int i = 0; i < count; i++){
			processSourcePDO(list, PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4), i, rev);
		}
	}

	private static void processSourcePDO(ObservableList<DetailsRow> list, Long pdo, int index, int rev) {
		int supply_type = (int)(((pdo) & 0xc0000000) >> 30);
		switch (supply_type) {
		case 0:
			if(index == 0){
				if(rev <= 1){
					addFIXED_5V_SRC_PDO_REV2(list, pdo);
				}
				else{
					addFIXED_5V_SRC_PDO_REV3(list, pdo);
				}
			}
			else{
				addFIXED_SRC_PDO(list, pdo);
			}
			break;
		case 1:
			addBATTERY_SRC_PDO(list, pdo);
			break;
		case 2:
			addVARIABLE_SRC_PDO(list, pdo);
			break;
		default:
			addPPS_SRC_APDO(list, pdo);
			break;
		}
	}

    private static void addFIXED_5V_SRC_PDO_REV2(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("FIXED_5V_SRC_PDO_REV2").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        valInt = (int)(((input) & 0x20000000) >> 29);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("dual_role_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x10000000) >> 28);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_suspend_support").value(valString).level(1).build());
        valInt = (int)(((input) & 0x08000000) >> 27);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("unconstrained_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_comm_capable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("dual_role_data").value(valString).level(1).build());
        val = ((input) & 0x01c00000) >> 22;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00300000) >> 20);
        valString = enumSourcePeakCurrent.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("peak_cur").value(valString).level(1).build());
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("max_cur_10ma").value(valString).level(1).build());
    }

    private static void addFIXED_5V_SRC_PDO_REV3(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("FIXED_5V_SRC_PDO_REV3").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        valInt = (int)(((input) & 0x20000000) >> 29);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("dual_role_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x10000000) >> 28);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_suspend_support").value(valString).level(1).build());
        valInt = (int)(((input) & 0x08000000) >> 27);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("unconstrained_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_comm_capable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("dual_role_data").value(valString).level(1).build());
        valInt = (int)(((input) & 0x01000000) >> 24);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("unchunked_support").value(valString).level(1).build());
        val = ((input) & 0x00c00000) >> 22;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00300000) >> 20);
        valString = enumSourcePeakCurrent.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("peak_cur").value(valString).level(1).build());
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("max_cur_10ma").value(valString).level(1).build());
    }

    private static void addFIXED_SRC_PDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("FIXED_SRC_PDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        val = ((input) & 0x3fc00000) >> 22;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00300000) >> 20);
        valString = enumSourcePeakCurrent.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("peak_cur").value(valString).level(1).build());
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("max_cur_10ma").value(valString).level(1).build());
    }

    private static void addVARIABLE_SRC_PDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("VARIABLE_SRC_PDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        val = ((input) & 0x3ff00000) >> 20;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("max_volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("min_volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("max_cur_10ma").value(valString).level(1).build());
    }

    private static void addBATTERY_SRC_PDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("BATTERY_SRC_PDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        val = ((input) & 0x3ff00000) >> 20;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("max_volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("min_volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 250) + " mW)";
        list.add(new DetailsRow.Builder().name("max_power_250mw").value(valString).level(1).build());
    }

    private static void addPPS_SRC_APDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("PPS_SRC_APDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        valInt = (int)(((input) & 0x30000000) >> 28);
        valString = enumAPDOType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("apdo_type").value(valString).level(1).build());
        val = ((input) & 0x0e000000) >> 25;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x01fe0000) >> 17;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 100) + " mV)";
        list.add(new DetailsRow.Builder().name("max_volt_100mv").value(valString).level(1).build());
        val = ((input) & 0x00010000) >> 16;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x0000ff00) >> 8;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 100) + " mV)";
        list.add(new DetailsRow.Builder().name("min_volt_100mv").value(valString).level(1).build());
        val = ((input) & 0x00000080) >> 7;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x0000007f) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mA)";
        list.add(new DetailsRow.Builder().name("max_cur_50ma").value(valString).level(1).build());
    }

	private static void processSinkCap(ObservableList<DetailsRow> list, byte[] pkt, int count, int rev) {
		for(int i = 0; i < count; i++){
			processSinkPDO(list, PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4), i, rev);
		}
	}

	private static void processSinkPDO(ObservableList<DetailsRow> list, Long pdo, int index, int rev) {
		int supply_type = (int)(((pdo) & 0xc0000000) >> 30);
		switch (supply_type) {
		case 0:
			if(index == 0){
				if(rev <= 1){
					addFIXED_5V_SINK_PDO_REV2(list, pdo);
				}
				else{
					addFIXED_5V_SINK_PDO_REV3(list, pdo);
				}
			}
			else{
				addFIXED_SINK_PDO(list, pdo);
			}
			break;
		case 1:
			addBATTERY_SINK_PDO(list, pdo);
			break;
		case 2:
			addVARIABLE_SINK_PDO(list, pdo);
			break;
		default:
			addPPS_SINK_APDO(list, pdo);
			break;
		}
	}

    private static void addFIXED_5V_SINK_PDO_REV2(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("FIXED_5V_SINK_PDO_REV2").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        valInt = (int)(((input) & 0x20000000) >> 29);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("dual_role_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x10000000) >> 28);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("higher_capability").value(valString).level(1).build());
        valInt = (int)(((input) & 0x08000000) >> 27);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("unconstrained_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_comm_cap").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("dual_role_data").value(valString).level(1).build());
        val = ((input) & 0x01f00000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("max_cur_10ma").value(valString).level(1).build());
    }

    private static void addFIXED_5V_SINK_PDO_REV3(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("FIXED_5V_SINK_PDO_REV3").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        valInt = (int)(((input) & 0x20000000) >> 29);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("dual_role_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x10000000) >> 28);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("higher_capability").value(valString).level(1).build());
        valInt = (int)(((input) & 0x08000000) >> 27);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("unconstrained_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_comm_cap").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("dual_role_data").value(valString).level(1).build());
        valInt = (int)(((input) & 0x01800000) >> 23);
        valString = enumSinkFRRequiremnent.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("fr_swap_req").value(valString).level(1).build());
        val = ((input) & 0x00700000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("max_cur_10ma").value(valString).level(1).build());
    }

    private static void addFIXED_SINK_PDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("FIXED_SINK_PDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        val = ((input) & 0x3ff00000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("max_cur_10ma").value(valString).level(1).build());
    }

    private static void addBATTERY_SINK_PDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("BATTERY_SINK_PDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        val = ((input) & 0x3ff00000) >> 20;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("max_volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("min_volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 250) + " mW)";
        list.add(new DetailsRow.Builder().name("op_power_250mw").value(valString).level(1).build());
    }

    private static void addVARIABLE_SINK_PDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("VARIABLE_SINK_PDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        val = ((input) & 0x3ff00000) >> 20;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("max_volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mV)";
        list.add(new DetailsRow.Builder().name("min_volt_50mv").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("op_cur_10ma").value(valString).level(1).build());
    }

    private static void addPPS_SINK_APDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("PPS_SINK_APDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xc0000000) >> 30);
        valString = enumSupplyType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("supply").value(valString).level(1).build());
        valInt = (int)(((input) & 0x30000000) >> 28);
        valString = enumAPDOType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("apdo_type").value(valString).level(1).build());
        val = ((input) & 0x0e000000) >> 25;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x01fe0000) >> 17;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 100) + " mV)";
        list.add(new DetailsRow.Builder().name("max_volt_100mv").value(valString).level(1).build());
        val = ((input) & 0x00010000) >> 16;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x0000ff00) >> 8;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 100) + " mV)";
        list.add(new DetailsRow.Builder().name("min_volt_100mv").value(valString).level(1).build());
        val = ((input) & 0x00000080) >> 7;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x0000007f) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mA)";
        list.add(new DetailsRow.Builder().name("max_cur_50ma").value(valString).level(1).build());
    }

	private static void processRequest(ObservableList<DetailsRow> list, int rev, Long input) {
		int index = (int)(((input) & 0x70000000) >> 28);
		int supply_type = MainViewPktParser.lastSupplyType[index];
		switch (supply_type) {
		case 0:
		case 2:
			if(rev <= 1){
				addFIXED_VAR_RDO_REV2(list, input);
			}
			else{
				addFIXED_VAR_RDO_REV3(list, input);
			}
			break;
		case 1:
			if(rev <= 1){
				addBATTERY_RDO_REV2(list, input);
			}
			else{
				addBATTERY_RDO_REV3(list, input);
			}
			break;
		default:
			addPPS_RDO(list, input);
			break;
		}
	}

    private static void addFIXED_VAR_RDO_REV2(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("FIXED_VAR_RDO_REV2").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0x80000000) >> 31;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x70000000) >> 28);
        valString = valInt.toString();
        list.add(new DetailsRow.Builder().name("object_pos").value(valString).level(1).build());
        int gvBack = (int)(((input) & 0x08000000) >> 27);
        valString = enumYesNo.values()[gvBack].name();
        list.add(new DetailsRow.Builder().name("giveback_flag").value(valString).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("capability_mismatch").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_comm_capable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x01000000) >> 24);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("no_usb_suspend").value(valString).level(1).build());
        val = ((input) & 0x00f00000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("op_current_10ma").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        if(gvBack == 0){
        	list.add(new DetailsRow.Builder().name("max_op_cur_10ma").value(valString).level(1).build());
        }
        else{
        	list.add(new DetailsRow.Builder().name("min_op_cur_10ma").value(valString).level(1).build());
        }
    }

    private static void addFIXED_VAR_RDO_REV3(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("FIXED_VAR_RDO_REV3").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0x80000000) >> 31;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x70000000) >> 28);
        valString = valInt.toString();
        list.add(new DetailsRow.Builder().name("object_pos").value(valString).level(1).build());
        int gvBack = (int)(((input) & 0x08000000) >> 27);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("giveback_flag").value(valString).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("capability_mismatch").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_comm_capable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x01000000) >> 24);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("no_usb_suspend").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00800000) >> 23);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("unchunked_support").value(valString).level(1).build());
        val = ((input) & 0x00700000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        list.add(new DetailsRow.Builder().name("op_current_10ma").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " mA)";
        if(gvBack == 0){
        	list.add(new DetailsRow.Builder().name("max_op_cur_10ma").value(valString).level(1).build());
        }
        else{
        	list.add(new DetailsRow.Builder().name("min_op_cur_10ma").value(valString).level(1).build());
        }
    }

    private static void addBATTERY_RDO_REV2(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("BATTERY_RDO_REV2").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0x80000000) >> 31;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x70000000) >> 28);
        valString = valInt.toString();
        list.add(new DetailsRow.Builder().name("object_pos").value(valString).level(1).build());
        int gvBack = (int)(((input) & 0x08000000) >> 27);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("giveback_flag").value(valString).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("capability_mismatch").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_comm_capable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x01000000) >> 24);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("no_usb_suspend").value(valString).level(1).build());
        val = ((input) & 0x00f00000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 250) + " mW)";
        list.add(new DetailsRow.Builder().name("op_power_250mw").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 250) + " mW)";
        if(gvBack == 0){
        	list.add(new DetailsRow.Builder().name("max_op_power_250mw").value(valString).level(1).build());
        }
        else{
        	list.add(new DetailsRow.Builder().name("min_op_power_250mw").value(valString).level(1).build());
        }
    }

    private static void addBATTERY_RDO_REV3(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("BATTERY_RDO_REV3").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0x80000000) >> 31;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x70000000) >> 28);
        valString = valInt.toString();
        list.add(new DetailsRow.Builder().name("object_pos").value(valString).level(1).build());
        int gvBack = (int)(((input) & 0x08000000) >> 27);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("giveback_flag").value(valString).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("capability_mismatch").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_comm_capable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x01000000) >> 24);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("no_usb_suspend").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00800000) >> 23);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("unchunked_support").value(valString).level(1).build());
        val = ((input) & 0x00700000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x000ffc00) >> 10;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 250) + " mW)";
        list.add(new DetailsRow.Builder().name("op_power_250mw").value(valString).level(1).build());
        val = ((input) & 0x000003ff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 250) + " mW)";
        if(gvBack == 0){
        	list.add(new DetailsRow.Builder().name("max_op_power_250mw").value(valString).level(1).build());
        }
        else{
        	list.add(new DetailsRow.Builder().name("min_op_power_250mw").value(valString).level(1).build());
        }
    }

    private static void addPPS_RDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("PPS_RDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0x80000000) >> 31;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x70000000) >> 28);
        valString = valInt.toString();
        list.add(new DetailsRow.Builder().name("object_pos").value(valString).level(1).build());
        val = ((input) & 0x08000000) >> 27;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("capability_mismatch").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_comm_capable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x01000000) >> 24);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("no_usb_suspend").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00800000) >> 23);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("unchunked_supported").value(valString).level(1).build());
        val = ((input) & 0x00700000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x000ffe00) >> 9;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 20) + " mV)";
        list.add(new DetailsRow.Builder().name("output_volt_20mv").value(valString).level(1).build());
        val = ((input) & 0x00000180) >> 7;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x0000007f) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 50) + " mA)";
        list.add(new DetailsRow.Builder().name("op_cur_50ma").value(valString).level(1).build());
    }

    private static void addBATTERY_STATUS(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("BATTERY_STATUS").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0xffff0000) >> 16;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " WH)";
        list.add(new DetailsRow.Builder().name("batt_present_capacity").value(valString).level(1).build());
        val = ((input) & 0x0000f000) >> 12;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        int isBattPresent = (int)(((input) & 0x00000200) >> 9);
        valInt = (int)(((input) & 0x00000c00) >> 10);
        if(isBattPresent == 0){
        	valString = "0x"+ Integer.toHexString(valInt);
            if(valInt == 0){
                list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
            }
            else{
                list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
            }
        }
        else{
	        valString = enumBattChargingStatus.values()[valInt].name();
	        list.add(new DetailsRow.Builder().name("batt_charging_status").value(valString).level(1).build());
        }

        valString = enumYesNo.values()[isBattPresent].name();
        list.add(new DetailsRow.Builder().name("batt_present").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000100) >> 8);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("invalid_batt_ref").value(valString).level(1).build());
        val = ((input) & 0x000000ff) >> 0;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
    }

    private static void addALERT_ADO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("ALERT_ADO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0x80000000) >> 31;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x40000000) >> 30);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ovp").value(valString).level(1).build());
        valInt = (int)(((input) & 0x20000000) >> 29);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("src_input_change").value(valString).level(1).build());
        valInt = (int)(((input) & 0x10000000) >> 28);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("op_cond_change").value(valString).level(1).build());
        valInt = (int)(((input) & 0x08000000) >> 27);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("otp").value(valString).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ocp").value(valString).level(1).build());
        valInt = (int)(((input) & 0x02000000) >> 25);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("batt_status_change").value(valString).level(1).build());
        val = ((input) & 0x01000000) >> 24;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00f00000) >> 20);
        valString = "0b" + StringUtils.leftPad(Integer.toBinaryString(valInt), 4, '0');
        list.add(new DetailsRow.Builder().name("fixed_bats").value(valString).level(1).build());
        valInt = (int)(((input) & 0x000f0000) >> 16);
        valString = "0b" + StringUtils.leftPad(Integer.toBinaryString(valInt), 4, '0');
        list.add(new DetailsRow.Builder().name("hot_swap_bats").value(valString).level(1).build());
        val = ((input) & 0x0000ffff) >> 0;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
    }

    private static void addGET_COUNTRY_INFO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("GET_COUNTRY_INFO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0xff000000) >> 24;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("first_char_alpha").value(valString).level(1).build());
        val = ((input) & 0x00ff0000) >> 16;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("second_char_alpha").value(valString).level(1).build());
        val = ((input) & 0x0000ffff) >> 0;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
    }

	private static void processCountryCodes(ObservableList<DetailsRow> list, byte[] pkt) {
		Long val = PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX);
		String valString =  val.toString();
		list.add(new DetailsRow.Builder().name("Length").value(valString).level(0).build());

		for(int i = PktCollecter.EXTD_DATA_BYTE0_IDX + 2; i < pkt.length; i = i + 2){
		    int length = 2;
			byte[] newArray = new byte[2];
			System.arraycopy(pkt, i, newArray, 0, length);
			try {
				list.add(new DetailsRow.Builder().name("COUNTRY_CODE_" + Integer.toString(i + 1)).value(new String(newArray, "UTF-8")).level(0).build());
			} catch (UnsupportedEncodingException e) {
			}
		}
	}

	private static void processCountryInfo(ObservableList<DetailsRow> list, byte[] pkt) {
	    int length = 2;
		byte[] newArray = new byte[2];
		System.arraycopy(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX, newArray, 0, length);
	    try {
			list.add(new DetailsRow.Builder().name("COUNTRY_CODE").value(new String(newArray, "UTF-8")).level(0).build());
		} catch (UnsupportedEncodingException e) {
		}

		Long val = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 2);
		String valString;
		if(val == 0){
			list.add(new DetailsRow.Builder().name("RSVD").value("0").level(0).build());
		}
		else{
			valString = val.toString();
			list.add(new DetailsRow.Builder().name("RSVD").value(valString).level(0).bcolor(BG.RED).build());
		}
		list.add(new DetailsRow.Builder().name("COUNTRY_SPECIFIC_DATA").value("Offset = 4").level(0).build());
	}

	private static void processPPSStatus(ObservableList<DetailsRow> list, byte[] pkt) {
		Long input;
		Integer valInt;
		Long val;
	    String valString;
	    input = PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX);
	    if(input == 0xFFFF){
	    	valString = "Not Supported";
	    }
	    else{
	    	valString = "0x" + Long.toHexString(input) + " (" + Long.toString(input * 20) + " mV)";
	    }
	    list.add(new DetailsRow.Builder().name("OUTPUT_VOLTAGE").value(valString).level(0).build());

	    input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 2);
	    if(input == 0xFF){
	    	valString = "Not Supported";
	    }
	    else{
	    	valString = "0x" + Long.toHexString(input) + " (" + Long.toString(input * 50) + " mA)";
	    }
	    list.add(new DetailsRow.Builder().name("OUTPUT_CURRENT").value(valString).level(0).build());

	    input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 3);
        list.add(new DetailsRow.Builder().name("REAL_TIME_FLAGS").value("0x" + Long.toHexString(input)).level(0).build());
        val = ((input) & 0x000000f0) >> 4;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000008) >> 3);
        valString = enumPPSStatusOMF.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("operating_mode").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000006) >> 1);
        valString = enumStatusTemperature.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("present_temperature").value(valString).level(1).build());
        val = ((input) & 0x00000001) >> 0;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
	}

	private static void processManuInfo(ObservableList<DetailsRow> list, byte[] pkt) {
	    list.add(new DetailsRow.Builder().name("VID").value("0x" + Long.toHexString(PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX))).level(0).build());
	    list.add(new DetailsRow.Builder().name("PID").value("0x" + Long.toHexString(PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 2))).level(0).build());

	    int length = pkt.length - (PktCollecter.EXTD_DATA_BYTE0_IDX + 4);
		byte[] newArray = new byte[30];
		System.arraycopy(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 4, newArray, 0, length);
	    try {
			list.add(new DetailsRow.Builder().name("MANU_STRING").value(new String(newArray, "UTF-8")).level(0).build());
		} catch (UnsupportedEncodingException e) {
		}

	}

	private static void processGetManuInfo(ObservableList<DetailsRow> list, byte[] pkt) {
		Long infoTarget = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX);
		String valString;
		if(infoTarget == 0){
			valString = "Port/Cable Plug";
			list.add(new DetailsRow.Builder().name("MANU_INFO_TARGET").value(valString).level(0).build());
		}
		else if(infoTarget == 1){
			valString = "Battery";
			list.add(new DetailsRow.Builder().name("MANU_INFO_TARGET").value(valString).level(0).build());
		}
		else{
			valString = infoTarget.toString();
			list.add(new DetailsRow.Builder().name("MANU_INFO_TARGET").value(valString).level(0).bcolor(BG.RED).build());
		}

		Long input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 1);
		if((infoTarget != 1) || (input > 7)){
			list.add(new DetailsRow.Builder().name("MANU_INFO_REF").value(Long.toString(input)).level(0).bcolor(BG.RED).build());
		}
		else{
			list.add(new DetailsRow.Builder().name("MANU_INFO_REF").value(Long.toString(input)).level(0).build());
		}


	}

	private static void processBatteryCaps(ObservableList<DetailsRow> list, byte[] pkt) {
		Long input;
		Integer valInt;
		Long val;
	    String valString;
        list.add(new DetailsRow.Builder().name("VID").value("0x" + Long.toHexString(PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX))).level(0).build());
        list.add(new DetailsRow.Builder().name("PID").value("0x" + Long.toHexString(PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 2))).level(0).build());

        input = PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 4);
        if(input == 0){
        	valString = "Battery Not Present";
        }
        else if(input == 0xFFFF){
        	valString = "Unknown";
        }
        else{
            valString = "0x" + Long.toHexString(input) + " (" + Long.toString(input * 10) + " WH)";
        }
        list.add(new DetailsRow.Builder().name("BATT_DESIGN_CAPACITY").value(valString).level(1).build());

        input = PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 6);
        if(input == 0){
        	valString = "Battery Not Present";
        }
        else if(input == 0xFFFF){
        	valString = "Unknown";
        }
        else{
            valString = "0x" + Long.toHexString(input) + " (" + Long.toString(input * 10) + " WH)";
        }
        list.add(new DetailsRow.Builder().name("LAST_FULL_CHARGE_CAPACITY").value(valString).level(1).build());

        input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 8);
        list.add(new DetailsRow.Builder().name("BATTERY_TYPE").value("0x" + Long.toHexString(input)).level(0).build());
        val = ((input) & 0x000000fe) >> 1;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000001) >> 0);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("invalid_battery_ref").value(valString).level(1).build());
	}

	private static void processGetBatteryStatus(ObservableList<DetailsRow> list, byte[] pkt) {
		Long input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX);
		if(input > 7){
			list.add(new DetailsRow.Builder().name("BATTERY_CAP_REF").value(Long.toString(input)).level(0).bcolor(BG.RED).build());
		}
		else{
			list.add(new DetailsRow.Builder().name("BATTERY_CAP_REF").value(Long.toString(input)).level(0).build());
		}
	}

	private static void processGetBatteryCap(ObservableList<DetailsRow> list, byte[] pkt) {
		Long input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX);
		if(input > 7){
			list.add(new DetailsRow.Builder().name("BATTERY_CAP_REF").value(Long.toString(input)).level(0).bcolor(BG.RED).build());
		}
		else{
			list.add(new DetailsRow.Builder().name("BATTERY_CAP_REF").value(Long.toString(input)).level(0).build());
		}
	}

	private static void processStatus(ObservableList<DetailsRow> list, byte[] pkt) {
		Long input;
		Integer valInt;
		Long val;
	    String valString;
	    input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX);
	    if(input == 0){
	    	valString = "Not Supported";
	    }
	    else if(input == 1){
	    	valString = "< 2C";
	    }
	    else{
	    	valString = input.toString() + " C";
	    }
	    list.add(new DetailsRow.Builder().name("INTERNAL_TEMP").value(valString).level(0).build());

	    input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 1);
        list.add(new DetailsRow.Builder().name("PRESENT_INPUT").value("0x" + Long.toHexString(input)).level(0).build());

        val = ((input) & 0x000000e0) >> 5;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000010) >> 4);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("internal_power_non_batt").value(valString).level(1).build());
        Integer intPowerFromBattery = (int)(((input) & 0x00000008) >> 3);
        valString = enumYesNo.values()[intPowerFromBattery].name();
        list.add(new DetailsRow.Builder().name("internal_power_batt").value(valString).level(1).build());
        Integer extPower = (int)(((input) & 0x00000002) >> 1);
        valInt = (int)(((input) & 0x00000004) >> 2);
        if(extPower == 1){
	        valString = enumYesNo.values()[valInt].name();
	        list.add(new DetailsRow.Builder().name("ext_power_ac_dc").value(valString).level(1).build());
        }
        else{
        	if(valInt == 0){
        		list.add(new DetailsRow.Builder().name("rsvd").value("0").level(1).build());
        	}
        	else{
        		list.add(new DetailsRow.Builder().name("rsvd").value("1").level(1).bcolor(BG.RED).build());
        	}
        }

        valString = enumStatusDCAC.values()[extPower].name();
        list.add(new DetailsRow.Builder().name("external_power").value(valString).level(1).build());
        val = ((input) & 0x00000001) >> 0;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }

	    input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 2);
	    if(intPowerFromBattery != 0){
		    list.add(new DetailsRow.Builder().name("PRESENT_BATT_INPUT").value("0x" + Long.toHexString(input)).level(0).build());

	        valInt = (int)(((input) & 0x000000f0) >> 4);
	        valString = "0b" + valInt.toBinaryString(valInt);
	        list.add(new DetailsRow.Builder().name("hot_swap_batt").value(valString).level(1).build());
	        valInt = (int)(((input) & 0x0000000f) >> 0);
	        valString = "0b" + valInt.toBinaryString(valInt);
	        list.add(new DetailsRow.Builder().name("fixed_batt").value(valString).level(1).build());
	    }
	    else{
	    	if(input != 0){
	    		list.add(new DetailsRow.Builder().name("PRESENT_BATT_INPUT").value("0x" + Long.toHexString(input)).level(0).bcolor(BG.RED).build());
	    	}
	    	else{
	    		list.add(new DetailsRow.Builder().name("PRESENT_BATT_INPUT").value("0x" + Long.toHexString(input)).level(0).build());
	    	}
	    }

	    input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 3);
        list.add(new DetailsRow.Builder().name("EVENT_FLAGS").value("0x" + Long.toHexString(input)).level(0).build());
        val = ((input) & 0x000000e0) >> 5;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000010) >> 4);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cf_mode").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000008) >> 3);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ovp").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000004) >> 2);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("otp").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000002) >> 1);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ocp").value(valString).level(1).build());
        val = ((input) & 0x00000001) >> 0;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }

        input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 4);
        list.add(new DetailsRow.Builder().name("TEMPERATURE_STATUS").value("0x" + Long.toHexString(input)).level(0).build());
        val = ((input) & 0x000000f8) >> 3;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000006) >> 1);
        valString = enumStatusTemperature.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("status").value(valString).level(1).build());
        val = ((input) & 0x00000001) >> 0;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
	}

	private static void processSourceCapExtended(ObservableList<DetailsRow> list, byte[] pkt) {
        list.add(new DetailsRow.Builder().name("VID").value("0x" + Long.toHexString(PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX))).level(0).build());
        list.add(new DetailsRow.Builder().name("PID").value("0x" + Long.toHexString(PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 2))).level(0).build());
        list.add(new DetailsRow.Builder().name("XID").value("0x" + Long.toHexString(PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 4))).level(0).build());
        list.add(new DetailsRow.Builder().name("FW_VER").value("0x" + Long.toHexString(PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 8))).level(0).build());
        list.add(new DetailsRow.Builder().name("HW_VER").value("0x" + Long.toHexString(PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 9))).level(0).build());

        Long input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 10);
        list.add(new DetailsRow.Builder().name("VOLT_REGULATION").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0x000000f8) >> 3;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000004) >> 2);
        valString = enumExtdSrcCapIOC.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ioc").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000003) >> 0);
        valString = enumExtdSrcCapLoadStep.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("load_step").value(valString).level(1).build());

        input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 11);
        if(input == 0){
        	valString = "Not Supported";
        }
        else{
        	valString = "0x" + Long.toHexString(input);
        }
        list.add(new DetailsRow.Builder().name("HOLDUP_TIME").value(valString).level(0).build());

        input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 12);
        list.add(new DetailsRow.Builder().name("COMPLIANCE").value("0x" + Long.toHexString(input)).level(0).build());
        val = ((input) & 0x000000f8) >> 3;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000004) >> 2);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ps2_compliant").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000002) >> 1);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ps1_compliant").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000001) >> 0);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("lps_compliant").value(valString).level(1).build());

        input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 13);
        list.add(new DetailsRow.Builder().name("TOUCH_CURRENT").value("0x" + Long.toHexString(input)).level(0).build());
        val = ((input) & 0x000000f8) >> 3;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000004) >> 2);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("gnd_pin_for_earth").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000002) >> 1);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("gnd_pin_support").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000001) >> 0);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("low_touch_cur_eps").value(valString).level(1).build());

        input = PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 14);
        list.add(new DetailsRow.Builder().name("PEAK_CURRENT_1").value("0x" + Long.toHexString(input)).level(0).build());
        valInt = (int)(((input) & 0x00008000) >> 15);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vbus_droop").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00007800) >> 11);
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 5) + " %)";
        list.add(new DetailsRow.Builder().name("duty_cycle").value(valString).level(1).build());
        val = ((input) & 0x000007e0) >> 5;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 20) + " ms)";
        list.add(new DetailsRow.Builder().name("overload_period").value(valString).level(1).build());
        val = ((input) & 0x0000001f) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " %)";
        list.add(new DetailsRow.Builder().name("percent_overload").value(valString).level(1).build());

        input = PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 16);
        list.add(new DetailsRow.Builder().name("PEAK_CURRENT_2").value("0x" + Long.toHexString(input)).level(0).build());
        valInt = (int)(((input) & 0x00008000) >> 15);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vbus_droop").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00007800) >> 11);
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 5) + " %)";
        list.add(new DetailsRow.Builder().name("duty_cycle").value(valString).level(1).build());
        val = ((input) & 0x000007e0) >> 5;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 20) + " ms)";
        list.add(new DetailsRow.Builder().name("overload_period").value(valString).level(1).build());
        val = ((input) & 0x0000001f) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " %)";
        list.add(new DetailsRow.Builder().name("percent_overload").value(valString).level(1).build());

        input = PDUtils.get16bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 18);
        list.add(new DetailsRow.Builder().name("PEAK_CURRENT_3").value("0x" + Long.toHexString(input)).level(0).build());
        valInt = (int)(((input) & 0x00008000) >> 15);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vbus_droop").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00007800) >> 11);
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 5) + " %)";
        list.add(new DetailsRow.Builder().name("duty_cycle").value(valString).level(1).build());
        val = ((input) & 0x000007e0) >> 5;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 20) + " ms)";
        list.add(new DetailsRow.Builder().name("overload_period").value(valString).level(1).build());
        val = ((input) & 0x0000001f) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val * 10) + " %)";
        list.add(new DetailsRow.Builder().name("percent_overload").value(valString).level(1).build());

        input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 20);
        if(input == 0){
        	valString = "IEC 60950-1";
            list.add(new DetailsRow.Builder().name("TOUCH_TEMP").value(valString).level(0).build());
        }
        else if(input == 1){
        	valString = "IEC 62368-1 TS1";
            list.add(new DetailsRow.Builder().name("TOUCH_TEMP").value(valString).level(0).build());
        }
        else if(input == 2){
        	valString = "IEC 62368-1 TS2";
            list.add(new DetailsRow.Builder().name("TOUCH_TEMP").value(valString).level(0).build());
        }
        else{
        	valString = "0x" + Long.toHexString(input);
            list.add(new DetailsRow.Builder().name("TOUCH_TEMP").value(valString).level(0).bcolor(BG.RED).build());
        }

        input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 21);
        list.add(new DetailsRow.Builder().name("SOURCE_INPUTS").value("0x" + Long.toHexString(input)).level(0).build());
        val = ((input) & 0x000000f8) >> 3;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000004) >> 2);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("internal_batt_present").value(valString).level(1).build());
        Integer valExternalSupply = (int)(((input) & 0x00000001) >> 0);
        valInt = (int)(((input) & 0x00000002) >> 1);
        if(valExternalSupply == 1){
	        valString = enumYesNo.values()[valInt].name();
	        list.add(new DetailsRow.Builder().name("unconstrained").value(valString).level(1).build());
        }
        else{
        	if(valInt != 0){
        		list.add(new DetailsRow.Builder().name("rsvd").value(Integer.toString(valInt)).level(1).bcolor(BG.RED).build());
        	}
        	else{
        		list.add(new DetailsRow.Builder().name("rsvd").value(Integer.toString(valInt)).level(1).build());
        	}
        }

        valString = enumYesNo.values()[valExternalSupply].name();
        list.add(new DetailsRow.Builder().name("external_supply_present").value(valString).level(1).build());

        input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 22);
        list.add(new DetailsRow.Builder().name("BATTERIES").value("0x" + Long.toHexString(input)).level(0).build());
        valInt = (int)(((input) & 0x000000f0) >> 4);
        valString = Integer.toString(valInt);
        list.add(new DetailsRow.Builder().name("hot_swappable_batt_count").value(valString).level(1).build());
        valInt = (int)(((input) & 0x0000000f) >> 0);
        valString = Integer.toString(valInt);
        list.add(new DetailsRow.Builder().name("fixed_batt_count").value(valString).level(1).build());

        input = PDUtils.get8bitValue(pkt, PktCollecter.EXTD_DATA_BYTE0_IDX + 23);
        list.add(new DetailsRow.Builder().name("SOURCE_PDP").value("0x" + Long.toHexString(input)).level(0).build());
        val = ((input) & 0x00000080) >> 7;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x0000007f) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + " Watts)";
        list.add(new DetailsRow.Builder().name("rated_pdp").value(valString).level(1).build());
	}

	private static void processVDM(ObservableList<DetailsRow> list, byte[] pkt, int count, enumSOPType sopType, int rev) {
		Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX);

		int isStruct = (int)(((input) & 0x00008000) >> 15);
		if(isStruct == 0){
			processUnstructuredVDM(list, pkt, count, input);
		}
		else{
			processStructuredVDM(list, pkt, count, input, sopType, rev);
		}

	}

    private static void processStructuredVDM(ObservableList<DetailsRow> list, byte[] pkt, int count, Long vdmHeader, enumSOPType sopType, int rev) {
    	Integer svid = (int)(((vdmHeader) & 0xffff0000) >> 16);
    	Integer valCommand = (int)(((vdmHeader) & 0x0000001f) >> 0);
    	Integer valCtype = (int)(((vdmHeader) & 0x000000c0) >> 6);
    	addSTRUCT_VDM_HDR(list, vdmHeader, valCommand, valCtype, svid);

    	switch (valCommand) {
		case 1:
			processDiscoverIdentity(list, pkt, valCtype, count, svid, sopType, rev);
			break;
		case 2:
			processDiscoverSVID(list, pkt, count, svid);
			break;
		case 3:
			processDiscoverModes(list, pkt, valCtype, count, svid);
			break;
		case 4:
			processEnterMode(list, pkt, valCtype, count, svid);
			break;
		case 5:
			processExitMode(list, pkt, valCtype, count, svid);
			break;
		case 6:
			processAttention(list, pkt, valCtype, count, svid);
			break;
		case 16:
			processDPStatusUpdate(list, pkt, valCtype, count, svid);
			break;
		case 17:
			processDPconfigure(list, pkt, valCtype, count, svid);
			break;
		default:
			for(int i = 1; i < count; i++){
				Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
		        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
			}
			break;
		}

	}



	private static void processUnstructuredVDM(ObservableList<DetailsRow> list, byte[] pkt, int count, Long vdmHeader) {
    	addUNSTRUCT_VDM_HDR(list, vdmHeader);
		for(int i = 1; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}

	}

	private static void addSTRUCT_VDM_HDR(ObservableList<DetailsRow> list, Long input, Integer valCommand, Integer valCtype, Integer svid) {
        String cmdString = enumSVDMCmd.values()[valCommand].name();
        String ctypeString = enumSVDMCmdType.values()[valCtype].name();
        list.add(new DetailsRow.Builder().name("STRUCT_VDM_HDR-" + cmdString + " " + ctypeString).value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;

        String svidString = "";
        switch (svid) {
		case 0xFF00:
			svidString = "PD_SID";
			break;
		case 0x8087:
			svidString = "INTEL_VID";
			break;
		case 0xFF01:
			svidString = "DP_VID";
			break;
		case 0x04b4:
			svidString = "CY_VID";
			break;
		default:
			svidString = "Unknown VID";
			break;
		}

        valString = "0x" + Long.toHexString(svid) + " (" + svidString + ")";
        if(((valCommand == 1) || (valCommand == 2)) && (svid != 0xFF00)){
        	list.add(new DetailsRow.Builder().name("svid").value(valString).level(1).bcolor(BG.RED).build());
        }
        else{
        	list.add(new DetailsRow.Builder().name("svid").value(valString).level(1).build());
        }

        valInt = (int)(((input) & 0x00008000) >> 15);
        valString = enumVDMType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vdm_type").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00006000) >> 13);
        valString = enumSVDMVersion.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("svdm_ver").value(valString).level(1).build());
        val = ((input) & 0x00001800) >> 11;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000700) >> 8);
        valString = valInt.toString();
        if((valInt != 0)&& ((valCommand < 4 || (valCommand > 6)))){
        	list.add(new DetailsRow.Builder().name("object_position").value(valString).level(1).bcolor(BG.RED).build());
        }
        else{
        	if((valInt == 0) && ((valCommand >= 4) && (valCommand <= 6))){
        		list.add(new DetailsRow.Builder().name("object_position").value(valString).level(1).bcolor(BG.RED).build());
        	}
        	else{
        		list.add(new DetailsRow.Builder().name("object_position").value(valString).level(1).build());
        	}
        }


        list.add(new DetailsRow.Builder().name("command_type").value(ctypeString).level(1).build());
        val = ((input) & 0x00000020) >> 5;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }

        list.add(new DetailsRow.Builder().name("command").value(cmdString).level(1).build());
    }

    private static void addUNSTRUCT_VDM_HDR(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("UNSTRUCT_VDM_HDR").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0xffff0000) >> 16;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("vendor_id").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00008000) >> 15);
        valString = enumVDMType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vdm_type").value(valString).level(1).build());
        val = ((input) & 0x00007fff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("vendor_use").value(valString).level(1).build());
    }

	private static void processDPconfigure(ObservableList<DetailsRow> list, byte[] pkt, Integer valCtype, int count,
			Integer svid) {
		int i = 1;
		if((svid == 0xFF01) && (valCtype == 0)){
			i = 2;
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + 4);
			addDP_CONFIGURE(list, input);
		}

		for(; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}

	}

	private static void processDPStatusUpdate(ObservableList<DetailsRow> list, byte[] pkt, Integer valCtype, int count,
			Integer svid) {
		int i = 1;
		if((svid == 0xFF01) && (valCtype == 1)){
			i = 2;
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + 4);
			addDP_STATUS(list, input);
		}

		for(; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}

	}

	private static void processAttention(ObservableList<DetailsRow> list, byte[] pkt, Integer valCtype, int count,
			Integer svid) {
		int i = 1;
		if(svid == 0xFF01){
			i = 2;
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + 4);
			addDP_STATUS(list, input);
		}

		for(; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}
	}

	private static void processExitMode(ObservableList<DetailsRow> list, byte[] pkt, Integer valCtype, int count,
			Integer svid) {
		for(int i = 1; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}

	}

	private static void processEnterMode(ObservableList<DetailsRow> list, byte[] pkt, Integer valCtype, int count,
			Integer svid) {
		for(int i = 1; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}

	}

	private static void processDiscoverModes(ObservableList<DetailsRow> list, byte[] pkt, Integer valCtype, int count,
			Integer svid) {
		int i = 1;
		if((svid == 0xFF01) && (valCtype == 1)){
			i = 2;
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + 4);
			addDP_MODES(list, input);
		}

		for(; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}
	}

    private static void addDP_STATUS(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("DP_STATUS").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0xfffffe00) >> 9;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000100) >> 8);
        valString = enumDPStatusIRQ.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("irq").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000080) >> 7);
        valString = enumDPStatusHPD.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("hpd_state").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000040) >> 6);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("exit_dp_mode_req").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000020) >> 5);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_config_req").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000010) >> 4);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("multi_function_preferred").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000008) >> 3);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("enabled").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000004) >> 2);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("power_low").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000003) >> 0);
        valString = enumDPStatusConn.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("dfp_d_ufp_d_connected").value(valString).level(1).build());
    }

    private static void addDP_MODES(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("DP_MODES").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0xff000000) >> 24;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00ff0000) >> 16);
        valString = "0x" + Long.toHexString(valInt);
        if(valInt == 0){
        	valString += " Not supported";
        }
        if((valInt & 0x1) != 0){
        	valString += ", A";
        }
        if((valInt & 0x2) != 0){
        	valString += ", B";
        }
        if((valInt & 0x4) != 0){
        	valString += ", C";
        }
        if((valInt & 0x8) != 0){
        	valString += ", D";
        }
        if((valInt & 0x10) != 0){
        	valString += ", E";
        }
        if((valInt & 0x20) != 0){
        	valString += ", F";
        }
        list.add(new DetailsRow.Builder().name("ufp_d_pin_assignment").value(valString).level(1).build());
        valInt = (int)(((input) & 0x0000ff00) >> 8);

        valString = "0x" + Long.toHexString(valInt);
        if(valInt == 0){
        	valString += " Not supported";
        }
        if((valInt & 0x1) != 0){
        	valString += ", A";
        }
        if((valInt & 0x2) != 0){
        	valString += ", B";
        }
        if((valInt & 0x4) != 0){
        	valString += ", C";
        }
        if((valInt & 0x8) != 0){
        	valString += ", D";
        }
        if((valInt & 0x10) != 0){
        	valString += ", E";
        }

        list.add(new DetailsRow.Builder().name("dfp_d_pin_assignment").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000080) >> 7);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb2_not_used").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000040) >> 6);
        valString = enumDPDiscModeReceptacle.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("receptacle_indication").value(valString).level(1).build());
        valInt = (int)(((input) & 0x0000003c) >> 2);
        valString = enumDPConfSignalling.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("signaling").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000003) >> 0);
        valString = enumDPDiscModePortCap.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("port_capability").value(valString).level(1).build());
    }

    private static void addDP_CONFIGURE(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("DP_CONFIGURE").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0xffff0000) >> 16;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x0000ff00) >> 8);
        valString = "0x" + Long.toHexString(valInt);
        if(valInt == 0){
        	valString += " Deselect";
        }
        if((valInt & 0x1) != 0){
        	valString += ", A";
        }
        if((valInt & 0x2) != 0){
        	valString += ", B";
        }
        if((valInt & 0x4) != 0){
        	valString += ", C";
        }
        if((valInt & 0x8) != 0){
        	valString += ", D";
        }
        if((valInt & 0x10) != 0){
        	valString += ", E";
        }
        if((valInt & 0x20) != 0){
        	valString += ", F";
        }

        list.add(new DetailsRow.Builder().name("ufp_u_pin_assignment").value(valString).level(1).build());
        val = ((input) & 0x000000c0) >> 6;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x0000003c) >> 2);
        valString = enumDPConfSignalling.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("signaling").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000003) >> 0);
        valString = enumDPConfigureSelect.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("select_config").value(valString).level(1).build());
    }

	private static void processDiscoverSVID(ObservableList<DetailsRow> list, byte[] pkt, int count, Integer svid) {
		for(int i = 1; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}
	}

	private static void processDiscoverIdentity(ObservableList<DetailsRow> list, byte[] pkt, Integer valCtype,
			int count, Integer svid, enumSOPType sopType, int rev) {
		int i = 1;
		if((svid == 0xFF01) && (valCtype == 1)){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + 4);
			String productType = "";
			Integer valInt = (int)(((input) & 0x38000000) >> 27);
			if(sopType.equals(enumSOPType.SOP)){
				productType = enumIDHdrProductTypeUFP.values()[valInt].name();
			}
			else{
				productType = enumIDHdrProductTypeCable.values()[valInt].name();
			}

			if(rev <= 1){
				addID_HDR_VDO_REV2(list, input, productType);
			}
			else{
				addID_HDR_VDO_REV3(list, input, productType);
			}
			i++;
			input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + 8);
			list.add(new DetailsRow.Builder().name("CERT_VDO").value("0x" + Long.toHexString(input)).level(0).build());
			i++;
			input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + 12);
			addPRODUCT_VDO(list, input);
			i++;
			input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + 16);
			if("PASSIVE_CABLE".equals(productType)){
				if(rev <= 1){
					addPASSIVE_CBL_VDO_REV2(list, input);
				}
				else{
					addPASSIVE_CBL_VDO_REV3(list, input);
				}
				i++;
			}
			else if("ACTIVE_CABLE".equals(productType)){
				if(rev <= 1){
					addACTIVE_CBL_VDO_REV2(list, input);
				}
				else{
					addACTIVE_CBL_VDO_REV3(list, input);
				}
				i++;
			}
			else if("AMA".equals(productType)){
				if(rev <= 1){
					addAMA_VDO_REV2(list, input);
				}
				else{
					addAMA_VDO_REV3(list, input);
				}
				i++;
			}
		}

		for(; i < count; i++){
			Long input = PDUtils.get32bitValue(pkt, PktCollecter.DATA_BYTE0_IDX + i * 4);
	        list.add(new DetailsRow.Builder().name("DATA_OBJ" + (i + 1)).value("0x" + Long.toHexString(input)).level(0).build());
		}
	}
	
    private static void addID_HDR_VDO_REV2(ObservableList<DetailsRow> list, Long input, String productType) {
        list.add(new DetailsRow.Builder().name("ID_HDR_VDO_REV2").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0x80000000) >> 31);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_host").value(valString).level(1).build());
        valInt = (int)(((input) & 0x40000000) >> 30);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_device").value(valString).level(1).build());
        list.add(new DetailsRow.Builder().name("product_type_ufp").value(productType).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("modal_operation_sup").value(valString).level(1).build());
        val = ((input) & 0x03ff0000) >> 16;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x0000ffff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("usb_vendor_id").value(valString).level(1).build());
    }
    
    private static void addID_HDR_VDO_REV3(ObservableList<DetailsRow> list, Long input, String productType) {
        list.add(new DetailsRow.Builder().name("ID_HDR_VDO_REV3").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0x80000000) >> 31);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_host").value(valString).level(1).build());
        valInt = (int)(((input) & 0x40000000) >> 30);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_device").value(valString).level(1).build());
        list.add(new DetailsRow.Builder().name("product_type_ufp").value(productType).level(1).build());
        valInt = (int)(((input) & 0x04000000) >> 26);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("modal_operation_sup").value(valString).level(1).build());
        valInt = (int)(((input) & 0x03800000) >> 23);
        valString = enumIDHdrProductTypeDFP.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("product_type_dfp").value(valString).level(1).build());
        val = ((input) & 0x007f0000) >> 16;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        val = ((input) & 0x0000ffff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("usb_vendor_id").value(valString).level(1).build());
    } 
    
    private static void addPRODUCT_VDO(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("PRODUCT_VDO").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        val = ((input) & 0xffff0000) >> 16;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("pid").value(valString).level(1).build());
        val = ((input) & 0x0000ffff) >> 0;
        valString = "0x" + Long.toHexString(val) + " (" + Long.toString(val) + ")";
        list.add(new DetailsRow.Builder().name("bcddevice").value(valString).level(1).build());
    }
    
    private static void addPASSIVE_CBL_VDO_REV2(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("PASSIVE_CBL_VDO_REV2").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xf0000000) >> 28);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("hw_version").value(valString).level(1).build());
        valInt = (int)(((input) & 0x0f000000) >> 24);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("fw_version").value(valString).level(1).build());
        val = ((input) & 0x00f00000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x000c0000) >> 18);
        valString = enumCableTCPlugToCaptiveRev2.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("plug_to_a_b_c_or_captive").value(valString).level(1).build());
        val = ((input) & 0x00020000) >> 17;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x0001e000) >> 13);
        valString = enumCableLatencyPassive.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_latency").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00001800) >> 11);
        valString = enumCablePassiveTermType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_term_type").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000400) >> 10);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("sstx1_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000200) >> 9);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("sstx2_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000100) >> 8);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ssrx1_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000080) >> 7);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ssrx2_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000060) >> 5);
        valString = enumCableVbusCur.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vbus_cur_cap").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000010) >> 4);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vbus_thu_cable").value(valString).level(1).build());
        val = ((input) & 0x00000008) >> 3;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000007) >> 0);
        valString = enumCableSSSupport.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_superspeed").value(valString).level(1).build());
    }
    
    private static void addPASSIVE_CBL_VDO_REV3(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("PASSIVE_CBL_VDO_REV3").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xf0000000) >> 28);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("hw_version").value(valString).level(1).build());
        valInt = (int)(((input) & 0x0f000000) >> 24);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("fw_version").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00e00000) >> 21);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("vdo_version").value(valString).level(1).build());
        val = ((input) & 0x00100000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x000c0000) >> 18);
        valString = enumCableTCPlugToCaptiveRev3.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("plug_to_c_or_captive").value(valString).level(1).build());
        val = ((input) & 0x00020000) >> 17;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x0001e000) >> 13);
        valString = enumCableLatencyPassive.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_latency").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00001800) >> 11);
        valString = enumCablePassiveTermType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_term_type").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000600) >> 9);
        valString = enumCableMaxVolt.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("max_vbus_volt").value(valString).level(1).build());
        val = ((input) & 0x00000180) >> 7;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000060) >> 5);
        valString = enumCableVbusCur.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vbus_cur_cap").value(valString).level(1).build());
        val = ((input) & 0x00000018) >> 3;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000007) >> 0);
        valString = enumCableSSSupport.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_superspeed").value(valString).level(1).build());
    }
    
    private static void addACTIVE_CBL_VDO_REV2(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("ACTIVE_CBL_VDO_REV2").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        Integer valVBUSThru = (int)(((input) & 0x00000010) >> 4);
        valInt = (int)(((input) & 0xf0000000) >> 28);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("hw_version").value(valString).level(1).build());
        valInt = (int)(((input) & 0x0f000000) >> 24);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("fw_version").value(valString).level(1).build());
        val = ((input) & 0x00f00000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x000c0000) >> 18);
        valString = enumCableTCPlugToCaptiveRev2.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("plug_to_a_b_c_or_captive").value(valString).level(1).build());
        val = ((input) & 0x00020000) >> 17;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x0001e000) >> 13);
        valString = enumCableLatencyActive.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_latency").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00001800) >> 11);
        valString = enumCableActiveTermType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_term_type").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000400) >> 10);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("sstx1_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000200) >> 9);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("sstx2_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000100) >> 8);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ssrx1_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000080) >> 7);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ssrx2_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000060) >> 5);
        valString = enumCableVbusCur.values()[valInt].name();
        if(valVBUSThru == 0){
        	if(valInt != 0){
        		list.add(new DetailsRow.Builder().name("vbus_cur_cap").value(valString).level(1).bcolor(BG.RED).build());
        	}
        	else{
        		list.add(new DetailsRow.Builder().name("vbus_cur_cap").value(valString).level(1).build());
        	}
        }
        else{
            
            list.add(new DetailsRow.Builder().name("vbus_cur_cap").value(valString).level(1).build());        	
        }
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vbus_thu_cable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000008) >> 3);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("sop_dprime_controller_present").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000007) >> 0);
        valString = enumCableSSSupport.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_superspeed").value(valString).level(1).build());
    }
    
    private static void addACTIVE_CBL_VDO_REV3(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("ACTIVE_CBL_VDO_REV3").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        Integer valVBUSThru = (int)(((input) & 0x00000010) >> 4);
        valInt = (int)(((input) & 0xf0000000) >> 28);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("hw_version").value(valString).level(1).build());
        valInt = (int)(((input) & 0x0f000000) >> 24);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("fw_version").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00e00000) >> 21);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("vdo_version").value(valString).level(1).build());
        val = ((input) & 0x00100000) >> 20;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x000c0000) >> 18);
        valString = enumCableTCPlugToCaptiveRev3.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("plug_to_c_or_captive").value(valString).level(1).build());
        val = ((input) & 0x00020000) >> 17;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x0001e000) >> 13);
        valString = enumCableLatencyActive.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_latency").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00001800) >> 11);
        valString = enumCableActiveTermType.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("cable_term_type").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000600) >> 9);
        valString = enumCableMaxVolt.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("max_vbus_volt").value(valString).level(1).build());
        val = ((input) & 0x00000180) >> 7;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000060) >> 5);
        valString = enumCableVbusCur.values()[valInt].name();
        if(valVBUSThru == 0){
        	if(valInt != 0){
        		list.add(new DetailsRow.Builder().name("vbus_cur_cap").value(valString).level(1).bcolor(BG.RED).build());
        	}
        	else{
        		list.add(new DetailsRow.Builder().name("vbus_cur_cap").value(valString).level(1).build());
        	}
        }
        else{
            
            list.add(new DetailsRow.Builder().name("vbus_cur_cap").value(valString).level(1).build());        	
        }


        valString = enumYesNo.values()[valVBUSThru].name();
        list.add(new DetailsRow.Builder().name("vbus_thru_cable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000008) >> 3);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("sop_dprime_controller_present").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000007) >> 0);
        valString = enumCableSSSupport.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_superspeed").value(valString).level(1).build());
    }
    
    private static void addAMA_VDO_REV2(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("AMA_VDO_REV2").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xf0000000) >> 28);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("hw_version").value(valString).level(1).build());
        valInt = (int)(((input) & 0x0f000000) >> 24);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("fw_version").value(valString).level(1).build());
        val = ((input) & 0x00fff000) >> 12;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x00000800) >> 11);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("sstx1_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000400) >> 10);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("sstx2_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000200) >> 9);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ssrx1_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000100) >> 8);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("ssrx2_configurable").value(valString).level(1).build());
        valInt = (int)(((input) & 0x000000e0) >> 5);
        valString = enumAMAVCONNPower.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vconn_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000010) >> 4);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vconn_req").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000008) >> 3);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vbus_req").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000007) >> 0);
        valString = enumAMAUSBSSSupport.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_superspeed").value(valString).level(1).build());
    }

    private static void addAMA_VDO_REV3(ObservableList<DetailsRow> list, Long input) {
        list.add(new DetailsRow.Builder().name("AMA_VDO_REV3").value("0x" + Long.toHexString(input)).level(0).build());
        Long val;
        Integer valInt;
        String valString;
        valInt = (int)(((input) & 0xf0000000) >> 28);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("hw_version").value(valString).level(1).build());
        valInt = (int)(((input) & 0x0f000000) >> 24);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("fw_version").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00e00000) >> 21);
        valString = "0x" + Integer.toHexString(valInt);
        list.add(new DetailsRow.Builder().name("vdo_version").value(valString).level(1).build());
        val = ((input) & 0x001fff00) >> 8;
        valString = "0x" + Long.toHexString(val);
        if(val == 0){
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).build());
        }
        else{
            list.add(new DetailsRow.Builder().name("rsvd").value(valString).level(1).bcolor(BG.RED).build());
        }
        valInt = (int)(((input) & 0x000000e0) >> 5);
        valString = enumAMAVCONNPower.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vconn_power").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000010) >> 4);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vconn_req").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000008) >> 3);
        valString = enumYesNo.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("vbus_req").value(valString).level(1).build());
        valInt = (int)(((input) & 0x00000007) >> 0);
        valString = enumAMAUSBSSSupport.values()[valInt].name();
        list.add(new DetailsRow.Builder().name("usb_superspeed").value(valString).level(1).build());
    }

    
    
}
