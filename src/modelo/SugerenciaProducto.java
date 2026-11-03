package modelo;

import java.io.Serializable;

public class SugerenciaProducto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Empleado empleado;
    private String nombre;
    private String descripcion;
    private EstadoSolicitud estado;

    public SugerenciaProducto(Empleado empleado, String nombre, String descripcion) {
        this.empleado = empleado;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = EstadoSolicitud.PENDIENTE;
        empleado.agregarSugerencia(this);
    }

    public void aprobar() { estado = EstadoSolicitud.APROBADA; }
    public void rechazar() { estado = EstadoSolicitud.RECHAZADA; }

    public Empleado getEmpleado() { return empleado; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public EstadoSolicitud getEstado() { return estado; }

    @Override
    public String toString() {
        return nombre + " (" + descripcion + ") sugerido por " + empleado.getNombre() + " -> " + estado;
    }
}
