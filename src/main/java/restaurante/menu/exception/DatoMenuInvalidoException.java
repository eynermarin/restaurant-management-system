package restaurante.menu.exception;

/**
 * Se lanza cuando un dato del menú o de una consulta no es válido: nombre vacío
 * o repetido, calorías o precio negativos, rango mal formado, opción inexistente.
 * La lanza el modelo o el servicio ANTES de modificar nada, así que el catálogo
 * queda igual que antes del intento. La vista solo muestra el mensaje.
 */
public class DatoMenuInvalidoException extends Exception {

    public DatoMenuInvalidoException(String mensaje) {
        super(mensaje);
    }
}
