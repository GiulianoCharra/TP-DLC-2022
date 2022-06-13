package org.utn.dlc.persistencia;

import org.utn.dlc.dominio.Vocabulario;

public abstract class PVocabulario {

    public static final String VOCABULARIO_ID_VOCABULARIO = "id_palabra";
    public static final String VOCABULARIO_PALABRA = "palabra";
    public static final String VOCABULARIO_CANT_DOCUMENTOS = "cantDocumentos";
    public static final String VOCABULARIO_MAX_FRECUENCIA = "maxFrecuenciaPalabra";


    public static Vocabulario buscarByIdPalabra(int idPalabra) {
        return null;
    }
}
