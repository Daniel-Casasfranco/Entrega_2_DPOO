package interfaz;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.Administrador;
import modelo.Categoria;
import modelo.Club;
import modelo.Deporte;
import modelo.Equipo;
import modelo.Jugador;
import modelo.Socio;
import modelo.Usuario;

/** Administracion de equipos y jugadores (crear, dar de baja, inscribir socios). */
public class PanelEquipos extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloEquipos =
            UI.modeloTabla("Equipo", "Deporte", "Categoria", "Jugadores", "Cupo");
    private final JTable tablaEquipos = UI.tabla(modeloEquipos);
    private final DefaultTableModel modeloJugadores =
            UI.modeloTabla("Codigo", "Jugador", "Posicion", "Camiseta", "Sanciones", "Estado");
    private final JTable tablaJugadores = UI.tabla(modeloJugadores);

    private final JTextField campoNombre = UI.campo("");
    private final JComboBox<Deporte> comboDeporte = UI.combo(Arrays.asList(Listas.DEPORTES_CONJUNTO), Listas::texto);
    private final JComboBox<Categoria> comboCategoria = UI.combo(Arrays.asList(Categoria.values()), Listas::texto);
    private final JTextField campoCupo = UI.campo("15");

    private final JComboBox<Socio> comboSocio = UI.combo(Arrays.<Socio>asList(), Listas::nombreConCodigo);
    private final JTextField campoPosicion = UI.campo("");
    private final JTextField campoCamiseta = UI.campo("");

    public PanelEquipos(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Equipos y jugadores"), BorderLayout.NORTH);

        tablaEquipos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarJugadores();
            }
        });

        JPanel tablas = new JPanel(new GridLayout(2, 1, 0, 10));
        tablas.add(UI.seccion("Equipos del club", UI.scroll(tablaEquipos)));
        tablas.add(UI.seccion("Jugadores del equipo seleccionado", UI.scroll(tablaJugadores)));

        JPanel formularios = new JPanel(new GridLayout(2, 1, 0, 10));
        formularios.add(UI.seccion("Crear equipo", UI.formulario(
                "Nombre:", campoNombre, "Deporte:", comboDeporte, "Categoria:", comboCategoria,
                "Cupo maximo:", campoCupo,
                UI.boton("Crear equipo", e -> crearEquipo()))));
        formularios.add(UI.seccion("Jugadores", UI.formulario(
                "Socio:", comboSocio, "Posicion:", campoPosicion, "Camiseta:", campoCamiseta,
                UI.boton("Inscribir en el equipo seleccionado", e -> inscribir()),
                UI.boton("Dar de baja al jugador seleccionado", e -> darDeBajaJugador()),
                UI.boton("Dar de baja el equipo seleccionado", e -> darDeBajaEquipo()))));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablas, formularios);
        division.setResizeWeight(0.62);
        add(division, BorderLayout.CENTER);
    }

    private Administrador admin() {
        return (Administrador) usuario;
    }

    private Equipo equipoSeleccionado() {
        int fila = UI.filaSeleccionada(tablaEquipos);
        return fila < 0 ? null : club.getEquipos().get(fila);
    }

    private void crearEquipo() {
        if (UI.ejecutar(this, () -> club.crearEquipo(admin(), new Equipo(UI.texto(campoNombre, "Nombre"),
                UI.seleccionado(comboDeporte, "un deporte"), UI.seleccionado(comboCategoria, "una categoria"),
                UI.entero(campoCupo, "Cupo maximo"))), "Equipo creado")) {
            campoNombre.setText("");
            actualizar();
        }
    }

    private void inscribir() {
        Equipo equipo = equipoSeleccionado();
        if (equipo == null) {
            UI.error(this, "Seleccione un equipo en la tabla");
            return;
        }
        if (UI.ejecutar(this, () -> club.inscribirJugador(UI.seleccionado(comboSocio, "un socio").getCodigo(),
                equipo.getNombre(), UI.texto(campoPosicion, "Posicion"), UI.entero(campoCamiseta, "Camiseta")),
                "Jugador inscrito en " + equipo.getNombre())) {
            actualizar();
        }
    }

    private void darDeBajaJugador() {
        Equipo equipo = equipoSeleccionado();
        int fila = UI.filaSeleccionada(tablaJugadores);
        if (equipo == null || fila < 0) {
            UI.error(this, "Seleccione un equipo y uno de sus jugadores");
            return;
        }
        Jugador jugador = equipo.getJugadores().get(fila);
        if (UI.ejecutar(this, () -> club.darDeBajaJugador(admin(), jugador), "Jugador dado de baja")) {
            actualizar();
        }
    }

    private void darDeBajaEquipo() {
        Equipo equipo = equipoSeleccionado();
        if (equipo == null) {
            UI.error(this, "Seleccione un equipo en la tabla");
            return;
        }
        if (UI.ejecutar(this, () -> club.darDeBajaEquipo(admin(), equipo), "Equipo dado de baja")) {
            actualizar();
        }
    }

    private void mostrarJugadores() {
        modeloJugadores.setRowCount(0);
        Equipo equipo = equipoSeleccionado();
        if (equipo == null) {
            return;
        }
        for (Jugador j : equipo.getJugadores()) {
            modeloJugadores.addRow(new Object[] {j.getCodigo(), Listas.nombre(j), j.getPosicion(),
                j.getNumeroCamiseta(), j.getSanciones().size(), j.estaSuspendido() ? "Suspendido" : "Habilitado"});
        }
    }

    @Override
    public void actualizar() {
        int seleccion = tablaEquipos.getSelectedRow();
        modeloEquipos.setRowCount(0);
        for (Equipo e : club.getEquipos()) {
            modeloEquipos.addRow(new Object[] {e.getNombre(), Listas.texto(e.getDeporte()),
                Listas.texto(e.getCategoria()), e.getJugadores().size(), e.getCupoMaximo()});
        }
        if (seleccion >= 0 && seleccion < modeloEquipos.getRowCount()) {
            tablaEquipos.setRowSelectionInterval(seleccion, seleccion);
        } else if (modeloEquipos.getRowCount() > 0) {
            tablaEquipos.setRowSelectionInterval(0, 0);
        }
        UI.recargar(comboSocio, Listas.socios(club));
        mostrarJugadores();
    }
}
