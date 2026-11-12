package restaurante.atencion;

import java.math.BigDecimal;
import java.util.ArrayList;

import restaurante.atencion.model.*;
import restaurante.atencion.service.ServicioAtencion;
import restaurante.menu.control.ControladorMenu;
import restaurante.menu.model.OpcionMenu;

public class PruebaServicio {

    public static void main(String[] args) {

        ControladorMenu controladorMenu = new ControladorMenu();

        ServicioAtencion servicio = new ServicioAtencion(
            controladorMenu.getConsultaMenu()
        );

        controladorMenu.setVerificadorUso(servicio);

        ArrayList<OpcionMenu> opciones = servicio.obtenerMenu();

        if (opciones.isEmpty()) {
            throw new AssertionError("El menu esta vacio.");
        }

        OpcionMenu opcion = opciones.get(0);

        ArrayList<String> clientes = new ArrayList<>();
        clientes.add("Carlos");
        clientes.add("Maria");

        servicio.abrirVisita(1, "Daniel", clientes);

        servicio.agregarPedido(1, "Carlos", opcion.getId(), 2);

        BigDecimal total = servicio.obtenerTotal(1);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new AssertionError("Total invalido.");
        }

        if (!servicio.opcionUsadaEnPedidos(opcion.getId())) {
            throw new AssertionError("No se registro el producto usado.");
        }

        servicio.solicitarCuenta(1, ModalidadCuenta.CONJUNTA);

        if (total.compareTo(BigDecimal.ZERO) > 0) {
            servicio.registrarPago(
                1, null, "EFECTIVO", total, "EF-001"
            );
        }

        servicio.cerrarVisita(1);

        if (!servicio.buscarMesa(1).estaDisponible()) {
            throw new AssertionError("La mesa no fue liberada.");
        }

        if (servicio.getHistorial().size() != 1) {
            throw new AssertionError("No se guardo la visita cerrada.");
        }

        System.out.println("Producto: " + opcion.getNombre());
        System.out.println("Total cobrado: " + total);
        System.out.println("Mesa disponible: " +
            servicio.buscarMesa(1).estaDisponible());
        System.out.println("PRUEBA DE INTEGRACION EXITOSA");
    }
}