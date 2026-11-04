package interfaz;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.ASCUN;
import modelo.Administrador;
import modelo.Categoria;
import modelo.Club;
import modelo.Competencia;
import modelo.Deporte;
import modelo.Equipo;
import modelo.InscripcionEquipo;
import modelo.Jugador;
import modelo.JugadorNomina;
import modelo.PartidoCompeticion;
import modelo.TorneoDeLosCerros;
import modelo.Usuario;

/**
 * Competencias externas. Todos ven la tabla de posiciones y los resultados; el
 * administrador crea competencias y el entrenador inscribe equipos y registra partidos.
 */
public class PanelCompetencias extends PanelClub {

    private static final long serialVersionUID = 1L;

    private static final String TORNEO = "Torneo de los Cerros";
    private static final String ASCUN_TEXTO = "ASCUN";

    private final DefaultTableModel modeloCompetencias =
            UI.modeloTabla("Competencia", "Tipo", "Deporte", "Categoria", "Temporada", "Equipos", "Partidos");
    private final JTable tablaCompetencias = UI.tabla(modeloCompetencias);
    private final DefaultTableModel modeloPosiciones = UI.modeloTabla("#", "Equipo", "G", "E", "P", "Puntos");
    private final DefaultTableModel modeloResultados =
            UI.modeloTabla("Fecha", "Equipo", "Rival", "Marcador", "Escenario");

    // Administrador: crear competencia
    private final JComboBox<String> comboTipo = UI.combo(Arrays.asList(TORNEO, ASCUN_TEXTO), s -> s);
    private final JTextField campoNombre = UI.campo("");
    private final JTextField campoOrganizador = UI.campo("");
    private final JTextField campoTemporada = UI.campo(String.valueOf(LocalDate.now().getYear()));
    private final JComboBox<Deporte> comboDeporte = UI.combo(Arrays.asList(Listas.DEPORTES_CONJUNTO), Listas::texto);
    private final JComboBox<Categoria> comboCategoria = UI.combo(Arrays.asList(Categoria.values()), Listas::texto);
    private final JTextField campoNomina = UI.campo("7");

    // Entrenador: inscribir equipo y registrar partidos
    private final JComboBox<Equipo> comboEquipo = UI.combo(new ArrayList<Equipo>(), Equipo::getNombre);
    private final JList<Jugador> listaNomina = UI.listaMultiple(new ArrayList<Jugador>(),
            j -> j.getNombre() + " #" + j.getNumeroCamiseta() + (j.estaSuspendido() ? " (suspendido)" : ""));
    private final JComboBox<InscripcionEquipo> comboInscripcion =
            UI.combo(new ArrayList<InscripcionEquipo>(), i -> i.getEquipo().getNombre());
    private final JTextField campoRival = UI.campo("");
    private final JTextField campoEscenario = UI.campo("");
    private final JTextField campoFecha = UI.campo(LocalDate.now().toString());
    private final JTextField campoPropio = UI.campo("0");
    private final JTextField campoContrario = UI.campo("0");
    private final TablaEstadisticas estadisticas = new TablaEstadisticas();

