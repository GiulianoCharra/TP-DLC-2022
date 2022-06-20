package org.utn.dlc.logica;

import org.utn.dlc.dominio.Documento;
import org.utn.dlc.dominio.Posteo;

import java.util.ArrayList;

public abstract class Buscador implements Runnable{

    public static ArrayList<Documento> buscar(String[] palabras) throws Exception {

        ArrayList<Posteo> resultadosPosteos = Posteo.buscarPosteosPorPalabra(palabras);

        ArrayList<Documento> resultadosDocumentos = new ArrayList<>();
        for (Posteo p : resultadosPosteos) {
            resultadosDocumentos.add(p.getDocumento());
        }

        return resultadosDocumentos;
    }
}
