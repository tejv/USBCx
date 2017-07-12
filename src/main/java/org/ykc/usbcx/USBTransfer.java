package org.ykc.usbcx;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.usb.UsbDevice;
import javax.usb.UsbException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ykc.usbmanager.*;


public class USBTransfer implements Runnable{
	public static final Logger logger = LoggerFactory.getLogger(USBTransfer.class.getName());
	public static  final short VID = 0x04B4;
	public static  final short PID = 0x0078;
	static  final byte IN_EP_CC_DATA = (byte)0x81;
	static  final byte OUT_EP_CMD = (byte)0x2;
	static  final byte IN_EP_MEASURE = (byte)0x83;
	static  final byte IN_EP_CMD_RESP = (byte)0x84;
	static  final int MAX_READ_SIZE = 65535;
	private final int SHORT_PKT_SIZE = 8;
	private byte[] tempDataArray = new byte[MAX_READ_SIZE];
	private volatile boolean isRunning = false;
	private volatile boolean isTerminated = false;
	private UsbDevice dev = null;
	private boolean dataTransferStopped = true;
	private PageQueue pageQueue = new PageQueue();
	private PageSave pageSave = new PageSave();
	private PktCollecter pktCollecter = new PktCollecter();


	public PageSave getPageSave() {
		return pageSave;
	}

	public PageQueue getPageQueue() {
		return pageQueue;
	}

	USBTransfer()
	{
		this(null);
	}

	USBTransfer(UsbDevice newDev)
	{
		dev = newDev;
		Thread saveThread = new Thread(pageSave);
		saveThread.start();
		startSaving();
	}

	public void setDevice(UsbDevice newDev)
	{
		dev = newDev;
	}

	public UsbDevice getDevice()
	{
		return dev;
	}

	private void startSaving(){
		ZonedDateTime time = ZonedDateTime.now();
		String timeString = String.format("%04d", time.getYear())  + "_" +
							String.format("%02d", time.getMonthValue()) + "_" +
				            String.format("%02d", time.getDayOfMonth()) + "_" +
				            String.format("%02d", time.getHour()) + "_" +
				            String.format("%02d", time.getMinute()) + "_" +
				            String.format("%02d", time.getSecond());

		File logDir = new File(Preferences.getLogDir(), timeString);
		logger.info("Time: " + timeString);
		logger.info("Log Directory path: " + logDir.getAbsolutePath());

		if (! logDir.exists()) {
			logDir.mkdirs();
		}
		String logFileName = "USBCx_" + timeString;
		logger.info("Log File Name: " + logFileName);
		pageSave.start(logDir.getAbsolutePath(), logFileName, pageQueue);
	}

	public boolean start(Long config)
	{
		pageQueue.clear();
		pageSave.stop();
		pktCollecter.clear();
		startSaving();
		byte[] command = new byte[8];

		command[0] = 16;
		command[4] = Utils.uint32_get_b0(config);
		command[5] = Utils.uint32_get_b1(config);
		command[6] = Utils.uint32_get_b2(config);
		command[7] = Utils.uint32_get_b3(config);


		if(USBManager.epXfer(dev, OUT_EP_CMD, command) > 0)
		{
			return true;
		}
		return false;
	}

	public boolean stop()
	{
		/* TODO */
		// residualQueue.clear();
		byte[] command = new byte[4];
		command[0] = 3;

		if(USBManager.epXfer(dev, OUT_EP_CMD, command) > 0)
		{
			return true;
		}
		return false;

	}

	public boolean setTigger(byte[] cmd)
	{
		if(USBManager.epXfer(dev, OUT_EP_CMD, cmd) > 0)
		{
			return true;
		}
		return false;
	}

	public boolean getVersion(byte[] readBuf)
	{
		byte[] command = new byte[4];
		command[0] = 4;

		if(USBManager.epXfer(dev, OUT_EP_CMD, command) <= 0)
		{
			return false;
		}

		if(USBManager.epXfer(dev, IN_EP_CMD_RESP, readBuf) == 4)
		{
			return true;
		}
		return false;
	}

	public boolean putDeviceInBootloadMode()
	{
		byte[] command = new byte[4];
		command[0] = 0x5;

		if(USBManager.epXfer(dev, OUT_EP_CMD, command) == 4)
		{
			return true;
		}
		return false;
	}

	public void resetQueue(){
		pageQueue.clear();
	}

	private boolean checkInTransfer()
	{
		byte[] command = new byte[MAX_READ_SIZE];

		if(USBManager.epXfer(dev, IN_EP_CC_DATA, command) > 0)
		{
			return true;
		}
		return false;

	}

	public PageQueue getDataQueue(){
		return pageQueue;
	}

	public void pause()
	{
		isRunning = false;
	}

	public void resume()
	{
		isRunning = true;
		dataTransferStopped = false;
	}

	@Override
	public void run() {

		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
		}

		while(true)
		{
			if(isTerminated){
				break;
			}
			if(isRunning)
			{
				try {
					int size = USBManager.epXfer(dev, IN_EP_CC_DATA, tempDataArray);
					if (size > SHORT_PKT_SIZE)
					{
						/* Extract packets and store */
						pktCollecter.run(tempDataArray, pageQueue, size);
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			else
			{
				dataTransferStopped = true;
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}

		}
	}

	public boolean isTransferStopped()
	{
		return dataTransferStopped;
	}

	public boolean setTerm(byte[] cmd) {
		if(USBManager.epXfer(dev, OUT_EP_CMD, cmd) <= 0)
		{
			return false;
		}
		return true;
	}

	public boolean getVoltCur(byte[] readBuf)
	{
		byte[] command = new byte[4];
		command[0] = 17;

		if(USBManager.epXfer(dev, OUT_EP_CMD, command) <= 0)
		{
			return false;
		}
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
		}

		if(USBManager.epXfer(dev, IN_EP_CMD_RESP, readBuf) == 8)
		{
			return true;
		}
		return false;
	}

	public void terminate() {
		isTerminated = true;

	}
}

