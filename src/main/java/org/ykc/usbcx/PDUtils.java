package org.ykc.usbcx;

import java.util.HashMap;


public class PDUtils {

	public static final String[] CTRL_MSG_TYPE = {"C_RSVD0", "GD_CRC", "GO_TO_MIN", "ACCEPT", "REJECT", "PING", "PS_RDY", "GET_SRC_CAP", "GET_SNK_CAP", "DR_SWAP", "PR_SWAP", "VCONN_SWAP", "WAIT", "SOFT_RESET", "C_RSVD14", "C_RSVD15", "NOT_SUPPORTED", "GET_SRC_CAP_EXT", "GET_STATUS", "FR_SWAP", "GET_PPS_STATUS", "GET_CNTRY_CODE", "C_RSVD22", "C_RSVD23", "C_RSVD24", "C_RSVD25", "C_RSVD26", "C_RSVD27", "C_RSVD28", "C_RSVD29", "C_RSVD30", "C_RSVD31"};
	public static final String[] DATA_MSG_TYPE = {"D_RSVD0 ", "SRC_CAP", "REQUEST", "BIST", "SNK_CAP", "BAT_STATUS", "ALERT", "GET_CNTRY_INFO", "D_RSVD8", "D_RSVD9", "D_RSVD10", "D_RSVD11", "D_RSVD12", "D_RSVD13", "D_RSVD14", "VDM", "D_RSVD16", "D_RSVD17", "D_RSVD18", "D_RSVD19", "D_RSVD20", "D_RSVD21", "D_RSVD22", "D_RSVD23", "D_RSVD24", "D_RSVD25", "D_RSVD26", "D_RSVD27", "D_RSVD28", "D_RSVD29", "D_RSVD30", "D_RSVD31" };
	public static final String[] EXTD_MSG_TYPE = {"E_RSVD0", "SRC_CAP_EXT", "STATUS", "GET_BAT_CAP", "GET_BAT_STATUS", "BAT_CAP", "GET_MANU_INFO", "MANU_INFO", "SECURITY_REQ", "SECURITY_RESP", "FW_UPDT_REQ", "FW_UPDT_RESP", "PPS_STATUS", "CNTRY_INFO", "CNTRY_CODES", "E_RSVD15", "E_RSVD16", "E_RSVD17", "E_RSVD18", "E_RSVD19", "E_RSVD20", "E_RSVD21", "E_RSVD22", "E_RSVD23", "E_RSVD24", "E_RSVD25", "E_RSVD26", "E_RSVD27", "E_RSVD28", "E_RSVD29", "E_RSVD30", "E_RSVD31" };
	public static final String[] MSG_CLASS = {"CONTROL","DATA","EXTENDED"};
	public static final String[] SOP_TYPE = {"SOP", "SOP_PRIME", "SOP_DRPIME", "SOP_P_DBG", "SOP_DP_DBG", "HARD_RESET", "CABLE_RESET"};

	public enum PDMsgClass
	{
		CTRL_MESSAGE,
		DATA_MESSAGE,
		EXTENDED_MESSAGE
	}
	public enum enumTrueFalse
	{
		FALSE,
		TRUE
	}

	public enum enumYesNo
	{
		No,
		Yes
	}
	public enum enumCtrlMsg
    {
        C_RSVD0,
        GD_CRC,
        GO_TO_MIN,
        ACCEPT,
        REJECT,
        PING,
        PS_RDY,
        GET_SRC_CAP,
        GET_SNK_CAP,
        DR_SWAP,
        PR_SWAP,
        VCONN_SWAP,
        WAIT,
        SOFT_RESET,
        C_RSVD14,
        C_RSVD15,
        NOT_SUPPORTED,
        GET_SRC_CAP_EXT,
        GET_STATUS,
        FR_SWAP,
        GET_PPS_STATUS,
        GET_COUNTRY_CODES,
        C_RSVD22,
        C_RSVD23,
        C_RSVD24,
        C_RSVD25,
        C_RSVD26,
        C_RSVD27,
        C_RSVD28,
        C_RSVD29,
        C_RSVD30,
        C_RSVD31,
    };

