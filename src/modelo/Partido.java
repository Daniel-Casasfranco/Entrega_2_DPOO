package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Partido  implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
    private final LocalDate fecha;
    private final Deporte deporte;
    private final List<EstadisticaPartido> estadisticas = new ArrayList<>();
    protected Partido(LocalDate fecha, Deporte deporte) {
        if (fecha == null || deporte == null) {
            throw new IllegalArgumentException("Datos de partido invalidos");
        }
        this.fecha = fecha;
        this.deporte = deporte;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public List<EstadisticaPartido> getEstadisticas() {
        return new ArrayList<>(estadisticas);
    }

    public void agregarEstadistica(EstadisticaPartido estadistica) {
        if (estadistica == null) {
            throw new IllegalArgumentException("Estadistica inexistente");
        }
        if (estadistica.getPartido() != this) {
            throw new IllegalArgumentException("La estadistica es de otro partido");
        }
        if (this instanceof PartidoEquipo) {
            PartidoEquipo pe = (PartidoEquipo) this;
            boolean pertenece = pe.getEquipo1().getJugadores().contains(estadistica.getJugador());
            if (pe.getEquipo2() != null) {
                pertenece |= pe.getEquipo2().getJugadores().contains(estadistica.getJugador());
            }
            if (!pertenece) {
                throw new IllegalArgumentException("Jugador no participa en el partido");
            }
        }
        for (EstadisticaPartido anterior : estadisticas) {
            if (anterior.getJugador() == estadistica.getJugador()) {
                throw new IllegalArgumentException("Estadistica duplicada");
            }
        }
        if ((deporte == Deporte.FUTBOL && !(estadistica instanceof EstadisticaFutbol)) ||
            (deporte == Deporte.BALONCESTO && !(estadistica instanceof EstadisticaBaloncesto)) ||
            (deporte == Deporte.VOLEIBOL && !(estadistica instanceof EstadisticaVoleibol))) {
            throw new IllegalArgumentException("Estadistica de otro deporte");
        }
        estadisticas.add(estadistica);
    }
}
