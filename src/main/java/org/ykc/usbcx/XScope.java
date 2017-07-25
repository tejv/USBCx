package org.ykc.usbcx;

import java.lang.invoke.SwitchPoint;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.naming.InitialContext;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class XScope {
	private static final int CC1_SERIES_IDX = 0;
	private static final int CC2_SERIES_IDX = 1;
	private static final int VBUS_SERIES_IDX = 2;
	private static final int AMP_SERIES_IDX = 3;
	public static final String[] X_SCALE = {"100ms/div","50ms/div","25ms/div", "20ms/div","10ms/div","5ms/div", "3ms/div","2ms/div","1ms/div"};
	private LineChart<Number,Number> lchartData;
	private ComboBox<String> cboxGraphXScale;
	private Button bGraphScrollLeft;
	private Button bGraphScrollRight;
	private NumberAxis xAxis;
	private NumberAxis yAxis;
    private CheckBox chkGraphCC1;
    private CheckBox chkGraphCC2;
    private CheckBox chkGraphVbus;
    private CheckBox chkGraphAmp;


    private Label lblGraphYValue;
    private Label lblGraphXValue;
    private Label lblGraphDeltaY;
    private Label lblGraphDeltaX;

	private XYChart.Series<Number, Number> seriesCC1;
	private XYChart.Series<Number, Number> seriesCC2;
	private XYChart.Series<Number, Number> seriesVbus;
	private XYChart.Series<Number, Number> seriesAmp;
	private XYChart.Series<Number, Number> seriesHMarker;
	private XYChart.Series<Number, Number> seriesVMarker;
	Node chartPlotArea;
	private int maxPoints = 1000;
	private int liveStartIdx = 0;
	int pointCounter = 0;
	ArrayList<DataNode> dataPoints;
	boolean cc1En = true;
	boolean cc2En = true;
	boolean vbusEn = true;
	boolean ampEn = true;
	Double xValueOld = 0.0;
	Double yValueOld = 0.0;
	int curScale = 0;



	public XScope(LineChart<Number, Number> lchartData, NumberAxis xAxis, NumberAxis yAxis, ComboBox<String> cboxGraphXScale,
		    Button bGraphScrollLeft, Button bGraphScrollRight, CheckBox chkGraphCC1, CheckBox chkGraphCC2, CheckBox chkGraphVbus,
			CheckBox chkGraphAmp, Label lblGraphYValue, Label lblGraphXValue, Label lblGraphDeltaY, Label lblGraphDeltaX) {
		this.lchartData = lchartData;
		this.cboxGraphXScale = cboxGraphXScale;
		this.bGraphScrollLeft = bGraphScrollLeft;
		this.bGraphScrollRight = bGraphScrollRight;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.chkGraphCC1 = chkGraphCC1;
		this.chkGraphCC2 = chkGraphCC2;
		this.chkGraphVbus = chkGraphVbus;
		this.chkGraphAmp = chkGraphAmp;
		this.lblGraphYValue = lblGraphYValue;
		this.lblGraphXValue = lblGraphXValue;
		this.lblGraphDeltaY = lblGraphDeltaY;
		this.lblGraphDeltaX = lblGraphDeltaX;
		init();
	}

	private void chartInit(){
		lchartData.setAnimated(false);
		lchartData.setCreateSymbols(false);
		lchartData.getXAxis().setAutoRanging(false);
		xAxis.setTickUnit(100000);
		yAxis.setAutoRanging(true);
		//lchartData.getYAxis().setAutoRanging(false);
		//lchartData.getXAxis().setTickLabelsVisible(false);
		//lchartData.getXAxis().setTickMarkVisible(false);
    	lchartData.getXAxis().setLabel("Time (us)");
    	lchartData.getYAxis().setLabel("Volt/Amp (mV/mA)");

    	seriesCC1 = new XYChart.Series<Number,Number>();
    	seriesCC1.setName("CC1");
    	seriesCC2 = new XYChart.Series<Number,Number>();
    	seriesCC2.setName("CC2");
    	seriesVbus = new XYChart.Series<Number,Number>();
    	seriesVbus.setName("VBUS");
    	seriesAmp = new XYChart.Series<Number,Number>();
    	seriesAmp.setName("Amp");
//    	seriesHMarker = new XYChart.Series<Number,Number>();
//    	seriesHMarker.setName("HMarker");
//    	seriesVMarker = new XYChart.Series<Number,Number>();
//    	seriesVMarker.setName("Vmarker");

        lchartData.getData().addAll(seriesVbus, seriesAmp, seriesCC1, seriesCC2);
		chartPlotArea = lchartData.lookup(".chart-plot-background");
		chartPlotArea.setOnMouseMoved(e -> mouseMoved(e) );
		chartPlotArea.setOnMouseClicked(e -> mouseClicked(e) );
		chartPlotArea.setOnScroll(e -> graphZoom(e) );
	}

	private void panelInit(){
		cboxGraphXScale.getItems().addAll(X_SCALE);
		cboxGraphXScale.getSelectionModel().select(0);
		cboxGraphXScale.setOnAction(e -> changeXscale(e) );
        bGraphScrollLeft.setOnAction(e -> graphScrollLeft(e) );
        bGraphScrollRight.setOnAction(e -> graphScrollRight(e) );
        chkGraphCC1.setOnAction(e -> enableDisableCC1(e) );
        chkGraphCC2.setOnAction(e -> enableDisableCC2(e) );
        chkGraphVbus.setOnAction(e -> enableDisableVBUS(e) );
        chkGraphAmp.setOnAction(e -> enableDisableAmp(e) );
	}

	private void setXscale(int idx){
		switch (idx) {
		case 0: //100ms/div
			maxPoints = 1000;
			xAxis.setTickUnit(100000);
			break;
		case 1: // 50ms/div
			maxPoints = 500;
			xAxis.setTickUnit(50000);
			break;
		case 2: // 25
			maxPoints = 250;
			xAxis.setTickUnit(25000);
			break;
		case 3: // 20
			maxPoints = 200;
			xAxis.setTickUnit(20000);
			break;
		case 4: // 10
			maxPoints = 100;
			xAxis.setTickUnit(10000);
			break;
		case 5: // 5
			maxPoints = 50;
			xAxis.setTickUnit(5000);
			break;
		case 6: // 3
			maxPoints = 30;
			xAxis.setTickUnit(3000);
			break;
		case 7: // 2
			maxPoints = 20;
			xAxis.setTickUnit(2000);
			break;
		case 8: // 1
			maxPoints = 10;
			xAxis.setTickUnit(1000);
			break;
		default:
			break;
		}
		reload();
	}

	private void changeXscale(ActionEvent e) {
		int idx = cboxGraphXScale.getSelectionModel().getSelectedIndex();
		curScale = idx;
		setXscale(curScale);
	}

	private void enableDisableCC1(ActionEvent e){
		cc1En = chkGraphCC1.isSelected();
	    refreshChart();
	}

	private void enableDisableCC2(ActionEvent e){
		cc2En = chkGraphCC2.isSelected();
	    refreshChart();
	}

	private void enableDisableVBUS(ActionEvent e){
		vbusEn = chkGraphVbus.isSelected();
	    refreshChart();
	}

	private void enableDisableAmp(ActionEvent e){
		ampEn = chkGraphAmp.isSelected();
	    refreshChart();
	}

	private void init(){
		chartInit();
        panelInit();
	}

	private void incCurScale(){
		if(curScale < 8){
			curScale++;
			cboxGraphXScale.getSelectionModel().select(curScale);
		}
	}

	private void decCurScale(){
		if(curScale > 0 ){
			curScale--;
			cboxGraphXScale.getSelectionModel().select(curScale);
		}
	}

	private void graphZoom(ScrollEvent e) {
		int xValue = xAxis.getValueForDisplay(e.getX()).intValue();
		double direction = e.getDeltaY();
		if(direction > 0){
			incCurScale();
		}
		else{
			decCurScale();
		}
		setXscale(curScale);
		displaySpecificTimeWindow(xValue);
	}

	private void mouseClicked(MouseEvent e) {
		Double xValue = (Double) xAxis.getValueForDisplay(e.getX());
		Double yValue = (Double) yAxis.getValueForDisplay(e.getY());
		Number xDiff = Math.abs(xValueOld - xValue);
		Number yDiff = Math.abs(yValueOld - yValue);
		NumberFormat formatter = NumberFormat.getInstance(Locale.US);
		formatter.setMaximumFractionDigits(0);

		lblGraphDeltaX.setText(formatter.format(xDiff) + "u");
		lblGraphDeltaY.setText(formatter.format(yDiff) + "m");

		xValueOld = xValue;
		yValueOld = yValue;
	}

	private void mouseMoved(MouseEvent e) {
		Number xValue = xAxis.getValueForDisplay(e.getX());
		Number yValue = yAxis.getValueForDisplay(e.getY());
		NumberFormat formatter = NumberFormat.getInstance(Locale.US);
		formatter.setMaximumFractionDigits(0);
		if(xValue != null && yValue != null){
			lblGraphXValue.setText(formatter.format(xValue) + "u");
			lblGraphYValue.setText(formatter.format(yValue) + "m");
			}
	}

	public void setDataPoints(ArrayList<DataNode> dataPoints){
		this.dataPoints = dataPoints;
		reload();
	}

	private void reload(){
		if((dataPoints == null) || (dataPoints.size() == 0)){
			bGraphScrollLeft.setDisable(true);
			bGraphScrollRight.setDisable(true);
			return;
		}
		bGraphScrollLeft.setDisable(true);
		if(dataPoints.size() >= maxPoints){
			bGraphScrollRight.setDisable(false);
		}
		else{
			bGraphScrollRight.setDisable(true);
		}
		liveStartIdx = 0;
		refreshChart();
	}

	private void refreshChart() {
		if((dataPoints == null) || (dataPoints.size() == 0)){
			return;
		}
		int pointsCount = Math.min(maxPoints, (dataPoints.size() - liveStartIdx));
		xAxis.setLowerBound(dataPoints.get(liveStartIdx).getTimeStamp());
		xAxis.setUpperBound(dataPoints.get(liveStartIdx + pointsCount - 1).getTimeStamp());
		loadnDataPoints(liveStartIdx, pointsCount);

	}

	public void updateMaxPoints(int maxPoints){
		seriesCC1.getData().clear();
		seriesCC2.getData().clear();
		seriesVbus.getData().clear();
		seriesAmp.getData().clear();
		pointCounter = 0;
		this.maxPoints = maxPoints;
	}

	private void loadnDataPoints(int startIndex, int count){

		seriesCC1.getData().clear();
		seriesCC2.getData().clear();
		seriesVbus.getData().clear();
		seriesAmp.getData().clear();

		for(int i = startIndex; i < (startIndex + count); i++ ){
			DataNode x = dataPoints.get(i);
			int time = x.getTimeStamp();
			if(cc1En){
				seriesCC1.getData().add(new XYChart.Data<Number,Number>(time, x.getCc1()));
			}
			if(cc2En){
				seriesCC2.getData().add(new XYChart.Data<Number,Number>(time, x.getCc2()));
			}
			if(vbusEn){
				seriesVbus.getData().add(new XYChart.Data<Number,Number>(time, x.getVolt()));
			}
			if(ampEn){
				seriesAmp.getData().add(new XYChart.Data<Number,Number>(time, x.getAmp()));
			}
		}
	}

	private void updateScrollButtons(){
		if((liveStartIdx - maxPoints) < 0){
			bGraphScrollLeft.setDisable(true);
		}
		else{
			bGraphScrollLeft.setDisable(false);
		}
		if(liveStartIdx + maxPoints > dataPoints.size()){
			bGraphScrollRight.setDisable(true);
		}
		else{
			bGraphScrollRight.setDisable(false);
		}
	}

    @FXML
    void graphScrollLeft(ActionEvent event) {
    	if((liveStartIdx - maxPoints) >= 0){
    		liveStartIdx -= maxPoints;
    		refreshChart();
    		bGraphScrollRight.setDisable(false);
    		updateScrollButtons();
    	}
    }

    @FXML
    void graphScrollRight(ActionEvent event) {
    	if((liveStartIdx + maxPoints) <= dataPoints.size()){
    		liveStartIdx += maxPoints;
    		refreshChart();
    		bGraphScrollLeft.setDisable(false);
    		updateScrollButtons();
    	}
    }

    public boolean displaySpecificTimeWindow(int time){
    	if(dataPoints == null){
    		return false;
    	}
    	int i = 0;
    	for(int j = 0; i < dataPoints.size(); j++){
    		int pointsCount = Math.min(maxPoints, (dataPoints.size() - j * maxPoints));
    		if((time >= dataPoints.get(i).getTimeStamp()) && (time <= dataPoints.get(i + pointsCount - 1).getTimeStamp())) {
    			break;
    		}
    		i = i + maxPoints;
    	}
    	if(i < dataPoints.size()){
    		liveStartIdx = i;
    		updateScrollButtons();
    		refreshChart();
    		return true;
    	}
    	return false;
    }

	public void clear() {
		seriesCC1.getData().clear();
		seriesCC2.getData().clear();
		seriesVbus.getData().clear();
		seriesAmp.getData().clear();
		dataPoints = null;
		reload();
	}



}
