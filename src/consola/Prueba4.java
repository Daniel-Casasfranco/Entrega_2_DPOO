package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.CanchaPadel;
import modelo.CanchaTenis;
import modelo.Club;
import modelo.Deporte;
import modelo.MesaTenisDeMesa;
import modelo.Modalidad;
import modelo.ReglaNegocioException;
import modelo.Reserva;

@DisplayName("Prueba 4: reservas, disponibilidad y cupos por disciplina")
class Prueba4Test extends PruebaBase {

    private CanchaTenis tenis1;
    private CanchaTenis tenis2;
    private CanchaTenis tenis3;
    private CanchaPadel padel;
    private MesaTenisDeMesa mesa;
    private LocalDate fecha;

    @BeforeEach
    void crearInstalacionesYReservas() {
        tenis1 = new CanchaTenis("T-01", "Tenis 1", false);
        tenis2 = new CanchaTenis("T-02", "Tenis 2", false);
        tenis3 = new CanchaTenis("T-03", "Tenis 3", false);
        padel = new CanchaPadel("P-01", "Padel", false);
        mesa = new MesaTenisDeMesa("M-01", "Tenis de mesa", false);
        club.getInstalaciones().add(tenis1);
        club.getInstalaciones().add(tenis2);
        club.getInstalaciones().add(tenis3);
        club.getInstalaciones().add(padel);
        club.getInstalaciones().add(mesa);
        fecha = LocalDate.now().plusDays(2);

        // Dos canchas de tenis distintas con horarios que se cruzan: permitido
        club.reservar(DatosBase.SOCIO1, tenis1, fecha, LocalTime.of(9, 0),
                LocalTime.of(10, 0), 2, Modalidad.SENCILLOS);
        club.reservar(DatosBase.SOCIO2, tenis2, fecha, LocalTime.of(9, 30),
                LocalTime.of(10, 30), 4, Modalidad.DOBLES);
    }

    @Test
    @DisplayName("Se permiten dos canchas de tenis distintas al mismo tiempo")
    void reservasValidas() {
        assertEquals(2, club.getReservas().size());
    }

    @Test
    @DisplayName("No se puede reservar la misma cancha en un horario ocupado")
    void mismaCanchaOcupada() {
        assertThrows(ReglaNegocioException.class,
                () -> club.reservar(DatosBase.SOCIO3, tenis1, fecha, LocalTime.of(9, 30),
                        LocalTime.of(10, 15), 2, Modalidad.SENCILLOS));
    }

    @Test
    @DisplayName("No se supera el maximo simultaneo de canchas de tenis")
    void maximoSimultaneoTenis() {
        assertThrows(ReglaNegocioException.class,
                () -> club.reservar(DatosBase.SOCIO3, tenis3, fecha, LocalTime.of(9, 45),
                        LocalTime.of(10, 15), 2, Modalidad.SENCILLOS));
    }

    @Test
    @DisplayName("El padel no admite sencillos")
    void padelSinSencillos() {
        assertThrows(ReglaNegocioException.class,
                () -> club.reservar(DatosBase.SOCIO3, padel, fecha, LocalTime.of(9, 0),
                        LocalTime.of(10, 0), 2, Modalidad.SENCILLOS));
    }

    private void reservasRestantes() {
        club.reservar(DatosBase.SOCIO3, tenis3, fecha, LocalTime.of(10, 30),
                LocalTime.of(11, 0), 2, Modalidad.SENCILLOS);
        club.reservar(DatosBase.SOCIO4, padel, fecha, LocalTime.of(9, 30),
                LocalTime.of(10, 30), 4, Modalidad.DOBLES);
        club.reservar(DatosBase.SOCIO4, mesa, fecha, LocalTime.of(9, 30),
                LocalTime.of(10, 30), 2, Modalidad.SENCILLOS);
    }

    @Test
    @DisplayName("Al liberarse el cupo y en otras disciplinas si se puede reservar")
    void liberacionDeCupos() {
        assertDoesNotThrow(this::reservasRestantes);
        assertEquals(5, club.getReservas().size());
    }

    @Test
    @DisplayName("Instalaciones y reservas se conservan al guardar y cargar")
    void persistencia() {
        reservasRestantes();
        Club recuperado = guardarYRecargar();
        assertEquals(5, recuperado.getInstalaciones().size());
        assertEquals(5, recuperado.getReservas().size());

        int tenis = 0, padelCount = 0, mesaCount = 0;
        for (Reserva r : recuperado.getReservas()) {
            assertTrue(recuperado.getInstalaciones().contains(r.getInstalacion()));
            Deporte d = r.getInstalacion().getDisciplina();
            if (d == Deporte.TENIS) tenis++;
            else if (d == Deporte.PADEL) padelCount++;
            else if (d == Deporte.TENIS_DE_MESA) mesaCount++;
        }
        assertEquals(3, tenis);
        assertEquals(1, padelCount);
        assertEquals(1, mesaCount);
    }
}
