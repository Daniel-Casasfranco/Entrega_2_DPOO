package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import interfaz.DatosDemo;
import graficas.DatosGraficas;
import modelo.*;
import persistencia.Persistencia;

@DisplayName("Graficas: calculo de los datos que se visualizan")
class PruebaGraficasTest extends PruebaBase {

    private Equipo azules;
    private Equipo rojos;

    @BeforeEach
    void crearEquipos() {
        azules = crearEquipo("Azules", 100);
        rojos = crearEquipo("Rojos", 200);
    }

    private Equipo crearEquipo(String nombre, int primerCodigo) {
        Equipo equipo = new Equipo(nombre, Deporte.FUTBOL, Categoria.MAYORES, 15);
        club.getEquipos().add(equipo);
        for (int i = 0; i < 7; i++) {
            int codigo = primerCodigo + i;
            club.getUsuarios().add(new Socio(codigo, "J" + codigo, "Prueba", "s" + codigo, "1234",
                    LocalDate.of(2000, 1, 1)));
            club.inscribirJugador(codigo, nombre, "Defensa", i + 1);
        }
        return equipo;
    }

    private Map<Jugador, Map<String, Integer>> goles(Jugador jugador, int goles) {
        Map<String, Integer> valores = new HashMap<>();
        valores.put("goles", goles);
        Map<Jugador, Map<String, Integer>> m = new HashMap<>();
        m.put(jugador, valores);
        return m;
    }

    @Test
    @DisplayName("Pastel: victorias, empates y derrotas de un equipo")
    void resultadosEquipo() {
        LocalDate hoy = LocalDate.now();
        club.registrarPartidoInterno(DatosBase.ENTRENADOR1, azules, rojos, hoy, 2, 1, null);
        club.registrarPartidoInterno(DatosBase.ENTRENADOR1, azules, rojos, hoy, 1, 1, null);
        club.registrarPartidoInterno(DatosBase.ENTRENADOR1, rojos, azules, hoy, 3, 0, null);

        Map<String, Double> r = DatosGraficas.resultadosEquipo(club, azules, DatosGraficas.TIPO_TODOS, null);
        assertEquals(1.0, r.get(DatosGraficas.VICTORIAS));
        assertEquals(1.0, r.get(DatosGraficas.EMPATES));
        assertEquals(1.0, r.get(DatosGraficas.DERROTAS));

        // Rojos ve los mismos partidos desde el otro lado
        Map<String, Double> rr = DatosGraficas.resultadosEquipo(club, rojos, DatosGraficas.TIPO_TODOS, null);
        assertEquals(1.0, rr.get(DatosGraficas.VICTORIAS));
        assertEquals(1.0, rr.get(DatosGraficas.DERROTAS));
    }

    @Test
    @DisplayName("Pastel: el filtro por competencia ignora los partidos internos")
    void filtroCompetencia() {
        Competencia torneo = new TorneoDeLosCerros("Cerros", "Org", "2026", Deporte.FUTBOL, Categoria.MAYORES, 7);
        club.getCompetencias().add(torneo);
        InscripcionEquipo ins = club.inscribirEquipo(DatosBase.ENTRENADOR1, azules, torneo, azules.getJugadores());
        club.registrarPartidoCompeticion(DatosBase.ENTRENADOR1, ins, "Rival", "Estadio", LocalDate.now(), 0, 2, null);
        club.registrarPartidoInterno(DatosBase.ENTRENADOR1, azules, rojos, LocalDate.now(), 4, 0, null);

        Map<String, Double> soloTorneo = DatosGraficas.resultadosEquipo(club, azules,
                DatosGraficas.TIPO_TODOS, torneo);
        assertEquals(0.0, soloTorneo.get(DatosGraficas.VICTORIAS));
        assertEquals(1.0, soloTorneo.get(DatosGraficas.DERROTAS));

        Map<String, Double> internos = DatosGraficas.resultadosEquipo(club, azules,
                DatosGraficas.TIPO_INTERNOS, null);
        assertEquals(1.0, internos.get(DatosGraficas.VICTORIAS));
        assertEquals(0.0, internos.get(DatosGraficas.DERROTAS));
    }

