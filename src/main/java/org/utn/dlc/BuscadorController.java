package org.utn.dlc;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.utn.dlc.dominio.Documento;
import org.utn.dlc.logica.Buscador;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BuscadorController implements Initializable {

    public TextField txt_texto_ingresado;
    public ImageView img_buscar;
    public VBox hb_resultados;
    public Button btn_anterior;
    public Button btn_siguiente;
    public Label lbl_cant_resultados;
    public ComboBox<Integer> cmb_cantidad_resultados_mostrar;
    public Label lbl_desde_hasta;

    private ArrayList<Documento> resultados;
    private int inicio = 0;
    private int fin = 0;
    private final ArrayList<Integer> cantMonstrar = new ArrayList<>(
            Arrays.asList(10, 20, 40, 60, 80, 100));
    private int cantResultadosMostrar;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmb_cantidad_resultados_mostrar.getItems().addAll(cantMonstrar);
        cmb_cantidad_resultados_mostrar.setValue(cantMonstrar.get(0));

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

        //Guardo las palabras ingresadas
        String[] palabras = txt_texto_ingresado.getText().split(" ");


        //Busco las palabras incresadas
        resultados = Buscador.buscar(palabras);

        //Muesto la cantidad de resultados
        lbl_cant_resultados.setText("Resultados: " +  resultados.size());

        //Se habilita el combo que permite seleccionar la cantidad de resultados que se muestran a la vez
        cmb_cantidad_resultados_mostrar.setDisable(false);

        //Se muestran n resultados a la vez
        cantResultadosMostrar = cmb_cantidad_resultados_mostrar.getValue();
        cargarResultados(0, cantResultadosMostrar);
    }

    private void cargarResultados(int inicio, int fin){

        //Borro los resultados anteriores
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
                addResultado(i, documento);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        lbl_desde_hasta.setText(inicio + " - " + fin);
        this.inicio = inicio;
        this.fin = fin;
    }

    public int cantResultados() {
        return resultados.size();
    }

    public void addResultado(int i, Documento doc) throws URISyntaxException {

        File file = new File(doc.getPath());

        //Se crear el link al documento
        Hyperlink hl = new Hyperlink((i+1) + "-" +doc.getNombre());
        hl.setFont(Font.font(15));
        hl.setOnMouseClicked(event -> {
            try {
                cargarDocumento(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //Se crear un separador
        Separator s = new Separator();
        HBox.setHgrow(s, Priority.ALWAYS);
        s.setPadding(new Insets(10));

        //Se crea la imagen para descargar
        ImageView imv = new ImageView(new Image(getClass().getResource("imagenes/download.png").toURI().toString()));
        imv.setFitWidth(20);
        imv.setFitHeight(20);
        HBox.setMargin(imv,new Insets(0, 5, 0, 0));
        imv.setOnMouseClicked(event -> {
            descargarDocumento(file);
        });

        //Se crean los Hbox
        HBox izquierda = new HBox(hl);
        HBox centro = new HBox(s);
        HBox derecha = new HBox(imv);

        izquierda.setAlignment(Pos.CENTER_LEFT);
        centro.setAlignment(Pos.CENTER);
        derecha.setAlignment(Pos.CENTER_RIGHT);

        izquierda.minWidth(100);
        HBox.setHgrow(izquierda, Priority.NEVER);

        centro.minWidth(10);
        HBox.setHgrow(centro, Priority.ALWAYS);

        derecha.minWidth(100);
        HBox.setHgrow(derecha, Priority.NEVER);

        hb_resultados.getChildren().addAll(
                new Separator(),
                new HBox(
                        izquierda,
                        centro,
                        derecha
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
        cargarResultados(fin, fin + cantResultadosMostrar);
    }

    public void mostrarAnteriorerResultados(ActionEvent actionEvent) {
        cargarResultados(inicio - cantResultadosMostrar, inicio);
    }

    public void mostrarResultados(ActionEvent actionEvent) {
        cantResultadosMostrar = cmb_cantidad_resultados_mostrar.getValue();
        cargarResultados(0, cantResultadosMostrar);
    }
}
