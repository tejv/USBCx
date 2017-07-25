package org.ykc.usbcx;

public class ScopeCollecter {
	private static final int SCOPE_PKT_SIZE = 64;
	private static final int MAX_SAMPLES_IN_PKT = 5;
	public static void run(byte[] inputArray, PageQueue pQueue, int length){
		int pkt_count = length / SCOPE_PKT_SIZE;
        for (int i = 0; i < pkt_count; i = i + SCOPE_PKT_SIZE)
        {
        	for(int j = 0; j < MAX_SAMPLES_IN_PKT; j++){
        		int sampleIdx = i + j * 12;
        		int timeStamp = Utils.get_uint32(inputArray[sampleIdx + 0], inputArray[sampleIdx + 1], inputArray[sampleIdx + 2], inputArray[sampleIdx + 3]);
				int vbus = Utils.get_uint16(inputArray[sampleIdx + 4], inputArray[sampleIdx + 5]);
				int cc1 = Utils.get_uint16(inputArray[sampleIdx + 6], inputArray[sampleIdx + 7]);
				int cc2 = Utils.get_uint16(inputArray[sampleIdx + 8], inputArray[sampleIdx + 9]);
				int cur = Utils.get_uint16(inputArray[sampleIdx + 10], inputArray[sampleIdx + 11]);
	            if (cur < 2048)
	            	cur = ((2048 - cur) * 100)/ 36;
	            else
	            	cur = ((cur - 2048) * 100)/ 36;
				vbus = (vbus  * 3296 * 11) / 4096;
				cc1 = (cc1  * 3296 ) / 4096;
				cc2 = (cc2  * 3296 ) / 4096;

				pQueue.addScopeSample(new DataNode(timeStamp, (short)cc1, (short)cc2, (short)vbus, (short)cur));
        	}
        }
	}
}
