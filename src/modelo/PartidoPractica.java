
package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PartidoPractica extends Partido {

    private static final long serialVersionUID = 1L;

    private List<InscripcionDisciplina> participantes;
    private List<EstadisticaPractica> estadisticasPractica;
    private Modalidad modalidad;
    private boolean resultadoRegistrado;

    public PartidoPractica(List<InscripcionDisciplina> participantes,
            Modalidad modalidad, LocalDate fecha) {

        super(fecha, obtenerDeporte(participantes));

        if (modalidad == null) {
            throw new IllegalArgumentException("La modalidad es obligatoria");
        }

        if (modalidad == Modalidad.SENCILLOS && participantes.size() != 2) {
            throw new IllegalArgumentException("Sencillos requiere 2 jugadores");
        }

        if (modalidad == Modalidad.DOBLES && participantes.size() != 4) {
            throw new IllegalArgumentException("Dobles requiere 4 jugadores");
        }

        if (getDeporte() == Deporte.PADEL && modalidad != Modalidad.DOBLES) {
            throw new IllegalArgumentException("Padel solo permite dobles");
        }

        for (int i = 0; i < participantes.size(); i++) {

            InscripcionDisciplina actual = participantes.get(i);

            if (actual == null) {
                throw new IllegalArgumentException("Participante inexistente");
            }

            if (actual.getDisciplina() != getDeporte()) {
                throw new IllegalArgumentException(
                        "Los jugadores deben practicar el mismo deporte");
            }

            for (int j = i + 1; j < participantes.size(); j++) {

                InscripcionDisciplina siguiente = participantes.get(j);

                if (siguiente == null) {
                    throw new IllegalArgumentException("Participante inexistente");
                }

                if (actual.getSocio() == siguiente.getSocio()) {
                    throw new IllegalArgumentException("Hay jugadores repetidos");
                }
            }
        }

        this.participantes = new ArrayList<>(participantes);
        this.estadisticasPractica = new ArrayList<>();
        this.modalidad = modalidad;
        this.resultadoRegistrado = false;
    }

    private static Deporte obtenerDeporte(
            List<InscripcionDisciplina> participantes) {

        if (participantes == null || participantes.isEmpty()) {
            throw new IllegalArgumentException("Debe haber participantes");
        }

        if (participantes.get(0) == null) {
            throw new IllegalArgumentException("Participante inexistente");
        }

        return participantes.get(0).getDisciplina();
    }

    public List<InscripcionDisciplina> getParticipantes() {
        return new ArrayList<>(participantes);
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public boolean resuelto() {
        return resultadoRegistrado;
    }

    public List<EstadisticaPractica> getEstadisticasPractica() {
        return new ArrayList<>(estadisticasPractica);
    }

    public void agregarEstadisticaPractica(EstadisticaPractica estadistica) {

        if (resultadoRegistrado) {
            throw new IllegalStateException("El resultado ya fue registrado");
        }

        if (estadistica == null) {
            throw new IllegalArgumentException("La estadistica es obligatoria");
        }

        if (!participantes.contains(estadistica.getInscripcion())) {
            throw new IllegalArgumentException(
                    "El participante no pertenece al partido");
        }

        if (estadisticasPractica.size() >= participantes.size()) {
            throw new IllegalStateException(
                    "Ya se registraron todas las estadisticas");
        }

        for (EstadisticaPractica anterior : estadisticasPractica) {

            if (anterior.getInscripcion() == estadistica.getInscripcion()) {
                throw new IllegalArgumentException(
                        "El participante ya tiene estadisticas");
            }
        }

        estadisticasPractica.add(estadistica);
    }

    public void registrarResultado() {

        if (resultadoRegistrado) {
            throw new IllegalStateException("El resultado ya fue registrado");
        }

        if (estadisticasPractica.size() != participantes.size()) {
            throw new IllegalStateException(
                    "Faltan estadisticas de participantes");
        }

        resultadoRegistrado = true;
    }
}
