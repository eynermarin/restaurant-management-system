package restaurante.atencion;

import java.math.BigDecimal;
import java.util.ArrayList;

import restaurante.atencion.model.ModalidadCuenta;
import restaurante.atencion.observador.BitacoraEventos;
import restaurante.atencion.service.ServicioAtencion;
import restaurante.menu.control.ControladorMenu;
import restaurante.menu.model.OpcionMenu;

public class PruebaObserver {

    public static void main(String[] args) {

        ControladorMenu menu = new ControladorMenu();
        ServicioAtencion servicio =
            new ServicioAtencion(menu.getConsultaMenu());

        BitacoraEventos bitacora = new BitacoraEventos();
        servicio.agregarObservador(bitacora);

        ArrayList<String> clientes = new ArrayList<>();
        clientes.add("Carlos");

        servicio.abrirVisita(1, "Daniel", clientes);

        OpcionMenu opcion = servicio.obtenerMenu().get(0);

        servicio.agregarPedido(1, "Carlos", opcion.getId(), 1);
        servicio.solicitarCuenta(1, ModalidadCuenta.CONJUNTA);

        BigDecimal total = servicio.obtenerTotal(1);

        if (total.compareTo(BigDecimal.ZERO) > 0) {
            servicio.registrarPago(
                1, null, "EFECTIVO", total, "EF-001"
            );
        }

        servicio.cerrarVisita(1);

        System.out.println(bitacora.obtenerTexto());

        int esperados = total.signum() > 0 ? 3 : 2;

        if (bitacora.getEventos().size() != esperados) {
            throw new AssertionError(
                "No se registraron todos los eventos."
            );
        }

        System.out.println("PRUEBA OBSERVER EXITOSA");
    }
}