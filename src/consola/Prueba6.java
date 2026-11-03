package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import modelo.*;

@DisplayName("Prueba 6: practicas, niveles, estadisticas y ranking")
class Prueba6Test extends PruebaBase {

    private Socio sofia;
    private Socio juan;
    private Socio maria;
    private Socio pedro;
    private InscripcionDisciplina tSofia;
    private InscripcionDisciplina tJuan;
    private InscripcionDisciplina tMaria;
    private LocalDate fecha;

    @BeforeEach
    void inscribirEnTenis() {
        sofia = usuario(DatosBase.SOCIO1);
        juan = usuario(DatosBase.SOCIO2);
        maria = usuario(DatosBase.SOCIO3);
        pedro = usuario(DatosBase.SOCIO4);
        tSofia = new InscripcionDisciplina(sofia, Deporte.TENIS, Nivel.PRINCIPIANTE);
        tJuan = new InscripcionDisciplina(juan, Deporte.TENIS, Nivel.INTERMEDIO);
        tMaria = new InscripcionDisciplina(maria, Deporte.TENIS, Nivel.AVANZADO);
        fecha = LocalDate.now().plusDays(1);
    }

    private PartidoPractica jugarSencillo() {
        PartidoPractica sencillo = club.programarPractica(
                Arrays.asList(tSofia, tJuan), Modalidad.SENCILLOS, fecha);
        club.registrarPractica(sencillo, estadisticasTenis(tSofia, 2, tJuan, 0));
        return sencillo;
    }

    @Test
    @DisplayName("Sencillo entre niveles adyacentes actualiza el ranking")
    void sencilloNivelesAdyacentes() {
        PartidoPractica sencillo = jugarSencillo();
        assertTrue(sencillo.resuelto());
        assertEquals(2, sencillo.getEstadisticasPractica().size());
        assertEquals(1010, tSofia.getPuntajeRanking());
        assertEquals(990, tJuan.getPuntajeRanking());
    }

    @Test
    @DisplayName("Principiante contra Avanzado sin entrenador se rechaza")
    void nivelesIncompatibles() {
        assertThrows(ReglaNegocioException.class,
                () -> club.programarPractica(Arrays.asList(tSofia, tMaria), Modalidad.SENCILLOS, fecha));
    }

    @Test
    @DisplayName("Un socio vinculado como entrenador puede jugar contra niveles extremos")
    void excepcionSocioEntrenador() {
        jugarSencillo();
        club.vincularSocioEntrenador(DatosBase.SOCIO1, DatosBase.ENTRENADOR1);
        PartidoPractica partido = club.programarPractica(
                Arrays.asList(tSofia, tMaria), Modalidad.SENCILLOS, fecha);
        club.registrarPractica(partido, estadisticasTenis(tSofia, 0, tMaria, 2));
        assertEquals(1000, tSofia.getPuntajeRanking());
        assertEquals(1010, tMaria.getPuntajeRanking());
    }

    @Test
    @DisplayName("Dobles de padel registran cuatro estadisticas individuales")
    void doblesPadel() {
        // Igual que en Prueba6: Sofia ya es entrenadora, lo que permite mezclar niveles
        club.vincularSocioEntrenador(DatosBase.SOCIO1, DatosBase.ENTRENADOR1);
        InscripcionDisciplina pSofia = new InscripcionDisciplina(sofia, Deporte.PADEL, Nivel.PRINCIPIANTE);
        InscripcionDisciplina pJuan = new InscripcionDisciplina(juan, Deporte.PADEL, Nivel.INTERMEDIO);
        InscripcionDisciplina pMaria = new InscripcionDisciplina(maria, Deporte.PADEL, Nivel.AVANZADO);
        InscripcionDisciplina pPedro = new InscripcionDisciplina(pedro, Deporte.PADEL, Nivel.AVANZADO);
        PartidoPractica dobles = club.programarPractica(
                Arrays.asList(pSofia, pJuan, pMaria, pPedro), Modalidad.DOBLES, fecha);
        Map<InscripcionDisciplina, Map<String, Integer>> datos = new HashMap<>();
        datos.put(pSofia, datosRaqueta(2));
        datos.put(pJuan, datosRaqueta(2));
        datos.put(pMaria, datosRaqueta(1));
        datos.put(pPedro, datosRaqueta(1));
        club.registrarPractica(dobles, datos);

        assertTrue(dobles.resuelto());
        assertEquals(4, dobles.getEstadisticasPractica().size());
        assertEquals(1010, pSofia.getPuntajeRanking());
        assertEquals(1010, pJuan.getPuntajeRanking());
        assertEquals(990, pMaria.getPuntajeRanking());
        assertEquals(990, pPedro.getPuntajeRanking());
    }

