package org.utn.dlc.persistencia;

import org.utn.dlc.dominio.Documento;
import org.utn.dlc.dominio.Posteo;
import org.utn.dlc.soporte.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class PPosteo {

    public static final String POSTEO_ID_VOCABULARIO = "idVocabulario";
    public static final String POSTEO_ID_DOCUMENTO = "idDocumento";
    public static final String POSTEO_FRECUENCIA = "frecuencia";
    public static final String POSTEO_PESO = "peso";


    // ---------------------------------------------------------------------------
    protected static ArrayList<Posteo> buildPosteos(ResultSet rs) throws Exception {
        ArrayList<Posteo> r = new ArrayList();

        Posteo posteo;
        while ((posteo = buildPosteo(rs)) != null) {
            r.add(posteo);
        }
        return r;
    }

    /**
     * Construye un Alumno a partir de un ResultSet previamente ejecutado.
     *
     * @param rs
     * @return
     * @throws Exception
     */
    protected static Posteo buildPosteo(ResultSet rs) throws Exception {
        Posteo posteo = null;
        if (rs.next()) {
            posteo = new Posteo(
                    rs.getInt(PPosteo.POSTEO_ID_VOCABULARIO),
                    rs.getInt(PPosteo.POSTEO_ID_DOCUMENTO),
                    rs.getInt(PPosteo.POSTEO_FRECUENCIA),
                    rs.getInt(PPosteo.POSTEO_PESO)
            );
        }
        return posteo;
    }

    public static List<Posteo> loadList(Conexion db, Integer[] ids, int limit, int offset) throws Exception {
        if (db == null) {
            throw new Exception("DBAlumno Error: DBManager NO especificado");
        }
        if (limit < 0) {
            throw new Exception("DBAlumno Error: limit incorrecto");
        }
        if (offset < 0) {
            throw new Exception("DBAlumno Error: offset incorrecto");
        }

        String query = Conexion.buildSelectQuery(
                "*",
                "v_alumno",
                ids == null ? null : String.format("%s IN (?)", PPosteo.POSTEO_ID_VOCABULARIO),
                null,
                null,
                String.format("%s, %s",  PPosteo.POSTEO_ID_VOCABULARIO, PPosteo.POSTEO_ID_DOCUMENTO),
                limit,
                offset
        );

        db.prepareQuery(query);

        int parameterIndex = 1;
        if (ids != null) {
            db.setArray(parameterIndex++, "INTEGER", ids);
        }

        if (limit > 0) {
            db.setInt(parameterIndex++, limit);
        }

        if (offset > 0) {
            db.setInt(parameterIndex++, offset);
        }

        ResultSet rs = db.executeQuery(query);
        List<Posteo> alumnos = buildPosteos(rs);
        rs.close();

        return alumnos;
    }

    public static ArrayList<Posteo> buscarPosteos(String[] palabras) throws Exception {

        Conexion con = new Conexion(Conexion.SQLSERVER_DRIVER_NAME);
        Integer[] idPalabras = new Integer[palabras.length];
        for (int i = 0; i < palabras.length; i++) {
            idPalabras[i] = palabras[i].hashCode();
        }

        con.connect();
        String query = Conexion.buildSelectQuery(
                "*",
                "v_alumno",
                idPalabras == null ? null : String.format("%s IN (?)", PPosteo.POSTEO_ID_VOCABULARIO),
                null,
                null,
                String.format("%s, %s", PPosteo.POSTEO_ID_VOCABULARIO, PPosteo.POSTEO_ID_DOCUMENTO),
                0,
                0
        );

        con.prepareQuery(query);

        int parameterIndex = 1;
        if (idPalabras != null) {
            con.setArray(parameterIndex++, "INTEGER", idPalabras);
        }

        ResultSet rs = con.executeQuery(query);
        ArrayList<Posteo> posteos = buildPosteos(rs);

        rs.close();
        con.close();

        return posteos;
    }

    public static Posteo buscarByIdVocabulario(int idVocabulario) {
        return null;
    }
}
