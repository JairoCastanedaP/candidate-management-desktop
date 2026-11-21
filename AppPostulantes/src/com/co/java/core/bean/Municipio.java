package com.co.java.core.bean;

/**
 * Municipio de Colombia según la codificación DIVIPOLA del DANE:
 * 5 dígitos, 2 del departamento y 3 del municipio (15001 = Tunja, Boyacá).
 */
public class Municipio {

    private String municipioId;
    private String nombre;
    private Departamento departamento;

    public Municipio() {
    }

    public Municipio(String municipioId) {
        this.municipioId = municipioId;
    }

    public Municipio(String municipioId, String nombre, Departamento departamento) {
        this.municipioId = municipioId;
        this.nombre = nombre;
        this.departamento = departamento;
    }

    public String getMunicipioId() {
        return municipioId;
    }

    public void setMunicipioId(String municipioId) {
        this.municipioId = municipioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    @Override
    public String toString() {
        return municipioId + " - " + nombre + (departamento == null ? "" : " (" + departamento.getNombre() + ")");
    }
}
