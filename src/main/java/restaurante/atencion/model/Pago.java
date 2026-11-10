
package restaurante.atencion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {

    private final int id;
    private final String metodo;
    private final BigDecimal monto;
    private final LocalDateTime fechaPago;
    private final String referencia;
    private final String aliasComensal;

    public Pago(int id, String metodo, BigDecimal monto,
                String referencia, String aliasComensal) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                "El identificador del pago debe ser positivo."
            );
        }

        if (metodo == null || metodo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Debe indicar un metodo de pago."
            );
        }

        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "El monto del pago debe ser positivo."
            );
        }

        if (referencia == null || referencia.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El pago debe tener una referencia."
            );
        }

        this.id = id;
        this.metodo = metodo.trim();
        this.monto = monto;
        this.referencia = referencia.trim();
        this.aliasComensal = aliasComensal == null
                ? null : aliasComensal.trim();
        this.fechaPago = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getMetodo() {
        return metodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getAliasComensal() {
        return aliasComensal;
    }

    @Override
    public String toString() {
        return metodo + " - " + monto.toPlainString();
    }
}
