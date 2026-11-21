package com.co.java.core.app;

/**
 * Punto de entrada de la aplicación (F6): aplica el estilo Nimbus y abre FrmPostulantes.
 * La prueba en consola de la persistencia y las validaciones está en PruebaConsola.
 */
public class AppPostulantes {

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AppPostulantes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        // Swing se crea y se usa en su propio hilo (Event Dispatch Thread)
        java.awt.EventQueue.invokeLater(() -> new FrmPostulantes().setVisible(true));
    }
}
