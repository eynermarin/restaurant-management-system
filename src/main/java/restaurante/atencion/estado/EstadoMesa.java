
package restaurante.atencion.estado;

import restaurante.atencion.model.*;
import java.util.ArrayList;

public interface EstadoMesa {

    void abrirVisita(Mesa mesa, String mesero,
                     ArrayList<String> alias);

    void agregarPedido(Mesa mesa, ItemPedido pedido);

    void solicitarCuenta(Mesa mesa,
                         ModalidadCuenta modalidad);

    void registrarPago(Mesa mesa, Pago pago);

    Visita cerrarVisita(Mesa mesa);

    String getNombre();
}
