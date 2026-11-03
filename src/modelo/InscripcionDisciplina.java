
package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InscripcionDisciplina implements Serializable {

    private static final long serialVersionUID = 1L;

    private Socio socio;
    private Deporte disciplina;
    private Nivel nivel;
    private int puntajeRanking;
    private List<RegistroRanking> historial;

    public InscripcionDisciplina(Socio socio, Deporte disciplina, Nivel nivel) {

        if (socio == null || disciplina == null || nivel == null) {
            throw new IllegalArgumentException("Datos de inscripcion incompletos");
        }

        if (disciplina != Deporte.TENIS &&
                disciplina != Deporte.PADEL &&
                disciplina != Deporte.TENIS_DE_MESA) {

            throw new IllegalArgumentException("No es una disciplina individual");
        }

        for (InscripcionDisciplina inscripcion : socio.getInscripciones()) {
            if (inscripcion.getDisciplina() == disciplina) {
                throw new IllegalArgumentException("El socio ya esta inscrito");
            }
        }

        this.socio = socio;
        this.disciplina = disciplina;
        this.nivel = nivel;
        this.puntajeRanking = 1000;
        this.historial = new ArrayList<>();

        socio.agregarInscripcion(this);
    }

    public Socio getSocio() {
        return socio;
    }

    public Deporte getDisciplina() {
        return disciplina;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public int getPuntajeRanking() {
        return puntajeRanking;
    }

    public List<RegistroRanking> getHistorial() {
        return new ArrayList<>(historial);
    }

    public void setNivel(Nivel nivel) {

        if (nivel == null) {
            throw new IllegalArgumentException("El nivel es obligatorio");
        }

        this.nivel = nivel;
    }

    public void actualizarRanking(int nuevoPuntaje, LocalDate fecha) {

        if (nuevoPuntaje < 0) {
            throw new IllegalArgumentException("El puntaje no puede ser negativo");
        }

        RegistroRanking registro = new RegistroRanking(
                fecha, puntajeRanking, nuevoPuntaje);

        historial.add(registro);
        puntajeRanking = nuevoPuntaje;
    }
}
