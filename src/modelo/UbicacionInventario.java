package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UbicacionInventario implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private List<ExistenciaInventario> existencias;

    public UbicacionInventario(String nombre) {
        this.nombre = nombre;
        this.existencias = new ArrayList<>();
    }

    public void agregarExistencia(ExistenciaInventario existencia) {
        existencias.add(existencia);
    }

    public ExistenciaInventario buscarExistencia(Producto producto) {
        for (ExistenciaInventario e : existencias) {
            if (e.getProducto() == producto) {
                return e;
            }
        }
        return null;
    }

    public void agregar(Producto producto, int cantidad) {
        ExistenciaInventario existencia = buscarExistencia(producto);
        if (existencia == null) {
            new ExistenciaInventario(this, producto, cantidad);
        } else {
            existencia.aumentar(cantidad);
        }
    }

    public String getNombre() { return nombre; }
    public List<ExistenciaInventario> getExistencias() { return existencias; }

    @Override
    public String toString() {
        return nombre;
    }
}

