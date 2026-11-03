package modelo;

public class TorneoDeLosCerros extends Competencia {
	private static final long serialVersionUID = 1L;
	public TorneoDeLosCerros(String nombre, String organizador, String temporada,
        Deporte deporte, Categoria categoria, int nominaMinima) {
        super(nombre, organizador, temporada, deporte, categoria, nominaMinima);
    }
}
