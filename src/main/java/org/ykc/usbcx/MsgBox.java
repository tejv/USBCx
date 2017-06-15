package org.ykc.usbcx;

import javafx.stage.*;

import java.awt.Color;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.*;
import javafx.scene.image.Image;

public class MsgBox {
    public static void display(String title, String message) {
        Stage window = new Stage();

        //Block events to other windows
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(800);
        window.getIcons().add(new Image("main_app_icon.png"));

        TextArea area = new TextArea();
        area.setEditable(false);
        
        area.setText(message);


        BorderPane layout = new BorderPane();
        layout.setCenter(area);

        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout);
        scene.getStylesheets().add("application.css");
        window.setScene(scene);
        window.showAndWait();
    }
}
