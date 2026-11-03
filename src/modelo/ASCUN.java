package modelo;

import java.util.ArrayList;
import java.util.List;

public class ASCUN extends Competencia {
	
	private static final long serialVersionUID = 1L;
	
    private final List<Fase> fases = new ArrayList<>();
    public ASCUN(String nombre, String organizador, String temporada, Deporte deporte,
        Categoria categoria, int nominaMinima) {
        super(nombre, organizador, temporada, deporte, categoria, nominaMinima);
    }

    public void agregarFase(Fase fase) {
        if (fase == null) {
            throw new IllegalArgumentException("Fase invalida");
        }
        fases.add(fase);
    }

    public List<Fase> getFases() {
        return new ArrayList<>(fases);
    }
}