    public enum enumDataMsg
    {
        D_RSVD0,
        SRC_CAP,
        REQUEST,
        BIST,
        SNK_CAP,
        BAT_STATUS,
        ALERT,
        GET_COUNTRY_INFO,
        D_RSVD8,
        D_RSVD9,
        D_RSVD10,
        D_RSVD11,
        D_RSVD12,
        D_RSVD13,
        D_RSVD14,
        VDM,
        D_RSVD16,
        D_RSVD17,
        D_RSVD18,
        D_RSVD19,
        D_RSVD20,
        D_RSVD21,
        D_RSVD22,
        D_RSVD23,
        D_RSVD24,
        D_RSVD25,
        D_RSVD26,
        D_RSVD27,
        D_RSVD28,
        D_RSVD29,
        D_RSVD30,
        D_RSVD31,
    };

    public enum enumExtendedMsg
    {
        E_RSVD0,
        SRC_CAP_EXT,
        STATUS,
        GET_BAT_CAP,
        GET_BAT_STATUS,
        BAT_CAPS,
        GET_MANU_INFO,
        MANU_INFO,
        SECURITY_REQ,
        SECURITY_RESP,
        FW_UPDT_REQ,
        FW_UPDT_RESP,
        PPS_STATUS,
        COUNTRY_INFO,
        COUNTRY_CODES,
        E_RSVD15,
        E_RSVD16,
        E_RSVD17,
        E_RSVD18,
        E_RSVD19,
        E_RSVD20,
        E_RSVD21,
        E_RSVD22,
        E_RSVD23,
        E_RSVD24,
        E_RSVD25,
        E_RSVD26,
        E_RSVD27,
        E_RSVD28,
        E_RSVD29,
        E_RSVD30,
        E_RSVD31,
    };

    public enum enumPortPowerRole
    {
        SNK,
        SRC,
    };

    public enum enumPortDataRole
    {
        UFP,
        DFP,
    };

    public enum enumCablePlug
    {
        DFP_UFP,
        CBL_PLUG,
    };

    public enum enumSpecRev
    {
        v1,
        v2,
        v3,
        RSVD
    };

    public enum enumSOPType
    {
        SOP,
        SOP_PRIME,
        SOP_DPRIME,
        SOP_P_DBG,
        SOP_DP_DBG,
        HARD_RESET,
        CABLE_RESET,
        INVALID
    };

    public enum enumPD_MSG_IDX
    {
        SNO_IDX,
        HDR_IDX,
        DATA_IDX,
        RSVD3,
        RSVD4,
        RSVD5,
        RSVD6,
        RSVD7,
        RSVD8,
        RSVD9,
        RSVD10,
        RSVD11,
        RSVD12,
        VBUS_IDX,
        START_TIME_IDX,
        END_TIME_IDX,
    };

    public enum enumPktType
    {
    	PD_PKT,
    	VOLT_PKT,
    	RSVD2,
    	RSVD3
    }

    public enum enumVoltPktEvt
    {
    	NONE,
    	VBUS_DN,
    	VBUS_UP,
    	CC_DEF,
        CC_1_5A,
        CC_3A,
    	DETACH
    }

    public enum enumSupplyType
    {
    	FIXED_SUPPLY,
    	BATTERY_SUPPLY,
    	VARIABLE_SUPPLY,
    	APDO
    }

    public enum enumAPDOType
    {
    	PPS,
    	RSVD1,
    	RSVD2,
    	RSVD3
    }

    public enum enumSourcePeakCurrent
    {
    	PEAK_EQ_IOC,
    	PEAK_EQ_110_IOC_10ms,
    	PEAK_EQ_125_IOC_10ms,
    	PEAK_EQ_150_IOC_10ms,
    }

    public enum enumSinkFRRequiremnent
    {
    	FAST_SWAP_NOT_SUPPORTED,
    	CUR_DEF_USB,
    	CUR_1_5A,
    	CUR_3A,
    }

