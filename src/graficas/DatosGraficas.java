package graficas;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import modelo.Club;
import modelo.Competencia;
import modelo.Convocatoria;
import modelo.Deporte;
import modelo.Equipo;
import modelo.EstadisticaBaloncesto;
import modelo.EstadisticaFutbol;
import modelo.EstadisticaPartido;
import modelo.EstadisticaPractica;
import modelo.EstadisticaVoleibol;
import modelo.InscripcionDisciplina;
import modelo.Jugador;
import modelo.Mensualidad;
import modelo.Partido;
import modelo.PartidoCompeticion;
import modelo.PartidoEquipo;
import modelo.PartidoInterno;
import modelo.PartidoPractica;
import modelo.RegistroRanking;
import modelo.Reserva;
import modelo.SesionEntrenamiento;
import modelo.Socio;
import modelo.Usuario;
import modelo.Venta;
import modelo.VentaCafeteria;
import modelo.VentaTienda;

/**
 * Calcula, a partir de la informacion del club, los datos que muestran las graficas.
 * No depende de Swing: recibe filtros y devuelve mapas o series, por lo que se puede
 * probar con JUnit sin abrir ventanas.
 */
public final class DatosGraficas {

    public static final String VICTORIAS = "Victorias";
    public static final String EMPATES = "Empates";
    public static final String DERROTAS = "Derrotas";

    public static final String TIPO_TODOS = "Todos";
    public static final String TIPO_INTERNOS = "Partidos internos";
    public static final String TIPO_COMPETENCIA = "Competencias";

    public static final String TIENDA = "Tienda";
    public static final String CAFETERIA = "Cafeteria";
    public static final String MENSUALIDADES = "Mensualidades";

    public static final String ASISTIO = "Asistio";
    public static final String LLEGO_TARDE = "Llego tarde";
    public static final String FALTO = "Falto";

    public static final String[] DIAS = {"Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"};
    public static final int HORA_APERTURA = 6;
    public static final int HORA_CIERRE = 22;

    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");

    private DatosGraficas() {
    }

    /** Etiquetas del eje X y valores de cada serie, listos para barras o lineas. */
    public static class Series {
        private final List<String> etiquetas;
        private final Map<String, double[]> series;

        public Series(List<String> etiquetas, Map<String, double[]> series) {
            this.etiquetas = etiquetas;
            this.series = series;
        }

        public List<String> getEtiquetas() {
            return etiquetas;
        }

        public Map<String, double[]> getSeries() {
            return series;
        }
    }

    // ------------------------------------------------------------------
    // 1. Pastel: distribucion de resultados
    // ------------------------------------------------------------------

    /**
     * Victorias, empates y derrotas de un equipo.
     *
     * @param tipo        TIPO_TODOS, TIPO_INTERNOS o TIPO_COMPETENCIA
     * @param competencia si no es null, solo cuenta los partidos de esa competencia
     */
    public static Map<String, Double> resultadosEquipo(Club club, Equipo equipo, String tipo,
            Competencia competencia) {
        double ganados = 0;
        double empatados = 0;
        double perdidos = 0;
        for (Partido p : club.getPartidos()) {
            if (!(p instanceof PartidoEquipo) || !cumpleFiltro(p, tipo, competencia)) {
                continue;
            }
            PartidoEquipo pe = (PartidoEquipo) p;
            if (!pe.tieneResultado()) {
                continue;
            }
            int propio;
            int rival;
            if (pe.getEquipo1() == equipo) {
                propio = pe.getMarcadorEquipo1();
                rival = pe.getMarcadorEquipo2();
            } else if (pe.getEquipo2() == equipo) {
                propio = pe.getMarcadorEquipo2();
                rival = pe.getMarcadorEquipo1();
            } else {
                continue;
            }
            if (propio > rival) {
                ganados++;
            } else if (propio == rival) {
                empatados++;
            } else {
                perdidos++;
            }
        }
        return mapaResultados(ganados, empatados, perdidos);
    }

    /** Victorias y derrotas de un socio en partidos de practica de una disciplina (null = todas). */
    public static Map<String, Double> resultadosIndividual(Club club, Socio socio, Deporte disciplina) {
        double ganados = 0;
        double perdidos = 0;
        for (Partido p : club.getPartidos()) {
            if (!(p instanceof PartidoPractica)) {
                continue;
            }
            PartidoPractica pp = (PartidoPractica) p;
            if (!pp.resuelto() || (disciplina != null && pp.getDeporte() != disciplina)) {
                continue;
            }
            Boolean gano = ganoPractica(pp, socio);
            if (gano == null) {
                continue;
            }
            if (gano) {
                ganados++;
            } else {
                perdidos++;
            }
        }
        return mapaResultados(ganados, 0, perdidos);
    }

