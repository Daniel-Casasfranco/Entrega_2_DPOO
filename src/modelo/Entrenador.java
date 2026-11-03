package modelo;

public class Entrenador extends Empleado {
	
	private static final long serialVersionUID = 1L;
	
    public Entrenador(int codigo, String nombre, String apellido, String login, String password, String codigoDescuento) {
        super(codigo, nombre, apellido, login, password, codigoDescuento);
    }
}
