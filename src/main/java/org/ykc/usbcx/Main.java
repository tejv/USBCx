package org.ykc.usbcx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	public static final Logger logger = LoggerFactory.getLogger(Main.class.getName());
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
			BorderPane root = (BorderPane) loader.load();
			MainWindowController controller = (MainWindowController)loader.getController();
			Scene scene = new Scene(root,900,700);
			scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("USBCx");
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/main_app_icon.png")));
			primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		logger.info("Application starting");
		launch(args);
	}
}
