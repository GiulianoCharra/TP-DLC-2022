package org.utn.dlc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.utn.dlc.logica.Indexador;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLOutput;
import java.util.Objects;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("buscador"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    static void newScene(String fxml) throws IOException {
        Scene newScene = new Scene(loadFXML(fxml), 640, 480);
        createScene(newScene);
    }
    static void newScene(Parent root) throws IOException {
        Scene newScene = new Scene(root, 640, 480);
        createScene(newScene);
    }

    static void createScene(Scene scene)
    {
        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }



    public static void main(String[] args) throws Exception {
        Thread indexar = new Thread(() -> {
            String ruta;
            try {
                ruta = App.class.getResource("documentos").toURI().getPath();
                while (true){
                    System.out.println("Iniciando Indexado");
                    Indexador.indexar(ruta);
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });Thread buscar = new Thread(() -> {
            launch();
        });


        indexar.start();
        buscar.start();
    }

    /**
     * Abre una ventana donde se solicita que se seleccione una carpeta
     *
     * @return devuelve la ruta del directorio seleccionado
     */
    public static String cargarRuta() {
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooser.showOpenDialog(directoryChooser);
        File f = directoryChooser.getSelectedFile();
        return f == null ? null : String.valueOf(f);
    }
}