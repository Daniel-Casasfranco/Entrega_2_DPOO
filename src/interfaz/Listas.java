package interfaz;

import java.util.ArrayList;
import java.util.List;

import modelo.ArticuloTienda;
import modelo.Club;
import modelo.Deporte;
import modelo.Empleado;
import modelo.Entrenador;
import modelo.Equipo;
import modelo.Jugador;
import modelo.Producto;
import modelo.Socio;
import modelo.Usuario;

/** Consultas de apoyo que usan varios paneles para llenar combos y tablas. */


public final class Listas {

    public static final Deporte[] DEPORTES_CONJUNTO = {Deporte.FUTBOL, Deporte.BALONCESTO, Deporte.VOLEIBOL};
    public static final Deporte[] DISCIPLINAS_INDIVIDUALES = {Deporte.TENIS, Deporte.PADEL, Deporte.TENIS_DE_MESA};

    private Listas() {
    }

    public static List<Socio> socios(Club club) {
        List<Socio> resultado = new ArrayList<>();
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Socio) {
                resultado.add((Socio) u);
            }
        }
        return resultado;
    }

    public static List<Entrenador> entrenadores(Club club) {
        List<Entrenador> resultado = new ArrayList<>();
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Entrenador) {
                resultado.add((Entrenador) u);
            }
        }
        return resultado;
    }

    public static List<Empleado> empleados(Club club) {
        List<Empleado> resultado = new ArrayList<>();
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Empleado) {
                resultado.add((Empleado) u);
            }
        }
        return resultado;
    }

    public static List<Equipo> equiposDe(Club club, Deporte deporte) {
        List<Equipo> resultado = new ArrayList<>();
        for (Equipo e : club.getEquipos()) {
            if (deporte == null || e.getDeporte() == deporte) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    public static List<ArticuloTienda> articulos(Club club) {
        List<ArticuloTienda> resultado = new ArrayList<>();
        for (Producto p : club.getProductos()) {
            if (p instanceof ArticuloTienda) {
                resultado.add((ArticuloTienda) p);
            }
        }
        return resultado;
    }

    public static List<Producto> productosCafeteria(Club club) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : club.getProductos()) {
            if (!(p instanceof ArticuloTienda)) {
                resultado.add(p);
            }
        }
        return resultado;
    }
    
    
    

    /** Registro deportivo (Jugador) de un socio, o null si no juega en equipos. */
    
    public static Jugador jugadorDe(Socio socio) {
        if (socio instanceof Jugador) {
            return (Jugador) socio;
        }
        return socio.getJugadorDeportivo();
    }

    public static String nombre(Usuario u) {
        return u == null ? "" : u.getNombre() + " " + u.getApellido();
    }

    public static String nombreConCodigo(Usuario u) {
        return u == null ? "" : u.getCodigo() + " - " + nombre(u);
    }

    public static String texto(Enum<?> valor) {
        if (valor == null) {
            return "";
        }
        String t = valor.name().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }
}
