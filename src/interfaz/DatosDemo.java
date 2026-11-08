package interfaz;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import consola.DatosBase;
import consola.DatosTienda;
import modelo.ASCUN;
import modelo.ArticuloTienda;
import modelo.Asistencia;
import modelo.Categoria;
import modelo.Club;
import modelo.Competencia;
import modelo.Deporte;
import modelo.Entrenador;
import modelo.Equipo;
import modelo.InscripcionDisciplina;
import modelo.InscripcionEquipo;
import modelo.Instalacion;
import modelo.Jugador;
import modelo.Mensualidad;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.PartidoPractica;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.SesionEntrenamiento;
import modelo.Socio;
import modelo.TorneoDeLosCerros;
import modelo.Turno;
import modelo.Usuario;

/**
 * Datos de demostracion para probar la interfaz y ver las graficas con informacion:
 * equipos, competencias, partidos de las ultimas semanas, practicas individuales,
 * reservas, sesiones con asistencia, ventas y mensualidades. Todo se registra con
 * los metodos de {@link Club}, asi que respeta las reglas de negocio.
 */


// como este punto era muy repetitivo se pidio a la ia que hiciera los datos.


public final class DatosDemo {

    private static final String[] NOMBRES = {"Andres", "Camila", "Diego", "Valentina", "Santiago", "Isabella",
        "Mateo", "Mariana", "Samuel", "Daniela", "Nicolas", "Gabriela", "Sebastian", "Paula", "Tomas", "Laura",
        "Felipe", "Natalia", "Julian", "Carolina", "Esteban", "Juliana", "David", "Sara", "Alejandro", "Manuela",
        "Simon", "Antonia", "Emilio", "Lucia", "Martin", "Elena", "Pablo", "Ana", "Joaquin", "Sofia"};
    private static final String[] APELLIDOS = {"Gomez", "Rodriguez", "Martinez", "Lopez", "Garcia", "Hernandez",
        "Ramirez", "Torres", "Diaz", "Moreno", "Vargas", "Castro", "Rojas", "Ortiz", "Rincon", "Suarez"};
    private static final String[] POSICIONES_FUTBOL = {"Portero", "Defensa", "Defensa", "Defensa", "Medio",
        "Medio", "Medio", "Delantero", "Delantero"};
    private static final String[] POSICIONES_BALONCESTO = {"Base", "Escolta", "Alero", "Ala-pivot", "Pivot",
        "Alero", "Escolta"};

    private final Club club;
    private final Random azar = new Random(2026);
    private final LocalDate hoy = LocalDate.now();
    private int siguienteCodigo = 100;

    private DatosDemo(Club club) {
        this.club = club;
    }

    public static void cargar(Club club) {
        new DatosDemo(club).cargarTodo();
    }

