package graficas;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import interfaz.Listas;
import interfaz.PanelClub;
import interfaz.UI;
import modelo.Club;
import modelo.Competencia;
import modelo.Deporte;
import modelo.Equipo;
import modelo.Socio;
import modelo.Usuario;

/**
 * Pestana de visualizaciones. Tiene cuatro graficas, cada una con su barra de filtros:
 * pastel de resultados, barras comparativas, lineas de evolucion y el mapa de calor de
 * ocupacion de instalaciones (grafica complementaria).
 */
public class PanelGraficas extends PanelClub {

    private static final long serialVersionUID = 1L;

    private static final String MODO_EQUIPO = "Equipo";
    private static final String MODO_INDIVIDUAL = "Jugador de disciplina individual";
    private static final String MODO_ESTADISTICA = "Estadistica de jugadores de un equipo";
    private static final String MODO_INGRESOS = "Ingresos por rubro (5 dias, sin impuestos)";
    private static final String MODO_RANKING = "Ranking de disciplina individual";
    private static final String MODO_ASISTENCIA = "Asistencia a entrenamientos";

    private final GraficaPastel pastel = new GraficaPastel("Distribucion de resultados");
    private final GraficaBarras barras = new GraficaBarras("Comparacion");
    private final GraficaLineas lineas = new GraficaLineas("Evolucion");
    private final GraficaMapaCalor mapa = new GraficaMapaCalor("Ocupacion de instalaciones por dia y hora");

    // Filtros del pastel
    private final JComboBox<String> pastelModo = UI.combo(Arrays.asList(MODO_EQUIPO, MODO_INDIVIDUAL), s -> s);
    private final JComboBox<Equipo> pastelEquipo = comboEquipos();
    private final JComboBox<String> pastelTipo = comboTipos();
    private final JComboBox<Competencia> pastelCompetencia = comboCompetencias();
    private final JComboBox<Deporte> pastelDisciplina = comboDisciplinas(false);
    private final JComboBox<Socio> pastelSocio = UI.combo(new ArrayList<Socio>(), Listas::nombre);
    private final CardLayout pastelTarjetas = new CardLayout();
    private final JPanel pastelFiltros = new JPanel(pastelTarjetas);

    // Filtros de barras
    private final JComboBox<String> barrasModo = UI.combo(Arrays.asList(MODO_ESTADISTICA, MODO_INGRESOS), s -> s);
    private final JComboBox<Equipo> barrasEquipo = comboEquipos();
    private final JComboBox<String> barrasEstadistica = UI.combo(new ArrayList<String>(), s -> s);
    private final JComboBox<String> barrasTipo = comboTipos();
    private final JComboBox<Competencia> barrasCompetencia = comboCompetencias();
    private final JTextField barrasInicio = new JTextField(LocalDate.now().minusDays(4).toString(), 9);
    private final CardLayout barrasTarjetas = new CardLayout();
    private final JPanel barrasFiltros = new JPanel(barrasTarjetas);

    // Filtros de lineas
    private final JComboBox<String> lineasModo = UI.combo(Arrays.asList(MODO_RANKING, MODO_ASISTENCIA), s -> s);
    private final JComboBox<Deporte> lineasDisciplina = comboDisciplinas(false);
    private final JComboBox<Socio> lineasSocio = UI.combo(new ArrayList<Socio>(),
            s -> s == null ? "Todos los inscritos" : Listas.nombre(s));
    private final JComboBox<Equipo> lineasEquipo = UI.combo(new ArrayList<Equipo>(),
            e -> e == null ? "Todos los socios" : e.getNombre());
    private final JSpinner lineasSemanas = new JSpinner(new SpinnerNumberModel(8, 2, 52, 1));
    private final CardLayout lineasTarjetas = new CardLayout();
    private final JPanel lineasFiltros = new JPanel(lineasTarjetas);

    // Filtros del mapa de calor
    private final JComboBox<Deporte> mapaDisciplina = comboDisciplinas(true);
    private final JTextField mapaDesde = new JTextField(LocalDate.now().minusWeeks(8).toString(), 9);
    private final JTextField mapaHasta = new JTextField(LocalDate.now().plusWeeks(2).toString(), 9);

    /** Evita redibujar mientras se recargan los combos. */
    private boolean recargando;

