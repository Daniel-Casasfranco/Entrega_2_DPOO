package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import modelo.Club;
import modelo.Usuario;

/** Ventana de inicio de sesion (RF-43). */
public class VentanaLogin extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Club club;
    private final JTextField campoLogin = new JTextField(16);
    private final JPasswordField campoClave = new JPasswordField(16);

    public VentanaLogin(Club club) {
        super("Club Deportivo Seneca - Iniciar sesion");
        this.club = club;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel encabezado = new JLabel("Club Deportivo Seneca", SwingConstants.CENTER);
        encabezado.setFont(new Font("SansSerif", Font.BOLD, 22));
        encabezado.setForeground(Color.WHITE);
        encabezado.setOpaque(true);
        encabezado.setBackground(UI.COLOR_PRIMARIO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JButton ingresar = UI.boton("Ingresar", e -> ingresar());
        getRootPane().setDefaultButton(ingresar);

        JPanel formulario = UI.formulario("Usuario:", campoLogin, "Clave:", campoClave, ingresar);
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));

        JLabel ayuda = new JLabel("<html><small>Usuarios de prueba: admin / admin123, carlos / 1234 (entrenador),"
                + "<br>mateo / 1234 (fisioterapeuta), sofia / 1234 (socia)</small></html>", SwingConstants.CENTER);
        ayuda.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));

        getContentPane().setLayout(new BorderLayout());
        add(encabezado, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(ayuda, BorderLayout.SOUTH);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        Usuario usuario = club.iniciarSesion(campoLogin.getText().trim(), new String(campoClave.getPassword()));
        if (usuario == null) {
            UI.error(this, "Usuario o clave incorrectos");
            campoClave.setText("");
            return;
        }
        dispose();
        new VentanaPrincipal(club, usuario).setVisible(true);
    }
}
