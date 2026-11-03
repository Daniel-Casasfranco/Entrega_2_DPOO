package modelo;

public abstract class EstadisticaPartido  implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
    private final Jugador jugador;
    private final Partido partido;
    protected EstadisticaPartido(Jugador jugador, Partido partido) {
        if (jugador == null || partido == null) {
            throw new IllegalArgumentException("Datos invalidos");
        }
        this.jugador = jugador;
        this.partido = partido;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public Partido getPartido() {
        return partido;
    }
}
