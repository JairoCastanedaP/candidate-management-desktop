package com.co.java.core.app;

import com.co.java.core.bean.Municipio;
import com.co.java.core.bean.Postulante;
import com.co.java.core.persistencia.MunicipioDB;
import com.co.java.core.persistencia.MunicipioDBImpl;
import com.co.java.core.persistencia.PostulanteDB;
import com.co.java.core.persistencia.PostulanteDBImpl;
import com.co.java.core.validacion.ValidadorPostulante;
import java.time.LocalDate;
import java.util.List;

/**
 * Punto de entrada de la aplicación.
 * Por ahora ejecuta en consola un recorrido completo del CRUD (registrar, buscar,
 * actualizar y eliminar un postulante de prueba) y de las validaciones; cuando
 * exista el formulario, abrirá FrmPostulantes.
 */
public class AppPostulantes {

    static PostulanteDB postulanteDB = new PostulanteDBImpl();
    static MunicipioDB municipioDB = new MunicipioDBImpl();
    static ValidadorPostulante validador = new ValidadorPostulante(postulanteDB, municipioDB);

    public static void main(String[] args) {
        titulo("Listar");
        listar();

        titulo("Registrar (validar + SP_INSERTAR)");
        // Con espacios de más y en minúsculas: el validador lo normaliza
        Postulante nuevo = new Postulante("  pedro   josé ", "ramírez", "salas", "cc", "7170001",
                LocalDate.of(1995, 5, 10), null, "320 555 7788", "  Pedro.Ramirez@Example.com ",
                "m", "carrera 10 # 12-34", new Municipio("15238"));
        Long id = insertar(nuevo);
        System.out.println("Id generado: " + id);

        titulo("Buscar \"rami\" sin tilde (SP_BUSCAR)");
        buscar("rami");

        titulo("Actualizar (validar + SP_ACTUALIZAR)");
        // Mismo documento y correo: al actualizar no cuentan como duplicados de sí mismo
        nuevo.setApellidoMaterno("SALAZAR");
        nuevo.setTelefonoFijo("(608) 760 1122");
        nuevo.setMunicipio(new Municipio("15759"));
        actualizar(nuevo);
        buscarXId(id);

        titulo("Validaciones: casos inválidos");
        probarValidaciones();

        titulo("Eliminar");
        eliminar(id);
        listar();

        titulo("Municipios: código \"1523\" y nombre \"san jose\" sin tilde");
        municipioDB.buscarXCodigo("1523").forEach(System.out::println);
        municipioDB.buscarXNombre("san jose").forEach(System.out::println);
    }

    /** Un caso por regla. Cada uno parte de un postulante válido y cambia un solo dato. */
    static void probarValidaciones() {
        Postulante p = valido();
        p.setNombres("AL");
        p.setApellidoPaterno("PÉREZ 2");
        mostrarErrores("1. Nombre corto y apellido con número", p);

        p = valido();
        p.setNumeroDocumento("1049600001"); // ya pertenece a ANA LUCÍA (datos de ejemplo)
        mostrarErrores("2. Documento duplicado", p);

        p = valido();
        p.setTipoDocumento("TI");
        p.setNumeroDocumento("12.345");
        mostrarErrores("2. TI con puntos", p);

        p = valido();
        p.setTipoDocumento("CE");
        p.setNumeroDocumento("12345678");
        mostrarErrores("2. CE con 8 dígitos", p);

        p = valido();
        p.setCorreo("laura.gomez@gmail");
        mostrarErrores("3. Correo sin punto en el dominio", p);

        p = valido();
        p.setCorreo("laura@@gmail.com");
        mostrarErrores("3. Correo con dos @", p);

        p = valido();
        p.setCorreo("ANA.TORRES@EXAMPLE.COM"); // ya pertenece a ANA LUCÍA
        mostrarErrores("3. Correo duplicado (en mayúsculas)", p);

        p = valido();
        p.setTelefonoFijo(null);
        p.setTelefonoCelular("  ");
        mostrarErrores("4. Sin teléfonos", p);

        p = valido();
        p.setTelefonoCelular("210 123 4567");
        mostrarErrores("4. Celular que no empieza por 3", p);

        p = valido();
        p.setTelefonoFijo("(601) 742 1234"); // Tunja es Boyacá: indicativo 608
        mostrarErrores("4. Fijo con indicativo de otro departamento", p);

        p = valido();
        p.setTelefonoFijo("608.742.1234");
        mostrarErrores("4. Fijo con puntos", p);

        p = valido();
        p.setSexo("X");
        mostrarErrores("5. Sexo no válido", p);

        p = valido();
        p.setApellidoMaterno("   ");
        mostrarErrores("Válido: apellido materno vacío (es opcional)", p);
    }

    static Postulante valido() {
        return new Postulante("LAURA", "GÓMEZ", "ROJAS", "CC", "1052000111",
                LocalDate.of(2000, 3, 15), "(608) 742 9999", "311 222 3344",
                "laura.gomez@gmail.com", "F", "CALLE 20 # 9-10", new Municipio("15001"));
    }

    static void mostrarErrores(String caso, Postulante p) {
        List<String> errores = validador.validar(p);
        System.out.println("- " + caso + ":");
        if (errores.isEmpty()) {
            System.out.println("    sin errores");
        }
        errores.forEach(e -> System.out.println("    " + e));
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

    /** Valida y registra. Si hay errores los muestra y devuelve null. */
    static Long insertar(Postulante postulante) {
        List<String> errores = validador.validar(postulante);
        if (!errores.isEmpty()) {
            errores.forEach(System.out::println);
            return null;
        }
        return postulanteDB.insertar(postulante);
    }

    /** Valida y actualiza. Si hay errores los muestra y no actualiza. */
    static void actualizar(Postulante postulante) {
        List<String> errores = validador.validar(postulante);
        if (!errores.isEmpty()) {
            errores.forEach(System.out::println);
            return;
        }
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