    public enum enumBistMode
    {
    	RSVD0,
    	RSVD1,
    	RSVD2,
    	RSVD3,
    	RSVD4,
    	BIST_CARRIER_MODE,
    	RSVD6,
    	RSVD7,
    	BIST_TEST_DATA,
    	RSVD9,
    	RSVD10,
    	RSVD11,
    	RSVD12,
    	RSVD13,
    	RSVD14,
    	RSVD15,
    	RSVD16,
    	RSVD17,
    	RSVD18,
    	RSVD19,
    	RSVD20,
    	RSVD21,
    	RSVD22,
    	RSVD23,
    	RSVD24,
    	RSVD25,
    	RSVD26,
    	RSVD27,
    	RSVD28,
    	RSVD29,
    	RSVD30,
    	RSVD31,
    }

    public enum enumVDMType
    {
    	UNSTRUCTURED,
    	STRUCTURED
    }

    public enum enumSVDMVersion
    {
    	VER_1_0,
    	VER_2_0,
    	RSVD2,
    	RSVD3
    }

    public enum enumSVDMCmdType
    {
    	REQ,
    	ACK,
    	NAK,
    	BUSY
    }

    public enum enumSVDMCmd
    {
    	RSVD,
    	DISCOVER_IDENTITY,
    	DISCOVER_SVID,
    	DISCOVER_MODES,
    	ENTER_MODE,
    	EXIT_MODE,
    	ATTENTION,
    	RSVD7,
    	RSVD8,
    	RSVD9,
    	RSVD10,
    	RSVD11,
    	RSVD12,
    	RSVD13,
    	RSVD14,
    	RSVD15,
    	DP_STATUS_UPDATE,
    	DP_CONFIGURE,
    	RSVD18,
    	RSVD19,
    	RSVD20,
    	RSVD21,
    	RSVD22,
    	RSVD23,
    	RSVD24,
    	RSVD25,
    	RSVD26,
    	RSVD27,
    	RSVD28,
    	RSVD29,
    	RSVD30,
    	RSVD31,
    }

    public enum enumIDHdrProductTypeUFP
    {
    	UNDEFINED,
    	PDUSB_HUB,
    	PDUSB_PERIPHERAL,
    	RSVD3,
    	RSVD4,
    	AMA,
    	RSVD6,
    	RSVD7,

    }

    public enum enumIDHdrProductTypeCable
    {
    	RSVD0,
    	RSVD1,
    	RSVD2,
    	PASSIVE_CABLE,
    	ACTIVE_CABLE,
    	RSVD5,
    	RSVD6,
    	RSVD7,

    }

    public enum enumIDHdrProductTypeDFP
    {
    	RSVD0,
    	PDUSB_HUB,
    	PDUSB_HOST,
    	POWER_BRICK,
    	ALTERNATE_MODE_CONTROLLER,
    	RSVD5,
    	RSVD6,
    	RSVD7,
    }

    public enum enumCableVDOVersion
    {
    	VER_1_0,
    	RSVD1,
    	RSVD2,
    	RSVD3,
    	RSVD4,
    	RSVD5,
    	RSVD6,
    	RSVD7,
    }

    public enum enumCableTCPlugToCaptiveRev2
    {
    	USB_TYPEA,
    	USB_TYPEB,
    	USB_TYPEC,
    	CAPTIVE,
    }
    public enum enumCableTCPlugToCaptiveRev3
    {
    	RSVD0,
    	RSVD1,
    	USB_TYPEC,
    	CAPTIVE,
    }

    public enum enumCableLatencyActive
    {
    	RSVD0,
    	LATENCY_10ns,
    	LATENCY_10_20ns,
    	LATENCY_20_30ns,
    	LATENCY_30_40ns,
    	LATENCY_40_50ns,
    	LATENCY_50_60ns,
    	LATENCY_60_70ns,
    	LATENCY_1000ns,
    	LATENCY_2000ns,
    	LATENCY_3000ns,
    	RSVD11,
    	RSVD12,
    	RSVD13,
    	RSVD14,
    	RSVD15,
    }

