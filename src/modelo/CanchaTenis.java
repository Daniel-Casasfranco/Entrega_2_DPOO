package modelo;

import java.util.ArrayList;
import java.util.List;

public class CanchaTenis extends Instalacion {

    private static final long serialVersionUID = 1L;

    public CanchaTenis(String codigo, String nombre, boolean techada) {
        super(codigo, nombre, Deporte.TENIS, techada, modalidades());
    }

    private static List<Modalidad> modalidades() {
        List<Modalidad> lista = new ArrayList<>();
        lista.add(Modalidad.SENCILLOS);
        lista.add(Modalidad.DOBLES);
        return lista;
    }
}
