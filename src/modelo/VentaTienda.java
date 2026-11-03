package modelo;

import java.util.Map;

public class VentaTienda extends Venta {

    private static final long serialVersionUID = 1L;

    private static final double TASA_IVA = 0.19;

    private double iva;
    private String codigoDescuentoUsado;
    private double descuento;

    public VentaTienda(Usuario comprador, Map<ArticuloTienda, Integer> articulos, double porcentajeDescuento) {
        super(comprador, articulos);
        this.descuento = redondear(subtotal * porcentajeDescuento);
        this.iva = redondear((subtotal - descuento) * TASA_IVA);
        terminar();
    }

    @Override
    public double calcularTotal() {
        return subtotal - descuento + iva;
    }

    public double getIva() { return iva; }
    public double getDescuento() { return descuento; }
    public String getCodigoDescuentoUsado() { return codigoDescuentoUsado; }
    public void setCodigoDescuentoUsado(String codigo) { this.codigoDescuentoUsado = codigo; }

    @Override
    public String toString() {
        return "TIENDA | " + super.toString() + " | descuento: " + descuento + " | IVA: " + iva;
    }
}

