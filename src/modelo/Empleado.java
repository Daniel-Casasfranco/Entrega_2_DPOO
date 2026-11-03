package modelo;

public abstract class Empleado extends Usuario {
	
	private static final long serialVersionUID = 1L;
	
    private final String codigoDescuento;
    private final java.util.List<Turno> turnos = new java.util.ArrayList<>();
    protected Empleado(int codigo, String nombre, String apellido, String login, String password, String codigoDescuento) {
        super(codigo, nombre, apellido, login, password);
        this.codigoDescuento = codigoDescuento;
    }

    public String getCodigoDescuento() {
        return codigoDescuento;
    }
    private final java.util.List<SugerenciaProducto> sugerencias = new java.util.ArrayList<>();

    public void agregarSugerencia(SugerenciaProducto sugerencia) {
        sugerencias.add(sugerencia);
    }

    public java.util.List<SugerenciaProducto> getSugerencias() {
        return sugerencias;
    }

    public void agregarTurno(Turno turno) {
        turnos.add(turno);
    }

    public java.util.List<Turno> getTurnos() {
        return turnos;
    }
}
