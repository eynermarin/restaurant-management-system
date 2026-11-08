package restaurante.menu.exception;

/**
 * Se lanza cuando no existe ninguna combinación completa que respete el límite
 * de calorías, o cuando una categoría requerida no tiene opciones disponibles.
 * La lanza GeneradorCombinaciones; el mensaje explica la causa para que el
 * usuario cambie X o active/desactive categorías. No modifica ningún dato.
 */
public class SinCombinacionesException extends Exception {

    public SinCombinacionesException(String mensaje) {
        super(mensaje);
    }
}