    /** Devuelve si el socio gano el partido, o null si no participo. */
    static Boolean ganoPractica(PartidoPractica partido, Socio socio) {
        List<InscripcionDisciplina> participantes = partido.getParticipantes();
        int mitad = participantes.size() / 2;
        int posicion = -1;
        for (int i = 0; i < participantes.size(); i++) {
            if (participantes.get(i).getSocio() == socio) {
                posicion = i;
            }
        }
        if (posicion < 0 || mitad == 0) {
            return null;
        }
        int setsPrimera = sets(partido, participantes.get(0));
        int setsSegunda = sets(partido, participantes.get(mitad));
        boolean ganaPrimera = setsPrimera > setsSegunda;
        return posicion < mitad ? ganaPrimera : !ganaPrimera;
    }

    private static int sets(PartidoPractica partido, InscripcionDisciplina inscripcion) {
        for (EstadisticaPractica e : partido.getEstadisticasPractica()) {
            if (e.getInscripcion() == inscripcion) {
                return e.getSets();
            }
        }
        return 0;
    }

    private static Map<String, Double> mapaResultados(double ganados, double empatados, double perdidos) {
        Map<String, Double> resultado = new LinkedHashMap<>();
        resultado.put(VICTORIAS, ganados);
        resultado.put(EMPATES, empatados);
        resultado.put(DERROTAS, perdidos);
        return resultado;
    }

    private static boolean cumpleFiltro(Partido p, String tipo, Competencia competencia) {
        if (TIPO_INTERNOS.equals(tipo) && !(p instanceof PartidoInterno)) {
            return false;
        }
        if (TIPO_COMPETENCIA.equals(tipo) && !(p instanceof PartidoCompeticion)) {
            return false;
        }
        if (competencia != null) {
            return p instanceof PartidoCompeticion && ((PartidoCompeticion) p).getCompetencia() == competencia;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // 2. Barras: estadistica por jugador e ingresos por rubro
    // ------------------------------------------------------------------

    /** Nombres de las estadisticas que se registran en cada deporte de conjunto. */
    public static String[] estadisticasDe(Deporte deporte) {
        switch (deporte) {
            case FUTBOL:
                return new String[] {"Goles", "Asistencias", "Tarjetas amarillas", "Tarjetas rojas"};
            case BALONCESTO:
                return new String[] {"Puntos", "Rebotes"};
            case VOLEIBOL:
                return new String[] {"Aces", "Bloqueos"};
            default:
                return new String[0];
        }
    }

    static int valorEstadistica(EstadisticaPartido dato, String estadistica) {
        if (dato instanceof EstadisticaFutbol) {
            EstadisticaFutbol f = (EstadisticaFutbol) dato;
            switch (estadistica) {
                case "Goles": return f.getGoles();
                case "Asistencias": return f.getAsistencias();
                case "Tarjetas amarillas": return f.getTarjetasAmarillas();
                case "Tarjetas rojas": return f.getTarjetasRojas();
                default: return 0;
            }
        }
        if (dato instanceof EstadisticaBaloncesto) {
            EstadisticaBaloncesto b = (EstadisticaBaloncesto) dato;
            return "Puntos".equals(estadistica) ? b.getPuntos() : "Rebotes".equals(estadistica) ? b.getRebotes() : 0;
        }
        if (dato instanceof EstadisticaVoleibol) {
            EstadisticaVoleibol v = (EstadisticaVoleibol) dato;
            return "Aces".equals(estadistica) ? v.getAces() : "Bloqueos".equals(estadistica) ? v.getBloqueos() : 0;
        }
        return 0;
    }

    /** Total de una estadistica por cada jugador del equipo, con los mismos filtros del pastel. */
    public static Map<String, Double> estadisticaPorJugador(Club club, Equipo equipo, String estadistica,
            String tipo, Competencia competencia) {
        Map<Jugador, Double> totales = new LinkedHashMap<>();
        for (Jugador j : equipo.getJugadores()) {
            totales.put(j, 0.0);
        }
        for (Partido p : club.getPartidos()) {
            if (!(p instanceof PartidoEquipo) || !cumpleFiltro(p, tipo, competencia)) {
                continue;
            }
            for (EstadisticaPartido dato : p.getEstadisticas()) {
                if (totales.containsKey(dato.getJugador())) {
                    totales.put(dato.getJugador(), totales.get(dato.getJugador()) + valorEstadistica(dato, estadistica));
                }
            }
        }
        Map<String, Double> resultado = new LinkedHashMap<>();
        for (Map.Entry<Jugador, Double> e : totales.entrySet()) {
            resultado.put(nombreCorto(e.getKey()), e.getValue());
        }
        return resultado;
    }

    /** Valor de una venta sin impuestos: sin IVA en tienda, sin impuesto al consumo ni propina en cafeteria. */
    public static double ingresoSinImpuestos(Venta venta) {
        if (venta instanceof VentaTienda) {
            return venta.getSubtotal() - ((VentaTienda) venta).getDescuento();
        }
        if (venta instanceof VentaCafeteria) {
            return venta.getSubtotal() - venta.getPuntosRedimidos();
        }
        return venta.getSubtotal();
    }

    /** Ingresos sin impuestos por dia y por rubro, en los {@code dias} dias que empiezan en {@code inicio}. */
    public static Series ingresosPorDia(Club club, LocalDate inicio, int dias) {
        List<String> etiquetas = new ArrayList<>();
        double[] tienda = new double[dias];
        double[] cafeteria = new double[dias];
        double[] mensualidades = new double[dias];
        for (int i = 0; i < dias; i++) {
            etiquetas.add(inicio.plusDays(i).format(DIA_MES));
        }
        for (Venta v : club.getVentas()) {
            int i = indiceDia(inicio, v.getFecha().toLocalDate(), dias);
            if (i >= 0) {
                if (v instanceof VentaTienda) {
                    tienda[i] += ingresoSinImpuestos(v);
                } else {
                    cafeteria[i] += ingresoSinImpuestos(v);
                }
            }
        }
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Socio) {
                for (Mensualidad m : ((Socio) u).getMensualidades()) {
                    int i = indiceDia(inicio, m.getFechaPago(), dias);
                    if (i >= 0) {
                        mensualidades[i] += m.getValor();
                    }
                }
            }
        }
        Map<String, double[]> series = new LinkedHashMap<>();
        series.put(TIENDA, tienda);
        series.put(CAFETERIA, cafeteria);
        series.put(MENSUALIDADES, mensualidades);
        return new Series(etiquetas, series);
    }

