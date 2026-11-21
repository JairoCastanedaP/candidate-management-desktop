package com.co.java.core.validacion;

import com.co.java.core.bean.Municipio;
import com.co.java.core.bean.Postulante;
import com.co.java.core.persistencia.MunicipioDB;
import com.co.java.core.persistencia.PostulanteDB;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de negocio del postulante. Se ejecutan antes de registrar o actualizar.
 *
 * <p>{@link #validar(Postulante)} primero normaliza los datos (quita espacios sobrantes,
 * pasa a mayúsculas y el correo a minúsculas) y luego devuelve todos los errores
 * encontrados, para que el formulario los muestre juntos. Si la lista está vacía,
 * el postulante se puede guardar.</p>
 *
 * <p>No se usan expresiones regulares: cada regla recorre el texto carácter por carácter.</p>
 */
public class ValidadorPostulante {

    public static final int LONGITUD_MINIMA_NOMBRE = 3;
    public static final int LONGITUD_MAXIMA_NOMBRE = 60;
    public static final int LONGITUD_MAXIMA_CORREO = 120;
    public static final int LONGITUD_MAXIMA_DIRECCION = 200;

    public static final String[] TIPOS_DOCUMENTO = {"CC", "TI", "CE", "PA", "PPT"};
    public static final String[] SEXOS = {"F", "M"};

    private final PostulanteDB postulanteDB;
    private final MunicipioDB municipioDB;

    public ValidadorPostulante(PostulanteDB postulanteDB, MunicipioDB municipioDB) {
        this.postulanteDB = postulanteDB;
        this.municipioDB = municipioDB;
    }

    public List<String> validar(Postulante p) {
        normalizar(p);
        List<String> errores = new ArrayList<>();

        // 1. Nombres y apellidos
        validarNombre("Nombres", p.getNombres(), true, errores);
        validarNombre("Apellido paterno", p.getApellidoPaterno(), true, errores);
        validarNombre("Apellido materno", p.getApellidoMaterno(), false, errores);

        // 2. Documento según su tipo, sin duplicados
        if (validarDocumento(p.getTipoDocumento(), p.getNumeroDocumento(), errores)) {
            Postulante existente = postulanteDB.buscarXDocumento(p.getTipoDocumento(), p.getNumeroDocumento());
            if (esOtroPostulante(existente, p)) {
                errores.add("El documento " + p.getDocumento() + " ya pertenece a "
                        + existente.getNombreCompleto() + ".");
            }
        }

        // 3. Correo válido (sin regex), sin duplicados
        String errorCorreo = validarCorreo(p.getCorreo());
        if (errorCorreo != null) {
            errores.add(errorCorreo);
        } else {
            Postulante existente = postulanteDB.buscarXCorreo(p.getCorreo());
            if (esOtroPostulante(existente, p)) {
                errores.add("El correo " + p.getCorreo() + " ya pertenece a "
                        + existente.getNombreCompleto() + ".");
            }
        }

        // Municipio: se necesita antes del teléfono fijo para conocer el indicativo
        Municipio municipio = validarMunicipio(p, errores);

        // 4. Al menos un teléfono con formato colombiano
        validarTelefonos(p.getTelefonoFijo(), p.getTelefonoCelular(), municipio, errores);

        // 5. Sexo F o M
        if (!contiene(SEXOS, p.getSexo())) {
            errores.add("Seleccione el sexo: F (femenino) o M (masculino).");
        }

        // Otros datos obligatorios de la ficha
        validarFechaNacimiento(p.getFechaNacimiento(), errores);
        if (p.getDireccion() == null) {
            errores.add("La dirección es obligatoria.");
        } else if (p.getDireccion().length() > LONGITUD_MAXIMA_DIRECCION) {
            errores.add("La dirección admite máximo " + LONGITUD_MAXIMA_DIRECCION + " caracteres.");
        }

        return errores;
    }

    // ------------------------------------------------------------------ normalización

    /** Quita espacios sobrantes, pasa los textos a mayúsculas y el correo a minúsculas. */
    private void normalizar(Postulante p) {
        p.setNombres(mayusculas(limpiar(p.getNombres())));
        p.setApellidoPaterno(mayusculas(limpiar(p.getApellidoPaterno())));
        p.setApellidoMaterno(mayusculas(limpiar(p.getApellidoMaterno())));
        p.setTipoDocumento(mayusculas(limpiar(p.getTipoDocumento())));
        p.setNumeroDocumento(mayusculas(limpiar(p.getNumeroDocumento())));
        p.setTelefonoFijo(limpiar(p.getTelefonoFijo()));
        p.setTelefonoCelular(limpiar(p.getTelefonoCelular()));
        String correo = limpiar(p.getCorreo());
        p.setCorreo(correo == null ? null : correo.toLowerCase());
        p.setSexo(mayusculas(limpiar(p.getSexo())));
        p.setDireccion(mayusculas(limpiar(p.getDireccion())));
    }

