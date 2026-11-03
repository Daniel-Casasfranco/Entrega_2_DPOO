
package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class RegistroRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate fecha;
    private int puntajeAnterior;
    private int puntajeNuevo;

    public RegistroRanking(LocalDate fecha, int puntajeAnterior, int puntajeNuevo) {

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }

        if (puntajeAnterior < 0 || puntajeNuevo < 0) {
            throw new IllegalArgumentException("El puntaje no puede ser negativo");
        }

        this.fecha = fecha;
        this.puntajeAnterior = puntajeAnterior;
        this.puntajeNuevo = puntajeNuevo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getPuntajeAnterior() {
        return puntajeAnterior;
    }

    public int getPuntajeNuevo() {
        return puntajeNuevo;
    }
}
