package org.utn.dlc.dominio;

import org.utn.dlc.persistencia.PVocabulario;

import java.util.Hashtable;

public class Vocabulario {

    private int idPalabra;
    private String palabra;
    private int cantDocumentos;
    private int maxFrecuenciaPalabra;

    public Vocabulario(Vocabulario voc) {
        this(voc.getIdPalabra(), voc.getPalabra(), voc.getCantDocumentos(), voc.getMaxFrecuenciaPalabra());
    }

    public Vocabulario(String palabra) {
        this(palabra.hashCode(), palabra, 1, 1);
    }

    public Vocabulario(int idPalabra, String palabra, int cantDocumentos, int maxFrecuenciaPalabra) {
        this.idPalabra = idPalabra;
        this.palabra = palabra;
        this.cantDocumentos = cantDocumentos;
        this.maxFrecuenciaPalabra = maxFrecuenciaPalabra;
    }

    public static void insertarPalabra(Hashtable<Integer, Vocabulario> vocabulario) {
    }

    public static void actualizarPalabra(Hashtable<Integer, Vocabulario> vocabularioParaActializar) {
    }

    public int getIdPalabra() {
        return idPalabra;
    }

    public void setIdPalabra(int idPalabra) {
        this.idPalabra = idPalabra;
    }

    public String getPalabra() {
        return palabra;
    }

    public void setPalabra(String palabra) {
        this.palabra = palabra;
    }

    public int getCantDocumentos() {
        return cantDocumentos;
    }

    public void setCantDocumentos(int cantDocumentos) {
        this.cantDocumentos = cantDocumentos;
    }

    public int getMaxFrecuenciaPalabra() {
        return maxFrecuenciaPalabra;
    }

    public void setMaxFrecuenciaPalabra(int maxFrecuenciaPalabra) {
        this.maxFrecuenciaPalabra = maxFrecuenciaPalabra;
    }

    public void increaseCantDoc(){
        this.cantDocumentos++;
    }

    public void increaseMaxFrec(){
        this.maxFrecuenciaPalabra++;
    }


    public static Vocabulario buscarByIdPalabra(int idPalabra) {
        return  PVocabulario.buscarByIdPalabra(idPalabra);
    }

    @Override
    public String toString() {
        return  "palabra: '" + palabra + '\'' +
                ", cantDocumentos: " + cantDocumentos +
                ", maxFrecuenciaPalabra: " + maxFrecuenciaPalabra;
    }
}
