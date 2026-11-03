package consola;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import consola.DatosTienda;
import modelo.*;

@DisplayName("Prueba 9: inventario, sugerencias y reportes de ventas")
class Prueba9Test extends PruebaBase {

    @BeforeEach
    void cargarTienda() {
        DatosTienda.cargar(club);
    }

    private int existencia(Club c, String ubicacion, Producto producto) {
        for (UbicacionInventario u : c.getUbicaciones()) {
            if (u.getNombre().equals(ubicacion)) {
                ExistenciaInventario e = u.buscarExistencia(producto);
                return e == null ? 0 : e.getCantidad();
            }
        }
        return fail("No existe la ubicacion " + ubicacion);
    }

    // ---------- Inventario ----------

    @Test
    @DisplayName("El administrador consulta el inventario completo y por ubicacion")
    void consultarInventario() {
        assertFalse(club.consultarInventario(DatosBase.ADMIN, null).isEmpty());
        assertFalse(club.consultarInventario(DatosBase.ADMIN, "Bodega").isEmpty());
    }

    @Test
    @DisplayName("Consultas de inventario invalidas se rechazan")
    void consultarInventarioInvalido() {
        assertThrows(ReglaNegocioException.class, () -> club.consultarInventario(DatosBase.ADMIN, "Sotano"));
        assertThrows(ReglaNegocioException.class, () -> club.consultarInventario(DatosBase.SOCIO1, null));
    }

    @Test
    @DisplayName("Reabastecer 5 raquetas: pasan de 2 a 7")
    void reabastecer() {
        club.reabastecer(DatosBase.ADMIN, DatosTienda.raqueta, 5);
        assertEquals(7, existencia(club, "Tienda", DatosTienda.raqueta));
    }

    @Test
    @DisplayName("Reabastecimientos invalidos se rechazan")
    void reabastecerInvalido() {
        assertThrows(ReglaNegocioException.class, () -> club.reabastecer(DatosBase.ADMIN, DatosTienda.raqueta, 0));
        assertThrows(ReglaNegocioException.class, () -> club.reabastecer(DatosBase.ADMIN, DatosTienda.raqueta, -3));
        assertThrows(ReglaNegocioException.class,
                () -> club.reabastecer(DatosBase.ENTRENADOR1, DatosTienda.raqueta, 5));
        assertThrows(ReglaNegocioException.class,
                () -> club.reabastecer(DatosBase.ADMIN, new ArticuloTienda("Gorra", 30000, "Ropa"), 5));
        assertEquals(2, existencia(club, "Tienda", DatosTienda.raqueta));
    }

    @Test
    @DisplayName("Mover 6 balones de la Bodega a la Tienda")
    void moverArticulo() {
        club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Tienda", 6);
        assertEquals(14, existencia(club, "Bodega", DatosTienda.balon));
        assertEquals(16, existencia(club, "Tienda", DatosTienda.balon));
    }

