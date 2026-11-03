package modelo;

import java.io.Serializable;

public class ExistenciaInventario implements Serializable {

    private static final long serialVersionUID = 1L;

    private Producto producto;
    private int cantidad;

    public ExistenciaInventario(UbicacionInventario ubicacion, Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        ubicacion.agregarExistencia(this);
    }

    public boolean hayStock(int cantidadPedida) {
        return cantidad >= cantidadPedida;
    }

    public void descontar(int cantidadPedida) {
        if (!hayStock(cantidadPedida)) {
            throw new ReglaNegocioException("No hay suficientes unidades de " + producto.getNombre());
        }
        cantidad -= cantidadPedida;
    }

    public void aumentar(int cantidadNueva) {
        cantidad += cantidadNueva;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
}
