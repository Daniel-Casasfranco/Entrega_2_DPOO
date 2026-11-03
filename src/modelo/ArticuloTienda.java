package modelo;

public class ArticuloTienda extends Producto {

    private static final long serialVersionUID = 1L;

    private String categoria;

    public ArticuloTienda(String nombre, double precio, String categoria) {
        super(nombre, precio);
        this.categoria = categoria;
    }

    public String getCategoria() { return categoria; }
}
