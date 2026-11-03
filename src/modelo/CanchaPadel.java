package modelo;

import java.util.ArrayList;
import java.util.List;

public class CanchaPadel extends Instalacion {

    private static final long serialVersionUID = 1L;

    public CanchaPadel(String codigo, String nombre, boolean techada) {
        super(codigo, nombre, Deporte.PADEL, techada, modalidades());
    }

    private static List<Modalidad> modalidades() {
        List<Modalidad> lista = new ArrayList<>();
        lista.add(Modalidad.DOBLES);
        return lista;
    }
}
