package modelo;

import java.util.ArrayList;

public class CanchaFutbol extends Instalacion {

    private static final long serialVersionUID = 1L;

    public CanchaFutbol(String codigo, String nombre, boolean techada) {
        super(codigo, nombre, Deporte.FUTBOL, techada, new ArrayList<Modalidad>());
    }
}