    public enum enumCableLatencyPassive
    {
    	RSVD0,
    	LATENCY_10ns,
    	LATENCY_10_20ns,
    	LATENCY_20_30ns,
    	LATENCY_30_40ns,
    	LATENCY_40_50ns,
    	LATENCY_50_60ns,
    	LATENCY_60_70ns,
    	LATENCY_MORE_THAN_70ns,
    	RSVD9,
    	RSVD10,
    	RSVD11,
    	RSVD12,
    	RSVD13,
    	RSVD14,
    	RSVD15,
    }

    public enum enumCablePassiveTermType
    {
    	VCONN_NOT_REQ,
    	VCONN_REQ,
    	RSVD2,
    	RSVD3,
    }

    public enum enumCableMaxVolt
    {
    	VOLT_20V,
    	VOLT_30V,
    	VOLT_40V,
    	VOLT_50V,
    }

    public enum enumCableVbusCur
    {
    	RSVD0,
    	CUR_3A,
    	CUR_5A,
    	RSVD3,
    }

    public enum enumCableSSSupport
    {
    	USB2_0_ONLY,
    	USB_3_1_GEN1,
    	USB_3_1_GEN1_GEN2,
    	RSVD3,
    	RSVD4,
    	RSVD5,
    	RSVD6,
    	RSVD7,
    }

    public enum enumCableActiveTermType
    {
    	RSVD0,
    	RSVD1,
    	ONE_END_ACTIVE_ONE_PASSIVE_VCONN_REQ,
    	BOTH_END_ACTIVE_VCONN_REQ,
    }

    public enum enumAMAVDOVersion
    {
    	VER_1_0,
    	RSVD1,
    	RSVD2,
    	RSVD3,
    	RSVD4,
    	RSVD5,
    	RSVD6,
    	RSVD7,
    }

    public enum enumAMAVCONNPower
    {
    	POWER_1W,
    	POWER_1_5W,
    	POWER_2W,
    	POWER_3W,
    	POWER_4W,
    	POWER_5W,
    	POWER_6W,
    	RSVD7,
    }

    public enum enumAMAUSBSSSupport
    {
    	USB2_0_ONLY,
    	USB_3_1_GEN1,
    	USB_3_1_GEN1_GEN2,
    	BILLBOARD_ONLY,
    	RSVD4,
    	RSVD5,
    	RSVD6,
    	RSVD7,
    }

    public enum enumDPConfuration
    {
    	USB,
    	DFP_D,
    	UFP_D,
    	RSVD3,
    }

    public enum enumDPConfSignalling
    {
    	SIGNALLING_UNSPECIFIED,
    	DP_V1_3,
    	GEN2,
    	RSVD3,
    	RSVD4,
    	RSVD5,
    	RSVD6,
    	RSVD7,
    	RSVD8,
    	RSVD9,
    	RSVD10,
    	RSVD11,
    	RSVD12,
    	RSVD13,
    	RSVD14,
    	RSVD15,
    }

    public enum enumDPStatusConn
    {
    	DISABLED,
    	DFP_D_CONNECTED,
    	UFP_D_CONNECTED,
    	BOTH_UFP_D_DFP_D_CONNECTED,
    }

    public static final HashMap<Integer, String> mapDPConfUFPU = new HashMap<Integer, String>();
    static
    {
    	mapDPConfUFPU.put(0x0, "Deselect Pin Assignment");
    	mapDPConfUFPU.put(0x1, "Pin Assignment A");
    	mapDPConfUFPU.put(0x2, "Pin Assignment B");
    	mapDPConfUFPU.put(0x4, "Pin Assignment C");
    	mapDPConfUFPU.put(0x8, "Pin Assignment D");
    	mapDPConfUFPU.put(0x10, "Pin Assignment E");
    	mapDPConfUFPU.put(0x20, "Pin Assignment F");
    }

