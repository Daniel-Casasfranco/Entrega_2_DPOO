package interfaz;

import java.awt.BorderLayout;

import javax.swing.JTextArea;

import modelo.Club;
import modelo.Empleado;
import modelo.InscripcionDisciplina;
import modelo.Jugador;
import modelo.Mensualidad;
import modelo.Socio;
import modelo.Turno;
import modelo.Usuario;

/** Consulta del perfil del usuario (RF-44). */
public class PanelPerfil extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final JTextArea texto = UI.areaTexto();

    public PanelPerfil(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Mi perfil"), BorderLayout.NORTH);
        add(UI.scroll(texto), BorderLayout.CENTER);
    }

    @Override
    public void actualizar() {
    	//informacion general de usuario
        StringBuilder sb = new StringBuilder();
        sb.append(club.consultarPerfil(usuario)).append('\n');
        sb.append("Rol: ").append(VentanaPrincipal.rol(usuario)).append('\n');
        sb.append("Usuario: ").append(usuario.getLogin()).append("\n\n");
        
        //mira si es un empleado para agregar mas info
        if (usuario instanceof Empleado) {
            Empleado e = (Empleado) usuario;
            sb.append("Codigo de descuento: ").append(e.getCodigoDescuento()).append('\n');
            sb.append("Turnos:\n");
            if (e.getTurnos().isEmpty()) {
                sb.append("  (sin turnos asignados)\n");
            }
            for (Turno t : e.getTurnos()) {
                sb.append("  ").append(t).append('\n');
            }
        }
        
        //mira si es un socio para agregar mas info
        if (usuario instanceof Socio) {
            Socio s = (Socio) usuario;
            sb.append("Edad: ").append(s.getEdad()).append(" anos\n");
            sb.append("Puntos de fidelidad: ").append(s.getPuntosFidelidad()).append('\n');
            sb.append("Entrenador asociado: ")
                    .append(s.getEntrenadorAsociado() == null ? "ninguno" : Listas.nombre(s.getEntrenadorAsociado()))
                    .append("\n\n");

            Jugador j = Listas.jugadorDe(s);
            sb.append("Registro deportivo: ");
            sb.append(j == null ? "no juega en equipos\n"
                    : j.getPosicion() + ", camiseta " + j.getNumeroCamiseta() + ", equipos: " + j.getEquipos().size()
                            + (j.estaSuspendido() ? " (SUSPENDIDO)" : "") + "\n");

            sb.append("Disciplinas individuales:\n");
            if (s.getInscripciones().isEmpty()) {
                sb.append("  (ninguna)\n");
            }
            for (InscripcionDisciplina i : s.getInscripciones()) {
                sb.append("  ").append(Listas.texto(i.getDisciplina())).append(" | nivel ")
                        .append(Listas.texto(i.getNivel())).append(" | ranking ").append(i.getPuntajeRanking())
                        .append('\n');
            }

            sb.append("Mensualidades pagadas:\n");
            if (s.getMensualidades().isEmpty()) {
                sb.append("  (ninguna)\n");
            }
            for (Mensualidad m : s.getMensualidades()) {
                sb.append("  ").append(m.getPeriodo()).append(" | ").append(UI.dinero(m.getValor()))
                        .append(" | pagada el ").append(m.getFechaPago()).append('\n');
            }
        }
        texto.setText(sb.toString());
        texto.setCaretPosition(0);
    }
}
