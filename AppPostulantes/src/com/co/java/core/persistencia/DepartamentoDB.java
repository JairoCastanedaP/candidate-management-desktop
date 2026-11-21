package com.co.java.core.persistencia;

import com.co.java.core.bean.Departamento;
import java.util.List;

public interface DepartamentoDB {

    /** Los 33 departamentos ordenados por nombre. */
    List<Departamento> listar();
}