    @Test
    @DisplayName("Barras: suma una estadistica por jugador e incluye a los que tienen cero")
    void estadisticaPorJugador() {
        Jugador goleador = azules.getJugadores().get(0);
        club.registrarPartidoInterno(DatosBase.ENTRENADOR1, azules, rojos, LocalDate.now(), 2, 0,
                goles(goleador, 2));
        club.registrarPartidoInterno(DatosBase.ENTRENADOR1, azules, rojos, LocalDate.now(), 1, 0,
                goles(goleador, 1));

        Map<String, Double> datos = DatosGraficas.estadisticaPorJugador(club, azules, "Goles",
                DatosGraficas.TIPO_TODOS, null);
        assertEquals(7, datos.size());
        assertEquals(3.0, datos.get(goleador.getNombre() + " #" + goleador.getNumeroCamiseta()));
    }

    @Test
    @DisplayName("Barras: ingresos por dia sin IVA, sin impuesto al consumo y sin propina")
    void ingresosSinImpuestos() {
        DatosTienda.cargar(club);
        Map<ArticuloTienda, Integer> canasta = new LinkedHashMap<>();
        canasta.put(DatosTienda.balon, 2); // 100000 + IVA
        club.comprarTienda(DatosBase.SOCIO1, canasta, null);
        Map<Producto, Integer> pedido = new LinkedHashMap<>();
        pedido.put(DatosTienda.agua, 2); // 6000 + impuesto + propina
        club.comprarCafeteria(DatosBase.SOCIO1, pedido, 0.10, 0);
        LocalDate hoy = LocalDate.now();
        new Mensualidad((Socio) usuario(DatosBase.SOCIO2), "x", 150000, hoy.minusDays(2));

        DatosGraficas.Series s = DatosGraficas.ingresosPorDia(club, hoy.minusDays(4), 5);
        assertEquals(5, s.getEtiquetas().size());
        assertEquals(100000, s.getSeries().get(DatosGraficas.TIENDA)[4], 0.01);
        assertEquals(6000, s.getSeries().get(DatosGraficas.CAFETERIA)[4], 0.01);
        assertEquals(150000, s.getSeries().get(DatosGraficas.MENSUALIDADES)[2], 0.01);
        assertEquals(0, s.getSeries().get(DatosGraficas.MENSUALIDADES)[4], 0.01);
    }