    @Test
    @DisplayName("Movimientos invalidos se rechazan y no cambian el inventario")
    void moverArticuloInvalido() {
        assertThrows(ReglaNegocioException.class,
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Tienda", 50));
        assertThrows(ReglaNegocioException.class,
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.raqueta, "Bodega", "Tienda", 1));
        assertThrows(ReglaNegocioException.class,
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Sotano", 1));
        assertThrows(ReglaNegocioException.class,
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Tienda", 0));
        assertEquals(20, existencia(club, "Bodega", DatosTienda.balon));
        assertEquals(10, existencia(club, "Tienda", DatosTienda.balon));
    }

    // ---------- Sugerencias ----------

    @Test
    @DisplayName("Empleados sugieren productos y el administrador los evalua")
    void sugerencias() {
        SugerenciaProducto s1 = club.sugerirProducto(DatosBase.ENTRENADOR1, "Botilito de agua",
                "Botilito reutilizable para entrenar");
        SugerenciaProducto s2 = club.sugerirProducto(DatosBase.FISIO, "Barra energetica",
                "Snack para despues del entrenamiento");
        club.aprobarSugerencia(DatosBase.ADMIN, s1);
        club.rechazarSugerencia(DatosBase.ADMIN, s2);
        assertEquals(EstadoSolicitud.APROBADA, s1.getEstado());
        assertEquals(EstadoSolicitud.RECHAZADA, s2.getEstado());
    }

    @Test
    @DisplayName("Sugerencias invalidas se rechazan")
    void sugerenciasInvalidas() {
        assertThrows(ReglaNegocioException.class,
                () -> club.sugerirProducto(DatosBase.SOCIO1, "Gorra", "Gorra del club"));
        SugerenciaProducto s1 = club.sugerirProducto(DatosBase.ENTRENADOR1, "Botilito", "desc");
        SugerenciaProducto s2 = club.sugerirProducto(DatosBase.FISIO, "Barra", "desc");
        club.aprobarSugerencia(DatosBase.ADMIN, s1);
        assertThrows(ReglaNegocioException.class, () -> club.aprobarSugerencia(DatosBase.ADMIN, s1));
        assertThrows(ReglaNegocioException.class, () -> club.aprobarSugerencia(DatosBase.ENTRENADOR2, s2));
    }

    // ---------- Reporte de ventas ----------

    private void registrarVentasDeHoy() {
        LocalDate hoy = LocalDate.now();
        Socio sofia = usuario(DatosBase.SOCIO1);
        Socio juan = usuario(DatosBase.SOCIO2);
        new Mensualidad(sofia, hoy.getYear() + "-" + hoy.getMonthValue(), 150000, hoy);
        new Mensualidad(juan, hoy.getYear() + "-" + hoy.getMonthValue(), 150000, hoy);
        club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.balon, 2), null);
        club.comprarTienda(DatosBase.SOCIO2, canasta(DatosTienda.camiseta, 1), null);
        club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.agua, 1), 0.10, 0);
    }

    @Test
    @DisplayName("El administrador genera el reporte de ventas de hoy")
    void reporteVentas() {
        registrarVentasDeHoy();
        LocalDate hoy = LocalDate.now();
        assertFalse(club.reporteVentas(DatosBase.ADMIN, hoy, hoy).isEmpty());
    }

    @Test
    @DisplayName("Reportes de ventas invalidos se rechazan")
    void reporteVentasInvalido() {
        registrarVentasDeHoy();
        LocalDate hoy = LocalDate.now();
        assertThrows(ReglaNegocioException.class,
                () -> club.reporteVentas(DatosBase.ADMIN, hoy, hoy.minusDays(1)));
        assertThrows(ReglaNegocioException.class,
                () -> club.reporteVentas(DatosBase.ADMIN, hoy.minusYears(2), hoy.minusYears(2).plusDays(30)));
        assertThrows(ReglaNegocioException.class,
                () -> club.reporteVentas(DatosBase.SOCIO1, hoy, hoy));
    }

    @Test
    @DisplayName("Inventario y sugerencias se conservan al guardar y cargar")
    void persistencia() {
        club.reabastecer(DatosBase.ADMIN, DatosTienda.raqueta, 5);
        club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Tienda", 6);
        club.sugerirProducto(DatosBase.ENTRENADOR1, "Botilito de agua", "desc");

        Club recuperado = guardarYRecargar();
        assertEquals(7, existencia(recuperado, "Tienda", DatosTienda.producto(recuperado, "Raqueta")));
        assertEquals(14, existencia(recuperado, "Bodega", DatosTienda.producto(recuperado, "Balon")));
        Entrenador carlos = (Entrenador) DatosBase.usuario(recuperado, DatosBase.ENTRENADOR1);
        assertEquals(1, carlos.getSugerencias().size());
    }

    private static Map<ArticuloTienda, Integer> canasta(ArticuloTienda articulo, int cantidad) {
        Map<ArticuloTienda, Integer> m = new LinkedHashMap<>();
        m.put(articulo, cantidad);
        return m;
    }

    private static Map<Producto, Integer> pedido(Producto producto, int cantidad) {
        Map<Producto, Integer> m = new LinkedHashMap<>();
        m.put(producto, cantidad);
        return m;
    }
}
