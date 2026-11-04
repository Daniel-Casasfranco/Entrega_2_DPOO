package interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.Administrador;
import modelo.Club;
import modelo.Empleado;
import modelo.SolicitudCambioTurno;
import modelo.Turno;
import modelo.Usuario;

/**
 * Turnos de trabajo. El administrador consulta todos los turnos y aprueba o rechaza las
 * solicitudes; los empleados ven sus turnos y piden cambios (RF-37, RF-38).
 */
public class PanelTurnos extends PanelClub {

    private static final long serialVersionUID = 1L;

    static final List<String> DIAS = Arrays.asList("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES",
            "SABADO", "DOMINGO");

    private final DefaultTableModel modeloTurnos = UI.modeloTabla("Empleado", "Dia", "Inicio", "Fin");
    private final JTable tablaTurnos = UI.tabla(modeloTurnos);
    private final List<Turno> turnos = new ArrayList<>();

    private final DefaultTableModel modeloSolicitudes =
            UI.modeloTabla("Empleado", "Turno actual", "Nuevo horario", "Motivo", "Estado");
    private final JTable tablaSolicitudes = UI.tabla(modeloSolicitudes);
    private final List<SolicitudCambioTurno> solicitudes = new ArrayList<>();

    private final JTextField campoMotivo = UI.campo("");
    private final JComboBox<String> comboDia = UI.combo(DIAS, s -> s);
    private final JTextField campoInicio = UI.campo("08:00");
    private final JTextField campoFin = UI.campo("12:00");

    public PanelTurnos(Club club, Usuario usuario) {
        super(club, usuario);
        boolean admin = usuario instanceof Administrador;
        add(UI.titulo(admin ? "Turnos del personal" : "Mis turnos"), BorderLayout.NORTH);

        JPanel tablas = new JPanel(new GridLayout(2, 1, 0, 10));
        tablas.add(UI.seccion(admin ? "Turnos" : "Mis turnos (seleccione uno para pedir cambio)",
                UI.scroll(tablaTurnos)));
        tablas.add(UI.seccion("Solicitudes de cambio", UI.scroll(tablaSolicitudes)));

        if (admin) {
            JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            botones.add(UI.boton("Aprobar solicitud", e -> evaluar(true)));
            botones.add(UI.boton("Rechazar solicitud", e -> evaluar(false)));
            add(tablas, BorderLayout.CENTER);
            add(botones, BorderLayout.SOUTH);
        } else {
            JPanel formulario = UI.seccion("Solicitar cambio del turno seleccionado", UI.formulario(
                    "Motivo:", campoMotivo, "Nuevo dia:", comboDia, "Hora inicio (HH:MM):", campoInicio,
                    "Hora fin (HH:MM):", campoFin, UI.boton("Enviar solicitud", e -> solicitar())));
            JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablas, formulario);
            division.setResizeWeight(0.65);
            add(division, BorderLayout.CENTER);
        }
    }

    private void solicitar() {
        int fila = UI.filaSeleccionada(tablaTurnos);
        if (fila < 0) {
            UI.error(this, "Seleccione el turno que desea cambiar");
            return;
        }
        Turno turno = turnos.get(fila);
        if (UI.ejecutar(this, () -> {
            LocalTime inicio = UI.hora(campoInicio, "Hora inicio");
            LocalTime fin = UI.hora(campoFin, "Hora fin");
            club.solicitarCambioTurno(usuario.getCodigo(), turno, UI.texto(campoMotivo, "Motivo"),
                    UI.seleccionado(comboDia, "un dia"), inicio, fin);
        }, "Solicitud enviada")) {
            campoMotivo.setText("");
            actualizar();
        }
    }

    private void evaluar(boolean aprobar) {
        int fila = UI.filaSeleccionada(tablaSolicitudes);
        if (fila < 0) {
            UI.error(this, "Seleccione una solicitud");
            return;
        }
        SolicitudCambioTurno s = solicitudes.get(fila);
        if (aprobar) {
            UI.ejecutar(this, () -> club.aprobarCambio(s), "Cambio aprobado: el turno fue actualizado");
        } else {
            String observacion = JOptionPane.showInputDialog(this, "Motivo del rechazo:");
            if (observacion != null) {
                UI.ejecutar(this, () -> club.rechazarCambio(s, observacion), "Solicitud rechazada");
            }
        }
        actualizar();
    }

    @Override
    public void actualizar() {
        turnos.clear();
        solicitudes.clear();
        if (usuario instanceof Administrador) {
            turnos.addAll(club.consultarTurnos(usuario.getCodigo()));
            solicitudes.addAll(club.getSolicitudes());
        } else {
            turnos.addAll(((Empleado) usuario).getTurnos());
            for (SolicitudCambioTurno s : club.getSolicitudes()) {
                if (s.getEmpleado() == usuario) {
                    solicitudes.add(s);
                }
            }
        }
        modeloTurnos.setRowCount(0);
        for (Turno t : turnos) {
            modeloTurnos.addRow(new Object[] {Listas.nombre(t.getEmpleado()), t.getDiaSemana(), t.getHoraInicio(),
                t.getHoraFin()});
        }
        modeloSolicitudes.setRowCount(0);
        for (SolicitudCambioTurno s : solicitudes) {
            modeloSolicitudes.addRow(new Object[] {Listas.nombre(s.getEmpleado()),
                s.getTurno().getDiaSemana() + " " + s.getTurno().getHoraInicio() + "-" + s.getTurno().getHoraFin(),
                s.getNuevoDiaSemana() + " " + s.getNuevaHoraInicio() + "-" + s.getNuevaHoraFin(), s.getMotivo(),
                Listas.texto(s.getEstado())});
        }
    }
}
