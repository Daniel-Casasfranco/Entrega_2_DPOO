package modelo;

import java.util.ArrayList;

public class CanchaBaloncesto extends Instalacion {

    private static final long serialVersionUID = 1L;

    public CanchaBaloncesto(String codigo, String nombre, boolean techada) {
        super(codigo, nombre, Deporte.BALONCESTO, techada, new ArrayList<Modalidad>());
    }
}
