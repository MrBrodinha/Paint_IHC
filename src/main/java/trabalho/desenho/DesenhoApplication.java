package trabalho.desenho;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class DesenhoApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(DesenhoApplication.class.getResource("desenho.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 650);
        stage.setTitle("Desenho");
        Image icon= new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/iconapp.png"));
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}