package consola;


import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.Administrador;
import modelo.CanchaTenis;
import modelo.Club;
import modelo.Modalidad;
import modelo.ReglaNegocioException;
import modelo.Usuario;

@DisplayName("Prueba 10: inicio de sesion, perfil y restricciones del administrador")
class Prueba10Test extends PruebaBase {

    @Test
    @DisplayName("Ana inicia sesion con usuario y clave correctos")
    void inicioSesionCorrecto() {
        assertInstanceOf(Administrador.class, club.iniciarSesion("admin", "admin123"));
    }

    @Test
    @DisplayName("Una clave incorrecta no inicia sesion")
    void inicioSesionIncorrecto() {
        assertNull(club.iniciarSesion("admin", "incorrecta"));
    }

    @Test
    @DisplayName("Se consulta el perfil del usuario")
    void consultarPerfil() {
        Usuario ana = club.iniciarSesion("admin", "admin123");
        String perfil = club.consultarPerfil(ana);
        assertNotNull(perfil);
        assertFalse(perfil.isEmpty());
    }

    @Test
    @DisplayName("El administrador no puede reservar canchas para uso personal")
    void administradorNoReserva() {
        CanchaTenis cancha = new CanchaTenis("T-01", "Cancha de tenis 1", false);
        club.getInstalaciones().add(cancha);
        assertThrows(ReglaNegocioException.class,
                () -> club.reservar(DatosBase.ADMIN, cancha, LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0), LocalTime.of(10, 0), 2, Modalidad.SENCILLOS));
    }

    @Test
    @DisplayName("El administrador no puede inscribirse como jugador")
    void administradorNoJuega() {
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirJugador(DatosBase.ADMIN, "Azules", "Defensa", 4));
    }

    @Test
    @DisplayName("Usuarios y autenticacion se conservan al guardar y cargar")
    void persistencia() {
        Club recuperado = guardarYRecargar();
        assertTrue(recuperado.getUsuarios().size() >= 8);
        assertInstanceOf(Administrador.class, recuperado.iniciarSesion("admin", "admin123"));
        assertNull(recuperado.iniciarSesion("admin", "incorrecta"));
    }
}
