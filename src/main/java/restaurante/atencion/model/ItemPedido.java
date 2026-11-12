
package restaurante.atencion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ItemPedido {

    private final int id;
    private final String aliasComensal;
    private final int idOpcion;
    private final String nombreOpcion;
    private final String categoria;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final LocalDateTime fechaPedido;

    private boolean anulado;
    private String motivoAnulacion;
    private LocalDateTime fechaAnulacion;

    public ItemPedido(int id, String aliasComensal,
            int idOpcion, String nombreOpcion,
            String categoria, int cantidad,
            BigDecimal precioUnitario) {

        if (id <= 0 || idOpcion <= 0 || cantidad <= 0) {
            throw new IllegalArgumentException(
                "El identificador y la cantidad deben ser positivos."
            );
        }

        if (aliasComensal == null || aliasComensal.trim().isEmpty()
                || nombreOpcion == null || nombreOpcion.trim().isEmpty()
                || categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Los datos del pedido no pueden estar vacios."
            );
        }

        if (precioUnitario == null
                || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "El precio no puede ser negativo."
            );
        }

        this.id = id;
        this.aliasComensal = aliasComensal.trim();
        this.idOpcion = idOpcion;
        this.nombreOpcion = nombreOpcion.trim();
        this.categoria = categoria.trim();
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.fechaPedido = LocalDateTime.now();

        this.anulado = false;
    }

    public BigDecimal calcularSubtotal() {
        if (anulado) {
            return BigDecimal.ZERO;
        }

        return precioUnitario.multiply(
            BigDecimal.valueOf(cantidad)
        );
    }

    void anular(String motivo) {
        if (anulado) {
            throw new IllegalStateException(
                "Este articulo ya fue anulado."
            );
        }

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Debe indicar un motivo de anulacion."
            );
        }

        this.anulado = true;
        this.motivoAnulacion = motivo.trim();
        this.fechaAnulacion = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getAliasComensal() {
        return aliasComensal;
    }

    public int getIdOpcion() {
        return idOpcion;
    }

    public String getNombreOpcion() {
        return nombreOpcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public boolean isAnulado() {
        return anulado;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }
}
