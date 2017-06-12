package org.ykc.usbcx;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PktCollecter {
	static  final int CC_PACKET_SIZE = 64;
	static final int CC_PACKET_NON_DATA_SIZE = 20;
	static final int CC_PACKET_DATA_SIZE = 44;
	static  final int CC_PACKET_WORDS = CC_PACKET_SIZE / 4;
	static final int HEADER_BYTE0_IDX = 4;
	static final int EXTD_HEADER_BYTE0_IDX = 20;

	BlockingQueue<byte[]> residualQueue = new LinkedBlockingQueue<>();

	void clear(){
		residualQueue.clear();
	}

	public void run(byte[] inputArray, PageQueue pQueue, int length){
		byte[] dataArray;
		int size;
		if(residualQueue.isEmpty()){
			dataArray = inputArray;
			size = length;
		}
		else{
			size = residualQueue.size() * CC_PACKET_DATA_SIZE + length;
			byte[] newArray = new byte[size];
			int tempPktIdx = 0;
			while(!residualQueue.isEmpty()){
				System.arraycopy(residualQueue.remove(), 0, newArray, tempPktIdx, CC_PACKET_SIZE);
				tempPktIdx += CC_PACKET_SIZE;
			}
			System.arraycopy(inputArray, 0, newArray, tempPktIdx, length);
			dataArray = newArray;		
		}

		int pkt_count = size / CC_PACKET_SIZE;
        for (int i = 0; i < pkt_count; i++)
        {
        	int dataBytes = CC_PACKET_NON_DATA_SIZE;
        	int arrayIndex = i* CC_PACKET_SIZE;
        	int hdrIdx = arrayIndex + HEADER_BYTE0_IDX;
        	int extdHdrIdx = arrayIndex + EXTD_HEADER_BYTE0_IDX;
        	Long hdr =  (long) Utils.byteToInt(dataArray[hdrIdx], dataArray[hdrIdx + 1], dataArray[hdrIdx + 2], dataArray[hdrIdx + 3]);
        	int extdHdr = Utils.get_uint16(dataArray[extdHdrIdx], dataArray[extdHdrIdx + 1]);
        	if((PDUtils.get_field_extended(hdr) == true) && 
        	   (PDUtils.get_field_is_chunked(extdHdr) == false) && (PDUtils.get_field_chunk_no(hdr) == 0))
        	{
        		int extdBytes = PDUtils.get_field_extended_count(extdHdr);
        		int pCount = Utils.roundUp(extdBytes, CC_PACKET_DATA_SIZE);
        		if(pCount == 1){
            		dataBytes += extdBytes;
            		pQueue.add(Arrays.copyOfRange(dataArray, arrayIndex, arrayIndex + dataBytes- 1 ));        			
        		}
        		else if(pkt_count <= (i + pCount) ){
        			int curIndex = i;
        			byte[] tempPkt = new byte[1000];
        			int tempPktIdx = 0;
        			while(i < (curIndex + pCount)){
        				arrayIndex = i* CC_PACKET_SIZE;
        				int startIdx = arrayIndex;
        				int len = CC_PACKET_SIZE;
        				if(i != curIndex){
        					startIdx += CC_PACKET_NON_DATA_SIZE;
        					len = CC_PACKET_DATA_SIZE;
        				}
        				
        				System.arraycopy(dataArray, startIdx, tempPkt, tempPktIdx, len);
        				if(i == (curIndex + pCount - 1))
        				{
        					/* Last chunk */
        					pQueue.add(Arrays.copyOfRange(tempPkt, 0, CC_PACKET_NON_DATA_SIZE + extdBytes - 1 ));
        				}
        				tempPktIdx += CC_PACKET_SIZE;
        				i++;
        			} 
        			i--;
        		}
        		else{
        			while(i < pkt_count){
        				arrayIndex = i* CC_PACKET_SIZE;
        				residualQueue.add(Arrays.copyOfRange(dataArray, arrayIndex, arrayIndex + CC_PACKET_SIZE- 1 ));
        				i++;
        			}
        		}
        	}
        	else{
        		dataBytes += PDUtils.get_field_msg_count(hdr) * 4;
        		pQueue.add(Arrays.copyOfRange(dataArray, arrayIndex, arrayIndex + dataBytes- 1 ));
        	}	
        }
	}
}
