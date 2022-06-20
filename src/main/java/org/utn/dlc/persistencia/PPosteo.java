package org.utn.dlc.persistencia;

import org.utn.dlc.dominio.Posteo;
import org.utn.dlc.dominio.Vocabulario;
import org.utn.dlc.soporte.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public abstract class PPosteo {

    public static final String POSTEO_ID_VOCABULARIO = "idVocabulario";
    public static final String POSTEO_ID_DOCUMENTO = "idDocumento";
    public static final String POSTEO_FRECUENCIA = "frecPalabra";
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
                    rs.getInt(PPosteo.POSTEO_ID_DOCUMENTO),
                    rs.getInt(PPosteo.POSTEO_FRECUENCIA),
                    rs.getDouble(PPosteo.POSTEO_PESO)
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
                String.format("%s, %s", PPosteo.POSTEO_ID_VOCABULARIO, PPosteo.POSTEO_ID_DOCUMENTO),
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

    public static ArrayList<Posteo> buscarPosteosPorPalabra(String[] palabras) throws Exception {

        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            StringBuilder query = new StringBuilder();
            query.append("SELECT " );
            query.append("p.[idDocumento] " );
            query.append(", SUM(p.[frecPalabra]) as frecPalabra" );
            query.append(", SUM(p.[peso]) as peso ");
            query.append("FROM [dbo].[POSTEO] p " );
            query.append("WHERE p.[idVocabulario] IN(");
            for (int i = 0; i < palabras.length; i++) {
                query.append("?");
                if (i+1 < palabras.length)
                    query.append(",");
            }
            query.append(") ");
            query.append("GROUP BY p.[idDocumento] " );
            query.append("ORDER BY SUM(p.[frecPalabra]) DESC ");

            PreparedStatement pstmt = con.prepareStatement(query.toString());

            for (int i = 1; i <= palabras.length; i++) {
                pstmt.setInt(i, palabras[i-1].hashCode());
            }

            ResultSet rs = pstmt.executeQuery();

            ArrayList<Posteo> posteos = buildPosteos(rs);

            rs.close();
            pstmt.close();
            con.close();

            return posteos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Posteo buscarByIdVocabulario(int idVocabulario) {
        return null;
    }

    public static void updatePosteos(HashSet<Posteo> posteos, int idDocumento, Hashtable<Integer, Vocabulario> vocabulario) {
        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            String query = "UPDATE [dbo].[Posteo] " +
                           "SET [frecPalabra] = ?, [peso] = ? " +
                           "WHERE [idVocabulario]=? and ,[idDocumento] = ?";

            PreparedStatement pstmt = con.prepareStatement(query);
            con.setAutoCommit(false);
            int i = 0;
            for (Vocabulario v : vocabulario.values()) {
                pstmt.setInt(1, v.getMaxFrecuenciaPalabra());
                double peso = 0;
                for (Posteo p : posteos) {
                    if (p.getPalabra().equals(v)) {
                        peso = p.getPeso();
                    }
                }
                pstmt.setFloat(2, (float) peso);
                pstmt.setInt(3, v.getIdPalabra());
                pstmt.setInt(4, idDocumento);
                pstmt.addBatch();

                i++;
                if (i % 100 == 0 || i == vocabulario.size())
                    pstmt.executeBatch();
            }

            con.commit();
            con.setAutoCommit(true);
            pstmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertPosteos(HashSet<Posteo> posteos) {
        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            String query = "INSERT INTO [dbo].[Posteo](" +
                           "[idVocabulario]," +
                           "[idDocumento]," +
                           "[frecPalabra]," +
                           "[peso])" +
                           "VALUES " +
                           "(?,?,?,?)";

            PreparedStatement pstmt = con.prepareStatement(query);
            con.setAutoCommit(false);
            int i = 0;
            for (Posteo p : posteos) {
                pstmt.setInt(1, p.getPalabra().getIdPalabra());
                pstmt.setInt(2, p.getDocumento().getIdDocumento());
                pstmt.setInt(3, p.getFrecuencia());
                pstmt.setDouble(4, p.getPeso());
                pstmt.addBatch();

                i++;
                if (i % 100 == 0 || i == posteos.size())
                    pstmt.executeBatch();
            }

            con.commit();
            con.setAutoCommit(true);
            pstmt.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
