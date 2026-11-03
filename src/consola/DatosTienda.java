package consola;

import java.util.ArrayList;
import java.util.List;

import modelo.ArticuloTienda;
import modelo.Bebida;
import modelo.Club;
import modelo.ExistenciaInventario;
import modelo.Producto;
import modelo.Snack;
import modelo.UbicacionInventario;

public class DatosTienda {

	public static ArticuloTienda balon;
    public static ArticuloTienda camiseta;
    public static ArticuloTienda raqueta;
    public static Bebida agua;
    public static Bebida cafe;
    public static Snack galleta;

    public static void cargar(Club club) {
        balon = new ArticuloTienda("Balon", 50000, "Implementos");
        camiseta = new ArticuloTienda("Camiseta", 80000, "Ropa");
        raqueta = new ArticuloTienda("Raqueta", 200000, "Implementos");
        agua = new Bebida("Agua", 3000, false);
        cafe = new Bebida("Cafe", 5000, true);
        List<String> alergenos = new ArrayList<>();
        alergenos.add("gluten");
        alergenos.add("leche");
        galleta = new Snack("Galleta", 4000, alergenos);

        club.getProductos().add(balon);
        club.getProductos().add(camiseta);
        club.getProductos().add(raqueta);
        club.getProductos().add(agua);
        club.getProductos().add(cafe);
        club.getProductos().add(galleta);

        UbicacionInventario tienda = new UbicacionInventario("Tienda");
        UbicacionInventario bodega = new UbicacionInventario("Bodega");
        club.getUbicaciones().add(tienda);
        club.getUbicaciones().add(bodega);

        new ExistenciaInventario(tienda, balon, 10);
        new ExistenciaInventario(tienda, camiseta, 5);
        new ExistenciaInventario(tienda, raqueta, 2);
        new ExistenciaInventario(bodega, balon, 20);
        new ExistenciaInventario(bodega, camiseta, 10);
    }

    public static Producto producto(Club club, String nombre) {
        for (Producto p : club.getProductos()) {
            if (p.getNombre().equals(nombre)) {
                return p;
            }
        }
        return null;
    }
}
