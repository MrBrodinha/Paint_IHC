package trabalho.desenho;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

public class DesenhoController {
    @FXML
    private Canvas canvas;
    @FXML
    private ColorPicker color;
    @FXML
    private TextField slide_text;
    @FXML
    private Slider slide;
    @FXML
    private TextField x;
    @FXML
    private TextField y;
    @FXML
    private Label espessura;
    @FXML
    private ToggleButton pincel;
    @FXML
    private ToggleButton borracha;
    @FXML
    private ToggleButton pick;
    @FXML
    private ToggleButton quad;
    @FXML
    private ToggleButton triang;
    @FXML
    private ToggleButton circ;
    @FXML
    private Button imagem;
    @FXML
    private Button criar;
    @FXML
    private Button edit;
    @FXML
    private Button save;
    @FXML
    private ToggleButton lupa;
    @FXML
    private Button botao;
    @FXML
    private Button fractal;

    private static int CANVAS_WIDTH = 740;
    private static final int CANVAS_HEIGHT = 605;
    // Left and right border
    private static final int X_OFFSET = 25;
    // Top and Bottom border
    private static final int Y_OFFSET = 25;
    // Values for the Mandelbro set
    private static double MANDELBROT_RE_MIN = -2;
    private static double MANDELBROT_RE_MAX = 1;
    private static double MANDELBROT_IM_MIN = -1.2;
    private static double MANDELBROT_IM_MAX = 1.2;

    private void paintSet(GraphicsContext ctx, double reMin, double reMax, double imMin, double imMax) {
        double precision = Math.max((reMax - reMin) / CANVAS_WIDTH, (imMax - imMin) / CANVAS_HEIGHT);
        int convergenceSteps = 50;
        for (double c = reMin, xR = 0; xR < CANVAS_WIDTH; c = c + precision, xR++) {
            for (double ci = imMin, yR = 0; yR < CANVAS_HEIGHT; ci = ci + precision, yR++) {
                double convergenceValue = checkConvergence(ci, c, convergenceSteps);
                double t1 = (double) convergenceValue / convergenceSteps;
                double c1 = Math.min(255 * 2 * t1, 255);
                double c2 = Math.max(255 * (2 * t1 - 1), 0);

                if (convergenceValue != convergenceSteps) {
                    ctx.setFill(Color.color(c2 / 255.0, c1 / 255.0, c2 / 255.0));
                } else {
                    ctx.setFill(Color.PURPLE); // Convergence Color
                }
                ctx.fillRect(xR, yR, 1, 1);
            }
        }
    }

    private int checkConvergence(double ci, double c, int convergenceSteps) {
        double z = 0;
        double zi = 0;
        for (int i = 0; i < convergenceSteps; i++) {
            double ziT = 2 * (z * zi);
            double zT = z * z - (zi * zi);
            z = zT + c;
            zi = ziT + ci;

            if (z * z + zi * zi >= 4.0) {
                return i;
            }
        }
        return convergenceSteps;
    }

    public void openMandelbrotSet() {
        x.setText(String.valueOf(CANVAS_WIDTH));
        y.setText(String.valueOf(CANVAS_HEIGHT));
        canvas.setWidth(CANVAS_WIDTH);
        canvas.setHeight(CANVAS_HEIGHT);
        canvas.setLayoutX(X_OFFSET);
        canvas.setLayoutY(Y_OFFSET);

        paintSet(canvas.getGraphicsContext2D(),
                MANDELBROT_RE_MIN,
                MANDELBROT_RE_MAX,
                MANDELBROT_IM_MIN,
                MANDELBROT_IM_MAX);
    }

    public String escolherImage() {
        FileChooser filec = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        FileChooser.ExtensionFilter extFilterGIF = new FileChooser.ExtensionFilter("GIF files (*.gif)", "*.GIF");
        filec.getExtensionFilters().addAll(extFilterJPG, extFilterPNG, extFilterGIF);

        File file = filec.showOpenDialog(null);

        if (file != null) {
            return file.toURI().toString();
        }

        return "";
    }

    double scaleFactor;

    public void zoom(ScrollEvent event) {
        if (lupa.isSelected()) {
            double scaleFactor = (event.getDeltaY() > 0) ? 1.05 : 1 / 1.05;

            // Zoom canvas
            canvas.setScaleX(canvas.getScaleX() * scaleFactor);
            canvas.setScaleY(canvas.getScaleY() * scaleFactor);
        }
    }

    public void return_original() {
        canvas.setScaleX(1.0);
        canvas.setScaleY(1.0);
        canvas.setTranslateX(0.0);
        canvas.setTranslateY(0.0);
    }


