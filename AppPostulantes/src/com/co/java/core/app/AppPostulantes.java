package com.co.java.core.app;

import com.co.java.core.bean.Municipio;
import com.co.java.core.bean.Postulante;
import com.co.java.core.persistencia.MunicipioDB;
import com.co.java.core.persistencia.MunicipioDBImpl;
import com.co.java.core.persistencia.PostulanteDB;
import com.co.java.core.persistencia.PostulanteDBImpl;
import java.time.LocalDate;

/**
 * Punto de entrada de la aplicación.
 * Por ahora ejecuta en consola un recorrido completo del CRUD (registrar, buscar,
 * actualizar y eliminar un postulante de prueba); cuando exista el formulario,
 * abrirá FrmPostulantes.
 */
public class AppPostulantes {

    static PostulanteDB postulanteDB = new PostulanteDBImpl();
    static MunicipioDB municipioDB = new MunicipioDBImpl();

    public static void main(String[] args) {
        titulo("Listar");
        listar();

        titulo("Registrar (SP_INSERTAR)");
        Postulante nuevo = new Postulante("PEDRO JOSÉ", "RAMÍREZ", "SALAS", "CC", "7170001",
                LocalDate.of(1995, 5, 10), null, "320 555 7788", "pedro.ramirez@example.com",
                "M", "CARRERA 10 # 12-34", new Municipio("15238"));
        Long id = insertar(nuevo);
        System.out.println("Id generado: " + id);

        titulo("Buscar \"rami\" sin tilde (SP_BUSCAR)");
        buscar("rami");

        titulo("Actualizar (SP_ACTUALIZAR)");
        nuevo.setApellidoMaterno("SALAZAR");
        nuevo.setTelefonoFijo("(608) 760 1122");
        nuevo.setMunicipio(new Municipio("15759"));
        actualizar(nuevo);
        buscarXId(id);

        titulo("Buscar por documento y por correo");
        System.out.println("CC 7170001 pertenece a: "
                + postulanteDB.buscarXDocumento("CC", "7170001").getNombreCompleto());
        System.out.println("Correo PEDRO.RAMIREZ@EXAMPLE.COM pertenece a: "
                + postulanteDB.buscarXCorreo("PEDRO.RAMIREZ@EXAMPLE.COM").getNombreCompleto());
        System.out.println("TI 7170001: " + postulanteDB.buscarXDocumento("TI", "7170001"));

        titulo("Eliminar");
        eliminar(id);
        listar();

        titulo("Municipios: código \"15\" y nombre \"medellin\" sin tilde");
        municipioDB.buscarXCodigo("15").forEach(System.out::println);
        municipioDB.buscarXNombre("medellin").forEach(System.out::println);
    }

    static void listar() {
        postulanteDB.listar().forEach(System.out::println);
    }

    static void buscar(String texto) {
        postulanteDB.buscar(texto).forEach(System.out::println);
    }

    static void buscarXId(Long id) {
        System.out.println(postulanteDB.buscarXId(id));
    }

    static Long insertar(Postulante postulante) {
        return postulanteDB.insertar(postulante);
    }

    static void actualizar(Postulante postulante) {
        postulanteDB.actualizar(postulante);
    }

    static void eliminar(Long id) {
        postulanteDB.eliminar(id);
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}
