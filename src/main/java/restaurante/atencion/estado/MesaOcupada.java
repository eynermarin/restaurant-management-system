
package restaurante.atencion.estado;

import java.util.ArrayList;
import restaurante.atencion.model.*;
import restaurante.atencion.exception.MesaNoDisponibleException;

public class MesaOcupada implements EstadoMesa {

    @Override
    public void abrirVisita(Mesa mesa, String mesero,
                            ArrayList<String> alias) {
        throw new MesaNoDisponibleException(
            "La mesa ya se encuentra ocupada."
        );
    }

    @Override
    public void agregarPedido(Mesa mesa, ItemPedido pedido) {
        mesa.getVisitaActual().agregarPedido(pedido);
    }

    @Override
    public void solicitarCuenta(Mesa mesa, ModalidadCuenta modalidad) {
        mesa.getVisitaActual().solicitarCuenta(modalidad);
        mesa.cambiarEstado(new MesaPendientePago());
    }

    @Override
    public void registrarPago(Mesa mesa, Pago pago) {
        throw new IllegalStateException(
            "Primero debe solicitar la cuenta."
        );
    }

    @Override
    public Visita cerrarVisita(Mesa mesa) {
        throw new IllegalStateException(
            "Primero debe solicitar y pagar la cuenta."
        );
    }

    @Override
    public String getNombre() {
        return "OCUPADA";
    }
}
