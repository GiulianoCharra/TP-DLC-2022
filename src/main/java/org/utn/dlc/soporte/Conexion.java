package org.utn.dlc.soporte;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Conexion {

    public enum DBConnectionMode {
        SINGLE_CONNECTION_MODE, POOL_CONNECTION_MODE
    }

    public static final String POSTGRES_DRIVER_NAME = "org.postgresql.Driver";
    public static final String MYSQL_DRIVER_NAME = "com.mysql.jdbc.Driver";
    public static final String H2_DRIVER_NAME = "org.h2.Driver";
    public static final String SQLSERVER_DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private DBConnectionMode connectionMode;

    private String driverName;
    private String usr = "";
    private String pwd = "";
    private String url = "jdbc:sqlserver://localhost\\CHONGOPC:1433;database=TP-DLC";

    private String resourceName;

    private Context ctx = null;
    private DataSource ds = null;
    private Connection cn = null;
    private Statement stmt = null;
    private PreparedStatement pstmt = null;
    private CallableStatement cstmt = null;

    private final HashMap<String, PreparedStatement> STATEMENTS;

    public Connection conectar() throws SQLException, ClassNotFoundException {
        Class.forName(SQLSERVER_DRIVER_NAME);
        return DriverManager.getConnection(url);
    }

    public ResultSet executeStatement(String query) throws SQLException, ClassNotFoundException {
        Connection c = conectar();
        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(query);

        close(rs);
        close(st);
        close(c);

        return rs;
    }

    public ResultSet executePrepareStatement(String query) throws SQLException, ClassNotFoundException {
        Connection c = conectar();
        PreparedStatement pst = c.prepareStatement(query);
        ResultSet rs = pst.executeQuery(query);

        close(rs);
        close(pst);
        close(c);

        return rs;
    }

    public void close(Connection c) throws SQLException {
        c.close();
    }

    public void close(Statement c) throws SQLException {
        c.close();
    }

    public void close(ResultSet c) throws SQLException {
        c.close();
    }

    /**
     * Creates a new instance of DBManager
     */
    public Conexion() {
        super();
        STATEMENTS = new HashMap<>();
    }

    public Conexion(String resourceName) {
        this();
        setConnectionMode(DBConnectionMode.POOL_CONNECTION_MODE);
        setResourceName(resourceName);
    }

    public Conexion(String driverName, String url) {
        this();
        setConnectionMode(DBConnectionMode.SINGLE_CONNECTION_MODE);
        setDriverName(driverName);
        setUrl(url);
    }

    public Conexion(String driverName, String url, String usr, String pwd) {
        this(driverName, url);
        setUserName(usr);
        setPassword(pwd);
    }

    /**
     *
     * @return
     */
    public DBConnectionMode getConnectionMode() {
        return connectionMode;
    }

    /**
     *
     * @param connectionMode
     */
    public void setConnectionMode(DBConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
    }

    /**
     *
     * @return
     */
    public String getDriverName() {
        return driverName;                            // "org.postgresql.Driver";
    }

    /**
     *
     * @param driverName
     */
    public void setDriverName(String driverName) {
        this.driverName = driverName;                 // "org.postgresql.Driver";
    }

    /**
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     */
    public String getUserName() {
        return usr;
    }

    /**
     *
     * @param usr
     */
    public void setUserName(String usr) {
        this.usr = usr;
    }

    /**
     *
     * @return
     */
    public String getPassword() {
        return null;
    }

    /**
     *
     * @param pwd
     */
    public void setPassword(String pwd) {
        this.pwd = pwd;
    }

    /**
     *
     * @return
     */
    public String getResourceName() {
        return resourceName;                          // "[java:comp/env/]jdbc/<dataSourceName>";
    }

    /**
     *
     * @param resourceName
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;             // "[java:comp/env/]jdbc/<dataSourceName>";
    }

    /**
     *
     * @throws Exception
     */
    private void setContext() throws Exception {
        if (ds == null) {
            if (resourceName == null) {
                throw new Exception("DBManager ERROR: ResourceName (JNDI) NO especificado");
            }
            ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(resourceName);
        }
    }

    /**
     *
     * @throws Exception
     */
    public Conexion connect() throws Exception {
        if (cn == null) {
            cn = getNewConnection();
        }
        return this;
    }

    public void disconnect() {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
            } finally {
                stmt = null;
            }
        }

        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (Exception e) {
            } finally {
                pstmt = null;
            }
        }

        if (cstmt != null) {
            try {
                cstmt.close();
            } catch (Exception e) {
            } finally {
                cstmt = null;
            }
        }

        if (cn != null) {
            try {
                cn.close();
            } catch (Exception e) {
            } finally {
                cn = null;
            }
        }

        ds = null;

        if (ctx != null) {
            try {
                ctx.close();
            } catch (Exception e) {
            } finally {
                ctx = null;
            }
        }

        for (PreparedStatement statement : STATEMENTS.values()) {
            try {
                statement.close();
            } catch (Exception e) {
            }
        }
        STATEMENTS.clear();
    }

    public void close() {
        disconnect();
    }

    /**
     *
     * @throws Exception
     */
    public void reconnect() throws Exception {
        if (cn != null) {
            disconnect();
        }
        connect();
    }

    /**
     *
     * @return @throws Exception
     */
    public Connection getNewConnection() throws Exception {
        if (connectionMode == DBConnectionMode.SINGLE_CONNECTION_MODE) {
            Class.forName(driverName);
            return DriverManager.getConnection(url, usr, pwd);
        } else {
            setContext();
            return ds.getConnection();
        }
    }

    /**
     * Comienza una transacción.
     *
     * @throws Exception
     */
    public void beginTransaction() throws Exception {
        cn.setAutoCommit(false);
    }

    public void commit() throws Exception {
        cn.commit();
        cn.setAutoCommit(true);
    }

    public void rollback() {
        try {
            cn.rollback();
            cn.setAutoCommit(true);
        } catch (Exception e) {
            throw new Error("DBManager ERROR: no se pudo efectuar Rollback");
        }
    }

    /**
     * Ejecuta una query SQL utilizando un Statement.
     *
     * @param query
     * @return
     * @throws Exception
     */
    public ResultSet executeQuery(String query) throws Exception {
        stmt = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return stmt.executeQuery(query);
    }

    /**
     * Ejecuta una instrucción SQL utilizando un Statement.
     *
     * @param statement
     * @return
     * @throws Exception
     */
    public Statement executeUpdate(String statement) throws Exception {
        stmt = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.executeUpdate(statement, Statement.RETURN_GENERATED_KEYS);
        return stmt;
    }

    /**
     * Precompila una instrucción SQL utilizando un PreparedStatement.
     *
     * @param query
     * @throws Exception
     */
    public void prepareQuery(String query) throws Exception {
        if (query == null || query.isEmpty()) {
            throw new Exception("DBManager Error: consulta incorrecta");
        }

        pstmt = STATEMENTS.get(query);
        if (pstmt == null) {
            pstmt = cn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            STATEMENTS.put(query, pstmt);
        }
    }

    /**
     * Precompila una instrucción SQL utilizando un PreparedStatement.
     *
     * @param statement
     * @throws Exception
     */
    public void prepareUpdate(String statement) throws Exception {
        if (statement == null || statement.isEmpty()) {
            throw new Exception("DBManager Error: instrucción incorrecta");
        }

        pstmt = STATEMENTS.get(statement);
        if (pstmt == null) {
            pstmt = cn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
            STATEMENTS.put(statement, pstmt);
        }
    }

    /**
     * Ejecuta una instrucción SQL, previamente preparada/precomplidada,
     * utilizando un PreparedStatement.
     *
     * @return
     * @throws Exception
     */
    public boolean execute() throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta ejecutar una instrucción NO preparada/precompilada.");
        }
        return pstmt.execute();
    }

    /**
     * Ejecuta una query, previamente preparada/precomplidada, utilizando un
     * PreparedStatement.
     *
     * @return
     * @throws Exception
     */
    public ResultSet executeQuery() throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta ejecutar una query NO preparada/precompilada.");
        }
        return pstmt.executeQuery();
    }

    /**
     * Ejecuta una instrucción SQL, previamente preparada/precomplidada,
     * utilizando un PreparedStatement.
     *
     * @return
     * @throws Exception
     */
    public Statement executeUpdate() throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta ejecutar una query NO preparada/precompilada.");
        }
        pstmt.executeUpdate();
        return pstmt;
    }

    /**
     * Setea un parámentro de tipo Integer de una instrucción SQL, previamente
     * preparada/precompilada, utilizando un PreparedStatement.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setInt(int parameterIndex, int value) throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta parametrizar una instrucción/query NO preparada/precompilada.");
        }
        pstmt.setInt(parameterIndex, value);
    }

    /**
     * Setea un parámentro de tipo Integer de una instrucción SQL, previamente
     * preparada/precompilada, utilizando un PreparedStatement.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setInt(int parameterIndex, Integer value) throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta parametrizar una instrucción/query NO preparada/precompilada.");
        }
        if (value == null) {
            pstmt.setNull(parameterIndex, Types.INTEGER);
        } else {
            pstmt.setInt(parameterIndex, value);
        }
    }

    /**
     * Setea un parámentro de tipo Integer de una instrucción SQL, previamente
     * preparada/precompilada, utilizando un PreparedStatement.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setLong(int parameterIndex, Long value) throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta parametrizar una instrucción/query NO preparada/precompilada.");
        }
        if (value == null) {
            pstmt.setNull(parameterIndex, Types.INTEGER);
        } else {
            pstmt.setLong(parameterIndex, value);
        }
    }

    /**
     * Setea un parámentro de tipo ShortInt de una instrucción SQL, previamente
     * preparada/precompilada, utilizando un PreparedStatement.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setShort(int parameterIndex, short value) throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta parametrizar una instrucción/query NO preparada/precompilada.");
        }
        pstmt.setShort(parameterIndex, value);
    }

    /**
     * Setea un parámentro de tipo ShortInt de una instrucción SQL, previamente
     * preparada/precompilada, utilizando un PreparedStatement.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setShort(int parameterIndex, Integer value) throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta parametrizar una instrucción/query NO preparada/precompilada.");
        }
        if (value == null) {
            pstmt.setNull(parameterIndex, Types.SMALLINT);
        } else {
            pstmt.setShort(parameterIndex, value.shortValue());
        }
    }

    /**
     * Idem <code>setShort</code>.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setSmallInt(int parameterIndex, short value) throws Exception {
        setShort(parameterIndex, value);
    }

    /**
     * Idem <code>setShort</code>.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setSmallInt(int parameterIndex, Integer value) throws Exception {
        setShort(parameterIndex, value);
    }

    /**
     * Setea un parámentro de tipo String de una instrucción SQL, previamente
     * preparada/precompilada, utilizando un PreparedStatement.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setString(int parameterIndex, String value) throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta parametrizar una instrucción/query NO preparada/precompilada.");
        }
        if (value == null) {
            pstmt.setNull(parameterIndex, Types.VARCHAR);
        } else {
            pstmt.setString(parameterIndex, value);
        }
    }

    /**
     * Setea un parámentro de tipo String de una instrucción SQL, previamente
     * preparada/precompilada, utilizando un PreparedStatement.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setString(int parameterIndex, char value) throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta parametrizar una instrucción/query NO preparada/precompilada.");
        }
        if (value == '\0') {
            pstmt.setNull(parameterIndex, Types.VARCHAR);
        } else {
            pstmt.setString(parameterIndex, String.valueOf(value));
        }
    }

    /**
     * Setea un parámentro de tipo Date de una instrucción SQL, previamente
     * preparada/precompilada, utilizando un PreparedStatement.
     *
     * @param parameterIndex
     * @param value
     * @throws Exception
     */
    public void setDate(int parameterIndex, java.util.Date value) throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta parametrizar una instrucción/query NO preparada/precompilada.");
        }
        if (value == null) {
            pstmt.setNull(parameterIndex, Types.DATE);
        } else {
            pstmt.setDate(parameterIndex, (java.sql.Date) value);
        }
    }

    public void setArray(int parameterIndex, String typeName, Object[] values) throws Exception {
        if (pstmt == null) {
            throw new Exception("DBManager Error: se intenta parametrizar una instrucción/query NO preparada/precompilada.");
        }

        if (values == null) {
            pstmt.setNull(parameterIndex, Types.ARRAY);
        } else {
            pstmt.setArray(parameterIndex, cn.createArrayOf(typeName, values));
        }
    }

    // util
    public static String joinArray(String separator, Object[] values) {
        if (separator != null && !separator.isEmpty() && values != null) {
            List<String> list = new ArrayList();
            for (Object value : values) {
                list.add(value.toString());
            }
            return String.join(separator, list);
        }

        return null;
    }

    public static String joinArray(Object[] values) {
        return joinArray(", ", values);
    }

    public static String buildSelectQuery(
            String qSelect,
            String qFrom,
            String qWhere,
            String qGroupBy,
            String qHaving,
            String qOrderBy,
            int qLimit, int qOffset) {

        qSelect = String.format("SELECT %s", qSelect);

        qFrom = String.format(" FROM %s", qFrom);

        qWhere = qWhere == null || qWhere.isEmpty()
                ? ""
                : String.format(" WHERE (%s)", qWhere);

        qGroupBy = qGroupBy == null || qGroupBy.isEmpty()
                ? ""
                : String.format(" GROUP BY %s", qGroupBy);

        qHaving = qHaving == null || qHaving.isEmpty()
                ? ""
                : String.format(" HAVING (%s)", qHaving);

        qOrderBy = qOrderBy == null || qOrderBy.isEmpty()
                ? ""
                : String.format(" ORDER BY %s", qOrderBy);

        String query = String.format("%s%s%s%s%s%s", qSelect, qFrom, qWhere, qGroupBy, qHaving, qOrderBy);

        if (qLimit > 0) {
            query += String.format(" LIMIT %d", qLimit);
        }

        if (qOffset > 0) {
            query += String.format(" OFFSET %d", qOffset);
        }

        return query;
    }

    public static String buildSelectQuery(
            String qSelect,
            String qFrom,
            String qWhere,
            String qGroupBy,
            String qHaving,
            String qOrderBy) {

        return buildSelectQuery(qSelect, qFrom, qWhere, qGroupBy, qHaving, qOrderBy, 0, 0);
    }

}
