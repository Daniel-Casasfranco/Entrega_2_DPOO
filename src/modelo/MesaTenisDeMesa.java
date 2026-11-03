package modelo;

import java.util.ArrayList;
import java.util.List;

public class MesaTenisDeMesa extends Instalacion {

    private static final long serialVersionUID = 1L;

    public MesaTenisDeMesa(String codigo, String nombre, boolean techada) {
        super(codigo, nombre, Deporte.TENIS_DE_MESA, techada, modalidades());
    }

    private static List<Modalidad> modalidades() {
        List<Modalidad> lista = new ArrayList<>();
        lista.add(Modalidad.SENCILLOS);
        lista.add(Modalidad.DOBLES);
        return lista;
    }
}
