package modelo;

import java.util.ArrayList;
import java.util.List;

public class Equipo  implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
    private final String nombre;
    private final Deporte deporte;
    private final Categoria categoria;
    private final int cupoMaximo;
    private final List<Jugador> jugadores = new ArrayList<>();
    public Equipo(String nombre, Deporte deporte, Categoria categoria, int cupoMaximo) {
        if (nombre == null || nombre.isBlank() || deporte == null || categoria == null || cupoMaximo <= 0)
            throw new IllegalArgumentException("Datos de equipo invalidos");
        if (deporte != Deporte.FUTBOL && deporte != Deporte.BALONCESTO && deporte != Deporte.VOLEIBOL)
            throw new IllegalArgumentException("No es deporte de conjunto");
        this.nombre = nombre;
        this.deporte = deporte;
        this.categoria = categoria;
        this.cupoMaximo = cupoMaximo;
    }

    public String getNombre() {
        return nombre;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public List<Jugador> getJugadores() {
        return new ArrayList<>(jugadores);
    }

    public void inscribirJugador(Jugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("Jugador inexistente");
        }
        if (jugadores.contains(jugador)) {
            throw new IllegalArgumentException("Ya pertenece al equipo");
        }
        if (jugadores.size() >= cupoMaximo) {
            throw new IllegalArgumentException("Equipo lleno");
        }
        if (jugador.estaEnDeporte(deporte)) {
            throw new IllegalArgumentException("Ya pertenece a otro equipo del deporte");
        }
        if (!cumpleCategoria(jugador.getEdad())) {
            throw new IllegalArgumentException("Edad fuera de categoria");
        }
        for (Jugador existente : jugadores) {
            if (existente.getNumeroCamiseta() == jugador.getNumeroCamiseta())
                throw new IllegalArgumentException("Numero de camiseta repetido");
        }
        jugadores.add(jugador);
        jugador.agregarEquipo(this);
    }

    public void retirarJugador(Jugador jugador) {
        if (jugadores.remove(jugador)) {
            jugador.quitarEquipo(this);
        }
    }

    public boolean cumpleCategoria(int edad) {
        switch (categoria) {
            case SUB_13: return edad >= 0 && edad < 13;
            case SUB_15: return edad < 15;
            case SUB_17: return edad < 17;
            case MAYORES: return edad >= 17;
            default: return false;
        }
    }
}
