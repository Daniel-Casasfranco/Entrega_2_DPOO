package modelo;

public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String motivo) {
        super(motivo);
    }
}
