
package restaurante.atencion.estado;

import java.util.ArrayList;
import restaurante.atencion.model.*;

public class MesaDisponible implements EstadoMesa {

    @Override
    public void abrirVisita(Mesa mesa, String mesero,
                            ArrayList<String> alias) {
        Visita visita = new Visita(mesero, alias);
        mesa.iniciarVisita(visita);
        mesa.cambiarEstado(new MesaOcupada());
    }

    @Override
    public void agregarPedido(Mesa mesa, ItemPedido pedido) {
        throw new IllegalStateException(
            "No se pueden agregar pedidos a una mesa disponible."
        );
    }

    @Override
    public void solicitarCuenta(Mesa mesa, ModalidadCuenta modalidad) {
        throw new IllegalStateException(
            "No hay una visita activa para solicitar la cuenta."
        );
    }

    @Override
    public void registrarPago(Mesa mesa, Pago pago) {
        throw new IllegalStateException(
            "No se puede pagar una mesa disponible."
        );
    }

    @Override
    public Visita cerrarVisita(Mesa mesa) {
        throw new IllegalStateException(
            "No hay una visita que cerrar."
        );
    }

    @Override
    public String getNombre() {
        return "DISPONIBLE";
    }
}
