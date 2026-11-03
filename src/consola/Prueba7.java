package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import consola.DatosTienda;
import modelo.*;

@DisplayName("Prueba 7: compras en tienda y cafeteria")
class Prueba7Test extends PruebaBase {

    private static final double DELTA = 0.01;

    @BeforeEach
    void cargarTienda() {
        DatosTienda.cargar(club);
        // Pedro esta dentro de una cancha de padel techada todo el dia
        CanchaPadel padel = new CanchaPadel("P-01", "Cancha de padel techada 1", true);
        club.getInstalaciones().add(padel);
        club.reservar(DatosBase.SOCIO4, padel, LocalDate.now(), LocalTime.of(0, 0),
                LocalTime.of(23, 59), 4, Modalidad.DOBLES);
    }

    // ---------- Tienda (IVA 19%) ----------

    @Test
    @DisplayName("Compra en tienda sin descuento: 2 balones = 119000")
    void compraTienda() {
        VentaTienda v = club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.balon, 2), null);
        assertEquals(119000, v.getTotal(), DELTA);
        assertEquals(1, club.getVentas().size());
    }

    @Test
    @DisplayName("Compras invalidas en la tienda se rechazan")
    void comprasTiendaInvalidas() {
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.balon, 20), null));
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.camiseta, 0), null));
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarTienda(DatosBase.SOCIO1, new LinkedHashMap<ArticuloTienda, Integer>(), null));
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarTienda(DatosBase.ADMIN, canasta(DatosTienda.camiseta, 1), null));
        assertTrue(club.getVentas().isEmpty());
    }

    @Test
    @DisplayName("Un empleado usa su propio codigo (15%): 80920")
    void descuentoEmpleado() {
        VentaTienda v = club.comprarTienda(DatosBase.ENTRENADOR1,
                canasta(DatosTienda.camiseta, 1), "DESC-CARLOS");
        assertEquals(80920, v.getTotal(), DELTA);
    }

    @Test
    @DisplayName("Un socio usa el codigo de un empleado (8%): 87584")
    void descuentoCompartido() {
        VentaTienda v = club.comprarTienda(DatosBase.SOCIO2,
                canasta(DatosTienda.camiseta, 1), "DESC-CARLOS");
        assertEquals(87584, v.getTotal(), DELTA);
    }

    @Test
    @DisplayName("Un codigo de descuento inexistente se rechaza")
    void codigoFalso() {
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarTienda(DatosBase.SOCIO2, canasta(DatosTienda.camiseta, 1), "CODIGO-FALSO"));
    }

    // ---------- Cafeteria (consumo 8% + propina) ----------

    @Test
    @DisplayName("El snack informa sus alergenos")
    void alergenos() {
        assertTrue(DatosTienda.galleta.getAlergenos().contains("gluten"));
        assertTrue(DatosTienda.galleta.getAlergenos().contains("leche"));
    }

    @Test
    @DisplayName("Cafeteria con propina 10%: 2 aguas + 1 galleta = 11800")
    void compraCafeteriaConPropina() {
        VentaCafeteria v = club.comprarCafeteria(DatosBase.SOCIO1,
                pedido(DatosTienda.agua, 2, DatosTienda.galleta, 1), 0.10, 0);
        assertEquals(11800, v.getTotal(), DELTA);
    }

    @Test
    @DisplayName("Cafeteria sin propina: 1 agua = 3240")
    void compraCafeteriaSinPropina() {
        VentaCafeteria v = club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.agua, 1), 0.0, 0);
        assertEquals(3240, v.getTotal(), DELTA);
    }

    @Test
    @DisplayName("Propina negativa o articulo de tienda en cafeteria se rechazan")
    void comprasCafeteriaInvalidas() {
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.agua, 1), -0.05, 0));
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.balon, 1), 0.10, 0));
    }

    @Test
    @DisplayName("No se vende bebida caliente dentro de una cancha techada")
    void bebidaCalienteCanchaTechada() {
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarCafeteria(DatosBase.SOCIO4, pedido(DatosTienda.cafe, 1), 0.10, 0));
        assertDoesNotThrow(
                () -> club.comprarCafeteria(DatosBase.SOCIO4, pedido(DatosTienda.agua, 1), 0.10, 0));
        assertDoesNotThrow(
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.cafe, 1), 0.10, 0));
    }

    // ---------- Puntos de fidelidad ----------

    @Test
    @DisplayName("Sofia acumula puntos y los usa: total 7180")
    void puntosFidelidad() {
        Socio sofia = usuario(DatosBase.SOCIO1);
        club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.balon, 2), null);
        club.comprarCafeteria(DatosBase.SOCIO1, pedido(DatosTienda.agua, 2, DatosTienda.galleta, 1), 0.10, 0);
        int puntosAntes = sofia.getPuntosFidelidad();
        assertTrue(puntosAntes >= 1000);

        VentaCafeteria v = club.comprarCafeteria(DatosBase.SOCIO1,
                pedido(DatosTienda.agua, 1, DatosTienda.galleta, 1), 0.10, 1000);
        assertEquals(7180, v.getTotal(), DELTA);
        assertEquals(1000, v.getPuntosRedimidos());
        assertEquals(puntosAntes - 1000 + v.getPuntosGanados(), sofia.getPuntosFidelidad());
    }

    @Test
    @DisplayName("No se pueden usar mas puntos de los que se tienen")
    void puntosInsuficientes() {
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.agua, 1), 0.10, 500));
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarCafeteria(DatosBase.SOCIO1, pedido(DatosTienda.agua, 1), 0.10, 5000));
    }

    // ---------- Historial ----------

    @Test
    @DisplayName("Un socio consulta su historial; el administrador no tiene")
    void historialCompras() {
        club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.balon, 2), null);
        club.comprarCafeteria(DatosBase.SOCIO1, pedido(DatosTienda.agua, 1), 0.10, 0);
        assertEquals(2, club.consultarHistorialCompras(DatosBase.SOCIO1).size());
        assertThrows(ReglaNegocioException.class, () -> club.consultarHistorialCompras(DatosBase.ADMIN));
    }

    @Test
    @DisplayName("Las ventas y el inventario se conservan al guardar y cargar")
    void persistencia() {
        club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.balon, 2), null);
        Club recuperado = guardarYRecargar();
        assertEquals(1, recuperado.getVentas().size());
        assertEquals(119000, recuperado.getVentas().get(0).getTotal(), DELTA);
        UbicacionInventario tienda = null;
        for (UbicacionInventario u : recuperado.getUbicaciones()) {
            if (u.getNombre().equals("Tienda")) {
                tienda = u;
            }
        }
        assertNotNull(tienda);
        Producto balon = DatosTienda.producto(recuperado, "Balon");
        assertEquals(8, tienda.buscarExistencia(balon).getCantidad());
    }

    private static Map<ArticuloTienda, Integer> canasta(ArticuloTienda articulo, int cantidad) {
        Map<ArticuloTienda, Integer> m = new LinkedHashMap<>();
        m.put(articulo, cantidad);
        return m;
    }

    private static Map<Producto, Integer> pedido(Producto p1, int c1) {
        Map<Producto, Integer> m = new LinkedHashMap<>();
        m.put(p1, c1);
        return m;
    }

    private static Map<Producto, Integer> pedido(Producto p1, int c1, Producto p2, int c2) {
        Map<Producto, Integer> m = pedido(p1, c1);
        m.put(p2, c2);
        return m;
    }
}
