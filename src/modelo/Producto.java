package modelo;

import java.io.Serializable;

public abstract class Producto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String nombre;
    protected double precio;

    public Producto(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public boolean permitido(boolean enTechada) {
        return true;
    }

    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
}
