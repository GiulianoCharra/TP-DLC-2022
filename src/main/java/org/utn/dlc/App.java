package org.utn.dlc;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.utn.dlc.logica.Indexador;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
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

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) throws IOException {

        Runnable indexador = () -> {
            try {
                String ruta = cargarRuta();
                if (ruta != null)
                    Indexador.indexar(ruta);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Runnable buscador = () -> {
            launch();
        };

        buscador.run();
        indexador.run();

        //launch();
    }

    /**
     * Abre una ventana donde se solicita que se seleccione una carpeta
     * @return devuelve la ruta del directorio seleccionado
     */
    public static String cargarRuta(){

        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooser.showOpenDialog(directoryChooser);
        File f = directoryChooser.getSelectedFile();
        return f == null ? null: String.valueOf(f);
    }

}