    private void cargarTodo() {
        DatosBase.cargar(club);
        DatosTienda.cargar(club);
        club.getProductos().add(new ArticuloTienda("Gorra", 35000, "Ropa"));

        Entrenador diego = new Entrenador(5, "Diego", "Vargas", "diego", "1234", "DESC-DIEGO");
        club.getUsuarios().add(diego);

        instalaciones();
        Equipo halcones = equipo("Halcones", Deporte.FUTBOL, 9, POSICIONES_FUTBOL);
        Equipo condores = equipo("Condores", Deporte.FUTBOL, 9, POSICIONES_FUTBOL);
        Equipo tigres = equipo("Tigres", Deporte.BALONCESTO, 7, POSICIONES_BALONCESTO);
        Equipo pumas = equipo("Pumas", Deporte.BALONCESTO, 7, POSICIONES_BALONCESTO);

        Competencia cerros = new TorneoDeLosCerros("Copa de los Cerros", "Liga Bogota", String.valueOf(hoy.getYear()),
                Deporte.FUTBOL, Categoria.MAYORES, 7);
        Competencia ascun = new ASCUN("ASCUN Baloncesto", "ASCUN", String.valueOf(hoy.getYear()),
                Deporte.BALONCESTO, Categoria.MAYORES, 5);
        club.agregarCompetencia(cerros);
        club.agregarCompetencia(ascun);
        InscripcionEquipo insHalcones = club.inscribirEquipo(DatosBase.ENTRENADOR1, halcones, cerros,
                halcones.getJugadores());
        InscripcionEquipo insCondores = club.inscribirEquipo(DatosBase.ENTRENADOR1, condores, cerros,
                condores.getJugadores());
        InscripcionEquipo insTigres = club.inscribirEquipo(DatosBase.ENTRENADOR2, tigres, ascun,
                tigres.getJugadores());
        InscripcionEquipo insPumas = club.inscribirEquipo(DatosBase.ENTRENADOR2, pumas, ascun,
                pumas.getJugadores());

        String[] rivales = {"U. Nacional", "U. Javeriana", "U. del Rosario", "U. Externado", "U. de la Sabana"};
        for (int semana = 10; semana >= 1; semana--) {
            LocalDate sabado = hoy.minusWeeks(semana).with(DayOfWeek.SATURDAY);
            LocalDate domingo = sabado.plusDays(1);
            partidoInterno(halcones, condores, sabado, semana == 1);
            partidoInterno(tigres, pumas, sabado, false);
            if (semana % 2 == 0) {
                partidoCompetencia(insHalcones, rivales[semana % rivales.length], domingo);
                partidoCompetencia(insTigres, rivales[(semana + 1) % rivales.length], domingo);
            } else {
                partidoCompetencia(insCondores, rivales[(semana + 2) % rivales.length], domingo);
                partidoCompetencia(insPumas, rivales[(semana + 3) % rivales.length], domingo);
            }
        }

        disciplinasIndividuales();
        reservas();
        sesiones(halcones, DatosBase.ENTRENADOR1, "F-01", DayOfWeek.TUESDAY, LocalTime.of(17, 0));
        sesiones(condores, DatosBase.ENTRENADOR1, "F-01", DayOfWeek.THURSDAY, LocalTime.of(17, 0));
        sesiones(tigres, DatosBase.ENTRENADOR2, "B-01", DayOfWeek.WEDNESDAY, LocalTime.of(18, 0));
        sesiones(pumas, DatosBase.ENTRENADOR2, "B-01", DayOfWeek.FRIDAY, LocalTime.of(18, 0));
        turnos(diego);
        sugerencias();
        ventasYMensualidades();
    }

    // ------------------------------------------------------------------

    private void instalaciones() {
        agregarInstalacion(Deporte.FUTBOL, "F-01", "Cancha de futbol principal", false);
        agregarInstalacion(Deporte.BALONCESTO, "B-01", "Coliseo de baloncesto", true);
        agregarInstalacion(Deporte.VOLEIBOL, "V-01", "Cancha de voleibol", true);
        agregarInstalacion(Deporte.TENIS, "T-01", "Tenis 1", false);
        agregarInstalacion(Deporte.TENIS, "T-02", "Tenis 2", false);
        agregarInstalacion(Deporte.TENIS, "T-03", "Tenis 3", false);
        agregarInstalacion(Deporte.PADEL, "P-01", "Padel cubierto", true);
        agregarInstalacion(Deporte.PADEL, "P-02", "Padel exterior", false);
        agregarInstalacion(Deporte.TENIS_DE_MESA, "M-01", "Mesa 1", false);
        agregarInstalacion(Deporte.TENIS_DE_MESA, "M-02", "Mesa 2", false);
    }

    private void agregarInstalacion(Deporte d, String codigo, String nombre, boolean techada) {
        club.getInstalaciones().add(PanelInstalaciones.crear(d, codigo, nombre, techada));
    }

    private Instalacion instalacion(String codigo) {
        for (Instalacion i : club.getInstalaciones()) {
            if (i.getCodigo().equals(codigo)) {
                return i;
            }
        }
        return null;
    }

    private Socio nuevoSocio(int anioMin, int anioMax) {
        int codigo = siguienteCodigo++;
        String nombre = NOMBRES[codigo % NOMBRES.length];
        String apellido = APELLIDOS[(codigo * 7) % APELLIDOS.length];
        LocalDate nacimiento = LocalDate.of(anioMin + azar.nextInt(anioMax - anioMin + 1), 1 + azar.nextInt(12),
                1 + azar.nextInt(28));
        Socio s = new Socio(codigo, nombre, apellido, "s" + codigo, "1234", nacimiento);
        club.getUsuarios().add(s);
        return s;
    }

