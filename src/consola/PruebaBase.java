package consola;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import consola.DatosBase;
import modelo.Club;
import modelo.Equipo;
import modelo.Usuario;
import persistencia.Persistencia;

/**
 * Base comun de las pruebas JUnit. Cada prueba recibe un Club nuevo con los
 * datos base, guardado en una carpeta temporal que JUnit borra al terminar.
 */
public abstract class PruebaBase {

    @TempDir
    Path carpeta;

    protected Club club;

    @BeforeEach
    void crearClub() {
        club = new Persistencia(carpeta.toString()).cargar();
        DatosBase.cargar(club);
    }

    /** Guarda el club y lo vuelve a leer desde el archivo, como otra ejecucion. */
    protected Club guardarYRecargar() {
        club.guardar();
        return new Persistencia(carpeta.toString()).cargar();
    }

    protected <T extends Usuario> T usuario(int codigo) {
        @SuppressWarnings("unchecked")
        T u = (T) DatosBase.usuario(club, codigo);
        return u;
    }

    protected static Equipo equipo(Club club, String nombre) {
        for (Equipo e : club.getEquipos()) {
            if (nombre.equals(e.getNombre())) {
                return e;
            }
        }
        return null;
    }
}