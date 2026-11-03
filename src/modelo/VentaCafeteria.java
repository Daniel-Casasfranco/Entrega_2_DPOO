package modelo;

import java.util.Map;

public class VentaCafeteria extends Venta {

    private static final long serialVersionUID = 1L;

    private static final double TASA_CONSUMO = 0.08;

    private double impuestoConsumo;
    private double propina;  
   
    public VentaCafeteria(Socio socio, Map<Producto, Integer> productos, double porcentajePropina, int puntosUsar) {
        super(socio, productos);
        this.puntosRedimidos = puntosUsar;
        this.impuestoConsumo = redondear((subtotal - puntosUsar) * TASA_CONSUMO);
        this.propina = redondear(subtotal * porcentajePropina);
        terminar();
    }

    @Override
    public double calcularTotal() {
        return subtotal - puntosRedimidos + impuestoConsumo + propina;
    }

    public double getImpuestoConsumo() { return impuestoConsumo; }
    public double getPropina() { return propina; }

    @Override
    public String toString() {
        return "CAFETERIA | " + super.toString() + " | puntos usados: " + puntosRedimidos
                + " | impuesto consumo: " + impuestoConsumo + " | propina: " + propina;
    }
}
