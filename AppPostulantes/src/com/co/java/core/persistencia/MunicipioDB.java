package com.co.java.core.persistencia;

import com.co.java.core.bean.Municipio;
import java.util.List;

public interface MunicipioDB {

    /** Devuelve el municipio o null si no existe. */
    Municipio buscarXId(String municipioId);

    /** Municipios de un departamento, ordenados por nombre. */
    List<Municipio> listarXDepartamento(String departamentoId);

    /** Municipios cuyo código empieza con el texto indicado (por ejemplo "15" = Boyacá). */
    List<Municipio> buscarXCodigo(String codigo);

    /** Municipios cuyo nombre contiene el texto indicado, sin distinguir mayúsculas ni tildes. */
    List<Municipio> buscarXNombre(String nombre);
}
