package modelo;

import java.time.LocalDate;

public class PartidoCompeticion extends PartidoEquipo {
	private static final long serialVersionUID = 1L;
	private final String institucionRival;
    private final String escenario;
    private Competencia competencia;
    private Fase fase;
    public PartidoCompeticion(LocalDate fecha, Equipo equipo, String institucionRival, String escenario) {
        super(fecha, equipo.getDeporte(), equipo, null);
        if (institucionRival == null || institucionRival.isBlank() || escenario == null || escenario.isBlank())
            throw new IllegalArgumentException("Rival o escenario invalido");
        this.institucionRival = institucionRival;
        this.escenario = escenario;
    }

    public String getInstitucionRival() {
        return institucionRival;
    }

    public String getEscenario() {
        return escenario;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public void setCompetencia(Competencia competencia) {
        if (competencia != null && competencia.getDeporte() != getDeporte())
            throw new IllegalArgumentException("Deporte incompatible");
        this.competencia = competencia;
    }

    public Fase getFase() {
        return fase;
    }

    public void setFase(Fase fase) {
        this.fase = fase;
    }
}