    private static int indiceDia(LocalDate inicio, LocalDate fecha, int dias) {
        long diferencia = java.time.temporal.ChronoUnit.DAYS.between(inicio, fecha);
        return diferencia >= 0 && diferencia < dias ? (int) diferencia : -1;
    }

    // ------------------------------------------------------------------
    // 3. Lineas: ranking y asistencia
    // ------------------------------------------------------------------

    /** Socios inscritos en una disciplina individual. */
    public static List<Socio> sociosDeDisciplina(Club club, Deporte disciplina) {
        List<Socio> resultado = new ArrayList<>();
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Socio && inscripcion((Socio) u, disciplina) != null) {
                resultado.add((Socio) u);
            }
        }
        return resultado;
    }

    public static InscripcionDisciplina inscripcion(Socio socio, Deporte disciplina) {
        for (InscripcionDisciplina i : socio.getInscripciones()) {
            if (i.getDisciplina() == disciplina) {
                return i;
            }
        }
        return null;
    }

    /**
     * Evolucion del puntaje de ranking. El primer punto ("Inicio") es el puntaje antes
     * del primer partido; despues hay un punto por cada fecha con partidos.
     */
    public static Series evolucionRanking(Club club, Deporte disciplina, List<Socio> socios) {
        TreeSet<LocalDate> fechas = new TreeSet<>();
        for (Socio s : socios) {
            InscripcionDisciplina ins = inscripcion(s, disciplina);
            if (ins != null) {
                for (RegistroRanking r : ins.getHistorial()) {
                    fechas.add(r.getFecha());
                }
            }
        }
        List<String> etiquetas = new ArrayList<>();
        etiquetas.add("Inicio");
        for (LocalDate f : fechas) {
            etiquetas.add(f.format(DIA_MES));
        }

        Map<String, double[]> series = new LinkedHashMap<>();
        for (Socio s : socios) {
            InscripcionDisciplina ins = inscripcion(s, disciplina);
            if (ins == null) {
                continue;
            }
            List<RegistroRanking> historial = ins.getHistorial();
            double[] valores = new double[etiquetas.size()];
            double actual = historial.isEmpty() ? ins.getPuntajeRanking() : historial.get(0).getPuntajeAnterior();
            valores[0] = actual;
            int i = 1;
            for (LocalDate f : fechas) {
                for (RegistroRanking r : historial) {
                    if (r.getFecha().equals(f)) {
                        actual = r.getPuntajeNuevo();
                    }
                }
                valores[i++] = actual;
            }
            series.put(s.getNombre() + " " + s.getApellido(), valores);
        }
        return new Series(etiquetas, series);
    }

    /**
     * Asistencia a entrenamientos por semana (lunes a domingo). Si {@code equipo} no es null,
     * solo cuenta a los jugadores de ese equipo o las sesiones asignadas a el.
     */
    public static Series asistenciaSemanal(Club club, Equipo equipo, LocalDate hasta, int semanas) {
        LocalDate ultimoLunes = hasta.with(DayOfWeek.MONDAY);
        LocalDate primerLunes = ultimoLunes.minusWeeks(semanas - 1);
        List<String> etiquetas = new ArrayList<>();
        for (int i = 0; i < semanas; i++) {
            etiquetas.add("Sem " + primerLunes.plusWeeks(i).format(DIA_MES));
        }
        double[] asistio = new double[semanas];
        double[] tarde = new double[semanas];
        double[] falto = new double[semanas];

        Set<Integer> codigosEquipo = new HashSet<>();
        if (equipo != null) {
            for (Jugador j : equipo.getJugadores()) {
                codigosEquipo.add(j.getCodigo());
            }
        }

        for (SesionEntrenamiento s : club.getSesiones()) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(primerLunes, s.getFecha());
            if (dias < 0 || dias >= semanas * 7L) {
                continue;
            }
            int semana = (int) (dias / 7);
            for (Convocatoria c : s.getConvocatorias()) {
                if (c.getAsistencia() == null) {
                    continue;
                }
                if (equipo != null && s.getEquipo() != equipo && !codigosEquipo.contains(c.getSocio().getCodigo())) {
                    continue;
                }
                switch (c.getAsistencia()) {
                    case ASISTIO:
                        asistio[semana]++;
                        break;
                    case LLEGO_TARDE:
                        tarde[semana]++;
                        break;
                    default:
                        falto[semana]++;
                }
            }
        }
        Map<String, double[]> series = new LinkedHashMap<>();
        series.put(ASISTIO, asistio);
        series.put(LLEGO_TARDE, tarde);
        series.put(FALTO, falto);
        return new Series(etiquetas, series);
    }

    // ------------------------------------------------------------------
    // 4. Grafica complementaria: ocupacion de instalaciones
    // ------------------------------------------------------------------

    public static List<String> horasDelDia() {
        List<String> horas = new ArrayList<>();
        for (int h = HORA_APERTURA; h < HORA_CIERRE; h++) {
            horas.add(String.valueOf(h));
        }
        return horas;
    }

    /**
     * Cuantas horas-instalacion estan ocupadas (reservas + sesiones de entrenamiento) por dia
     * de la semana y hora del dia, en el periodo dado. Filas: lunes..domingo; columnas: horas.
     */
    public static double[][] ocupacionInstalaciones(Club club, Deporte disciplina, LocalDate desde, LocalDate hasta) {
        double[][] ocupacion = new double[7][HORA_CIERRE - HORA_APERTURA];
        for (Reserva r : club.getReservas()) {
            if (disciplina == null || r.getInstalacion().getDisciplina() == disciplina) {
                sumarOcupacion(ocupacion, r.getFecha(), r.getHoraInicio(), r.getHoraFin(), desde, hasta);
            }
        }
        for (SesionEntrenamiento s : club.getSesiones()) {
            if (disciplina == null || s.getInstalacion().getDisciplina() == disciplina) {
                sumarOcupacion(ocupacion, s.getFecha(), s.getHora(), s.getHoraFin(), desde, hasta);
            }
        }
        return ocupacion;
    }

    private static void sumarOcupacion(double[][] ocupacion, LocalDate fecha, LocalTime inicio, LocalTime fin,
            LocalDate desde, LocalDate hasta) {
        if (fecha.isBefore(desde) || fecha.isAfter(hasta)) {
            return;
        }
        int dia = fecha.getDayOfWeek().getValue() - 1;
        for (int h = HORA_APERTURA; h < HORA_CIERRE; h++) {
            LocalTime franjaInicio = LocalTime.of(h, 0);
            LocalTime franjaFin = h + 1 < 24 ? LocalTime.of(h + 1, 0) : LocalTime.MAX;
            if (inicio.isBefore(franjaFin) && fin.isAfter(franjaInicio)) {
                ocupacion[dia][h - HORA_APERTURA]++;
            }
        }
    }

    private static String nombreCorto(Jugador j) {
        return j.getNombre() + " #" + j.getNumeroCamiseta();
    }
}
