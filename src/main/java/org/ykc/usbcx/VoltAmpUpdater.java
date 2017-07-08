package org.ykc.usbcx;

import java.text.NumberFormat;
import java.util.Locale;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Stop;

public class VoltAmpUpdater implements Runnable{
	boolean isRunning;
	USBControl usbControl;
    private Label lblVolt;
    private Label lblCur;
    private Label lblCC1;
    private Label lblCC2;
	String voltString = "0";
	String curString = "0";
	String cc1String = "0";
	String cc2String = "0";

	public VoltAmpUpdater(USBControl usbControl, Label lblVolt, Label lblCur, Label lblCC1, Label lblCC2) {
		this.usbControl = usbControl;
		this.lblCur = lblCur;
		this.lblVolt = lblVolt;
		this.lblCC1 = lblCC1;
		this.lblCC2 = lblCC2;
	}

	public void resume(){
		isRunning = true;
	}

	public void pause(){
		isRunning = false;
		lblVolt.setText("0");
		lblCur.setText("0");
		lblCC1.setText("0");
		lblCC2.setText("0");
	}
	@Override
	public void run() {
		while(true){
			if(isRunning == true){
				byte[] voltAmp = new byte[8];
				if(usbControl.getVoltAmp(voltAmp) == true){
					int vbus = Utils.get_uint16(voltAmp[0], voltAmp[1]);
					int cur = Utils.get_uint16(voltAmp[2], voltAmp[3]);
					int cc1 = Utils.get_uint16(voltAmp[4], voltAmp[5]);
					int cc2 = Utils.get_uint16(voltAmp[6], voltAmp[7]);
                    if (cur < 2048)
                    	cur = ((2048 - cur) * 100)/ 36;
                    else
                    	cur = ((cur - 2048) * 100)/ 36;
					vbus = (vbus  * 3296 * 11) / 4096;
					cc1 = (cc1  * 3296 ) / 4096;
					cc2 = (cc2  * 3296 ) / 4096;
					voltString = NumberFormat.getNumberInstance(Locale.US).format(vbus);
					curString = NumberFormat.getNumberInstance(Locale.US).format(cur);
					cc1String = NumberFormat.getNumberInstance(Locale.US).format(cc1);
					cc2String = NumberFormat.getNumberInstance(Locale.US).format(cc2);
					Platform.runLater(() -> {
						lblVolt.setText(voltString);
						lblCur.setText(curString);
						lblCC1.setText(cc1String);
						lblCC2.setText(cc2String);
			        });
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

	}

}