    public static final HashMap<Integer, String> mapDPDiscModeUFPD = new HashMap<Integer, String>();
    static
    {
    	mapDPDiscModeUFPD.put(0x0, "UFP_D Not Supported");
    	mapDPDiscModeUFPD.put(0x1, "Pin Assignment A");
    	mapDPDiscModeUFPD.put(0x2, "Pin Assignment B");
    	mapDPDiscModeUFPD.put(0x4, "Pin Assignment C");
    	mapDPDiscModeUFPD.put(0x8, "Pin Assignment D");
    	mapDPDiscModeUFPD.put(0x10, "Pin Assignment E");
    }

    public static final HashMap<Integer, String> mapDiscModeDFPD = new HashMap<Integer, String>();
    static
    {
    	mapDiscModeDFPD.put(0x0, "DFP_D Not Supported");
    	mapDiscModeDFPD.put(0x1, "Pin Assignment A");
    	mapDiscModeDFPD.put(0x2, "Pin Assignment B");
    	mapDiscModeDFPD.put(0x4, "Pin Assignment C");
    	mapDiscModeDFPD.put(0x8, "Pin Assignment D");
    	mapDiscModeDFPD.put(0x10, "Pin Assignment E");
    	mapDiscModeDFPD.put(0x20, "Pin Assignment F");
    }

    public enum enumDPDiscModePortCap
    {
    	RSVD,
    	UFP_D_CAPABLE,
    	DFP_D_CAPABLE,
    	BOTH_UFP_D_DFP_D_CAPABLE,
    }

    public enum enumDPConfigureSelect
    {
    	USB,
    	SET_UFP_U_AS_DFP_D,
    	SET_UFP_U_AS_UFP_D,
    	RSVD3,
    }

    public static final HashMap<Integer, String> mapDiscModeSignal= new HashMap<Integer, String>();
    static
    {
    	mapDiscModeSignal.put(0x1, "DP v1.3 supported");
    	mapDiscModeSignal.put(0x2, "USB Gen2 suppported");
    }

    public enum enumDPDiscModeReceptacle
    {
    	DP_PLUG,
    	DP_RECEPTACLE,
    }

    public enum enumDPStatusHPD
    {
    	HPD_LOW,
    	HPD_HIGH,
    }

    public enum enumDPStatusIRQ
    {
    	IRQ_CLEAR,
    	IRQ_SET,
    }

    public enum enumDPStatusExitDPMode
    {
    	MAINTAIN_CURRENT_MODE,
    	EXIT_REQUEST,
    }

    public enum enumDPStatusUSBConfReq
    {
    	MAINTAIN_CURRENT_CONF,
    	REQ_SWITCH_TO_USB_CONF,
    }

    public static final HashMap<Integer, String> mapVIDS= new HashMap<Integer, String>();
    static
    {
    	mapVIDS.put(0xFF00, "SVID");
    	mapVIDS.put(0xFF01, "DP_VID");
    	mapVIDS.put(0x04B4, "CY_VID");
    	mapVIDS.put(0x8087, "TBT_VID");
    }

	public enum enumBattChargingStatus
	{
		CHARGING,
		DISCHARGING,
		IDLE,
		RSVD3
	}

	public enum enumExtdSrcCapLoadStep{
		LOAD_STEP_150mA_per_us,
		LOAD_STEP_500mA_per_us,
		RSVD2,
		RSVD3
	}

	public enum enumExtdSrcCapIOC{
		Percent_25,
		Percent_90,
	}

	public enum enumStatusTemperature{
		NOT_SUPPORTED,
		NORMAL,
		WARNING,
		OVER_TEMPERATURE
	}

	public enum enumPPSStatusOMF{
		CONSTANT_VOLTAGE,
		CURRENT_FOLDBACK
	}

	public enum enumStatusDCAC{
		DC,
		AC
	}

    public static boolean get_field_ok(Long hdr)
    {
        if( (hdr >>> 31) == 0)
        {
            return false;
        }
        return true;
    }

    public static boolean get_field_crc_error(Long hdr)
    {
        if ( ((hdr >>> 30) & 0x1) == 0)
        {
            return false;
        }
        return true;
    }

