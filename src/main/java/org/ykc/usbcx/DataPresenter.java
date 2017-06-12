package org.ykc.usbcx;

import javax.xml.stream.events.StartDocument;

import javafx.scene.paint.Stop;

public class DataPresenter implements Runnable{
	private static boolean isRunning = false;
	private static DataPage curPage;


	public static DataPage getCurPage() {
		return curPage;
	}

	public static void setCurPage(DataPage curPage) {
		DataPresenter.curPage = curPage;
	}

	public void start(){
		isRunning = true;
	}

	public void stop(){
		isRunning = false;
	}

	public void clear(){

	}

	@Override
	public void run() {
//		int sleep_counter = 0;
//		while(true)
//		{
//			if(isRunning)
//			{
//				if(curPage != null){
//					while(curPage.getSize() == false)
//					{
//						sleep_counter++;
//						if(sleep_counter > 1000)
//						{
//							sleep_counter = 0;
//							try {
//								Thread.sleep(50);
//							} catch (InterruptedException e) {
//							}
//						}
//						try {
//
//
//							Integer[] tRow;
//							tRow = rawQueue.peek();
//							addToModel(tRow);
//							rawQueue.remove();
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//
//			try {
//				Thread.sleep(350);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//			}
//		}
	}

}
