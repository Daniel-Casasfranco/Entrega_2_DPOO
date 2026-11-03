package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import modelo.*;

@DisplayName("Prueba 8: cambios de turno")
class Prueba8Test extends PruebaBase {

    private Turno turnoCarlos;
    private Turno turnoLaura;
    private Turno turnoMateo;

    @BeforeEach
    void crearTurnos() {
        Entrenador carlos = usuario(DatosBase.ENTRENADOR1);
        Entrenador laura = usuario(DatosBase.ENTRENADOR2);
        Fisioterapeuta mateo = usuario(DatosBase.FISIO);
        Entrenador diego = new Entrenador(5, "Diego", "Vargas", "diego", "1234", "DESC-DIEGO");
        club.getUsuarios().add(diego);

        // Todos trabajan el lunes de 8:00 a 12:00
        turnoCarlos = new Turno(carlos, "LUNES", LocalTime.of(8, 0), LocalTime.of(12, 0));
        turnoLaura = new Turno(laura, "LUNES", LocalTime.of(8, 0), LocalTime.of(12, 0));
        new Turno(diego, "LUNES", LocalTime.of(8, 0), LocalTime.of(12, 0));
        turnoMateo = new Turno(mateo, "LUNES", LocalTime.of(8, 0), LocalTime.of(12, 0));
    }

    private SolicitudCambioTurno solicitar(int empleado, Turno turno, String dia) {
        return club.solicitarCambioTurno(empleado, turno, "Motivo", dia,
                LocalTime.of(8, 0), LocalTime.of(12, 0));
    }

    private SolicitudCambioTurno moverCarlosAlMartes() {
        SolicitudCambioTurno s = solicitar(DatosBase.ENTRENADOR1, turnoCarlos, "MARTES");
        club.aprobarCambio(s);
        return s;
    }

    @Test
    @DisplayName("Se aprueba el cambio si quedan al menos dos entrenadores")
    void aprobarCambioValido() {
        SolicitudCambioTurno s = moverCarlosAlMartes();
        assertEquals(EstadoSolicitud.APROBADA, s.getEstado());
        assertEquals("MARTES", turnoCarlos.getDiaSemana());
    }

    @Test
    @DisplayName("Se rechaza el cambio si quedaria un solo entrenador")
    void minimoEntrenadores() {
        moverCarlosAlMartes();
        SolicitudCambioTurno s = solicitar(DatosBase.ENTRENADOR2, turnoLaura, "MIERCOLES");
        assertThrows(ReglaNegocioException.class, () -> club.aprobarCambio(s));
        assertEquals("LUNES", turnoLaura.getDiaSemana());
    }

    @Test
    @DisplayName("Se rechaza el cambio si no quedaria ningun fisioterapeuta")
    void minimoFisioterapeutas() {
        SolicitudCambioTurno s = solicitar(DatosBase.FISIO, turnoMateo, "JUEVES");
        assertThrows(ReglaNegocioException.class, () -> club.aprobarCambio(s));
        assertEquals("LUNES", turnoMateo.getDiaSemana());
    }

    @Test
    @DisplayName("Una solicitud ya evaluada no se vuelve a aprobar")
    void solicitudYaEvaluada() {
        SolicitudCambioTurno s = moverCarlosAlMartes();
        assertThrows(ReglaNegocioException.class, () -> club.aprobarCambio(s));
    }

    @Test
    @DisplayName("Un empleado no puede pedir el cambio del turno de otro")
    void turnoAjeno() {
        assertThrows(ReglaNegocioException.class,
                () -> solicitar(DatosBase.ENTRENADOR1, turnoLaura, "VIERNES"));
    }

    @Test
    @DisplayName("Solo el administrador consulta los turnos")
    void consultarTurnos() {
        assertThrows(ReglaNegocioException.class, () -> club.consultarTurnos(DatosBase.SOCIO1));
        assertEquals(4, club.consultarTurnos(DatosBase.ADMIN).size());
    }

    @Test
    @DisplayName("Turnos y solicitudes se conservan al guardar y cargar")
    void persistencia() {
        moverCarlosAlMartes();
        Club recuperado = guardarYRecargar();
        assertEquals(1, recuperado.getSolicitudes().size());
        assertEquals(EstadoSolicitud.APROBADA, recuperado.getSolicitudes().get(0).getEstado());
        Entrenador carlos = (Entrenador) DatosBase.usuario(recuperado, DatosBase.ENTRENADOR1);
        assertEquals("MARTES", carlos.getTurnos().get(0).getDiaSemana());
    }
}
