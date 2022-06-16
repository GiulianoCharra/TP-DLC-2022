package org.utn.dlc.logica;

import org.utn.dlc.dominio.Documento;
import org.utn.dlc.dominio.Posteo;

import java.util.ArrayList;

public abstract class Buscador {

    public static ArrayList<Documento> buscar(String[] palabras) throws Exception {

        ArrayList<Posteo> resultadosPosteos = Posteo.buscarPosteos(palabras);
        ArrayList<Documento> resultadosDocumentos = new ArrayList<>();

        for (Posteo res : resultadosPosteos) {
            resultadosDocumentos.add(res.getDocumento());
        }
        return resultadosDocumentos;
    }
}
