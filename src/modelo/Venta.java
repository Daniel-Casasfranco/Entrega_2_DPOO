package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Venta implements Serializable{
	
    private static final long serialVersionUID = 1L;

    protected static final double PORCENTAJE_PUNTOS = 0.02;

    protected LocalDateTime fecha;
    protected String metodoPago;
    protected Usuario comprador;
    protected double subtotal;
    protected double total;
    protected int puntosGanados;
    protected int puntosRedimidos;
    private List<LineaVenta> lineas;

    public Venta(Usuario comprador, Map<? extends Producto, Integer> productos) {
        this.comprador = comprador;
        this.fecha = LocalDateTime.now();
        this.metodoPago = "EFECTIVO";
        this.lineas = new ArrayList<>();
        for (Map.Entry<? extends Producto, Integer> e : productos.entrySet()) {
            LineaVenta linea = new LineaVenta(e.getKey(), e.getValue());
            lineas.add(linea);
            subtotal += linea.calcularSubtotal();
        }
    }

    public abstract double calcularTotal();

    protected void terminar() {
        total = redondear(calcularTotal());
        puntosGanados = (int) (total * PORCENTAJE_PUNTOS);
    }

    protected static double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

    public LocalDateTime getFecha() { return fecha; }
    public String getMetodoPago() { return metodoPago; }
    public Usuario getComprador() { return comprador; }
    public double getSubtotal() { return subtotal; }
    public double getTotal() { return total; }
    public int getPuntosGanados() { return puntosGanados; }
    public int getPuntosRedimidos() { return puntosRedimidos; }
    public List<LineaVenta> getLineas() { return lineas; }

    @Override
    public String toString() {
        return fecha.toLocalDate() + " | " + comprador.getNombre() + " | " + lineas
                + " | subtotal: " + subtotal + " | total: " + total
                + " | puntos ganados: " + puntosGanados;
    }
}
