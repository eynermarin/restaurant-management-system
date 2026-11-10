
package restaurante.atencion.estado;

import java.util.ArrayList;
import restaurante.atencion.model.*;
import restaurante.atencion.exception.MesaNoDisponibleException;

public class MesaPendientePago implements EstadoMesa {

    @Override
    public void abrirVisita(Mesa mesa, String mesero,
                            ArrayList<String> alias) {
        throw new MesaNoDisponibleException(
            "La mesa tiene una cuenta pendiente de pago."
        );
    }

    @Override
    public void agregarPedido(Mesa mesa, ItemPedido pedido) {
        throw new IllegalStateException(
            "No se aceptan pedidos despues de solicitar la cuenta."
        );
    }

    @Override
    public void solicitarCuenta(Mesa mesa, ModalidadCuenta modalidad) {
        throw new IllegalStateException(
            "La cuenta ya fue solicitada."
        );
    }

    @Override
    public void registrarPago(Mesa mesa, Pago pago) {
        mesa.getVisitaActual().registrarPago(pago);
    }

    @Override
    public Visita cerrarVisita(Mesa mesa) {

        Visita visita = mesa.getVisitaActual();

        visita.cerrarVisita();

        mesa.finalizarVisita();
        mesa.cambiarEstado(new MesaDisponible());

        return visita;
    }

    @Override
    public String getNombre() {
        return "PENDIENTE DE PAGO";
    }
}
