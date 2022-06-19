package org.utn.dlc;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.utn.dlc.dominio.Documento;
import org.utn.dlc.logica.Buscador;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class BuscadorController implements Initializable {

    public TextField txt_texto_ingresado;
    public ImageView img_buscar;
    public VBox hb_resultados;
    public Button btn_anterior;
    public Button btn_siguiente;
    public Label lbl_cant_resultados;

    private ArrayList<Documento> resultados;
    private int i = 0;

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

        i = 0;

        if (txt_texto_ingresado.getText().trim().isEmpty())
            return;
        String[] palabras = txt_texto_ingresado.getText().split(" ");

        resultados = Buscador.buscar(palabras);

        lbl_cant_resultados.setText("Resultados: " +  resultados.size());
        hb_resultados.getChildren().clear();
        cargarResultados(i, 20);
    }

    private void cargarResultados(int inicio, int fin){
        hb_resultados.getChildren().clear();
        if (fin > cantResultados()){
            fin = cantResultados();
            btn_siguiente.setDisable(true);
        }
        else
            btn_siguiente.setDisable(false);
        if (inicio <= 0){
            inicio = 0;
            btn_anterior.setDisable(true);
        }
        else
            btn_anterior.setDisable(false);

        for (int i = inicio; i < fin; i++) {
            Documento documento = resultados.get(i);
            try {
                addResultado(documento);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        i = fin;
    }

    public int cantResultados() {
        return resultados.size();
    }

    public void addResultado(Documento doc) throws URISyntaxException {

        File file = new File(doc.getPath());

        Hyperlink hl = new Hyperlink(doc.getNombre());
        hl.setOnMouseClicked(event -> {
            try {
                cargarDocumento(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Separator s = new Separator();
        s.setPadding(new Insets(10));
        ImageView imv = new ImageView(new Image(getClass().getResource("imagenes/download.png").toURI().toString()));
        imv.setOnMouseClicked(event -> {
            descargarDocumento(file);
        });
        hb_resultados.getChildren().addAll(
                new Separator(),
                new HBox(
                        hl,
                        s,
                        imv
                )
        );
    }

    public void cargarDocumento(File doc) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("documento.fxml"));
        Parent root = loader.load();

        DocumentoController documentoController = loader.getController();
        documentoController.cargarDocumento(doc);
        App.newScene(root);
    }

    public void descargarDocumento(File file) {
        String ruta = cargarRuta();
        if (ruta == null)
            return;
        Scanner scan;
        FileWriter nuevo;
        try {
            nuevo = new FileWriter(ruta + ".txt");
            scan = new Scanner(file, StandardCharsets.ISO_8859_1);
            while (scan.hasNextLine())
                nuevo.write(scan.nextLine()+"\n");
            nuevo.close();
            JOptionPane.showMessageDialog(null,"Documento guardado correctamente");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String cargarRuta() {

        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        directoryChooser.showOpenDialog(directoryChooser);
        File f = directoryChooser.getSelectedFile();
        return f == null ? null : String.valueOf(f);
    }

    public void buscarEnter(ActionEvent actionEvent) throws Exception {
        buscar();
    }

    public void buscarClick(MouseEvent mouseEvent) throws Exception {
        buscar();
    }

    public void mostrarSiguientesResultados(ActionEvent actionEvent) {
        cargarResultados(i, i + 20);
    }

    public void mostrarAnteriorerResultados(ActionEvent actionEvent) {
        cargarResultados(i - 20, i);
    }
}
