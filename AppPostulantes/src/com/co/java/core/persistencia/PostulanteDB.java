package com.co.java.core.persistencia;

import com.co.java.core.bean.Postulante;
import java.util.List;

public interface PostulanteDB {

    List<Postulante> listar();

    /** Busca por nombres, apellido paterno o apellido materno (PKG_POSTULANTE.SP_BUSCAR). */
    List<Postulante> buscar(String texto);

    /** Devuelve el postulante o null si no existe. */
    Postulante buscarXId(Long id);

    /** Devuelve el postulante con ese tipo y número de documento, o null si no existe. */
    Postulante buscarXDocumento(String tipoDocumento, String numeroDocumento);

    /** Devuelve el postulante con ese correo (sin distinguir mayúsculas) o null si no existe. */
    Postulante buscarXCorreo(String correo);

    /** Registra el postulante (PKG_POSTULANTE.SP_INSERTAR) y devuelve el id generado. */
    Long insertar(Postulante postulante);

    /** Actualiza el postulante (PKG_POSTULANTE.SP_ACTUALIZAR). */
    void actualizar(Postulante postulante);

    void eliminar(Long id);
}
