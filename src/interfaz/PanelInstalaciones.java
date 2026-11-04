package interfaz;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.CanchaBaloncesto;
import modelo.CanchaFutbol;
import modelo.CanchaPadel;
import modelo.CanchaTenis;
import modelo.CanchaVoleibol;
import modelo.Club;
import modelo.Deporte;
import modelo.Instalacion;
import modelo.MesaTenisDeMesa;
import modelo.ReglaNegocioException;
import modelo.Reserva;
import modelo.Usuario;

/** Administracion de instalaciones y consulta de todas las reservas. */
public class PanelInstalaciones extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloInstalaciones =
            UI.modeloTabla("Codigo", "Nombre", "Disciplina", "Techada", "Reservas", "Sesiones");
    private final DefaultTableModel modeloReservas =
            UI.modeloTabla("Fecha", "Inicio", "Fin", "Instalacion", "Socio", "Modalidad", "Jugadores");

    private final JComboBox<Deporte> comboDisciplina = UI.combo(Arrays.asList(Deporte.values()), Listas::texto);
    private final JTextField campoCodigo = UI.campo("");
    private final JTextField campoNombre = UI.campo("");
    private final JCheckBox casillaTechada = new JCheckBox("Techada");

    public PanelInstalaciones(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Instalaciones y reservas"), BorderLayout.NORTH);

        JPanel tablas = new JPanel(new GridLayout(2, 1, 0, 10));
        tablas.add(UI.seccion("Instalaciones", UI.scroll(UI.tabla(modeloInstalaciones))));
        JTable tablaReservas = UI.tabla(modeloReservas);
        tablas.add(UI.seccion("Historial de reservas", UI.scroll(tablaReservas)));

        JPanel formulario = UI.seccion("Nueva instalacion", UI.formulario(
                "Disciplina:", comboDisciplina, "Codigo:", campoCodigo, "Nombre:", campoNombre,
                "", casillaTechada, UI.boton("Agregar instalacion", e -> agregar())));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablas, formulario);
        division.setResizeWeight(0.7);
        add(division, BorderLayout.CENTER);
    }

    private void agregar() {
        if (UI.ejecutar(this, () -> {
            String codigo = UI.texto(campoCodigo, "Codigo");
            String nombre = UI.texto(campoNombre, "Nombre");
            for (Instalacion i : club.getInstalaciones()) {
                if (i.getCodigo().equalsIgnoreCase(codigo)) {
                    throw new ReglaNegocioException("Ya existe una instalacion con el codigo " + codigo);
                }
            }
            club.getInstalaciones().add(crear(UI.seleccionado(comboDisciplina, "una disciplina"), codigo, nombre,
                    casillaTechada.isSelected()));
            club.guardar();
        }, "Instalacion agregada")) {
            campoCodigo.setText("");
            campoNombre.setText("");
            actualizar();
        }
    }

    static Instalacion crear(Deporte disciplina, String codigo, String nombre, boolean techada) {
        switch (disciplina) {
            case FUTBOL: return new CanchaFutbol(codigo, nombre, techada);
            case BALONCESTO: return new CanchaBaloncesto(codigo, nombre, techada);
            case VOLEIBOL: return new CanchaVoleibol(codigo, nombre, techada);
            case TENIS: return new CanchaTenis(codigo, nombre, techada);
            case PADEL: return new CanchaPadel(codigo, nombre, techada);
            default: return new MesaTenisDeMesa(codigo, nombre, techada);
        }
    }

    @Override
    public void actualizar() {
        modeloInstalaciones.setRowCount(0);
        for (Instalacion i : club.getInstalaciones()) {
            modeloInstalaciones.addRow(new Object[] {i.getCodigo(), i.getNombre(), Listas.texto(i.getDisciplina()),
                i.isTechada() ? "Si" : "No", i.getReservas().size(), i.getSesiones().size()});
        }
        modeloReservas.setRowCount(0);
        for (Reserva r : club.getReservas()) {
            modeloReservas.addRow(new Object[] {r.getFecha(), r.getHoraInicio(), r.getHoraFin(),
                r.getInstalacion().getNombre(), Listas.nombre(r.getSocio()), Listas.texto(r.getModalidad()),
                r.getNumeroJugadores()});
        }
    }
}
