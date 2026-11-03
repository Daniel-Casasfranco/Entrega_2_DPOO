package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Jugador extends Socio {
	private static final long serialVersionUID = 1L;
    private final String posicion;
    private final int numeroCamiseta;
    private final List<Equipo> equipos = new ArrayList<>();
    private final List<Sancion> sanciones = new ArrayList<>();
    public Jugador(int codigo, String nombre, String apellido, String login, String password,
        LocalDate fechaNacimiento, String posicion, int numeroCamiseta) {
        super(codigo, nombre, apellido, login, password, fechaNacimiento);
        this.posicion = posicion;
        this.numeroCamiseta = numeroCamiseta;
    }

    public String getPosicion() {
        return posicion;
    }

    public int getNumeroCamiseta() {
        return numeroCamiseta;
    }

    public List<Equipo> getEquipos() {
        return new ArrayList<>(equipos);
    }

    public boolean estaEnDeporte(Deporte deporte) {
        for (Equipo equipo : equipos) {
            if (equipo.getDeporte() == deporte) {
                return true;
            }
        }
        return false;
    }

    public List<Sancion> getSanciones() {
        return new ArrayList<>(sanciones);
    }

    public boolean estaSuspendido() {
        for (Sancion sancion : sanciones) {
            if (sancion.estaActiva()) {
                return true;
            }
        }
        return false;
    }

    public void agregarSancion(Sancion sancion) {
        if (sancion == null || sancion.getJugador() != this) {
            throw new IllegalArgumentException("Sancion invalida");
        }
        sanciones.add(sancion);
    }

    public void cumplirSuspension() {
        for (Sancion sancion : sanciones) {
            if (sancion.estaActiva()) {
                sancion.cumplirPartido();
            }
        }
    }
    void agregarEquipo(Equipo equipo) {
        equipos.add(equipo);
    }
    void quitarEquipo(Equipo equipo) {
        equipos.remove(equipo);
    }
}
