package modelo;

public class Fase  implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
    private final TipoFase tipo;
    private final int orden;
    public Fase(TipoFase tipo, int orden) {
        if (tipo == null || orden <= 0) {
            throw new IllegalArgumentException("Fase invalida");
        }
        this.tipo = tipo;
        this.orden = orden;
    }

    public TipoFase getTipo() {
        return tipo;
    }

    public int getOrden() {
        return orden;
    }
}
