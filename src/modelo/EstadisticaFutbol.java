package modelo;

public class EstadisticaFutbol extends EstadisticaPartido {
	
	private static final long serialVersionUID = 1L;
	
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    public EstadisticaFutbol(Jugador jugador, Partido partido) {
        super(jugador, partido);
    }

    public int getGoles() {
        return goles;
    }

    public void setGoles(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Valor negativo");
        }
        goles = valor;
    }

    public int getAsistencias() {
        return asistencias;
    }

    public void setAsistencias(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Valor negativo");
        }
        asistencias = valor;
    }

    public int getTarjetasAmarillas() {
        return tarjetasAmarillas;
    }

    public void setTarjetasAmarillas(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Valor negativo");
        }
        tarjetasAmarillas = valor;
    }

    public int getTarjetasRojas() {
        return tarjetasRojas;
    }

    public void setTarjetasRojas(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Valor negativo");
        }
        tarjetasRojas = valor;
    }
}
