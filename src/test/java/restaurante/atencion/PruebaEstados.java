package restaurante.atencion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import restaurante.atencion.estado.*;
import restaurante.atencion.model.*;
import restaurante.atencion.service.ServicioAtencion;
import restaurante.menu.control.ControladorMenu;
import restaurante.menu.model.OpcionMenu;

public class PruebaEstados {

    private static int correctas = 0;

    public static void main(String[] args) {

        Mesa mesa = new Mesa(1);

        verificar("Estado inicial disponible",
            mesa.estaDisponible());

        rechazar("No pasar a ocupada sin visita",
            () -> mesa.cambiarEstado(new MesaOcupada()));

        rechazar("No pasar directamente a pendiente de pago",
            () -> mesa.cambiarEstado(new MesaPendientePago()));

        verificar("La mesa sigue disponible",
            mesa.estaDisponible());

        // Abrir una visita por el procedimiento normal
        ControladorMenu menu = new ControladorMenu();

        ServicioAtencion servicio = new ServicioAtencion(
            menu.getConsultaMenu()
        );

        servicio.abrirVisita(
            1, "Carlos",
            new ArrayList<>(Arrays.asList("Juan"))
        );

        Mesa activa = servicio.buscarMesa(1);

        verificar("Mesa ocupada despues de abrir",
            "OCUPADA".equals(activa.getEstado()));

        rechazar("No liberar una mesa ocupada",
            () -> activa.cambiarEstado(new MesaDisponible()));

        rechazar("No solicitar cambio de estado sin cuenta",
            () -> activa.cambiarEstado(new MesaPendientePago()));

        verificar("Mesa conserva su visita",
            activa.getVisitaActual() != null
                && "OCUPADA".equals(activa.getEstado()));

        // Agregar un producto y solicitar la cuenta
        ArrayList<OpcionMenu> productos = servicio.obtenerMenu();

        if (productos.isEmpty()) {
            throw new AssertionError("No hay productos disponibles.");
        }

        OpcionMenu producto = productos.get(0);

        BigDecimal precio = producto.getPrecio();

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AssertionError("Se necesita un producto con precio positivo.");
        }

        servicio.agregarPedido(
            1, "Juan", producto.getId(), 1
        );

        servicio.solicitarCuenta(
            1, ModalidadCuenta.CONJUNTA
        );

        verificar("Mesa pendiente de pago",
            "PENDIENTE DE PAGO".equals(activa.getEstado()));

        rechazar("No liberar mientras haya visita activa",
            () -> activa.cambiarEstado(new MesaDisponible()));

        rechazar("No finalizar una visita sin pagar",
            () -> activa.finalizarVisita());

        verificar("El saldo sigue pendiente",
            servicio.obtenerSaldo(1).compareTo(precio) == 0);

        // Pagar y cerrar de manera correcta
        servicio.registrarPago(
            1, null, "EFECTIVO", precio, ""
        );

        servicio.cerrarVisita(1);

        verificar("Mesa disponible despues del cierre",
            activa.estaDisponible());

        verificar("Visita almacenada en historial",
            servicio.getHistorial().size() == 1);

        System.out.println();
        System.out.println(
            "RESULTADO: " + correctas +
            " comprobaciones correctas."
        );
    }

    private static void verificar(
        String descripcion, boolean condicion
    ) {
        if (!condicion) {
            throw new AssertionError("FALLO: " + descripcion);
        }

        correctas++;
        System.out.println("OK - " + descripcion);
    }

    private static void rechazar(
        String descripcion, Runnable operacion
    ) {
        try {
            operacion.run();
        } catch (IllegalStateException e) {
            correctas++;
            System.out.println("OK - " + descripcion);
            return;
        }

        throw new AssertionError(
            "FALLO: " + descripcion +
            " (se permitio una operacion invalida)"
        );
    }
}