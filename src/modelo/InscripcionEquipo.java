package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InscripcionEquipo  implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
    private final LocalDate fecha;
    private final Equipo equipo;
    private final Competencia competencia;
    private String grupo;
    private int puntos;
    private int ganados;
    private int empatados;
    private int perdidos;
    private final List<JugadorNomina> nomina = new ArrayList<>();
    public InscripcionEquipo(Equipo equipo, Competencia competencia) {
        if (equipo == null || competencia == null) {
            throw new IllegalArgumentException("Datos invalidos");
        }
        this.equipo = equipo;
        this.competencia = competencia;
        this.fecha = LocalDate.now();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getGanados() {
        return ganados;
    }

    public int getEmpatados() {
        return empatados;
    }

    public int getPerdidos() {
        return perdidos;
    }

    public List<JugadorNomina> getNomina() {
        return new ArrayList<>(nomina);
    }

    public void inscribirJugadorNomina(Jugador jugador) {
        if (jugador == null || jugador.estaSuspendido() || !equipo.getJugadores().contains(jugador))
            throw new IllegalArgumentException("El jugador no pertenece al equipo");
        for (JugadorNomina registro : nomina) {
            if (registro.getJugador() == jugador) {
                throw new IllegalArgumentException("Jugador repetido");
            }
        }
        nomina.add(new JugadorNomina(jugador, this));
    }

    public boolean contieneJugador(Jugador jugador) {
        for (JugadorNomina registro : nomina) {
            if (registro.getJugador() == jugador) {
                return true;
            }
        }
        return false;
    }

    public boolean cumpleNominaMinima() {
        return nomina.size() >= competencia.getNominaMinima();
    }

    public void registrarResultado(int golesFavor, int golesContra, int puntosVictoria,
        int puntosEmpate, int puntosDerrota) {
        if (golesFavor < 0 || golesContra < 0) {
            throw new IllegalArgumentException("Resultado invalido");
        }
        if (golesFavor > golesContra) {
            ganados++;
            puntos += puntosVictoria;
        }
        else if (golesFavor == golesContra) {
            empatados++;
            puntos += puntosEmpate;
        }
        else {
            perdidos++;
            puntos += puntosDerrota;
        }
    }
}
