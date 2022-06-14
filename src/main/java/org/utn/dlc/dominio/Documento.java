package org.utn.dlc.dominio;


import org.utn.dlc.persistencia.PDocumento;

import java.util.Date;
import java.util.Hashtable;

public class Documento{

    private int idDocumento;
    private String nombre;
    private String path;
    private Date fechaHoraActualizacion;

    public Documento() {
    }

    public Documento(String nombre, String path, Date fechaHoraActualizacion) {
        this(nombre.hashCode(), nombre, path, fechaHoraActualizacion);
    }

    public Documento(int idDocumento, String nombre, String path, Date fechaHoraActualizacion) {
        this.idDocumento = idDocumento;
        this.nombre = nombre;
        this.path = path;
        this.fechaHoraActualizacion = fechaHoraActualizacion;
    }

    public static Hashtable<Integer, Documento> buscarAllDocumentos() throws Exception {
        return PDocumento.buscarAllDocumentos();
    }

    public static void insertarDocumento(Documento documento) {
        PDocumento.insertarDocumento(documento);
    }

    public static void actualizarFechaDocumento(Documento documento) {
        actualizarFechaDocumento(documento.getIdDocumento());
    }

    public static void actualizarFechaDocumento(int idDocumento, Date fechaUltimaActualizacion) {
        PDocumento.actualizarDocumento(idDocumento);
    }

    public int getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(int idDocumento) {
        this.idDocumento = idDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getFechaHoraActualizacion() {
        return fechaHoraActualizacion;
    }

    public void setFechaHoraActualizacion(Date fechaHoraActualizacion) {
        this.fechaHoraActualizacion = fechaHoraActualizacion;
    }

    public static Documento buscarByIdDocumento(int idDocumento){
        return PDocumento.buscarByIdDocumento(idDocumento);
    }


    public boolean esActualizado(Documento documento) {
        return esActualizado(documento.getFechaHoraActualizacion());
    }

    public boolean esActualizado(Date fechaHora) {
        return this.fechaHoraActualizacion != fechaHora;
    }

    @Override
    public String toString() {
        return  "idDocumento: " + idDocumento +
                ", nombre: '" + nombre + '\'' +
                ", path: '" + path + '\'' +
                ", fechaHoraActualizacion: " + fechaHoraActualizacion;
    }
}
