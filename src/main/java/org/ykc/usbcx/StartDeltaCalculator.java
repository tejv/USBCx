package org.ykc.usbcx;

import java.text.NumberFormat;
import java.util.Locale;

import javafx.scene.control.Label;

public class StartDeltaCalculator {
	static long oldStartTime = 0;
	public static void setDelta(byte[] pkt, Label lblStartDelta){
		Long delta = 0L;
		Long newStartTime = PDUtils.get32bitValue(pkt, PktCollecter.TIME_START_BYTE0_IDX);
		if(newStartTime >= oldStartTime){
			delta = newStartTime - oldStartTime;
		}
		else{
			delta =  oldStartTime - newStartTime;
		}
		oldStartTime = newStartTime;
		lblStartDelta.setText(NumberFormat.getNumberInstance(Locale.US).format(delta) + " us");
	}
}
