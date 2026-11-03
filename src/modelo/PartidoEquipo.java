package modelo;

import java.time.LocalDate;

public abstract class PartidoEquipo extends Partido {
	private static final long serialVersionUID = 1L;
	private final Equipo equipo1;
    private final Equipo equipo2;
    private int marcadorEquipo1;
    private int marcadorEquipo2;
    private boolean resultadoRegistrado;
    protected PartidoEquipo(LocalDate fecha, Deporte deporte, Equipo equipo1, Equipo equipo2) {
        super(fecha, deporte);
        if (equipo1 == null || equipo1.getDeporte() != deporte ||
            (equipo2 != null && (equipo1 == equipo2 || equipo2.getDeporte() != deporte || equipo1.getCategoria() != equipo2.getCategoria()))) {
            throw new IllegalArgumentException("Equipos incompatibles");
        }
        this.equipo1 = equipo1;
        this.equipo2 = equipo2;
    }

    public Equipo getEquipo1() {
        return equipo1;
    }

    public Equipo getEquipo2() {
        return equipo2;
    }

    public int getMarcadorEquipo1() {
        return marcadorEquipo1;
    }

    public int getMarcadorEquipo2() {
        return marcadorEquipo2;
    }

    public boolean tieneResultado() {
        return resultadoRegistrado;
    }

    public void registrarResultado(int marcador1, int marcador2) {
        if (resultadoRegistrado) {
            throw new IllegalStateException("Resultado ya registrado");
        }
        if (marcador1 < 0 || marcador2 < 0) {
            throw new IllegalArgumentException("Marcador negativo");
        }
        if (getDeporte() != Deporte.FUTBOL && marcador1 == marcador2)
            throw new IllegalArgumentException("Este deporte no permite empates");
        this.marcadorEquipo1 = marcador1;
        this.marcadorEquipo2 = marcador2;
        resultadoRegistrado = true;
    }
}
