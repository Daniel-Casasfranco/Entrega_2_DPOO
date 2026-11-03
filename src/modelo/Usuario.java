package modelo;

public abstract class Usuario implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final int codigo;
    private final String nombre;
    private final String apellido;
    private final String login;
    private final String password;
    protected Usuario(int codigo, String nombre, String apellido, String login, String password) {
        if (nombre == null || nombre.isBlank() || login == null || login.isBlank()
            || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Datos de usuario invalidos");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.login = login;
        this.password = password;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getLogin() {
        return login;
    }

    protected String getPasswordParaRegistro() {
        return password;
    }

    public boolean validarPassword(String candidata) {
        return password.equals(candidata);
    }

    public String consultarPerfil() {
        return codigo + " - " + nombre + " " + apellido;
    }
}
