package restaurante.atencion.exception;

public class MesaNoDisponibleException extends RuntimeException {

    public MesaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
