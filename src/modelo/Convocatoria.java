package modelo;

import java.io.Serializable;

public class Convocatoria implements Serializable {

    private static final long serialVersionUID = 1L;

    private SesionEntrenamiento sesion;
    private Socio socio;
    private Asistencia asistencia;   
    private int nota;               
    private String comentario;     

    public Convocatoria(SesionEntrenamiento sesion, Socio socio) {
        this.sesion = sesion;
        this.socio = socio;
        sesion.agregarConvocatoria(this);
    }

    public void registrar(Asistencia asistencia, int nota, String comentario) {
        if (asistencia == null) {
            throw new ReglaNegocioException("Debe indicar la asistencia");
        }
        if (nota != 0 && (nota < 1 || nota > 5)) {
            throw new ReglaNegocioException("La nota debe estar entre 1 y 5");
        }
        this.asistencia = asistencia;
        this.nota = nota;
        this.comentario = comentario;
    }

    public SesionEntrenamiento getSesion() { return sesion; }
    public Socio getSocio() { return socio; }
    public Asistencia getAsistencia() { return asistencia; }
    public int getNota() { return nota; }
    public String getComentario() { return comentario; }

    @Override
    public String toString() {
        String texto = sesion.getFecha() + " " + sesion.getHora() + " | "
                + sesion.getInstalacion().getNombre() + " | " + socio.getNombre()
                + " | asistencia: " + (asistencia == null ? "sin registrar" : asistencia.toString());
        if (nota > 0) {
            texto += " | nota: " + nota;
        }
        if (comentario != null) {
            texto += " | comentario: " + comentario;
        }
        return texto;
    }
}
