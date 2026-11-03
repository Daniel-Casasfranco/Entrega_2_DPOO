package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.Categoria;
import modelo.Club;
import modelo.Deporte;
import modelo.Equipo;
import modelo.ReglaNegocioException;
import modelo.Socio;


@DisplayName("Prueba 1: inscripcion de jugadores en equipos (RF-01)")
class Prueba1Test extends PruebaBase {

    private Equipo azules;
    private Equipo rojos;

    @BeforeEach
    void crearEquipos() {
        azules = new Equipo("Azules", Deporte.FUTBOL, Categoria.MAYORES, 2);
        rojos = new Equipo("Rojos", Deporte.FUTBOL, Categoria.MAYORES, 10);
        club.getEquipos().add(azules);
        club.getEquipos().add(rojos);
    }

    private void inscribirJuanYPedro() {
        club.inscribirJugador(DatosBase.SOCIO2, "Azules", "Defensa", 4);
        club.inscribirJugador(DatosBase.SOCIO4, "Azules", "Delantero", 9);
    }

    @Test
    @DisplayName("Juan y Pedro se inscriben en Azules")
    void inscripcionesValidas() {
        assertDoesNotThrow(this::inscribirJuanYPedro);
        assertEquals(2, azules.getJugadores().size());
    }

    @Test
    @DisplayName("No se puede inscribir en un equipo lleno")
    void equipoLleno() {
        inscribirJuanYPedro();
        club.getUsuarios().add(new Socio(30, "Lucas", "Prueba", "lucas", "1234", LocalDate.of(2003, 2, 4)));
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirJugador(30, "Azules", "Defensa", 5));
        assertEquals(2, azules.getJugadores().size());
    }

    @Test
    @DisplayName("No se puede estar en dos equipos del mismo deporte")
    void dosEquiposMismoDeporte() {
        inscribirJuanYPedro();
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirJugador(DatosBase.SOCIO2, "Rojos", "Defensa", 4));
        assertEquals(0, rojos.getJugadores().size());
    }

    @Test
    @DisplayName("Una socia sin la edad no entra a la categoria Mayores")
    void edadInsuficiente() {
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirJugador(DatosBase.SOCIO3, "Rojos", "Defensa", 8));
    }

    @Test
    @DisplayName("Las inscripciones se conservan al guardar y cargar")
    void persistencia() {
        inscribirJuanYPedro();
        Club recuperado = guardarYRecargar();
        assertEquals(2, equipo(recuperado, "Azules").getJugadores().size());
        assertTrue(recuperado.getUsuarios().size() >= 8);
    }
}
