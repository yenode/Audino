package com.audino;

import com.audino.controller.MainController;
import com.audino.service.MongoService;
import com.audino.util.ConfigurationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class AudinoApplication extends Application {

    private MainController mainController;

    @Override
    public void start(Stage primaryStage) throws IOException {
        ConfigurationManager.getInstance().initialize();
        MongoService.getInstance().initialize();
        loadFonts();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        BorderPane root = loader.load();
        mainController = loader.getController();

        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

        primaryStage.setTitle("Audino: Intelligent Prescription Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();
    }

    private void loadFonts() {
        try {
            Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Light.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Medium.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-SemiBold.ttf"), 10);
        } catch (Exception e) {
            System.err.println("Could not load fonts: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        if (mainController != null) {
            mainController.shutdown();
        }
        MongoService.getInstance().close();
        System.out.println("Audino application has been stopped.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}