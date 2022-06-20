package org.utn.dlc;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class DocumentoController implements Initializable {


    public Label lbl_nombre_documento;
    public TextArea txta_documento;
    public Button btn_anterior;
    public Button btn_siguiente;
    public TextField txt_pagina_actual;
    public Label lbl_total_paginas;
    private ArrayList<StringBuilder> paginas = new ArrayList<>();
    private int numeroPagina;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        formatPositiveInteger(txt_pagina_actual);
    }

    public static void formatPositiveInteger(TextField tf) {
        tf.textProperty().addListener((observableValue, o, num) ->
        {
            if (!num.matches("\\d*"))
                tf.setText(num.replaceAll("[^\\d]", ""));
        });
    }

    public void cargarDocumento(File documento) {
        StringBuilder page = new StringBuilder();
        try (Scanner scan = new Scanner(new File(documento.getPath()), StandardCharsets.ISO_8859_1)) {
            int i = 1;
            String linea;
            while (scan.hasNextLine()) {
                linea = scan.nextLine();
                page.append(linea).append("\n");
                if (i % 50 == 0) {
                    paginas.add(page);
                    page = new StringBuilder();
                }
                i++;
            }
            //Se verifica si hay una ultima pagina con alguna linea
            if (page.length() > 0)
                paginas.add(page);

        } catch (Exception e) {
            e.printStackTrace();
        }

        lbl_nombre_documento.setText(documento.getName());
        cargarPagina(1);
    }

    private void cargarPagina(int pagina) {
        if (pagina > cantPaginas()){
            pagina = cantPaginas();
            btn_siguiente.setDisable(true);
        }
        else
            btn_siguiente.setDisable(false);

        if (pagina <= 1){
            pagina = 1;
            btn_anterior.setDisable(true);
        }
        else
            btn_anterior.setDisable(false);

        /*StringBuilder pagina = new StringBuilder();
        for (int i = inicio; i < fin; i++) {
            pagina.append(text.get(i)).append("\n");
        }*/

        txt_pagina_actual.setText(String.valueOf(pagina));
        lbl_total_paginas.setText(String.valueOf(cantPaginas()));
        txta_documento.setText(paginas.get(pagina-1).toString());
        numeroPagina = pagina;
    }

    public int cantPaginas() {
        return paginas.size();
    }

    public void onActionMostrarSiguientePagina(ActionEvent actionEvent) {
        siguientePagina();
    }

    public void onKeyPressedMostrarSiguientePagina(KeyEvent actionEvent) {
        if (actionEvent.getCode() == KeyCode.RIGHT) {
            siguientePagina();
        }
    }

    private void siguientePagina(){
        numeroPagina ++;
        cargarPagina(numeroPagina);
    }

    public void onActionMostrarPaginaAnterior(ActionEvent actionEvent) {
        paginaAnterior();
    }

    public void onKeyPressedMostrarPaginaAnterior(KeyEvent actionEvent) {
        if (actionEvent.getCode() == KeyCode.LEFT) {
            paginaAnterior();
        }
    }

    private void paginaAnterior(){
        numeroPagina --;
        cargarPagina(numeroPagina);
    }

    public void cargarPaginaEspecifica(ActionEvent actionEvent) {
        if (!txt_pagina_actual.getText().isBlank()){
            int pagina = Integer.parseInt(txt_pagina_actual.getText());
            cargarPagina(pagina);
        }
        txt_pagina_actual.setText(String.valueOf(numeroPagina));
    }
}