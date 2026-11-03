package modelo;

import java.time.LocalDate;

public class Sancion  implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private final int id;
    private final TipoSancion tipo;
    private final LocalDate fecha;
    private final Jugador jugador;
    private final int partidosSuspension;
    private int partidosCumplidos;
    private EstadoSancion estado = EstadoSancion.ACTIVA;
    public Sancion(int id, TipoSancion tipo, LocalDate fecha, Jugador jugador, int partidosSuspension) {
        if (tipo == null || fecha == null || jugador == null || partidosSuspension < 1) {
            throw new IllegalArgumentException("Datos de sancion invalidos");
        }
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.jugador = jugador;
        this.partidosSuspension = partidosSuspension;
    }

    public int getId() {
        return id;
    }

    public TipoSancion getTipo() {
        return tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public int getPartidosSuspension() {
        return partidosSuspension;
    }

    public int getPartidosCumplidos() {
        return partidosCumplidos;
    }

    public EstadoSancion getEstado() {
        return estado;
    }

    public boolean estaActiva() {
        return estado == EstadoSancion.ACTIVA;
    }

    public void cumplirPartido() {
        if (!estaActiva()) {
            return;
        }
        partidosCumplidos++;
        if (partidosCumplidos >= partidosSuspension) {
            estado = EstadoSancion.CUMPLIDA;
        }
    }
}
