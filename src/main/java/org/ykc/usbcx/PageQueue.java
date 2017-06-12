package org.ykc.usbcx;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class PageQueue {
	public static final Logger logger = LoggerFactory.getLogger(PageQueue.class.getName());
	private ConcurrentLinkedQueue<DataPage> pageList = new ConcurrentLinkedQueue<DataPage>();
	private DataPage curPage = new DataPage();
	private ObservableList<IPageChangeListener> listenerList = FXCollections.observableArrayList();

	public PageQueue() {

	}

	public void clear(){
		curPage =  new DataPage();
		pageList.clear();
	}

	public boolean isEmpty(){
		return pageList.isEmpty();
	}

	public boolean enqueue(DataPage newPage){
		return pageList.add(newPage);
	}

	public DataPage dequeue(){
		return pageList.remove();
	}

	public void add(byte[] dataArray){
		if(curPage.add(dataArray) == false){
			enqueue(curPage);
			curPage = new DataPage();
			publishPageChangeEvent(curPage);
		}
	}

	public void addListener(IPageChangeListener listener){
		listenerList.add(listener);
	}

	private void publishPageChangeEvent(DataPage newPage){
		for(int i = 0 ; i < listenerList.size(); i++){
			IPageChangeListener listener = listenerList.get(i);
			listener.pageChanged(newPage);
		}
	}

	public DataPage getCurPage(){
		return curPage;
	}
}
