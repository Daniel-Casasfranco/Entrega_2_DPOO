package modelo;

public class EstadisticaVoleibol extends EstadisticaPartido {
	
	private static final long serialVersionUID = 1L;
	
    private int aces;
    private int bloqueos;
    public EstadisticaVoleibol(Jugador jugador, Partido partido) {
        super(jugador, partido);
    }

    public int getAces() {
        return aces;
    }

    public void setAces(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Valor negativo");
        }
        aces = valor;
    }

    public int getBloqueos() {
        return bloqueos;
    }

    public void setBloqueos(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Valor negativo");
        }
        bloqueos = valor;
    }
}
