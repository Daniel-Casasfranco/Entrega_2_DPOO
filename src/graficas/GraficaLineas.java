package graficas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Diagrama de lineas. El eje X tiene etiquetas (fechas o semanas) y cada serie
 * tiene un valor por etiqueta. Un valor {@code Double.NaN} deja un hueco en la linea.
 */
public class GraficaLineas extends Grafica {

    private static final long serialVersionUID = 1L;

    private List<String> etiquetasX = new ArrayList<>();
    private Map<String, double[]> series = new LinkedHashMap<>();
    private boolean ejeDesdeCero = true;

    public GraficaLineas(String titulo) {
        super(titulo);
    }

    public void setDatos(List<String> etiquetasX, Map<String, double[]> series) {
        this.etiquetasX = new ArrayList<>(etiquetasX);
        this.series = new LinkedHashMap<>(series);
        repaint();
    }

    /** Si es falso, el eje Y empieza cerca del valor minimo (util para ranking). */
    public void setEjeDesdeCero(boolean desdeCero) {
        this.ejeDesdeCero = desdeCero;
        repaint();
    }

    @Override
    public boolean tieneDatos() {
        if (etiquetasX.isEmpty()) {
            return false;
        }
        for (double[] valores : series.values()) {
            for (double v : valores) {
                if (!Double.isNaN(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void dibujar(Graphics2D g, Rectangle area) {
        double maximo = -Double.MAX_VALUE;
        double minimo = Double.MAX_VALUE;
        for (double[] valores : series.values()) {
            for (double v : valores) {
                if (!Double.isNaN(v)) {
                    maximo = Math.max(maximo, v);
                    minimo = Math.min(minimo, v);
                }
            }
        }

        List<String> nombres = new ArrayList<>(series.keySet());
        int altoLeyenda = dibujarLeyenda(g, nombres, area.x + 50, area.y, area.width - 50) + 6;

        g.setFont(FUENTE_PEQUENA);
        FontMetrics fm = g.getFontMetrics();
        int izquierda = anchoEtiquetasY(g, maximo);
        Rectangle plano = new Rectangle(area.x + izquierda, area.y + altoLeyenda + 8,
                area.width - izquierda - 16, area.height - altoLeyenda - 8 - fm.getHeight() - 10);

        // Eje Y: desde cero o ajustado al rango de los datos
        double base = 0;
        if (!ejeDesdeCero) {
            double paso = pasoEje(Math.max(maximo - minimo, 1), 5);
            base = Math.floor((minimo - paso / 2) / paso) * paso;
        }
        Rectangle planoDesplazado = plano;
        double maximoEje;
        double minimoEje;
        if (ejeDesdeCero) {
            maximoEje = dibujarEjeY(g, plano, maximo, Math.min(0, minimo));
            double paso = pasoEje(Math.max(maximo - Math.min(0, minimo), 1), 5);
            minimoEje = Math.floor(Math.min(0, minimo) / paso) * paso;
        } else {
            maximoEje = dibujarEjeRango(g, planoDesplazado, base, maximo);
            minimoEje = base;
        }

        int n = etiquetasX.size();
        double pasoX = n > 1 ? plano.width / (double) (n - 1) : 0;
        int cadaCuanto = Math.max(1, (int) Math.ceil(n / (plano.width / 70.0)));

        g.setColor(COLOR_TEXTO);
        for (int i = 0; i < n; i++) {
            if (i % cadaCuanto == 0 || i == n - 1) {
                int x = n > 1 ? (int) (plano.x + i * pasoX) : plano.x + plano.width / 2;
                textoCentrado(g, etiquetasX.get(i), x, plano.y + plano.height + fm.getAscent() + 6);
            }
        }

        int s = 0;
        for (Map.Entry<String, double[]> serie : series.entrySet()) {
            Color c = color(s);
            double[] valores = serie.getValue();
            Path2D.Double linea = new Path2D.Double();
            boolean empezada = false;
            for (int i = 0; i < n && i < valores.length; i++) {
                if (Double.isNaN(valores[i])) {
                    empezada = false;
                    continue;
                }
                double x = n > 1 ? plano.x + i * pasoX : plano.x + plano.width / 2.0;
                double y = aPixel(valores[i], minimoEje, maximoEje, plano);
                if (!empezada) {
                    linea.moveTo(x, y);
                    empezada = true;
                } else {
                    linea.lineTo(x, y);
                }
            }
            g.setColor(c);
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(linea);
            g.setStroke(new BasicStroke(1f));

            for (int i = 0; i < n && i < valores.length; i++) {
                if (Double.isNaN(valores[i])) {
                    continue;
                }
                double x = n > 1 ? plano.x + i * pasoX : plano.x + plano.width / 2.0;
                double y = aPixel(valores[i], minimoEje, maximoEje, plano);
                Ellipse2D.Double punto = new Ellipse2D.Double(x - 4, y - 4, 8, 8);
                g.setColor(Color.WHITE);
                g.fill(punto);
                g.setColor(c);
                g.setStroke(new BasicStroke(2f));
                g.draw(punto);
                g.setStroke(new BasicStroke(1f));
                agregarZona(new Ellipse2D.Double(x - 8, y - 8, 16, 16),
                        serie.getKey() + " | " + etiquetasX.get(i) + ": " + formatear(valores[i]));
            }
            s++;
        }
    }

    /** Eje Y que va de {@code base} hasta el maximo, sin forzar el cero. */
    private static double dibujarEjeRango(Graphics2D g, Rectangle plano, double base, double maximo) {
        double paso = pasoEje(Math.max(maximo - base, 1), 5);
        double maximoEje = Math.ceil((maximo + paso / 2) / paso) * paso;
        g.setFont(FUENTE_PEQUENA);
        FontMetrics fm = g.getFontMetrics();
        for (double v = base; v <= maximoEje + paso / 2; v += paso) {
            int y = aPixel(v, base, maximoEje, plano);
            g.setColor(COLOR_GUIA);
            g.drawLine(plano.x, y, plano.x + plano.width, y);
            g.setColor(COLOR_EJE);
            String etiqueta = formatear(v);
            g.drawString(etiqueta, plano.x - fm.stringWidth(etiqueta) - 6, y + fm.getAscent() / 2 - 1);
        }
        g.setColor(COLOR_EJE);
        g.drawLine(plano.x, plano.y, plano.x, plano.y + plano.height);
        g.drawLine(plano.x, plano.y + plano.height, plano.x + plano.width, plano.y + plano.height);
        return maximoEje;
    }
}