    private Equipo equipo(String nombre, Deporte deporte, int jugadores, String[] posiciones) {
        Equipo e = new Equipo(nombre, deporte, Categoria.MAYORES, 15);
        club.crearEquipo((modelo.Administrador) DatosBase.usuario(club, DatosBase.ADMIN), e);
        for (int i = 0; i < jugadores; i++) {
            Socio s = nuevoSocio(1996, 2005);
            club.inscribirJugador(s.getCodigo(), nombre, posiciones[i % posiciones.length], i + 1);
        }
        return e;
    }

    private void partidoInterno(Equipo local, Equipo visitante, LocalDate fecha, boolean conExpulsion) {
        boolean futbol = local.getDeporte() == Deporte.FUTBOL;
        int a = futbol ? azar.nextInt(4) : 55 + azar.nextInt(35);
        int b = futbol ? azar.nextInt(4) : 55 + azar.nextInt(35);
        if (!futbol && a == b) {
            a++;
        }
        Map<Jugador, Map<String, Integer>> stats = new HashMap<>();
        repartir(stats, local, a);
        repartir(stats, visitante, b);
        if (conExpulsion) {
            Jugador expulsado = visitante.getJugadores().get(2);
            stats.computeIfAbsent(expulsado, k -> new HashMap<>()).put("tarjetasRojas", 1);
        }
        intentar(() -> club.registrarPartidoInterno(DatosBase.ENTRENADOR1, local, visitante, fecha,
                golesOPuntos(stats, local), golesOPuntos(stats, visitante), stats));
    }

    private void partidoCompetencia(InscripcionEquipo ins, String rival, LocalDate fecha) {
        Equipo equipo = ins.getEquipo();
        boolean futbol = equipo.getDeporte() == Deporte.FUTBOL;
        int propio = futbol ? azar.nextInt(4) : 50 + azar.nextInt(40);
        int contrario = futbol ? azar.nextInt(3) : 50 + azar.nextInt(40);
        if (!futbol && propio == contrario) {
            propio++;
        }
        Map<Jugador, Map<String, Integer>> stats = new HashMap<>();
        repartir(stats, equipo, propio);
        intentar(() -> club.registrarPartidoCompeticion(DatosBase.usuario(club, DatosBase.ENTRENADOR1).getCodigo(),
                ins, rival, "Estadio " + rival, fecha, golesOPuntos(stats, equipo), contrario, stats));
    }

    /** Reparte goles (futbol) o puntos (baloncesto) entre los jugadores del equipo. */
    private void repartir(Map<Jugador, Map<String, Integer>> stats, Equipo equipo, int total) {
        List<Jugador> jugadores = equipo.getJugadores();
        boolean futbol = equipo.getDeporte() == Deporte.FUTBOL;
        jugadores.removeIf(Jugador::estaSuspendido);
        for (Jugador j : jugadores) {
            Map<String, Integer> m = new HashMap<>();
            if (futbol) {
                m.put("goles", 0);
                m.put("asistencias", 0);
                m.put("tarjetasAmarillas", azar.nextInt(8) == 0 ? 1 : 0);
            } else {
                m.put("puntos", 0);
                m.put("rebotes", azar.nextInt(9));
            }
            stats.put(j, m);
        }
        String clave = futbol ? "goles" : "puntos";
        int restante = total;
        while (restante > 0) {
            // Los delanteros y los primeros jugadores anotan mas
            int indice = futbol ? Math.min(jugadores.size() - 1, 5 + azar.nextInt(4)) : azar.nextInt(jugadores.size());
            if (futbol && azar.nextInt(4) == 0) {
                indice = azar.nextInt(jugadores.size());
            }
            int valor = futbol ? 1 : Math.min(restante, 1 + azar.nextInt(3));
            Map<String, Integer> m = stats.get(jugadores.get(indice));
            m.put(clave, m.get(clave) + valor);
            if (futbol) {
                Map<String, Integer> asistente = stats.get(jugadores.get(azar.nextInt(jugadores.size())));
                asistente.put("asistencias", asistente.get("asistencias") + 1);
            }
            restante -= valor;
        }
    }

