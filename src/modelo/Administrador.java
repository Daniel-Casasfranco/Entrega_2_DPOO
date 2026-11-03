package modelo;

import java.util.List;

public class Administrador extends Usuario {
	
	private static final long serialVersionUID = 1L;
	
    public Administrador(int codigo, String nombre, String apellido, String login, String password) {
        super(codigo, nombre, apellido, login, password);
    }

    public boolean puedeReservar() {
        return false;
    }

    public boolean puedeInscribirsePartido() {
        return false;
    }

    public boolean aprobarCambioTurno(Club club, SolicitudCambioTurno solicitud) {
        club.aprobarCambio(solicitud);
        return solicitud.getEstado() == EstadoSolicitud.APROBADA;
    }

    public boolean rechazarCambioTurno(Club club, SolicitudCambioTurno solicitud) {
        club.rechazarCambio(solicitud, "Rechazado por el administrador");
        return solicitud.getEstado() == EstadoSolicitud.RECHAZADA;
    }

    public List<Turno> consultarTurnos(Club club) {
        return club.consultarTurnos(getCodigo());
    }

    public List<SolicitudCambioTurno> consultarSolicitudesCambioTurno(Club club) {
        return club.getSolicitudes();
    }
}
