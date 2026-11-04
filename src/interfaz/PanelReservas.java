package interfaz;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.Club;
import modelo.Deporte;
import modelo.Instalacion;
import modelo.Modalidad;
import modelo.Reserva;
import modelo.Usuario;

/** Reservas de canchas para disciplinas individuales (RF-08 a RF-10). */
public class PanelReservas extends PanelClub {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modelo =
            UI.modeloTabla("Fecha", "Inicio", "Fin", "Instalacion", "Disciplina", "Modalidad", "Jugadores");

    private final JComboBox<Instalacion> comboInstalacion = UI.combo(new ArrayList<Instalacion>(),
            i -> i.getNombre() + " (" + Listas.texto(i.getDisciplina()) + (i.isTechada() ? ", techada" : "") + ")");
    private final JTextField campoFecha = UI.campo(LocalDate.now().plusDays(1).toString());
    private final JTextField campoInicio = UI.campo("09:00");
    private final JTextField campoFin = UI.campo("10:00");
    private final JComboBox<Modalidad> comboModalidad = UI.combo(Arrays.asList(Modalidad.values()), Listas::texto);

    public PanelReservas(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Reservas"), BorderLayout.NORTH);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                UI.seccion("Mis reservas", UI.scroll(UI.tabla(modelo))),
                UI.seccion("Nueva reserva", UI.formulario("Instalacion:", comboInstalacion,
                        "Fecha (AAAA-MM-DD):", campoFecha, "Hora inicio (HH:MM):", campoInicio,
                        "Hora fin (HH:MM):", campoFin, "Modalidad:", comboModalidad,
                        UI.boton("Reservar", e -> reservar()))));
        division.setResizeWeight(0.6);
        add(division, BorderLayout.CENTER);
    }

    private void reservar() {
        if (UI.ejecutar(this, () -> {
            Modalidad modalidad = UI.seleccionado(comboModalidad, "una modalidad");
            int jugadores = modalidad == Modalidad.SENCILLOS ? 2 : 4;
            club.reservar(usuario.getCodigo(), UI.seleccionado(comboInstalacion, "una instalacion"),
                    UI.fecha(campoFecha, "Fecha"), UI.hora(campoInicio, "Hora inicio"), UI.hora(campoFin, "Hora fin"),
                    jugadores, modalidad);
        }, "Reserva confirmada")) {
            actualizar();
        }
    }

    @Override
    public void actualizar() {
        List<Instalacion> individuales = new ArrayList<>();
        for (Instalacion i : club.getInstalaciones()) {
            if (Arrays.asList(Listas.DISCIPLINAS_INDIVIDUALES).contains(i.getDisciplina())) {
                individuales.add(i);
            }
        }
        UI.recargar(comboInstalacion, individuales);
        modelo.setRowCount(0);
        for (Reserva r : club.getReservas()) {
            if (r.getSocio() == usuario) {
                Deporte d = r.getInstalacion().getDisciplina();
                modelo.addRow(new Object[] {r.getFecha(), r.getHoraInicio(), r.getHoraFin(),
                    r.getInstalacion().getNombre(), Listas.texto(d), Listas.texto(r.getModalidad()),
                    r.getNumeroJugadores()});
            }
        }
    }
}
