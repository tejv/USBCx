package org.ykc.usbcx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

public class DetailsLoader {
	public static void run(ObservableList<DetailsRow> list, TreeTableView<DetailsRow> ttView){
		ObservableList<TreeItem<DetailsRow>> treeList= FXCollections.observableArrayList();
		ttView.getRoot().getChildren().clear();
		int lastLevel = 0;
		TreeItem<DetailsRow> lastRootItem = ttView.getRoot();
		TreeItem<DetailsRow> lastTreeItem = lastRootItem;
		for(int i = 0; i < list.size(); i++){
			DetailsRow f = list.get(i);
			int fLevel = f.getLevel();
			TreeItem<DetailsRow> x = new TreeItem<DetailsRow>(f);

			if(fLevel > lastLevel){
				lastRootItem = lastTreeItem;
			}
			else if(fLevel < lastLevel){
				int lastRootIdx = findLastMatchingLevel(list, i, fLevel -1);
				if(lastRootIdx == 0)
				{
					lastRootItem = ttView.getRoot();
				}
				else {
					lastRootItem = treeList.get(lastRootIdx);
				}
			}
			treeList.add(x);
			lastRootItem.getChildren().add(x);
			lastTreeItem = x;
			lastLevel = fLevel;
		}
		//expandTreeView(ttView.getRoot());
	}

	private static int findLastMatchingLevel(ObservableList<DetailsRow> list, int max_count, int expLevel){
		for(int i = max_count; i >= 0; i--){
			if(list.get(i).getLevel() == expLevel)
			{
	  			return i;
			}
		}
		return 0;
	}
	
	private static void expandTreeView(TreeItem<?> item){
	    if(item != null && !item.isLeaf()){
	        item.setExpanded(true);
	        for(TreeItem<?> child:item.getChildren()){
	            expandTreeView(child);
	        }
	    }
	}
	
	private static void collapseTreeView(TreeItem<?> item){
	    if(item != null && !item.isLeaf()){
	        item.setExpanded(false);
	        for(TreeItem<?> child:item.getChildren()){
	            collapseTreeView(child);
	        }
	    }
	}
}
