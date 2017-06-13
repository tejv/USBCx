package org.ykc.usbcx;

import java.io.Serializable;
import java.util.Arrays;


@SuppressWarnings("serial")
public class DataPage implements Serializable{
	private int maxPageSize = 8000;
	private byte[][] packetList;
	private int index;

	public DataPage() {
		this(8000);
	}

	public DataPage(int maxPageSize) {
		this.maxPageSize = maxPageSize;
		packetList = new byte[maxPageSize][];
		index = -1;
	}

	public boolean add(byte[] item){

		if((index + 1) >= maxPageSize){
			return false;
		}
		int temp = index + 1;
		packetList[temp] = item;
		index++;
		return true;
	}

	public boolean isFull(){
		if((index + 1) >= maxPageSize){
			return true;
		}
		return false;
	}

	public byte[] getItem(int idx){
		try {
			return packetList[idx];
		} catch (Exception e) {
			throw new RuntimeException("Invalid index");
		}
	}

	public int getSize(){
		return index + 1;
	}
}
