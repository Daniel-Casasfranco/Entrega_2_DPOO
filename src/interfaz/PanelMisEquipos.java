package interfaz;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.Club;
import modelo.Equipo;
import modelo.EstadisticaBaloncesto;
import modelo.EstadisticaFutbol;
import modelo.EstadisticaPartido;
import modelo.EstadisticaVoleibol;
import modelo.Jugador;
import modelo.Sancion;
import modelo.Socio;
import modelo.Usuario;

/** El socio se inscribe en equipos y consulta sus estadisticas y sanciones (RF-01, RF-24). */
public class PanelMisEquipos extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloEquipos = UI.modeloTabla("Equipo", "Deporte", "Categoria", "Jugadores");
    private final DefaultTableModel modeloEstadisticas = UI.modeloTabla("Fecha", "Deporte", "Detalle");
    private final DefaultTableModel modeloSanciones =
            UI.modeloTabla("Fecha", "Tipo", "Partidos", "Cumplidos", "Estado");
    private final JLabel estado = new JLabel();

    private final JComboBox<Equipo> comboEquipo = UI.combo(new ArrayList<Equipo>(),
            e -> e.getNombre() + " (" + Listas.texto(e.getDeporte()) + ", " + Listas.texto(e.getCategoria()) + ")");
    private final JTextField campoPosicion = UI.campo("");
    private final JTextField campoCamiseta = UI.campo("");

    
    
    
    
    public PanelMisEquipos(Club club, Usuario usuario) {
    	
        super(club, usuario);
        
        //agrega titulo arriba del panel
        add(UI.titulo("Mis equipos"), BorderLayout.NORTH);
        
        //creo 3 paneles distintintos para cada parte ademas de el split pane
        
        
        //crea un grid para agregar las tres tablas a la izquierda con el split panel
        JPanel tablas = new JPanel(new GridLayout(3, 1, 0, 8));
        tablas.add(UI.seccion("Equipos en los que juego", UI.scroll(UI.tabla(modeloEquipos))));
        tablas.add(UI.seccion("Mis estadisticas", UI.scroll(UI.tabla(modeloEstadisticas))));
        tablas.add(UI.seccion("Mis sanciones", UI.scroll(UI.tabla(modeloSanciones))));
        
        
        
        //crea otro panel para incribirse en el equipo con un combo box y dos text areas un boton tambien.
        JPanel formulario = new JPanel(new BorderLayout());
        formulario.add(UI.seccion("Inscribirme en un equipo", UI.formulario("Equipo:", comboEquipo,
                "Posicion:", campoPosicion, "Camiseta:", campoCamiseta,
                UI.boton("Inscribirme", e -> inscribirme()))), BorderLayout.NORTH);
        formulario.add(estado, BorderLayout.SOUTH);

        
        // crea la division de tablas y formulario 
        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablas, formulario);
        division.setResizeWeight(0.62);
        add(division, BorderLayout.CENTER);
    }
    
    
    
    

    private void inscribirme() {
    	//funcion que inscribe con la informacion dada en las areas 
    	
        if (UI.ejecutar(this, () -> club.inscribirJugador(usuario.getCodigo(),
                UI.seleccionado(comboEquipo, "un equipo").getNombre(), UI.texto(campoPosicion, "Posicion"),
                UI.entero(campoCamiseta, "Camiseta")), "Inscripcion realizada")) {
            actualizar();
        }
    }

    /** Texto con los valores de una estadistica de partido. */
    static String describir(EstadisticaPartido e) {
        if (e instanceof EstadisticaFutbol) {
            EstadisticaFutbol f = (EstadisticaFutbol) e;
            return "Goles " + f.getGoles() + ", asistencias " + f.getAsistencias() + ", amarillas "
                    + f.getTarjetasAmarillas() + ", rojas " + f.getTarjetasRojas();
        }
        if (e instanceof EstadisticaBaloncesto) {
            EstadisticaBaloncesto b = (EstadisticaBaloncesto) e;
            return "Puntos " + b.getPuntos() + ", rebotes " + b.getRebotes();
        }
        if (e instanceof EstadisticaVoleibol) {
            EstadisticaVoleibol v = (EstadisticaVoleibol) e;
            return "Aces " + v.getAces() + ", bloqueos " + v.getBloqueos();
        }
        return "";
    }

    
    
    
    @Override
    public void actualizar() {
        UI.recargar(comboEquipo, club.getEquipos());
        modeloEquipos.setRowCount(0);
        modeloEstadisticas.setRowCount(0);
        modeloSanciones.setRowCount(0);

        Jugador jugador = Listas.jugadorDe((Socio) usuario);
        if (jugador == null) {
            estado.setText("Aun no esta inscrito en ningun equipo.");
            return;
        }
        campoPosicion.setText(jugador.getPosicion());
        campoCamiseta.setText(String.valueOf(jugador.getNumeroCamiseta()));
        estado.setText(jugador.estaSuspendido() ? "Estado: SUSPENDIDO" : "Estado: habilitado para jugar");
        for (Equipo e : jugador.getEquipos()) {
            modeloEquipos.addRow(new Object[] {e.getNombre(), Listas.texto(e.getDeporte()),
                Listas.texto(e.getCategoria()), e.getJugadores().size()});
        }
        for (EstadisticaPartido e : club.consultarEstadisticas(jugador)) {
            modeloEstadisticas.addRow(new Object[] {e.getPartido().getFecha(),
                Listas.texto(e.getPartido().getDeporte()), describir(e)});
        }
        for (Sancion s : club.consultarSancion(jugador)) {
            modeloSanciones.addRow(new Object[] {s.getFecha(), Listas.texto(s.getTipo()), s.getPartidosSuspension(),
                s.getPartidosCumplidos(), Listas.texto(s.getEstado())});
        }
    }
}