    public PanelCompetencias(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Competencias"), BorderLayout.NORTH);

        tablaCompetencias.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });

        JPanel izquierda = new JPanel(new GridLayout(3, 1, 0, 8));
        izquierda.add(UI.seccion("Competencias registradas", UI.scroll(tablaCompetencias)));
        izquierda.add(UI.seccion("Tabla de posiciones", UI.scroll(UI.tabla(modeloPosiciones))));
        izquierda.add(UI.seccion("Resultados", UI.scroll(UI.tabla(modeloResultados))));

        JPanel derecha;
        if (usuario instanceof Administrador) {
            derecha = UI.seccion("Crear competencia", UI.formulario(
                    "Tipo:", comboTipo, "Nombre:", campoNombre, "Organizador:", campoOrganizador,
                    "Temporada:", campoTemporada, "Deporte:", comboDeporte, "Categoria:", comboCategoria,
                    "Nomina minima:", campoNomina, UI.boton("Crear competencia", e -> crearCompetencia())));
        } else {
            comboEquipo.addActionListener(e -> mostrarJugadoresEquipo());
            comboInscripcion.addActionListener(e -> mostrarNomina());

            JPanel inscribir = new JPanel(new BorderLayout(6, 6));
            inscribir.add(UI.formulario("Equipo:", comboEquipo), BorderLayout.NORTH);
            inscribir.add(UI.seccion("Nomina (seleccione con Ctrl o Shift)", UI.scroll(listaNomina)),
                    BorderLayout.CENTER);
            inscribir.add(UI.boton("Inscribir en la competencia seleccionada", e -> inscribirEquipo()),
                    BorderLayout.SOUTH);

            JPanel partido = new JPanel(new BorderLayout(6, 6));
            partido.add(UI.formulario("Equipo inscrito:", comboInscripcion, "Institucion rival:", campoRival,
                    "Escenario:", campoEscenario, "Fecha (AAAA-MM-DD):", campoFecha,
                    "Marcador propio:", campoPropio, "Marcador rival:", campoContrario), BorderLayout.NORTH);
            partido.add(UI.seccion("Estadisticas individuales", estadisticas), BorderLayout.CENTER);
            partido.add(UI.boton("Registrar partido", e -> registrarPartido()), BorderLayout.SOUTH);

            JTabbedPane acciones = new JTabbedPane();
            acciones.addTab("Inscribir equipo", inscribir);
            acciones.addTab("Registrar partido", partido);
            derecha = new JPanel(new BorderLayout());
            derecha.add(acciones);
        }

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierda, derecha);
        division.setResizeWeight(0.6);
        add(division, BorderLayout.CENTER);
    }

    private Competencia seleccionada() {
        int fila = UI.filaSeleccionada(tablaCompetencias);
        return fila < 0 ? null : club.getCompetencias().get(fila);
    }

    private void crearCompetencia() {
        if (UI.ejecutar(this, () -> {
            String nombre = UI.texto(campoNombre, "Nombre");
            String organizador = UI.texto(campoOrganizador, "Organizador");
            String temporada = UI.texto(campoTemporada, "Temporada");
            Deporte deporte = UI.seleccionado(comboDeporte, "un deporte");
            Categoria categoria = UI.seleccionado(comboCategoria, "una categoria");
            int nomina = UI.entero(campoNomina, "Nomina minima");
            Competencia c = TORNEO.equals(comboTipo.getSelectedItem())
                    ? new TorneoDeLosCerros(nombre, organizador, temporada, deporte, categoria, nomina)
                    : new ASCUN(nombre, organizador, temporada, deporte, categoria, nomina);
            club.agregarCompetencia(c);
        }, "Competencia creada")) {
            campoNombre.setText("");
            actualizar();
        }
    }

    private void inscribirEquipo() {
        Competencia competencia = seleccionada();
        if (competencia == null) {
            UI.error(this, "Seleccione una competencia en la tabla");
            return;
        }
        if (UI.ejecutar(this, () -> club.inscribirEquipo(usuario.getCodigo(),
                UI.seleccionado(comboEquipo, "un equipo"), competencia, listaNomina.getSelectedValuesList()),
                "Equipo inscrito en " + competencia.getNombre())) {
            actualizar();
        }
    }

    private void registrarPartido() {
        if (seleccionada() == null) {
            UI.error(this, "Seleccione una competencia en la tabla");
            return;
        }
        if (UI.ejecutar(this, () -> club.registrarPartidoCompeticion(usuario.getCodigo(),
                UI.seleccionado(comboInscripcion, "un equipo inscrito"), UI.texto(campoRival, "Institucion rival"),
                UI.texto(campoEscenario, "Escenario"), UI.fecha(campoFecha, "Fecha"),
                UI.entero(campoPropio, "Marcador propio"), UI.entero(campoContrario, "Marcador rival"),
                estadisticas.leer()), "Partido registrado")) {
            actualizar();
        }
    }

    private void mostrarDetalle() {
        modeloPosiciones.setRowCount(0);
        modeloResultados.setRowCount(0);
        Competencia c = seleccionada();
        if (c == null) {
            return;
        }
        int puesto = 1;
        for (InscripcionEquipo i : club.consultarTablaPosiciones(c)) {
            modeloPosiciones.addRow(new Object[] {puesto++, i.getEquipo().getNombre(), i.getGanados(),
                i.getEmpatados(), i.getPerdidos(), i.getPuntos()});
        }
        for (PartidoCompeticion p : club.consultarResultados(c)) {
            modeloResultados.addRow(new Object[] {p.getFecha(), p.getEquipo1().getNombre(),
                p.getInstitucionRival(), p.getMarcadorEquipo1() + " - " + p.getMarcadorEquipo2(), p.getEscenario()});
        }

        if (!(usuario instanceof Administrador)) {
            List<Equipo> compatibles = new ArrayList<>();
            for (Equipo e : club.getEquipos()) {
                if (e.getDeporte() == c.getDeporte() && e.getCategoria() == c.getCategoria()
                        && c.buscarInscripcion(e) == null) {
                    compatibles.add(e);
                }
            }
            UI.recargar(comboEquipo, compatibles);
            UI.recargar(comboInscripcion, c.getInscripciones());
            mostrarJugadoresEquipo();
            mostrarNomina();
        }
    }

    private void mostrarJugadoresEquipo() {
        Equipo e = (Equipo) comboEquipo.getSelectedItem();
        UI.recargar(listaNomina, e == null ? new ArrayList<Jugador>() : e.getJugadores());
        if (e != null) {
            listaNomina.setSelectionInterval(0, e.getJugadores().size() - 1);
        }
    }

    private void mostrarNomina() {
        InscripcionEquipo i = (InscripcionEquipo) comboInscripcion.getSelectedItem();
        List<Jugador> nomina = new ArrayList<>();
        if (i != null) {
            for (JugadorNomina jn : i.getNomina()) {
                nomina.add(jn.getJugador());
            }
        }
        estadisticas.mostrar(i == null ? null : i.getEquipo().getDeporte(), nomina);
    }

    @Override
    public void actualizar() {
        int seleccion = tablaCompetencias.getSelectedRow();
        modeloCompetencias.setRowCount(0);
        for (Competencia c : club.getCompetencias()) {
            modeloCompetencias.addRow(new Object[] {c.getNombre(), c instanceof ASCUN ? ASCUN_TEXTO : TORNEO,
                Listas.texto(c.getDeporte()), Listas.texto(c.getCategoria()), c.getTemporada(),
                c.getInscripciones().size(), c.getPartidos().size()});
        }
        if (seleccion >= 0 && seleccion < modeloCompetencias.getRowCount()) {
            tablaCompetencias.setRowSelectionInterval(seleccion, seleccion);
        } else if (modeloCompetencias.getRowCount() > 0) {
            tablaCompetencias.setRowSelectionInterval(0, 0);
        }
        mostrarDetalle();
    }
}
