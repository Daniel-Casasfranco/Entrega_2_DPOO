package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class Mensualidad implements Serializable {

    private static final long serialVersionUID = 1L;

    private Socio socio;
    private String periodo;     
    private double valor;
    private LocalDate fechaPago;

    public Mensualidad(Socio socio, String periodo, double valor, LocalDate fechaPago) {
        this.socio = socio;
        this.periodo = periodo;
        this.valor = valor;
        this.fechaPago = fechaPago;
        socio.agregarMensualidad(this);
    }

    public Socio getSocio() { return socio; }
    public String getPeriodo() { return periodo; }
    public double getValor() { return valor; }
    public LocalDate getFechaPago() { return fechaPago; }
}
