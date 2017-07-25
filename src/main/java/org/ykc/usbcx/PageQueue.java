package org.ykc.usbcx;

import java.io.File;
import java.util.ArrayList;
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
	private ArrayList<DataNode> scopeBuffer = new ArrayList<DataNode>();
	private final int MAX_SCOPE_SAMPLES = 300000;

	public PageQueue() {

	}

	public void clear(){
		scopeBuffer.clear();
		pageList.clear();
		curPage =  new DataPage();
		publishPageChangeEvent(curPage);
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

	public boolean isScopeBufferFull() {
		if(scopeBuffer.size() >= MAX_SCOPE_SAMPLES){
			return true;
		}
		return false;
	}

	public boolean addScopeSample(DataNode node){
		if(scopeBuffer.size() == 0){
			scopeBuffer.add(new DataNode(node.getTimeStamp()-2, (short)0, (short)0, (short)0, (short)0));
			scopeBuffer.add(new DataNode(node.getTimeStamp()-1, node.getCc1(), node.getCc2(), node.getVolt(), node.getAmp()));
		}
		scopeBuffer.add(node);
		return true;
	}

	public ArrayList<DataNode> getScopeSamples(){
		return scopeBuffer;
	}
}
