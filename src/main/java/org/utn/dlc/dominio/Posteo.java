package org.utn.dlc.dominio;

import org.utn.dlc.persistencia.PPosteo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class Posteo {

    private Vocabulario palabra;
    private Documento documento;
    private int frecuencia;
    private double peso;

    public Posteo(int idVocabulario, int idDocumento, int frecuencia, double peso) {
        this(Vocabulario.buscarByIdPalabra(idVocabulario), Documento.buscarByIdDocumento(idDocumento), frecuencia, peso);
    }
    public Posteo(int idDocumento, int frecuencia, double peso) {
        this(null, Documento.buscarByIdDocumento(idDocumento), frecuencia, peso);
    }

    public Posteo(Vocabulario palabra, Documento documento) {
        this(palabra, documento, 1, 0);
    }

    public Posteo(Vocabulario palabra, Documento documento, int frecuencia) {
        this(palabra, documento, frecuencia, 0);
    }

    public Posteo(Vocabulario palabra, Documento documento, int frecuencia, double peso) {
        this.palabra = palabra;
        this.documento = documento;
        this.frecuencia = frecuencia;
        this.peso = peso;
    }

    public static ArrayList<Posteo> buscarPosteosPorPalabra(String[] palabras) throws Exception {

        return PPosteo.buscarPosteosPorPalabra(palabras);
    }

    public static void insertarPosteos(HashSet<Posteo> posteos) {
        PPosteo.insertPosteos(posteos);
    }

    public static void actualizarPosteos(HashSet<Posteo> posteos, Documento documentoActual, Hashtable<Integer, Vocabulario> vocabularioParaActualizar) {
        PPosteo.updatePosteos(posteos, documentoActual.getIdDocumento(), vocabularioParaActualizar);
    }

    public Vocabulario getPalabra() {
        return palabra;
    }

    public void setPalabra(Vocabulario palabra) {
        this.palabra = palabra;
    }

    public Documento getDocumento() {
        return documento;
    }

    public void setDocumento(Documento documento) {
        this.documento = documento;
    }

    public int getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(int frecuencia) {
        this.frecuencia = frecuencia;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public void calcularPeso(int N, int n) {

        this.peso = ((this.frecuencia * Math.log((double) N / n)) / (Math.sqrt(Math.pow(suma(N, n), 2))));
    }

    public double suma(int N, int n) {
        return frecuencia * Math.log((double) N / n);
    }

    public static Posteo buscarByIdVocabulario(int idVocabulario) {
        return PPosteo.buscarByIdVocabulario(idVocabulario);
    }

    @Override
    public String toString() {
        return "vocabulario: " + palabra.getPalabra() +
                ", documento: " + documento.getNombre() +
                ", frecuencia: " + frecuencia +
                ", peso: " + peso;
    }
}
