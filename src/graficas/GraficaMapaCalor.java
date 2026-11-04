package graficas;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapa de calor: una cuadricula de filas x columnas donde el color de cada celda
 * indica que tan alto es su valor (blanco = 0, azul intenso = maximo).
 */
public class GraficaMapaCalor extends Grafica {

    private static final long serialVersionUID = 1L;

    private static final Color FRIO = new Color(0xF1F5FB);
    private static final Color CALIENTE = new Color(0x1B4F9C);

    private List<String> filas = new ArrayList<>();
    private List<String> columnas = new ArrayList<>();
    private double[][] valores = new double[0][0];
    private String unidad = "";

    public GraficaMapaCalor(String titulo) {
        super(titulo);
    }

    public void setDatos(List<String> filas, List<String> columnas, double[][] valores) {
        this.filas = new ArrayList<>(filas);
        this.columnas = new ArrayList<>(columnas);
        this.valores = valores;
        repaint();
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    private double maximo() {
        double max = 0;
        for (double[] fila : valores) {
            for (double v : fila) {
                max = Math.max(max, v);
            }
        }
        return max;
    }

    @Override
    public boolean tieneDatos() {
        return maximo() > 0;
    }

    @Override
    protected void dibujar(Graphics2D g, Rectangle area) {
        double maximo = maximo();
        g.setFont(FUENTE_PEQUENA);
        FontMetrics fm = g.getFontMetrics();

        int anchoFilas = 0;
        for (String f : filas) {
            anchoFilas = Math.max(anchoFilas, fm.stringWidth(f));
        }
        anchoFilas += 10;

        int altoEscala = 26;
        int x0 = area.x + anchoFilas;
        int y0 = area.y + fm.getHeight() + 4;
        double anchoCelda = (area.width - anchoFilas) / (double) Math.max(1, columnas.size());
        double altoCelda = (area.height - fm.getHeight() - 4 - altoEscala) / (double) Math.max(1, filas.size());

        g.setColor(COLOR_TEXTO);
        for (int c = 0; c < columnas.size(); c++) {
            textoCentrado(g, columnas.get(c), (int) (x0 + c * anchoCelda + anchoCelda / 2), area.y + fm.getAscent());
        }

        for (int f = 0; f < filas.size(); f++) {
            int y = (int) (y0 + f * altoCelda);
            g.setColor(COLOR_TEXTO);
            g.drawString(filas.get(f), area.x, (int) (y + altoCelda / 2 + fm.getAscent() / 2 - 1));
            for (int c = 0; c < columnas.size(); c++) {
                double v = valores[f][c];
                Rectangle celda = new Rectangle((int) (x0 + c * anchoCelda), y,
                        (int) Math.ceil(anchoCelda) - 2, (int) Math.ceil(altoCelda) - 2);
                g.setColor(mezclar(v / maximo));
                g.fillRoundRect(celda.x, celda.y, celda.width, celda.height, 4, 4);
                agregarZona(celda, filas.get(f) + " " + columnas.get(c) + ": " + formatear(v) + " " + unidad);
                if (v > 0 && celda.width >= fm.stringWidth(formatear(v)) + 2 && celda.height >= fm.getAscent()) {
                    g.setColor(v / maximo > 0.55 ? Color.WHITE : COLOR_TEXTO);
                    textoCentrado(g, formatear(v), celda.x + celda.width / 2,
                            celda.y + celda.height / 2 + fm.getAscent() / 2 - 1);
                }
            }
        }

        // Escala de colores
        int ancho = Math.min(240, area.width - anchoFilas);
        int ex = x0;
        int ey = area.y + area.height - 12;
        for (int i = 0; i < ancho; i++) {
            g.setColor(mezclar(i / (double) ancho));
            g.drawLine(ex + i, ey, ex + i, ey + 10);
        }
        g.setColor(COLOR_EJE);
        g.drawString("0", ex - fm.stringWidth("0") - 4, ey + 9);
        g.drawString(formatear(maximo) + " " + unidad, ex + ancho + 6, ey + 9);
    }

    private static Color mezclar(double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (FRIO.getRed() + (CALIENTE.getRed() - FRIO.getRed()) * t);
        int gr = (int) (FRIO.getGreen() + (CALIENTE.getGreen() - FRIO.getGreen()) * t);
        int b = (int) (FRIO.getBlue() + (CALIENTE.getBlue() - FRIO.getBlue()) * t);
        return new Color(r, gr, b);
    }
}
