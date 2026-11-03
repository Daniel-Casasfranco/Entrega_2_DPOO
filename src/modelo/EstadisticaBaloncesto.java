package modelo;

public class EstadisticaBaloncesto extends EstadisticaPartido {
	
	private static final long serialVersionUID = 1L;
	
    private int puntos;
    private int rebotes;
    public EstadisticaBaloncesto(Jugador jugador, Partido partido) {
        super(jugador, partido);
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Valor negativo");
        }
        puntos = valor;
    }

    public int getRebotes() {
        return rebotes;
    }

    public void setRebotes(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Valor negativo");
        }
        rebotes = valor;
    }
}
