
package modelo;

import java.io.Serializable;

public class EstadisticaPractica implements Serializable {

    private static final long serialVersionUID = 1L;

    private InscripcionDisciplina inscripcion;
    private int sets;
    private int games;
    private int aces;
    private int erroresNoForzados;
    private int puntosTotales;

    public EstadisticaPractica(InscripcionDisciplina inscripcion,
            int sets, int games, int aces,
            int erroresNoForzados, int puntosTotales) {

        if (inscripcion == null) {
            throw new IllegalArgumentException("La inscripcion es obligatoria");
        }

        if (sets < 0 || games < 0 || aces < 0 ||
                erroresNoForzados < 0 || puntosTotales < 0) {

            throw new IllegalArgumentException(
                    "Las estadisticas no pueden ser negativas");
        }

        this.inscripcion = inscripcion;
        this.sets = sets;
        this.games = games;
        this.aces = aces;
        this.erroresNoForzados = erroresNoForzados;
        this.puntosTotales = puntosTotales;
    }

    public InscripcionDisciplina getInscripcion() {
        return inscripcion;
    }

    public int getSets() {
        return sets;
    }

    public int getGames() {
        return games;
    }

    public int getAces() {
        return aces;
    }

    public int getErroresNoForzados() {
        return erroresNoForzados;
    }

    public int getPuntosTotales() {
        return puntosTotales;
    }
}