    @Test
    @DisplayName("Tenis de mesa solo exige sets y puntos totales")
    void tenisDeMesa() {
        InscripcionDisciplina mJuan = new InscripcionDisciplina(juan, Deporte.TENIS_DE_MESA, Nivel.INTERMEDIO);
        InscripcionDisciplina mMaria = new InscripcionDisciplina(maria, Deporte.TENIS_DE_MESA, Nivel.AVANZADO);
        PartidoPractica partido = club.programarPractica(
                Arrays.asList(mJuan, mMaria), Modalidad.SENCILLOS, fecha);
        Map<InscripcionDisciplina, Map<String, Integer>> datos = new HashMap<>();
        datos.put(mJuan, datosMesa(3, 33));
        datos.put(mMaria, datosMesa(1, 24));
        club.registrarPractica(partido, datos);

        assertTrue(partido.resuelto());
        assertEquals(2, partido.getEstadisticasPractica().size());
        assertEquals(1010, mJuan.getPuntajeRanking());
        assertEquals(990, mMaria.getPuntajeRanking());
    }

    @Test
    @DisplayName("Un empate se rechaza sin modificar resultados ni ranking")
    void empateRechazado() {
        PartidoPractica partido = club.programarPractica(
                Arrays.asList(tSofia, tJuan), Modalidad.SENCILLOS, fecha);
        assertThrows(ReglaNegocioException.class,
                () -> club.registrarPractica(partido, estadisticasTenis(tSofia, 2, tJuan, 2)));
        assertFalse(partido.resuelto());
        assertTrue(partido.getEstadisticasPractica().isEmpty());
        assertEquals(1000, tSofia.getPuntajeRanking());
        assertEquals(1000, tJuan.getPuntajeRanking());
    }

    @Test
    @DisplayName("No se registra dos veces el resultado de una practica")
    void resultadoDuplicado() {
        PartidoPractica sencillo = jugarSencillo();
        assertThrows(ReglaNegocioException.class,
                () -> club.registrarPractica(sencillo, estadisticasTenis(tSofia, 2, tJuan, 0)));
    }

    @Test
    @DisplayName("Partidos, rankings, historial y entrenador asociado se conservan")
    void persistencia() {
        jugarSencillo();
        club.vincularSocioEntrenador(DatosBase.SOCIO1, DatosBase.ENTRENADOR1);
        PartidoPractica segundo = club.programarPractica(
                Arrays.asList(tSofia, tMaria), Modalidad.SENCILLOS, fecha);
        club.registrarPractica(segundo, estadisticasTenis(tSofia, 0, tMaria, 2));

        Club recuperado = guardarYRecargar();
        assertEquals(2, recuperado.getPartidos().size());
        Socio sofiaR = (Socio) DatosBase.usuario(recuperado, DatosBase.SOCIO1);
        Socio mariaR = (Socio) DatosBase.usuario(recuperado, DatosBase.SOCIO3);
        assertSame(DatosBase.usuario(recuperado, DatosBase.ENTRENADOR1), sofiaR.getEntrenadorAsociado());
        InscripcionDisciplina tenisSofia = inscripcion(sofiaR, Deporte.TENIS);
        assertEquals(1000, tenisSofia.getPuntajeRanking());
        assertEquals(2, tenisSofia.getHistorial().size());
        assertEquals(1010, inscripcion(mariaR, Deporte.TENIS).getPuntajeRanking());
    }

    private static InscripcionDisciplina inscripcion(Socio socio, Deporte disciplina) {
        for (InscripcionDisciplina i : socio.getInscripciones()) {
            if (i.getDisciplina() == disciplina) {
                return i;
            }
        }
        return fail("No se recupero la inscripcion a " + disciplina);
    }

    private static Map<InscripcionDisciplina, Map<String, Integer>> estadisticasTenis(
            InscripcionDisciplina uno, int setsUno, InscripcionDisciplina dos, int setsDos) {
        Map<InscripcionDisciplina, Map<String, Integer>> datos = new HashMap<>();
        datos.put(uno, datosRaqueta(setsUno));
        datos.put(dos, datosRaqueta(setsDos));
        return datos;
    }

    private static Map<String, Integer> datosRaqueta(int sets) {
        Map<String, Integer> datos = new HashMap<>();
        datos.put("sets", sets);
        datos.put("games", sets * 6);
        datos.put("aces", sets);
        datos.put("erroresNoForzados", 1);
        return datos;
    }

    private static Map<String, Integer> datosMesa(int sets, int puntosTotales) {
        Map<String, Integer> datos = new HashMap<>();
        datos.put("sets", sets);
        datos.put("puntosTotales", puntosTotales);
        return datos;
    }
}
