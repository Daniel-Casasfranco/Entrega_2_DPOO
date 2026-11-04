package interfaz;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.Asistencia;
import modelo.Club;
import modelo.Convocatoria;
import modelo.Equipo;
import modelo.Instalacion;
import modelo.Jugador;
import modelo.SesionEntrenamiento;
import modelo.Socio;
import modelo.Usuario;

/** Sesiones de entrenamiento: organizar y registrar asistencia y evaluacion (RF-26, RF-27). */
public class PanelSesiones extends PanelClub {

    private static final long serialVersionUID = 1L;

    private static final String SIN_REGISTRAR = "(sin registrar)";

    private final DefaultTableModel modeloSesiones =
            UI.modeloTabla("Fecha", "Hora", "Duracion", "Instalacion", "Equipo", "Convocados", "Estado");
    private final JTable tablaSesiones = UI.tabla(modeloSesiones);
    private final List<SesionEntrenamiento> misSesiones = new ArrayList<>();

    // Organizar sesion
    private final JComboBox<Instalacion> comboInstalacion =
            UI.combo(new ArrayList<Instalacion>(), i -> i.getNombre() + " (" + Listas.texto(i.getDisciplina()) + ")");
    private final JComboBox<Equipo> comboEquipo = UI.combo(new ArrayList<Equipo>(),
            e -> e == null ? "" : e.getNombre());
    private final JTextField campoFecha = UI.campo(LocalDate.now().plusDays(1).toString());
    private final JTextField campoHora = UI.campo("16:00");
    private final JTextField campoDuracion = UI.campo("90");
    private final JList<Socio> listaConvocados = UI.listaMultiple(new ArrayList<Socio>(), Listas::nombre);

    // Asistencia de la sesion seleccionada
    private final DefaultTableModel modeloAsistencia = new DefaultTableModel(
            new String[] {"Socio", "Asistencia", "Nota (1-5, 0 = sin nota)", "Comentario"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna > 0;
        }
    };
    private final JTable tablaAsistencia = new JTable(modeloAsistencia);
    private final List<Convocatoria> convocatorias = new ArrayList<>();

