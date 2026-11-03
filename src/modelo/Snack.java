package modelo;

import java.util.ArrayList;
import java.util.List;

public class Snack extends Producto {

    private static final long serialVersionUID = 1L;

    private List<String> alergenos;

    public Snack(String nombre, double precio, List<String> alergenos) {
        super(nombre, precio);
        this.alergenos = new ArrayList<>(alergenos);
    }

    public List<String> getAlergenos() { return alergenos; }
}