    public void handleMouseEvent2(MouseEvent e) {
        GraphicsContext g = canvas.getGraphicsContext2D();

        double t = slide.getValue();
        double x = e.getX() - t / 2;
        double y = e.getY() - t / 2;

        if (borracha.isSelected()) {
            g.setFill(Color.WHITE);
            g.fillRect(x, y, t, t);
        } else if (pincel.isSelected()) {
            g.setFill(color.getValue());
            g.fillRoundRect(x, y, t, t, t, t);
        } else if (pick.isSelected()) {
            x = e.getX();
            y = e.getY();
            SnapshotParameters params = new SnapshotParameters();
            Color cor = canvas.snapshot(params, null).getPixelReader().getColor((int) x, (int) y);
            color.setValue(cor);
        } else if (lupa.isSelected()) {
            double offsetX = (e.getX() - canvas.getWidth() / 2) * (1 - scaleFactor);
            double offsetY = (e.getY() - canvas.getHeight() / 2) * (1 - scaleFactor);
            // Move the canvas
            canvas.setTranslateX(canvas.getTranslateX() + offsetX);
            canvas.setTranslateY(canvas.getTranslateY() + offsetY);
        }
    }

    private EventHandler<MouseEvent> canvasClickHandler;

    private int check = 0;

    public void placeImageOnClick() {
        canvas.setCursor(Cursor.CROSSHAIR);
        check = 1;
        deselectEverything();
        Image image = new Image(escolherImage());
        slide.setValue(100);
        canvasClickHandler = event -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            double x = event.getX();
            double y = event.getY();

            double scaleFactor = slide.getValue() * 0.01; // Obtém o valor atual do slider

            double width = image.getWidth() * scaleFactor;
            double height = image.getHeight() * scaleFactor;

            gc.drawImage(image, x, y, width, height);
        };
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, canvasClickHandler);
    }

    public void disableImagePlacement() {
        canvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, canvasClickHandler);
        check = 0;
    }

    public void handleMouseEvent(MouseEvent e) {
        GraphicsContext g = canvas.getGraphicsContext2D();

        double t = slide.getValue();
        double x = e.getX() - t/2;
        double y = e.getY() - t/2;

        if (borracha.isSelected()) {
            g.setFill(Color.WHITE);
            g.fillRect(x, y, t, t);
        } else if (pincel.isSelected()) {
            g.setFill(color.getValue());
            g.fillRoundRect(x, y, t, t, t, t);
        } else if (pick.isSelected()) {
            SnapshotParameters params = new SnapshotParameters();
            Color cor = canvas.snapshot(params, null).getPixelReader().getColor((int) x, (int) y);
            color.setValue(cor);
        } else if (quad.isSelected()) {
            g.setFill(color.getValue());
            g.fillRect(x, y, t, t);
        } else if (triang.isSelected()) {
            g.setFill(color.getValue());
            g.fillPolygon(new double[]{x, x + t, x + t / 2}, new double[]{y + t, y + t, y}, 3);
        } else if (circ.isSelected()) {
            g.setFill(color.getValue());
            g.fillOval(x, y, t, t);
        }
    }

    public void reset_canvas () {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void mudarTamanho () {
        Canvas temp = canvas;

        canvas.setWidth(Double.parseDouble(x.getText()));
        canvas.setHeight(Double.parseDouble(y.getText()));

        GraphicsContext gn = canvas.getGraphicsContext2D();
        gn.drawImage(temp.snapshot(null, null), 0, 0);
    }

    public void abrirFile() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        Image image = new Image(escolherImage());
        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight());
        g.drawImage(image, 0, 0);
    }

    public void saveFile() {
        //saveFile
        FileChooser filec = new FileChooser();
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        filec.getExtensionFilters().addAll(extFilterPNG);

        File file = filec.showSaveDialog(null);

        //save file on pc
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                Image snapshot = canvas.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", fos);
            } catch (IOException e) {
                System.out.println("Failed to save canvas: " + e.getMessage());
            }
        }
    }

    /*
    public void zoom(ScrollEvent event) {
        final Scale scale = new Scale(1, 1);
        canvas.getTransforms().add(scale);

        double scaleFactor = (event.getDeltaY() > 0) ? 1.1 : 1 / 1.1;

        // Apply the scaling factor with bounds checking
        if (scaleFactor * scale.getX() >= 0.001 && scaleFactor * scale.getX() <= 1000.0) {
            scale.setX(scale.getX() * scaleFactor);
            scale.setY(scale.getY() * scaleFactor);
        }

        event.consume();
    }
     */

    public void initialize() {
        imagem_to_buttons();

        //meter o canvas default "branco"
        reset_canvas();

        //slider -> text
        slide.valueProperty().addListener((observable, oldValue, newValue) -> {
            slide_text.setText(String.format("%.0f", newValue));
        });

        //text -> slider
        slide_text.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d+")) {
                double value = Double.parseDouble(newValue);
                if (value >= slide.getMin() && value <= slide.getMax()) {
                    slide.setValue(value);
                }
            }
        });
    }

    public void deselectEverything() {
        pincel.setSelected(false);
        borracha.setSelected(false);
        pick.setSelected(false);
        quad.setSelected(false);
        triang.setSelected(false);
        circ.setSelected(false);
        lupa.setSelected(false);
        canvas.setCursor(Cursor.DEFAULT);
    }

    public void pincelSelected() {
        borracha.setSelected(false);
        pick.setSelected(false);
        quad.setSelected(false);
        triang.setSelected(false);
        circ.setSelected(false);
        lupa.setSelected(false);
        botao.setDisable(false);
        x.setDisable(false);
        y.setDisable(false);
        return_original();
        if (check == 1)
            disableImagePlacement();

        Image img = new Image (getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/pincel.png"));
        ImageCursor cursor = new ImageCursor(img);
        canvas.setCursor(cursor);
    }

    public void borrachaSelected() {
        pincel.setSelected(false);
        pick.setSelected(false);
        quad.setSelected(false);
        triang.setSelected(false);
        circ.setSelected(false);
        lupa.setSelected(false);
        botao.setDisable(false);
        x.setDisable(false);
        y.setDisable(false);
        return_original();
        if (check == 1)
            disableImagePlacement();

        Image img = new Image (getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/rubber.png"));
        ImageCursor cursor = new ImageCursor(img);
        canvas.setCursor(cursor);
    }

    public void pickSelected() {
        borracha.setSelected(false);
        pincel.setSelected(false);
        quad.setSelected(false);
        triang.setSelected(false);
        circ.setSelected(false);
        lupa.setSelected(false);
        botao.setDisable(false);
        x.setDisable(false);
        y.setDisable(false);
        return_original();
        if (check == 1)
            disableImagePlacement();

        Image img = new Image (getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/pick.png"));
        ImageCursor cursor = new ImageCursor(img);
        canvas.setCursor(cursor);
    }

    public void circSelected() {
        borracha.setSelected(false);
        pincel.setSelected(false);
        quad.setSelected(false);
        triang.setSelected(false);
        pick.setSelected(false);
        lupa.setSelected(false);
        botao.setDisable(false);
        x.setDisable(false);
        y.setDisable(false);
        return_original();
        if (check == 1)
            disableImagePlacement();

        canvas.setCursor(Cursor.CROSSHAIR);
    }

    public void quadSelected() {
        borracha.setSelected(false);
        pincel.setSelected(false);
        circ.setSelected(false);
        triang.setSelected(false);
        pick.setSelected(false);
        lupa.setSelected(false);
        botao.setDisable(false);
        x.setDisable(false);
        y.setDisable(false);
        return_original();
        if (check == 1)
            disableImagePlacement();

        canvas.setCursor(Cursor.CROSSHAIR);
    }

    public void triangSelected() {
        borracha.setSelected(false);
        pincel.setSelected(false);
        circ.setSelected(false);
        quad.setSelected(false);
        pick.setSelected(false);
        lupa.setSelected(false);
        botao.setDisable(false);
        x.setDisable(false);
        y.setDisable(false);
        return_original();
        if (check == 1)
            disableImagePlacement();

        canvas.setCursor(Cursor.CROSSHAIR);
    }

    public void lupaSelected() {
        canvas.setCursor(Cursor.MOVE);
        borracha.setSelected(false);
        pincel.setSelected(false);
        circ.setSelected(false);
        quad.setSelected(false);
        pick.setSelected(false);
        triang.setSelected(false);
        botao.setDisable(true);
        x.setDisable(true);
        y.setDisable(true);
        if (check == 1)
            disableImagePlacement();
    }

    public void imagem_to_buttons() {
        double maxWidth = 30;
        double maxHeight = 30;
        ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/Lines.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        espessura.setGraphic(img);
        espessura.setText("");

        img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/pincel.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        pincel.setGraphic(img);
        pincel.setText("");

        img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/rubber.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        borracha.setGraphic(img);
        borracha.setText("");

        img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/pick.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        pick.setGraphic(img);
        pick.setText("");

        img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/imagem.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        imagem.setGraphic(img);
        imagem.setText("");

        img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/newFile.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        criar.setGraphic(img);
        criar.setText("");

        img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/Folder.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        edit.setGraphic(img);
        edit.setText("");

        img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/save.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        save.setGraphic(img);
        save.setText("");

        img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/lupa.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        lupa.setGraphic(img);
        lupa.setText("");

        img = new ImageView(new Image(getClass().getResourceAsStream("/trabalho/desenho/logosjavafx/fractal.png")));
        img.setFitWidth(maxWidth);
        img.setFitHeight(maxHeight);
        img.setPreserveRatio(true);
        fractal.setGraphic(img);
        fractal.setText("");
    }

}