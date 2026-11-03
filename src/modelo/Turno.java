package modelo;

import java.io.Serializable;
import java.time.LocalTime;

public class Turno implements Serializable {

    private static final long serialVersionUID = 1L;

    private Empleado empleado;
    private String diaSemana;      
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public Turno(Empleado empleado, String diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        this.empleado = empleado;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        empleado.agregarTurno(this);
    }

    public void cambiar(SolicitudCambioTurno solicitud) {
        this.diaSemana = solicitud.getNuevoDiaSemana();
        this.horaInicio = solicitud.getNuevaHoraInicio();
        this.horaFin = solicitud.getNuevaHoraFin();
    }

    public boolean cubre(String dia, LocalTime hora) {
        return diaSemana.equalsIgnoreCase(dia)
                && !hora.isBefore(horaInicio) && hora.isBefore(horaFin);
    }

    public Empleado getEmpleado() { return empleado; }
    public String getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }

    @Override
    public String toString() {
        return empleado.getNombre() + ": " + diaSemana + " " + horaInicio + "-" + horaFin;
    }
}
