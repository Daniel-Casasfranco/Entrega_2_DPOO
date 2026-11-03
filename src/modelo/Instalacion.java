package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Instalacion implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String codigo;
    protected String nombre;
    protected Deporte disciplina;
    protected boolean techada;
    protected List<Modalidad> modalidadesPermitidas;
    private List<Reserva> reservas;
    private List<SesionEntrenamiento> sesiones;

    public Instalacion(String codigo, String nombre, Deporte disciplina, boolean techada,
            List<Modalidad> modalidadesPermitidas) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.disciplina = disciplina;
        this.techada = techada;
        this.modalidadesPermitidas = new ArrayList<>(modalidadesPermitidas);
        this.reservas = new ArrayList<>();
        this.sesiones = new ArrayList<>();
    }

    public boolean admite(int numeroJugadores, Modalidad modalidad) {
        if (modalidad == null || !modalidadesPermitidas.contains(modalidad)) {
            return false;
        }
        if (modalidad == Modalidad.SENCILLOS) {
            return numeroJugadores == 2;
        }
        return numeroJugadores == 4;
    }

    public boolean disponible(LocalDate fecha, LocalTime hora, int duracionMinutos) {
        LocalTime fin = hora.plusMinutes(duracionMinutos);
        for (Reserva r : reservas) {
            if (r.getFecha().equals(fecha)
                    && hora.isBefore(r.getHoraFin()) && fin.isAfter(r.getHoraInicio())) {
                return false;
            }
        }
        for (SesionEntrenamiento s : sesiones) {
            if (s.getFecha().equals(fecha)
                    && hora.isBefore(s.getHoraFin()) && fin.isAfter(s.getHora())) {
                return false;
            }
        }
        return true;
    }

    public void agregarReserva(Reserva reserva) {
        reservas.add(reserva);
    }

    public void agregarSesion(SesionEntrenamiento sesion) {
        sesiones.add(sesion);
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public Deporte getDisciplina() { return disciplina; }
    public boolean isTechada() { return techada; }
    public List<Modalidad> getModalidadesPermitidas() { return modalidadesPermitidas; }
    public List<Reserva> getReservas() { return reservas; }
    public List<SesionEntrenamiento> getSesiones() { return sesiones; }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}
