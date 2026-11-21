package com.co.java.core.persistencia.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.co.java.core.persistencia.util.DBParameters.*;

public class DBConnection {

    private DBConnection() {
    }

    /**
     * Abre una conexión nueva. Quien la pide debe cerrarla,
     * idealmente con try-with-resources.
     */
    public static Connection getConnection() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USU, PSW);
        } catch (ClassNotFoundException e) {
            throw new DBException("No se encontró el driver " + DRIVER, e);
        } catch (SQLException e) {
            throw new DBException("Error al conectar a la base de datos: " + e.getMessage(), e);
        }
    }
}
