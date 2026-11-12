package restaurante.atencion.exception;

public class PagoInvalidoException extends RuntimeException {

    public PagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}