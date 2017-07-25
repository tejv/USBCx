package org.ykc.usbcx;

import java.io.Serializable;

public class DataNode  implements Serializable{
	private int timeStamp;
	private short cc1;
	private short cc2;
	private short volt;
	private short amp;


	public DataNode(int timeStamp, short cc1, short cc2, short volt, short amp) {
		this.timeStamp = timeStamp;
		this.cc1 = cc1;
		this.cc2 = cc2;
		this.volt = volt;
		this.amp = amp;
	}

	public int getTimeStamp() {
		return timeStamp;
	}
	public short getCc1() {
		return cc1;
	}
	public short getCc2() {
		return cc2;
	}
	public short getVolt() {
		return volt;
	}
	public short getAmp() {
		return amp;
	}

}
