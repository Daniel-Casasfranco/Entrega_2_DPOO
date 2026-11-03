package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SesionEntrenamiento implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate fecha;
    private LocalTime hora;
    private int duracionMinutos;
    private Entrenador entrenador;
    private Instalacion instalacion;
    private Equipo equipo;                       
    private List<Convocatoria> convocatorias;  

    public SesionEntrenamiento(Entrenador entrenador, Instalacion instalacion,
            LocalDate fecha, LocalTime hora, int duracionMinutos) {
        this.entrenador = entrenador;
        this.instalacion = instalacion;
        this.fecha = fecha;
        this.hora = hora;
        this.duracionMinutos = duracionMinutos;
        this.convocatorias = new ArrayList<>();
        instalacion.agregarSesion(this);
    }

    public LocalTime getHoraFin() {
        return hora.plusMinutes(duracionMinutos);
    }

    public boolean haTerminado() {
        return LocalDateTime.of(fecha, getHoraFin()).isBefore(LocalDateTime.now());
    }

    public void agregarConvocatoria(Convocatoria convocatoria) {
        convocatorias.add(convocatoria);
    }

    public Convocatoria buscarConvocatoria(Socio socio) {
        for (Convocatoria c : convocatorias) {
            if (c.getSocio() == socio) {
                return c;
            }
        }
        return null;
    }

    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public Entrenador getEntrenador() { return entrenador; }
    public Instalacion getInstalacion() { return instalacion; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public List<Convocatoria> getConvocatorias() { return convocatorias; }

    @Override
    public String toString() {
        return fecha + " " + hora + " (" + duracionMinutos + " min) en "
                + instalacion.getNombre() + " con " + entrenador.getNombre();
    }
}