    private static int golesOPuntos(Map<Jugador, Map<String, Integer>> stats, Equipo equipo) {
        int total = 0;
        for (Jugador j : equipo.getJugadores()) {
            Map<String, Integer> m = stats.get(j);
            if (m != null) {
                total += m.getOrDefault("goles", 0) + m.getOrDefault("puntos", 0);
            }
        }
        return total;
    }

    // ------------------------------------------------------------------

    private void disciplinasIndividuales() {
        Socio sofia = (Socio) DatosBase.usuario(club, DatosBase.SOCIO1);
        Socio juan = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        Socio maria = (Socio) DatosBase.usuario(club, DatosBase.SOCIO3);
        Socio pedro = (Socio) DatosBase.usuario(club, DatosBase.SOCIO4);
        Socio andres = nuevoSocio(1995, 2004);
        Socio lucia = nuevoSocio(1995, 2004);

        List<InscripcionDisciplina> tenis = Arrays.asList(
                new InscripcionDisciplina(sofia, Deporte.TENIS, Nivel.INTERMEDIO),
                new InscripcionDisciplina(juan, Deporte.TENIS, Nivel.INTERMEDIO),
                new InscripcionDisciplina(pedro, Deporte.TENIS, Nivel.AVANZADO),
                new InscripcionDisciplina(andres, Deporte.TENIS, Nivel.AVANZADO),
                new InscripcionDisciplina(lucia, Deporte.TENIS, Nivel.INTERMEDIO));
        List<InscripcionDisciplina> mesa = Arrays.asList(
                new InscripcionDisciplina(juan, Deporte.TENIS_DE_MESA, Nivel.INTERMEDIO),
                new InscripcionDisciplina(maria, Deporte.TENIS_DE_MESA, Nivel.PRINCIPIANTE),
                new InscripcionDisciplina(sofia, Deporte.TENIS_DE_MESA, Nivel.INTERMEDIO));
        List<InscripcionDisciplina> padel = Arrays.asList(
                new InscripcionDisciplina(sofia, Deporte.PADEL, Nivel.INTERMEDIO),
                new InscripcionDisciplina(juan, Deporte.PADEL, Nivel.INTERMEDIO),
                new InscripcionDisciplina(pedro, Deporte.PADEL, Nivel.AVANZADO),
                new InscripcionDisciplina(lucia, Deporte.PADEL, Nivel.INTERMEDIO));

        // La "fuerza" hace que algunos ganen mas seguido, para que el ranking tenga tendencias
        Map<InscripcionDisciplina, Double> fuerza = new HashMap<>();
        double[] fuerzasTenis = {0.62, 0.45, 0.7, 0.55, 0.4};
        for (int i = 0; i < tenis.size(); i++) {
            fuerza.put(tenis.get(i), fuerzasTenis[i]);
        }
        for (InscripcionDisciplina i : mesa) {
            fuerza.put(i, 0.5 + 0.1 * mesa.indexOf(i));
        }

        for (int semana = 12; semana >= 1; semana--) {
            LocalDate fecha = hoy.minusWeeks(semana).with(DayOfWeek.WEDNESDAY);
            for (int k = 0; k < 2; k++) {
                InscripcionDisciplina a = tenis.get(azar.nextInt(tenis.size()));
                InscripcionDisciplina b = tenis.get(azar.nextInt(tenis.size()));
                if (a != b) {
                    jugarPractica(Arrays.asList(a, b), Modalidad.SENCILLOS, fecha.plusDays(k), fuerza, false);
                }
            }
            InscripcionDisciplina m1 = mesa.get(semana % mesa.size());
            InscripcionDisciplina m2 = mesa.get((semana + 1) % mesa.size());
            jugarPractica(Arrays.asList(m1, m2), Modalidad.SENCILLOS, fecha.plusDays(2), fuerza, true);
            if (semana % 3 == 0) {
                List<InscripcionDisciplina> orden = new ArrayList<>(padel);
                java.util.Collections.shuffle(orden, azar);
                jugarPractica(orden, Modalidad.DOBLES, fecha.plusDays(3), fuerza, false);
            }
        }
        // Una practica pendiente para que la socia Sofia pueda registrar un resultado
        intentar(() -> club.programarPractica(Arrays.asList(tenis.get(0), tenis.get(1)), Modalidad.SENCILLOS, hoy));
    }

