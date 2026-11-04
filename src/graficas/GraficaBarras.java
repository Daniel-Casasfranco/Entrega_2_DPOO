package graficas;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Diagrama de barras agrupadas. Hay una lista de categorias (eje X) y una o mas
 * series; cada serie tiene un valor por categoria.
 */
public class GraficaBarras extends Grafica {

    private static final long serialVersionUID = 1L;

    private List<String> categorias = new ArrayList<>();
    private Map<String, double[]> series = new LinkedHashMap<>();
    private String etiquetaEjeY = "";

    public GraficaBarras(String titulo) {
        super(titulo);
    }

    public void setDatos(List<String> categorias, Map<String, double[]> series) {
        this.categorias = new ArrayList<>(categorias);
        this.series = new LinkedHashMap<>(series);
        repaint();
    }

    /** Atajo para una sola serie: categoria -> valor. */
    public void setDatos(String nombreSerie, Map<String, Double> valores) {
        List<String> cats = new ArrayList<>(valores.keySet());
        double[] arreglo = new double[cats.size()];
        for (int i = 0; i < cats.size(); i++) {
            arreglo[i] = valores.get(cats.get(i));
        }
        Map<String, double[]> unica = new LinkedHashMap<>();
        unica.put(nombreSerie, arreglo);
        setDatos(cats, unica);
    }

    public void setEtiquetaEjeY(String etiqueta) {
        this.etiquetaEjeY = etiqueta;
        repaint();
    }

    @Override
    public boolean tieneDatos() {
        for (double[] valores : series.values()) {
            for (double v : valores) {
                if (v != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void dibujar(Graphics2D g, Rectangle area) {
        double maximo = 0;
        double minimo = 0;
        for (double[] valores : series.values()) {
            for (double v : valores) {
                maximo = Math.max(maximo, v);
                minimo = Math.min(minimo, v);
            }
        }

        List<String> nombres = new ArrayList<>(series.keySet());
        int altoLeyenda = 0;
        if (nombres.size() > 1) {
            altoLeyenda = dibujarLeyenda(g, nombres, area.x + 50, area.y, area.width - 50) + 6;
        }

        g.setFont(FUENTE_PEQUENA);
        FontMetrics fm = g.getFontMetrics();
        int izquierda = anchoEtiquetasY(g, maximo) + (etiquetaEjeY.isEmpty() ? 0 : 16);
        Rectangle plano = new Rectangle(area.x + izquierda, area.y + altoLeyenda + 8,
                area.width - izquierda - 8, area.height - altoLeyenda - 8 - fm.getHeight() * 2 - 6);

        if (!etiquetaEjeY.isEmpty()) {
            Graphics2D rotado = (Graphics2D) g.create();
            rotado.setColor(COLOR_EJE);
            rotado.rotate(-Math.PI / 2);
            textoCentrado(rotado, etiquetaEjeY, -(plano.y + plano.height / 2), area.x + fm.getAscent());
            rotado.dispose();
        }

        double maximoEje = dibujarEjeY(g, plano, maximo, minimo);
        double paso = pasoEje(Math.max(maximo - Math.min(0, minimo), 1), 5);
        double minimoEje = Math.floor(Math.min(0, minimo) / paso) * paso;

        int n = categorias.size();
        if (n == 0) {
            return;
        }
        double anchoGrupo = plano.width / (double) n;
        double anchoBarra = Math.min(46, anchoGrupo * 0.75 / Math.max(1, series.size()));
        int y0 = aPixel(0, minimoEje, maximoEje, plano);

        for (int c = 0; c < n; c++) {
            double inicioGrupo = plano.x + c * anchoGrupo + (anchoGrupo - anchoBarra * series.size()) / 2;
            int s = 0;
            for (Map.Entry<String, double[]> serie : series.entrySet()) {
                double valor = c < serie.getValue().length ? serie.getValue()[c] : 0;
                int yv = aPixel(valor, minimoEje, maximoEje, plano);
                Rectangle barra = new Rectangle((int) (inicioGrupo + s * anchoBarra), Math.min(yv, y0),
                        (int) Math.max(2, anchoBarra - 3), Math.abs(y0 - yv));
                g.setColor(color(s));
                g.fillRoundRect(barra.x, barra.y, barra.width, barra.height, 4, 4);
                String detalle = categorias.get(c) + (series.size() > 1 ? " - " + serie.getKey() : "")
                        + ": " + formatear(valor);
                agregarZona(barra, detalle);

                if (valor != 0 && barra.width >= fm.stringWidth(formatear(valor)) - 4) {
                    g.setColor(COLOR_TEXTO);
                    textoCentrado(g, formatear(valor), barra.x + barra.width / 2,
                            valor >= 0 ? barra.y - 4 : barra.y + barra.height + fm.getAscent() + 2);
                }
                s++;
            }

            g.setColor(COLOR_TEXTO);
            String etiqueta = recortar(fm, categorias.get(c), (int) anchoGrupo - 4);
            textoCentrado(g, etiqueta, (int) (plano.x + c * anchoGrupo + anchoGrupo / 2),
                    plano.y + plano.height + fm.getAscent() + 6);
        }
    }
}
