package interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.Administrador;
import modelo.Club;
import modelo.Empleado;
import modelo.SugerenciaProducto;
import modelo.Usuario;

/** Sugerencias de productos: los empleados sugieren y el administrador aprueba o rechaza (RF-39). */
public class PanelSugerencias extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modelo = UI.modeloTabla("Producto", "Descripcion", "Sugerido por", "Estado");
    private final JTable tabla = UI.tabla(modelo);
    private final List<SugerenciaProducto> sugerencias = new ArrayList<>();

    private final JTextField campoNombre = UI.campo("");
    private final JTextField campoDescripcion = UI.campo("");

    public PanelSugerencias(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Sugerencias de productos"), BorderLayout.NORTH);

        if (usuario instanceof Administrador) {
            JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            botones.add(UI.boton("Aprobar seleccionada", e -> evaluar(true)));
            botones.add(UI.boton("Rechazar seleccionada", e -> evaluar(false)));
            add(UI.seccion("Sugerencias de los empleados", UI.scroll(tabla)), BorderLayout.CENTER);
            add(botones, BorderLayout.SOUTH);
        } else {
            JPanel formulario = UI.seccion("Nueva sugerencia", UI.formulario(
                    "Producto:", campoNombre, "Descripcion:", campoDescripcion,
                    UI.boton("Enviar sugerencia", e -> sugerir())));
            JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    UI.seccion("Mis sugerencias", UI.scroll(tabla)), formulario);
            division.setResizeWeight(0.6);
            add(division, BorderLayout.CENTER);
        }
    }

    private void sugerir() {
        if (UI.ejecutar(this, () -> club.sugerirProducto(usuario.getCodigo(), UI.texto(campoNombre, "Producto"),
                campoDescripcion.getText().trim()), "Sugerencia enviada al administrador")) {
            campoNombre.setText("");
            campoDescripcion.setText("");
            actualizar();
        }
    }

    private void evaluar(boolean aprobar) {
        int fila = UI.filaSeleccionada(tabla);
        if (fila < 0) {
            UI.error(this, "Seleccione una sugerencia");
            return;
        }
        SugerenciaProducto s = sugerencias.get(fila);
        if (UI.ejecutar(this, () -> {
            if (aprobar) {
                club.aprobarSugerencia(usuario.getCodigo(), s);
            } else {
                club.rechazarSugerencia(usuario.getCodigo(), s);
            }
        }, aprobar ? "Sugerencia aprobada" : "Sugerencia rechazada")) {
            actualizar();
        }
    }

    @Override
    public void actualizar() {
        sugerencias.clear();
        if (usuario instanceof Administrador) {
            sugerencias.addAll(club.consultarSugerencias(usuario.getCodigo()));
        } else {
            sugerencias.addAll(((Empleado) usuario).getSugerencias());
        }
        modelo.setRowCount(0);
        for (SugerenciaProducto s : sugerencias) {
            modelo.addRow(new Object[] {s.getNombre(), s.getDescripcion(), Listas.nombre(s.getEmpleado()),
                Listas.texto(s.getEstado())});
        }
    }
}