    public static boolean get_field_eop_error(Long hdr)
    {
        if (((hdr >>> 29) & 0x1) == 0)
        {
            return false;
        }
        return true;
    }

    public static boolean get_field_idle_error(int hdr)
    {
        if (((hdr >>> 28) & 0x1) == 0)
        {
            return false;
        }
        return true;
    }

    public static enumPktType get_field_pkt_type(Long hdr)
    {
        return enumPktType.values()[(int)((hdr >>> 26) & 0x3)];

    }

    public static int get_field_chunk_no(Long hdr)
    {
        return (int)((hdr >>> 20) & 0x3F);

    }

    public static enumSOPType get_field_sop_type(Long hdr)
    {
        return enumSOPType.values()[(int)((hdr >>> 16) & 0xF)];
    }

    public static boolean get_field_extended(Long hdr)
    {
        if (((hdr >>> 15) & 0x1) == 0)
        {
            return false;
        }
        return true;
    }

    public static int get_field_msg_count(Long hdr)
    {
        return (int)((hdr >>> 12) & 0x7);
    }

    public static int get_field_msg_id(Long hdr)
    {
        return (int)((hdr >>> 9) & 0x7);
    }

    public static String get_field_power_role(Long hdr)
    {
        int val = (int)((hdr >>> 8) & 0x1);
        enumSOPType sop_type = get_field_sop_type(hdr);

        if (sop_type == enumSOPType.SOP)
        {
            return (enumPortPowerRole.values()[val]).name();
        }
        else
        {
            return  (enumCablePlug.values()[val]).name();
        }
    }

    public static enumSpecRev get_field_spec_rev(Long hdr)
    {
        return enumSpecRev.values()[(int)((hdr >>> 6) & 0x3)];
    }

    public static String get_field_data_role(Long hdr)
    {
        int val = (int)((hdr >>> 5) & 0x1);
        enumSOPType sop_type = get_field_sop_type(hdr);

        if (sop_type == enumSOPType.SOP)
        {
            return  (enumPortDataRole.values()[val]).name();
        }
        else
        {
            if(val == 0)
            {
                return "RSVD";
            }
            else
            {
                return "ERR";
            }
        }
    }

    public static String get_field_msg_type(Long hdr)
    {
        int val = (int)(hdr & 0x1F);
        if (get_field_extended(hdr) == false)
        {
            if(get_field_msg_count(hdr) == 0)
            {
                return  (enumCtrlMsg.values()[val]).name();
            }
            else
            {
                return  (enumDataMsg.values()[val]).name();
            }
        }
        else
        {
            return  (enumExtendedMsg.values()[val]).name();
        }
    }

    public static int get_field_extended_count(int ext_hdr)
    {
        if((ext_hdr >>> 15) == 0)
        {
            int val = (ext_hdr & 0x1FF);
            return val;
        }
        else
        {
            return 0;
        }
    }

    public static int get_field_extended_chunk_no(int ext_hdr)
    {
    	return (int)((ext_hdr >>> 11) & 0xF);
    }

    public static int get_pd_hdr(Long hdr)
    {
        return (int)(hdr & 0xFFFF);
    }

    public static int get_extended_hdr(int row)
    {
        return (int)(row & 0xFFFF);
    }

    public static boolean get_field_is_chunked(int ext_hdr)
    {
        if ((ext_hdr >>> 15) == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean get_field_is_request_chunk(int ext_hdr)
    {
        if (((ext_hdr >>> 10) & 0x1) == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

	public static Long get32bitValue(byte[] pkt, int idx){
		return Utils.getUnsignedInt(Utils.get_uint32(pkt[idx], pkt[idx + 1], pkt[idx + 2], pkt[idx + 3]));
	}


	public static Long get16bitValue(byte[] pkt, int idx){
		return Utils.getUnsignedInt(Utils.get_uint16(pkt[idx], pkt[idx + 1]));
	}

	public static Long get8bitValue(byte[] pkt, int idx){
		return Utils.getUnsignedInt(pkt[idx]);
	}
}


