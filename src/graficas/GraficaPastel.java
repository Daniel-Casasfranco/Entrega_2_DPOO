package graficas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Diagrama de pastel (tipo dona). Recibe un mapa categoria -> valor y dibuja
 * cada categoria como una porcion proporcional, con su leyenda y porcentaje.
 */
public class GraficaPastel extends Grafica {

    private static final long serialVersionUID = 1L;

    private Map<String, Double> datos = new LinkedHashMap<>();
    private Map<String, Color> coloresFijos = new LinkedHashMap<>();

    public GraficaPastel(String titulo) {
        super(titulo);
    }

    public void setDatos(Map<String, Double> datos) {
        this.datos = new LinkedHashMap<>(datos);
        repaint();
    }

    /** Permite fijar un color por categoria (por ejemplo verde para victorias). */
    public void setColor(String categoria, Color color) {
        coloresFijos.put(categoria, color);
    }

    private double total() {
        double total = 0;
        for (double v : datos.values()) {
            total += Math.max(0, v);
        }
        return total;
    }

    @Override
    public boolean tieneDatos() {
        return total() > 0;
    }

    private Color colorDe(String categoria, int indice) {
        Color fijo = coloresFijos.get(categoria);
        return fijo != null ? fijo : color(indice);
    }

    @Override
    protected void dibujar(Graphics2D g, Rectangle area) {
        double total = total();
        g.setFont(FUENTE);
        FontMetrics fm = g.getFontMetrics();

        int anchoLeyenda = 0;
        for (Map.Entry<String, Double> e : datos.entrySet()) {
            anchoLeyenda = Math.max(anchoLeyenda, fm.stringWidth(textoLeyenda(e.getKey(), e.getValue(), total)));
        }
        anchoLeyenda = Math.min(anchoLeyenda + 30, area.width / 2);

        int diametro = Math.min(area.width - anchoLeyenda - 30, area.height - 10);
        diametro = Math.max(diametro, 60);
        int x = area.x + (area.width - anchoLeyenda - 30 - diametro) / 2;
        int y = area.y + (area.height - diametro) / 2;

        double angulo = 90;
        int i = 0;
        for (Map.Entry<String, Double> e : datos.entrySet()) {
            double valor = Math.max(0, e.getValue());
            if (valor > 0) {
                double barrido = -360.0 * valor / total;
                Arc2D.Double porcion = new Arc2D.Double(x, y, diametro, diametro, angulo, barrido, Arc2D.PIE);
                g.setColor(colorDe(e.getKey(), i));
                g.fill(porcion);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2f));
                g.draw(porcion);
                agregarZona(porcion, textoLeyenda(e.getKey(), valor, total));

                double porcentaje = valor / total;
                if (porcentaje >= 0.06) {
                    double medio = Math.toRadians(angulo + barrido / 2);
                    int radio = (int) (diametro * 0.38);
                    int tx = x + diametro / 2 + (int) (Math.cos(medio) * radio);
                    int ty = y + diametro / 2 - (int) (Math.sin(medio) * radio);
                    g.setFont(FUENTE_TITULO);
                    g.setColor(Color.WHITE);
                    textoCentrado(g, Math.round(porcentaje * 100) + "%", tx, ty + 5);
                    g.setFont(FUENTE);
                }
                angulo += barrido;
            }
            i++;
        }

        // Hueco central (dona) con el total
        int hueco = diametro / 3;
        g.setColor(Color.WHITE);
        g.fillOval(x + diametro / 2 - hueco / 2, y + diametro / 2 - hueco / 2, hueco, hueco);
        g.setColor(COLOR_TEXTO);
        g.setFont(FUENTE_TITULO);
        textoCentrado(g, formatear(total), x + diametro / 2, y + diametro / 2 + 2);
        g.setFont(FUENTE_PEQUENA);
        g.setColor(COLOR_EJE);
        textoCentrado(g, "total", x + diametro / 2, y + diametro / 2 + 16);

        // Leyenda
        g.setFont(FUENTE);
        int lx = x + diametro + 30;
        int ly = area.y + (area.height - datos.size() * (fm.getHeight() + 8)) / 2;
        i = 0;
        for (Map.Entry<String, Double> e : datos.entrySet()) {
            g.setColor(colorDe(e.getKey(), i));
            g.fillRoundRect(lx, ly + 2, 13, 13, 4, 4);
            g.setColor(COLOR_TEXTO);
            String texto = recortar(fm, textoLeyenda(e.getKey(), e.getValue(), total), anchoLeyenda - 20);
            g.drawString(texto, lx + 20, ly + fm.getAscent());
            ly += fm.getHeight() + 8;
            i++;
        }
    }

    private static String textoLeyenda(String categoria, double valor, double total) {
        long porcentaje = total > 0 ? Math.round(100 * valor / total) : 0;
        return categoria + ": " + formatear(valor) + " (" + porcentaje + "%)";
    }
}
