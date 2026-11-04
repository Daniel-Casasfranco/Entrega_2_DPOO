package interfaz;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import graficas.DatosGraficas;
import modelo.Club;
import modelo.Deporte;
import modelo.Entrenador;
import modelo.InscripcionDisciplina;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.Partido;
import modelo.PartidoPractica;
import modelo.ReglaNegocioException;
import modelo.Socio;
import modelo.Usuario;

/**
 * Disciplinas individuales: inscripcion con nivel, ranking, programar practicas y
 * registrar sus resultados (RF-04 a RF-07, RF-29 a RF-31).
 */
public class PanelPracticas extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final Socio socio;

    private final DefaultTableModel modeloRanking = UI.modeloTabla("#", "Socio", "Nivel", "Puntaje");
    private final JComboBox<Deporte> comboRanking =
            UI.combo(Arrays.asList(Listas.DISCIPLINAS_INDIVIDUALES), Listas::texto);

    // Inscripcion
    private final JComboBox<Deporte> comboDisciplina =
            UI.combo(Arrays.asList(Listas.DISCIPLINAS_INDIVIDUALES), Listas::texto);
    private final JComboBox<Nivel> comboNivel = UI.combo(Arrays.asList(Nivel.values()), Listas::texto);
    private final JComboBox<Entrenador> comboEntrenador = UI.combo(new ArrayList<Entrenador>(), Listas::nombre);

    // Programar practica
    private final JComboBox<Deporte> comboPracticaDisciplina =
            UI.combo(Arrays.asList(Listas.DISCIPLINAS_INDIVIDUALES), Listas::texto);
    private final JComboBox<Modalidad> comboModalidad = UI.combo(Arrays.asList(Modalidad.values()), Listas::texto);
    private final JList<InscripcionDisciplina> listaRivales = UI.listaMultiple(new ArrayList<InscripcionDisciplina>(),
            i -> Listas.nombre(i.getSocio()) + " (" + Listas.texto(i.getNivel()) + ", " + i.getPuntajeRanking() + ")");
    private final JComboBox<InscripcionDisciplina> comboPareja = UI.combo(new ArrayList<InscripcionDisciplina>(),
            i -> i == null ? "(ninguna - sencillos)" : Listas.nombre(i.getSocio()));
    private final JTextField campoFecha = UI.campo(LocalDate.now().toString());

    // Registrar resultado
    private final DefaultTableModel modeloPendientes = UI.modeloTabla("Fecha", "Disciplina", "Modalidad", "Participantes");
    private final JTable tablaPendientes = UI.tabla(modeloPendientes);
    private final List<PartidoPractica> pendientes = new ArrayList<>();
    private final DefaultTableModel modeloResultado = new DefaultTableModel(
            new String[] {"Participante", "Sets", "Games", "Aces", "Errores no forzados", "Puntos totales"}, 0) {
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
    private final JTable tablaResultado = new JTable(modeloResultado);

    public PanelPracticas(Club club, Usuario usuario) {
        super(club, usuario);
        this.socio = (Socio) usuario;
        add(UI.titulo("Disciplinas individuales"), BorderLayout.NORTH);

        comboRanking.addActionListener(e -> mostrarRanking());
        comboPracticaDisciplina.addActionListener(e -> mostrarRivales());
        tablaPendientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarResultado();
            }
        });
        tablaResultado.setRowHeight(22);
        tablaResultado.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JPanel ranking = new JPanel(new BorderLayout(6, 6));
        ranking.add(UI.formulario("Disciplina:", comboRanking), BorderLayout.NORTH);
        ranking.add(UI.scroll(UI.tabla(modeloRanking)), BorderLayout.CENTER);

        JPanel inscripcion = new JPanel(new GridLayout(2, 1, 0, 8));
        inscripcion.add(UI.seccion("Inscribirme en una disciplina", UI.formulario("Disciplina:", comboDisciplina,
                "Nivel:", comboNivel, UI.boton("Inscribirme", e -> inscribirme()))));
        inscripcion.add(UI.seccion("Soy tambien entrenador", UI.formulario("Entrenador:", comboEntrenador,
                UI.boton("Vincular mi cuenta", e -> vincular()))));

        JPanel programar = new JPanel(new BorderLayout(6, 6));
        programar.add(UI.formulario("Disciplina:", comboPracticaDisciplina, "Modalidad:", comboModalidad,
                "Mi pareja (dobles):", comboPareja, "Fecha (AAAA-MM-DD):", campoFecha), BorderLayout.NORTH);
        programar.add(UI.seccion("Rivales (1 en sencillos, 2 en dobles; Ctrl para varios)",
                UI.scroll(listaRivales)), BorderLayout.CENTER);
        programar.add(UI.boton("Programar practica", e -> programar()), BorderLayout.SOUTH);

        JPanel resultado = new JPanel(new GridLayout(2, 1, 0, 8));
        resultado.add(UI.seccion("Practicas sin resultado en las que participo", UI.scroll(tablaPendientes)));
        JPanel estadisticas = new JPanel(new BorderLayout(6, 6));
        estadisticas.add(UI.scroll(tablaResultado), BorderLayout.CENTER);
        estadisticas.add(UI.boton("Registrar resultado", e -> registrarResultado()), BorderLayout.SOUTH);
        resultado.add(UI.seccion("Estadisticas (tenis de mesa: solo sets y puntos)", estadisticas));

        JTabbedPane acciones = new JTabbedPane();
        acciones.addTab("Inscripcion", inscripcion);
        acciones.addTab("Programar practica", programar);
        acciones.addTab("Registrar resultado", resultado);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, UI.seccion("Ranking", ranking), acciones);
        division.setResizeWeight(0.4);
        add(division, BorderLayout.CENTER);
    }

    private void inscribirme() {
        if (UI.ejecutar(this, () -> {
            new InscripcionDisciplina(socio, UI.seleccionado(comboDisciplina, "una disciplina"),
                    UI.seleccionado(comboNivel, "un nivel"));
            club.guardar();
        }, "Inscripcion realizada. Su puntaje inicial es 1000.")) {
            actualizar();
        }
    }

    private void vincular() {
        UI.ejecutar(this, () -> club.vincularSocioEntrenador(socio.getCodigo(),
                UI.seleccionado(comboEntrenador, "un entrenador").getCodigo()),
                "Cuenta vinculada: puede jugar practicas contra cualquier nivel");
    }

    private void programar() {
        if (UI.ejecutar(this, () -> {
            Deporte disciplina = UI.seleccionado(comboPracticaDisciplina, "una disciplina");
            InscripcionDisciplina propia = DatosGraficas.inscripcion(socio, disciplina);
            if (propia == null) {
                throw new ReglaNegocioException("Primero debe inscribirse en " + Listas.texto(disciplina));
            }
            Modalidad modalidad = UI.seleccionado(comboModalidad, "una modalidad");
            List<InscripcionDisciplina> rivales = listaRivales.getSelectedValuesList();
            InscripcionDisciplina pareja = (InscripcionDisciplina) comboPareja.getSelectedItem();
            int esperados = modalidad == Modalidad.SENCILLOS ? 1 : 2;
            if (rivales.size() != esperados) {
                throw new ReglaNegocioException("Seleccione " + esperados + " rival(es)");
            }
            if (modalidad == Modalidad.DOBLES && (pareja == null || rivales.contains(pareja))) {
                throw new ReglaNegocioException("En dobles elija una pareja que no este entre los rivales");
            }
            // El orden importa: la primera mitad es una pareja y la segunda la otra
            List<InscripcionDisciplina> participantes = new ArrayList<>();
            participantes.add(propia);
            if (modalidad == Modalidad.DOBLES) {
                participantes.add(pareja);
            }
            participantes.addAll(rivales);
            club.programarPractica(participantes, modalidad, UI.fecha(campoFecha, "Fecha"));
        }, "Practica programada")) {
            actualizar();
        }
    }

    private PartidoPractica pendienteSeleccionado() {
        int fila = UI.filaSeleccionada(tablaPendientes);
        return fila < 0 ? null : pendientes.get(fila);
    }

    private void mostrarResultado() {
        modeloResultado.setRowCount(0);
        PartidoPractica p = pendienteSeleccionado();
        if (p == null) {
            return;
        }
        for (InscripcionDisciplina i : p.getParticipantes()) {
            modeloResultado.addRow(new Object[] {Listas.nombre(i.getSocio()), 0, 0, 0, 0, 0});
        }
    }

    private void registrarResultado() {
        PartidoPractica p = pendienteSeleccionado();
        if (p == null) {
            UI.error(this, "Seleccione una practica");
            return;
        }
        if (tablaResultado.isEditing()) {
            tablaResultado.getCellEditor().stopCellEditing();
        }
        String[] claves = {"sets", "games", "aces", "erroresNoForzados", "puntosTotales"};
        Map<InscripcionDisciplina, Map<String, Integer>> datos = new HashMap<>();
        for (int f = 0; f < p.getParticipantes().size(); f++) {
            Map<String, Integer> valores = new HashMap<>();
            for (int c = 0; c < claves.length; c++) {
                Object v = modeloResultado.getValueAt(f, c + 1);
                valores.put(claves[c], v == null ? 0 : (Integer) v);
            }
            datos.put(p.getParticipantes().get(f), valores);
        }
        if (UI.ejecutar(this, () -> club.registrarPractica(p, datos), "Resultado registrado y ranking actualizado")) {
            actualizar();
        }
    }

    private void mostrarRanking() {
        modeloRanking.setRowCount(0);
        Deporte d = (Deporte) comboRanking.getSelectedItem();
        if (d == null) {
            return;
        }
        List<InscripcionDisciplina> lista = new ArrayList<>();
        for (Socio s : DatosGraficas.sociosDeDisciplina(club, d)) {
            lista.add(DatosGraficas.inscripcion(s, d));
        }
        lista.sort((a, b) -> Integer.compare(b.getPuntajeRanking(), a.getPuntajeRanking()));
        int puesto = 1;
        for (InscripcionDisciplina i : lista) {
            modeloRanking.addRow(new Object[] {puesto++, Listas.nombre(i.getSocio())
                + (i.getSocio() == socio ? "  (yo)" : ""), Listas.texto(i.getNivel()), i.getPuntajeRanking()});
        }
    }

    private void mostrarRivales() {
        Deporte d = (Deporte) comboPracticaDisciplina.getSelectedItem();
        List<InscripcionDisciplina> rivales = new ArrayList<>();
        if (d != null) {
            for (Socio s : DatosGraficas.sociosDeDisciplina(club, d)) {
                if (s != socio) {
                    rivales.add(DatosGraficas.inscripcion(s, d));
                }
            }
        }
        UI.recargar(listaRivales, rivales);
        List<InscripcionDisciplina> parejas = new ArrayList<>();
        parejas.add(null);
        parejas.addAll(rivales);
        UI.recargar(comboPareja, parejas);
    }

    @Override
    public void actualizar() {
        UI.recargar(comboEntrenador, Listas.entrenadores(club));
        mostrarRanking();
        mostrarRivales();

        pendientes.clear();
        modeloPendientes.setRowCount(0);
        for (Partido p : club.getPartidos()) {
            if (p instanceof PartidoPractica && !((PartidoPractica) p).resuelto()) {
                PartidoPractica pp = (PartidoPractica) p;
                StringBuilder nombres = new StringBuilder();
                boolean participo = false;
                for (InscripcionDisciplina i : pp.getParticipantes()) {
                    participo |= i.getSocio() == socio;
                    if (nombres.length() > 0) {
                        nombres.append(", ");
                    }
                    nombres.append(i.getSocio().getNombre());
                }
                if (participo) {
                    pendientes.add(pp);
                    modeloPendientes.addRow(new Object[] {pp.getFecha(), Listas.texto(pp.getDeporte()),
                        Listas.texto(pp.getModalidad()), nombres.toString()});
                }
            }
        }
        mostrarResultado();
    }
}
