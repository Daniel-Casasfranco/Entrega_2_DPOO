package interfaz;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.ArticuloTienda;
import modelo.Club;
import modelo.Producto;
import modelo.UbicacionInventario;
import modelo.Usuario;

/** Inventario: consultar por ubicacion, reabastecer la tienda y mover articulos (RF-35, RF-36). */
public class PanelInventario extends PanelClub {

    private static final long serialVersionUID = 1L;

    private static final String TODAS = "Todas las ubicaciones";

    private final DefaultTableModel modelo = UI.modeloTabla("Ubicacion", "Producto", "Cantidad");
    private final JComboBox<String> comboFiltro = UI.combo(new ArrayList<String>(), s -> s);

    private final JComboBox<ArticuloTienda> comboArticulo =
            UI.combo(new ArrayList<ArticuloTienda>(), Producto::getNombre);
    private final JTextField campoCantidadReabastecer = UI.campo("1");

    private final JComboBox<Producto> comboProducto = UI.combo(new ArrayList<Producto>(), Producto::getNombre);
    private final JComboBox<String> comboOrigen = UI.combo(new ArrayList<String>(), s -> s);
    private final JComboBox<String> comboDestino = UI.combo(new ArrayList<String>(), s -> s);
    private final JTextField campoCantidadMover = UI.campo("1");

    public PanelInventario(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Inventario"), BorderLayout.NORTH);

        comboFiltro.addActionListener(e -> mostrarInventario());
        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.add(UI.formulario("Ver:", comboFiltro), BorderLayout.NORTH);
        izquierda.add(UI.scroll(UI.tabla(modelo)), BorderLayout.CENTER);

        JPanel formularios = new JPanel(new GridLayout(2, 1, 0, 10));
        formularios.add(UI.seccion("Reabastecer la tienda", UI.formulario(
                "Articulo:", comboArticulo, "Cantidad:", campoCantidadReabastecer,
                UI.boton("Reabastecer", e -> reabastecer()))));
        formularios.add(UI.seccion("Mover entre ubicaciones", UI.formulario(
                "Producto:", comboProducto, "Origen:", comboOrigen, "Destino:", comboDestino,
                "Cantidad:", campoCantidadMover, UI.boton("Mover", e -> mover()))));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UI.seccion("Existencias", izquierda), formularios);
        division.setResizeWeight(0.6);
        add(division, BorderLayout.CENTER);
    }

    private void reabastecer() {
        if (UI.ejecutar(this, () -> club.reabastecer(usuario.getCodigo(),
                UI.seleccionado(comboArticulo, "un articulo"), UI.entero(campoCantidadReabastecer, "Cantidad")),
                "Tienda reabastecida")) {
            mostrarInventario();
        }
    }

    private void mover() {
        if (UI.ejecutar(this, () -> club.moverArticulo(usuario.getCodigo(),
                UI.seleccionado(comboProducto, "un producto"), UI.seleccionado(comboOrigen, "el origen"),
                UI.seleccionado(comboDestino, "el destino"), UI.entero(campoCantidadMover, "Cantidad")),
                "Articulos movidos")) {
            mostrarInventario();
        }
    }

    private void mostrarInventario() {
        modelo.setRowCount(0);
        String filtro = (String) comboFiltro.getSelectedItem();
        String ubicacion = filtro == null || TODAS.equals(filtro) ? null : filtro;
        try {
            for (String linea : club.consultarInventario(usuario.getCodigo(), ubicacion)) {
                String[] partes = linea.split(" \\| ");
                modelo.addRow(new Object[] {partes[0], partes[1], Integer.parseInt(partes[2])});
            }
        } catch (RuntimeException e) {
            // Sin existencias en la ubicacion: la tabla queda vacia
        }
    }

    @Override
    public void actualizar() {
        List<String> ubicaciones = new ArrayList<>();
        for (UbicacionInventario u : club.getUbicaciones()) {
            ubicaciones.add(u.getNombre());
        }
        List<String> filtros = new ArrayList<>();
        filtros.add(TODAS);
        filtros.addAll(ubicaciones);
        UI.recargar(comboFiltro, filtros);
        UI.recargar(comboOrigen, ubicaciones);
        UI.recargar(comboDestino, ubicaciones);
        if (ubicaciones.size() > 1 && comboDestino.getSelectedIndex() == comboOrigen.getSelectedIndex()) {
            comboDestino.setSelectedIndex(1);
        }
        UI.recargar(comboArticulo, Listas.articulos(club));
        UI.recargar(comboProducto, club.getProductos());
        mostrarInventario();
    }
}