    @Test
    @DisplayName("Pastel y lineas: resultados individuales y evolucion del ranking")
    void rankingIndividual() {
        Socio sofia = usuario(DatosBase.SOCIO1);
        Socio juan = usuario(DatosBase.SOCIO2);
        InscripcionDisciplina tSofia = new InscripcionDisciplina(sofia, Deporte.TENIS, Nivel.INTERMEDIO);
        InscripcionDisciplina tJuan = new InscripcionDisciplina(juan, Deporte.TENIS, Nivel.INTERMEDIO);
        LocalDate ayer = LocalDate.now().minusDays(1);
        PartidoPractica p = club.programarPractica(Arrays.asList(tSofia, tJuan), Modalidad.SENCILLOS, ayer);
        Map<InscripcionDisciplina, Map<String, Integer>> datos = new HashMap<>();
        datos.put(tSofia, raqueta(2));
        datos.put(tJuan, raqueta(0));
        club.registrarPractica(p, datos);

        Map<String, Double> resultados = DatosGraficas.resultadosIndividual(club, sofia, Deporte.TENIS);
        assertEquals(1.0, resultados.get(DatosGraficas.VICTORIAS));
        assertEquals(0.0, resultados.get(DatosGraficas.DERROTAS));

        DatosGraficas.Series s = DatosGraficas.evolucionRanking(club, Deporte.TENIS, Arrays.asList(sofia, juan));
        assertEquals(Arrays.asList("Inicio", ayer.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))),
                s.getEtiquetas());
        assertArrayEquals(new double[] {1000, 1010}, s.getSeries().get("Sofia Perez"), 0.01);
        assertArrayEquals(new double[] {1000, 990}, s.getSeries().get("Juan Torres"), 0.01);
    }

    private static Map<String, Integer> raqueta(int sets) {
        Map<String, Integer> m = new HashMap<>();
        m.put("sets", sets);
        m.put("games", sets * 6);
        m.put("aces", 1);
        m.put("erroresNoForzados", 1);
        return m;
    }

    @Test
    @DisplayName("Lineas: asistencia por semana de los jugadores de un equipo")
    void asistenciaSemanal() {
        CanchaFutbol cancha = new CanchaFutbol("F", "Cancha", false);
        club.getInstalaciones().add(cancha);
        Socio jugadorAzul = usuario(azules.getJugadores().get(0).getCodigo());
        Socio jugadorRojo = usuario(rojos.getJugadores().get(0).getCodigo());
        LocalDate lunesPasado = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        SesionEntrenamiento s = club.organizarSesion(DatosBase.ENTRENADOR1, cancha, lunesPasado,
                LocalTime.of(8, 0), 60, Arrays.asList(jugadorAzul, jugadorRojo));
        club.registrarAsistencia(DatosBase.ENTRENADOR1, s, jugadorAzul, Asistencia.ASISTIO, 4, "");
        club.registrarAsistencia(DatosBase.ENTRENADOR1, s, jugadorRojo, Asistencia.FALTO, 0, "");

        DatosGraficas.Series todos = DatosGraficas.asistenciaSemanal(club, null, LocalDate.now(), 2);
        assertArrayEquals(new double[] {1, 0}, todos.getSeries().get(DatosGraficas.ASISTIO), 0.01);
        assertArrayEquals(new double[] {1, 0}, todos.getSeries().get(DatosGraficas.FALTO), 0.01);

        DatosGraficas.Series soloAzules = DatosGraficas.asistenciaSemanal(club, azules, LocalDate.now(), 2);
        assertArrayEquals(new double[] {1, 0}, soloAzules.getSeries().get(DatosGraficas.ASISTIO), 0.01);
        assertArrayEquals(new double[] {0, 0}, soloAzules.getSeries().get(DatosGraficas.FALTO), 0.01);
    }

    @Test
    @DisplayName("Mapa de calor: horas ocupadas por dia de la semana y franja")
    void ocupacion() {
        CanchaTenis tenis = new CanchaTenis("T", "Tenis", false);
        club.getInstalaciones().add(tenis);
        LocalDate lunes = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY);
        club.reservar(DatosBase.SOCIO1, tenis, lunes, LocalTime.of(9, 0), LocalTime.of(10, 30), 2, Modalidad.SENCILLOS);

        double[][] m = DatosGraficas.ocupacionInstalaciones(club, Deporte.TENIS, lunes, lunes);
        int nueve = 9 - DatosGraficas.HORA_APERTURA;
        assertEquals(1, m[0][nueve], 0.01);
        assertEquals(1, m[0][nueve + 1], 0.01); // 10:00 a 10:30 tambien ocupa la franja de las 10
        assertEquals(0, m[0][nueve + 2], 0.01);
        assertEquals(0, DatosGraficas.ocupacionInstalaciones(club, Deporte.PADEL, lunes, lunes)[0][nueve], 0.01);
    }

    @Test
    @DisplayName("Los datos de demostracion cargan sin violar reglas y se pueden guardar")
    void datosDemo() {
        Club demo = new Persistencia(carpeta.resolve("demo").toString()).cargar();
        DatosDemo.cargar(demo);
        assertTrue(demo.getEquipos().size() >= 4);
        assertTrue(demo.getPartidos().size() > 20);
        assertTrue(demo.getSesiones().size() > 10);
        assertFalse(demo.getVentas().isEmpty());
        for (Competencia c : demo.getCompetencias()) {
            assertEquals(10, c.getPartidos().size(), "Partidos de " + c.getNombre());
        }
        demo.guardar();
        Club recuperado = new Persistencia(carpeta.resolve("demo").toString()).cargar();
        assertEquals(demo.getPartidos().size(), recuperado.getPartidos().size());
    }
}
