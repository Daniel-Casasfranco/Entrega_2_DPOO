package interfaz;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import modelo.Club;
import modelo.Convocatoria;
import modelo.EstadisticaPartido;
import modelo.Jugador;
import modelo.Sancion;
import modelo.Socio;
import modelo.Usuario;

/** Consulta del desempeno de un jugador por parte de entrenadores y fisioterapeutas (RF-28). */
public class PanelDesempeno extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final JComboBox<Socio> comboSocio = UI.combo(new ArrayList<Socio>(), Listas::nombreConCodigo);
    private final DefaultTableModel modeloAsistencia =
            UI.modeloTabla("Fecha", "Hora", "Instalacion", "Entrenador", "Asistencia", "Nota", "Comentario");
    private final DefaultTableModel modeloPartidos = UI.modeloTabla("Fecha", "Deporte", "Detalle");

    public PanelDesempeno(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Desempeno de jugadores"), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.add(UI.formulario("Socio:", comboSocio, UI.boton("Consultar", e -> consultar())),
                BorderLayout.NORTH);
        JPanel tablas = new JPanel(new java.awt.GridLayout(2, 1, 0, 8));
        tablas.add(UI.seccion("Entrenamientos evaluados", UI.scroll(UI.tabla(modeloAsistencia))));
        tablas.add(UI.seccion("Estadisticas en partidos y sanciones", UI.scroll(UI.tabla(modeloPartidos))));
        contenido.add(tablas, BorderLayout.CENTER);
        add(contenido, BorderLayout.CENTER);
    }

    private void consultar() {
        modeloAsistencia.setRowCount(0);
        modeloPartidos.setRowCount(0);
        Socio socio = (Socio) comboSocio.getSelectedItem();
        if (socio == null) {
            return;
        }
        UI.ejecutar(this, () -> {
            for (Convocatoria c : club.consultarDesempenoJugador(usuario.getCodigo(), socio)) {
                modeloAsistencia.addRow(new Object[] {c.getSesion().getFecha(), c.getSesion().getHora(),
                    c.getSesion().getInstalacion().getNombre(), Listas.nombre(c.getSesion().getEntrenador()),
                    Listas.texto(c.getAsistencia()), c.getNota() == 0 ? "-" : c.getNota(),
                    c.getComentario() == null ? "" : c.getComentario()});
            }
        }, null);

        Jugador jugador = Listas.jugadorDe(socio);
        if (jugador != null) {
            for (EstadisticaPartido e : club.consultarEstadisticas(jugador)) {
                modeloPartidos.addRow(new Object[] {e.getPartido().getFecha(), Listas.texto(e.getPartido().getDeporte()),
                    PanelMisEquipos.describir(e)});
            }
            for (Sancion s : club.consultarSancion(jugador)) {
                modeloPartidos.addRow(new Object[] {s.getFecha(), "Sancion", Listas.texto(s.getTipo()) + " - "
                    + Listas.texto(s.getEstado())});
            }
        }
    }

    @Override
    public void actualizar() {
        UI.recargar(comboSocio, Listas.socios(club));
    }
}
