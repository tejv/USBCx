package org.ykc.usbcx;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class Utils {
	public enum Radix {
		RADIX_HEX, RADIX_DECIMAL, RADIX_BINARY
	}
	public static boolean isCharHex(char c) {
		return ((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F'));
	}

	public static boolean isCharBinary(char c) {
		return ((c == '0') || (c == '1'));
	}

	public static String getSubString(String x, int start, int length) {
	    return x.substring(start, Math.min(start + length, x.length()));
	}

	public static byte uint16_get_lsb(int input) {
		return (byte) (input & 0xFF);
	}

	public static byte uint16_get_msb(int input) {
		return (byte) (input >>> 8);
	}

	public static byte uint32_get_b0(Long input) {
		return (byte) (input & 0xFF);
	}

	public static byte uint32_get_b1(Long input) {
		return (byte) ((input >>> 8) & 0xFF);
	}

	public static byte uint32_get_b2(Long input) {
		return (byte) ((input >>> 16) & 0xFF);
	}

	public static byte uint32_get_b3(Long input) {
		return (byte) ((input >>> 24) & 0xFF);
	}

	public static int get_uint32(byte b0, byte b1, byte b2, byte b3) {
		return ((b0 & 0xFF) | ((b1 & 0xFF) << 8) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 24));
	}

	public static short get_uint16(byte b0, byte b1) {
		return (short) ((b0 & 0xFF) | ((b1 & 0xFF) << 8));
	}

	public static int byteToInt(byte b0, byte b1, byte b2, byte b3) {
		return ((b0 & 0xFF) | ((b1 & 0xFF) << 8) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 24));
	}

	public static long castInttoLong(int x) {
		return x & 0x00000000ffffffffL;
	}

	public static int castLongtoUInt(long x) {
		return (int) (x & 0x00000000ffffffffL);
	}

	public static Long getLRightShift(Long x, Integer shift) {
		return x >>> shift;
	}

	public static Long getLLeftShift(Long x, Integer shift) {
		return x << shift;
	}

	public static Long get32bitMask(Integer bitCount) {
		if (bitCount > 32) {
			return 0L;
		}
		Long mask = (1L << bitCount) - 1;
		return mask;
	}

	public static Long getMaskValue(Long value, Integer offset, Integer size) {
		return (value >>> offset) & get32bitMask(size);
	}

	public static Long parseStringtoNumber(String input) {
		Long value = 0L;
		try {
			if((input == null) || input.isEmpty()){
				value = 0L;
			}
			else if (input.startsWith("0x")) {
				value = Long.parseLong(input.substring(2), 16);
			} else if (input.startsWith("0b")) {
				value = Long.parseLong(input.substring(2), 2);
			} else {
				value = Long.parseLong(input, 10);
			}
		} catch (NumberFormatException e) {
			return 0L;
		}
		return value;
	}

	public static String longToString(Long value, Radix radix){
		String x = "";
		switch (radix) {
		case RADIX_BINARY:
			x = "0b" + Long.toBinaryString(value);
			break;
		case RADIX_DECIMAL:
			x = Long.toString(value);
			break;
		default:
			x = "0x" + Long.toHexString(value).toUpperCase();
			break;
		}
		return x;
	}

	public static File getFileNewExtension(File input, String extension){
		String nameString = input.getName();
		String[] partsStrings = nameString.split("\\.");
		String newFile = input.getParentFile() + "\\" + partsStrings[0] + "." + extension;
		return new File(newFile);

	}

	public static String getFileExtension(File input){
		String outString = "";
		if(input.exists())
		{
			outString = FilenameUtils.getExtension(input.getName());
		}
		return outString;
	}

	public static void main(String[] args) {

		getFileNewExtension(new File(System.getProperty("user.home"), "BitPro/preferences/app.xml"), "temp");
	}

	public static String fixedLengthStringRightAlign(String string, int length) {
	    return String.format("%1$"+length+ "s", string);
	}
	public static String fixedLengthStringLeftAlign(String string, int length) {
		int appendLen = length -string.length();

		if(appendLen > 0){
			for(int i = 0; i < appendLen; i++ ){
				string += " ";
			}
		}
		return string;
	}

	public static String intToHexWithPadding(Integer input, Integer max_size){
		if(max_size <= 8){
			return String.format("%02x", input);
		}else if(max_size <= 16){
			return String.format("%04x", input);
		}
		else{
			return String.format("%08x", input);
		}
	}

	public static File getNewFileCopy(File file, String newExtension) throws IOException{
		File newFile = getFileNewExtension(file, newExtension);
		FileUtils.copyFile(file, newFile);
		return newFile;
	}

	public static int roundUp(int num, int divisor) {
	    return (num + divisor - 1) / divisor;
	}

	public static long getUnsignedInt(int x) {
		return x & 0x00000000ffffffffL;
	}

	public static long getUnsignedInt(short x) {
		return x & 0x000000000000ffffL;
	}

	public static long getUnsignedInt(byte x) {
		return x & 0x000000000000ffL;
	}


	public static String bytesToString(byte[] bytes) {
	 CharBuffer cBuffer = ByteBuffer.wrap(bytes).asCharBuffer();
	 return cBuffer.toString();
	}
}

