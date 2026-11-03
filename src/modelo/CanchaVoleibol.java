package modelo;

import java.util.ArrayList;

public class CanchaVoleibol extends Instalacion {

    private static final long serialVersionUID = 1L;

    public CanchaVoleibol(String codigo, String nombre, boolean techada) {
        super(codigo, nombre, Deporte.VOLEIBOL, techada, new ArrayList<Modalidad>());
    }
}
