package com.co.java.core.persistencia;

import com.co.java.core.bean.Departamento;
import com.co.java.core.bean.Municipio;
import com.co.java.core.bean.Postulante;
import com.co.java.core.persistencia.util.DBConnection;
import com.co.java.core.persistencia.util.DBException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PostulanteDBImpl implements PostulanteDB {

    // Mismas columnas que devuelve el cursor de PKG_POSTULANTE.SP_BUSCAR,
    // para mapear las filas con un solo método.
    private static final String SELECT_BASE = """
            SELECT P.POSTULANTE_ID, P.NOMBRES, P.APELLIDO_PATERNO, P.APELLIDO_MATERNO,
                   P.TIPO_DOCUMENTO, P.NUMERO_DOCUMENTO, P.FECHA_NACIMIENTO,
                   P.TELEFONO_FIJO, P.TELEFONO_CELULAR, P.CORREO, P.SEXO, P.DIRECCION,
                   M.MUNICIPIO_ID, M.NOMBRE AS MUNICIPIO,
                   D.DEPARTAMENTO_ID, D.NOMBRE AS DEPARTAMENTO, D.INDICATIVO
              FROM POSTULANTE P
              JOIN MUNICIPIO M    ON M.MUNICIPIO_ID = P.MUNICIPIO_ID
              JOIN DEPARTAMENTO D ON D.DEPARTAMENTO_ID = M.DEPARTAMENTO_ID
            """;

    @Override
    public List<Postulante> listar() {
        String sql = SELECT_BASE + " ORDER BY P.APELLIDO_PATERNO, P.APELLIDO_MATERNO, P.NOMBRES";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return getPostulantes(rs);
        } catch (SQLException e) {
            throw new DBException("Error al listar postulantes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Postulante> buscar(String texto) {
        String sql = "{call PKG_POSTULANTE.SP_BUSCAR(?,?)}";
        try (Connection connection = DBConnection.getConnection();
             CallableStatement cs = connection.prepareCall(sql)) {
            cs.registerOutParameter(1, Types.REF_CURSOR);
            cs.setString(2, texto);
            cs.execute();
            try (ResultSet rs = cs.getObject(1, ResultSet.class)) {
                return getPostulantes(rs);
            }
        } catch (SQLException e) {
            throw new DBException("Error al buscar postulantes: " + e.getMessage(), e);
        }
    }

    @Override
    public Postulante buscarXId(Long id) {
        return buscarUno(SELECT_BASE + " WHERE P.POSTULANTE_ID = ?", id);
    }

    @Override
    public Postulante buscarXDocumento(String tipoDocumento, String numeroDocumento) {
        return buscarUno(SELECT_BASE + " WHERE P.TIPO_DOCUMENTO = ? AND P.NUMERO_DOCUMENTO = ?",
                tipoDocumento, numeroDocumento);
    }

    @Override
    public Postulante buscarXCorreo(String correo) {
        return buscarUno(SELECT_BASE + " WHERE UPPER(P.CORREO) = UPPER(?)", correo);
    }

    @Override
    public Long insertar(Postulante postulante) {
        String sql = "{call PKG_POSTULANTE.SP_INSERTAR(?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (Connection connection = DBConnection.getConnection();
             CallableStatement cs = connection.prepareCall(sql)) {
            setDatos(cs, 1, postulante);
            cs.registerOutParameter(13, Types.NUMERIC);
            cs.execute();
            Long id = cs.getLong(13);
            postulante.setId(id);
            return id;
        } catch (SQLException e) {
            throw new DBException("Error al registrar el postulante: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Postulante postulante) {
        String sql = "{call PKG_POSTULANTE.SP_ACTUALIZAR(?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try (Connection connection = DBConnection.getConnection();
             CallableStatement cs = connection.prepareCall(sql)) {
            cs.setLong(1, postulante.getId());
            setDatos(cs, 2, postulante);
            cs.execute();
        } catch (SQLException e) {
            throw new DBException("Error al actualizar el postulante: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "DELETE FROM POSTULANTE WHERE POSTULANTE_ID = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DBException("No existe el postulante con id " + id);
            }
        } catch (SQLException e) {
            throw new DBException("Error al eliminar el postulante: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Métodos de apoyo
    // ------------------------------------------------------------------

    private Postulante buscarUno(String sql, Object... parametros) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getPostulante(rs) : null;
            }
        } catch (SQLException e) {
            throw new DBException("Error al buscar el postulante: " + e.getMessage(), e);
        }
    }

    /**
     * Asigna los 12 datos del postulante a partir de la posición indicada,
     * en el mismo orden de los parámetros de SP_INSERTAR y SP_ACTUALIZAR.
     */
    private void setDatos(CallableStatement cs, int inicio, Postulante p) throws SQLException {
        int i = inicio;
        cs.setString(i++, p.getNombres());
        cs.setString(i++, p.getApellidoPaterno());
        cs.setString(i++, p.getApellidoMaterno());
        cs.setString(i++, p.getTipoDocumento());
        cs.setString(i++, p.getNumeroDocumento());
        cs.setDate(i++, p.getFechaNacimiento() == null ? null : Date.valueOf(p.getFechaNacimiento()));
        cs.setString(i++, p.getTelefonoFijo());
        cs.setString(i++, p.getTelefonoCelular());
        cs.setString(i++, p.getCorreo());
        cs.setString(i++, p.getSexo());
        cs.setString(i++, p.getDireccion());
        cs.setString(i, p.getMunicipio() == null ? null : p.getMunicipio().getMunicipioId());
    }

    private List<Postulante> getPostulantes(ResultSet rs) throws SQLException {
        List<Postulante> postulantes = new ArrayList<>();
        while (rs.next()) {
            postulantes.add(getPostulante(rs));
        }
        return postulantes;
    }

    private Postulante getPostulante(ResultSet rs) throws SQLException {
        Postulante p = new Postulante();
        p.setId(rs.getLong("POSTULANTE_ID"));
        p.setNombres(rs.getString("NOMBRES"));
        p.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
        p.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
        p.setTipoDocumento(rs.getString("TIPO_DOCUMENTO"));
        p.setNumeroDocumento(rs.getString("NUMERO_DOCUMENTO"));
        Date fecha = rs.getDate("FECHA_NACIMIENTO");
        p.setFechaNacimiento(fecha == null ? null : fecha.toLocalDate());
        p.setTelefonoFijo(rs.getString("TELEFONO_FIJO"));
        p.setTelefonoCelular(rs.getString("TELEFONO_CELULAR"));
        p.setCorreo(rs.getString("CORREO"));
        p.setSexo(rs.getString("SEXO"));
        p.setDireccion(rs.getString("DIRECCION"));
        Departamento departamento = new Departamento(
                rs.getString("DEPARTAMENTO_ID"),
                rs.getString("DEPARTAMENTO"),
                rs.getString("INDICATIVO"));
        p.setMunicipio(new Municipio(rs.getString("MUNICIPIO_ID"), rs.getString("MUNICIPIO"), departamento));
        return p;
    }
}