    private void jugarPractica(List<InscripcionDisciplina> participantes, Modalidad modalidad, LocalDate fecha,
            Map<InscripcionDisciplina, Double> fuerza, boolean esMesa) {
        try {
            PartidoPractica p = club.programarPractica(participantes, modalidad, fecha);
            int mitad = participantes.size() / 2;
            double f1 = fuerza.getOrDefault(participantes.get(0), 0.5);
            double f2 = fuerza.getOrDefault(participantes.get(mitad), 0.5);
            boolean ganaPrimera = azar.nextDouble() < f1 / (f1 + f2);
            Map<InscripcionDisciplina, Map<String, Integer>> datos = new HashMap<>();
            for (int i = 0; i < participantes.size(); i++) {
                boolean gana = (i < mitad) == ganaPrimera;
                int sets = gana ? (esMesa ? 3 : 2) : azar.nextInt(esMesa ? 3 : 2);
                Map<String, Integer> m = new HashMap<>();
                m.put("sets", sets);
                if (esMesa) {
                    m.put("puntosTotales", sets * 11 + azar.nextInt(15));
                } else {
                    m.put("games", sets * 6 + azar.nextInt(4));
                    m.put("aces", azar.nextInt(6));
                    m.put("erroresNoForzados", azar.nextInt(12));
                }
                datos.put(participantes.get(i), m);
            }
            // En dobles cada pareja debe tener los mismos sets: se copia el del primero de cada pareja
            for (int i = 0; i < participantes.size(); i++) {
                int jefe = i < mitad ? 0 : mitad;
                datos.get(participantes.get(i)).put("sets", datos.get(participantes.get(jefe)).get("sets"));
            }
            club.registrarPractica(p, datos);
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            // Combinacion no permitida por las reglas (por ejemplo, niveles incompatibles): se omite
        }
    }

    // ------------------------------------------------------------------

    private void reservas() {
        List<Socio> socios = Listas.socios(club);
        String[] canchas = {"T-01", "T-02", "T-03", "P-02", "M-01", "M-02", "P-01"};
        for (int dia = -42; dia <= 14; dia++) {
            LocalDate fecha = hoy.plusDays(dia);
            boolean finDeSemana = fecha.getDayOfWeek().getValue() >= 6;
            int cantidad = (finDeSemana ? 5 : 2) + azar.nextInt(3);
            for (int k = 0; k < cantidad; k++) {
                // Mas demanda en la manana del fin de semana y al final de la tarde entre semana
                int hora = finDeSemana ? 7 + azar.nextInt(6) : (azar.nextInt(3) == 0 ? 6 + azar.nextInt(3)
                        : 17 + azar.nextInt(4));
                String codigo = canchas[azar.nextInt(canchas.length)];
                if (codigo.equals("P-01") && dia == 0) {
                    continue; // evita bloquear las compras de bebidas calientes de hoy
                }
                Instalacion inst = instalacion(codigo);
                Modalidad modalidad = inst.getDisciplina() == Deporte.PADEL || azar.nextInt(3) == 0
                        ? Modalidad.DOBLES : Modalidad.SENCILLOS;
                Socio socio = socios.get(azar.nextInt(socios.size()));
                intentar(() -> club.reservar(socio.getCodigo(), inst, fecha, LocalTime.of(hora, 0),
                        LocalTime.of(hora + 1, 0), modalidad == Modalidad.DOBLES ? 4 : 2, modalidad));
            }
        }
    }

