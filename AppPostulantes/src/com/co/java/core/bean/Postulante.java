package com.co.java.core.bean;

import java.time.LocalDate;

/**
 * Datos personales del postulante (ficha de postulante).
 * Tipos de documento: CC, TI, CE, PA y PPT.
 */
public class Postulante {

    private Long id;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String tipoDocumento;
    private String numeroDocumento;
    private LocalDate fechaNacimiento;
    private String telefonoFijo;
    private String telefonoCelular;
    private String correo;
    private String sexo;
    private String direccion;
    private Municipio municipio;

    public Postulante() {
    }

    public Postulante(String nombres, String apellidoPaterno, String apellidoMaterno,
                      String tipoDocumento, String numeroDocumento, LocalDate fechaNacimiento,
                      String telefonoFijo, String telefonoCelular, String correo, String sexo,
                      String direccion, Municipio municipio) {
        this.nombres = nombres;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.telefonoFijo = telefonoFijo;
        this.telefonoCelular = telefonoCelular;
        this.correo = correo;
        this.sexo = sexo;
        this.direccion = direccion;
        this.municipio = municipio;
    }

    public Postulante(Long id, String nombres, String apellidoPaterno, String apellidoMaterno,
                      String tipoDocumento, String numeroDocumento, LocalDate fechaNacimiento,
                      String telefonoFijo, String telefonoCelular, String correo, String sexo,
                      String direccion, Municipio municipio) {
        this(nombres, apellidoPaterno, apellidoMaterno, tipoDocumento, numeroDocumento,
                fechaNacimiento, telefonoFijo, telefonoCelular, correo, sexo, direccion, municipio);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefonoFijo() {
        return telefonoFijo;
    }

    public void setTelefonoFijo(String telefonoFijo) {
        this.telefonoFijo = telefonoFijo;
    }

    public String getTelefonoCelular() {
        return telefonoCelular;
    }

    public void setTelefonoCelular(String telefonoCelular) {
        this.telefonoCelular = telefonoCelular;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    /**
     * Documento en el formato "CC 1049600001". Se usa en mensajes y listados.
     */
    public String getDocumento() {
        return tipoDocumento + " " + numeroDocumento;
    }

    /**
     * Nombre completo en el formato "APELLIDO_PATERNO APELLIDO_MATERNO, NOMBRES".
     * Se usa en los mensajes de documento o correo duplicado.
     */
    public String getNombreCompleto() {
        String apellidos = apellidoMaterno == null || apellidoMaterno.isBlank()
                ? apellidoPaterno
                : apellidoPaterno + " " + apellidoMaterno;
        return apellidos + ", " + nombres;
    }

    @Override
    public String toString() {
        return "Postulante{" +
                "id=" + id +
                ", nombres='" + nombres + '\'' +
                ", apellidoPaterno='" + apellidoPaterno + '\'' +
                ", apellidoMaterno='" + apellidoMaterno + '\'' +
                ", documento='" + getDocumento() + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", telefonoFijo='" + telefonoFijo + '\'' +
                ", telefonoCelular='" + telefonoCelular + '\'' +
                ", correo='" + correo + '\'' +
                ", sexo='" + sexo + '\'' +
                ", direccion='" + direccion + '\'' +
                ", municipio=" + municipio +
                '}';
    }
}
