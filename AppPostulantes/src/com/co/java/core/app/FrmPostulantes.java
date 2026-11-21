package com.co.java.core.app;

import com.co.java.core.bean.Departamento;
import com.co.java.core.bean.Municipio;
import com.co.java.core.bean.Postulante;
import com.co.java.core.persistencia.DepartamentoDB;
import com.co.java.core.persistencia.DepartamentoDBImpl;
import com.co.java.core.persistencia.MunicipioDB;
import com.co.java.core.persistencia.MunicipioDBImpl;
import com.co.java.core.persistencia.PostulanteDB;
import com.co.java.core.persistencia.PostulanteDBImpl;
import com.co.java.core.persistencia.util.DBException;
import com.co.java.core.validacion.ValidadorPostulante;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 * Formulario principal: registro, actualización, eliminación y búsqueda de postulantes.
 *
 * <p>El diseño se edita con el GUI Builder de NetBeans (pestaña Design). El código
 * entre las marcas GEN-BEGIN y GEN-END lo genera NetBeans a partir de
 * FrmPostulantes.form: no se modifica a mano.</p>
 */
public class FrmPostulantes extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    private final PostulanteDB postulanteDB = new PostulanteDBImpl();
    private final DepartamentoDB departamentoDB = new DepartamentoDBImpl();
    private final MunicipioDB municipioDB = new MunicipioDBImpl();
    private final ValidadorPostulante validador = new ValidadorPostulante(postulanteDB, municipioDB);

    /** dd/MM/uuuu estricto: rechaza fechas que no existen, como 31/02/2000. */
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Id del postulante que se está editando; null cuando se registra uno nuevo. */
    private Long idEditando = null;

    /** Postulantes que se ven en la tabla, en el mismo orden de las filas. */
    private List<Postulante> postulantes = new ArrayList<>();

    /**
     * true mientras el código cambia el departamento seleccionado, para que el
     * evento del combo no recargue los municipios una vez más.
     */
    private boolean cambiandoDepartamento = false;

    public FrmPostulantes() {
        initComponents();
        configurarFechaNacimiento();
        configurarTabla();
        cargarDepartamentos();
        btnEliminar.setEnabled(false);
        listar();
        // No dejar achicar la ventana por debajo del tamaño en que se ven todos los campos
        setMinimumSize(getSize());
    }

    /** Máscara dd/mm/aaaa: el usuario solo escribe los dígitos. */
    private void configurarFechaNacimiento() {
        try {
            MaskFormatter mascara = new MaskFormatter("##/##/####");
            mascara.setPlaceholderCharacter('_');
            txtFechaNacimiento.setFormatterFactory(new DefaultFormatterFactory(mascara));
        } catch (ParseException e) {
            throw new IllegalStateException("Máscara de fecha no válida", e);
        }
    }

    /** Ancho de cada columna: Id, Documento, Nombres, Ap. paterno, Ap. materno, Correo, Teléfono, Municipio. */
    private void configurarTabla() {
        int[] anchos = {40, 110, 130, 110, 110, 200, 100, 180};
        for (int i = 0; i < anchos.length; i++) {
            tblPostulantes.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tblPostulantes.getTableHeader().setReorderingAllowed(false);
        // Al seleccionar una fila (con el mouse o con las flechas) se carga en el formulario
        tblPostulantes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionado();
            }
        });
    }

    // ------------------------------------------------------------------ listado y búsqueda

    /** Carga todos los postulantes (consulta con PreparedStatement). */
    private void listar() {
        try {
            mostrarEnTabla(postulanteDB.listar());
        } catch (DBException e) {
            mostrarError(e);
        }
    }

    /** Busca por nombres o apellidos con PKG_POSTULANTE.SP_BUSCAR; vacío trae todos. */
    private void buscar() {
        try {
            mostrarEnTabla(postulanteDB.buscar(txtBuscar.getText()));
        } catch (DBException e) {
            mostrarError(e);
        }
    }

    private void mostrarEnTabla(List<Postulante> lista) {
        postulantes = lista;
        DefaultTableModel modelo = (DefaultTableModel) tblPostulantes.getModel();
        modelo.setRowCount(0);
        for (Postulante p : lista) {
            modelo.addRow(new Object[]{
                p.getId(),
                p.getDocumento(),
                p.getNombres(),
                p.getApellidoPaterno(),
                p.getApellidoMaterno(),
                p.getCorreo(),
                p.getTelefonoCelular() != null ? p.getTelefonoCelular() : p.getTelefonoFijo(),
                nombreMunicipio(p.getMunicipio())
            });
        }
        lblTotal.setText(lista.size() == 1 ? "1 postulante" : lista.size() + " postulantes");
    }

    /** "TUNJA (BOYACÁ)"; si el municipio y el departamento se llaman igual (Bogotá), solo el nombre. */
    private String nombreMunicipio(Municipio m) {
        if (m == null) {
            return "";
        }
        if (m.getDepartamento() == null || m.getDepartamento().getNombre().equals(m.getNombre())) {
            return m.getNombre();
        }
        return m.getNombre() + " (" + m.getDepartamento().getNombre() + ")";
    }

    // ------------------------------------------------------------------ registrar y actualizar

    /**
     * Lee el formulario, valida y registra (SP_INSERTAR) o actualiza (SP_ACTUALIZAR).
     * Si hay errores, los muestra todos juntos y no guarda nada.
     */
    private void guardar() {
        try {
            List<String> errores = new ArrayList<>();
            Postulante postulante = leerFormulario(errores);
            List<String> erroresValidador = validador.validar(postulante);   // también consulta duplicados
            if (!errores.isEmpty()) {
                // la fecha ya tiene su propio mensaje: no repetir "es obligatoria"
                erroresValidador.removeIf(e -> e.startsWith("La fecha de nacimiento"));
            }
            errores.addAll(erroresValidador);
            if (!errores.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Revise los siguientes datos:\n\n• " + String.join("\n• ", errores),
                        "Datos incompletos o no válidos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String mensaje;
            if (idEditando == null) {
                Long id = postulanteDB.insertar(postulante);
                mensaje = "Postulante registrado con id " + id + ":\n" + postulante.getNombreCompleto();
            } else {
                postulanteDB.actualizar(postulante);
                mensaje = "Postulante actualizado:\n" + postulante.getNombreCompleto();
            }
            JOptionPane.showMessageDialog(this, mensaje, "Guardar", JOptionPane.INFORMATION_MESSAGE);
            limpiar();
            buscar();
        } catch (DBException e) {
            mostrarError(e);
        }
    }

    /**
     * Arma el postulante con lo que hay en pantalla. Los combos muestran
     * "CC - Cédula de ciudadanía" o "F - Femenino": se guarda solo el código.
     * Si la fecha no es válida agrega el mensaje a errores y la deja en null.
     */
    private Postulante leerFormulario(List<String> errores) {
        Postulante p = new Postulante();
        p.setId(idEditando);
        p.setNombres(txtNombres.getText());
        p.setApellidoPaterno(txtApellidoPaterno.getText());
        p.setApellidoMaterno(txtApellidoMaterno.getText());
        p.setTipoDocumento(codigo((String) cboTipoDocumento.getSelectedItem()));
        p.setNumeroDocumento(txtNumeroDocumento.getText());
        p.setFechaNacimiento(leerFecha(errores));
        p.setTelefonoFijo(txtTelefonoFijo.getText());
        p.setTelefonoCelular(txtTelefonoCelular.getText());
        p.setCorreo(txtCorreo.getText());
        p.setSexo(cboSexo.getSelectedIndex() <= 0 ? null : codigo((String) cboSexo.getSelectedItem()));
        p.setDireccion(txtDireccion.getText());
        p.setMunicipio((Municipio) cboMunicipio.getSelectedItem());
        return p;
    }

    /** "CC - Cédula de ciudadanía" → "CC" */
    private static String codigo(String opcion) {
        return opcion == null ? null : opcion.substring(0, opcion.indexOf(" - "));
    }

    private LocalDate leerFecha(List<String> errores) {
        String texto = txtFechaNacimiento.getText();
        if (texto.equals("__/__/____") || texto.isBlank()) {
            return null;                       // el validador avisa que es obligatoria
        }
        if (texto.contains("_")) {
            errores.add("La fecha de nacimiento está incompleta: use el formato dd/mm/aaaa.");
            return null;
        }
        try {
            return LocalDate.parse(texto, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            errores.add("La fecha de nacimiento " + texto + " no existe.");
            return null;
        }
    }

    /** Deja el formulario en blanco, listo para registrar un postulante nuevo. */
    private void limpiar() {
        idEditando = null;
        btnEliminar.setEnabled(false);
        titulo("Datos del postulante");
        txtNombres.setText("");
        txtApellidoPaterno.setText("");
        txtApellidoMaterno.setText("");
        cboTipoDocumento.setSelectedIndex(0);
        txtNumeroDocumento.setText("");
        txtFechaNacimiento.setValue(null);
        txtCorreo.setText("");
        cboSexo.setSelectedIndex(0);
        txtTelefonoFijo.setText("");
        txtTelefonoCelular.setText("");
        txtDireccion.setText("");
        txtBuscarMunicipio.setText("");
        cambiandoDepartamento = true;
        cboDepartamento.setSelectedIndex(-1);
        cambiandoDepartamento = false;
        cargarMunicipios(null);
        tblPostulantes.clearSelection();
        txtNombres.requestFocusInWindow();
    }

    // ------------------------------------------------------------------ editar y eliminar

    /** Pasa el postulante de la fila seleccionada al formulario para editarlo. */
    private void cargarSeleccionado() {
        int fila = tblPostulantes.getSelectedRow();
        if (fila < 0) {
            return;
        }
        Postulante p = postulantes.get(fila);
        idEditando = p.getId();
        txtNombres.setText(p.getNombres());
        txtApellidoPaterno.setText(p.getApellidoPaterno());
        txtApellidoMaterno.setText(vacioSiNulo(p.getApellidoMaterno()));
        seleccionarOpcion(cboTipoDocumento, p.getTipoDocumento());
        txtNumeroDocumento.setText(p.getNumeroDocumento());
        txtFechaNacimiento.setValue(p.getFechaNacimiento() == null ? null : p.getFechaNacimiento().format(FORMATO_FECHA));
        txtCorreo.setText(p.getCorreo());
        seleccionarOpcion(cboSexo, p.getSexo());
        txtTelefonoFijo.setText(vacioSiNulo(p.getTelefonoFijo()));
        txtTelefonoCelular.setText(vacioSiNulo(p.getTelefonoCelular()));
        txtDireccion.setText(p.getDireccion());
        txtBuscarMunicipio.setText("");
        try {
            seleccionarMunicipio(p.getMunicipio());
        } catch (DBException e) {
            mostrarError(e);
        }
        btnEliminar.setEnabled(true);
        titulo("Editando postulante id " + p.getId() + " - " + p.getDocumento());
    }

    /** Pide confirmación y elimina el postulante que se está editando. */
    private void eliminar() {
        if (idEditando == null) {
            JOptionPane.showMessageDialog(this, "Seleccione en la tabla el postulante que desea eliminar.",
                    "Eliminar postulante", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String materno = txtApellidoMaterno.getText().trim();
        String nombre = txtApellidoPaterno.getText().trim() + (materno.isEmpty() ? "" : " " + materno)
                + ", " + txtNombres.getText().trim();
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al postulante " + nombre + " (id " + idEditando + ")?\nEsta acción no se puede deshacer.",
                "Eliminar postulante", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            postulanteDB.eliminar(idEditando);
            JOptionPane.showMessageDialog(this, "Postulante eliminado.", "Eliminar postulante",
                    JOptionPane.INFORMATION_MESSAGE);
            limpiar();
            buscar();
        } catch (DBException e) {
            mostrarError(e);
        }
    }

    /** Selecciona en el combo la opción que empieza con el código: "CC" → "CC - Cédula de ciudadanía". */
    private static void seleccionarOpcion(javax.swing.JComboBox<String> combo, String codigo) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).startsWith(codigo + " - ")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    private static String vacioSiNulo(String texto) {
        return texto == null ? "" : texto;
    }

    /** Título del recuadro de datos: indica si se registra uno nuevo o se edita uno existente. */
    private void titulo(String texto) {
        ((TitledBorder) pnlDatos.getBorder()).setTitle(texto);
        pnlDatos.repaint();
    }

    // ------------------------------------------------------------------ departamento y municipio

    /** Llena el combo de departamentos y lo deja sin selección. */
    private void cargarDepartamentos() {
        cambiandoDepartamento = true;
        try {
            DefaultComboBoxModel<Departamento> modelo = new DefaultComboBoxModel<>();
            departamentoDB.listar().forEach(modelo::addElement);
            cboDepartamento.setModel(modelo);
            cboDepartamento.setSelectedIndex(-1);
        } catch (DBException e) {
            mostrarError(e);
        } finally {
            cambiandoDepartamento = false;
        }
    }

    /** Llena el combo de municipios con los del departamento indicado y lo deja sin selección. */
    private void cargarMunicipios(Departamento departamento) {
        DefaultComboBoxModel<Municipio> modelo = new DefaultComboBoxModel<>();
        if (departamento != null) {
            municipioDB.listarXDepartamento(departamento.getDepartamentoId()).forEach(modelo::addElement);
        }
        cboMunicipio.setModel(modelo);
        cboMunicipio.setSelectedIndex(-1);
    }

    /**
     * Busca municipios por código DIVIPOLA (si el texto son solo dígitos) o por nombre
     * (sin mayúsculas ni tildes). Si hay uno, lo selecciona; si hay varios, pide elegir.
     */
    private void buscarMunicipio() {
        String texto = txtBuscarMunicipio.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba un código DIVIPOLA (por ejemplo 15001) o parte del nombre del municipio.",
                    "Buscar municipio", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            List<Municipio> encontrados = soloDigitos(texto)
                    ? municipioDB.buscarXCodigo(texto)
                    : municipioDB.buscarXNombre(texto);
            if (encontrados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontró ningún municipio con \"" + texto + "\".",
                        "Buscar municipio", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Municipio elegido = encontrados.size() == 1 ? encontrados.get(0) : elegirMunicipio(encontrados, texto);
            if (elegido != null) {
                seleccionarMunicipio(elegido);
            }
        } catch (DBException e) {
            mostrarError(e);
        }
    }

    /** Muestra la lista de municipios encontrados; devuelve el elegido o null si se cancela. */
    private Municipio elegirMunicipio(List<Municipio> encontrados, String texto) {
        Municipio[] opciones = encontrados.toArray(new Municipio[0]);
        return (Municipio) JOptionPane.showInputDialog(this,
                "Se encontraron " + opciones.length + " municipios para \"" + texto + "\". Seleccione uno:",
                "Buscar municipio", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
    }

    /**
     * Selecciona el departamento del municipio, carga sus municipios y deja elegido el indicado.
     * También se usará al cargar un postulante de la tabla para editarlo.
     */
    private void seleccionarMunicipio(Municipio municipio) {
        String departamentoId = municipio.getDepartamento().getDepartamentoId();
        cambiandoDepartamento = true;
        try {
            for (int i = 0; i < cboDepartamento.getItemCount(); i++) {
                if (cboDepartamento.getItemAt(i).getDepartamentoId().equals(departamentoId)) {
                    cboDepartamento.setSelectedIndex(i);
                    break;
                }
            }
        } finally {
            cambiandoDepartamento = false;
        }
        cargarMunicipios((Departamento) cboDepartamento.getSelectedItem());
        for (int i = 0; i < cboMunicipio.getItemCount(); i++) {
            if (cboMunicipio.getItemAt(i).getMunicipioId().equals(municipio.getMunicipioId())) {
                cboMunicipio.setSelectedIndex(i);
                break;
            }
        }
    }

    private static boolean soloDigitos(String texto) {
        for (char c : texto.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private void mostrarError(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlFormulario = new javax.swing.JPanel();
        pnlDatos = new javax.swing.JPanel();
        lblNombres = new javax.swing.JLabel();
        txtNombres = new javax.swing.JTextField();
        lblApellidoPaterno = new javax.swing.JLabel();
        txtApellidoPaterno = new javax.swing.JTextField();
        lblApellidoMaterno = new javax.swing.JLabel();
        txtApellidoMaterno = new javax.swing.JTextField();
        lblFechaNacimiento = new javax.swing.JLabel();
        txtFechaNacimiento = new javax.swing.JFormattedTextField();
        lblTipoDocumento = new javax.swing.JLabel();
        cboTipoDocumento = new javax.swing.JComboBox<>();
        lblNumeroDocumento = new javax.swing.JLabel();
        txtNumeroDocumento = new javax.swing.JTextField();
        lblCorreo = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        lblSexo = new javax.swing.JLabel();
        cboSexo = new javax.swing.JComboBox<>();
        lblTelefonoFijo = new javax.swing.JLabel();
        txtTelefonoFijo = new javax.swing.JTextField();
        lblTelefonoCelular = new javax.swing.JLabel();
        txtTelefonoCelular = new javax.swing.JTextField();
        lblDireccion = new javax.swing.JLabel();
        txtDireccion = new javax.swing.JTextField();
        lblBuscarMunicipio = new javax.swing.JLabel();
        txtBuscarMunicipio = new javax.swing.JTextField();
        btnBuscarMunicipio = new javax.swing.JButton();
        lblDepartamento = new javax.swing.JLabel();
        cboDepartamento = new javax.swing.JComboBox<>();
        lblMunicipio = new javax.swing.JLabel();
        cboMunicipio = new javax.swing.JComboBox<>();
        pnlBotones = new javax.swing.JPanel();
        btnNuevo = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        pnlListado = new javax.swing.JPanel();
        pnlBusqueda = new javax.swing.JPanel();
        lblBuscar = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        scrPostulantes = new javax.swing.JScrollPane();
        tblPostulantes = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Gestión de postulantes");

        pnlFormulario.setLayout(new java.awt.BorderLayout());

        pnlDatos.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del postulante"));
        pnlDatos.setLayout(new java.awt.GridBagLayout());

        lblNombres.setText("Nombres:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblNombres, gridBagConstraints);

        txtNombres.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtNombres, gridBagConstraints);

        lblApellidoPaterno.setText("Apellido paterno:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblApellidoPaterno, gridBagConstraints);

        txtApellidoPaterno.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtApellidoPaterno, gridBagConstraints);

        lblApellidoMaterno.setText("Apellido materno:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblApellidoMaterno, gridBagConstraints);

        txtApellidoMaterno.setColumns(20);
        txtApellidoMaterno.setToolTipText("Opcional");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtApellidoMaterno, gridBagConstraints);

        lblFechaNacimiento.setText("Fecha de nacimiento:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblFechaNacimiento, gridBagConstraints);

        txtFechaNacimiento.setToolTipText("Formato dd/mm/aaaa, por ejemplo 15/03/2000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtFechaNacimiento, gridBagConstraints);

        lblTipoDocumento.setText("Tipo de documento:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblTipoDocumento, gridBagConstraints);

        cboTipoDocumento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CC - Cédula de ciudadanía", "TI - Tarjeta de identidad", "CE - Cédula de extranjería", "PA - Pasaporte", "PPT - Permiso por protección temporal" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(cboTipoDocumento, gridBagConstraints);

        lblNumeroDocumento.setText("Número de documento:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblNumeroDocumento, gridBagConstraints);

        txtNumeroDocumento.setColumns(20);
        txtNumeroDocumento.setToolTipText("Solo dígitos, sin puntos ni espacios (pasaporte: letras y números)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtNumeroDocumento, gridBagConstraints);

        lblCorreo.setText("Correo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblCorreo, gridBagConstraints);

        txtCorreo.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtCorreo, gridBagConstraints);

        lblSexo.setText("Sexo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblSexo, gridBagConstraints);

        cboSexo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Seleccione --", "F - Femenino", "M - Masculino" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(cboSexo, gridBagConstraints);

        lblTelefonoFijo.setText("Teléfono fijo:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblTelefonoFijo, gridBagConstraints);

        txtTelefonoFijo.setColumns(20);
        txtTelefonoFijo.setToolTipText("Indicativo y número, por ejemplo (608) 742 1234");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtTelefonoFijo, gridBagConstraints);

        lblTelefonoCelular.setText("Celular:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblTelefonoCelular, gridBagConstraints);

        txtTelefonoCelular.setColumns(20);
        txtTelefonoCelular.setToolTipText("10 dígitos, por ejemplo 310 123 4567");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtTelefonoCelular, gridBagConstraints);

        lblDireccion.setText("Dirección:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblDireccion, gridBagConstraints);

        txtDireccion.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtDireccion, gridBagConstraints);

        lblBuscarMunicipio.setText("Buscar municipio:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblBuscarMunicipio, gridBagConstraints);

        txtBuscarMunicipio.setColumns(20);
        txtBuscarMunicipio.setToolTipText("Código DIVIPOLA (por ejemplo 15001) o parte del nombre (por ejemplo tunja)");
        txtBuscarMunicipio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarMunicipioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(txtBuscarMunicipio, gridBagConstraints);

        btnBuscarMunicipio.setText("Buscar por código o nombre");
        btnBuscarMunicipio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarMunicipioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(btnBuscarMunicipio, gridBagConstraints);

        lblDepartamento.setText("Departamento:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblDepartamento, gridBagConstraints);

        cboDepartamento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboDepartamentoActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(cboDepartamento, gridBagConstraints);

        lblMunicipio.setText("Municipio:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(lblMunicipio, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 4, 6);
        pnlDatos.add(cboMunicipio, gridBagConstraints);

        pnlFormulario.add(pnlDatos, java.awt.BorderLayout.CENTER);

        pnlBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnNuevo.setText("Nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });
        pnlBotones.add(btnNuevo);

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        pnlBotones.add(btnGuardar);

        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });
        pnlBotones.add(btnEliminar);

        pnlFormulario.add(pnlBotones, java.awt.BorderLayout.SOUTH);

        getContentPane().add(pnlFormulario, java.awt.BorderLayout.NORTH);

        pnlListado.setBorder(javax.swing.BorderFactory.createTitledBorder("Postulantes registrados"));
        pnlListado.setLayout(new java.awt.BorderLayout());

        pnlBusqueda.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblBuscar.setText("Nombre o apellido:");
        pnlBusqueda.add(lblBuscar);

        txtBuscar.setColumns(30);
        txtBuscar.setToolTipText("Escriba parte del nombre o apellido y presione Enter. Vacío muestra todos.");
        txtBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarActionPerformed(evt);
            }
        });
        pnlBusqueda.add(txtBuscar);

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });
        pnlBusqueda.add(btnBuscar);

        lblTotal.setText("0 postulantes");
        pnlBusqueda.add(lblTotal);

        pnlListado.add(pnlBusqueda, java.awt.BorderLayout.NORTH);

        scrPostulantes.setPreferredSize(new java.awt.Dimension(900, 250));

        tblPostulantes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Documento", "Nombres", "Apellido paterno", "Apellido materno", "Correo", "Teléfono", "Municipio"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPostulantes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrPostulantes.setViewportView(tblPostulantes);

        pnlListado.add(scrPostulantes, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlListado, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarActionPerformed
        // Enter en la caja de búsqueda
        buscar();
    }//GEN-LAST:event_txtBuscarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void cboDepartamentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboDepartamentoActionPerformed
        // El usuario cambió de departamento: se muestran solo sus municipios
        if (cambiandoDepartamento) {
            return;
        }
        try {
            cargarMunicipios((Departamento) cboDepartamento.getSelectedItem());
        } catch (DBException e) {
            mostrarError(e);
        }
    }//GEN-LAST:event_cboDepartamentoActionPerformed

    private void txtBuscarMunicipioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarMunicipioActionPerformed
        // Enter en la caja de búsqueda de municipio
        buscarMunicipio();
    }//GEN-LAST:event_txtBuscarMunicipioActionPerformed

    private void btnBuscarMunicipioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarMunicipioActionPerformed
        buscarMunicipio();
    }//GEN-LAST:event_btnBuscarMunicipioActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        limpiar();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed

    /**
     * Permite abrir solo el formulario (Shift+F6). Normalmente lo abre AppPostulantes (F6).
     */
    public static void main(String args[]) {
        AppPostulantes.main(args);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnBuscarMunicipio;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JComboBox<com.co.java.core.bean.Departamento> cboDepartamento;
    private javax.swing.JComboBox<com.co.java.core.bean.Municipio> cboMunicipio;
    private javax.swing.JComboBox<String> cboSexo;
    private javax.swing.JComboBox<String> cboTipoDocumento;
    private javax.swing.JLabel lblApellidoMaterno;
    private javax.swing.JLabel lblApellidoPaterno;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblBuscarMunicipio;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblDepartamento;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblFechaNacimiento;
    private javax.swing.JLabel lblMunicipio;
    private javax.swing.JLabel lblNombres;
    private javax.swing.JLabel lblNumeroDocumento;
    private javax.swing.JLabel lblSexo;
    private javax.swing.JLabel lblTelefonoCelular;
    private javax.swing.JLabel lblTelefonoFijo;
    private javax.swing.JLabel lblTipoDocumento;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlBusqueda;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlFormulario;
    private javax.swing.JPanel pnlListado;
    private javax.swing.JScrollPane scrPostulantes;
    private javax.swing.JTable tblPostulantes;
    private javax.swing.JTextField txtApellidoMaterno;
    private javax.swing.JTextField txtApellidoPaterno;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtBuscarMunicipio;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JFormattedTextField txtFechaNacimiento;
    private javax.swing.JTextField txtNombres;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextField txtTelefonoCelular;
    private javax.swing.JTextField txtTelefonoFijo;
    // End of variables declaration//GEN-END:variables
}
