package interfaz;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.Club;
import modelo.Equipo;
import modelo.Jugador;
import modelo.Partido;
import modelo.PartidoInterno;
import modelo.Usuario;

/** Registro de partidos internos entre equipos del club con sus estadisticas (RF-02, RF-22). */
public class PanelPartidosInternos extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloPartidos =
            UI.modeloTabla("Fecha", "Deporte", "Local", "Marcador", "Visitante", "Estadisticas");

    private final JComboBox<Equipo> comboLocal = UI.combo(new ArrayList<Equipo>(), Equipo::getNombre);
    private final JComboBox<Equipo> comboVisitante = UI.combo(new ArrayList<Equipo>(), Equipo::getNombre);
    private final JTextField campoFecha = UI.campo(LocalDate.now().toString());
    private final JTextField campoLocal = UI.campo("0");
    private final JTextField campoVisitante = UI.campo("0");
    private final TablaEstadisticas estadisticas = new TablaEstadisticas();

    public PanelPartidosInternos(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Partidos internos"), BorderLayout.NORTH);

        comboLocal.addActionListener(e -> mostrarJugadores());
        comboVisitante.addActionListener(e -> mostrarJugadores());

        JPanel formulario = new JPanel(new BorderLayout(6, 6));
        formulario.add(UI.formulario("Equipo local:", comboLocal, "Equipo visitante:", comboVisitante,
                "Fecha (AAAA-MM-DD):", campoFecha, "Marcador local:", campoLocal,
                "Marcador visitante:", campoVisitante), BorderLayout.NORTH);
        formulario.add(UI.seccion("Estadisticas individuales (deje en 0 si no aplica)", estadisticas),
                BorderLayout.CENTER);
        formulario.add(UI.boton("Registrar partido", e -> registrar()), BorderLayout.SOUTH);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UI.seccion("Partidos internos registrados", UI.scroll(UI.tabla(modeloPartidos))),
                UI.seccion("Nuevo partido", formulario));
        division.setResizeWeight(0.5);
        add(division, BorderLayout.CENTER);
    }

    private void mostrarJugadores() {
        Equipo local = (Equipo) comboLocal.getSelectedItem();
        Equipo visitante = (Equipo) comboVisitante.getSelectedItem();
        List<Jugador> jugadores = new ArrayList<>();
        if (local != null) {
            jugadores.addAll(local.getJugadores());
        }
        if (visitante != null && visitante != local) {
            jugadores.addAll(visitante.getJugadores());
        }
        estadisticas.mostrar(local == null ? null : local.getDeporte(), jugadores);
    }

    private void registrar() {
        if (UI.ejecutar(this, () -> club.registrarPartidoInterno(usuario.getCodigo(),
                UI.seleccionado(comboLocal, "el equipo local"), UI.seleccionado(comboVisitante, "el visitante"),
                UI.fecha(campoFecha, "Fecha"), UI.entero(campoLocal, "Marcador local"),
                UI.entero(campoVisitante, "Marcador visitante"), estadisticas.leer()),
                "Partido registrado. Las tarjetas generan sanciones automaticamente.")) {
            actualizar();
        }
    }

    @Override
    public void actualizar() {
        UI.recargar(comboLocal, club.getEquipos());
        UI.recargar(comboVisitante, club.getEquipos());
        if (comboVisitante.getItemCount() > 1 && comboVisitante.getSelectedIndex() == comboLocal.getSelectedIndex()) {
            comboVisitante.setSelectedIndex(1);
        }
        modeloPartidos.setRowCount(0);
        for (Partido p : club.getPartidos()) {
            if (p instanceof PartidoInterno) {
                PartidoInterno pi = (PartidoInterno) p;
                modeloPartidos.addRow(new Object[] {pi.getFecha(), Listas.texto(pi.getDeporte()),
                    pi.getEquipo1().getNombre(), pi.getMarcadorEquipo1() + " - " + pi.getMarcadorEquipo2(),
                    pi.getEquipo2().getNombre(), pi.getEstadisticas().size()});
            }
        }
        mostrarJugadores();
    }
}
