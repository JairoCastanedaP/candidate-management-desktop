package com.co.java.core.persistencia;

import com.co.java.core.bean.Departamento;
import com.co.java.core.bean.Municipio;
import com.co.java.core.persistencia.util.DBConnection;
import com.co.java.core.persistencia.util.DBException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MunicipioDBImpl implements MunicipioDB {

    private static final String SELECT_BASE = """
            SELECT M.MUNICIPIO_ID, M.NOMBRE AS MUNICIPIO,
                   D.DEPARTAMENTO_ID, D.NOMBRE AS DEPARTAMENTO, D.INDICATIVO
              FROM MUNICIPIO M
              JOIN DEPARTAMENTO D ON D.DEPARTAMENTO_ID = M.DEPARTAMENTO_ID
            """;

    @Override
    public Municipio buscarXId(String municipioId) {
        List<Municipio> municipios = consultar(SELECT_BASE + " WHERE M.MUNICIPIO_ID = ?", municipioId);
        return municipios.isEmpty() ? null : municipios.get(0);
    }

    @Override
    public List<Municipio> buscarXCodigo(String codigo) {
        String sql = SELECT_BASE + " WHERE M.MUNICIPIO_ID LIKE ? ORDER BY M.MUNICIPIO_ID";
        return consultar(sql, limpiar(codigo) + "%");
    }

    @Override
    public List<Municipio> buscarXNombre(String nombre) {
        // BINARY_AI: sin distinguir mayúsculas ni tildes ("bogota" encuentra "BOGOTÁ, D.C.")
        String sql = SELECT_BASE + " WHERE M.NOMBRE COLLATE BINARY_AI LIKE ? ORDER BY M.NOMBRE, D.NOMBRE";
        return consultar(sql, "%" + limpiar(nombre) + "%");
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private List<Municipio> consultar(String sql, String parametro) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                List<Municipio> municipios = new ArrayList<>();
                while (rs.next()) {
                    Departamento departamento = new Departamento(
                            rs.getString("DEPARTAMENTO_ID"),
                            rs.getString("DEPARTAMENTO"),
                            rs.getString("INDICATIVO"));
                    municipios.add(new Municipio(rs.getString("MUNICIPIO_ID"), rs.getString("MUNICIPIO"), departamento));
                }
                return municipios;
            }
        } catch (SQLException e) {
            throw new DBException("Error al consultar municipios: " + e.getMessage(), e);
        }
    }
}
