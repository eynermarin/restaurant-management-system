package restaurante.atencion.pago;

import restaurante.atencion.exception.PagoInvalidoException;

public class PagoTarjeta extends MetodoPago {

    public PagoTarjeta() {
        super("TARJETA");
    }

    @Override
    public String generarReferencia(String referencia) {
        if (referencia == null || referencia.trim().isEmpty()) {
            throw new PagoInvalidoException(
                "Debe ingresar una referencia para el pago con tarjeta."
            );
        }

        return referencia.trim();
    }
}