package interfaz;

import java.awt.BorderLayout;

import javax.swing.table.DefaultTableModel;

import modelo.Club;
import modelo.Convocatoria;
import modelo.SesionEntrenamiento;
import modelo.Socio;
import modelo.Usuario;

/** El socio consulta sus convocatorias, asistencia y evaluaciones (RF-17, RF-18). */
public class PanelMisEntrenamientos extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modelo = UI.modeloTabla("Fecha", "Hora", "Instalacion", "Entrenador",
            "Estado", "Asistencia", "Nota", "Comentario");

    public PanelMisEntrenamientos(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Mis entrenamientos"), BorderLayout.NORTH);
        add(UI.seccion("Convocatorias", UI.scroll(UI.tabla(modelo))), BorderLayout.CENTER);
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        try {
            for (Convocatoria c : club.consultarConvocatorias((Socio) usuario)) {
                SesionEntrenamiento s = c.getSesion();
                String asistencia = "-";
                if (!s.haTerminado()) {
                    asistencia = "Sesion pendiente";
                } else if (c.getAsistencia() != null) {
                    asistencia = Listas.texto(c.getAsistencia());
                } else {
                    asistencia = "Sin registrar";
                }
                modelo.addRow(new Object[] {s.getFecha(), s.getHora(), s.getInstalacion().getNombre(),
                    Listas.nombre(s.getEntrenador()), s.haTerminado() ? "Terminada" : "Programada", asistencia,
                    c.getNota() == 0 ? "-" : c.getNota(), c.getComentario() == null ? "" : c.getComentario()});
            }
        } catch (RuntimeException e) {
            // El socio aun no tiene convocatorias: la tabla queda vacia
        }
    }
}
