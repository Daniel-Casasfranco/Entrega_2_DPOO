package interfaz;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import modelo.Club;
import modelo.Deporte;
import modelo.Usuario;

/** Reportes e historiales del administrador en un periodo (RF-40 a RF-42). */
public class PanelReportes extends PanelClub {

    private static final long serialVersionUID = 1L;

    private static final String VENTAS = "Ventas por rubro";
    private static final String ASISTENCIA = "Asistencia a entrenamientos";
    private static final String RESULTADOS = "Resultados de partidos";
    private static final String RANKING = "Evolucion del ranking";
    private static final String RESERVAS = "Historial de reservas";
    private static final String PARTIDOS = "Historial de partidos";
    private static final String SESIONES = "Historial de sesiones";

    private final JComboBox<String> comboReporte = UI.combo(
            Arrays.asList(VENTAS, ASISTENCIA, RESULTADOS, RANKING, RESERVAS, PARTIDOS, SESIONES), s -> s);
    private final JComboBox<Deporte> comboDisciplina =
            UI.combo(Arrays.asList(Listas.DISCIPLINAS_INDIVIDUALES), Listas::texto);
    private final JTextField campoDesde = UI.campo(LocalDate.now().minusMonths(1).toString());
    private final JTextField campoHasta = UI.campo(LocalDate.now().toString());
    private final JTextArea resultado = UI.areaTexto();

    public PanelReportes(Club club, Usuario usuario) {
        super(club, usuario);
        add(UI.titulo("Reportes"), BorderLayout.NORTH);
        comboReporte.addActionListener(e -> comboDisciplina.setEnabled(RANKING.equals(comboReporte.getSelectedItem())));
        comboDisciplina.setEnabled(false);

        add(UI.seccion("Parametros", UI.formulario("Reporte:", comboReporte,
                "Disciplina (solo ranking):", comboDisciplina, "Desde (AAAA-MM-DD):", campoDesde,
                "Hasta (AAAA-MM-DD):", campoHasta, UI.boton("Generar", e -> generar()))), BorderLayout.WEST);
        add(UI.seccion("Resultado", UI.scroll(resultado)), BorderLayout.CENTER);
    }

    private void generar() {
        resultado.setText("");
        UI.ejecutar(this, () -> {
            LocalDate desde = UI.fecha(campoDesde, "Desde");
            LocalDate hasta = UI.fecha(campoHasta, "Hasta");
            String tipo = (String) comboReporte.getSelectedItem();
            List<String> lineas;
            if (VENTAS.equals(tipo)) {
                lineas = club.reporteVentas(usuario.getCodigo(), desde, hasta);
            } else if (ASISTENCIA.equals(tipo)) {
                lineas = club.reporteAsistencia(desde, hasta);
            } else if (RESULTADOS.equals(tipo)) {
                lineas = club.reporteResultados(desde, hasta);
            } else if (RANKING.equals(tipo)) {
                lineas = club.reporteEvolucionRanking(UI.seleccionado(comboDisciplina, "una disciplina"),
                        desde, hasta);
            } else if (RESERVAS.equals(tipo)) {
                lineas = club.historialReservas(desde, hasta);
            } else if (PARTIDOS.equals(tipo)) {
                lineas = club.historialPartidos(desde, hasta);
            } else {
                lineas = club.historialSesiones(desde, hasta);
            }
            StringBuilder sb = new StringBuilder(tipo.toUpperCase()).append("  (").append(desde).append(" a ")
                    .append(hasta).append(")\n\n");
            for (String l : lineas) {
                sb.append(l).append('\n');
            }
            resultado.setText(sb.toString());
            resultado.setCaretPosition(0);
        }, null);
    }

    @Override
    public void actualizar() {
        // Los reportes se generan a peticion
    }
}
