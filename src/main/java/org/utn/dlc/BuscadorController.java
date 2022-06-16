package org.utn.dlc;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.utn.dlc.dominio.Documento;
import org.utn.dlc.logica.Buscador;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class BuscadorController implements Initializable {

    public TextField txt_texto_ingresado;
    public ImageView img_buscar;
    public VBox hb_resultados;

    private ArrayList<Documento> resultados;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /*try {
            addResultado(hb_resultados, "pepe");
            addResultado(hb_resultados, "papa");
            addResultado(hb_resultados, "pupua");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }*/
    }


    public void buscar() throws Exception {

        if (txt_texto_ingresado.getText().trim().isEmpty())
            return;
        String[] palabras = txt_texto_ingresado.getText().split(" ");

        resultados = Buscador.buscar(palabras);

        resultados.forEach(documento -> {
            try {
                addResultado(documento);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    public void addResultado(Documento doc) throws URISyntaxException {

        Hyperlink hl = new Hyperlink(doc.getNombre());
        hl.setOnMouseClicked(event -> {
            try {
                cargarDocumento(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Separator s = new Separator();
        s.setPadding(new Insets(10));
        ImageView imv = new ImageView(new Image(getClass().getResource("imagenes/download.png").toURI().toString()));

        hb_resultados.getChildren().addAll(
                new Separator(),
                new HBox(
                        hl,
                        s,
                        imv
                )
        );
    }

    public void cargarDocumento(MouseEvent actionEvent) throws IOException {
        Scene scene = new Scene(loadFXML("documento"), 640, 480);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    public void descargarDocumento(MouseEvent mouseEvent) {
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public void buscarEnter(ActionEvent actionEvent) throws Exception {
        buscar();
    }

    public void buscarClick(MouseEvent mouseEvent) throws Exception {
        buscar();
    }
}
