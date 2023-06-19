module trabalho.desenho {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;


    opens trabalho.desenho to javafx.fxml;
    exports trabalho.desenho;
}