package modelo;

public class Bebida extends Producto {

    private static final long serialVersionUID = 1L;

    private boolean caliente;

    public Bebida(String nombre, double precio, boolean caliente) {
        super(nombre, precio);
        this.caliente = caliente;
    }

    public boolean isCaliente() { return caliente; }

    @Override
    public boolean permitido(boolean enTechada) {
        return !(caliente && enTechada);
    }
}
