package org.utn.dlc;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.Scanner;

public class DocumentoController implements Initializable {


    public Label lbl_nombre_documento;
    public TextArea txta_documento;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void cargarDocumento(File documento) {
        StringBuilder text = new StringBuilder();
        try (Scanner scan = new Scanner(new File(documento.getPath()), StandardCharsets.ISO_8859_1)) {
            while (scan.hasNextLine()) {
                text.append(scan.nextLine()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        lbl_nombre_documento.setText(documento.getName());
        txta_documento.setText(String.valueOf(text));
    }

}