    public PanelGraficas(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Graficas"), BorderLayout.NORTH);

        pastel.setColor(DatosGraficas.VICTORIAS, new Color(0x10AC84));
        pastel.setColor(DatosGraficas.EMPATES, new Color(0xFECA57));
        pastel.setColor(DatosGraficas.DERROTAS, new Color(0xEE5253));
        lineas.setEjeDesdeCero(false);
        mapa.setUnidad("h");

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Resultados (pastel)", construirPastel());
        pestanas.addTab("Comparaciones (barras)", construirBarras());
        pestanas.addTab("Evolucion (lineas)", construirLineas());
        pestanas.addTab("Ocupacion (mapa de calor)", construirMapa());
        add(pestanas, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------
    // Construccion de cada pestana
    // ------------------------------------------------------------------

    private JPanel construirPastel() {
        pastelFiltros.add(fila("Equipo:", pastelEquipo, "Partidos:", pastelTipo, "Competencia:", pastelCompetencia),
                MODO_EQUIPO);
        pastelFiltros.add(fila("Disciplina:", pastelDisciplina, "Socio:", pastelSocio), MODO_INDIVIDUAL);
        escuchar(this::dibujarPastel, pastelModo, pastelEquipo, pastelTipo, pastelCompetencia, pastelSocio);
        pastelDisciplina.addActionListener(e -> {
            recargarSociosPastel();
            dibujarPastel();
        });
        pastelEquipo.addActionListener(e -> recargarCompetencias(pastelEquipo, pastelCompetencia, this::dibujarPastel));
        return envolver(fila("Ver resultados de:", pastelModo), pastelFiltros, pastel);
    }

    private JPanel construirBarras() {
        JPanel ingresos = fila("Primer dia (AAAA-MM-DD):", barrasInicio,
                UI.boton("Actualizar", e -> dibujarBarras()));
        barrasFiltros.add(fila("Equipo:", barrasEquipo, "Estadistica:", barrasEstadistica, "Partidos:", barrasTipo,
                "Competencia:", barrasCompetencia), MODO_ESTADISTICA);
        barrasFiltros.add(ingresos, MODO_INGRESOS);
        escuchar(this::dibujarBarras, barrasModo, barrasEstadistica, barrasTipo, barrasCompetencia);
        barrasEquipo.addActionListener(e -> {
            recargarEstadisticas();
            recargarCompetencias(barrasEquipo, barrasCompetencia, this::dibujarBarras);
        });
        barrasInicio.addActionListener(e -> dibujarBarras());
        return envolver(fila("Comparar:", barrasModo), barrasFiltros, barras);
    }

    private JPanel construirLineas() {
        lineasFiltros.add(fila("Disciplina:", lineasDisciplina, "Socio:", lineasSocio), MODO_RANKING);
        lineasFiltros.add(fila("Equipo:", lineasEquipo, "Semanas:", lineasSemanas), MODO_ASISTENCIA);
        escuchar(this::dibujarLineas, lineasModo, lineasSocio, lineasEquipo);
        lineasDisciplina.addActionListener(e -> {
            recargarSociosLineas();
            dibujarLineas();
        });
        lineasSemanas.addChangeListener(e -> dibujarLineas());
        return envolver(fila("Ver:", lineasModo), lineasFiltros, lineas);
    }

    private JPanel construirMapa() {
        escuchar(this::dibujarMapa, mapaDisciplina);
        mapaDesde.addActionListener(e -> dibujarMapa());
        mapaHasta.addActionListener(e -> dibujarMapa());
        JLabel explicacion = new JLabel("<html>Horas ocupadas (reservas + sesiones de entrenamiento) por dia de la "
                + "semana y franja horaria. Sirve para decidir cuando abrir mas cupos o programar mantenimiento.</html>");
        explicacion.setBorder(BorderFactory.createEmptyBorder(0, 6, 4, 6));
        JPanel filtros = fila("Instalaciones:", mapaDisciplina, "Desde:", mapaDesde, "Hasta:", mapaHasta,
                UI.boton("Actualizar", e -> dibujarMapa()));
        return envolver(filtros, explicacion, mapa);
    }

    // ------------------------------------------------------------------
    // Dibujo: pide los datos a DatosGraficas y se los pasa a la grafica
    // ------------------------------------------------------------------

    private void dibujarPastel() {
        if (recargando) {
            return;
        }
        String modo = (String) pastelModo.getSelectedItem();
        pastelTarjetas.show(pastelFiltros, modo);
        if (MODO_EQUIPO.equals(modo)) {
            Equipo equipo = (Equipo) pastelEquipo.getSelectedItem();
            pastel.setTitulo("Resultados de " + (equipo == null ? "-" : equipo.getNombre()));
            pastel.setDatos(equipo == null ? new java.util.HashMap<String, Double>()
                    : DatosGraficas.resultadosEquipo(club, equipo, (String) pastelTipo.getSelectedItem(),
                            (Competencia) pastelCompetencia.getSelectedItem()));
        } else {
            Socio socio = (Socio) pastelSocio.getSelectedItem();
            Deporte d = (Deporte) pastelDisciplina.getSelectedItem();
            pastel.setTitulo("Practicas de " + (socio == null ? "-" : Listas.nombre(socio)) + " en " + Listas.texto(d));
            Map<String, Double> datos = socio == null ? new java.util.HashMap<String, Double>()
                    : DatosGraficas.resultadosIndividual(club, socio, d);
            datos.remove(DatosGraficas.EMPATES); // En practicas siempre hay un ganador
            pastel.setDatos(datos);
        }
    }

    private void dibujarBarras() {
        if (recargando) {
            return;
        }
        String modo = (String) barrasModo.getSelectedItem();
        barrasTarjetas.show(barrasFiltros, modo);
        if (MODO_ESTADISTICA.equals(modo)) {
            Equipo equipo = (Equipo) barrasEquipo.getSelectedItem();
            String estadistica = (String) barrasEstadistica.getSelectedItem();
            barras.setTitulo(estadistica == null ? "Estadisticas" : estadistica + " por jugador - "
                    + (equipo == null ? "" : equipo.getNombre()));
            barras.setEtiquetaEjeY(estadistica == null ? "" : estadistica);
            barras.setDatos(estadistica == null ? "" : estadistica, equipo == null || estadistica == null
                    ? new java.util.LinkedHashMap<String, Double>()
                    : DatosGraficas.estadisticaPorJugador(club, equipo, estadistica,
                            (String) barrasTipo.getSelectedItem(), (Competencia) barrasCompetencia.getSelectedItem()));
        } else {
            LocalDate inicio;
            try {
                inicio = LocalDate.parse(barrasInicio.getText().trim());
            } catch (RuntimeException e) {
                UI.error(this, "La fecha debe tener el formato AAAA-MM-DD");
                return;
            }
            DatosGraficas.Series s = DatosGraficas.ingresosPorDia(club, inicio, 5);
            barras.setTitulo("Ingresos sin impuestos del " + inicio + " al " + inicio.plusDays(4));
            barras.setEtiquetaEjeY("Pesos");
            barras.setDatos(s.getEtiquetas(), s.getSeries());
        }
    }

    private void dibujarLineas() {
        if (recargando) {
            return;
        }
        String modo = (String) lineasModo.getSelectedItem();
        lineasTarjetas.show(lineasFiltros, modo);
        if (MODO_RANKING.equals(modo)) {
            Deporte d = (Deporte) lineasDisciplina.getSelectedItem();
            Socio socio = (Socio) lineasSocio.getSelectedItem();
            List<Socio> socios = socio != null ? Arrays.asList(socio)
                    : d == null ? new ArrayList<Socio>() : DatosGraficas.sociosDeDisciplina(club, d);
            DatosGraficas.Series s = DatosGraficas.evolucionRanking(club, d, socios);
            lineas.setTitulo("Evolucion del ranking en " + Listas.texto(d));
            lineas.setEjeDesdeCero(false);
            lineas.setDatos(s.getEtiquetas(), s.getSeries());
        } else {
            Equipo equipo = (Equipo) lineasEquipo.getSelectedItem();
            int semanas = (Integer) lineasSemanas.getValue();
            DatosGraficas.Series s = DatosGraficas.asistenciaSemanal(club, equipo, LocalDate.now(), semanas);
            lineas.setTitulo("Asistencia a entrenamientos por semana - "
                    + (equipo == null ? "todos los socios" : equipo.getNombre()));
            lineas.setEjeDesdeCero(true);
            lineas.setDatos(s.getEtiquetas(), s.getSeries());
        }
    }

    private void dibujarMapa() {
        if (recargando) {
            return;
        }
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(mapaDesde.getText().trim());
            hasta = LocalDate.parse(mapaHasta.getText().trim());
        } catch (RuntimeException e) {
            UI.error(this, "Las fechas deben tener el formato AAAA-MM-DD");
            return;
        }
        Deporte d = (Deporte) mapaDisciplina.getSelectedItem();
        mapa.setTitulo("Ocupacion de instalaciones" + (d == null ? "" : " de " + Listas.texto(d))
                + " (" + desde + " a " + hasta + ")");
        mapa.setDatos(Arrays.asList(DatosGraficas.DIAS), DatosGraficas.horasDelDia(),
                DatosGraficas.ocupacionInstalaciones(club, d, desde, hasta));
    }

    // ------------------------------------------------------------------
    // Recarga de combos dependientes
    // ------------------------------------------------------------------

    private void recargarCompetencias(JComboBox<Equipo> equipos, JComboBox<Competencia> competencias, Runnable luego) {
        Equipo equipo = (Equipo) equipos.getSelectedItem();
        List<Competencia> lista = new ArrayList<>();
        lista.add(null);
        if (equipo != null) {
            for (Competencia c : club.getCompetencias()) {
                if (c.buscarInscripcion(equipo) != null) {
                    lista.add(c);
                }
            }
        }
        boolean antes = recargando;
        recargando = true;
        UI.recargar(competencias, lista);
        recargando = antes;
        luego.run();
    }

    private void recargarEstadisticas() {
        Equipo equipo = (Equipo) barrasEquipo.getSelectedItem();
        boolean antes = recargando;
        recargando = true;
        UI.recargar(barrasEstadistica, equipo == null ? new ArrayList<String>()
                : Arrays.asList(DatosGraficas.estadisticasDe(equipo.getDeporte())));
        recargando = antes;
    }

    private void recargarSociosPastel() {
        Deporte d = (Deporte) pastelDisciplina.getSelectedItem();
        boolean antes = recargando;
        recargando = true;
        List<Socio> socios = d == null ? new ArrayList<Socio>() : DatosGraficas.sociosDeDisciplina(club, d);
        UI.recargar(pastelSocio, socios);
        if (usuario instanceof Socio && socios.contains(usuario)) {
            pastelSocio.setSelectedItem(usuario);
        }
        recargando = antes;
    }

    private void recargarSociosLineas() {
        Deporte d = (Deporte) lineasDisciplina.getSelectedItem();
        List<Socio> socios = new ArrayList<>();
        socios.add(null);
        if (d != null) {
            socios.addAll(DatosGraficas.sociosDeDisciplina(club, d));
        }
        boolean antes = recargando;
        recargando = true;
        UI.recargar(lineasSocio, socios);
        recargando = antes;
    }

    @Override
    public void actualizar() {
        recargando = true;
        UI.recargar(pastelEquipo, club.getEquipos());
        UI.recargar(barrasEquipo, club.getEquipos());
        List<Equipo> equipos = new ArrayList<>();
        equipos.add(null);
        equipos.addAll(club.getEquipos());
        UI.recargar(lineasEquipo, equipos);
        recargando = false;

        recargarCompetencias(pastelEquipo, pastelCompetencia, () -> { });
        recargarCompetencias(barrasEquipo, barrasCompetencia, () -> { });
        recargarEstadisticas();
        recargarSociosPastel();
        recargarSociosLineas();

        dibujarPastel();
        dibujarBarras();
        dibujarLineas();
        dibujarMapa();
    }

    // ------------------------------------------------------------------
    // Utilidades de construccion
    // ------------------------------------------------------------------

    private static JComboBox<Equipo> comboEquipos() {
        return UI.combo(new ArrayList<Equipo>(), e -> e.getNombre() + " (" + Listas.texto(e.getDeporte()) + ")");
    }

    private static JComboBox<String> comboTipos() {
        return UI.combo(Arrays.asList(DatosGraficas.TIPO_TODOS, DatosGraficas.TIPO_INTERNOS,
                DatosGraficas.TIPO_COMPETENCIA), s -> s);
    }

    private static JComboBox<Competencia> comboCompetencias() {
        return UI.combo(new ArrayList<Competencia>(), c -> c == null ? "Todas" : c.getNombre());
    }

    private static JComboBox<Deporte> comboDisciplinas(boolean conTodas) {
        List<Deporte> lista = new ArrayList<>();
        if (conTodas) {
            lista.add(null);
            lista.addAll(Arrays.asList(Deporte.values()));
        } else {
            lista.addAll(Arrays.asList(Listas.DISCIPLINAS_INDIVIDUALES));
        }
        return UI.combo(lista, d -> d == null ? "Todas" : Listas.texto(d));
    }

    private static void escuchar(Runnable accion, JComboBox<?>... combos) {
        for (JComboBox<?> c : combos) {
            c.addActionListener(e -> accion.run());
        }
    }

    /** Fila horizontal de filtros: pares (etiqueta, componente) o componentes sueltos. */
    private static JPanel fila(Object... elementos) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        for (Object e : elementos) {
            panel.add(e instanceof String ? new JLabel((String) e) : (java.awt.Component) e);
        }
        return panel;
    }

    private static JPanel envolver(JPanel arriba, java.awt.Component medio, Grafica grafica) {
        JPanel filtros = new JPanel(new BorderLayout());
        filtros.add(arriba, BorderLayout.NORTH);
        filtros.add(medio, BorderLayout.CENTER);
        filtros.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(filtros, BorderLayout.NORTH);
        JPanel marco = new JPanel(new BorderLayout());
        marco.setBorder(BorderFactory.createLineBorder(new Color(0xCED6E0)));
        marco.add(grafica);
        panel.add(marco, BorderLayout.CENTER);
        return panel;
    }
}
