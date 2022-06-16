package org.utn.dlc.persistencia;

import org.utn.dlc.dominio.Documento;
import org.utn.dlc.soporte.Conexion;

import java.sql.*;
import java.util.Date;
import java.util.Hashtable;

public abstract class PDocumento {

    public static final String DOCUMENTO_ID_DOCUMENTO = "idDocumento";
    public static final String DOCUMENTO_NOMBRE = "nombre";
    public static final String DOCUMENTO_PATH = "path";
    public static final String DOCUMENTO_FECHA_HORA_ACTUALIZACION = "fechaHoraActualizacion";

    public static Documento buscarByIdDocumento(int idDocumento) {

        Documento documento = null;
        try {
            Conexion con = new Conexion(Conexion.SQLSERVER_DRIVER_NAME);

            con.connect();
            String query = Conexion.buildSelectQuery(
                    "*",
                    "Documento",
                    String.format(" %s", DOCUMENTO_ID_DOCUMENTO + "=" + idDocumento),
                    null,
                    null,
                    null
            );

            con.prepareQuery(query);

            ResultSet rs = con.executeQuery(query);
            documento = buildDocumento(rs);

            rs.close();
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        return documento;
    }

    protected static Hashtable<Integer, Documento> buildDocumentos(ResultSet rs) throws Exception {
        Hashtable<Integer, Documento> r = new Hashtable<>();

        Documento documento;
        while ((documento = buildDocumento(rs)) != null) {
            r.put(documento.getIdDocumento(), documento);
        }
        return r;
    }

    /**
     * Construye un Documento a partir de un ResultSet previamente ejecutado.
     *
     * @param rs
     * @return
     * @throws Exception
     */
    protected static Documento buildDocumento(ResultSet rs) throws Exception {
        Documento documento = null;
        if (rs.next()) {
            documento = new Documento(
                    rs.getInt(PDocumento.DOCUMENTO_ID_DOCUMENTO),
                    rs.getString(PDocumento.DOCUMENTO_NOMBRE),
                    rs.getString(PDocumento.DOCUMENTO_PATH),
                    rs.getTimestamp(PDocumento.DOCUMENTO_FECHA_HORA_ACTUALIZACION)
            );
        }
        return documento;
    }

    public static Hashtable<Integer, Documento> findAllDocuments() {

        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            Statement stmt = con.createStatement();
            String query = "SELECT * FROM [Documento]";

            ResultSet rs = stmt.executeQuery(query);
            Hashtable<Integer, Documento> documentos = buildDocumentos(rs);

            rs.close();
            stmt.close();
            con.close();

            return documentos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /*public static Hashtable<Integer, Documento> buscarAllDocumentos() {

        try {
            Conexion con = new Conexion(Conexion.SQLSERVER_DRIVER_NAME);

            con.connect();
            String query = Conexion.buildSelectQuery(
                    "*",
                    "Documento",
                    null,
                    null,
                    null,
                    String.format(" %s", PDocumento.DOCUMENTO_ID_DOCUMENTO),
                    0,
                    0
            );

            con.prepareQuery(query);
            ResultSet rs = con.executeQuery(query);
            Hashtable<Integer, Documento> documentos = buildDocumentos(rs);

            rs.close();
            con.close();

            return documentos;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public static void insertarDocumento(Documento documento) {
        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            String query = "INSERT INTO [Documento](" +
                    "[idDocumento]," +
                    "[nombre]," +
                    "[path]," +
                    "[fechaHoraActualizacion])" +
                    "VALUES " +
                    "(?,?,?,?)";

            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, documento.getIdDocumento());
            pstmt.setString(2, documento.getNombre());
            pstmt.setString(3, documento.getPath());
            pstmt.setTimestamp(4, new java.sql.Timestamp(documento.getFechaHoraActualizacion().getTime()));

            pstmt.executeUpdate();
            pstmt.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void actualizarDocumento(Documento documento) {
    }

    public static void actualizarFechaDocumento(int idDocumento, Date fechaUltimaActualizacion) {
        try {
            Class.forName(Conexion.SQLSERVER_DRIVER_NAME);
            Connection con = DriverManager.getConnection(Conexion.URL, Conexion.USER, Conexion.PASS);

            String query = "UPDATE [dbo].[Documento] " +
                    "SET [fechaHoraActualizacion]=? " +
                    "WHERE [idDocumento]=?";

            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setTimestamp(1, new java.sql.Timestamp(fechaUltimaActualizacion.getTime()));
            pstmt.setInt(2, idDocumento);

            pstmt.executeUpdate();

            pstmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
