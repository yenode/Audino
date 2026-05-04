package com.audino;

import com.audino.util.ConfigurationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class AudinoApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        ConfigurationManager.getInstance().initialize();
        loadFonts();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        VBox root = loader.load();

        Scene scene = new Scene(root, 400, 350);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

        primaryStage.setTitle("Audino: Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
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

    public static void main(String[] args) {
        launch(args);
    }
}