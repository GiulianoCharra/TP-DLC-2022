package org.utn.dlc.persistencia;

import org.utn.dlc.dominio.Posteo;
import org.utn.dlc.dominio.Vocabulario;
import org.utn.dlc.soporte.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

public abstract class PVocabulario {

    public static final String VOCABULARIO_ID_VOCABULARIO = "idPalabra";
    public static final String VOCABULARIO_PALABRA = "palabra";
    public static final String VOCABULARIO_CANT_DOCUMENTOS = "cantDocumentos";
    public static final String VOCABULARIO_MAX_FRECUENCIA = "maxFrecuenciaPalabra";


    /**
     * Construye una Palabra a partir de un ResultSet previamente ejecutado.
     *
     * @param rs
     * @return
     * @throws Exception
     */
    protected static Vocabulario buildWord(ResultSet rs) throws Exception {
        Vocabulario palabra = null;
        if (rs.next()) {
            palabra = new Vocabulario(
                    rs.getInt(PVocabulario.VOCABULARIO_ID_VOCABULARIO),
                    rs.getString(PVocabulario.VOCABULARIO_PALABRA),
                    rs.getInt(PVocabulario.VOCABULARIO_CANT_DOCUMENTOS),
                    rs.getInt(PVocabulario.VOCABULARIO_MAX_FRECUENCIA)
            );
        }
        return palabra;
    }

    protected static Hashtable<Integer, Vocabulario> buildWords(ResultSet rs) throws Exception {
        Hashtable<Integer, Vocabulario> r = new Hashtable<>();

        Vocabulario vocabulario;
        while ((vocabulario = buildWord(rs)) != null) {
            r.put(vocabulario.getIdPalabra(), vocabulario);
        }
        return r;
    }

    public static Hashtable<Integer, Vocabulario> findAllWords() {
        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            Statement stmt = con.createStatement();
            String query = "SELECT * FROM [Vocabulario]";

            ResultSet rs = stmt.executeQuery(query);
            Hashtable<Integer, Vocabulario> words = buildWords(rs);

            rs.close();
            stmt.close();
            con.close();

            return words;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Vocabulario buscarByIdPalabra(int idPalabra) {
        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            String query = "SELECT * "+
                           "FROM [dbo].[Vocabulario] " +
                           "WHERE [idPalabra]=? ";

            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, idPalabra);

            ResultSet rs = pstmt.executeQuery();
            Vocabulario vocabulario = buildWord(rs);

            rs.close();
            pstmt.close();
            con.close();

            return vocabulario;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void insertWors(Hashtable<Integer, Vocabulario> vocabulario) {
        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            String query = "INSERT INTO [Vocabulario](" +
                    "[idPalabra]," +
                    "[palabra]," +
                    "[cantDocumentos]," +
                    "[maxFrecuenciaPalabra])" +
                    "VALUES " +
                    "(?,?,?,?)";

            PreparedStatement pstmt = con.prepareStatement(query);
            con.setAutoCommit(false);
            int i = 0;
            for (Vocabulario v : vocabulario.values()) {
                pstmt.setInt(1, v.getIdPalabra());
                pstmt.setString(2, v.getPalabra());
                pstmt.setInt(3, v.getCantDocumentos());
                pstmt.setInt(4, v.getMaxFrecuenciaPalabra());
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

    public static void updateWords(Hashtable<Integer, Vocabulario> vocabulario) {
        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            String query = "UPDATE [dbo].[Vocabulario] " +
                    "SET [cantDocumentos] = ?, [maxFrecuenciaPalabra] = ? " +
                    "WHERE [idPalabra] = ?";

            PreparedStatement pstmt = con.prepareStatement(query);
            con.setAutoCommit(false);
            int i = 0;
            for (Vocabulario v : vocabulario.values()) {
                pstmt.setInt(1, v.getCantDocumentos());
                pstmt.setInt(2, v.getMaxFrecuenciaPalabra());
                pstmt.setInt(3, v.getIdPalabra());
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
}
