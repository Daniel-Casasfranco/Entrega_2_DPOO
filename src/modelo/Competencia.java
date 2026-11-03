package modelo;

import java.util.ArrayList;
import java.util.List;

public abstract class Competencia  implements java.io.Serializable {
   
	private static final long serialVersionUID = 1L;
	
	private final String nombre;
    private final String entidadOrganizadora;
    private final String temporada;
    private final Deporte deporte;
    private final Categoria categoria;
    private final int nominaMinima;
    private final List<InscripcionEquipo> inscripciones = new ArrayList<>();
    private final List<PartidoCompeticion> partidos = new ArrayList<>();
    protected Competencia(String nombre, String entidadOrganizadora, String temporada,
        Deporte deporte, Categoria categoria, int nominaMinima) {
        if (nombre == null || nombre.isBlank() || deporte == null || categoria == null || nominaMinima <= 0)
            throw new IllegalArgumentException("Datos de competencia invalidos");
        this.nombre = nombre;
        this.entidadOrganizadora = entidadOrganizadora;
        this.temporada = temporada;
        this.deporte = deporte;
        this.categoria = categoria;
        this.nominaMinima = nominaMinima;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEntidadOrganizadora() {
        return entidadOrganizadora;
    }

    public String getTemporada() {
        return temporada;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public int getNominaMinima() {
        return nominaMinima;
    }

    public List<InscripcionEquipo> getInscripciones() {
        return new ArrayList<>(inscripciones);
    }

    public List<PartidoCompeticion> getPartidos() {
        return new ArrayList<>(partidos);
    }

    public InscripcionEquipo inscribirEquipo(Equipo equipo) {
        if (equipo == null || equipo.getDeporte() != deporte || equipo.getCategoria() != categoria)
            throw new IllegalArgumentException("Equipo de otra categoria o deporte");
        if (equipo.getJugadores().size() < nominaMinima)
            throw new IllegalArgumentException("No cumple nomina minima");
        for (InscripcionEquipo inscripcion : inscripciones) {
            if (inscripcion.getEquipo() == equipo) {
                throw new IllegalArgumentException("Equipo ya inscrito");
            }
        }
        InscripcionEquipo inscripcion = new InscripcionEquipo(equipo, this);
        inscripciones.add(inscripcion);
        return inscripcion;
    }

    public InscripcionEquipo buscarInscripcion(Equipo equipo) {
        for (InscripcionEquipo i : inscripciones) {
            if (i.getEquipo() == equipo) {
                return i;
            }
        }
        return null;
    }

    public List<InscripcionEquipo> tablaPosiciones() {
        List<InscripcionEquipo> tabla = new ArrayList<>(inscripciones);
        tabla.sort((a, b) -> Integer.compare(b.getPuntos(), a.getPuntos()));
        return tabla;
    }

    public void agregarPartido(PartidoCompeticion partido) {
        if (partido == null || partido.getDeporte() != deporte)
            throw new IllegalArgumentException("Partido incompatible");
        if (partidos.contains(partido)) {
            throw new IllegalArgumentException("Partido duplicado");
        }
        if (buscarInscripcion(partido.getEquipo1()) == null)
            throw new IllegalArgumentException("Equipo no inscrito");
        partido.setCompetencia(this);
        partidos.add(partido);
    }
}
