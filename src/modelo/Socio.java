
package modelo;

import java.time.LocalDate;

public class Socio extends Usuario {

	private static final long serialVersionUID = 1L;
	private final LocalDate fechaNacimiento;
    private int puntosFidelidad;
    private Jugador jugadorDeportivo;

    private Entrenador entrenadorAsociado;

    private final java.util.List<Mensualidad> mensualidades =
            new java.util.ArrayList<>();

    private final java.util.List<InscripcionDisciplina> inscripciones =
            new java.util.ArrayList<>();

    public Socio(int codigo, String nombre, String apellido, String login,
            String password, LocalDate fechaNacimiento) {

        super(codigo, nombre, apellido, login, password);

        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("Falta fecha de nacimiento");
        }

        this.fechaNacimiento = fechaNacimiento;
    }

    public Entrenador getEntrenadorAsociado() {
        return entrenadorAsociado;
    }

    public void asociarEntrenador(Entrenador entrenador) {

        if (entrenador == null) {
            throw new IllegalArgumentException("El entrenador es obligatorio");
        }

        if (entrenadorAsociado != null && entrenadorAsociado != entrenador) {
            throw new IllegalArgumentException(
                    "El socio ya tiene un entrenador asociado");
        }

        this.entrenadorAsociado = entrenador;
    }

    public Jugador getJugadorDeportivo() {
        return jugadorDeportivo;
    }

    public void setJugadorDeportivo(Jugador jugador) {
        this.jugadorDeportivo = jugador;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public int getPuntosFidelidad() {
        return puntosFidelidad;
    }

    public int getEdad() {
        return java.time.Period.between(
                fechaNacimiento, LocalDate.now()).getYears();
    }

    public void sumarPuntos(int puntos) {

        if (puntos < 0) {
            throw new IllegalArgumentException("Puntos invalidos");
        }

        puntosFidelidad += puntos;
    }
    
    public boolean tienePuntos(int puntos) {
        return puntosFidelidad >= puntos;
    }

    public void actualizarPuntos(int ganados, int redimidos) {
        puntosFidelidad += ganados - redimidos;
    }


    public void agregarMensualidad(Mensualidad mensualidad) {
        mensualidades.add(mensualidad);
    }

    public java.util.List<Mensualidad> getMensualidades() {
        return mensualidades;
    }

    public void agregarInscripcion(InscripcionDisciplina inscripcion) {
        inscripciones.add(inscripcion);
    }

    public java.util.List<InscripcionDisciplina> getInscripciones() {
        return inscripciones;
    }
}
