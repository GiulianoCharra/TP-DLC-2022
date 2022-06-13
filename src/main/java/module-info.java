module org.utn.dlc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.naming;
    requires java.desktop;

    opens org.utn.dlc to javafx.fxml;
    exports org.utn.dlc;
}
