package graficas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * Base de todas las graficas. Se encarga del fondo, el titulo, el mensaje cuando
 * no hay datos y los tooltips. Cada subclase solo dibuja su contenido dentro del
 * area que se le entrega.
 */
public abstract class Grafica extends JComponent {

    private static final long serialVersionUID = 1L;

    /** Paleta de colores que comparten todas las graficas. */
    public static final Color[] PALETA = {
        new Color(0x2E86DE), new Color(0xEE5253), new Color(0x10AC84), new Color(0xFF9F43),
        new Color(0x8854D0), new Color(0x00D2D3), new Color(0xF368E0), new Color(0x576574)
    };

    protected static final Color COLOR_TEXTO = new Color(0x2F3542);
    protected static final Color COLOR_EJE = new Color(0x8395A7);
    protected static final Color COLOR_GUIA = new Color(0xE4E8EE);
    protected static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 15);
    protected static final Font FUENTE = new Font("SansSerif", Font.PLAIN, 12);
    protected static final Font FUENTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 11);

    private static final DecimalFormat FORMATO_ENTERO = new DecimalFormat("#,##0");
    private static final DecimalFormat FORMATO_DECIMAL = new DecimalFormat("#,##0.##");

    private String titulo;
    private String mensajeVacio = "No hay datos para los filtros seleccionados";

    /** Zonas de la grafica que muestran un texto al pasar el mouse. */
    private final List<Shape> zonas = new ArrayList<>();
    private final List<String> textosZonas = new ArrayList<>();

    protected Grafica(String titulo) {
        this.titulo = titulo;
        setPreferredSize(new Dimension(640, 400));
        setToolTipText("");
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        repaint();
    }

    public String getTitulo() {
        return titulo;
    }

    public void setMensajeVacio(String mensaje) {
        this.mensajeVacio = mensaje;
        repaint();
    }

    /** Indica si hay algo que dibujar. */
    public abstract boolean tieneDatos();

    /** Dibuja el contenido de la grafica dentro del area dada. */
    protected abstract void dibujar(Graphics2D g, Rectangle area);

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        zonas.clear();
        textosZonas.clear();

        int arriba = 12;
        if (titulo != null && !titulo.isEmpty()) {
            g.setFont(FUENTE_TITULO);
            g.setColor(COLOR_TEXTO);
            textoCentrado(g, titulo, getWidth() / 2, 12 + g.getFontMetrics().getAscent());
            arriba = 22 + g.getFontMetrics().getHeight();
        }

        Rectangle area = new Rectangle(16, arriba, getWidth() - 32, getHeight() - arriba - 12);
        if (!tieneDatos()) {
            g.setFont(FUENTE);
            g.setColor(COLOR_EJE);
            textoCentrado(g, mensajeVacio, getWidth() / 2, getHeight() / 2);
        } else if (area.width > 40 && area.height > 40) {
            g.setFont(FUENTE);
            dibujar(g, area);
        }
        g.dispose();
    }

    /** Registra una zona que muestra un texto al pasar el mouse por encima. */
    protected void agregarZona(Shape zona, String texto) {
        zonas.add(zona);
        textosZonas.add(texto);
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        for (int i = zonas.size() - 1; i >= 0; i--) {
            if (zonas.get(i).contains(evento.getPoint())) {
                return textosZonas.get(i);
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Utilidades comunes para las subclases
    // ------------------------------------------------------------------

    protected static Color color(int indice) {
        return PALETA[indice % PALETA.length];
    }

    protected static void textoCentrado(Graphics2D g, String texto, int centroX, int baseY) {
        int ancho = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, centroX - ancho / 2, baseY);
    }

    /** Recorta un texto con "..." para que no pase del ancho dado. */
    protected static String recortar(FontMetrics fm, String texto, int anchoMaximo) {
        if (fm.stringWidth(texto) <= anchoMaximo) {
            return texto;
        }
        String puntos = "...";
        for (int fin = texto.length() - 1; fin > 0; fin--) {
            String corto = texto.substring(0, fin) + puntos;
            if (fm.stringWidth(corto) <= anchoMaximo) {
                return corto;
            }
        }
        return puntos;
    }

    public static String formatear(double valor) {
        if (valor == Math.rint(valor)) {
            return FORMATO_ENTERO.format(valor);
        }
        return FORMATO_DECIMAL.format(valor);
    }

    /** Calcula un paso "redondo" (1, 2, 5, 10, 20, 50...) para las marcas del eje. */
    protected static double pasoEje(double maximo, int marcasDeseadas) {
        if (maximo <= 0) {
            return 1;
        }
        double paso = maximo / marcasDeseadas;
        double potencia = Math.pow(10, Math.floor(Math.log10(paso)));
        double fraccion = paso / potencia;
        double redondo;
        if (fraccion <= 1) {
            redondo = 1;
        } else if (fraccion <= 2) {
            redondo = 2;
        } else if (fraccion <= 5) {
            redondo = 5;
        } else {
            redondo = 10;
        }
        return Math.max(redondo * potencia, 1e-9);
    }

    /**
     * Dibuja el eje Y con lineas guia horizontales y devuelve el valor maximo del eje
     * (multiplo del paso), que se usa para convertir valores a pixeles.
     */
    protected static double dibujarEjeY(Graphics2D g, Rectangle plano, double maximoDatos, double minimoDatos) {
        double minimo = Math.min(0, minimoDatos);
        double paso = pasoEje(Math.max(maximoDatos - minimo, 1), 5);
        double maximoEje = Math.ceil(Math.max(maximoDatos, minimo + paso) / paso) * paso;
        double minimoEje = Math.floor(minimo / paso) * paso;

        g.setFont(FUENTE_PEQUENA);
        FontMetrics fm = g.getFontMetrics();
        for (double v = minimoEje; v <= maximoEje + paso / 2; v += paso) {
            int y = aPixel(v, minimoEje, maximoEje, plano);
            g.setColor(COLOR_GUIA);
            g.drawLine(plano.x, y, plano.x + plano.width, y);
            g.setColor(COLOR_EJE);
            String etiqueta = formatear(v);
            g.drawString(etiqueta, plano.x - fm.stringWidth(etiqueta) - 6, y + fm.getAscent() / 2 - 1);
        }
        g.setColor(COLOR_EJE);
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(plano.x, plano.y, plano.x, plano.y + plano.height);
        int y0 = aPixel(0, minimoEje, maximoEje, plano);
        g.drawLine(plano.x, y0, plano.x + plano.width, y0);
        g.setStroke(new BasicStroke(1f));
        return maximoEje;
    }

    protected static int aPixel(double valor, double minimoEje, double maximoEje, Rectangle plano) {
        double proporcion = (valor - minimoEje) / (maximoEje - minimoEje);
        return (int) Math.round(plano.y + plano.height - proporcion * plano.height);
    }

    /** Ancho necesario para las etiquetas del eje Y. */
    protected static int anchoEtiquetasY(Graphics2D g, double maximo) {
        g.setFont(FUENTE_PEQUENA);
        return g.getFontMetrics().stringWidth(formatear(maximo * 1.2)) + 14;
    }

    /** Dibuja una leyenda horizontal (cuadro de color + nombre) y devuelve su alto. */
    protected int dibujarLeyenda(Graphics2D g, List<String> nombres, int x, int y, int ancho) {
        g.setFont(FUENTE_PEQUENA);
        FontMetrics fm = g.getFontMetrics();
        int cx = x;
        int cy = y;
        int alto = fm.getHeight() + 4;
        for (int i = 0; i < nombres.size(); i++) {
            String nombre = nombres.get(i);
            int largo = 16 + fm.stringWidth(nombre) + 16;
            if (cx + largo > x + ancho && cx > x) {
                cx = x;
                cy += alto;
            }
            g.setColor(color(i));
            g.fillRoundRect(cx, cy + 2, 11, 11, 3, 3);
            g.setColor(COLOR_TEXTO);
            g.drawString(nombre, cx + 16, cy + fm.getAscent());
            cx += largo;
        }
        return cy - y + alto;
    }
}
