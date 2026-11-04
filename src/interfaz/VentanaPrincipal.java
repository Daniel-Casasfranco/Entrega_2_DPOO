package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import graficas.PanelGraficas;
import modelo.Administrador;
import modelo.Club;
import modelo.Empleado;
import modelo.Entrenador;
import modelo.Socio;
import modelo.Usuario;

/**
 * Ventana principal. Muestra una pestana por cada funcionalidad disponible para el
 * rol del usuario que inicio sesion.
 */
public class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Club club;
    private final JTabbedPane pestanas = new JTabbedPane(JTabbedPane.LEFT);

    public VentanaPrincipal(Club club, Usuario usuario) {
        super("Club Deportivo Seneca - " + usuario.getNombre() + " " + usuario.getApellido()
                + " (" + rol(usuario) + ")");
        this.club = club;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        agregarPestanas(usuario);
        pestanas.addChangeListener(e -> actualizarPestanaActual());

        add(encabezado(usuario), BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        setMinimumSize(new Dimension(1000, 640));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        actualizarPestanaActual();
    }

    private void agregarPestanas(Usuario usuario) {
        agregar("Mi perfil", new PanelPerfil(club, usuario));

        if (usuario instanceof Administrador) {
            agregar("Equipos", new PanelEquipos(club, usuario));
            agregar("Competencias", new PanelCompetencias(club, usuario));
            agregar("Instalaciones", new PanelInstalaciones(club, usuario));
            agregar("Inventario", new PanelInventario(club, usuario));
            agregar("Sugerencias", new PanelSugerencias(club, usuario));
            agregar("Turnos", new PanelTurnos(club, usuario));
            agregar("Reportes", new PanelReportes(club, usuario));
        }
        if (usuario instanceof Entrenador) {
            agregar("Partidos internos", new PanelPartidosInternos(club, usuario));
            agregar("Competencias", new PanelCompetencias(club, usuario));
            agregar("Sesiones", new PanelSesiones(club, usuario));
        }
        if (usuario instanceof Empleado) {
            agregar("Desempeno", new PanelDesempeno(club, usuario));
            agregar("Mis turnos", new PanelTurnos(club, usuario));
            agregar("Sugerencias", new PanelSugerencias(club, usuario));
            agregar("Tienda", new PanelTienda(club, usuario));
        }
        if (usuario instanceof Socio) {
            agregar("Reservas", new PanelReservas(club, usuario));
            agregar("Mis equipos", new PanelMisEquipos(club, usuario));
            agregar("Disciplinas", new PanelPracticas(club, usuario));
            agregar("Entrenamientos", new PanelMisEntrenamientos(club, usuario));
            agregar("Tienda", new PanelTienda(club, usuario));
            agregar("Cafeteria", new PanelCafeteria(club, usuario));
            agregar("Mis compras", new PanelMisCompras(club, usuario));
        }
        agregar("Graficas", new PanelGraficas(club, usuario));
    }

    private void agregar(String titulo, PanelClub panel) {
        pestanas.addTab(titulo, panel);
    }

    private void actualizarPestanaActual() {
        if (pestanas.getSelectedComponent() instanceof PanelClub) {
            ((PanelClub) pestanas.getSelectedComponent()).actualizar();
        }
    }

    private JPanel encabezado(Usuario usuario) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(UI.COLOR_PRIMARIO);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel nombre = new JLabel("Club Deportivo Seneca");
        nombre.setFont(new Font("SansSerif", Font.BOLD, 18));
        nombre.setForeground(Color.WHITE);

        JLabel sesion = new JLabel(usuario.getNombre() + " " + usuario.getApellido() + " · " + rol(usuario) + "   ");
        sesion.setForeground(Color.WHITE);

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setOpaque(false);
        derecha.add(sesion, BorderLayout.CENTER);
        derecha.add(UI.boton("Cerrar sesion", e -> cerrarSesion()), BorderLayout.EAST);

        barra.add(nombre, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private void cerrarSesion() {
        club.guardar();
        dispose();
        new VentanaLogin(club).setVisible(true);
    }

    public static String rol(Usuario usuario) {
        if (usuario instanceof Administrador) {
            return "Administrador";
        }
        if (usuario instanceof Entrenador) {
            return "Entrenador";
        }
        if (usuario instanceof Empleado) {
            return "Fisioterapeuta";
        }
        return "Socio";
    }
}
