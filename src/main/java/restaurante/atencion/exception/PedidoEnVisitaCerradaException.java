package restaurante.atencion.exception;

public class PedidoEnVisitaCerradaException extends RuntimeException {

    public PedidoEnVisitaCerradaException(String mensaje) {
        super(mensaje);
    }
}