    private void sesiones(Equipo equipo, int codigoEntrenador, String cancha, DayOfWeek dia, LocalTime hora) {
        List<Socio> convocados = new ArrayList<>();
        for (Jugador j : equipo.getJugadores()) {
            Usuario u = DatosBase.usuario(club, j.getCodigo());
            if (u instanceof Socio) {
                convocados.add((Socio) u);
            }
        }
        for (int semana = 9; semana >= -1; semana--) {
            LocalDate fecha = hoy.minusWeeks(semana).with(dia);
            try {
                SesionEntrenamiento s = club.organizarSesion(codigoEntrenador, instalacion(cancha), fecha, hora, 90,
                        convocados);
                s.setEquipo(equipo);
                if (!s.haTerminado()) {
                    continue;
                }
                // La asistencia mejora un poco con las semanas
                double probAsistir = 0.55 + (9 - semana) * 0.04;
                for (Socio socio : convocados) {
                    double r = azar.nextDouble();
                    Asistencia a = r < probAsistir ? Asistencia.ASISTIO
                            : r < probAsistir + 0.2 ? Asistencia.LLEGO_TARDE : Asistencia.FALTO;
                    int nota = a == Asistencia.FALTO ? 0 : 2 + azar.nextInt(4);
                    club.registrarAsistencia(codigoEntrenador, s, socio, a, nota,
                            a == Asistencia.FALTO ? "" : nota >= 4 ? "Buen trabajo" : "Debe mejorar");
                }
            } catch (ReglaNegocioException e) {
                // Horario ocupado: se omite esa sesion
            }
        }
    }

    private void turnos(Entrenador diego) {
        String[] dias = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"};
        for (String d : dias) {
            new Turno((Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR1), d, LocalTime.of(8, 0),
                    LocalTime.of(14, 0));
            new Turno((Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR2), d, LocalTime.of(8, 0),
                    LocalTime.of(14, 0));
            new Turno(diego, d, LocalTime.of(8, 0), LocalTime.of(14, 0));
            new Turno((modelo.Fisioterapeuta) DatosBase.usuario(club, DatosBase.FISIO), d, LocalTime.of(8, 0),
                    LocalTime.of(14, 0));
        }
        Entrenador laura = (Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR2);
        club.solicitarCambioTurno(laura.getCodigo(), laura.getTurnos().get(4), "Curso de actualizacion",
                "SABADO", LocalTime.of(8, 0), LocalTime.of(14, 0));
    }

    private void sugerencias() {
        club.sugerirProducto(DatosBase.ENTRENADOR1, "Conos de entrenamiento", "Set de 20 conos para ejercicios");
        club.sugerirProducto(DatosBase.FISIO, "Bebida hidratante", "Bebida isotonica para despues de entrenar");
    }

    private void ventasYMensualidades() {
        List<Socio> socios = Listas.socios(club);
        String periodo = hoy.getYear() + "-" + hoy.getMonthValue();
        String periodoAnterior = hoy.minusMonths(1).getYear() + "-" + hoy.minusMonths(1).getMonthValue();
        for (Socio s : socios) {
            new Mensualidad(s, periodoAnterior, 150000, hoy.minusMonths(1).minusDays(azar.nextInt(5)));
            if (azar.nextInt(3) > 0) {
                new Mensualidad(s, periodo, 150000, hoy.minusDays(azar.nextInt(5)));
            }
        }

        List<ArticuloTienda> articulos = Listas.articulos(club);
        List<Producto> cafeteria = Listas.productosCafeteria(club);
        for (int k = 0; k < 12; k++) {
            Socio s = socios.get(azar.nextInt(socios.size()));
            Map<ArticuloTienda, Integer> canasta = new LinkedHashMap<>();
            canasta.put(articulos.get(azar.nextInt(articulos.size())), 1);
            intentar(() -> club.comprarTienda(s.getCodigo(), canasta, null));

            Map<Producto, Integer> pedido = new LinkedHashMap<>();
            pedido.put(cafeteria.get(azar.nextInt(cafeteria.size())), 1 + azar.nextInt(2));
            intentar(() -> club.comprarCafeteria(s.getCodigo(), pedido, 0.1, 0));
        }
        intentar(() -> club.comprarTienda(DatosBase.ENTRENADOR1, canasta(articulos.get(1)), "DESC-CARLOS"));
    }

    private static Map<ArticuloTienda, Integer> canasta(ArticuloTienda a) {
        Map<ArticuloTienda, Integer> m = new LinkedHashMap<>();
        m.put(a, 1);
        return m;
    }

    private static void intentar(Runnable accion) {
        try {
            accion.run();
        } catch (ReglaNegocioException | IllegalArgumentException | IllegalStateException e) {
            // Los datos de demostracion omiten las operaciones que las reglas rechazan
        }
    }
}
