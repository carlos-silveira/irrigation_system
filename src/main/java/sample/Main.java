package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Sistema de riego");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
    }


    public static void main(String[] args) {
//        new Arduino(new ConexionMySQL("root", "", "sistemaRiego", "localhost", 3306));
        launch(args);
    }
}
