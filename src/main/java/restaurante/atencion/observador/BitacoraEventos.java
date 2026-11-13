
package restaurante.atencion.observador;

import java.util.ArrayList;
import java.time.LocalDateTime;
import restaurante.atencion.model.*;

public class BitacoraEventos implements ObservadorVisita {

    private final ArrayList<String> eventos;

    public BitacoraEventos() {
        eventos = new ArrayList<>();
    }

    private void agregarEvento(String mensaje) {
        String hora = LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        );
        eventos.add(hora + " - " + mensaje);
    }

    @Override
    public void pedidoRegistrado(int numeroMesa, ItemPedido pedido) {
        agregarEvento(
            "Mesa " + numeroMesa +
            ": pedido registrado - " + pedido.getNombreOpcion()
        );
    }

    @Override
    public void pagoRegistrado(int numeroMesa, Pago pago) {
        agregarEvento(
            "Mesa " + numeroMesa +
            ": pago registrado - " + pago.getMonto()
        );
    }

    @Override
    public void visitaCerrada(int numeroMesa, Visita visita) {
        agregarEvento(
            "Mesa " + numeroMesa + ": visita cerrada."
        );
    }

    public ArrayList<String> getEventos() {
        return new ArrayList<>(eventos);
    }

    public String obtenerTexto() {

        StringBuilder texto = new StringBuilder();

        for (String evento : eventos) {
            texto.append(evento).append("\n");
        }

        return texto.toString();
    }
}
