package com.co.java.core.bean;

/**
 * Departamento de Colombia según la codificación DIVIPOLA del DANE (2 dígitos).
 * El indicativo es el prefijo de telefonía fija de su región (601 a 608).
 */
public class Departamento {

    private String departamentoId;
    private String nombre;
    private String indicativo;

    public Departamento() {
    }

    public Departamento(String departamentoId, String nombre, String indicativo) {
        this.departamentoId = departamentoId;
        this.nombre = nombre;
        this.indicativo = indicativo;
    }

    public String getDepartamentoId() {
        return departamentoId;
    }

    public void setDepartamentoId(String departamentoId) {
        this.departamentoId = departamentoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIndicativo() {
        return indicativo;
    }

    public void setIndicativo(String indicativo) {
        this.indicativo = indicativo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
