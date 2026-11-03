package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import modelo.*;

@DisplayName("Prueba 3: competencias, nomina y partidos oficiales (RF-05, RF-20, RF-21, RF-23)")
class Prueba3Test extends PruebaBase {

    private Equipo equipo;
    private Competencia torneo;

    @BeforeEach
    void crearEquipoYTorneo() {
        equipo = new Equipo("Universidad", Deporte.FUTBOL, Categoria.MAYORES, 15);
        club.getEquipos().add(equipo);
        for (int i = 0; i < 7; i++) {
            int codigo = 400 + i;
            club.getUsuarios().add(new Socio(codigo, "Jugador" + codigo, "Prueba", "s" + codigo,
                    "1234", LocalDate.of(2003, 1, 1)));
            club.inscribirJugador(codigo, "Universidad", "Defensa", i + 1);
        }
        torneo = new TorneoDeLosCerros("Cerros", "Organizacion", "2026",
                Deporte.FUTBOL, Categoria.MAYORES, 7);
        club.getCompetencias().add(torneo);
    }

    private InscripcionEquipo inscribir() {
        return club.inscribirEquipo(DatosBase.ENTRENADOR1, equipo, torneo, equipo.getJugadores());
    }

    private PartidoCompeticion jugar(InscripcionEquipo inscripcion) {
        return club.registrarPartidoCompeticion(DatosBase.ENTRENADOR1, inscripcion,
                "Otra universidad", "Estadio", LocalDate.now(), 2, 0,
                new HashMap<Jugador, Map<String, Integer>>());
    }

    @Test
    @DisplayName("Carlos inscribe el equipo con sus siete jugadores")
    void inscribirEquipo() {
        InscripcionEquipo inscripcion = inscribir();
        assertEquals(7, inscripcion.getNomina().size());
        assertEquals(1, torneo.getInscripciones().size());
    }

    @Test
    @DisplayName("Se registra un partido de competencia y suma puntos")
    void partidoCompetencia() {
        InscripcionEquipo inscripcion = inscribir();
        PartidoCompeticion partido = jugar(inscripcion);
        assertEquals("Otra universidad", partido.getInstitucionRival());
        assertEquals(1, torneo.getPartidos().size());
        assertTrue(inscripcion.getPuntos() > 0);
    }

    @Test
    @DisplayName("La competencia, la nomina y el partido se conservan al guardar y cargar")
    void persistencia() {
        jugar(inscribir());
        Club recuperado = guardarYRecargar();
        assertEquals(1, recuperado.getCompetencias().size());
        Competencia c = recuperado.getCompetencias().get(0);
        assertEquals(1, c.getInscripciones().size());
        assertEquals(1, c.getPartidos().size());
        assertEquals(7, c.getInscripciones().get(0).getNomina().size());
    }
}
