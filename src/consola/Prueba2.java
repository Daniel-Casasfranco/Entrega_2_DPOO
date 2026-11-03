package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.Categoria;
import modelo.Club;
import modelo.Deporte;
import modelo.Equipo;
import modelo.Jugador;
import modelo.PartidoInterno;
import modelo.ReglaNegocioException;
import modelo.Socio;

@DisplayName("Prueba 2: partido interno, estadisticas y sanciones (RF-02, RF-22, RF-24)")
class Prueba2Test extends PruebaBase {

    private Equipo azules;
    private Equipo rojos;
    private Jugador destacado;

    @BeforeEach
    void crearEquipos() {
        azules = crearEquipo("Azules", 100, 7);
        rojos = crearEquipo("Rojos", 200, 7);
        destacado = azules.getJugadores().get(0);
    }

    private Equipo crearEquipo(String nombre, int primerCodigo, int jugadores) {
        Equipo equipo = new Equipo(nombre, Deporte.FUTBOL, Categoria.MAYORES, 15);
        club.getEquipos().add(equipo);
        for (int i = 0; i < jugadores; i++) {
            int codigo = primerCodigo + i;
            club.getUsuarios().add(new Socio(codigo, "Jugador" + codigo, "Prueba", "s" + codigo,
                    "1234", LocalDate.of(2003, 1, 1)));
            club.inscribirJugador(codigo, nombre, "Defensa", i + 1);
        }
        return equipo;
    }

    private PartidoInterno registrarPartido() {
        Map<String, Integer> datos = new HashMap<>();
        datos.put("goles", 2);
        datos.put("tarjetasAmarillas", 2);
        Map<Jugador, Map<String, Integer>> estadisticas = new HashMap<>();
        estadisticas.put(destacado, datos);
        return club.registrarPartidoInterno(DatosBase.ENTRENADOR1, azules, rojos,
                LocalDate.now(), 2, 1, estadisticas);
    }

    @Test
    @DisplayName("Carlos registra Azules 2 - 1 Rojos con estadisticas")
    void partidoInterno() {
        PartidoInterno partido = registrarPartido();
        assertEquals(2, partido.getMarcadorEquipo1());
        assertEquals(1, partido.getMarcadorEquipo2());
        assertEquals(1, club.getPartidos().size());
    }

    @Test
    @DisplayName("Dos amarillas generan una sancion al jugador")
    void sancionPorTarjetas() {
        registrarPartido();
        assertFalse(destacado.getSanciones().isEmpty());
    }

    @Test
    @DisplayName("Se rechaza un partido con un equipo de solo seis jugadores")
    void alineacionInsuficiente() {
        Equipo incompleto = crearEquipo("Incompleto", 300, 6);
        assertThrows(ReglaNegocioException.class,
                () -> club.registrarPartidoInterno(DatosBase.ENTRENADOR1, incompleto, rojos,
                        LocalDate.now(), 0, 1, new HashMap<Jugador, Map<String, Integer>>()));
        assertTrue(club.getPartidos().isEmpty());
    }

    @Test
    @DisplayName("El resultado y las estadisticas se conservan al guardar y cargar")
    void persistencia() {
        registrarPartido();
        Club recuperado = guardarYRecargar();
        assertEquals(1, recuperado.getPartidos().size());
        PartidoInterno p = (PartidoInterno) recuperado.getPartidos().get(0);
        assertTrue(p.tieneResultado());
        assertEquals(2, p.getMarcadorEquipo1());
        assertEquals(1, p.getMarcadorEquipo2());
        assertFalse(p.getEstadisticas().isEmpty());
    }
}
