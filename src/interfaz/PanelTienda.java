package interfaz;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.ArticuloTienda;
import modelo.Club;
import modelo.ExistenciaInventario;
import modelo.UbicacionInventario;
import modelo.Usuario;
import modelo.VentaTienda;

/** Compras en la tienda del club con IVA y codigos de descuento (RF-12, RF-18). */
public class PanelTienda extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloArticulos =
            UI.modeloTabla("Articulo", "Categoria", "Precio", "Disponibles");
    private final JTable tablaArticulos = UI.tabla(modeloArticulos);
    private final List<ArticuloTienda> articulos = new ArrayList<>();

    private final DefaultTableModel modeloCanasta = UI.modeloTabla("Articulo", "Cantidad", "Subtotal");
    private final JTable tablaCanasta = UI.tabla(modeloCanasta);
    private final Map<ArticuloTienda, Integer> canasta = new LinkedHashMap<>();

    private final JTextField campoCantidad = UI.campo("1");
    private final JTextField campoCodigo = UI.campo("");
    private final JLabel resumen = new JLabel(" ");

    public PanelTienda(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Tienda"), BorderLayout.NORTH);

        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.add(UI.scroll(tablaArticulos), BorderLayout.CENTER);
        izquierda.add(UI.formulario("Cantidad:", campoCantidad, UI.boton("Agregar a la canasta", e -> agregar())),
                BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new BorderLayout(6, 6));
        derecha.add(UI.scroll(tablaCanasta), BorderLayout.CENTER);
        JPanel pie = new JPanel(new GridLayout(2, 1));
        pie.add(resumen);
        pie.add(UI.formulario("Codigo de descuento (opcional):", campoCodigo,
                UI.boton("Quitar seleccionado", e -> quitar()), UI.boton("Comprar", e -> comprar())));
        derecha.add(pie, BorderLayout.SOUTH);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UI.seccion("Articulos", izquierda), UI.seccion("Canasta (precios antes de IVA)", derecha));
        division.setResizeWeight(0.55);
        add(division, BorderLayout.CENTER);
    }

    private void agregar() {
        int fila = UI.filaSeleccionada(tablaArticulos);
        if (fila < 0) {
            UI.error(this, "Seleccione un articulo");
            return;
        }
        UI.ejecutar(this, () -> {
            int cantidad = UI.entero(campoCantidad, "Cantidad");
            ArticuloTienda a = articulos.get(fila);
            canasta.put(a, canasta.getOrDefault(a, 0) + cantidad);
            mostrarCanasta();
        }, null);
    }

    private void quitar() {
        int fila = UI.filaSeleccionada(tablaCanasta);
        if (fila >= 0) {
            canasta.remove(new ArrayList<>(canasta.keySet()).get(fila));
            mostrarCanasta();
        }
    }

    private void comprar() {
        VentaTienda[] venta = new VentaTienda[1];
        String codigo = campoCodigo.getText().trim();
        if (UI.ejecutar(this, () -> venta[0] = club.comprarTienda(usuario.getCodigo(), canasta,
                codigo.isEmpty() ? null : codigo), null)) {
            UI.info(this, "Compra realizada\n\nSubtotal: " + UI.dinero(venta[0].getSubtotal())
                    + "\nDescuento: " + UI.dinero(venta[0].getDescuento())
                    + "\nIVA (19%): " + UI.dinero(venta[0].getIva())
                    + "\nTotal: " + UI.dinero(venta[0].getTotal())
                    + "\nPuntos ganados: " + venta[0].getPuntosGanados());
            canasta.clear();
            campoCodigo.setText("");
            actualizar();
        }
    }

    private void mostrarCanasta() {
        modeloCanasta.setRowCount(0);
        double subtotal = 0;
        for (Map.Entry<ArticuloTienda, Integer> e : canasta.entrySet()) {
            double valor = e.getKey().getPrecio() * e.getValue();
            subtotal += valor;
            modeloCanasta.addRow(new Object[] {e.getKey().getNombre(), e.getValue(), UI.dinero(valor)});
        }
        resumen.setText("Subtotal: " + UI.dinero(subtotal) + "   (se agrega 19% de IVA al comprar)");
    }

    @Override
    public void actualizar() {
        UbicacionInventario tienda = null;
        for (UbicacionInventario u : club.getUbicaciones()) {
            if (u.getNombre().equalsIgnoreCase("Tienda")) {
                tienda = u;
            }
        }
        articulos.clear();
        articulos.addAll(Listas.articulos(club));
        modeloArticulos.setRowCount(0);
        for (ArticuloTienda a : articulos) {
            ExistenciaInventario e = tienda == null ? null : tienda.buscarExistencia(a);
            modeloArticulos.addRow(new Object[] {a.getNombre(), a.getCategoria(), UI.dinero(a.getPrecio()),
                e == null ? 0 : e.getCantidad()});
        }
        mostrarCanasta();
    }
}
