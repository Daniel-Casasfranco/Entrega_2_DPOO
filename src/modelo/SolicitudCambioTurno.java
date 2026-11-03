package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SolicitudCambioTurno implements Serializable {

    private static final long serialVersionUID = 1L;

    private Empleado empleado;
    private Turno turno;                       
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEvaluacion;    
    private String motivo;
    private String nuevoDiaSemana;
    private LocalTime nuevaHoraInicio;
    private LocalTime nuevaHoraFin;
    private String observacionEvaluacion;   

    public SolicitudCambioTurno(Empleado empleado, Turno turno, String motivo,
            String nuevoDiaSemana, LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) {
        this.empleado = empleado;
        this.turno = turno;
        this.motivo = motivo;
        this.nuevoDiaSemana = nuevoDiaSemana;
        this.nuevaHoraInicio = nuevaHoraInicio;
        this.nuevaHoraFin = nuevaHoraFin;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaSolicitud = LocalDateTime.now();
    }

    public void aprobar() {
        estado = EstadoSolicitud.APROBADA;
        fechaEvaluacion = LocalDateTime.now();
    }

    public void rechazar() {
        estado = EstadoSolicitud.RECHAZADA;
        fechaEvaluacion = LocalDateTime.now();
    }

    public boolean cubreNuevo(String dia, LocalTime hora) {
        return nuevoDiaSemana.equalsIgnoreCase(dia)
                && !hora.isBefore(nuevaHoraInicio) && hora.isBefore(nuevaHoraFin);
    }

    public Empleado getEmpleado() { return empleado; }
    public Turno getTurno() { return turno; }
    public EstadoSolicitud getEstado() { return estado; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public LocalDateTime getFechaEvaluacion() { return fechaEvaluacion; }
    public String getMotivo() { return motivo; }
    public String getNuevoDiaSemana() { return nuevoDiaSemana; }
    public LocalTime getNuevaHoraInicio() { return nuevaHoraInicio; }
    public LocalTime getNuevaHoraFin() { return nuevaHoraFin; }
    public String getObservacionEvaluacion() { return observacionEvaluacion; }
    public void setObservacionEvaluacion(String observacion) { this.observacionEvaluacion = observacion; }

    @Override
    public String toString() {
        return empleado.getNombre() + " pide pasar su turno a [" + nuevoDiaSemana + " "
                + nuevaHoraInicio + "-" + nuevaHoraFin + "] (motivo: " + motivo + ") -> " + estado;
    }
}
