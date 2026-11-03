package modelo;

import java.io.Serializable;

public class ReglaDeporte implements Serializable {

    private static final long serialVersionUID = 1L;

    private Deporte deporte;
    private int jugadoresMinimos;
    private boolean permiteEmpate;
    private int puntosVictoria;
    private int puntosEmpate;
    private int puntosDerrota;
    private int maxInstalacionesSimultaneas;

    public ReglaDeporte(Deporte deporte, int jugadoresMinimos, boolean permiteEmpate,
            int puntosVictoria, int puntosEmpate, int puntosDerrota,
            int maxInstalacionesSimultaneas) {
        this.deporte = deporte;
        this.jugadoresMinimos = jugadoresMinimos;
        this.permiteEmpate = permiteEmpate;
        this.puntosVictoria = puntosVictoria;
        this.puntosEmpate = puntosEmpate;
        this.puntosDerrota = puntosDerrota;
        this.maxInstalacionesSimultaneas = maxInstalacionesSimultaneas;
    }

    public int maximo() {
        return maxInstalacionesSimultaneas;
    }

    public Deporte getDeporte() { return deporte; }
    public int getJugadoresMinimos() { return jugadoresMinimos; }
    public boolean isPermiteEmpate() { return permiteEmpate; }
    public int getPuntosVictoria() { return puntosVictoria; }
    public int getPuntosEmpate() { return puntosEmpate; }
    public int getPuntosDerrota() { return puntosDerrota; }
}
