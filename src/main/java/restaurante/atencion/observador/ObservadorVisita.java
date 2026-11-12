
package restaurante.atencion.observador;

import restaurante.atencion.model.*;

public interface ObservadorVisita {

    void pedidoRegistrado(int numeroMesa, ItemPedido pedido);

    void pagoRegistrado(int numeroMesa, Pago pago);

    void visitaCerrada(int numeroMesa, Visita visita);
}
