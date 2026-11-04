package interfaz;

import java.awt.BorderLayout;
import java.time.format.DateTimeFormatter;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

import modelo.Club;
import modelo.LineaVenta;
import modelo.Socio;
import modelo.Usuario;
import modelo.Venta;
import modelo.VentaTienda;

/** Historial de compras del socio (RF-17). */
public class PanelMisCompras extends PanelClub {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DefaultTableModel modelo =
            UI.modeloTabla("Fecha", "Lugar", "Productos", "Subtotal", "Total", "Puntos ganados", "Puntos usados");
    private final JLabel puntos = new JLabel();

    public PanelMisCompras(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Mis compras"), BorderLayout.NORTH);
        add(UI.seccion("Historial", UI.scroll(UI.tabla(modelo))), BorderLayout.CENTER);
        add(puntos, BorderLayout.SOUTH);
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        puntos.setText("Puntos de fidelidad disponibles: " + ((Socio) usuario).getPuntosFidelidad());
        try {
            for (Venta v : club.consultarHistorialCompras(usuario.getCodigo())) {
                StringBuilder productos = new StringBuilder();
                for (LineaVenta l : v.getLineas()) {
                    if (productos.length() > 0) {
                        productos.append(", ");
                    }
                    productos.append(l.getCantidad()).append(" x ").append(l.getProducto().getNombre());
                }
                modelo.addRow(new Object[] {v.getFecha().format(FORMATO),
                    v instanceof VentaTienda ? "Tienda" : "Cafeteria", productos.toString(),
                    UI.dinero(v.getSubtotal()), UI.dinero(v.getTotal()), v.getPuntosGanados(),
                    v.getPuntosRedimidos()});
            }
        } catch (RuntimeException e) {
            // Aun no tiene compras
        }
    }
}
