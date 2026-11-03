package consola;

import modelo.ReglaNegocioException;

public class Salida {

    public static void titulo(String texto) {
        System.out.println("==================================================");
        System.out.println(texto);
        System.out.println("==================================================");
    }

    public static void seccion(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    }

    public static void accion(String texto) {
        System.out.println();
        System.out.println("Accion: " + texto);
    }

    public static void ok() {
        System.out.println("  Resultado: [OK]");
    }

    public static void inesperado(ReglaNegocioException e) {
        System.out.println("  Resultado: [ERROR INESPERADO: " + e.getMessage() + "]");
    }

    public static void rechazado(ReglaNegocioException e) {
        System.out.println("  Resultado: [RECHAZADO esperado: " + e.getMessage() + "]");
    }

    public static void debiaRechazarse() {
        System.out.println("  Resultado: [ERROR: se acepto y debia rechazarse]");
    }

    public static void linea(String texto) {
        System.out.println("  " + texto);
    }
    
    public static void esperaOk(String texto, Runnable accion) {
        accion(texto);
        try {
            accion.run();
            ok();
        } catch (ReglaNegocioException e) {
            inesperado(e);
        }
    }

    public static void esperaRechazo(String texto, Runnable accion) {
        accion(texto);
        try {
            accion.run();
            debiaRechazarse();
        } catch (ReglaNegocioException e) {
            rechazado(e);
        }
    }
}


