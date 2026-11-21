package com.co.java.core.persistencia;

import com.co.java.core.bean.Departamento;
import com.co.java.core.persistencia.util.DBConnection;
import com.co.java.core.persistencia.util.DBException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartamentoDBImpl implements DepartamentoDB {

    @Override
    public List<Departamento> listar() {
        String sql = "SELECT DEPARTAMENTO_ID, NOMBRE, INDICATIVO FROM DEPARTAMENTO ORDER BY NOMBRE";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Departamento> departamentos = new ArrayList<>();
            while (rs.next()) {
                departamentos.add(new Departamento(
                        rs.getString("DEPARTAMENTO_ID"),
                        rs.getString("NOMBRE"),
                        rs.getString("INDICATIVO")));
            }
            return departamentos;
        } catch (SQLException e) {
            throw new DBException("Error al listar departamentos: " + e.getMessage(), e);
        }
    }
}