    /** Quita espacios al inicio y al final, deja uno solo entre palabras y devuelve null si queda vacío. */
    static String limpiar(String texto) {
        if (texto == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean espacioPendiente = false;
        for (char c : texto.trim().toCharArray()) {
            if (Character.isWhitespace(c)) {
                espacioPendiente = true;
            } else {
                if (espacioPendiente) {
                    sb.append(' ');
                    espacioPendiente = false;
                }
                sb.append(c);
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private static String mayusculas(String texto) {
        return texto == null ? null : texto.toUpperCase();
    }

    // ------------------------------------------------------------------ 1. nombres

    private void validarNombre(String campo, String valor, boolean obligatorio, List<String> errores) {
        if (valor == null) {
            if (obligatorio) {
                errores.add("El campo " + campo + " es obligatorio.");
            }
            return;
        }
        if (valor.length() < LONGITUD_MINIMA_NOMBRE || valor.length() > LONGITUD_MAXIMA_NOMBRE) {
            errores.add("El campo " + campo + " debe tener entre " + LONGITUD_MINIMA_NOMBRE + " y "
                    + LONGITUD_MAXIMA_NOMBRE + " caracteres (tiene " + valor.length() + ").");
            return;
        }
        for (char c : valor.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ' && c != '\'') {
                errores.add("El campo " + campo + " solo admite letras, espacios y apóstrofo (carácter no válido: '" + c + "').");
                return;
            }
        }
    }

    // ------------------------------------------------------------------ 2. documento

    /** Devuelve true si el tipo y el número tienen formato válido (entonces se revisa el duplicado). */
    private boolean validarDocumento(String tipo, String numero, List<String> errores) {
        if (!contiene(TIPOS_DOCUMENTO, tipo)) {
            errores.add("Seleccione el tipo de documento: CC, TI, CE, PA o PPT.");
            return false;
        }
        if (numero == null) {
            errores.add("El número de documento es obligatorio.");
            return false;
        }
        int minimo;
        int maximo;
        switch (tipo) {
            case "CC" -> { minimo = 6; maximo = 10; }   // cédula de ciudadanía
            case "TI" -> { minimo = 10; maximo = 11; }  // tarjeta de identidad
            case "CE" -> { minimo = 6; maximo = 7; }    // cédula de extranjería
            case "PA" -> { minimo = 5; maximo = 9; }    // pasaporte
            default -> { minimo = 6; maximo = 8; }      // PPT: permiso por protección temporal
        }
        boolean alfanumerico = tipo.equals("PA");
        for (char c : numero.toCharArray()) {
            boolean valido = esDigito(c) || (alfanumerico && esLetraAscii(c));
            if (!valido) {
                errores.add(alfanumerico
                        ? "El pasaporte solo admite letras y números, sin espacios ni guiones."
                        : "El número de " + tipo + " solo admite dígitos, sin puntos ni espacios.");
                return false;
            }
        }
        if (numero.length() < minimo || numero.length() > maximo) {
            errores.add("El número de " + tipo + " debe tener " + rango(minimo, maximo)
                    + (alfanumerico ? " caracteres" : " dígitos") + " (tiene " + numero.length() + ").");
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ 3. correo (sin regex)

    /** Devuelve el mensaje de error o null si el correo es válido. */
    static String validarCorreo(String correo) {
        if (correo == null) {
            return "El correo es obligatorio.";
        }
        if (correo.length() > LONGITUD_MAXIMA_CORREO) {
            return "El correo admite máximo " + LONGITUD_MAXIMA_CORREO + " caracteres.";
        }
        int arroba = correo.indexOf('@');
        if (arroba == -1 || arroba != correo.lastIndexOf('@')) {
            return "El correo debe tener una sola @ (ejemplo: nombre@dominio.com).";
        }
        String usuario = correo.substring(0, arroba);
        String dominio = correo.substring(arroba + 1);
        if (usuario.isEmpty()) {
            return "El correo no tiene usuario antes de la @.";
        }
        if (dominio.isEmpty()) {
            return "El correo no tiene dominio después de la @.";
        }
        for (char c : usuario.toCharArray()) {
            if (!esLetraAscii(c) && !esDigito(c) && c != '.' && c != '_' && c != '-' && c != '+') {
                return "El usuario del correo tiene un carácter no válido: '" + c + "'.";
            }
        }
        if (usuario.startsWith(".") || usuario.endsWith(".") || usuario.contains("..")) {
            return "El usuario del correo no puede empezar ni terminar en punto ni tener puntos seguidos.";
        }
        for (char c : dominio.toCharArray()) {
            if (!esLetraAscii(c) && !esDigito(c) && c != '.' && c != '-') {
                return "El dominio del correo tiene un carácter no válido: '" + c + "'.";
            }
        }
        if (!dominio.contains(".")) {
            return "El dominio del correo debe tener al menos un punto (ejemplo: dominio.com).";
        }
        if (dominio.startsWith(".") || dominio.endsWith(".") || dominio.contains("..")) {
            return "El dominio del correo no puede empezar ni terminar en punto ni tener puntos seguidos.";
        }
        // Cada parte del dominio (gmail / com) no puede empezar ni terminar en guion
        int inicio = 0;
        while (inicio < dominio.length()) {
            int fin = dominio.indexOf('.', inicio);
            if (fin == -1) {
                fin = dominio.length();
            }
            String parte = dominio.substring(inicio, fin);
            if (parte.startsWith("-") || parte.endsWith("-")) {
                return "Las partes del dominio no pueden empezar ni terminar en guion.";
            }
            inicio = fin + 1;
        }
        // La extensión final (com, co, org) debe tener al menos 2 letras
        String extension = dominio.substring(dominio.lastIndexOf('.') + 1);
        if (extension.length() < 2) {
            return "La extensión del dominio debe tener al menos 2 letras (ejemplo: .com, .co).";
        }
        for (char c : extension.toCharArray()) {
            if (!esLetraAscii(c)) {
                return "La extensión del dominio solo admite letras (ejemplo: .com, .co).";
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ municipio

    /** Devuelve el municipio completo (con departamento e indicativo) o null si no es válido. */
    private Municipio validarMunicipio(Postulante p, List<String> errores) {
        if (p.getMunicipio() == null || limpiar(p.getMunicipio().getMunicipioId()) == null) {
            errores.add("Seleccione el municipio de residencia.");
            return null;
        }
        Municipio municipio = municipioDB.buscarXId(limpiar(p.getMunicipio().getMunicipioId()));
        if (municipio == null) {
            errores.add("El municipio " + p.getMunicipio().getMunicipioId() + " no existe en la DIVIPOLA.");
            return null;
        }
        p.setMunicipio(municipio);
        return municipio;
    }

    // ------------------------------------------------------------------ 4. teléfonos

    private void validarTelefonos(String fijo, String celular, Municipio municipio, List<String> errores) {
        if (fijo == null && celular == null) {
            errores.add("Registre al menos un teléfono: fijo o celular.");
            return;
        }
        if (celular != null) {
            String digitos = soloDigitos("El celular", celular, errores);
            if (digitos != null) {
                if (digitos.length() != 10 || digitos.charAt(0) != '3') {
                    errores.add("El celular debe tener 10 dígitos y empezar por 3 (ejemplo: 310 123 4567).");
                }
            }
        }
        if (fijo != null) {
            String digitos = soloDigitos("El teléfono fijo", fijo, errores);
            if (digitos != null) {
                if (digitos.length() != 10 || !digitos.startsWith("60")
                        || digitos.charAt(2) < '1' || digitos.charAt(2) > '8') {
                    errores.add("El teléfono fijo debe tener el indicativo 601 a 608 y 7 dígitos "
                            + "(ejemplo: (608) 742 1234).");
                } else if (municipio != null) {
                    String indicativo = municipio.getDepartamento().getIndicativo();
                    if (!digitos.startsWith(indicativo)) {
                        errores.add("El teléfono fijo de " + municipio.getNombre() + " ("
                                + municipio.getDepartamento().getNombre() + ") debe empezar por "
                                + indicativo + ".");
                    }
                }
            }
        }
    }

    /**
     * Revisa que el teléfono solo tenga dígitos, espacios, guiones y paréntesis.
     * Devuelve solo los dígitos, o null si encontró un carácter no permitido.
     */
    private String soloDigitos(String campo, String telefono, List<String> errores) {
        StringBuilder digitos = new StringBuilder();
        for (char c : telefono.toCharArray()) {
            if (esDigito(c)) {
                digitos.append(c);
            } else if (c != ' ' && c != '-' && c != '(' && c != ')') {
                errores.add(campo + " solo admite dígitos, espacios, guiones y paréntesis "
                        + "(carácter no válido: '" + c + "').");
                return null;
            }
        }
        return digitos.toString();
    }

    // ------------------------------------------------------------------ fecha de nacimiento

    private void validarFechaNacimiento(LocalDate fecha, List<String> errores) {
        if (fecha == null) {
            errores.add("La fecha de nacimiento es obligatoria.");
        } else if (fecha.isAfter(LocalDate.now())) {
            errores.add("La fecha de nacimiento no puede ser futura.");
        } else if (fecha.isBefore(LocalDate.now().minusYears(100))) {
            errores.add("La fecha de nacimiento no puede ser de hace más de 100 años.");
        }
    }

    // ------------------------------------------------------------------ utilidades

    /** true si hay otro postulante (con id distinto) con el mismo dato; al actualizar, uno mismo no cuenta. */
    private static boolean esOtroPostulante(Postulante existente, Postulante p) {
        return existente != null && !existente.getId().equals(p.getId());
    }

    private static boolean contiene(String[] valores, String valor) {
        for (String v : valores) {
            if (v.equals(valor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean esLetraAscii(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static String rango(int minimo, int maximo) {
        return minimo == maximo ? String.valueOf(minimo) : "entre " + minimo + " y " + maximo;
    }
}
