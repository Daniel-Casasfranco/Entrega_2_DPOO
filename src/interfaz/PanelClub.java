package interfaz;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import modelo.Club;
import modelo.Usuario;

/**
 * Base de las pestanas de la ventana principal. Cada pestana conoce el club y el
 * usuario que inicio sesion, y vuelve a leer sus datos cada vez que se muestra.
 */
public abstract class PanelClub extends JPanel {

    private static final long serialVersionUID = 1L;

    protected final Club club;
    protected final Usuario usuario;

    protected PanelClub(Club club, Usuario usuario) {
        super(new BorderLayout(10, 10));
        this.club = club;
        this.usuario = usuario;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    }

    /** Vuelve a cargar tablas y combos desde el club. */
    public abstract void actualizar();
}
