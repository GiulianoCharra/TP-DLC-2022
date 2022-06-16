module org.utn.dlc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.naming;
    requires java.desktop;
    requires com.microsoft.sqlserver.jdbc;

    opens org.utn.dlc to javafx.fxml;
    exports org.utn.dlc;
}
