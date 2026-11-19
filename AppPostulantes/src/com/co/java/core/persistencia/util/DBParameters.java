package com.co.java.core.persistencia.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Parámetros de conexión a la base de datos.
 * Se leen del archivo db.properties en la carpeta del proyecto,
 * para no dejar la contraseña escrita en el código.
 */
public class DBParameters {

    private static final String ARCHIVO = "db.properties";

    public static final String DRIVER;
    public static final String URL;
    public static final String USU;
    public static final String PSW;

    static {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(ARCHIVO)) {
            props.load(in);
        } catch (IOException e) {
            throw new DBException("No se pudo leer " + ARCHIVO
                    + ". Copie db.properties.example como db.properties y complete los datos.", e);
        }
        DRIVER = props.getProperty("db.driver", "oracle.jdbc.OracleDriver");
        URL = props.getProperty("db.url");
        USU = props.getProperty("db.user");
        PSW = props.getProperty("db.password");
    }

    private DBParameters() {
    }
}
