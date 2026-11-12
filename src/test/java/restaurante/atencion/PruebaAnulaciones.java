package restaurante.atencion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import restaurante.atencion.model.*;
import restaurante.atencion.service.ServicioAtencion;
import restaurante.menu.control.ControladorMenu;
import restaurante.menu.model.OpcionMenu;

public class PruebaAnulaciones {

    private static int correctas = 0;

    public static void main(String[] args) {

        ControladorMenu menu = new ControladorMenu();

        ServicioAtencion servicio = new ServicioAtencion(
            menu.getConsultaMenu()
        );

        ArrayList<OpcionMenu> opciones = servicio.obtenerMenu();

        if (opciones.isEmpty()) {
            throw new AssertionError("No hay productos disponibles.");
        }

        OpcionMenu producto = opciones.get(0);

        BigDecimal precio = producto.getPrecio();

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AssertionError("El producto no tiene precio positivo.");
        }

        servicio.abrirVisita(
            1, "Carlos",
            new ArrayList<>(Arrays.asList("Juan", "Pedro"))
        );

        // Primer pedido: una unidad
        servicio.agregarPedido(1, "Juan", producto.getId(), 1);

        // Segundo pedido: dos unidades
        servicio.agregarPedido(1, "Juan", producto.getId(), 2);

        Visita visita = servicio.buscarMesa(1).getVisitaActual();

        verificar("Dos pedidos registrados",
            visita.getPedidos().size() == 2);

        BigDecimal esperado = precio.multiply(
            BigDecimal.valueOf(3)
        );

        verificar("Total de tres unidades",
            visita.calcularTotal().compareTo(esperado) == 0);

        // Anular solamente el primer pedido
        int idPrimero = visita.getPedidos().get(0).getId();

        servicio.anularPedido(
            1, idPrimero, "Cliente cambio de opinion"
        );

        verificar("Primer pedido marcado como anulado",
            visita.getPedidos().get(0).isAnulado());

        verificar("El pedido anulado se conserva",
            visita.getPedidos().size() == 2);

        BigDecimal nuevoTotal = precio.multiply(
            BigDecimal.valueOf(2)
        );

        verificar("El pedido anulado deja de sumar",
            servicio.obtenerTotal(1).compareTo(nuevoTotal) == 0);

        verificar("Subtotal del comensal correcto",
            visita.subtotalComensal("Juan")
                .compareTo(nuevoTotal) == 0);

        verificar("La opcion sigue identificada como usada",
            servicio.opcionUsadaEnPedidos(producto.getId()));

        // Cobrar y cerrar
        servicio.solicitarCuenta(
            1, ModalidadCuenta.CONJUNTA
        );

        servicio.registrarPago(
            1, null, "EFECTIVO", nuevoTotal, ""
        );

        servicio.cerrarVisita(1);

        verificar("Visita guardada en historial",
            servicio.getHistorial().size() == 1);

        Visita historica = servicio.getHistorial().get(0);

        verificar("Historial conserva los dos pedidos",
            historica.getPedidos().size() == 2);

        verificar("Historial conserva la anulacion",
            historica.getPedidos().get(0).isAnulado());

        verificar("Mesa disponible nuevamente",
            servicio.buscarMesa(1).estaDisponible());

        verificar("Opcion usada sigue identificada tras el cierre",
            servicio.opcionUsadaEnPedidos(producto.getId()));

        // Reutilizar la mesa
        servicio.abrirVisita(
            1, "Ana",
            new ArrayList<>(Arrays.asList("Maria"))
        );

        verificar("Mesa puede abrir una nueva visita",
            !servicio.buscarMesa(1).estaDisponible());

        verificar("Historial anterior permanece intacto",
            servicio.getHistorial().size() == 1);

        System.out.println();
        System.out.println(
            "RESULTADO: " + correctas +
            " comprobaciones correctas."
        );
    }

    private static void verificar(
        String nombre, boolean condicion
    ) {
        if (!condicion) {
            throw new AssertionError("FALLO: " + nombre);
        }

        correctas++;
        System.out.println("OK - " + nombre);
    }
}