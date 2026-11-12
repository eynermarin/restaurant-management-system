package restaurante.atencion;

import java.math.BigDecimal;
import java.util.ArrayList;

import restaurante.atencion.model.ModalidadCuenta;
import restaurante.atencion.service.ServicioAtencion;
import restaurante.atencion.exception.PagoInvalidoException;
import restaurante.menu.control.ControladorMenu;
import restaurante.menu.model.OpcionMenu;

public class PruebaPagos {

    public static void main(String[] args) {

        ControladorMenu menu = new ControladorMenu();
        ServicioAtencion servicio =
            new ServicioAtencion(menu.getConsultaMenu());

        OpcionMenu opcion = servicio.obtenerMenu().get(0);

        if (opcion.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AssertionError(
                "Se necesita un producto con precio positivo."
            );
        }

        ArrayList<String> clientes = new ArrayList<>();
        clientes.add("Carlos");
        clientes.add("Maria");

        servicio.abrirVisita(1, "Daniel", clientes);

        servicio.agregarPedido(1, "Carlos", opcion.getId(), 2);
        servicio.agregarPedido(1, "Maria", opcion.getId(), 1);

        BigDecimal precio = opcion.getPrecio();
        BigDecimal total = precio.multiply(new BigDecimal("3"));

        if (servicio.obtenerTotal(1).compareTo(total) != 0) {
            throw new AssertionError("El total de la mesa es incorrecto.");
        }

        servicio.solicitarCuenta(1, ModalidadCuenta.INDIVIDUAL);

        BigDecimal saldoAnterior = servicio.obtenerSaldo(1);

        try {
            servicio.registrarPago(
                1, "Carlos", "TARJETA",
                saldoAnterior.add(BigDecimal.ONE), "AUT-001"
            );

            throw new AssertionError(
                "Se acepto un pago mayor al saldo."
            );

        } catch (PagoInvalidoException e) {
            System.out.println("Pago excesivo rechazado correctamente.");
        }

        if (servicio.obtenerSaldo(1).compareTo(saldoAnterior) != 0) {
            throw new AssertionError(
                "El saldo cambio despues del pago rechazado."
            );
        }

        try {
            servicio.registrarPago(
                1, "Carlos", "TARJETA",
                precio, ""
            );

            throw new AssertionError(
                "Se acepto tarjeta sin referencia."
            );

        } catch (PagoInvalidoException e) {
            System.out.println("Tarjeta sin referencia rechazada.");
        }

        servicio.registrarPago(
            1, "Carlos", "TARJETA",
            precio.multiply(new BigDecimal("2")), "AUT-002"
        );

        servicio.registrarPago(
            1, "Maria", "EFECTIVO", precio, ""
        );

        if (servicio.obtenerSaldo(1).compareTo(BigDecimal.ZERO) != 0) {
            throw new AssertionError(
                "La cuenta no quedo completamente pagada."
            );
        }

        servicio.cerrarVisita(1);

        if (!servicio.buscarMesa(1).estaDisponible()) {
            throw new AssertionError("La mesa no fue liberada.");
        }

        System.out.println("Total cobrado: " + total);
        System.out.println("Saldo pendiente: " + servicio.obtenerSaldo(1));
        System.out.println("PRUEBA DE PAGOS EXITOSA");
    }
}