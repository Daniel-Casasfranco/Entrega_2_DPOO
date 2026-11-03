package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import modelo.*;

@DisplayName("Prueba 5: sesiones de entrenamiento, asistencia y evaluacion")
class Prueba5Test extends PruebaBase {

    private Socio sofia;
    private Socio juan;
    private Socio maria;
    private Socio pedro;
    private CanchaTenis cancha;
    private LocalDate ayer;
    private LocalDate manana;
    private SesionEntrenamiento sesionPasada;
    private SesionEntrenamiento sesionFutura;

    @BeforeEach
    void organizarSesiones() {
        sofia = usuario(DatosBase.SOCIO1);
        juan = usuario(DatosBase.SOCIO2);
        maria = usuario(DatosBase.SOCIO3);
        pedro = usuario(DatosBase.SOCIO4);
        cancha = new CanchaTenis("T-01", "Cancha de tenis 1", false);
        club.getInstalaciones().add(cancha);
        ayer = LocalDate.now().minusDays(1);
        manana = LocalDate.now().plusDays(1);

        sesionPasada = club.organizarSesion(DatosBase.ENTRENADOR1, cancha, ayer,
                LocalTime.of(15, 0), 60, Arrays.asList(sofia, juan, maria));
        sesionFutura = club.organizarSesion(DatosBase.ENTRENADOR1, cancha, manana,
                LocalTime.of(15, 0), 60, Arrays.asList(sofia, juan));
    }

    @Test
    @DisplayName("Carlos organiza dos sesiones validas")
    void organizarSesionesValidas() {
        assertNotNull(sesionPasada);
        assertNotNull(sesionFutura);
        assertEquals(2, club.getSesiones().size());
    }

    @Test
    @DisplayName("No se puede organizar una sesion que se cruza en la misma cancha")
    void sesionCruzada() {
        assertThrows(ReglaNegocioException.class,
                () -> club.organizarSesion(DatosBase.ENTRENADOR2, cancha, ayer,
                        LocalTime.of(15, 30), 60, Collections.singletonList(pedro)));
    }

    @Test
    @DisplayName("Un socio no puede organizar sesiones")
    void socioNoOrganiza() {
        assertThrows(ReglaNegocioException.class,
                () -> club.organizarSesion(DatosBase.SOCIO1, cancha, manana,
                        LocalTime.of(18, 0), 60, Collections.singletonList(pedro)));
    }

    @Test
    @DisplayName("No se registra asistencia en una sesion que no ha terminado")
    void asistenciaSesionFutura() {
        assertThrows(ReglaNegocioException.class,
                () -> club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionFutura, sofia,
                        Asistencia.ASISTIO, 4, ""));
    }

    @Test
    @DisplayName("Solo el entrenador que dirige la sesion registra asistencia")
    void otroEntrenadorNoRegistra() {
        assertThrows(ReglaNegocioException.class,
                () -> club.registrarAsistencia(DatosBase.ENTRENADOR2, sesionPasada, sofia,
                        Asistencia.ASISTIO, 4, ""));
    }

    private void registrarAsistencias() {
        club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionPasada, sofia,
                Asistencia.ASISTIO, 5, "Excelente tecnica");
        club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionPasada, juan,
                Asistencia.LLEGO_TARDE, 3, "Debe mejorar la puntualidad");
        club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionPasada, maria,
                Asistencia.FALTO, 0, null);
    }

    @Test
    @DisplayName("Carlos registra la asistencia y la nota de la sesion de ayer")
    void registrarAsistenciaValida() {
        assertDoesNotThrow(this::registrarAsistencias);
        Convocatoria c = club.consultarAsistencia(sofia, sesionPasada);
        assertEquals(Asistencia.ASISTIO, c.getAsistencia());
        assertEquals(5, c.getNota());
    }

    @Test
    @DisplayName("Se rechaza una nota fuera de rango")
    void notaInvalida() {
        assertThrows(ReglaNegocioException.class,
                () -> club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionPasada, sofia,
                        Asistencia.ASISTIO, 9, ""));
    }

    @Test
    @DisplayName("Sofia consulta sus convocatorias; Pedro no tiene ninguna")
    void consultarConvocatorias() {
        assertEquals(2, club.consultarConvocatorias(sofia).size());
        assertThrows(ReglaNegocioException.class, () -> club.consultarConvocatorias(pedro));
    }

    @Test
    @DisplayName("No se consulta la asistencia de una sesion en proceso")
    void consultarAsistenciaSesionFutura() {
        assertThrows(ReglaNegocioException.class,
                () -> club.consultarAsistencia(sofia, sesionFutura));
    }

    @Test
    @DisplayName("El fisioterapeuta consulta el desempeno; un socio no puede")
    void consultarDesempeno() {
        registrarAsistencias();
        assertFalse(club.consultarDesempenoJugador(DatosBase.FISIO, sofia).isEmpty());
        assertThrows(ReglaNegocioException.class,
                () -> club.consultarDesempenoJugador(DatosBase.SOCIO2, sofia));
    }

    @Test
    @DisplayName("Las sesiones y asistencias se conservan al guardar y cargar")
    void persistencia() {
        registrarAsistencias();
        Club recuperado = guardarYRecargar();
        assertEquals(2, recuperado.getSesiones().size());
        int registradas = 0;
        for (SesionEntrenamiento s : recuperado.getSesiones()) {
            for (Convocatoria c : s.getConvocatorias()) {
                if (c.getAsistencia() != null) {
                    registradas++;
                }
            }
        }
        assertEquals(3, registradas);
    }
}
