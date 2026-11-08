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

import modelo.Bebida;
import modelo.Club;
import modelo.Producto;
import modelo.Snack;
import modelo.Socio;
import modelo.Usuario;
import modelo.VentaCafeteria;

/**
 * Compras en la cafeteria: impuesto al consumo, propina, puntos de fidelidad y
 * aviso de alergenos (RF-13 a RF-16).
 */

public class PanelCafeteria extends PanelClub {

    private static final long serialVersionUID = 1L;
    //Creo tabla 
    private final DefaultTableModel modeloProductos = UI.modeloTabla("Producto", "Tipo", "Precio", "Alergenos");
    
    private final JTable tablaProductos = UI.tabla(modeloProductos);
    
    private final List<Producto> productos = new ArrayList<>();

    private final DefaultTableModel modeloPedido = UI.modeloTabla("Producto", "Cantidad", "Subtotal");
    private final JTable tablaPedido = UI.tabla(modeloPedido);
    
    private final Map<Producto, Integer> pedido = new LinkedHashMap<>();

    private final JTextField campoCantidad = UI.campo("1");
    private final JTextField campoPropina = UI.campo("10");
    private final JTextField campoPuntos = UI.campo("0");
    private final JLabel resumen = new JLabel(" ");

    public PanelCafeteria(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Cafeteria"), BorderLayout.NORTH);

        JPanel izquierda = new JPanel(new BorderLayout(6, 6));
        izquierda.add(UI.scroll(tablaProductos), BorderLayout.CENTER);
        izquierda.add(UI.formulario("Cantidad:", campoCantidad, UI.boton("Agregar al pedido", e -> agregar())),
                BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new BorderLayout(6, 6));
        derecha.add(UI.scroll(tablaPedido), BorderLayout.CENTER);
        JPanel pie = new JPanel(new GridLayout(2, 1));
        pie.add(resumen);
        pie.add(UI.formulario("Propina (%):", campoPropina, "Puntos a usar:", campoPuntos,
                UI.boton("Quitar seleccionado", e -> quitar()), UI.boton("Pagar", e -> pagar())));
        derecha.add(pie, BorderLayout.SOUTH);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UI.seccion("Menu", izquierda), UI.seccion("Mi pedido", derecha));
        division.setResizeWeight(0.55);
        add(division, BorderLayout.CENTER);
    }

    private void agregar() {
        int fila = UI.filaSeleccionada(tablaProductos);
        if (fila < 0) {
            UI.error(this, "Seleccione un producto");
            return;
        }
        Producto p = productos.get(fila);
        if (p instanceof Snack && !((Snack) p).getAlergenos().isEmpty()) {
            UI.info(this, "Atencion: " + p.getNombre() + " contiene " + String.join(", ", ((Snack) p).getAlergenos()));
        }
        UI.ejecutar(this, () -> {
            pedido.put(p, pedido.getOrDefault(p, 0) + UI.entero(campoCantidad, "Cantidad"));
            mostrarPedido();
        }, null);
    }

    private void quitar() {
        int fila = UI.filaSeleccionada(tablaPedido);
        if (fila >= 0) {
            pedido.remove(new ArrayList<>(pedido.keySet()).get(fila));
            mostrarPedido();
        }
    }

    private void pagar() {
        VentaCafeteria[] venta = new VentaCafeteria[1];
        if (UI.ejecutar(this, () -> venta[0] = club.comprarCafeteria(usuario.getCodigo(), pedido,
                UI.decimal(campoPropina, "Propina") / 100.0, UI.entero(campoPuntos, "Puntos a usar")), null)) {
            UI.info(this, "Pago realizado\n\nSubtotal: " + UI.dinero(venta[0].getSubtotal())
                    + "\nPuntos usados: " + venta[0].getPuntosRedimidos()
                    + "\nImpuesto al consumo (8%): " + UI.dinero(venta[0].getImpuestoConsumo())
                    + "\nPropina: " + UI.dinero(venta[0].getPropina())
                    + "\nTotal: " + UI.dinero(venta[0].getTotal())
                    + "\nPuntos ganados: " + venta[0].getPuntosGanados());
            pedido.clear();
            campoPuntos.setText("0");
            actualizar();
        }
    }

    private void mostrarPedido() {
        modeloPedido.setRowCount(0);
        double subtotal = 0;
        for (Map.Entry<Producto, Integer> e : pedido.entrySet()) {
            double valor = e.getKey().getPrecio() * e.getValue();
            subtotal += valor;
            modeloPedido.addRow(new Object[] {e.getKey().getNombre(), e.getValue(), UI.dinero(valor)});
        }
        resumen.setText("Subtotal: " + UI.dinero(subtotal) + "   |   Mis puntos: "
                + ((Socio) usuario).getPuntosFidelidad());
    }

    @Override
    public void actualizar() {
        productos.clear();
        productos.addAll(Listas.productosCafeteria(club));
        modeloProductos.setRowCount(0);
        for (Producto p : productos) {
            String tipo = p instanceof Bebida ? (((Bebida) p).isCaliente() ? "Bebida caliente" : "Bebida fria") : "Snack";
            String alergenos = p instanceof Snack ? String.join(", ", ((Snack) p).getAlergenos()) : "";
            modeloProductos.addRow(new Object[] {p.getNombre(), tipo, UI.dinero(p.getPrecio()), alergenos});
        }
        mostrarPedido();
    }
}
