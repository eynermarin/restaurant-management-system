package restaurante.atencion.pago;

import java.math.BigDecimal;
import restaurante.atencion.exception.PagoInvalidoException;

public abstract class MetodoPago {

    private final String nombre;

    protected MetodoPago(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void validar(BigDecimal monto, BigDecimal saldo) {
        if (monto == null || saldo == null
                || monto.compareTo(BigDecimal.ZERO) <= 0
                || monto.compareTo(saldo) > 0) {
            throw new PagoInvalidoException(
                "El monto debe ser positivo y no superar el saldo."
            );
        }
    }

    public abstract String generarReferencia(String referencia);
}