    public PanelSesiones(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Sesiones de entrenamiento"), BorderLayout.NORTH);

        tablaSesiones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarAsistencia();
            }
        });
        comboEquipo.addActionListener(e -> seleccionarEquipo());

        JComboBox<String> opciones = new JComboBox<>(new String[] {SIN_REGISTRAR, "ASISTIO", "LLEGO_TARDE", "FALTO"});
        tablaAsistencia.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(opciones));
        tablaAsistencia.setRowHeight(22);
        tablaAsistencia.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JPanel organizar = new JPanel(new BorderLayout(6, 6));
        organizar.add(UI.formulario("Instalacion:", comboInstalacion, "Equipo (opcional):", comboEquipo,
                "Fecha (AAAA-MM-DD):", campoFecha, "Hora (HH:MM):", campoHora, "Duracion (min):", campoDuracion),
                BorderLayout.NORTH);
        organizar.add(UI.seccion("Convocados (Ctrl o Shift para varios)", UI.scroll(listaConvocados)),
                BorderLayout.CENTER);
        organizar.add(UI.boton("Organizar sesion", e -> organizar()), BorderLayout.SOUTH);

        JPanel asistencia = new JPanel(new BorderLayout(6, 6));
        asistencia.add(UI.scroll(tablaAsistencia), BorderLayout.CENTER);
        asistencia.add(UI.boton("Guardar asistencia de la sesion seleccionada", e -> guardarAsistencia()),
                BorderLayout.SOUTH);

        JTabbedPane acciones = new JTabbedPane();
        acciones.addTab("Organizar sesion", organizar);
        acciones.addTab("Asistencia y evaluacion", asistencia);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UI.seccion("Mis sesiones", UI.scroll(tablaSesiones)), acciones);
        division.setResizeWeight(0.5);
        add(division, BorderLayout.CENTER);
    }

    private void seleccionarEquipo() {
        Equipo equipo = (Equipo) comboEquipo.getSelectedItem();
        if (equipo == null) {
            return;
        }
        Set<Integer> codigos = new HashSet<>();
        for (Jugador j : equipo.getJugadores()) {
            codigos.add(j.getCodigo());
        }
        listaConvocados.clearSelection();
        for (int i = 0; i < listaConvocados.getModel().getSize(); i++) {
            if (codigos.contains(listaConvocados.getModel().getElementAt(i).getCodigo())) {
                listaConvocados.addSelectionInterval(i, i);
            }
        }
    }

    private void organizar() {
        if (UI.ejecutar(this, () -> {
            SesionEntrenamiento s = club.organizarSesion(usuario.getCodigo(),
                    UI.seleccionado(comboInstalacion, "una instalacion"), UI.fecha(campoFecha, "Fecha"),
                    UI.hora(campoHora, "Hora"), UI.entero(campoDuracion, "Duracion"),
                    listaConvocados.getSelectedValuesList());
            Equipo equipo = (Equipo) comboEquipo.getSelectedItem();
            if (equipo != null) {
                s.setEquipo(equipo);
                club.guardar();
            }
        }, "Sesion organizada")) {
            actualizar();
        }
    }

    private SesionEntrenamiento sesionSeleccionada() {
        int fila = UI.filaSeleccionada(tablaSesiones);
        return fila < 0 ? null : misSesiones.get(fila);
    }

    private void mostrarAsistencia() {
        modeloAsistencia.setRowCount(0);
        convocatorias.clear();
        SesionEntrenamiento s = sesionSeleccionada();
        if (s == null) {
            return;
        }
        for (Convocatoria c : s.getConvocatorias()) {
            convocatorias.add(c);
            modeloAsistencia.addRow(new Object[] {Listas.nombre(c.getSocio()),
                c.getAsistencia() == null ? SIN_REGISTRAR : c.getAsistencia().name(), String.valueOf(c.getNota()),
                c.getComentario() == null ? "" : c.getComentario()});
        }
    }

    private void guardarAsistencia() {
        SesionEntrenamiento s = sesionSeleccionada();
        if (s == null) {
            UI.error(this, "Seleccione una sesion en la tabla");
            return;
        }
        if (tablaAsistencia.isEditing()) {
            tablaAsistencia.getCellEditor().stopCellEditing();
        }
        int guardadas = 0;
        for (int f = 0; f < convocatorias.size(); f++) {
            String valor = (String) modeloAsistencia.getValueAt(f, 1);
            if (SIN_REGISTRAR.equals(valor)) {
                continue;
            }
            Convocatoria c = convocatorias.get(f);
            int fila = f;
            boolean ok = UI.ejecutar(this, () -> {
                int nota;
                try {
                    nota = Integer.parseInt(String.valueOf(modeloAsistencia.getValueAt(fila, 2)).trim());
                } catch (NumberFormatException e) {
                    throw new modelo.ReglaNegocioException("La nota de " + Listas.nombre(c.getSocio())
                            + " debe ser un numero");
                }
                club.registrarAsistencia(usuario.getCodigo(), s, c.getSocio(), Asistencia.valueOf(valor), nota,
                        (String) modeloAsistencia.getValueAt(fila, 3));
            }, null);
            if (!ok) {
                break;
            }
            guardadas++;
        }
        if (guardadas > 0) {
            UI.info(this, "Se guardo la asistencia de " + guardadas + " socio(s)");
        }
        actualizar();
    }

    @Override
    public void actualizar() {
        int seleccion = tablaSesiones.getSelectedRow();
        UI.recargar(comboInstalacion, club.getInstalaciones());
        List<Equipo> equipos = new ArrayList<>();
        equipos.add(null);
        equipos.addAll(club.getEquipos());
        UI.recargar(comboEquipo, equipos);
        UI.recargar(listaConvocados, Listas.socios(club));

        misSesiones.clear();
        modeloSesiones.setRowCount(0);
        for (SesionEntrenamiento s : club.getSesiones()) {
            if (s.getEntrenador() == usuario) {
                misSesiones.add(s);
                modeloSesiones.addRow(new Object[] {s.getFecha(), s.getHora(), s.getDuracionMinutos() + " min",
                    s.getInstalacion().getNombre(), s.getEquipo() == null ? "" : s.getEquipo().getNombre(),
                    s.getConvocatorias().size(), s.haTerminado() ? "Terminada" : "Programada"});
            }
        }
        if (seleccion >= 0 && seleccion < modeloSesiones.getRowCount()) {
            tablaSesiones.setRowSelectionInterval(seleccion, seleccion);
        }
        mostrarAsistencia();
    }
}
