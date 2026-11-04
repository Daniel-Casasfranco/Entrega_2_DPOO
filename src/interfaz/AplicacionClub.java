package interfaz;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import consola.DatosBase;
import modelo.Club;
import persistencia.Persistencia;

/**
 * Punto de entrada de la interfaz grafica. Carga el club desde
 * {@code datos/club/club.dat}; si no existe, ofrece cargar datos de demostracion.
 */
public class AplicacionClub {

    public static final String CARPETA_DATOS = "datos/club";

    public static void main(String[] args) {
        String carpeta = args.length > 0 ? args[0] : CARPETA_DATOS;
        SwingUtilities.invokeLater(() -> iniciar(carpeta));
    }

    private static void iniciar(String carpeta) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si no se puede, se usa el aspecto por defecto de Swing
        }

        Persistencia persistencia = new Persistencia(carpeta);
        boolean existia = persistencia.existe();
        Club club = persistencia.cargar();

        if (!existia) {
            int opcion = JOptionPane.showConfirmDialog(null,
                    "No hay datos guardados en " + carpeta + ".\n"
                            + "¿Desea cargar datos de demostracion (equipos, partidos, ventas...)?\n\n"
                            + "Si elige No, el club empieza solo con los usuarios basicos.",
                    "Club Deportivo Seneca", JOptionPane.YES_NO_OPTION);
            // Mientras se cargan los datos no se escribe el archivo en cada operacion
            club.setPersistencia(null);
            if (opcion == JOptionPane.YES_OPTION) {
                DatosDemo.cargar(club);
            } else {
                DatosBase.cargar(club);
            }
            club.setPersistencia(persistencia);
            club.guardar();
        }

        new VentanaLogin(club).setVisible(true);
    }
}
