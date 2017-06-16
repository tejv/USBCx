package org.ykc.usbcx;

import org.ykc.usbcx.DetailsRow.BG;
import org.ykc.usbcx.PDUtils.enumAPDOType;
import org.ykc.usbcx.PDUtils.enumBistMode;
import org.ykc.usbcx.PDUtils.enumPktType;
import org.ykc.usbcx.PDUtils.enumPortDataRole;
import org.ykc.usbcx.PDUtils.enumPortPowerRole;
import org.ykc.usbcx.PDUtils.enumSOPType;
import org.ykc.usbcx.PDUtils.enumSinkFRRequiremnent;
import org.ykc.usbcx.PDUtils.enumSourcePeakCurrent;
import org.ykc.usbcx.PDUtils.enumSpecRev;
import org.ykc.usbcx.PDUtils.enumSupplyType;
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
		MainViewPktParser.lastSupplyType[index] = supply_type;
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
}
