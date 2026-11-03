package persistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import modelo.Club;

public class Persistencia {

    private static final String ARCHIVO = "club.dat";

    private String carpeta;

    public Persistencia(String carpeta) {
        this.carpeta = carpeta;
    }

    public boolean existe() {
        return new File(carpeta, ARCHIVO).exists();
    }

    public void guardar(Club club) {
        File dir = new File(carpeta);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (ObjectOutputStream salida = new ObjectOutputStream(
                new FileOutputStream(new File(dir, ARCHIVO)))) {
            salida.writeObject(club);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el club: " + e.getMessage());
        }
    }

    public Club cargar() {
        Club club;
        if (!existe()) {
            club = new Club();
        } else {
            try (ObjectInputStream entrada = new ObjectInputStream(
                    new FileInputStream(new File(carpeta, ARCHIVO)))) {
                club = (Club) entrada.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("No se pudo cargar el club: " + e.getMessage());
            }
        }
        club.setPersistencia(this);
        return club;
    }
}
