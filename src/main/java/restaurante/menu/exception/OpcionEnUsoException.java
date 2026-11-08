package restaurante.menu.exception;

/**
 * Se lanza al intentar eliminar una opción que ya aparece en pedidos.
 * La lanza CatalogoMenu tras consultar al VerificadorUsoOpcion; la opción no se
 * borra, para no romper el historial. El usuario puede desactivarla en su lugar.
 */
public class OpcionEnUsoException extends Exception {

    public OpcionEnUsoException(String mensaje) {
        super(mensaje);
    }
}
