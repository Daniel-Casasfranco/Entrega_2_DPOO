package interfaz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import modelo.Deporte;
import modelo.Jugador;
import modelo.ReglaNegocioException;

/**
 * Tabla editable para escribir las estadisticas individuales de un partido de equipo.
 * Las columnas dependen del deporte (goles y tarjetas en futbol, puntos y rebotes en
 * baloncesto, aces y bloqueos en voleibol).
 */
public class TablaEstadisticas extends JScrollPane {

    private static final long serialVersionUID = 1L;

    private final List<Jugador> jugadores = new ArrayList<>();
    private String[] claves = new String[0];
    private DefaultTableModel modelo = new DefaultTableModel();
    private final JTable tabla = new JTable();

    public TablaEstadisticas() {
        tabla.setRowHeight(22);
        tabla.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setViewportView(tabla);
    }

    /** Claves que espera {@code Club} para cada deporte. */
    public static String[] claves(Deporte deporte) {
        switch (deporte) {
            case FUTBOL:
                return new String[] {"goles", "asistencias", "tarjetasAmarillas", "tarjetasRojas"};
            case BALONCESTO:
                return new String[] {"puntos", "rebotes"};
            case VOLEIBOL:
                return new String[] {"aces", "bloqueos"};
            default:
                return new String[0];
        }
    }

    private static String encabezado(String clave) {
        switch (clave) {
            case "tarjetasAmarillas": return "Amarillas";
            case "tarjetasRojas": return "Rojas";
            default: return Character.toUpperCase(clave.charAt(0)) + clave.substring(1);
        }
    }

    public void mostrar(Deporte deporte, List<Jugador> lista) {
        jugadores.clear();
        jugadores.addAll(lista);
        claves = deporte == null ? new String[0] : claves(deporte);

        String[] columnas = new String[claves.length + 1];
        columnas[0] = "Jugador";
        for (int i = 0; i < claves.length; i++) {
            columnas[i + 1] = encabezado(claves[i]);
        }
        modelo = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return columna > 0;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == 0 ? String.class : Integer.class;
            }
        };
        for (Jugador j : jugadores) {
            Object[] fila = new Object[columnas.length];
            fila[0] = j.getNombre() + " #" + j.getNumeroCamiseta() + (j.estaSuspendido() ? " (suspendido)" : "");
            for (int i = 1; i < fila.length; i++) {
                fila[i] = 0;
            }
            modelo.addRow(fila);
        }
        tabla.setModel(modelo);
    }

    /** Estadisticas escritas; solo incluye jugadores con algun valor distinto de cero. */
    public Map<Jugador, Map<String, Integer>> leer() {
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }
        Map<Jugador, Map<String, Integer>> resultado = new LinkedHashMap<>();
        for (int f = 0; f < jugadores.size(); f++) {
            Map<String, Integer> valores = new HashMap<>();
            boolean algunValor = false;
            for (int c = 0; c < claves.length; c++) {
                Object celda = modelo.getValueAt(f, c + 1);
                int valor = celda == null ? 0 : (Integer) celda;
                if (valor < 0) {
                    throw new ReglaNegocioException("Las estadisticas no pueden ser negativas");
                }
                valores.put(claves[c], valor);
                algunValor |= valor != 0;
            }
            if (algunValor) {
                resultado.put(jugadores.get(f), valores);
            }
        }
        return resultado;
    }
}
