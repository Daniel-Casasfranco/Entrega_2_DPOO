package interfaz;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import modelo.ReglaNegocioException;

/**
 * Utilidades de Swing que comparten todos los paneles: formularios, tablas,
 * combos con texto personalizado, lectura de campos y mensajes de error.
 */

	//clase muy importante porque es la que hace que toda el ui tenga 
	//visualmente el mismo aspecto ademas se puede editar facilmente

public final class UI {

    public static final Color COLOR_PRIMARIO = new Color(0x1E1E1E);
    public static final Color COLOR_FONDO = new Color(0x121212);
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.BOLD, 14);

    private UI() {
    }

    // ------------------------------------------------------------------
    // Construccion de componentes
    // ------------------------------------------------------------------

    public static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_TITULO);
        etiqueta.setForeground(COLOR_PRIMARIO);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return etiqueta;
    }

    public static JButton boton(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        return boton;
    }

    /** Panel con borde y titulo que agrupa un formulario o una tabla. */
    public static JPanel seccion(String titulo, JComponent contenido) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0xCED6E0)), titulo,
                        0, 0, FUENTE_SUBTITULO, COLOR_PRIMARIO),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Formulario de dos columnas. Recibe pares (texto de la etiqueta, componente); si el
     * ultimo elemento es un boton solo, lo pone abajo a la derecha.
     */
    public static JPanel formulario(Object... elementos) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.anchor = GridBagConstraints.WEST;
        int fila = 0;
        int i = 0;
        while (i < elementos.length) {
            if (elementos[i] instanceof String && i + 1 < elementos.length) {
                c.gridx = 0;
                c.gridy = fila;
                c.weightx = 0;
                c.fill = GridBagConstraints.NONE;
                panel.add(new JLabel((String) elementos[i]), c);
                c.gridx = 1;
                c.weightx = 1;
                c.fill = GridBagConstraints.HORIZONTAL;
                panel.add((Component) elementos[i + 1], c);
                i += 2;
            } else {
                c.gridx = 0;
                c.gridy = fila;
                c.gridwidth = 2;
                c.weightx = 1;
                c.fill = GridBagConstraints.NONE;
                c.anchor = GridBagConstraints.EAST;
                panel.add((Component) elementos[i], c);
                c.gridwidth = 1;
                c.anchor = GridBagConstraints.WEST;
                i++;
            }
            fila++;
        }
        // Empuja el contenido hacia arriba
        c.gridy = fila;
        c.weighty = 1;
        panel.add(new JLabel(), c);
        return panel;
    }

    /** Modelo de tabla que no permite editar las celdas. */
    public static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    public static JTable tabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.setAutoCreateRowSorter(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    /** Fila seleccionada en el modelo (no en la vista ordenada), o -1. */
    public static int filaSeleccionada(JTable tabla) {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? -1 : tabla.convertRowIndexToModel(fila);
    }

    public static JScrollPane scroll(Component componente) {
        return new JScrollPane(componente);
    }

    public static JTextArea areaTexto() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setMargin(new Insets(6, 6, 6, 6));
        return area;
    }

    public static JTextField campo(String valorInicial) {
        JTextField campo = new JTextField(valorInicial, 14);
        return campo;
    }

    /** Combo que muestra cada elemento con el texto que devuelve {@code nombre}. */
    public static <T> JComboBox<T> combo(List<T> elementos, Function<T, String> nombre) {
        JComboBox<T> combo = new JComboBox<>();
        for (T e : elementos) {
            combo.addItem(e);
        }
        combo.setRenderer(renderer(nombre));
        return combo;
    }

    public static <T> void recargar(JComboBox<T> combo, List<T> elementos) {
        Object anterior = combo.getSelectedItem();
        combo.removeAllItems();
        for (T e : elementos) {
            combo.addItem(e);
        }
        if (anterior != null && elementos.contains(anterior)) {
            combo.setSelectedItem(anterior);
        }
    }

    /** Lista de seleccion multiple. */
    public static <T> JList<T> listaMultiple(List<T> elementos, Function<T, String> nombre) {
        DefaultListModel<T> modelo = new DefaultListModel<>();
        for (T e : elementos) {
            modelo.addElement(e);
        }
        JList<T> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lista.setCellRenderer(renderer(nombre));
        lista.setVisibleRowCount(6);
        return lista;
    }

    public static <T> void recargar(JList<T> lista, List<T> elementos) {
        DefaultListModel<T> modelo = new DefaultListModel<>();
        for (T e : elementos) {
            modelo.addElement(e);
        }
        lista.setModel(modelo);
    }

    @SuppressWarnings("unchecked")
    private static <T> DefaultListCellRenderer renderer(Function<T, String> nombre) {
        return new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean foco) {
                String texto;
                try {
                    // Algunas funciones dan un texto para null (por ejemplo "Todas")
                    texto = nombre.apply((T) valor);
                } catch (NullPointerException e) {
                    texto = "";
                }
                return super.getListCellRendererComponent(lista, texto, indice, seleccionado, foco);
            }
        };
    }

    // ------------------------------------------------------------------
    // Lectura de campos
    // ------------------------------------------------------------------

    public static String texto(JTextField campo, String nombreCampo) {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) {
            throw new ReglaNegocioException("El campo '" + nombreCampo + "' es obligatorio");
        }
        return valor;
    }

    public static int entero(JTextField campo, String nombreCampo) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El campo '" + nombreCampo + "' debe ser un numero entero");
        }
    }

    public static double decimal(JTextField campo, String nombreCampo) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El campo '" + nombreCampo + "' debe ser un numero");
        }
    }

    /** Lee una fecha con formato AAAA-MM-DD. */
    public static LocalDate fecha(JTextField campo, String nombreCampo) {
        try {
            return LocalDate.parse(campo.getText().trim());
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("El campo '" + nombreCampo + "' debe tener el formato AAAA-MM-DD");
        }
    }

    /** Lee una hora con formato HH:MM. */
    public static LocalTime hora(JTextField campo, String nombreCampo) {
        try {
            return LocalTime.parse(campo.getText().trim());
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("El campo '" + nombreCampo + "' debe tener el formato HH:MM");
        }
    }

    public static <T> T seleccionado(JComboBox<T> combo, String nombreCampo) {
        @SuppressWarnings("unchecked")
        T valor = (T) combo.getSelectedItem();
        if (valor == null) {
            throw new ReglaNegocioException("Debe seleccionar " + nombreCampo);
        }
        return valor;
    }

    // ------------------------------------------------------------------
    // Ejecucion de acciones y mensajes
    // ------------------------------------------------------------------

    /**
     * Ejecuta una accion del modelo. Si una regla de negocio la rechaza, muestra el
     * motivo en un dialogo de error. Devuelve true si la accion se completo.
     */
    public static boolean ejecutar(Component padre, Runnable accion, String mensajeExito) {
        try {
            accion.run();
            if (mensajeExito != null) {
                JOptionPane.showMessageDialog(padre, mensajeExito, "Operacion exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException | IllegalStateException e) {
            error(padre, e.getMessage());
            return false;
        }
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "No se pudo completar la operacion",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String dinero(double valor) {
        return "$" + String.format("%,.0f", valor);
    }
}
