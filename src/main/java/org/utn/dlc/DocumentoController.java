package org.utn.dlc;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.utn.dlc.dominio.Documento;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DocumentoController implements Initializable {


    public Label lbl_nombre_documento;
    public TextArea txta_documento;

    private Documento documento;

    public void cargarDocumento(Documento documento, StringBuilder stringBuilder) {
        this.documento = documento;
        this.txta_documento.setText(String.valueOf(stringBuilder));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    public void cargar(Documento documento) {
        this.documento = documento;
        lbl_nombre_documento.setText(documento.getNombre());
    }
}