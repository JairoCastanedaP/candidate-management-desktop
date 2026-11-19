package com.co.java.core.app;

import com.co.java.core.persistencia.util.DBConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;

/**
 * Punto de entrada de la aplicación.
 * Por ahora verifica la conexión a Oracle y el procedimiento PKG_POSTULANTE.SP_BUSCAR;
 * cuando exista el formulario, abrirá FrmPostulantes.
 */
public class AppPostulantes {

    public static void main(String[] args) {
        probarConexion();
    }

    static void probarConexion() {
        String sql = "{call PKG_POSTULANTE.SP_BUSCAR(?,?)}";
        try (Connection connection = DBConnection.getConnection();
             CallableStatement cs = connection.prepareCall(sql)) {

            System.out.println("Conectado a " + connection.getMetaData().getDatabaseProductVersion());

            cs.registerOutParameter(1, Types.REF_CURSOR);
            cs.setString(2, null);
            cs.execute();

            try (ResultSet rs = cs.getObject(1, ResultSet.class)) {
                while (rs.next()) {
                    System.out.println(rs.getInt("POSTULANTE_ID") + " | "
                            + rs.getString("APELLIDO_PATERNO") + " "
                            + rs.getString("NOMBRES") + " | DNI "
                            + rs.getString("DNI") + " | "
                            + rs.getString("DISTRITO"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error en la prueba de conexión: " + e.getMessage());
        }
    }
}
