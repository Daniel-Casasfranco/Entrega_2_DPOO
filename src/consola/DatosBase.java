package consola;

import java.time.LocalDate;

import modelo.Administrador;
import modelo.Club;
import modelo.Deporte;
import modelo.Entrenador;
import modelo.Fisioterapeuta;
import modelo.ReglaDeporte;
import modelo.Socio;
import modelo.Usuario;

public class DatosBase {

    public static final int ADMIN = 1;
    public static final int ENTRENADOR1 = 2;
    public static final int ENTRENADOR2 = 3;
    public static final int FISIO = 4;
    public static final int SOCIO1 = 11;
    public static final int SOCIO2 = 12;
    public static final int SOCIO3 = 13;
    public static final int SOCIO4 = 14;

    public static void cargar(Club club) {
        club.getReglas().add(new ReglaDeporte(Deporte.FUTBOL, 7, true, 3, 1, 0, 1));
        club.getReglas().add(new ReglaDeporte(Deporte.BALONCESTO, 5, false, 2, 0, 0, 1));
        club.getReglas().add(new ReglaDeporte(Deporte.VOLEIBOL, 6, false, 2, 0, 0, 1));
        club.getReglas().add(new ReglaDeporte(Deporte.TENIS, 2, false, 0, 0, 0, 2));
        club.getReglas().add(new ReglaDeporte(Deporte.PADEL, 2, false, 0, 0, 0, 2));
        club.getReglas().add(new ReglaDeporte(Deporte.TENIS_DE_MESA, 2, false, 0, 0, 0, 2));

        club.getUsuarios().add(new Administrador(ADMIN, "Ana", "Admin", "admin", "admin123"));
        club.getUsuarios().add(new Entrenador(ENTRENADOR1, "Carlos", "Mendez", "carlos", "1234", "DESC-CARLOS"));
        club.getUsuarios().add(new Entrenador(ENTRENADOR2, "Laura", "Rojas", "laura", "1234", "DESC-LAURA"));
        club.getUsuarios().add(new Fisioterapeuta(FISIO, "Mateo", "Gil", "mateo", "1234", "DESC-MATEO"));
        club.getUsuarios().add(new Socio(SOCIO1, "Sofia", "Perez", "sofia", "1234", LocalDate.of(2010, 5, 10)));
        club.getUsuarios().add(new Socio(SOCIO2, "Juan", "Torres", "juan", "1234", LocalDate.of(2005, 3, 2)));
        club.getUsuarios().add(new Socio(SOCIO3, "Maria", "Lopez", "maria", "1234", LocalDate.of(2013, 7, 21)));
        club.getUsuarios().add(new Socio(SOCIO4, "Pedro", "Ruiz", "pedro", "1234", LocalDate.of(2000, 1, 15)));

        club.guardar();
    }

    public static Usuario usuario(Club club, int codigo) {
        for (Usuario u : club.getUsuarios()) {
            if (u.getCodigo() == codigo) {
                return u;
            }
        }
        return null;
    }
}
